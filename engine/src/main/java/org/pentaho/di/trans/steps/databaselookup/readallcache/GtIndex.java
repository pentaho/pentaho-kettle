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
