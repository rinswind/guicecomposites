package org.unseen.guice.composite.injectors;

import static com.google.inject.internal.Annotations.getKey;
import static org.unseen.guice.composite.injectors.Args.arg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.unseen.guice.composite.injectors.Arg;

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
      paramArray[p] = paramKey(paramTypes[p], method, paramAnnotations[p], errors);
    }
    
    /* The wrapped list is immutable */
    this.params = Arrays.asList(paramArray);
  }
  
  /**
   * Create a new injector based at the parent, populate it with the
   * parameters and the user definitions and return a binding to the class
   * that the factory must produce.
   * 
   * @param parent
   * @param composed
   * @param args
   * @return
   */
  public Binding<?> invoke(Injector parent, final Iterable<Module> composed, final Object[] args) {
    return parent.createChildInjector(new AbstractModule() {
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
    }).getBinding(result);
  }

  /**
   * Returns a key similar to {@code key}, but with an {@literal @}Parameter
   * binding annotation. This fails if another binding annotation is clobbered
   * in the process. If the key already has the {@literal @}Parameter annotation,
   * it is returned as-is to preserve any String value.
   */
  private static Key<?> paramKey(Type type, Method method, Annotation[] annotations, Errors errors) 
    throws ErrorsException {
    
    Key<?> key = getKey(TypeLiteral.get(type), method, annotations, errors); 

    Class<? extends Annotation> annotation = key.getAnnotationType();
    
    if (annotation == null) {
      return Key.get(key.getTypeLiteral(), arg(""));
    }

    if (annotation == Arg.class) {
      return key;
    }

    throw errors
      .withSource(method)
      .addMessage("Only @Parameter is allowed for factory parameters, but found @%s", annotation)
      .toException();
  }
}
