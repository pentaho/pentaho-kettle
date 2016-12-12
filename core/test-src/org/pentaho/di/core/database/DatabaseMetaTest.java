/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database;

import org.junit.Assert;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DatabaseMetaTest {
  private DatabaseMeta databaseMeta;

  @Before
  public void setUp() {
    databaseMeta = Mockito.mock( DatabaseMeta.class );
    Mockito.when( databaseMeta
      .databaseForBothDbInterfacesIsTheSame( Mockito.any( DatabaseInterface.class ), Mockito.any( DatabaseInterface.class ) ) )
      .thenCallRealMethod();
  }

  @Test
  public void testGetDatabaseInterfacesMapWontReturnNullIfCalledSimultaneouslyWithClear() throws InterruptedException, ExecutionException {
    final AtomicBoolean done = new AtomicBoolean( false );
    ExecutorService executorService = Executors.newCachedThreadPool();
    executorService.submit( new Runnable() {

      @Override
      public void run() {
        while ( !done.get() ) {
          DatabaseMeta.clearDatabaseInterfacesMap();
        }
      }
    } );
    Future<Exception> getFuture = executorService.submit( new Callable<Exception>() {

      @Override
      public Exception call() throws Exception {
        int i = 0;
        while ( !done.get() ) {
          Assert.assertNotNull( "Got null on try: " + i++, DatabaseMeta.getDatabaseInterfacesMap() );
          if ( i > 30000 ) {
            done.set( true );
          }
        }
        return null;
      }
    } );
    getFuture.get();
  }

  @Test
  public void testDatabaseAccessTypeCode() throws Exception {
    String expectedJndi = "JNDI";
    String access = DatabaseMeta.getAccessTypeDesc( DatabaseMeta.getAccessType( expectedJndi ) );
    Assert.assertEquals( expectedJndi, access );
  }

  @Test
  public void testApplyingDefaultOptions() throws Exception {
    HashMap<String, String> existingOptions = new HashMap<String, String>();
    existingOptions.put( "type1.extra", "extraValue" );
    existingOptions.put( "type1.existing", "existingValue" );
    existingOptions.put( "type2.extra", "extraValue2" );

    HashMap<String, String> newOptions = new HashMap<String, String>();
    newOptions.put( "type1.new", "newValue" );
    newOptions.put( "type1.existing", "existingDefault" );

    // Register Natives to create a default DatabaseMeta
    DatabasePluginType.getInstance().searchPlugins();
    DatabaseMeta meta = new DatabaseMeta();
    DatabaseInterface type = Mockito.mock( DatabaseInterface.class );
    meta.setDatabaseInterface( type );

    Mockito.when( type.getExtraOptions() ).thenReturn( existingOptions );
    Mockito.when( type.getDefaultOptions() ).thenReturn( newOptions );

    meta.applyDefaultOptions( type );
    Mockito.verify( type ).addExtraOption( "type1", "new", "newValue" );
    Mockito.verify( type, Mockito.never() ).addExtraOption( "type1", "existing", "existingDefault" );
  }

  @Test
  public void testQuoteReservedWords() {
    DatabaseMeta databaseMeta = Mockito.mock( DatabaseMeta.class );
    Mockito.doCallRealMethod().when( databaseMeta ).quoteReservedWords( Mockito.any( RowMetaInterface.class ) );
    Mockito.doCallRealMethod().when( databaseMeta ).quoteField( Mockito.anyString() );
    Mockito.doCallRealMethod().when( databaseMeta ).setDatabaseInterface( Mockito.any( DatabaseInterface.class ) );
    Mockito.doReturn( "\"" ).when( databaseMeta ).getStartQuote();
    Mockito.doReturn( "\"" ).when( databaseMeta ).getEndQuote();
    final DatabaseInterface databaseInterface = Mockito.mock( DatabaseInterface.class );
    Mockito.doReturn( true ).when( databaseInterface ).isQuoteAllFields();
    databaseMeta.setDatabaseInterface( databaseInterface );

    final RowMeta fields = new RowMeta();
    for ( int i = 0; i < 10; i++ ) {
      final ValueMeta valueMeta = new ValueMeta( "test_" + i );
      fields.addValueMeta( valueMeta );
    }

    for ( int i = 0; i < 10; i++ ) {
      databaseMeta.quoteReservedWords( fields );
    }

    for ( int i = 0; i < 10; i++ ) {
      databaseMeta.quoteReservedWords( fields );
      final String name = fields.getValueMeta( i ).getName();
      // check valueMeta index in list
      Assert.assertTrue( name.contains( "test_" + i ) );
      // check valueMeta is found by quoted name
      Assert.assertNotNull( fields.searchValueMeta( name ) );
    }
  }

  @Test
  public void testModifyingName() throws Exception {
    DatabaseMeta databaseMeta = Mockito.mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm = new OracleDatabaseMeta();
    Mockito.doCallRealMethod().when( databaseMeta ).setDatabaseInterface( Mockito.any( DatabaseInterface.class ) );
    Mockito.doCallRealMethod().when( databaseMeta ).setName( Mockito.anyString() );
    Mockito.doCallRealMethod().when( databaseMeta ).getName();
    Mockito.doCallRealMethod().when( databaseMeta ).getDisplayName();
    databaseMeta.setDatabaseInterface( odbm );
    databaseMeta.setName( "test" );

    List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
    list.add( databaseMeta );

    DatabaseMeta databaseMeta2 = Mockito.mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm2 = new OracleDatabaseMeta();
    Mockito.doCallRealMethod().when( databaseMeta2 ).setDatabaseInterface( Mockito.any( DatabaseInterface.class ) );
    Mockito.doCallRealMethod().when( databaseMeta2 ).setName( Mockito.anyString() );
    Mockito.doCallRealMethod().when( databaseMeta2 ).getName();
    Mockito.doCallRealMethod().when( databaseMeta2 ).setDisplayName( Mockito.anyString() );
    Mockito.doCallRealMethod().when( databaseMeta2 ).getDisplayName();
    Mockito.doCallRealMethod().when( databaseMeta2 ).verifyAndModifyDatabaseName( Mockito.any( ArrayList.class ), Mockito.anyString() );
    databaseMeta2.setDatabaseInterface( odbm2 );
    databaseMeta2.setName( "test" );

    databaseMeta2.verifyAndModifyDatabaseName( list, null );

    Assert.assertTrue( !databaseMeta.getDisplayName().equals( databaseMeta2.getDisplayName() ) );
  }

  @Test
  public void testGetFeatureSummary() throws Exception {
    DatabaseMeta databaseMeta = Mockito.mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm = new OracleDatabaseMeta();
    Mockito.doCallRealMethod().when( databaseMeta ).setDatabaseInterface( Mockito.any( DatabaseInterface.class ) );
    Mockito.doCallRealMethod().when( databaseMeta ).getFeatureSummary();
    Mockito.doCallRealMethod().when( databaseMeta ).getAttributes();
    databaseMeta.setDatabaseInterface( odbm );
    List<RowMetaAndData> result = databaseMeta.getFeatureSummary();
    Assert.assertNotNull( result );
    for ( RowMetaAndData rmd : result ) {
      Assert.assertEquals( 2, rmd.getRowMeta().size() );
      Assert.assertEquals( "Parameter", rmd.getRowMeta().getValueMeta( 0 ).getName() );
      Assert.assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 0 ).getType() );
      Assert.assertEquals( "Value", rmd.getRowMeta().getValueMeta( 1 ).getName() );
      Assert.assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 1 ).getType() );
    }
  }


  @Test
  public void indexOfName_NullArray() {
    Assert.assertEquals( -1, DatabaseMeta.indexOfName( null, "" ) );
  }

  @Test
  public void indexOfName_NullName() {
    Assert.assertEquals( -1, DatabaseMeta.indexOfName( new String[] { "1" }, null ) );
  }

  @Test
  public void indexOfName_ExactMatch() {
    Assert.assertEquals( 1, DatabaseMeta.indexOfName( new String[] { "a", "b", "c" }, "b" ) );
  }

  @Test
  public void indexOfName_NonExactMatch() {
    Assert.assertEquals( 1, DatabaseMeta.indexOfName( new String[] { "a", "b", "c" }, "B" ) );
  }


  @Test
  public void databases_WithSameDbConnTypes_AreTheSame() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );

    Assert.assertTrue( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerDatabaseMeta ) );
  }

  @Test
  public void databases_WithSameDbConnTypes_AreNotSame_IfPluginIdIsNull() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( null );

    Assert.assertFalse( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerDatabaseMeta ) );
  }

  @Test
  public void databases_WithDifferentDbConnTypes_AreDifferent_IfNonOfThemIsSubsetOfAnother() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface oracleDatabaseMeta = new OracleDatabaseMeta();
    oracleDatabaseMeta.setPluginId( "ORACLE" );

    Assert.assertFalse( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, oracleDatabaseMeta ) );
  }

  @Test
  public void databases_WithDifferentDbConnTypes_AreTheSame_IfOneConnTypeIsSubsetOfAnother_2LevelHierarchy() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface mssqlServerNativeDatabaseMeta =  new MSSQLServerNativeDatabaseMeta();
    mssqlServerNativeDatabaseMeta.setPluginId( "MSSQLNATIVE" );


    Assert.assertTrue( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta,
      mssqlServerNativeDatabaseMeta ) );
  }

  @Test
  public void databases_WithDifferentDbConnTypes_AreTheSame_IfOneConnTypeIsSubsetOfAnother_3LevelHierarchy() {
    class MSSQLServerNativeDatabaseMetaChild extends MSSQLServerDatabaseMeta {
      @Override
      public String getPluginId() {
        return "MSSQLNATIVE_CHILD";
      }
    }

    DatabaseInterface mssqlServerDatabaseMeta = new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface mssqlServerNativeDatabaseMetaChild = new MSSQLServerNativeDatabaseMetaChild();

    Assert.assertTrue(
      databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerNativeDatabaseMetaChild ) );
  }

  @Test
  public void testCheckParameters() {
    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    BaseDatabaseMeta databaseInterface = Mockito.mock( BaseDatabaseMeta.class );
    Mockito.when( meta.getDatabaseInterface() ).thenReturn( databaseInterface );
    Mockito.when( meta.getName() ).thenReturn( null );
    Mockito.when( meta.isPartitioned() ).thenReturn( false );
    Mockito.when( meta.checkParameters() ).thenCallRealMethod();
    Assert.assertEquals( 2, meta.checkParameters().length );
  }

  @Test
  public void setSQLServerInstanceTest() {
    DatabaseMeta dbmeta = new DatabaseMeta();
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface mssqlServerNativeDatabaseMeta =  new MSSQLServerNativeDatabaseMeta();
    mssqlServerNativeDatabaseMeta.setPluginId( "MSSQLNATIVE" );
    dbmeta.setDatabaseInterface( mssqlServerDatabaseMeta );
    dbmeta.setSQLServerInstance( "" );
    Assert.assertEquals( dbmeta.getSQLServerInstance(), null );
    dbmeta.setSQLServerInstance( "instance1" );
    Assert.assertEquals( dbmeta.getSQLServerInstance(), "instance1" );
    dbmeta.setDatabaseInterface( mssqlServerNativeDatabaseMeta );
    dbmeta.setSQLServerInstance( "" );
    Assert.assertEquals( dbmeta.getSQLServerInstance(), null );
    dbmeta.setSQLServerInstance( "instance1" );
    Assert.assertEquals( dbmeta.getSQLServerInstance(), "instance1" );
  }
}
