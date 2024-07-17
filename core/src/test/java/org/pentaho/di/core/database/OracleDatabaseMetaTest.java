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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleDatabaseException;
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
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class OracleDatabaseMetaTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  private OracleDatabaseMeta nativeMeta, ociMeta;

  @Before
  public void setupOnce() throws Exception {
    nativeMeta = new OracleDatabaseMeta();
    ociMeta = new OracleDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    ociMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_OCI );
    //nativeMeta.setSupportsTimestampDataType( true );
    KettleClientEnvironment.init();
  }

  @Test
  public void testOverriddenSettings() throws Exception {
    // Tests the settings of the Oracle Database Meta
    // according to the features of the DB as we know them

    assertEquals( 1521, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertFalse( nativeMeta.needsToLockAllTables() );
    assertEquals( "oracle.jdbc.driver.OracleDriver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:oracle:thin:@FOO:1024:BAR", nativeMeta.getURL( "FOO", "1024", "BAR" ) );
    assertEquals( "jdbc:oracle:thin:@FOO:11:BAR", nativeMeta.getURL( "FOO", "11", ":BAR" ) );
    assertEquals( "jdbc:oracle:thin:@BAR:65534/FOO", nativeMeta.getURL( "BAR", "65534", "/FOO" ) );
    assertEquals( "jdbc:oracle:thin:@FOO", nativeMeta.getURL( "", "", "FOO" ) );
    assertEquals( "jdbc:oracle:thin:@FOO", nativeMeta.getURL( null, "-1", "FOO" ) );
    assertEquals( "jdbc:oracle:thin:@FOO", nativeMeta.getURL( null, null, "FOO" ) );
    assertEquals( "jdbc:oracle:thin:@FOO:1234:BAR", nativeMeta.getURL( "FOO", "1234", "BAR" ) );
    assertEquals( "jdbc:oracle:thin:@", nativeMeta.getURL( "", "", "" ) ); // Pretty sure this is a bug...
    assertEquals( "jdbc:oracle:oci:@BAR", ociMeta.getURL( null, null, "BAR" ) );
    assertEquals(
        "jdbc:oracle:oci:@(description=(address=(host=FOO)(protocol=tcp)(port=9876))(connect_data=(sid=BAR)))", ociMeta
            .getURL( "FOO", "9876", "BAR" ) );
    try {
      ociMeta.getURL( null, null, null );
      fail( "Expected KettleDatabaseException here" );
    } catch ( KettleDatabaseException expected ) {
      // Keep going ...
    }
    try {
      ociMeta.getURL( "", "", "" );
      fail( "Expected KettleDatabaseException here" );
    } catch ( KettleDatabaseException expected ) {
      // Keep going ...
    }
    assertFalse( nativeMeta.supportsOptionsInURL() );
    assertTrue( nativeMeta.supportsSequences() );
    assertTrue( nativeMeta.supportsSequenceNoMaxValueOption() );
    assertTrue( nativeMeta.useSchemaNameForTableList() );
    assertTrue( nativeMeta.supportsSynonyms() );
    String[] reservedWords =
        new String[] { "ACCESS", "ADD", "ALL", "ALTER", "AND", "ANY", "ARRAYLEN", "AS", "ASC", "AUDIT", "BETWEEN", "BY",
          "CHAR", "CHECK", "CLUSTER", "COLUMN", "COMMENT", "COMPRESS", "CONNECT", "CREATE", "CURRENT", "DATE",
          "DECIMAL", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "ELSE", "EXCLUSIVE", "EXISTS", "FILE", "FLOAT",
          "FOR", "FROM", "GRANT", "GROUP", "HAVING", "IDENTIFIED", "IMMEDIATE", "IN", "INCREMENT", "INDEX", "INITIAL",
          "INSERT", "INTEGER", "INTERSECT", "INTO", "IS", "LEVEL", "LIKE", "LOCK", "LONG", "MAXEXTENTS", "MINUS",
          "MODE", "MODIFY", "NOAUDIT", "NOCOMPRESS", "NOT", "NOTFOUND", "NOWAIT", "NULL", "NUMBER", "OF", "OFFLINE",
          "ON", "ONLINE", "OPTION", "OR", "ORDER", "PCTFREE", "PRIOR", "PRIVILEGES", "PUBLIC", "RAW", "RENAME",
          "RESOURCE", "REVOKE", "ROW", "ROWID", "ROWLABEL", "ROWNUM", "ROWS", "SELECT", "SESSION", "SET", "SHARE",
          "SIZE", "SMALLINT", "SQLBUF", "START", "SUCCESSFUL", "SYNONYM", "SYSDATE", "TABLE", "THEN", "TO", "TRIGGER",
          "UID", "UNION", "UNIQUE", "UPDATE", "USER", "VALIDATE", "VALUES", "VARCHAR", "VARCHAR2", "VIEW", "WHENEVER",
          "WHERE", "WITH" };
    assertArrayEquals( reservedWords, nativeMeta.getReservedWords() );
    assertEquals( "http://download.oracle.com/docs/cd/B19306_01/java.102/b14355/urls.htm#i1006362", nativeMeta
        .getExtraOptionsHelpText() );
    assertArrayEquals( new String[] { "ojdbc14.jar", "orai18n.jar" }, nativeMeta.getUsedLibraries() );
    assertTrue( nativeMeta.requiresCreateTablePrimaryKeyAppend() );
    assertTrue( nativeMeta.supportsPreparedStatementMetadataRetrieval() ); // Since PDI-19514
    String quoteTest1 = "FOO 'BAR' \r TEST \n";
    String quoteTest2 = "FOO 'BAR' \\r TEST \\n";
    assertEquals( "'FOO ''BAR'' '||chr(10)||' TEST '||chr(13)||''", nativeMeta.quoteSQLString( quoteTest1 ) );
    assertEquals( "'FOO ''BAR'' \\r TEST \\n'", nativeMeta.quoteSQLString( quoteTest2 ) );
    assertFalse( nativeMeta.releaseSavepoint() );
    Variables v = new Variables();
    v.setVariable( "FOOVARIABLE", "FOOVALUE" );
    DatabaseMeta dm = new DatabaseMeta();
    dm.setDatabaseInterface( nativeMeta );
    assertEquals( "TABLESPACE FOOVALUE", nativeMeta.getTablespaceDDL( v, dm, "${FOOVARIABLE}" ) );
    assertEquals( "", nativeMeta.getTablespaceDDL( v, dm, "" ) );
    assertFalse( nativeMeta.supportsErrorHandlingOnBatchUpdates() );
    assertTrue( nativeMeta.supportsRepository() );
    assertEquals( 2000, nativeMeta.getMaxVARCHARLength() );
    assertFalse( nativeMeta.supportsTimestampDataType() );
    assertEquals( 32, nativeMeta.getMaxColumnsInIndex() );
  }

  @Test
  public void testOverriddenSQLStatements() throws Exception {
    assertEquals( " WHERE ROWNUM <= 5", nativeMeta.getLimitClause( 5 ) );
    String reusedFieldsQuery = "SELECT * FROM FOO WHERE 1=0";
    assertEquals( reusedFieldsQuery, nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( reusedFieldsQuery, nativeMeta.getSQLTableExists( "FOO" ) );
    String reusedColumnsQuery = "SELECT FOO FROM BAR WHERE 1=0";
    assertEquals( reusedColumnsQuery, nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( reusedColumnsQuery, nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );
    assertEquals( "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = 'FOO'", nativeMeta.getSQLSequenceExists( "foo" ) );

    assertEquals( "SELECT * FROM ALL_SEQUENCES WHERE SEQUENCE_NAME = 'BAR' AND SEQUENCE_OWNER = 'FOO'", nativeMeta
        .getSQLSequenceExists( "FOO.BAR" ) );
    assertEquals( "SELECT * FROM ALL_SEQUENCES WHERE SEQUENCE_NAME = 'BAR' AND SEQUENCE_OWNER = 'FOO'", nativeMeta
        .getSQLSequenceExists( "foo.bar" ) );

    assertEquals( "SELECT FOO.currval FROM DUAL", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT FOO.nextval FROM dual", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO DATE ) ",
        nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "FOO" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO DATE ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaDate( "FOO" ), "",
        false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO VARCHAR2(15) ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaString(
        "FOO", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO INTEGER ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaInteger(
        "FOO", 15, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO NUMBER(15, 10) ) ", nativeMeta.getAddColumnStatement( "FOO",
        new ValueMetaBigNumber(
        "FOO", 15, 10 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO NUMBER(15, 10) ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaNumber(
        "FOO", 15, 10 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO BLOB ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBinary(
        "FOO", 2048, 0 ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO CHAR(1) ) ", nativeMeta.getAddColumnStatement( "FOO", new ValueMetaBoolean(
        "FOO" ), "", false, "", false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO  UNKNOWN ) ", nativeMeta.getAddColumnStatement( "FOO",
        new ValueMetaInternetAddress( "FOO" ), "", false, "", false ) );

    String lineSep = System.getProperty( "line.separator" );
    assertEquals( "ALTER TABLE FOO DROP ( BAR ) " + lineSep, nativeMeta.getDropColumnStatement(
        "FOO", new ValueMetaString( "BAR" ), "", false, "", false ) );
    String modColStmtExpected =
        "ALTER TABLE FOO ADD ( BAR_KTL VARCHAR2(2000) ) ;" + lineSep + "UPDATE FOO SET BAR_KTL=BAR;" + lineSep
            + "ALTER TABLE FOO DROP ( BAR ) " + lineSep + ";" + lineSep + "ALTER TABLE FOO ADD ( BAR VARCHAR2(2000) ) ;"
            + lineSep + "UPDATE FOO SET BAR=BAR_KTL;" + lineSep + "ALTER TABLE FOO DROP ( BAR_KTL ) " + lineSep;
    assertEquals( modColStmtExpected, nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "BAR" ), "", false, "",
        false ) );
    modColStmtExpected =
        "ALTER TABLE \"FOO\" ADD ( BAR_KTL VARCHAR2(2000) ) ;" + lineSep + "UPDATE \"FOO\" SET BAR_KTL=BAR;" + lineSep
            + "ALTER TABLE \"FOO\" DROP ( BAR ) " + lineSep + ";" + lineSep + "ALTER TABLE \"FOO\" ADD ( BAR VARCHAR2(2000) ) ;"
            + lineSep + "UPDATE \"FOO\" SET BAR=BAR_KTL;" + lineSep + "ALTER TABLE \"FOO\" DROP ( BAR_KTL ) " + lineSep;
    assertEquals( modColStmtExpected, nativeMeta.getModifyColumnStatement( "\"FOO\"", new ValueMetaString( "BAR" ), "", false, "",
        false ) );


    modColStmtExpected =
        "ALTER TABLE FOO ADD ( A12345678901234567890123456789_KTL VARCHAR2(2000) ) ;" + lineSep
            + "UPDATE FOO SET A12345678901234567890123456789_KTL=A1234567890123456789012345678901234567890;" + lineSep
            + "ALTER TABLE FOO DROP ( A1234567890123456789012345678901234567890 ) " + lineSep + ";" + lineSep
            + "ALTER TABLE FOO ADD ( A1234567890123456789012345678901234567890 VARCHAR2(2000) ) ;" + lineSep
            + "UPDATE FOO SET A1234567890123456789012345678901234567890=A12345678901234567890123456789_KTL;" + lineSep
            + "ALTER TABLE FOO DROP ( A12345678901234567890123456789_KTL ) " + lineSep;
    assertEquals( modColStmtExpected, nativeMeta.getModifyColumnStatement( "FOO", new ValueMetaString( "A1234567890123456789012345678901234567890" ), "", false, "",
        false ) );

    String expectedProcSql =
        "SELECT DISTINCT DECODE(package_name, NULL, '', package_name||'.') || object_name " + "FROM user_arguments "
            + "ORDER BY 1";

    assertEquals( expectedProcSql, nativeMeta.getSQLListOfProcedures() );

    String expectedLockOneItem = "LOCK TABLE FOO IN EXCLUSIVE MODE;" + lineSep;
    assertEquals( expectedLockOneItem, nativeMeta.getSQLLockTables( new String[] { "FOO" } ) );
    String expectedLockMultiItem =
        "LOCK TABLE FOO IN EXCLUSIVE MODE;" + lineSep + "LOCK TABLE BAR IN EXCLUSIVE MODE;" + lineSep;
    assertEquals( expectedLockMultiItem, nativeMeta.getSQLLockTables( new String[] { "FOO", "BAR" } ) );
    assertNull( nativeMeta.getSQLUnlockTables( null ) ); // Commit unlocks tables
    assertEquals( "SELECT SEQUENCE_NAME FROM all_sequences", nativeMeta.getSQLListOfSequences() );
    assertEquals(
        "BEGIN EXECUTE IMMEDIATE 'DROP TABLE FOO'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;",
        nativeMeta.getDropTableIfExistsStatement( "FOO" ) );

  }

  @Test
  public void testGetFieldDefinition() throws Exception {
    assertEquals( "FOO DATE",
        nativeMeta.getFieldDefinition( new ValueMetaDate( "FOO" ), "", "", false, true, false ) );
    assertEquals( "DATE",
        nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );

    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaBoolean( "FOO" ), "", "", false, false, false ) );

    assertEquals( "NUMBER(5, 3)",
        nativeMeta.getFieldDefinition( new ValueMetaNumber( "FOO", 5, 3 ), "", "", false, false, false ) );
    assertEquals( "NUMBER(5)",
        nativeMeta.getFieldDefinition( new ValueMetaBigNumber( "FOO", 5, 0 ), "", "", false, false, false ) );
    assertEquals( "INTEGER",
        nativeMeta.getFieldDefinition( new ValueMetaInteger( "FOO", 17, 0 ), "", "", false, false, false ) );

    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", DatabaseMeta.CLOB_LENGTH, 0 ), "", "", false, false, false ) );
    assertEquals( "CHAR(1)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 1, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR2(15)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", 15, 0 ), "", "", false, false, false ) );
    assertEquals( "VARCHAR2(2000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO" ), "", "", false, false, false ) );
    assertEquals( "VARCHAR2(2000)",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", nativeMeta.getMaxVARCHARLength(), 0 ), "", "", false, false, false ) );
    assertEquals( "CLOB",
        nativeMeta.getFieldDefinition( new ValueMetaString( "FOO", nativeMeta.getMaxVARCHARLength() + 1, 0 ), "", "", false, false, false ) );

    assertEquals( "BLOB",
        nativeMeta.getFieldDefinition( new ValueMetaBinary( "FOO", 45, 0 ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN",
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, false ) );

    assertEquals( " UNKNOWN" + System.getProperty( "line.separator" ),
        nativeMeta.getFieldDefinition( new ValueMetaInternetAddress( "FOO" ), "", "", false, false, true ) );

  }

  private int rowCnt = 0;
  private String[] row1 = new String[] { "ROW1COL1", "ROW1COL2" };
  private String[] row2 = new String[] { "ROW2COL1", "ROW2COL2" };

  @Test
  public void testCheckIndexExists() throws Exception {
    String expectedSQL = "SELECT * FROM USER_IND_COLUMNS WHERE TABLE_NAME = 'FOO'";
    Database db = Mockito.mock(  Database.class );
    RowMetaInterface rm = Mockito.mock( RowMetaInterface.class );
    ResultSet rs = Mockito.mock( ResultSet.class );
    DatabaseMeta dm = Mockito.mock( DatabaseMeta.class );
    Mockito.when( dm.getQuotedSchemaTableCombination( "", "FOO" ) ).thenReturn( "FOO" );
    Mockito.when( rs.next() ).thenReturn( rowCnt < 2 );
    Mockito.when( db.openQuery( expectedSQL ) ).thenReturn( rs );
    Mockito.when( db.getReturnRowMeta() ).thenReturn( rm );
    Mockito.when( rm.getString( row1, "COLUMN_NAME", "" ) ).thenReturn( "ROW1COL2" );
    Mockito.when( rm.getString( row2, "COLUMN_NAME", "" ) ).thenReturn( "ROW2COL2" );
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

  @Test
  public void testSupportsTimestampDataTypeIsTrue() throws Exception {
    nativeMeta.setSupportsTimestampDataType( true );
    assertEquals( "TIMESTAMP",
      nativeMeta.getFieldDefinition( new ValueMetaTimestamp( "FOO" ), "", "", false, false, false ) );
    assertEquals( "ALTER TABLE FOO ADD ( FOO TIMESTAMP ) ",
      nativeMeta.getAddColumnStatement( "FOO", new ValueMetaTimestamp( "FOO" ), "", false, "", false ) );
  }

}
