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
