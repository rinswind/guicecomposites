package org.unseen.guice.composite.scopes;

import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * @author Todor Boev
 *
 * @param <T>
 */
public class DynamicScopeParameterProvider<T> implements Provider<T> {
  private final Key<T> key;
  
  public DynamicScopeParameterProvider(Key<T> key) {
    this.key = key;
  }
                                                    
  public T get() {
    throw new IllegalStateException("There is no cached parameter for " + key);
  }
}
