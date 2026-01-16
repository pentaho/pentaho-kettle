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

import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.avro.AvroSpec;

import java.util.List;

public class AvroNestedFieldGetterTest {

  public static final String TEST_SCHEMA_AVRO = "test-schema.avsc";

  @Test
  public void testCreateFields_shouldHaveCorrectNumberOfFields() throws Exception {
    String schemaFilePath = getFilePath( TEST_SCHEMA_AVRO );
    PentahoAvroInputFormat format = new PentahoAvroInputFormat();
    format.setBowl( DefaultBowl.getInstance() );
    format.setInputSchemaFile( schemaFilePath );
    format.setInputFile( null );
    Schema schema = format.readAvroSchema();
    List<? extends IAvroInputField> leafFields = AvroNestedFieldGetter.getLeafFields( schema );

    Assert.assertEquals( 16, leafFields.size() );
  }

  @Test
  public void testCreateFields_timestampMicrosField() throws Exception {
    IAvroInputField field = getFieldByName( "timestampMicrosField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "timestampMicrosField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.LONG, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "timestamp-micros", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.TIMESTAMP_MICROS, field.getAvroType() );
  }

  @Test
  public void testCreateFields_timestampMillisField() throws Exception {
    IAvroInputField field = getFieldByName( "timestampMillisField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "timestampMillisField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.LONG, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "timestamp-millis", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.TIMESTAMP_MILLIS, field.getAvroType() );
  }

  @Test
  public void testCreateFields_timestampNanosField() throws Exception {
    IAvroInputField field = getFieldByName( "timestampNanosField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "timestampNanosField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.LONG, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "timestamp-nanos", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_TIMESTAMP, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.TIMESTAMP_NANOS, field.getAvroType() );
  }

  @Test
  public void testCreateFields_dateField() throws Exception {
    IAvroInputField field = getFieldByName( "dateField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "dateField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.INT, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "date", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_DATE, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.DATE, field.getAvroType() );
  }

  @Test
  public void testCreateFields_timeMillisField() throws Exception {
    IAvroInputField field = getFieldByName( "timeMillisField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "timeMillisField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.INT, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "time-millis", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_INTEGER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.TIME_MILLIS, field.getAvroType() );
  }

  @Test
  public void testCreateFields_timeMicrosField() throws Exception {
    IAvroInputField field = getFieldByName( "timeMicrosField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "timeMicrosField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.LONG, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "time-micros", fieldSchema.getLogicalType().getName() );
    Assert.assertEquals( ValueMetaInterface.TYPE_INTEGER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.TIME_MICROS, field.getAvroType() );
  }

  @Test
  public void testCreateFields_decimalField() throws Exception {
    IAvroInputField field = getFieldByName( "decimalField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "decimalField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.BYTES, fieldSchema.getType() );
    Assert.assertNotNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( "decimal", fieldSchema.getLogicalType().getName() );

    LogicalTypes.Decimal decimalLogicalType = (LogicalTypes.Decimal) fieldSchema.getLogicalType();
    Assert.assertEquals( 10, decimalLogicalType.getPrecision() );
    Assert.assertEquals( 2, decimalLogicalType.getScale() );

    Assert.assertEquals( ValueMetaInterface.TYPE_BIGNUMBER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.DECIMAL, field.getAvroType() );
  }

  @Test
  public void testCreateFields_stringField() throws Exception {
    IAvroInputField field = getFieldByName( "stringField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "stringField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.STRING, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.STRING, field.getAvroType() );
  }

  @Test
  public void testCreateFields_booleanField() throws Exception {
    IAvroInputField field = getFieldByName( "booleanField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "booleanField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.BOOLEAN, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_BOOLEAN, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.BOOLEAN, field.getAvroType() );
  }

  @Test
  public void testCreateFields_intField() throws Exception {
    IAvroInputField field = getFieldByName( "intField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "intField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.INT, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_INTEGER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.INTEGER, field.getAvroType() );
  }

  @Test
  public void testCreateFields_longField() throws Exception {
    IAvroInputField field = getFieldByName( "longField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "longField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.LONG, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_INTEGER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.LONG, field.getAvroType() );
  }

  @Test
  public void testCreateFields_floatField() throws Exception {
    IAvroInputField field = getFieldByName( "floatField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "floatField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.FLOAT, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_NUMBER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.FLOAT, field.getAvroType() );
  }

  @Test
  public void testCreateFields_doubleField() throws Exception {
    IAvroInputField field = getFieldByName( "doubleField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "doubleField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.DOUBLE, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_NUMBER, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.DOUBLE, field.getAvroType() );
  }

  @Test
  public void testCreateFields_bytesField() throws Exception {
    IAvroInputField field = getFieldByName( "bytesField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "bytesField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.BYTES, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_BINARY, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.BYTES, field.getAvroType() );
  }

  @Test
  public void testCreateFields_fixedField() throws Exception {
    IAvroInputField field = getFieldByName( "fixedField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "fixedField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.FIXED, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_BINARY, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.FIXED, field.getAvroType() );
  }

  @Test
  public void testCreateFields_enumField() throws Exception {
    IAvroInputField field = getFieldByName( "enumField" );
    Schema fieldSchema = getSchemaForPath( getSchema(), field.getAvroFieldName() );

    Assert.assertEquals( "enumField", field.getPentahoFieldName() );
    Assert.assertEquals( Schema.Type.ENUM, fieldSchema.getType() );
    Assert.assertNull( fieldSchema.getLogicalType() );
    Assert.assertEquals( ValueMetaInterface.TYPE_STRING, field.getPentahoType() );
    Assert.assertEquals( AvroSpec.DataType.STRING, field.getAvroType() );
  }

  private Schema getSchema() throws Exception {
    String schemaFilePath = getFilePath( TEST_SCHEMA_AVRO );
    PentahoAvroInputFormat format = new PentahoAvroInputFormat();
    format.setBowl( DefaultBowl.getInstance() );
    format.setInputSchemaFile( schemaFilePath );
    format.setInputFile( null );
    return format.readAvroSchema();
  }

  private IAvroInputField getFieldByName( String fieldName ) throws Exception {
    Schema schema = getSchema();
    List<? extends IAvroInputField> leafFields = AvroNestedFieldGetter.getLeafFields( schema );
    return leafFields.stream()
      .filter( f -> f.getAvroFieldName().equals( fieldName ) )
      .findFirst()
      .orElseThrow( () -> new AssertionError( "Field not found: " + fieldName ) );
  }


  private Schema getSchemaForPath( Schema schema, String path ) {
    if ( path == null || path.isEmpty() ) {
      return schema;
    }

    String[] parts = path.split( "\\." );
    Schema current = schema;

    for ( String part : parts ) {
      if ( current.getType() == Schema.Type.RECORD ) {
        Schema.Field field = current.getField( part );
        if ( field != null ) {
          current = field.schema();
        }
      }
    }

    return current;
  }


  private String getFilePath( String file ) {
    return getClass().getClassLoader().getSystemClassLoader().getResource( file ).getPath();
  }
}
