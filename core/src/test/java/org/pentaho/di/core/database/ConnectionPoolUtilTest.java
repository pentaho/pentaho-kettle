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

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseException;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


/**
 * User: Dzmitry Stsiapanau Date: 12/11/13 Time: 1:59 PM
 */
@RunWith( MockitoJUnitRunner.class )
public class ConnectionPoolUtilTest implements Driver {
  private static final String PASSWORD = "manager";
  private static final String ENCR_PASSWORD = "Encrypted 2be98afc86aa7f2e4cb14af7edf95aac8";
  @Mock( answer = Answers.RETURNS_MOCKS ) LogChannelInterface logChannelInterface;
  @Mock( answer = Answers.RETURNS_MOCKS ) DatabaseMeta dbMeta;
  @Mock BasicDataSource dataSource;
  final int INITIAL_SIZE = 1;
  final int MAX_SIZE = 10;


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
    when( dbMeta.getDriverClass() ).thenReturn( this.getClass().getCanonicalName() );
    when( dbMeta.getConnectionPoolingProperties() ).thenReturn( new Properties() );
    when( dbMeta.environmentSubstitute( anyString() ) ).thenAnswer(
      invocation -> invocation.getArguments()[0] );
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

  @Test
  public void testGetConnectionName() throws Exception {
    when( dbMeta.getName() ).thenReturn( "CP2" );
    when( dbMeta.getPassword() ).thenReturn( ENCR_PASSWORD );
    String connectionName = ConnectionPoolUtil.buildPoolName( dbMeta, "" );
    assertTrue( connectionName.equals( "CP2" ) );
    assertFalse( connectionName.equals( "CP2pentaho" ) );

    when( dbMeta.getDatabaseName() ).thenReturn( "pentaho" );
    connectionName = ConnectionPoolUtil.buildPoolName( dbMeta, "" );
    assertTrue( connectionName.equals( "CP2pentaho" ) );
    assertFalse( connectionName.equals( "CP2pentaholocal" ) );

    when( dbMeta.getHostname() ).thenReturn( "local" );
    connectionName = ConnectionPoolUtil.buildPoolName( dbMeta, "" );
    assertTrue( connectionName.equals( "CP2pentaholocal" ) );
    assertFalse( connectionName.equals( "CP2pentaholocal3306" ) );

    when( dbMeta.getDatabasePortNumberString() ).thenReturn( "3306" );
    connectionName = ConnectionPoolUtil.buildPoolName( dbMeta, "" );
    assertTrue( connectionName.equals( "CP2pentaholocal3306" ) );
  }

  @Test
  public void testConfigureDataSource() throws KettleDatabaseException {
    when( dbMeta.getURL( "partId" ) ).thenReturn( "jdbc:foo://server:111" );
    when( dbMeta.getUsername() ).thenReturn( "suzy" );
    when( dbMeta.getPassword() ).thenReturn( "password" );

    ConnectionPoolUtil.configureDataSource(
      dataSource, dbMeta, "partId", INITIAL_SIZE, MAX_SIZE );

    verify( dataSource ).setDriverClassName( "org.pentaho.di.core.database.ConnectionPoolUtilTest" );
    verify( dataSource ).setDriverClassLoader( any( ClassLoader.class ) );
    verify( dataSource ).setUrl( "jdbc:foo://server:111" );
    verify( dataSource ).addConnectionProperty( "user", "suzy" );
    verify( dataSource ).addConnectionProperty( "password", "password" );
    verify( dataSource ).setInitialSize( INITIAL_SIZE );
    verify( dataSource ).setMaxActive( MAX_SIZE );
  }

  @Test
  public void testConfigureDataSourceWhenNoDatabaseInterface() throws KettleDatabaseException {
    when( dbMeta.getDatabaseInterface() ).thenReturn( null );
    ConnectionPoolUtil.configureDataSource(
      dataSource, dbMeta, "partId", INITIAL_SIZE, MAX_SIZE );
    verify( dataSource, never() ).setDriverClassLoader( any( ClassLoader.class ) );
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
