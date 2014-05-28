package org.pentaho.di.core.database;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


/**
 * User: Dzmitry Stsiapanau Date: 12/11/13 Time: 1:59 PM
 */
public class ConnectionPoolUtilTest implements Driver {
  private static final String PASSWORD = "manager";
  private static final String ENCR_PASSWORD = "Encrypted 2be98afc86aa7f2e4cb14af7edf95aac8";
  LogChannelInterface logChannelInterface;
  DatabaseMeta dbMeta;

  public ConnectionPoolUtilTest() {
    try {
      DriverManager.registerDriver( this );
    } catch ( SQLException e ) {
      e.printStackTrace();
    }
  }

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    logChannelInterface = mock( LogChannelInterface.class, RETURNS_MOCKS );
    dbMeta = mock( DatabaseMeta.class, RETURNS_MOCKS );
    when( dbMeta.getDriverClass() ).thenReturn( this.getClass().getCanonicalName() );
    when( dbMeta.getConnectionPoolingProperties() ).thenReturn( new Properties() );
    when( dbMeta.environmentSubstitute( anyString() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[0];
      }
    } );
  }

  @After
  public void tearDown() throws Exception {
    DriverManager.deregisterDriver( this );
  }

  @Test
  public void testGetConnection() throws Exception {
    when( dbMeta.getName() ).thenReturn( "CP1" );
    when( dbMeta.getPassword() ).thenReturn( PASSWORD );
    Connection conn = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "", 1, 2 );
    assertTrue( conn != null );
  }

  @Test
  public void testGetConnectionEncrypted() throws Exception {
    when( dbMeta.getName() ).thenReturn( "CP2" );
    when( dbMeta.getPassword() ).thenReturn( ENCR_PASSWORD );
    Connection conn = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "", 1, 2 );
    assertTrue( conn != null );
  }

  @Override
  public Connection connect( String url, Properties info ) throws SQLException {
    String password = info.getProperty( "password" );
    return PASSWORD.equals( password ) ? mock( Connection.class ) : null;
  }

  @Override
  public boolean acceptsURL( String url ) throws SQLException {
    return true;
  }

  @Override
  public DriverPropertyInfo[] getPropertyInfo( String url, Properties info ) throws SQLException {
    return null;
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
  public boolean jdbcCompliant() {
    return false;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null;
  }
}
