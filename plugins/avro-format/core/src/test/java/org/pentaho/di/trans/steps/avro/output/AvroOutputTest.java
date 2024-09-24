/*! ******************************************************************************
 *
 * Pentaho Data Integration
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
package org.pentaho.di.trans.steps.avro.output;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.reporting.libraries.base.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )
public class AvroOutputTest {

  private static final String OUTPUT_STEP_NAME = "Output Step Name";
  private static final String OUTPUT_FILE_NAME = "outputFileName";

  @Mock
  private StepMeta mockStepMeta;
  @Mock
  private StepDataInterface mockStepDataInterface;
  @Mock
  private TransMeta mockTransMeta;
  @Mock
  private Trans mockTrans;
  @Mock
  private AvroOutputData avroOutputData;
  @Mock
  private RowHandler mockRowHandler;
  @Mock
  private IPentahoAvroOutputFormat mockPentahoAvroOutputFormat;
  @Mock
  private LogChannelInterface mockLogChannelInterface;
  @Mock
  private IPentahoAvroOutputFormat.IPentahoRecordWriter mockPentahoAvroRecordWriter;

  private List<AvroOutputField> avroOutputFields;
  private AvroOutputMeta avroOutputMeta;
  private AvroOutput avroOutput;
  private RowMeta dataInputRowMeta;
  private RowMetaAndData[] dataInputRows;
  private int currentAvroRow;

  @Before
  public void setUp() throws Exception {
    currentAvroRow = 0;
    setDataInputRows();
    setAvroOutputRows();
    avroOutputMeta = new AvroOutputMeta( );
    avroOutputMeta.setFilename( OUTPUT_FILE_NAME );
    avroOutputMeta.setOutputFields( avroOutputFields );

    avroOutputMeta.setParentStepMeta( mockStepMeta );
    when( mockStepMeta.getName() ).thenReturn( OUTPUT_STEP_NAME );
    when( mockTransMeta.findStep( OUTPUT_STEP_NAME ) ).thenReturn( mockStepMeta );

    avroOutput = spy( new AvroOutput( mockStepMeta, mockStepDataInterface, 0, mockTransMeta, mockTrans ) );
    avroOutput.setInputRowMeta( dataInputRowMeta );
    avroOutput.setRowHandler( mockRowHandler );
    avroOutput.setLogLevel( LogLevel.ERROR );
  }

  // TODO We need to get this test working, it is failing for some odd reason
  public void testProcessRow() throws Exception {
    boolean result;
    int rowsProcessed = 0;
    ArgumentCaptor<RowMeta> rowMetaCaptor = ArgumentCaptor.forClass( RowMeta.class );
    ArgumentCaptor<Object[]> dataCaptor = ArgumentCaptor.forClass( Object[].class );

    do {
      result = avroOutput.processRow( avroOutputMeta, avroOutputData );
      if ( result ) {
        rowsProcessed++;
      }
    } while ( result );

    // 3 rows to be outputted to an avro file
    assertEquals( 3, rowsProcessed );
    verify( mockRowHandler, times( 3 ) ).putRow( rowMetaCaptor.capture(), dataCaptor.capture() );
    List<RowMeta> rowMetaCaptured = rowMetaCaptor.getAllValues();
    List<Object[]> dataCaptured = dataCaptor.getAllValues();
    for ( int rowNum = 0; rowNum < 3; rowNum++ ) {
      assertEquals( 0, rowMetaCaptured.get( rowNum ).indexOfValue( "StringName" ) );
      assertEquals( "string" + ( rowNum % 3 + 1 ), dataCaptured.get( rowNum )[ 0 ] );
    }
  }

  @Test
  public void testInit() {
    try {
      // initialize meta and data objects before verifying the main init step that runs.
      avroOutput.init( avroOutputMeta, avroOutputData );
    } catch ( Exception ex ) {
      fail( "Something failed with the initialization of the AvroOutputStep: " + ex.getMessage() );
    }
  }

  @Test
  public void testProcessRowFailureUnsupported() throws Exception {
    doThrow( new UnsupportedOperationException( "UnsupportedExceptionMessage" ) ).when( avroOutput ).init();
    when( avroOutput.getLogChannel() ).thenReturn( mockLogChannelInterface );
    assertFalse( avroOutput.processRow( avroOutputMeta, avroOutputData ) );

    verify( mockLogChannelInterface,
      times( 1 ) )
      .logError( "UnsupportedExceptionMessage" );
  }

  @Test
  public void testProcessRowFailureGeneral() throws Exception {
    doThrow( new KettleException( "Error in TRANS_NAME, STEP_NAME" ) ).when( avroOutput ).init();
    when( avroOutput.getLogChannel() ).thenReturn( mockLogChannelInterface );
    when( mockTrans.getName() ).thenReturn( "TestTransFail" );
    when( avroOutput.getStepname() ).thenReturn( "TestAvroOutput" );
    assertFalse( avroOutput.processRow( avroOutputMeta, avroOutputData ) );

    verify( mockLogChannelInterface,
      times( 1 ) )
      .logError( StringUtils.getLineSeparator() + "Error in TestTransFail, TestAvroOutput" + StringUtils.getLineSeparator() );
  }

  private Object[] returnNextAvroRow() {
    Object[] result = null;
    if ( currentAvroRow < dataInputRows.length ) {
      result = dataInputRows[ currentAvroRow ].getData().clone();
      currentAvroRow++;
    }
    return result;
  }

  private void setAvroOutputRows() {
    AvroOutputField avroOutputField = mock( AvroOutputField.class );
    avroOutputFields =  new ArrayList<>();
    avroOutputFields.add( avroOutputField );
  }

  private void setDataInputRowMeta() {
    dataInputRowMeta = new RowMeta();
    ValueMetaInterface valueMetaString = new ValueMetaString( "StringName" );
    dataInputRowMeta.addValueMeta( valueMetaString );
  }

  private void setDataInputRows() {
    setDataInputRowMeta();
    dataInputRows = new RowMetaAndData[] {
      new RowMetaAndData( dataInputRowMeta, "string1" ),
      new RowMetaAndData( dataInputRowMeta, "string2" ),
      new RowMetaAndData( dataInputRowMeta, "string3" )
    };
  }
}
