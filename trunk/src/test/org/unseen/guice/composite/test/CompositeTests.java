package org.unseen.guice.composite.test;

import static org.junit.Assert.assertTrue;
import static org.unseen.guice.composite.scope.CompositeScope.COMPOSITE;

import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

/**
 * @author Todor Boev
 *
 */
public class CompositeTests {
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
  
  @Test
  public void diamondTest() {
    Injector inj = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Root.class).in(COMPOSITE);
        bind(Left.class).in(COMPOSITE);
        bind(Right.class).in(COMPOSITE);
        bind(Peak.class).in(COMPOSITE);
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
