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

package org.pentaho.di.core;

import junit.framework.TestCase;
import org.apache.commons.lang.SystemUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.math.BigDecimal;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Test class for the basic functionality of Const.
 *
 */
public class ConstTest extends TestCase {

  private static String DELIMITER1 = ",";
  private static String DELIMITER2 = "</newpage>";
  private static String ENCLOSURE1 = "\"";
  private static String ENCLOSURE2 = "html";

  protected boolean isArraySorted( String[] arr ) {
    if ( arr.length < 2 ) {
      return true;
    }

    for ( int idx = 0; idx < arr.length - 1; idx++ ) {
      if ( arr[idx].compareTo( arr[idx + 1] ) > 0 ) {
        return false;
      }
    }

    return true;
  }

  /**
   * Test initCap. Regression test for PDI-1338: "javascript initcap() can't deal correctly with special non-ASCII
   * chars".
   */
  @Test
  public void testInitCap() {
    assertEquals( "Sven", Const.initCap( "Sven" ) );
    assertEquals( "Matt", Const.initCap( "MATT" ) );
    assertEquals( "Sven Boden", Const.initCap( "sven boden" ) );
    assertEquals( "Sven  Boden ", Const.initCap( "sven  boden " ) );
    assertEquals( "Sven Boden Was Here", Const.initCap( "sven boden was here" ) );

    // Here the original code failed as it saw the "o umlaut" as non-ASCII, and would
    // assume it needed to start a new word here.
    assertEquals( "K\u00F6nnen", Const.initCap( "k\u00F6nnen" ) );
  }

  /**
   * Test sortString().
   */
  @Test
  public void testSortStrings() {
    String[] arr1 = { "Red", "Blue", "Black", "Black", "Green" };
    String[] arr2 = { "aaa", "zzz", "yyy", "sss", "ttt", "t" };
    String[] arr3 = { "A", "B", "C", "D" };

    String[] results = Const.sortStrings( arr1 );
    assertTrue( isArraySorted( arr1 ) );
    assertTrue( isArraySorted( results ) );

    results = Const.sortStrings( arr2 );
    assertTrue( isArraySorted( arr2 ) );
    assertTrue( isArraySorted( results ) );

    results = Const.sortStrings( arr3 );
    assertTrue( isArraySorted( arr3 ) );
    assertTrue( isArraySorted( results ) );
  }

  @Test
  public void testIsEmpty() {
    assertTrue( Const.isEmpty( (String) null ) );
    assertTrue( Const.isEmpty( "" ) );
    assertFalse( Const.isEmpty( "test" ) );
  }

  @Test
  public void testIsEmptyStringArray() {
    assertTrue( Const.isEmpty( (String[]) null ) );
    assertTrue( Const.isEmpty( new String[] {} ) );
    assertFalse( Const.isEmpty( new String[] { "test" } ) );
  }

  @Test
  public void testIsEmptyObjectArray() {
    assertTrue( Const.isEmpty( (Object[]) null ) );
    assertTrue( Const.isEmpty( new Object[] {} ) );
    assertFalse( Const.isEmpty( new Object[] { "test" } ) );
  }

  @Test
  public void testIsEmptyList() {
    assertTrue( Const.isEmpty( (List) null ) );
    assertTrue( Const.isEmpty( new ArrayList() ) );
    assertFalse( Const.isEmpty( Arrays.asList( "test", 1 ) ) );
  }

  @Test
  public void testIsEmptyStringBuffer() {
    assertTrue( Const.isEmpty( (StringBuffer) null ) );
    assertTrue( Const.isEmpty( new StringBuffer( "" ) ) );
    assertFalse( Const.isEmpty( new StringBuffer( "test" ) ) );
  }

  @Test
  public void testIsEmptyStringBuilder() {
    assertTrue( Const.isEmpty( (StringBuilder) null ) );
    assertTrue( Const.isEmpty( new StringBuilder( "" ) ) );
    assertFalse( Const.isEmpty( new StringBuilder( "test" ) ) );
  }

  @Test
  public void testNVL() {
    assertNull( Const.NVL( null, null ) );
    assertEquals( "test", Const.NVL( "test", "test1" ) );
    assertEquals( "test", Const.NVL( "test", null ) );
    assertEquals( "test1", Const.NVL( null, "test1" ) );
  }

  @Test
  public void testNrSpacesBefore() {
    try {
      Const.nrSpacesBefore( null );
      fail( "Expected NullPointerException" );
    } catch ( NullPointerException ex ) {
      // Ignore
    }

    assertEquals( 0, Const.nrSpacesBefore( "" ) );
    assertEquals( 1, Const.nrSpacesBefore( " " ) );
    assertEquals( 3, Const.nrSpacesBefore( "   " ) );
    assertEquals( 0, Const.nrSpacesBefore( "test" ) );
    assertEquals( 0, Const.nrSpacesBefore( "test  " ) );
    assertEquals( 3, Const.nrSpacesBefore( "   test" ) );
    assertEquals( 4, Const.nrSpacesBefore( "    test  " ) );
  }

  @Test
  public void testNrSpacesAfter() {
    try {
      Const.nrSpacesAfter( null );
      fail( "Expected NullPointerException" );
    } catch ( NullPointerException ex ) {
      // Ignore
    }

    assertEquals( 0, Const.nrSpacesAfter( "" ) );
    assertEquals( 1, Const.nrSpacesAfter( " " ) );
    assertEquals( 3, Const.nrSpacesAfter( "   " ) );
    assertEquals( 0, Const.nrSpacesAfter( "test" ) );
    assertEquals( 2, Const.nrSpacesAfter( "test  " ) );
    assertEquals( 0, Const.nrSpacesAfter( "   test" ) );
    assertEquals( 2, Const.nrSpacesAfter( "    test  " ) );
  }

  @Test
  public void testLtrim() {
    assertEquals( null, Const.ltrim( null ) );
    assertEquals( "", Const.ltrim( "" ) );
    assertEquals( "", Const.ltrim( "  " ) );
    assertEquals( "test ", Const.ltrim( "test " ) );
    assertEquals( "test ", Const.ltrim( "  test " ) );
  }

  @Test
  public void testRtrim() {
    assertEquals( null, Const.rtrim( null ) );
    assertEquals( "", Const.rtrim( "" ) );
    assertEquals( "", Const.rtrim( "  " ) );
    assertEquals( "test", Const.rtrim( "test " ) );
    assertEquals( "test ", Const.ltrim( "  test " ) );
  }

  @Test
  public void testTrim() {
    assertEquals( null, Const.trim( null ) );
    assertEquals( "", Const.trim( "" ) );
    assertEquals( "", Const.trim( "  " ) );
    assertEquals( "test", Const.trim( "test " ) );
    assertEquals( "test", Const.trim( "  test " ) );
  }

  @Test
  public void testOnlySpaces() {
    try {
      Const.onlySpaces( null );
      fail( "Expected NullPointerException" );
    } catch ( NullPointerException ex ) {
      // Ignore
    }
    assertEquals( true, Const.onlySpaces( "" ) );
    assertEquals( true, Const.onlySpaces( "  " ) );
    assertEquals( false, Const.onlySpaces( "   test " ) );
  }

  /**
   * Test splitString with String separator.
   */
  @Test
  public void testSplitString() {
    assertEquals( 0, Const.splitString( "", ";" ).length );
    assertEquals( 0, Const.splitString( null, ";" ).length );

    String[] a = Const.splitString( ";", ";" );
    assertEquals( 1, a.length );
    assertEquals( "", a[0] );

    a = Const.splitString( "a;b;c;d", ";" );
    assertEquals( 4, a.length );
    assertEquals( "a", a[0] );
    assertEquals( "b", a[1] );
    assertEquals( "c", a[2] );
    assertEquals( "d", a[3] );

    a = Const.splitString( "a;b;c;d;", ";" );
    assertEquals( 4, a.length );
    assertEquals( "a", a[0] );
    assertEquals( "b", a[1] );
    assertEquals( "c", a[2] );
    assertEquals( "d", a[3] );

    a = Const.splitString( "AACCAADAaAADD", "AA" );
    assertEquals( 4, a.length );
    assertEquals( "", a[0] );
    assertEquals( "CC", a[1] );
    assertEquals( "DA", a[2] );
    assertEquals( "ADD", a[3] );

    a = Const.splitString( "CCAABBAA", "AA" );
    assertEquals( 2, a.length );
    assertEquals( "CC", a[0] );
    assertEquals( "BB", a[1] );
  }

