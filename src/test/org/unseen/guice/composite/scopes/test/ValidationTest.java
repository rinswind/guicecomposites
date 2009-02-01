package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.DynamicScopes;
import org.unseen.guice.composite.scopes.Parameter;

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.ScopeAnnotation;

public class ValidationTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface TestScoped {
  }
  
  public interface DependentFactory {
    Dependent create(@Parameter String name);
  }
  
  public interface Dependency {
  }
  
  @TestScoped
  public static class Dependent {
    @Inject Dependency dep;
    @Inject @Parameter String name;
  }
  
  @TestScoped
  public static class DependencyImpl implements Dependency {
  }
  
  @Test(expected = CreationException.class)
  public void testMissingDependency() { 
    Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        DynamicScopes.bindScope(binder(), TestScoped.class);
        
        DynamicScopes.bindFactory(binder(), DependentFactory.class, TestScoped.class);
      }
    });
  }
}
