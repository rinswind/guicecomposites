package org.unseen.guice.composite.injectors;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotates an injected parameter or field whose value comes from an argument
 * to a factory method.
 */
@BindingAnnotation
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Parameter {
  String value() default "";
}