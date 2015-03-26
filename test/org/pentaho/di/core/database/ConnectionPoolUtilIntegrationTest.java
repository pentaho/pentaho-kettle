/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;

public class ConnectionPoolUtilIntegrationTest {

  private static final String PASSWORD = "manager";
  private static Class<?> PKG = Database.class;
  private static final int INITIAL_POOL_SIZE = 1;
  Driver driver;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Before
  public void setUp() throws Exception {
    driver = mock( Driver.class, RETURNS_MOCKS );
    DriverManager.registerDriver( driver );
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

  private void testGetConnectionFromPool( final int threadCount ) throws Exception {
    final LogChannelInterface logChannelInterface = mock( LogChannelInterface.class, RETURNS_MOCKS );
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
        return ConnectionPoolUtil.getConnection( logChannelInterface, dbMeta, "", INITIAL_POOL_SIZE, threadCount );
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
