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
package org.unseen.guice.composite.scopes;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.inject.Injector;

/**
 * Backend of a dynamically generated factory.
 * 
 * @author rinsvind@gmail.com (Todor Boev)
 * 
 */
public class FactoryInstance implements InvocationHandler {
  /** Scope to create */
  private final DynamicScope scope;
  /** Context at which to base the new scope */
  private final DynamicScopeInstance context;
  /** Injector to create the objects in to the new scope */
  private final Injector injector;

  /** Method suite */
  private final Map<Method, FactoryMethod> methods;

  /**
   * @param scope
   * @param context
   * @param injector
   * @param methods
   */
  public FactoryInstance(DynamicScope scope, DynamicScopeInstance context, Injector injector,
      Map<Method, FactoryMethod> methods) {

    this.scope = scope;
    this.context = context;
    this.injector = injector;
    this.methods = methods;
  }

  public DynamicScope scope() {
    return scope;
  }

  public DynamicScopeInstance scopeInstance() {
    return context;
  }

  public Injector injector() {
    return injector;
  }

  /**
   * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
   *      java.lang.reflect.Method, java.lang.Object[])
   */
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return methods.get(method).invoke(proxy, this, args);
  }
}
