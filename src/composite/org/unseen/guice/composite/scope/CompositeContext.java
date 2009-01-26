package org.unseen.guice.composite.scope;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @author Todor Boev
 *
 */
public class CompositeContext {
  private static final ThreadLocal<CompositeContext> PARENT = new ThreadLocal<CompositeContext>();
  private static final ThreadLocal<CompositeContext> CURRENT = new ThreadLocal<CompositeContext>() {
    @Override
    protected CompositeContext initialValue() {
      return new CompositeContext(PARENT.get());
    }
  };

  public static <T> T contextualGet(Key<T> key, Provider<T> unscoped) {
    CompositeContext ctx = CURRENT.get();
    boolean owner = ctx.touch();
    try {
      T obj = ctx.get(key, unscoped);
      
//      if (Provider.class.isAssignableFrom(key.type())) {
//        final Key provKey = (Key) key.tag();
//        final Provider prov = (Provider) obj;
//        obj = (T) new Provider() {
//          @Override
//          public Object get() {
//            /*
//             * Continue the creation by opening a new runtime context based at
//             * the context that is active at the time this provider is created.
//             */
//            PARENT.set(ctx);
//            try {
//              return contextualGet(provKey, prov);
//            } finally {
//              PARENT.remove();
//            }
//          }
//        };
//      }
      
      return obj;
    } finally {
      if (owner) {
        ctx.seal();
        CURRENT.remove();
      }
    }
  }
  
  private static int nextNo;
  
  private final int no = nextNo++; 
  
  private final CompositeContext parent;
  private final Map<Key<?>, Object> objects = new HashMap<Key<?>, Object>();
  private boolean touched;
  private boolean sealed;
  
  private CompositeContext(CompositeContext parent) {
    this.parent = parent;
  }
  
  @Override
  public String toString() {
    return no + (parent != null ? "->" + parent.toString() : "");
  }
  
  private <T> T get(Key<T> key, Provider<T> unscoped) {
    T val = search(key);
    
    if (val == null) {
      val = unscoped.get();
      put(key, val);
    }
    
    return val;
  }
  
  private <T> void put(Key<T> key, T val) {
    if (sealed) {
      throw new IllegalStateException("Context is sealed. No more objects can be put in.");
    }
    
    if (objects.containsKey(key)) {
      throw new RuntimeException("Instance of " + key + " already on the context");
    }
    objects.put(key, val);
  }
  
  @SuppressWarnings("unchecked")
  private <T> T search(Key<T> key) {
    T val = (T) objects.get(key);
    if (val != null) {
      return val;
    }
    
    if (parent != null) {
      return parent.search(key);
    }
    
    return null;
  }
  
  private boolean touch() {
    boolean res = !touched;
    touched = true;
    return res;
  }
  
  private void seal() {
    sealed = true;
  }
}
