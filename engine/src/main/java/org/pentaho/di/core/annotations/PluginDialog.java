/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alternate way of defining plugin's dialog. Classes annotated with "PluginDialog" are automatically recognized and added
 * to the corresponding plugin's classloader.
 *
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface PluginDialog {

  enum PluginType {
    STEP, JOBENTRY
  }
  /**
   * @return The ID of the plugin.
   */
  String id();

  /**
   * @return The image resource path
   */
  String image();

  /**
   * @return The documentation url
   */
  String documentationUrl() default "";

  /**
   * @return The cases url
   */
  String casesUrl() default "";

  /**
   * @return The forum url
   */
  String forumUrl() default "";

  /**
   * @return The plugin type
   */
  PluginType pluginType();
}
