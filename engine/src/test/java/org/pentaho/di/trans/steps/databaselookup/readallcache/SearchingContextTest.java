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


package org.pentaho.di.trans.steps.databaselookup.readallcache;

import org.junit.Test;

import java.util.BitSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class SearchingContextTest {

  @Test
  public void returnsClearWorkingSet() {
    SearchingContext ctx = new SearchingContext();
    ctx.init( 4 );
    ctx.getWorkingSet().set( 1 );
    assertEquals( "Should return cleared object", -1, ctx.getWorkingSet().nextSetBit( 0 ) );
  }

  @Test
  public void intersectionDetectsBecomingEmpty() {

    SearchingContext ctx = new SearchingContext();
    ctx.init( 4 );

    BitSet set = ctx.getWorkingSet();
    set.set( 1 );
    set.set( 2 );
    ctx.intersect( set, false );
    assertFalse( ctx.isEmpty() );

    set = ctx.getWorkingSet();
    ctx.intersect( set, false );
    assertTrue( ctx.isEmpty() );
  }
}
