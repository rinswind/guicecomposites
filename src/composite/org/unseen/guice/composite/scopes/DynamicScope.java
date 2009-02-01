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

  public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
    return new DynamicScopeProvider<T>(key, unscoped, tag);
  }
}
