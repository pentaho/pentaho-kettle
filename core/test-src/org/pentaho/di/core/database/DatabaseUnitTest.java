package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;

public class DatabaseUnitTest {

  static LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );

  /**
   * PDI-11363. when using getLookup calls there is no need to make attempt to retrieve row set metadata for every call.
   * That may bring performance penalty depends on jdbc driver implementation. For some drivers that penalty can be huge
   * (postgres).
   * 
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
    assertNotNull( Database.createKettleDatabaseBatchException( "", new BatchUpdateException( new int[0] ) )
        .getUpdateCounts() );
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
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertRow( ps, true, true );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testInsertRowWithoutBatchDoesntThrowKettleBatchException() throws KettleDatabaseException, SQLException {
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeUpdate() ).thenThrow( new SQLException() );

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
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
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.emptyAndCommit( ps, true, 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testEmptyAndCommitWithoutBatchDoesntThrowKettleBatchException() throws KettleDatabaseException, SQLException {
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
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
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    when( ps.executeBatch() ).thenThrow( new SQLException() );

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
    database.setCommit( 1 );
    database.setConnection( mockConnection );
    database.insertFinished( ps, true );
  }

  @SuppressWarnings( "deprecation" )
  @Test( expected = KettleDatabaseException.class )
  public void testInsertFinishedWithoutBatchDoesntThrowKettleBatchException() throws KettleDatabaseException, SQLException {
    LoggingObjectInterface mockLoggingObjectInterface = mock( LoggingObjectInterface.class );
    DatabaseMeta mockDatabaseMeta = mock( DatabaseMeta.class );
    Connection mockConnection = mock( Connection.class );
    DatabaseMetaData mockDatabaseMetaData = mock( DatabaseMetaData.class );
    PreparedStatement ps = mock( PreparedStatement.class );

    when( mockLoggingObjectInterface.getLogLevel() ).thenReturn( LogLevel.NOTHING );
    when( mockDatabaseMeta.supportsBatchUpdates() ).thenReturn( true );
    when( mockConnection.getMetaData() ).thenReturn( mockDatabaseMetaData );
    when( mockDatabaseMetaData.supportsBatchUpdates() ).thenReturn( true );
    doThrow( new SQLException() ).when( ps ).close();

    Database database = new Database( mockLoggingObjectInterface, mockDatabaseMeta );
    database.setConnection( mockConnection );
    try {
      database.insertFinished( ps, true );
    } catch ( KettleDatabaseBatchException e ) {
      // noop
    }
  }
}
