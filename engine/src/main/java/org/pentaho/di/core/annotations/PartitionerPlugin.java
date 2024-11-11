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


package org.pentaho.di.core.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An alternate way of defining partiioners. Classes annotated with "PartitionerPlugin" are automatically recognized and
 * registered as a partitioner plugin.
 *
 * Important: The XML definitions alienate annoated steps and the two methods of definition are therefore mutually
 * exclusive.
 *
 * @author Alex Silva
 *
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface PartitionerPlugin {
  String id();

  String name() default "";

  String description() default "";

  String i18nPackageName() default "";

  String classLoaderGroup() default "";
}
