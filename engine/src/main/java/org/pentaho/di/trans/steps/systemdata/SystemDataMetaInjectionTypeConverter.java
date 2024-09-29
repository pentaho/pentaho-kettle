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
