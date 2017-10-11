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

package org.pentaho.di.core.gui;

import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.job.JobEntryResult;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobTrackerTest {

  @Test
  // PDI-11389 Number of job trackers should be limited by KETTLE_MAX_JOB_TRACKER_SIZE
  public void testAddJobTracker() throws Exception {
    final String old = System.getProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE );

    final Integer maxTestSize = 30;
    try {
      System.setProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE, maxTestSize.toString() );

      JobMeta jobMeta = mock( JobMeta.class );
      JobTracker jobTracker = new JobTracker( jobMeta );

      for ( int n = 0; n < maxTestSize * 2; n++ ) {
        jobTracker.addJobTracker( mock( JobTracker.class ) );
      }

      assertTrue( "More JobTrackers than allowed were added", jobTracker.getTotalNumberOfItems() <= maxTestSize );
    } finally {
      if ( old == null ) {
        System.clearProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE );
      } else {
        System.setProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE, old );
      }
    }
  }


  @Test
  public void findJobTracker_EntryNameIsNull() {
    JobTracker jobTracker = createTracker();
    jobTracker.addJobTracker( createTracker() );

    JobEntryCopy copy = createEntryCopy( null );

    assertNull( jobTracker.findJobTracker( copy ) );
  }

  @Test
  public void findJobTracker_EntryNameNotFound() {
    JobTracker jobTracker = createTracker();
    for ( int i = 0; i < 3; i++ ) {
      jobTracker.addJobTracker( createTracker( Integer.toString( i ), 1 ) );
    }

    JobEntryCopy copy = createEntryCopy( "not match" );

    assertNull( jobTracker.findJobTracker( copy ) );
  }

  @Test
  public void findJobTracker_EntryNameFound() {
    JobTracker jobTracker = createTracker();
    JobTracker[] children = new JobTracker[] {
      createTracker( "0", 1 ),
      createTracker( "1", 1 ),
      createTracker( "2", 1 )
    };
    for ( JobTracker child : children ) {
      jobTracker.addJobTracker( child );
    }

    JobEntryCopy copy = createEntryCopy( "1" );

    assertEquals( children[1], jobTracker.findJobTracker( copy ) );
  }


  private static JobTracker createTracker() {
    return createTracker( null, -1 );
  }

  private static JobTracker createTracker( String jobEntryName, int jobEntryNr ) {
    JobMeta jobMeta = mock( JobMeta.class );
    JobTracker jobTracker = new JobTracker( jobMeta );
    if ( jobEntryName != null ) {
      JobEntryResult result = mock( JobEntryResult.class );
      when( result.getJobEntryName() ).thenReturn( jobEntryName );
      when( result.getJobEntryNr() ).thenReturn( jobEntryNr );
      jobTracker.setJobEntryResult( result );
    }
    return jobTracker;
  }

  private static JobEntryCopy createEntryCopy( String entryName ) {
    JobEntryInterface entry = mock( JobEntryInterface.class );
    when( entry.getName() ).thenReturn( entryName );

    JobEntryCopy copy = new JobEntryCopy( entry );
    copy.setNr( 1 );
    return copy;
  }

}
