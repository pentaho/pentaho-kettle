/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.database.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.database.ConnectionPoolUtil;
import org.pentaho.di.core.database.DataSourceNamingException;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.database.DataSourceProviderInterface.DatasourceType;

public class DatabaseUtilTest {
  private Context context;
  private String testName;

  @Before
  public void setup() {
    DatabaseUtil.clearDSCache();
    context = mock( Context.class );
    testName = "testName";
  }

  @After
  public void tearDown() {
    DatabaseUtil.clearDSCache();
  }

  @Test( expected = NamingException.class )
  public void testNullName() throws NamingException {
    DatabaseUtil.getDataSourceFromJndi( null, context );
  }

  @Test( expected = NamingException.class )
  public void testEmptyName() throws NamingException {
    DatabaseUtil.getDataSourceFromJndi( "", context );
  }

  @Test( expected = NamingException.class )
  public void testWrongType() throws NamingException {
    when( context.lookup( anyString() ) ).thenReturn( new Object() );
    DatabaseUtil.getDataSourceFromJndi( testName, context );
  }

  @Test( expected = NamingException.class )
  public void testNotFound() throws NamingException {
    when( context.lookup( anyString() ) ).thenThrow( new NamingException() );
    try {
      DatabaseUtil.getDataSourceFromJndi( testName, context );
    } catch ( NamingException ne ) {
      verify( context.lookup( testName ) );
      verify( context.lookup( "java:" + testName ) );
      verify( context.lookup( "java:comp/env/jdbc/" + testName ) );
      verify( context.lookup( "jdbc/" + testName ) );
      throw ne;
    }
  }

  @Test
  public void testCl() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( testName ) ).thenReturn( dataSource );
    DatabaseUtil util = new DatabaseUtil();
    ClassLoader orig = Thread.currentThread().getContextClassLoader();
    ClassLoader cl = mock( ClassLoader.class );
    try {
      Thread.currentThread().setContextClassLoader( cl );
      util.getNamedDataSource( testName );
    } catch ( Exception ex ) {
    } finally {
      try {
        verify( cl, never() ).loadClass( anyString() );
        verify( cl, never() ).getResource( anyString() );
        verify( cl, never() ).getResourceAsStream( anyString() );
      } catch ( Exception ex ) {
      }
      Thread.currentThread().setContextClassLoader( orig );
    }
  }

  @Test
  public void testNormal() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( testName ) ).thenReturn( dataSource );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
  }

  @Test
  public void testJBoss() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( "java:" + testName ) ).thenReturn( dataSource );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
  }

  @Test
  public void testTomcat() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( "java:comp/env/jdbc/" + testName ) ).thenReturn( dataSource );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
  }

  @Test
  public void testOther() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( "jdbc/" + testName ) ).thenReturn( dataSource );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
  }

  @Test
  public void testCaching() throws NamingException {
    DataSource dataSource = mock( DataSource.class );
    when( context.lookup( testName ) ).thenReturn( dataSource ).thenThrow( new NullPointerException() );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
    assertEquals( dataSource, DatabaseUtil.getDataSourceFromJndi( testName, context ) );
  }

  @Test
  public void testInvalidateNamedDataSource() throws KettleDatabaseException, NamingException {
    DatabaseUtil dsp = new DatabaseUtil();
    String namedDatasource = UUID.randomUUID().toString();
    DataSource dataSource = mock( DataSource.class );

    assertEquals( null, dsp.invalidateNamedDataSource( namedDatasource, DatasourceType.JNDI ) );
    when( context.lookup( namedDatasource ) ).thenReturn( dataSource );
    DatabaseUtil.getDataSourceFromJndi( namedDatasource, context );
    assertEquals( dataSource, dsp.invalidateNamedDataSource( namedDatasource, DatasourceType.JNDI ) );
    assertEquals( null, dsp.invalidateNamedDataSource( namedDatasource, DatasourceType.JNDI ) );
  }

}
