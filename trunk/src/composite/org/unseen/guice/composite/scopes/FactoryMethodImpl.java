package org.unseen.guice.composite.scopes;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * Implements a single factory method.
 */
public class FactoryMethodImpl implements FactoryMethod {
  /** The method this handler implements */
  private final Method method;
  /** The object created into the new dynamic context */
  private Key<?> result;
  /** The parameters passed into the new dynamic context */
  private List<Key<?>> params;
  
  /**
   * @param method
   * @param errors
   * @throws ErrorsException
   */
  public FactoryMethodImpl(Method method) {
    this.method = method;
  }
  
  /**
   * @see org.unseen.guice.composite.scopes.FactoryMethod#invoke(org.unseen.guice.composite.scopes.FactoryInstance, java.lang.Object[])
   */
  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, FactoryInstance instance,Object[] args) throws Throwable {
    /* Spawn a new scope */
    DynamicScopeInstance active = DynamicScopeInstance.activate(instance.scope(), instance.context());
    try {
      int p = 0;
      for (Key<?> paramKey : params) {
        active.put((Key) paramKey, args[p++]);
      }
      
      return instance.injector().getInstance(result);
    } catch (ProvisionException e) {
      /* If this is an exception declared by the factory method, throw it as-is */
      if (e.getErrorMessages().size() == 1) {
        Throwable cause = e.getErrorMessages().iterator().next().getCause();
        if (cause != null && canRethrow(method, cause)) {
          throw cause;
        }
      }
      throw e;
    } finally {
      DynamicScopeInstance.deactivate();
    }
  }

  /**
   * @throws ErrorsException 
   * @see org.unseen.guice.composite.scopes.FactoryMethod#validate(com.google.inject.Injector, com.google.inject.internal.Errors)
   */
  public void validate(Injector injector, Errors errors) {
    try {
      /* Build this methods return type */
      this.result = getKey(
          TypeLiteral.get(method.getGenericReturnType()), method, method.getAnnotations(), errors);
      
      /* Build this methods arguments */
      Type[] paramTypes = method.getGenericParameterTypes();
      Annotation[][] paramAnnotations = method.getParameterAnnotations();
      Key<?>[] paramArray = new Key<?>[paramTypes.length];
      for (int p = 0; p < paramArray.length; p++) {
        paramArray[p] = paramKey(paramTypes[p], method, paramAnnotations[p], errors);
      }
      
      this.params = Arrays.asList(paramArray);
      
      /* Ask the injector to check if it can build our result type */
      injector.getBinding(result);
    } catch (ErrorsException e) {
      errors.merge(e.getErrors());
    } catch (ConfigurationException e) {
      errors.merge(e.getErrorMessages());
    } catch (CreationException e) {
      errors.merge(e.getErrorMessages());
    }
  }
  
  /**
   * Returns a key similar to {@code Key}, but with an {@literal @}Parameter
   * binding annotation. This fails if another binding annotation is clobbered
   * in the process. If the key already has the {@literal @}Parameter annotation,
   * it is returned as-is to preserve any String value.
   */
  private static Key<?> paramKey(Type type, Method method, Annotation[] annotations, Errors errors) 
    throws ErrorsException {
    
    Key<?> key = getKey(TypeLiteral.get(type), method, annotations, errors); 

    Class<? extends Annotation> annotation = key.getAnnotationType();
    
    if (annotation == null) {
      return Key.get(key.getTypeLiteral(), DynamicScopes.parameter(""));
    }

    if (annotation == Parameter.class) {
      return key;
    }

    throw errors
      .withSource(method)
      .addMessage("Only @Parameter is allowed for factory parameters, but found @%s", annotation)
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
