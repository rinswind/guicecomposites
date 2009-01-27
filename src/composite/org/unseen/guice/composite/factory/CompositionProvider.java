package org.unseen.guice.composite.factory;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.internal.collect.ImmutableList;
import com.google.inject.internal.collect.ImmutableMap;
import com.google.inject.spi.Message;
import com.google.inject.util.Providers;

/**
 * The newer implementation of factory provider. This implementation uses a
 * child injector to create values.
 *
 * @param <F>
 */
public  class CompositionProvider<F> implements Provider<F> {
  /**
   * If a factory method parameter isn't annotated, it gets this annotation.
   */
  private static final Parameter DEFAULT_ANNOTATION = new Parameter() {
    public String value() {
      return "";
    }

    public Class<? extends Annotation> annotationType() {
      return Parameter.class;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Parameter && ((Parameter) o).value().equals("");
    }

    @Override
    public int hashCode() {
      return 127 * "value".hashCode() ^ "".hashCode();
    }

    @Override
    public String toString() {
      return "@" + Parameter.class.getName() + "(value=)";
    }
  };

  /**
   * An immutable reflective method signature expressed in terms of Key rather
   * than Class.
   */
  private static class MethodSig {
    final Key<?> result;
    final List<Key<?>> params;
    
    public MethodSig(Method method, Errors errors) throws ErrorsException {
      this.result = getKey(
          TypeLiteral.get(method.getGenericReturnType()), method, method.getAnnotations(), errors);
      
      Type[] paramTypes = method.getGenericParameterTypes();
      Annotation[][] paramAnnotations = method.getParameterAnnotations();
      Key<?>[] paramArray = new Key<?>[paramTypes.length];
      for (int p = 0; p < paramArray.length; p++) {
        paramArray[p] = paramKey(
            method, 
            getKey(TypeLiteral.get(paramTypes[p]), method, paramAnnotations[p], errors), 
            errors);
      }
      
      /* The wrapped list is immutable */
      this.params = Arrays.asList(paramArray);
    }
    
    /**
     * Returns a key similar to {@code key}, but with an {@literal @}Parameter
     * binding annotation. This fails if another binding annotation is clobbered
     * in the process. If the key already has the {@literal @}Parameter annotation,
     * it is returned as-is to preserve any String value.
     */
    private static <T> Key<T> paramKey(Method method, Key<T> key, Errors errors) throws ErrorsException {
      Class<? extends Annotation> annotation = key.getAnnotationType();
      
      if (annotation == null) {
        return Key.get(key.getTypeLiteral(), DEFAULT_ANNOTATION);
      }

      if (annotation == Parameter.class) {
        return key;
      }

      throw errors
        .withSource(method)
        .addMessage("Only @Parameter is allowed for factory parameters, but found @%s", annotation)
        .toException();
    }
  }
  
  /** Dynamically generated implementation of the factory */
  private final F factory;
  /** Implementations of the factory methods */ 
  private final ImmutableMap<Method, MethodSig> factoryMethods;

  /**
   * The hosting Injector, or null if we haven't been initialized yet or this is
   * a root provider.
   */
  private Injector parentSpace;
  private Iterable<Module> composed;
  
  /**
   * @param factoryIface
   * @param composed
   */
  public CompositionProvider(Class<F> factoryIface, Iterable<Module> composed) {
    this.composed = composed;
    
    Errors errors = new Errors();
    try {
      ImmutableMap.Builder<Method, MethodSig> factoryMethodsBuilder = ImmutableMap.builder();
      // TODO: also grab methods from superinterfaces
      for (Method method : factoryIface.getMethods()) {
        factoryMethodsBuilder.put(method, new MethodSig(method, errors));
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
      new InvocationHandler() {
        public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
          /* Delegate equals, toString, hashCode to this factory */
          if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
          }

          /* For the factory methods use an appropriate binding */
          Provider<?> provider = getBindingFromNewComposition(method, args).getProvider();
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
      }));
  }

  /**
   * At injector-creation time, we initialize the invocation handler. At this
   * time we make sure all factory methods will be able to build the target
   * types.
   */
  @Inject
  public void setParentSpace(Injector injector) {
    if (this.parentSpace != null) {
      throw new ConfigurationException(ImmutableList.of(new Message(CompositionProvider.class,
          "CompositeFactories may only be used in one Injector.")));
    }

    this.parentSpace = injector;

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

  public F get() {
    return factory;
  }

  /**
   * Creates a child injector that binds the args, and returns the binding for
   * the method's result.
   */
  private Binding<?> getBindingFromNewComposition(final Method method, final Object[] args) {
    final MethodSig sig = factoryMethods.get(method);

    /* Add a module with the parameter and return type bindings */
    Module composition = new AbstractModule() {
      /* Raw keys are necessary for the args array and return value */
      @SuppressWarnings("unchecked")
      protected void configure() {
        Binder binder = binder().withSource(method);

        /* Introduce the external parameters into the composition instance. */
        int p = 0;
        for (Key<?> paramKey : sig.params) {
          binder.bind((Key) paramKey).toProvider(Providers.of(args[p++]));
        }

        /* Add the content of the composition */
        for (Module m : composed) {
          install(m);
        }
      }
    };
    
    Injector childSpace = parentSpace != null 
      ? parentSpace.createChildInjector(composition)
      : Guice.createInjector(composition);
      
    return childSpace.getBinding(sig.result);
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
