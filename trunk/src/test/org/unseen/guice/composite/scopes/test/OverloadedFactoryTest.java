package org.unseen.guice.composite.scopes.test;

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertNotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

public class OverloadedFactoryTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface OverloadScoped {
  }
  
  public static class Parent {
    @Inject 
    public Parent(@Arg(OverloadScoped.class) Object o) {
    } 
  }
  
  public static class Child extends Parent { 
    @Inject 
    public Child(@Arg(OverloadScoped.class) Object o) { 
      super(o); 
    } 
  }

  public interface ParentFactory {
    Parent create(Object o); 
  }
  
  public interface ChildFactory extends ParentFactory { 
    Child create(Object o); 
  }

  @Test
  public void testReturn() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ChildFactory.class).toScope(OverloadScoped.class);
        bind(Parent.class).in(OverloadScoped.class);
        bind(Child.class).in(OverloadScoped.class);
      }
    });
    
    ChildFactory childFact = inj.getInstance(ChildFactory.class);
    Child child = childFact.create(new Object());
    assertNotNull(child);
    
    ParentFactory parentFact = inj.getInstance(ChildFactory.class);
    Parent parent = parentFact.create(new Object());
    assertNotNull(parent);
  }
}
