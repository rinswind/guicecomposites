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
    return "DynamicScope(" + tag.getCanonicalName() + ")";
  }
  
  public Class<? extends Annotation> annotation() {
    return tag;
  }
  
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return new Provider<T>() {
      public T get() {
        /*
         * TODO Make sure the scope of the current provider is always equal or wider
         * than the active scope. Wider because a part of the parent scope might be
         * lazily created because of demand by a narrower scope. This will require
         * me to introduce explicit scope ordering.
         */
        return DynamicScopeInstance.active().search(key, unscoped, tag);
      }
    };
  }
}
