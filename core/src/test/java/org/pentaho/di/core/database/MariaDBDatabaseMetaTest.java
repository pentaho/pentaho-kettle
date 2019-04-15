/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.sql.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.pentaho.di.core.exception.KettleDatabaseException;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.*;

public class MariaDBDatabaseMetaTest extends MySQLDatabaseMetaTest {
  private DatabaseMetaData databaseMetaData;

  private ResultSetMetaData resultSetMetaData, resultSetMetaDataException;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    databaseMetaData = mock( DatabaseMetaData.class );

    resultSetMetaData = mock( ResultSetMetaData.class );

    given( resultSetMetaData.getColumnLabel( 1 ) ).willReturn( "NUMBER" );
    given( resultSetMetaData.getColumnLabel( 2 ) ).willReturn( "NAME" );
    given( resultSetMetaData.getColumnLabel( 3 ) ).willReturn( "LAST_NAME" );
    given( resultSetMetaData.getColumnLabel( 4 ) ).willReturn( "FIRST_NAME" );
    given( resultSetMetaData.getColumnLabel( 5 ) ).willReturn( "DB" );
    given( resultSetMetaData.getColumnLabel( 6 ) ).willReturn( "NoAliasText" );

    resultSetMetaDataException = mock( ResultSetMetaData.class );

    when( resultSetMetaDataException.getColumnLabel( 1 ) ).thenThrow( new SQLException() );
    when( resultSetMetaDataException.getColumnLabel( 2 ) ).thenThrow( new SQLException() );
    when( resultSetMetaDataException.getColumnLabel( 3 ) ).thenThrow( new SQLException() );
    when( resultSetMetaDataException.getColumnLabel( 4 ) ).thenThrow( new SQLException() );
    when( resultSetMetaDataException.getColumnLabel( 5 ) ).thenThrow( new SQLException() );
    when( resultSetMetaDataException.getColumnLabel( 6 ) ).thenThrow( new SQLException() );
  }

  @Test
  public void testGetLegacyColumnName() {
    MariaDBDatabaseMeta nativeMeta = new MariaDBDatabaseMeta();

    try {
      assertEquals( "NUMBER", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 1 ) );
      assertEquals( "NAME", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 2 ) );
      assertEquals( "LAST_NAME", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 3 ) );
      assertEquals( "FIRST_NAME", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 4 ) );
      assertEquals( "DB", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 5 ) );
      assertEquals( "NoAliasText", nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaData, 6 ) );
    }
    catch ( KettleDatabaseException kettleDatabaseException ) {

    }
    catch ( Exception e ) {

    }
  }

  @Test
  public void testGetLegacyColumnNameNullParametersException() throws Exception {
    MariaDBDatabaseMeta nativeMeta = new MariaDBDatabaseMeta();

    expectedException.expect( Exception.class );

    nativeMeta.getLegacyColumnName( null, null, 1 );
  }

  @Test
  public void testGetLegacyColumnNameDatabaseException() throws Exception {
    MariaDBDatabaseMeta nativeMeta = new MariaDBDatabaseMeta();

    expectedException.expect( KettleDatabaseException.class );

    nativeMeta.getLegacyColumnName( databaseMetaData, resultSetMetaDataException, 1 );
  }

  @Test
  public void testMysqlOverrides() {
    MariaDBDatabaseMeta nativeMeta = new MariaDBDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    MariaDBDatabaseMeta odbcMeta = new MariaDBDatabaseMeta();
    odbcMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_ODBC );

    assertArrayEquals( new String[] { "mariadb-java-client-1.4.6.jar" }, nativeMeta.getUsedLibraries() );
    assertEquals( 3306, nativeMeta.getDefaultDatabasePort() );
    assertEquals( -1, odbcMeta.getDefaultDatabasePort() );

    assertEquals( "org.mariadb.jdbc.Driver", nativeMeta.getDriverClass() );
    assertEquals( "sun.jdbc.odbc.JdbcOdbcDriver", odbcMeta.getDriverClass() );
    assertEquals( "jdbc:odbc:FOO", odbcMeta.getURL(  "IGNORED", "IGNORED", "FOO" ) );
    assertEquals( "jdbc:mariadb://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:mariadb://FOO/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) );

    // The fullExceptionLog method is covered by another test case.
  }
}
