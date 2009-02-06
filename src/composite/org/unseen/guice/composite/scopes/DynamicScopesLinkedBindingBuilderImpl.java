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
  
  @SuppressWarnings("unchecked")
  public ScopedBindingBuilder toDynamicScope(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
    
    binder.bindScope(tag, new DynamicScope(tag));
    return wrapped.toProvider(new FactoryProvider(key.getTypeLiteral().getRawType(), tag, binder));
  }
  
//  @SuppressWarnings("unchecked")
//  public ScopedBindingBuilder toSingletonDynamicScope(final Class<?> impl) {
//    PrivateBinder privBinder = binder.newPrivateBinder();
//    
//    Key implKey = Key.get(impl);
//    
//    /* FIX must analyze all the methods of the factory and bind the impl 
//     * only to their products. Must make sure all the products return
//     * types compatible with the impl class.
//     */
//    privBinder.bind(implKey);
//    privBinder.expose(implKey);
//    
//    for (Class<?> iface : impl.getInterfaces()) {
//      Key<?> ifaceKey = Key.get(iface);
//      privBinder.bind(ifaceKey).to(implKey);
//      privBinder.expose(ifaceKey);
//    }
//    
//    /* Must instantiate a new scoping annotation on the fly? */
//    return toDynamicScope();
//  }
  
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
