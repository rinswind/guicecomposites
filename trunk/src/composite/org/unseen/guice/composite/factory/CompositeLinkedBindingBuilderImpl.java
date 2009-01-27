package org.unseen.guice.composite.factory;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * @author Todor Boev
 * @param <T>
 */
public class CompositeLinkedBindingBuilderImpl<T> implements CompositeLinkedBindingBuilder<T> {
  private final Key<T> key;
  private final LinkedBindingBuilder<T> wrapped;
  
  public CompositeLinkedBindingBuilderImpl(Key<T> key, LinkedBindingBuilder<T> wrapped) {
    this.key = key;
    this.wrapped = wrapped;
  }
  
  public void toComposition(Module... modules) {
    toComposition(Arrays.asList(modules));
  }
  
  @SuppressWarnings("unchecked")
  public void toComposition(final Class<?> impl) {
    toComposition(new AbstractModule() {
      @Override
      protected void configure() {
        Key implKey = Key.get(impl);
        
        bind(implKey);
        for (Class<?> iface : impl.getInterfaces()) {
          bind(Key.get(iface)).to(implKey);
        }
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  public void toComposition(Iterable<Module> modules) {
    toProvider(new CompositeProvider(key.getTypeLiteral().getRawType(), modules));
  }

  public ScopedBindingBuilder to(Class<? extends T> implementation) {
    return wrapped.to(implementation);
  }

  public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
    return wrapped.to(implementation);
  }

  public ScopedBindingBuilder to(Key<? extends T> targetKey) {
    return wrapped.to(targetKey);
  }

  public void toInstance(T instance) {
    wrapped.toInstance(instance);
  }

  public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
    return wrapped.toProvider(provider);
  }

  public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
    return wrapped.toProvider(providerType);
  }

  public ScopedBindingBuilder toProvider(Key<? extends Provider<? extends T>> providerKey) {
    return wrapped.toProvider(providerKey);
  }

  public void asEagerSingleton() {
    wrapped.asEagerSingleton();
  }

  public void in(Class<? extends Annotation> scopeAnnotation) {
    wrapped.in(scopeAnnotation);
  }

  public void in(Scope scope) {
    wrapped.in(scope);
  }
}
