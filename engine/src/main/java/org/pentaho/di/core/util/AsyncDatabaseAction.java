/*
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Static methods for asynchronous execution of actions on a {@link Database},
 * with a set timeout for the async thread.
 *
 * The .executeAction() method takes a Consumer<Database>, which defines what
 * action to take on the Database.
 *
 * The .executeSql / .getTables / .getSchemas / etc. methods are convenience
 * methods which leverage .executeAction() to retrieve specific info.
 *
 * These methods can be used wherever Database info needs to be retrieved
 * without blocking the calling thread, for example with setting UI elements:
 *
 *    AsyncDatabaseAction.getTables( dbMeta, schemaName,
 *          tables -> display.asyncExec(() -> combo.setItems(tables))
 *
 *  Note that the action of setting the items still needs to happen on
 *  the SWT display thread, hence the need for display.asyncExec().  So with
 *  the above two lines,
 *
 *     AsyncDatabaseAction thread retrieves the table array from the database,
 *     When retrieved, `combo.setItems(tables)`  is put on the display thread's queue.
 */
@SuppressWarnings ( "WeakerAccess" )
public class AsyncDatabaseAction {

  private static final long TIMEOUT = 2000;
  private static final int ROW_LIMIT = 10000;

  private AsyncDatabaseAction() {
  }

  public static void executeAction( DatabaseMeta databaseMeta, Consumer<Database> dbAction ) {
    CompletableFuture.supplyAsync( () -> internalExec( databaseMeta, dbAction, new LoggingObject( databaseMeta ) ) )
      .acceptEither( timeout(), r -> {
        // no-op
      } );
    // ^ this trick is used to enforce a timeout on the async thread in Java8.
    //  With Java 9+ we can use the .orTimeout() method.

  }

  public static void executeSql( DatabaseMeta databaseMeta, String sql, Consumer<List<Object[]>> rowConsumer ) {
    executeAction( databaseMeta, database -> {
      try {
        rowConsumer.accept( database.getRows( sql, ROW_LIMIT ) );
      } catch ( KettleDatabaseException | NullPointerException e ) {
        logError( databaseMeta, e );
        rowConsumer.accept( Collections.emptyList() );
      }
    } );
  }

  public static void getTables( DatabaseMeta databaseMeta, String schema, Consumer<String[]> tablesConsumer ) {
    executeAction( databaseMeta, database -> {
      try {
        tablesConsumer.accept( database.getTablenames( schema, false ) );
      } catch ( KettleDatabaseException | NullPointerException e ) {
        logError( databaseMeta, e );
        tablesConsumer.accept( new String[ 0 ] );
      }
    } );
  }

  public static void getSchemas( DatabaseMeta databaseMeta, Consumer<String[]> schemasConsumer ) {
    executeAction( databaseMeta, database -> {
      try {
        schemasConsumer.accept( database.getSchemas() );
      } catch ( KettleDatabaseException | NullPointerException e ) {
        logError( databaseMeta, e );
        schemasConsumer.accept( new String[ 0 ] );
      }
    } );
  }

  public static void getFields( DatabaseMeta databaseMeta, String tablename,
                                Consumer<RowMetaInterface> fieldsConsumer ) {
    executeAction( databaseMeta, database -> {
      try {
        fieldsConsumer.accept( database.getTableFields( tablename ) );
      } catch ( KettleDatabaseException | NullPointerException e ) {
        logError( databaseMeta, e );
        fieldsConsumer.accept( new RowMeta() );
      }
    } );
  }

  private static Void internalExec( DatabaseMeta databaseMeta, Consumer<Database> dbAction,
                                    LoggingObjectInterface log ) {
    if ( databaseMeta != null ) {
      try ( Database db = new Database( log, databaseMeta ) ) {
        db.connect();
        dbAction.accept( db );
      } catch ( KettleDatabaseException e ) {
        logError( databaseMeta, e );
        dbAction.accept( null );
      }
    }
    return null;
  }

  private static void logError( DatabaseMeta database, Throwable e ) {
    new LogChannel( database ).logError( e.getMessage(), e );
  }

  private static CompletableFuture<Void> timeout() {
    ScheduledExecutorService timeoutThread = Executors.newSingleThreadScheduledExecutor();
    CompletableFuture<Void> f = new CompletableFuture<>();
    timeoutThread.schedule( () -> f.completeExceptionally( new TimeoutException() ), TIMEOUT, MILLISECONDS );
    return f;
  }
}
