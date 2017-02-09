/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;

public class ValueMetaInteger extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaInteger() {
    this( null );
  }

  public ValueMetaInteger( String name ) {
    super( name, ValueMetaInterface.TYPE_INTEGER );
  }

  public ValueMetaInteger( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_INTEGER, length, precision );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getInteger( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Long.class;
  }

  @Override
  public String getFormatMask() {
    String integerMask = this.conversionMask;

    if ( Utils.isEmpty( integerMask ) ) {
      if ( this.isLengthInvalidOrZero() ) {
        integerMask = DEFAULT_INTEGER_FORMAT_MASK;
        // as
        // before
        // version
        // 3.0
      } else {
        StringBuilder integerPattern = new StringBuilder();

        // First the format for positive integers...
        //
        integerPattern.append( " " );
        for ( int i = 0; i < getLength(); i++ ) {
          integerPattern.append( '0' ); // all zeroes.
        }
        integerPattern.append( ";" );

        // Then the format for the negative numbers...
        //
        integerPattern.append( "-" );
        for ( int i = 0; i < getLength(); i++ ) {
          integerPattern.append( '0' ); // all zeroes.
        }

        integerMask = integerPattern.toString();

      }
    }

    return integerMask;
  }
}
