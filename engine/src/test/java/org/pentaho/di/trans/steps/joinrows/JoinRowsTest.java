/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.joinrows;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.RowStepCollector;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Denis Mashukov
 */
public class JoinRowsTest {

  private StepMetaInterface meta;
  private JoinRowsData data;

  @Before
  public void setUp() throws Exception {
    meta = new JoinRowsMeta();
    data = new JoinRowsData();
  }

  @After
  public void tearDown() {
    meta = null;
    data = null;
  }

  /**
   * BACKLOG-8520 Check that method call does't throw an error NullPointerException.
   */
  @Test
  public void checkThatMethodPerformedWithoutError() throws Exception {
    getJoinRows().dispose( meta, data );
  }

  @Test
  public void disposeDataFiles() throws Exception {
    File mockFile1 = mock( File.class );
    File mockFile2 = mock( File.class );
    data.file = new File[] {null, mockFile1, mockFile2};
    getJoinRows().dispose( meta, data );
    verify( mockFile1, times( 1 ) ).delete();
    verify( mockFile2, times( 1 ) ).delete();
  }

  private JoinRows getJoinRows() throws Exception {
    StepMeta stepMeta = new StepMeta();
    TransMeta transMeta = new TransMeta();
    Trans trans = new Trans( transMeta );

    transMeta.clear();
    transMeta.addStep( stepMeta );
    transMeta.setStep( 0, stepMeta );
    stepMeta.setName( "test" );
    trans.setLog( mock( LogChannelInterface.class ) );
    trans.prepareExecution( null );
    trans.startThreads();

    return new JoinRows( stepMeta, null, 0, transMeta, trans );
  }

  @Test
  public void testJoinRowsStep() throws Exception {
    JoinRowsMeta joinRowsMeta = new JoinRowsMeta();
    joinRowsMeta.setMainStepname( "main step name" );
    joinRowsMeta.setPrefix( "out" );
    joinRowsMeta.setCacheSize( 3 );

    JoinRowsData joinRowsData = new JoinRowsData();

    JoinRows joinRows = getJoinRows();
    joinRows.getTrans().setRunning( true );

    joinRows.init( joinRowsMeta, joinRowsData );


    List<RowSet> rowSets = new ArrayList<>();
    rowSets.add( getRowSetWithData( 3, "main --", true ) );
    rowSets.add( getRowSetWithData( 3, "secondary --", false ) );

    joinRows.setInputRowSets( rowSets );

    RowStepCollector rowStepCollector = new RowStepCollector();

    joinRows.addRowListener( rowStepCollector );
    joinRows.getLogChannel().setLogLevel( LogLevel.ROWLEVEL );
    KettleLogStore.init();


    while ( true ) {
      if ( !joinRows.processRow( joinRowsMeta, joinRowsData ) ) {
        break;
      }
    }

    rowStepCollector.getRowsWritten();

    //since we have data join of two row sets with size 3 then we must have 9 written rows
    assertEquals( 9, rowStepCollector.getRowsWritten().size() );
    assertEquals( 6, rowStepCollector.getRowsRead().size() );

    Object[][] expectedResult = createExpectedResult();

    List<Object[]> rowWritten = rowStepCollector.getRowsWritten().stream().map( RowMetaAndData::getData ).collect( Collectors.toList() );

    for ( int i = 0; i < 9; i++ ) {
      assertTrue( Arrays.equals( expectedResult[i], rowWritten.get( i ) ) );
    }
  }


  BlockingRowSet getRowSetWithData( int size, String dataPrefix, boolean isMainStep ) {
    BlockingRowSet blockingRowSet = new BlockingRowSet( size );
    RowMeta rowMeta = new RowMeta();

    ValueMetaInterface valueMetaString = new ValueMetaString( dataPrefix + " first value name" );
    ValueMetaInterface valueMetaInteger = new ValueMetaString( dataPrefix + " second value name" );
    ValueMetaInterface valueMetaBoolean = new ValueMetaString( dataPrefix + " third value name" );

    rowMeta.addValueMeta( valueMetaString );
    rowMeta.addValueMeta( valueMetaInteger );
    rowMeta.addValueMeta( valueMetaBoolean );

    blockingRowSet.setRowMeta( rowMeta );

    for ( int i = 0; i < size; i++ ) {
      Object[] rowData = new Object[3];
      rowData[0] = dataPrefix + " row[" + i + "]-first value";
      rowData[1] = dataPrefix + " row[" + i + "]-second value";
      rowData[2] = dataPrefix + " row[" + i + "]-third value";
      blockingRowSet.putRow( rowMeta, rowData );
    }

    if ( isMainStep ) {
      blockingRowSet.setThreadNameFromToCopy( "main step name", 0, null, 0 );
    } else {
      blockingRowSet.setThreadNameFromToCopy( "secondary step name", 0, null, 0 );
    }

    blockingRowSet.setDone();

    return blockingRowSet;
  }

  private Object[][] createExpectedResult() {
    Object[][] objects = {{"main -- row[0]-first value", "main -- row[0]-second value", "main -- row[0]-third value", "secondary -- row[0]-first value", "secondary -- row[0]-second value", "secondary -- row[0]-third value"},
      {"main -- row[0]-first value", "main -- row[0]-second value", "main -- row[0]-third value", "secondary -- row[1]-first value", "secondary -- row[1]-second value", "secondary -- row[1]-third value"},
      {"main -- row[0]-first value", "main -- row[0]-second value", "main -- row[0]-third value", "secondary -- row[2]-first value", "secondary -- row[2]-second value", "secondary -- row[2]-third value"},
      {"main -- row[1]-first value", "main -- row[1]-second value", "main -- row[1]-third value", "secondary -- row[0]-first value", "secondary -- row[0]-second value", "secondary -- row[0]-third value"},
      {"main -- row[1]-first value", "main -- row[1]-second value", "main -- row[1]-third value", "secondary -- row[1]-first value", "secondary -- row[1]-second value", "secondary -- row[1]-third value"},
      {"main -- row[1]-first value", "main -- row[1]-second value", "main -- row[1]-third value", "secondary -- row[2]-first value", "secondary -- row[2]-second value", "secondary -- row[2]-third value"},
      {"main -- row[2]-first value", "main -- row[2]-second value", "main -- row[2]-third value", "secondary -- row[0]-first value", "secondary -- row[0]-second value", "secondary -- row[0]-third value"},
      {"main -- row[2]-first value", "main -- row[2]-second value", "main -- row[2]-third value", "secondary -- row[1]-first value", "secondary -- row[1]-second value", "secondary -- row[1]-third value"},
      {"main -- row[2]-first value", "main -- row[2]-second value", "main -- row[2]-third value", "secondary -- row[2]-first value", "secondary -- row[2]-second value", "secondary -- row[2]-third value"}};
    return objects;
  }
}
