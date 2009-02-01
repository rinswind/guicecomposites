package org.unseen.guice.composite.scopes.test;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

import static java.lang.annotation.RetentionPolicy.*;
import static java.lang.annotation.ElementType.*;

import static org.unseen.guice.composite.scopes.DynamicScopes.*;

import static junit.framework.Assert.*;

public class DisjointGraphsTest {
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
    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bindScope(HorizontalScoped.class, scope(HorizontalScoped.class));
        bindScope(VerticalScoped.class, scope(VerticalScoped.class));
        
        bind(CenterFactory.class).toProvider(factory(CenterFactory.class, HorizontalScoped.class));
        
        bind(TopFactory.class).toProvider(factory(TopFactory.class, VerticalScoped.class)).in(HorizontalScoped.class);
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
