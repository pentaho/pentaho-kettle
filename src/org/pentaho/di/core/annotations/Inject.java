package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Allows for properties to be dynamically injected into classes during runtime.  
 * 
 * Both methods and fields can be annotated.
 * 
 * @author Alex Silva
 *
 */
@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject
{
	/**
	 * The name of the property to be injected.
	 * Default values:
	 * <ol><li>
	 * <u>Fields:</u> The name of the annotated field.  For instance, annotating a field named "callerId" with @Inject has the same effect as
	 * annotating this field with @Inject("callerId")</li>
	 * <li><u>Methods:</u>The name of the property being set by the method, as defined by <code>Introspector.decapitalize</code> and 
	 * the Java Beans API.</li>
	 * </ol>
	 * @return
	 */
	String property() default "";
}
