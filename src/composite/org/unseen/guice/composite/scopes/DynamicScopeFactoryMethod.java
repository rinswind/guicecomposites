package org.unseen.guice.composite.scopes;

import static com.google.inject.internal.Annotations.getKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * Implements a single factory method.
 */
public class DynamicScopeFactoryMethod {
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
  
  /** The object created into the new dynamic context */
  private final Key<?> result;
  /** The parameters passed into the new dynamic context */
  private final List<Key<?>> params;
  /** The parent of the dyamic context this method will create */
  private final DynamicContext parent;
  /** The dynamic scope which this method creates */
  private final Class<? extends Annotation> scope;
  /** The Injector we use to create the first objects of the new DynamicContext */
  private final Injector injector;
  
  /**
   * @param method
   * @param scope
   * @param injector
   * @param errors
   * @throws ErrorsException
   */
  public DynamicScopeFactoryMethod(Method method, Class<? extends Annotation> scope,
      DynamicContext parent, Injector injector, Errors errors) throws ErrorsException {
    
    this.parent = parent;
    this.scope = scope;
    this.injector = injector;
    
    this.result = getKey(
        TypeLiteral.get(method.getGenericReturnType()), method, method.getAnnotations(), errors);
    
    Type[] paramTypes = method.getGenericParameterTypes();
    Annotation[][] paramAnnotations = method.getParameterAnnotations();
    Key<?>[] paramArray = new Key<?>[paramTypes.length];
    for (int p = 0; p < paramArray.length; p++) {
      paramArray[p] = paramKey(paramTypes[p], method, paramAnnotations[p], errors);
    }
    
    this.params = Arrays.asList(paramArray);
  }
  
  /**
   * @param args
   * @param parent
   * @param injector
   * @return
   */
  @SuppressWarnings("unchecked")
  public Object invoke(Object[] args) {
    DynamicContext active = DynamicContext.activate(scope, parent);
    try {
      int p = 0;
      for (Key<?> paramKey : params) {
        active.put((Key) paramKey, args[p++]);
      }
      
      return injector.getInstance(result);
    } finally {
      DynamicContext.deactivate();
    }
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
