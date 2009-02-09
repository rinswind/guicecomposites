package org.unseen.guice.composite.scopes.binder;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.unseen.guice.composite.scopes.AnonymousScope;
import org.unseen.guice.composite.scopes.DynamicScope;
import org.unseen.guice.composite.scopes.FactoryMethod;
import org.unseen.guice.composite.scopes.FactoryProvider;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.PrivateBinder;
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
  public ScopedBindingBuilder toScope(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
    
    Class<T> iface = (Class<T>) key.getTypeLiteral().getRawType();
    
    /* Create a scope and a factory for that scope */
    DynamicScope scope = new DynamicScope(tag);
    FactoryProvider<T> factory = new FactoryProvider<T>(iface, scope);
    
    /* Bind the scope */
    binder.bindScope(tag, scope);
    
    /* Merge the parameters into a unique set */
    Set<Key<?>> params = new HashSet<Key<?>>();
    for (FactoryMethod method : factory.methodSuite().values()) {
      params.addAll(method.parameterTypes());
    }
    
    /* Bind the parameters into the user-defined dynamic scope */
    for (Key<?> paramKey : params) {
      /*
       * All factory parameters are by default null if not overridden by values
       * cached from a factory method arguments.
       */
      binder.bind(paramKey).toProvider((Provider) Providers.of(null)).in(scope);
    }
    
    /* Finally bind the factory itself and continue the DSL */
    return wrapped.toProvider(factory);
  }
  
  @SuppressWarnings("unchecked")
  public ScopedBindingBuilder toClassScope(Class<?> impl) {
    /* We want to hide the parameter bindings in a private space */
    PrivateBinder privBinder = binder.newPrivateBinder();
    
    Class<T> iface = (Class<T>) key.getTypeLiteral().getRawType();
    
    /*
     * Create a scope and a factory for that scope. This scope has no associated
     * annotation.
     */
    DynamicScope scope = new DynamicScope(AnonymousScope.class);
    FactoryProvider<T> factory = new FactoryProvider<T>(iface, scope);
    
    /* Merge all parameters and return values into two unique sets */
    Set<Key<?>> params = new HashSet<Key<?>>();
    Set<Key<?>> returns = new HashSet<Key<?>>();
    for (FactoryMethod method : factory.methodSuite().values()) {
      returns.add(method.returnType());
      params.addAll(method.parameterTypes());
    }
    
    /* Bind the return values into the anonymous scope and expose them */
    for (Key returnKey : returns) {
      privBinder.bind(returnKey).to(impl).in(scope);
      privBinder.expose(returnKey);
    }
    
    /* Bind the parameters into the anonymous scope */
    for (Key paramKey : params) {
      privBinder.bind(paramKey).toProvider(Providers.of(null)).in(scope);
    }
    
    /* Finally bind the factory itself and continue the DSL */
    return wrapped.toProvider(factory);
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
