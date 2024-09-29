/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core;

import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;

import static org.junit.Assert.*;

public class BlockingListeningRowSetTest {
  @Test
  public void testClass() {
    BlockingListeningRowSet rowSet = new BlockingListeningRowSet( 1 );
    assertEquals( 0, rowSet.size() );
    final Object[] row = new Object[]{};
    final RowMeta meta = new RowMeta();
    rowSet.putRow( meta, row );
    assertSame( meta, rowSet.getRowMeta() );
    assertEquals( 1, rowSet.size() );
    assertFalse( rowSet.isBlocking() );
    assertSame( row, rowSet.getRow() );
    assertEquals( 0, rowSet.size() );
    rowSet.putRow( meta, row );
    assertSame( row, rowSet.getRowImmediate() );
    rowSet.putRow( meta, row );
    assertEquals( 1, rowSet.size() );
    rowSet.clear();
    assertEquals( 0, rowSet.size() );
  }
}
