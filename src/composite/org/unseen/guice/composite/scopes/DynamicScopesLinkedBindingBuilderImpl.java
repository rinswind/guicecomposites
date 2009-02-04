package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * @author Todor Boev
 * @param <T>
 */
public class DynamicScopesLinkedBindingBuilderImpl<T> implements DynamicScopesLinkedBindingBuilder<T> {
  private final Key<T> key;
  private final LinkedBindingBuilder<T> wrapped;
  private final Binder binder;
  
  public DynamicScopesLinkedBindingBuilderImpl(Key<T> key, LinkedBindingBuilder<T> wrapped, Binder binder) {
    this.key = key;
    this.wrapped = wrapped;
    this.binder = binder;
  }
  
//  @SuppressWarnings("unchecked")
//  public void toDynamicScope(final Class<?> impl) {
//    toComposition(new AbstractModule() {
//      @Override
//      protected void configure() {
//        Key implKey = Key.get(impl);
//        
//        bind(implKey);
//        for (Class<?> iface : impl.getInterfaces()) {
//          bind(Key.get(iface)).to(implKey);
//        }
//      }
//    });
//  }
  
  @SuppressWarnings("unchecked")
  public ScopedBindingBuilder toDynamicScope(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
    
    binder.bindScope(tag, new DynamicScope(tag));
    return wrapped.toProvider(new DynamicScopeFactoryProvider(key.getTypeLiteral().getRawType(), tag, binder));
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
