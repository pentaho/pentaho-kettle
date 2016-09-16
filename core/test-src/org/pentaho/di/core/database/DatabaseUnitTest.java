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
import static org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType.JNDI;
import static org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType.POOLED;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;

import javax.sql.DataSource;

public class DatabaseUnitTest {

  static LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );

  @BeforeClass
  public static void setUp() throws Exception {
    KettleClientEnvironment.init();
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

    db.getLookup( ps, false, true );

    RowMetaInterface rowMeta = db.getReturnRowMeta();
    assertEquals( 1, rowMeta.size() );

    ValueMetaInterface valueMeta = rowMeta.getValueMeta( 0 );
    assertEquals( ValueMetaInterface.TYPE_STRING, valueMeta.getType() );
    assertEquals( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING, valueMeta.getStorageType() );
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
    verify( provider ).getNamedDataSource( anyString(), eq( JNDI ) );
    verify( provider, never() ).getNamedDataSource( anyString(), eq( POOLED ) );
  }
}
