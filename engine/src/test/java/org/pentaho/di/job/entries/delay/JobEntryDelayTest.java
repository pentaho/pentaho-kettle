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


package org.pentaho.di.job.entries.delay;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pentaho.di.core.util.Utils;

public class JobEntryDelayTest {

  @Test
  public void testGetRealMaximumTimeout() {
    JobEntryDelay entry = new JobEntryDelay();
    assertTrue( Utils.isEmpty( entry.getRealMaximumTimeout() ) );

    entry.setMaximumTimeout( " 1" );
    assertEquals( "1", entry.getRealMaximumTimeout() );

    entry.setVariable( "testValue", " 20" );
    entry.setMaximumTimeout( "${testValue}" );
    assertEquals( "20", entry.getRealMaximumTimeout() );
  }
}
