package org.unseen.guice.composite.test.injectors;

import static org.junit.Assert.*;

import org.junit.Test;
import org.unseen.guice.composite.injectors.CompositeModule;
import org.unseen.guice.composite.injectors.Parameter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
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
    public BoxImpl(@Named("one") String one, @Parameter int two) {
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
  public void test() {
    Injector inj = Guice.createInjector(new CompositeModule() {
      @Override
      protected void configure() {
        bind(BoxFactory.class).toComposition(BoxImpl.class);
        bindConstant().annotatedWith(Names.named("one")).to("one");
      }
    });
    
    Box b = inj.getInstance(BoxFactory.class).create(42);
    assertEquals("one", b.one());
    assertEquals(42, b.two());
  }
}
