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


package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class ValueSerializable implements ValueInterface, Cloneable {

  protected Serializable serializable;

  public ValueSerializable( Serializable ser ) {
    this.serializable = ser;
  }

  @Override
  public Serializable getSerializable() {
    return serializable;
  }

  @Override
  public int getPrecision() {
    return 0;
  }

  @Override
  public String getString() {
    return ( serializable != null ) ? serializable.toString() : null;
  }

  @Override
  public int getType() {
    return Value.VALUE_TYPE_SERIALIZABLE;
  }

  @Override
  public String getTypeDesc() {
    return "Object";
  }

  @Override
  public Object clone() {
    try {
      ValueSerializable retval = (ValueSerializable) super.clone();
      return retval;
    } catch ( CloneNotSupportedException e ) {
      return null;
    }
  }

  // These dont do anything but are needed for the ValueInterface
  @Override
  public void setBigNumber( BigDecimal number ) {
  }

  @Override
  public void setBoolean( boolean bool ) {
  }

  @Override
  public void setDate( Date date ) {
  }

  @Override
  public void setInteger( long number ) {
  }

  @Override
  public void setLength( int length, int precision ) {
  }

  @Override
  public void setLength( int length ) {
  }

  @Override
  public void setNumber( double number ) {
  }

  @Override
  public void setPrecision( int precision ) {
  }

  @Override
  public void setString( String string ) {
  }

  @Override
  public void setSerializable( Serializable ser ) {

  }

  @Override
  public BigDecimal getBigNumber() {
    return null;
  }

  @Override
  public boolean getBoolean() {
    return false;
  }

  @Override
  public Date getDate() {
    return null;
  }

  @Override
  public long getInteger() {
    return 0;
  }

  @Override
  public int getLength() {
    return 0;
  }

  @Override
  public double getNumber() {
    return 0;
  }

  @Override
  public byte[] getBytes() {
    return null;
  }

  @Override
  public void setBytes( byte[] b ) {
  }
}
