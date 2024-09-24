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
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class DB2DatabaseMetaTest {
  private DB2DatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new DB2DatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 50000, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsSetCharacterStream() );
    assertEquals( "com.ibm.db2.jcc.DB2Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:db2://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertTrue( nativeMeta.supportsSchemas() );
    assertArrayEquals( new String[] {
      // http://publib.boulder.ibm.com/infocenter/db2luw/v8/index.jsp?topic=/com.ibm.db2.udb.doc/admin/r0001095.htm
      // For portability across the DB2 Universal Database products, the following should be considered reserved
      // words.
      // The following list also contains the ISO/ANSI SQL99 reserved words for future compatibility.
      "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER",
      "AND", "ANY", "APPLICATION", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "ASSOCIATE", "ASUTIME", "AT",
      "AUDIT", "AUTHORIZATION", "AUX", "AUXILIARY", "BEFORE", "BEGIN", "BETWEEN", "BINARY", "BIT", "BLOB",
      "BOOLEAN", "BOTH", "BREADTH", "BUFFERPOOL", "BY", "CACHE", "CALL", "CALLED", "CAPTURE", "CARDINALITY",
      "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CCSID", "CHAR", "CHARACTER", "CHECK", "CLASS", "CLOB",
      "CLOSE", "CLUSTER", "COLLATE", "COLLATION", "COLLECTION", "COLLID", "COLUMN", "COMMENT", "COMMIT",
      "COMPLETION", "CONCAT", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR",
      "CONTAINS", "CONTINUE", "CORRESPONDING", "COUNT", "COUNT_BIG", "CREATE", "CROSS", "CUBE", "CURRENT",
      "CURRENT_DATE", "CURRENT_LC_CTYPE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_SERVER", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", "CURRENT_USER", "CURSOR", "CYCLE", "DATA", "DATABASE", "DATE",
      "DAY", "DAYS", "DB2GENERAL", "DB2GENRL", "DB2SQL", "DBINFO", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
      "DEFAULT", "DEFAULTS", "DEFERRABLE", "DEFERRED", "DEFINITION", "DELETE", "DEPTH", "DEREF", "DESC",
      "DESCRIBE", "DESCRIPTOR", "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY",
      "DISALLOW", "DISCONNECT", "DISTINCT", "DO", "DOMAIN", "DOUBLE", "DROP", "DSNHATTR", "DSSIZE", "DYNAMIC",
      "EACH", "EDITPROC", "ELSE", "ELSEIF", "ENCODING", "END", "END-EXEC", "END-EXEC1", "EQUALS", "ERASE",
      "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXEC", "EXECUTE", "EXISTS", "EXIT", "EXTERNAL",
      "FALSE", "FENCED", "FETCH", "FIELDPROC", "FILE", "FINAL", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND",
      "FREE", "FROM", "FULL", "FUNCTION", "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT",
      "GRAPHIC", "GROUP", "GROUPING", "HANDLER", "HAVING", "HOLD", "HOST", "HOUR", "HOURS", "IDENTITY", "IF",
      "IGNORE", "IMMEDIATE", "IN", "INCLUDING", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", "INITIALIZE",
      "INITIALLY", "INNER", "INOUT", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTEGRITY",
      "INTERSECT", "INTERVAL", "INTO", "IS", "ISOBID", "ISOLATION", "ITERATE", "JAR", "JAVA", "JOIN", "KEY",
      "LABEL", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LC_CTYPE", "LEADING", "LEAVE", "LEFT", "LESS", "LEVEL",
      "LIKE", "LIMIT", "LINKTYPE", "LOCAL", "LOCALE", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "LOCATORS",
      "LOCK", "LOCKMAX", "LOCKSIZE", "LONG", "LOOP", "MAP", "MATCH", "MAXVALUE", "MICROSECOND", "MICROSECONDS",
      "MINUTE", "MINUTES", "MINVALUE", "MODE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "MONTHS", "NAMES",
      "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NEW_TABLE", "NEXT", "NO", "NOCACHE", "NOCYCLE",
      "NODENAME", "NODENUMBER", "NOMAXVALUE", "NOMINVALUE", "NONE", "NOORDER", "NOT", "NULL", "NULLS",
      "NUMERIC", "NUMPARTS", "OBID", "OBJECT", "OF", "OFF", "OLD", "OLD_TABLE", "ON", "ONLY", "OPEN",
      "OPERATION", "OPTIMIZATION", "OPTIMIZE", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT",
      "OVERRIDING", "PACKAGE", "PAD", "PARAMETER", "PARAMETERS", "PART", "PARTIAL", "PARTITION", "PATH",
      "PIECESIZE", "PLAN", "POSITION", "POSTFIX", "PRECISION", "PREFIX", "PREORDER", "PREPARE", "PRESERVE",
      "PRIMARY", "PRIOR", "PRIQTY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "PSID", "PUBLIC", "QUERYNO", "READ",
      "READS", "REAL", "RECOVERY", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "RELATIVE", "RELEASE",
      "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART", "RESTRICT", "RESULT", "RESULT_SET_LOCATOR", "RETURN",
      "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWS", "RRN", "RUN",
      "SAVEPOINT", "SCHEMA", "SCOPE", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS", "SECQTY",
      "SECTION", "SECURITY", "SELECT", "SENSITIVE", "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS",
      "SIGNAL", "SIMPLE", "SIZE", "SMALLINT", "SOME", "SOURCE", "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL",
      "SQLEXCEPTION", "SQLID", "SQLSTATE", "SQLWARNING", "STANDARD", "START", "STATE", "STATEMENT", "STATIC",
      "STAY", "STOGROUP", "STORES", "STRUCTURE", "STYLE", "SUBPAGES", "SUBSTRING", "SYNONYM", "SYSFUN",
      "SYSIBM", "SYSPROC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESPACE", "TEMPORARY", "TERMINATE", "THAN",
      "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION",
      "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "TYPE", "UNDER", "UNDO", "UNION", "UNIQUE", "UNKNOWN",
      "UNNEST", "UNTIL", "UPDATE", "USAGE", "USER", "USING", "VALIDPROC", "VALUE", "VALUES", "VARCHAR",
      "VARIABLE", "VARIANT", "VARYING", "VCAT", "VIEW", "VOLUMES", "WHEN", "WHENEVER", "WHERE", "WHILE", "WITH",
      "WITHOUT", "WLM", "WORK", "WRITE", "YEAR", "YEARS", "ZONE" }, nativeMeta.getReservedWords() );

    assertEquals( 32672, nativeMeta.getMaxVARCHARLength() );
    assertTrue( nativeMeta.supportsBatchUpdates() );
    assertFalse( nativeMeta.supportsGetBlob() );
    assertArrayEquals( new String[] { "db2jcc.jar", "db2jcc_license_cu.jar" }, nativeMeta.getUsedLibraries() );
    assertTrue( nativeMeta.supportsSequences() );
    assertEquals( ":", nativeMeta.getExtraOptionIndicator() );
    assertFalse( nativeMeta.supportsSequenceNoMaxValueOption() );
    assertTrue( nativeMeta.requiresCastToVariousForIsNull() );
    assertTrue( nativeMeta.isDisplaySizeTwiceThePrecision() );
    assertFalse( nativeMeta.supportsNewLinesInSQL() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "ALTER TABLE FOO ACTIVATE NOT LOGGED INITIALLY WITH EMPTY TABLE", nativeMeta.getTruncateTableStatement( "FOO" ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR CLOB",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BLOB(" + ( nativeMeta.getMaxVARCHARLength() + 2 ) + ")",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 10 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR BLOB",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR CHAR(200) FOR BIT DATA",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR", 200, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD COLUMN BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    String lineSep = System.getProperty( "line.separator" );

    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + lineSep + ";" + lineSep + "ALTER TABLE FOO ADD COLUMN BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "LOCK TABLE FOO IN SHARE MODE;" + lineSep + "LOCK TABLE BAR IN SHARE MODE;" + lineSep,
        nativeMeta.getSQLLockTables( new String[] { "FOO", "BAR" } ) );

    assertNull( nativeMeta.getSQLUnlockTables( new String[] { "FOO", "BAR" } ) );
    assertEquals( "SELECT SEQNAME FROM SYSCAT.SEQUENCES", nativeMeta.getSQLListOfSequences() );
    assertEquals( "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQNAME = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQSCHEMA = 'FOO' AND SEQNAME = 'BAR'", nativeMeta.getSQLSequenceExists( "FOO.BAR" ) );
    assertEquals( "SELECT PREVIOUS VALUE FOR FOO FROM SYSIBM.SYSDUMMY1", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT NEXT VALUE FOR FOO FROM SYSIBM.SYSDUMMY1", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "insert into FOO(FOOVERSION) values (1)", nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHARACTER(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "FOO", "", true, false, false ) );

    assertEquals( "BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 12, 0 ), "FOO", "", true, false, false ) );
    assertEquals( "INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NOCACHE)",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 12, 0 ), "FOO", "", true, false, false ) );

    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 0, 0 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(12)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 12, 0 ), "", "", false, false, false ) ); // Pretty sure this is a bug - should be an Integer here.
    assertEquals( "DECIMAL(12, 4)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 12, 4 ), "", "", false, false, false ) );

    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 10, 0 ), "", "", false, false, false ) );

    int realMaxBeforeCLOB = Math.max( nativeMeta.getMaxVARCHARLength(), DatabaseMeta.CLOB_LENGTH );

    int realMinBeforeCLOB = Math.min( nativeMeta.getMaxVARCHARLength(), DatabaseMeta.CLOB_LENGTH );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", realMaxBeforeCLOB + 1, 0 ), "", "", false, false, false ) );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", realMaxBeforeCLOB, 0 ), "", "", false, false, false ) );
    assertEquals( String.format( "VARCHAR(%d)", realMinBeforeCLOB - 1 ),
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", realMinBeforeCLOB - 1, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR()",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 0, 0 ), "", "", false, false, false ) ); // Definitely a bug here - VARCHAR() is not valid SQL anywhere . . .

    // Binary Stuff . . .
    assertEquals( String.format( "BLOB(%d)", realMaxBeforeCLOB + 1 ),
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", realMaxBeforeCLOB + 1, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 0, 0 ), "", "", false, false, false ) );
    assertEquals( "CHAR(150) FOR BIT DATA",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 150, 0 ), "", "", false, false, false ) );



    // Then unknown . . .
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );
  }

}
