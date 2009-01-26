package org.unseen.guice.composite.test.mocks;

import org.unseen.guice.composite.factory.Parameter;


public interface RequestFactory {
  Request create(@Parameter("parameter") String parameter);
}