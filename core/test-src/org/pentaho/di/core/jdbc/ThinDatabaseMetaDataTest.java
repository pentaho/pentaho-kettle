/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.UUID;

import org.junit.Test;

public class ThinDatabaseMetaDataTest {

  static ThinConnection mockConnection = mock( ThinConnection.class );
  static ThinDatabaseMetaData thinDbmd = spy( new ThinDatabaseMetaData( mockConnection ) );

  private static String randomString() {
    return UUID.randomUUID().toString();
  }

  private boolean isResultSetEmpty( ResultSet result ) throws SQLException {
    if ( null == result ) {
      return true;
    }
    int rowCount = 0;
    try {
      while ( result.next() ) {
        rowCount++;
      }
    } catch ( SQLException ignore ) {
    }
    result.beforeFirst();
    return 0 == rowCount;
  }

  @Test
  public void testGetAttributes() throws SQLException {
    ResultSet result = thinDbmd.getAttributes( randomString(), randomString(), randomString(), randomString() );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 21, resultMetaData.getColumnCount() );

    assertEquals( "TYPE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TYPE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "TYPE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "ATTR_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "DATA_TYPE", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 5 ) );

    assertEquals( "ATTR_TYPE_NAME", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "ATTR_SIZE", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 7 ) );

    assertEquals( "DECIMAL_DIGITS", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 8 ) );

    assertEquals( "NUM_PREC_RADIX", resultMetaData.getColumnName( 9 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 9 ) );

    assertEquals( "NULLABLE", resultMetaData.getColumnName( 10 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 10 ) );

    assertEquals( "REMARKS", resultMetaData.getColumnName( 11 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 11 ) );

    assertEquals( "ATTR_DEF", resultMetaData.getColumnName( 12 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 12 ) );

    assertEquals( "SQL_DATA_TYPE", resultMetaData.getColumnName( 13 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 13 ) );

    assertEquals( "SQL_DATETIME_SUB", resultMetaData.getColumnName( 14 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 14 ) );

    assertEquals( "CHAR_OCTET_LENGTH", resultMetaData.getColumnName( 15 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 15 ) );

    assertEquals( "ORDINAL_POSITION", resultMetaData.getColumnName( 16 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 16 ) );

    assertEquals( "IS_NULLABLE", resultMetaData.getColumnName( 17 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 17 ) );

    assertEquals( "SCOPE_CATALOG", resultMetaData.getColumnName( 18 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 18 ) );

    assertEquals( "SCOPE_SCHEMA", resultMetaData.getColumnName( 19 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 19 ) );

    assertEquals( "SCOPE_TABLE", resultMetaData.getColumnName( 20 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 20 ) );

    assertEquals( "SOURCE_DATA_TYPE", resultMetaData.getColumnName( 21 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 21 ) );
  }

  @Test
  public void testGetBestRowIdentifier() throws SQLException {
    ResultSet result = thinDbmd.getBestRowIdentifier( randomString(), randomString(), randomString(), 0, false );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 8, resultMetaData.getColumnCount() );

    assertEquals( "SCOPE", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 1 ) );

    assertEquals( "COLUMN_NAME", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "DATA_TYPE", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 3 ) );

    assertEquals( "TYPE_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "COLUMN_SIZE", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 5 ) );

    assertEquals( "BUFFER_LENGTH", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 6 ) );

    assertEquals( "DECIMAL_DIGITS", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 7 ) );

    assertEquals( "PSEUDO_COLUMN", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 8 ) );
  }

  @Test
  public void testGetCatalogs() throws SQLException {
    ResultSet result = thinDbmd.getCatalogs();
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 1, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );
  }

  @Test
  public void testGetClientInfoProperties() throws SQLException {
    ResultSet result = thinDbmd.getClientInfoProperties();
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 4, resultMetaData.getColumnCount() );

    assertEquals( "NAME", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "MAX_LEN", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 2 ) );

    assertEquals( "DEFAULT_VALUE", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "DESCRIPTION", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );
  }

  @Test
  public void testGetColumnPrivileges() throws SQLException {
    ResultSet result = thinDbmd.getColumnPrivileges( randomString(), randomString(), randomString(), randomString() );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 8, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TABLE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "TABLE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "COLUMN_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "GRANTOR", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 5 ) );

    assertEquals( "GRANTEE", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "PRIVILEGE", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 7 ) );

    assertEquals( "IS_GRANTABLE", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 8 ) );
  }

  @Test
  public void testGetColumns() throws SQLException {
    doReturn( new ArrayList<ThinServiceInformation>() ).when( thinDbmd ).getServiceInformation();

    ResultSet result = thinDbmd.getColumns( randomString(), randomString(), randomString(), randomString() );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 22, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TABLE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "TABLE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "COLUMN_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "DATA_TYPE", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 5 ) );

    assertEquals( "TYPE_NAME", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "COLUMN_SIZE", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 7 ) );

    assertEquals( "BUFFER_LENGTH", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 8 ) );

    assertEquals( "DECIMAL_DIGITS", resultMetaData.getColumnName( 9 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 9 ) );

    assertEquals( "NUM_PREC_RADIX", resultMetaData.getColumnName( 10 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 10 ) );

    assertEquals( "NULLABLE", resultMetaData.getColumnName( 11 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 11 ) );

    assertEquals( "REMARKS", resultMetaData.getColumnName( 12 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 12 ) );

    assertEquals( "COLUMN_DEF", resultMetaData.getColumnName( 13 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 13 ) );

    assertEquals( "SQL_DATA_TYPE", resultMetaData.getColumnName( 14 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 14 ) );

    assertEquals( "SQL_DATATIME_SUB", resultMetaData.getColumnName( 15 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 15 ) );

    assertEquals( "CHAR_OCTET_LENGTH", resultMetaData.getColumnName( 16 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 16 ) );

    assertEquals( "ORDINAL_POSITION", resultMetaData.getColumnName( 17 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 17 ) );

    assertEquals( "IS_NULLABLE", resultMetaData.getColumnName( 18 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 18 ) );

    assertEquals( "SCOPE_CATALOG", resultMetaData.getColumnName( 19 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 19 ) );

    assertEquals( "SCOPE_SCHEMA", resultMetaData.getColumnName( 20 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 20 ) );

    assertEquals( "SCOPE_TABLE", resultMetaData.getColumnName( 21 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 21 ) );

    assertEquals( "SOURCE_DATA_TYPE", resultMetaData.getColumnName( 22 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 22 ) );
  }

  @Test
  public void testGetCrossReference() throws SQLException {
    ResultSet result = thinDbmd.getCrossReference( randomString(), randomString(), randomString(), randomString(), randomString(), randomString() );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 8, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TABLE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "TABLE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "COLUMN_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "GRANTOR", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 5 ) );

    assertEquals( "GRANTEE", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "PRIVILEGE", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 7 ) );

    assertEquals( "IS_GRANTABLE", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 8 ) );
  }

  @Test
  public void testGetImportedKeys() throws SQLException {
    ResultSet result = thinDbmd.getImportedKeys( randomString(), randomString(), randomString() );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 14, resultMetaData.getColumnCount() );

    assertEquals( "PKTABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "PKTABLE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "PKTABLE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "PKCOLUMN_NAME", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "FKTABLE_CAT", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 5 ) );

    assertEquals( "FKTABLE_SCHEM", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "FKTABLE_NAME", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 7 ) );

    assertEquals( "FKCOLUMN_NAME", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 8 ) );

    assertEquals( "KEY_SEQ", resultMetaData.getColumnName( 9 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 9 ) );

    assertEquals( "UPDATE_RULE", resultMetaData.getColumnName( 10 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 10 ) );

    assertEquals( "DELETE_RULE", resultMetaData.getColumnName( 11 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 11 ) );

    assertEquals( "FK_NAME", resultMetaData.getColumnName( 12 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 12 ) );

    assertEquals( "PK_NAME", resultMetaData.getColumnName( 13 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 13 ) );

    assertEquals( "DEFERRABILITY", resultMetaData.getColumnName( 14 ) );
    assertEquals( Types.BIGINT, resultMetaData.getColumnType( 14 ) );
  }

  @Test
  public void testGetSchemas() throws SQLException {
    ResultSet result = thinDbmd.getSchemas();
    assertFalse( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();
    assertEquals( 2, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_SCHEM", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TABLE_CATALOG", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    result.next(); // Get to first row
    assertEquals( "Kettle", result.getString( 1 ) );
    assertEquals( null, result.getString( 2 ) );
  }

  @Test
  public void testGetTables() throws SQLException {
    doReturn( new ArrayList<ThinServiceInformation>() ).when( thinDbmd ).getServiceInformation();

    // Test with invalid types first, expect no columns returned
    ResultSet result = thinDbmd.getTables(
      randomString(), randomString(), randomString(), new String[] { randomString() } );
    assertTrue( isResultSetEmpty( result ) );

    ResultSetMetaData resultMetaData = result.getMetaData();

    // Test with all types (no filters applied)
    result = thinDbmd.getTables( randomString(), randomString(), randomString(), null );
    assertTrue( isResultSetEmpty( result ) );

    resultMetaData = result.getMetaData();
    assertEquals( 10, resultMetaData.getColumnCount() );

    assertEquals( "TABLE_CAT", resultMetaData.getColumnName( 1 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 1 ) );

    assertEquals( "TABLE_SCHEM", resultMetaData.getColumnName( 2 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 2 ) );

    assertEquals( "TABLE_NAME", resultMetaData.getColumnName( 3 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 3 ) );

    assertEquals( "TABLE_TYPE", resultMetaData.getColumnName( 4 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 4 ) );

    assertEquals( "REMARKS", resultMetaData.getColumnName( 5 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 5 ) );

    assertEquals( "TYPE_CAT", resultMetaData.getColumnName( 6 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 6 ) );

    assertEquals( "TYPE_SCHEM", resultMetaData.getColumnName( 7 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 7 ) );

    assertEquals( "TYPE_NAME", resultMetaData.getColumnName( 8 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 8 ) );

    assertEquals( "SELF_REFERENCING_COL_NAME", resultMetaData.getColumnName( 9 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 9 ) );

    assertEquals( "REF_GENERATION", resultMetaData.getColumnName( 10 ) );
    assertEquals( Types.VARCHAR, resultMetaData.getColumnType( 10 ) );
  }
}
