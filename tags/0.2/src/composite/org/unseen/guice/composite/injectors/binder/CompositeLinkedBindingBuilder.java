package org.unseen.guice.composite.injectors.binder;

import com.google.inject.Module;
import com.google.inject.binder.LinkedBindingBuilder;

/**
 * @author Todor Boev
 * @param <T>
 */
public interface CompositeLinkedBindingBuilder<T> extends LinkedBindingBuilder<T> {
  void toComposition(Iterable<Module> modules);
  
  void toComposition(Module... modules);
  
  void toComposition(Class<?> impl);
}
