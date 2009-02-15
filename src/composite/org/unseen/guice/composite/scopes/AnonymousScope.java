package org.unseen.guice.composite.scopes;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * Used as the default scope tag for One-Class Dynamic Scopes created via
 * <code>
 *   bind(Factory.class).toClassScope(Product.class);
 * </code>
 *  
 * @author Todor Boev
 */
@ScopeAnnotation
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface AnonymousScope {
}
