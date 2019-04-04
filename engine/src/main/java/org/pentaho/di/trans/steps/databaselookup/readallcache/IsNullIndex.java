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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.BitSet;
import java.util.Comparator;

/**
 * @author Andrey Khayrutdinov
 */
class IsNullIndex extends Index implements Comparator<Index.IndexedValue> {

  private final boolean isMatchingNull;

  IsNullIndex( int column, ValueMetaInterface valueMeta, int rowsAmount, boolean isMatchingNull ) {
    super( column, valueMeta, rowsAmount );
    this.isMatchingNull = isMatchingNull;
  }

  @Override
  Comparator<IndexedValue> createComparator() {
    return this;
  }

  @Override
  void doApply( SearchingContext context, ValueMetaInterface lookupMeta, Object lookupValue ) throws KettleException {
    int artificialRow = isMatchingNull ? Integer.MAX_VALUE : -1;

    int afterLastValue = findInsertionPointOf( new IndexedValue( null, artificialRow ) );
    if ( afterLastValue == 0 ) {
      // no matching values
      context.setEmpty();
    } else {
      int length = values.length;

      int start, end;
      if ( afterLastValue < length / 2 ) {
        start = 0;
        end = afterLastValue;
      } else {
        start = afterLastValue;
        end = length;
      }

      BitSet bitSet = context.getWorkingSet();
      for ( int i = start; i < end; i++ ) {
        bitSet.set( values[ i ].row, true );
      }

      context.intersect( bitSet, ( start != 0 ) );
    }
  }


  @Override
  int getRestrictionPower() {
    return isMatchingNull ? Byte.MIN_VALUE : Byte.MAX_VALUE;
  }


  @Override
  public int compare( IndexedValue o1, IndexedValue o2 ) {
    // to unify doApply() routing for both cases, the order depends on isMatchingNull:
    //   isMatchingNull == true  --> nulls are first
    //   isMatchingNull == false --> nulls are last
    // regardless the flag's value, rows' order is kept
    try {
      boolean null1 = valueMeta.isNull( o1.key );
      boolean null2 = valueMeta.isNull( o2.key );

      int c;
      if ( null1 ) {
        if ( null2 ) {
          c = 0;
        } else {
          c = -1;
        }
      } else {
        if ( null2 ) {
          c = 1;
        } else {
          c = 0;
        }
      }
      if ( c == 0 ) {
        return Integer.compare( o1.row, o2.row );
      } else {
        return isMatchingNull ? c : -c;
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }
}
