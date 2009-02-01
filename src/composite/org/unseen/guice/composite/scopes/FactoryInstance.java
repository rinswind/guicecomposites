package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.inject.Injector;

/**
 * @author Todor Boev
 *
 * @param <F>
 */
public class FactoryInstance implements InvocationHandler {
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
  
  /*
   * State
   */
  private final Class<? extends Annotation> scope;
  private final DynamicScopeInstance context;
  private final Injector injector;
  
  /** Method suite */
  private final Map<Method, FactoryMethod> methods; 
  
  /**
   * @param scope
   * @param context
   * @param injector
   * @param methods
   */
  public FactoryInstance(Class<? extends Annotation> scope, DynamicScopeInstance context,
      Injector injector, Map<Method, FactoryMethod> methods) {
    
    this.scope = scope;
    this.context = context;
    this.injector = injector;
    this.methods = methods;
  }
  
  public Class<? extends Annotation> scope() {
    return scope;
  }
  
  public Injector injector() {
    return injector;
  }
  
  public DynamicScopeInstance context() { 
    return context; 
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
    return methods.get(method).invoke(this, args);
  }
}
