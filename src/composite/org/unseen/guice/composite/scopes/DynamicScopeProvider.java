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
    return "DynamicScopeProvider[ scope: " + scope.getSimpleName() + ", key: " + key.getTypeLiteral().getRawType().getSimpleName() + "]";
  }
  
  public T get() {
    System.out.println(this + ".get()");
    
    DynamicContext active = DynamicContext.active();
    if (active == null) {
      throw new IllegalStateException();
    }
    
    /*
     * TODO Make sure the scope of the current provider is always equal or wider
     * than the active scope. Wider because a part of the parent scope might be
     * lazily created because of demand of the first narrower scope it owns.
     * This will require me to introduce scope ordering.
     */
    
    return active.search(key, unscoped, scope);
  }
}