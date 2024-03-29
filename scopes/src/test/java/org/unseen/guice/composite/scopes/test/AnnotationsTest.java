/**
 * Copyright (C) 2009 Todor Boev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.unseen.guice.composite.scopes.Args.arg;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.FactoryMethodImpl;

import com.google.inject.Key;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
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
    
    assertEquals(one.hashCode(), two.hashCode());
  }
  
  @Test
  public void testNnamedEquals() throws SecurityException, NoSuchMethodException {
    Arg one = getArg(Annotated.class, "full");
    Arg two = arg("a", TestScope.class);
    
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
  
  @Test
  public void testParamKeys() throws ErrorsException, SecurityException, NoSuchMethodException {
    Method method = Annotated.class.getMethod("unnamed", new Class[]{String.class});
    Annotation[] tags = method.getParameterAnnotations()[0];
    
    Key<?> one = FactoryMethodImpl.getParamKey(TypeLiteral.get(String.class), TestScope.class,
        method, tags, new Errors());
    Key<?> two = Key.get(String.class, arg(TestScope.class));
    
    assertEquals(one, two);
  }
  
  private static Arg getArg(Class<?> clazz, String method) throws SecurityException,
      NoSuchMethodException {
    
    return (Arg) clazz.getMethod(method, new Class[] { String.class }).getParameterAnnotations()[0][0];
  }
}
