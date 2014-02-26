/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyString;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.plugins.DatabasePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

public class DatabaseOneTest {

  public abstract static class AbstractDriver implements Driver {
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

  public static class RightDriver extends AbstractDriver {
    private static final Driver instance = new RightDriver();
    static {
      try {
        DriverManager.registerDriver( instance );
      } catch ( SQLException e ) {
        throw new RuntimeException( "Failed registering driver: " + RightDriver.class, e );
      }
    }

    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
      if ( acceptsURL( url ) ) {
        boolean ok = url.startsWith( JDBC_URL_SUCCESS );
        ok = ok && ( !info.containsKey( "user" ) || USER_NAME.equals( info.getProperty( "user" ) ) );
        ok = ok && ( !info.containsKey( "password" ) || USER_PASS.equals( info.getProperty( "password" ) ) );

        ok = ok && ( !url.contains( "user:" ) || url.contains( USER_NAME ) );
        ok = ok && ( !url.contains( "password:" ) || url.contains( USER_PASS ) );

        if ( ok ) {
          return mock( Connection.class );
        } else {
          throw new SQLException( BAD_CRED );
        }
      }

      return null;
    }

    @Override
    public boolean acceptsURL( String url ) throws SQLException {
      return true;
    }
  }

  public static class WrongDriver extends AbstractDriver {
    private static final Driver instance = new WrongDriver();
    static {
      try {
        DriverManager.registerDriver( instance );
      } catch ( SQLException e ) {
        throw new RuntimeException( "Failed registering driver: " + WrongDriver.class, e );
      }
    }

    @Override
    public Connection connect( String url, Properties info ) throws SQLException {
      if ( acceptsURL( url ) ) {
        return mock( Connection.class );
      }

      throw new IllegalArgumentException( "Can't understand URL:" + url );
    }

    @Override
    public boolean acceptsURL( String url ) throws SQLException {
      return false;
    }
  }

  private Database db1;
  private static DatabaseInterface dbface1, dbface2;

  private static final String JDBC_URL_FAIL = "jdbc:url:fail";
  private static final String JDBC_URL_SUCCESS = "jdbc:url:success";
  private static final String BAD_CRED = "bad-credentials";
  private static final String USER_NAME = "user-name";
  private static final String USER_PASS = "user-password";

  @BeforeClass
  public static void setupBeforeClass() throws Exception {
    PluginRegistry.init();

    PluginRegistry.getInstance().registerPluginType( DatabasePluginType.class );
    dbface1 = registerPlugin( "dumb1", false );
    dbface2 = registerPlugin( "dumb2", true );
  }

  private static DatabaseInterface registerPlugin( String pluginId, boolean separateClassLoader ) throws Exception {
    final DatabaseInterface dbi = mock( DatabaseInterface.class );

    PluginInterface pi = mock( PluginInterface.class );
    when( pi.getPluginType() ).then(
      new Answer<Class<? extends PluginTypeInterface>>() {
        @Override
        public Class<? extends DatabasePluginType> answer( InvocationOnMock invocation ) throws Throwable {
          return DatabasePluginType.class;
        }
      } );
    when( pi.getIds() ).thenReturn( new String[] { pluginId } );
    when( pi.getMainType() ).then( new Answer<Class<? extends DatabaseInterface>>() {
      @Override
      public Class<? extends DatabaseInterface> answer( InvocationOnMock invocation ) throws Throwable {
        return dbi.getClass();
      }
    } );
    when( pi.getClassMap() ).then( new Answer<Map<Class<?>, String>>() {
      @Override
      public Map<Class<?>, String> answer( InvocationOnMock invocation ) throws Throwable {
        Map<Class<?>, String> m = new HashMap<Class<?>, String>();
        m.put( dbi.getClass(), dbi.getClass().getName() );
        return m;
      }
    } );
    when( pi.matches( pluginId ) ).thenReturn( true );
    when( pi.getName() ).thenReturn( pluginId );
    when( pi.isSeparateClassLoaderNeeded() ).thenReturn( separateClassLoader );

    URL url = DatabaseOneTest.class
        .getClassLoader().getResource( "org/pentaho/di/core/database/DatabaseOneTest.class" );
    File f = new File( url.getPath() );
    File p = f.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
    String root = p.getAbsolutePath();
    when( pi.getLibraries() ).thenReturn( Arrays.asList( new String[] { root } ) );

    PluginRegistry.getInstance().registerPlugin( DatabasePluginType.class, pi );
    return dbi;
  }

