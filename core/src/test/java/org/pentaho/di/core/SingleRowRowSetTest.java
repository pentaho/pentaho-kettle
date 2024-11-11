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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class SingleRowRowSetTest {
  Object[] row;
  SingleRowRowSet rowSet;

  @Before
  public void setup() {
    rowSet = new SingleRowRowSet();
    row = new Object[]{};
  }

  @Test
  public void testPutRow() throws Exception {
    rowSet.putRow( new RowMeta(), row );
    assertSame( row, rowSet.getRow() );
  }

  @Test
  public void testPutRowWait() throws Exception {
    rowSet.putRowWait( new RowMeta(), row, 1, TimeUnit.SECONDS );
    assertSame( row, rowSet.getRowWait( 1, TimeUnit.SECONDS ) );
  }

  @Test
  public void testGetRowImmediate() throws Exception {
    rowSet.putRow( new RowMeta(), row );
    assertSame( row, rowSet.getRowImmediate() );
  }

  @Test
  public void testSize() throws Exception {
    assertEquals( 0, rowSet.size() );
    rowSet.putRow( new RowMeta(), row );
    assertEquals( 1, rowSet.size() );
    rowSet.clear();
    assertEquals( 0, rowSet.size() );
  }
}
