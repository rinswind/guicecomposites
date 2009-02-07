package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.spi.BindingScopingVisitor;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 * @param <F>
 */
public class FactoryProvider<F> implements Provider<F> {
  private static final FactoryMethod EQUALS =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return args[0] == proxy || args[0] == instance;
    }
    
    public List<Key<?>> parameterTypes() {
      throw new UnsupportedOperationException();
    }

    public Key<?> returnType() {
      throw new UnsupportedOperationException();
    }
  };
  
  private static final FactoryMethod TO_STRING =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return proxy.getClass().getInterfaces()[0].getName();
    }
    
    public List<Key<?>> parameterTypes() {
      throw new UnsupportedOperationException();
    }

    public Key<?> returnType() {
      throw new UnsupportedOperationException();
    }
  };
  
  private static final FactoryMethod HASH_CODE =  new FactoryMethod() {
    public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
      return instance.hashCode();
    }
    
    public List<Key<?>> parameterTypes() {
      throw new UnsupportedOperationException();
    }

    public Key<?> returnType() {
      throw new UnsupportedOperationException();
    }
  };
  
  /**
   *
   */
  private static class ScopeChecker implements BindingScopingVisitor<Void> {
    private final String checked;
    private final Object source;
    private final DynamicScope expected;
    private final Errors errors;
    
    public ScopeChecker(DynamicScope expected, String checked, Object source,
        Errors errors) {
      
      this.checked = checked;
      this.source = source;
      this.expected = expected;
      this.errors = errors;
    }
    
    public Void visitScopeAnnotation(Class<? extends Annotation> tag) {
      if (expected.annotation() != tag) {
        error(tag);
      }
      return null;
    }
    
    public Void visitScope(Scope scope) {
      if (expected != scope) {
        error(scope);
      }
      return null;
    }
    
    public Void visitEagerSingleton() {
      error("Eager Singleton");
      return null;
    }

    public Void visitNoScoping() {
      error("No scope");
      return null;
    }

    private void error(Object found) {
      errors.withSource(source).addMessage("For %s expected scope %s but found %s", checked, expected, found);
    }
  };
  
  /** The interface of the factories we create */
  private final Class<F> iface;
  /** The method suite of the factories we create */
  private final Map<Method, FactoryMethod> methods;
  
  /** Part of the state loaded into every created factory */
  private final DynamicScope scope;
  /** Part of the state loaded into every created factory - injected later */
  private Injector injector;
  
  /**
   * @param binder
   * @throws ErrorsException 
   */
  public FactoryProvider(Class<F> iface, DynamicScope scope) {
    if (!iface.isInterface()) {
      throw new ConfigurationException(Arrays.asList(new Message("Only interfaces can be used for "
          + " scope factories. Found a concrete class: " + iface)));
    }
    
    this.iface = iface;
    this.scope = scope;
    
    this.methods = new HashMap<Method, FactoryMethod>();

    for (Class<?> cl = iface; cl != null; cl = iface.getSuperclass()) {
      for (Method method : cl.getMethods()) {
        methods.put(method, new FactoryMethodImpl(method));
      }
    }
  }

  /**
   * @return
   */
  public Map<Method, FactoryMethod> methodSuite() {
    return methods;
  }
  
  /**
   * At injector-creation time, validate the the factory method suite.
   */
  @Inject
  public void setInjector(Injector injector) {
    this.injector = injector;
    
    /*
     * Validate that the product of the factory can be built and that the
     * product is in the same scope as the factory.
     */
    Errors errors = new Errors();
    Binding<?> binding = null;
    for (Map.Entry<Method, FactoryMethod> ent : methods.entrySet()) {
      try {
        binding = injector.getBinding(ent.getValue().returnType());
        
        /*
         * If this is not an annonymous singleton dynamic scope we must validate
         * that the user has bound all return values to the same scope as the
         * one managed by this factory.
         */
        if (this.scope.annotation() != null) {
          binding.acceptScopingVisitor(new ScopeChecker(this.scope, "return value", ent.getKey(), errors));
        }
      } catch (ConfigurationException e) {
        errors.merge(e.getErrorMessages());
      } catch (CreationException e) {
        errors.merge(e.getErrorMessages());
      }
    }
    errors.throwConfigurationExceptionIfErrorsExist();

    /*
     * Add the Object methods to the suite after the validation so that we avoid
     * their processing.
     */
    try {
      methods.put(Object.class.getMethod("equals", Object.class), EQUALS);
      methods.put(Object.class.getMethod("toString"), TO_STRING);
      methods.put(Object.class.getMethod("hashCode"), HASH_CODE);
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Unexpected", e);
    }
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
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
