/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.trans.steps.databaselookup.readallcache;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.BitSet;

/**
 * @author Andrey Khayrutdinov
 */
class EqIndex extends Index {

  static Index nonEqualityIndex( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    return new EqIndex( column, valueMeta, rowsAmount, true );
  }


  private final boolean isMatchingNonEquality;

  EqIndex( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    this( column, valueMeta, rowsAmount, false );
  }

  private EqIndex( int column, ValueMetaInterface valueMeta, int rowsAmount, boolean isMatchingNonEquality ) {
    super( column, valueMeta, rowsAmount );
    this.isMatchingNonEquality = isMatchingNonEquality;
  }

  @Override
  void doApply( SearchingContext context,
                ValueMetaInterface lookupMeta, Object lookupValue ) throws KettleException {
    int firstValue = findInsertionPointOf( new IndexedValue( lookupValue, -1 ) );
    final int length = values.length;
    if ( firstValue == length || valueMeta.compare( values[ firstValue ].key, lookupValue ) != 0 ) {
      // nothing was found
      if ( isMatchingNonEquality ) {
        // everything is acceptable, just do nothing
        return;
      }
      context.setEmpty();
    } else {
      BitSet bitSet = context.getWorkingSet();
      bitSet.set( values[ firstValue ].row, true );
      int lastValue = firstValue + 1;
      while ( lastValue != length && valueMeta.compare( values[ lastValue ].key, lookupValue ) == 0 ) {
        bitSet.set( values[ lastValue ].row, true );
        lastValue++;
      }

      context.intersect( bitSet, isMatchingNonEquality );
    }
  }

  @Override
  int getRestrictionPower() {
    // "==" is a good restriction, whereas "!=" is quite weak
    return isMatchingNonEquality ? Short.MAX_VALUE : Short.MIN_VALUE;
  }
}
