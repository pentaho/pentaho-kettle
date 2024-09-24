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
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class SAPDBDatabaseMetaTest {

  private SAPDBDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new SAPDBDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( -1, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "com.sap.dbtech.jdbc.DriverSapDB", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:sapdb://FOO/WIBBLE", nativeMeta.getURL( "FOO", "IGNORED", "WIBBLE" ) );
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertArrayEquals( new String[] { "sapdbc.jar" }, nativeMeta.getUsedLibraries() );
    assertEquals( new String[] {
      "ABS", "ABSOLUTE", "ACOS", "ADDDATE", "ADDTIME", "ALL", "ALPHA", "ALTER", "ANY", "ASCII", "ASIN", "ATAN",
      "ATAN2", "AVG", "BINARY", "BIT", "BOOLEAN", "BYTE", "CASE", "CEIL", "CEILING", "CHAR", "CHARACTER",
      "CHECK", "CHR", "COLUMN", "CONCAT", "CONSTRAINT", "COS", "COSH", "COT", "COUNT", "CROSS", "CURDATE",
      "CURRENT", "CURTIME", "DATABASE", "DATE", "DATEDIFF", "DAY", "DAYNAME", "DAYOFMONTH", "DAYOFWEEK",
      "DAYOFYEAR", "DEC", "DECIMAL", "DECODE", "DEFAULT", "DEGREES", "DELETE", "DIGITS", "DISTINCT", "DOUBLE",
      "EXCEPT", "EXISTS", "EXP", "EXPAND", "FIRST", "FIXED", "FLOAT", "FLOOR", "FOR", "FROM", "FULL",
      "GET_OBJECTNAME", "GET_SCHEMA", "GRAPHIC", "GREATEST", "GROUP", "HAVING", "HEX", "HEXTORAW", "HOUR",
      "IFNULL", "IGNORE", "INDEX", "INITCAP", "INNER", "INSERT", "INT", "INTEGER", "INTERNAL", "INTERSECT",
      "INTO", "JOIN", "KEY", "LAST", "LCASE", "LEAST", "LEFT", "LENGTH", "LFILL", "LIST", "LN", "LOCATE", "LOG",
      "LOG10", "LONG", "LONGFILE", "LOWER", "LPAD", "LTRIM", "MAKEDATE", "MAKETIME", "MAPCHAR", "MAX", "MBCS",
      "MICROSECOND", "MIN", "MINUTE", "MOD", "MONTH", "MONTHNAME", "NATURAL", "NCHAR", "NEXT", "NO", "NOROUND",
      "NOT", "NOW", "NULL", "NUM", "NUMERIC", "OBJECT", "OF", "ON", "ORDER", "PACKED", "PI", "POWER", "PREV",
      "PRIMARY", "RADIANS", "REAL", "REJECT", "RELATIVE", "REPLACE", "RFILL", "RIGHT", "ROUND", "ROWID",
      "ROWNO", "RPAD", "RTRIM", "SECOND", "SELECT", "SELUPD", "SERIAL", "SET", "SHOW", "SIGN", "SIN", "SINH",
      "SMALLINT", "SOME", "SOUNDEX", "SPACE", "SQRT", "STAMP", "STATISTICS", "STDDEV", "SUBDATE", "SUBSTR",
      "SUBSTRING", "SUBTIME", "SUM", "SYSDBA", "TABLE", "TAN", "TANH", "TIME", "TIMEDIFF", "TIMESTAMP",
      "TIMEZONE", "TO", "TOIDENTIFIER", "TRANSACTION", "TRANSLATE", "TRIM", "TRUNC", "TRUNCATE", "UCASE", "UID",
      "UNICODE", "UNION", "UPDATE", "UPPER", "USER", "USERGROUP", "USING", "UTCDATE", "UTCDIFF", "VALUE",
      "VALUES", "VARCHAR", "VARGRAPHIC", "VARIANCE", "WEEK", "WEEKOFYEAR", "WHEN", "WHERE", "WITH", "YEAR",
      "ZONED" }, nativeMeta.getReservedWords() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR TYPE VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "BIGINT NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "BIGINT NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 0 ), "", "FOO", false, false, false ) );

    // Decimal Types
    assertEquals( "DECIMAL(10, 3)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 3 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(19)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 19, 0 ), "", "", false, false, false ) );

    // Integers
    assertEquals( "INT64",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 4, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 5, 0 ), "", "", false, false, false ) );

    assertEquals( "DOUBLE",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", -7, -3 ), "", "", false, false, false ) );

    // Strings
    assertEquals( "VARCHAR(32719)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32719, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB SUB_TYPE TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32720, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(8000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", -122, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );

  }

}
