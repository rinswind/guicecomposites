package org.unseen.guice.composite.factory;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

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
import com.google.inject.internal.collect.ImmutableMultimap;
import com.google.inject.spi.Message;
import com.google.inject.util.Providers;

/**
 * The newer implementation of factory provider. This implementation uses a
 * child injector to create values.
 *
 * @param <F>
 */
public  class CompositionProvider<F> implements InvocationHandler, Provider<F> {
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

  /** The factory interface, implemented and provided */
  private final F factory;
  
  /*
   * These two really form a list of reflective factory method signatures.
   * Except these are described in terms of Key rather than Class.
   */
  /** Factory method to return type */
  private final ImmutableMap<Method, Key<?>> returnTypesByMethod;
  /** Factory method to parameter types */
  private final ImmutableMultimap<Method, Key<?>> paramTypes;

  /**
   * The hosting Injector, or null if we haven't been initialized yet or this is
   * a root provider.
   */
  private Injector parentSpace;
  private Iterable<Module> composed;
  
  /**
   * @param compositionFactory a Java interface that defines one or more create
   *          methods.
   * @param producedType a concrete type that is assignable to the return types
   *          of all factory methods.
   */
  public CompositionProvider(Class<F> compositionFactory, Iterable<Module> composed) {
    this.composed = composed;
    
    Errors errors = new Errors();
    try {
      ImmutableMap.Builder<Method, Key<?>> returnTypesBuilder = ImmutableMap.builder();
      ImmutableMultimap.Builder<Method, Key<?>> paramTypesBuilder = ImmutableMultimap.builder();

      // TODO: also grab methods from superinterfaces
      for (Method method : compositionFactory.getMethods()) {
        Key<?> returnType = getKey(TypeLiteral.get(method.getGenericReturnType()), method, method.getAnnotations(), errors);
        returnTypesBuilder.put(method, returnType);
        
        Type[] params = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        int p = 0;
        for (Type param : params) {
          Key<?> paramKey = getKey(TypeLiteral.get(param), method, paramAnnotations[p++], errors);
          paramTypesBuilder.put(method, assistKey(method, paramKey, errors));
        }
      }
      returnTypesByMethod = returnTypesBuilder.build();
      paramTypes = paramTypesBuilder.build();
    } catch (ErrorsException e) {
      throw new ConfigurationException(e.getErrors().getMessages());
    }

    factory = compositionFactory.cast(Proxy.newProxyInstance(compositionFactory.getClassLoader(),
        new Class[] { compositionFactory }, this));
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
   * When a factory method is invoked, we create a child injector that binds all
   * parameters, then use that to get an instance of the return type.
   */
  public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
    /* Delegate equals, toString, hashCode to this factory */
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    /* For the factory methods use an appropriate binding */
    Provider<?> provider = getBindingFromNewChildSpace(method, args).getProvider();
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
   * Creates a child injector that binds the args, and returns the binding for
   * the method's result.
   */
  private Binding<?> getBindingFromNewChildSpace(final Method method, final Object[] args) {
    final Key<?> returnType = returnTypesByMethod.get(method);

    /* Add a module with the parameter and return type bindings */
    Module child = new AbstractModule() {
      @SuppressWarnings("unchecked")
      // raw keys are necessary for the args array and return value
      protected void configure() {
        Binder binder = binder().withSource(method);

        /*
         * Set up providers for the config parameters. Wrap them in providers to
         * allow null, and to prevent them from leaking to the parent space.
         */
        int p = 0;
        for (Key<?> paramKey : paramTypes.get(method)) {
          binder.bind((Key) paramKey).toProvider(Providers.of(args[p++]));
        }

        /* Add the content of the child space */
        for (Module m : composed) {
          install(m);
        }
      }
    };
    
    Injector childSpace = parentSpace != null 
      ? parentSpace.createChildInjector(child)
      : Guice.createInjector(child);
      
    return childSpace.getBinding(returnType);
  }

  /**
   * Returns a key similar to {@code key}, but with an {@literal @}External
   * binding annotation. This fails if another binding annotation is clobbered
   * in the process. If the key already has the {@literal @}External annotation,
   * it is returned as-is to preserve any String value.
   */
  private <T> Key<T> assistKey(Method method, Key<T> key, Errors errors) throws ErrorsException {
    Class<? extends Annotation> annotation = key.getAnnotationType();
    
    if (annotation == null) {
      return Key.get(key.getTypeLiteral(), DEFAULT_ANNOTATION);
    }

    if (annotation == Parameter.class) {
      return key;
    }

    throw errors
      .withSource(method)
      .addMessage("Only @External is allowed for factory parameters, but found @%s", annotation)
      .toException();
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
