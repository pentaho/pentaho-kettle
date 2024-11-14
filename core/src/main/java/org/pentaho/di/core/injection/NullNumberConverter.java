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


package org.pentaho.di.core.injection;

import org.apache.commons.lang.StringUtils;

/**
 * An extension to the {@link DefaultInjectionTypeConverter} that converts null and empty values to -1 for
 * number/int type variables. This ensures that 0's aren't inserted for field values that are meant to remain empty.
 * This assumes that the step dialog knows to treat field values set to -1 as "empty".
 */
public class NullNumberConverter extends DefaultInjectionTypeConverter {

  @Override
  public int string2intPrimitive( final String v ) {
    return StringUtils.isBlank( v ) ? -1 : Integer.parseInt( v );
  }

  @Override
  public int integer2intPrimitive( final Long v ) {
    return v == null ? -1 : v.intValue();
  }

  @Override
  public int number2intPrimitive( final Double v ) {
    return v == null ? -1 : Math.round( v.floatValue() );
  }
}
