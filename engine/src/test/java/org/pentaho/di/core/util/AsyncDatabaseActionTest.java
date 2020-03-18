/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
 *
 * **************************************************************************
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
 */

package org.pentaho.di.core.util;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.H2DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaPluginType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AsyncDatabaseActionTest {

  private DatabaseMeta dbMeta;
  private CompletableFuture<String[]> completion = new CompletableFuture<>();
  private LogListener errorLogListener = new LogListener();
  private static final int COMPLETION_TIMEOUT = 2000;

  @BeforeClass
  public static void beforeClass() throws SQLException, KettleException {
    KettleEnvironment.init( false );
    PluginRegistry.addPluginType( ValueMetaPluginType.getInstance() );
    PluginRegistry.init( false );
    if ( ValueMetaFactory.getValueMetaPluginClasses().size() < 10 ) {
      fail( "ValueMetaPluginClasses not present in plugin registry." );
    }
    // create an in memory db that will be available for 15 seconds.
    try ( Connection connection = DriverManager.getConnection( "jdbc:h2:mem:TEST;DB_CLOSE_DELAY=15" ) ) {
      Statement statement = connection.createStatement();
      statement.setQueryTimeout( 1 );
      statement.execute( "CREATE TABLE FOO (col1 INTEGER, col2 VARCHAR(10));" );
      statement.execute( "CREATE TABLE BAR (col1 INTEGER, col2 INTEGER);" );
      statement.execute( "CREATE TABLE BAZ (col1 INTEGER, col2 INTEGER, col3 VARCHAR(256));" );
      statement.execute( "INSERT INTO  BAR VALUES (123, 321)" );
    }
  }

  @Before public void before() {
    dbMeta = new DatabaseMeta();
    H2DatabaseMeta h2 = new H2DatabaseMeta();
    dbMeta.setDatabaseInterface( h2 );
    dbMeta.setName( "mem:TEST" );
    dbMeta.setDBName( "mem:TEST" );
    dbMeta.setDatabaseType( "H2" );
    KettleLogStore.getAppender().addLoggingEventListener( errorLogListener );
  }


  @Test
  public void getTables() throws InterruptedException, ExecutionException, TimeoutException {
    AsyncDatabaseAction.getTables( dbMeta, "PUBLIC", completion::complete );
    String[] tables = completion.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
    assertThat( tables.length, equalTo( 3 ) );
    assertThat( sorted( tables ), equalTo( new String[] { "BAR", "BAZ", "FOO" } ) );
  }

  @Test
  public void getSchemas() throws InterruptedException, ExecutionException, TimeoutException {
    AsyncDatabaseAction.getSchemas( dbMeta, completion::complete );
    String[] schemas = completion.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
    assertThat( schemas.length, equalTo( 2 ) );
    assertThat( sorted( schemas ), equalTo( new String[] { "INFORMATION_SCHEMA", "PUBLIC" } ) );
  }

  @Test
  public void getFields() throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<RowMetaInterface> rowMetaCompletion = new CompletableFuture<>();
    AsyncDatabaseAction.getFields( dbMeta, "BAZ", rowMetaCompletion::complete );
    RowMetaInterface rowMeta = rowMetaCompletion.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
    assertThat( rowMeta.size(), equalTo( 3 ) );
    assertThat( sorted( rowMeta.getFieldNames() ), equalTo( new String[] { "COL1", "COL2", "COL3" } ) );
  }

  @Test
  public void executeSql() throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<List<Object[]>> rowMetaCompletion = new CompletableFuture<>();
    AsyncDatabaseAction.executeSql( dbMeta, "SELECT * FROM BAR", rowMetaCompletion::complete );
    List<Object[]> rows = rowMetaCompletion.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
    assertThat( rows.size(), equalTo( 1 ) );
    assertThat( rows.get( 0 )[ 0 ], equalTo( 123L ) );
    assertThat( rows.get( 0 )[ 1 ], equalTo( 321L ) );
  }

  @Test
  public void executeAction() throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<List<Object[]>> rowMetaCompletion = new CompletableFuture<>();
    AsyncDatabaseAction.executeAction( dbMeta, database -> {
      try {
        rowMetaCompletion.complete( database.getFirstRows( "BAR", 2 ) );
      } catch ( KettleDatabaseException e ) {
        throw new IllegalStateException( e );
      }
    } );
    List<Object[]> rows = rowMetaCompletion.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
    assertThat( rows.size(), equalTo( 1 ) );
    assertThat( rows.get( 0 )[ 0 ], equalTo( 123L ) );
    assertThat( rows.get( 0 )[ 1 ], equalTo( 321L ) );
  }

  /**
   * Verify that an exception on the async db thread results in a logged error.
   */
  @SuppressWarnings( "squid:S2699" )  // assertion is implicit. non-timeout validates that an error event was logged
  @Test public void testDbErrorsSqlAction() throws InterruptedException, ExecutionException, TimeoutException {
    dbMeta.setDatabaseType( "GENERIC" ); // causes incorrect jdbc url to be used.
    CompletableFuture<List<Object[]>> rowMetaCompletion = new CompletableFuture<>();
    AsyncDatabaseAction.executeSql( dbMeta, "SELECT * FROM BAR", rowMetaCompletion::complete );
    // blocks till an error message is logged, or timeoutexception
    errorLogListener.errorOccurred.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
  }

  @Test
  @SuppressWarnings( "squid:S2699" )  // assertion is implicit. non-timeout validates that an error event was logged
  public void getTablesError() throws InterruptedException, ExecutionException, TimeoutException {
    dbMeta.setDatabaseType( "GENERIC" );
    AsyncDatabaseAction.getTables( dbMeta, "PUBLIC", completion::complete );
    errorLogListener.errorOccurred.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
  }

  @Test
  @SuppressWarnings( "squid:S2699" )  // assertion is implicit. non-timeout validates that an error event was logged
  public void getSchemasError() throws InterruptedException, ExecutionException, TimeoutException {
    dbMeta.setDatabaseType( "GENERIC" );
    AsyncDatabaseAction.getSchemas( dbMeta, completion::complete );
    errorLogListener.errorOccurred.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
  }

  @Test
  @SuppressWarnings( "squid:S2699" )  // assertion is implicit. non-timeout validates that an error event was logged
  public void getFieldsError() throws InterruptedException, ExecutionException, TimeoutException {
    dbMeta.setDatabaseType( "GENERIC" );
    CompletableFuture<RowMetaInterface> rowMetaCompletion = new CompletableFuture<>();
    AsyncDatabaseAction.getFields( dbMeta, "BAZ", rowMetaCompletion::complete );
    errorLogListener.errorOccurred.get( COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS );
  }

  private String[] sorted( String[] strings ) {
    return Stream.of( strings ).sorted().toArray( String[]::new );
  }


  protected static class LogListener implements KettleLoggingEventListener {
    private CompletableFuture<Void> errorOccurred = new CompletableFuture<>();

    public void eventAdded( KettleLoggingEvent event ) {
      if ( event.getLevel() == LogLevel.ERROR ) {
        errorOccurred.complete( null );
      }
    }
  }
}
