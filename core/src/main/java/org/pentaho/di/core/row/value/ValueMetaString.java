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

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Comparator;

public class ValueMetaString extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaString() {
    this( null );
  }

  public ValueMetaString( String name ) {
    super( name, ValueMetaInterface.TYPE_STRING );
  }

  public ValueMetaString( String name, Comparator<Object> comparator ) {
    super( name, ValueMetaInterface.TYPE_STRING, comparator );
  }

  public ValueMetaString( String name, int length, int precision ) {
    super( name, ValueMetaInterface.TYPE_STRING, length, precision );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return getString( object );
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return String.class;
  }
}
