/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
