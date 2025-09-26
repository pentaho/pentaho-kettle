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

package org.pentaho.di.job.entries.trans;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.trans.TransMeta;
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
import static org.pentaho.di.job.entries.trans.JobEntryTransHelper.JOB_ENTRY_TRANS_REFERENCE_PATH;
import static org.pentaho.di.job.entries.trans.JobEntryTransHelper.PARAMETERS;
import static org.pentaho.di.job.entry.BaseJobEntryHelper.IS_VALID_REFERENCE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class JobEntryTransHelperTest {
  private static final String TEST_PARAM_1 = "param1";
  private static final String TEST_PARAM_2 = "param2";
  JobEntryTransHelper jobEntryTransHelper;
  TransMeta transMeta;
  JobMeta jobMeta;
  JobEntryTrans jobEntryTrans;

  @Before
  public void setUp() {
    transMeta = mock( TransMeta.class );
    jobMeta = mock( JobMeta.class );
    jobEntryTrans = mock( JobEntryTrans.class );
    jobEntryTransHelper = spy( new JobEntryTransHelper ( jobEntryTrans ) );
  }

  @Test
  public void testParametersAction_ThrowsException() throws KettleException {
    doThrow( new KettleException() ).when( jobEntryTrans ).getTransMeta( null, null, null );
    doReturn( new String[] { TEST_PARAM_1, TEST_PARAM_2 } ).when( transMeta ).listParameters();

    JSONObject response = jobEntryTransHelper.handleJobEntryAction( PARAMETERS, jobMeta, new HashMap<>() );
    assertEquals( StepInterface.FAILURE_RESPONSE, response.get( StepInterface.ACTION_STATUS ) );
  }

  @Test
  public void testParametersAction_ReturnsParametersData() throws KettleException {
    doReturn( transMeta ).when( jobEntryTrans ).getTransMeta( null, null, null );
    doReturn( new String[] { TEST_PARAM_1, TEST_PARAM_2 } ).when( transMeta ).listParameters();

    JSONObject response = jobEntryTransHelper.handleJobEntryAction( PARAMETERS, jobMeta, new HashMap<>() );
    JSONArray parameters = (JSONArray) response.get( PARAMETERS );

    assertNotNull( parameters );
    assertEquals( TEST_PARAM_1, parameters.get( 0 ) );
    assertEquals( TEST_PARAM_2, parameters.get( 1 ) );
  }

  @Test
  public void testReferencePath() throws KettleException {
    doReturn( transMeta ).when( jobEntryTrans ).getTransMeta( null, null, jobMeta );
    when( jobEntryTrans.getDirectory() ).thenReturn( "/path" );
    when( jobEntryTrans.getTransname() ).thenReturn( "transName" );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    JSONObject response = jobEntryTransHelper.jobEntryAction( JOB_ENTRY_TRANS_REFERENCE_PATH, jobMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( JOB_ENTRY_TRANS_REFERENCE_PATH ) );
    assertEquals( "/path/transName", response.get( JOB_ENTRY_TRANS_REFERENCE_PATH ) );
    assertEquals( true, response.get( IS_VALID_REFERENCE ) );
  }

  @Test
  public void testReferencePath_throwsException() throws KettleException {
    doThrow( new KettleException( "Invalid transformation" ) ).when( jobEntryTrans ).getTransMeta( null, null, jobMeta );
    when( jobEntryTrans.getDirectory() ).thenReturn( "/path" );
    when( jobEntryTrans.getTransname() ).thenReturn( "transName" );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( invocation -> invocation.getArgument( 0 ) );
    JSONObject response = jobEntryTransHelper.jobEntryAction( JOB_ENTRY_TRANS_REFERENCE_PATH, jobMeta, null );

    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response );
    assertNotNull( response.get( JOB_ENTRY_TRANS_REFERENCE_PATH ) );
    assertEquals( "/path/transName", response.get( JOB_ENTRY_TRANS_REFERENCE_PATH ) );
    assertEquals( false, response.get( IS_VALID_REFERENCE ) );
  }

  @Test
  public void testHandleStepAction_whenMethodNameIsInvalid() {
    JSONObject response = jobEntryTransHelper.handleJobEntryAction( "invalidMethod", jobMeta, null );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
