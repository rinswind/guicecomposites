package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

public class DisjointContextTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface HorizontalScoped {  
  }
  
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface VerticalScoped {  
  }
  
  public interface TopFactory {
    Top create();
  }
  
  public interface CenterFactory {
    Center create();
  }
  
  @HorizontalScoped
  public static class Left {
  }
  
  @HorizontalScoped
  public static class Right {
  }
  
  @HorizontalScoped
  public static class Center {
    @Inject TopFactory tops;
  }
  
  @VerticalScoped
  public static class Top {
    @Inject Left left;
    @Inject Right right;
  }
  
  @Test
  public void testDisjointScopes() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(CenterFactory.class).toScope(HorizontalScoped.class);
        bind(TopFactory.class).toScope(VerticalScoped.class).in(HorizontalScoped.class);
      }
    });
    
    CenterFactory fact = inj.getInstance(CenterFactory.class);
    
    Center center = fact.create();
   
    Top top1 = center.tops.create();
    Top top2 = center.tops.create();
    
    assertTrue(top1 != top2);
    assertTrue(top1.left == top2.left);
    assertTrue(top1.right == top2.right);
  }
}
