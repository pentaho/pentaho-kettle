/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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
 * WITHOUT WARRANTIES OR CONDITIONS OF Mockito.any KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.databaselookup;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.MySQLDatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.databaselookup.readallcache.ReadAllCache;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


/**
 * @author Andrey Khayrutdinov
 */
public class DatabaseLookupUTest {

  private static final String BINARY_FIELD = "aBinaryFieldInDb";
  private static final String ID_FIELD = "id";

  @BeforeClass
  public static void setUp() throws Exception {
    KettleEnvironment.init();
  }

  @Test
  public void mySqlVariantDbIsLazyConverted() throws Exception {
    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper = createMockHelper();
    DatabaseLookupMeta meta = createDatabaseMeta();
    DatabaseLookupData data = createDatabaseData();
    Database db = createVirtualDb( meta.getDatabaseMeta() );

    DatabaseLookup lookup = spyLookup( mockHelper, db, meta.getDatabaseMeta() );

    lookup.init( meta, data );
    lookup.processRow( meta, data );

    Mockito.verify( db ).getLookup( Mockito.any( PreparedStatement.class ), Mockito.anyBoolean(), Mockito.eq( false ) );
  }


  private StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> createMockHelper() {
    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper =
      new StepMockHelper<DatabaseLookupMeta, DatabaseLookupData>( "test DatabaseLookup", DatabaseLookupMeta.class,
        DatabaseLookupData.class );
    Mockito.when( mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );
    Mockito.when( mockHelper.trans.isRunning() ).thenReturn( true );

    RowMeta inputRowMeta = new RowMeta();
    RowSet rowSet = Mockito.mock( RowSet.class );
    Mockito.when( rowSet.getRowWait( Mockito.anyLong(), Mockito.any( TimeUnit.class ) ) ).thenReturn( new Object[ 0 ] ).thenReturn( null );
    Mockito.when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );

    Mockito.when( mockHelper.trans.findRowSet( Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt() ) ).thenReturn( rowSet );

    Mockito.when( mockHelper.transMeta.findNextSteps( Matchers.any( StepMeta.class ) ) )
      .thenReturn( Collections.singletonList( Mockito.mock( StepMeta.class ) ) );
    Mockito.when( mockHelper.transMeta.findPreviousSteps( Mockito.any( StepMeta.class ), Mockito.anyBoolean() ) )
      .thenReturn( Collections.singletonList( Mockito.mock( StepMeta.class ) ) );

