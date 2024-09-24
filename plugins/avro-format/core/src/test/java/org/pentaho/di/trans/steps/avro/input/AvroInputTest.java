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
package org.pentaho.di.trans.steps.avro.input;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.RowHandler;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith( MockitoJUnitRunner.class )

/**
 * This test simulates two avro file contents being sent as pdi input fields.  Each file contains two rows of data so
 * the processRow method should be successfull 4 times.  We check that all 4 rows are returned and that the fields
 * passed to pdi output are syncronized with the input row that corresponds to the avro content processed.
 *
 * @author tkafalas
 */
public class AvroInputTest {

  static final String INPUT_STEP_NAME = "Input Step Name";
  static final String INPUT_STREAM_FIELD_NAME = "inputStreamFieldName";
  static final String PASS_FIELD_NAME = "passFieldName";
  int currentInputRow;

  @Mock( extraInterfaces = StepMetaInterface.class )
  private StepMeta mockStepMeta;
  @Mock
  private StepDataInterface mockStepDataInterface;
  @Mock
  private TransMeta mockTransMeta;
  @Mock
  private Trans mockTrans;
  @Mock
  private AvroInputData sdi;
  @Mock
  private RowHandler mockRowHandler;
  @Mock
  private IPentahoAvroInputFormat mockPentahoAvroInputFormat;
  @Mock
  IPentahoAvroInputFormat.IPentahoRecordReader mockPentahoAvroRecordReader;

  private AvroInputMeta avroInputMeta;
  private AvroInput avroInput;
  private RowMeta avroRowMeta;
  private RowMetaAndData[] avroRows;
  private RowMeta inputRowMeta;
  private RowMetaAndData[] inputRows;

  @Before
  public void setUp() throws Exception {
    currentInputRow = 0;
    setInputRows();
    setAvroRows();
    avroInputMeta = new AvroInputMeta( );
    avroInputMeta.setDataLocation( INPUT_STREAM_FIELD_NAME, AvroInputMetaBase.LocationDescriptor.FIELD_NAME );

    avroInputMeta.setParentStepMeta( mockStepMeta );
    when( mockStepMeta.getName() ).thenReturn( INPUT_STEP_NAME );
    when( mockTransMeta.findStep( INPUT_STEP_NAME ) ).thenReturn( mockStepMeta );

    avroInput = new AvroInput( mockStepMeta, mockStepDataInterface, 0, mockTransMeta,
      mockTrans );
    avroInput.setRowHandler( mockRowHandler );
    avroInput.setInputRowMeta( inputRowMeta );
    avroInput.setLogLevel( LogLevel.ERROR );

  }

  private Object[] returnNextInputRow() {
    Object[] result = null;
    if ( currentInputRow < inputRows.length ) {
      result = inputRows[ currentInputRow ].getData().clone();
      currentInputRow++;
    } else {
      result = null;
    }
    return result;
  }

  @Test
  public void testEmbedSetup() {
    avroInput.init( (StepMetaInterface) mockStepMeta, mockStepDataInterface );
  }

  // TODO We need to get this test working, it is failing for some odd reason
  public void testProcessRow() throws Exception {
    boolean result;
    int rowsProcessed = 0;
    ArgumentCaptor<RowMeta> rowMetaCaptor = ArgumentCaptor.forClass( RowMeta.class );
    ArgumentCaptor<Object[]> dataCaptor = ArgumentCaptor.forClass( Object[].class );

    do {
      result = avroInput.processRow( avroInputMeta, sdi );
      if ( result == true ) {
        rowsProcessed++;
      }
    } while ( result == true );

    assertEquals( 4, rowsProcessed ); // 2 files 2 rows each
    verify( mockRowHandler, times( 4 ) ).putRow( rowMetaCaptor.capture(), dataCaptor.capture() );
    List<RowMeta> rowMeta = rowMetaCaptor.getAllValues();
    List<Object[]> dataCaptured = dataCaptor.getAllValues();
    for ( int rowNum = 0; rowNum < 4; rowNum++ ) {
      assertEquals( 0, rowMeta.get( rowNum ).indexOfValue( "str" ) );
      assertEquals( "string" + ( rowNum % 2 + 1 ), dataCaptured.get( rowNum )[ 0 ] );
    }

  }

  @Test
  public void testInit() {
    assertEquals( true, avroInput.init() );
  }

  private RowMeta setAvroRowMeta() {
    avroRowMeta = new RowMeta();
    ValueMetaInterface valueMetaString = new ValueMetaString( "str" );
    avroRowMeta.addValueMeta( valueMetaString );
    ValueMetaInterface valueMetaBoolean = new ValueMetaBoolean( "bool" );
    avroRowMeta.addValueMeta( valueMetaBoolean );
    ValueMetaInterface valueMetaInteger = new ValueMetaInteger( "int" );
    avroRowMeta.addValueMeta( valueMetaInteger );
    return avroRowMeta;
  }

  private RowMeta setInputRowMeta() {
    inputRowMeta = new RowMeta();
    ValueMetaInterface valueMetaString = new ValueMetaString( INPUT_STREAM_FIELD_NAME );
    inputRowMeta.addValueMeta( valueMetaString );
    ValueMetaInterface valueMetaString2 = new ValueMetaString( PASS_FIELD_NAME );
    inputRowMeta.addValueMeta( valueMetaString2 );
    return inputRowMeta;
  }

  private void setInputRows() {
    setInputRowMeta();
    inputRows = new RowMetaAndData[] {
      new RowMetaAndData( avroRowMeta, "avroFile1", "pass1" ),
      new RowMetaAndData( avroRowMeta, "avroFile2", "pass2" )
    };

  }

  private void setAvroRows() {
    setAvroRowMeta();
    avroRows = new RowMetaAndData[] {
      new RowMetaAndData( avroRowMeta, "string1", true, 123 ),
      new RowMetaAndData( avroRowMeta, "string2", true, 321  )
    };
  }

  private class AvroRecordIterator implements Iterator<RowMetaAndData> {
    Iterator<RowMetaAndData> iter;
    boolean reset;

    AvroRecordIterator() {
      init();
    }

    private void init() {
      iter = Arrays.asList( avroRows ).iterator();
      reset = false;
    }

    @Override public boolean hasNext() {
      if ( reset ) {
        init();
      }
      if ( !iter.hasNext() ) {
        reset = true;
      }
      return iter.hasNext();
    }

    @Override public RowMetaAndData next() {
      if ( reset ) {
        init(); // Simultate a new iterator for the new file
      }
      return iter.next().clone();
    }
  }
}
