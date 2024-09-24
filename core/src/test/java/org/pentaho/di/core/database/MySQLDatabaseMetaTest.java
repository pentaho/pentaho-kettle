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

import static org.mockito.BDDMockito.doReturn;
import static org.mockito.BDDMockito.mock;
import static org.mockito.Mockito.doThrow;

import com.mysql.cj.jdbc.Driver;
import org.junit.Before;
import org.junit.Test;
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

import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

public class MySQLDatabaseMetaTest {
  MySQLDatabaseMeta nativeMeta;

  @Before
  public void setupBefore() throws Exception {
    nativeMeta = new MySQLDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    Class.forName( Driver.class.getName() );
  }

  @Test
  public void testSettings() throws Exception {
    assertArrayEquals( new int[] { DatabaseMeta.TYPE_ACCESS_NATIVE, DatabaseMeta.TYPE_ACCESS_JNDI },
        nativeMeta.getAccessTypeList() );
    assertEquals( 3306, nativeMeta.getDefaultDatabasePort() );
    assertTrue( nativeMeta.supportsAutoInc() );
    assertEquals( 1, nativeMeta.getNotFoundTK( true ) );
    assertEquals( 0, nativeMeta.getNotFoundTK( false ) );
    assertEquals( "com.mysql.cj.jdbc.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:mysql://FOO:BAR/WIBBLE", nativeMeta.getURL( "FOO", "BAR", "WIBBLE" ) );
    assertEquals( "jdbc:mysql://FOO/WIBBLE", nativeMeta.getURL( "FOO", "", "WIBBLE" ) );
    assertEquals( "&", nativeMeta.getExtraOptionSeparator() );
    assertEquals( "?", nativeMeta.getExtraOptionIndicator() );
    assertFalse( nativeMeta.supportsTransactions() );
    assertFalse( nativeMeta.supportsBitmapIndex() );
    assertTrue( nativeMeta.supportsViews() );
    assertFalse( nativeMeta.supportsSynonyms() );
    assertArrayEquals( new String[] { "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN",
      "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK",
      "COLLATE", "COLUMN", "CONDITION", "CONNECTION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS",
      "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES",
      "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED",
      "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH",
      "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FOR", "FORCE",
      "FOREIGN", "FROM", "FULLTEXT", "GOTO", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND",
      "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT",
      "INT", "INTEGER", "INTERVAL", "INTO", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT",
      "LIKE", "LIMIT", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCATE", "LOCK", "LONG", "LONGBLOB", "LONGTEXT",
      "LOOP", "LOW_PRIORITY", "MATCH", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND",
      "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE",
      "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "POSITION", "PRECISION", "PRIMARY", "PROCEDURE",
      "PURGE", "READ", "READS", "REAL", "REFERENCES", "REGEXP", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESTRICT",
      "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE",
      "SEPARATOR", "SET", "SHOW", "SMALLINT", "SONAME", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE",
      "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STRAIGHT_JOIN",
      "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO",
      "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME",
      "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "WHEN", "WHERE", "WHILE", "WITH",
      "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL" }, nativeMeta.getReservedWords() );

    assertEquals( "`", nativeMeta.getStartQuote() );
    assertEquals( "`", nativeMeta.getEndQuote() );
    assertTrue( nativeMeta.needsToLockAllTables() );
    assertEquals( "https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-configuration-properties.html", nativeMeta.getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "mysql-connector-java-3.1.14-bin.jar" }, nativeMeta.getUsedLibraries() ); // this is way wrong
    assertTrue( nativeMeta.isSystemTable( "sysTest" ) );
    assertTrue( nativeMeta.isSystemTable( "dtproperties" ) );
    assertFalse( nativeMeta.isSystemTable( "SysTest" ) );
    assertFalse( nativeMeta.isSystemTable( "dTproperties" ) );
    assertFalse( nativeMeta.isSystemTable( "Testsys" ) );
    assertTrue( nativeMeta.isMySQLVariant() );
    assertFalse( nativeMeta.releaseSavepoint() );
    assertTrue( nativeMeta.supportsErrorHandlingOnBatchUpdates() );
    assertFalse( nativeMeta.isRequiringTransactionsOnQueries() );
    assertTrue( nativeMeta.supportsRepository() );
  }

  @Test
  public void testSQLStatements() {
    assertEquals( " LIMIT 15", nativeMeta.getLimitClause( 15 ) );
    assertEquals( "SELECT * FROM FOO LIMIT 0", nativeMeta.getSQLQueryFields(  "FOO" ) );
    assertEquals( "SELECT * FROM FOO LIMIT 0", nativeMeta.getSQLTableExists(  "FOO" ) );
    assertEquals( "SELECT FOO FROM BAR LIMIT 0", nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DATETIME",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "BAR" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DATETIME",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR CHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBoolean( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR INT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 0, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR INT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 0 ), "", false, "", false ) );


    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 3 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(21, 4)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 21, 4 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR MEDIUMTEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", nativeMeta.getMaxVARCHARLength() + 2, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR VARCHAR(15)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 10, -7 ), "", false, "", false ) ); // Bug here - invalid SQL

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(22, 7)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", -10, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR DOUBLE",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 5, 7 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR  UNKNOWN",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInternetAddress( "BAR" ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber( "BAR", 26, 8 ), "BAR", true, "", false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP BAR" + lineSep,
        nativeMeta.getDropColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR VARCHAR(15)",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR", 15, 0 ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO MODIFY BAR TINYTEXT",
        nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "", true ) );

    assertEquals( "ALTER TABLE FOO ADD BAR INT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR", 4, 0 ), "", true, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT NOT NULL PRIMARY KEY",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger( "BAR" ), "BAR", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR BIGINT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 10, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR DECIMAL(22)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBigNumber( "BAR", 22, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR CHAR(1)",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 1, 0 ), "", false, "", false ) );

    assertEquals( "ALTER TABLE FOO ADD BAR LONGTEXT",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString( "BAR", 16777250, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD BAR LONGBLOB",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary( "BAR", 16777250, 0 ), "", false, "", false ) );

    assertEquals( "LOCK TABLES FOO WRITE, BAR WRITE;" + lineSep,
        nativeMeta.getSQLLockTables(  new String[] { "FOO", "BAR" } ) );

    assertEquals( "UNLOCK TABLES", nativeMeta.getSQLUnlockTables( new String[] { } ) );

    assertEquals( "insert into FOO(FOOKEY, FOOVERSION) values (1, 1)", nativeMeta.getSQLInsertAutoIncUnknownDimensionRow( "FOO", "FOOKEY", "FOOVERSION" ) );
  }

  /**
   *
   * @return
   * @throws Exception
   */
  private ResultSetMetaData getResultSetMetaData() throws Exception {
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );

    /**
     * Fields setup around the following query:
     *
     * select
     *   CUSTOMERNUMBER as NUMBER
     * , CUSTOMERNAME as NAME
     * , CONTACTLASTNAME as LAST_NAME
     * , CONTACTFIRSTNAME as FIRST_NAME
     * , 'MySQL' as DB
     * , 'NoAliasText'
     * from CUSTOMERS
     * ORDER BY CUSTOMERNAME;
     */

    doReturn( "NUMBER" ).when( resultSetMetaData ).getColumnLabel( 1 );
    doReturn( "NAME" ).when( resultSetMetaData ).getColumnLabel( 2 );
    doReturn( "LAST_NAME" ).when( resultSetMetaData ).getColumnLabel( 3 );
    doReturn( "FIRST_NAME" ).when( resultSetMetaData ).getColumnLabel( 4 );
    doReturn( "DB" ).when( resultSetMetaData ).getColumnLabel( 5 );
    doReturn( "NoAliasText" ).when( resultSetMetaData ).getColumnLabel( 6 );

    doReturn( "CUSTOMERNUMBER" ).when( resultSetMetaData ).getColumnName( 1 );
    doReturn( "CUSTOMERNAME" ).when( resultSetMetaData ).getColumnName( 2 );
    doReturn( "CONTACTLASTNAME" ).when( resultSetMetaData ).getColumnName( 3 );
    doReturn( "CONTACTFIRSTNAME" ).when( resultSetMetaData ).getColumnName( 4 );
    doReturn( "MySQL" ).when( resultSetMetaData ).getColumnName( 5 );
    doReturn( "NoAliasText" ).when( resultSetMetaData ).getColumnName( 6 );

    return resultSetMetaData;
  }

  /**
   *
   * @return
   * @throws Exception
   */
  private ResultSetMetaData getResultSetMetaDataException() throws Exception {
    ResultSetMetaData resultSetMetaData = mock( ResultSetMetaData.class );

    doThrow( new SQLException() ).when( resultSetMetaData ).getColumnLabel( 1 );
    doThrow( new SQLException() ).when( resultSetMetaData ).getColumnName( 1 );

    return resultSetMetaData;
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldNumber() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "NUMBER", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 1 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "NAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 2 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldLastName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "LAST_NAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 3 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldFirstName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "FIRST_NAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 4 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldDB() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "DB", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 5 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverGreaterThanThreeFieldNoAliasText() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "NoAliasText", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 6 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldCustomerNumber() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "CUSTOMERNUMBER", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 1 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldCustomerName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "CUSTOMERNAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 2 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldContactLastName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "CONTACTLASTNAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 3 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldContactFirstName() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "CONTACTFIRSTNAME", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 4 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldMySQL() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "MySQL", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 5 ) );
  }

  @Test
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeFieldNoAliasText() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    assertEquals( "NoAliasText", new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaData(), 6 ) );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameNullDBMetaDataException() throws Exception {
    new MySQLDatabaseMeta().getLegacyColumnName( null, getResultSetMetaData(), 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameNullRSMetaDataException() throws Exception {
    new MySQLDatabaseMeta().getLegacyColumnName( mock( DatabaseMetaData.class ), null, 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameDriverGreaterThanThreeException() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 5 ).when( databaseMetaData ).getDriverMajorVersion();

    new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaDataException(), 1 );
  }

  @Test( expected = KettleDatabaseException.class )
  public void testGetLegacyColumnNameDriverLessOrEqualToThreeException() throws Exception {
    DatabaseMetaData databaseMetaData = mock( DatabaseMetaData.class );
    doReturn( 3 ).when( databaseMetaData ).getDriverMajorVersion();

    new MySQLDatabaseMeta().getLegacyColumnName( databaseMetaData, getResultSetMetaDataException(), 1 );
  }

  @Test
  public void testGetDefaultOptions() {
    MySQLDatabaseMeta mySQLDatabaseMeta = new MySQLDatabaseMeta();
    mySQLDatabaseMeta.setPluginId( "foobar" );
    Map<String, String> map =mySQLDatabaseMeta.getDefaultOptions();
    assertNotNull( map );
    assertEquals( 2, map.size() );
    for ( String key : map.keySet() ) {
      assert( key.startsWith( "foobar." ) );
    }
  }
}
