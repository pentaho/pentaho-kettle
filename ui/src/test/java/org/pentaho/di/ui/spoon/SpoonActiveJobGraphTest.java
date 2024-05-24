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

package org.pentaho.di.ui.spoon;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.ui.spoon.delegates.SpoonDelegates;
import org.pentaho.di.ui.spoon.delegates.SpoonTabsDelegate;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.xul.swt.tab.TabSet;
import static org.mockito.ArgumentMatchers.any;

public class SpoonActiveJobGraphTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );
    spoon.delegates = mock( SpoonDelegates.class );
    spoon.delegates.tabs = mock( SpoonTabsDelegate.class );
    spoon.tabfolder = mock( TabSet.class );

    doCallRealMethod().when( spoon ).getActiveJobGraph();
  }

  @Test
  public void returnNullActiveJobGraphIfJobTabNotExists() {
    JobGraph actualJobGraph = spoon.getActiveJobGraph();
    assertNull( actualJobGraph );
  }

  @Test
  public void returnActiveJobGraphIfJobTabExists() {
    TabMapEntry tabMapEntry = mock( TabMapEntry.class );
    JobGraph jobGraph = mock( JobGraph.class );
    Mockito.when( tabMapEntry.getObject() ).thenReturn( jobGraph );
    Mockito.when( spoon.delegates.tabs.getTab( any() ) ).thenReturn( tabMapEntry );

    JobGraph actualJobGraph = spoon.getActiveJobGraph();
    assertNotNull( actualJobGraph );
  }

}
