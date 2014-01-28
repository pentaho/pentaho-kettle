package org.pentaho.di.job.entries.evaluatetablecontent;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.mockito.stubbing.Answer;

public class MockDriver implements Driver {
  private static final List<MockDriver> drivers = new ArrayList<MockDriver>();

  public static synchronized void registerInstance() throws SQLException {
    MockDriver driver = new MockDriver();
    DriverManager.registerDriver( driver );
    drivers.add( driver );
  }

  public static synchronized void deregeisterInstances() throws SQLException {
    for ( Driver driver : drivers ) {
      DriverManager.deregisterDriver( driver );
    }
    drivers.clear();
  }

  public MockDriver() {

  }

  @Override
  public boolean acceptsURL( String url ) throws SQLException {
    return true;
  }

  @Override
  public Connection connect( String url, Properties info ) throws SQLException {
    Connection conn = mock( Connection.class );
    Statement stmt = mock( Statement.class );
    ResultSet rs = mock( ResultSet.class );
    ResultSetMetaData md = mock( ResultSetMetaData.class );

    when( stmt.getMaxRows() ).thenReturn( 5 );
    when( stmt.getResultSet() ).thenReturn( rs );
    when( stmt.executeQuery( anyString() ) ).thenReturn( rs );

    when( rs.getMetaData() ).thenReturn( md );
    when( rs.getLong( anyInt() ) ).thenReturn( 5L );
    when( rs.next() ).thenAnswer( new Answer<Boolean>() {
      private int count = 0;

      public Boolean answer( org.mockito.invocation.InvocationOnMock invocation ) throws Throwable {
        return count++ == 0;
      }
    } );

    when( md.getColumnCount() ).thenReturn( 1 );
    when( md.getColumnName( anyInt() ) ).thenReturn( "count" );
    when( md.getColumnType( anyInt() ) ).thenReturn( java.sql.Types.INTEGER );

    when( conn.createStatement() ).thenReturn( stmt );

    return conn;
  }

  @Override
  public int getMajorVersion() {
    return 0;
  }

  @Override
  public int getMinorVersion() {
    return 0;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean jdbcCompliant() {
    // TODO Auto-generated method stub
    return false;
  }
}
