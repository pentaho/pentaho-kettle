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

package org.pentaho.di.core.util;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of StringUtil.
 *
 * @author Sven Boden
 */
public class StringUtilTest extends TestCase {
  /**
   * Test initCap for JIRA PDI-619.
   */
  public void testinitCap() {
    assertEquals( "", StringUtil.initCap( null ) );
    assertEquals( "", StringUtil.initCap( "" ) );
    assertEquals( "", StringUtil.initCap( "   " ) );

    assertEquals( "A word", StringUtil.initCap( "a word" ) );
    assertEquals( "A word", StringUtil.initCap( "A word" ) );

    assertEquals( "Award", StringUtil.initCap( "award" ) );
    assertEquals( "Award", StringUtil.initCap( "Award" ) );

    assertEquals( "AWard", StringUtil.initCap( "aWard" ) );
    assertEquals( "AWard", StringUtil.initCap( "AWard" ) );
  }

  /**
   * Create an example map to be used for variable resolution.
   *
   * @return Map of variablenames/values.
   */
  Map<String, String> createVariables1( String open, String close ) {
    Map<String, String> map = new HashMap<String, String>();

    map.put( "EMPTY", "" );
    map.put( "checkcase", "case1" );
    map.put( "CheckCase", "case2" );
    map.put( "CHECKCASE", "case3" );
    map.put( "VARIABLE_1", "VARIABLE1" );

    map.put( "recursive1", "A" + open + "recursive2" + close );
    map.put( "recursive2", "recurse" );

    map.put( "recursive3", open + "recursive4" + close + "B" );
    map.put( "recursive4", "recurse" );

    map.put( "recursive5", open + "recursive6" + close + "B" );
    map.put( "recursive6", "Z" + open + "recursive7" + close );
    map.put( "recursive7", "final" );

    // endless recursive
    map.put( "recursive_all", open + "recursive_all1" + close + " tail" );
    map.put( "recursive_all1", open + "recursive_all" + close + " tail1" );

    return map;
  }

  /**
   * Test the basic substitute call.
   */
  public void testSubstituteBasic() {
    Map<String, String> map = createVariables1( "${", "}" );
    assertEquals( "||", StringUtil.substitute( "|${EMPTY}|", map, "${", "}" ) );
    assertEquals( "|case1|", StringUtil.substitute( "|${checkcase}|", map, "${", "}" ) );
    assertEquals( "|case2|", StringUtil.substitute( "|${CheckCase}|", map, "${", "}" ) );
    assertEquals( "|case3|", StringUtil.substitute( "|${CHECKCASE}|", map, "${", "}" ) );
    assertEquals( "|Arecurse|", StringUtil.substitute( "|${recursive1}|", map, "${", "}" ) );
    assertEquals( "|recurseB|", StringUtil.substitute( "|${recursive3}|", map, "${", "}" ) );
    assertEquals( "|ZfinalB|", StringUtil.substitute( "|${recursive5}|", map, "${", "}" ) );

    try {
      StringUtil.substitute( "|${recursive_all}|", map, "${", "}", 0 );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }

    map = createVariables1( "%%", "%%" );
    assertEquals( "||", StringUtil.substitute( "|%%EMPTY%%|", map, "%%", "%%" ) );
    assertEquals( "|case1|", StringUtil.substitute( "|%%checkcase%%|", map, "%%", "%%" ) );
    assertEquals( "|case2|", StringUtil.substitute( "|%%CheckCase%%|", map, "%%", "%%" ) );
    assertEquals( "|case3|", StringUtil.substitute( "|%%CHECKCASE%%|", map, "%%", "%%" ) );
    assertEquals( "|Arecurse|", StringUtil.substitute( "|%%recursive1%%|", map, "%%", "%%" ) );
    assertEquals( "|recurseB|", StringUtil.substitute( "|%%recursive3%%|", map, "%%", "%%" ) );
    assertEquals( "|ZfinalB|", StringUtil.substitute( "|%%recursive5%%|", map, "%%", "%%" ) );

    try {
      StringUtil.substitute( "|%%recursive_all%%|", map, "%%", "%%" );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }

    map = createVariables1( "${", "}" );
    assertEquals( "||", StringUtil.environmentSubstitute( "|%%EMPTY%%|", map ) );
    assertEquals( "|case1|", StringUtil.environmentSubstitute( "|%%checkcase%%|", map ) );
    assertEquals( "|case2|", StringUtil.environmentSubstitute( "|%%CheckCase%%|", map ) );
    assertEquals( "|case3|", StringUtil.environmentSubstitute( "|%%CHECKCASE%%|", map ) );
    assertEquals( "|Arecurse|", StringUtil.environmentSubstitute( "|%%recursive1%%|", map ) );
    assertEquals( "|recurseB|", StringUtil.environmentSubstitute( "|%%recursive3%%|", map ) );
    assertEquals( "|ZfinalB|", StringUtil.environmentSubstitute( "|%%recursive5%%|", map ) );

    try {
      StringUtil.environmentSubstitute( "|%%recursive_all%%|", map );
      fail( "recursive check is failing" );
    } catch ( RuntimeException rex ) {
    }
  }

  /**
   * Test isEmpty() call.
   */
  public void testIsEmpty() {
    assertTrue( StringUtil.isEmpty( (String) null ) );
    assertTrue( StringUtil.isEmpty( "" ) );

    assertFalse( StringUtil.isEmpty( "A" ) );
    assertFalse( StringUtil.isEmpty( " A " ) );
  }

  /**
   * Test getIndent() call.
   */
  public void testGetIndent() {
    assertEquals( "", StringUtil.getIndent( 0 ) );
    assertEquals( " ", StringUtil.getIndent( 1 ) );
    assertEquals( "  ", StringUtil.getIndent( 2 ) );
    assertEquals( "   ", StringUtil.getIndent( 3 ) );
  }

  public void testIsVariable() throws Exception {
    assertTrue( StringUtil.isVariable( "${somename}" ) );
    assertTrue( StringUtil.isVariable( "%%somename%%" ) );
    assertTrue( StringUtil.isVariable( "$[somename]" ) );
    assertFalse( StringUtil.isVariable( "somename" ) );
    assertFalse( StringUtil.isVariable( null ) );

  }
}
