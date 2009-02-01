package org.unseen.guice.composite.scopes;

import com.google.inject.Injector;
import com.google.inject.internal.Errors;

public interface FactoryMethod {
  /**
   * @param instance The factory instance over which this method is invoked.
   * @param args The arguments of the invocation.
   * @return the first object graph of the new scope.
   * @throws Throwable
   */
  Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable;
  
  /**
   * @param injector
   * @param errors
   */
  void validate(Injector injector, Errors errors);
}