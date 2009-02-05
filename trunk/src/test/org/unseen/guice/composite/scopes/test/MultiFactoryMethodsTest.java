package org.unseen.guice.composite.scopes.test;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.Socket;

import org.junit.Test;
import org.unseen.guice.composite.scopes.DynamicScopesModule;
import org.unseen.guice.composite.scopes.Parameter;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

public class MultiFactoryMethodsTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface ConnectionScoped {
  }
  
  interface ConnectionFactory {
    Connection create(Socket socket);
    Connection create(Socket socket, @Parameter("setting") String setting);
  }
  
  public static class Connection {
    @Inject Socket socket;
    @Inject @Parameter("setting") String setting;
  }
  
  @Test 
  public void multiFactoryMethodsTest() {
    Injector inj = Guice.createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ConnectionFactory.class).toDynamicScope(ConnectionScoped.class);
        bind(Connection.class).in(ConnectionScoped.class);
      }
    });
    
    ConnectionFactory fact = inj.getInstance(ConnectionFactory.class);
    fact.create(new Socket());
    fact.create(new Socket(), "test");
  }
}
