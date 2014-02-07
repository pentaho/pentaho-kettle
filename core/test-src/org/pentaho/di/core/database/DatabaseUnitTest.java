package org.pentaho.di.core.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleDatabaseException;
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
    Mockito.when( meta.getQuotedSchemaTableCombination( Mockito.anyString(),
        Mockito.anyString() ) ).thenReturn( "a" );
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
    String[] name = new String[]{ "a" };
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
}
