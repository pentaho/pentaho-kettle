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
class LtIndex extends Index {

  static Index greaterOrEqualCache( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    return new LtIndex( column, valueMeta, rowsAmount, true );
  }


  private final boolean isMatchingGreaterOrEqual;

  LtIndex( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    this( column, valueMeta, rowsAmount, false );
  }

  private LtIndex( int column, ValueMetaInterface valueMeta, int rowsAmount, boolean isMatchingGreaterOrEqual ) {
    super( column, valueMeta, rowsAmount );
    this.isMatchingGreaterOrEqual = isMatchingGreaterOrEqual;
  }

  @Override
  void doApply( SearchingContext context, ValueMetaInterface lookupMeta, Object lookupValue ) {
    int firstValue = findInsertionPointOf( new IndexedValue( lookupValue, -1 ) );
    if ( firstValue == 0 ) {
      // everything is greater than lookupValue
      if ( isMatchingGreaterOrEqual ) {
        // then do nothing
        return;

      }
      context.setEmpty();
    } else {
      BitSet bitSet = context.getWorkingSet();

      int start, end;
      if ( firstValue < values.length / 2 ) {
        start = 0;
        end = firstValue;
      } else {
        start = firstValue;
        end = values.length;
      }

      for ( int i = start; i < end; i++ ) {
        bitSet.set( values[ i ].row, true );
      }

      context.intersect( bitSet, ( start != 0 ) ^ isMatchingGreaterOrEqual );
    }
  }

  @Override
  int getRestrictionPower() {
    return -1000;
  }
}
