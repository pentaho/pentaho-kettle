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

package org.pentaho.di.trans.steps.avro.output;

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.trans.steps.avro.AvroTypeConverter;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;

public class AvroOutputMetaInjectionTest extends BaseMetadataInjectionTest<AvroOutputMeta> {

  @Before
  public void setup() {
    setup( new AvroOutputMeta(  ) );
  }

  @Test
  public void test() throws Exception {
    check( "FILENAME", new StringGetter() {
      public String get() {
        return meta.getFilename();
      }
    } );
    check( "OPTIONS_COMPRESSION", new StringGetter() {
      public String get() {
        return meta.getCompressionType().toUpperCase();
      }
    }, "SNAPPY" );
    check( "SCHEMA_FILENAME", new StringGetter() {
      public String get() {
        return meta.getSchemaFilename();
      }
    } );
    check( "SCHEMA_NAMESPACE", new StringGetter() {
      public String get() {
        return meta.getNamespace();
      }
    } );
    check( "SCHEMA_RECORD_NAME", new StringGetter() {
      public String get() {
        return meta.getRecordName();
      }
    } );
    check( "SCHEMA_DOC_VALUE", new StringGetter() {
      public String get() {
        return meta.getDocValue();
      }
    } );
    check( "FIELD_PATH", new StringGetter() {
      public String get() {
        return meta.getOutputFields().get( 0 ).getFormatFieldName();
      }
    } );
    check( "FIELD_NAME", new StringGetter() {
      public String get() {
        return meta.getOutputFields().get( 0 ).getPentahoFieldName();
      }
    } );
    check( "FIELD_IF_NULL", new StringGetter() {
      public String get() {
        return meta.getOutputFields().get( 0 ).getDefaultValue();
      }
    } );
    check( "FIELD_NULL_STRING", new BooleanGetter() {
      public boolean get() {
        return meta.getOutputFields().get( 0 ).getAllowNull();
      }
    } );
    check( "OVERRIDE_OUTPUT", new BooleanGetter() {
      public boolean get() {
        return meta.isOverrideOutput();
      }
    } );
    check( "FIELD_DECIMAL_PRECISION", new IntGetter() {
      public int get() {
        return meta.getOutputFields().get( 0 ).getPrecision();
      }
    } );
    check( "FIELD_DECIMAL_SCALE", new IntGetter() {
      public int get() {
        return meta.getOutputFields().get( 0 ).getScale();
      }
    } );


    int[] supportedPdiTypes = {
      ValueMetaInterface.TYPE_NUMBER,
      ValueMetaInterface.TYPE_STRING,
      ValueMetaInterface.TYPE_DATE,
      ValueMetaInterface.TYPE_BOOLEAN,
      ValueMetaInterface.TYPE_INTEGER,
      ValueMetaInterface.TYPE_BIGNUMBER,
      ValueMetaInterface.TYPE_SERIALIZABLE,
      ValueMetaInterface.TYPE_BINARY,
      ValueMetaInterface.TYPE_TIMESTAMP,
      ValueMetaInterface.TYPE_INET
    };
    String[] typeNames = new String[ supportedPdiTypes.length ];
    int[] typeIds = new int[ supportedPdiTypes.length ];
    for ( int j = 0; j < supportedPdiTypes.length; j++ ) {
      typeNames[ j ] = ValueMetaInterface.getTypeDescription( supportedPdiTypes[ j ] );
      String avroTypeName = AvroTypeConverter.convertToAvroType( supportedPdiTypes[ j ] );
      for ( AvroSpec.DataType avrotType : AvroSpec.DataType.values() ) {
        if ( avrotType.getName().equals( avroTypeName ) ) {
          typeIds[ j ] = avrotType.getId();
        }
      }
    }
    checkStringToInt( "FIELD_TYPE", new IntGetter() {
      public int get() {
        return meta.getOutputFields().get( 0 ).getFormatType();
      }
    }, typeNames, typeIds );


    AvroSpec.DataType[] supportedAvroTypes = {
      AvroSpec.DataType.STRING,
      AvroSpec.DataType.INTEGER,
      AvroSpec.DataType.LONG,
      AvroSpec.DataType.FLOAT,
      AvroSpec.DataType.DOUBLE,
      AvroSpec.DataType.BOOLEAN,
      AvroSpec.DataType.DECIMAL,
      AvroSpec.DataType.DATE,
      AvroSpec.DataType.TIMESTAMP_MILLIS,
      AvroSpec.DataType.BYTES
    };
    typeNames = new String[ supportedAvroTypes.length ];
    typeIds = new int[ supportedAvroTypes.length ];
    for ( int i = 0; i < supportedAvroTypes.length; i++ ) {
      typeNames[ i ] = supportedAvroTypes[ i ].getName();
      typeIds[ i ] = supportedAvroTypes[ i ].getId();
    }
    checkStringToInt( "FIELD_AVRO_TYPE", new IntGetter() {
      public int get() {
        return meta.getOutputFields().get( 0 ).getFormatType();
      }
    }, typeNames, typeIds );
    skipPropertyTest( "OPTIONS_TIME_IN_FILE_NAME" );
    skipPropertyTest( "OPTIONS_DATE_FORMAT" );
    skipPropertyTest( "FIELD_NULLABLE" );
    skipPropertyTest( "OPTIONS_DATE_IN_FILE_NAME" );
  }
}
