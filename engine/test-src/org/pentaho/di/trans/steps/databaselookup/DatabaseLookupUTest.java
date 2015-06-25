/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.TimedRow;
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
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.pentaho.metastore.api.IMetaStore;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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

    verify( db ).getLookup( any( PreparedStatement.class ), anyBoolean(), eq( true ) );
  }


  private StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> createMockHelper() {
    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper =
      new StepMockHelper<DatabaseLookupMeta, DatabaseLookupData>( "test DatabaseLookup", DatabaseLookupMeta.class,
        DatabaseLookupData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );

    RowMeta inputRowMeta = new RowMeta();
    RowSet rowSet = mock( RowSet.class );
    when( rowSet.getRowWait( anyLong(), any( TimeUnit.class ) ) ).thenReturn( new Object[ 0 ] ).thenReturn( null );
    when( rowSet.getRowMeta() ).thenReturn( inputRowMeta );

    when( mockHelper.trans.findRowSet( anyString(), anyInt(), anyString(), anyInt() ) ).thenReturn( rowSet );

    when( mockHelper.transMeta.findNextSteps( Matchers.any( StepMeta.class ) ) )
      .thenReturn( singletonList( mock( StepMeta.class ) ) );
    when( mockHelper.transMeta.findPreviousSteps( any( StepMeta.class ), anyBoolean() ) )
      .thenReturn( singletonList( mock( StepMeta.class ) ) );

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

    meta = spy( meta );
    doAnswer( new Answer() {
      @Override public Object answer( InvocationOnMock invocation ) throws Throwable {
        RowMetaInterface row = (RowMetaInterface) invocation.getArguments()[ 0 ];
        ValueMetaInterface v = new ValueMetaBinary( BINARY_FIELD );
        row.addValueMeta( v );
        return null;
      }
    } ).when( meta ).getFields(
      any( RowMetaInterface.class ),
      anyString(),
      any( RowMetaInterface[].class ),
      any( StepMeta.class ),
      any( VariableSpace.class ),
      any( Repository.class ),
      any( IMetaStore.class )
    );
    return meta;
  }


  private DatabaseLookupData createDatabaseData() {
    return new DatabaseLookupData();
  }


  private Database createVirtualDb( DatabaseMeta meta ) throws Exception {
    ResultSet rs = mock( ResultSet.class );
    when( rs.getMetaData() ).thenReturn( mock( ResultSetMetaData.class ) );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeQuery() ).thenReturn( rs );

    Connection connection = mock( Connection.class );
    when( connection.prepareStatement( anyString() ) ).thenReturn( ps );

    Database db = new Database( mock( LoggingObjectInterface.class ), meta );
    db.setConnection( connection );

    db = spy( db );
    doNothing().when( db ).normalConnect( anyString() );

    ValueMetaInterface binary = new ValueMetaString( BINARY_FIELD );
    binary.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );

    ValueMetaInterface id = new ValueMetaInteger( ID_FIELD );

    RowMetaInterface metaByQuerying = new RowMeta();
    metaByQuerying.addValueMeta( binary );
    metaByQuerying.addValueMeta( id );

    doReturn( metaByQuerying ).when( db ).getTableFields( anyString() );

    return db;
  }


  private DatabaseLookup spyLookup( StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mocks, Database db,
                                    DatabaseMeta dbMeta ) {
    DatabaseLookup lookup =
      new DatabaseLookup( mocks.stepMeta, mocks.stepDataInterface, 1, mocks.transMeta, mocks.trans );
    lookup = Mockito.spy( lookup );

    doReturn( db ).when( lookup ).getDatabase( eq( dbMeta ) );
    for ( RowSet rowSet : lookup.getOutputRowSets() ) {
      if ( mockingDetails( rowSet ).isMock() ) {
        when( rowSet.putRow( any( RowMetaInterface.class ), any( Object[].class ) ) ).thenReturn( true );
      }
    }

    return lookup;
  }

  @Test
  public void getRowInCacheTest() throws KettleException {

    StepMockHelper<DatabaseLookupMeta, DatabaseLookupData> mockHelper = new StepMockHelper<DatabaseLookupMeta,
      DatabaseLookupData>( "Test", DatabaseLookupMeta.class, DatabaseLookupData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( mockHelper.logChannelInterface );

    DatabaseLookup look =
      new DatabaseLookup( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta,
        mockHelper.trans );
    DatabaseLookupData lookData = new DatabaseLookupData();
    lookData.look = new LinkedHashMap<RowMetaAndData, TimedRow>();
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
    look.storeRowInCache( lookupMeta, kgsRow1, add1 );
    look.storeRowInCache( lookupMeta, kgsRow2, add2 );

    Object[] rowToCache = new Object[ 1 ];
    rowToCache[ 0 ] = 0L;
    lookData.conditions = new int[ 1 ];
    lookData.conditions[ 0 ] = DatabaseLookupMeta.CONDITION_GE;
    Object[] dataFromCache = look.getRowFromCache( lookupMeta, rowToCache );

    Assert.assertTrue( Arrays.equals( dataFromCache, add1 ) );
  }
}
