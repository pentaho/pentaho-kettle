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

package org.pentaho.di.ui.spoon.trans;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TransGridDelegateTest {

  @Test
  public void testSubStepCompare() {

    //Equal
    assertEquals( 0, TransGridDelegate.subStepCompare( "1", "1" ) );

    //object1 less than object2
    assertTrue( TransGridDelegate.subStepCompare( "1", "2" ) < 0 );
    assertTrue( TransGridDelegate.subStepCompare( "1", "1.1" ) < 0 );
    assertTrue( TransGridDelegate.subStepCompare( "1.2", "1.3" ) < 0 );

    //object2 less than object1
    assertTrue( TransGridDelegate.subStepCompare( "2", "1" ) > 0 );
    assertTrue( TransGridDelegate.subStepCompare( "1.1", "1" ) > 0 );
    assertTrue( TransGridDelegate.subStepCompare( "1.2", "1.1" ) > 0 );
  }
}
