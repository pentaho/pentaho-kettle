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

package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.trans.TransMeta;

/**
 * Try to test database functionality using a hypersonic database. This is just a small fraction of the functionality,
 * but could already trap a few problems.
 *
 * @author Sven Boden
 */
public class DatabaseIT {

  private static final int COUNT_OF_STEPS_WITH_DATABASE = 10;

  private LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );
  DatabaseMeta databaseMysqlMeta = new DatabaseMeta( "junit_db", "Mysql", "JDBC", null, "stub:stub", null, null, null );

  private RowMetaInterface params = Mockito.mock( RowMetaInterface.class );

  private Object[] data = new Object[] {};

  private PreparedStatement ps;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
  }

  @Before
  public void before() throws SQLException {
    ps = Mockito.mock( PreparedStatement.class );
    Mockito.when( ps.getMaxRows() ).thenReturn( 13 );
  }

  /**
   * Test that mysql fetch size is not set if fetch size more than
   * max rows ResultSet can return.
   *
   * @throws KettleDatabaseException
   * @throws SQLException
   */
  @Test
  public void testOpenQueryFetchSizeNotSet() throws KettleDatabaseException, SQLException {

    Database db = Mockito.spy( new MockDatabase( log, databaseMysqlMeta, 5 ) );
    try {
      db.openQuery( ps, params, data );
    } catch ( KettleDatabaseException e ) {
      // it is OK since we do not using real connection
    }
    Mockito.verify( ps, Mockito.times( 0 ) ).setFetchSize( Mockito.anyInt() );
  }

  /**
   * Test that non-mysql databases can set fetch size.
   *
   * @throws SQLException
   */
  @Test
  public void testOpenQueryFetchSizeSet() throws SQLException {
    DatabaseMeta emptyMeta = new DatabaseMeta();
    Database db = Mockito.spy( new MockDatabase( log, emptyMeta, 5 ) );
    try {
      db.openQuery( ps, params, data );
    } catch ( KettleDatabaseException e ) {
      // it is OK since we do not using real connection
    }
    Mockito.verify( ps, Mockito.times( 1 ) ).setFetchSize( Mockito.anyInt() );
  }

  /**
   * Test that if for mysql variant if max rows more than fetch size
   * we do set fetch size
   * @throws SQLException
   */
  @Test
  public void testOpenQuerySetMySqlFetchSize() throws SQLException {
    Database db = Mockito.spy( new MockDatabase( log, databaseMysqlMeta, 5 ) );
    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    Mockito.when( ps.getMaxRows() ).thenReturn( Const.FETCH_SIZE + 1 );
    try {
      db.openQuery( ps, params, data );
    } catch ( KettleDatabaseException e ) {
      // it is OK since we do not using real connection
    }
    Mockito.verify( ps, Mockito.times( 1 ) ).setFetchSize( Mockito.anyInt() );
  }

  @Test
  public void testDatabaseCasing() throws Exception {
    String tableName = "mIxCaSiNG";
    Database db = setupDatabase();
    db.connect();

    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaInteger( "ID" ),
      new ValueMetaInteger( "DLR_CD" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      valuesMeta[i].setLength( 8 );
      valuesMeta[i].setPrecision( 0 );
      rm.addValueMeta( valuesMeta[i] );
    }

    String createStatement = db.getCreateTableStatement( tableName, rm, null, false, null, true );
    db.execStatement( createStatement );

    // Make sure that the tablename is of mixed case
    assertFalse( tableName.equals( tableName.toLowerCase() ) );

    assertTrue( db.checkTableExists( tableName ) );
    assertEquals( false, db.checkTableExists( "unknown" ) );

    // We're testing here whether tables names are case insensitive.
    // If this would fail, it can either be a problem with PDI or
    // be a problem with a new H2 JDBC driver.
    assertTrue( db.checkTableExists( tableName.toLowerCase() ) );

    db.disconnect();
  }

  @Test
  public void testQuoting() throws Exception {
    Database database = setupDatabase();
    DatabaseMeta dbInfo = database.getDatabaseMeta();
    database.connect();

    assertNull( dbInfo.quoteField( null ) );
    assertEquals( "table1", dbInfo.quoteField( "table1" ) );
    assertEquals( "\"table 1\"", dbInfo.quoteField( "table 1" ) );
    assertEquals( "\"table-1\"", dbInfo.quoteField( "table-1" ) );
    assertEquals( "\"table+1\"", dbInfo.quoteField( "table+1" ) );
    assertEquals( "\"table.1\"", dbInfo.quoteField( "table.1" ) );

    assertNull( dbInfo.getQuotedSchemaTableCombination( null, null ) );
    assertEquals( "table1", dbInfo.getQuotedSchemaTableCombination( null, "table1" ) );
    assertEquals( "\"table 1\"", dbInfo.getQuotedSchemaTableCombination( null, "table 1" ) );
    assertEquals( "\"table-1\"", dbInfo.getQuotedSchemaTableCombination( null, "table-1" ) );
    assertEquals( "\"table+1\"", dbInfo.getQuotedSchemaTableCombination( null, "table+1" ) );
    assertEquals( "\"table.1\"", dbInfo.getQuotedSchemaTableCombination( null, "table.1" ) );

    assertEquals( "\"schema1\".\"null\"", dbInfo.getQuotedSchemaTableCombination( "schema1", null ) );
    assertEquals( "\"schema1\".\"table1\"", dbInfo.getQuotedSchemaTableCombination( "schema1", "table1" ) );

    // These 2 are maybe dodgy, but current behaviour
    assertEquals( "\"schema 1\".\"table 1\"", dbInfo.getQuotedSchemaTableCombination( "schema 1", "table 1" ) );
    assertEquals( "\"schema1\".\"table1\"", dbInfo.getQuotedSchemaTableCombination( "schema1", "\"table1\"" ) );

    database.disconnect();

  }

  @Test
  public void testEquals() throws Exception {
    Database db1 = setupDatabase();
    Database db2 = setupDatabase2();
    assertFalse( db1.equals( db2 ) );
  }

  @Test
  public void testBatchCommit() throws Exception {
    String tableName = "CommitTest";
    Database db = setupDatabase();
    db.connect();

    RowMetaInterface rm = new RowMeta();

    ValueMetaInterface[] valuesMeta =
    { new ValueMetaInteger( "ID" ), new ValueMetaInteger( "VALUE" ), };

    for ( int i = 0; i < valuesMeta.length; i++ ) {
      valuesMeta[i].setLength( 8 );
      valuesMeta[i].setPrecision( 0 );
      rm.addValueMeta( valuesMeta[i] );
    }

    String createStatement = db.getCreateTableStatement( tableName, rm, null, false, null, true );
    db.execStatement( createStatement );

    int insertSize = 3;
    db.setCommit( insertSize - 1 );

    fillDbInBatch( tableName, db, insertSize );

    db.truncateTable( tableName );

    insertSize = 3;
    db.setCommit( insertSize );

    fillDbInBatch( tableName, db, insertSize );

    db.disconnect();
  }

  @Test
  public void testNonPooledAndPooledNormalConnect() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool( COUNT_OF_STEPS_WITH_DATABASE );

    List<Callable<Connection>> tasks = new ArrayList<Callable<Connection>>();
    for ( int i = 0; i < COUNT_OF_STEPS_WITH_DATABASE; i++ ) {
      tasks.add(
          new Callable<Connection>() {
            @Override
            public Connection call() throws Exception {
              Database db = setupDatabase();
              db.normalConnect( null );
              return db.getConnection();
            }
          }
      );
      tasks.add(
          new Callable<Connection>() {
            @Override
            public Connection call() throws Exception {
              Database db2 = setupPoolingDatabaseWOConnect();
              db2.normalConnect( null );
              return db2.getConnection();
            }
          }
      );
    }
    List<Future<Connection>> futures = executorService.invokeAll( tasks );
    for ( Future<Connection> future : futures ) {
      assertNotNull( future.get() );
    }
  }

  public Database setupPoolingDatabaseWOConnect() throws Exception {
    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname"  );
    DatabaseMeta dbInfo = new DatabaseMeta(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<connection>\n"
            + "<name>db_pool</name>\n"
            + "<server>127.0.0.1</server>\n"
            + "<type>H2</type>\n"
            + "<access>Native</access>\n"
            + "<database>mem:db</database>\n"
            + "<port></port>\n"
            + "<username>sa</username>\n"
            + "<password></password>\n"
            + "<attributes>\n"
            + "<attribute><code>INITIAL_POOL_SIZE</code><attribute>5</attribute></attribute>\n"
            + "<attribute><code>IS_CLUSTERED</code><attribute>N</attribute></attribute>\n"
            + "<attribute><code>MAXIMUM_POOL_SIZE</code><attribute>10</attribute></attribute>\n"
            + "<attribute><code>USE_POOLING</code><attribute>Y</attribute></attribute>\n"
            + "</attributes>\n"
            + "</connection>" );
    // Add the database connections
    transMeta.addDatabase( dbInfo );

    Database database = new Database( transMeta, dbInfo );
    return database;
  }

  public Database setupDatabase() throws Exception {

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname"  );
    // Add the database connections
    DatabaseMeta databaseMeta = new DatabaseMeta(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<connection>"
            + "<name>db</name>"
            + "<server>127.0.0.1</server>"
            + "<type>H2</type>"
            + "<access>Native</access>"
            + "<database>mem:db</database>"
            + "<port></port>"
            + "<username>sa</username>"
            + "<password></password>"
            + "</connection>" );
    transMeta.addDatabase( databaseMeta );

    Database database = new Database( transMeta, databaseMeta );

    return database;
  }

  public Database setupDatabase2() throws Exception {

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "transname"  );
    // Add the database connections
    DatabaseMeta databaseMeta = new DatabaseMeta(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<connection>"
            + "<name>db2</name>"
            + "<server>10.12.4.1</server>"
            + "<type>H2</type>"
            + "<access>Native</access>"
            + "<database>mem:db2</database>"
            + "<port></port>"
            + "<username>sa</username>"
            + "<password></password>"
            + "</connection>" );
    transMeta.addDatabase( databaseMeta );

    Database database = new Database( transMeta, databaseMeta );

    return database;
  }

  private void fillDbInBatch( String tableName, Database db, int insertSize ) throws SQLException,
    KettleDatabaseException {
    String insert = "INSERT INTO " + tableName + " VALUES( ?, ? )";

    PreparedStatement ps = db.getConnection().prepareStatement( insert );
    final PreparedStatement psMocked = spy( ps );

    // Need mock to check executing with empty batch
    // some jdbc drivers verify batch is not empty some throw sqlexception
    // or in really neglected cases NullPointerException
    when( psMocked.executeBatch() ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        int[] succ = (int[]) invocation.callRealMethod();
        if ( succ.length == 0 ) {
          throw new SQLException( "Batch is empty" );
        }
        return succ;
      }
    } );

    for ( int i = 1; i <= insertSize; i++ ) {
      psMocked.setInt( 1, i );
      psMocked.setInt( 2, i );
      db.insertRow( psMocked, true, true );
    }
    db.emptyAndCommit( psMocked, true );
  }

  class MockDatabase extends Database {
    DatabaseMetaData md = Mockito.mock( DatabaseMetaData.class );
    int version;

    public MockDatabase( LoggingObjectInterface parentObject, DatabaseMeta databaseMeta, int version ) {
      super( parentObject, databaseMeta );
      this.version = version;
      Mockito.when( md.getDriverMajorVersion() ).thenReturn( version );
    }

    @Override
    public DatabaseMetaData getDatabaseMetaData() throws KettleDatabaseException {
      return md;
    }
  }

}
