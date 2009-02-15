package org.unseen.guice.composite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.unseen.guice.composite.injectors.test.CompositeInjectorsSuite;
import org.unseen.guice.composite.scopes.test.CompositeScopesSuite;

@RunWith(Suite.class)
@SuiteClasses({
  CompositeInjectorsSuite.class,
  CompositeScopesSuite.class
})
public class AllTests {

}
