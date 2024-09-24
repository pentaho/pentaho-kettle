/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

public class OracleRDBDatabaseMetaTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();
  private OracleRDBDatabaseMeta nativeMeta, jndiMeta;

  @Before
  public void setupOnce() throws Exception {
    nativeMeta = new OracleRDBDatabaseMeta();
    jndiMeta = new OracleRDBDatabaseMeta();
    nativeMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_NATIVE );
    jndiMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    KettleClientEnvironment.init();
  }


  @Test
  public void testOverriddenSettings() throws Exception {
    // Tests the settings of the Oracle Database Meta
    // according to the features of the DB as we know them

    assertEquals( -1, nativeMeta.getDefaultDatabasePort() );
    assertFalse( nativeMeta.supportsAutoInc() );
    assertEquals( "oracle.rdb.jdbc.rdbThin.Driver", nativeMeta.getDriverClass() );
    assertEquals( "jdbc:rdbThin://FOO:1024/BAR", nativeMeta.getURL( "FOO", "1024", "BAR" ) );
    assertEquals( "jdbc:rdbThin://FOO:11/:BAR", nativeMeta.getURL( "FOO", "11", ":BAR" ) );
    assertEquals( "jdbc:rdbThin://BAR:65534//FOO", nativeMeta.getURL( "BAR", "65534", "/FOO" ) );
    assertEquals( "jdbc:rdbThin://:/FOO", nativeMeta.getURL( "", "", "FOO" ) ); // Pretty sure this is a bug...
    assertEquals( "jdbc:rdbThin://null:-1/FOO", nativeMeta.getURL( null, "-1", "FOO" ) ); // Pretty sure this is a bug...
    assertEquals( "jdbc:rdbThin://null:null/FOO", nativeMeta.getURL( null, null, "FOO" ) ); // Pretty sure this is a bug...
    assertEquals( "jdbc:rdbThin://FOO:1234/BAR", nativeMeta.getURL( "FOO", "1234", "BAR" ) );
    assertEquals( "jdbc:rdbThin://:/", nativeMeta.getURL( "", "", "" ) ); // Pretty sure this is a bug...
    assertEquals( "jdbc:rdbThin://null:null/BAR", jndiMeta.getURL( null, null, "BAR" ) );
    assertFalse( nativeMeta.supportsOptionsInURL() );
    assertTrue( nativeMeta.supportsSequences() );
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
    assertArrayEquals( new String[] { "rdbthin.jar" }, nativeMeta.getUsedLibraries() );
    assertFalse( nativeMeta.supportsRepository() );
    assertEquals( 9999999, nativeMeta.getMaxVARCHARLength() );
    assertEquals( "SELECT SEQUENCE_NAME FROM USER_SEQUENCES", nativeMeta.getSQLListOfSequences() );
    assertEquals( "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = 'FOO'", nativeMeta.getSQLSequenceExists( "FOO" ) );
    assertEquals( "SELECT * FROM USER_SEQUENCES WHERE SEQUENCE_NAME = 'FOO'", nativeMeta.getSQLSequenceExists( "foo" ) );
    assertEquals( "SELECT FOO.currval FROM DUAL", nativeMeta.getSQLCurrentSequenceValue( "FOO" ) );
    assertEquals( "SELECT FOO.nextval FROM dual", nativeMeta.getSQLNextSequenceValue( "FOO" ) );
    String reusedFieldsQuery = "SELECT * FROM FOO WHERE 1=0";;
    assertEquals( reusedFieldsQuery, nativeMeta.getSQLQueryFields( "FOO" ) );
    assertEquals( reusedFieldsQuery, nativeMeta.getSQLTableExists( "FOO" ) );
    String reusedColumnsQuery = "SELECT FOO FROM BAR WHERE 1=0";
    assertEquals( reusedColumnsQuery, nativeMeta.getSQLQueryColumnFields( "FOO", "BAR" ) );
    assertEquals( reusedColumnsQuery, nativeMeta.getSQLColumnExists( "FOO", "BAR" ) );

  }


}
