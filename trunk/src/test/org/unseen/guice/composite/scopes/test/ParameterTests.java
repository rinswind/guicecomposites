package org.unseen.guice.composite.scopes.test;

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.DynamicScopesModule;
import org.unseen.guice.composite.scopes.Parameter;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;
import com.google.inject.internal.Nullable;

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
    final String param;
    
    @Inject     
    public Parameterized(@Nullable @Parameter String param) {
      this.param = param;
    }
  }
  
  @Test
  public void testParameter() {
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
  
  @Test
  public void testNullParameter() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ParameterizedFactory.class).toDynamicScope(ParameterizedScope.class);
        bind(Parameterized.class).in(ParameterizedScope.class);
      }
    });
    
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    Parameterized par = fact.create(null);
    assertNull(par.param);
  }
}
