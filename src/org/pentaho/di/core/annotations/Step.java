package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * An alternate way of defining steps. Classes annotated with "Step" are
 * automatically recognized and registered as a step.
 * 
 * Important: The XML definitions alienate annoated steps and the two methods of definition are therefore
 * mutually exclusive.
 * 
 * @author Alex Silva
 * 
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Step
{
	String[] name();

	String description() default "";

	String tooltip() default "";

	String image();

	int category();
	
	String categoryDescription() default "";
	
	String i18nPackageName() default "";
	
}
