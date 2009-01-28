package org.unseen.guice.composite.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.Test;
import org.unseen.guice.composite.scope.DynamicScopes;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.ScopeAnnotation;

/**
 * @author Todor Boev
 *
 */
public class CompositeScopeTest {
  public static class Root {
    @Inject Left left;
    @Inject Left right;
  }
  
  public static class Left {
    @Inject Peak peak;
  }
  
  public static class Right {
    @Inject Peak peak;
  }
  
  public static class Peak {
  }
  
  
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface TestScope {
  }
  
  @Test
  public void diamondTest() {
    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bindScope(TestScope.class, DynamicScopes.dynamicScope(TestScope.class));
        
        bind(Root.class).in(TestScope.class);
        bind(Left.class).in(TestScope.class);
        bind(Right.class).in(TestScope.class);
        bind(Peak.class).in(TestScope.class);
      }
    });
    
    Provider<Root> prov = inj.getProvider(Root.class);
    
    Root root1 = prov.get();
    Root root2 = prov.get();
    
    assertTrue(root1.left.peak == root1.right.peak);
    
    assertTrue(root1 != root2);
    assertTrue(root1.left != root2.left);
    assertTrue(root1.right != root2.right);
    assertTrue(root1.left.peak != root2.left.peak);
  }
}
