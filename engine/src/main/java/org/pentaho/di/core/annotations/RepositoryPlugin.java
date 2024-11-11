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
