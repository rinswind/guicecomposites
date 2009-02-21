package org.unseen.guice.composite.scopes;

import static com.google.inject.internal.Annotations.getKey;
import static org.unseen.guice.composite.scopes.Args.arg;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
  /** The method this handler implements */
  private final Method method;
  /** The object created into the new dynamic context */
  private final Key<?> result;
  /** The parameters passed into the new dynamic context */
  private final List<Key<?>> params;
  
  public FactoryMethodImpl(TypeLiteral<?> factory, Method method, DynamicScope scope) {
    this.method = method;
    
    /*
     * If even one key fails to be built the errors will contain the reason and
     * an ErrorsException will be thrown out of getKey or getParamKey
     */
    Errors errors = new Errors();
    try {
      /* Build this methods return type */
      this.result = getKey(factory.getReturnType(method), method, method.getAnnotations(), errors);
      
      /* Build this methods arguments */
      List<TypeLiteral<?>> paramTypes = factory.getParameterTypes(method);
      Annotation[][] paramAnnotations = method.getParameterAnnotations();
      Key<?>[] paramArray = new Key<?>[paramTypes.size()];
      Class<? extends Annotation> tag = scope.annotation();
      
      for (int p = 0; p < paramArray.length; p++) {
        paramArray[p] = getParamKey(paramTypes.get(p), tag, method, paramAnnotations[p], errors);  
      }
    
      this.params = Arrays.asList(paramArray);
    } catch (ErrorsException e) {
      throw new ConfigurationException(errors.getMessages());
    }
  }

  public Key<?> returnType() {
    return result;
  }
  
  public List<Key<?>> parameterTypes() {
    return params;
  }

  @SuppressWarnings("unchecked")
  public Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable {
    DynamicScopeInstance active = DynamicScopeInstance.activate(instance.scope(), instance.scopeInstance());
    try {
      int p = 0;
      for (Key<?> paramKey : params) {
        active.seed((Key) paramKey, args[p++]);
      }
      
      /*
       * If nested scope factories need to be created to satisfy this
       * instantiation they will capture the scope instance we have activated
       * just now.
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
  public static Key<?> getParamKey(TypeLiteral<?> paramType, Class<? extends Annotation> scope,
      Method method, Annotation[] paramTags, Errors errors) throws ErrorsException {
    
    Key<?> key = getKey(paramType, method, paramTags, errors); 

    Class<? extends Annotation> tag = key.getAnnotationType();
    
    if (tag == null) {
      return Key.get(key.getTypeLiteral(), arg(scope));
    }

    if (tag == Arg.class) {
      /*
       * By default the annotations on factory methods may omit the scope name.
       * We add it for them.
       */
      Arg arg = (Arg) key.getAnnotation(); 
      if (arg.value() == AnonymousScope.class) {
        return Key.get(key.getTypeLiteral(), arg(arg.name(), scope));
      }
      
      return key;
    }

    throw errors
      .withSource(method)
      .addMessage("Only @Arg is allowed for factory parameters, but found @%s", tag)
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
