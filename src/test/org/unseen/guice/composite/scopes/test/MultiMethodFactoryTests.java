package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.Parameter;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;
import com.google.inject.internal.Nullable;

public class MultiMethodFactoryTests {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface ConnectionScoped {
  }
  
  interface BoxFactory {
    IntegerBox create(Integer num);
    
    StringBox create(String str);
    
    CombinedBox create(Integer num, String str);
  }
  
  public static class IntegerBox {
    final Integer num;
    
    @Inject
    public IntegerBox(@Nullable @Parameter Integer num) {
      this.num = num;
    }
  }
  
  public static class StringBox {
    final String str;
    
    @Inject
    public StringBox(@Nullable @Parameter String str) {
      this.str = str;
    }
  }
  
  public static class CombinedBox {
    @Inject IntegerBox numBox;
    @Inject StringBox strBox;
  }
  
  @Test 
  public void testMultipleProducts() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toDynamicScope(ConnectionScoped.class);
        bind(IntegerBox.class).in(ConnectionScoped.class);
        bind(StringBox.class).in(ConnectionScoped.class);
        bind(CombinedBox.class).in(ConnectionScoped.class);
      }
    });
    
    BoxFactory fact = inj.getInstance(BoxFactory.class);
    
    IntegerBox ib = fact.create(42);
    assertTrue(42 == ib.num);
    
    StringBox sb = fact.create("42");
    assertEquals("42", sb.str);
    
    CombinedBox cb = null;
    
    cb = fact.create(42, "42");
    assertTrue(42 == cb.numBox.num);
    assertEquals("42", cb.strBox.str);
    
    cb = fact.create(null, "42");
    assertNull(cb.numBox.num);
    assertEquals("42", cb.strBox.str);
    
    cb = fact.create(42, null);
    assertTrue(42 == cb.numBox.num);
    assertNull(null, cb.strBox.str);
  }
}
