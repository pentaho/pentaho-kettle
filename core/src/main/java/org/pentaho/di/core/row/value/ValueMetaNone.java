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

public class ValueMetaNone extends ValueMetaBase implements ValueMetaInterface {

  public ValueMetaNone() {
    this( null );
  }

  public ValueMetaNone( String name ) {
    super( name, ValueMetaInterface.TYPE_NONE );
  }

  @Override
  public Object getNativeDataType( Object object ) throws KettleValueException {
    return object;
  }

  @Override
  public Class<?> getNativeDataTypeClass() throws KettleValueException {
    return Object.class;
  }
}
