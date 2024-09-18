/*! ******************************************************************************
 *
 * Pentaho Big Data
 *
 * Copyright (C) 2022-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.trans.steps.avro.AvroSpec;
import org.pentaho.di.trans.steps.avro.output.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.*;
import org.pentaho.di.core.util.Assert;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.InetAddress;
import java.nio.file.FileAlreadyExistsException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PentahoAvroReadWriteTest {
  private static InetAddress DEFAULT_INET_ADDR;
  private static String[][] DEFAULT_SCHEME_DESCRIPTION = null;

  {
    try {
      DEFAULT_INET_ADDR = InetAddress.getByName( "www.microsoft.com" );

      DEFAULT_SCHEME_DESCRIPTION = new String[][] {
        { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
        { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
        { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "0" },
        { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_BIGNUMBER ), "2", "1" },
        { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_INET ), "0", "0" },
        { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ), "0", "0" },
        { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.INTEGER.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_INTEGER ), "0", "0" },
        { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DATE.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_DATE ), "0", "0" },
        { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.TIMESTAMP_MILLIS.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_TIMESTAMP ), "0", "0" },
        { "avroBytes10", "pentahoBinary10", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
          String.valueOf( ValueMetaInterface.TYPE_BINARY ), "0", "0" }
      };

    } catch ( Exception e ) {
      //should not happen
    }
  }

  private final int AVRO_NAME_INDEX = 0;
  private final int PENTAHO_NAME_INDEX = 1;
  private final int AVRO_TYPE_INDEX = 2;
  private final int PDI_TYPE_INDEX = 3;
  private final int PRECISION_INDEX = 4;
  private final int SCALE_INDEX = 5;
  private final int DATE_FORMAT_INDEX = 6;

  public TemporaryFolder tempFolder = new TemporaryFolder();

  private DateFormat dateFormat = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss.SSS" );
  Date date1 = null;
  Date date2 = null;
  Date timeStamp1 = null;
  Date timeStamp2 = null;

  @Before
  public void setup() throws Exception {
    tempFolder.create();

    date1 = ( dateFormat.parse( "2000/01/01 00:00:00.000" ) );
    date2 = ( dateFormat.parse( "1999/12/31 00:00:00.000" ) );
    timeStamp1 = new Timestamp( dateFormat.parse( "2001/11/01 20:30:15.123" ).getTime() );
    timeStamp2 = new Timestamp( dateFormat.parse( "1999/12/31 23:59:59.999" ).getTime() );
  }

  private RowMeta buildRowMeta( String[][] schemaDescription ) {
    RowMeta rowMeta = new RowMeta();

    for ( String[] schemaField : schemaDescription ) {
      rowMeta.addValueMeta(
        getValueMetaInterface( schemaField[ PENTAHO_NAME_INDEX ], Integer.valueOf( schemaField[ PDI_TYPE_INDEX ] ) ) );
    }

    return rowMeta;
  }

  private ArrayList<AvroInputField> buildAvroInputFields( String[][] schemaDescription ) {
    ArrayList<AvroInputField> avroInputFields = new ArrayList<AvroInputField>();

    for ( String[] schemaField : schemaDescription ) {
      AvroInputField avroInputField = new AvroInputField();
      avroInputField.setFormatFieldName( schemaField[ AVRO_NAME_INDEX ] );
      avroInputField.setPentahoFieldName( schemaField[ PENTAHO_NAME_INDEX ] );
      avroInputField.setAvroType( AvroSpec.DataType.values()[ Integer.parseInt( schemaField[ AVRO_TYPE_INDEX ] ) ] );
      avroInputField.setPentahoType( Integer.valueOf( schemaField[ PDI_TYPE_INDEX ] ) );
      if ( ( schemaField.length > DATE_FORMAT_INDEX ) && ( schemaField[ DATE_FORMAT_INDEX ] != null ) ) {
        avroInputField.setStringFormat( schemaField[ DATE_FORMAT_INDEX ] );
      }
      avroInputFields.add( avroInputField );
    }
    return avroInputFields;
  }

  private ArrayList<AvroOutputField> buildAvroOutputFields(String[][] schemaDescription ) {
    ArrayList<AvroOutputField> avroOutputFields = new ArrayList<AvroOutputField>();

    for ( String[] schemaField : schemaDescription ) {
      AvroOutputField avroOutputField = new AvroOutputField();
      avroOutputField.setFormatFieldName( schemaField[ AVRO_NAME_INDEX ] );
      avroOutputField.setPentahoFieldName( schemaField[ PENTAHO_NAME_INDEX ] );
      avroOutputField.setFormatType( AvroSpec.DataType.values()[ Integer.parseInt( schemaField[ AVRO_TYPE_INDEX ] ) ] );
      avroOutputField.setAllowNull( true );
      avroOutputField.setDefaultValue( null );
      avroOutputField.setPrecision( schemaField[ PRECISION_INDEX ] );
      avroOutputField.setScale( schemaField[ SCALE_INDEX ] );
      avroOutputFields.add( avroOutputField );
    }
    return avroOutputFields;
  }

  @Test
  public void testAvroFileWriteAndRead() throws Exception {
    Object[] rowData =
      new Object[] { "Row1Field1", "Row1Field2", new Double( 3.1 ), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR,
        Boolean.TRUE, new Long( 1 ), date1, timeStamp1, "foobar".getBytes() };

    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", false );
    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", true );
    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.SNAPPY,
      "avroOutputSnappy.avro", false );
    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.DEFLATE,
      "avroOutputDeflate.avro", false );
  }

  @Test
  public void testAvroFileWriteAndReadNegativeValues() throws Exception {
    Object[] rowData =
      new Object[] { "Row2Field1", "Row2Field2", new Double( -3.2 ), new BigDecimal( "-4.2" ), DEFAULT_INET_ADDR,
        Boolean.FALSE, new Long( -2L ), date2, timeStamp2, "Donald Duck".getBytes() };
    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", true );
  }

  @Test
  public void testAvroFileWriteAndReadNullValues() throws Exception {
    Object[] rowData = new Object[] { "Row3Field1", null, null, null, null, null, null, null, null, null };

    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", true );
  }


  @Test( expected = org.apache.avro.file.DataFileWriter.AppendWriteException.class )
  public void testAvroFileNullsNotAllowed() throws Exception {
    Object[] rowData = new Object[] { "Row3Field1", null, null, null, null, null, null, null, null, null };
    String[] defaultValues = { null, null, null, null, null, null, null, null, null, null };

    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", defaultValues, null, true );
  }

  @Test
  public void testOverwriteFileIsTrue() throws Exception {
    Object[] rowData =
      new Object[] { "Row1Field1", "Row1Field2", new Double( 3.1 ), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR,
        Boolean.TRUE, new Long( 1 ), date1, timeStamp1, "foobar".getBytes() };

    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", true );
    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", true );
  }

  @Test
  public void testOverwriteFileIsFalse() throws Exception {
    Object[] rowData = new Object[] { "Row1Field1", "Row1Field2", new Double( 3.1 ), new BigDecimal( "4.1" ),
      DEFAULT_INET_ADDR, Boolean.TRUE, new Long( 1 ), date1, timeStamp1, "foobar".getBytes() };

    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
      "avroOutputNone.avro", false );
    try {
      doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
        "avroOutputNone.avro", false );
      fail();
    } catch ( FileAlreadyExistsException ex ) {
      assertTrue( ex != null );
    }
  }

  @Test
  public void testParseDateOnInput() throws Exception {
    Object[] rowData =
      new Object[] { "2000-01-02" };
    String[][] outputSchemaDescription = new String[][] {
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" }
    };
    String[][] inputSchemaDescription = new String[][] {
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_DATE ), "0", "0", "yyyy-MM-dd" }
    };

    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    SimpleDateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
    Date[] expectedResults = new Date[] { format.parse( "2000-01-02" ) };

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }

  @Test
  public void testScaleOnOutput() throws Exception {
    Object[] rowData = new Object[] { new Double( 1.987 ) };
    String[][] outputSchemaDescription = new String[][] {
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "2" }
    };
    String[][] inputSchemaDescription = new String[][] {
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) }
    };

    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    Object[] expectedResults = new Object[] { new Double( 1.99 ) };

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }


  @Test
  public void testConvertToStringOnOutput() throws Exception {
    Object[] rowData =
      new Object[] { "Row1Field1", "Row1Field2", new Double( 3.1 ), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR,
        Boolean.TRUE, new Long( 1 ), date1, timeStamp1, "foobar".getBytes() };
    String[][] outputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "0" },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BIGNUMBER ), "2", "1" },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INET ), "0", "0" },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ), "0", "0" },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ), "0", "0" },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_DATE ), "0", "0" },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_TIMESTAMP ), "0", "0" },
      { "avroBytes10", "pentahoBinary10", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BINARY ), "0", "0" }
    };

    String[][] inputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) },
      { "avroBytes10", "pentahoBinary10", String.valueOf( AvroSpec.DataType.STRING.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ) }
    };


    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    String[] expectedResults = new String[ rowData.length ];
    for ( int i = 0; i < rowMetaAndData.size(); i++ ) {
      expectedResults[ i ] = rowMetaAndData.getString( i, null );
    }

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );

  }

  @Test
  public void testConvertToBooleanOnOutput() throws Exception {
    Object[] rowData =
      new Object[] { "Y", "Row1Field2", new Double( 3.1 ), new BigDecimal( "4.1", MathContext.DECIMAL64 ), true,
        new Long( 0 ) };
    String[][] outputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "0" },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BIGNUMBER ), "2", "1" },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ), "0", "0" },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ), "0", "0" }
    };

    String[][] inputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.BOOLEAN.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ) }
    };


    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    Boolean[] expectedResults = new Boolean[ rowData.length ];
    for ( int i = 0; i < rowMetaAndData.size(); i++ ) {
      expectedResults[ i ] = rowMetaAndData.getBoolean( i, true );
    }

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }

  @Test
  public void testConvertToLongOnOutput() throws Exception {
    Object[] rowData =
      new Object[] { "1", "2", new Double( 3.1 ), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR, Boolean.TRUE,
        new Long( 1 ), date1, timeStamp1 };
    String[][] outputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "0" },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BIGNUMBER ), "2", "1" },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INET ), "0", "0" },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ), "0", "0" },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ), "0", "0" },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_DATE ), "0", "0" },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_TIMESTAMP ), "0", "0" }
    };

    String[][] inputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ) }
    };


    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    Long[] expectedResults = new Long[ rowData.length ];
    for ( int i = 0; i < rowMetaAndData.size(); i++ ) {
      expectedResults[ i ] = rowMetaAndData.getInteger( i, -999 );
    }

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }

  @Test
  public void testConvertToDoubleOnOutput() throws Exception {
    Object[] rowData =
      new Object[] { "1", "2", new Double( 3.1 ), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR, Boolean.TRUE,
        new Long( 1 ), date1, timeStamp1 };
    String[][] outputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ), "0", "0" },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BIGNUMBER ), "2", "1" },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INET ), "0", "0" },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BOOLEAN ), "0", "0" },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_INTEGER ), "0", "0" },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_DATE ), "0", "0" },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_TIMESTAMP ), "0", "0" }
    };

    String[][] inputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DOUBLE.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) },
      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.LONG.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_NUMBER ) }
    };


    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    Double[] expectedResults = new Double[ rowData.length ];
    for ( int i = 0; i < rowMetaAndData.size(); i++ ) {
      expectedResults[ i ] = rowMetaAndData.getNumber( i, -999 );
    }

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }

  @Test
  public void testConvertToBytesOnOutput() throws Exception {
    Object[] rowData = new Object[] { "Row1Field1", "Row1Field2", "foobar".getBytes() };
    String[][] outputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_STRING ), "0", "0" },
      { "avroBytes10", "pentahoBinary10", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BINARY ), "0", "0" }
    };

    String[][] inputSchemaDescription = new String[][] {
      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BINARY ) },
      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BINARY ) },
      { "avroBytes10", "pentahoBinary10", String.valueOf( AvroSpec.DataType.BYTES.ordinal() ),
        String.valueOf( ValueMetaInterface.TYPE_BINARY ) }
    };


    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );

    byte[][] expectedResults = new byte[ rowData.length ][];
    for ( int i = 0; i < rowMetaAndData.size(); i++ ) {
      expectedResults[ i ] = rowMetaAndData.getBinary( i, null );
    }

    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData,
      IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults, true );
  }

  //  @Test
  //  public void testConvertToAvroDecimal() throws Exception {
  //    Object[] rowData = new Object[] { "1", "2", new Double(3.1), new BigDecimal( "4.1" ), DEFAULT_INET_ADDR,
  // Boolean.TRUE, new Long(1), date1, timeStamp1};
  //    String[][] outputSchemaDescription = new String[][] {
  //      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_STRING ) },
  //      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_STRING )  },
  //      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_NUMBER )  },
  //      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String
  // .valueOf( ValueMetaInterface.TYPE_BIGNUMBER ) },
  //      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_INET ) },
  //      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BOOLEAN )  },
  //      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_INTEGER )  },
  //      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_DATE ) },
  //      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String
  // .valueOf( ValueMetaInterface.TYPE_TIMESTAMP )  }
  //    };
  //
  //    String[][] inputSchemaDescription = new String[][] {
  //      { "avroField1", "pentahoField1", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER ) },
  //      { "avroField2", "pentahoField2", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER )  },
  //      { "avroDouble3", "pentahoNumber3", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER )  },
  //      { "avroDecimal4", "pentahoBigNumber4", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String
  // .valueOf( ValueMetaInterface.TYPE_BIGNUMBER ) },
  //      { "avroString5", "pentahoInet5", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER ) },
  //      { "avroBoolean6", "pentahoBoolean6", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER )  },
  //      { "avroInt7", "pentahoInt7", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER )  },
  //      { "avroDate8", "pentahoDate8", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String.valueOf(
  // ValueMetaInterface.TYPE_BIGNUMBER ) },
  //      { "avroTimestamp9", "pentahoTimestamp9", String.valueOf( AvroSpec.DataType.DECIMAL.ordinal() ), String
  // .valueOf( ValueMetaInterface.TYPE_BIGNUMBER )  }
  //    };
  //
  //
  //    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );
  //    RowMetaAndData rowMetaAndData = new RowMetaAndData( rowMeta, rowData );
  //
  //    BigDecimal[] expectedResults = new BigDecimal[rowData.length];
  //    for (int i = 0; i < rowMetaAndData.size(); i++) {
  //      expectedResults[i] = rowMetaAndData.getBigNumber(i, null);
  //    }
  //
  //    doReadWrite( inputSchemaDescription, outputSchemaDescription, rowData, IPentahoAvroOutputFormat.COMPRESSION
  // .UNCOMPRESSED, "avroOutputNone.avro", null, expectedResults );
  //  }

  //  @Test
  //  public void testConvertToAvroTimestamp() throws Exception {
  //    Object[] rowData = new Object[] { "Row1Field1", "Row1Field2", 3.1, new BigDecimal( "4.1", MathContext
  // .DECIMAL64 ), DEFAULT_INET_ADDR, true, 1L, date1, timeStamp1, "foobar".getBytes() };
  //
  //    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
  // "avroOutputNone.avro" );
  //  }
  //
  //  @Test
  //  public void testConvertToAvroFloat() throws Exception {
  //    Object[] rowData = new Object[] { "Row1Field1", "Row1Field2", 3.1, new BigDecimal( "4.1", MathContext
  // .DECIMAL64 ), DEFAULT_INET_ADDR, true, 1L, date1, timeStamp1, "foobar".getBytes() };
  //
  //    doReadWrite( DEFAULT_SCHEME_DESCRIPTION, rowData, IPentahoAvroOutputFormat.COMPRESSION.UNCOMPRESSED,
  // "avroOutputNone.avro" );
  //  }
  //

  private String getFilePath( String fileName ) {
    String filePath;
    if ( tempFolder.getRoot().toString().substring( 1, 2 ).equals( ":" ) ) {
      filePath = tempFolder.getRoot().toString().substring( 2 ) + "/" + fileName;
    } else {
      filePath = tempFolder.getRoot().toString() + "/" + fileName;
    }
    filePath = filePath.replace( "\\", "/" );
    return filePath;
  }

  private void doReadWrite( String[][] inputSchemaDescription, String[][] outputSchemaDescription, Object[] rowData,
                            IPentahoAvroOutputFormat.COMPRESSION compressionType, String outputFileName,
                            String[] defaultValues, Object[] expectedResults, boolean overwrite ) throws Exception {
    List<AvroInputField> avroInputFields = buildAvroInputFields( inputSchemaDescription );
    List<AvroOutputField> avroOutputFields = buildAvroOutputFields( outputSchemaDescription );
    RowMeta rowMeta = buildRowMeta( outputSchemaDescription );

    if ( defaultValues != null ) {
      for ( int i = 0; i < defaultValues.length; i++ ) {
        avroOutputFields.get( i ).setAllowNull( false );
        avroOutputFields.get( i ).setDefaultValue( defaultValues[ i ] );
      }
    }

    String filePath = getFilePath( outputFileName );

    testRecordWriter( avroOutputFields, rowMeta, rowData, compressionType, filePath, overwrite );

    RowMeta outputRowMeta = buildRowMeta( inputSchemaDescription );
    testRecordReader( avroInputFields, rowMeta, avroOutputFields, rowData, filePath, expectedResults );
  }

  private void doReadWrite( String[][] schemaDescription, Object[] rowData,
                            IPentahoAvroOutputFormat.COMPRESSION compressionType, String outputFileName,
                            String[] defaultValues, Object[] expectedResults, boolean overwrite ) throws Exception {
    doReadWrite( schemaDescription, schemaDescription, rowData, compressionType, outputFileName, defaultValues,
      expectedResults, overwrite );
  }

  private void doReadWrite( String[][] schemaDescription, Object[] rowData,
                            IPentahoAvroOutputFormat.COMPRESSION compressionType, String outputFileName,
                            boolean overwrite )
    throws Exception {
    doReadWrite( schemaDescription, schemaDescription, rowData, compressionType, outputFileName, null, null,
      overwrite );
  }

  private void testRecordWriter( List<AvroOutputField> avroOutputFields, RowMeta rowMeta, Object[] rowData,
                                 IPentahoAvroOutputFormat.COMPRESSION compressionType, String filePath,
                                 boolean overwrite )
    throws Exception {

    PentahoAvroOutputFormat avroOutputFormat = new PentahoAvroOutputFormat();
    avroOutputFormat.setNameSpace( "nameSpace" );
    avroOutputFormat.setRecordName( "recordName" );
    avroOutputFormat.setFields( avroOutputFields );
    avroOutputFormat.setCompression( compressionType );
    avroOutputFormat.setOutputFile( filePath, overwrite );

    IPentahoOutputFormat.IPentahoRecordWriter avroRecordWriter = avroOutputFormat.createRecordWriter();
    Assert.assertNotNull( avroRecordWriter, "avroRecordWriter should NOT be null!" );
    Assert.assertTrue( avroRecordWriter instanceof PentahoAvroRecordWriter,
      "avroRecordWriter should be instance of PentahoAvroRecordWriter" );

    avroRecordWriter.write( new RowMetaAndData( rowMeta, rowData ) );

    try {
      avroRecordWriter.close();
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private void testRecordReader( List<AvroInputField> avroInputFields, RowMeta rowMeta,
                                 List<AvroOutputField> avroOutputFields,
                                 Object[] origValues, String filePath, Object[] expectedResults ) throws Exception {

    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( true );

    PentahoAvroInputFormat pentahoAvroInputFormat = new PentahoAvroInputFormat( );
    pentahoAvroInputFormat.setInputFields( avroInputFields );
    pentahoAvroInputFormat.setInputFile( filePath );
    pentahoAvroInputFormat.setOutputRowMeta( rowMeta );
    IPentahoInputFormat.IPentahoRecordReader pentahoRecordReader = pentahoAvroInputFormat.createRecordReader( null );
    for ( RowMetaAndData row : pentahoRecordReader ) {
      for ( int colNum = 0; colNum < avroInputFields.size(); colNum++ ) {
        Object expectedValue = ( expectedResults == null ? origValues[ colNum ] : expectedResults[ colNum ] );
        Object actualValue = row.getData()[ colNum ];
        AvroInputField avroInputField = avroInputFields.get( colNum );
        AvroOutputField avroOutputField = avroOutputFields.get( colNum );
        String defaultValue = avroOutputField.getDefaultValue();

        String errMsg = "field " + colNum + " does not match in " + row;

        if ( expectedValue == null && !avroOutputField.getAllowNull() ) {
          //If here we are comparing the read value to the default value
          if ( avroInputField.getPentahoType() == ValueMetaInterface.TYPE_INET ) {
            assertTrue( errMsg, actualValue.toString().contains( defaultValue ) );
          } else if ( avroInputField.getPentahoType() == ValueMetaInterface.TYPE_DATE ) {
            try {
              assertEquals( errMsg, dateFormat.parse( defaultValue ).toString(), actualValue.toString() );
            } catch ( ParseException e ) {
              e.printStackTrace();
            }
          } else if ( avroInputField.getPentahoType() == ValueMetaInterface.TYPE_TIMESTAMP ) {
            try {
              assertEquals( errMsg, new Timestamp( dateFormat.parse( defaultValue ).getTime() ).toString(),
                actualValue.toString() );
            } catch ( ParseException e ) {
              e.printStackTrace();
            }
          } else if ( avroInputField.getPentahoType() == ValueMetaInterface.TYPE_BINARY ) {
            assertEquals( errMsg, defaultValue, new String( (byte[]) actualValue ) );
          } else {
            assertEquals( errMsg, defaultValue, actualValue.toString() );
          }
        } else {
          // If here we are comparing read value with the original value
          if ( expectedValue instanceof BigDecimal ) {
            assert ( ( (BigDecimal) expectedValue ).compareTo(
              (BigDecimal) actualValue ) == 0 );
          } else if ( expectedValue instanceof byte[] ) {
            assertEquals( errMsg, new String( (byte[]) expectedValue ),
              new String( (byte[]) actualValue ) );
          } else if ( expectedValue instanceof InetAddress ) {
            byte[] origAddress = ( (InetAddress) expectedValue ).getAddress();
            byte[] readAddress = ( (InetAddress) actualValue ).getAddress();
            assertEquals( errMsg, new String( origAddress ), new String( readAddress ) );
          } else {
            assertEquals( errMsg, expectedValue, actualValue );
          }
        }
      }
    }
  }

  private ValueMetaInterface getValueMetaInterface( String fieldName, int fieldType ) {
    switch ( fieldType ) {
      case ValueMetaInterface.TYPE_INET:
        return new ValueMetaInternetAddress( fieldName );
      case ValueMetaInterface.TYPE_STRING:
        return new ValueMetaString( fieldName );
      case ValueMetaInterface.TYPE_INTEGER:
        return new ValueMetaInteger( fieldName );
      case ValueMetaInterface.TYPE_NUMBER:
        return new ValueMetaNumber( fieldName );
      case ValueMetaInterface.TYPE_BIGNUMBER:
        return new ValueMetaBigNumber( fieldName );
      case ValueMetaInterface.TYPE_TIMESTAMP:
        ValueMetaTimestamp valueMetaTimestamp = new ValueMetaTimestamp( fieldName );
        valueMetaTimestamp.setConversionMask( "yyyy/MM/dd HH:mm:ss.SSS" );
        return valueMetaTimestamp;
      case ValueMetaInterface.TYPE_DATE:
        ValueMetaDate valueMetaDate = new ValueMetaDate( fieldName );
        valueMetaDate.setConversionMask( "yyyy/MM/dd HH:mm:ss.SSS" );
        return valueMetaDate;
      case ValueMetaInterface.TYPE_BOOLEAN:
        return new ValueMetaBoolean( fieldName );
      case ValueMetaInterface.TYPE_BINARY:
        return new ValueMetaBinary( fieldName );
    }
    return null;
  }

}
