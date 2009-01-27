package org.unseen.guice.composite.factory;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;

public class CompositionAnnotatedBindingBuilderImpl<T> extends CompositionLinkedBindingBuilderImpl<T>
    implements CompositionAnnotatedBindingBuilder<T> {

  private final AnnotatedBindingBuilder<T> wrapped;
  
  public CompositionAnnotatedBindingBuilderImpl(Key<T> key, AnnotatedBindingBuilder<T> wrapped) {
    super(key, wrapped);
    this.wrapped = wrapped;
  }
  
  public CompositionLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    wrapped.annotatedWith(annotationType);
    return this;
  }

  public CompositionLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    wrapped.annotatedWith(annotation);
    return this;
  }
}
