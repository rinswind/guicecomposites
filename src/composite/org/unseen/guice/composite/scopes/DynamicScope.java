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
  
  public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
    return new DynamicScopeProvider<T>(key, unscoped, tag);
  }
}
