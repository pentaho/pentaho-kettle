/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.verification.VerificationMode;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.hash.ByteArrayHashMap;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepPartitioningMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.row.RowMeta.extractData;

public class DimensionLookupTest {
  private DatabaseMeta databaseMeta;

  private StepMeta stepMeta;

  private DimensionLookup dimensionLookup, dimensionLookupSpy;
  private DimensionLookupMeta dimensionLookupMeta;
  private DimensionLookupData dimensionLookupData;

  private ValueMetaString lookup;
  private ValueMetaInteger id;
  private ValueMetaInteger version;
  private ValueMetaDate from;
  private ValueMetaDate to;

  @SuppressWarnings("ResultOfMethodCallIgnored")
  @Before
  public void setUp() throws Exception {
    lookup = new ValueMetaString( "lookup" );
    id = new ValueMetaInteger( "id" );
    version = new ValueMetaInteger( "version" );
    from = new ValueMetaDate( "date_from" );
    to = new ValueMetaDate( "date_to" );

    databaseMeta = mock( DatabaseMeta.class );
    doReturn( "" ).when( databaseMeta ).quoteField( anyString() );

    dimensionLookupMeta = spy( new DimensionLookupMeta() );
    doReturn( databaseMeta ).when( dimensionLookupMeta ).getDatabaseMeta();
    doReturn( new String[] {} ).when( dimensionLookupMeta ).getKeyLookup();
    doReturn( new String[] {} ).when( dimensionLookupMeta ).getFieldLookup();
    doReturn( new int[] {} ).when( dimensionLookupMeta ).getFieldUpdate();

    stepMeta = mock( StepMeta.class );
    doReturn( "step" ).when( stepMeta ).getName();
    doReturn( mock( StepPartitioningMeta.class ) ).when( stepMeta ).getTargetStepPartitioningMeta();
    doReturn( dimensionLookupMeta ).when( stepMeta ).getStepMetaInterface();

    Database db = mock( Database.class );
    doReturn( mock( Connection.class ) ).when( db ).getConnection();

    dimensionLookupData = mock( DimensionLookupData.class );
    dimensionLookupData.db = db;
    dimensionLookupData.keynrs = new int[] { };
    dimensionLookupData.fieldnrs = new int[] { };
    dimensionLookupData.datefieldnr = -1;
    dimensionLookupData.notFoundTk = 0L;

    TransMeta transMeta = mock( TransMeta.class );
    doReturn( stepMeta ).when( transMeta ).findStep( anyString() );

    dimensionLookup = new DimensionLookup( stepMeta, dimensionLookupData, 1, transMeta, mock( Trans.class ) );
    dimensionLookup.setData( dimensionLookupData );
    dimensionLookup.setMeta( dimensionLookupMeta );
    dimensionLookupSpy = spy( dimensionLookup );
    doReturn( stepMeta ).when( dimensionLookupSpy ).getStepMeta();
    doReturn( false ).when( dimensionLookupSpy ).isRowLevel();
    doReturn( false ).when( dimensionLookupSpy ).isDebug();
    doReturn( true ).when( dimensionLookupSpy ).isAutoIncrement();
    doNothing().when( dimensionLookupSpy ).logDetailed( anyString() );
    when( dimensionLookupSpy.getTrans().getCurrentDate() ).thenReturn( new Date() );
  }

  @Test
  public void testDimInsert() throws Exception {
    RowMetaInterface rowMetaInterface = mock( RowMetaInterface.class );
    Object[] objects = mock( List.class ).toArray();
    Date date = mock( Date.class );
    dimensionLookupSpy.dimInsert( rowMetaInterface, objects, new Long( "132323" ), true, null, date, date );
    verify( databaseMeta, times( 0 ) ).supportsAutoGeneratedKeys();

    dimensionLookupSpy.dimInsert( rowMetaInterface, objects, null, true, null, date, date );
    verify( databaseMeta, times( 2 ) ).supportsAutoGeneratedKeys();
  }

  @Test
  public void cacheNotFoundKeys() throws Exception {
    final RowMeta rowMeta = initInputRowMeta();
    dimensionLookupMeta.setDefault();
    dimensionLookupMeta.setUpdate( false );
    dimensionLookupMeta.setKeyStream( new String[] {lookup.getName()} );

    dimensionLookupData.keynrs = new int[] {0};
    dimensionLookupData.min_date = dimensionLookupMeta.getMinDate();
    dimensionLookupData.max_date = dimensionLookupMeta.getMaxDate();
    dimensionLookupData.inputRowMeta = rowMeta;
    dimensionLookupData.outputRowMeta = initOutputRowMeta( dimensionLookupData.inputRowMeta );
    dimensionLookupData.lookupRowMeta = initLookupRowMeta();
    dimensionLookupData.cacheKeyRowMeta = rowMeta.clone();
    dimensionLookupData.cache = new ByteArrayHashMap( dimensionLookupData.cacheKeyRowMeta );
    when( dimensionLookupData.db.getReturnRowMeta() ).thenReturn( initReturnRowMeta() );

    // lookup for "key1", try found in DB and add to cache
    Object[] values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {"key1"} );
    assertArrayEquals( new Object[] {"key1", 0L}, values );
    assertTrue( "key1", containsCacheKey( "key1" ) );
    verify( dimensionLookupData.db ).getLookup( any( PreparedStatement.class ) );

