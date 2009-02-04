package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;

public class DynamicScopesAnnotatedBindingBuilderImpl<T> extends DynamicScopesLinkedBindingBuilderImpl<T>
    implements DynamicScopesAnnotatedBindingBuilder<T> {

  private final AnnotatedBindingBuilder<T> wrapped;
  
  public DynamicScopesAnnotatedBindingBuilderImpl(Key<T> key, AnnotatedBindingBuilder<T> wrapped, Binder binder) {
    super(key, wrapped, binder);
    this.wrapped = wrapped;
  }
  
  public DynamicScopesLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    wrapped.annotatedWith(annotationType);
    return this;
  }

  public DynamicScopesLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    wrapped.annotatedWith(annotation);
    return this;
  }
}
