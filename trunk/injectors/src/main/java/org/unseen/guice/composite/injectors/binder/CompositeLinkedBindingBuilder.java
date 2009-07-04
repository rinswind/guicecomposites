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

import com.google.inject.Module;
import com.google.inject.binder.LinkedBindingBuilder;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 *
 * @param <T>
 */
public interface CompositeLinkedBindingBuilder<T> extends LinkedBindingBuilder<T> {
  void toComposition(Iterable<Module> modules);
  
  void toComposition(Module... modules);
  
  void toComposition(Class<?> impl);
}
