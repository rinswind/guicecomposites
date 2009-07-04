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

import static com.google.inject.internal.BytecodeGen.getClassLoader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.ConfigurationException;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.internal.Errors;
import com.google.inject.internal.ErrorsException;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 */
public class CompositeFactory<F> implements InvocationHandler {
  private final F proxy;
  private final Injector parent;
  private final Iterable<Module> composed;
  private final Map<Method, CompositeFactoryMethod> methods; 
  
  /**
   * @param <F>
   * @param factory
   * @param composed
   * @param parent
   * @return
   */
  public static <F> F get(Class<F> factory, Iterable<Module> composed, Injector parent) {
    return new CompositeFactory<F>(factory, composed, parent).proxy();
  }
  
  /**
   * @param factory
   */
  private CompositeFactory(Class<F> factory, Iterable<Module> composed, Injector parent) {
    this.parent = parent;
    this.composed = composed;
    
    Errors errors = new Errors();
    try {
      this.methods = new HashMap<Method, CompositeFactoryMethod>();
      // TODO: also grab methods from superinterfaces
      for (Method method : factory.getMethods()) {
        methods.put(method, new CompositeFactoryMethod(method, errors));
      }
    } catch (ErrorsException e) {
      throw new ConfigurationException(e.getErrors().getMessages());
    }
    
    /*
     * FIX Can cause trouble under OSGi. The problem here is that this class
     * loader is different from the class loader that contains the internal
     * classes we use to support this proxy. We need a class loader bridge that
     * will delegate the loading of our internal classes to our loader and
     * everything else to the loader of the factory interface.
     */
    this.proxy = factory.cast(Proxy.newProxyInstance(
        getClassLoader(factory), new Class[] { factory }, this));
  }
  
  /**
   * @return
   */
  private F proxy() {
    return proxy;
  }
  
  /**
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
   */
  public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
    /* Delegate equals, toString, hashCode to this factory */
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    /* For the factory methods use an appropriate binding */
    Provider<?> provider = methods.get(method).invoke(parent, composed, args).getProvider();
    try {
      return provider.get();
    } catch (ProvisionException e) {
      /* If this is an exception declared by the factory method, throw it as-is */
      if (e.getErrorMessages().size() == 1) {
        Throwable cause = e.getErrorMessages().iterator().next().getCause();
        if (cause != null && canRethrow(method, cause)) {
          throw cause;
        }
      }
      throw e;
    }
  }
  
  @Override
  public String toString() {
    return proxy.getClass().getInterfaces()[0].getName();
  }
  
  @Override
  public boolean equals(Object o) {
    return o == this || o == proxy;
  }
  
  /**
   * Returns true if {@code thrown} can be thrown by {@code invoked} without
   * wrapping.
   */
  private static boolean canRethrow(Method invoked, Throwable thrown) {
    if (thrown instanceof Error || thrown instanceof RuntimeException) {
      return true;
    }

    for (Class<?> declared : invoked.getExceptionTypes()) {
      if (declared.isInstance(thrown)) {
        return true;
      }
    }

    return false;
  }
}
