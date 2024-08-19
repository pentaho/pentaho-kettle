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

import java.sql.ResultSet;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class MSSQLServerDatabaseMetaTest {
  MSSQLServerDatabaseMeta nativeMeta;
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @Before
  public void setupOnce() throws Exception {
    nativeMeta = new MSSQLServerDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertFalse( nativeMeta.supportsCatalogs() );
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 1433, nativeMeta.getDefaultDatabasePort() );
    assertEquals( "net.sourceforge.jtds.jdbc.Driver", nativeMeta.getDriverClass() );

    assertEquals( "jdbc:jtds:sqlserver://FOO/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) );
    assertEquals( "jdbc:jtds:sqlserver://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );

    assertEquals( "FOO.BAR", nativeMeta.getSchemaTableCombination( "FOO", "BAR" ) );
    assertFalse( nativeMeta.supportsBitmapIndex() );

    assertArrayEquals( new String[] {
      /*
       * Transact-SQL Reference: Reserved Keywords Includes future keywords: could be reserved in future releases of SQL
       * Server as new features are implemented. REMARK: When SET QUOTED_IDENTIFIER is ON (default), identifiers can be
       * delimited by double quotation marks, and literals must be delimited by single quotation marks. When SET
       * QUOTED_IDENTIFIER is OFF, identifiers cannot be quoted and must follow all Transact-SQL rules for identifiers.
       */
      "ABSOLUTE", "ACTION", "ADD", "ADMIN", "AFTER", "AGGREGATE", "ALIAS", "ALL", "ALLOCATE", "ALTER", "AND",
      "ANY", "ARE", "ARRAY", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "BACKUP", "BEFORE", "BEGIN",
      "BETWEEN", "BINARY", "BIT", "BLOB", "BOOLEAN", "BOTH", "BREADTH", "BREAK", "BROWSE", "BULK", "BY", "CALL",
      "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHECK", "CHECKPOINT", "CLASS",
      "CLOB", "CLOSE", "CLUSTERED", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "COMPLETION",
      "COMPUTE", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONSTRUCTOR", "CONTAINS",
      "CONTAINSTABLE", "CONTINUE", "CONVERT", "CORRESPONDING", "CREATE", "CROSS", "CUBE", "CURRENT",
      "CURRENT_DATE", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER",
      "CURSOR", "CYCLE", "DATA", "DATABASE", "DATE", "DAY", "DBCC", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE",
      "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DENY", "DEPTH", "DEREF", "DESC", "DESCRIBE", "DESCRIPTOR",
      "DESTROY", "DESTRUCTOR", "DETERMINISTIC", "DIAGNOSTICS", "DICTIONARY", "DISCONNECT", "DISK", "DISTINCT",
      "DISTRIBUTED", "DOMAIN", "DOUBLE", "DROP", "DUMMY", "DUMP", "DYNAMIC", "EACH", "ELSE", "END", "END-EXEC",
      "EQUALS", "ERRLVL", "ESCAPE", "EVERY", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXIT",
      "EXTERNAL", "FALSE", "FETCH", "FILE", "FILLFACTOR", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FREE",
      "FREETEXT", "FREETEXTTABLE", "FROM", "FULL", "FUNCTION", "GENERAL", "GET", "GLOBAL", "GO", "GOTO",
      "GRANT", "GROUP", "GROUPING", "HAVING", "HOLDLOCK", "HOST", "HOUR", "IDENTITY", "IDENTITY_INSERT",
      "IDENTITYCOL", "IF", "IGNORE", "IMMEDIATE", "IN", "INDEX", "INDICATOR", "INITIALIZE", "INITIALLY",
      "INNER", "INOUT", "INPUT", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION",
      "ITERATE", "JOIN", "KEY", "KILL", "LANGUAGE", "LARGE", "LAST", "LATERAL", "LEADING", "LEFT", "LESS",
      "LEVEL", "LIKE", "LIMIT", "LINENO", "LOAD", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOCATOR", "MAP",
      "MATCH", "MINUTE", "MODIFIES", "MODIFY", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR",
      "NCLOB", "NEW", "NEXT", "NO", "NOCHECK", "NONCLUSTERED", "NONE", "NOT", "NULL", "NULLIF", "NUMERIC",
      "OBJECT", "OF", "OFF", "OFFSETS", "OLD", "ON", "ONLY", "OPEN", "OPENDATASOURCE", "OPENQUERY",
      "OPENROWSET", "OPENXML", "OPERATION", "OPTION", "OR", "ORDER", "ORDINALITY", "OUT", "OUTER", "OUTPUT",
      "OVER", "PAD", "PARAMETER", "PARAMETERS", "PARTIAL", "PATH", "PERCENT", "PLAN", "POSTFIX", "PRECISION",
      "PREFIX", "PREORDER", "PREPARE", "PRESERVE", "PRIMARY", "PRINT", "PRIOR", "PRIVILEGES", "PROC",
      "PROCEDURE", "PUBLIC", "RAISERROR", "READ", "READS", "READTEXT", "REAL", "RECONFIGURE", "RECURSIVE",
      "REF", "REFERENCES", "REFERENCING", "RELATIVE", "REPLICATION", "RESTORE", "RESTRICT", "RESULT", "RETURN",
      "RETURNS", "REVOKE", "RIGHT", "ROLE", "ROLLBACK", "ROLLUP", "ROUTINE", "ROW", "ROWCOUNT", "ROWGUIDCOL",
      "ROWS", "RULE", "SAVE", "SAVEPOINT", "SCHEMA", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SECTION", "SELECT",
      "SEQUENCE", "SESSION", "SESSION_USER", "SET", "SETS", "SETUSER", "SHUTDOWN", "SIZE", "SMALLINT", "SOME",
      "SPACE", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "START", "STATE",
      "STATEMENT", "STATIC", "STATISTICS", "STRUCTURE", "SYSTEM_USER", "TABLE", "TEMPORARY", "TERMINATE",
      "TEXTSIZE", "THAN", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TOP",
      "TRAILING", "TRAN", "TRANSACTION", "TRANSLATION", "TREAT", "TRIGGER", "TRUE", "TRUNCATE", "TSEQUAL",
      "UNDER", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPDATETEXT", "USAGE", "USE", "USER", "USING",
      "VALUE", "VALUES", "VARCHAR", "VARIABLE", "VARYING", "VIEW", "WAITFOR", "WHEN", "WHENEVER", "WHERE",
      "WHILE", "WITH", "WITHOUT", "WORK", "WRITE", "WRITETEXT", "YEAR", "ZONE" }, nativeMeta.getReservedWords() );

    assertArrayEquals( new String[] { "jtds-1.2.5.jar" }, nativeMeta.getUsedLibraries() );
    assertEquals( "http://jtds.sourceforge.net/faq.html#urlFormat", nativeMeta.getExtraOptionsHelpText() );
    assertTrue( nativeMeta.supportsSchemas() );
    assertTrue( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.supportsSequenceNoMaxValueOption() );
    assertFalse( nativeMeta.useSafePoints() );
    assertTrue( nativeMeta.supportsErrorHandlingOnBatchUpdates() );
    assertEquals( 8000, nativeMeta.getMaxVARCHARLength() );
  }


  @Test
  public void testSQLStatements() {
    assertEquals( "SELECT TOP 1 * FROM FOO", nativeMeta.getSQLQueryFields( "FOO" ) );
    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "SELECT top 0 * FROM FOO WITH (UPDLOCK, HOLDLOCK);"
      + lineSep + "SELECT top 0 * FROM BAR WITH (UPDLOCK, HOLDLOCK);" + lineSep,
      nativeMeta.getSQLLockTables(  new String[] { "FOO", "BAR" } ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DATETIME",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DATETIME",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO DROP COLUMN BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ALTER COLUMN BAR VARCHAR(100)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "", true ) );

    assertEquals( "select o.name from sysobjects o, sysusers u where  xtype in ( 'FN', 'P' ) and o.uid = u.uid order by o.name",
        nativeMeta.getSQLListOfProcedures( "FOO" ) );

    assertEquals( "select name from sys.schemas", nativeMeta.getSQLListOfSchemas() );
    assertEquals( "insert into FOO(FOOVERSION) values (1)", nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
    assertEquals( "SELECT NEXT VALUE FOR FOO", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "SELECT current_value FROM sys.sequences WHERE name = 'FOO'", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT 1 FROM sys.sequences WHERE name = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT name FROM sys.sequences", nativeMeta.getSQLListOfSequences() );
  }

  @Test
  public void testGetFieldDefinition( ) throws Exception {
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "BAR" ), "", "", false, false, false ) );

    assertEquals( "BIGINT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 10, 0 ), "", "", false, false, false ) );

    assertEquals( "BIGINT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "BAR", 10, 0 ), "", "", false, false, false ) );

    assertEquals( "BIGINT",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "BAR", 10, 0 ), "", "", false, false, false ) );

    assertEquals( "INT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 0, 0 ), "", "", false, false, false ) );

    assertEquals( "INT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 5, 0 ), "", "", false, false, false ) );

    assertEquals( "DECIMAL(10,3)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 10, 3 ), "", "", false, false, false ) );

    assertEquals( "DECIMAL(10,3)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "BAR", 10, 3 ), "", "", false, false, false ) );

    assertEquals( "DECIMAL(21,4)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "BAR", 21, 4 ), "", "", false, false, false ) );

    assertEquals( "TEXT",
        nativeMeta.getFieldDefinition( new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", "", false, false, false ) );

    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "BAR", 15, 0 ), "", "", false, false, false ) );

    assertEquals( "FLOAT(53)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 10, -7 ), "", "", false, false, false ) ); // Bug here - invalid SQL

    assertEquals( "DECIMAL(22,7)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "BAR", 22, 7 ), "", "", false, false, false ) );
    assertEquals( "FLOAT(53)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", -10, 7 ), "", "", false, false, false ) );
    assertEquals( "DECIMAL(5,7)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR", 5, 7 ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "BAR" ), "", "", false, false, false ) );

    assertEquals( "BIGINT PRIMARY KEY IDENTITY(0,1)",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "BAR" ), "BAR", "", true, false, false ) );

    assertEquals( "BIGINT PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR" ), "BAR", "", false, false, false ) );

    assertEquals( "BIGINT PRIMARY KEY IDENTITY(0,1)",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "BAR" ), "", "BAR", true, false, false ) );
    assertEquals( "BIGINT PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "BAR" ), "", "BAR", false, false, false ) );
    assertEquals( "VARBINARY(MAX)",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( ), "", "BAR", false, false, false ) );
    assertEquals( "VARBINARY(MAX)",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "BAR" ), "", "BAR", false, false, false ) );
  }

  private int rowCnt = 0;
  private String[] row1 = new String[] { "ROW1COL1", "ROW1COL2" };
  private String[] row2 = new String[] { "ROW2COL1", "ROW2COL2" };

  @Test
  public void testCheckIndexExists() throws Exception {
    String expectedSQL = "select i.name table_name, c.name column_name from     sysindexes i, sysindexkeys k, syscolumns c where    i.name = 'FOO' AND      i.id = k.id AND      i.id = c.id AND      k.colid = c.colid "; // yes, space at the end like in the dbmeta
    Database db = Mockito.mock(  Database.class );
    RowMetaInterface rm = Mockito.mock( RowMetaInterface.class );
    ResultSet rs = Mockito.mock( ResultSet.class );
    DatabaseMeta dm = Mockito.mock( DatabaseMeta.class );
    Mockito.when( dm.getQuotedSchemaTableCombination( "", "FOO" ) ).thenReturn( "FOO" );
    Mockito.when( rs.next() ).thenReturn( rowCnt < 2 );
    Mockito.when( db.openQuery( expectedSQL ) ).thenReturn( rs );
    Mockito.when( db.getReturnRowMeta() ).thenReturn( rm );
    Mockito.when( rm.getString( row1, "column_name", "" ) ).thenReturn( "ROW1COL2" );
    Mockito.when( rm.getString( row2, "column_name", "" ) ).thenReturn( "ROW2COL2" );
    Mockito.when( db.getRow( rs ) ).thenAnswer( new Answer<Object[]>() {
        @Override
        public Object[] answer( InvocationOnMock invocation ) throws Throwable {
          rowCnt++;
          if ( rowCnt == 1 ) {
            return row1;
          } else if ( rowCnt == 2 ) {
            return row2;
          } else {
            return null;
          }
        }
    } );
    Mockito.when(  db.getDatabaseMeta() ).thenReturn( dm );
    assertTrue( nativeMeta.checkIndexExists( db, "", "FOO", new String[] { "ROW1COL2", "ROW2COL2" } ) );
    assertFalse( nativeMeta.checkIndexExists( db, "", "FOO", new String[] { "ROW2COL2", "NOTTHERE" } ) );
    assertFalse( nativeMeta.checkIndexExists( db, "", "FOO", new String[] { "NOTTHERE", "ROW1COL2" } ) );

  }

}
