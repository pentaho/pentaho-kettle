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

package org.pentaho.di.trans.steps.databaselookup;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.TimedRow;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Old code, copied from {@linkplain DatabaseLookup}
 *
 * @author Andrey Khayrutdinov
 */
public class DefaultCache implements DatabaseLookupData.Cache {

  public static DefaultCache newCache( DatabaseLookupData data, int cacheSize ) {
    if ( cacheSize > 0 ) {
      cacheSize = (int) ( cacheSize * 1.5 );
    } else {
      cacheSize = 16;
    }
    return new DefaultCache( data, cacheSize );
  }


  private final DatabaseLookupData data;
  private final LinkedHashMap<RowMetaAndData, TimedRow> map;

  DefaultCache( DatabaseLookupData data, int capacity ) {
    this.data = data;
    map = new LinkedHashMap<>( capacity );
  }

  @Override
  public Object[] getRowFromCache( RowMetaInterface lookupMeta, Object[] lookupRow ) throws KettleException {
    if ( data.allEquals ) {
      // only do the map lookup when all equals otherwise conditions >, <, <> will give wrong results
      TimedRow timedRow = map.get( new RowMetaAndData( data.lookupMeta, lookupRow ) );
      if ( timedRow != null ) {
        return timedRow.getRow();
      }
    } else { // special handling of conditions <,>, <> etc.
      if ( !data.hasDBCondition ) { // e.g. LIKE not handled by this routine, yet
        // TODO: find an alternative way to look up the data based on the condition.
        // Not all conditions are "=" so we are going to have to evaluate row by row
        // A sorted list or index might be a good solution here...
        //
        for ( RowMetaAndData key : map.keySet() ) {
          // Now verify that the key is matching our conditions...
          //
          boolean match = true;
          int lookupIndex = 0;
          for ( int i = 0; i < data.conditions.length && match; i++ ) {
            ValueMetaInterface cmpMeta = lookupMeta.getValueMeta( lookupIndex );
            Object cmpData = lookupRow[ lookupIndex ];
            ValueMetaInterface keyMeta = key.getValueMeta( i );
            Object keyData = key.getData()[ i ];

            switch ( data.conditions[ i ] ) {
              case DatabaseLookupMeta.CONDITION_EQ:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) == 0 );
                break;
              case DatabaseLookupMeta.CONDITION_NE:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) != 0 );
                break;
              case DatabaseLookupMeta.CONDITION_LT:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) > 0 );
                break;
              case DatabaseLookupMeta.CONDITION_LE:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) >= 0 );
                break;
              case DatabaseLookupMeta.CONDITION_GT:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) < 0 );
                break;
              case DatabaseLookupMeta.CONDITION_GE:
                match = ( cmpMeta.compare( cmpData, keyMeta, keyData ) <= 0 );
                break;
              case DatabaseLookupMeta.CONDITION_IS_NULL:
                match = keyMeta.isNull( keyData );
                break;
              case DatabaseLookupMeta.CONDITION_IS_NOT_NULL:
                match = !keyMeta.isNull( keyData );
                break;
              case DatabaseLookupMeta.CONDITION_BETWEEN:
                // Between key >= cmp && key <= cmp2
                ValueMetaInterface cmpMeta2 = lookupMeta.getValueMeta( lookupIndex + 1 );
                Object cmpData2 = lookupRow[ lookupIndex + 1 ];
                match = ( keyMeta.compare( keyData, cmpMeta, cmpData ) >= 0 );
                if ( match ) {
                  match = ( keyMeta.compare( keyData, cmpMeta2, cmpData2 ) <= 0 );
                }
                lookupIndex++;
                break;
              // TODO: add LIKE operator (think of changing the hasDBCondition logic then)
              default:
                match = false;
                data.hasDBCondition = true; // avoid looping in here the next time, also safety when a new condition
                // will be introduced
                break;

            }
            lookupIndex++;
          }
          if ( match ) {
            TimedRow timedRow = map.get( key );
            if ( timedRow != null ) {
              return timedRow.getRow();
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void storeRowInCache( DatabaseLookupMeta meta, RowMetaInterface lookupMeta, Object[] lookupRow,
                               Object[] add ) {
    RowMetaAndData rowMetaAndData = new RowMetaAndData( lookupMeta, lookupRow );
    // DEinspanjer 2009-02-01 XXX: I want to write a test case to prove this point before checking in.
    // /* Don't insert a row with a duplicate key into the cache. It doesn't seem
    // * to serve a useful purpose and can potentially cause the step to return
    // * different values over the life of the transformation (if the source DB rows change)
    // * Additionally, if using the load all data feature, re-inserting would reverse the order
    // * specified in the step.
    // */
    // if (!data.look.containsKey(rowMetaAndData)) {
    // data.look.put(rowMetaAndData, new TimedRow(add));
    // }
    map.put( rowMetaAndData, new TimedRow( add ) );

    // See if we have to limit the cache_size.
    // Sample 10% of the rows in the cache.
    // Remove everything below the second lowest date.
    // That should on average remove more than 10% of the entries
    // It's not exact science, but it will be faster than the old algorithm

    // DEinspanjer 2009-02-01: If you had previously set a cache size and then turned on load all, this
    // method would throw out entries if the previous cache size wasn't big enough.
    if ( !meta.isLoadingAllDataInCache() && meta.getCacheSize() > 0 && map.size() > meta.getCacheSize() ) {
      List<RowMetaAndData> keys = new ArrayList<RowMetaAndData>( map.keySet() );
      List<Date> samples = new ArrayList<Date>();
      int incr = keys.size() / 10;
      if ( incr == 0 ) {
        incr = 1;
      }
      for ( int k = 0; k < keys.size(); k += incr ) {
        RowMetaAndData key = keys.get( k );
        TimedRow timedRow = map.get( key );
        samples.add( timedRow.getLogDate() );
      }

      Collections.sort( samples );

      if ( samples.size() > 1 ) {
        Date smallest = samples.get( 1 );

        // Everything below the smallest date goes away...
        for ( RowMetaAndData key : keys ) {
          TimedRow timedRow = map.get( key );

          if ( timedRow.getLogDate().compareTo( smallest ) < 0 ) {
            map.remove( key );
          }
        }
      }
    }
  }
}
