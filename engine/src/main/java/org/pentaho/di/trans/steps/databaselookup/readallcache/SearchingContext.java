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

import java.util.BitSet;

/**
 * @author Andrey Khayrutdinov
 */
class SearchingContext {
  private BitSet candidates;
  private boolean noResult;

  private BitSet working;

  public SearchingContext() {
    candidates = null;
    noResult = false;
  }

  boolean isEmpty() {
    return noResult;
  }

  void setEmpty() {
    noResult = true;
    // make the previous object eligible for GC
    candidates = new BitSet( 0 );
    working = null;
  }

  BitSet getCandidates() {
    return candidates;
  }

  void init( int amount ) {
    candidates = new BitSet( amount );
    candidates.set( 0, amount, true );

    working = new BitSet( amount );
  }

  BitSet getWorkingSet() {
    working.clear();
    return working;
  }

  void intersect( BitSet set, boolean inverse ) {
    if ( inverse ) {
      candidates.andNot( set );
    } else {
      candidates.and( set );
    }
    checkEmpty();
  }

  private void checkEmpty() {
    if ( candidates.nextSetBit( 0 ) == -1 ) {
      setEmpty();
    }
  }
}
