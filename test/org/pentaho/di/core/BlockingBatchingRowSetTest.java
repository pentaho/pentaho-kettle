/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * Test class for the basic functionality of the blocking & batching row set.
 * 
 * @author Matt Casters
 */
public class BlockingBatchingRowSetTest extends TestCase {
  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface valuesMeta[] = { new ValueMeta("ROWNR", ValueMeta.TYPE_INTEGER), };

    for (int i = 0; i < valuesMeta.length; i++) {
      rm.addValueMeta(valuesMeta[i]);
    }

    return rm;
  }

  /**
   * The basic stuff.
   */
  public void testBasicCreation() {
    RowSet set = new BlockingBatchingRowSet(10);

    assertTrue(!set.isDone());
    assertEquals(0, set.size());
  }

  /**
   * Functionality test.
   */
  public void testFuntionality1() {
    BlockingBatchingRowSet set = new BlockingBatchingRowSet(10);

    RowMetaInterface rm = createRowMetaInterface();

    Object[] r1 = new Object[] { new Long(1L) };
    Object[] r2 = new Object[] { new Long(2L) };
    Object[] r3 = new Object[] { new Long(3L) };

    assertEquals(0, set.size());

    // Add first row. State 1
    //
    set.putRow(rm, r1);
    assertEquals(1, set.size());

    // Add another row. State: 1 2
    //
    set.putRow(rm, r2);
    assertEquals(2, set.size());

    // Pop off row.  This should return null (no row available: has a timeout)
    //
    Object[] r = set.getRow();
    assertNull(r);
    
    // Add another row. State: 2 3
    //
    set.putRow(rm, r3);
    assertEquals(3, set.size());

    // Signal done...
    //
    set.setDone();
    assertTrue(set.isDone());
    
    // Get a row back...
    //
    r = set.getRow();
    assertNotNull(r);
    assertEquals(2, set.size());
    assertEquals(r[0], r1[0]);

    // Get a row back...
    //
    r = set.getRow();
    assertNotNull(r);
    assertEquals(1, set.size());
    assertEquals(r[0], r2[0]);

    // Get a row back...
    //
    r = set.getRow();
    assertNotNull(r);
    assertEquals(0, set.size());
    assertEquals(r[0], r3[0]);
 }
}