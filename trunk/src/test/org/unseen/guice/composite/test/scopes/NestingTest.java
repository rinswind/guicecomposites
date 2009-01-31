package org.unseen.guice.composite.test.scopes;

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertTrue;
import static org.unseen.guice.composite.scopes.DynamicScopes.external;
import static org.unseen.guice.composite.scopes.DynamicScopes.factory;
import static org.unseen.guice.composite.scopes.DynamicScopes.scope;
import static org.unseen.guice.composite.scopes.DynamicScopes.parameter;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.Socket;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Parameter;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ScopeAnnotation;

/**
 * @author Todor Boev
 *
 */
public class NestingTest {
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface ServerScoped {
  }
  
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface ConnectionScoped {
  }
  
  @ScopeAnnotation
  @Retention(RUNTIME)
  @Target({TYPE, METHOD})
  @interface RequestScoped {
  }
  
  public interface ServerFactory {
    Server create();
  }
  
  public interface Server {
    Connection handleConnection(Socket sock);
  }
  
  public interface ConnectionFactory {
    Connection create(Socket sock);
  }
  
  public interface Connection {
    Socket socket();
    
    Server server();
    
    Request handleRequest();
  }
  
  public interface RequestFactory {
    Request create();
  }
  
  public interface Request {
    Connection connection();
    Response response();
  }
  
  public interface Response {
    Connection connection();
    Request request();
  }  
  
  @ServerScoped
  public static class ServerImpl implements Server {
    private final ConnectionFactory connections;
    
    @Inject 
    public ServerImpl(ConnectionFactory connections) {
      this.connections = connections;
    }

    public Connection handleConnection(Socket sock) {
      return connections.create(sock);
    }
  }
  
  @ConnectionScoped
  public static class ConnectionImpl implements Connection {
    private final Socket sock;
    private final Server server;
    private final RequestFactory requests;
    
    @Inject 
    public ConnectionImpl(@Parameter Socket sock, Server server, RequestFactory requests) {
      this.sock = sock;
      this.server = server;
      this.requests = requests;
    }

    public Socket socket() {
      return sock;
    }
    
    public Server server() {
      return server;
    }

    public Request handleRequest() {
      return requests.create();
    }

  }
  
  @RequestScoped
  public static class RequestImpl implements Request {
    private final Connection conn;
    private final Response resp;
    
    @Inject
    public RequestImpl(Connection conn, Response resp) {
      this.conn = conn;
      this.resp = resp;
    }

    public Connection connection() {
      return conn;
    }

    public Response response() {
      return resp;
    }
  }  
  
  @RequestScoped
  public static class ResponseImpl implements Response {
    private final Connection conn;
    private final Request req;
    
    @Inject
    public ResponseImpl(Connection conn, Request req) {
      this.conn = conn;
      this.req = req;
    }

    public Connection connection() {
      return conn;
    }

    public Request request() {
      return req;
    }
  }  
  
  @Test
  public void testDynamicContextNesting() {
    Injector inj = createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bindScope(ServerScoped.class, scope(ServerScoped.class));
        bindScope(ConnectionScoped.class, scope(ConnectionScoped.class));
        bindScope(RequestScoped.class, scope(RequestScoped.class));
        
        /* The ServerFactory lives in no scope and creates ServerScoped */
        bind(ServerFactory.class)
        .toProvider(factory(ServerFactory.class, ServerScoped.class));
        
        bind(Server.class).to(ServerImpl.class);
        
        /* The ConnectionFactory lives in ServerScoped but creates ConnectionScoped */
        bind(ConnectionFactory.class)
        .toProvider(factory(ConnectionFactory.class, ConnectionScoped.class))
        .in(ServerScoped.class);
        
        bind(Connection.class).to(ConnectionImpl.class);
        
        /* Define an external object added into each connection space */
        bind(Socket.class)
        .annotatedWith(parameter(""))
        .toProvider(external(Key.get(Socket.class, parameter(""))))
        .in(ConnectionScoped.class);
        
        /* The request factory lives in ConnectionScoped and creates RequestScoped */
        bind(RequestFactory.class)
        .toProvider(factory(RequestFactory.class, RequestScoped.class))
        .in(ConnectionScoped.class);
        
        bind(Request.class).to(RequestImpl.class);
        bind(Response.class).to(ResponseImpl.class);
      }
    });
    
    ServerFactory fact = inj.getInstance(ServerFactory.class);
    
    Server serv = fact.create();
    
    Connection conn1 = serv.handleConnection(new Socket());
    Request req11 = conn1.handleRequest();
    Request req12 = conn1.handleRequest();
    
    Connection conn2 = serv.handleConnection(new Socket());
    Request req21 = conn2.handleRequest();
    Request req22 = conn2.handleRequest();

    assertTrue(conn1.server() == conn2.server());
    
    assertTrue(conn1 != conn2);
    
    assertTrue(conn1.socket() != conn2.socket());
    
    assertTrue(req11 != req12);
    
    assertTrue(req11.connection() == conn1);
    assertTrue(req11.response().connection() == conn1);
    /* Must use equals because Request<->Response form a loop */
    assertTrue(req11.response().request().equals(req11));
    
    assertTrue(req12.connection() == conn1);
    assertTrue(req12.response().connection() == conn1);
    assertTrue(req12.response().request().equals(req12));
    
    assertTrue(req21 != req22);
    
    assertTrue(req21.connection() == conn2);
    assertTrue(req21.response().connection() == conn2);
    assertTrue(req21.response().request().equals(req21));
 
    assertTrue(req22.connection() == conn2);
    assertTrue(req22.response().connection() == conn2);
    assertTrue(req22.response().request().equals(req22));
    
    assertTrue(req12 != req21);
    assertTrue(req21 != req22);
  }
}
