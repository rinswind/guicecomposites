package org.unseen.guice.composite.scopes.test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.DynamicScopesModule;
import org.unseen.guice.composite.scopes.Parameter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;
import static com.google.inject.Guice.*;

import static junit.framework.Assert.*;

public class ParameterTests {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({METHOD, TYPE})
  @interface ParameterizedScope {
  }
  
  interface ParameterizedFactory {
    Parameterized create(String param);
  }
  
  public static class Parameterized {
    @Inject @Parameter String param;
  }
  
  @Test
  public void testParameterPassing() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ParameterizedFactory.class).toDynamicScope(ParameterizedScope.class);
        bind(Parameterized.class).in(ParameterizedScope.class);
      }
    });
    
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    Parameterized par = fact.create("test");
    assertEquals("test", par.param);
  }
}
