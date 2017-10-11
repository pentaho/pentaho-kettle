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

package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alternate way of defining repository plugins. Classes annotated with "RepositoryPLugin" are automatically
 * recognized and registered as a repository plugin.
 *
 * Important: The XML definitions alienate annotated repository plugins and the two methods of definition are therefore
 * mutually exclusive.
 *
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface RepositoryPlugin {
  String id();

  String name();

  String description() default "";

  String metaClass();

  String i18nPackageName() default "";

  String classLoaderGroup() default "";
}
