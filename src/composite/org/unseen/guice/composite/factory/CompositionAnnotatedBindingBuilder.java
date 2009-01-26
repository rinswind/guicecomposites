package org.unseen.guice.composite.factory;

import java.lang.annotation.Annotation;

public interface CompositionAnnotatedBindingBuilder<T> extends CompositionLinkedBindingBuilder<T> {
  CompositionLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType);

  CompositionLinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
