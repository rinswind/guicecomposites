package org.unseen.guice.composite.scopes.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.unseen.guice.composite.scopes.AnonymousScope;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.Args;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class CurriedConstructorTest {
  public interface BoxFactory { 
    Box create(int two);
  }
  
  public interface Box {
    String one();
    int two();
  }
  
  public static class BoxImpl implements Box {
    private final String one;
    private final int two;
    
    @Inject
    public BoxImpl(@Named("one") String one, @Arg int two) {
      this.one = one;
      this.two = two;
    }
    
    public String one() {
      return one;
    }

    public int two() {
      return two;
    }
  }
  
  @Test
  public void testConstructors() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toClassScope(BoxImpl.class);
        bindConstant().annotatedWith(Names.named("one")).to("one");
      }
    });
    
    BoxFactory fact = inj.getInstance(BoxFactory.class);
    
    Box b1 = fact.create(42);
    Box b2 = fact.create(666);
    
    assertTrue(b1 != b2);
    
    assertEquals("one", b1.one());
    assertEquals(42, b1.two());
    
    assertEquals("one", b2.one());
    assertEquals(666, b2.two());
  }
  
  @Test(expected = ConfigurationException.class)
  public void testPrivateParameters() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toClassScope(BoxImpl.class);
        bindConstant().annotatedWith(Names.named("one")).to("one");
      }
    });
    
    inj.getBinding(Key.get(Integer.class, Args.arg(AnonymousScope.class)));
  }
  
  @Ignore
  @Test(expected = ConfigurationException.class)
  public void testPrivateImplemetation() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toClassScope(BoxImpl.class);
        bindConstant().annotatedWith(Names.named("one")).to("one");
      }
    });
    
    inj.getBinding(Key.get(BoxImpl.class));
  }
}
