/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.StepMockUtil;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class TransExecutorUnitTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

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

    internalTrans = spy( new Trans() );
    internalTrans.setLog( mock( LogChannelInterface.class ) );
    doNothing().when( internalTrans ).prepareExecution( nullable( String[].class ) );
    doNothing().when( internalTrans ).startThreads();
    doNothing().when( internalTrans ).waitUntilFinished();
    doNothing().when( executor ).discardLogLines( any( TransExecutorData.class ) );

    doReturn( internalTrans ).when( executor ).createInternalTrans();
    internalResult = new Result();
    doReturn( internalResult ).when( internalTrans ).getResult();

    meta = new TransExecutorMeta();
    data = new TransExecutorData();
  }

  @After
  public void cleanUp() {
    executor = null;
    meta = null;
    data = null;
    internalTrans = null;
    internalResult = null;
  }

  @Test
  public void testCreateInternalTransSetsRepository() throws KettleException {
    Trans transParentMock = mock( Trans.class );
    Repository repositoryMock = mock( Repository.class );
    TransExecutorData transExecutorDataMock = mock( TransExecutorData.class );
    TransMeta transMetaMock = mock( TransMeta.class );

    executor.init( meta, data );
    when( transParentMock.getRepository() ).thenReturn( repositoryMock );
    when( transParentMock.getLogLevel() ).thenReturn( LogLevel.DEBUG );
    doNothing().when( transParentMock ).initializeVariablesFrom( any( VariableSpace.class ) );
    when( executor.getLogLevel() ).thenReturn( LogLevel.DEBUG );
    when( executor.createInternalTrans() ).thenCallRealMethod();
    when( executor.getTrans() ).thenReturn( transParentMock );
    when( executor.getData() ).thenReturn( transExecutorDataMock );
    when( transMetaMock.listVariables() ).thenReturn( new String[0] );
    when( transMetaMock.listParameters() ).thenReturn( new String[0] );
    when( transExecutorDataMock.getExecutorTransMeta() ).thenReturn( transMetaMock );

    Trans internalTrans = executor.createInternalTrans();
    assertNotNull( internalTrans );

    Trans parentTrans = internalTrans.getParentTrans();
    assertEquals( parentTrans, transParentMock );
    assertEquals( parentTrans.getRepository(), repositoryMock );
    assertEquals( internalTrans.getRepository(), repositoryMock );
  }


  @Test
  public void collectsResultsFromInternalTransformation() throws Exception {
    prepareOneRowForExecutor();

    RowMetaAndData expectedResult = new RowMetaAndData( new RowMeta(), "fake result" );
    internalResult.getRows().add( expectedResult );

    RowSet rowSet = new QueueRowSet();
    // any value except null
    StepMeta stepMeta = mockStepAndMapItToRowSet( "stepMetaMock", rowSet );
    meta.setOutputRowsSourceStepMeta( stepMeta );

    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

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
    prepareOneRowForExecutor();

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

    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

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

  /**
   * Given an input data and a transformation executor with specified field to group rows on.
   * <br/>
   * When transformation executor is processing rows of an input data,
   * then rows should be accumulated in a group as long as the specified field value stays the same.
   */
  @Test
  public void shouldAccumulateRowsWhenGroupFieldIsSpecified() throws KettleException {
    prepareMultipleRowsForExecutor();

    meta.setGroupField( "groupField" );

    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "groupField" ) );
    executor.setInputRowMeta( rowMeta );

    // start processing
    executor.processRow( meta, data ); // 1st row - 'value1'
    // should be added to group buffer
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row - still 'value1'
    // first 4 rows should be added to the same group
    assertEquals( 4, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 5th row - value has been changed - 'value12'
    // previous group buffer should be flushed
    // and a new group should be started
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 6th row - 'value12'
    executor.processRow( meta, data ); // 7th row - 'value12'
    // the rest rows should be added to another group
    assertEquals( 3, data.groupBuffer.size() );

    executor.processRow( meta, data ); // end of file
    // group buffer should be flushed in the end
    assertEquals( 0, data.groupBuffer.size() );
  }

  /**
   * Given an input data and a transformation executor
   * with specified number of rows to send to the transformation (X).
   * <br/>
   * When transformation executor is processing rows of an input data,
   * then every X rows should be accumulated in a group.
   */
  @Test
  public void shouldAccumulateRowsByCount() throws KettleException {
    prepareMultipleRowsForExecutor();

    meta.setGroupSize( "5" );
    executor.init( meta, data );

    // start processing
    executor.processRow( meta, data ); // 1st row
    // should be added to group buffer
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row
    // first 4 rows should be added to the same group
    assertEquals( 4, data.groupBuffer.size() );

    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.processRow( meta, data ); // 5th row
    // once the 5th row is processed, the transformation executor should be triggered
    // and thus, group buffer should be flushed
    assertEquals( 0, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 6th row
    // previous group buffer should be flushed
    // and a new group should be started
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data ); // 7th row
    // the rest rows should be added to another group
    assertEquals( 2, data.groupBuffer.size() );

    executor.processRow( meta, data ); // end of file
    // group buffer should be flushed in the end
    assertEquals( 0, data.groupBuffer.size() );
  }

  @Test
  public void testCollectTransResultsDisabledHop() throws KettleException {
    StepMeta outputRowsSourceStepMeta = mock( StepMeta.class );
    meta.setOutputRowsSourceStepMeta( outputRowsSourceStepMeta );

    Result result = mock( Result.class );
    RowMetaAndData rowMetaAndData = mock( RowMetaAndData.class );
    when( result.getRows() ).thenReturn( Arrays.asList( rowMetaAndData ) );

    doNothing().when( executor ).putRowTo( any(), any(), any() );

    executor.init( meta, data );
    executor.collectTransResults( result );
    verify( executor, never() ).putRowTo( any(), any(), any() );
  }

  @Test
  public void testCollectExecutionResultsDisabledHop() throws KettleException {
    StepMeta executionResultTargetStepMeta = mock( StepMeta.class );
    meta.setExecutionResultTargetStepMeta( executionResultTargetStepMeta );

    RowMetaInterface executionResultsOutputRowMeta = mock( RowMetaInterface.class );
    data.setExecutionResultsOutputRowMeta( executionResultsOutputRowMeta );

    doNothing().when( executor ).putRowTo( any(), any(), any() );

    executor.init( meta, data );
    Result result = mock( Result.class );
    executor.collectExecutionResults( result );

    verify( executor, never() ).putRowTo( any(), any(), any() );
  }

  @Test
  public void testCollectExecutionResultFilesDisabledHop() throws KettleException {
    Result result = mock( Result.class );
    ResultFile resultFile = mock( ResultFile.class, RETURNS_DEEP_STUBS );

    when( result.getResultFilesList() ).thenReturn( Arrays.asList( resultFile ) );

    StepMeta resultFilesTargetStepMeta = mock( StepMeta.class );
    meta.setResultFilesTargetStepMeta( resultFilesTargetStepMeta );

    RowMetaInterface resultFilesOutputRowMeta = mock( RowMetaInterface.class );
    data.setResultFilesOutputRowMeta( resultFilesOutputRowMeta );

    doNothing().when( executor ).putRowTo( any(), any(), any() );

    executor.init( meta, data );
    executor.collectExecutionResultFiles( result );

    verify( executor, never() ).putRowTo( any(), any(), any() );
  }

  // values to be grouped
  private void prepareMultipleRowsForExecutor() throws KettleException {
    doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value1" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( new Object[] { "value12" } )
      .doReturn( null )
      .when( executor ).getRow();
  }

  private void prepareOneRowForExecutor() throws Exception {
    doReturn( new Object[] { "row" } ).doReturn( null ).when( executor ).getRow();
  }

  private StepMeta mockStepAndMapItToRowSet( String stepName, RowSet rowSet ) throws KettleStepException {
    StepMeta stepMeta = mock( StepMeta.class );
    when( stepMeta.getName() ).thenReturn( stepName );
    doReturn( rowSet ).when( executor ).findOutputRowSet( stepName );
    return stepMeta;
  }

  @Test
  //PDI-16066
  public void testExecuteTrans() throws KettleException {

    String childParam = "childParam";
    String childValue = "childValue";
    String paramOverwrite = "paramOverwrite";
    String parentValue = "parentValue";

    meta.getParameters().setVariable( new String[]{ childParam, paramOverwrite } );
    meta.getParameters().setInput( new String[]{ null, null } );
    meta.getParameters().setField( new String[]{ null, null } );
    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );

    executor.setVariable( paramOverwrite, parentValue );
    executor.setVariable( childParam, childValue );

    Mockito.when( executor.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    parent.setLog( new LogChannel( this ) );
    Mockito.doCallRealMethod().when( executor ).createInternalTrans( );
    Mockito.when(  executor.getData().getExecutorTransMeta().listVariables() ).thenReturn( new String[0] );
    Mockito.when(  executor.getData().getExecutorTransMeta().listParameters() ).thenReturn( new String[0] /*{parentParam}*/ );

    Trans internalTrans = executor.createInternalTrans();
    executor.getData().setExecutorTrans( internalTrans );
    executor.passParametersToTrans( Arrays.asList( meta.getParameters().getInput() ) );

    //When the child parameter does exist in the parent parameters, overwrite the child parameter by the parent parameter.
    Assert.assertEquals( parentValue, internalTrans.getVariable( paramOverwrite ) );

    //All other parent parameters need to get copied into the child parameters  (when the 'Inherit all variables from the transformation?' option is checked)
    Assert.assertEquals( childValue, internalTrans.getVariable( childParam ) );
  }

  @Test
  //PDI-16066
  public void testExecuteTransWithFieldsAndNoInput() throws KettleException {
    String childParam = "childParam";
    String childValue = "childValue";
    String fieldValue1 = "fieldValue1";
    String fieldValue2 = "fieldValue2";
    String paramOverwrite = "paramOverwrite";
    String parentValue = "parentValue";

    meta.getParameters().setVariable( new String[]{ childParam, paramOverwrite } );
    meta.getParameters().setInput( new String[]{ null, null } );
    meta.getParameters().setField( new String[]{ childParam, paramOverwrite } );
    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );

    executor.setVariable( paramOverwrite, parentValue );
    executor.setVariable( childParam, childValue );

    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );

    Mockito.when( executor.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    parent.setLog( new LogChannel( this ) );
    Mockito.doCallRealMethod().when( executor ).createInternalTrans( );
    Mockito.when(  executor.getData().getExecutorTransMeta().listVariables() ).thenReturn( new String[0] );
    Mockito.when(  executor.getData().getExecutorTransMeta().listParameters() ).thenReturn( new String[0] /*{parentParam}*/ );

    executor.getData().setInputRowMeta( inputRowMeta );
    Mockito.when(  executor.getData().getInputRowMeta().getFieldNames() ).thenReturn( new String[]{"childParam", "paramOverwrite"} );

    Trans internalTrans = executor.createInternalTrans();
    executor.getData().setExecutorTrans( internalTrans );
    executor.passParametersToTrans( Arrays.asList( new String[]{ fieldValue1, fieldValue2 } ) );

    //When the child parameter does exist in the parent parameters, overwrite the child parameter by the parent parameter.
    Assert.assertEquals( fieldValue2, internalTrans.getVariable( paramOverwrite ) );

    //All other parent parameters need to get copied into the child parameters  (when the 'Inherit all variables from the transformation?' option is checked)
    Assert.assertEquals( fieldValue1, internalTrans.getVariable( childParam ) );
  }

  @Test
  //PDI-19098
  public void testExecuteTransWithFieldsCaseInsensitive() throws KettleException {
    String childParam = "childParam";
    String childValue = "childValue";
    String fieldValue1 = "fieldValue1";
    String fieldValue2 = "fieldValue2";
    String paramOverwrite = "paramOverwrite";
    String parentValue = "parentValue";

    meta.getParameters().setVariable( new String[]{ childParam, paramOverwrite } );
    meta.getParameters().setInput( new String[]{ null, null } );
    meta.getParameters().setField( new String[]{ "CHILDPARAM", paramOverwrite } );
    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );

    executor.setVariable( paramOverwrite, parentValue );
    executor.setVariable( childParam, childValue );

    RowMetaInterface inputRowMeta = mock( RowMetaInterface.class );

    Mockito.when( executor.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    parent.setLog( new LogChannel( this ) );
    Mockito.doCallRealMethod().when( executor ).createInternalTrans( );
    Mockito.when(  executor.getData().getExecutorTransMeta().listVariables() ).thenReturn( new String[0] );
    Mockito.when(  executor.getData().getExecutorTransMeta().listParameters() ).thenReturn( new String[0] );

    executor.getData().setInputRowMeta( inputRowMeta );
    Mockito.when(  executor.getData().getInputRowMeta().getFieldNames() ).thenReturn( new String[]{"childParam", "paramOverwrite"} );

    Trans internalTrans = executor.createInternalTrans();
    executor.getData().setExecutorTrans( internalTrans );
    executor.passParametersToTrans( Arrays.asList( new String[]{ fieldValue1, fieldValue2 } ) );

    //When the child parameter does exist in the parent parameters, overwrite the child parameter by the parent parameter.
    Assert.assertEquals( fieldValue2, internalTrans.getVariable( paramOverwrite ) );

    //All other parent parameters need to get copied into the child parameters  (when the 'Inherit all variables from the transformation?' option is checked)
    Assert.assertEquals( fieldValue1, internalTrans.getVariable( childParam ) );
  }


  @Test
  //PDI-16066
  public void testExecuteTransWithInputsAndNoFields() throws KettleException {

    String childParam = "childParam";
    String childValue = "childValue";
    String inputValue1 = "inputValue1";
    String inputValue2 = "inputValue2";
    String paramOverwrite = "paramOverwrite";
    String parentValue = "parentValue";

    meta.getParameters().setVariable( new String[]{ childParam, paramOverwrite } );
    meta.getParameters().setInput( new String[]{ inputValue1, inputValue2 } );
    meta.getParameters().setField( new String[]{ null, null } );
    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );

    executor.setVariable( paramOverwrite, parentValue );
    executor.setVariable( childParam, childValue );

    Mockito.when( executor.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    parent.setLog( new LogChannel( this ) );
    Mockito.doCallRealMethod().when( executor ).createInternalTrans( );
    Mockito.when(  executor.getData().getExecutorTransMeta().listVariables() ).thenReturn( new String[0] );
    Mockito.when(  executor.getData().getExecutorTransMeta().listParameters() ).thenReturn( new String[0] /*{parentParam}*/ );

    Trans internalTrans = executor.createInternalTrans();
    executor.getData().setExecutorTrans( internalTrans );
    executor.passParametersToTrans( Arrays.asList( meta.getParameters().getField() ) );

    //When the child parameter does exist in the parent parameters, overwrite the child parameter by the parent parameter.
    Assert.assertEquals( inputValue2, internalTrans.getVariable( paramOverwrite ) );

    //All other parent parameters need to get copied into the child parameters  (when the 'Inherit all variables from the transformation?' option is checked)
    Assert.assertEquals( inputValue1, internalTrans.getVariable( childParam ) );
  }


  @Test
  public void testSafeStop() throws Exception {
    TransExecutorData transExecutorDataMock = mock( TransExecutorData.class );
    TransMeta transMetaMock = mock( TransMeta.class );
    when( executor.getData() ).thenReturn( transExecutorDataMock );
    when( transMetaMock.listVariables() ).thenReturn( new String[0] );
    when( transMetaMock.listParameters() ).thenReturn( new String[0] );
    when( transExecutorDataMock.getExecutorTransMeta() ).thenReturn( transMetaMock );
    prepareOneRowForExecutor();
    meta.setGroupSize( "1" );
    data.groupSize = 1;

    internalResult.setSafeStop( true );

    Trans parent = Mockito.spy( new Trans() );

    Mockito.when( executor.getTrans() ).thenReturn( parent );
    doNothing().when( executor ).initializeVariablesFromParent( any() );
    doNothing().when( executor ).passParametersToTrans( any() );

    executor.init( meta, data );
    executor.setInputRowMeta( new RowMeta() );
    assertTrue( executor.processRow( meta, data ) );
    verify( executor.getTrans() ).safeStop();
    verify( executor.getTrans(), never() ).stopAll();
  }

  @Test
  public void testAbortWithError() throws Exception {
    prepareOneRowForExecutor();
    meta.setGroupSize( "1" );
    data.groupSize = 1;

    internalResult.setSafeStop( false );
    internalResult.setNrErrors( 1 );

    Trans parent = Mockito.spy( new Trans() );
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    executor.init( meta, data );
    executor.setInputRowMeta( new RowMeta() );
    assertTrue( executor.processRow( meta, data ) );
    verify( executor.getTrans(), never() ).safeStop();
    verify( executor.getTrans(), never() ).stopAll();
  }

  private void prepareNoRowForExecutor() throws Exception {
    doReturn( null ).when( executor ).getRow();
  }

  @Test
  public void testGetLastIncomingFieldValuesWithEmptyData() throws Exception {
    prepareNoRowForExecutor();

    executor.init( meta, data );
    executor.processRow( meta, data );
    verify( executor, times( 0 ) ).getLastIncomingFieldValues();
  }

  @Test
  public void testGetLastIncomingFieldValuesWithData() throws KettleException {
    prepareMultipleRowsForExecutor();

    Trans parent = new Trans();
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    meta.setGroupField( "groupField" );
    executor.init( meta, data );

    RowMetaInterface rowMeta = new RowMeta();
    rowMeta.addValueMeta( new ValueMetaString( "groupField" ) );
    executor.setInputRowMeta( rowMeta );

    // start processing
    executor.processRow( meta, data ); // 1st row - 'value1'
    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row - still 'value1'

    // same group, zero calls
    verify( executor, times( 0 ) ).getLastIncomingFieldValues();

    executor.processRow( meta, data ); // 5th row - value has been changed - 'value12'

    // group changed - 1 calls = 1 for trans execution
    verify( executor, times( 1 ) ).getLastIncomingFieldValues();

    executor.processRow( meta, data ); // 6th row - 'value12'
    executor.processRow( meta, data ); // 7th row - 'value12'
    executor.processRow( meta, data ); // end of file

    //  No more rows = + 1 call to get the previous value
    verify( executor, times( 2 ) ).getLastIncomingFieldValues();

  }

  @Test
  //PDI-18191
  public void testInitializeVariablesFromTransInheritingAllVariables() throws KettleException {
    String childValue = "childValue";
    String parentValue = "parentValue";
    String variableName = "v_name";

    Trans parent = new Trans();
    parent.setVariable( variableName, parentValue );
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    meta.getParameters().setInheritingAllVariables( true );

    executor.init( meta, data );

    Trans internalTrans = executor.createInternalTrans();
    internalTrans.setVariable( variableName, childValue );
    executor.getData().setExecutorTrans( internalTrans );

    executor.initializeVariablesFromParent( internalTrans );

    // if InheritingAllVariables then transExecutor can initialize variables from parent trans
    Assert.assertEquals( parentValue, internalTrans.getVariable( variableName ) );

  }

  @Test
  //PDI-18191
  public void testInitializeVariablesFromTransNotInheritingAllVariables() throws KettleException {
    String childValue = "childValue";
    String parentValue = "parentValue";
    String variableName = "v_name";

    Trans parent = new Trans();
    parent.setVariable( "v_name", parentValue );
    Mockito.when( executor.getTrans() ).thenReturn( parent );

    meta.getParameters().setInheritingAllVariables( false );

    executor.init( meta, data );

    Trans internalTrans = executor.createInternalTrans();
    internalTrans.setVariable( variableName, childValue );
    executor.getData().setExecutorTrans( internalTrans );

    executor.initializeVariablesFromParent( internalTrans );

    // if not InheritingAllVariables then transExecutor can NOT initialize variables from parent trans
    Assert.assertEquals( childValue, internalTrans.getVariable( variableName ) );

  }



}
