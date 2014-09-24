package org.pentaho.di.core.database;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SqlScriptSplitterTest {

  @Test
  public void testScripts() {
    test("SELECT 1;SELECT 2", "SELECT 1", "SELECT 2");
    test("SELECT '1;2'", "SELECT '1;2'");
    test("SELECT \"1;2\"", "SELECT \"1;2\"");
    test("SELECT -- 1;2", "SELECT -- 1;2");
    test("SELECT /*1;2*/", "SELECT /*1;2*/");
    test("SELECT /1;2", "SELECT /1", "2");
  }

  static void test(String sql, String... result) {
    List<String> real = SqlScriptSplitter.split(sql);
    assertEquals(Arrays.asList(result), real);
  }
}
