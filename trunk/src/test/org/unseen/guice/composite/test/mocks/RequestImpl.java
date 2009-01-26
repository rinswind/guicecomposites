package org.unseen.guice.composite.test.mocks;

import org.unseen.guice.composite.factory.Parameter;

import com.google.inject.Inject;

public class RequestImpl implements Request {
  private final String parameter;
  private final Connection conn;
  private final Response resp;
  
  @Inject
  public RequestImpl(@Parameter("parameter") String parameter, Connection conn, Response resp) {
    this.parameter = parameter;
    this.conn = conn;
    this.resp = resp;
  }

  @Override
  public String toString() {
    return "Request[ " + parameter + " ]";
  }
  
  public String parameter() {
    return parameter;
  }
  
  public Connection connection() {
    return conn;
  }

  public Response response() {
    return resp;
  }
}