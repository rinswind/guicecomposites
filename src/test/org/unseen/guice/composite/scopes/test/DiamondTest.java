package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scopes.DynamicScopes;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

/**
 * @author Todor Boev
 *
 */
public class DiamondTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface DiamondScoped {
  }
  
  @DiamondScoped
  public static class Root {
    @Inject Left left;
    @Inject Left right;
  }
  
  @DiamondScoped
  public static class Left {
    @Inject Peak peak;
  }
  
  @DiamondScoped
  public static class Right {
    @Inject Peak peak;
  }
  
  @DiamondScoped
  public static class Peak {
  }
  
  public interface DiamondFactory {
    Root create();
  }
  
  @Test
  public void diamondTest() {
    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        DynamicScopes.bindScope(binder(), DiamondScoped.class);
        DynamicScopes.bindFactory(binder(), DiamondFactory.class, DiamondScoped.class);
      }
    });
    
    DiamondFactory fact = inj.getInstance(DiamondFactory.class);
    
    Root root1 = fact.create();
    Root root2 = fact.create();
    
    assertTrue(root1.left.peak == root1.right.peak);
    
    assertTrue(root1 != root2);
    assertTrue(root1.left != root2.left);
    assertTrue(root1.right != root2.right);
    assertTrue(root1.left.peak != root2.left.peak);
  }
}
