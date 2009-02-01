package org.unseen.guice.composite.scopes;

import com.google.inject.Injector;
import com.google.inject.internal.Errors;

public interface FactoryMethod {
  /**
   * @param proxy the proxy called from the user code.
   * @param instance the instance backing the proxy.
   * @param args the arguments to the method call.
   * @return
   * @throws Throwable
   */
  Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable;
  
  /**
   * @param injector the Injector in which everyone lives.
   * @param errors a place to add errors.
   */
  void validate(Injector injector, Errors errors);
}