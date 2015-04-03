package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SqlScriptParserTest {

  @Test
  public void testSplit() {
    testSplit( (String) null, new String[0] );
    testSplit( "", new String[0] );
    testSplit( " ", new String[0] );
    testSplit( "SELECT 1;SELECT 2", "SELECT 1", "SELECT 2" );
    testSplit( "SELECT '1;2'", "SELECT '1;2'" );
    testSplit( "SELECT \"1;2\"", "SELECT \"1;2\"" );
    testSplit( "SELECT -- 1;2", "SELECT -- 1;2" );
    testSplit( "SELECT /*1;2*/", "SELECT /*1;2*/" );
    testSplit( "SELECT /1;2", "SELECT /1", "2" );
    testSplit( "SELECT /1;;;;2", "SELECT /1", "2" );
    testSplit( "SELECT /1;\n  \n", "SELECT /1" );
    testSplit( "SELECT \"hello\\\"world\" FROM dual", "SELECT \"hello\\\"world\" FROM dual" );
    testSplit( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\");",
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" );
    testSplit( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value');",
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" );
    testSplit( "SELECT \"test\\\";SELECT 1", "SELECT \"test\\\";SELECT 1" );
    testSplit( "SELECT 'test\\';SELECT 1", "SELECT 'test\\';SELECT 1" );
    testSplit( "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\");SELECT 1",
      "create table pdi13654 (col1 string) TBLPROPERTIES (\"quoteChar\"=\"\\\"\", \"escapeChar\"=\"\\\\\")", "SELECT 1" );
  }

  private void testSplit( String sql, String... result ) {
    List<String> real = SqlScriptParser.getInstance().split( sql );
    assertEquals( Arrays.asList( result ), real );
  }

  @Test
  public void testRemoveComments() {
    testRemoveComments( null, null );
    testRemoveComments( "", "" );
    testRemoveComments( "SELECT col1 FROM test", "SELECT col1 FROM test" );
    testRemoveComments( "SELECT col1 FROM test --end comment", "SELECT col1 FROM test " );
    testRemoveComments( "SELECT \n col1, col2\n FROM \n test", "SELECT \n col1, col2\n FROM \n test" );
    testRemoveComments( "SELECT \n \"col1\", col2\n FROM --test\n test",
      "SELECT \n \"col1\", col2\n FROM \n test" );
    testRemoveComments( "SELECT /* \"my_column'\" */ col1 FROM /* 'my_table' */ account",
      "SELECT  col1 FROM  account" );
    testRemoveComments( "SELECT '/' as col1, '*/*' as regex ", "SELECT '/' as col1, '*/*' as regex " );
    testRemoveComments( "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') ",
      "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') " );
    testRemoveComments( "SELECT /* my data*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
      "SELECT  col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
        "SELECT /*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
        "SELECT \n/*+ ORACLE hint*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'",
        "SELECT \n/*+ ORACLE hint*/\n col1, col2, col3 FROM account WHERE name = 'Pentaho'" );
    testRemoveComments( "SELECT \"hello\\\"world\" FROM dual", "SELECT \"hello\\\"world\" FROM dual" );
    testRemoveComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")",
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES (\"prop1\" = \"my\\\"value\")" );
    testRemoveComments( "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')",
      "CREATE TABLE test1 (col1 STRING) TBLPROPERTIES ('prop1' = 'my\\\"value')" );
  }

  private void testRemoveComments( String input, String expected ) {
    assertEquals( expected, SqlScriptParser.getInstance().removeComments( input ) );
  }
}
