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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;

public class NetezzaDatabaseMetaTest {
  private NetezzaDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new NetezzaDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );

  }

  @Test
  public void testSettings() throws Exception {
    assertEquals( "&", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "?", nativeMeta.getExtraOptionIndicator() );
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 5480, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "org.netezza.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:netezza://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:netezza://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // I think this is a bug...
    assertEquals( "jdbc:netezza://FOO:null/WIBBLE", nativeMeta.getURL( "FOO", null, "WIBBLE" ) ); // I think this is a bug...
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertTrue( nativeMeta.supportsSequences() );
    assertFalse( nativeMeta.supportsAutoInc() );

    assertArrayEquals( new String[] {
      // As per the user manual
      "ABORT", "ADMIN", "AGGREGATE", "ALIGN", "ALL", "ALLOCATE", "ANALYSE", "ANALYZE", "AND", "ANY", "AS",
      "ASC", "BETWEEN", "BINARY", "BIT", "BOTH", "CASE", "CAST", "CHAR", "CHARACTER", "CHECK", "CLUSTER",
      "COALESCE", "COLLATE", "COLLATION", "COLUMN", "CONSTRAINT", "COPY", "CROSS", "CURRENT", "CURRENT_CATALOG",
      "CURRENT_DATE", "CURRENT_DB", "CURRENT_SCHEMA", "CURRENT_SID", "CURRENT_TIME", "CURRENT_TIMESTAMP",
      "CURRENT_USER", "CURRENT_USERID", "CURRENT_USEROID", "DEALLOCATE", "DEC", "DECIMAL", "DECODE", "DEFAULT",
      "DEFERRABLE", "DESC", "DISTINCT", "DISTRIBUTE", "DO", "ELSE", "END", "EXCEPT", "EXCLUDE", "EXISTS",
      "EXPLAIN", "EXPRESS", "EXTEND", "EXTRACT", "FALSE", "FIRST", "FLOAT", "FOLLOWING", "FOR", "FOREIGN",
      "FROM", "FULL", "FUNCTION", "GENSTATS", "GLOBAL", "GROUP", "HAVING", "ILIKE", "IN", "INDEX", "INITIALLY",
      "INNER", "INOUT", "INTERSECT", "INTERVAL", "INTO", "IS", "ISNULL", "JOIN", "LAST", "LEADING", "LEFT",
      "LIKE", "LIMIT", "LISTEN", "LOAD", "LOCAL", "LOCK", "MATERIALIZED", "MINUS", "MOVE", "NATURAL", "NCHAR",
      "NEW", "NOT", "NOTNULL", "NULL", "NULLIF", "NULLS", "NUMERIC", "NVL", "NVL2", "OFF", "OFFSET", "OLD",
      "ON", "ONLINE", "ONLY", "OR", "ORDER", "OTHERS", "OUT", "OUTER", "OVER", "OVERLAPS", "PARTITION",
      "POSITION", "PRECEDING", "PRECISION", "PRESERVE", "PRIMARY", "PUBLIC", "RANGE", "RECLAIM", "REFERENCES",
      "RESET", "REUSE", "RIGHT", "ROWS", "ROWSETLIMIT", "RULE", "SEARCH", "SELECT", "SEQUENCE", "SESSION_USER",
      "SETOF", "SHOW", "SOME", "SUBSTRING", "SYSTEM", "TABLE", "THEN", "TIES", "TIME", "TIMESTAMP", "TO",
      "TRAILING", "TRANSACTION", "TRIGGER", "TRIM", "TRUE", "UNBOUNDED", "UNION", "UNIQUE", "USER", "USING",
      "VACUUM", "VARCHAR", "VERBOSE", "VIEW", "WHEN", "WHERE", "WITH", "WRITE", "ABSOLUTE", "ACTION", "ADD",
      "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS",
      "ASC", "ASSERTION", "AT", "AUTHORIZATION", "BEFORE", "BEGIN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH",
      "BREADTH", "BY", "CALL", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK",
      "CLASS", "CLOB", "CLOSE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMPLETION", "CONNECT",
      "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTINUE", "CORRESPONDING", "CREATE", "CROSS",
      "CUBE", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_",
      "TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL",
      "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DEPTH", "DEREF", "DESC", "DESCRIBE",
      "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT",
      "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELSE", "END_EXEC", "END", "EQUALS", "ESCAPE",
      "EVERY", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXTERNAL", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR",
      "FOREIGN", "FOUND", "FREE", "FROM", "FULL", "FUNCTION", "GENERAL", "GET", "GLOBAL", "GO", "GOTO", "GRANT",
      "GROUP", "GROUPING", "HAVING", "HOST", "HOUR", "IDENTITY", "IGNORE", "IMMEDIATE", "IN", "INDICATOR",
      "INITIALIZE", "INITIALLY", "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL",
      "INTO", "IS", "ISOLATION", "ITERATE", "JOIN", "KEY", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING",
      "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "MAP",
      "MATCH", "MINUTE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR",
      "NCLOB", "NEW", "NEXT", "NO", "NONE", "NOT", "NULL", "NUMERIC", "OBJECT", "OF", "OFF", "OLD", "ON",
      "ONLY", "OPEN", "OPERATION", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT", "PAD",
      "PARAMETER", "PARAMETERS", "PARTIAL", "PATH", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE",
      "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "READS", "REAL", "RECURSIVE",
      "REF", "REFERENCES", "REFERENCING", "RELATIVE", "RESTRICT", "RESULT", "RETURN", "RETURNS", "REVOKE",
      "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL",
      "SEARCH", "SECOND", "SECTION", "SELECT", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS", "SIZE",
      "SMALLINT", "SOME", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING",
      "START", "STATE", "STATEMENT", "STATIC", "STRUCTURE", "SYSTEM_USER", "TABLE", "TEMPORARY", "TERMINATE",
      "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION",
      "TRANSLATION", "TREAT", "TRIGGER", "TRUE", "UNDER", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE",
      "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VIEW", "WHEN", "WHENEVER",
      "WHERE", "WITH", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE" }, nativeMeta.getReservedWords() );

    assertFalse( nativeMeta.isDefaultingToUppercase() );
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );
    assertArrayEquals( new String[] { "nzjdbc.jar" }, nativeMeta.getUsedLibraries() ); // this is way wrong

  }

  @Test
  public void testSQLStatements() {
    assertEquals( " limit 15", nativeMeta.getLimitClause( 15 ) );
    assertEquals( "SELECT * FROM FOO limit 1", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT * FROM FOO limit 1", nativeMeta.getSQLTableExists( "FOO" ) );

    assertEquals( "SELECT FOO FROM BAR limit 1", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( "SELECT FOO FROM BAR limit 1", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );
    assertEquals( "select next value for FOO", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "select last_value from FOO", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT seqname AS sequence_name from _v_sequence where seqname = 'foo'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT seqname AS sequence_name from _v_sequence", nativeMeta.getSQLListOfSequences() );
    assertNull( nativeMeta.getAddColumnStatement( "", null, "", false, "", false ) );
    assertNull( nativeMeta.getDropColumnStatement( "", null, "", false, "", false ) );
    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO MODIFY COLUMN BAR" + lineSep + ";" + lineSep,
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), null, false, null, false ) ); // Pretty sure this is a bug ...
    assertNull( nativeMeta.getSQLListOfProcedures() );
    assertNull( nativeMeta.getSQLLockTables( new String[] { } ) );
    assertNull( nativeMeta.getSQLUnlockTables( new String[] { } ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO date",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), null, null, false, true, false ) );
    assertEquals( "boolean",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), null, null, false, false, false ) );

    assertEquals( "",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 0, 0 ), null, null, false, false, false ) );
    assertEquals( "byteint",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 1, 0 ), null, null, false, false, false ) );
    assertEquals( "byteint",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 2, 0 ), null, null, false, false, false ) );
    assertEquals( "smallint",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 3, 0 ), null, null, false, false, false ) );
    assertEquals( "smallint",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 4, 0 ), null, null, false, false, false ) );
    for ( int i = 5; i < 10; i++ ) {
      assertEquals( "integer",
          nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", i, 0 ), null, null, false, false, false ) );
    }
    assertEquals( "bigint",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), null, null, false, false, false ) );
    assertEquals( "",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", -22, 3 ), null, null, false, false, false ) );
    assertEquals( "",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 0, 3 ), null, null, false, false, false ) );
    assertEquals( "real",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 1, 3 ), null, null, false, false, false ) ); // pretty sure this is a bug ...
    assertEquals( "real",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 2, 3 ), null, null, false, false, false ) ); // pretty sure this is a bug ...
    for ( int i = 3; i < 9; i++ ) {
      assertEquals( "real",
          nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", i, 3 ), null, null, false, false, false ) );
    }
    for ( int i = 10; i < 18; i++ ) {
      assertEquals( "double",
          nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", i, 3 ), null, null, false, false, false ) );
    }
    assertEquals( "numeric(18, 3)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 18, 3 ), null, null, false, false, false ) );
    assertEquals( "numeric(19)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 19, -12 ), null, null, false, false, false ) );

    assertEquals( "varchar(32767)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", ( NetezzaDatabaseMeta.MAX_CHAR_LEN + 2 ), 0 ), null, null, false, false, false ) );
    assertEquals( "varchar(10)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 10, 0 ), null, null, false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 10, 0 ), null, null, false, false, false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( " UNKNOWN" + lineSep,
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 10, 0 ), null, null, false, false, true ) );
  }

}
