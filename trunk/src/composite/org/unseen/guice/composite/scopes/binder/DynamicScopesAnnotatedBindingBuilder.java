package org.unseen.guice.composite.scopes.binder;

import java.lang.annotation.Annotation;

public interface DynamicScopesAnnotatedBindingBuilder<T> extends DynamicScopesLinkedBindingBuilder<T> {
  DynamicScopesLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType);

  DynamicScopesLinkedBindingBuilder<T> annotatedWith(Annotation annotation);
}
