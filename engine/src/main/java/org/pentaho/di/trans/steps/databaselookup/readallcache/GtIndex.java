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

import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.BitSet;

/**
 * @author Andrey Khayrutdinov
 */
class GtIndex extends Index {

  static Index lessOrEqualCache( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    return new GtIndex( column, valueMeta, rowsAmount, true );
  }


  private final boolean isMatchingLessOrEqual;

  GtIndex( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    this( column, valueMeta, rowsAmount, false );
  }

  GtIndex( int column, ValueMetaInterface valueMeta, int rowsAmount, boolean isMatchingLessOrEqual ) {
    super( column, valueMeta, rowsAmount );
    this.isMatchingLessOrEqual = isMatchingLessOrEqual;
  }

  @Override
  void doApply( SearchingContext context, ValueMetaInterface lookupMeta, Object lookupValue ) {
    int firstValue = findInsertionPointOf( new IndexedValue( lookupValue, Integer.MAX_VALUE ) );
    final int length = values.length;
    if ( firstValue == length ) {
      // everything is less than lookupValue
      if ( isMatchingLessOrEqual ) {
        // then do nothing
        return;
      }
      context.setEmpty();
    } else {
      BitSet bitSet = context.getWorkingSet();

      int start, end;
      if ( firstValue < length / 2 ) {
        start = 0;
        end = firstValue;
      } else {
        start = firstValue;
        end = length;
      }

      for ( int i = start; i < end; i++ ) {
        bitSet.set( values[ i ].row, true );
      }

      context.intersect( bitSet, ( start == 0 ) ^ isMatchingLessOrEqual );
    }
  }

  @Override
  int getRestrictionPower() {
    return -1000;
  }
}
