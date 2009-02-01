package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @author Todor Boev
 *
 * @param <T>
 */
public class DynamicScopeProvider<T, S extends Annotation> implements Provider<T> {
  private final Key<T> key;
  private final Provider<T> unscoped;
  private final Class<S> scope;
  
  public DynamicScopeProvider(Key<T> key, Provider<T> unscoped, Class<S> scope) {
    this.key = key; 
    this.unscoped = unscoped;
    this.scope = scope;
  }
  
  @Override
  public String toString() {
    return "DynamicScopeProvider[ scope: " + scope.getSimpleName() + ", key: " + key + "]";
  }
  
  public T get() {
    /*
     * TODO Make sure the scope of the current provider is always equal or wider
     * than the active scope. Wider because a part of the parent scope might be
     * lazily created because of demand by a narrower scope. This will require
     * me to introduce explicit scope ordering.
     */
    return DynamicScopeInstance.active().search(key, unscoped, scope);
  }
}
