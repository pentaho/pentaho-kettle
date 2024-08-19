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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

public class UniVerseDatabaseMetaTest {

  private UniVerseDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new UniVerseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 65535, nativeMeta.getMaxVARCHARLength() );
    assertEquals( -1, nativeMeta.getDefaultDatabasePort() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "com.ibm.u2.jdbc.UniJDBCDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:ibm-u2://FOO/WIBBLE", nativeMeta.getURL( "FOO", "IGNORED", "WIBBLE" ) );
    assertEquals( "\"FOO\".\"BAR\"", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertTrue( nativeMeta.supportsNewLinesInSQL() );
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );
    assertArrayEquals( new String[] {
      "@NEW", "@OLD", "ACTION", "ADD", "AL", "ALL", "ALTER", "AND", "AR", "AS", "ASC", "ASSOC", "ASSOCIATED",
      "ASSOCIATION", "AUTHORIZATION", "AVERAGE", "AVG", "BEFORE", "BETWEEN", "BIT", "BOTH", "BY", "CALC",
      "CASCADE", "CASCADED", "CAST", "CHAR", "CHAR_LENGTH", "CHARACTER", "CHARACTER_LENGTH", "CHECK", "COL.HDG",
      "COL.SPACES", "COL.SPCS", "COL.SUP", "COLUMN", "COMPILED", "CONNECT", "CONSTRAINT", "CONV", "CONVERSION",
      "COUNT", "COUNT.SUP", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "DATA", "DATE", "DBA", "DBL.SPC",
      "DEC", "DECIMAL", "DEFAULT", "DELETE", "DESC", "DET.SUP", "DICT", "DISPLAY.NAME", "DISPLAYLIKE",
      "DISPLAYNAME", "DISTINCT", "DL", "DOUBLE", "DR", "DROP", "DYNAMIC", "E.EXIST", "EMPTY", "EQ", "EQUAL",
      "ESCAPE", "EVAL", "EVERY", "EXISTING", "EXISTS", "EXPLAIN", "EXPLICIT", "FAILURE", "FIRST", "FLOAT",
      "FMT", "FOOTER", "FOOTING", "FOR", "FOREIGN", "FORMAT", "FROM", "FULL", "GE", "GENERAL", "GRAND",
      "GRAND.TOTAL", "GRANT", "GREATER", "GROUP", "GROUP.SIZE", "GT", "HAVING", "HEADER", "HEADING", "HOME",
      "IMPLICIT", "IN", "INDEX", "INNER", "INQUIRING", "INSERT", "INT", "INTEGER", "INTO", "IS", "JOIN", "KEY",
      "LARGE.RECORD", "LAST", "LE", "LEADING", "LEFT", "LESS", "LIKE", "LOCAL", "LOWER", "LPTR", "MARGIN",
      "MATCHES", "MATCHING", "MAX", "MERGE.LOAD", "MIN", "MINIMIZE.SPACE", "MINIMUM.MODULUS", "MODULO",
      "MULTI.VALUE", "MULTIVALUED", "NATIONAL", "NCHAR", "NE", "NO", "NO.INDEX", "NO.OPTIMIZE", "NO.PAGE",
      "NOPAGE", "NOT", "NRKEY", "NULL", "NUMERIC", "NVARCHAR", "ON", "OPTION", "OR", "ORDER", "OUTER", "PCT",
      "PRECISION", "PRESERVING", "PRIMARY", "PRIVILEGES", "PUBLIC", "REAL", "RECORD.SIZE", "REFERENCES",
      "REPORTING", "RESOURCE", "RESTORE", "RESTRICT", "REVOKE", "RIGHT", "ROWUNIQUE", "SAID", "SAMPLE",
      "SAMPLED", "SCHEMA", "SELECT", "SEPARATION", "SEQ.NUM", "SET", "SINGLE.VALUE", "SINGLEVALUED", "SLIST",
      "SMALLINT", "SOME", "SPLIT.LOAD", "SPOKEN", "SUBSTRING", "SUCCESS", "SUM", "SUPPRESS", "SYNONYM", "TABLE",
      "TIME", "TO", "TOTAL", "TRAILING", "TRIM", "TYPE", "UNION", "UNIQUE", "UNNEST", "UNORDERED", "UPDATE",
      "UPPER", "USER", "USING", "VALUES", "VARBIT", "VARCHAR", "VARYING", "VERT", "VERTICALLY", "VIEW", "WHEN",
      "WHERE", "WITH", }, nativeMeta.getReservedWords() );

    assertArrayEquals( new String[] { "unijdbc.jar", "asjava.zip" }, nativeMeta.getUsedLibraries() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (0, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
    assertEquals( "DELETE FROM FOO",
        nativeMeta.getTruncateTableStatement( "FOO" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO DATE",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATE",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, false, false ) ); // Note - Rocket U2 does *not* support timestamps ...

    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "", "FOO", false, false, false ) );

    // Numeric Types
    assertEquals( "DECIMAL(5, 5)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 5, 5 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(19, 0)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 19, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 18, 0 ), "", "", false, false, false ) );
    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", -7, -3 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(65535)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 65537, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );

  }

}
