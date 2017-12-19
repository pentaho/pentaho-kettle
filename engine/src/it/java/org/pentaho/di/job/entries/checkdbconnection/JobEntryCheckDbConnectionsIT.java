/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.job.entries.checkdbconnection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.job.Job;

public class JobEntryCheckDbConnectionsIT {

  private static final String H2_DATABASE = "myDb";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  @After
  public void cleanup() {
    try {
      FileObject dbFile = KettleVFS.getFileObject( H2_DATABASE + ".h2.db" );
      if ( dbFile.exists() ) {
        System.out.println( "deleting file" );
        dbFile.delete();
      }
    } catch ( KettleFileException | FileSystemException ignored ) {
      // Ignore, we tried cleaning up
    }
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

    DatabaseMeta db = new DatabaseMeta( "InMemory H2", "H2", null, null, H2_DATABASE, "-1", null, null );
    meta.setConnections( new DatabaseMeta[]{ db } );
    meta.setWaittimes( new int[]{ JobEntryCheckDbConnections.UNIT_TIME_MILLI_SECOND } );
    meta.setWaitfors( new String[]{ String.valueOf( waitMilliseconds ) } );
    Result result = meta.execute( new Result(), 0 );
    Assert.assertTrue( result.getResult() );
  }

  @Test( timeout = 5000 )
  public void testWaitingtime() {
    int waitTimes = 3;
    Job mockedJob = Mockito.mock( Job.class );
    Mockito.when( mockedJob.isStopped() ).thenReturn( false );

    JobEntryCheckDbConnections meta = new JobEntryCheckDbConnections();
    meta.setParentJob( mockedJob );
    meta.setLogLevel( LogLevel.DETAILED );

    DatabaseMeta db = new DatabaseMeta( "InMemory H2", "H2", null, null, H2_DATABASE, "-1", null, null );
    meta.setConnections( new DatabaseMeta[]{ db } );
    meta.setWaittimes( new int[]{ JobEntryCheckDbConnections.UNIT_TIME_SECOND } );
    meta.setWaitfors( new String[]{ String.valueOf( waitTimes ) } );

    Result result = meta.execute( new Result(), 0 );

    Assert.assertTrue( meta.getNow() - meta.getTimeStart() >= waitTimes * 1000 );
    Assert.assertTrue( result.getResult() );
  }
}
