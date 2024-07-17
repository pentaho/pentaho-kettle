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
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class VerticaDatabaseMetaTest {

  private VerticaDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new VerticaDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 5433, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "com.vertica.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:vertica://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:vertica://FOO:/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Believe this is a bug - must have the port. Inconsistent with others
    assertFalse( nativeMeta.isFetchSizeSupported() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertArrayEquals( new String[] { "vertica_2.5_jdk_5.jar" }, nativeMeta.getUsedLibraries() );
    assertEquals( 4000, nativeMeta.getMaxVARCHARLength() );
    assertArrayEquals( new String[] {
      // From "SQL Reference Manual.pdf" found on support.vertica.com
      "ABORT", "ABSOLUTE", "ACCESS", "ACTION", "ADD", "AFTER", "AGGREGATE", "ALL", "ALSO", "ALTER", "ANALYSE",
      "ANALYZE", "AND", "ANY", "ARRAY", "AS", "ASC", "ASSERTION", "ASSIGNMENT", "AT", "AUTHORIZATION",
      "BACKWARD", "BEFORE", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BIT", "BLOCK_DICT", "BLOCKDICT_COMP",
      "BOOLEAN", "BOTH", "BY", "CACHE", "CALLED", "CASCADE", "CASE", "CAST", "CATALOG_PATH", "CHAIN", "CHAR",
      "CHARACTER", "CHARACTERISTICS", "CHECK", "CHECKPOINT", "CLASS", "CLOSE", "CLUSTER", "COALESCE", "COLLATE",
      "COLUMN", "COMMENT", "COMMIT", "COMMITTED", "COMMONDELTA_COMP", "CONSTRAINT", "CONSTRAINTS", "CONVERSION",
      "CONVERT", "COPY", "CORRELATION", "CREATE", "CREATEDB", "CREATEUSER", "CROSS", "CSV", "CURRENT_DATABASE",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "CYCLE", "DATA",
      "DATABASE", "DATAPATH", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFAULTS",
      "DEFERRABLE", "DEFERRED", "DEFINER", "DELETE", "DELIMITER", "DELIMITERS", "DELTARANGE_COMP",
      "DELTARANGE_COMP_SP", "DELTAVAL", "DESC", "DETERMINES", "DIRECT", "DISTINCT", "DISTVALINDEX", "DO",
      "DOMAIN", "DOUBLE", "DROP", "EACH", "ELSE", "ENCODING", "ENCRYPTED", "END", "EPOCH", "ERROR", "ESCAPE",
      "EXCEPT", "EXCEPTIONS", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXPLAIN", "EXTERNAL", "EXTRACT",
      "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FORCE", "FOREIGN", "FORWARD", "FREEZE", "FROM", "FULL",
      "FUNCTION", "GLOBAL", "GRANT", "GROUP", "HANDLER", "HAVING", "HOLD", "HOUR", "ILIKE", "IMMEDIATE",
      "IMMUTABLE", "IMPLICIT", "IN", "IN_P", "INCLUDING", "INCREMENT", "INDEX", "INHERITS", "INITIALLY",
      "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INSTEAD", "INT", "INTEGER", "INTERSECT", "INTERVAL",
      "INTO", "INVOKER", "IS", "ISNULL", "ISOLATION", "JOIN", "KEY", "LANCOMPILER", "LANGUAGE", "LARGE", "LAST",
      "LATEST", "LEADING", "LEFT", "LESS", "LEVEL", "LIKE", "LIMIT", "LISTEN", "LOAD", "LOCAL", "LOCALTIME",
      "LOCALTIMESTAMP", "LOCATION", "LOCK", "MATCH", "MAXVALUE", "MERGEOUT", "MINUTE", "MINVALUE", "MOBUF",
      "MODE", "MONTH", "MOVE", "MOVEOUT", "MULTIALGORITHM_COMP", "MULTIALGORITHM_COMP_SP", "NAMES", "NATIONAL",
      "NATURAL", "NCHAR", "NEW", "NEXT", "NO", "NOCREATEDB", "NOCREATEUSER", "NODE", "NODES", "NONE", "NOT",
      "NOTHING", "NOTIFY", "NOTNULL", "NOWAIT", "NULL", "NULLIF", "NUMERIC", "OBJECT", "OF", "OFF", "OFFSET",
      "OIDS", "OLD", "ON", "ONLY", "OPERATOR", "OPTION", "OR", "ORDER", "OUT", "OUTER", "OVERLAPS", "OVERLAY",
      "OWNER", "PARTIAL", "PARTITION", "PASSWORD", "PLACING", "POSITION", "PRECISION", "PREPARE", "PRESERVE",
      "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURAL", "PROCEDURE", "PROJECTION", "QUOTE", "READ", "REAL",
      "RECHECK", "RECORD", "RECOVER", "REFERENCES", "REFRESH", "REINDEX", "REJECTED", "RELATIVE", "RELEASE",
      "RENAME", "REPEATABLE", "REPLACE", "RESET", "RESTART", "RESTRICT", "RETURNS", "REVOKE", "RIGHT", "RLE",
      "ROLLBACK", "ROW", "ROWS", "RULE", "SAVEPOINT", "SCHEMA", "SCROLL", "SECOND", "SECURITY", "SEGMENTED",
      "SELECT", "SEQUENCE", "SERIALIZABLE", "SESSION", "SESSION_USER", "SET", "SETOF", "SHARE", "SHOW",
      "SIMILAR", "SIMPLE", "SMALLINT", "SOME", "SPLIT", "STABLE", "START", "STATEMENT", "STATISTICS", "STDIN",
      "STDOUT", "STORAGE", "STRICT", "SUBSTRING", "SYSID", "TABLE", "TABLESPACE", "TEMP", "TEMPLATE",
      "TEMPORARY", "TERMINATOR", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMESTAMPTZ", "TIMETZ", "TO", "TOAST",
      "TRAILING", "TRANSACTION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TRUE_P", "TRUNCATE", "TRUSTED", "TYPE",
      "UNCOMMITTED", "UNENCRYPTED", "UNION", "UNIQUE", "UNKNOWN", "UNLISTEN", "UNSEGMENTED", "UNTIL", "UPDATE",
      "USAGE", "USER", "USING", "VACUUM", "VALID", "VALIDATOR", "VALINDEX", "VALUES", "VARCHAR", "VARYING",
      "VERBOSE", "VIEW", "VOLATILE", "WHEN", "WHERE", "WITH", "WITHOUT", "WORK", "WRITE", "YEAR", "ZONE" }, nativeMeta.getReservedWords() );

    assertArrayEquals( new String[] {}, nativeMeta.getViewTypes() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertTrue( nativeMeta.supportsBooleanDataType() );
    assertTrue( nativeMeta.requiresCastToVariousForIsNull() );
    assertEquals( "?", nativeMeta.getExtraOptionIndicator() );
    assertEquals( "&", nativeMeta.getExtraOptionSeparator() );
    assertTrue( nativeMeta.supportsSequences() );
    assertFalse( nativeMeta.supportsTimeStampToDateConversion() );
    assertFalse( nativeMeta.supportsGetBlob() );
    assertTrue( nativeMeta.isDisplaySizeTwiceThePrecision() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( " LIMIT 5", nativeMeta.getLimitClause( 5 ) );
    assertEquals( "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "--NOTE: Table cannot be altered unless all projections are dropped.\nALTER TABLE FOO ALTER COLUMN BAR SET DATA TYPE VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "SELECT FOO FROM BAR LIMIT 1", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );
    assertEquals( "SELECT * FROM FOO LIMIT 1", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT 1 FROM FOO LIMIT 1", nativeMeta.getSQLTableExists( "FOO" ) );
    assertEquals( "SELECT sequence_name FROM sequences WHERE sequence_name = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT sequence_name FROM sequences", nativeMeta.getSQLListOfSequences() );
    assertEquals( "SELECT nextval('FOO')", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "SELECT currval('FOO')", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );

  }

  @Test
  public void testGetFieldDefinition() throws Exception {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "BOOLEAN",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "", "", false, false, false ) );
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO" ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO" ), "", "", false, false, false ) );
    assertEquals( "VARCHAR",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( "VARBINARY",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 0, 0 ), "", "", false, false, false ) );
    assertEquals( "VARBINARY(50)",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 50, 0 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
