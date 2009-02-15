package org.unseen.guice.composite.scopes;

import static com.google.inject.internal.BytecodeGen.getClassLoader;

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
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
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
    
    public ScopeChecker(DynamicScope expected, String checked, Object source, Errors errors) {
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
  private final TypeLiteral<F> iface;
  /** The method suite of the factories we create */
  private final Map<Method, FactoryMethod> methods;
  
  /** Part of the state loaded into every created factory */
  private final DynamicScope scope;
  /** Part of the state loaded into every created factory - injected later */
  private Injector injector;
  
  public FactoryProvider(TypeLiteral<F> iface, DynamicScope scope) {
    if (!iface.getRawType().isInterface()) {
      throw new ConfigurationException(Arrays.asList(new Message("Only interfaces can be used for "
          + " scope factories. Found a concrete class: " + iface)));
    }
    
    this.iface = iface;
    this.scope = scope;
    
    this.methods = new HashMap<Method, FactoryMethod>();

    for (Class<?> cl = iface.getRawType(); cl != null; cl = cl.getSuperclass()) {
      for (Method method : cl.getMethods()) {
        methods.put(method, new FactoryMethodImpl(iface, method, scope));
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
         * If this is not an anonymous singleton dynamic scope we must validate
         * that the user has bound all return values to the same scope as the
         * one managed by this factory.
         */
        if (this.scope.annotation() != AnonymousScope.class) {
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
    
    @SuppressWarnings("unchecked")
    Class<F> type = (Class<F>) iface.getRawType();
    
    return type.cast(Proxy.newProxyInstance(getClassLoader(type), new Class[] {type}, factory));
  }
}
