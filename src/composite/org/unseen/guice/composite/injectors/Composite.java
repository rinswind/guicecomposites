package org.unseen.guice.composite.injectors;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class Composite {
  private Composite() {
  }

  public static <T> CompositeAnnotatedBindingBuilder<T> bind(Binder binder, Class<T> key) {
    return new CompositeAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key));
  }

  public static <T> CompositeAnnotatedBindingBuilder<T> bind(Binder binder, TypeLiteral<T> key) {
    return new CompositeAnnotatedBindingBuilderImpl<T>(Key.get(key), binder.bind(key));
  }

  public static <T> CompositeLinkedBindingBuilder<T> bind(Binder binder, Key<T> key) {
    return new CompositeLinkedBindingBuilderImpl<T>(key, binder.bind(key));
  }
}
