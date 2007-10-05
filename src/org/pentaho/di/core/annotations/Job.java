package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.pentaho.di.job.JobEntryType;

/**
 * An alternative when defining Jobs. Classes annotated with "Job" are
 * automatically recognized and registered as a job.
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
public @interface Job
{
	String id();

	String tooltip() default "";

	String image();

	JobEntryType type();
	
	String version() default "";
}
