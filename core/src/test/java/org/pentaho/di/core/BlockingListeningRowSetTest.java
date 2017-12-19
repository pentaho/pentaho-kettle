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
