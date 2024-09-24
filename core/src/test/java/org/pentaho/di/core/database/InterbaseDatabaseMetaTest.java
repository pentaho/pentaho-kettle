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
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

public class InterbaseDatabaseMetaTest {

  private InterbaseDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new InterbaseDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 3050, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "interbase.interclient.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:interbase://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:interbase://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Pretty sure this is a bug (colon after foo)
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertArrayEquals( new String[] { "interclient.jar" }, nativeMeta.getUsedLibraries() );
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
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );
    assertFalse( nativeMeta.supportsBatchUpdates() );
    assertFalse( nativeMeta.supportsGetBlob() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR TYPE VARCHAR(15)",
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
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "", "FOO", false, false, false ) );

    assertEquals( "NUMERIC(10)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC(10)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC(10, 5)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 5 ), "", "", false, false, false ) );
    assertEquals( "DOUBLE PRECISION",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -7, -2 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(32663)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32663, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(32664)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", -43, 0 ), "", "", false, false, false ) );
    assertEquals( "BLOB SUB_TYPE TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32664, 0 ), "", "", false, false, false ) );
    assertEquals( "BLOB SUB_TYPE TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 32665, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
