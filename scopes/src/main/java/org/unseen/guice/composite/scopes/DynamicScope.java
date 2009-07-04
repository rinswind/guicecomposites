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

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author rinsvind@gmail.com (Todor Boev)
 * 
 */
public class DynamicScope implements Scope {
  private final Class<? extends Annotation> tag;

  public DynamicScope(Class<? extends Annotation> tag) {
    this.tag = tag;
  }

  @Override
  public String toString() {
    return "DynamicScope(" + (tag != null ? tag.getCanonicalName() : "anonymous") + ")";
  }

  public Class<? extends Annotation> annotation() {
    return tag;
  }

  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    /*
     * TODO Make sure the scope of the current provider is always equal or wider
     * than the active scope. Wider because a part of the parent scope might be
     * lazily created on demand by a narrower scope. This will require me to
     * introduce explicit scope ordering.
     */

    /*
     * This provider must be called in one-shot mode only during recursive
     * creation initiated by a call to a dynamic scope factory. The factory will
     * setup the DynamicScopeInstance from which this provider can obtain
     * values. The DynamicScopeInstance is lost as soon as the creation
     * finishes. If this provider is injected directly into an object it's get()
     * method will be called after the active dynamic scope is dead and the
     * provider won't work. Therefore dynamically scoped providers must never be
     * injected.
     */
    return new Provider<T>() {
      public T get() {
        return DynamicScopeInstance.active().search(key, unscoped, DynamicScope.this);
      }
    };
  }
}
