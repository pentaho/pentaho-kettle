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

package org.pentaho.di.job.entries.columnsexist;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for column exist job entry.
 *
 * @author Tim Ryzhov
 * @since 21-03-2017
 *
 */

public class JobEntryColumnsExistTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String TABLENAME = "TABLE";
  private static final String SCHEMANAME = "SCHEMA";
  private static final String[] COLUMNS = new String[]{"COLUMN1", "COLUMN2"};
  private JobEntryColumnsExist jobEntry;
  private Database db;


  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @AfterClass
  public static void tearDown() {
    KettleEnvironment.reset();
  }

  @Before
  public void setUp() {
    Job parentJob = new Job( null, new JobMeta() );
    jobEntry = spy( new JobEntryColumnsExist( "" ) );
    parentJob.getJobMeta().addJobEntry( new JobEntryCopy( jobEntry ) );
    parentJob.setStopped( false );
    jobEntry.setParentJob( parentJob );
    parentJob.setLogLevel( LogLevel.NOTHING );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    jobEntry.setDatabase( dbMeta );
    db = spy( new Database( jobEntry, dbMeta ) );
    jobEntry.setParentJob( parentJob );
    jobEntry.setTablename( TABLENAME );
    jobEntry.setArguments( COLUMNS );
    jobEntry.setSchemaname( SCHEMANAME );
  }

  @Test
  public void jobFail_tableNameIsEmpty() throws KettleException {
    jobEntry.setTablename( null );
    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be error", 1, result.getNrErrors() );
    assertFalse( "Result should be false", result.getResult() );
  }

  @Test
  public void jobFail_columnsArrayIsEmpty() throws KettleException {
    jobEntry.setArguments( null );
    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be error", 1, result.getNrErrors() );
    assertFalse( "Result should be false", result.getResult() );
  }

  @Test
  public void jobFail_connectionIsNull() throws KettleException {
    jobEntry.setDatabase( null );
    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be error", 1, result.getNrErrors() );
    assertFalse( "Result should be false", result.getResult() );
  }

  @Test
  public void jobFail_tableNotExist() throws KettleException {
    when( jobEntry.getNewDatabaseFromMeta() ).thenReturn( db );
    doNothing().when( db ).connect( anyString(), any() );
    doReturn( false ).when( db ).checkTableExists( anyString(), anyString() );

    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be error", 1, result.getNrErrors() );
    assertFalse( "Result should be false", result.getResult() );
    verify( db, atLeastOnce() ).disconnect();
  }

  @Test
  public void jobFail_columnNotExist() throws KettleException {
    doReturn( db ).when( jobEntry ).getNewDatabaseFromMeta();
    doNothing().when( db ).connect( anyString(), anyString() );
    doReturn( true ).when( db ).checkTableExists( anyString(), anyString() );
    doReturn( false ).when( db ).checkColumnExists( anyString(), anyString(), anyString() );
    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be some errors", 1, result.getNrErrors() );
    assertFalse( "Result should be false", result.getResult() );
    verify( db, atLeastOnce() ).disconnect();
  }

  @Test
  public void jobSuccess() throws KettleException {
    doReturn( db ).when( jobEntry ).getNewDatabaseFromMeta();
    doNothing().when( db ).connect( any(), any() );
    doReturn( true ).when( db ).checkColumnExists( anyString(), anyString(), anyString() );
    doReturn( true ).when( db ).checkTableExists( anyString(), anyString() );
    final Result result = jobEntry.execute( new Result(), 0 );
    assertEquals( "Should be no error", 0, result.getNrErrors() );
    assertTrue( "Result should be true", result.getResult() );
    assertEquals( "Lines written", COLUMNS.length, result.getNrLinesWritten() );
    verify( db, atLeastOnce() ).disconnect();
  }
}
