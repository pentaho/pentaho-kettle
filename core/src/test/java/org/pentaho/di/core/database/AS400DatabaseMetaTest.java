/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class AS400DatabaseMetaTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  AS400DatabaseMeta nativeMeta;

  @Before
  public void setupOnce() throws Exception {
    nativeMeta = new AS400DatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    KettleClientEnvironment.init();
  }

  @Test
  public void testSettings() throws Exception {
    int[] aTypes =
        new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI };
    assertArrayEquals( aTypes, nativeMeta.getAccessTypeList() );
    assertEquals( "com.ibm.as400.access.AS400JDBCDriver", nativeMeta.getDriverClass() );
    assertEquals( 65536, nativeMeta.getMaxTextFieldLength() );
    assertEquals( "jdbc:as400://foo/bar", nativeMeta.getURL( "foo", "1500", "bar" ) ); // note - AS400 driver ignores the port
    String[] expectedReservedWords = new String[] {
      // http://publib.boulder.ibm.com/infocenter/iseries/v5r4/index.jsp
      // This is the list of currently reserved DB2 UDB for iSeries words. Words may be added at any time.
      // For a list of additional words that may become reserved in the future, see the IBM SQL and
      // ANSI reserved words in the IBM SQL Reference Version 1 SC26-3255.
      "ACTIVATE", "ADD", "ALIAS", "ALL", "ALLOCATE", "ALLOW", "ALTER", "AND", "ANY", "AS", "ASENSITIVE", "AT",
      "ATTRIBUTES", "AUTHORIZATION", "BEGIN", "BETWEEN", "BINARY", "BY", "CACHE", "CALL", "CALLED", "CARDINALITY",
      "CASE", "CAST", "CCSID", "CHAR", "CHARACTER", "CHECK", "CLOSE", "COLLECTION", "COLUMN", "COMMENT", "COMMIT",
      "CONCAT", "CONDITION", "CONNECT", "CONNECTION", "CONSTRAINT", "CONTAINS", "CONTINUE", "COUNT", "COUNT_BIG",
      "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_PATH", "CURRENT_SCHEMA", "CURRENT_SERVER", "CURRENT_TIME",
      "CURRENT_TIMESTAMP", "CURRENT_TIMEZONE", "CURRENT_USER", "CURSOR", "CYCLE", "DATABASE", "DATAPARTITIONNAME",
      "DATAPARTITIONNUM", "DATE", "DAY", "DAYS", "DBINFO", "DBPARTITIONNAME", "DBPARTITIONNUM", "DB2GENERAL",
      "DB2GENRL", "DB2SQL", "DEALLOCATE", "DECLARE", "DEFAULT", "DEFAULTS", "DEFINITION", "DELETE", "DENSERANK",
      "DENSE_RANK", "DESCRIBE", "DESCRIPTOR", "DETERMINISTIC", "DIAGNOSTICS", "DISABLE", "DISALLOW", "DISCONNECT",
      "DISTINCT", "DO", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELSE", "ELSEIF", "ENABLE", "ENCRYPTION", "END", "ENDING",
      "END-EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXCLUDING", "EXCLUSIVE", "EXECUTE", "EXISTS", "EXIT",
      "EXTERNAL", "EXTRACT", "FENCED", "FETCH", "FILE", "FINAL", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION",
      "GENERAL", "GENERATED", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GRAPHIC", "GROUP", "HANDLER", "HASH",
      "HASHED_VALUE", "HAVING", "HINT", "HOLD", "HOUR", "HOURS", "IDENTITY", "IF", "IMMEDIATE", "IN", "INCLUDING",
      "INCLUSIVE", "INCREMENT", "INDEX", "INDICATOR", "INHERIT", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INTEGRITY",
      "INTERSECT", "INTO", "IS", "ISOLATION", "ITERATE", "JAVA", "JOIN", "KEY", "LABEL", "LANGUAGE", "LATERAL", "LEAVE",
      "LEFT", "LIKE", "LINKTYPE", "LOCAL", "LOCALDATE", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LOOP",
      "MAINTAINED", "MATERIALIZED", "MAXVALUE", "MICROSECOND", "MICROSECONDS", "MINUTE", "MINUTES", "MINVALUE", "MODE",
      "MODIFIES", "MONTH", "MONTHS", "NEW", "NEW_TABLE", "NEXTVAL", "NO", "NOCACHE", "NOCYCLE", "NODENAME",
      "NODENUMBER", "NOMAXVALUE", "NOMINVALUE", "NOORDER", "NORMALIZED", "NOT", "NULL", "OF", "OLD", "OLD_TABLE", "ON",
      "OPEN", "OPTIMIZE", "OPTION", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERRIDING", "PACKAGE", "PAGESIZE",
      "PARAMETER", "PART", "PARTITION", "PARTITIONING", "PARTITIONS", "PASSWORD", "PATH", "POSITION", "PREPARE",
      "PREVVAL", "PRIMARY", "PRIVILEGES", "PROCEDURE", "PROGRAM", "QUERY", "RANGE", "RANK", "READ", "READS", "RECOVERY",
      "REFERENCES", "REFERENCING", "REFRESH", "RELEASE", "RENAME", "REPEAT", "RESET", "RESIGNAL", "RESTART", "RESULT",
      "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROUTINE", "ROW", "ROWNUMBER", "ROW_NUMBER", "ROWS", "RRN",
      "RUN", "SAVEPOINT", "SCHEMA", "SCRATCHPAD", "SCROLL", "SEARCH", "SECOND", "SECONDS", "SELECT", "SENSITIVE",
      "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SIGNAL", "SIMPLE", "SOME", "SOURCE", "SPECIFIC", "SQL", "SQLID",
      "STACKED", "START", "STARTING", "STATEMENT", "STATIC", "SUBSTRING", "SUMMARY", "SYNONYM", "SYSTEM_USER", "TABLE",
      "THEN", "TIME", "TIMESTAMP", "TO", "TRANSACTION", "TRIGGER", "TRIM", "TYPE", "UNDO", "UNION", "UNIQUE", "UNTIL",
      "UPDATE", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARIABLE", "VARIANT", "VERSION", "VIEW", "VOLATILE",
      "WHEN", "WHERE", "WHILE", "WITH", "WITHOUT", "WRITE", "YEAR", "YEARS" };

    assertArrayEquals( expectedReservedWords, nativeMeta.getReservedWords() );
    assertArrayEquals( new String[] { "jt400.jar" }, nativeMeta.getUsedLibraries() );
    assertFalse( nativeMeta.supportsFloatRoundingOnUpdate() );
    assertEquals( 32672, nativeMeta.getMaxVARCHARLength() );
    assertTrue( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.supportsSequenceNoMaxValueOption() );

  }

  @Test
  public void testSQLStatements() {
    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(100)", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR",
        100, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR SET TIMESTAMP", nativeMeta.getModifyColumnStatement( "FOO",
        new ValueMetaTimestamp( "BAR" ), "", false, "", false ) ); // Fixed: http://jira.pentaho.com/browse/PDI-15570

    assertEquals( "SELECT SEQNAME FROM SYSCAT.SEQUENCES", nativeMeta.getSQLListOfSequences() );
    assertEquals( "SELECT * FROM SYSCAT.SEQUENCES WHERE SEQNAME = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT PREVIOUS VALUE FOR FOO FROM SYSIBM.SYSDUMMY1", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT NEXT VALUE FOR FOO FROM SYSIBM.SYSDUMMY1", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "DOUBLE",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO" ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(5)",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 5, 0 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(5, 3)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 5, 3 ), "", "", false, false, false ) );

    assertEquals( "DECIMAL",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 0, 3 ), "", "", false, false, false ) ); // This is a bug

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", DatabaseMeta.CLOB_LENGTH + 1, 0 ), "", "", false, false, false ) );

    assertEquals( String.format( "VARCHAR(%d)", ( nativeMeta.getMaxVARCHARLength() - 1 ) ),
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", nativeMeta.getMaxVARCHARLength() - 1, 0 ), "", "", false, false, false ) );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", nativeMeta.getMaxVARCHARLength() + 1, 0 ), "", "", false, false, false ) );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", DatabaseMeta.CLOB_LENGTH - 1, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );

  }

}
