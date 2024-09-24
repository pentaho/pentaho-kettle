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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.Comparator;

import java.util.Arrays;

/**
 * @author Andrey Khayrutdinov
 */
abstract class Index {

  final int column;
  final ValueMetaInterface valueMeta;
  final IndexedValue[] values;

  Index( int column, ValueMetaInterface valueMeta, int rowsAmount ) {
    this.column = column;
    this.valueMeta = valueMeta;
    this.values = new IndexedValue[ rowsAmount ];
  }

  Comparator<IndexedValue> createComparator() {
    return new IndexValueComparator( valueMeta );
  }

  void performIndexingOf( Object[][] rows ) {
    for ( int i = 0, len = rows.length; i < len; i++ ) {
      values[ i ] = new IndexedValue( rows[ i ][ column ], i );
    }
    // sort values using meta to compare and row number as seconds dimension
    Arrays.sort( values, createComparator() );
  }

  /**
   * Performs binary search algorithm looking for {@code value} in the sorted array and returns decoded index of
   * insertion (decoded index returned by Arrays.binarySearch())
   * @param value value to look for
   * @return decoded index
   */
  int findInsertionPointOf( IndexedValue value ) {
    int index = Arrays.binarySearch( values, value, createComparator() );
    // index == ( -insertion_point - 1)
    return -( index + 1 );
  }


  public int getColumn() {
    return column;
  }

  public void applyRestrictionsTo( SearchingContext context, ValueMetaInterface lookupMeta, Object lookupValue ) {
    try {
      doApply( context, lookupMeta, lookupValue );
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  abstract void doApply( SearchingContext context, ValueMetaInterface lookupMeta, Object lookupValue )
    throws KettleException;

  /**
   * Return the "anti-strength" of the restriction of the index. It is a heuristic weight of the restriction, needed
   * to push more "powerful" filters before less "powerful" to cut as much values as possible at the beginning.
   *
   * @return integer number, the less it is, the more "powerful" restriction is presumed to be
   */
  abstract int getRestrictionPower();

  static Comparator<Index> restrictionComparator() {
    return new Comparator<Index>() {
      @Override
      public int compare( Index o1, Index o2 ) {
        return Integer.compare( o1.getRestrictionPower(), o2.getRestrictionPower() );
      }
    };
  }


  static class IndexedValue {
    final Object key;
    final int row;

    public IndexedValue( Object key, int row ) {
      this.key = key;
      this.row = row;
    }
  }

  static class IndexValueComparator implements Comparator<IndexedValue> {

    private final ValueMetaInterface meta;

    public IndexValueComparator( ValueMetaInterface meta ) {
      this.meta = meta;
    }

    @Override
    public int compare( IndexedValue o1, IndexedValue o2 ) {
      // does not expect nulls here!
      int c;
      try {
        c = meta.compare( o1.key, o2.key );
      } catch ( KettleValueException e ) {
        throw new RuntimeException( e );
      }

      return ( c == 0 ) ? Integer.compare( o1.row, o2.row ) : c;
    }
  }
}
