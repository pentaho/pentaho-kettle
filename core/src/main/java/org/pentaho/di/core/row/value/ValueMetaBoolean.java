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
