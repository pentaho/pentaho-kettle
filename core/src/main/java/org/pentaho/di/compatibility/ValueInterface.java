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

package org.pentaho.di.compatibility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * This interface provides a way to look at a Number, String, Integer, Date... the same way. The methods mentioned in
 * this interface are common to all Value types.
 *
 * @author Matt
 * @since 15-10-2004
 */
public interface ValueInterface {
  public int getType();

  public String getTypeDesc();

  public String getString();

  public double getNumber();

  public Date getDate();

  public boolean getBoolean();

  public long getInteger();

  public BigDecimal getBigNumber();

  public Serializable getSerializable();

  public byte[] getBytes();

  public void setString( String string );

  public void setNumber( double number );

  public void setDate( Date date );

  public void setBoolean( boolean bool );

  public void setInteger( long number );

  public void setBigNumber( BigDecimal number );

  public void setSerializable( Serializable ser );

  public void setBytes( byte[] b );

  public int getLength();

  public int getPrecision();

  public void setLength( int length );

  public void setPrecision( int precision );

  public void setLength( int length, int precision );

  public Object clone();
}
