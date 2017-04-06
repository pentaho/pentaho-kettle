/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SqlScriptParserTest {

  private final String ANY_DB = "ANY_DB";
  private final String ORACLE = "ORACLE";

  @Test
  public void testSplit() {
    testSplit( (String) null, null, new String[0] );
    testSplit( "", "", new String[0] );
    testSplit( " ", " ", new String[0] );
    testSplit( "SELECT 1;SELECT 2", ANY_DB, "SELECT 1", "SELECT 2" );
    testSplit( "SELECT '1;2'", ANY_DB, "SELECT '1;2'" );
    testSplit( "SELECT \"1;2\"", ANY_DB, "SELECT \"1;2\"" );
    testSplit( "SELECT -- 1;2", ANY_DB, "SELECT -- 1;2" );
    testSplit( "SELECT /*1;2*/", ANY_DB, "SELECT /*1;2*/" );
    testSplit( "SELECT /1;2", ANY_DB, "SELECT /1", "2" );
    testSplit( "SELECT /1;;;;2", ANY_DB, "SELECT /1", "2" );
    testSplit( "SELECT /1;\n  \n", ANY_DB, "SELECT /1" );
    testSplit( "SELECT \"hello\\\"world\" FROM dual", ANY_DB, "SELECT \"hello\\\"world\" FROM dual" );
    testSplit( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\");", ANY_DB,
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" );
    testSplit( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value');", ANY_DB,
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" );
    testSplit( "SELECT \"test\\\";SELECT 1", ANY_DB, "SELECT \"test\\\";SELECT 1" );
    testSplit( "SELECT 'test\\';SELECT 1", ANY_DB, "SELECT 'test\\';SELECT 1" );
    testSplit( "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\");SELECT 1", ANY_DB,
      "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\")", "SELECT 1" );
    //PDI-16224
    testSplit( "SELECT 1 from test where t='\\'||t=a;", ORACLE, "SELECT 1 from test where t='\\'||t=a" );
  }

  private void testSplit( String sql, String databaseProductName, String... result ) {
    SqlScriptParser sqlScriptParser = new SqlScriptParser( databaseProductName );
    List<String> real = sqlScriptParser.split( sql );
    assertEquals( Arrays.asList( result ), real );
  }

  @Test
  public void testRemoveComments() {
    testRemoveComments( null, null, null );
    testRemoveComments( "", "", "" );
    testRemoveComments( "SELECT col1 FROM test", ANY_DB, "SELECT col1 FROM test" );
    testRemoveComments( "SELECT col1 FROM test --end comment", ANY_DB, "SELECT col1 FROM test " );
    testRemoveComments( "SELECT \n col1, col2\n FROM \n test", ANY_DB, "SELECT \n col1, col2\n FROM \n test" );
    testRemoveComments( "SELECT \n \"col1\", col2\n FROM --test\n test", ANY_DB,
      "SELECT \n \"col1\", col2\n FROM \n test" );
    testRemoveComments( "SELECT /* \"my_column'\" */ col1 FROM /* 'my_table' */ account", ANY_DB,
      "SELECT  col1 FROM  account" );
    testRemoveComments( "SELECT '/' as col1, '*/*' as regex ", ANY_DB, "SELECT '/' as col1, '*/*' as regex " );
    testRemoveComments( "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') ", "ANY_DB",
      "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') " );
    testRemoveComments( "SELECT /* my data*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'", ANY_DB,
      "SELECT  col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'", ANY_DB,
        "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'", ANY_DB,
        "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'", ANY_DB,
        "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \"hello\\\"world\" FROM dual", ANY_DB, "SELECT \"hello\\\"world\" FROM dual" );
    testRemoveComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")", ANY_DB,
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" );
    testRemoveComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')", ANY_DB,
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" );
    //PDI-16224
    testRemoveComments( "SELECT 1 from test where t='\\'/* comment */||t=a", ORACLE, "SELECT 1 from test where t='\\'||t=a" );
  }

  private void testRemoveComments( String input, String databaseProductName, String expected ) {
    SqlScriptParser sqlScriptParser = new SqlScriptParser( databaseProductName );
    assertEquals( expected, sqlScriptParser.removeComments( input ) );
  }
}
