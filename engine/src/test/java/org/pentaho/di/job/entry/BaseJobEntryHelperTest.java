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

package org.pentaho.di.job.entry;

import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.JobMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class BaseJobEntryHelperTest {

  private BaseJobEntryHelper helper;
  private LogChannelInterface log;
  private static final String METHOD_NAME = "testMethod";

  @Before
  public void setUp() {
    helper = new BaseJobEntryHelper() {
      @Override
      protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
        JSONObject response = new JSONObject();
        if ( "testMethod".equals( method ) ) {
          response.put( ACTION_STATUS, SUCCESS_RESPONSE );
        } else {
          response.put( ACTION_STATUS, FAILURE_RESPONSE );
        }
        return response;
      }
    };

    log = mock( LogChannelInterface.class );
    helper.log = log;
  }

  @Test
  public void testJobEntryAction_Success() {
    JobMeta jobMeta = mock( JobMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.jobEntryAction( METHOD_NAME, jobMeta, queryParams );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testJobEntryAction_Failure() {
    JobMeta jobMeta = mock(JobMeta.class);
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = helper.jobEntryAction( "invalidMethod", jobMeta, queryParams );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testJobEntryAction_ExceptionHandling() {
    BaseJobEntryHelper exceptionHelper = new BaseJobEntryHelper() {
      @Override
      protected JSONObject handleJobEntryAction( String method, JobMeta jobMeta, Map<String, String> queryParams ) {
        throw new RuntimeException( "Test exception" );
      }
    };

    exceptionHelper.log = log;

    JobMeta jobMeta = mock( JobMeta.class );
    Map<String, String> queryParams = new HashMap<>();
    JSONObject response = exceptionHelper.jobEntryAction( METHOD_NAME, jobMeta, queryParams );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    verify( log ).logError(contains( "Error executing step action" ), anyString() );
  }

  @Test
  public void testIsFailedResponse_NullResponse() {
    assertTrue( helper.isFailedResponse( null ) );
  }

  @Test
  public void testIsFailedResponse_NoStatus() {
    JSONObject response = new JSONObject();
    assertFalse( helper.isFailedResponse( response ) );
  }

  @Test
  public void testIsFailedResponse_FailureStatus() {
    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, FAILURE_RESPONSE );
    assertTrue( helper.isFailedResponse( response ) );
  }

  @Test
  public void testIsFailedResponse_SuccessStatus() {
    JSONObject response = new JSONObject();
    response.put( ACTION_STATUS, SUCCESS_RESPONSE );
    assertFalse( helper.isFailedResponse( response ) );
  }
}
