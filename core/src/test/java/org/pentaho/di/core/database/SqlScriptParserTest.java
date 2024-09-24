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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class SqlScriptParserTest {

  private SqlScriptParser sqlScriptParser = new SqlScriptParser( true );
  private SqlScriptParser oracleSqlScriptParser = new SqlScriptParser( false );

  @Test
  public void testSplit() {
    assertEquals( Arrays.asList( new String[0] ), sqlScriptParser.split( null ) );
    assertEquals( Arrays.asList( new String[0] ), sqlScriptParser.split( "" ) );
    assertEquals( Arrays.asList( new String[0] ), sqlScriptParser.split( " " ) );
    assertEquals( Arrays.asList( "SELECT 1", "SELECT 2" ), sqlScriptParser.split( "SELECT 1;SELECT 2" ) );
    assertEquals( Collections.singletonList( "SELECT '1;2'" ), sqlScriptParser.split( "SELECT '1;2'" ) );
    assertEquals( Collections.singletonList( "SELECT \"1;2\"" ), sqlScriptParser.split( "SELECT \"1;2\"" ) );
    assertEquals( Collections.singletonList( "SELECT -- 1;2" ), sqlScriptParser.split( "SELECT -- 1;2" ) );
    assertEquals( Collections.singletonList( "SELECT /*1;2*/" ), sqlScriptParser.split( "SELECT /*1;2*/" ) );
    assertEquals( Arrays.asList( "SELECT /1", "2" ), sqlScriptParser.split( "SELECT /1;2" ) );
    assertEquals( Arrays.asList( "SELECT /1", "2" ), sqlScriptParser.split( "SELECT /1;;;;2" ) );
    assertEquals( Collections.singletonList( "SELECT /1" ), sqlScriptParser.split( "SELECT /1;\n  \n" ) );
    assertEquals( Collections.singletonList( "SELECT \"hello\\\"world\" FROM dual" ),
            sqlScriptParser.split( "SELECT \"hello\\\"world\" FROM dual" ) );
    assertEquals( Collections.singletonList( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" ),
            sqlScriptParser.split( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\");" ) );
    assertEquals( Collections.singletonList( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" ),
            sqlScriptParser.split( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value');" ) );
    assertEquals( Collections.singletonList( "SELECT \"test\\\";SELECT 1" ), sqlScriptParser.split( "SELECT \"test\\\";SELECT 1" ) );
    assertEquals( Collections.singletonList( "SELECT 'test\\';SELECT 1" ), sqlScriptParser.split( "SELECT 'test\\';SELECT 1" ) );
    assertEquals( Arrays.asList( "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\")", "SELECT 1" ),
            sqlScriptParser.split( "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\");SELECT 1" ) );
    //PDI-16224
    assertEquals( Collections.singletonList( "SELECT 1 from test where t='\\'||t=a" ), oracleSqlScriptParser.split( "SELECT 1 from test where t='\\'||t=a;" ) );
  }

  @Test
  public void testRemoveComments() {
    assertEquals( null, sqlScriptParser.removeComments( null ) );
    assertEquals( "", sqlScriptParser.removeComments( "" ) );
    assertEquals( "SELECT col1 FROM test", sqlScriptParser.removeComments( "SELECT col1 FROM test" ) );
    assertEquals( "SELECT col1 FROM test ", sqlScriptParser.removeComments( "SELECT col1 FROM test --end comment" ) );
    assertEquals( "SELECT \n col1, col2\n FROM \n test", sqlScriptParser.removeComments( "SELECT \n col1, col2\n FROM \n test" ) );
    assertEquals( "SELECT \n \"col1\", col2\n FROM \n test", sqlScriptParser.removeComments( "SELECT \n \"col1\", col2\n FROM --test\n test" ) );
    assertEquals( "SELECT  col1 FROM  account", sqlScriptParser.removeComments( "SELECT /* \"my_column'\" */ col1 FROM /* 'my_table' */ account" ) );
    assertEquals( "SELECT '/' as col1, '*/*' as regex ", sqlScriptParser.removeComments( "SELECT '/' as col1, '*/*' as regex " ) );
    assertEquals( "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') ",
            sqlScriptParser.removeComments( "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') " ) );
    assertEquals( "SELECT  col1, col2, col3 FROM account WHERE name = 'Pentaho'",
            sqlScriptParser.removeComments( "SELECT /* my data*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" ) );
    assertEquals( "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
            sqlScriptParser.removeComments( "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" ) );
    assertEquals( "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
            sqlScriptParser.removeComments( "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" ) );
    assertEquals( "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'",
            sqlScriptParser.removeComments( "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'" ) );
    assertEquals( "SELECT \"hello\\\"world\" FROM dual", sqlScriptParser.removeComments( "SELECT \"hello\\\"world\" FROM dual" ) );
    assertEquals( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")",
            sqlScriptParser.removeComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" ) );
    assertEquals( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')",
            sqlScriptParser.removeComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" ) );
    //PDI-16224
    assertEquals( "SELECT 1 from test where t='\\'||t=a", oracleSqlScriptParser.removeComments( "SELECT 1 from test where t='\\'/* comment */||t=a" ) );
  }

}
