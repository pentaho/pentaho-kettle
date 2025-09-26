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

package org.pentaho.di.job.entries.job;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.step.StepInterface;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.pentaho.di.job.entries.job.JobEntryJobHelper.JOB_ENTRY_JOB_REFERENCE_PATH;
import static org.pentaho.di.job.entries.job.JobEntryJobHelper.PARAMETERS;
import static org.pentaho.di.job.entry.BaseJobEntryHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class JobEntryJobHelperTest {
  private static final String TEST_PARAM_1 = "param1";
  private static final String TEST_PARAM_2 = "param2";
  JobEntryJobHelper jobEntryJobHelper;
  JobMeta jobMeta;
  JobEntryJob jobEntryJob;

  @Before
  public void setUp() {
    jobMeta = mock( JobMeta.class );
    jobEntryJob = mock( JobEntryJob.class );
    jobEntryJobHelper = spy( new JobEntryJobHelper ( jobEntryJob ) );
  }

  @Test
  public void testParametersAction_ThrowsException() throws KettleException {
    doThrow( new KettleException() ).when( jobEntryJob ).getJobMeta( null, null, null );

    JSONObject response = jobEntryJobHelper.handleJobEntryAction( PARAMETERS, jobMeta, new HashMap<>() );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testParametersAction_ReturnsParametersData() throws KettleException {
    JobMeta inputJobMeta = mock( JobMeta.class );
    doReturn( new String[] { TEST_PARAM_1, TEST_PARAM_2 } ).when( inputJobMeta ).listParameters();
    doReturn( inputJobMeta ).when( jobEntryJob ).getJobMeta( null, null, null );

    JSONObject response = jobEntryJobHelper.handleJobEntryAction( PARAMETERS, jobMeta, new HashMap<>() );
    JSONArray parameters = (JSONArray) response.get( PARAMETERS );

    assertNotNull( parameters );
    assertEquals( TEST_PARAM_1, parameters.get( 0 ) );
    assertEquals( TEST_PARAM_2, parameters.get( 1 ) );
  }

  @Test
  public void testReferencePath() throws KettleException {
    doReturn( jobMeta ).when( jobEntryJob ).getJobMeta( null, null, jobMeta );
    when( jobEntryJob.getDirectory() ).thenReturn( "/path" );
    when( jobEntryJob.getJobName() ).thenReturn( "jobName" );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    JSONObject response = jobEntryJobHelper.jobEntryAction( JOB_ENTRY_JOB_REFERENCE_PATH, jobMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( JOB_ENTRY_JOB_REFERENCE_PATH ) );
    assertEquals( "/path/jobName", response.get( JOB_ENTRY_JOB_REFERENCE_PATH ) );
    assertEquals( true, response.get( IS_VALID_REFERENCE ) );
  }

  @Test
  public void testReferencePath_throwsException() throws KettleException {
    doThrow( new KettleException( "Invalid job" ) ).when( jobEntryJob ).getJobMeta( null, null, jobMeta );
    when( jobEntryJob.getDirectory() ).thenReturn( "/path" );
    when( jobEntryJob.getJobName() ).thenReturn( "jobName" );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    JSONObject response = jobEntryJobHelper.jobEntryAction( JOB_ENTRY_JOB_REFERENCE_PATH, jobMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( JOB_ENTRY_JOB_REFERENCE_PATH ) );
    assertEquals( "/path/jobName", response.get( JOB_ENTRY_JOB_REFERENCE_PATH ) );
    assertEquals( false, response.get( IS_VALID_REFERENCE ) );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = jobEntryJobHelper.handleJobEntryAction( "invalidMethod", jobMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
