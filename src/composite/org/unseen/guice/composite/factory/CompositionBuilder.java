package org.unseen.guice.composite.factory;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * @author Todor Boev
 *
 * @param <T>
 */
public class CompositionBuilder<T> implements CompositionAnnotatedBindingBuilder<T>,
    CompositionLinkedBindingBuilder<T> {
  
  private final Binder binder;
  
  public CompositionBuilder(Binder binder) {
    this.binder = binder;
  }
  
  public CompositionLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    // TODO Auto-generated method stub
    return null;
  }

  public CompositionLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    // TODO Auto-generated method stub
    return null;
  }

  public void toComposition(Iterable<Module> modules) {
    // TODO Auto-generated method stub
    
  }

  public void toComposition(Module... modules) {
    // TODO Auto-generated method stub
    
  }

  public ScopedBindingBuilder to(Class<? extends T> implementation) {
    // TODO Auto-generated method stub
    return null;
  }

  public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
    // TODO Auto-generated method stub
    return null;
  }

  public ScopedBindingBuilder to(Key<? extends T> targetKey) {
    // TODO Auto-generated method stub
    return null;
  }

  public void toInstance(T instance) {
    // TODO Auto-generated method stub
    
  }

  public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
    // TODO Auto-generated method stub
    return null;
  }

  public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
    // TODO Auto-generated method stub
    return null;
  }

  public ScopedBindingBuilder toProvider(Key<? extends Provider<? extends T>> providerKey) {
    // TODO Auto-generated method stub
    return null;
  }

  public void asEagerSingleton() {
    // TODO Auto-generated method stub
  }

  public void in(Class<? extends Annotation> scopeAnnotation) {
    // TODO Auto-generated method stub
  }

  public void in(Scope scope) {
    // TODO Auto-generated method stub
  }
}
