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


package org.pentaho.di.core.row.value;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

public class ValueMetaBoolean extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaBoolean() {
    this( null );
  }

  public ValueMetaBoolean( String name ) {
    super( name, ValueMetaInterface.TYPE_BOOLEAN );
  }

  public ValueMetaBoolean( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_BOOLEAN, length, precision );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getBoolean( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Boolean.class;
  }
}
