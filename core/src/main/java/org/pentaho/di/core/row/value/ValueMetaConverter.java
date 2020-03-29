/*! ******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.plugins.IValueMetaConverter;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is intended to facilitate any needed conversions of a ValueMetaInterface field from one type to another.
 * It was initially implemented for Orc storage in the pentaho-hadoop-shims project.  This class is added here because
 * the conversions are not dependant on orc in any way.
 *
 * <p><b>Important note:</b><br/>This class is not intended to mimic the conversions that exist on PDI! It handles
 * conversions from and to Avro/Parquet/Orc format, as so it must only take in consideration those Specs!</p>
 * <p>
 * Created by tkafalas on 12/8/2017.
 */
public class ValueMetaConverter implements Serializable, IValueMetaConverter {
  private SimpleDateFormat datePattern = new SimpleDateFormat( ValueMetaBase.DEFAULT_DATE_FORMAT_MASK );
  private int precision = 0;

  public SimpleDateFormat getDatePattern() {
    return datePattern;
  }

  public void setDatePattern( SimpleDateFormat datePattern ) {
    if ( datePattern != null ) {
      this.datePattern = datePattern;
    }
  }

  public int getPrecision() {
    return precision;
  }

  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  @Override
  public Object convertFromSourceToTargetDataType( int sourceValueMetaType, int targetValueMetaType, Object value )
    throws ValueMetaConversionException {
    if ( value == null ) {
      return null;
    }

    switch ( sourceValueMetaType ) {
      case ValueMetaInterface.TYPE_INET:
        return convertFromInetMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_STRING:
        return convertFromStringMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_INTEGER:
        return convertFromIntegerMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_NUMBER:
        return convertFromNumberMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return convertFromBigNumberMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_TIMESTAMP:
        return convertFromTimestampMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_DATE:
        return convertFromDateMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_BOOLEAN:
        return convertFromBooleanMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_BINARY:
        return convertFromBinaryMetaInterface( targetValueMetaType, value );
      case ValueMetaInterface.TYPE_SERIALIZABLE:
        return convertFromSerializableMetaInterface( targetValueMetaType, value );
      default:
        throwBadConversionCombination( sourceValueMetaType, targetValueMetaType, value );
    }
    return null;
  }

