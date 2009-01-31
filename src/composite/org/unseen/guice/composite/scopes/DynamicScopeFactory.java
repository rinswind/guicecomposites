package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * @author Todor Boev
 *
 * @param <F>
 */
public class DynamicScopeFactory<F> implements InvocationHandler {
  /** Dynamic proxy that can be called to create a new dynamic context */
  private final F proxy;
  /** Methods used to create the new contexts */
  private final Map<Method, DynamicScopeFactoryMethod> methods; 
  
  /**
   * @param <F>
   * @param iface
   * @param parent
   * @param injector
   * @return
   */
  public static <F> F get(Class<F> iface, Class<? extends Annotation> scope, DynamicContext parent, Injector injector) {
    return new DynamicScopeFactory<F>(iface, scope, parent, injector).proxy();
  }
  
  /**
   * @param iface
   */
  private DynamicScopeFactory(Class<F> iface, Class<? extends Annotation> scope, DynamicContext parent, Injector injector) {
    Errors errors = new Errors();
    try {
      this.methods = new HashMap<Method, DynamicScopeFactoryMethod>();
      
      /* TODO Also grab methods from superinterfaces */
      for (Method method : iface.getMethods()) {
        methods.put(method, new DynamicScopeFactoryMethod(method, scope, parent, injector, errors));
      }
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
    this.proxy = iface.cast(Proxy.newProxyInstance(
        iface.getClassLoader(), new Class[] { iface }, this));
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
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    /* Delegate equals, toString, hashCode to this factory */
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }
    
    /* For the factory methods use method handler */
    try {
      return methods.get(method).invoke(args);
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
