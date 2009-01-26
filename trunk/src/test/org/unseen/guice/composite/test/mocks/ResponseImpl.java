package org.unseen.guice.composite.test.mocks;

import com.google.inject.Inject;

public class ResponseImpl implements Response {
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