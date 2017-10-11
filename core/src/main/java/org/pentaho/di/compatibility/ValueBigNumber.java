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
 * This class contains a Value of type BigNumber and the length/precision by which it is described.
 *
 * @author Matt
 * @since 05-09-2005
 *
 */
public class ValueBigNumber implements ValueInterface, Cloneable {
  private BigDecimal number;
  private int length;
  private int precision;

  public ValueBigNumber() {
    this.number = null;
    this.length = -1;
    this.precision = -1;
  }

  public ValueBigNumber( BigDecimal number ) {
    // System.out.println("new ValueBigNumber("+number+")"); OK

    this.number = number;
    this.length = -1;
    this.precision = -1;
  }

  @Override
  public int getType() {
    return Value.VALUE_TYPE_BIGNUMBER;
  }

  @Override
  public Serializable getSerializable() {
    return number;
  }

  @Override
  public String getTypeDesc() {
    return "BigNumber";
  }

  @Override
  public String getString() {
    if ( number == null ) {
      return null;
    }
    return number.toString();
  }

  @Override
  public double getNumber() {
    if ( number == null ) {
      return 0.0;
    }
    return this.number.doubleValue();
  }

  @Override
  public Date getDate() {
    if ( number == null ) {
      return null;
    }
    return new Date( number.longValue() );
  }

  @Override
  public boolean getBoolean() {
    if ( number == null ) {
      return false;
    }
    return number.longValue() != 0L;
  }

  @Override
  public long getInteger() {
    if ( number == null ) {
      return 0L;
    }
    return number.longValue();
  }

  @Override
  public void setString( String string ) {
    try {
      this.number = new BigDecimal( string );
    } catch ( NumberFormatException e ) {
      this.number = BigDecimal.ZERO;
    }
  }

  @Override
  public void setNumber( double number ) {
    this.number = BigDecimal.valueOf( number );
  }

  @Override
  public void setDate( Date date ) {
    this.number = new BigDecimal( date.getTime() );
  }

  @Override
  public void setBoolean( boolean bool ) {
    this.number = bool ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public void setInteger( long number ) {
    this.number = new BigDecimal( number );
  }

  @Override
  public void setSerializable( Serializable ser ) {

  }

  @Override
  public int getLength() {
    return length;
  }

  @Override
  public int getPrecision() {
    return precision;
  }

  @Override
  public void setLength( int length, int precision ) {
    this.length = length;
    this.precision = precision;
  }

  @Override
  public void setLength( int length ) {
    this.length = length;
  }

  @Override
  public void setPrecision( int precision ) {
    this.precision = precision;
  }

  @Override
  public Object clone() {
    try {
      ValueBigNumber retval = (ValueBigNumber) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  @Override
  public BigDecimal getBigNumber() {
    return number;
  }

  @Override
  public void setBigNumber( BigDecimal number ) {
    this.number = number;
  }

  @Override
  public byte[] getBytes() {
    return null;
  }

  @Override
  public void setBytes( byte[] b ) {
  }
}
