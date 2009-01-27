package org.unseen.guice.composite.factory;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.internal.collect.ImmutableList;
import com.google.inject.internal.collect.ImmutableMap;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 * @param <F>
 */
public  class CompositionProvider<F> implements InvocationHandler, Provider<F> {
  /** Dynamically generated implementation of the factory */
  private final F factory;
  /** Implementations of the factory methods */ 
  private final ImmutableMap<Method, FactoryMethod> factoryMethods;
  /** The parent space if any */
  private Injector parent;
  /** The content of the compositions we are going to create */
  private final Iterable<Module> composed;
  
  /**
   * @param factoryIface
   * @param composed
   */
  public CompositionProvider(Class<F> factoryIface, Iterable<Module> composed) {
    this.composed = composed;
    
    Errors errors = new Errors();
    try {
      ImmutableMap.Builder<Method, FactoryMethod> factoryMethodsBuilder = ImmutableMap.builder();
      // TODO: also grab methods from superinterfaces
      for (Method method : factoryIface.getMethods()) {
        factoryMethodsBuilder.put(method, new FactoryMethod(method, errors));
      }
      this.factoryMethods = factoryMethodsBuilder.build();
    } catch (ErrorsException e) {
      throw new ConfigurationException(e.getErrors().getMessages());
    }

    this.factory = factoryIface.cast(Proxy.newProxyInstance(
      /*
       * FIX Can cause trouble under OSGi. The problem here is that this class
       * loader is different from the class loader that contains the internal
       * classes we use to support this proxy. We need a class loader bridge that
       *  will delegate the loading of our internal classes to our loader and
       * everything else to the loader of the factory interface.
       */
      factoryIface.getClassLoader(),
      new Class[] { factoryIface }, 
      this));
  }

  /**
   * At injector-creation time, we initialize the invocation handler. At this
   * time we make sure all factory methods will be able to build the target
   * types.
   */
  @Inject
  public void setParentSpace(Injector injector) {
    if (this.parent != null) {
      throw new ConfigurationException(ImmutableList.of(new Message(CompositionProvider.class,
          "CompositeFactories may only be used in one Injector.")));
    }

    this.parent = injector;

//    /*
//     * To perform validation try to get a binding out of each factory method
//     * right now - at early injection time.
//     */
//    for (Method method : returnTypesByMethod.keySet()) {
//      Object[] args = new Object[method.getParameterTypes().length];
//      Arrays.fill(args, "dummy object for validating Factories");
//      getBindingFromNewChildSpace(method, args);
//    }
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
    return factory;
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
    Provider<?> provider = factoryMethods.get(method).createComposition(parent, composed, args).getProvider();
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
    return factory.getClass().getInterfaces()[0].getName();
  }
  
  @Override
  public boolean equals(Object o) {
    return o == this || o == factory;
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
