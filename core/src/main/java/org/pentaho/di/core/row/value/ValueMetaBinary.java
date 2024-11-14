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

public class ValueMetaBinary extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaBinary() {
    this( null );
  }

  public ValueMetaBinary( String name ) {
    super( name, ValueMetaInterface.TYPE_BINARY );
  }

  public ValueMetaBinary( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_BINARY, length, precision );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getBinary( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return byte[].class;
  }
}
