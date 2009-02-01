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
  private static final Method TO_STRING;
  private static final Method EQUALS;
  private static final Method HASH_CODE;
  
  static {
    try {
      TO_STRING = Object.class.getDeclaredMethod("toString");
      EQUALS = Object.class.getDeclaredMethod("equals", Object.class);
      HASH_CODE = Object.class.getDeclaredMethod("hashCode");
    } catch (Exception exc) {
      throw new RuntimeException(exc);
    }
  }
  
  /** Dynamic proxy that can be called to create a new dynamic context */
  private final F proxy;
  /** Methods used to create the new contexts */
  private final Map<Method, DynamicScopeFactoryMethod> methods; 
  
  private final Class<? extends Annotation> scope;
  private final DynamicContext parent;
  private final Injector injector;
  
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
    this.scope = scope;
    this.parent = parent;
    this.injector = injector;
    
    Errors errors = new Errors();
    try {
      this.methods = new HashMap<Method, DynamicScopeFactoryMethod>();
      
      /* TODO Also grab methods from superinterfaces */
      for (Method method : iface.getMethods()) {
        methods.put(method, new DynamicScopeFactoryMethod(method, errors));
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
    /* equals() */
    if (method == EQUALS) {
      return args[0] == this || args[0] == proxy;
    }
    
    /* toString() */
    if (method == TO_STRING) {
      return proxy.getClass().getInterfaces()[0].getName();
    }
    
    /* hashCode() */
    if (method == HASH_CODE) {
      return this.hashCode();
    }
    
    /* Factory method - delegate to a specialized hander */
    try {
      return methods.get(method).invoke(scope, parent, injector, args);
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
