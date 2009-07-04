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
package org.unseen.guice.composite.scopes.binder;

import java.lang.annotation.Annotation;

import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 * @param <T>
 */
public class DynamicScopesAnnotatedBindingBuilderImpl<T> extends DynamicScopesLinkedBindingBuilderImpl<T>
    implements DynamicScopesAnnotatedBindingBuilder<T> {

  private final AnnotatedBindingBuilder<T> wrapped;
  
  public DynamicScopesAnnotatedBindingBuilderImpl(Key<T> key, AnnotatedBindingBuilder<T> wrapped, Binder binder) {
    super(key, wrapped, binder);
    this.wrapped = wrapped;
  }
  
  public DynamicScopesLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    wrapped.annotatedWith(annotationType);
    return this;
  }

  public DynamicScopesLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    wrapped.annotatedWith(annotation);
    return this;
  }
}
