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

public class Exasol4DatabaseMetaTest {
  Exasol4DatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new Exasol4DatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 8563, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertFalse( nativeMeta.needsToLockAllTables() );
    assertEquals( "com.exasol.jdbc.EXADriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:exa:FOO:BAR", nativeMeta.getURL( "FOO", "BAR", "IGNORED" ) );
    assertTrue( nativeMeta.supportsOptionsInURL() );
    assertFalse( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.useSchemaNameForTableList() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertEquals( new String[] {
      "ABSOLUTE", "ACTION", "ADD", "AFTER", "ALL", "ALLOCATE", "ALTER", "AND", "APPEND", "ARE", "ARRAY", "AS",
      "ASC", "ASENSITIVE", "ASSERTION", "AT", "ATTRIBUTE", "AUTHID", "AUTHORIZATION", "BEFORE", "BEGIN",
      "BETWEEN", "BIGINT", "BINARY", "BIT", "BLOB", "BLOCKED", "BOOL", "BOOLEAN", "BOTH", "BY", "BYTE", "CALL",
      "CALLED", "CARDINALITY", "CASCADE", "CASCADED", "CASE", "CASESPECIFIC", "CAST", "CATALOG", "CHAIN",
      "CHAR", "CHARACTER", "CHARACTERISTICS", "CHARACTER_SET_CATALOG", "CHARACTER_SET_NAME",
      "CHARACTER_SET_SCHEMA", "CHECK", "CHECKED", "CLOSE", "COALESCE", "COLLATE", "COLLATION",
      "COLLATION_CATALOG", "COLLATION_NAME", "COLLATION_SCHEMA", "COLUMN", "COMMIT", "CONDITION", "CONNECTION",
      "CONSTANT", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS", "CONTINUE", "CONTROL", "CONVERT",
      "CORRESPONDING", "CREATE", "CS", "CSV", "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE",
      "CURRENT_SCHEMA", "CURRENT_SESSION", "CURRENT_STATEMENT", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATALINK", "DATE", "DATETIME_INTERVAL_CODE",
      "DATETIME_INTERVAL_PRECISION", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE",
      "DEFERRED", "DEFINED", "DEFINER", "DELETE", "DEREF", "DERIVED", "DESC", "DESCRIBE", "DESCRIPTOR",
      "DETERMINISTIC", "DISABLE", "DISABLED", "DISCONNECT", "DISPATCH", "DISTINCT", "DLURLCOMPLETE",
      "DLURLPATH", "DLURLPATHONLY", "DLURLSCHEME", "DLURLSERVER", "DLVALUE", "DO", "DOMAIN", "DOUBLE", "DROP",
      "DYNAMIC", "DYNAMIC_FUNCTION", "DYNAMIC_FUNCTION_CODE", "EACH", "ELSE", "ELSEIF", "ELSIF", "ENABLE",
      "ENABLED", "END", "END-EXEC", "ENFORCE", "EQUALS", "ERRORS", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC",
      "EXECUTE", "EXISTS", "EXIT", "EXPORT", "EXTERNAL", "EXTRACT", "FALSE", "FBV", "FETCH", "FILE", "FINAL",
      "FIRST", "FLOAT", "FOLLOWING", "FOR", "FORALL", "FORCE", "FORMAT", "FOUND", "FREE", "FROM", "FS", "FULL",
      "FUNCTION", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRANTED", "GROUP",
      "GROUPING", "GROUP_CONCAT", "HAVING", "HOLD", "HOUR", "IDENTITY", "IF", "IFNULL", "IMMEDIATE",
      "IMPLEMENTATION", "IMPORT", "IN", "INDEX", "INDICATOR", "INNER", "INOUT", "INPUT", "INSENSITIVE",
      "INSERT", "INSTANCE", "INSTANTIABLE", "INT", "INTEGER", "INTEGRITY", "INTERSECT", "INTERVAL", "INTO",
      "INVOKER", "IS", "ITERATE", "JOIN", "KEY_MEMBER", "KEY_TYPE", "LARGE", "LAST", "LATERAL", "LEADING",
      "LEAVE", "LEFT", "LIKE", "LIMIT", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOG", "LONGVARCHAR",
      "LOOP", "MAP", "MATCH", "MATCHED", "MERGE", "METHOD", "MINUS", "MINUTE", "MOD", "MODIFIES", "MODIFY",
      "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEXT", "NLS_DATE_FORMAT",
      "NLS_DATE_LANGUAGE", "NLS_NUMERIC_CHARACTERS", "NLS_TIMESTAMP_FORMAT", "NO", "NOLOGGING", "NONE", "NOT",
      "NULL", "NULLIF", "NUMBER", "NUMERIC", "OBJECT", "OF", "OFF", "OLD", "ON", "ONLY", "OPEN", "OPTION",
      "OPTIONS", "OR", "ORDER", "ORDERING", "ORDINALITY", "OTHERS", "OUT", "OUTER", "OUTPUT", "OVER",
      "OVERLAPS", "OVERLAY", "OVERRIDING", "PAD", "PARALLEL_ENABLE", "PARAMETER", "PARAMETER_SPECIFIC_CATALOG",
      "PARAMETER_SPECIFIC_NAME", "PARAMETER_SPECIFIC_SCHEMA", "PARTIAL", "PATH", "PERMISSION", "PLACING",
      "POSITION", "PRECEDING", "PREPARE", "PRESERVE", "PRIOR", "PRIVILEGES", "PROCEDURE", "RANDOM", "RANGE",
      "READ", "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGEXP_LIKE",
      "RELATIVE", "RELEASE", "RENAME", "REPEAT", "REPLACE", "RESTORE", "RESTRICT", "RESULT", "RETURN",
      "RETURNED_LENGTH", "RETURNED_OCTET_LENGTH", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROUTINE",
      "ROW", "ROWS", "ROWTYPE", "SAVEPOINT", "SCHEMA", "SCOPE", "SCRIPT", "SCROLL", "SEARCH", "SECOND",
      "SECTION", "SECURITY", "SELECT", "SELECTIVE", "SELF", "SENSITIVE", "SEPARATOR", "SEQUENCE", "SESSION",
      "SESSION_USER", "SET", "SETS", "SHORTINT", "SIMILAR", "SMALLINT", "SOURCE", "SPACE", "SPECIFIC",
      "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIGINT", "SQL_BIT", "SQL_CHAR",
      "SQL_DATE", "SQL_DECIMAL", "SQL_DOUBLE", "SQL_FLOAT", "SQL_INTEGER", "SQL_LONGVARCHAR", "SQL_NUMERIC",
      "SQL_REAL", "SQL_SMALLINT", "SQL_TIMESTAMP", "SQL_TINYINT", "SQL_TYPE_DATE", "SQL_TYPE_TIMESTAMP",
      "SQL_VARCHAR", "START", "STATE", "STATEMENT", "STATIC", "STRUCTURE", "STYLE", "SUBSTRING", "SUBTYPE",
      "SYSDATE", "SYSTEM", "SYSTEM_USER", "SYSTIMESTAMP", "TABLE", "TEMPORARY", "TEXT", "THEN", "TIME",
      "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TINYINT", "TO", "TRAILING", "TRANSACTION", "TRANSFORM",
      "TRANSFORMS", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUNCATE", "UNDER", "UNION", "UNIQUE",
      "UNKNOWN", "UNLINK", "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR",
      "VARCHAR2", "VARRAY", "VERIFY", "VIEW", "WHEN", "WHENEVER", "WHERE", "WHILE", "WINDOW", "WITH", "WITHIN",
      "WITHOUT", "WORK", "YEAR", "YES", "ZONE" }, nativeMeta.getReservedWords() );
    assertEquals( "http://www.exasol.com/knowledge-center.html", nativeMeta.getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "exajdbc.jar" }, nativeMeta.getUsedLibraries() );
    assertTrue( nativeMeta.checkIndexExists( null, "NOT-THERE", "NOT-THERE", null ) ); // Always returns true - this is a bug IMO
    assertFalse( nativeMeta.requiresCreateTablePrimaryKeyAppend() );
    assertFalse( nativeMeta.supportsPreparedStatementMetadataRetrieval() );
    assertEquals( -1, nativeMeta.getMaxColumnsInIndex() );
    assertFalse( nativeMeta.releaseSavepoint() );
    assertFalse( nativeMeta.supportsErrorHandlingOnBatchUpdates() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( " WHERE ROWNUM <= 15", nativeMeta.getLimitClause( 15 ) );
    assertEquals( "SELECT /*+FIRST_ROWS*/ * FROM FOO WHERE 1=0", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT /*+FIRST_ROWS*/ * FROM FOO WHERE 1=0", nativeMeta.getSQLTableExists( "FOO" ) );

    assertEquals( "SELECT /*+FIRST_ROWS*/ FOO FROM BAR WHERE 1=0", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( "SELECT /*+FIRST_ROWS*/ FOO FROM BAR WHERE 1=0", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR TIMESTAMP ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( BAR TIMESTAMP ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR BOOLEAN ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBoolean( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL(15) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL(15) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL(15, 5) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 15, 5 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( BAR DECIMAL(15, 5) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 15, 5 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR INTEGER ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR VARCHAR(15) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR VARCHAR(2000000) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR UNKNOWN ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD ( BAR BIGINT NOT NULL PRIMARY KEY ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", false, "", false ) );
  }

}
