/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.plugins;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation describes extra plugin-specific class types to be managed by the registry.
 * <p>
 * The type, implementation and nodeName arrays are correlated 1-to-1.
 *
 * @author nbaker
 *
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface PluginClassTypeMapping {
  /**
   * Returns an array of class types that the PluginType will track and respond to. these classes are ususaly interfaces
   * and the implementation class needs to decend from them
   *
   * @return array of class types
   */
  Class<?>[] classTypes();

  /**
   * Returns as array of implementations that correspond to the class types in the Annotation
   *
   * @return
   */
  Class<?>[] implementationClass() default {
  // Empty
  };

}
