/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Pentaho : http://www.pentaho.com
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
package org.pentaho.di.ui.spoon.delegates;


import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SpoonJobDelegateTest {
  private SpoonJobDelegate delegate;
  private Spoon spoon;
  private JobLogTable jobLogTable;
  private JobMeta jobMeta;
  private ArrayList<JobMeta> jobMap;

  @Before
  public void before() {
    jobMap = new ArrayList<JobMeta>();

    jobMeta = mock( JobMeta.class );
    delegate = mock( SpoonJobDelegate.class );
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );

    doReturn( jobMap ).when( delegate ).getJobList();
    doReturn( spoon ).when( delegate ).getSpoon();
    jobLogTable = mock( JobLogTable.class );
  }

  @Test
  public void testAddAndCloseTransformation() {
    doCallRealMethod().when( delegate ).closeJob( any() );
    doCallRealMethod().when( delegate ).addJob( any() );
    assertTrue( delegate.addJob( jobMeta ) );
    assertFalse( delegate.addJob( jobMeta ) );
    delegate.closeJob( jobMeta );
    assertTrue( delegate.addJob( jobMeta ) );
  }
}
