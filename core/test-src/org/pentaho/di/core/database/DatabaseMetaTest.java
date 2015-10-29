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

package org.pentaho.di.core.database;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.pentaho.di.core.database.DatabaseMeta.indexOfName;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;

public class DatabaseMetaTest {
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
    DatabaseInterface type = mock( DatabaseInterface.class );
    meta.setDatabaseInterface( type );

    when( type.getExtraOptions() ).thenReturn( existingOptions );
    when( type.getDefaultOptions() ).thenReturn( newOptions );

    meta.applyDefaultOptions( type );
    verify( type ).addExtraOption( "type1", "new", "newValue" );
    verify( type, never() ).addExtraOption( "type1", "existing", "existingDefault" );
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
  public void testGetFeatureSummary() throws Exception {
    DatabaseMeta databaseMeta = mock( DatabaseMeta.class );
    OracleDatabaseMeta odbm = new OracleDatabaseMeta();
    doCallRealMethod().when( databaseMeta ).setDatabaseInterface( any( DatabaseInterface.class ) );
    doCallRealMethod().when( databaseMeta ).getFeatureSummary();
    doCallRealMethod().when( databaseMeta ).getAttributes();
    databaseMeta.setDatabaseInterface( odbm );
    List<RowMetaAndData> result = databaseMeta.getFeatureSummary();
    assertNotNull( result );
    for ( RowMetaAndData rmd : result ) {
      assertEquals( 2, rmd.getRowMeta().size() );
      assertEquals( "Parameter", rmd.getRowMeta().getValueMeta( 0 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 0 ).getType() );
      assertEquals( "Value", rmd.getRowMeta().getValueMeta( 1 ).getName() );
      assertEquals( ValueMetaInterface.TYPE_STRING, rmd.getRowMeta().getValueMeta( 1 ).getType() );
    }
  }


  @Test
  public void indexOfName_NullArray() {
    assertEquals( -1, indexOfName( null, "" ) );
  }

  @Test
  public void indexOfName_NullName() {
    assertEquals( -1, indexOfName( new String[] { "1" }, null ) );
  }

  @Test
  public void indexOfName_ExactMatch() {
    assertEquals( 1, indexOfName( new String[] { "a", "b", "c" }, "b" ) );
  }

  @Test
  public void indexOfName_NonExactMatch() {
    assertEquals( 1, indexOfName( new String[] { "a", "b", "c" }, "B" ) );
  }
}
