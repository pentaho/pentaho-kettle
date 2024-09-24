/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/
package org.pentaho.di.core.database;

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Test;

import org.pentaho.di.core.exception.KettleDatabaseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.doThrow;
import static org.mockito.BDDMockito.mock;

public class MariaDBDatabaseMetaTest extends MySQLDatabaseMetaTest {
  /**
   *
   * @return
   * @throws Exception
   */
  private ResultSetMetaData getResultSetMetaData() throws Exception {
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );

    /**
     * Fields setup around the following query:
     *
     * select
     *   CUSTOMERNUMBER as NUMBER
     * , CUSTOMERNAME as NAME
     * , CONTACTLASTNAME as LAST_NAME
     * , CONTACTFIRSTNAME as FIRST_NAME
     * , 'MariaDB' as DB
     * , 'NoAliasText'
     * from CUSTOMERS
     * ORDER BY CUSTOMERNAME;
     */

    doReturn( "NUMBER" ).when( resultSetMetaData ).getColumnLabel( 1 );
    doReturn( "NAME" ).when( resultSetMetaData ).getColumnLabel( 2 );
    doReturn( "LAST_NAME" ).when( resultSetMetaData ).getColumnLabel( 3 );
    doReturn( "FIRST_NAME" ).when( resultSetMetaData ).getColumnLabel( 4 );
    doReturn( "DB" ).when( resultSetMetaData ).getColumnLabel( 5 );
    doReturn( "NoAliasText" ).when( resultSetMetaData ).getColumnLabel( 6 );

    return resultSetMetaData;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  private ResultSetMetaData getResultSetMetaDataException() throws Exception {
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );

    doThrow( new SQLException() ).when( resultSetMetaData ).getColumnLabel( 1 );

    return resultSetMetaData;
  }

  @Test
  public void testGetLegacyColumnNameFieldNumber() throws Exception {
    assertEquals( "NUMBER", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 1 ) );
  }

  @Test
  public void testGetLegacyColumnNameFieldName() throws Exception {
    assertEquals( "NAME", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 2 ) );
  }

  @Test
  public void testGetLegacyColumnNameFieldLastName() throws Exception {
    assertEquals( "LAST_NAME", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 3 ) );
  }

  @Test
  public void testGetLegacyColumnNameFieldFirstName() throws Exception {
    assertEquals( "FIRST_NAME", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 4 ) );
  }

  @Test
  public void testGetLegacyColumnNameFieldDB() throws Exception {
    assertEquals( "DB", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 5 ) );
  }

  @Test
  public void testGetLegacyColumnNameNoAliasText() throws Exception {
    assertEquals( "NoAliasText", new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaData(), 6 ) );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameNullDBMetaDataException() throws Exception {
    new MariaDBDatabaseMeta().getLegacyColumnName( null, getResultSetMetaData(), 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameNullRSMetaDataException() throws Exception {
    new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), null, 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameDatabaseException() throws Exception {
    new MariaDBDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), getResultSetMetaDataException(), 1 );
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
