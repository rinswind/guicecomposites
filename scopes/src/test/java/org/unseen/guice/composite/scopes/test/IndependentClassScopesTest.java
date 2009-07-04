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
import static org.junit.Assert.*;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
public class IndependentClassScopesTest {
  public interface BoxFactory { 
    Box create(int num);
  }
  
  public static class Box {
    private final int one;
    
    @Inject
    public Box(@Arg int one) {
      this.one = one;
    }
    
    public int one() {
      return one;
    }
  }
  
  public interface OtherBoxFactory { 
    OtherBox create(int two);
  }
  
  public static class OtherBox {
    private final int one;
    
    @Inject
    public OtherBox(@Arg int one) {
      this.one = one;
    }
    
    public int one() {
      return one;
    }
  }
  
  @Test
  public void testIndependence() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toClassScope(Box.class);
        bind(OtherBoxFactory.class).toClassScope(OtherBox.class);
      }
    });
    
    Box b = inj.getInstance(BoxFactory.class).create(1);
    OtherBox ob = inj.getInstance(OtherBoxFactory.class).create(2);
    
    assertEquals(1, b.one());
    assertEquals(2, ob.one());
  }
}
