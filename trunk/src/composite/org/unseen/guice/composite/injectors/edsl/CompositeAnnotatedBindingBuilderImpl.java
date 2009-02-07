package org.unseen.guice.composite.injectors.edsl;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;

public class CompositeAnnotatedBindingBuilderImpl<T> extends CompositeLinkedBindingBuilderImpl<T>
    implements CompositeAnnotatedBindingBuilder<T> {

  private final AnnotatedBindingBuilder<T> wrapped;
  
  public CompositeAnnotatedBindingBuilderImpl(Key<T> key, AnnotatedBindingBuilder<T> wrapped) {
    super(key, wrapped);
    this.wrapped = wrapped;
  }
  
  public CompositeLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    wrapped.annotatedWith(annotationType);
    return this;
  }

  public CompositeLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    wrapped.annotatedWith(annotation);
    return this;
  }
}
