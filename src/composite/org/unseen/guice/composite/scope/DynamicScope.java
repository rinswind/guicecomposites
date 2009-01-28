package org.unseen.guice.composite.scope;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;

/**
 * @author Todor Boev
 *
 */
public class DynamicScope implements Scope {
  private final Class<? extends Annotation> tag;
  
  public DynamicScope(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
    
    this.tag = tag;
  }
  
  @Override
  public String toString() {
    return "DynamicScope(" + tag + ")";
  }
  
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
//    System.out.printf("%s.scope(%s, %s)\n", this, key, unscoped);
    
//  if (Provider.class.isAssignableFrom(key.type())) {
//  final Key provKey = (Key) key.tag();
//  final Provider prov = (Provider) obj;
//  obj = (T) new Provider() {
//    @Override
//    public Object get() {
//      /*
//       * Continue the creation by opening a new runtime context based at
//       * the context that is active at the time this provider is created.
//       */
//      PARENT.set(ctx);
//      try {
//        return contextualGet(provKey, prov);
//      } finally {
//        PARENT.remove();
//      }
//    }
//  };
//}
    return new Provider<T>() {
      public T get() {
        return DynamicContext.lookup(key, unscoped, tag);
      }
    };
  }
}
