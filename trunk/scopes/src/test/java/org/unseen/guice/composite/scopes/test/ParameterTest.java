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

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.unseen.guice.composite.scopes.Args.arg;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Before;
import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;
import com.google.inject.ScopeAnnotation;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
public class ParameterTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({METHOD, TYPE})
  @interface ParameterizedScope {
  }
  
  @Retention(RUNTIME)
  @Target({PARAMETER})
  @interface Nullable {
  }
  
  interface ParameterizedFactory {
    Parameterized create(@Arg(name="a") String a, @Arg(name="b") String b);
  }
  
  public static class Parameterized {
    final String a;
    final String b;
    
    @Inject     
    public Parameterized(
        @Arg(name="a", value=ParameterizedScope.class) String a, 
        @Nullable @Arg(name="b", value=ParameterizedScope.class) String b) {
      this.a = a;
      this.b = b;
    }
  }
  
  private Injector inj;
  
  @Before
  public void setupInjector() {
    this.inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ParameterizedFactory.class).toScope(ParameterizedScope.class);
        bind(Parameterized.class).in(ParameterizedScope.class);
      }
    });
  }
  
  @Test
  public void testParameter() {
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    Parameterized par = fact.create("a", "b");
    assertEquals("a", par.a);
    assertEquals("b", par.b);
  }
  
  @Test
  public void testNullParameter() {
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    Parameterized par = fact.create("a", null);
    assertEquals("a", par.a);
    assertNull(par.b);
  }
  
  @Test(expected = ProvisionException.class)
  public void testUnannotatedNullParameter() {
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    fact.create(null, "b");
  }
  
  @Test
  public void testParameterBindings() {
    assertNotNull(inj.getBinding(Key.get(String.class, arg("a", ParameterizedScope.class))));
    assertNotNull(inj.getBinding(Key.get(String.class, arg("b", ParameterizedScope.class))));
  }
}
