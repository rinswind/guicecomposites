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

import com.google.inject.Key;
import com.google.inject.binder.AnnotatedBindingBuilder;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 * @param <T>
 */
public class CompositeAnnotatedBindingBuilderImpl<T> extends CompositeLinkedBindingBuilderImpl<T>
    implements CompositeAnnotatedBindingBuilder<T> {

  private final AnnotatedBindingBuilder<T> wrapped;
  
  public CompositeAnnotatedBindingBuilderImpl(Key<T> key, AnnotatedBindingBuilder<T> wrapped) {
    super(key, wrapped);
    this.wrapped = wrapped;
  }
  
  public CompositeLinkedBindingBuilder<T> annotatedWith(Class<? extends Annotation> annotationType) {
    wrapped.annotatedWith(annotationType);
    return this;
  }

  public CompositeLinkedBindingBuilder<T> annotatedWith(Annotation annotation) {
    wrapped.annotatedWith(annotation);
    return this;
  }
}
