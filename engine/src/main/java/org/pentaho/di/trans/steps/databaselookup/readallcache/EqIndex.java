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
