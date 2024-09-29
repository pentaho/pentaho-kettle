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
package org.pentaho.di.trans.steps.avro.input;

// TODO Fix the unit test and uncomment this import
//import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.injection.BaseMetadataInjectionTest;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import static org.junit.Assert.assertEquals;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

public class AvroInputMetaInjectionTest extends BaseMetadataInjectionTest<AvroInputMeta> {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Before
  public void setup() {
    setup( new AvroInputMeta( ) );
    AvroInputField avroInputField = new AvroInputField();
    AvroLookupField avroLookupField = new AvroLookupField();
    meta.setInputFields( new AvroInputField[] { avroInputField } );
    meta.setLookupFields( Arrays.asList( avroLookupField ) );
  }

  @Test
  public void test() throws Exception {

    checkStoredOrdinal( "DATA_LOCATION_TYPE", () -> meta.getDataLocationType(),
      AvroInputMetaBase.LocationDescriptor.class );

    check( "ALLOW_NULL_FOR_MISSING_FIELDS", () -> meta.isAllowNullForMissingFields() );
    check( "AVRO_FILENAME", () -> meta.getDataLocation() );
    check( "DATA_FORMAT", () -> meta.getFormat() );
    check( "DATA_LOCATION", () -> meta.getDataLocation() );
    check( "PASS_THROUGH_FIELDS", () -> meta.passingThruFields );
    check( "SCHEMA_FILENAME", () -> meta.getSchemaLocation() );
    check( "SCHEMA_LOCATION", () -> meta.getSchemaLocation() );
    checkStoredOrdinal( "SCHEMA_LOCATION_TYPE", () -> meta.getSchemaLocationType(),
      AvroInputMetaBase.LocationDescriptor.class );
    check( "DATABASE_STREAM_NAME", () -> meta.getDataLocation() );

    check( "FIELD_NULL_STRING", () -> meta.getInputFields()[ 0 ].getNullString() );
    check( "FIELD_PATH", () -> meta.getInputFields()[ 0 ].getAvroFieldName() );
    check( "AVRO_FORMAT_TYPE", () -> meta.getInputFields()[ 0 ].getFormatType() );

    check( "LOOKUP_DEFAULT_VALUE", () -> meta.getLookupFields().get( 0 ).defaultValue );
    check( "LOOKUP_FIELD_NAME", () -> meta.getLookupFields().get( 0 ).getFieldName() );
    check( "LOOKUP_VARIABLE_NAME", () -> meta.getLookupFields().get( 0 ).getVariableName() );

    checkPdiTypes( "FIELD_TYPE", () -> meta.getInputFields()[ 0 ].getType() );
    //checkStringToEnum( "AVRO_TYPE", () -> meta.getInputFields()[ 0 ].getAvroType(), AvroSpec.DataType.class );
    check( "FIELD_NAME", () -> meta.getInputFields()[ 0 ].getName() );
    check( "FIELD_IF_NULL", () -> meta.getInputFields()[ 0 ].getIfNullValue() );
  }

  private void checkStoredOrdinal( String propertyName, EnumGetter getter, Class enumType )
    throws KettleException {

    Object[] values = enumType.getEnumConstants();
    ValueMetaInterface valueMeta = new ValueMetaInteger( "f" );

    long i = 0;
    for ( Object v : values ) {
      injector.setProperty( meta, propertyName, setValue( valueMeta, i++ ), "f" );
      assertEquals( v, getter.get() );
    }

    skipPropertyTest( propertyName );
  }

}
