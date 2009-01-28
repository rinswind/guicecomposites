package org.unseen.guice.composite.scope;

import java.lang.annotation.Annotation;

import com.google.inject.Scope;

public class DynamicScopes {
  private DynamicScopes() {
  }
  
  public static Scope get(Class<? extends Annotation> tag) {
    return new DynamicScope(tag);
  }
}
