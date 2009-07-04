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
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
public class MultiMethodFactoryTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface BoxScoped {
  }
  
  @Retention(RUNTIME)
  @Target({PARAMETER})
  @interface Nullable {
  }
  
  interface BoxFactory {
    IntegerBox create(Integer num);
    
    StringBox create(String str);
    
    CombinedBox create(Integer num, String str);
  }
  
  public static class IntegerBox {
    final Integer num;
    
    @Inject
    public IntegerBox(@Nullable @Arg(BoxScoped.class) Integer num) {
      this.num = num;
    }
  }
  
  public static class StringBox {
    final String str;
    
    @Inject
    public StringBox(@Nullable @Arg(BoxScoped.class) String str) {
      this.str = str;
    }
  }
  
  public static class CombinedBox {
    @Inject IntegerBox numBox;
    @Inject StringBox strBox;
  }
  
  @Test 
  public void testMultipleProducts() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toScope(BoxScoped.class);
        bind(IntegerBox.class).in(BoxScoped.class);
        bind(StringBox.class).in(BoxScoped.class);
        bind(CombinedBox.class).in(BoxScoped.class);
      }
    });
    
    BoxFactory fact = inj.getInstance(BoxFactory.class);
    
    IntegerBox ib = fact.create(42);
    assertTrue(42 == ib.num);
    
    StringBox sb = fact.create("42");
    assertEquals("42", sb.str);
    
    CombinedBox cb = null;
    
    cb = fact.create(42, "42");
    assertTrue(42 == cb.numBox.num);
    assertEquals("42", cb.strBox.str);
    
    cb = fact.create(null, "42");
    assertNull(cb.numBox.num);
    assertEquals("42", cb.strBox.str);
    
    cb = fact.create(42, null);
    assertTrue(42 == cb.numBox.num);
    assertNull(null, cb.strBox.str);
  }
}
