package org.unseen.guice.composite.scopes;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.inject.ConfigurationException;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * Implements a single factory method.
 */
public class FactoryMethodImpl implements FactoryMethod {
  private static final Parameter DEFAULT_TAG = new Parameter() {
    public String value() {
      return "";
    }

    public Class<? extends Annotation> annotationType() {
      return Parameter.class;
    }
    
    public String toString() {
      return "@" + Parameter.class.getName() + "(value=)";
    }
    
    public boolean equals(Object o) {
      return o instanceof Parameter && "".equals(((Parameter) o).value());
    }
    
    public int hashCode() {
      return (127 * "value".hashCode()) ^ "".hashCode();
    }
  };

  /** The method this handler implements */
  private final Method method;
  /** The object created into the new dynamic context */
  private final Key<?> result;
  /** The parameters passed into the new dynamic context */
  private final List<Key<?>> params;
  
  /**
   * @param method
   * @param binder
   * @param errors
   * @throws ErrorsException 
   */
  public FactoryMethodImpl(Method method) {
    this.method = method;
    
    /* If even one key fails to be built the errors will contain the reason
     * and an ErrorsException will be thrown out of getKey or getParamKey 
     */
    Errors errors = new Errors();
    try {
      /* Build this methods return type */
      this.result = getKey(
          TypeLiteral.get(method.getGenericReturnType()), method, method.getAnnotations(), errors);
      
      /* Build this methods arguments */
      Type[] paramTypes = method.getGenericParameterTypes();
      Annotation[][] paramAnnotations = method.getParameterAnnotations();
      Key<?>[] paramArray = new Key<?>[paramTypes.length];
      for (int p = 0; p < paramArray.length; p++) {
        paramArray[p] = getParamKey(paramTypes[p], method, paramAnnotations[p], errors);  
      }
    
      this.params = Arrays.asList(paramArray);
    } catch (ErrorsException e) {
      throw new ConfigurationException(errors.getMessages());
    }
  }

  /**
   * @see org.unseen.guice.composite.scopes.FactoryMethod#returnType()
   */
  public Key<?> returnType() {
    return result;
  }
  
  /**
   * @see org.unseen.guice.composite.scopes.FactoryMethod#parameterTypes()
   */
  public List<Key<?>> parameterTypes() {
    return params;
  }

  /**
   * @see org.unseen.guice.composite.scopes.FactoryMethod#invoke(org.unseen.guice.composite.scopes.FactoryInstance, java.lang.Object[])
   */
  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
    DynamicScopeInstance active = DynamicScopeInstance.activate(instance.scope(), instance.scopeInstance());
    try {
      int p = 0;
      for (Key<?> paramKey : params) {
        active.seed((Key) paramKey, args[p++]);
      }
      
      /*
       * If scope factories need to be created to satisfy this instantiation
       * they will capture the scope instance we have activated just now.
       */
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
   * Returns a key similar to {@code Key}, but with an {@literal @}Parameter
   * binding annotation. This fails if another binding annotation is clobbered
   * in the process. If the key already has the {@literal @}Parameter annotation,
   * it is returned as-is to preserve any String value.
   */
  private static Key<?> getParamKey(Type paramType, Method method, Annotation[] paramTags,
      Errors errors) throws ErrorsException {
    
    Key<?> key = getKey(TypeLiteral.get(paramType), method, paramTags, errors); 

    Class<? extends Annotation> tag = key.getAnnotationType();
    
    if (tag == null) {
      return Key.get(key.getTypeLiteral(), DEFAULT_TAG);
    }

    if (tag == Parameter.class) {
      return key;
    }

    throw errors
      .withSource(method)
      .addMessage("Only @Parameter is allowed for factory parameters, but found @%s", tag)
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
