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

import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.junit.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

public class DatabaseConnectUnitTest {

  static LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );
  String name = "testName";
  String jndiName = "testJNDIName";
  String fullJndiName = "jdbc/testJNDIName";
  String displayName = "testDisplayName";

  @BeforeClass
  public static void beforeClass() throws NamingException {
    if ( !NamingManager.hasInitialContextFactoryBuilder() ) {
      // If JNDI is not initialized, use simpleJNDI
      System.setProperty( Context.INITIAL_CONTEXT_FACTORY, "org.osjava.sj.memory.MemoryContextFactory" ); // pentaho#simple-jndi;1.0.0
      System.setProperty( "org.osjava.sj.jndi.shared", "true" );
      InitialContextFactoryBuilder simpleBuilder = new SimpleNamingContextBuilder();
      NamingManager.setInitialContextFactoryBuilder( simpleBuilder );
    }
  }

  @After
  public void after() throws NamingException {
    InitialContext ctx = new InitialContext();
    ctx.unbind( fullJndiName );
  }

  @Test
  public void testConnect() throws SQLException, NamingException, KettleDatabaseException {
    InitialContext ctx = new InitialContext();

    DatabaseMeta meta = Mockito.mock( DatabaseMeta.class );
    Mockito.when( meta.getName() ).thenReturn( name );
    Mockito.when( meta.getDatabaseName() ).thenReturn( jndiName );
    Mockito.when( meta.getDisplayName() ).thenReturn( displayName );
    Mockito.when( meta.getAccessType() ).thenReturn( DatabaseMeta.TYPE_ACCESS_JNDI );
    Mockito.when( meta.environmentSubstitute( jndiName ) ).thenReturn( jndiName );

    Connection connection = Mockito.mock( Connection.class );

    DataSource ds = Mockito.mock( DataSource.class );
    Mockito.when( ds.getConnection() ).thenReturn( connection );

    ctx.bind( fullJndiName, ds );

    Database db = new Database( log, meta );

    db.connect();
    Assert.assertEquals( connection, db.getConnection() );
  }

}
