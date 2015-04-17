/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.job.JobMeta;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class JobTrackerTest {

  @Test
  // PDI-11389 Number of job trackers should be limited by KETTLE_MAX_JOB_TRACKER_SIZE
  public void testAddJobTracker() throws Exception {
    Integer maxTestSize = 30;
    System.setProperty( Const.KETTLE_MAX_JOB_TRACKER_SIZE, maxTestSize.toString() );

    JobMeta jobMeta = mock( JobMeta.class );
    JobTracker jobTracker = new JobTracker( jobMeta );

    for ( int n = 0; n < maxTestSize * 2; n++ ) {
      jobTracker.addJobTracker( mock( JobTracker.class ) );
    }

    assertTrue( "More JobTrackers than allowed were added", jobTracker.getTotalNumberOfItems() <= maxTestSize );
  }
}
