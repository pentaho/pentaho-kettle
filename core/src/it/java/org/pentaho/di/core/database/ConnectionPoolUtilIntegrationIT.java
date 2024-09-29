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

import org.apache.commons.dbcp2.DelegatingConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.database.util.DatabaseUtil;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_MOCKS;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConnectionPoolUtilIntegrationIT {

  private static final String PASSWORD = "manager";
  private static Class<?> PKG = Database.class;
  Driver driver;

  private static long MAX_WAIT_TIME = 1000;
  private static int MAX_ACTIVE = 2;
  private static String VALIDATION_QUERY = "select 1 from INFORMATION_SCHEMA.USERS";

  private LogChannelInterface logChannelInterface;
  Properties dsProps;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    driver = mock( Driver.class, RETURNS_MOCKS );
    DriverManager.registerDriver( driver );

    logChannelInterface = mock( LogChannelInterface.class, RETURNS_MOCKS );
    dsProps = new Properties();
    dsProps.setProperty( ConnectionPoolUtil.DEFAULT_AUTO_COMMIT, "true" );
    dsProps.setProperty( ConnectionPoolUtil.DEFAULT_READ_ONLY, "true" );
    dsProps.setProperty( ConnectionPoolUtil.DEFAULT_TRANSACTION_ISOLATION, "1" );
    dsProps.setProperty( ConnectionPoolUtil.DEFAULT_CATALOG, "" );
    dsProps.setProperty( ConnectionPoolUtil.MAX_IDLE, "30" );
    dsProps.setProperty( ConnectionPoolUtil.MIN_IDLE, "3" );
    dsProps.setProperty( ConnectionPoolUtil.MAX_WAIT, String.valueOf( MAX_WAIT_TIME ) ); // tested
    dsProps.setProperty( ConnectionPoolUtil.VALIDATION_QUERY, VALIDATION_QUERY );
    dsProps.setProperty( ConnectionPoolUtil.TEST_ON_BORROW, "true" );
    dsProps.setProperty( ConnectionPoolUtil.TEST_ON_RETURN, "true" );
    dsProps.setProperty( ConnectionPoolUtil.TEST_WHILE_IDLE, "true" );
    dsProps.setProperty( ConnectionPoolUtil.TIME_BETWEEN_EVICTION_RUNS_MILLIS, "300000" );
    dsProps.setProperty( ConnectionPoolUtil.POOL_PREPARED_STATEMENTS, "true" ); // tested
    dsProps.setProperty( ConnectionPoolUtil.MAX_OPEN_PREPARED_STATEMENTS, "2" ); // tested
    dsProps.setProperty( ConnectionPoolUtil.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED, "true" ); // tested
    dsProps.setProperty( ConnectionPoolUtil.REMOVE_ABANDONED, "false" );
    dsProps.setProperty( ConnectionPoolUtil.REMOVE_ABANDONED_TIMEOUT, "1000" );
    dsProps.setProperty( ConnectionPoolUtil.LOG_ABANDONED, "false" );
    dsProps.setProperty( ConnectionPoolUtil.MAX_ACTIVE, String.valueOf( MAX_ACTIVE ) );
  }

  @After
  public void tearDown() throws Exception {
    DriverManager.deregisterDriver( driver );
  }

  @Test
  public void testGet_01_ConnectionFromPool() throws Exception {
    testGetConnectionFromPool( 1 );
  }

  @Test
  public void testGet_02_ConnectionFromPool() throws Exception {
    testGetConnectionFromPool( 2 );
  }

  @Test
  public void testGet_04_ConnectionFromPool() throws Exception {
    testGetConnectionFromPool( 4 );
  }

  @Test
  public void testGet_08_ConnectionFromPool() throws Exception {
    testGetConnectionFromPool( 8 );
  }

  @Test
  public void testPreparedStatementsProperty() throws Exception {
    Connection conn = null;
    PreparedStatement[] ps = new PreparedStatement[ 3 ];
    try {
      DatabaseMeta dbMeta = new DatabaseMeta( "testPreparedStatements", "H2", "JDBC", null, "mem:test", null, "SA", "" );
      dbMeta.setConnectionPoolingProperties( dsProps );
      conn = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "part1" );
      ps[ 0 ] = conn.prepareStatement( VALIDATION_QUERY );
      ps[ 1 ] = conn.prepareStatement( VALIDATION_QUERY );
      boolean failed = false;
      try {
        ps[ 2 ] = conn.prepareStatement( VALIDATION_QUERY );
      } catch ( Exception e ) {
        failed = true;
      }
      assertTrue( "Properties 'poolPreparedStatements' or 'maxOpenPreparedStatements' don't work", failed );
    } finally {
      DatabaseUtil.closeSilently( ps );
      DatabaseUtil.closeSilently( conn );
    }
  }

  // maxPoolSize is set to "2", maxWait = MAX_WAIT_TIME
  // so after getting the next connection the thread should block for 3 seconds and then
  // throw "Cannot get connection exception"
  @Test( timeout = 6000 )
  public void testMaxActiveProperty() throws Exception {
    Connection[] c = new Connection[ 3 ];
    DatabaseMeta dbMeta = new DatabaseMeta( "testPreparedStatements", "H2", "JDBC", null, "mem:test", null, "SA", "" );
    dbMeta.setConnectionPoolingProperties( dsProps );
    try {
      c[ 0 ] = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "part1" );
      c[ 1 ] = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "part1" );
      long startTime = System.currentTimeMillis();
      try {
        // this must wait a bit and throw an exception
        c[ 2 ] = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "part1" );
      } catch ( SQLException e ) {
        long waitedTime = System.currentTimeMillis() - startTime;
        assertFalse( "Waited < maxWait", waitedTime < MAX_WAIT_TIME );
      }
    } finally {
      DatabaseUtil.closeSilently( c );
    }
  }

  @Test
  public void testAccessToUnderlyingConnectionAllowedProperty() throws Exception {
    DatabaseMeta dbMeta = new DatabaseMeta( "testAccessToUnderlying", "H2", "JDBC", null, "mem:test", null, "SA", "" );
    dbMeta.setConnectionPoolingProperties( dsProps );
    Connection conn = ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "part1" );
    Connection dconn = ( (DelegatingConnection) conn ).getInnermostDelegate();
    assertNotNull( "Property 'accessToUnderlyingConnectionAllowed' doesn't work", dconn );
  }

  private void testGetConnectionFromPool( final int threadCount ) throws Exception {
    ArgumentCaptor<String> captorLogMessage = ArgumentCaptor.forClass( String.class );

    final DatabaseMeta dbMeta = mock( DatabaseMeta.class, RETURNS_MOCKS );
    when( dbMeta.getDriverClass() ).thenReturn( driver.getClass().getCanonicalName() );
    when( dbMeta.getConnectionPoolingProperties() ).thenReturn( new Properties() );
    when( dbMeta.environmentSubstitute( anyString() ) ).thenAnswer( new Answer<Object>() {
      @Override
      public Object answer( InvocationOnMock invocation ) throws Throwable {
        return invocation.getArguments()[0];
      }
    } );
    when( dbMeta.getName() ).thenReturn( "CP1" );
    when( dbMeta.getPassword() ).thenReturn( PASSWORD );

    Callable<Connection> task = new Callable<Connection>() {
      @Override
      public Connection call() throws Exception {
        return ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "" );
      }
    };
    List<Callable<Connection>> tasks = Collections.nCopies( threadCount, task );
    ExecutorService executorService = Executors.newFixedThreadPool( threadCount );
    List<Future<Connection>> futures = executorService.invokeAll( tasks );

    assertNotNull( futures );
    assertEquals( threadCount, futures.size() );

    //pool should be creates only once for KettleClientEnvironment
    verify( logChannelInterface, atMost( 2 ) ).logBasic( captorLogMessage.capture() );
    List<String> capturedLogEntry = captorLogMessage.getAllValues();
    if ( capturedLogEntry != null && !capturedLogEntry.isEmpty() ) {
      assertEquals( BaseMessages.getString( PKG, "Database.CreatingConnectionPool", dbMeta.getName() ), capturedLogEntry.get( 0 ) );
      assertEquals( BaseMessages.getString( PKG, "Database.CreatedConnectionPool", dbMeta.getName() ), capturedLogEntry.get( 1 ) );
    }
  }
}
