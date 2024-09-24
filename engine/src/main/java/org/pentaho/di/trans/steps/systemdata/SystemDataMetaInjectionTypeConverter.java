/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.systemdata;

import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.injection.InjectionTypeConverter;

public class SystemDataMetaInjectionTypeConverter extends InjectionTypeConverter {

  @Override
  public Enum<?> string2enum( Class<?> enumClass, String v ) throws KettleValueException {
    // For SystemDataMeta, enum should be a SystemDataTypes
    SystemDataTypes type = SystemDataTypes.getTypeFromString( v );
    if ( !SystemDataTypes.TYPE_SYSTEM_INFO_NONE.toString().equals( v ) && type == SystemDataTypes.TYPE_SYSTEM_INFO_NONE ) {
      // Throw exception to let user know entered string was not valid SystemDataType
      throw new KettleValueException( "Unknown value '" + v + "' for enum " + enumClass );
    } else {
      return type;
    }
  }
}
