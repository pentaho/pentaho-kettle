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


package org.pentaho.di.core.row.value;

import java.util.Date;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.Utils;

public class ValueMetaDate extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaDate() {
    this( null );
  }

  public ValueMetaDate( String name ) {
    super( name, ValueMetaInterface.TYPE_DATE );
  }

  public ValueMetaDate( String name, int type ) {
    super( name, type );
  }

  public ValueMetaDate( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_DATE, length, precision );
  }

  @Override
  public Date getDate( Object object ) throws KettleValueException {
    return super.getDate( object );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getDate( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Date.class;
  }

  @Override
  public String getFormatMask() {
    return getDateFormatMask();
  }
}
