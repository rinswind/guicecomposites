package org.unseen.guice.composite.scopes;

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.internal.Errors;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 * @param <F>
 */
public class DynamicScopeFactoryProvider<F, S extends Annotation> implements Provider<F> {
  private static final FactoryMethod EQUALS =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return args[0] == proxy || args[0] == instance;
    }

    public void validate(Injector injector, Errors errors) {
    }
  };
  
  private static final FactoryMethod TO_STRING =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return proxy.getClass().getInterfaces()[0].getName();
    }

    public void validate(Injector injector, Errors errors) {
    }
  };
  
  private static final FactoryMethod HASH_CODE =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return instance.hashCode();
    }

    public void validate(Injector injector, Errors errors) {
    }
  };
  
  /** The interface of the factories we create */
  private final Class<F> iface;
  /** The method suite of the factories we create */
  private final Map<Method, FactoryMethod> methods;
  
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
    try {
      this.methods = new HashMap<Method, FactoryMethod>();
      
      /* TODO Also grab methods from superinterfaces */
      for (Method method : iface.getMethods()) {
        methods.put(method, new FactoryMethodImpl(method));
      }
      
      /*
       * Add the Object methods to the suite - except these the other object
       * methods are handled by the proxy
       */
      methods.put(Object.class.getMethod("equals", Object.class), EQUALS);
      methods.put(Object.class.getMethod("toString"), TO_STRING);
      methods.put(Object.class.getMethod("hashCode"), HASH_CODE);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Unexpected", e);
    }
  }

  /**
   * At injector-creation time, we initialize and validate the the method suite.
   */
  @Inject
  public void setInjector(Injector injector) {
    if (this.injector != null) {
      throw new ConfigurationException(asList(new Message(DynamicScopeFactoryProvider.class,
          "DynamicScopeFactoryProviders can only be used in one Injector.")));
    }
    
    this.injector = injector;
    
    Errors errors = new Errors();
    for (FactoryMethod m : methods.values()) {
      m.validate(injector, errors);
    }
    errors.throwConfigurationExceptionIfErrorsExist();
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
    if (injector == null) {
      throw new CreationException(asList(new Message(DynamicScopeFactoryProvider.class,
          "DynamicScopeFactoryProvider is not initalized with an Injector")));
    }
    
    /* Capture the current scope if any - this is the last part of the instance state */
    DynamicScopeInstance active = DynamicScopeInstance.isActive() ? DynamicScopeInstance.active() : null;
    
    /*
     * Return a factory that will continue the creation starting from the scope
     * that is active right now.
     */
    FactoryInstance factory = new FactoryInstance(scope, active, injector, methods);

    /*
     * FIX Can cause trouble under OSGi. The problem here is that the factory
     * interface class loader is different from the class loader of the internal
     * classes we use to support the proxy. We need a class loader bridge that
     * will delegate the loading of our internal classes to our loader and
     * everything else to the loader of the factory interface.
     * 
     * With no bridge to make this work under OSGi we must not have internal
     * unexported classes that support the proxy. The easiest way it so have
     * everything in one public package so that the consumers import the impl
     * classes together with the public interface. This of course is bad OSGi
     * practice.
     */
    return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, factory));
  }
}
