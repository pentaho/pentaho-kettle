package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SimpleSqlScriptParserTest {
  @Test
  public void testSplits() {
    testSplit("SELECT 1;SELECT 2", "SELECT 1", "SELECT 2");
    testSplit("SELECT '1;2'", "SELECT '1;2'");
    testSplit("SELECT \"1;2\"", "SELECT \"1;2\"");
    testSplit("SELECT -- 1;2", "SELECT -- 1;2");
    testSplit("SELECT /*1;2*/", "SELECT /*1;2*/");
    testSplit("SELECT /1;2", "SELECT /1", "2");
    testSplit("SELECT ;\t\t\n \r  ;2", "SELECT ", "2");
  }

  @Test
  public void testRemove() {
    testRemoveComments("SELECT /*123;\nzz*/", "SELECT ");
    testRemoveComments("SELECT '/*123;\nzz*/'", "SELECT '/*123;\nzz*/'");
    testRemoveComments("SELECT --123;\nzz", "SELECT \nzz");
    testRemoveComments("SELECT '--123;\n--zz'", "SELECT '--123;\n--zz'");
  }

  void testSplit(String sql, String... expectedResult) {
    List<String> real = new SimpleSqlScriptParser().split(sql);
    assertEquals(Arrays.asList(expectedResult), real);
  }

  void testRemoveComments(String sql, String expectedResult) {
    String real = new SimpleSqlScriptParser().removeComments(sql);
    assertEquals(expectedResult, real);
  }
  
  String[][] testAndResultString =
  {

    // SQL with no comments
    { "SELECT * FROM MYTABLE", "SELECT * FROM MYTABLE" },

    // SQL with a one-line comment
    { "SELECT * FROM\n-- Test 1\n MYTABLE;", "SELECT * FROM\n\n MYTABLE;" },

    // SQL with multi-line comment at the top
    { "/* This \n is \n a multiline \n comment \n*/\nSELECT 1 FROM DUAL", "\nSELECT 1 FROM DUAL" },

    // SQL with multi-line comment in the middle
    { "SELECT 1 FROM\n/* This \n is \n a multiline \n comment \n*/\nDUAL", "SELECT 1 FROM\n\nDUAL" },

    // SQL with double-dashes inside a string
    {
      "UPDATE table1 SET col1 = '----' WHERE col1 IS NULL;\nUPDATE table1 SET col2 = '-----' "
        + "WHERE col2 IS NULL;\nUPDATE table1 SET col3 = '-----' WHERE col3 IS NULL;",
      "UPDATE table1 SET col1 = '----' WHERE col1 IS NULL;\nUPDATE table1 SET col2 = '-----' "
        + "WHERE col2 IS NULL;\nUPDATE table1 SET col3 = '-----' WHERE col3 IS NULL;" },

    // SQL with multi-line comment inside a string
    { "TEST '/* comment in string */'", "TEST '/* comment in string */'" },

    // SQL with double-dashes inside a string
    { "SELECT 'value-1' AS v1, 'value--1' AS v2;", "SELECT 'value-1' AS v1, 'value--1' AS v2;" },

    // SQL with double-dashes inside a double-quoted string
    { "SELECT \"value-1\" AS v1, \"value--1\" AS v2;", "SELECT \"value-1\" AS v1, \"value--1\" AS v2;" },

    // SQL with a single quote inside a double-quoted string
    {
      "select transname \"Doesn't w--'ork\" from logs.log_trans limit 0,20000;",
      "select transname \"Doesn't w--'ork\" from logs.log_trans limit 0,20000;" },

    // SQL with a double quote and comment inside a single-quoted string
    {
      "select transname '\"hello--world\"' from --logs.log_trans",
      "select transname '\"hello--world\"' from " },

    // Multi-line comment with SQL keywords
    {
      "/* my comment which may have sql keywords in it such as function, procedure etc. "
        + "Seems to cause issues within\ncomments.\n*/\n\n"
        + "if exists ( some query)\ndrop view myView;\n\ncreate view myView as\nselect query here ",
      "\n\nif exists ( some query)\ndrop view myView;\n\ncreate view myView as\nselect query here " },

    // Multi-line comment with quotes inside
    { "/*\n' my comment\n' more comment\n' a third line of the comment.\n*/", "" },

    // Null input
    { null, null },

    // Empty input
    { "", "" },

    { "SELECT col1 FROM test", "SELECT col1 FROM test" },
    { "SELECT col1 FROM test --end comment", "SELECT col1 FROM test " },
    { "SELECT \n col1, col2\n FROM \n test", "SELECT \n col1, col2\n FROM \n test" },
    { "SELECT \n \"col1\", col2\n FROM --test\n test", "SELECT \n \"col1\", col2\n FROM \n test" },
    { "SELECT /* \"my_column'\" */ col1 FROM /* 'my_table' */ account", "SELECT  col1 FROM  account" },
    { "SELECT '/' as col1, '*/*' as regex ", "SELECT '/' as col1, '*/*' as regex " },
    { "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') ",
      "SELECT INSTR('/loader/*/*.txt', '/') - INSTR('/loader/*/*.txt', '/') " },
    { "SELECT /* my data*/ col1, col2, col3 FROM account WHERE name = 'Pentaho'",
      "SELECT  col1, col2, col3 FROM account WHERE name = 'Pentaho'" }
  
  };

  @Test
  public void testRemoveComments() {
    for ( String[] testSet : testAndResultString ) {
      assertEquals( new SimpleSqlScriptParser().removeComments( testSet[0] ), testSet[1] );
    }
  }
}
