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

import org.pentaho.di.core.Const;

/**
 * This class contains a Value of type Integer and the length by which it is described.
 *
 * @author Matt
 * @since 15-10-2004
 *
 */
public class ValueInteger implements ValueInterface, Cloneable {
  private long number;
  private int length;

  public ValueInteger() {
    this.number = 0L;
    this.length = -1;
  }

  public ValueInteger( long number ) {
    this.number = number;
    this.length = -1;
  }

  @Override
  public int getType() {
    return Value.VALUE_TYPE_INTEGER;
  }

  @Override
  public String getTypeDesc() {
    return "Integer";
  }

  @Override
  public String getString() {
    return Long.toString( number );
  }

  @Override
  public double getNumber() {
    return this.number;
  }

  @Override
  public Date getDate() {
    return new Date( number );
  }

  @Override
  public boolean getBoolean() {
    return number != 0L;
  }

  @Override
  public long getInteger() {
    return number;
  }

  @Override
  public void setSerializable( Serializable ser ) {

  }

  @Override
  public void setString( String string ) {
    this.number = Const.toLong( string, 0L );
  }

  @Override
  public void setNumber( double number ) {
    this.number = Math.round( number );
  }

  @Override
  public void setDate( Date date ) {
    this.number = date.getTime();
  }

  @Override
  public void setBoolean( boolean bool ) {
    this.number = bool ? 1L : 0L;
  }

  @Override
  public void setInteger( long number ) {
    this.number = number;
  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getPrecision() {
    return 0;
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
      ValueInteger retval = (ValueInteger) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber() {
    return new BigDecimal( number );
  }

  @Override
  public void setBigNumber( BigDecimal number ) {
    this.number = number.longValue();

  }

  @Override
  public Serializable getSerializable() {
    return new Long( number );
  }

  @Override
  public byte[] getBytes() {
    return null;
  }

  @Override
  public void setBytes( byte[] b ) {
  }
}
