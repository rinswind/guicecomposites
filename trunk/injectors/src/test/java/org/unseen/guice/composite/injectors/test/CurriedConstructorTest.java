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
package org.unseen.guice.composite.injectors.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.unseen.guice.composite.injectors.Arg;
import org.unseen.guice.composite.injectors.binder.CompositeModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 */
public class CurriedConstructorTest {
  public interface BoxFactory { 
    Box create(int two);
  }
  
  public interface Box {
    String one();
    int two();
  }
  
  public static class BoxImpl implements Box {
    private final String one;
    private final int two;
    
    @Inject
    public BoxImpl(@Named("one") String one, @Arg int two) {
      this.one = one;
      this.two = two;
    }
    
    public String one() {
      return one;
    }

    public int two() {
      return two;
    }
  }
  
  @Test
  public void test() {
    Injector inj = Guice.createInjector(new CompositeModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toComposition(BoxImpl.class);
        bindConstant().annotatedWith(Names.named("one")).to("one");
      }
    });
    
    Box b = inj.getInstance(BoxFactory.class).create(42);
    assertEquals("one", b.one());
    assertEquals(42, b.two());
  }
}
