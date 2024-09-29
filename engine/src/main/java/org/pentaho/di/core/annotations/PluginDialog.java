/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
