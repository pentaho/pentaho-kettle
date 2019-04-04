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
  public String string2string( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public int string2intPrimitive( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Integer string2integer( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public long string2longPrimitive( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Long string2long( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public boolean string2booleanPrimitive( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Boolean string2boolean( String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Enum<?> string2enum( Class<?> enumClass, String v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public String boolean2string( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public int boolean2intPrimitive( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Integer boolean2integer( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public long boolean2longPrimitive( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Long boolean2long( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public boolean boolean2booleanPrimitive( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Boolean boolean2boolean( Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Enum<?> boolean2enum( Class<?> enumClass, Boolean v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public String integer2string( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public int integer2intPrimitive( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Integer integer2integer( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public long integer2longPrimitive( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Long integer2long( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public boolean integer2booleanPrimitive( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Boolean integer2boolean( Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Enum<?> integer2enum( Class<?> enumClass, Long v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public String number2string( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public int number2intPrimitive( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Integer number2integer( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public long number2longPrimitive( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Long number2long( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public boolean number2booleanPrimitive( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Boolean number2boolean( Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }

  public Enum<?> number2enum( Class<?> enumClass, Double v ) throws KettleValueException {
    throw new RuntimeException( "Conversion not supported" );
  }
}
