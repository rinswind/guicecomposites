package org.unseen.guice.composite.factory;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class Composition {
  private Composition() {
  }

  public static <T> CompositionAnnotatedBindingBuilder<T> bind(Binder binder, Class<T> key) {
    return new CompositionAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key));
  }

  public static <T> CompositionAnnotatedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> key) {
    return new CompositionAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key));
  }

  public static <T> CompositionLinkedBindingBuilder<T> bind(Binder binder, Key<T> key) {
    return new CompositionLinkedBindingBuilderImpl<T>(key, binder.bind(key));
  }
}
