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

package org.pentaho.di.job.entries.zipfile;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.job.JobMeta;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.ACTION_STATUS;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_METHOD_NOT_FOUND_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.FAILURE_RESPONSE;
import static org.pentaho.di.job.entry.JobEntryHelperInterface.SUCCESS_RESPONSE;

public class JobEntryZipFileHelperTest {

  private JobEntryZipFileHelper helper;
  private JobMeta jobMeta;
  private JobEntryZipFile jobEntryZipFile;

  @Before
  public void setUp() {
    jobMeta = mock( JobMeta.class );
    jobEntryZipFile = mock( JobEntryZipFile.class );
    helper = new JobEntryZipFileHelper( jobEntryZipFile );
  }

  @Test
  public void testShowFileNameAction_Success() {
    Map<String, String> params = new HashMap<>();
    params.put( "zipfilename", "test" );
    params.put( "adddate", "Y" );
    params.put( "addtime", "Y" );
    params.put( "SpecifyFormat", "N" );
    params.put( "date_time_format", "" );

    when( jobMeta.environmentSubstitute( "test" ) ).thenReturn( "test" );
    when( jobEntryZipFile.getFullFilename( "test", true, true, false, "" ) ).thenReturn( "test_20251201_153045.zip" );

    JSONObject response = helper.showFileNameAction( jobMeta, params );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
    JSONArray files = (JSONArray) response.get( "files" );
    assertEquals( 1, files.size() );
    assertEquals( "test_20251201_153045.zip", files.get( 0 ) );
  }

  @Test
  public void testShowFileNameAction_EmptyFilename() {
    Map<String, String> params = new HashMap<>();
    params.put( "zipfilename", "" );

    when( jobMeta.environmentSubstitute( "" ) ).thenReturn( "" );

    JSONObject response = helper.showFileNameAction( jobMeta, params );

    assertNotNull( response );
    assertNotNull( response.get( "message" ) );
    JSONArray files = (JSONArray) response.get( "files" );
    assertEquals( 0, files.size() );
  }

  @Test
  public void testHandleJobEntryAction_ValidMethod() {
    Map<String, String> params = new HashMap<>();
    params.put( "zipfilename", "test" );
    params.put( "adddate", "N" );
    params.put( "addtime", "N" );
    params.put( "SpecifyFormat", "N" );
    params.put( "date_time_format", "" );

    when( jobMeta.environmentSubstitute( "test" ) ).thenReturn( "test" );
    when( jobEntryZipFile.getFullFilename( "test", false, false, false, "" ) ).thenReturn( "test.zip" );

    JSONObject response = helper.handleJobEntryAction( "showFileName", jobMeta, params );

    assertNotNull( response );
    assertEquals( SUCCESS_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleJobEntryAction_InvalidMethod() {
    JSONObject response = helper.handleJobEntryAction( "invalid", jobMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleJobEntryAction_Exception() {
    Map<String, String> params = new HashMap<>();
    params.put( "zipfilename", "test" );

    when( jobMeta.environmentSubstitute( anyString() ) ).thenThrow( new RuntimeException( "error" ) );

    JSONObject response = helper.handleJobEntryAction( "showFileName", jobMeta, params );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }
}
