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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertNull;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaBoolean;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaInternetAddress;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.row.value.ValueMetaTimestamp;

public class NeoviewDatabaseMetaTest {

  private NeoviewDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() {
    nativeMeta = new NeoviewDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 18650, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertEquals( 0, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "com.hp.t4jdbc.HPT4Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:hpt4jdbc://FOO:BAR/:schema=WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:hpt4jdbc://FOO:BAR/:schema=WIBBLE", nativeMeta.getURL( "FOO", "BAR", "schema=WIBBLE" ) );
    assertEquals( "jdbc:hpt4jdbc://FOO:BAR/::catalog=abc:serverDataSource=foo:schema=wibble", nativeMeta.getURL( "FOO", "BAR", ":catalog=abc:serverDataSource=foo:schema=wibble" ) ); // also pretty sure this is broken (two colons before catalog
    assertEquals( "jdbc:hpt4jdbc://FOO:BAR/:catalog=abc:serverDataSource=foo:schema=wibble", nativeMeta.getURL( "FOO", "BAR", "catalog=abc:serverDataSource=foo:schema=wibble" ) );
    assertEquals( "jdbc:hpt4jdbc://FOO:/:schema=WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) ); // Pretty sure this is a bug (colon after foo)
    assertTrue( nativeMeta.isFetchSizeSupported() );
    assertTrue( nativeMeta.supportsOptionsInURL() );
    assertTrue( nativeMeta.useSchemaNameForTableList() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertTrue( nativeMeta.supportsSynonyms() );
    assertFalse( nativeMeta.needsToLockAllTables() );
    assertTrue( nativeMeta.supportsGetBlob() );
    assertEquals( "", nativeMeta.getLimitClause( 15 ) );
    assertArrayEquals( new String[] { "hpt4jdbc.jar" }, nativeMeta.getUsedLibraries() );
    assertArrayEquals( new String[] {
      "ACTION", "FOR", "PROTOTYPE", "ADD", "FOREIGN", "PUBLIC", "ADMIN", "FOUND", "READ", "AFTER", "FRACTION",
      "READS", "AGGREGATE", "FREE", "REAL", "ALIAS", "FROM", "RECURSIVE", "ALL", "FULL", "REF", "ALLOCATE",
      "FUNCTION", "REFERENCES", "ALTER", "GENERAL", "REFERENCING", "AND", "GET", "RELATIVE", "ANY", "GLOBAL",
      "REPLACE", "ARE", "GO", "RESIGNAL", "ARRAY", "GOTO", "RESTRICT", "AS", "GRANT", "RESULT", "ASC", "GROUP",
      "RETURN", "ASSERTION", "GROUPING", "RETURNS", "ASYNC", "HAVING", "REVOKE", "AT", "HOST", "RIGHT",
      "AUTHORIZATION", "HOUR", "ROLE", "AVG", "IDENTITY", "ROLLBACK", "BEFORE", "IF", "ROLLUP", "BEGIN",
      "IGNORE", "ROUTINE", "BETWEEN", "IMMEDIATE", "ROW", "BINARY", "IN", "ROWS", "BIT", "INDICATOR",
      "SAVEPOINT", "BIT_LENGTH", "INITIALLY", "SCHEMA", "BLOB", "INNER", "SCOPE", "BOOLEAN", "INOUT", "SCROLL",
      "BOTH", "INPUT", "SEARCH", "BREADTH", "INSENSITIVE", "SECOND", "BY", "INSERT", "SECTION", "CALL", "INT",
      "SELECT", "CASE", "INTEGER", "SENSITIVE", "CASCADE", "INTERSECT", "SESSION", "CASCADED", "INTERVAL",
      "SESSION_USER", "CAST", "INTO", "SET", "CATALOG", "IS", "SETS", "CHAR", "ISOLATION", "SIGNAL",
      "CHAR_LENGTH", "ITERATE", "SIMILAR", "CHARACTER", "JOIN", "SIZE", "CHARACTER_LENGTH", "KEY", "SMALLINT",
      "CHECK", "LANGUAGE", "SOME", "CLASS", "LARGE", "CLOB", "LAST", "SPECIFIC", "CLOSE", "LATERAL",
      "SPECIFICTYPE", "COALESCE", "LEADING", "SQL", "COLLATE", "LEAVE", "SQL_CHAR", "COLLATION", "LEFT",
      "SQL_DATE", "COLUMN", "LESS", "SQL_DECIMAL", "COMMIT", "LEVEL", "SQL_DOUBLE", "COMPLETION", "LIKE",
      "SQL_FLOAT", "CONNECT", "LIMIT", "SQL_INT", "CONNECTION", "LOCAL", "SQL_INTEGER", "CONSTRAINT",
      "LOCALTIME", "SQL_REAL", "CONSTRAINTS", "LOCALTIMESTAMP", "SQL_SMALLINT", "CONSTRUCTOR", "LOCATOR",
      "SQL_TIME", "CONTINUE", "LOOP", "SQL_TIMESTAMP", "CONVERT", "LOWER", "SQL_VARCHAR", "CORRESPONDING",
      "MAP", "SQLCODE", "COUNT", "MATCH", "SQLERROR", "CREATE", "MAX", "SQLEXCEPTION", "CROSS", "MIN",
      "SQLSTATE", "CUBE", "MINUTE", "SQLWARNING", "CURRENT", "MODIFIES", "STRUCTURE", "CURRENT_DATE", "MODIFY",
      "SUBSTRING", "CURRENT_PATH", "MODULE", "SUM", "CURRENT_ROLE", "MONTH", "SYSTEM_USER", "CURRENT_TIME",
      "NAMES", "TABLE", "CURRENT_TIMESTAMP", "NATIONAL", "TEMPORARY", "CURRENT_USER", "NATURAL", "TERMINATE",
      "CURSOR", "NCHAR", "TEST", "CYCLE", "NCLOB", "THAN", "DATE", "NEW", "THEN", "DATETIME", "NEXT", "THERE",
      "DAY", "NO", "TIME", "DEALLOCATE", "NONE", "TIMESTAMP", "DEC", "NOT", "TIMEZONE_HOUR", "DECIMAL", "NULL",
      "TIMEZONE_MINUTE", "DECLARE", "NULLIF", "TO", "DEFAULT", "NUMERIC", "TRAILING", "DEFERRABLE", "OBJECT",
      "TRANSACTION", "DEFERRED", "OCTET_LENGTH", "TRANSLATE", "DELETE", "OF", "TRANSLATION", "DEPTH", "OFF",
      "TRANSPOSE", "DEREF", "OID", "TREAT", "DESC", "OLD", "TRIGGER", "DESCRIBE", "ON", "TRIM", "DESCRIPTOR",
      "ONLY", "TRUE", "DESTROY", "OPEN", "UNDER", "DESTRUCTOR", "OPERATORS", "UNION", "DETERMINISTIC", "OPTION",
      "UNIQUE", "DIAGNOSTICS", "OR", "UNKNOWN", "DISTINCT", "ORDER", "UNNEST", "DICTIONARY", "ORDINALITY",
      "UPDATE", "DISCONNECT", "OTHERS", "UPPER", "DOMAIN", "OUT", "UPSHIFT", "DOUBLE", "OUTER", "USAGE", "DROP",
      "OUTPUT", "USER", "DYNAMIC", "OVERLAPS", "USING", "EACH", "PAD", "VALUE", "ELSE", "PARAMETER", "VALUES",
      "ELSEIF", "PARAMETERS", "VARCHAR", "END", "PARTIAL", "VARIABLE", "END-EXEC", "PENDANT", "VARYING",
      "EQUALS", "POSITION", "VIEW", "ESCAPE", "POSTFIX", "VIRTUAL", "EXCEPT", "PRECISION", "VISIBLE",
      "EXCEPTION", "PREFIX", "WAIT", "EXEC", "PREORDER", "WHEN", "EXECUTE", "PREPARE", "WHENEVER", "EXISTS",
      "PRESERVE", "WHERE", "EXTERNAL", "PRIMARY", "WHILE", "EXTRACT", "PRIOR", "WITH", "FALSE", "PRIVATE",
      "WITHOUT", "FETCH", "PRIVILEGES", "WORK", "FIRST", "PROCEDURE", "WRITE", "FLOAT", "PROTECTED", "YEAR",
      "ZONE" }, nativeMeta.getReservedWords() );
    assertEquals( "https://www.hpe.com/psnow/doc/c01850029", nativeMeta.getExtraOptionsHelpText() );
    assertEquals( 4028, nativeMeta.getMaxVARCHARLength() );

  }

  @Test
  public void testSQLStatements() {
    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "SELECT [FIRST 1] * FROM FOO", nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( "SELECT [FIRST 1] * FROM FOO", nativeMeta.getSQLTableExists( "FOO" ) );
    assertEquals( "SELECT [FIRST 1] FOO FROM BAR", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( "SELECT [FIRST 1] FOO FROM BAR", nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );

    assertEquals( "DELETE FROM FOO", nativeMeta.getTruncateTableStatement( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD ( BAR VARCHAR(15) ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO DROP ( BAR ) " + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) ); // if all the others require parens, this is likely a bug

    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (0, 1)",
        nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );

    assertEquals( "LOCK TABLE FOO IN EXCLUSIVE MODE;" + lineSep + "LOCK TABLE BAR IN EXCLUSIVE MODE;" + lineSep,
        nativeMeta.getSQLLockTables( new String[] { "FOO", "BAR" } ) );

    assertNull( nativeMeta.getSQLUnlockTables( new String[] { "FOO", "BAR" } ) );
  }

  @Test
  public void testGetFieldDefinition() {
    assertEquals( "FOO TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "TIMESTAMP",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    // Primary/Tech Keys
    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", true, false, false ) );
    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "FOO", "", false, false, false ) );
    assertEquals( "INTEGER NOT NULL PRIMARY KEY",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 8, 0 ), "", "FOO", false, false, false ) );

    // Regular Integers
    assertEquals( "NUMERIC(10)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 10, 0 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC(18)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 18, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 9, 0 ), "", "", false, false, false ) );
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 19, 0 ), "", "", false, false, false ) );

    assertEquals( "NUMERIC(10, 5)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 10, 5 ), "", "", false, false, false ) );
    assertEquals( "FLOAT",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 19, 5 ), "", "", false, false, false ) );
    assertEquals( "NUMERIC(-7)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -7, -2 ), "", "", false, false, false ) ); // This is a bug...
    assertEquals( "NUMERIC(-7, 2)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", -7, 2 ), "", "", false, false, false ) ); // This is a bug ...

    // String Types
    assertEquals( "VARCHAR(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR(4028)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", nativeMeta.getMaxVARCHARLength(), 0 ), "", "", false, false, false ) );
    assertEquals( "CHAR(4029)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 4029, 0 ), "", "", false, false, false ) );
    assertEquals( "CHAR(4036)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 4036, 0 ), "", "", false, false, false ) );
    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 4037, 0 ), "", "", false, false, false ) );

    // Binary
    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 4037, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );
    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );

  }
  @Test
  public void testGetValueFromResultSet() throws Exception {
    Object rtn = null;
    ResultSet resultSet = Mockito.mock( ResultSet.class );
    ResultSetMetaData metaData = Mockito.mock( ResultSetMetaData.class );
    Mockito.when( resultSet.getMetaData() ).thenReturn( metaData );

    Mockito.when( resultSet.getTimestamp( 1 ) ).thenReturn( new java.sql.Timestamp( 65535 ) );
    Mockito.when( resultSet.getTime( 2 ) ).thenReturn( new java.sql.Time( 1000 ) );
    Mockito.when( resultSet.getTimestamp( 3 ) ).thenReturn( new java.sql.Timestamp( 65535 ) ); // ValueMetaDate -> Timestamp
    ValueMetaTimestamp ts = new ValueMetaTimestamp( "FOO" );
    ts.setOriginalColumnType( java.sql.Types.TIMESTAMP );
    ValueMetaDate tm = new ValueMetaDate( "BAR" );
    tm.setOriginalColumnType( java.sql.Types.TIME );
    ValueMetaDate dt = new ValueMetaDate( "WIBBLE" );
    dt.setOriginalColumnType( java.sql.Types.DATE );

    rtn = nativeMeta.getValueFromResultSet( resultSet, ts, 0 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Timestamp", rtn.getClass().getName() );

    rtn = nativeMeta.getValueFromResultSet( resultSet, tm, 1 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Time", rtn.getClass().getName() );

    rtn = nativeMeta.getValueFromResultSet( resultSet, dt, 2 );
    assertNotNull( rtn );
    assertEquals( "java.sql.Timestamp", rtn.getClass().getName() );

    Mockito.when( resultSet.wasNull() ).thenReturn( true );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaString( "WOBBLE" ), 3 );
    assertNull( rtn );

    // Verify that getDate is not called, getTime is called once, and getTimestamp was called 2 times (once for TimeStamp, once for Date)
    Mockito.verify( resultSet, Mockito.times( 0 ) ).getDate( Mockito.anyInt() );
    Mockito.verify( resultSet, Mockito.times( 1 ) ).getTime( Mockito.anyInt() );
    Mockito.verify( resultSet, Mockito.times( 2 ) ).getTimestamp( Mockito.anyInt() );

    // Now that the date stuff is done, validate the behaviors of other aspects of getValueFromResultSet
    Mockito.when( resultSet.wasNull() ).thenReturn( false );
    Mockito.when(  resultSet.getBoolean( 1 ) ).thenReturn( new Boolean( true ) );
    Mockito.when(  resultSet.getDouble( 1 ) ).thenReturn( new Double( 15 ) );
    Mockito.when(  resultSet.getBigDecimal( 1 ) ).thenReturn( new BigDecimal( "15" ) );
    Mockito.when(  resultSet.getLong( 1 ) ).thenReturn( new Long( "15" ) );
    Mockito.when(  resultSet.getString( 1 ) ).thenReturn( "ASTRING" );
    Mockito.when(  resultSet.getBytes( 1 ) ).thenReturn( "ASTRING".getBytes() );
    Blob mockBlob = Mockito.mock( Blob.class );
    byte[] bytes = "FOO".getBytes();
    ByteArrayInputStream bais = new ByteArrayInputStream( bytes );

    Mockito.when( mockBlob.getBinaryStream() ).thenReturn( bais );
    Mockito.when( mockBlob.length() ).thenReturn( new Long( bytes.length ) );
    Mockito.when( mockBlob.getBytes( Mockito.anyLong(), Mockito.anyInt() ) ).thenReturn( bytes );
    Mockito.when(  resultSet.getBlob( 1 ) ).thenReturn( mockBlob );

    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaBoolean( "FOO" ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof Boolean );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaNumber( "FOO", 15, 5 ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof Double );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaBigNumber( "FOO", 15, 5 ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof BigDecimal );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaInteger( "FOO", 5, 0 ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof Long );
    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaString( "FOO", 25, 0 ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof String );

    ValueMetaString binStr = new ValueMetaString( "FOO" );
    binStr.setStorageType( ValueMetaString.STORAGE_TYPE_BINARY_STRING );
    rtn = nativeMeta.getValueFromResultSet( resultSet, binStr, 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof byte[] );

    rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaBinary( "FOO", 150, 0 ), 0 );
    assertNotNull( rtn );
    assertTrue( rtn instanceof byte[] );

    try {
      Mockito.when(  resultSet.getBoolean( 15 ) ).thenThrow( new SQLException( "Expected Exception Here" ) );
      rtn = nativeMeta.getValueFromResultSet( resultSet, new ValueMetaBoolean( "FOO" ), 14 );
      fail( "Should not get here" );
    } catch ( Exception someException ) {
      assertTrue( someException instanceof KettleDatabaseException );
    }

  }

}
