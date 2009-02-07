package org.unseen.guice.composite.scopes;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.CreationException;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 *
 */
public class DynamicScopeInstance {
  private static final ThreadLocal<DynamicScopeInstance> ACTIVE = new ThreadLocal<DynamicScopeInstance>();

  private final Class<? extends Annotation> tag;
  private final DynamicScopeInstance parent;
  private final Map<Key<?>, Object> cache;
  
  private DynamicScopeInstance(Class<? extends Annotation> scope, DynamicScopeInstance parent) {
    this.tag = scope;
    this.parent = parent;
    this.cache = new HashMap<Key<?>, Object>();
  }
  
  @Override
  public String toString() {
    return "DynamicScopeInstance(" + tag.getCanonicalName() + ")";
  }
  
  /**
   * Can be used to pre-load the content of the dynamic context.
   * 
   * @param <T>
   * @param key
   * @param val
   */
  public <T> void put(Key<T> key, T val) {
    if (cache.containsKey(key)) {
      throw new CreationException(Arrays.asList(new Message(key
          + " already ached in scope instance " + tag)));
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
  public <T> T search(Key<T> key, Provider<T> unscoped, Class<? extends Annotation> scope) {
    T val = null;
    
    if (this.tag == scope) {
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
    }
    else if (parent != null) {
      val = parent.search(key, unscoped, scope);
    } 
    else {
      throw new CreationException(asList(new Message("No cache level found for " + key
          + " scoped as " + scope + " and searched in " + this.tag + " and it's parents")));
    }

    return val;
  }

  /**
   * Called to start a new wave of object creation.
   * 
   * @param scope
   * @param parent
   * @return
   */
  public static DynamicScopeInstance activate(Class<? extends Annotation> scope, DynamicScopeInstance parent) {
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
   * Called at the end of a wave of object creation to clear the current context. 
   */
  public static void deactivate() {
    ACTIVE.remove();
  }
}
