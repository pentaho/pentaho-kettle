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
 * An alternate way of defining steps. Classes annotated with "Step" are automatically recognized and registered as a
 * step.
 *
 * Important: The XML definitions alienate annotated steps and the two methods of definition are therefore mutually
 * exclusive.
 *
 * @author Alex Silva
 *
 */
@Documented
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface Step {

  /**
   * @return The ID of the step. You can specify more than one ID in a comma separated format: id1,id2,id3 for
   *         deprecation purposes.
   */
  String id();

  String name();

  String description() default "";

  /**
   * @return The image resource path
   */
  String image() default "";

  /**
   * @return True if a separate class loader is needed every time this class is instantiated
   */
  boolean isSeparateClassLoaderNeeded() default false;

  String classLoaderGroup() default "";

  String categoryDescription() default "";

  String i18nPackageName() default "";

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

  String suggestion() default "";
}
