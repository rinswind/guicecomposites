package org.unseen.guice.composite.factory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.AnnotatedConstantBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.matcher.Matcher;
import com.google.inject.spi.Message;
import com.google.inject.spi.TypeConverter;

/**
 * @author Todor Boev
 *
 */
public abstract class CompositionModule implements Module {
  private Binder binder;

  public final synchronized void configure(Binder builder) {
    if (this.binder != null) {
      throw new IllegalStateException("Re-entry is not allowed.");
    }

    this.binder = builder;
    try {
      configure();
    } finally {
      this.binder = null;
    }
  }

  protected abstract void configure();

  protected Binder binder() {
    return binder;
  }

  protected void bindScope(Class<? extends Annotation> scopeAnnotation,
      Scope scope) {
    binder.bindScope(scopeAnnotation, scope);
  }

  protected <T> CompositionLinkedBindingBuilder<T> bind(Key<T> key) {
    return new binder.bind(key);
  }

  protected <T> CompositionAnnotatedBindingBuilder<T> bind(TypeLiteral<T> typeLiteral) {
    return binder.bind(typeLiteral);
  }

  protected <T> CompositionAnnotatedBindingBuilder<T> bind(Class<T> clazz) {
    return binder.bind(clazz);
  }

  protected AnnotatedConstantBindingBuilder bindConstant() {
    return binder.bindConstant();
  }

  protected void install(Module module) {
    binder.install(module);
  }

  protected void addError(String message, Object... arguments) {
    binder.addError(message, arguments);
  }

  protected void addError(Throwable t) {
    binder.addError(t);
  }

  protected void addError(Message message) {
    binder.addError(message);
  }

  protected void requestInjection(Object... objects) {
    binder.requestInjection(objects);
  }

  protected void requestStaticInjection(Class<?>... types) {
    binder.requestStaticInjection(types);
  }

  protected void bindInterceptor(Matcher<? super Class<?>> classMatcher,
      Matcher<? super Method> methodMatcher,
      MethodInterceptor... interceptors) {
    binder.bindInterceptor(classMatcher, methodMatcher, interceptors);
  }

  protected void requireBinding(Key<?> key) {
    binder.getProvider(key);
  }

  protected void requireBinding(Class<?> type) {
    binder.getProvider(type);
  }

  protected <T> Provider<T> getProvider(Key<T> key) {
    return binder.getProvider(key);
  }

  protected <T> Provider<T> getProvider(Class<T> type) {
    return binder.getProvider(type);
  }

  protected void convertToTypes(Matcher<? super TypeLiteral<?>> typeMatcher,
      TypeConverter converter) {
    binder.convertToTypes(typeMatcher, converter);
  }

  protected Stage currentStage() {
    return binder.currentStage();
  }
}
