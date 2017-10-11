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

package org.pentaho.di.trans.steps.dimensionlookup;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

/**
 * This class will act as a special purpose dimension Cache. The idea here is to not only cache the last version of a
 * dimension entry, but all versions. So basically, the entry key is the natural key as well as the from-to date range.
 *
 * The way to achieve that result is to keep a sorted list in memory. Because we want as few conversion errors as
 * possible, we'll use the same row as we get from the database.
 *
 * @author matt
 *
 */
public class DimensionCache implements Comparator<Object[]> {
  private RowMetaInterface rowMeta;
  private List<Object[]> rowCache;
  private int[] keyIndexes;
  private int fromDateIndex;
  private int toDateIndex;

  /**
   * Create a new dimension cache object
   *
   * @param rowMeta
   *          the description of the rows to store
   * @param keyIndexes
   *          the indexes of the natural key (in that order)
   * @param fromDateIndex
   *          the field index where the start of the date range can be found
   * @param toDateIndex
   *          the field index where the end of the date range can be found
   */
  public DimensionCache( RowMetaInterface rowMeta, int[] keyIndexes, int fromDateIndex, int toDateIndex ) {
    this.rowMeta = rowMeta;
    this.keyIndexes = keyIndexes;
    this.fromDateIndex = fromDateIndex;
    this.toDateIndex = toDateIndex;
  }

  /**
   * Add a row to the back of the list
   *
   * @param row
   *          the row to add
   */
  public void addRow( Object[] row ) {
    rowCache.add( row );
  }

  /**
   * Get a row from the cache on a certain index
   *
   * @param index
   *          the index to look for
   * @return the row on the specified index
   */
  public Object[] getRow( int index ) {
    return rowCache.get( index );
  }

  /**
   * Insert a row into the list on a certain index
   *
   * @param index
   *          the index on which the row should be inserted
   * @param row
   *          the row to add
   */
  public void addRow( int index, Object[] row ) {
    rowCache.add( index, row );
  }

  /**
   * Looks up a row in the (sorted) cache.
   *
   * @param lookupRowData
   *          The data of the lookup row. Make sure that on the index of the from date, you put the lookup date.
   * @throws a
   *           KettleException in case there are conversion errors during the lookup of the row
   */
  public int lookupRow( Object[] lookupRowData ) throws KettleException {
    try {
      // First perform the lookup!
      //
      int index = Collections.binarySearch( rowCache, lookupRowData, this );
      if ( index < 0 ) {
        // What we have now is the insertion point.
        // Since we only compare on the start of the date range (see also: below in Compare.compare())
        // we will usually get the insertion point of the row
        // However, that insertion point is the actual row index IF the supplied lookup date (in the lookup row) is
        // between
        //
        // This row at the insertion point where the natural keys match and the start
        //
        int insertionPoint = -( index + 1 );
        if ( insertionPoint < rowCache.size() - 1 ) {
          // Get the row in question
          //
          Object[] row = rowCache.get( insertionPoint );

          // See if the natural key matches...
          //
          int cmp = rowMeta.compare( row, lookupRowData, keyIndexes );
          if ( cmp == 0 ) {
            // The natural keys match, now see if the lookup date (lookupRowData[fromDateIndex]) is between
            // row[fromDateIndex] and row[toDateIndex]
            //
            Date fromDate = rowMeta.getDate( row, fromDateIndex );
            Date toDate = rowMeta.getDate( row, toDateIndex );
            Date lookupDate = rowMeta.getDate( lookupRowData, fromDateIndex );

            if ( fromDate == null && toDate != null ) {
              // This is the case where the fromDate is null and the toDate is not.
              // This is a special case where null as a start date means -Infinity
              //
              if ( toDate.compareTo( lookupDate ) > 0 ) {
                return insertionPoint; // found the key!!
              } else {
                // This should never happen, it's a flaw in the data or the binary search algorithm...
                // TODO: print the row perhaps?
                //
                throw new KettleException(
                  "Key sorting problem detected during row cache lookup: the lookup date of "
                    + "the row retrieved is higher than or equal to the end of the date range." );
              }
            } else if ( fromDate != null && toDate == null ) {
              // This is the case where the toDate is null and the fromDate is not.
              // This is a special case where null as an end date means +Infinity
              //
              if ( fromDate.compareTo( lookupDate ) <= 0 ) {
                return insertionPoint; // found the key!!
              } else {
                // This should never happen, it's a flaw in the data or the binary search algorithm...
                // TODO: print the row perhaps?
                //
                throw new KettleException(
                  "Key sorting problem detected during row cache lookup: the lookup date of the row "
                    + "retrieved is lower than or equal to the start of the date range." );
              }
            } else {
              // Both dates are available: simply see if the lookup date falls in between...
              //
              if ( fromDate.compareTo( lookupDate ) <= 0 && toDate.compareTo( lookupDate ) > 0 ) {
                return insertionPoint;
              }
              // Else this is a cache miss.
            }
          }
        }
      }
      return index;
    } catch ( RuntimeException e ) {
      throw new KettleException( e );
    }
  }

