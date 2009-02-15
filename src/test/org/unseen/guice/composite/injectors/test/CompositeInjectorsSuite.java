package org.unseen.guice.composite.injectors.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  NestingTest.class,
  CurriedConstructorTest.class
})
public class CompositeInjectorsSuite {

}
