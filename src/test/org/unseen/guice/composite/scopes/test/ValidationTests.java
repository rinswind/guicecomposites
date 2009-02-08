package org.unseen.guice.composite.scopes.test;

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.CreationException;
import com.google.inject.Inject;
import com.google.inject.ScopeAnnotation;
import com.google.inject.Singleton;

public class ValidationTests {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface TestScoped {
  }
  
  public interface DependentFactory {
    Dependent create();
  }
  
  public interface Dependency {
  }
  
  public static class Dependent {
    @Inject Dependency dep;
  }
  
  public static class DependencyImpl implements Dependency {
  }
  
  @Test(expected = CreationException.class)
  public void testMissingDependency() {
    createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(DependentFactory.class).toScope(TestScoped.class);
        bind(Dependent.class).in(TestScoped.class);
      }
    });
  }
  
  @Test(expected = CreationException.class)
  public void testBadlyScopedReturn() { 
    createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(DependentFactory.class).toScope(TestScoped.class);
        bind(Dependency.class).to(DependencyImpl.class).in(TestScoped.class);
        bind(Dependent.class).in(Singleton.class);
      }
    });
  }
  
  @Test
  public void testCorrectlyScopedReturn() { 
    createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(DependentFactory.class).toScope(TestScoped.class);
        bind(Dependency.class).to(DependencyImpl.class).in(TestScoped.class);
        bind(Dependent.class).in(TestScoped.class);
      }
    }).getInstance(DependentFactory.class);
  }
}
