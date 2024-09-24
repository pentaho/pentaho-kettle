/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

@SuppressWarnings( "deprecation" )
public class DatabaseTest {

  @ClassRule
  public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  private static final String TEST_NAME_OF_DB_CONNECTION = "TEST_CONNECTION";
  private static final String SQL_MOCK_EXCEPTION_MESSAGE = "SQL mock exception";
  private static final SQLException SQL_EXCEPTION = new SQLException( SQL_MOCK_EXCEPTION_MESSAGE );
  private static final String EXISTING_TABLE_NAME = "TABLE";
  private static final String NOT_EXISTING_TABLE_NAME = "NOT_EXISTING_TABLE";
  private static final String SCHEMA_TO_CHECK = "schemaPattern";
  private static final String[] TABLE_TYPES_TO_GET = { "TABLE", "VIEW" };

  //common fields
  private String sql = "select * from employees";
  private String columnName = "salary";
  private String fullJndiName = "jdbc/testJNDIName";
  private ResultSet rs = mock( ResultSet.class );
  private DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
  private DatabaseMetaData dbMetaDataMock = mock( DatabaseMetaData.class );
  private LoggingObjectInterface log = mock( LoggingObjectInterface.class );
  private DatabaseInterface databaseInterface = mock( DatabaseInterface.class );

  private DatabaseMeta meta = mock( DatabaseMeta.class );
  private PreparedStatement ps = mock( PreparedStatement.class );
  private DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
  private ResultSetMetaData rsMetaData = mock( ResultSetMetaData.class );
  private Connection conn;
  //end common fields

  @BeforeClass
  public static void setUpClass() throws Exception {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    conn = mockConnection( mock( DatabaseMetaData.class ) );
    when( log.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( dbMetaMock.getDatabaseInterface() ).thenReturn( databaseInterface );
    if ( !NamingManager.hasInitialContextFactoryBuilder() ) {
      // If JNDI is not initialized, use simpleJNDI
      System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
        "org.osjava.sj.memory.MemoryContextFactory" ); // pentaho#simple-jndi;1.0.0
      System.setProperty( "org.osjava.sj.jndi.shared", "true" );
      InitialContextFactoryBuilder simpleBuilder = new SimpleNamingContextBuilder();
      NamingManager.setInitialContextFactoryBuilder( simpleBuilder );
    }
  }

  @After
  public void tearDown() throws NamingException {
    InitialContext ctx = new InitialContext();
    ctx.unbind( fullJndiName );
  }

