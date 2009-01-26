package org.unseen.guice.composite.test.mocks;

import com.google.inject.Inject;

public class ServerImpl implements Server {
  private final ConnectionFactory connections;
  
  @Inject 
  public ServerImpl(ConnectionFactory connections) {
    this.connections = connections;
  }

  public Connection handleConnection(String host) {
    return connections.create(host);
  }
}