package org.unseen.guice.composite.test.mocks;

import org.unseen.guice.composite.factory.Parameter;

import com.google.inject.Inject;

public class ConnectionImpl implements Connection {
  private final String host;
  private final Server server;
  private final RequestFactory requests;
  
  @Inject 
  public ConnectionImpl(@Parameter("host") String host, Server server, RequestFactory requests) {
    this.host = host;
    this.server = server;
    this.requests = requests;
  }

  @Override
  public String toString() {
    return "Connection[ " + host + " ]";
  }
  
  public String host() {
    return host;
  }

  public Server server() {
    return server;
  }

  public Request handleRequest(String parameter) {
    return requests.create(parameter);
  }
}