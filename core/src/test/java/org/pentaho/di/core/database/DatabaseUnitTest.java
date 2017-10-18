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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.List;

import javax.sql.DataSource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.variables.VariableSpace;

public class DatabaseUnitTest {
  private static final String TEST_NAME_OF_DB_CONNECTION = "TEST_CONNECTION";
  private static final String SQL_MOCK_EXCEPTION_MESSAGE = "SQL mock exception";
  private static final SQLException SQL_EXCEPTION = new SQLException( SQL_MOCK_EXCEPTION_MESSAGE );
  private static final String EXISTING_TABLE_NAME = "TABLE";
  private static final String NOT_EXISTING_TABLE_NAME = "NOT_EXISTING_TABLE";
  private static final String SCHEMA_TO_CHECK = "schemaPattern";
  private static final String[] TABLE_TYPES_TO_GET = { "TABLE", "VIEW" };
  private ResultSet resultSetMock = mock( ResultSet.class );
  private DatabaseMeta dbMetaMock = mock( DatabaseMeta.class );
  private DatabaseMetaData dbMetaDataMock = mock( DatabaseMetaData.class );
  static LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );

  @BeforeClass
  public static void setUp() throws Exception {
    KettleClientEnvironment.init();
  }

  @Test
  public void testGetQueryFieldsFromPreparedStatement() throws Exception {
    String sql = "select * from employees";
    String columnName = "salary";

    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    Connection conn = mockConnection( mock( DatabaseMetaData.class ) );
    ResultSetMetaData rsMetaData = mock( ResultSetMetaData.class );

    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );

    Mockito.when( meta.stripCR( anyString() ) ).thenReturn( sql );
    Mockito.when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    Mockito.when( conn.prepareStatement( sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY ) )
        .thenReturn( ps );
    Mockito.when( ps.getMetaData() ).thenReturn( rsMetaData );

    Database db = new Database( log, meta );
    db.setConnection( conn );
    RowMetaInterface rowMetaInterface = db.getQueryFieldsFromPreparedStatement( sql );

    assertEquals( rowMetaInterface.size(), 1 );
    assertEquals( rowMetaInterface.getValueMeta( 0 ).getName(), columnName );
    assertTrue( rowMetaInterface.getValueMeta( 0 ) instanceof ValueMetaNumber );
  }

  @Test
  public void testGetQueryFieldsFromDatabaseMetaData() throws Exception {
    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    Connection conn = mockConnection( dbMetaData );
    ResultSet columns = mock( ResultSet.class );
    String columnName = "year";
    String columnType = "Integer";
    int columnSize = 15;

    Mockito.when( dbMetaData.getColumns( anyString(), anyString(), anyString(), anyString() ) ).thenReturn( columns );
    Mockito.when( columns.next() ).thenReturn( true ).thenReturn( false );
    Mockito.when( columns.getString( "COLUMN_NAME" ) ).thenReturn( columnName );
    Mockito.when( columns.getString( "SOURCE_DATA_TYPE" ) ).thenReturn( columnType );
    Mockito.when( columns.getInt( "COLUMN_SIZE" ) ).thenReturn( columnSize );

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
    String sql = "select * from employees";
    String columnName = "salary";

    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    Connection conn = mockConnection( mock( DatabaseMetaData.class ) );
    ResultSetMetaData rsMetaData = mock( ResultSetMetaData.class );
    ResultSet rs = Mockito.mock( ResultSet.class );

    when( rsMetaData.getColumnCount() ).thenReturn( 1 );
    when( rsMetaData.getColumnName( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnLabel( 1 ) ).thenReturn( columnName );
    when( rsMetaData.getColumnType( 1 ) ).thenReturn( Types.DECIMAL );
    when( ps.executeQuery() ).thenReturn( rs );

    Mockito.when( meta.stripCR( anyString() ) ).thenReturn( sql );
    Mockito.when( meta.getDatabaseInterface() ).thenReturn( new MySQLDatabaseMeta() );
    Mockito.when( conn.prepareStatement( sql ) ).thenReturn( ps );
    Mockito.when( rs.getMetaData() ).thenReturn( rsMetaData );

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
    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    Mockito.when( meta.getQuotedSchemaTableCombination( Mockito.anyString(), Mockito.anyString() ) ).thenReturn( "a" );
    Mockito.when( meta.quoteField( Mockito.anyString() ) ).thenReturn( "a" );

    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    ResultSet rs = Mockito.mock( ResultSet.class );
    Mockito.when( ps.executeQuery() ).thenReturn( rs );

    ResultSetMetaData rmeta = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( rs.getMetaData() ).thenReturn( rmeta );

    Mockito.when( rmeta.getColumnCount() ).thenReturn( 0 );
    Mockito.when( ps.getMetaData() ).thenReturn( rmeta );

    Database db = new Database( log, meta );

    Connection conn = Mockito.mock( Connection.class );
    Mockito.when( conn.prepareStatement( Mockito.anyString() ) ).thenReturn( ps );

    db.setConnection( conn );
    String[] name = new String[] { "a" };
    db.setLookup( "a", name, name, name, name, "a" );

    for ( int i = 0; i < 10; i++ ) {
      db.getLookup();
    }
    Mockito.verify( rmeta, Mockito.times( 1 ) ).getColumnCount();
  }

  /**
   * Test that for every PreparedStatement passed into lookup signature we do reset and re-create row meta.
   *
   * @throws SQLException
   * @throws KettleDatabaseException
   */
  @Test
  public void testGetLookupCallPSpassed() throws SQLException, KettleDatabaseException {
    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );

    PreparedStatement ps = Mockito.mock( PreparedStatement.class );
    ResultSet rs = Mockito.mock( ResultSet.class );
    Mockito.when( ps.executeQuery() ).thenReturn( rs );

    ResultSetMetaData rmeta = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( rs.getMetaData() ).thenReturn( rmeta );

    Mockito.when( rmeta.getColumnCount() ).thenReturn( 0 );
    Mockito.when( ps.getMetaData() ).thenReturn( rmeta );

    Database db = new Database( log, meta );

    db.getLookup( ps );
    Mockito.verify( rmeta, Mockito.times( 1 ) ).getColumnCount();
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
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertRow( ps, true, true );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testInsertRowWithoutBatchDoesntThrowKettleBatchException() throws KettleDatabaseException, SQLException {
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeUpdate() ).thenThrow( new SQLException() );

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setConnection( mockConnection );
    try {
      database.insertRow( ps, true, true );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @Test( expected = KettleDatabaseBatchException.class )
  public void testEmptyAndCommitWithBatchAlwaysThrowsKettleBatchException() throws KettleDatabaseException,
    SQLException {
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.emptyAndCommit( ps, true, 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testEmptyAndCommitWithoutBatchDoesntThrowKettleBatchException()
    throws KettleDatabaseException, SQLException {
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setConnection( mockConnection );
    try {
      database.emptyAndCommit( ps, true, 1 );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @SuppressWarnings( "deprecation" )
  @Test( expected = KettleDatabaseBatchException.class )
  public void testInsertFinishedWithBatchAlwaysThrowsKettleBatchException() throws KettleDatabaseException,
    SQLException {
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertFinished( ps, true );
  }

  @SuppressWarnings( "deprecation" )
  @Test( expected = KettleDatabaseException.class )
  public void testInsertFinishedWithoutBatchDoesntThrowKettleBatchException()
    throws KettleDatabaseException, SQLException {
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    Connection mockConnection = mockConnection( mockDatabaseMetaData );

    PreparedStatement ps = mock( PreparedStatement.class );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( mockLogger(), mockDatabaseMeta );
    database.setConnection( mockConnection );
    try {
      database.insertFinished( ps, true );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }

  @Test
  public void insertRowAndExecuteBatchCauseNoErrors() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.supportsBatchUpdates() ).thenReturn( true );

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( true );

    Database db = new Database( mockLogger(), dbMeta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    PreparedStatement ps = mock( PreparedStatement.class );

    db.insertRow( ps, true, false );
    verify( ps ).addBatch();

    db.executeAndClearBatch( ps );
    verify( ps ).executeBatch();
    verify( ps ).clearBatch();
  }

  @Test
  public void insertRowWhenDbDoNotSupportBatchLeadsToCommit() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.supportsBatchUpdates() ).thenReturn( false );

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    when( dbMetaData.supportsBatchUpdates() ).thenReturn( false );

    Database db = new Database( mockLogger(), dbMeta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    PreparedStatement ps = mock( PreparedStatement.class );

    db.insertRow( ps, true, false );
    verify( ps, never() ).addBatch();
    verify( ps ).executeUpdate();
  }

  @Test
  public void testGetCreateSequenceStatement() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.supportsSequences() ).thenReturn( true );
    when( dbMeta.supportsSequenceNoMaxValueOption() ).thenReturn( true );

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    DatabaseInterface databaseInterface = mock( DatabaseInterface.class );
    doReturn( databaseInterface ).when( dbMeta ).getDatabaseInterface();

    Database db = new Database( mockLogger(), dbMeta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    db.getCreateSequenceStatement( "schemaName", "seq", "10", "1", "-1", false );
    verify( databaseInterface, times( 1 ) ).getSequenceNoMaxValueOption();
  }

  @Test
  public void testPrepareSQL() throws Exception {
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseInterface databaseInterface = mock( DatabaseInterface.class );
    doReturn( databaseInterface ).when( dbMeta ).getDatabaseInterface();

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );

    Database db = new Database( mockLogger(), dbMeta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );
    db.prepareSQL( "SELECT * FROM DUMMY" );
    db.prepareSQL( "SELECT * FROM DUMMY", true );

    verify( databaseInterface, times( 2 ) ).supportsAutoGeneratedKeys();
  }

  @Test
  public void testGetCreateTableStatement() throws Exception {
    ValueMetaInterface v = mock( ValueMetaInterface.class );

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );

    DatabaseInterface databaseInterface = mock( DatabaseInterface.class );
    doReturn( " " ).when( databaseInterface ).getDataTablespaceDDL( any( VariableSpace.class ), eq( dbMeta ) );
    doReturn( "CREATE TABLE " ).when( databaseInterface ).getCreateTableStatement();

    doReturn( databaseInterface ).when( dbMeta ).getDatabaseInterface();

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );

    Database db = new Database( mockLogger(), dbMeta );
    db.setConnection( mockConnection( dbMetaData ) );
    db.setCommit( 1 );

    String tableName = "DUMMY", tk = "tKey", pk = "pKey";
    RowMetaInterface fields = mock( RowMetaInterface.class );
    doReturn( 1 ).when( fields ).size();
    doReturn( v ).when( fields ).getValueMeta( 0 );
    boolean useAutoInc = true, semiColon = true;

    doReturn( "double foo" ).when( dbMeta ).getFieldDefinition( v, tk, pk, useAutoInc );

    doReturn( true ).when( dbMeta ).requiresCreateTablePrimaryKeyAppend();

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
    when( resultSetMock.next() ).thenReturn( true, false );
    when( resultSetMock.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( resultSetMock );
    Database db = new Database( mockLogger(), dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertTrue( "The table " + EXISTING_TABLE_NAME + " is not in db meta data but should be here", db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableNotExistsByDbMeta() throws Exception {
    when( resultSetMock.next() ).thenReturn( true, false );
    when( resultSetMock.getString( "TABLE_NAME" ) ).thenReturn( EXISTING_TABLE_NAME );
    when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( resultSetMock );
    Database db = new Database( mockLogger(), dbMetaMock );
    db.setConnection( mockConnection( dbMetaDataMock ) );

    assertFalse( "The table " + NOT_EXISTING_TABLE_NAME + " is in db meta data but should not be here", db.checkTableExistsByDbMeta( SCHEMA_TO_CHECK, NOT_EXISTING_TABLE_NAME ) );
  }

  @Test
  public void testCheckTableExistsByDbMetaThrowsKettleDatabaseException() {
    KettleDatabaseException kettleDatabaseException =
        new KettleDatabaseException( "Unable to check if table [" + EXISTING_TABLE_NAME + "] exists on connection [" + TEST_NAME_OF_DB_CONNECTION + "].", SQL_EXCEPTION );
    try {
      when( dbMetaMock.getName() ).thenReturn( TEST_NAME_OF_DB_CONNECTION );
      when( resultSetMock.next() ).thenReturn( true, false );
      when( resultSetMock.getString( "TABLE_NAME" ) ).thenThrow( SQL_EXCEPTION );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( resultSetMock );
      Database db = new Database( mockLogger(), dbMetaMock );
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
    KettleDatabaseException kettleDatabaseException = new KettleDatabaseException( "Unable to get database meta-data from the database." );
    try {
      when( resultSetMock.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( resultSetMock );
      Database db = new Database( mockLogger(), dbMetaMock );
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
    KettleDatabaseException kettleDatabaseException = new KettleDatabaseException( "Unable to get table-names from the database meta-data.", SQL_EXCEPTION );
    try {
      when( resultSetMock.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenThrow( SQL_EXCEPTION );
      Database db = new Database( mockLogger(), dbMetaMock );
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
    KettleDatabaseException kettleDatabaseException = new KettleDatabaseException( "Unable to get table-names from the database meta-data." );
    try {
      when( resultSetMock.next() ).thenReturn( true, false );
      when( dbMetaDataMock.getTables( any(), anyString(), anyString(), aryEq( TABLE_TYPES_TO_GET ) ) ).thenReturn( null );
      Database db = new Database( mockLogger(), dbMetaMock );
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

    ResultSet rs = mock( ResultSet.class );
    when( rs.getMetaData() ).thenReturn( rsMeta );

    PreparedStatement ps = mock( PreparedStatement.class );
    when( ps.executeQuery() ).thenReturn( rs );

    DatabaseMeta meta = new DatabaseMeta();
    meta.setDatabaseInterface( new MySQLDatabaseMeta() );

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );

    Database db = new Database( log, meta );
    db.setConnection( mockConnection( dbMetaData ) );

    db.getLookup( ps, false );

    RowMetaInterface rowMeta = db.getReturnRowMeta();
    assertEquals( 1, rowMeta.size() );

    ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
    assertEquals( ValueMetaInterface.TYPE_BINARY, valueMeta.getType() );
  }

  private static String concatWordsForRegexp( String... words ) {
    String emptySpace = "\\s*";
    StringBuilder sb = new StringBuilder( emptySpace );
    for ( String word : words ) {
      sb.append( word ).append( emptySpace );
    }
    return sb.toString();
  }

  private static LoggingObjectInterface mockLogger() {
    LoggingObjectInterface logger = mock( LoggingObjectInterface.class );
    when( logger.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    return logger;
  }

  private static Connection mockConnection( DatabaseMetaData dbMetaData ) throws SQLException {
    Connection connection = mock( Connection.class );
    when( connection.getMetaData() ).thenReturn( dbMetaData );
    return connection;
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

    Database db = new Database( log, meta );

    final DataSourceProviderInterface existing =
      DataSourceProviderFactory.getDataSourceProviderInterface();
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
  public void testDisconnectPstmCloseFail()
    throws SQLException, KettleDatabaseException, NoSuchFieldException, IllegalAccessException {

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );

    Database db = new Database( mockLogger(), dbMeta );
    Connection connection = mockConnection( dbMetaData );
    db.setConnection( connection );
    db.setCommit( 1 );

    PreparedStatement ps = mock( PreparedStatement.class );

    Class<Database> databaseClass = Database.class;
    Field fieldPstmt = databaseClass.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );

    Mockito.doThrow( new SQLException( "Test SQL exception" ) ).when( ps ).close();

    db.disconnect();
    verify( connection, times( 1 ) ).close();

  }


  @Test
  public void testDisconnectCommitFail() throws SQLException, NoSuchFieldException, IllegalAccessException {

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    when( dbMeta.supportsEmptyTransactions() ).thenReturn( true );

    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );
    when( dbMetaData.supportsTransactions() ).thenReturn( true );

    Database db = new Database( mockLogger(), dbMeta );
    Connection connection = mockConnection( dbMetaData );
    db.setConnection( connection );
    db.setCommit( 1 );

    PreparedStatement ps = mock( PreparedStatement.class );

    Class<Database> databaseClass = Database.class;
    Field fieldPstmt = databaseClass.getDeclaredField( "pstmt" );
    fieldPstmt.setAccessible( true );
    fieldPstmt.set( db, ps );

    Mockito.doThrow( new SQLException( "Test SQL exception" ) ).when( connection ).commit();

    db.disconnect();

    verify( connection, times( 1 ) ).close();

  }


  @Test
  public void testDisconnectConnectionGroup() throws SQLException {

    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    DatabaseMetaData dbMetaData = mock( DatabaseMetaData.class );

    Database db = new Database( mockLogger(), dbMeta );
    Connection connection = mockConnection( dbMetaData );
    db.setConnection( connection );

    db.setConnectionGroup( "1" );
    db.disconnect();

    verify( connection, never() ).close();
  }

}
