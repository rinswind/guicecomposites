package org.unseen.guice.composite.scopes;


import java.lang.annotation.Annotation;

import com.google.inject.ConfigurationException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.internal.collect.ImmutableList;
import com.google.inject.spi.Message;

/**
 * @author Todor Boev
 * @param <F>
 */
public class DynamicScopeFactoryProvider<F, S extends Annotation> implements Provider<F> {
  private final Class<F> iface;
  private final Class<S> tag;
  private Injector injector;
  
  /**
   * @param iface interface of the factory
   * @param scope the scope of which this factory creates instances.
   */
  public DynamicScopeFactoryProvider(Class<F> iface, Class<S> scope) {
    this.iface = iface;
    this.tag = scope;
  }

  /**
   * At injector-creation time, we initialize the invocation handler.
   */
  @Inject
  public void setInjector(Injector injector) {
    if (this.injector != null) {
      throw new ConfigurationException(ImmutableList.of(new Message(DynamicScopeFactoryProvider.class,
          "DynamicScopeFactoryProviders can only be used in one Injector.")));
    }
    
    this.injector = injector;
  }

  /**
   * @see com.google.inject.Provider#get()
   */
  public F get() {
    if (injector == null) {
      throw new IllegalStateException("DynamicScopeFactoryProvider is not initalized with an Injector");
    }
    
    /* Capture the current scope if any */
    DynamicContext active = DynamicContext.active();
    
    /* Return a factory that will continue the scope creation later on */
    return DynamicScopeFactory.get(iface, tag, active, injector);
  }
}
