package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a Kettle Lifecycle Plugin that will be invoked during Kettle Environment
 * initialization and shutdown.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface KettleLifecyclePlugin {

  String id();

  String name() default "";

  /**
   * @return {@code true} if a separate class loader is needed every time this class is instantiated 
   */
  boolean isSeparateClassLoaderNeeded() default false;
}
