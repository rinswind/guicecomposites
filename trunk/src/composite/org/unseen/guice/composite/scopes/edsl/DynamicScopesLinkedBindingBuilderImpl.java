package org.unseen.guice.composite.scopes.edsl;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.unseen.guice.composite.scopes.DynamicScope;
import org.unseen.guice.composite.scopes.FactoryMethod;
import org.unseen.guice.composite.scopes.FactoryProvider;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.util.Providers;

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
    
    Class<T> iface = (Class<T>) key.getTypeLiteral().getRawType();
    
    /* Bind a new scope instance for this factory */
    binder.bindScope(tag, new DynamicScope(tag));
    
    /* Create the factory */
    FactoryProvider<T> factory = new FactoryProvider<T>(iface, tag);
    
    /* Bind providers for the common set of factory parameters */
    Set<Key<?>> params = new HashSet<Key<?>>();
    for (FactoryMethod method : factory.methodSuite().values()) {
      params.addAll(method.parameterTypes());
    }
    
    for (Key<?> paramKey : params) {
      /*
       * All factory parameters are by default null if not overridden by values
       * cached from a factory method arguments.
       */
      binder.bind(paramKey).toProvider((Provider) Providers.of(null)).in(tag);
    }
    
    /* Finally bind the factory itself and continue the DSL */
    return wrapped.toProvider(factory);
  }
  
//  @SuppressWarnings("unchecked")
//  public ScopedBindingBuilder toSingletonDynamicScope(final Class<?> impl) {
//    PrivateBinder privBinder = binder.newPrivateBinder();
//    
//    Key implKey = Key.get(impl);
//    
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
