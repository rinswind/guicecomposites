package org.unseen.guice.composite.factory;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;
import com.google.inject.util.Providers;

/**
 * Implements a single factory method.
 */
public class CompositeFactoryMethod {
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
  
  private final Method method;
  private final Key<?> result;
  private final List<Key<?>> params;
  
  /**
   * @param method
   * @param errors
   * @throws ErrorsException
   */
  public CompositeFactoryMethod(Method method, Errors errors) throws ErrorsException {
    this.method = method;
    
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
   * @param parent
   * @param composed
   * @param args
   * @return
   */
  public Binding<?> createComposition(
      Injector parent, final Iterable<Module> composed, final Object[] args) {
    
    /* Add a module with the parameter and return type bindings */
    Module composition = new AbstractModule() {
      /* Raw keys are necessary for the args array and return value */
      @SuppressWarnings("unchecked")
      protected void configure() {
        Binder binder = binder().withSource(method);

        /* Introduce the external parameters into the composition instance. */
        int p = 0;
        for (Key<?> paramKey : params) {
          binder.bind((Key) paramKey).toProvider(Providers.of(args[p++]));
        }

        /* Add the content of the composition */
        for (Module m : composed) {
          install(m);
        }
      }
    };
    
    return parent.createChildInjector(composition).getBinding(result);
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
