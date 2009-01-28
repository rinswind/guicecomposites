package org.unseen.guice.composite.scope;

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
  private static final ThreadLocal<DynamicContext> PARENT = new ThreadLocal<DynamicContext>();
  private static final ThreadLocal<DynamicContext> CURRENT = new ThreadLocal<DynamicContext>();

  /**
   * @param <T>
   * @param key
   * @param unscoped
   * @param scope
   * @return
   */
  public static <T> T lookup(Key<T> key, Provider<T> unscoped, Class<? extends Annotation> scope) {
    DynamicContext ctx = CURRENT.get();
    boolean owner = ctx == null;
    if (ctx == null) {
      ctx = new DynamicContext(scope, PARENT.get());
      CURRENT.set(ctx);
    }
    
    try {
      return ctx.search(key, unscoped, scope);
    } finally {
      if (owner) {
        CURRENT.remove();
      }
    }
  }
  
  private final Class<? extends Annotation> scope;
  private final DynamicContext parent;
  private final Map<Key<?>, Object> cache = new HashMap<Key<?>, Object>();
  
  private DynamicContext(Class<? extends Annotation> scope, DynamicContext parent) {
    this.scope = scope;
    this.parent = parent;
  }
  
  @SuppressWarnings("unchecked")
  private <T> T search(Key<T> key, Provider<T> unscoped, Class<? extends Annotation> scope) {
    T val = null;
    
    if (this.scope == scope) {
      val = (T) cache.get(key);
      
      if (val == null) { 
        val = unscoped.get();
        cache.put(key, val);
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
}
