/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.transexecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.StepMockUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Andrey Khayrutdinov
 */
public class TransExecutorUnitTest {

  @BeforeClass
  public static void initKettle() throws Exception {
    KettleEnvironment.init();
  }

  private TransExecutor executor;
  private TransExecutorMeta meta;
  private TransExecutorData data;
  private Trans internalTrans;
  private Result internalResult;

  @Before
  public void setUp() throws Exception {
    executor = StepMockUtil.getStep( TransExecutor.class, TransExecutorMeta.class, "TransExecutorUnitTest" );
    executor = spy( executor );

    TransMeta internalTransMeta = mock( TransMeta.class );
    doReturn( internalTransMeta ).when( executor ).loadExecutorTransMeta();

    internalTrans = mock( Trans.class );
    doReturn( internalTrans ).when( executor ).createInternalTrans();
    internalResult = new Result();
    when( internalTrans.getResult() ).thenReturn( internalResult );

    meta = new TransExecutorMeta();
    data = new TransExecutorData();
  }

  @After
  public void tearDown() {
    executor = null;
    meta = null;
    data = null;
    internalTrans = null;
    internalResult = null;
  }


  @Test
  public void collectsResultsFromInternalTransformation() throws Exception {
    passOneRowToExecutor();

    RowMetaAndData expectedResult = new RowMetaAndData( new RowMeta(), "fake result" );
    internalResult.getRows().add( expectedResult );

    RowSet rowSet = new QueueRowSet();
    // any value except null
    StepMeta stepMeta = mockStepAndMapItToRowSet( "stepMetaMock", rowSet );
    meta.setOutputRowsSourceStepMeta( stepMeta );

    executor.init( meta, data );
    executor.setInputRowMeta( new RowMeta() );
    assertTrue( "Passing one line at first time", executor.processRow( meta, data ) );
    assertFalse( "Executing the internal trans during the second round", executor.processRow( meta, data ) );

    Object[] resultsRow = rowSet.getRowImmediate();
    assertNotNull( resultsRow );
    assertArrayEquals( expectedResult.getData(), resultsRow );
    assertNull( "Only one row is expected", rowSet.getRowImmediate() );
  }


  @Test
  public void collectsExecutionResults() throws Exception {
    passOneRowToExecutor();

    StepMeta parentStepMeta = mock( StepMeta.class );
    when( parentStepMeta.getName() ).thenReturn( "parentStepMeta" );
    meta.setParentStepMeta( parentStepMeta );

    internalResult.setResult( true );
    meta.setExecutionResultField( "executionResultField" );

    internalResult.setNrErrors( 1 );
    meta.setExecutionNrErrorsField( "executionNrErrorsField" );

    internalResult.setNrLinesRead( 2 );
    meta.setExecutionLinesReadField( "executionLinesReadField" );

    internalResult.setNrLinesWritten( 3 );
    meta.setExecutionLinesWrittenField( "executionLinesWrittenField" );

    internalResult.setNrLinesInput( 4 );
    meta.setExecutionLinesInputField( "executionLinesInputField" );

    internalResult.setNrLinesOutput( 5 );
    meta.setExecutionLinesOutputField( "executionLinesOutputField" );

    internalResult.setNrLinesRejected( 6 );
    meta.setExecutionLinesRejectedField( "executionLinesRejectedField" );

    internalResult.setNrLinesUpdated( 7 );
    meta.setExecutionLinesUpdatedField( "executionLinesUpdatedField" );

    internalResult.setNrLinesDeleted( 8 );
    meta.setExecutionLinesDeletedField( "executionLinesDeletedField" );

    internalResult.setNrFilesRetrieved( 9 );
    meta.setExecutionFilesRetrievedField( "executionFilesRetrievedField" );

    internalResult.setExitStatus( 10 );
    meta.setExecutionExitStatusField( "executionExitStatusField" );


    RowSet rowSet = new QueueRowSet();
    // any value except null
    StepMeta stepMeta = mockStepAndMapItToRowSet( "stepMetaMock", rowSet );
    meta.setExecutionResultTargetStepMeta( stepMeta );

    executor.init( meta, data );
    executor.setInputRowMeta( new RowMeta() );
    assertTrue( "Passing one line at first time", executor.processRow( meta, data ) );
    assertFalse( "Executing the internal trans during the second round", executor.processRow( meta, data ) );

    Object[] resultsRow = rowSet.getRowImmediate();
    assertNotNull( resultsRow );
    assertNull( "Only one row is expected", rowSet.getRowImmediate() );

    assertEquals( internalResult.getResult(), resultsRow[ 0 ] );
    assertEquals( internalResult.getNrErrors(), resultsRow[ 1 ] );
    assertEquals( internalResult.getNrLinesRead(), resultsRow[ 2 ] );
    assertEquals( internalResult.getNrLinesWritten(), resultsRow[ 3 ] );
    assertEquals( internalResult.getNrLinesInput(), resultsRow[ 4 ] );
    assertEquals( internalResult.getNrLinesOutput(), resultsRow[ 5 ] );
    assertEquals( internalResult.getNrLinesRejected(), resultsRow[ 6 ] );
    assertEquals( internalResult.getNrLinesUpdated(), resultsRow[ 7 ] );
    assertEquals( internalResult.getNrLinesDeleted(), resultsRow[ 8 ] );
    assertEquals( internalResult.getNrFilesRetrieved(), resultsRow[ 9 ] );
    assertEquals( internalResult.getExitStatus(), ( (Number) resultsRow[ 10 ] ).intValue() );
  }


  private void passOneRowToExecutor() throws Exception {
    doReturn( new Object[] { "row" } ).doReturn( null ).when( executor ).getRow();
  }

  private StepMeta mockStepAndMapItToRowSet( String stepName, RowSet rowSet ) throws Exception {
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getName() ).thenReturn( stepName );
    doReturn( rowSet ).when( executor ).findOutputRowSet( stepName );
    return stepMeta;
  }
}
