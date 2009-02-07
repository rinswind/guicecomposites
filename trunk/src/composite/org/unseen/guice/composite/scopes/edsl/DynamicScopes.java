package org.unseen.guice.composite.scopes.edsl;


import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class DynamicScopes {
  private DynamicScopes() {
  }
  
  public static <T> DynamicScopesAnnotatedBindingBuilder<T> bind(Binder binder, Class<T> key) {
    return new DynamicScopesAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key), binder);
  }

  public static <T> DynamicScopesAnnotatedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> key) {
    return new DynamicScopesAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key), binder);
  }

  public static <T> DynamicScopesLinkedBindingBuilder<T> bind(Binder binder, Key<T> key) {
    return new DynamicScopesLinkedBindingBuilderImpl<T>(key, binder.bind(key), binder);
  }
}
