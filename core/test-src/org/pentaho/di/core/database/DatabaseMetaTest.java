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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;

public class DatabaseMetaTest {
  private DatabaseMeta databaseMeta;

  @Before
  public void setUp() {
    databaseMeta = mock( DatabaseMeta.class );
    when( databaseMeta
      .databaseForBothDbInterfacesIsTheSame( any( DatabaseInterface.class ), any( DatabaseInterface.class ) ) )
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
          assertNotNull( "Got null on try: " + i++, DatabaseMeta.getDatabaseInterfacesMap() );
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
    assertEquals( expectedJndi, access );
  }

  @Test
  public void testQuoteReservedWords() {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    doCallRealMethod().when( databaseMeta ).quoteReservedWords( any( RowMetaInterface.class ) );
    doCallRealMethod().when( databaseMeta ).quoteField( anyString() );
    doCallRealMethod().when( databaseMeta ).setDatabaseInterface( any( DatabaseInterface.class ) );
    doReturn( "\"" ).when( databaseMeta ).getStartQuote();
    doReturn( "\"" ).when( databaseMeta ).getEndQuote();
    final DatabaseInterface databaseInterface = mock( DatabaseInterface.class );
    doReturn( true ).when( databaseInterface ).isQuoteAllFields();
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
      assertTrue( name.contains( "test_" + i ) );
      // check valueMeta is found by quoted name
      assertNotNull( fields.searchValueMeta( name ) );
    }
  }

  @Test
  public void testModifyingName() throws Exception {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm = new OracleDatabaseMeta();
    doCallRealMethod().when( databaseMeta ).setDatabaseInterface( any( DatabaseInterface.class ) );
    doCallRealMethod().when( databaseMeta ).setName( anyString() );
    doCallRealMethod().when( databaseMeta ).getName();
    doCallRealMethod().when( databaseMeta ).getDisplayName();
    databaseMeta.setDatabaseInterface( odbm );
    databaseMeta.setName( "test" );

    List<DatabaseMeta> list = new ArrayList<DatabaseMeta>();
    list.add( databaseMeta );

    DatabaseMeta databaseMeta2 = mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm2 = new OracleDatabaseMeta();
    doCallRealMethod().when( databaseMeta2 ).setDatabaseInterface( any( DatabaseInterface.class ) );
    doCallRealMethod().when( databaseMeta2 ).setName( anyString() );
    doCallRealMethod().when( databaseMeta2 ).getName();
    doCallRealMethod().when( databaseMeta2 ).setDisplayName( anyString() );
    doCallRealMethod().when( databaseMeta2 ).getDisplayName();
    doCallRealMethod().when( databaseMeta2 ).verifyAndModifyDatabaseName( any( ArrayList.class ), anyString() );
    databaseMeta2.setDatabaseInterface( odbm2 );
    databaseMeta2.setName( "test" );

    databaseMeta2.verifyAndModifyDatabaseName( list, null );

    assertTrue( !databaseMeta.getDisplayName().equals( databaseMeta2.getDisplayName() ) );
  }


  @Test
  public void indexOfName_NullArray() {
    assertEquals( -1, DatabaseMeta.indexOfName( null, "" ) );
  }

  @Test
  public void indexOfName_NullName() {
    assertEquals( -1, DatabaseMeta.indexOfName( new String[] { "1" }, null ) );
  }

  @Test
  public void indexOfName_ExactMatch() {
    assertEquals( 1, DatabaseMeta.indexOfName( new String[] { "a", "b", "c" }, "b" ) );
  }

  @Test
  public void indexOfName_NonExactMatch() {
    assertEquals( 1, DatabaseMeta.indexOfName( new String[] { "a", "b", "c" }, "B" ) );
  }


  @Test
  public void databases_WithSameDbConnTypes_AreTheSame() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );

    assertTrue( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerDatabaseMeta ) );
  }

  @Test
  public void databases_WithSameDbConnTypes_AreNotSame_IfPluginIdIsNull() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( null );

    assertFalse( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerDatabaseMeta ) );
  }

  @Test
  public void databases_WithDifferentDbConnTypes_AreDifferent_IfNonOfThemIsSubsetOfAnother() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface oracleDatabaseMeta = new OracleDatabaseMeta();
    oracleDatabaseMeta.setPluginId( "ORACLE" );

    assertFalse( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, oracleDatabaseMeta ) );
  }

  @Test
  public void databases_WithDifferentDbConnTypes_AreTheSame_IfOneConnTypeIsSubsetOfAnother_2LevelHierarchy() {
    DatabaseInterface mssqlServerDatabaseMeta =  new MSSQLServerDatabaseMeta();
    mssqlServerDatabaseMeta.setPluginId( "MSSQL" );
    DatabaseInterface mssqlServerNativeDatabaseMeta =  new MSSQLServerNativeDatabaseMeta();
    mssqlServerNativeDatabaseMeta.setPluginId( "MSSQLNATIVE" );


    assertTrue( databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta,
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

    assertTrue(
      databaseMeta.databaseForBothDbInterfacesIsTheSame( mssqlServerDatabaseMeta, mssqlServerNativeDatabaseMetaChild ) );
  }
}
