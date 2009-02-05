package org.unseen.guice.composite.scopes.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  DiamondTest.class, 
  DisjointContextTest.class, 
  NestingTest.class, 
  ValidationTest.class, 
  MultiFactoryMethodsTest.class
})
public class DynamicScopesSuite {
}
