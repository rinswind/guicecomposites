package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.ScopeAnnotation;
import com.google.inject.binder.ScopedBindingBuilder;

public class DynamicScopes {
  private DynamicScopes() {
  }
  
  /**
   * @param <S>
   * @param binder
   * @param tag
   */
  public static void bindScope(Binder binder, Class<? extends Annotation> tag) {
    checkScopeAnnotation(tag);
    binder.bindScope(tag, new DynamicScope(tag));
  }

  /**
   * @param <F>
   * @param binder
   * @param iface
   * @param target
   * @return
   */
  public static <F> ScopedBindingBuilder bindFactory(Binder binder, Class<F> iface, Class<? extends Annotation> target) {
    checkScopeAnnotation(target);
    return binder.bind(iface).toProvider(new DynamicScopeFactoryProvider<F>(iface, target, binder));
  }
  
  /**
   * @param tag
   */
  private static void checkScopeAnnotation(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
  }
}
