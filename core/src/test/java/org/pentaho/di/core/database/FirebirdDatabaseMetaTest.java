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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class FirebirdDatabaseMetaTest {

  private FirebirdDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new FirebirdDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 3050, nativeMeta.getDefaultDatabasePort() );

    assertEquals( "&", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "?", nativeMeta.getExtraOptionIndicator() );
    assertEquals( "org.firebirdsql.jdbc.FBDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:firebirdsql://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:firebirdsql://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // This is a bug I believe
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.supportsAutoInc() );

    assertArrayEquals( new String[] {
      "ABSOLUTE", "ACTION", "ACTIVE", "ADD", "ADMIN", "AFTER", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE",
      "AS", "ASC", "ASCENDING", "ASSERTION", "AT", "AUTHORIZATION", "AUTO", "AUTODDL", "AVG", "BASED",
      "BASENAME", "BASE_NAME", "BEFORE", "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BLOB", "BLOBEDIT", "BOTH",
      "BUFFER", "BY", "CACHE", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER",
      "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK", "CHECK_POINT_LEN", "CHECK_POINT_LENGTH", "CLOSE", "COALESCE",
      "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMMITTED", "COMPILETIME", "COMPUTED", "CONDITIONAL",
      "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTAINING", "CONTINUE", "CONVERT",
      "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CSTRING", "CURRENT", "CURRENT_DATE", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_USER", "DATABASE", "DATE", "DAY", "DB_KEY", "DEALLOCATE", "DEBUG", "DEC",
      "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCENDING", "DESCRIBE",
      "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISPLAY", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP",
      "ECHO", "EDIT", "ELSE", "END", "END-EXEC", "ENTRY_POINT", "ESCAPE", "EVENT", "EXCEPT", "EXCEPTION",
      "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERN", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILE", "FILTER",
      "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE_IT", "FROM", "FULL", "FUNCTION", "GDSCODE", "GENERATOR",
      "GEN_ID", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUP_COMMIT_WAIT", "GROUP_COMMIT_WAIT_TIME",
      "HAVING", "HELP", "HOUR", "IDENTITY", "IF", "IMMEDIATE", "IN", "INACTIVE", "INDEX", "INDICATOR", "INIT",
      "INITIALLY", "INNER", "INPUT", "INPUT_TYPE", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT",
      "INTERVAL", "INTO", "IS", "ISOLATION", "ISQL", "JOIN", "KEY", "LANGUAGE", "LAST", "LC_MESSAGES",
      "LC_TYPE", "LEADING", "LEFT", "LENGTH", "LEV", "LEVEL", "LIKE", "LOCAL", "LOGFILE", "LOG_BUFFER_SIZE",
      "LOG_BUF_SIZE", "LONG", "LOWER", "MANUAL", "MATCH", "MAX", "MAXIMUM", "MAXIMUM_SEGMENT", "MAX_SEGMENT",
      "MERGE", "MESSAGE", "MIN", "MINIMUM", "MINUTE", "MODULE", "MODULE_NAME", "MONTH", "NAMES", "NATIONAL",
      "NATURAL", "NCHAR", "NEXT", "NO", "NOAUTO", "NOT", "NULL", "NULLIF", "NUM_LOG_BUFS", "NUM_LOG_BUFFERS",
      "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT",
      "OUTPUT_TYPE", "OVERFLOW", "OVERLAPS", "PAD", "PAGE", "PAGELENGTH", "PAGES", "PAGE_SIZE", "PARAMETER",
      "PARTIAL", "PASSWORD", "PLAN", "POSITION", "POST_EVENT", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY",
      "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "QUIT", "RAW_PARTITIONS", "RDB$DB_KEY", "READ", "REAL",
      "RECORD_VERSION", "REFERENCES", "RELATIVE", "RELEASE", "RESERV", "RESERVING", "RESTRICT", "RETAIN",
      "RETURN", "RETURNING_VALUES", "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROWS", "RUNTIME",
      "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SHADOW", "SHARED",
      "SHELL", "SHOW", "SINGULAR", "SIZE", "SMALLINT", "SNAPSHOT", "SOME", "SORT", "SPACE", "SQL", "SQLCODE",
      "SQLERROR", "SQLSTATE", "SQLWARNING", "STABILITY", "STARTING", "STARTS", "STATEMENT", "STATIC",
      "STATISTICS", "SUB_TYPE", "SUBSTRING", "SUM", "SUSPEND", "SYSTEM_USER", "TABLE", "TEMPORARY",
      "TERMINATOR", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING",
      "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIGGER", "TRIM", "TRUE", "TYPE", "UNCOMMITTED", "UNION",
      "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
      "VARIABLE", "VARYING", "VERSION", "VIEW", "WAIT", "WEEKDAY", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH",
      "WORK", "WRITE", "YEAR", "YEARDAY", "ZONE", "ABSOLUTE", "ACTION", "ACTIVE", "ADD", "ADMIN", "AFTER",
      "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASCENDING", "ASSERTION", "AT",
      "AUTHORIZATION", "AUTO", "AUTODDL", "AVG", "BASED", "BASENAME", "BASE_NAME", "BEFORE", "BEGIN", "BETWEEN",
      "BIT", "BIT_LENGTH", "BLOB", "BLOBEDIT", "BOTH", "BUFFER", "BY", "CACHE", "CASCADE", "CASCADED", "CASE",
      "CAST", "CATALOG", "CHAR", "CHARACTER", "CHAR_LENGTH", "CHARACTER_LENGTH", "CHECK", "CHECK_POINT_LEN",
      "CHECK_POINT_LENGTH", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMMITTED",
      "COMPILETIME", "COMPUTED", "CONDITIONAL", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS",
      "CONTAINING", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CSTRING", "CURRENT",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "DATABASE", "DATE", "DAY", "DB_KEY",
      "DEALLOCATE", "DEBUG", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC",
      "DESCENDING", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISPLAY", "DISTINCT", "DO",
      "DOMAIN", "DOUBLE", "DROP", "ECHO", "EDIT", "ELSE", "END", "END-EXEC", "ENTRY_POINT", "ESCAPE", "EVENT",
      "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERN", "EXTERNAL", "EXTRACT", "FALSE",
      "FETCH", "FILE", "FILTER", "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE_IT", "FROM", "FULL", "FUNCTION",
      "GDSCODE", "GENERATOR", "GEN_ID", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "GROUP_COMMIT_WAIT",
      "GROUP_COMMIT_WAIT_TIME", "HAVING", "HELP", "HOUR", "IDENTITY", "IF", "IMMEDIATE", "IN", "INACTIVE",
      "INDEX", "INDICATOR", "INIT", "INITIALLY", "INNER", "INPUT", "INPUT_TYPE", "INSENSITIVE", "INSERT", "INT",
      "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "ISQL", "JOIN", "KEY", "LANGUAGE", "LAST",
      "LC_MESSAGES", "LC_TYPE", "LEADING", "LEFT", "LENGTH", "LEV", "LEVEL", "LIKE", "LOCAL", "LOGFILE",
      "LOG_BUFFER_SIZE", "LOG_BUF_SIZE", "LONG", "LOWER", "MANUAL", "MATCH", "MAX", "MAXIMUM",
      "MAXIMUM_SEGMENT", "MAX_SEGMENT", "MERGE", "MESSAGE", "MIN", "MINIMUM", "MINUTE", "MODULE", "MODULE_NAME",
      "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOAUTO", "NOT", "NULL", "NULLIF",
      "NUM_LOG_BUFS", "NUM_LOG_BUFFERS", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR",
      "ORDER", "OUTER", "OUTPUT", "OUTPUT_TYPE", "OVERFLOW", "OVERLAPS", "PAD", "PAGE", "PAGELENGTH", "PAGES",
      "PAGE_SIZE", "PARAMETER", "PARTIAL", "PASSWORD", "PLAN", "POSITION", "POST_EVENT", "PRECISION", "PREPARE",
      "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "QUIT", "RAW_PARTITIONS",
      "RDB$DB_KEY", "READ", "REAL", "RECORD_VERSION", "REFERENCES", "RELATIVE", "RELEASE", "RESERV",
      "RESERVING", "RESTRICT", "RETAIN", "RETURN", "RETURNING_VALUES", "RETURNS", "REVOKE", "RIGHT", "ROLE",
      "ROLLBACK", "ROWS", "RUNTIME", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION",
      "SESSION_USER", "SET", "SHADOW", "SHARED", "SHELL", "SHOW", "SINGULAR", "SIZE", "SMALLINT", "SNAPSHOT",
      "SOME", "SORT", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SQLWARNING", "STABILITY", "STARTING",
      "STARTS", "STATEMENT", "STATIC", "STATISTICS", "SUB_TYPE", "SUBSTRING", "SUM", "SUSPEND", "SYSTEM_USER",
      "TABLE", "TEMPORARY", "TERMINATOR", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO",
      "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIGGER", "TRIM", "TRUE", "TYPE", "UNCOMMITTED",
      "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
      "VARIABLE", "VARYING", "VERSION", "VIEW", "WAIT", "WEEKDAY", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH",
      "WORK", "WRITE", "YEAR", "YEARDAY", "ZONE" }, nativeMeta.getReservedWords() );
    assertArrayEquals( new String[] { "jaybird-full-2.1.0.jar" }, nativeMeta.getUsedLibraries() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR TIMESTAMP",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR TYPE TIMESTAMP",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, true, false ) );

    assertEquals( "\"SELECT\"VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "SELECT", 15, 0 ), "", "", false, true, false ) ); // Missing space between quote is a bug

    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "BIGINT NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "FOO", "", false, false, false ) );
    assertEquals( "BIGINT NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO" ), "", "FOO", false, false, false ) );

    assertEquals( "DECIMAL(20)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 20, 0 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(7, 4)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 7, 4 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 5, 0 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 7, 0 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 8, 0 ), "", "", false, false, false ) );

    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 4, 0 ), "", "", false, false, false ) );

    assertEquals( "SMALLINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 1, 0 ), "", "", false, false, false ) );

    assertEquals( "BIGINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) );

    assertEquals( "DOUBLE",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", -7, 33 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(8000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(50)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 50, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB SUB_TYPE TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32721, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 32721, 0 ), "", "", false, false, false ) );

    assertEquals( "UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( "UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }
}
