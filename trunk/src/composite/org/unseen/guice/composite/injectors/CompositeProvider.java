package org.unseen.guice.composite.injectors;


import java.util.Arrays;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
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