  protected Object convertFromStringMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {
    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof String ) ) {
      handleConversionError(
        "Error.  Expecting value of type string.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }
    String stringValue = (String) value;

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_INET:
          try {
            return InetAddress.getByName( value.toString() );
          } catch ( UnknownHostException e ) {
            return null;
          }
        case ValueMetaInterface.TYPE_STRING:
          return new String( stringValue );
        case ValueMetaInterface.TYPE_INTEGER:
          return Long.parseLong( stripDecimal( stringValue ) );
        case ValueMetaInterface.TYPE_NUMBER:
          Double doubleValue = Double.parseDouble( stringValue );
          if ( getPrecision() > 0 ) {
            BigDecimal bigDecimal = new BigDecimal( doubleValue );
            bigDecimal = bigDecimal.setScale( getPrecision(), RoundingMode.HALF_UP );
            doubleValue = bigDecimal.doubleValue();
          }
          return doubleValue;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          return new BigDecimal( stringValue );
        case ValueMetaInterface.TYPE_TIMESTAMP:
          return new Timestamp( ( datePattern.parse( stringValue ) ).getTime() );
        case ValueMetaInterface.TYPE_DATE:
          return datePattern.parse( stringValue );
        case ValueMetaInterface.TYPE_BOOLEAN:
          return Boolean.parseBoolean( stringValue );
        case ValueMetaInterface.TYPE_BINARY:
          return stringValue.getBytes();
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_STRING, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_STRING, targetValueMetaType, value, e );
    }
    return null;
  }

  protected Object convertFromDateMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    // value is expected to be of type Date
    if ( !( value instanceof Date ) ) {
      handleConversionError(
        "Error.  Expecting value of type Date.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    try {
      Date dateValue = (Date) value;
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_INTEGER:
          return dateValue.getTime();
        case ValueMetaInterface.TYPE_STRING:
          return datePattern.format( dateValue );
        case ValueMetaInterface.TYPE_TIMESTAMP:
          return new Timestamp( dateValue.getTime() );
        case ValueMetaInterface.TYPE_DATE:
          return new Date( dateValue.getTime() );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_DATE, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_DATE, targetValueMetaType, value, e );
    }
    return null;
  }

  protected Object convertFromNumberMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }
    if ( !( value instanceof Double ) ) {
      handleConversionError(
        "Error.  Expecting value of type Double.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_STRING:
          return Double.toString( (Double) value );
        case ValueMetaInterface.TYPE_NUMBER:
          Double doubleValue = new Double( (Double) value );
          if ( getPrecision() > 0 ) {
            BigDecimal bigDecimal = new BigDecimal( doubleValue );
            bigDecimal = bigDecimal.setScale( getPrecision(), RoundingMode.HALF_UP );
            doubleValue = bigDecimal.doubleValue();
          }
          return doubleValue;
        case ValueMetaInterface.TYPE_INTEGER:
          return ( (Double) value ).longValue();
        case ValueMetaInterface.TYPE_BIGNUMBER:
          return new BigDecimal( (Double) value );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_NUMBER, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_NUMBER, targetValueMetaType, value, e );
    }
    return null;
  }

  protected Object convertFromBooleanMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof Boolean ) ) {
      handleConversionError(
        "Error.  Expecting value of type Boolean.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_STRING:
          return Boolean.toString( (Boolean) value );
        case ValueMetaInterface.TYPE_BOOLEAN:
          return new Boolean( (Boolean) value );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_BOOLEAN, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_BOOLEAN, targetValueMetaType, value, e );
    }

    return null;
  }

  protected Object convertFromIntegerMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return value;
    }

    if ( !( value instanceof Long ) ) {
      handleConversionError(
        "Error.  Expecting value of type Long.    actual value type = '" + value.getClass() + "'.    value = '" + value
          + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_STRING:
          return Long.toString( (Long) value );
        case ValueMetaInterface.TYPE_INTEGER:
          return new Long( (Long) value );
        case ValueMetaInterface.TYPE_NUMBER:
          Double doubleValue = ( (Long) value ).doubleValue();
          if ( getPrecision() > 0 ) {
            BigDecimal bigDecimal = new BigDecimal( doubleValue );
            bigDecimal = bigDecimal.setScale( getPrecision(), RoundingMode.HALF_UP );
            doubleValue = bigDecimal.doubleValue();
          }
          return doubleValue;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          return new BigDecimal( ( (Long) value ).doubleValue() );
        case ValueMetaInterface.TYPE_DATE:
          return new Date( (long) value );
        case ValueMetaInterface.TYPE_TIMESTAMP:
          return new Timestamp( (long) value );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_INTEGER, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_INTEGER, targetValueMetaType, value, e );
    }
    return value;
  }

  protected Object convertFromBigNumberMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    // value is expected to be of type BigDecimal
    if ( !( value instanceof BigDecimal ) ) {
      handleConversionError(
        "Error.  Expecting value of type BigNumber(BigDecimal).    actual value type = '" + value.getClass()
          + "'.    value = '" + value + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_STRING:
          return value.toString();
        case ValueMetaInterface.TYPE_NUMBER:
          Double doubleValue = ( (BigDecimal) value ).doubleValue();
          if ( getPrecision() > 0 ) {
            BigDecimal bigDecimal = new BigDecimal( doubleValue );
            bigDecimal = bigDecimal.setScale( getPrecision(), RoundingMode.HALF_UP );
            doubleValue = bigDecimal.doubleValue();
          }
          return doubleValue;
        case ValueMetaInterface.TYPE_BIGNUMBER:
          return new BigDecimal( ( (BigDecimal) value ).toString() );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_BIGNUMBER, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_BIGNUMBER, targetValueMetaType, value, e );
    }

    return value;
  }

  protected Object convertFromTimestampMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof Timestamp ) ) {
      handleConversionError(
        "Error.  Expecting value of type Timestamp.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    Date dateValue;
    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_STRING:
          dateValue = new Date( ( (Timestamp) value ).getTime() );
          return datePattern.format( dateValue );
        case ValueMetaInterface.TYPE_INTEGER:
          return ( (Timestamp) value ).getTime();
        case ValueMetaInterface.TYPE_TIMESTAMP:
          return new Timestamp( ( (Timestamp) value ).getTime() );
        case ValueMetaInterface.TYPE_DATE:
          return new Date( ( (Timestamp) value ).getTime() );
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_TIMESTAMP, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_TIMESTAMP, targetValueMetaType, value, e );
    }

    return value;
  }

  protected Object convertFromInetMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof InetAddress ) ) {
      handleConversionError(
        "Error.  Expecting value of type InetAddress.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    InetAddress origInetAddress = (InetAddress) value;
    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_INET:
          try {
            return InetAddress.getByName( origInetAddress.getHostAddress() );
          } catch ( UnknownHostException e ) {
            return null;
          }
        case ValueMetaInterface.TYPE_STRING:
          return origInetAddress.getHostAddress();
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_INET, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_INET, targetValueMetaType, value, e );
    }

    return null;
  }

  protected Object convertFromBinaryMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof ByteBuffer ) && !( value instanceof byte[] ) ) {
      handleConversionError(
        "Error.  Expecting value of type ByteBuffer.    actual value type = '" + value.getClass() + "'.    value = '"
          + value + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_BINARY:
          if ( value instanceof byte[] ) {
            return value;
          }
          return ( (ByteBuffer) value ).array();
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_BINARY, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_BINARY, targetValueMetaType, value, e );
    }
    return null;
  }

  protected Object convertFromSerializableMetaInterface( int targetValueMetaType, Object value )
    throws ValueMetaConversionException {

    if ( value == null ) {
      return null;
    }

    if ( !( value instanceof Serializable ) ) {
      handleConversionError(
        "Error.  Expecting value of type Serializable.    actual value type = '" + value.getClass()
          + "'.    value = '" + value + "'." );
    }

    try {
      switch ( targetValueMetaType ) {
        case ValueMetaInterface.TYPE_SERIALIZABLE:
          return value;
        default:
          throwBadConversionCombination( ValueMetaInterface.TYPE_SERIALIZABLE, targetValueMetaType, value );
      }
    } catch ( Exception e ) {
      throwErroredConversion( ValueMetaInterface.TYPE_SERIALIZABLE, targetValueMetaType, value, e );
    }
    return null;
  }

  private void throwBadConversionCombination( int sourceValueMetaType, int targetValueMetaType, Object sourceValue )
    throws ValueMetaConversionException {

    handleConversionError(
      "Error.  Can not convert from " + ValueMetaInterface.getTypeDescription( sourceValueMetaType ) + " to "
        + ValueMetaInterface.getTypeDescription( targetValueMetaType ) + ".  Actual value type = '" + sourceValue
        .getClass()
        + "'.    value = '" + sourceValue + "'." );
  }

  private void throwErroredConversion( int sourceValueMetaType, int targetValueMetaType, Object sourceValue,
                                       Exception e ) throws ValueMetaConversionException {
    handleConversionError(
      "Error trying to convert from " + ValueMetaInterface.getTypeDescription( sourceValueMetaType ) + " to "
        + ValueMetaInterface.getTypeDescription( targetValueMetaType ) + ".  value = '" + sourceValue + "'.  Error:  "
        + e.getClass() + ":  " + e.getMessage(), e );
  }

  private void handleConversionError( String errorMessage ) throws ValueMetaConversionException {
    handleConversionError( errorMessage, null );
  }

  private void handleConversionError( String errorMessage, Exception e ) throws ValueMetaConversionException {
    throw new ValueMetaConversionException( errorMessage, e );
    //      TODO - log an error message to let the user know there's a problem.  For now, return null
  }

  private String stripDecimal( String s ) {
    int decimalPosition = s.indexOf( '.' );
    return decimalPosition != -1 ? s.substring( 0, decimalPosition ) : s;
  }
}