  private DatabaseMeta createMeta( DatabaseInterface dbface,
      Class<? extends Driver> driver, String url,
      String user, String pass, Properties props,
      boolean opts ) throws Exception {
    DatabaseMeta meta = mock( DatabaseMeta.class, RETURNS_MOCKS );
    when( meta.getDatabaseInterface() ).thenReturn( dbface );
    when( meta.getDriverClass() ).thenReturn( driver.getName() );
    when( meta.getURL( anyString() ) ).thenReturn( url );
    when( meta.getURL( ) ).thenReturn( url );
    when( meta.environmentSubstitute( anyString() ) ).then( returnsFirstArg() );
    when( meta.getConnectionProperties() ).thenReturn( props != null ? props : new Properties() );
    when( meta.supportsOptionsInURL() ).thenReturn( opts );

    if ( user != null ) {
      when( meta.getUsername() ).thenReturn( user );
    }

    if ( pass != null ) {
      when( meta.getPassword() ).thenReturn( pass );
    }

    return meta;
  }

  /* 
   * This is a test for the fix of PDI-11261.
   * it checks that correct exception is thrown when connection failed
   * even though bad driver is registered in DriverManager 
   */
  @Test
  public void testFailedConnect() throws Exception {
    Class.forName( WrongDriver.class.getName() );
    db1 = new Database( null, createMeta( dbface1, RightDriver.class, JDBC_URL_FAIL, null, null, null, true ) );

    try {
      db1.normalConnect( null );
    } catch ( KettleDatabaseException kde ) {
      checkStackTrace( kde, BAD_CRED, "(PDI-11261). Stack trace does not contain expected exception message:" );
    }
  }

  @Test
  public void testSuccessConnectWithoutDelegatingDriver() throws Exception {
    Class.forName( WrongDriver.class.getName() );

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, null, null, null, false ) );
    db1.normalConnect( null );
    assertNotNull( "Connection shouldn't be null for Database (simple connect) ", db1.getConnection() );

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, USER_NAME, USER_PASS, null, false ) );
    db1.normalConnect( null );
    assertNotNull( "Connection shouldn't be null for Database (user/password connect) ", db1.getConnection() );

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, USER_NAME, null, null, false ) );
    db1.normalConnect( null );
    assertNotNull( "Connection shouldn't be null for Database (user connect) ", db1.getConnection() );

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, USER_NAME + "_", USER_PASS, null, false ) );
    try {
      db1.normalConnect( null );
    } catch ( KettleDatabaseException kde ) {
      checkStackTrace( kde, BAD_CRED, "(Bad user name). Stack trace does not contain expected exception message:" );
    }

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, USER_NAME, USER_PASS + "_", null, false ) );
    try {
      db1.normalConnect( null );
    } catch ( KettleDatabaseException kde ) {
      checkStackTrace( kde, BAD_CRED, "(Bad password). Stack trace does not contain expected exception message:" );
    }

    db1 = new Database( null,
        createMeta( dbface1, RightDriver.class, JDBC_URL_SUCCESS, USER_NAME, USER_PASS, null, true ) );
    db1.normalConnect( null );
    assertNotNull( "Connection shouldn't be null for Database (user/pass/properties connect).  ", db1.getConnection() );

    Properties props = new Properties();
    props.setProperty( "user", USER_NAME );
    props.setProperty( "pass", USER_PASS );

  }

  @Test
  public void testSuccessConnectWithDelegatingDriver() throws Exception {
    Class.forName( WrongDriver.class.getName() );
    db1 = new Database( null,
        createMeta( dbface2, SeparateDriver.class, JDBC_URL_SUCCESS, null, null, null, false ) );
    db1.normalConnect( null );
    assertNotNull( "Connection shouldn't be null for Database (simple connect) ", db1.getConnection() );
  }

  private static void checkStackTrace( Throwable ex, String str, String message ) {
    Throwable cause = ex;
    while ( cause != null ) {
      if ( str.equals( cause.getMessage() ) ) {
        break;
      }
      cause = cause.getCause();
    }

    assertNotNull( message + str, cause );
  }

}
