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
package org.unseen.guice.composite.injectors;

import java.util.Arrays;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.spi.Message;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 * @param <F>
 */
public class CompositeProvider<F> implements Provider<F> {
  private final Class<F> factoryIface;
  private final Iterable<Module> composed;
  /** Dynamically generated implementation of the factory */
  private F factory;
  
  /**
   * @param factoryIface
   * @param composed
   */
  public CompositeProvider(Class<F> factoryIface, Iterable<Module> composed) {
    this.factoryIface = factoryIface;
    this.composed = composed;
  }

  /**
   * At injector-creation time, we initialize the invocation handler.
   */
  @Inject
  public void setParent(Injector injector) {
    if (factory != null) {
      throw new ConfigurationException(Arrays.asList(new Message(CompositeProvider.class,
          "CompositeFactories may only be used in one Injector.")));
    }

    factory = CompositeFactory.get(factoryIface, composed, injector);
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
    if (factory == null) {
      throw new IllegalStateException("CompositeFactory not initalized with an Injector");
    }
    
    return factory;
  }
}
