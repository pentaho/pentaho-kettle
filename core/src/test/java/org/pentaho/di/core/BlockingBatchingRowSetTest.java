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

import java.util.ArrayList;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for the basic functionality of the blocking & batching row set.
 *
 * @author Matt Casters
 */
public class BlockingBatchingRowSetTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  public RowMetaInterface createRowMetaInterface() {
    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta = { new ValueMetaInteger( "ROWNR" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      rm.addValueMeta( valuesMeta[i] );
    }

    return rm;
  }

  /**
   * The basic stuff.
   */
  @Test
  public void testBasicCreation() {
    RowSet set = new BlockingBatchingRowSet( 10 );

    assertTrue( !set.isDone() );
    assertEquals( 0, set.size() );
  }

  /**
   * Functionality test.
   */
  @Test
  public void testFuntionality1() {
    BlockingBatchingRowSet set = new BlockingBatchingRowSet( 10 );

    RowMetaInterface rm = createRowMetaInterface();

    List<Object[]> rows = new ArrayList<Object[]>();
    for ( int i = 0; i < 5; i++ ) {
      rows.add( new Object[] { new Long( i ), } );
    }

    assertEquals( 0, set.size() );

    // Pop off row. This should return null (no row available: has a timeout)
    //
    Object[] r = set.getRow();
    assertNull( r );

    // Add rows. set doesn't report rows, batches them
    // this batching row set has 2 buffers with 2 rows, the 5th row will cause the rows to be exposed.
    //
    int index = 0;
    while ( index < 4 ) {
      set.putRow( rm, rows.get( index++ ) );
      assertEquals( 0, set.size() );
    }
    set.putRow( rm, rows.get( index++ ) );
    assertEquals( 5, set.size() );

    // Signal done...
    //
    set.setDone();
    assertTrue( set.isDone() );

    // Get a row back...
    //
    r = set.getRow();
    assertNotNull( r );
    assertArrayEquals( rows.get( 0 ), r );

    // Get a row back...
    //
    r = set.getRow();
    assertNotNull( r );
    assertArrayEquals( rows.get( 1 ), r );

    // Get a row back...
    //
    r = set.getRow();
    assertNotNull( r );
    assertArrayEquals( rows.get( 2 ), r );
  }
}
