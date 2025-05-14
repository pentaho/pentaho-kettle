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

package org.pentaho.di.job.entries.checkdbconnection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;

import static org.junit.Assert.assertTrue;

public class JobEntryCheckDbConnectionsIT {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  /**
   * Test whether a Millisecond-level timeout actually waits for N milliseconds, instead of N seconds
   */
  @Test( timeout = 10000 )
  public void testMillisecondWait() {
    int waitMilliseconds = 15;
    Job mockedJob = Mockito.mock( Job.class );
    Mockito.when( mockedJob.isStopped() ).thenReturn( false );

    JobEntryCheckDbConnections meta = new JobEntryCheckDbConnections();
    meta.setParentJob( mockedJob );
    meta.setLogLevel( LogLevel.BASIC );

    DatabaseMeta db = new DatabaseMeta( "testPreparedStatements", "H2", "JDBC", null, "mem:test", null, "SA", "" );
    meta.setConnections( new DatabaseMeta[] { db } );
    meta.setWaittimes( new int[] { JobEntryCheckDbConnections.UNIT_TIME_MILLI_SECOND } );
    meta.setWaitfors( new String[] { String.valueOf( waitMilliseconds ) } );
    Result result = meta.execute( new Result(), 0 );
    assertTrue( result.getResult() );
  }

  @Test( timeout = 5000 )
  public void testWaitingtime() {
    int waitTimes = 3;
    Job mockedJob = Mockito.mock( Job.class );
    Mockito.when( mockedJob.isStopped() ).thenReturn( false );

    JobEntryCheckDbConnections meta = new JobEntryCheckDbConnections();
    meta.setParentJob( mockedJob );
    meta.setLogLevel( LogLevel.DETAILED );

    DatabaseMeta db = new DatabaseMeta( "testPreparedStatements", "H2", "JDBC", null, "mem:test", null, "SA", "" );
    meta.setConnections( new DatabaseMeta[] { db } );
    meta.setWaittimes( new int[] { JobEntryCheckDbConnections.UNIT_TIME_SECOND } );
    meta.setWaitfors( new String[] { String.valueOf( waitTimes ) } );

    Result result = meta.execute( new Result(), 0 );

    assertTrue( meta.getNow() - meta.getTimeStart() >= waitTimes * 1000 );
    assertTrue( result.getResult() );
  }
}
