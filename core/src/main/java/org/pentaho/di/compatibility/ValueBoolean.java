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
import java.math.BigDecimal;
import java.util.Date;

/**
 * This class contains a Value of type Boolean.
 *
 * @author Matt Casters
 * @since 15-10-2004
 */
public class ValueBoolean implements ValueInterface, Cloneable {
  private boolean bool;

  public ValueBoolean() {
    this.bool = false;
  }

  public ValueBoolean( boolean bool ) {
    this.bool = bool;
  }

  @Override
  public int getType() {
    return Value.VALUE_TYPE_BOOLEAN;
  }

  @Override
  public String getTypeDesc() {
    return "Boolean";
  }

  @Override
  public String getString() {
    return bool ? "Y" : "N";
  }

  @Override
  public double getNumber() {
    return bool ? 1.0 : 0.0;
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public boolean getBoolean() {
    return bool;
  }

  @Override
  public long getInteger() {
    return bool ? 1L : 0L;
  }

  @Override
  public void setString( String string ) {
    this.bool =
      "Y".equalsIgnoreCase( string ) || "TRUE".equalsIgnoreCase( string ) || "YES".equalsIgnoreCase( string );
  }

  @Override
  public void setNumber( double number ) {
    this.bool = ( number == 0.0 ) ? false : true;
  }

  @Override
  public void setDate( Date date ) {
    this.bool = false;
  }

  @Override
  public void setSerializable( Serializable ser ) {

  }

  @Override
  public void setBoolean( boolean bool ) {
    this.bool = bool;
  }

  @Override
  public void setInteger( long number ) {
    this.bool = ( number == 0 ) ? false : true;
  }

  @Override
  public int getLength() {
    return -1;
  }

  @Override
  public int getPrecision() {
    return -1;
  }

  @Override
  public void setLength( int length, int precision ) {
  }

  @Override
  public void setLength( int length ) {
  }

  @Override
  public void setPrecision( int precision ) {
  }

  @Override
  public Object clone() {
    try {
      ValueBoolean retval = (ValueBoolean) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber() {
    return new BigDecimal( bool ? 1 : 0 );
  }

  @Override
  public void setBigNumber( BigDecimal number ) {
    bool = number.signum() != 0;
  }

  @Override
  public Serializable getSerializable() {
    return Boolean.valueOf( bool );
  }

  @Override
  public byte[] getBytes() {
    return null;
  }

  @Override
  public void setBytes( byte[] b ) {
  }
}
