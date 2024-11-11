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


package org.pentaho.di.job.entries.waitforsql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.job.Job;

public class JobEntryWaitForSQLIT {

  DatabaseMeta mockDbMeta;
  Job parentJob;
  LogChannelInterface parentJobLogChannel;

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @Before
  public void setup() {
    mockDbMeta = mock( DatabaseMeta.class );
    parentJob = spy( new Job() );
    when( parentJob.isStopped() ).thenReturn( false );
  }

  @Test
  public void testGoodResult() throws KettleException {
    JobEntryWaitForSQL entry = spy( new JobEntryWaitForSQL() );
    doReturn( true ).when( entry ).SQLDataOK( any( Result.class ), anyLong(), anyString(), anyString(), anyString() );
    doNothing().when( entry ).checkConnection();

    entry.setDatabase( mockDbMeta );
    entry.successCondition = JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_GREATER;
    entry.rowsCountValue = "0";
    entry.setMaximumTimeout( "1" ); // Seconds
    entry.setCheckCycleTime( "1" ); // Seconds
    entry.tablename = UUID.randomUUID().toString();
    entry.setParentJob( parentJob );

    Result result = entry.execute( new Result(), 0 );
    assertNotNull( result );
    assertTrue( result.getResult() );
    assertEquals( 0, result.getNrErrors() );
  }

  @Test
  public void testBadResult() throws KettleException {
    JobEntryWaitForSQL entry = spy( new JobEntryWaitForSQL() );
    doReturn( false ).when( entry ).SQLDataOK( any( Result.class ), anyLong(), anyString(), anyString(), anyString() );
    doNothing().when( entry ).checkConnection();

    entry.setDatabase( mockDbMeta );
    entry.successCondition = JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_GREATER;
    entry.rowsCountValue = "0";
    entry.setMaximumTimeout( "1" ); // Seconds
    entry.setCheckCycleTime( "1" ); // Seconds
    entry.tablename = UUID.randomUUID().toString();
    entry.setParentJob( parentJob );

    Result result = entry.execute( new Result(), 0 );
    assertNotNull( result );
    assertFalse( result.getResult() );
    assertEquals( 1, result.getNrErrors() );
  }

  @Test
  public void testSuccessOnTimeout() throws KettleException {
    JobEntryWaitForSQL entry = spy( new JobEntryWaitForSQL() );
    doReturn( false ).when( entry ).SQLDataOK( any( Result.class ), anyLong(), anyString(), anyString(), anyString() );
    doNothing().when( entry ).checkConnection();

    entry.setDatabase( mockDbMeta );
    entry.successCondition = JobEntryWaitForSQL.SUCCESS_CONDITION_ROWS_COUNT_GREATER;
    entry.rowsCountValue = "0";
    entry.setMaximumTimeout( "1" ); // Seconds
    entry.setCheckCycleTime( "1" ); // Seconds
    entry.tablename = UUID.randomUUID().toString();
    entry.setSuccessOnTimeout( true );
    entry.setParentJob( parentJob );

    Result result = entry.execute( new Result(), 0 );
    assertNotNull( result );
    assertTrue( result.getResult() );
    assertEquals( 0, result.getNrErrors() );
  }
}
