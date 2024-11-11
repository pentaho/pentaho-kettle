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

import org.pentaho.di.core.exception.KettleValueException;

/**
 * Default type converter for metadata injection.
 * 
 * @author Alexander Buloichik
 */
public class DefaultInjectionTypeConverter extends InjectionTypeConverter {
  @Override
  public String string2string( String v ) {
    return v;
  }

  @Override
  public int string2intPrimitive( String v ) {
    return Integer.parseInt( v );
  }

  @Override
  public Integer string2integer( String v ) {
    return v == null ? null : string2intPrimitive( v );
  }

  @Override
  public long string2longPrimitive( String v ) {
    return Long.parseLong( v );
  }

  @Override
  public Long string2long( String v ) {
    return v == null ? null : string2longPrimitive( v );
  }

  @Override
  public boolean string2booleanPrimitive( String v ) {
    return "Y".equalsIgnoreCase( v ) || "Yes".equalsIgnoreCase( v ) || "true".equalsIgnoreCase( v );
  }

  @Override
  public Boolean string2boolean( String v ) {
    return v == null ? null : string2booleanPrimitive( v );
  }

  @Override
  public Enum<?> string2enum( Class<?> enumClass, String v ) throws KettleValueException {
    if ( v == null ) {
      return null;
    }
    for ( Object eo : enumClass.getEnumConstants() ) {
      Enum<?> e = (Enum<?>) eo;
      if ( e.name().equals( v ) ) {
        return e;
      }
    }
    throw new KettleValueException( "Unknown value " + v + " for enum " + enumClass );
  }

  @Override
  public String boolean2string( Boolean v ) throws KettleValueException {
    if ( v == null ) {
      return null;
    }
    return v ? "Y" : "N";
  }

  @Override
  public int boolean2intPrimitive( Boolean v ) throws KettleValueException {
    return v ? 1 : 0;
  }

  @Override
  public Integer boolean2integer( Boolean v ) throws KettleValueException {
    return v == null ? null : boolean2intPrimitive( v );
  }

  @Override
  public long boolean2longPrimitive( Boolean v ) throws KettleValueException {
    return v ? 1 : 0;
  }

  @Override
  public Long boolean2long( Boolean v ) throws KettleValueException {
    return v == null ? null : boolean2longPrimitive( v );
  }

  @Override
  public boolean boolean2booleanPrimitive( Boolean v ) throws KettleValueException {
    return v.booleanValue();
  }

  @Override
  public Boolean boolean2boolean( Boolean v ) throws KettleValueException {
    return v;
  }

  @Override
  public String integer2string( Long v ) throws KettleValueException {
    if ( v == null ) {
      return null;
    }
    return v.toString();
  }

  @Override
  public int integer2intPrimitive( Long v ) throws KettleValueException {
    return v.intValue();
  }

  @Override
  public Integer integer2integer( Long v ) throws KettleValueException {
    return v == null ? null : integer2intPrimitive( v );
  }

  @Override
  public long integer2longPrimitive( Long v ) throws KettleValueException {
    return v.longValue();
  }

  @Override
  public Long integer2long( Long v ) throws KettleValueException {
    return v == null ? null : integer2longPrimitive( v );
  }

  @Override
  public boolean integer2booleanPrimitive( Long v ) throws KettleValueException {
    return v.longValue() != 0;
  }

  @Override
  public Boolean integer2boolean( Long v ) throws KettleValueException {
    return v == null ? null : integer2booleanPrimitive( v );
  }

  @Override
  public String number2string( Double v ) throws KettleValueException {
    if ( v == null ) {
      return null;
    }
    return v.toString();
  }

  @Override
  public int number2intPrimitive( Double v ) throws KettleValueException {
    return Math.round( v.floatValue() );
  }

  @Override
  public Integer number2integer( Double v ) throws KettleValueException {
    return v == null ? null : number2intPrimitive( v );
  }

  @Override
  public long number2longPrimitive( Double v ) throws KettleValueException {
    return Math.round( v );
  }

  @Override
  public Long number2long( Double v ) throws KettleValueException {
    return v == null ? null : number2longPrimitive( v );
  }

  @Override
  public boolean number2booleanPrimitive( Double v ) throws KettleValueException {
    return number2intPrimitive( v ) != 0;
  }

  @Override
  public Boolean number2boolean( Double v ) throws KettleValueException {
    return v == null ? null : number2booleanPrimitive( v );
  }
}
