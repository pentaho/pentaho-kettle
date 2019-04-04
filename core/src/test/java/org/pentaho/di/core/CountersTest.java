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

package org.pentaho.di.core;

import junit.framework.TestCase;

/**
 * Test class for counters functionality.
 *
 * @author Sven Boden
 */
public class CountersTest extends TestCase {
  /**
   * Test about all. Class is not too big.
   */
  public void testGeneralFunctionality() {
    Counters cntrs = Counters.getInstance();
    assertNull( cntrs.getCounter( "counter1" ) );
    cntrs.setCounter( "counter1", new Counter() );
    assertNotNull( cntrs.getCounter( "counter1" ) );

    // Clear 1 counter
    cntrs.clearCounter( "counter1" );
    assertNull( cntrs.getCounter( "counter1" ) );

    // Clear all
    cntrs.setCounter( "counter1", new Counter() );
    cntrs.setCounter( "counter2", new Counter() );
    assertNotNull( cntrs.getCounter( "counter1" ) );
    assertNotNull( cntrs.getCounter( "counter2" ) );
    cntrs.clear();
    assertNull( cntrs.getCounter( "counter1" ) );
    assertNull( cntrs.getCounter( "counter2" ) );

    // Same object is returned
    Counters cntrsCopy = Counters.getInstance();
    assertTrue( cntrsCopy == cntrs );
  }

}