  @Test
  public void testConnectJNDI() throws SQLException, NamingException, KettleDatabaseException {
    InitialContext ctx = new InitialContext();
    String jndiName = "testJNDIName";
    when( meta.getName() ).thenReturn( "testName" );
    when( meta.getDatabaseName() ).thenReturn( jndiName );
    when( meta.getDisplayName() ).thenReturn( "testDisplayName" );
    when( meta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    when( meta.environmentSubstitute( jndiName ) ).thenReturn( jndiName );

    DataSource ds = mock( DataSource.class );
    when( ds.getConnection() ).thenReturn( conn );
    ctx.bind( fullJndiName, ds );

    Database db = new Database( log, meta );
    db.connect();
    assertEquals( conn, db.getConnection() );
  }

  @Test
  public void testGetQueryFieldsFromPreparedStatement() throws Exception {
    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );

    when( meta.stripCR( anyString() ) ).thenReturn( sql );
    when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    when( conn.prepareStatement( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY ) ).thenReturn( ps );
    when( ps.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFromPreparedStatement( sql );

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertTrue( rowMetaInterface.getValueMeta( 0 ) instanceof ValueMetaNumber );
  }

  @Test
  public void testGetQueryFieldsFromDatabaseMetaData() throws Exception {
    DatabaseMeta meta = mock( DatabaseMeta.class );
    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    Connection conn = mockConnection( dbMetaData );
    ResultSet rs = mock( ResultSet.class );
    String columnName = "year";
    String columnType = "Integer";
    int columnSize = 15;

    when( dbMetaData.getColumns( anyString(), anyString(), or( anyString(), eq( null ) ), or( anyString(), eq( null ) ) ) ).thenReturn( rs );
    when( rs.next() ).thenReturn( true ).thenReturn( false );
    when( rs.getString( "COLUMN_NAME" ) ).thenReturn( columnName );
    when( rs.getString( "SOURCE_DATA_TYPE" ) ).thenReturn( columnType );
    when( rs.getInt( "COLUMN_SIZE" ) ).thenReturn( columnSize );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFromDatabaseMetaData();

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getOriginalColumnTypeName(), columnType );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getLength(), columnSize );
  }

  @Test
  public void testGetQueryFieldsFallback() throws Exception {
    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );
    when( ps.executeQuery() ).thenReturn( rs );

    when( meta.stripCR( anyString() ) ).thenReturn( sql );
    when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    when( conn.prepareStatement( sql ) ).thenReturn( ps );
    when( rs.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFallback( sql, false, null, null );

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertTrue( rowMetaInterface.getValueMeta( 0 ) instanceof ValueMetaNumber );
  }

  /**
   * PDI-11363. when using getLookup calls there is no need to make attempt to retrieve row set metadata for every call.
   * That may bring performance penalty depends on jdbc driver implementation. For some drivers that penalty can be huge
   * (postgres).
   * <p/>
   * During the execution calling getLookup() method we changing usually only lookup where clause which will not impact
   * return row structure.
   *
   * @throws KettleDatabaseException
   * @throws SQLException
   */
  @Test
  public void testGetLookupMetaCalls() throws KettleDatabaseException, SQLException {
    when( meta.getQuotedSchemaTableCombination( anyString(), anyString() ) ).thenReturn( "a" );
    when( meta.quoteField( anyString() ) ).thenReturn( "a" );
    when( ps.executeQuery() ).thenReturn( rs );
    when( rs.getMetaData() ).thenReturn( rsMetaData );
    when( rsMetaData.getColumnCount() ).thenReturn( 0 );
    when( ps.getMetaData() ).thenReturn( rsMetaData );
    Database db = new Database( log, meta );
    Connection conn = mock( Connection.class );
    when( conn.prepareStatement( or( anyString(), eq( null ) ) ) ).thenReturn( ps );

    db.setConnection( conn );
    String[] name = new String[] { "a" };
    db.setLookup( "a", name, name, name, name, "a" );
    for ( int i = 0; i < 10; i++ ) {
      db.getLookup();
    }
    verify( rsMetaData, times( 1 ) ).getColumnCount();
  }

  /**
   * Test that for every PreparedStatement passed into lookup signature we do reset and re-create row meta.
   *
   * @throws SQLException
   * @throws KettleDatabaseException
   */
  @Test
  public void testGetLookupCallPSpassed() throws SQLException, KettleDatabaseException {
    when( ps.executeQuery() ).thenReturn( rs );
    when( rs.getMetaData() ).thenReturn( rsMetaData );
    when( rsMetaData.getColumnCount() ).thenReturn( 0 );
    when( ps.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.getLookup( ps );
    verify( rsMetaData, times( 1 ) ).getColumnCount();
  }

  @Test
  public void testCreateKettleDatabaseBatchExceptionNullUpdatesWhenSQLException() {
    assertNull( Database.createKettleDatabaseBatchException( "", new SQLException() ).getUpdateCounts() );
  }

  @Test
  public void testCreateKettleDatabaseBatchExceptionNotUpdatesWhenBatchUpdateException() {
    assertNotNull(
      Database.createKettleDatabaseBatchException( "", new BatchUpdateException( new int[ 0 ] ) ).getUpdateCounts() );
  }

  @Test
  public void testCreateKettleDatabaseBatchExceptionConstructsExceptionList() {
    BatchUpdateException root = new BatchUpdateException();
    SQLException next = new SQLException();
    SQLException next2 = new SQLException();
    root.setNextException( next );
    next.setNextException( next2 );
    List<Exception> exceptionList = Database.createKettleDatabaseBatchException( "", root ).getExceptionsList();
    assertEquals( 2, exceptionList.size() );
    assertEquals( next, exceptionList.get( 0 ) );
    assertEquals( next2, exceptionList.get( 1 ) );
  }

  @Test( expected = KettleDatabaseBatchException.class )
  public void testInsertRowWithBatchAlwaysThrowsKettleBatchException() throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection conn = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( conn );
    database.insertRow( ps, true, true );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testInsertRowWithoutBatchDoesntThrowKettleBatchException() throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeUpdate() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setConnection( conn );
    try {
      database.insertRow( ps, true, true );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @Test( expected = KettleDatabaseBatchException.class )
  public void testEmptyAndCommitWithBatchAlwaysThrowsKettleBatchException()
    throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.emptyAndCommit( ps, true, 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testEmptyAndCommitWithoutBatchDoesntThrowKettleBatchException()
    throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( log, meta );
    database.setConnection( mockConnection );
    try {
      database.emptyAndCommit( ps, true, 1 );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @Test( expected = KettleDatabaseBatchException.class )
  public void testInsertFinishedWithBatchAlwaysThrowsKettleBatchException()
    throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( log, meta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertFinished( ps, true );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testInsertFinishedWithoutBatchDoesntThrowKettleBatchException()
    throws KettleDatabaseException, SQLException {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( dbMetaData );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( log, meta );
    database.setConnection( mockConnection );
    try {
      database.insertFinished( ps, true );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @Test
  public void insertRowAndExecuteBatchCauseNoErrors() throws Exception {
    when( meta.supportsBatchUpdates() ).thenReturn( true );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.insertRow( ps, true, false );
    verify( ps ).addBatch();

    db.executeAndClearBatch( ps );
    verify( ps ).executeBatch();
    verify( ps ).clearBatch();
  }

  @Test
  public void insertRowWhenDbDoNotSupportBatchLeadsToCommit() throws Exception {
    when( meta.supportsBatchUpdates() ).thenReturn( false );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( false );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.insertRow( ps, true, false );
    verify( ps, never() ).addBatch();
    verify( ps ).executeUpdate();
  }

  @Test
  public void testGetCreateSequenceStatement() throws Exception {
    when( meta.supportsSequences() ).thenReturn( true );
    when( meta.supportsSequenceNoMaxValueOption() ).thenReturn( true );
    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.getCreateSequenceStatement( "schemaName", "seq", "10", "1", "-1", false );
    verify( databaseInterface, times( 1 ) ).getSequenceNoMaxValueOption();
  }

  @Test
  public void testPrepareSQL() throws Exception {
    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.prepareSQL( "SELECT * FROM DUMMY" );
    db.prepareSQL( "SELECT * FROM DUMMY", true );

    verify( databaseInterface, times( 2 ) ).supportsAutoGeneratedKeys();
  }

  @Test
  public void testGetCreateTableStatement() throws Exception {
    ValueMetaInterface v = mock( ValueMetaInterface.class );
    doReturn( " " ).when( databaseInterface ).getDataTablespaceDDL( any( VariableSpace.class ), eq( meta ) );
    doReturn( "CREATE TABLE " ).when( databaseInterface ).getCreateTableStatement();

    doReturn( databaseInterface ).when( meta ).getDatabaseInterface();
    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    String tableName = "DUMMY", tk = "tKey", pk = "pKey";
    RowMetaInterface fields = mock( RowMetaInterface.class );
    doReturn( 1 ).when( fields ).size();
    doReturn( v ).when( fields ).getValueMeta( 0 );
    boolean useAutoInc = true, semiColon = true;

    doReturn( "double foo" ).when( meta ).getFieldDefinition( v, tk, pk, useAutoInc );
    doReturn( true ).when( meta ).requiresCreateTablePrimaryKeyAppend();
    String statement = db.getCreateTableStatement( tableName, fields, tk, useAutoInc, pk, semiColon );
    String expectedStatRegexp = concatWordsForRegexp(
      "CREATE TABLE DUMMY", "\\(",
      "double foo", ",",
      "PRIMARY KEY \\(tKey\\)", ",",
      "PRIMARY KEY \\(pKey\\)",
      "\\)", ";" );
    assertTrue( statement.matches( expectedStatRegexp ) );
    doReturn( "CREATE COLUMN TABLE " ).when( databaseInterface ).getCreateTableStatement();
    statement = db.getCreateTableStatement( tableName, fields, tk, useAutoInc, pk, semiColon );

    expectedStatRegexp = concatWordsForRegexp(
      "CREATE COLUMN TABLE DUMMY", "\\(",
      "double foo", ",",
      "PRIMARY KEY \\(tKey\\)", ",",
      "PRIMARY KEY \\(pKey\\)",
      "\\)", ";" );
    assertTrue( statement.matches( expectedStatRegexp ) );
  }

  @Test
  public void testCheckTableExistsByDbMeta_Success() throws Exception {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaMock.getTables(
      same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertTrue( "The table " + EXISTING_TABLE_NAME + " is not in db meta data but should be here",
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableNotExistsByDbMeta() throws Exception {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaMock.getTables(
      same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertFalse( "The table " + NOT_EXISTING_TABLE_NAME + " is in db meta data but should not be here",
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, NOT_EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsKettleDatabaseException() {
    KettleDatabaseException kettleDatabaseException =
      new KettleDatabaseException(
        "Unable to check if table [" + EXISTING_TABLE_NAME + "] exists on connection [" + TEST_NAME_OF_DB_CONNECTION
          + "].", SQL_EXCEPTION );
    try {
      when( dbMetaMock.getName() ).thenReturn( TEST_NAME_OF_DB_CONNECTION );
      when( rs.next() ).thenReturn( true, false );
      when( rs.getString( "TABLE_NAME" ) ).thenThrow( SQL_EXCEPTION );
      when( dbMetaMock.getTables(
        same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown KettleDatabaseException but was not." );
    } catch ( KettleDatabaseException e ) {
      assertTrue( e instanceof KettleDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown KettleDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsKettleDatabaseException_WhenDbMetaNull() {
    KettleDatabaseException kettleDatabaseException =
      new KettleDatabaseException( "Unable to get database meta-data from the database." );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaMock.getTables(
        same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( rs );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( null ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown KettleDatabaseException but was not." );
    } catch ( KettleDatabaseException e ) {
      assertTrue( e instanceof KettleDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown KettleDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsKettleDatabaseException_WhenUnableToGetTableNames() {
    KettleDatabaseException kettleDatabaseException =
      new KettleDatabaseException( "Unable to get table-names from the database meta-data.", SQL_EXCEPTION );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaMock.getTables(
        same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenThrow( SQL_EXCEPTION );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown KettleDatabaseException but was not." );
    } catch ( KettleDatabaseException e ) {
      assertTrue( e instanceof KettleDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown KettleDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsKettleDatabaseException_WhenResultSetNull() {
    KettleDatabaseException kettleDatabaseException =
      new KettleDatabaseException( "Unable to get table-names from the database meta-data." );
    try {
      when( rs.next() ).thenReturn( true, false );
      when( dbMetaMock.getTables(
        same( dbMetaDataMock ), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( null );
      Database db = new Database( log, dbMetaMock );
      db.setConnection( mockConnection( dbMetaDataMock ) );
      db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME );
      fail( "There should be thrown KettleDatabaseException but was not." );
    } catch ( KettleDatabaseException e ) {
      assertTrue( e instanceof KettleDatabaseException );
      assertEquals( kettleDatabaseException.getLocalizedMessage(), e.getLocalizedMessage() );
    } catch ( Exception ex ) {
      fail( "There should be thrown KettleDatabaseException but was :" + ex.getMessage() );
    }
  }

  @Test
  public void mySqlVarBinaryIsConvertedToStringType() throws Exception {
    ResultSetMetaData rsMeta = mock( ResultSetMetaData.class );
    when( rsMeta.getColumnCount() ).thenReturn( 1 );
    when( rsMeta.getColumnLabel( 1 ) ).thenReturn( "column" );
    when( rsMeta.getColumnName( 1 ) ).thenReturn( "column" );
    when( rsMeta.getColumnType( 1 ) ).thenReturn( java.sql.Types.VARBINARY );
    when( rs.getMetaData() ).thenReturn( rsMeta );
    when( ps.executeQuery() ).thenReturn( rs );

    DatabaseMeta meta = new DatabaseMeta();
    meta.setDatabaseInterface( new MySQLDatabaseMeta() );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.getLookup( ps, false );

    RowMetaInterface rowMeta = db.getReturnRowMeta();
    assertEquals( 1, db.getReturnRowMeta().size() );

    ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
    assertEquals( ValueMetaInterface.TYPE_BINARY, valueMeta.getType() );
  }

  private String concatWordsForRegexp( String... words ) {
    String emptySpace = "\\s*";
    StringBuilder sb = new StringBuilder( emptySpace );
    for ( String word : words ) {
      sb.append( word ).append( emptySpace );
    }
    return sb.toString();
  }

  private Connection mockConnection( DatabaseMetaData dbMetaData ) throws SQLException {
    Connection conn = mock( Connection.class );
    MockDriver.conn = conn;
    when( conn.getMetaData() ).thenReturn( dbMetaData );
    when( conn.isValid( anyInt() ) ).thenReturn( true );
    return conn;
  }

  @Test
  public void usesCustomDsProviderIfSet_Pooling() throws Exception {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setUsingConnectionPool( true );
    testUsesCustomDsProviderIfSet( meta );
  }

  @Test
  public void usesCustomDsProviderIfSet_Jndi() throws Exception {
    DatabaseMeta meta = new DatabaseMeta();
    meta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    testUsesCustomDsProviderIfSet( meta );
  }

  private DataSourceProviderInterface testUsesCustomDsProviderIfSet( DatabaseMeta meta ) throws Exception {
    Connection connection = mock( Connection.class );
    DataSource ds = mock( DataSource.class );
    when( ds.getConnection() ).thenReturn( connection );
    when( ds.getConnection( anyString(), anyString() ) ).thenReturn( connection );
    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    when( provider.getNamedDataSource( anyString(), any( DataSourceProviderInterface.DatasourceType.class ) ) )
      .thenReturn( ds );
    when( provider.getPooledDataSourceFromMeta( any( DatabaseMeta.class ), any( DataSourceProviderInterface.DatasourceType.class ) ) )
      .thenReturn( ds );

    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( null );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    assertEquals( connection, db.getConnection() );
    return provider;
  }

  @Test
  public void jndiAccessTypePrevailsPooled() throws Exception {
    // this test is a guard of Database.normalConnect() contract:
    // it firstly tries to use JNDI name
    DatabaseMeta meta = new DatabaseMeta();
    meta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    meta.setUsingConnectionPool( true );

    DataSourceProviderInterface provider = testUsesCustomDsProviderIfSet( meta );
    verify( provider ).getNamedDataSource( anyString(), eq( DatasourceType.JNDI ) );
    verify( provider, never() ).getNamedDataSource( anyString(), eq( DatasourceType.POOLED ) );
  }

  @Test
  public void testNormalConnect_WhenTheProviderDoesNotReturnDataSourceWithPool() throws Exception {
    Driver driver = new MockDriver();
    DriverManager.registerDriver( driver );

    when( meta.isUsingConnectionPool() ).thenReturn( true );
    when( meta.getDriverClass() ).thenReturn( driver.getClass().getName() );
    when( meta.getURL( or( anyString(), eq( null ) ) ) ).thenReturn( "mockUrl" );
    when( meta.getInitialPoolSize() ).thenReturn( 1 );
    when( meta.getMaximumPoolSize() ).thenReturn( 1 );

    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    doThrow( new UnsupportedOperationException() ).when( provider ).getPooledDataSourceFromMeta( meta, DatasourceType.POOLED );
    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( "ConnectThatDoesNotExistInProvider" );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    //we will check only it not null since it will be wrapped by pool and its not eqal with conn from driver
    assertNotNull( db.getConnection() );

    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testNormalConnectWhenDatasourceNeedsUpdate() throws Exception {
    Driver driver = new MockDriver();
    DriverManager.registerDriver( driver );

    Properties prop = mock( Properties.class );
    when( meta.isUsingConnectionPool() ).thenReturn( true );
    when( meta.getDriverClass() ).thenReturn( driver.getClass().getName() );
    when( meta.getURL( anyString() ) ).thenReturn( "mockUrl" );
    when( meta.getInitialPoolSize() ).thenReturn( 1 );
    when( meta.getMaximumPoolSize() ).thenReturn( 1 );
    when( meta.isNeedUpdate() ).thenReturn( true );
    when( meta.getAttributes() ).thenReturn( prop );

    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    DataSource dataSource = mock( DataSource.class );
    Connection connection = mock( Connection.class );
    doThrow( new UnsupportedOperationException() ).when( provider ).getPooledDataSourceFromMeta( meta, DatasourceType.POOLED );
    when( dataSource.getConnection() ).thenReturn( connection );
    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( "ConnectThatDoesNotExistInProvider" );
      verify( meta, times( 1 ) ).setNeedUpdate( false );
      verify( provider, times( 1 ) ).invalidateNamedDataSource( any(), any() );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    //we will check only it not null since it will be wrapped by pool and its not equal with conn from driver
    assertNotNull( db.getConnection() );
    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testNormalConnectWhenDatasourceDontNeedsUpdate() throws Exception {
    Driver driver = new MockDriver();
    DriverManager.registerDriver( driver );

    when( meta.isUsingConnectionPool() ).thenReturn( true );
    when( meta.getDriverClass() ).thenReturn( driver.getClass().getName() );
    when( meta.getURL( anyString() ) ).thenReturn( "mockUrl" );
    when( meta.getInitialPoolSize() ).thenReturn( 1 );
    when( meta.getMaximumPoolSize() ).thenReturn( 1 );
    when( meta.isNeedUpdate() ).thenReturn( false );

    DataSourceProviderInterface provider = mock( DataSourceProviderInterface.class );
    doThrow( new UnsupportedOperationException() ).when( provider ).getPooledDataSourceFromMeta( meta, DatasourceType.POOLED );
    Database db = new Database( log, meta );
    final DataSourceProviderInterface existing = DataSourceProviderFactory.getDataSourceProviderInterface();
    try {
      DataSourceProviderFactory.setDataSourceProviderInterface( provider );
      db.normalConnect( null );
      verify( meta, times( 0 ) ).setNeedUpdate( false );
      verify( provider, times( 0 ) ).invalidateNamedDataSource( any(), any() );
    } finally {
      DataSourceProviderFactory.setDataSourceProviderInterface( existing );
    }
    //we will check only it not null since it will be wrapped by pool and its not equal with conn from driver
    assertNotNull( db.getConnection() );
    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testDisconnectPstmCloseFail()
    throws SQLException, KettleDatabaseException, NoSuchFieldException, IllegalAccessException {
    Database db = new Database( log, meta );
    Connection connection = mockConnection( dbMetaData );
    db.setConnection( connection );
    db.setCommit( 1 );
    Class<Database> databaseClass = Database.class;
    Field fieldPstmt = databaseClass.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );
    doThrow( new SQLException( "Test SQL exception" ) ).when( ps ).close();

    db.disconnect();
    verify( connection, times( 1 ) ).close();
  }


  @Test
  public void testDisconnectCommitFail() throws SQLException, NoSuchFieldException, IllegalAccessException {
    when( meta.supportsEmptyTransactions() ).thenReturn( true );
    when( dbMetaData.supportsTransactions() ).thenReturn( true );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    db.setCommit( 1 );

    Field fieldPstmt = Database.class.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );

    doThrow( new SQLException( "Test SQL exception" ) ).when( conn ).commit();
    db.disconnect();
    verify( conn, times( 1 ) ).close();
  }


  @Test
  public void testDisconnectConnectionGroup() throws SQLException {
    Database db = new Database( log, meta );
    db.setConnection( conn );
    db.setConnectionGroup( "1" );
    db.disconnect();
    verify( conn, never() ).close();
  }

  @Test
  public void testGetTablenames() throws SQLException, KettleDatabaseException {
    when( rs.next() ).thenReturn( true, false );
    when( rs.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaMock.getTables(
      same( dbMetaDataMock ), or( anyString(), eq( null ) ), or( anyString(), eq( null ) ), any() ) ).thenReturn( rs );
    Database db = new Database( log, dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    String[] tableNames = db.getTablenames();
    assertEquals( tableNames.length, 1 );
  }

  @Test
  public void testCheckTableExistsNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkTableExists( any(), any() );
    verify( db, times( 1 ) ).checkTableExists( any() );
    verify( db, times( 0 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckTableExistsFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkTableExists( any(), any() );
    verify( db, times( 1 ) ).checkTableExists( any() );
    verify( db, times( 0 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckTableExistsTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.checkTableExists( any(), any() );
    } catch ( KettleDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Unable to get table-names from the database meta-data" ) );
    }

    verify( db, times( 0 ) ).checkTableExists( any() );
    verify( db, times( 1 ) ).checkTableExistsByDbMeta( any(), any() );
  }

  @Test
  public void testCheckColumnExistsNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkColumnExists( any(), any(), any() );
    verify( db, times( 1 ) ).checkColumnExists( any(), any() );
    verify( db, times( 0 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testCheckColumnExistsFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.checkColumnExists( any(), any(), any() );
    verify( db, times( 1 ) ).checkColumnExists( any(), any() );
    verify( db, times( 0 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testCheckColumnExistsTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.checkColumnExists( any(), any(), any() );
    } catch ( KettleDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Metadata check failed. Fallback to statement check." ) );
    }

    verify( db, times( 0 ) ).checkColumnExists( any(), any() );
    verify( db, times( 1 ) ).checkColumnExistsByDbMeta( any(), any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaNoProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    Database db = spy( new Database( log, databaseMeta ) );

    try {
      db.getTableFieldsMeta( any(), any() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
    //verify( db, times( 1 ) ).getQueryFields( any(), any() );
    verify( db, times( 0 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaFalseProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "false" );
    Database db = spy( new Database( log, databaseMeta ) );

    db.getTableFieldsMeta( any(), any() );
    //verify( db, times( 1 ) ).getQueryFields( any(), any() );
    verify( db, times( 0 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  @Test
  public void testGetTableFieldsMetaTrueProperty() throws Exception {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setVariable( Const.KETTLE_COMPATIBILITY_USE_JDBC_METADATA, "true" );
    Database db = spy( new Database( log, databaseMeta ) );
    db.setConnection( conn );

    try {
      db.getTableFieldsMeta( any(), any() );
    } catch ( KettleDatabaseException e ) {
      // Expecting an error since we aren't mocking everything in a database connection.
      assertThat( e.getMessage(), containsString( "Failed to fetch fields from jdbc meta" ) );
    }

    //verify( db, times( 0 ) ).getQueryFields( any(), any() );
    verify( db, times( 1 ) ).getTableFieldsMetaByDbMeta( any(), any() );
  }

  public static class MockDriver implements Driver {
    public static Connection conn;

    public MockDriver() {

    }

    @Override public Connection connect( String url, Properties info ) throws SQLException {
      return conn;
    }

    @Override public boolean acceptsURL( String url ) throws SQLException {
      return true;
    }

    @Override public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
      return new DriverPropertyInfo[ 0 ];
    }

    @Override public int getMajorVersion() {
      return 0;
    }

    @Override public int getMinorVersion() {
      return 0;
    }

    @Override public boolean jdbcCompliant() {
      return true;
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException {
      return null;
    }
  }

}