    return mockHelper;
  }


  private DatabaseLookupMeta createDatabaseMeta() throws KettleException {
    MySQLDatabaseMeta mysql = new MySQLDatabaseMeta();
    mysql.setName( "MySQL" );
    DatabaseMeta dbMeta = new DatabaseMeta();
    dbMeta.setDatabaseInterface( mysql );

    DatabaseLookupMeta meta = new DatabaseLookupMeta();
    meta.setDatabaseMeta( dbMeta );
    meta.setTablename( "VirtualTable" );

    meta.setTableKeyField( new String[] { ID_FIELD } );
    meta.setKeyCondition( new String[] { "=" } );

    meta.setReturnValueNewName( new String[] { "returned value" } );
    meta.setReturnValueField( new String[] { BINARY_FIELD } );
    meta.setReturnValueDefaultType( new int[] { ValueMetaInterface.TYPE_BINARY } );

    meta.setStreamKeyField1( new String[ 0 ] );
    meta.setStreamKeyField2( new String[ 0 ] );

    meta.setReturnValueDefault( new String[] { "" } );

    meta = Mockito.spy( meta );
    Mockito.doAnswer( new Answer() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        RowMetaInterface row = (RowMetaInterface) invocation.getArguments()[ 0 ];
        ValueMetaInterface v = new ValueMetaBinary( BINARY_FIELD );
        row.addValueMeta( v );
        return null;
      }
    } ).when( meta ).getFields(
      Mockito.any( RowMetaInterface.class ),
      Mockito.anyString(),
      Mockito.any( RowMetaInterface[].class ),
      Mockito.any( StepMeta.class ),
      Mockito.any( VariableSpace.class ),
      Mockito.any( Repository.class ),
      Mockito.any( IMetaStore.class ) );
    return meta;
  }


  private DatabaseLookupData createDatabaseData() {
    return new DatabaseLookupData();
  }


  private Database createVirtualDb( DatabaseMeta meta ) throws Exception {
    ResultSet rs = Mockito.mock( ResultSet.class );
    Mockito.when( rs.getMetaData() ).thenReturn( Mockito.mock( ResultSetMetaData.class ) );

    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    Mockito.when( ps.executeQuery() ).thenReturn( rs );

    Connection connection = Mockito.mock( Connection.class );
    Mockito.when( connection.prepareStatement( Mockito.anyString() ) ).thenReturn( ps );

    Database db = new Database( Mockito.mock( LoggingObjectInterface.class ), meta );
    db.setConnection( connection );

    db = Mockito.spy( db );
    Mockito.doNothing().when( db ).normalConnect( Mockito.anyString() );

    ValueMetaInterface binary = new ValueMetaString( BINARY_FIELD );
    binary.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );

    ValueMetaInterface id = new ValueMetaInteger( ID_FIELD );

    RowMetaInterface metaByQuerying = new RowMeta();
    metaByQuerying.addValueMeta( binary );
    metaByQuerying.addValueMeta( id );

    Mockito.doReturn( metaByQuerying ).when( db ).getTableFields( Mockito.anyString() );

    return db;
  }


  private DatabaseLookup spyLookup( StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mocks, Database db,
                                    DatabaseMeta dbMeta ) {
    DatabaseLookup lookup =
      new DatabaseLookup( mocks.stepMeta, mocks.stepDataInterface, 1, mocks.transMeta, mocks.trans );
    lookup = Mockito.spy( lookup );

    Mockito.doReturn( db ).when( lookup ).getDatabase( Mockito.eq( dbMeta ) );
    for ( RowSet rowSet : lookup.getOutputRowSets() ) {
      if ( Mockito.mockingDetails( rowSet ).isMock() ) {
        Mockito.when( rowSet.putRow( Mockito.any( RowMetaInterface.class ), Mockito.any( Object[].class ) ) ).thenReturn( true );
      }
    }

    return lookup;
  }

  @Test
  public void getRowInCacheTest() throws KettleException {

    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper =
      new StepMockHelper<>( "Test", DatabaseLookupMeta.class, DatabaseLookupData.class );
    Mockito.when( mockHelper.logChannelInterfaceFactory.create( Mockito.any(), Mockito.any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );

    DatabaseLookup look =
      new DatabaseLookup( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta,
        mockHelper.trans );
    DatabaseLookupData lookData = new DatabaseLookupData();
    lookData.cache = DefaultCache.newCache( lookData, 0 );
    lookData.lookupMeta = new RowMeta();

    look.init( new DatabaseLookupMeta(), lookData );

    ValueMetaInterface valueMeta = new ValueMetaInteger( "fieldTest" );
    RowMeta lookupMeta = new RowMeta();
    lookupMeta.setValueMetaList( Collections.singletonList( valueMeta ) );
    Object[] kgsRow1 = new Object[ 1 ];
    kgsRow1[ 0 ] = 1L;
    Object[] kgsRow2 = new Object[ 1 ];
    kgsRow2[ 0 ] = 2L;
    Object[] add1 = new Object[ 1 ];
    add1[ 0 ] = 10L;
    Object[] add2 = new Object[ 1 ];
    add2[ 0 ] = 20L;
    lookData.cache.storeRowInCache( mockHelper.processRowsStepMetaInterface, lookupMeta, kgsRow1, add1 );
    lookData.cache.storeRowInCache( mockHelper.processRowsStepMetaInterface, lookupMeta, kgsRow2, add2 );

    Object[] rowToCache = new Object[ 1 ];
    rowToCache[ 0 ] = 0L;
    lookData.conditions = new int[ 1 ];
    lookData.conditions[ 0 ] = DatabaseLookupMeta.CONDITION_GE;
    Object[] dataFromCache = lookData.cache.getRowFromCache( lookupMeta, rowToCache );

    Assert.assertArrayEquals( dataFromCache, add1 );
  }


  @Test
  public void createsReadOnlyCache_WhenReadAll_AndNotAllEquals() throws Exception {
    DatabaseLookupData data = getCreatedData( false );
    Assert.assertThat( data.cache, CoreMatchers.is( CoreMatchers.instanceOf( ReadAllCache.class ) ) );
  }

  @Test
  public void createsReadDefaultCache_WhenReadAll_AndAllEquals() throws Exception {
    DatabaseLookupData data = getCreatedData( true );
    Assert.assertThat( data.cache, CoreMatchers.is( CoreMatchers.instanceOf( DefaultCache.class ) ) );
  }

  private DatabaseLookupData getCreatedData( boolean allEquals ) throws Exception {
    Database db = Mockito.mock( Database.class );
    Mockito.when( db.getRows( Mockito.anyString(), Mockito.anyInt() ) )
      .thenReturn( Collections.singletonList( new Object[] { 1L } ) );

    RowMeta returnRowMeta = new RowMeta();
    returnRowMeta.addValueMeta( new ValueMetaInteger() );
    Mockito.when( db.getReturnRowMeta() ).thenReturn( returnRowMeta );

    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper = createMockHelper();
    DatabaseLookupMeta meta = createTestMeta();
    DatabaseLookupData data = new DatabaseLookupData();

    DatabaseLookup step = createSpiedStep( db, mockHelper, meta );
    step.init( meta, data );


    data.db = db;
    data.keytypes = new int[] { ValueMetaInterface.TYPE_INTEGER };
    if ( allEquals ) {
      data.allEquals = true;
      data.conditions = new int[] { DatabaseLookupMeta.CONDITION_EQ };
    } else {
      data.allEquals = false;
      data.conditions = new int[] { DatabaseLookupMeta.CONDITION_LT };
    }
    step.processRow( meta, data );

    return data;
  }

  private DatabaseLookupMeta createTestMeta() {
    DatabaseLookupMeta meta = new DatabaseLookupMeta();
    meta.setCached( true );
    meta.setLoadingAllDataInCache( true );
    meta.setDatabaseMeta( Mockito.mock( DatabaseMeta.class ) );
    // it's ok here, we won't do actual work
    meta.allocate( 1, 0 );
    meta.setStreamKeyField1( new String[] { "Test" } );
    return meta;
  }

  private DatabaseLookup createSpiedStep( Database db,
                                          StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper,
                                          DatabaseLookupMeta meta ) throws KettleException {
    DatabaseLookup step = spyLookup( mockHelper, db, meta.getDatabaseMeta() );
    Mockito.doNothing().when( step ).determineFieldsTypesQueryingDb();
    Mockito.doReturn( null ).when( step ).lookupValues( Mockito.any( RowMetaInterface.class ), Mockito.any( Object[].class ) );

    RowMeta input = new RowMeta();
    input.addValueMeta( new ValueMetaInteger( "Test" ) );
    step.setInputRowMeta( input );
    return step;
  }


  @Test
  public void createsReadDefaultCache_AndUsesOnlyNeededFieldsFromMeta() throws Exception {
    Database db = Mockito.mock( Database.class );
    Mockito.when( db.getRows( Mockito.anyString(), Mockito.anyInt() ) )
      .thenReturn( Arrays.asList( new Object[] { 1L }, new Object[] { 2L } ) );

    RowMeta returnRowMeta = new RowMeta();
    returnRowMeta.addValueMeta( new ValueMetaInteger() );
    returnRowMeta.addValueMeta( new ValueMetaInteger() );
    Mockito.when( db.getReturnRowMeta() ).thenReturn( returnRowMeta );

    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper = createMockHelper();
    DatabaseLookupMeta meta = createTestMeta();
    DatabaseLookupData data = new DatabaseLookupData();

    DatabaseLookup step = createSpiedStep( db, mockHelper, meta );
    step.init( meta, data );

    data.db = db;
    data.keytypes = new int[] { ValueMetaInterface.TYPE_INTEGER };
    data.allEquals = true;
    data.conditions = new int[] { DatabaseLookupMeta.CONDITION_EQ };

    step.processRow( meta, data );

    data.lookupMeta = new RowMeta();
    data.lookupMeta.addValueMeta( new ValueMetaInteger() );

    Assert.assertNotNull( data.cache.getRowFromCache( data.lookupMeta, new Object[] { 1L } ) );
    Assert.assertNotNull( data.cache.getRowFromCache( data.lookupMeta, new Object[] { 2L } ) );
  }
}
