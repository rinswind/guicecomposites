/**
 * Copyright (C) 2009 Todor Boev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unseen.guice.composite.scopes;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.CreationException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.Message;

/**
 * A cache used to wire a graph of objects placed in the same dynamic scope. A
 * fresh cache is created every time a factory method is called on the dynamic
 * scope factory. The cache lives until the creation process is completed and is
 * than thrown away. The cache can survive the factory method call only if the
 * creation process spawns a dynamic scope factory for a narrower scope.
 * 
 * @author rinsvind@gmail.com (Todor Boev)
 * 
 */
public class DynamicScopeInstance {
  private static final ThreadLocal<DynamicScopeInstance> ACTIVE = new ThreadLocal<DynamicScopeInstance>();

  private final DynamicScope scope;
  private final DynamicScopeInstance parent;
  private final Map<Key<?>, Object> cache;

  private DynamicScopeInstance(DynamicScope scope, DynamicScopeInstance parent) {
    this.scope = scope;
    this.parent = parent;
    this.cache = new HashMap<Key<?>, Object>();
  }

  @Override
  public String toString() {
    return "DynamicScopeInstance(" + scope + ")";
  }

  /**
   * Can be used to pre-load the content of the dynamic scope cache. Called to
   * populate the scope with objects the user has computed in a non-DI way.
   * These objects usually passed as parameters to the factory method that
   * initiates the wiring of a new object graph.
   * 
   * @param <T>
   * @param key
   * @param val
   */
  public <T> void seed(Key<T> key, T val) {
    if (cache.containsKey(key)) {
      throw new CreationException(Arrays.asList(new Message(key + " already seeded in " + this)));
    }
    cache.put(key, val);
  }

  /**
   * Searches for a cached object. If it fails to find it creates a new one and
   * caches it at the appropriate scope level.
   * 
   * @param <T>
   * @param key
   * @param unscoped
   * @param scope
   * @return the value of the key in the current scope instance. Can return null
   *         if we are searching for an optional scope parameter.
   */
  @SuppressWarnings("unchecked")
  public <T> T search(Key<T> key, Provider<T> unscoped, DynamicScope scope) {
    T val = null;

    if (this.scope == scope) {
      /*
       * Must check if the cache contains the key because it might be bound to
       * null. So we can't distinguish a null value from a missing value. The
       * other way to support null parameters would be to box all cached objects
       * in a container that can also be empty
       */
      if (cache.containsKey(key)) {
        val = (T) cache.get(key);
      } else {
        val = unscoped.get();

        /*
         * In case of cycles val would be a proxy. This proxy would be created
         * when the recursion loops into a search() call to this object and
         * Guice detects we try to create val while already the creation of val
         * is in progress. At that point val will be cached by the looped
         * search() call and than returned to us. Here we will cached it again -
         * no harm done.
         */
        cache.put(key, val);
      }
    } else if (parent != null) {
      val = parent.search(key, unscoped, scope);
    } else {
      throw new CreationException(asList(new Message("No cache level found for " + key
          + " scoped as " + scope + " and searched in " + this.scope + " and it's parents")));
    }

    return val;
  }

  /**
   * Called to setup the creation of a new object graph.
   * 
   * @param scope
   * @param parent
   * @return
   */
  public static DynamicScopeInstance activate(DynamicScope scope, DynamicScopeInstance parent) {
    if (ACTIVE.get() != null) {
      throw new CreationException(Arrays.asList(new Message(
          "A dynamic scope instance is already active in this thread: " + ACTIVE.get())));
    }

    DynamicScopeInstance ctx = new DynamicScopeInstance(scope, parent);
    ACTIVE.set(ctx);
    return ctx;
  }

  /**
   * @return
   */
  public static boolean isActive() {
    return ACTIVE.get() != null;
  }

  /**
   * Called during a wave of object creation to cache the new objects or to
   * capture the active context into factories of narrower contexts.
   * 
   * @return
   */
  public static DynamicScopeInstance active() {
    DynamicScopeInstance active = ACTIVE.get();
    if (active == null) {
      throw new CreationException(Arrays.asList(new Message(
          "No dynamic scope instance is active in this thread")));
    }
    return active;
  }

  /**
   * Called at the end of a wave of object creation to clear the current
   * context.
   */
  public static void deactivate() {
    ACTIVE.remove();
  }
}