  /**
   * Test splitString with char separator.
   */
  @Test
  public void testSplitStringChar() {
    assertEquals( 0, Const.splitString( "", ';' ).length );
    assertEquals( 0, Const.splitString( null, ';' ).length );

    String[] a = Const.splitString( ";", ';' );
    assertEquals( 1, a.length );
    assertEquals( "", a[0] );

    a = Const.splitString( "a;b;c;d", ';' );
    assertEquals( 4, a.length );
    assertEquals( "a", a[0] );
    assertEquals( "b", a[1] );
    assertEquals( "c", a[2] );
    assertEquals( "d", a[3] );

    a = Const.splitString( "a;b;c;d;", ';' );
    assertEquals( 4, a.length );
    assertEquals( "a", a[0] );
    assertEquals( "b", a[1] );
    assertEquals( "c", a[2] );
    assertEquals( "d", a[3] );

    a = Const.splitString( ";CC;DA;ADD", ';' );
    assertEquals( 4, a.length );
    assertEquals( "", a[0] );
    assertEquals( "CC", a[1] );
    assertEquals( "DA", a[2] );
    assertEquals( "ADD", a[3] );

    a = Const.splitString( "CC;BB;", ';' );
    assertEquals( 2, a.length );
    assertEquals( "CC", a[0] );
    assertEquals( "BB", a[1] );
  }

  /**
   * Test splitString with delimiter and enclosure
   */
  @Test
  public void testSplitStringNullWithDelimiterNullAndEnclosureNull() {
    String[] result = Const.splitString( null, null, null );
    assertNull( result );
  }

  @Test
  public void testSplitStringNullWithDelimiterNullAndEnclosureNullRemoveEnclosure() {
    String[] result = Const.splitString( null, null, null, true );
    assertNull( result );
  }

  @Test
  public void testSplitStringWithDelimiterNullAndEnclosureNull() {
    String stringToSplit = "Hello, world";
    String[] result = Const.splitString( stringToSplit, null, null );
    assertSplit( result, stringToSplit );
  }

  @Test
  public void testSplitStringWithDelimiterNullAndEnclosureNullRemoveEnclosure() {
    String stringToSplit = "Hello, world";
    String[] result = Const.splitString( stringToSplit, null, null, true );
    assertSplit( result, stringToSplit );
  }

