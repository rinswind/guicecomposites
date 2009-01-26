package org.unseen.guice.composite.test.mocks;

public interface Request {
  String parameter();
  Connection connection();
  Response response();
}