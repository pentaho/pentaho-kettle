/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class UtilsTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleClientEnvironment.init();
  }

  @Test
  public void testIsEmpty() {
    assertTrue( Utils.isEmpty( (String) null ) );
    assertTrue( Utils.isEmpty( "" ) );
    assertFalse( Utils.isEmpty( "test" ) );
  }

  @Test
  public void testIsEmptyStringArray() {
    assertTrue( Utils.isEmpty( (String[]) null ) );
    assertTrue( Utils.isEmpty( new String[] {} ) );
    assertFalse( Utils.isEmpty( new String[] { "test" } ) );
  }

  @Test
  public void testIsEmptyObjectArray() {
    assertTrue( Utils.isEmpty( (Object[]) null ) );
    assertTrue( Utils.isEmpty( new Object[] {} ) );
    assertFalse( Utils.isEmpty( new Object[] { "test" } ) );
  }

  @Test
  public void testIsEmptyList() {
    assertTrue( Utils.isEmpty( (List<String>) null ) );
    assertTrue( Utils.isEmpty( new ArrayList<String>() ) );
    assertFalse( Utils.isEmpty( Arrays.asList( "test", 1 ) ) );
  }

  @Test
  public void testIsEmptyStringBuffer() {
    assertTrue( Utils.isEmpty( (StringBuffer) null ) );
    assertTrue( Utils.isEmpty( new StringBuffer( "" ) ) );
    assertFalse( Utils.isEmpty( new StringBuffer( "test" ) ) );
  }

  @Test
  public void testIsEmptyStringBuilder() {
    assertTrue( Utils.isEmpty( (StringBuilder) null ) );
    assertTrue( Utils.isEmpty( new StringBuilder( "" ) ) );
    assertFalse( Utils.isEmpty( new StringBuilder( "test" ) ) );
  }

  @Test
  public void testResolvePassword() {
    String password = "password";
    // is supposed the password stays the same
    assertSame( password, Utils.resolvePassword(
        Variables.getADefaultVariableSpace(), password ).intern() );
  }

  @Test
  public void testResolvePasswordEncrypted() {
    String decPassword = "password";
    // is supposed encrypted with Encr.bat util
    String encPassword = "Encrypted 2be98afc86aa7f2e4bb18bd63c99dbdde";
    assertSame( decPassword, Utils.resolvePassword(
        Variables.getADefaultVariableSpace(), encPassword ).intern() );
  }

  @Test
  public void testResolvePasswordNull() {
    String password = null;
    // null is valid input parameter
    assertSame( password, Utils.resolvePassword(
        Variables.getADefaultVariableSpace(), password ) );
  }

  @Test
  public void testResolvePasswordVariable() {
    String passwordKey = "PASS_VAR";
    String passwordVar = "${" + passwordKey + "}";
    String passwordValue = "password";
    Variables vars = new Variables();
    vars.setVariable( passwordKey, passwordValue );
    //resolvePassword gets variable
    assertSame( passwordValue, Utils.resolvePassword( vars, passwordVar ).intern() );
  }

  @Test
  public void testNormalizeArraysMethods() {
    String[] s1 = new String[] { "one" };
    String[] s2 = new String[] { "one", "two" };
    String[] s3 = new String[] { "one", "two", "three" };
    long[] l1 = new long[] { 1 };
    long[] l2 = new long[] { 1, 2 };
    long[] l3 = new long[] { 1, 2, 3 };
    short[] sh1 = new short[] { 1 };
    short[] sh2 = new short[] { 1, 2 };
    short[] sh3 = new short[] { 1, 2, 3 };
    boolean[] b1 = new boolean[] { true };
    boolean[] b2 = new boolean[] { true, false };
    boolean[] b3 = new boolean[] { true, false, true };
    int[] i1 = new int[] { 1 };
    int[] i2 = new int[] { 1, 2 };
    int[] i3 = new int[] { 1, 3 };

    String[][] newS = Utils.normalizeArrays( 3, s1, s2 );
    assertEquals( 2, newS.length );
    assertEquals( 3, newS[ 0 ].length );
    assertEquals( 3, newS[ 1 ].length );
    newS = Utils.normalizeArrays( 3, s1, null );
    assertEquals( 2, newS.length );
    assertEquals( 3, newS[ 0 ].length );
    assertEquals( 3, newS[ 1 ].length );
    newS = Utils.normalizeArrays( 2, s2 );
    assertEquals( 1, newS.length );
    assertEquals( 2, newS[ 0 ].length );
    assertArrayEquals( newS[ 0 ], s2 );
    assertTrue( newS[ 0 ] == s2 ); // If arrays are equal sized, it should return original object

    long[][] newL = Utils.normalizeArrays( 3, l1, l2 );
    assertEquals( 2, newL.length );
    assertEquals( 3, newL[ 0 ].length );
    assertEquals( 3, newL[ 1 ].length );
    newL = Utils.normalizeArrays( 3, l1, null );
    assertEquals( 2, newL.length );
    assertEquals( 3, newL[ 0 ].length );
    assertEquals( 3, newL[ 1 ].length );
    newL = Utils.normalizeArrays( 2, l2 );
    assertEquals( 1, newL.length );
    assertEquals( 2, newL[ 0 ].length );
    assertArrayEquals( newL[ 0 ], l2 );
    assertTrue( newL[ 0 ] == l2 ); // If arrays are equal sized, it should return original object

    short[][] newSh = Utils.normalizeArrays( 3, sh1, sh2 );
    assertEquals( 2, newSh.length );
    assertEquals( 3, newSh[ 0 ].length );
    assertEquals( 3, newSh[ 1 ].length );
    newSh = Utils.normalizeArrays( 3, sh1, null );
    assertEquals( 2, newSh.length );
    assertEquals( 3, newSh[ 0 ].length );
    assertEquals( 3, newSh[ 1 ].length );
    newSh = Utils.normalizeArrays( 2, sh2 );
    assertEquals( 1, newSh.length );
    assertEquals( 2, newSh[ 0 ].length );
    assertArrayEquals( newSh[ 0 ], sh2 );
    assertTrue( newSh[ 0 ] == sh2 ); // If arrays are equal sized, it should return original object

    boolean[][] newB = Utils.normalizeArrays( 3, b1, b2 );
    assertEquals( 2, newB.length );
    assertEquals( 3, newB[ 0 ].length );
    assertEquals( 3, newB[ 1 ].length );
    newB = Utils.normalizeArrays( 3, b1, null );
    assertEquals( 2, newB.length );
    assertEquals( 3, newB[ 0 ].length );
    assertEquals( 3, newB[ 1 ].length );
    newB = Utils.normalizeArrays( 2, b2 );
    assertEquals( 1, newB.length );
    assertEquals( 2, newB[ 0 ].length );
    assertTrue( newB[ 0 ] == b2 ); // If arrays are equal sized, it should return original object

    int[][] newI = Utils.normalizeArrays( 3, i1, i2 );
    assertEquals( 2, newI.length );
    assertEquals( 3, newI[ 0 ].length );
    assertEquals( 3, newI[ 1 ].length );
    newI = Utils.normalizeArrays( 3, i1, null );
    assertEquals( 2, newI.length );
    assertEquals( 3, newI[ 0 ].length );
    assertEquals( 3, newI[ 1 ].length );
    newI = Utils.normalizeArrays( 2, i2 );
    assertEquals( 1, newI.length );
    assertEquals( 2, newI[ 0 ].length );
    assertArrayEquals( newI[ 0 ], i2 );
    assertTrue( newI[ 0 ] == i2 ); // If arrays are equal sized, it should return original object

  }

}
