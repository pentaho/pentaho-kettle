/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.Assert;
import org.pentaho.di.job.Job;

public class JobEntryCheckDbConnectionsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }


  /**
   * Test whether a Millisecond-level timeout actually waits for N milliseconds, instead of N seconds
   */
  @Test(timeout=5000)
  public void testMillisecondWait() {
    int waitMilliseconds = 15;
    Job mockedJob = Mockito.mock( Job.class );
    Mockito.when( mockedJob.isStopped() ).thenReturn( false );

    JobEntryCheckDbConnections meta = new JobEntryCheckDbConnections();
    meta.setParentJob( mockedJob );
    meta.setLogLevel( LogLevel.BASIC );

    DatabaseMeta db = new DatabaseMeta( "InMemory H2", "H2", null, null, "myDb", "-1", null, null );
    meta.connections = new DatabaseMeta[]{ db };
    meta.waittimes = new int[]{ JobEntryCheckDbConnections.UNIT_TIME_MILLI_SECOND };
    meta.waitfors = new String[]{ String.valueOf( waitMilliseconds ) };
    Result result = meta.execute( new Result(), 0 );
    Assert.assertTrue( result.getResult() );
  }
}
