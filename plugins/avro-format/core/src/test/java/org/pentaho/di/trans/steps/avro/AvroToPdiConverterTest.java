package org.pentaho.di.trans.steps.avro;

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.avro.Conversions;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.steps.avro.input.AvroInputField;
import org.pentaho.di.trans.steps.avro.input.PentahoAvroInputFormat;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvroToPdiConverterTest {

  public static final String TEST_SCHEMA_AVRO = "test-schema.avsc";
  private Schema schema;

  @Before
  public void setUp() throws Exception {
    String schemaFilePath = getFilePath( TEST_SCHEMA_AVRO );
    PentahoAvroInputFormat format = new PentahoAvroInputFormat();
    format.setBowl( DefaultBowl.getInstance() );
    format.setInputSchemaFile( schemaFilePath );
    format.setInputFile( null );
    schema = format.readAvroSchema();
  }

  @Test
  public void convertAvroToPdi_shouldConvertDateLogicalTypeCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_DATE );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "dateField" ) );

    Schema dateSchema = LogicalTypes.date().addToSchema( Schema.create( Schema.Type.INT ) );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    Object result = converter.convertAvroToPdi( 18628, mockInputField, dateSchema );

    LocalDate expectedDate = LocalDate.ofEpochDay( 18628 );
    Date expected = Date.from( expectedDate.atStartOfDay( ZoneId.systemDefault() ).toInstant() );
    assertEquals( expected, result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertTimestampMillisLogicalTypeCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_TIMESTAMP );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "timestampField" ) );

    Schema timestampSchema = LogicalTypes.timestampMillis().addToSchema( Schema.create( Schema.Type.LONG ) );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    long millis = 1609459200000L;
    Object result = converter.convertAvroToPdi( millis, mockInputField, timestampSchema );

    assertEquals( new Timestamp( millis ), result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertTimestampMicrosLogicalTypeCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_TIMESTAMP );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "timestampField" ) );

    Schema timestampSchema = LogicalTypes.timestampMicros().addToSchema( Schema.create( Schema.Type.LONG ) );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    long micros = 1609459200000000L;
    Object result = converter.convertAvroToPdi( micros, mockInputField, timestampSchema );

    assertNotNull( result );
    assertTrue( result instanceof Timestamp );
  }

  @Test
  public void convertAvroToPdi_shouldConvertDecimalLogicalTypeCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_BIGNUMBER );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "decimalField" ) );

    Schema decimalSchema = LogicalTypes.decimal( 10, 2 ).addToSchema( Schema.create( Schema.Type.BYTES ) );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    BigDecimal value = new BigDecimal( "123.45" );
    Conversions.DecimalConversion conversion = new Conversions.DecimalConversion();
    ByteBuffer bytes = conversion.toBytes( value, decimalSchema, LogicalTypes.decimal( 10, 2 ) );

    Object result = converter.convertAvroToPdi( bytes, mockInputField, decimalSchema );

    assertEquals( value, result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertFloatCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "floatField" ) );

    Schema floatSchema = Schema.create( Schema.Type.FLOAT );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    Object result = converter.convertAvroToPdi( 123.45f, mockInputField, floatSchema );

    assertEquals( 123.45, (Double) result, 0.001 );
  }

  @Test
  public void convertAvroToPdi_shouldConvertDoubleCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_NUMBER );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "doubleField" ) );

    Schema doubleSchema = Schema.create( Schema.Type.DOUBLE );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    Object result = converter.convertAvroToPdi( 123.45, mockInputField, doubleSchema );

    assertEquals( 123.45, result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertLongCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "longField" ) );

    Schema longSchema = Schema.create( Schema.Type.LONG );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    Object result = converter.convertAvroToPdi( 123456789L, mockInputField, longSchema );

    assertEquals( 123456789L, result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertFixedBytesCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_BINARY );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "fixedField" ) );

    Schema fixedSchema = Schema.createFixed( "TestFixed", null, null, 4 );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    byte[] bytes = new byte[] {1, 2, 3, 4};
    GenericData.Fixed fixedValue = new GenericData.Fixed( fixedSchema, bytes );
    Object result = converter.convertAvroToPdi( fixedValue, mockInputField, fixedSchema );

    assertArrayEquals( bytes, (byte[]) result );
  }

  @Test
  public void convertAvroToPdi_shouldHandleBytesTypeCorrectly() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_BINARY );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "bytesField" ) );

    Schema bytesSchema = Schema.create( Schema.Type.BYTES );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    byte[] bytes = new byte[] {1, 2, 3, 4};
    ByteBuffer buffer = ByteBuffer.wrap( bytes );
    Object result = converter.convertAvroToPdi( buffer, mockInputField, bytesSchema );

    assertArrayEquals( bytes, (byte[]) result );
  }

  @Test
  public void convertAvroToPdi_shouldConvertTextNodeToString() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_STRING );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "textField" ) );

    Schema stringSchema = Schema.create( Schema.Type.STRING );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    TextNode textNode = new TextNode( "test value" );
    Object result = converter.convertAvroToPdi( textNode, mockInputField, stringSchema );

    assertEquals( "test value", result );
  }

  @Test
  public void convertAvroToPdi_shouldHandleInvalidTypeConversionGracefully() {
    AvroInputField mockInputField = mock( AvroInputField.class );
    when( mockInputField.getPentahoType() ).thenReturn( ValueMetaInterface.TYPE_INTEGER );
    when( mockInputField.getPathParts() ).thenReturn( List.of( "invalidField" ) );

    Schema stringSchema = Schema.create( Schema.Type.STRING );
    AvroToPdiConverter converter = new AvroToPdiConverter( schema );

    Object result = converter.convertAvroToPdi( "not a number", mockInputField, stringSchema );

    assertNull( result );
  }


  private String getFilePath( String file ) {
    return getClass().getClassLoader().getSystemClassLoader().getResource( file ).getPath();
  }
}
