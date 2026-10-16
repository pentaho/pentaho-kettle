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

package org.pentaho.di.job.entries.columnsexist;

import org.json.simple.JSONObject;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.row.RowMetaInterface;
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

public class JobEntryColumnsExistHelperTest {

  private JobEntryColumnsExistHelper helper;
  private JobMeta jobMeta;

  @BeforeClass
  public static void setUpClass() {
    KettleLogStore.init();
  }

  @Before
  public void setUp() {
    jobMeta = mock( JobMeta.class );
    helper = new JobEntryColumnsExistHelper();
  }

  // ---- handleJobEntryAction dispatch ----

  @Test
  public void testHandleJobEntryAction_ValidMethod() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( jobMeta.findDatabase( anyString() ) ).thenReturn( databaseMeta );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

    Map<String, String> params = new HashMap<>();
    params.put( "connection", "testConn" );
    params.put( "schemaname", "testSchema" );
    params.put( "tablename", "testTable" );

    // The method dispatches to getTableColumns; result will be FAILURE_RESPONSE due to DB connect
    // but it must NOT return FAILURE_METHOD_NOT_FOUND_RESPONSE
    JSONObject response = helper.handleJobEntryAction( "getTableColumns", jobMeta, params );

    assertNotNull( response );
    Object status = response.get( ACTION_STATUS );
    assertNotNull( status );
    // Must not be the "method not found" failure
    assertEquals( false, FAILURE_METHOD_NOT_FOUND_RESPONSE.equals( status ) );
  }

  @Test
  public void testHandleJobEntryAction_UnknownMethod() {
    JSONObject response = helper.handleJobEntryAction( "unknownMethod", jobMeta, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_METHOD_NOT_FOUND_RESPONSE, response.get( ACTION_STATUS ) );
  }

  @Test
  public void testHandleJobEntryAction_ExceptionDuringDispatch() {
    // Pass null jobMeta to trigger an exception inside the method
    JSONObject response = helper.handleJobEntryAction( "getTableColumns", null, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
  }

  // ---- getTableColumns ----

  @Test
  public void testGetTableColumns_NullJobMeta() {
    JSONObject response = helper.getTableColumns( null, new HashMap<>() );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "message" ) );
  }

  @Test
  public void testGetTableColumns_NoDatabaseConnection() {
    when( jobMeta.findDatabase( anyString() ) ).thenReturn( null );

    Map<String, String> params = new HashMap<>();
    params.put( "connection", "missingConn" );
    params.put( "schemaname", "" );
    params.put( "tablename", "employees" );

    JSONObject response = helper.getTableColumns( jobMeta, params );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "message" ) );
  }

  @Test
  public void testGetTableColumns_NullRow() throws Exception {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    Database mockDatabase = mock( Database.class );
    RowMetaInterface mockRow = null; // simulate getTableFieldsMeta returning null

    when( jobMeta.findDatabase( anyString() ) ).thenReturn( databaseMeta );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( inv -> inv.getArgument( 0 ) );
    when( mockDatabase.getTableFieldsMeta( anyString(), anyString() ) ).thenReturn( mockRow );

    // Use factory method override to inject the mock Database
    JobEntryColumnsExistHelper helperWithMockDb = new JobEntryColumnsExistHelper() {
      @Override
      protected Database createDatabase( JobMeta jm, DatabaseMeta dm ) {
        return mockDatabase;
      }
    };

    Map<String, String> params = new HashMap<>();
    params.put( "connection", "testConn" );
    params.put( "schemaname", "testSchema" );
    params.put( "tablename", "testTable" );

    JSONObject response = helperWithMockDb.getTableColumns( jobMeta, params );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "message" ) );
  }

  @Test
  public void testGetTableColumns_DatabaseConnectException() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    when( jobMeta.findDatabase( anyString() ) ).thenReturn( databaseMeta );
    when( jobMeta.environmentSubstitute( anyString() ) ).thenAnswer( inv -> inv.getArgument( 0 ) );

    // No actual DB — connect() will throw; we expect a FAILURE_RESPONSE
    Map<String, String> params = new HashMap<>();
    params.put( "connection", "testConn" );
    params.put( "schemaname", "" );
    params.put( "tablename", "employees" );

    JSONObject response = helper.getTableColumns( jobMeta, params );

    assertNotNull( response );
    assertEquals( FAILURE_RESPONSE, response.get( ACTION_STATUS ) );
    assertNotNull( response.get( "message" ) );
  }
}
