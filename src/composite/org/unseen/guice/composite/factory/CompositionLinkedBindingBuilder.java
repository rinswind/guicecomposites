package org.unseen.guice.composite.factory;

import com.google.inject.Module;
import com.google.inject.binder.LinkedBindingBuilder;

/**
 * @author Todor Boev
 *
 */
public interface CompositionLinkedBindingBuilder<T> extends LinkedBindingBuilder<T> {
  void toComposition(Iterable<Module> modules);
  
  void toComposition(Module... modules);
}
