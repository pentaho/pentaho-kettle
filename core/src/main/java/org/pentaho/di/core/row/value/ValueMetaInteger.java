/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
    return getIntegerFormatMask();
  }
}
