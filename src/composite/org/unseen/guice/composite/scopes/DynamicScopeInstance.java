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

  private final Class<? extends Annotation> scope;
  private final DynamicScopeInstance parent;
  private final Map<Key<?>, Object> cache;
  
  private DynamicScopeInstance(Class<? extends Annotation> scope, DynamicScopeInstance parent) {
    this.scope = scope;
    this.parent = parent;
    this.cache = new HashMap<Key<?>, Object>();
  }
  
  @Override
  public String toString() {
    return "DynamicScopeInstance[ " + scope.getSimpleName() + " ]";
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
          + " already ached in scope instance " + scope)));
    }
    cache.put(key, val);
  }
  
  /**
   * Searches for a cached object. If it fails to find it creates a new one and caches it 
   * at the appropriate scope level.
   * 
   * @param <T>
   * @param key
   * @param unscoped
   * @param scope
   * @return
   */
  @SuppressWarnings("unchecked")
  public <T, S extends Annotation> T search(Key<T> key, Provider<T> unscoped, Class<S> scope) {
    T val = null;
    
    if (this.scope == scope) {
      val = (T) cache.get(key);
      
      if (val == null) { 
        val = unscoped.get();
        
        /*
         * In case of cycles a proxy to val has already been cached. So we sould
         * override the proxy with itself - no harm done.
         */
        cache.put(key, val);
      } 
    }
    else if (parent != null) {
      val = parent.search(key, unscoped, scope);
    } 
    else {
      throw new CreationException(asList(new Message("No cache level found for " + key
          + " scoped as " + scope + " and searched in " + this.scope + " and it's parents")));
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
          "A DynamicScopeInstance is already active in this thread: " + ACTIVE.get())));
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