    // lookup for "key1", hit from cache
    values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {"key1"} );
    assertArrayEquals( new Object[] {"key1", 0L}, values );
    assertTrue( "key1", containsCacheKey( "key1" ) );
    verify( dimensionLookupData.db ).getLookup( any( PreparedStatement.class ) );


    final VerificationMode twice = times( 2 );
    // lookup for null, try found in DB and add to cache
    values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {null} );
    assertArrayEquals( new Object[] {null, 0L}, values );
    assertTrue( "null", containsCacheKey( null ) );
    verify( dimensionLookupData.db, twice ).getLookup( any( PreparedStatement.class ) );

    // lookup for null, hit from cache
    values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {null} );
    assertArrayEquals( new Object[] {null, 0L}, values );
    assertTrue( "null", containsCacheKey( null ) );
    verify( dimensionLookupData.db, twice ).getLookup( any( PreparedStatement.class ) );
  }

  @Test
  public void lookupValuesWithPreloadingCache() throws Exception {
    final RowMeta rowMeta = initInputRowMeta();

    dimensionLookupMeta.setDefault();
    dimensionLookupMeta.setPreloadingCache( true );
    dimensionLookupMeta.setUpdate( false );
    dimensionLookupMeta.setKeyField( id.getName() );

    dimensionLookupData.keynrs = new int[] {0};
    dimensionLookupData.inputRowMeta = rowMeta;
    dimensionLookupData.outputRowMeta = initOutputRowMeta( rowMeta );
    dimensionLookupData.preloadIndexes = Collections.singletonList( 0 );
    dimensionLookupData.preloadFromDateIndex = 2;
    dimensionLookupData.preloadToDateIndex = 3;

    final RowMeta cacheMeta = initLookupRowMeta(lookup);
    dimensionLookupData.preloadCache = new DimensionCache( cacheMeta, new int[] {1}, dimensionLookupData.preloadFromDateIndex, dimensionLookupData.preloadToDateIndex );
    dimensionLookupData.preloadCache.setRowCache(
      Arrays.asList(
        new Object[] {0L, null, null, null}, // zero row (notFoundTk)
        new Object[] {1L, "key1", dimensionLookupMeta.getMinDate(), dimensionLookupMeta.getMaxDate()}        )
    );
    dimensionLookupData.preloadCache.sortRows();

    Object[] values;
    values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {"key1"} );
    assertArrayEquals( new Object[]{"key1", 1L}, values );

    values = dimensionLookupSpy.lookupValues( rowMeta, new Object[] {"key2"} );
    assertArrayEquals( new Object[]{"key2", 0L}, values );
  }


  private boolean containsCacheKey( String value ) {
    final byte[] key = extractData( dimensionLookupData.cacheKeyRowMeta, new Object[] {value} );
    return dimensionLookupData.cache.containsKey( key );
  }

  private RowMeta initInputRowMeta() {
    final RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( lookup );
    return rowMeta;
  }

  private RowMeta initOutputRowMeta( RowMetaInterface inputRowMeta ) {
    final RowMeta rowMeta = new RowMeta();
    rowMeta.addRowMeta( inputRowMeta );
    rowMeta.addValueMeta( id );
    return rowMeta;
  }

  private RowMeta initLookupRowMeta( ValueMetaInterface... lookup ) {
    final RowMeta rowMeta = new RowMeta();
    rowMeta.addValueMeta( id );
    if (lookup != null) {
      for ( ValueMetaInterface meta : lookup ) {
        rowMeta.addValueMeta( meta );
      }
    }
    rowMeta.addValueMeta( from );
    rowMeta.addValueMeta( to );
    return rowMeta;
  }

  private RowMeta initReturnRowMeta() {
    final RowMeta returnRowMeta = new RowMeta();
    returnRowMeta.addValueMeta( id );
    returnRowMeta.addValueMeta( version );
    returnRowMeta.addValueMeta( from );
    returnRowMeta.addValueMeta( to );
    return returnRowMeta;
  }
}
