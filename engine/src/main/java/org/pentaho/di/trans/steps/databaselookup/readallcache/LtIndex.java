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
