package org.unseen.guice.composite.scope;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author Todor Boev
 *
 */
public class CompositeScope implements Scope {
  public static final Scope COMPOSITE = new CompositeScope();
  
  private CompositeScope() {
  }
  
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return new Provider<T>() {
      public T get() {
        return CompositeContext.contextualGet(key, unscoped);
      }
    };
  }
}
