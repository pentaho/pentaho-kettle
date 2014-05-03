/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import java.math.BigDecimal;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBigNumber extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaBigNumber() {
    this( null );
  }

  public ValueMetaBigNumber( String name ) {
    super( name, ValueMetaInterface.TYPE_BIGNUMBER );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getBigNumber( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return BigDecimal.class;
  }

  @Override protected void compareStorageAndActualFormat() {
    if ( storageMetadata == null ) {
      identicalFormat = true;
    } else {

      // If a trim type is set, we need to at least try to trim the strings.
      // In that case, we have to set the identical format off.
      //
      if ( trimType != TRIM_TYPE_NONE ) {
        identicalFormat = false;
      } else {

        // If there is a string encoding set and it's the same encoding in the
        // binary string, then we don't have to convert
        // If there are no encodings set, then we're certain we don't have to
        // convert as well.
        //
        if ( getStringEncoding() != null
          && getStringEncoding().equals( storageMetadata.getStringEncoding() ) || getStringEncoding() == null
          && storageMetadata.getStringEncoding() == null ) {

          // However, perhaps the conversion mask changed since we read the
          // binary string?
          // The output can be different from the input. If the mask is
          // different, we need to do conversions.
          // Otherwise, we can just ignore it...
          //
          if ( isDate() ) {
            if ( ( getConversionMask() != null && getConversionMask().equals( storageMetadata.getConversionMask() ) )
              || ( getConversionMask() == null && storageMetadata.getConversionMask() == null ) ) {
              identicalFormat = true;
            } else {
              identicalFormat = false;
            }
          } else if ( isNumeric() ) {
            // Check the lengths first
            //
            if ( getLength() != storageMetadata.getLength() ) {
              identicalFormat = false;
            } else if ( getPrecision() != storageMetadata.getPrecision() ) {
              identicalFormat = false;
            } else if ( ( getConversionMask() != null
              && getConversionMask().equals( storageMetadata.getConversionMask() )
              || ( getConversionMask() == null && storageMetadata.getConversionMask() == null ) ) ) {
              // For the same reasons as above, if the conversion mask, the
              // decimal or the grouping symbol changes
              // we need to convert from the binary strings to the target data
              // type and then back to a string in the required format.
              //
              if ( ( getGroupingSymbol() != null && getGroupingSymbol().equals(
                storageMetadata.getGroupingSymbol() ) )
                || ( getConversionMask() == null && storageMetadata.getConversionMask() == null ) ) {
                if ( ( getDecimalFormat( true ) != null && getDecimalFormat( true ).equals(
                  storageMetadata.getDecimalFormat( true ) ) )
                  || ( getDecimalFormat( true ) == null && storageMetadata.getDecimalFormat( true ) == null ) ) {
                  identicalFormat = true;
                } else {
                  identicalFormat = false;
                }
              } else {
                identicalFormat = false;
              }
            } else {
              identicalFormat = false;
            }
          }
        }
      }
    }
  }
}
