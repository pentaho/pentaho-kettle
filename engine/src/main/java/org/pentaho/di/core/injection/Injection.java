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

package org.pentaho.di.core.injection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field that support injection should be marked by this annotation.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( { ElementType.FIELD, ElementType.METHOD } )
public @interface Injection {

  /** Injection key. */
  String name();

  /** Injection group. */
  String group() default "";

  /** Converter from RowMetaAndData to java types. */
  Class<? extends InjectionTypeConverter> converter() default DefaultInjectionTypeConverter.class;

  /** Convert empty values or not. By default, empty value doesn't change target value. */
  boolean convertEmpty() default false;

  /** Property for Required Fields **/
  boolean required() default false;
}
