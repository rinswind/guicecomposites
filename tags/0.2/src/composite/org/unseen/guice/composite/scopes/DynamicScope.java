package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author Todor Boev
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
    return new Provider<T>() {
      public T get() {
        /*
         * TODO Make sure the scope of the current provider is always equal or
         * wider than the active scope. Wider because a part of the parent scope
         * might be lazily created on demand by a narrower scope. This will
         * require me to introduce explicit scope ordering.
         */
        
        /*
         * This provider must be called in one-shot mode only during recursive
         * creation that was initiated by a call to a dynamic scope factory. The
         * factory will setup the dynamic scope instance from which this
         * provider can obtain values. The dynamic scope instance is lost as
         * soon as the creation finishes. If this provider is injected directly
         * into an object it's get() method will be called after the active
         * dynamic scope is dead and the provider won't work. Therefore
         * dynamically scoped providers must never be injected.
         */
        return DynamicScopeInstance.active().search(key, unscoped, DynamicScope.this);
      }
    };
  }
}
