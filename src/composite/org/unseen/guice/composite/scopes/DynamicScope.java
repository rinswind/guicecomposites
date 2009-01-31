package org.unseen.guice.composite.scopes;

import java.lang.annotation.Annotation;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author Todor Boev
 * 
 */
public class DynamicScope<S extends Annotation> implements Scope {
  private final Class<S> tag;

  public DynamicScope(Class<S> tag) {
    this.tag = tag;
  }

  public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
    return new DynamicScopeProvider<T, S>(key, unscoped, tag);
  }
}
