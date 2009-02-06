package org.unseen.guice.composite.scopes;

import java.util.List;

import com.google.inject.Key;

/**
 * @author Todor Boev
 */
public interface FactoryMethod {
  /**
   * @return
   */
  List<Key<?>> parameterTypes();
  
  /**
   * @return
   */
  Key<?> returnType();
  
  /**
   * @param proxy the proxy called from the user code.
   * @param instance the instance backing the proxy.
   * @param args the arguments to the method call.
   * @return
   * @throws Throwable
   */
  Object invoke(Object proxy, FactoryInstance instance, Object[] args) throws Throwable;
}
