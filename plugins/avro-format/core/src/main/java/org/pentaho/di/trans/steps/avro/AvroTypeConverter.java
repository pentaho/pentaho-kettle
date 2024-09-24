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
package org.pentaho.di.trans.steps.avro;

import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;

/**
 * Created by rmansoor on 8/8/2018.
 */
public class AvroTypeConverter {


  public static String convertToAvroType( int pdiType ) {
    switch ( pdiType ) {
      case ValueMetaInterface.TYPE_INET:
      case ValueMetaInterface.TYPE_STRING:
        return AvroSpec.DataType.STRING.getName();
      case ValueMetaInterface.TYPE_TIMESTAMP:
        return AvroSpec.DataType.TIMESTAMP_MILLIS.getName();
      case ValueMetaInterface.TYPE_BINARY:
        return AvroSpec.DataType.BYTES.getName();
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return AvroSpec.DataType.DECIMAL.getName();
      case ValueMetaInterface.TYPE_BOOLEAN:
        return AvroSpec.DataType.BOOLEAN.getName();
      case ValueMetaInterface.TYPE_DATE:
        return AvroSpec.DataType.DATE.getName();
      case ValueMetaInterface.TYPE_INTEGER:
        return AvroSpec.DataType.LONG.getName();
      case ValueMetaInterface.TYPE_NUMBER:
        return AvroSpec.DataType.DOUBLE.getName();
      default:
        return AvroSpec.DataType.NULL.getName();
    }
  }

  public static String convertToAvroType( String type ) {
    int pdiType = ValueMetaFactory.getIdForValueMeta( type );
    if ( pdiType > 0 ) {
      return convertToAvroType( pdiType );
    } else {
      return type;
    }
  }

}
