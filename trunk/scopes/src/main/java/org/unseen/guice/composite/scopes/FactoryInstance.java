package org.unseen.guice.composite.scopes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.inject.Injector;

/**
 * @author Todor Boev
 */
public class FactoryInstance implements InvocationHandler {
  /** Scope to create */
  private final DynamicScope scope;
  /** Context at which to base the new scope */
  private final DynamicScopeInstance context;
  /** Injector to create the objects in to the new scope */
  private final Injector injector;
  
  /** Method suite */
  private final Map<Method, FactoryMethod> methods; 
  
  /**
   * @param scope
   * @param context
   * @param injector
   * @param methods
   */
  public FactoryInstance(DynamicScope scope, DynamicScopeInstance context,
      Injector injector, Map<Method, FactoryMethod> methods) {
    
    this.scope = scope;
    this.context = context;
    this.injector = injector;
    this.methods = methods;
  }
  
  public DynamicScope scope() {
    return scope;
  }
  
  public DynamicScopeInstance scopeInstance() { 
    return context; 
  }
  
  public Injector injector() {
    return injector;
  }
  
  /**
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return methods.get(method).invoke(proxy, this, args);
  }
}
