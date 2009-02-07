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
import org.unseen.guice.composite.scopes.Parameter;
import org.unseen.guice.composite.scopes.edsl.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.ScopeAnnotation;
import com.google.inject.internal.Nullable;

public class ParameterTests {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({METHOD, TYPE})
  @interface ParameterizedScope {
  }
  
  interface ParameterizedFactory {
    Parameterized create(@Parameter("a") String a, @Parameter("b") String b);
  }
  
  public static class Parameterized {
    final String a;
    final String b;
    
    @Inject     
    public Parameterized(@Parameter("a") String a, @Nullable @Parameter("b") String b) {
      this.a = a;
      this.b = b;
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
    Parameterized par = fact.create("a", "b");
    assertEquals("a", par.a);
    assertEquals("b", par.b);
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
    Parameterized par = fact.create("a", null);
    assertEquals("a", par.a);
    assertNull(par.b);
  }
  
  @Test(expected = ProvisionException.class)
  public void testUnannotatedNullParameter() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ParameterizedFactory.class).toDynamicScope(ParameterizedScope.class);
        bind(Parameterized.class).in(ParameterizedScope.class);
      }
    });
    
    ParameterizedFactory fact = inj.getInstance(ParameterizedFactory.class);
    fact.create(null, "b");
  }
}