  @Test
  public void testSplitStringWithDelimiterAndEnclosureNull() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, null );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEnclosureNullMultiChar() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER2 );
    String[] result = Const.splitString( stringToSplit, DELIMITER2, null );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEnclosureNullRemoveEnclosure() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, null, true );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEnclosureNullMultiCharRemoveEnclosure() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER2 );
    String[] result = Const.splitString( stringToSplit, DELIMITER2, null, true );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEmptyEnclosure() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, "" );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEmptyEnclosureMultiChar() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER2 );
    String[] result = Const.splitString( stringToSplit, DELIMITER2, "" );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEmptyEnclosureRemoveEnclosure() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, "", true );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndEmptyEnclosureMultiCharRemoveEnclosure() {
    String mask = "Hello%s world";
    String[] chunks = {"Hello", " world"};

    String stringToSplit = String.format( mask, DELIMITER2 );
    String [] result = Const.splitString( stringToSplit, DELIMITER2, "", true );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosure1() {
    //"Hello, world"
    String mask = "%sHello%s world%s";

    String stringToSplit = String.format( mask, ENCLOSURE1, DELIMITER1, ENCLOSURE1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, ENCLOSURE1 );
    assertSplit( result, stringToSplit );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiChar1() {
    //"Hello, world"
    String mask = "%sHello%s world%s";

    String stringToSplit = String.format( mask, ENCLOSURE2, DELIMITER2, ENCLOSURE2 );
    String[] result = Const.splitString( stringToSplit, DELIMITER2, ENCLOSURE2 );
    assertSplit( result, stringToSplit );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureRemoveEnclosure1() {
    //"Hello, world"
    String mask = "%sHello%s world%s";
    String[] chunks1 = { "Hello" + DELIMITER1 + " world" };

    String stringToSplit = String.format( mask, ENCLOSURE1, DELIMITER1, ENCLOSURE1 );
    String[] result = Const.splitString( stringToSplit, DELIMITER1, ENCLOSURE1, true );
    assertSplit( result, chunks1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiCharRemoveEnclosure1() {
    //"Hello, world"
    String mask = "%sHello%s world%s";
    String[] chunks2 = { "Hello" + DELIMITER2 + " world" };

    String stringToSplit = String.format( mask, ENCLOSURE2, DELIMITER2, ENCLOSURE2 );
    String[] result = Const.splitString( stringToSplit, DELIMITER2, ENCLOSURE2, true );
    assertSplit( result, chunks2 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosure2() {
    testSplitStringWithDelimiterAndQuoteEnclosure2( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiChar2() {
    testSplitStringWithDelimiterAndQuoteEnclosure2( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringWithDelimiterAndQuoteEnclosure2( String e, String d ) {
    //"Hello, world","I","am","here"
    String mask = "%sHello%s world%s%s%sI%s%s%sam%s%s%shere%s";

    String[] chunks1 = { e + "Hello" + d + " world" + e,
      e + "I" + e,
      e + "am" + e,
      e + "here" + e };

    String stringToSplit = String.format( mask, e, d, e, d, e, e, d, e, e, d, e, e );
    String[] result = Const.splitString( stringToSplit, d, e );
    assertSplit( result, chunks1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureRemoveEnclosure2() {
    testSplitStringWithDelimiterAndQuoteEnclosureRemoveEnclosure2( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiCharRemoveEnclosure2() {
    testSplitStringWithDelimiterAndQuoteEnclosureRemoveEnclosure2( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringWithDelimiterAndQuoteEnclosureRemoveEnclosure2( String e, String d ) {
    //"Hello, world","I","am","here"
    String mask = "%sHello%s world%s%s%sI%s%s%sam%s%s%shere%s";

    String[] chunks1 = { "Hello" + d + " world",
      "I", "am", "here" };

    String stringToSplit = String.format( mask, e, d, e, d, e, e, d, e, e, d, e, e );
    String[] result = Const.splitString( stringToSplit, d, e, true );
    assertSplit( result, chunks1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosure3() {
    testSplitStringWithDelimiterAndQuoteEnclosure3( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiChar3() {
    testSplitStringWithDelimiterAndQuoteEnclosure3( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringWithDelimiterAndQuoteEnclosure3( String e, String d ) {
    //"Hello, world","I,","am,,",", here"
    String mask = "%sHello%s world%s" + "%s" + "%sI%s%s" + "%s" + "%sam%s%s%s" + "%s" + "%s%s here%s";

    String[] chunks1 = { e + "Hello" + d + " world" + e,
      e + "I" + d + e,
      e + "am" + d + d + e,
      e + d + " here" + e };
    String stringToSplit = String.format( mask, e, d, e, d, e, d, e, d, e, d, d, e, d, e, d, e );
    String[] result = Const.splitString( stringToSplit, d, e );
    assertSplit( result, chunks1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureRemovesEnclosure3() {
    testSplitStringWithDelimiterAndQuoteEnclosureRemovesEnclosure3( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringWithDelimiterAndQuoteEnclosureMultiCharRemovesEnclosure3() {
    testSplitStringWithDelimiterAndQuoteEnclosureRemovesEnclosure3( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringWithDelimiterAndQuoteEnclosureRemovesEnclosure3( String e, String d ) {
    //"Hello, world","I,","am,,",", here"
    String mask = "%sHello%s world%s" + "%s" + "%sI%s%s" + "%s" + "%sam%s%s%s" + "%s" + "%s%s here%s";

    String[] chunks1 = { "Hello" + d + " world",
      "I" + d,
      "am" + d + d,
      d + " here" };
    String stringToSplit = String.format( mask, e, d, e, d, e, d, e, d, e, d, d, e, d, e, d, e );
    String[] result = Const.splitString( stringToSplit, d, e, true );
    assertSplit( result, chunks1 );
  }

  @Test
  public void testSplitStringWithDifferentDelimiterAndEnclosure() {
    // Try a different delimiter and enclosure
    String[] result = Const.splitString( "a;'b;c;d';'e,f';'g';h", ";", "'" );
    assertNotNull( result );
    assertEquals( 5, result.length );
    assertEquals( "a", result[0] );
    assertEquals( "'b;c;d'", result[1] );
    assertEquals( "'e,f'", result[2] );
    assertEquals( "'g'", result[3] );
    assertEquals( "h", result[4] );

    // Check for null and empty as the last split
    result = Const.splitString( "a;b;c;", ";", null );
    assertNotNull( result );
    assertEquals( 3, result.length );

    result = Const.splitString( "a;b;c;''", ";", "'" );
    assertNotNull( result );
    assertEquals( 4, result.length );
  }

  @Test
  public void testSplitStringWithMultipleCharacterDelimiterAndEnclosure() {
    // Check for multiple-character strings
    String[] result =
      Const.splitString( "html this is a web page html</newpage>html and so is this html", "</newpage>", "html" );
    assertNotNull( result );
    assertEquals( 2, result.length );
    assertEquals( "html this is a web page html", result[0] );
    assertEquals( "html and so is this html", result[1] );
  }

  @Test
  public void testSplitStringRemoveEnclosureNested1() {
    testSplitStringRemoveEnclosureNested1( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringRemoveEnclosureNestedMultiChar1() {
    testSplitStringRemoveEnclosureNested1( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringRemoveEnclosureNested1( String e, String d ) {
    //"a, "b" c"
    String mask = "%sa" + "%s" + " %sb%s c%s";

    String[] chunks = { "a" + d + " " + e + "b" + e + " c" };

    String stringToSplit = String.format( mask, e, d, e, e, e );
    String[] result = Const.splitString( stringToSplit, d, e, true );
    assertSplit( result, chunks );
  }

  @Test
  public void testSplitStringRemoveEnclosureNested2() {
    testSplitStringRemoveEnclosureNested( ENCLOSURE1, DELIMITER1 );
  }

  @Test
  public void testSplitStringRemoveEnclosureNestedMultiChar2() {
    testSplitStringRemoveEnclosureNested( ENCLOSURE2, DELIMITER2 );
  }

  private void testSplitStringRemoveEnclosureNested( String e, String d ) {
    //"""a,b,c"""
    String mask = "%s%s%sa" + "%s" + "b" + "%s" + "c%s%s%s";
    String[] chunks = { e + e + "a" + d  + "b" + d + "c" + e + e};

    String stringToSplit = String.format( mask, e, e, e, d, d, e, e, e );
    String[] result = Const.splitString( stringToSplit, d, e, true );
    assertSplit( result, chunks );
  }

  private void assertSplit( String[] result, String... chunks ) {
    assertNotNull( result );
    assertEquals( chunks.length, result.length );
    for ( int i = 0; i < chunks.length; i++ ) {
      assertEquals( chunks[i], result[i] );
    }
  }

  /**
   * Test splitString with delimiter and enclosure
   */
  @Test
  public void testSplitStringWithEscaping() {
    String[] result;

    result = Const.splitString( null, null, null );
    assertNull( result );

    result = Const.splitString( "Hello, world", null, null );
    assertNotNull( result );
    assertEquals( result.length, 1 );
    assertEquals( result[0], "Hello, world" );

    result = Const.splitString( "Hello\\, world,Hello\\, planet,Hello\\, 3rd rock", ',', true );
    assertNotNull( result );
    assertEquals( result.length, 3 );
    assertEquals( result[0], "Hello\\, world" );
    assertEquals( result[1], "Hello\\, planet" );
    assertEquals( result[2], "Hello\\, 3rd rock" );
  }

  /**
   * Test splitPath.
   */
  @Test
  public void testSplitPath() {
    String[] a = Const.splitPath( "", "/" );
    assertEquals( 0, a.length );

    a = Const.splitPath( null, "/" );
    assertEquals( 0, a.length );

    a = Const.splitPath( "/", "/" );
    assertEquals( 0, a.length );

    a = Const.splitPath( "/level1", "/" );
    assertEquals( 1, a.length );
    assertEquals( "level1", a[0] );

    a = Const.splitPath( "level1", "/" );
    assertEquals( 1, a.length );
    assertEquals( "level1", a[0] );

    a = Const.splitPath( "/level1/level2", "/" );
    assertEquals( 2, a.length );
    assertEquals( "level1", a[0] );
    assertEquals( "level2", a[1] );

    a = Const.splitPath( "level1/level2", "/" );
    assertEquals( 2, a.length );
    assertEquals( "level1", a[0] );
    assertEquals( "level2", a[1] );

    a = Const.splitPath( "/level1/level2/lvl3", "/" );
    assertEquals( 3, a.length );
    assertEquals( "level1", a[0] );
    assertEquals( "level2", a[1] );
    assertEquals( "lvl3", a[2] );

    a = Const.splitPath( "level1/level2/lvl3", "/" );
    assertEquals( 3, a.length );
    assertEquals( "level1", a[0] );
    assertEquals( "level2", a[1] );
    assertEquals( "lvl3", a[2] );
  }

  @Test
  public void testRound_BigDecimal() {
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.0" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.2" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.5" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.5" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "1.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "1.7" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.0" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.2" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.5" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.5" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "2.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "3.0" ), Const.round( new BigDecimal( "2.7" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.0" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.2" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.5" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.5" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-1.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-1.7" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.0" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.2" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.5" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.5" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-2.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-3.0" ), Const.round( new BigDecimal( "-2.7" ), 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.010" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.012" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.015" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.015" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.010" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.017" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.020" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.022" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.025" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.025" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "0.020" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "0.030" ), Const.round( new BigDecimal( "0.027" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.010" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.012" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.012" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.012" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.015" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.015" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.015" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.015" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.015" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.015" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.015" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.015" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.017" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.010" ), Const.round( new BigDecimal( "-0.017" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.017" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.020" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.022" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.022" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.022" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.025" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.025" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.025" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.025" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.025" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.025" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.025" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.025" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.027" ), 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-0.020" ), Const.round( new BigDecimal( "-0.027" ), 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-0.030" ), Const.round( new BigDecimal( "-0.027" ), 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "100.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "120.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "150.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "150.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "100.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "170.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "200.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "220.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "250.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "250.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "200.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "300.0" ), Const.round( new BigDecimal( "270.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-100.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-120.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-120.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-120.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-150.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-150.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-150.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-150.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-150.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-150.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-150.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-150.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-170.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-100.0" ), Const.round( new BigDecimal( "-170.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-170.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-200.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-220.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-220.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-220.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-250.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-250.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-250.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-250.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-250.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-250.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-250.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-250.0" ), -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2, BigDecimal.ROUND_UP ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-270.0" ), -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( new BigDecimal( "-200.0" ), Const.round( new BigDecimal( "-270.0" ), -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2,
        BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2,
        BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( new BigDecimal( "-300.0" ), Const.round( new BigDecimal( "-270.0" ), -2, Const.ROUND_HALF_CEILING ) );
  }

  @Test
  public void testRound() {
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 1.0, Const.round( 1.0, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 1.0, Const.round( 1.0, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 2.0, Const.round( 1.2, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 1.0, Const.round( 1.2, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 2.0, Const.round( 1.2, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 1.0, Const.round( 1.2, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 1.0, Const.round( 1.2, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 1.0, Const.round( 1.2, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 1.0, Const.round( 1.2, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 1.0, Const.round( 1.2, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 2.0, Const.round( 1.5, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 1.0, Const.round( 1.5, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 2.0, Const.round( 1.5, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 1.0, Const.round( 1.5, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 2.0, Const.round( 1.5, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 1.0, Const.round( 1.5, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2.0, Const.round( 1.5, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 2.0, Const.round( 1.5, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 2.0, Const.round( 1.7, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 1.0, Const.round( 1.7, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 2.0, Const.round( 1.7, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 1.0, Const.round( 1.7, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 2.0, Const.round( 1.7, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 2.0, Const.round( 1.7, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2.0, Const.round( 1.7, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 2.0, Const.round( 1.7, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2.0, Const.round( 2.0, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 2.0, Const.round( 2.0, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 3.0, Const.round( 2.2, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 2.0, Const.round( 2.2, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 3.0, Const.round( 2.2, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 2.0, Const.round( 2.2, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 2.0, Const.round( 2.2, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 2.0, Const.round( 2.2, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2.0, Const.round( 2.2, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 2.0, Const.round( 2.2, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 3.0, Const.round( 2.5, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 2.0, Const.round( 2.5, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 3.0, Const.round( 2.5, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 2.0, Const.round( 2.5, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 3.0, Const.round( 2.5, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 2.0, Const.round( 2.5, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2.0, Const.round( 2.5, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 3.0, Const.round( 2.5, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 3.0, Const.round( 2.7, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 2.0, Const.round( 2.7, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 3.0, Const.round( 2.7, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 2.0, Const.round( 2.7, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 3.0, Const.round( 2.7, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 3.0, Const.round( 2.7, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 3.0, Const.round( 2.7, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 3.0, Const.round( 2.7, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -1.0, Const.round( -1.0, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -1.0, Const.round( -1.0, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -2.0, Const.round( -1.2, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -1.0, Const.round( -1.2, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -1.0, Const.round( -1.2, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -2.0, Const.round( -1.2, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -1.0, Const.round( -1.2, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -1.0, Const.round( -1.2, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -1.0, Const.round( -1.2, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -1.0, Const.round( -1.2, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -2.0, Const.round( -1.5, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -1.0, Const.round( -1.5, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -1.0, Const.round( -1.5, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -2.0, Const.round( -1.5, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -2.0, Const.round( -1.5, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -1.0, Const.round( -1.5, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2.0, Const.round( -1.5, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -1.0, Const.round( -1.5, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -2.0, Const.round( -1.7, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -1.0, Const.round( -1.7, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -1.0, Const.round( -1.7, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -2.0, Const.round( -1.7, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -2.0, Const.round( -1.7, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -2.0, Const.round( -1.7, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2.0, Const.round( -1.7, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -2.0, Const.round( -1.7, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2.0, Const.round( -2.0, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -2.0, Const.round( -2.0, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -3.0, Const.round( -2.2, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -2.0, Const.round( -2.2, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -2.0, Const.round( -2.2, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -3.0, Const.round( -2.2, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -2.0, Const.round( -2.2, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -2.0, Const.round( -2.2, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2.0, Const.round( -2.2, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -2.0, Const.round( -2.2, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -3.0, Const.round( -2.5, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -2.0, Const.round( -2.5, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -2.0, Const.round( -2.5, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -3.0, Const.round( -2.5, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -3.0, Const.round( -2.5, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -2.0, Const.round( -2.5, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2.0, Const.round( -2.5, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -2.0, Const.round( -2.5, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -3.0, Const.round( -2.7, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -2.0, Const.round( -2.7, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -2.0, Const.round( -2.7, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -3.0, Const.round( -2.7, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -3.0, Const.round( -2.7, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -3.0, Const.round( -2.7, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -3.0, Const.round( -2.7, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -3.0, Const.round( -2.7, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.010, Const.round( 0.010, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.010, Const.round( 0.010, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.020, Const.round( 0.012, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.010, Const.round( 0.012, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.020, Const.round( 0.012, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.010, Const.round( 0.012, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.010, Const.round( 0.012, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.010, Const.round( 0.012, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.010, Const.round( 0.012, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.010, Const.round( 0.012, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.020, Const.round( 0.015, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.010, Const.round( 0.015, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.020, Const.round( 0.015, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.010, Const.round( 0.015, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.020, Const.round( 0.015, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.010, Const.round( 0.015, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.020, Const.round( 0.015, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.020, Const.round( 0.015, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.020, Const.round( 0.017, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.010, Const.round( 0.017, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.020, Const.round( 0.017, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.010, Const.round( 0.017, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.020, Const.round( 0.017, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.020, Const.round( 0.017, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.020, Const.round( 0.017, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.020, Const.round( 0.017, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.020, Const.round( 0.020, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.020, Const.round( 0.020, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.030, Const.round( 0.022, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.020, Const.round( 0.022, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.030, Const.round( 0.022, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.020, Const.round( 0.022, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.020, Const.round( 0.022, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.020, Const.round( 0.022, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.020, Const.round( 0.022, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.020, Const.round( 0.022, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.030, Const.round( 0.025, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.020, Const.round( 0.025, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.030, Const.round( 0.025, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.020, Const.round( 0.025, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.030, Const.round( 0.025, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.020, Const.round( 0.025, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.020, Const.round( 0.025, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.030, Const.round( 0.025, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 0.030, Const.round( 0.027, 2, BigDecimal.ROUND_UP ) );
    assertEquals( 0.020, Const.round( 0.027, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 0.030, Const.round( 0.027, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 0.020, Const.round( 0.027, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 0.030, Const.round( 0.027, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 0.030, Const.round( 0.027, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 0.030, Const.round( 0.027, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 0.030, Const.round( 0.027, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.010, Const.round( -0.010, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.010, Const.round( -0.010, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.020, Const.round( -0.012, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.010, Const.round( -0.012, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.010, Const.round( -0.012, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.020, Const.round( -0.012, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.010, Const.round( -0.012, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.010, Const.round( -0.012, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.010, Const.round( -0.012, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.010, Const.round( -0.012, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.020, Const.round( -0.015, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.010, Const.round( -0.015, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.010, Const.round( -0.015, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.020, Const.round( -0.015, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.020, Const.round( -0.015, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.010, Const.round( -0.015, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.020, Const.round( -0.015, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.010, Const.round( -0.015, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.020, Const.round( -0.017, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.010, Const.round( -0.017, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.010, Const.round( -0.017, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.020, Const.round( -0.017, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.020, Const.round( -0.017, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.020, Const.round( -0.017, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.020, Const.round( -0.017, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.020, Const.round( -0.017, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.020, Const.round( -0.020, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.020, Const.round( -0.020, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.030, Const.round( -0.022, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.020, Const.round( -0.022, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.020, Const.round( -0.022, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.030, Const.round( -0.022, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.020, Const.round( -0.022, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.020, Const.round( -0.022, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.020, Const.round( -0.022, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.020, Const.round( -0.022, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.030, Const.round( -0.025, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.020, Const.round( -0.025, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.020, Const.round( -0.025, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.030, Const.round( -0.025, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.030, Const.round( -0.025, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.020, Const.round( -0.025, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.020, Const.round( -0.025, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.020, Const.round( -0.025, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -0.030, Const.round( -0.027, 2, BigDecimal.ROUND_UP ) );
    assertEquals( -0.020, Const.round( -0.027, 2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -0.020, Const.round( -0.027, 2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -0.030, Const.round( -0.027, 2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -0.030, Const.round( -0.027, 2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -0.030, Const.round( -0.027, 2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -0.030, Const.round( -0.027, 2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -0.030, Const.round( -0.027, 2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 100.0, Const.round( 100.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 100.0, Const.round( 100.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200.0, Const.round( 120.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100.0, Const.round( 120.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200.0, Const.round( 120.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100.0, Const.round( 120.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 100.0, Const.round( 120.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100.0, Const.round( 120.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 100.0, Const.round( 120.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 100.0, Const.round( 120.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200.0, Const.round( 150.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100.0, Const.round( 150.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200.0, Const.round( 150.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100.0, Const.round( 150.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200.0, Const.round( 150.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100.0, Const.round( 150.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200.0, Const.round( 150.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200.0, Const.round( 150.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200.0, Const.round( 170.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100.0, Const.round( 170.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200.0, Const.round( 170.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100.0, Const.round( 170.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200.0, Const.round( 170.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200.0, Const.round( 170.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200.0, Const.round( 170.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200.0, Const.round( 170.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200.0, Const.round( 200.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200.0, Const.round( 200.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300.0, Const.round( 220.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200.0, Const.round( 220.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300.0, Const.round( 220.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200.0, Const.round( 220.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200.0, Const.round( 220.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200.0, Const.round( 220.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200.0, Const.round( 220.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200.0, Const.round( 220.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300.0, Const.round( 250.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200.0, Const.round( 250.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300.0, Const.round( 250.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200.0, Const.round( 250.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 300.0, Const.round( 250.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200.0, Const.round( 250.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200.0, Const.round( 250.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 300.0, Const.round( 250.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300.0, Const.round( 270.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200.0, Const.round( 270.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300.0, Const.round( 270.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200.0, Const.round( 270.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 300.0, Const.round( 270.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 300.0, Const.round( 270.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 300.0, Const.round( 270.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 300.0, Const.round( 270.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -100.0, Const.round( -100.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100.0, Const.round( -100.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200.0, Const.round( -120.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100.0, Const.round( -120.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100.0, Const.round( -120.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200.0, Const.round( -120.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -100.0, Const.round( -120.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100.0, Const.round( -120.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -100.0, Const.round( -120.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100.0, Const.round( -120.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200.0, Const.round( -150.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100.0, Const.round( -150.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100.0, Const.round( -150.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200.0, Const.round( -150.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200.0, Const.round( -150.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100.0, Const.round( -150.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200.0, Const.round( -150.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100.0, Const.round( -150.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200.0, Const.round( -170.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100.0, Const.round( -170.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100.0, Const.round( -170.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200.0, Const.round( -170.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200.0, Const.round( -170.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200.0, Const.round( -170.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200.0, Const.round( -170.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200.0, Const.round( -170.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200.0, Const.round( -200.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200.0, Const.round( -200.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300.0, Const.round( -220.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200.0, Const.round( -220.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200.0, Const.round( -220.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300.0, Const.round( -220.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200.0, Const.round( -220.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200.0, Const.round( -220.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200.0, Const.round( -220.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200.0, Const.round( -220.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300.0, Const.round( -250.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200.0, Const.round( -250.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200.0, Const.round( -250.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300.0, Const.round( -250.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -300.0, Const.round( -250.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200.0, Const.round( -250.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200.0, Const.round( -250.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200.0, Const.round( -250.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300.0, Const.round( -270.0, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200.0, Const.round( -270.0, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200.0, Const.round( -270.0, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300.0, Const.round( -270.0, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -300.0, Const.round( -270.0, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -300.0, Const.round( -270.0, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -300.0, Const.round( -270.0, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -300.0, Const.round( -270.0, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( Double.NaN, Const.round( Double.NaN, 0, BigDecimal.ROUND_UP ) );
    assertEquals( Double.NEGATIVE_INFINITY, Const.round( Double.NEGATIVE_INFINITY, 0, BigDecimal.ROUND_UP ) );
    assertEquals( Double.POSITIVE_INFINITY, Const.round( Double.POSITIVE_INFINITY, 0, BigDecimal.ROUND_UP ) );
  }

  @Test
  public void testRound_Long() {
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 1L, Const.round( 1L, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 1L, Const.round( 1L, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_UP ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 2L, Const.round( 2L, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 2L, Const.round( 2L, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -1L, Const.round( -1L, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -1L, Const.round( -1L, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_UP ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_DOWN ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_CEILING ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -2L, Const.round( -2L, 0, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -2L, Const.round( -2L, 0, Const.ROUND_HALF_CEILING ) );

    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 100L, Const.round( 100L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 100L, Const.round( 100L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200L, Const.round( 120L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100L, Const.round( 120L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200L, Const.round( 120L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100L, Const.round( 120L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 100L, Const.round( 120L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100L, Const.round( 120L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 100L, Const.round( 120L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 100L, Const.round( 120L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200L, Const.round( 150L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100L, Const.round( 150L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200L, Const.round( 150L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100L, Const.round( 150L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200L, Const.round( 150L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 100L, Const.round( 150L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200L, Const.round( 150L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200L, Const.round( 150L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200L, Const.round( 170L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 100L, Const.round( 170L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200L, Const.round( 170L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 100L, Const.round( 170L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200L, Const.round( 170L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200L, Const.round( 170L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200L, Const.round( 170L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200L, Const.round( 170L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200L, Const.round( 200L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200L, Const.round( 200L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300L, Const.round( 220L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200L, Const.round( 220L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300L, Const.round( 220L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200L, Const.round( 220L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 200L, Const.round( 220L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200L, Const.round( 220L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200L, Const.round( 220L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 200L, Const.round( 220L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300L, Const.round( 250L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200L, Const.round( 250L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300L, Const.round( 250L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200L, Const.round( 250L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 300L, Const.round( 250L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 200L, Const.round( 250L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 200L, Const.round( 250L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 300L, Const.round( 250L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( 300L, Const.round( 270L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( 200L, Const.round( 270L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( 300L, Const.round( 270L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( 200L, Const.round( 270L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( 300L, Const.round( 270L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( 300L, Const.round( 270L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( 300L, Const.round( 270L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( 300L, Const.round( 270L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -100L, Const.round( -100L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100L, Const.round( -100L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200L, Const.round( -120L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100L, Const.round( -120L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100L, Const.round( -120L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200L, Const.round( -120L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -100L, Const.round( -120L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100L, Const.round( -120L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -100L, Const.round( -120L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100L, Const.round( -120L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200L, Const.round( -150L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100L, Const.round( -150L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100L, Const.round( -150L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200L, Const.round( -150L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200L, Const.round( -150L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -100L, Const.round( -150L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200L, Const.round( -150L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -100L, Const.round( -150L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200L, Const.round( -170L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -100L, Const.round( -170L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -100L, Const.round( -170L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200L, Const.round( -170L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200L, Const.round( -170L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200L, Const.round( -170L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200L, Const.round( -170L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200L, Const.round( -170L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200L, Const.round( -200L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200L, Const.round( -200L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300L, Const.round( -220L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200L, Const.round( -220L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200L, Const.round( -220L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300L, Const.round( -220L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -200L, Const.round( -220L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200L, Const.round( -220L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200L, Const.round( -220L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200L, Const.round( -220L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300L, Const.round( -250L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200L, Const.round( -250L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200L, Const.round( -250L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300L, Const.round( -250L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -300L, Const.round( -250L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -200L, Const.round( -250L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -200L, Const.round( -250L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -200L, Const.round( -250L, -2, Const.ROUND_HALF_CEILING ) );

    assertEquals( -300L, Const.round( -270L, -2, BigDecimal.ROUND_UP ) );
    assertEquals( -200L, Const.round( -270L, -2, BigDecimal.ROUND_DOWN ) );
    assertEquals( -200L, Const.round( -270L, -2, BigDecimal.ROUND_CEILING ) );
    assertEquals( -300L, Const.round( -270L, -2, BigDecimal.ROUND_FLOOR ) );
    assertEquals( -300L, Const.round( -270L, -2, BigDecimal.ROUND_HALF_UP ) );
    assertEquals( -300L, Const.round( -270L, -2, BigDecimal.ROUND_HALF_DOWN ) );
    assertEquals( -300L, Const.round( -270L, -2, BigDecimal.ROUND_HALF_EVEN ) );
    assertEquals( -300L, Const.round( -270L, -2, Const.ROUND_HALF_CEILING ) );
  }

  public static void assertEquals( Object expected, Object actual ) {
    assertEquals( "", expected, actual );
  }

  public static void assertEquals( String msg, Object expected, Object actual ) {
    if ( expected instanceof BigDecimal && actual instanceof BigDecimal ) {
      if ( ( (BigDecimal) expected ).compareTo( (BigDecimal) actual ) != 0 ) {
        Assert.assertEquals( msg, expected, actual );
      }
    } else if ( expected instanceof Number && actual instanceof Number ) {
      Assert.assertEquals( msg + " dataType(" + expected + "," + actual + ")", expected.getClass(), actual.getClass() );
      Assert.assertEquals( msg, expected, actual );
    } else {
      Assert.assertEquals( msg, expected, actual );
    }
  }

  @Test
  public void testToInt() {
    assertEquals( 123, Const.toInt( "123", -12 ) );
    assertEquals( -12, Const.toInt( "123f", -12 ) );
  }

  @Test
  public void testToLong() {
    assertEquals( 1447252914241L, Const.toLong( "1447252914241", -12 ) );
    assertEquals( -1447252914241L, Const.toLong( "1447252914241L", -1447252914241L ) );
  }

  @Test
  public void testToDouble() {
    Assert.assertEquals( 123.45, Const.toDouble( "123.45", -12.34 ), 1e-15 );
    Assert.assertEquals( -12.34, Const.toDouble( "123asd", -12.34 ), 1e-15 );
  }

  @Test
  public void testRightPad() {
    final String s = "Pad me baby one more time";

    assertEquals( "     ", Const.rightPad( (String) null, 5 ) );
    assertEquals( "Pad", Const.rightPad( s, 3 ) );

    final StringBuffer sb = new StringBuffer( s );
    assertEquals( s + "   ", Const.rightPad( sb, 28 ) );
    assertEquals( "Pad me baby", Const.rightPad( sb, 11 ) );

    final StringBuilder sb2 = new StringBuilder( s );
    assertEquals( s + "   ", Const.rightPad( sb2, 28 ) );
    assertEquals( "Pad me baby", Const.rightPad( sb2, 11 ) );
  }

  @Test
  public void testReplace() {
    final String source = "A journey of a thousand miles never begins";
    assertEquals( "A journey of a thousand miles begins with a single step", Const.replace( source, "never begins",
        "begins with a single step" ) );
    assertEquals( source, Const.replace( source, "evil", "good" ) );
    assertEquals( "short", Const.replace( "short", "long pattern", "replacement" ) );
    assertEquals( "", Const.replace( "", "anything", "something" ) );
    assertEquals( null, Const.replace( null, "test", "junk" ) );
    assertEquals( null, Const.replace( "test", null, "junk" ) );
    assertEquals( null, Const.replace( "test", "junk", null ) );
  }

  @Test
  public void testRepl() {
    String source = "A journey of a thousand miles never begins";
    StringBuffer sb = new StringBuffer( source );
    Const.repl( sb, "never begins", "begins with a single step" );
    assertEquals( "A journey of a thousand miles begins with a single step", sb.toString() );
    sb = new StringBuffer( source );
    Const.repl( sb, "evil", "good" );
    assertEquals( source, sb.toString() );
    sb = new StringBuffer( "short" );
    Const.repl( sb, "long pattern", "replacement" );
    assertEquals( "short", sb.toString() );
    sb = new StringBuffer( "" );
    Const.repl( sb, "anything", "something" );
    assertEquals( "", sb.toString() );
    sb = new StringBuffer( "Replace what looks like a regex '[a-z1-3*+]' with '$1'" );
    Const.repl( sb, "[a-z1-3*+]", "$1" );
    assertEquals( "Replace what looks like a regex '$1' with '$1'", sb.toString() );

    // StringBuilder version
    StringBuilder sb2 = new StringBuilder( source );
    Const.repl( sb2, "never begins", "begins with a single step" );
    assertEquals( "A journey of a thousand miles begins with a single step", sb2.toString() );
    sb2 = new StringBuilder( source );
    Const.repl( sb2, "evil", "good" );
    assertEquals( source, sb2.toString() );
    sb2 = new StringBuilder( "short" );
    Const.repl( sb2, "long pattern", "replacement" );
    assertEquals( "short", sb2.toString() );
    sb2 = new StringBuilder( "" );
    Const.repl( sb2, "anything", "something" );
    assertEquals( "", sb2.toString() );
    sb2 = new StringBuilder( "Replace what looks like a regex '[a-z1-3*+]' with '$1'" );
    Const.repl( sb2, "[a-z1-3*+]", "$1" );
    assertEquals( "Replace what looks like a regex '$1' with '$1'", sb2.toString() );

    sb2 = new StringBuilder( "JUNK" );
    Const.repl(  sb2, null, "wibble" );
    assertEquals( "JUNK", sb2.toString() );
    Const.repl(  sb2, "JUNK", null );

  }

  @Test
  public void testGetOS() {
    final String key = "os.name";
    final String os = System.getProperty( key );
    System.setProperty( key, "BeOS" );
    assertEquals( "BeOS", Const.getOS() );
    System.setProperty( key, os );
  }

  @Test
  public void testQuoteCharByOS() {
    assertEquals( SystemUtils.IS_OS_WINDOWS ? "\"" : "'", Const.getQuoteCharByOS() );
  }

  @Test
  public void testOptionallyQuoteStringByOS() {
    assertEquals( Const.getQuoteCharByOS() + "Quote me" + Const.getQuoteCharByOS(), Const.optionallyQuoteStringByOS(
        "Quote me" ) );
    assertEquals( Const.getQuoteCharByOS() + "Quote=me" + Const.getQuoteCharByOS(), Const.optionallyQuoteStringByOS(
        "Quote=me" ) );
    assertEquals( "Quoteme", Const.optionallyQuoteStringByOS( "Quoteme" ) );
    assertEquals( "Quote" + Const.getQuoteCharByOS() + "me", Const.optionallyQuoteStringByOS( "Quote" + Const
        .getQuoteCharByOS() + "me" ) );
  }

  @Test
  public void testIsWindows() {
    assertEquals( SystemUtils.IS_OS_WINDOWS, Const.isWindows() );
  }

  @Test
  public void testIsLinux() {
    assertEquals( SystemUtils.IS_OS_LINUX, Const.isLinux() );
  }

  @Test
  public void testIsOSX() {
    assertEquals( SystemUtils.IS_OS_MAC_OSX, Const.isOSX() );
  }

  @Test
  public void testIsKDE() {
    final String kdeVersion = System.getProperty( "KDE_SESSION_VERSION" );
    assertEquals( kdeVersion != null && !kdeVersion.isEmpty(), Const.isKDE() );
  }

  @Test
  public void testGetHostName() {
    assertFalse( Const.getHostname().isEmpty() );
  }

  @Test
  public void testGetHostnameReal() {
    doWithModifiedSystemProperty( "KETTLE_SYSTEM_HOSTNAME", "MyHost", new Runnable() {
      @Override
      public void run() {
        assertEquals( "MyHost", Const.getHostnameReal() );
      }
    } );
  }

  @Test
  public void testReplEnv() {
    assertNull( Const.replEnv( (String) null ) );
    System.setProperty( "testProp", "testValue" );
    assertEquals( "Value for testProp property is testValue.", Const.replEnv(
        "Value for testProp property is %%testProp%%." ) );
    assertEquals( "Value for testProp property is testValue.", Const.replEnv( new String[] {
      "Value for testProp property is %%testProp%%." } )[0] );
  }

  @Test
  public void testNullToEmpty() {
    assertEquals( "", Const.nullToEmpty( null ) );
    assertEquals( "value", Const.nullToEmpty( "value" ) );
  }

  @Test
  public void testIndexOfString() {
    assertEquals( -1, Const.indexOfString( null, (String[]) null ) );
    assertEquals( -1, Const.indexOfString( null, new String[] {} ) );
    assertEquals( 1, Const.indexOfString( "bar", new String[] { "foo", "bar" } ) );
    assertEquals( -1, Const.indexOfString( "baz", new String[] { "foo", "bar" } ) );
    assertEquals( -1, Const.indexOfString( null, (List<String>) null ) );
    assertEquals( 1, Const.indexOfString( "bar", Arrays.asList( "foo", "bar" ) ) );
    assertEquals( -1, Const.indexOfString( "baz", Arrays.asList( "foo", "bar" ) ) );
  }

  @Test
  public void testIndexsOfStrings() {
    Assert.assertArrayEquals( new int[] { 2, 1, -1 }, Const.indexsOfStrings( new String[] { "foo", "bar", "qux" },
        new String[] { "baz", "bar", "foo" } ) );
  }

  @Test
  public void testIndexsOfFoundStrings() {
    Assert.assertArrayEquals( new int[] { 2, 1 }, Const.indexsOfFoundStrings( new String[] { "qux", "foo", "bar" },
        new String[] { "baz", "bar", "foo" } ) );
  }

  @Test
  public void testGetDistinctStrings() {
    assertNull( Const.getDistinctStrings( null ) );
    assertTrue( Const.getDistinctStrings( new String[] {} ).length == 0 );
    Assert.assertArrayEquals( new String[] { "bar", "foo" }, Const.getDistinctStrings( new String[] { "foo", "bar", "foo",
      "bar" } ) );
  }

  @Test
  public void testStackTracker() {
    assertTrue( Const.getStackTracker( new Exception() ).contains( getClass().getName() + ".testStackTracker("
        + getClass().getSimpleName() + ".java:" ) );
  }

  @Test
  public void testGetCustomStackTrace() {
    assertTrue( Const.getCustomStackTrace( new Exception() ).contains( getClass().getName()
        + ".testGetCustomStackTrace(" + getClass().getSimpleName() + ".java:" ) );
  }

  @Test
  public void testCreateNewClassLoader() throws KettleException {
    ClassLoader cl = Const.createNewClassLoader();
    assertTrue( cl instanceof URLClassLoader && ( (URLClassLoader) cl ).getURLs().length == 0 );
  }

  @Test
  public void testCreateByteArray() {
    assertTrue( Const.createByteArray( 5 ).length == 5 );
  }

  @Test
  public void testCreateFilename() {
    assertEquals( "dir" + Const.FILE_SEPARATOR + "file__1.ext", Const.createFilename( "dir" + Const.FILE_SEPARATOR,
        "File\t~ 1", ".ext" ) );
    assertEquals( "dir" + Const.FILE_SEPARATOR + "file__1.ext", Const.createFilename( "dir", "File\t~ 1", ".ext" ) );
  }

  @Test
  public void testCreateName() {
    assertNull( Const.createName( null ) );
    assertEquals( "test - trans", Const.createName( "transformations" + Const.FILE_SEPARATOR + "test\t~- trans.ktr" ) );
  }

  @Test
  public void testFilenameOnly() {
    assertNull( Const.filenameOnly( null ) );
    assertTrue( Const.filenameOnly( "" ).isEmpty() );
    assertEquals( "file.txt", Const.filenameOnly( "dir" + Const.FILE_SEPARATOR + "file.txt" ) );
    assertEquals( "file.txt", Const.filenameOnly( "file.txt" ) );
  }

  @Test
  public void testGetDateFormats() {
    final String[] formats = Const.getDateFormats();
    assertTrue( formats.length > 0 );
    for ( String format : formats ) {
      assertTrue( format != null && !format.isEmpty() );
    }
  }

  @Test
  public void testGetNumberFormats() {
    final String[] formats = Const.getNumberFormats();
    assertTrue( formats.length > 0 );
    for ( String format : formats ) {
      assertTrue( format != null && !format.isEmpty() );
    }
  }

  @Test
  public void testGetConversionFormats() {
    final List<String> dateFormats = Arrays.asList( Const.getDateFormats() );
    final List<String> numberFormats = Arrays.asList( Const.getNumberFormats() );
    final List<String> conversionFormats = Arrays.asList( Const.getConversionFormats() );
    assertEquals( dateFormats.size() + numberFormats.size(), conversionFormats.size() );
    assertTrue( conversionFormats.containsAll( dateFormats ) );
    assertTrue( conversionFormats.containsAll( numberFormats ) );
  }

  @Test
  public void testGetTransformationAndJobFilterNames() {
    List<String> filters = Arrays.asList( Const.getTransformationAndJobFilterNames() );
    assertTrue( filters.size() == 5 );
    for ( String filter : filters ) {
      assertFalse( filter.isEmpty() );
    }
  }

  @Test
  public void testGetTransformationFilterNames() {
    List<String> filters = Arrays.asList( Const.getTransformationFilterNames() );
    assertTrue( filters.size() == 3 );
    for ( String filter : filters ) {
      assertFalse( filter.isEmpty() );
    }
  }

  @Test
  public void testGetJobFilterNames() {
    List<String> filters = Arrays.asList( Const.getJobFilterNames() );
    assertTrue( filters.size() == 3 );
    for ( String filter : filters ) {
      assertFalse( filter.isEmpty() );
    }
  }

  @Test
  public void testNanoTime() {
    assertTrue( String.valueOf( Const.nanoTime() ).endsWith( "000" ) );
  }

  @Test
  public void testTrimToType() {
    final String source = " trim me hard ";
    assertEquals( "trim me hard", Const.trimToType( source, ValueMetaInterface.TRIM_TYPE_BOTH ) );
    assertEquals( "trim me hard ", Const.trimToType( source, ValueMetaInterface.TRIM_TYPE_LEFT ) );
    assertEquals( " trim me hard", Const.trimToType( source, ValueMetaInterface.TRIM_TYPE_RIGHT ) );
    assertEquals( source, Const.trimToType( source, ValueMetaInterface.TRIM_TYPE_NONE ) );
  }

  @Test
  public void testSafeAppendDirectory() {
    final String expected = "dir" + Const.FILE_SEPARATOR + "file";
    assertEquals( expected, Const.safeAppendDirectory( "dir", "file" ) );
    assertEquals( expected, Const.safeAppendDirectory( "dir" + Const.FILE_SEPARATOR, "file" ) );
    assertEquals( expected, Const.safeAppendDirectory( "dir", Const.FILE_SEPARATOR + "file" ) );
    assertEquals( expected, Const.safeAppendDirectory( "dir" + Const.FILE_SEPARATOR, Const.FILE_SEPARATOR + "file" ) );
  }

  @Test
  public void testGetEmptyPaddedStrings() {
    final String[] strings = Const.getEmptyPaddedStrings();
    for ( int i = 0; i < 250; i++ ) {
      assertEquals( i, strings[i].length() );
    }
  }

  @Test
  public void testGetPercentageFreeMemory() {
    assertTrue( Const.getPercentageFreeMemory() > 0 );
  }

  @Test
  public void testRemoveDigits() {
    assertNull( Const.removeDigits( null ) );
    assertEquals( "foobar", Const.removeDigits( "123foo456bar789" ) );
  }

  @Test
  public void testGetDigitsOnly() {
    assertNull( Const.removeDigits( null ) );
    assertEquals( "123456789", Const.getDigitsOnly( "123foo456bar789" ) );
  }

  @Test
  public void testRemoveTimeFromDate() {
    final Date date = Const.removeTimeFromDate( new Date() );
    assertEquals( 0, date.getHours() );
    assertEquals( 0, date.getMinutes() );
    assertEquals( 0, date.getSeconds() );
  }

  @Test
  public void testEscapeUnescapeXML() {
    final String xml = "<xml xmlns:test=\"http://test\">";
    final String escaped = "&lt;xml xmlns:test=&quot;http://test&quot;&gt;";
    assertNull( Const.escapeXML( null ) );
    assertNull( Const.unEscapeXml( null ) );
    assertEquals( escaped, Const.escapeXML( xml ) );
    assertEquals( xml, Const.unEscapeXml( escaped ) );
  }

  @Test
  public void testEscapeUnescapeHtml() {
    final String html = "<td>";
    final String escaped = "&lt;td&gt;";
    assertNull( Const.escapeHtml( null ) );
    assertNull( Const.unEscapeHtml( null ) );
    assertEquals( escaped, Const.escapeHtml( html ) );
    assertEquals( html, Const.unEscapeHtml( escaped ) );
  }

  @Test
  public void testEscapeSQL() {
    assertEquals( "SELECT ''Let''s rock!'' FROM dual", Const.escapeSQL( "SELECT 'Let's rock!' FROM dual" ) );
  }

  @Test
  public void testRemoveCRLF() {
    assertEquals( "foo\tbar", Const.removeCRLF( "foo\r\n\tbar" ) );
    assertEquals( "", Const.removeCRLF( "" ) );
    assertEquals( "", Const.removeCRLF( null ) );
    assertEquals( "", Const.removeCRLF( "\r\n" ) );
    assertEquals( "This is a test of the emergency broadcast system",
        Const.removeCRLF( "This \r\nis \ra \ntest \rof \n\rthe \r\nemergency \rbroadcast \nsystem\r\n" ) );
  }

  @Test
  public void testRemoveCR() {
    assertEquals( "foo\n\tbar", Const.removeCR( "foo\r\n\tbar" ) );
    assertEquals( "", Const.removeCR( "" ) );
    assertEquals( "", Const.removeCR( null ) );
    assertEquals( "", Const.removeCR( "\r" ) );
    assertEquals( "\n\n", Const.removeCR( "\n\r\n" ) );
    assertEquals( "This \nis a \ntest of \nthe \nemergency broadcast \nsystem\n",
        Const.removeCR( "This \r\nis \ra \ntest \rof \n\rthe \r\nemergency \rbroadcast \nsystem\r\n" ) );
  }

  @Test
  public void testRemoveLF() {
    assertEquals( "foo\r\tbar", Const.removeLF( "foo\r\n\tbar" ) );
    assertEquals( "", Const.removeLF( "" ) );
    assertEquals( "", Const.removeLF( null ) );
    assertEquals( "", Const.removeLF( "\n" ) );
    assertEquals( "\r\r", Const.removeLF( "\r\n\r" ) );
    assertEquals( "This \ris \ra test \rof \rthe \remergency \rbroadcast system\r",
        Const.removeLF( "This \r\nis \ra \ntest \rof \n\rthe \r\nemergency \rbroadcast \nsystem\r\n" ) );
  }

  @Test
  public void testRemoveTAB() {
    assertEquals( "foo\r\nbar", Const.removeTAB( "foo\r\n\tbar" ) );
    assertEquals( "", Const.removeTAB( "" ) );
    assertEquals( "", Const.removeTAB( null ) );
    assertEquals( "", Const.removeTAB( "\t" ) );
    assertEquals( "\r", Const.removeTAB( "\t\r\t" ) );
    assertEquals( "Thisisatest",
        Const.removeTAB( "\tThis\tis\ta\ttest" ) );
  }

  @Test
  public void testAddTimeToDate() throws Exception {
    final Date date = new Date( 1447252914241L );
    assertNull( Const.addTimeToDate( null, null, null ) );
    assertEquals( date, Const.addTimeToDate( date, null, null ) );
    assertEquals( 1447256637241L, Const.addTimeToDate( date, "01:02:03", "HH:mm:ss" ).getTime() );
  }

  @Test
  public void testGetOccurenceString() {
    assertEquals( 0, Const.getOccurenceString( "", "" ) );
    assertEquals( 0, Const.getOccurenceString( "foo bar bazfoo", "cat" ) );
    assertEquals( 2, Const.getOccurenceString( "foo bar bazfoo", "foo" ) );
  }

  @Test
  public void testGetAvailableFontNames() {
    assertTrue( Const.GetAvailableFontNames().length > 0 );
  }

  @Test
  public void testGetKettlePropertiesFileHeader() {
    assertFalse( Const.getKettlePropertiesFileHeader().isEmpty() );
  }

  @Test
  public void testProtectXMLCDATA() {
    assertEquals( null, Const.protectXMLCDATA( null ) );
    assertEquals( "", Const.protectXMLCDATA( "" ) );
    assertEquals( "<![CDATA[foo]]>", Const.protectXMLCDATA( "foo" ) );
  }

  @Test
  public void testGetOcuranceString() {
    assertEquals( 0, Const.getOcuranceString( "", "" ) );
    assertEquals( 0, Const.getOcuranceString( "foo bar bazfoo", "cat" ) );
    assertEquals( 2, Const.getOcuranceString( "foo bar bazfoo", "foo" ) );
  }

  @Test
  public void testEscapeXml() {
    final String xml = "<xml xmlns:test=\"http://test\">";
    final String escaped = "&lt;xml xmlns:test=&quot;http://test&quot;&gt;";
    assertNull( Const.escapeXml( null ) );
    assertEquals( escaped, Const.escapeXml( xml ) );
  }

  @Test
  public void testLpad() {
    final String s = "pad me";
    assertEquals( s, Const.Lpad( s, "-", 0 ) );
    assertEquals( s, Const.Lpad( s, "-", 3 ) );
    assertEquals( "--" + s, Const.Lpad( s, "-", 8 ) );
    // add in some edge cases
    assertEquals( s, Const.Lpad( s, null, 15 ) ); // No NPE
    assertEquals( s, Const.Lpad( s, "", 15 ) );
    assertEquals( s, Const.Lpad( s, "*", 5 ) );
    assertEquals( null, Const.Lpad( null, "*", 15 ) );
    assertEquals( "****Test", Const.Lpad( "Test", "**********", 8 ) );
    assertEquals( "*Test", Const.Lpad( "Test", "**", 5 ) );
    assertEquals( "****", Const.Lpad( "", "*", 4 ) );
  }

  @Test
  public void testRpad() {
    final String s = "pad me";
    assertEquals( s, Const.Rpad( s, "-", 0 ) );
    assertEquals( s, Const.Rpad( s, "-", 3 ) );
    assertEquals( s + "--", Const.Rpad( s, "-", 8 ) );
    // add in some edge cases
    assertEquals( s, Const.Rpad( s, null, 15 ) ); // No NPE
    assertEquals( s, Const.Rpad( s, "", 15 ) );
    assertEquals( s, Const.Rpad( s, "*", 5 ) );
    assertEquals( null, Const.Rpad( null, "*", 15 ) );
    assertEquals( "Test****", Const.Rpad( "Test", "**********", 8 ) );
    assertEquals( "Test*", Const.Rpad( "Test", "**", 5 ) );
    assertEquals( "****", Const.Rpad( "", "*", 4 ) );
  }

  @Test
  public void testClassIsOrExtends() {
    assertFalse( Const.classIsOrExtends( Object.class, Object.class ) );
    assertTrue( Const.classIsOrExtends( String.class, String.class ) );
    assertTrue( Const.classIsOrExtends( ArrayList.class, ArrayList.class ) );
  }

  @Test
  public void testReleaseType() {
    for ( Const.ReleaseType type : Const.ReleaseType.values() ) {
      assertFalse( type.getMessage().isEmpty() );
    }
  }

  private void doWithModifiedSystemProperty( final String key, final String value, Runnable action ) {
    final String curValue = System.getProperty( key );
    System.setProperty( key, value );
    action.run();
    if ( curValue != null ) {
      System.setProperty( key, curValue );
    }
  }
}
