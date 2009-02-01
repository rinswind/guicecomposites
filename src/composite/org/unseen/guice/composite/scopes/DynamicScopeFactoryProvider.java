package org.unseen.guice.composite.scopes;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.internal.collect.ImmutableList;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 * @param <F>
 */
public class DynamicScopeFactoryProvider<F, S extends Annotation> implements Provider<F> {
  /** The interface of the factories we create */
  private final Class<F> iface;
  /** The method suite of the factories we create */
  private final Map<Method, DynamicScopeFactoryMethod> methods;
  
  /** Part of the state loaded into every created factory */
  private final Class<S> scope;
  /** Part of the state loaded into every created factory - injected later */
  private Injector injector;
  
  /**
   * @param iface interface of the factory
   * @param scope the scope of which this factory creates instances.
   */
  public DynamicScopeFactoryProvider(Class<F> iface, Class<S> scope) {
    this.iface = iface;
    this.scope = scope;
    
    /* Build the method suite shared by all factory instances we create */
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
  }

  /**
   * At injector-creation time, we initialize the invocation handler.
   */
  @Inject
  public void setInjector(Injector injector) {
    if (this.injector != null) {
      throw new ConfigurationException(ImmutableList.of(new Message(DynamicScopeFactoryProvider.class,
          "DynamicScopeFactoryProviders can only be used in one Injector.")));
    }
    
    this.injector = injector;
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
    if (injector == null) {
      throw new IllegalStateException("DynamicScopeFactoryProvider is not initalized with an Injector");
    }
    
    /* Capture the current scope if any */
    DynamicContext active = DynamicContext.active();
    
    /* Return a factory that will continue the scope creation later on */
    DynamicScopeFactory factory = new DynamicScopeFactory(scope, active, injector, methods);
    
    /*
     * FIX Can cause trouble under OSGi. The problem here is that this class
     * loader is different from the class loader that contains the internal
     * classes we use to support this proxy. We need a class loader bridge that
     * will delegate the loading of our internal classes to our loader and
     * everything else to the loader of the factory interface.
     */
    return iface.cast(Proxy.newProxyInstance(
        iface.getClassLoader(), new Class[] { iface }, factory));
  }
}
