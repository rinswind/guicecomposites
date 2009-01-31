package org.unseen.guice.composite.injectors;

import java.lang.annotation.Annotation;

public interface CompositeAnnotatedBindingBuilder<T> extends CompositeLinkedBindingBuilder<T> {
  CompositeLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType);

  CompositeLinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
