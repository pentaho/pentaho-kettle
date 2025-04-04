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


package org.pentaho.di.trans.steps.jobexecutor;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.StepMockUtil;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author Mikhail_Chen-Len-Son
 */
public class JobExecutorTest {

  private JobExecutor executor;
  private JobExecutorMeta meta;
  private JobExecutorData data;

  @Before
  public void setUp() throws Exception {
    executor = StepMockUtil.getStep( JobExecutor.class, JobExecutorMeta.class, "TransExecutorUnitTest" );
    executor = spy( executor );
    executor.setInputRowMeta( mock( RowMetaInterface.class ) );

    doNothing().when( executor ).discardLogLines( any( JobExecutorData.class ) );

    meta = new JobExecutorMeta();
    data = new JobExecutorData();
    Job job = mock( Job.class );
    doReturn( job ).when( executor ).createJob( nullable( Repository.class ), nullable( JobMeta.class ),
      any( LoggingObjectInterface.class ) );
    doReturn( ArrayUtils.EMPTY_STRING_ARRAY ).when( job ).listParameters();

    data.groupBuffer = new ArrayList<>();
    data.groupSize = -1;
    data.groupTime = -1;
    data.groupField = null;
  }

  @After
  public void tearDown() {
    executor = null;
    meta = null;
    data = null;
  }

  /**
   * Given an input data and a job executor with specified field to group rows on.
   * <br/>
   * When job executor is processing rows of an input data,
   * then rows should be accumulated in a group as long as the specified field value stays the same.
   */
  @Test
  public void shouldAccumulateRowsWhenGroupFieldIsSpecified() throws KettleException {
    prepareMultipleRowsForExecutor();

    data.groupField = "groupField";
    executor.init( meta, data );

    when( executor.getExecutorJob() ).thenReturn( mock( Job.class ) );
    when( executor.getExecutorJob().getJobMeta() ).thenReturn( mock( JobMeta.class ) );

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
   * Given an input data and a job executor
   * with specified number of rows to send to the transformation (X).
   * <br/>
   * When job executor is processing rows of an input data,
   * then every X rows should be accumulated in a group.
   */
  @Test
  public void shouldAccumulateRowsByCount() throws KettleException {
    prepareMultipleRowsForExecutor();

    data.groupSize = 5;
    executor.init( meta, data );

    when( executor.getExecutorJob() ).thenReturn( mock( Job.class ) );
    when( executor.getExecutorJob().getJobMeta() ).thenReturn( mock( JobMeta.class ) );

    // start processing
    executor.processRow( meta, data ); // 1st row
    // should be added to group buffer
    assertEquals( 1, data.groupBuffer.size() );

    executor.processRow( meta, data );
    executor.processRow( meta, data );
    executor.processRow( meta, data ); // 4th row
    // first 4 rows should be added to the same group
    assertEquals( 4, data.groupBuffer.size() );

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

  @Test
  public void testParametersAction() throws KettleException {
    Trans trans = mock( Trans.class );
    Repository repositoryMock = mock( Repository.class );
    StepMetaInterface stepMetaInterfaceMock = mock( StepMetaInterface.class );
    TransMeta transMeta = mock( TransMeta.class );
    JobMeta jobMeta = mock( JobMeta.class );
    when( executor.getStepMetaInterface() ).thenReturn( stepMetaInterfaceMock );
    when( trans.getRepository() ).thenReturn( repositoryMock );
    when( jobMeta.listParameters() ).thenReturn( new String[] { "param1", "param2" } );
    when( jobMeta.getParameterDescription( "param1" ) ).thenReturn( "desc1" );
    when( jobMeta.getParameterDescription( "param2" ) ).thenReturn( "desc2" );
    when( jobMeta.getParameterDefault( "param1" ) ).thenReturn( "default1" );
    when( jobMeta.getParameterDefault( "param2" ) ).thenReturn( "default2" );
    executor.init( meta, data );

    try ( MockedStatic<JobExecutorMeta> mocked = mockStatic( JobExecutorMeta.class ) ) {
      when( JobExecutorMeta.loadJobMeta( meta, meta.getRepository(), executor ) ).thenReturn( jobMeta );
      JSONObject response = executor.doAction( "parameters", meta, transMeta, trans, new HashMap<>() );
      JSONArray parameters = (JSONArray) response.get( "parameters" );

      assertEquals( 2, parameters.size() );
      JSONObject param1 = (JSONObject) parameters.get( 0 );
      assertEquals( "param1", param1.get( "variable" ) );
      assertEquals( "", param1.get( "field" ) );
      assertEquals( "default1", param1.get( "input" ) );
      JSONObject param2 = (JSONObject) parameters.get( 1 );
      assertEquals( "param2", param2.get( "variable" ) );
      assertEquals( "", param2.get( "field" ) );
      assertEquals( "default2", param2.get( "input" ) );
    }
  }

  @Test
  public void testDoAction_ThrowException() {
    Trans trans = mock( Trans.class );
    TransMeta transMeta = mock( TransMeta.class );
    JSONObject response = executor.doAction( "invalidMethod", meta, transMeta, trans, new HashMap<>() );

    assertEquals( StepInterface.FAILURE_METHOD_NOT_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testParametersAction_ThrowException() throws KettleException {
    Trans trans = mock( Trans.class );
    TransMeta transMeta = mock( TransMeta.class );
    JobMeta inputTransMeta = mock( JobMeta.class );
    when( inputTransMeta.getParameterDescription( anyString() ) ).thenThrow( new UnknownParamException( "Error" ) );
    JSONObject response = executor.doAction( "parameters", meta, transMeta, trans, new HashMap<>() );

    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

}
