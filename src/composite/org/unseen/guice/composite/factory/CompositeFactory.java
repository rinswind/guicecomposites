package org.unseen.guice.composite.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.internal.collect.ImmutableMap;

/**
 * @author Todor Boev
 */
public class CompositeFactory<F> implements InvocationHandler {
  private final F proxy;
  private final Injector parent;
  private final Iterable<Module> composed;
  private final ImmutableMap<Method, CompositeFactoryMethod> methods; 
  
  /**
   * @param <F>
   * @param factory
   * @param composed
   * @param parent
   * @return
   */
  public static <F> F get(Class<F> factory, Iterable<Module> composed, Injector parent) {
    return new CompositeFactory<F>(factory, composed, parent).proxy();
  }
  
  /**
   * @param factory
   */
  private CompositeFactory(Class<F> factory, Iterable<Module> composed, Injector parent) {
    this.parent = parent;
    this.composed = composed;
    
    Errors errors = new Errors();
    try {
      ImmutableMap.Builder<Method, CompositeFactoryMethod> methodsBuilder = ImmutableMap.builder();
      // TODO: also grab methods from superinterfaces
      for (Method method : factory.getMethods()) {
        methodsBuilder.put(method, new CompositeFactoryMethod(method, errors));
      }
      this.methods = methodsBuilder.build();
    } catch (ErrorsException e) {
      throw new ConfigurationException(e.getErrors().getMessages());
    }
    
    /*
     * FIX Can cause trouble under OSGi. The problem here is that this class
     * loader is different from the class loader that contains the internal
     * classes we use to support this proxy. We need a class loader bridge that
     * will delegate the loading of our internal classes to our loader and
     * everything else to the loader of the factory interface.
     */
    this.proxy = factory.cast(Proxy.newProxyInstance(
        factory.getClassLoader(), new Class[] { factory }, this));
  }
  
  /**
   * @return
   */
  private F proxy() {
    return proxy;
  }
  
  /**
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
   */
  public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
    /* Delegate equals, toString, hashCode to this factory */
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    /* For the factory methods use an appropriate binding */
    Provider<?> provider = methods.get(method).createComposition(parent, composed, args).getProvider();
    try {
      return provider.get();
    } catch (ProvisionException e) {
      /* If this is an exception declared by the factory method, throw it as-is */
      if (e.getErrorMessages().size() == 1) {
        Throwable cause = e.getErrorMessages().iterator().next().getCause();
        if (cause != null && canRethrow(method, cause)) {
          throw cause;
        }
      }
      throw e;
    }
  }
  
  @Override
  public String toString() {
    return proxy.getClass().getInterfaces()[0].getName();
  }
  
  @Override
  public boolean equals(Object o) {
    return o == this || o == proxy;
  }
  
  /**
   * Returns true if {@code thrown} can be thrown by {@code invoked} without
   * wrapping.
   */
  private static boolean canRethrow(Method invoked, Throwable thrown) {
    if (thrown instanceof Error || thrown instanceof RuntimeException) {
      return true;
    }

    for (Class<?> declared : invoked.getExceptionTypes()) {
      if (declared.isInstance(thrown)) {
        return true;
      }
    }

    return false;
  }
}
