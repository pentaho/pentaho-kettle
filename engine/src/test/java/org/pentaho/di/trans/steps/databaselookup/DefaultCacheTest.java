/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.pentaho.test.util.InternalState.getInternalState;
import static org.pentaho.test.util.InternalState.setInternalState;


public class DefaultCacheTest {

  @Test
  public void storeRowInCacheSameIdTest() {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    //Several inserts with the same key and different values
    RowMeta rowMeta1 = new RowMeta(  );
    RowMeta rowMeta2 = new RowMeta(  );
    RowMeta rowMeta3 = new RowMeta(  );
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta1, new Object[]{1}, new Object[]{ 100 } );
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta2, new Object[]{1}, new Object[]{ 200 } );
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta3, new Object[]{1}, new Object[]{ 300 } );
    LinkedHashMap<RowMetaAndData, Object[]> map = (LinkedHashMap<RowMetaAndData, Object[]>) getInternalState( defaultCache, "map" );
    //Only one entry expected
    assertEquals( 1, map.size() );
    //The value expected is the first one inserted
    assertEquals( 100, map.get( new RowMetaAndData( rowMeta1, new Object[]{1} ) )[0] );
  }

  @Test
  public void storeRowInCacheTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    RowMeta rowMeta = new RowMeta();
    //Several inserts with different key and different values
    for ( int i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    LinkedHashMap<RowMetaAndData, Object[]> map = (LinkedHashMap<RowMetaAndData, Object[]>) getInternalState( defaultCache, "map" );
    //All inserted entries expected
    assertEquals( 10, map.size() );
  }

  @Test
  public void storeRowInCacheMaxSizeTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 3 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( false );
    when( databaseLookupMeta.getCacheSize() ).thenReturn( 3 );
    RowMeta rowMeta = new RowMeta();
    //Several inserts with different key and different values
    for ( int i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    LinkedHashMap<RowMetaAndData, Object[]> map = (LinkedHashMap<RowMetaAndData, Object[]>) getInternalState( defaultCache, "map" );
    //Max capacity of cache is 3, so in the end we can only have 3 entries
    assertEquals( 3, map.size() );
    //The cache eliminates the older entries when new ones are inserted, so in the end only expect the last 3 entries inserted
    assertEquals( 800, map.get( new RowMetaAndData( rowMeta, new Object[]{8} ) )[0] );
    assertEquals( 900, map.get( new RowMetaAndData( rowMeta, new Object[]{9} ) )[0] );
    assertEquals( 1000, map.get( new RowMetaAndData( rowMeta, new Object[]{10} ) )[0] );
  }

  @Test
  public void getRowFromCacheAllEqualConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", true );
    RowMeta rowMeta = new RowMeta();
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    //Several inserts with different key and different values
    for ( int i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( int i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( new Object[]{ i * 100 }[0], result[0] );
    }
  }

  @Test
  public void getRowFromCacheEqualConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_EQ } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( new Object[]{ i * 100 }[0], result[0] );
    }
  }

  @Test
  public void getRowFromCacheNotEqualConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_NE } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 2; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( 100L, result[0] );
    }
  }

  @Test
  public void getRowFromCacheLessConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_LT } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 2; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( 100L, result[0] );
    }
  }

  @Test
  public void getRowFromCacheLessOrEqualConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_LE } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( 100L, result[0] );
    }
  }

  @Test
  public void getRowFromCacheGreaterConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_GT } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 1; i < 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( new Object[]{ ( i + 1 ) * 100 }[0], result[0] );
    }
  }

  @Test
  public void getRowFromCacheGreaterOrEqualConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_GE } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( new Object[]{ i * 100 }[0], result[0] );
    }
  }

  @Test
  public void getRowFromCacheIsNullConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_IS_NULL } );
    //insert with null lookupdata
    defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{null}, new Object[]{ 100 } );
    Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{null} );
    assertEquals( new Object[] { 100 }[ 0 ], result[ 0 ] );
  }

  @Test
  public void getRowFromCacheNotNullConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_IS_NOT_NULL } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i}, new Object[]{ i * 100 } );
    }
    for ( long i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i} );
      assertEquals( 100L, result[0] );
    }
  }

  @Test
  public void getRowFromCacheInBetweenConditionTest() throws Exception {
    DatabaseLookupData databaseLookupData = mock( DatabaseLookupData.class );
    DatabaseLookupMeta databaseLookupMeta = mock( DatabaseLookupMeta.class );
    DefaultCache defaultCache = new DefaultCache( databaseLookupData, 10 );
    when( databaseLookupMeta.isLoadingAllDataInCache() ).thenReturn( true );
    setInternalState( databaseLookupData, "allEquals", false );
    RowMeta rowMeta = new RowMeta();
    ArrayList<ValueMetaInterface> valueMetaList = new ArrayList<>(  );
    valueMetaList.add( new ValueMetaInteger( ) );
    valueMetaList.add( new ValueMetaInteger( ) );
    rowMeta.setValueMetaList( valueMetaList );
    setInternalState( databaseLookupData, "lookupMeta", rowMeta );
    setInternalState( databaseLookupData, "conditions", new int[]{ DatabaseLookupMeta.CONDITION_BETWEEN } );
    //Several inserts with different key and different values
    for ( long i = 1; i <= 10; ++i ) {
      defaultCache.storeRowInCache( databaseLookupMeta, rowMeta, new Object[]{i, i * 10}, new Object[]{ i * 100, i * 1000 } );
    }
    for ( long i = 1; i <= 10; ++i ) {
      Object[] result = defaultCache.getRowFromCache( rowMeta, new Object[]{i, i * 10 } );
      assertEquals( new Object[]{ i * 100 }[0], result[0] );
    }
  }
}
