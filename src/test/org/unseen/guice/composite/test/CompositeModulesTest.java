package org.unseen.guice.composite.test;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.Scopes.SINGLETON;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.unseen.guice.composite.factory.CompositeModule;
import org.unseen.guice.composite.test.mocks.Connection;
import org.unseen.guice.composite.test.mocks.ConnectionFactory;
import org.unseen.guice.composite.test.mocks.ConnectionImpl;
import org.unseen.guice.composite.test.mocks.Request;
import org.unseen.guice.composite.test.mocks.RequestFactory;
import org.unseen.guice.composite.test.mocks.RequestImpl;
import org.unseen.guice.composite.test.mocks.Response;
import org.unseen.guice.composite.test.mocks.ResponseImpl;
import org.unseen.guice.composite.test.mocks.Server;
import org.unseen.guice.composite.test.mocks.ServerImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;

public class CompositeModulesTest {
  @Test
  public void testRuntimeScopes() {
    Injector inj = createInjector(new CompositeModule() {
      @Override
      protected void configure() {
        bind(Server.class).to(ServerImpl.class).in(SINGLETON);
        
        bind(ConnectionFactory.class).toComposition(new CompositeModule() {
          @Override
          protected void configure() {
            bind(Connection.class).to(ConnectionImpl.class).in(SINGLETON);
            
            bind(RequestFactory.class).toComposition(new AbstractModule() {
              @Override
              protected void configure() {
                bind(Request.class).to(RequestImpl.class).in(SINGLETON);
                bind(Response.class).to(ResponseImpl.class).in(SINGLETON);
              }
            });
          }
        });
      }
    });
    
    Server serv = inj.getInstance(Server.class);
    
    Connection conn1 = serv.handleConnection("host1");
    Connection conn2 = serv.handleConnection("host2");
    
    Request req11 = conn1.handleRequest("request11");
    Request req12 = conn1.handleRequest("request12");
    
    Request req21 = conn2.handleRequest("request21");
    Request req22 = conn2.handleRequest("request22");

    assertTrue(conn1.server() == conn2.server());
    
    assertTrue(conn1 != conn2);
    
    assertEquals("host1", conn1.host());
    
    assertTrue(req11 != req12);
    
    assertEquals("request11", req11.parameter());
    assertTrue(req11.connection() == conn1);
    assertTrue(req11.response().connection() == conn1);
    /* Must use equals because Request<->Response form a loop */
    assertTrue(req11.response().request().equals(req11));
    
    assertEquals("request12", req12.parameter());
    assertTrue(req12.connection() == conn1);
    assertTrue(req12.response().connection() == conn1);
    assertTrue(req12.response().request().equals(req12));
    
    assertEquals("host2", conn2.host());
    
    assertTrue(req21 != req22);
    
    assertEquals("request21", req21.parameter());
    assertTrue(req21.connection() == conn2);
    assertTrue(req21.response().connection() == conn2);
    assertTrue(req21.response().request().equals(req21));
 
    assertEquals("request22", req22.parameter());
    assertTrue(req22.connection() == conn2);
    assertTrue(req22.response().connection() == conn2);
    assertTrue(req22.response().request().equals(req22));
    
    assertTrue(req12 != req21);
    assertTrue(req21 != req22);
  }
}
