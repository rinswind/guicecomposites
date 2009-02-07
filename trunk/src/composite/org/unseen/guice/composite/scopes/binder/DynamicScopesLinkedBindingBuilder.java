package org.unseen.guice.composite.scopes.binder;

import java.lang.annotation.Annotation;

import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * @author Todor Boev
 * @param <T>
 */
public interface DynamicScopesLinkedBindingBuilder<T> extends LinkedBindingBuilder<T> {
  ScopedBindingBuilder toDynamicScope(Class<? extends Annotation> tag);
  
  ScopedBindingBuilder toSingletonDynamicScope(Class<?> member);
}
