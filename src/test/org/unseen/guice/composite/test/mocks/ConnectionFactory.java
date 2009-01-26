package org.unseen.guice.composite.test.mocks;

import org.unseen.guice.composite.factory.Parameter;

public interface ConnectionFactory {
  Connection create(@Parameter("host") String host);
}