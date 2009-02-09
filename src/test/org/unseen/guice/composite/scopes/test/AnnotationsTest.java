package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.unseen.guice.composite.scopes.Args.arg;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;

import com.google.inject.ScopeAnnotation;

public class AnnotationsTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface TestScope{
  }
  
  public interface Annotated {
    void unscoped(@Arg String arg);
    
    void unnamed(@Arg(TestScope.class) String arg);
    
    void full(@Arg(name="a", value=TestScope.class) String arg);
  }
  
  @Test
  public void testUnnamedEquals() throws SecurityException, NoSuchMethodException {
    Arg one = getArg(Annotated.class, "unnamed");
    Arg two = arg(TestScope.class);
    
    assertEquals(one.annotationType(), two.annotationType());
    assertEquals(one, two);
  }
  
  @Test
  public void testNnamedEquals() throws SecurityException, NoSuchMethodException {
    Arg one = getArg(Annotated.class, "full");
    Arg two = arg(TestScope.class, "a");
    
    assertEquals(one.annotationType(), two.annotationType());
    assertEquals(one, two);
  }
  
  @Test
  public void testAnonymousEquals() throws SecurityException, NoSuchMethodException {
    Arg one = getArg(Annotated.class, "unscoped");
    Arg two = arg(null);
     
    assertEquals(one.annotationType(), two.annotationType());
    assertEquals(one, two);
  }
  
  private static Arg getArg(Class<?> clazz, String method) throws SecurityException,
      NoSuchMethodException {
    
    return (Arg) clazz.getMethod(method, new Class[] { String.class }).getParameterAnnotations()[0][0];
  }
}
