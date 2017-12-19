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

package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;

/**
 * This class contains a Value of type String and the length by which it is described.
 *
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueString implements ValueInterface, Cloneable {
  private String string;
  private int length;

  private static final ThreadLocal<SimpleDateFormat> LOCAL_SIMPLE_DATE_PARSER =
    new ThreadLocal<SimpleDateFormat>() {
      @Override
      protected SimpleDateFormat initialValue() {
        return new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
      }
    };

  public ValueString() {
    string = null;
    length = -1;
  }

  public ValueString( String string ) {
    this.string = string;
    length = -1;
  }

  @Override
  public int getType() {
    return Value.VALUE_TYPE_STRING;
  }

  @Override
  public String getTypeDesc() {
    return "String";
  }

  @Override
  public String getString() {
    return this.string;
  }

  @Override
  public double getNumber() {
    return Const.toDouble( string, 0.0 );
  }

  @Override
  public Date getDate() {
    if ( string != null ) {
      try {
        return LOCAL_SIMPLE_DATE_PARSER.get().parse( string );
      } catch ( ParseException e ) {
        return null;
      }
    }
    return null;
  }

  @Override
  public boolean getBoolean() {
    return "Y".equalsIgnoreCase( string )
      || "TRUE".equalsIgnoreCase( string ) || "YES".equalsIgnoreCase( string ) || "1".equalsIgnoreCase( string );
  }

  @Override
  public long getInteger() {
    return Const.toLong( Const.ltrim( string ), 0L ); // Remove the leading space to make "int to string to int"
                                                      // conversion possible.
  }

  @Override
  public void setString( String string ) {
    this.string = string;
  }

  @Override
  public void setNumber( double number ) {
    this.string = "" + number;
  }

  @Override
  public void setDate( Date date ) {
    this.string = LOCAL_SIMPLE_DATE_PARSER.get().format( date );
  }

  @Override
  public void setBoolean( boolean bool ) {
    this.string = bool ? "Y" : "N";
  }

  @Override
  public void setInteger( long number ) {
    this.string = "" + number;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getPrecision() {
    return -1;
  }

  @Override
  public void setLength( int length, int precision ) {
    this.length = length;
  }

  @Override
  public void setLength( int length ) {
    this.length = length;
  }

  @Override
  public void setPrecision( int precision ) {
  }

  @Override
  public Object clone() {
    try {
      ValueString retval = (ValueString) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber() {
    if ( Utils.isEmpty( string ) ) {
      return null;
    }

    // Localise , to .
    if ( Const.DEFAULT_DECIMAL_SEPARATOR != '.' ) {
      string = string.replace( Const.DEFAULT_DECIMAL_SEPARATOR, '.' );
    }

    return new BigDecimal( string );
  }

  @Override
  public void setBigNumber( BigDecimal number ) {
    string = number.toString();
  }

  @Override
  public Serializable getSerializable() {
    return string;
  }

  @Override
  public void setSerializable( Serializable ser ) {
    ser.toString();
  }

  @Override
  public byte[] getBytes() {
    if ( string == null ) {
      return null;
    }

    char[] arr = string.toCharArray();
    byte[] retByte = new byte[arr.length];

    for ( int i = 0; i < arr.length; i++ ) {
      // only take low byte of char.
      retByte[i] = (byte) ( arr[i] & 0xFF );
    }
    return retByte;
  }

  @Override
  public void setBytes( byte[] b ) {
    try {
      string = new String( b, "US-ASCII" );
    } catch ( UnsupportedEncodingException e ) {
      // we should not get here, ASCII is a mandatory encoding
      string = null;
    }
  }
}
