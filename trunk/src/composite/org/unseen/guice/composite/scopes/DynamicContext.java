package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @author Todor Boev
 *
 */
public class DynamicContext {
  private static final ThreadLocal<DynamicContext> ACTIVE = new ThreadLocal<DynamicContext>();

  private final Class<? extends Annotation> scope;
  private final DynamicContext parent;
  private final Map<Key<?>, Object> cache = new HashMap<Key<?>, Object>();
  
  private DynamicContext(Class<? extends Annotation> scope, DynamicContext parent) {
    this.scope = scope;
    this.parent = parent;
  }
  
  @Override
  public String toString() {
    return "DynamicContext[ " + scope.getSimpleName() + " ]";
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
      throw new IllegalArgumentException(this + ": " + key + " already cached");
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
    System.out.println(this + ": search(" + scope.getSimpleName() + ", " + key.getTypeLiteral().getRawType().getSimpleName() + ")");
    
    T val = null;
    
    if (this.scope == scope) {
      val = (T) cache.get(key);
      
      if (val == null) { 
        val = unscoped.get();
        
        /* In case of cycles a proxy to val has already been cached. So don't cache it */
        if (!cache.containsKey(key)) {
          cache.put(key, val);
          System.out.println(this + ": created " + key.getTypeLiteral().getRawType().getSimpleName());
        } else {
          System.out.println(this + ": proxy detected " + key.getTypeLiteral().getRawType().getSimpleName());
        }
        
      } else {
        System.out.println(this + ": found " + key.getTypeLiteral().getRawType().getSimpleName());
      }
    }
    else if (parent != null) {
      val = parent.search(key, unscoped, scope);
    } 
    else {
      throw new RuntimeException("No cache level found for " + key + " scoped as " + scope);
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
  public static DynamicContext activate(Class<? extends Annotation> scope, DynamicContext parent) {
    System.out.println("activate(" + scope.getSimpleName() + ", " + parent + ")");
    
    if (ACTIVE.get() != null) { 
      throw new IllegalStateException("DynamicContext is already active in this thread: " + ACTIVE.get());
    }
    
    DynamicContext ctx = new DynamicContext(scope, parent);
    ACTIVE.set(ctx);
    return ctx;
  }
  
  /**
   * Called during a wave of object creation to cache the new objects or to
   * capture the active context into factories of narrower contexts.
   * 
   * @return
   */
  public static DynamicContext active() {
    return ACTIVE.get();
  }
  
  /**
   * Called at the end of a wave of object creation to clear the current context. 
   */
  public static void deactivate() {
    System.out.println("deactivate()");
    ACTIVE.remove();
  }
  
//  /**
//   * Drives a "wave of creation" starting from one unscoped provider and key.
//   * 
//   * @paramw <T>
//   * @param key: What object are we looking for.
//   * @param unscoped: A provider to use if an instance needs to be lazily created
//   *        and cached.
//   * @param scope: In case the current cache is not set this is used to create a
//   *        new scope. Otherwise it is used to find the appropriate cache level
//   *        to look for instances and store a lazily created instance.
//   * @param parent: In case the current cache is not set this is used as a basis
//   *        for a newly created cache.
//   * @return
//   */
//  public static <T, S extends Annotation> T lookup(Key<T> key, Provider<T> unscoped, Class<S> scope) {
//    DynamicContext ctx = ACTIVE.get();
//    boolean owner = ctx == null;
//    if (ctx == null) {
//      ctx = new DynamicContext(scope, PARENT.get());
//      ACTIVE.set(ctx);
//    }
//    
//    try {
//      return ctx.search(key, unscoped, scope);
//    } finally {
//      if (owner) {
//        ACTIVE.remove();
//      }
//    }
//  }
}
