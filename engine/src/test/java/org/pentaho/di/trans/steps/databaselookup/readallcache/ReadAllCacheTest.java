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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.databaselookup.DatabaseLookupData;
import org.pentaho.di.trans.steps.databaselookup.DatabaseLookupMeta;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Andrey Khayrutdinov
 */
public class ReadAllCacheTest {

  private DatabaseLookupData stepData;
  private RowMeta keysMeta;
  private Object[][] keys;
  private Object[][] data;

  @Before
  public void setUp() {
    stepData = new DatabaseLookupData();
    stepData.conditions = new int[ 4 ];

    keysMeta = new RowMeta();
    keysMeta.addValueMeta( new ValueMetaInteger() );
    keysMeta.addValueMeta( new ValueMetaString() );
    keysMeta.addValueMeta( new ValueMetaDate() );
    keysMeta.addValueMeta( new ValueMetaInteger() );

    keys = new Object[][] {
      new Object[] { 0L, "0", new Date( 0 ), null },
      new Object[] { 0L, "0", new Date( 50 ), null },
      new Object[] { 2L, "2", new Date( 200 ), null },
      new Object[] { 1L, "1", new Date( 100 ), null },
      new Object[] { 1L, "1", new Date( 150 ), null }
    };

    data = new Object[][] {
      new Object[] { 0 },
      new Object[] { 1 },
      new Object[] { 2 },
      new Object[] { 3 },
      new Object[] { 4 }
    };
  }

  @After
  public void tearDown() {
    stepData = null;
    keysMeta = null;
    keys = null;
    data = null;
  }

  @Test( expected = UnsupportedOperationException.class )
  public void storeRowInCache_ThrowsException() throws Exception {
    buildCache( "" ).storeRowInCache( new DatabaseLookupMeta(), keysMeta.clone(), keys[ 0 ], data[ 0 ] );
  }

  @Test
  public void hasDbConditionStopsSearching() throws Exception {
    stepData.hasDBCondition = true;
    assertNull( buildCache( "" ).getRowFromCache( keysMeta.clone(), keys[ 0 ] ) );
  }

  @Test
  public void lookup_Finds_Only() throws Exception {
    ReadAllCache cache = buildCache( "=,<,=,IS NULL" );
    Object[] found = cache.getRowFromCache( keysMeta.clone(), new Object[] { 1L, "2", new Date( 100 ), null } );
    assertArrayEquals( "(keys[0] == 1) && (keys[1] < '2') && (keys[2] == 100) --> row 3", data[ 3 ], found );
  }

  @Test
  public void lookup_Finds_FirstMatching() throws Exception {
    ReadAllCache cache = buildCache( "=,IS NOT NULL,<=,IS NULL" );
    Object[] found = cache.getRowFromCache( keysMeta.clone(), new Object[] { 1L, null, new Date( 1000000 ), null } );
    assertArrayEquals( "(keys[0] == 1) && (keys[2] < 1000000) --> row 3", data[ 3 ], found );
  }

  @Test
  public void lookup_Finds_WithBetweenOperator() throws Exception {
    RowMeta meta = keysMeta.clone();
    meta.setValueMeta( 3, new ValueMetaDate() );
    meta.addValueMeta( new ValueMetaInteger() );

    ReadAllCache cache = buildCache( "<>,IS NOT NULL,BETWEEN,IS NULL" );
    Object[] found = cache.getRowFromCache(
      meta, new Object[] { -1L, null, new Date( 140 ), new Date( 160 ), null } );
    assertArrayEquals( "(140 <= keys[2] <= 160) --> row 4", data[ 4 ], found );
  }

  @Test
  public void lookup_Finds_WithTwoBetweenOperators() throws Exception {
    RowMeta meta = new RowMeta();
    meta.addValueMeta( new ValueMetaInteger() );
    meta.addValueMeta( new ValueMetaString() );
    meta.addValueMeta( new ValueMetaString() );
    meta.addValueMeta( new ValueMetaDate() );
    meta.addValueMeta( new ValueMetaDate() );
    meta.addValueMeta( new ValueMetaInteger() );

    ReadAllCache cache = buildCache( ">,BETWEEN,BETWEEN,IS NULL" );
    Object[] found = cache.getRowFromCache(
      meta, new Object[] { -1L, "1", "3", new Date( 0 ), new Date( 1000 ), null } );
    assertArrayEquals( "('1' <= keys[1] <= '3') && (0 <= keys[2] <= 1000) --> row 2", data[ 2 ], found );
  }

  @Test
  public void lookup_DoesNotFind_FilteredByIndex() throws Exception {
    ReadAllCache cache = buildCache( "=,IS NOT NULL,>=,IS NOT NULL" );
    Object[] found = cache.getRowFromCache( keysMeta.clone(), new Object[] { 1L, null, new Date( 0 ), null } );
    assertNull( "(keys[3] != NULL) --> none", found );
  }

  @Test
  public void lookup_DoesNotFind_WithBetweenOperator() throws Exception {
    RowMeta meta = keysMeta.clone();
    meta.setValueMeta( 3, new ValueMetaDate() );
    meta.addValueMeta( new ValueMetaInteger() );

    ReadAllCache cache = buildCache( "<>,IS NOT NULL,BETWEEN,IS NULL" );
    Object[] found = cache.getRowFromCache(
      meta, new Object[] { -1L, null, new Date( 1000 ), new Date( 2000 ), null } );
    assertNull( "(1000 <= keys[2] <= 2000) --> none", found );
  }

  private ReadAllCache buildCache( String conditions ) throws Exception {
    StringTokenizer tokenizer = new StringTokenizer( conditions, "," );
    List<String> operators = Arrays.asList( DatabaseLookupMeta.conditionStrings );
    int conditionIndex = 0;
    while ( tokenizer.hasMoreElements() ) {
      String operator = tokenizer.nextToken();
      int index = operators.indexOf( operator );
      if ( index == -1 ) {
        throw new RuntimeException( conditions + " -- " + operator );
      }
      stepData.conditions[ conditionIndex ] = index;
      conditionIndex++;
    }

    ReadAllCache.Builder builder = new ReadAllCache.Builder( stepData, keys.length );
    builder.setKeysMeta( keysMeta );
    for ( int i = 0; i < keys.length; i++ ) {
      Object[] keyTuple = keys[ i ];
      Object[] dataTuple = data[ i ];
      builder.add( keyTuple, dataTuple );
    }
    return builder.build();
  }


  @Test
  public void lookup_HandlesAbsenceOfLookupValue() throws Exception {
    stepData = new DatabaseLookupData();
    stepData.conditions = new int[] { DatabaseLookupMeta.CONDITION_IS_NOT_NULL };

    ReadAllCache.Builder builder = new ReadAllCache.Builder( stepData, 2 );
    RowMeta keysMeta = new RowMeta();
    keysMeta.addValueMeta( new ValueMetaInteger() );
    builder.setKeysMeta( keysMeta );
    builder.add( new Object[] { null }, new Object[] { "null" } );
    builder.add( new Object[] { 1L }, new Object[] { "one" } );
    ReadAllCache cache = builder.build();

    Object[] found = cache.getRowFromCache( new RowMeta(), new Object[ 0 ] );
    assertArrayEquals( "(keys[1] == 1L) --> row 2", new Object[] { "one" }, found );
  }
}
