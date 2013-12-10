/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import org.junit.Test;

public class SqlCommentScrubberTest {

  String[][] testAndResultString =
  {

    // SQL with no comments
    { "SELECT * FROM MYTABLE", "SELECT * FROM MYTABLE" },

    // SQL with a one-line comment
    { "SELECT * FROM\n-- Test 1\n MYTABLE;", "SELECT * FROM\n MYTABLE;" },

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
    { "", "" }, };

  @Test
  public void testRemoveComments() {
    for ( String[] testSet : testAndResultString ) {
      assertEquals( SqlCommentScrubber.removeComments( testSet[0] ), testSet[1] );
    }
  }
}
