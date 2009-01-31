package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.ScopeAnnotation;

public class DynamicScopes {
  private DynamicScopes() {
  }
  
  /**
   * @param <S>
   * @param tag
   * @return
   */
  public static <S extends Annotation> Scope scope(Class<S> tag) {
    checkScopeAnnotation(tag);
    return new DynamicScope<S>(tag);
  }

  /**
   * @param <F>
   * @param <S>
   * @param iface
   * @param tag
   * @return
   */
  public static <F, S extends Annotation> Provider<F> factory(Class<F> iface, Class<S> tag) {
    checkScopeAnnotation(tag);
    return new DynamicScopeFactoryProvider<F, S>(iface, tag);
  }
  
  /**
   * @param <T>
   * @param key
   * @return
   */
  public static <T> Provider<T> external(Key<T> key) {
    return new DynamicScopeParameterProvider<T>(key);
  }
  
  /**
   * @param value
   * @return
   */
  public static Parameter parameter(final String value) {
    return new Parameter() {
      public String value() {
        return value;
      }

      public Class<? extends Annotation> annotationType() {
        return Parameter.class;
      }
      
      public String toString() {
        return "@" + Parameter.class.getName() + "(value=" + value + ")";
      }
      
      public boolean equals(Object o) {
        return o instanceof Parameter && value.equals(((Parameter) o).value());
      }
      
      public int hashCode() {
        return (127 * "value".hashCode()) ^ value.hashCode();
      }

    };
  }
  
  /**
   * @param tag
   */
  private static void checkScopeAnnotation(Class<? extends Annotation> tag) {
    if (tag.getAnnotation(ScopeAnnotation.class) == null) {
      throw new IllegalArgumentException(tag + " is not a scope annotation");
    }
  }
}
