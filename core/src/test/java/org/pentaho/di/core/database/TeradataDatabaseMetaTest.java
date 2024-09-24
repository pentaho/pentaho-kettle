/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2022 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.Map;

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

public class TeradataDatabaseMetaTest {
  private TeradataDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new TeradataDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 1025, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "com.teradata.jdbc.TeraDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:teradata://FOO/DATABASE=WIBBLE", nativeMeta.getURL( "FOO", "IGNOREDHERE", "WIBBLE" ) );
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertEquals( "\"FOO\".\"BAR\"", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertEquals( ",", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "/", nativeMeta.getExtraOptionIndicator() );
    assertEquals( "https://teradata-docs.s3.amazonaws.com/doc/connectivity/jdbc/reference/current/jdbcug_chapter_2.html#BABJIHBJ",
        nativeMeta.getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "terajdbc4.jar", "tdgssjava.jar" }, nativeMeta.getUsedLibraries() );
    assertArrayEquals( new String[] {
      "ABORT", "ABORTSESSION", "ABS", "ACCESS_LOCK", "ACCOUNT", "ACOS", "ACOSH", "ADD", "ADD_MONTHS", "ADMIN",
      "AFTER", "AGGREGATE", "ALL", "ALTER", "AMP", "AND", "ANSIDATE", "ANY", "ARGLPAREN", "AS", "ASC", "ASIN",
      "ASINH", "AT", "ATAN", "ATAN2", "ATANH", "ATOMIC", "AUTHORIZATION", "AVE", "AVERAGE", "AVG", "BEFORE",
      "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BT", "BUT", "BY", "BYTE", "BYTEINT", "BYTES",
      "CALL", "CASE", "CASE_N", "CASESPECIFIC", "CAST", "CD", "CHAR", "CHAR_LENGTH", "CHAR2HEXINT", "CHARACTER",
      "CHARACTER_LENGTH", "CHARACTERS", "CHARS", "CHECK", "CHECKPOINT", "CLASS", "CLOB", "CLOSE", "CLUSTER",
      "CM", "COALESCE", "COLLATION", "COLLECT", "COLUMN", "COMMENT", "COMMIT", "COMPRESS", "CONSTRAINT",
      "CONSTRUCTOR", "CONSUME", "CONTAINS", "CONTINUE", "CONVERT_TABLE_HEADER", "CORR", "COS", "COSH", "COUNT",
      "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CS", "CSUM", "CT", "CUBE", "CURRENT", "CURRENT_DATE",
      "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURSOR", "CV", "CYCLE", "DATABASE", "DATABLOCKSIZE", "DATE",
      "DATEFORM", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRED", "DEGREES", "DEL",
      "DELETE", "DESC", "DETERMINISTIC", "DIAGNOSTIC", "DISABLED", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP",
      "DUAL", "DUMP", "DYNAMIC", "EACH", "ECHO", "ELSE", "ELSEIF", "ENABLED", "END", "EQ", "EQUALS", "ERROR",
      "ERRORFILES", "ERRORTABLES", "ESCAPE", "ET", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXP",
      "EXPLAIN", "EXTERNAL", "EXTRACT", "FALLBACK", "FASTEXPORT", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN",
      "FORMAT", "FOUND", "FREESPACE", "FROM", "FULL", "FUNCTION", "GE", "GENERATED", "GIVE", "GRANT", "GRAPHIC",
      "GROUP", "GROUPING", "GT", "HANDLER", "HASH", "HASHAMP", "HASHBAKAMP", "HASHBUCKET", "HASHROW", "HAVING",
      "HELP", "HOUR", "IDENTITY", "IF", "IMMEDIATE", "IN", "INCONSISTENT", "INDEX", "INITIATE", "INNER",
      "INOUT", "INPUT", "INS", "INSERT", "INSTANCE", "INSTEAD", "INT", "INTEGER", "INTEGERDATE", "INTERSECT",
      "INTERVAL", "INTO", "IS", "ITERATE", "JAR", "JOIN", "JOURNAL", "KEY", "KURTOSIS", "LANGUAGE", "LARGE",
      "LE", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LN", "LOADING", "LOCAL", "LOCATOR", "LOCK", "LOCKING",
      "LOG", "LOGGING", "LOGON", "LONG", "LOOP", "LOWER", "LT", "MACRO", "MAP", "MAVG", "MAX", "MAXIMUM",
      "MCHARACTERS", "MDIFF", "MERGE", "METHOD", "MIN", "MINDEX", "MINIMUM", "MINUS", "MINUTE", "MLINREG",
      "MLOAD", "MOD", "MODE", "MODIFIES", "MODIFY", "MONITOR", "MONRESOURCE", "MONSESSION", "MONTH", "MSUBSTR",
      "MSUM", "MULTISET", "NAMED", "NATURAL", "NE", "NEW", "NEW_TABLE", "NEXT", "NO", "NONE", "NOT", "NOWAIT",
      "NULL", "NULLIF", "NULLIFZERO", "NUMERIC", "OBJECT", "OBJECTS", "OCTET_LENGTH", "OF", "OFF", "OLD",
      "OLD_TABLE", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "ORDERING", "OUT", "OUTER", "OUTPUT", "OVER",
      "OVERLAPS", "OVERRIDE", "PARAMETER", "PASSWORD", "PERCENT", "PERCENT_RANK", "PERM", "PERMANENT",
      "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIVILEGES", "PROCEDURE", "PROFILE",
      "PROTECTION", "PUBLIC", "QUALIFIED", "QUALIFY", "QUANTILE", "QUEUE", "RADIANS", "RANDOM", "RANGE_N",
      "RANK", "READS", "REAL", "RECURSIVE", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT",
      "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELATIVE", "RELEASE",
      "RENAME", "REPEAT", "REPLACE", "REPLCONTROL", "REPLICATION", "REQUEST", "RESTART", "RESTORE", "RESULT",
      "RESUME", "RET", "RETRIEVE", "RETURN", "RETURNS", "REVALIDATE", "REVOKE", "RIGHT", "RIGHTS", "ROLE",
      "ROLLBACK", "ROLLFORWARD", "ROLLUP", "ROW", "ROW_NUMBER", "ROWID", "ROWS", "SAMPLE", "SAMPLEID", "SCROLL",
      "SECOND", "SEL", "SELECT", "SESSION", "SET", "SETRESRATE", "SETS", "SETSESSRATE", "SHOW", "SIN", "SINH",
      "SKEW", "SMALLINT", "SOME", "SOUNDEX", "SPECIFIC", "SPOOL", "SQL", "SQLEXCEPTION", "SQLTEXT",
      "SQLWARNING", "SQRT", "SS", "START", "STARTUP", "STATEMENT", "STATISTICS", "STDDEV_POP", "STDDEV_SAMP",
      "STEPINFO", "STRING_CS", "SUBSCRIBER", "SUBSTR", "SUBSTRING", "SUM", "SUMMARY", "SUSPEND", "TABLE", "TAN",
      "TANH", "TBL_CS", "TEMPORARY", "TERMINATE", "THEN", "THRESHOLD", "TIME", "TIMESTAMP", "TIMEZONE_HOUR",
      "TIMEZONE_MINUTE", "TITLE", "TO", "TOP", "TRACE", "TRAILING", "TRANSACTION", "TRANSFORM", "TRANSLATE",
      "TRANSLATE_CHK", "TRIGGER", "TRIM", "TRUE", "TYPE", "UC", "UDTCASTAS", "UDTCASTLPAREN", "UDTMETHOD",
      "UDTTYPE", "UDTUSAGE", "UESCAPE", "UNDEFINED", "UNDO", "UNION", "UNIQUE", "UNTIL", "UPD", "UPDATE",
      "UPPER", "UPPERCASE", "USER", "USING", "VALUE", "VALUES", "VAR_POP", "VAR_SAMP", "VARBYTE", "VARCHAR",
      "VARGRAPHIC", "VARYING", "VIEW", "VOLATILE", "WHEN", "WHERE", "WHILE", "WIDTH_BUCKET", "WITH", "WITHOUT",
      "WORK", "YEAR", "ZEROIFNULL", "ZONE" }, nativeMeta.getReservedWords() );

    nativeMeta.setDatabasePortNumberString( "1025" );
    nativeMeta.setPluginId( "FOOPLUGIN" );
    Map<String, String> xtraOptions = nativeMeta.getExtraOptions();
    assertTrue( xtraOptions.containsKey( "FOOPLUGIN.DBS_PORT" ) );
    assertEquals( "1025", xtraOptions.get( "FOOPLUGIN.DBS_PORT" ) );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "show table FOO", nativeMeta.getSQLTableExists( "FOO" ) );
    assertEquals( "SELECT * FROM DBC.columns WHERE tablename =BAR AND columnname =FOO",
        nativeMeta.getSQLColumnExists( "FOO", "BAR" ) ); // Likely a bug - table/column not quoted.
    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, true, false ) );

    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, false, false ) );

    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO" ), "", "FOO", false, false, false ) );

    assertEquals( "DECIMAL(10, 0)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 0 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 6, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 9, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 3, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 4, 0 ), "", "", false, false, false ) );
    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 5, 0 ), "", "", false, false, false ) );
    assertEquals( "BYTEINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 2, 0 ), "", "", false, false, false ) );
    assertEquals( "BYTEINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 1, 0 ), "", "", false, false, false ) );

    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", -23, 0 ), "", "", false, false, false ) );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 64001, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(64000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 64000, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 1, 0 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }
}
