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

package org.pentaho.di.trans.steps.avro.input;

import org.apache.avro.Schema;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

public class PentahoAvroInputFormatTest {

  public static final String SAMPLE_SCHEMA_AVRO = "sample-schema.avro";
  public static final String SAMPLE_DATA_AVRO = "sample-data.avro";
  public static final String TYPES_SCHEMA_AVRO = "org/pentaho/di/trans/steps/avro/avro-types-schema.json";

  private PentahoAvroInputFormat format;

  @Before
  public void setUp() throws Exception {
    format = new PentahoAvroInputFormat( );
  }

  @Test
  public void getSplits() {
    assertTrue( format.getSplits().isEmpty() );
  }

  @Test( expected = Exception.class )
  public void readSchemaNoFiles() throws Exception {
    format.setInputFile( null );
    format.setInputSchemaFile( null );
    format.setInputFields( null );

    IPentahoInputFormat.IPentahoRecordReader reader = format.createRecordReader( null );
  }

  @Test
  public void readSchema() throws Exception {
    String schemaFile = getFilePath( SAMPLE_SCHEMA_AVRO );
    format.setInputSchemaFile( schemaFile );
    format.setInputFile( null );
    Schema schema = format.readAvroSchema();
    assertEquals( 2, schema.getFields().size() );
  }

  @Test
  public void readSchemaFromDataFile() throws Exception {
    String dataFile = getFilePath( SAMPLE_DATA_AVRO );
    format.setInputSchemaFile( null );
    format.setInputFile( dataFile );
    Schema schema = format.readAvroSchema();
    assertEquals( 2, schema.getFields().size() );
  }

  @Test
  public void testGetDefaultFields() throws Exception {
    PentahoAvroInputFormat format = Mockito.spy( new PentahoAvroInputFormat( ) );
    Schema.Parser parser = new Schema.Parser();
    Schema schema = parser.parse( new File( getFilePath( TYPES_SCHEMA_AVRO ) ) );
    doReturn( schema ).when( format ).readAvroSchema();

    List<? extends IAvroInputField> defaultFields = format.getDefaultFields();

    List<String> expectedFields = Arrays.asList(
      "boolean_field", "int_field", "long_field", "float_field",
      "double_field", "bytes_field", "string_field", "union_string_field",
      "decimal_bytes_field", "decimal_fixed_field", "date_field", "timestamp_millis_field",
      "address", "zip_code", "double", "date", "time", "active", "cost"
    );
    List<String> actualFields =
      defaultFields.stream().map( IAvroInputField::getAvroFieldName ).collect( Collectors.toList() );
    assertEquals( 19, defaultFields.size() );
    assertTrue( expectedFields.equals( actualFields ) );
  }

  private String getFilePath( String file ) {
    return getClass().getClassLoader().getSystemClassLoader().getResource( file ).getPath();
  }
}
