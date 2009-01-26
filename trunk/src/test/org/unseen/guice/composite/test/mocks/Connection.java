package org.unseen.guice.composite.test.mocks;

public interface Connection {
  String host();
  Server server();
  Request handleRequest(String parameter);
}