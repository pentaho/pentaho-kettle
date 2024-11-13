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

import org.pentaho.di.core.exception.KettleValueException;

/**
 * Base class converter for metadata injection.
 * 
 * Developer can extend it for own converter implementation.
 * 
 * @author Alexander Buloichik
 */
public class InjectionTypeConverter {
  private static final String CONVERSION_UNSUPPORTED = "Conversion not supported";

  public String string2string( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public int string2intPrimitive( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Integer string2integer( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public long string2longPrimitive( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Long string2long( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public boolean string2booleanPrimitive( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Boolean string2boolean( String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Enum<?> string2enum( Class<?> enumClass, String v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public String boolean2string( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public int boolean2intPrimitive( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Integer boolean2integer( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public long boolean2longPrimitive( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Long boolean2long( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public boolean boolean2booleanPrimitive( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Boolean boolean2boolean( Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Enum<?> boolean2enum( Class<?> enumClass, Boolean v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public String integer2string( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public int integer2intPrimitive( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Integer integer2integer( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public long integer2longPrimitive( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Long integer2long( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public boolean integer2booleanPrimitive( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Boolean integer2boolean( Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Enum<?> integer2enum( Class<?> enumClass, Long v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public String number2string( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public int number2intPrimitive( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Integer number2integer( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public long number2longPrimitive( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Long number2long( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public boolean number2booleanPrimitive( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Boolean number2boolean( Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }

  public Enum<?> number2enum( Class<?> enumClass, Double v ) throws KettleValueException {
    throw new UnsupportedOperationException( CONVERSION_UNSUPPORTED );
  }
}
