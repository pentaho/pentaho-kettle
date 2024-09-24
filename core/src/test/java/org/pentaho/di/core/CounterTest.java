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
 * Test class for counter functionality.
 *
 * @author Sven Boden
 */
public class CounterTest extends TestCase {
  /**
   * Constructor test 1.
   */
  public void testConstructor1() {
    Counter cnt1 = new Counter();
    assertEquals( 1L, cnt1.getCounter() );
    assertEquals( 1L, cnt1.getIncrement() );
    assertEquals( 0L, cnt1.getMaximum() );
    assertEquals( 1L, cnt1.getStart() );
    assertFalse( cnt1.isLoop() );

    Counter cnt2 = new Counter( 2L );
    assertEquals( 2L, cnt2.getCounter() );
    assertEquals( 1L, cnt2.getIncrement() );
    assertEquals( 0L, cnt2.getMaximum() );
    assertEquals( 2L, cnt2.getStart() );
    assertFalse( cnt2.isLoop() );

    Counter cnt3 = new Counter( 3L, 2L );
    assertEquals( 3L, cnt3.getCounter() );
    assertEquals( 2L, cnt3.getIncrement() );
    assertEquals( 0L, cnt3.getMaximum() );
    assertEquals( 3L, cnt3.getStart() );
    assertFalse( cnt3.isLoop() );

    Counter cnt4 = new Counter( 5L, 2L, 20L );
    assertEquals( 5L, cnt4.getCounter() );
    assertEquals( 2L, cnt4.getIncrement() );
    assertEquals( 20L, cnt4.getMaximum() );
    assertEquals( 5L, cnt4.getStart() );
    assertTrue( cnt4.isLoop() );
  }

  /**
   * Test the setting of stuff.
   */
  public void testSets() {
    Counter cnt1 = new Counter();
    cnt1.setCounter( 5L );
    assertEquals( 5L, cnt1.getCounter() );

    cnt1.setIncrement( 2L );
    assertEquals( 2L, cnt1.getIncrement() );

    cnt1.setLoop( true );
    assertTrue( cnt1.isLoop() );

    cnt1.setMaximum( 100L );
    assertEquals( 100L, cnt1.getMaximum() );
  }

  /**
   * Test next().
   */
  public void testNext() {
    Counter cnt1 = new Counter();
    cnt1.setCounter( 2L );
    assertEquals( 2L, cnt1.next() );
    assertEquals( 3L, cnt1.next() );
    assertEquals( 4L, cnt1.next() );
    assertEquals( 5L, cnt1.next() );
    assertEquals( 6L, cnt1.next() );
    assertEquals( 7L, cnt1.next() );
    assertEquals( 8L, cnt1.next() );
    assertEquals( 9L, cnt1.next() );
    assertEquals( 10L, cnt1.next() );

    Counter cnt2 = new Counter();
    cnt2.setCounter( 1L );
    cnt2.setIncrement( 3L );
    cnt2.setMaximum( 10L );
    assertEquals( 1L, cnt2.next() );
    assertEquals( 4L, cnt2.next() );
    assertEquals( 7L, cnt2.next() );
    assertEquals( 10L, cnt2.next() );
    assertEquals( 13L, cnt2.next() );

    Counter cnt3 = new Counter();
    cnt3.setCounter( 1L );
    cnt3.setIncrement( 3L );
    cnt3.setMaximum( 11L );
    cnt3.setLoop( true );
    assertEquals( 1L, cnt3.next() );
    assertEquals( 4L, cnt3.next() );
    assertEquals( 7L, cnt3.next() );
    assertEquals( 10L, cnt3.next() );
    assertEquals( 1L, cnt3.next() );
    assertEquals( 4L, cnt3.next() );
    assertEquals( 7L, cnt3.next() );
    assertEquals( 10L, cnt3.next() );
    assertEquals( 1L, cnt3.next() );

    cnt3.setCounter( 10L );
    assertEquals( 10L, cnt3.next() );
  }
}
