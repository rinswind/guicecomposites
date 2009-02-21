package org.unseen.guice.composite.scopes;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotates an injected parameter or field whose value comes from an argument
 * to a factory method.
 */
@BindingAnnotation
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface Arg {
  String name() default "";
  
  Class<? extends Annotation> value() default AnonymousScope.class;
}