  public void sortRows() {
    Collections.sort( rowCache, this );
  }

  /**
   * Compare 2 rows of data using the natural keys and indexes specified.
   *
   * @param o1
   * @param o2
   * @return
   */
  public int compare( Object[] o1, Object[] o2 ) {
    try {
      // First compare on the natural keys...
      //
      int cmp = rowMeta.compare( o1, o2, keyIndexes );
      if ( cmp != 0 ) {
        return cmp;
      }

      // Then see if the start of the date range of o2 falls between the start and end of o2
      //
      ValueMetaInterface fromDateMeta = rowMeta.getValueMeta( fromDateIndex );
      ValueMetaInterface toDateMeta = rowMeta.getValueMeta( toDateIndex );

      Date fromDate = fromDateMeta.getDate( o1[fromDateIndex] );
      Date toDate = toDateMeta.getDate( o1[toDateIndex] );
      Date lookupDate = fromDateMeta.getDate( o2[fromDateIndex] );

      int fromCmpLookup = 0;
      if ( fromDate == null ) {
        if ( lookupDate == null ) {
          fromCmpLookup = 0;
        } else {
          fromCmpLookup = -1;
        }
      } else {
        if ( lookupDate == null ) {
          fromCmpLookup = 1;
        } else {
          fromCmpLookup = fromDateMeta.compare( fromDate, lookupDate );
        }
      }
      if ( fromCmpLookup < 0 ) {
        if ( toDate != null ) {
          int toCmpLookup = toDateMeta.compare( toDate, lookupDate );
          if ( toCmpLookup > 0 ) {
            return 0;
          }
        }
      }
      return fromCmpLookup;
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  /**
   * @return the rowMeta
   */
  public RowMetaInterface getRowMeta() {
    return rowMeta;
  }

  /**
   * @param rowMeta
   *          the rowMeta to set
   */
  public void setRowMeta( RowMetaInterface rowMeta ) {
    this.rowMeta = rowMeta;
  }

  /**
   * @return the rowCache
   */
  public List<Object[]> getRowCache() {
    return rowCache;
  }

  /**
   * @param rowCache
   *          the rowCache to set
   */
  public void setRowCache( List<Object[]> rowCache ) {
    this.rowCache = rowCache;
  }

  /**
   * @return the keyIndexes
   */
  public int[] getKeyIndexes() {
    return keyIndexes;
  }

  /**
   * @param keyIndexes
   *          the keyIndexes to set
   */
  public void setKeyIndexes( int[] keyIndexes ) {
    this.keyIndexes = keyIndexes;
  }

  /**
   * @return the fromDateIndex
   */
  public int getFromDateIndex() {
    return fromDateIndex;
  }

  /**
   * @param fromDateIndex
   *          the fromDateIndex to set
   */
  public void setFromDateIndex( int fromDateIndex ) {
    this.fromDateIndex = fromDateIndex;
  }

  /**
   * @return the toDateIndex
   */
  public int getToDateIndex() {
    return toDateIndex;
  }

  /**
   * @param toDateIndex
   *          the toDateIndex to set
   */
  public void setToDateIndex( int toDateIndex ) {
    this.toDateIndex = toDateIndex;
  }
}
