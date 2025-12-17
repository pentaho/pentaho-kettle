/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2025 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 *
 ******************************************************************************/


package org.pentaho.di.trans.steps.jobexecutor;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.pentaho.di.core.ObjectLocationSpecificationMethod;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogChannelInterfaceFactory;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.pentaho.di.trans.step.BaseStepHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.trans.step.BaseStepHelper.REFERENCE_PATH;
import static org.pentaho.di.trans.step.StepHelperInterface.ACTION_STATUS;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.trans.step.StepHelperInterface.SUCCESS_RESPONSE;
import static org.pentaho.di.trans.steps.jobexecutor.JobExecutorHelper.IS_JOB_VALID;
import static org.pentaho.di.trans.steps.jobexecutor.JobExecutorHelper.JOB_PRESENT;
import static org.pentaho.di.trans.steps.jobexecutor.JobExecutorHelper.PARAMETERS;

public class JobExecutorHelperTest {

  JobExecutorMeta jobExecutorMeta;
  TransMeta transMeta;
  JobMeta jobMeta;
  JobExecutorHelper jobExecutorHelper;
  MockedStatic<KettleLogStore> kettleLogStoreMock;

  @Before
  public void setUp() throws Exception {
    transMeta = mock( TransMeta.class );
    jobMeta = mock( JobMeta.class );
    jobExecutorMeta = mock( JobExecutorMeta.class );
    jobExecutorHelper = new JobExecutorHelper( jobExecutorMeta );

    kettleLogStoreMock = mockStatic( KettleLogStore.class );
    LoggingBuffer loggingBuffer = mock( LoggingBuffer.class );
    LogChannelInterfaceFactory logChannelInterfaceFactory = mock( LogChannelInterfaceFactory.class );
    LogChannelInterface logChannelInterface = mock( LogChannelInterface.class );
    KettleLogStore.setLogChannelInterfaceFactory( logChannelInterfaceFactory );
    when( logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        logChannelInterface );
    when( logChannelInterfaceFactory.create( any() ) ).thenReturn( logChannelInterface );

    kettleLogStoreMock.when( KettleLogStore::getLogChannelInterfaceFactory ).thenReturn( logChannelInterfaceFactory );
    kettleLogStoreMock.when( KettleLogStore::getAppender ).thenReturn( loggingBuffer );

    JobExecutorParameters parameters = mock( JobExecutorParameters.class );
    when( jobMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( jobMeta.listParameters() ).thenReturn( new String[] { "param1", "param2" } );
    when( jobMeta.getParameterDescription( "param1" ) ).thenReturn( "desc1" );
    when( jobMeta.getParameterDescription( "param2" ) ).thenReturn( "desc2" );
    when( jobMeta.getParameterDefault( "param1" ) ).thenReturn( "default1" );
    when( jobMeta.getParameterDefault( "param2" ) ).thenReturn( "default2" );
    when( jobExecutorMeta.getParameters() ).thenReturn( parameters );
  }

  @After
  public void tearDown() {
    if ( kettleLogStoreMock != null ) {
      kettleLogStoreMock.close();
    }
  }

  @Test
  public void testGetParametersFromJob_returnsParameters() throws KettleException {
    try ( MockedStatic<JobExecutorMeta> ignored = mockStatic( JobExecutorMeta.class ) ) {
      when( JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenReturn( jobMeta );
      JSONObject response = jobExecutorHelper.stepAction( PARAMETERS, transMeta, null );

      JSONArray parameters = (JSONArray) response.get( PARAMETERS );
      assertNotNull( response );
      assertTrue( response.containsKey( PARAMETERS ) );
      assertTrue( response.get( PARAMETERS ) instanceof JSONArray );
      assertEquals( 2, parameters.size() );
      JSONObject param1 = (JSONObject) parameters.get( 0 );
      assertEquals( "param1", param1.get( "variable" ) );
      assertEquals( "", param1.get( "field" ) );
      assertEquals( "default1", param1.get( "input" ) );
    }
  }

  @Test
  public void testGetParametersFromJob_throwsException() throws KettleException {
    when( jobMeta.getParameterDescription( anyString() ) ).thenThrow( new UnknownParamException() );
    JSONObject response = jobExecutorHelper.stepAction( PARAMETERS, transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testIsJobValid_withValidJob() throws KettleException {
    try ( MockedStatic<JobExecutorMeta> ignored = mockStatic( JobExecutorMeta.class ) ) {
      when( JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenReturn( jobMeta );
      JSONObject response = jobExecutorHelper.stepAction( IS_JOB_VALID, transMeta, null );

      assertNotNull( response );
      assertTrue( response.containsKey( JOB_PRESENT ) );
      assertTrue( response.get( JOB_PRESENT ) instanceof Boolean );
    }
  }

  @Test
  public void testIsJobValidAction_ForInvalidJob() throws KettleException {
    when( jobMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    when( transMeta.getBowl() ).thenReturn( DefaultBowl.getInstance() );
    try ( MockedStatic<JobExecutorMeta> ignored = mockStatic( JobExecutorMeta.class ) ) {

      when( JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenThrow( new KettleException( "File not found" ) );

      JSONObject response = jobExecutorHelper.stepAction( IS_JOB_VALID, transMeta, null );
      String transPresent = response.get( JOB_PRESENT ).toString();
      assertEquals( "false", transPresent );
    }
  }

  @Test
  public void testReferencePath() {
    try ( MockedStatic<JobExecutorMeta> jobExecutorMetaMockedStatic = mockStatic( JobExecutorMeta.class ) ) {
      jobExecutorMetaMockedStatic.when( () ->  JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenReturn( jobMeta );
      when( jobExecutorMeta.getDirectoryPath() ).thenReturn( "/path" );
      when( jobExecutorMeta.getJobName() ).thenReturn( "jobName" );
      when( jobExecutorMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
      JSONObject response = jobExecutorHelper.stepAction( REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( REFERENCE_PATH ) );
      assertEquals( "/path/jobName", response.get( REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_withFileNameSpecification() {
    try ( MockedStatic<JobExecutorMeta> jobExecutorMetaMockedStatic = mockStatic( JobExecutorMeta.class ) ) {
      jobExecutorMetaMockedStatic.when( () ->  JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenReturn( jobMeta );
      when( jobExecutorMeta.getFileName() ).thenReturn( "/path/jobName" );
      when( jobExecutorMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.FILENAME );
      when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
      JSONObject response = jobExecutorHelper.stepAction( REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( REFERENCE_PATH ) );
      assertEquals( "/path/jobName", response.get( REFERENCE_PATH ) );
      assertEquals( true, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testReferencePath_throwsException() {
    try ( MockedStatic<JobExecutorMeta> jobExecutorMetaMockedStatic = mockStatic( JobExecutorMeta.class ) ) {
      jobExecutorMetaMockedStatic.when( () ->  JobExecutorMeta.loadJobMeta( transMeta.getBowl(), jobExecutorMeta, jobExecutorMeta.getRepository(), transMeta ) )
          .thenThrow( new KettleException( "invalid_job" ) );
      when( jobExecutorMeta.getDirectoryPath() ).thenReturn( "/path" );
      when( jobExecutorMeta.getJobName() ).thenReturn( "jobName" );
      when( jobExecutorMeta.getSpecificationMethod() ).thenReturn( ObjectLocationSpecificationMethod.REPOSITORY_BY_NAME );
      when( transMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
      JSONObject response = jobExecutorHelper.stepAction( REFERENCE_PATH, transMeta, null );

      assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
      assertNotNull( response );
      assertNotNull( response.get( REFERENCE_PATH ) );
      assertEquals( "/path/jobName", response.get( REFERENCE_PATH ) );
      assertEquals( false, response.get( IS_VALID_REFERENCE ) );
    }
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = jobExecutorHelper.stepAction( "invalidMethod", transMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
