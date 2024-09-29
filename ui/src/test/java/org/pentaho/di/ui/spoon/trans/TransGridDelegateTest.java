/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
