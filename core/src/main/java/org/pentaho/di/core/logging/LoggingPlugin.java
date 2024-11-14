/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes annotated with "LoggingPlugin" are automatically recognized and registered as a new logging channel. They are
 * added to the central logging store as listeners.
 *
 * @author matt
 *
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface LoggingPlugin {
  /**
   * @return The ID of the logging plug-in
   */
  String id();

  String classLoaderGroup() default "";

  boolean isSeparateClassLoaderNeeded() default false;

  String name() default "";
}
