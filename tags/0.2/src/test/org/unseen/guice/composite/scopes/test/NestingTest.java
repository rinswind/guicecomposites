package org.unseen.guice.composite.scopes.test;

import static com.google.inject.Guice.createInjector;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.Socket;

import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.ScopeAnnotation;

/**
 * @author Todor Boev
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
    
    Request handleRequest(String header);
  }
  
  public interface RequestFactory {
    Request create(String header);
  }
  
  public interface Request {
    String header();
    Connection connection();
    Response response();
  }
  
  public interface Response {
    Connection connection();
    Request request();
  }  
  
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
  
  public static class ConnectionImpl implements Connection {
    private final Socket sock;
    private final Server server;
    private final RequestFactory requests;
    
    @Inject
    public ConnectionImpl(@Arg(ConnectionScoped.class) Socket sock, Server server, RequestFactory requests) {
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

    public Request handleRequest(String header) {
      return requests.create(header);
    }
  }
  
  public static class RequestImpl implements Request {
    private final String header;
    private final Connection conn;
    private final Response resp;
    
    @Inject
    public RequestImpl(@Arg(RequestScoped.class) String header, Connection conn, Response resp) {
      this.header = header;
      this.conn = conn;
      this.resp = resp;
    }

    public String header() {
      return header;
    }
    
    public Connection connection() {
      return conn;
    }

    public Response response() {
      return resp;
    }
  }  
  
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
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        /* The ServerFactory lives in no scope and creates ServerScoped */
        bind(ServerFactory.class).toScope(ServerScoped.class);
        bind(Server.class).to(ServerImpl.class).in(ServerScoped.class);
        
        /* The ConnectionFactory lives in ServerScoped but creates ConnectionScoped */
        bind(ConnectionFactory.class).toScope(ConnectionScoped.class).in(ServerScoped.class);
        bind(Connection.class).to(ConnectionImpl.class).in(ConnectionScoped.class);
        
        /* The request factory lives in ConnectionScoped and creates RequestScoped */
        bind(RequestFactory.class).toScope(RequestScoped.class).in(ConnectionScoped.class);
        bind(Request.class).to(RequestImpl.class).in(RequestScoped.class);
        bind(Response.class).to(ResponseImpl.class).in(RequestScoped.class);
      }
    });
    
    ServerFactory fact = inj.getInstance(ServerFactory.class);
    
    Server serv = fact.create();
    
    Connection conn1 = serv.handleConnection(new Socket());
    Request req11 = conn1.handleRequest("req11");
    Request req12 = conn1.handleRequest("req12");
    
    Connection conn2 = serv.handleConnection(new Socket());
    Request req21 = conn2.handleRequest("req21");
    Request req22 = conn2.handleRequest("req22");

    assertTrue(conn1.server() == conn2.server());
    
    assertTrue(conn1 != conn2);
    
    assertTrue(conn1.socket() != conn2.socket());
    
    assertTrue(req11 != req12);
    
    assertEquals("req11", req11.header());
    assertTrue(req11.connection() == conn1);
    assertTrue(req11.response().connection() == conn1);
    /* Must use equals because Request<->Response form a loop */
    assertTrue(req11.response().request().equals(req11));
    
    assertEquals("req12", req12.header());
    assertTrue(req12.connection() == conn1);
    assertTrue(req12.response().connection() == conn1);
    assertTrue(req12.response().request().equals(req12));
    
    assertTrue(req21 != req22);
    
    assertEquals("req21", req21.header());
    assertTrue(req21.connection() == conn2);
    assertTrue(req21.response().connection() == conn2);
    assertTrue(req21.response().request().equals(req21));
 
    assertEquals("req22", req22.header());
    assertTrue(req22.connection() == conn2);
    assertTrue(req22.response().connection() == conn2);
    assertTrue(req22.response().request().equals(req22));
    
    assertTrue(req12 != req21);
    assertTrue(req21 != req22);
  }
}
