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
package org.unseen.guice.composite.injectors.binder;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import org.unseen.guice.composite.injectors.CompositeProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.binder.ScopedBindingBuilder;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 * @param <T>
 */
public class CompositeLinkedBindingBuilderImpl<T> implements CompositeLinkedBindingBuilder<T> {
  private final Key<T> key;
  private final LinkedBindingBuilder<T> wrapped;
  
  public CompositeLinkedBindingBuilderImpl(Key<T> key, LinkedBindingBuilder<T> wrapped) {
    this.key = key;
    this.wrapped = wrapped;
  }
  
  public void toComposition(Module... modules) {
    toComposition(Arrays.asList(modules));
  }
  
  @SuppressWarnings("unchecked")
  public void toComposition(final Class<?> impl) {
    toComposition(new AbstractModule() {
      @Override
      protected void configure() {
        Key implKey = Key.get(impl);
        
        bind(implKey);
        for (Class<?> iface : impl.getInterfaces()) {
          bind(Key.get(iface)).to(implKey);
        }
      }
    });
  }
  
  @SuppressWarnings("unchecked")
  public void toComposition(Iterable<Module> modules) {
    toProvider(new CompositeProvider(key.getTypeLiteral().getRawType(), modules));
  }

  public ScopedBindingBuilder to(Class<? extends T> implementation) {
    return wrapped.to(implementation);
  }

  public ScopedBindingBuilder to(TypeLiteral<? extends T> implementation) {
    return wrapped.to(implementation);
  }

  public ScopedBindingBuilder to(Key<? extends T> targetKey) {
    return wrapped.to(targetKey);
  }

  public void toInstance(T instance) {
    wrapped.toInstance(instance);
  }

  public ScopedBindingBuilder toProvider(Provider<? extends T> provider) {
    return wrapped.toProvider(provider);
  }

  public ScopedBindingBuilder toProvider(Class<? extends Provider<? extends T>> providerType) {
    return wrapped.toProvider(providerType);
  }

  public ScopedBindingBuilder toProvider(Key<? extends Provider<? extends T>> providerKey) {
    return wrapped.toProvider(providerKey);
  }

  public void asEagerSingleton() {
    wrapped.asEagerSingleton();
  }

  public void in(Class<? extends Annotation> scopeAnnotation) {
    wrapped.in(scopeAnnotation);
  }

  public void in(Scope scope) {
    wrapped.in(scope);
  }
}
