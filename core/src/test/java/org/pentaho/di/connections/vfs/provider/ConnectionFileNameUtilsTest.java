/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.connections.vfs.provider;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
/**
 * Unit tests for ConnectionFileNameUtils class
 */
public class ConnectionFileNameUtilsTest {

  private ConnectionFileNameUtils utils;

  @Before
  public void setUp() {
    utils = ConnectionFileNameUtils.getInstance();
  }

  // ==================== getInstance() Tests ====================

  @Test
  public void testGetInstanceReturnsSameInstance() {
    ConnectionFileNameUtils instance1 = ConnectionFileNameUtils.getInstance();
    ConnectionFileNameUtils instance2 = ConnectionFileNameUtils.getInstance();

    assertSame( "getInstance should return the same instance ( singleton )", instance1, instance2 );
  }

  @Test
  public void testGetInstanceReturnsNonNull() {
    ConnectionFileNameUtils instance = ConnectionFileNameUtils.getInstance();

    assertNotNull( "getInstance should never return null", instance );
  }

  // ==================== appendPath() Tests ====================

  @Test
  public void testAppendPathWithValidPath() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, "folder/file.txt" );

    assertEquals( "/base/folder/file.txt", builder.toString() );
  }

  @Test
  public void testAppendPathWithLeadingSlash() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, "/folder/file.txt" );

    assertEquals( "/base/folder/file.txt", builder.toString() );
  }

  @Test
  public void testAppendPathToEmptyBuilder() {
    StringBuilder builder = new StringBuilder();

    utils.appendPath( builder, "folder/file.txt" );

    assertEquals( "folder/file.txt", builder.toString() );
  }

  @Test
  public void testAppendPathWithNullPath() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, null );

    assertEquals( "/base", builder.toString() );
  }

  @Test
  public void testAppendPathWithEmptyPath() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, "" );

    assertEquals( "/base", builder.toString() );
  }

  @Test
  public void testAppendPathWithOnlySlash() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, "/" );

    assertEquals( "/base", builder.toString() );
  }

  @Test
  public void testAppendPathAddsTrailingSeparatorToBase() {
    StringBuilder builder = new StringBuilder( "/base" );

    utils.appendPath( builder, "folder" );

    assertEquals( "/base/folder", builder.toString() );
  }

  @Test
  public void testAppendPathWhenBaseAlreadyHasTrailingSeparator() {
    StringBuilder builder = new StringBuilder( "/base/" );

    utils.appendPath( builder, "folder" );

    assertEquals( "/base/folder", builder.toString() );
  }

  // ==================== isDescendantOrSelf() Tests ====================

  @Test
  public void testIsDescendantOrSelfWithDirectChild() {
    boolean result = utils.isDescendantOrSelf( "/base/child", "/base" );

    assertTrue( "Direct child should be descendant", result );
  }

  @Test
  public void testIsDescendantOrSelfWithNestedChild() {
    boolean result = utils.isDescendantOrSelf( "/base/folder/subfolder/file.txt", "/base/folder" );

    assertTrue( "Nested child should be descendant", result );
  }

  @Test
  public void testIsDescendantOrSelfWithSamePath() {
    boolean result = utils.isDescendantOrSelf( "/base/folder", "/base/folder" );

    assertTrue( "Same path should be descendant ( self )", result );
  }

  @Test
  public void testIsDescendantOrSelfWithSibling() {
    boolean result = utils.isDescendantOrSelf( "/base/folder1", "/base/folder2" );

    assertFalse( "Sibling should not be descendant", result );
  }

  @Test
  public void testIsDescendantOrSelfWithParent() {
    boolean result = utils.isDescendantOrSelf( "/base", "/base/folder" );

    assertFalse( "Parent should not be descendant of child", result );
  }

  @Test
  public void testIsDescendantOrSelfWithUnrelatedPath() {
    boolean result = utils.isDescendantOrSelf( "/other/path", "/base" );

    assertFalse( "Unrelated path should not be descendant", result );
  }

  @Test
  public void testIsDescendantOrSelfWithPartialMatch() {
    boolean result = utils.isDescendantOrSelf( "/base-other/file", "/base" );

    assertFalse( "Partial prefix match should not be descendant", result );
  }

  @Test
  public void testIsDescendantOrSelfWithTrailingSeparators() {
    boolean result = utils.isDescendantOrSelf( "/base/folder/", "/base/" );

    assertTrue( "Paths with trailing separators should work", result );
  }

  @Test
  public void testIsDescendantOrSelfWithEmptyBasePath() {
    boolean result = utils.isDescendantOrSelf( "/base/folder", "" );

    assertTrue( "Any path is descendant of empty base", result );
  }

  @Test
  public void testIsDescendantOrSelfCaseSensitive() {
    boolean result = utils.isDescendantOrSelf( "/base/Folder", "/base/folder" );

    assertFalse( "Should be case-sensitive", result );
  }

  // ==================== trimLeadingSeparator() String Tests ====================

  @Test
  public void testTrimLeadingSeparatorWithLeadingSlash() {
    String result = utils.trimLeadingSeparator( "/folder/file.txt" );

    assertEquals( "folder/file.txt", result );
  }

  @Test
  public void testTrimLeadingSeparatorWithoutLeadingSlash() {
    String result = utils.trimLeadingSeparator( "folder/file.txt" );

    assertEquals( "folder/file.txt", result );
  }

  @Test
  public void testTrimLeadingSeparatorWithOnlySlash() {
    String result = utils.trimLeadingSeparator( "/" );

    assertEquals( "", result );
  }

  @Test
  public void testTrimLeadingSeparatorWithEmptyString() {
    String result = utils.trimLeadingSeparator( "" );

    assertEquals( "", result );
  }

  @Test
  public void testTrimLeadingSeparatorWithMultipleLeadingSlashes() {
    String result = utils.trimLeadingSeparator( "//folder" );

    assertEquals( "/folder", result );
  }

  // ==================== trimLeadingSeparator() StringBuilder Tests ====================

  @Test
  public void testTrimLeadingSeparatorBuilderWithLeadingSlash() {
    StringBuilder builder = new StringBuilder( "/folder/file.txt" );

    utils.trimLeadingSeparator( builder );

    assertEquals( "folder/file.txt", builder.toString() );
  }

  @Test
  public void testTrimLeadingSeparatorBuilderWithOnlySlash() {
    StringBuilder builder = new StringBuilder( "/" );

    utils.trimLeadingSeparator( builder );

    assertEquals( "", builder.toString() );
  }

  // ==================== ensureLeadingSeparator() String Tests ====================

  @Test
  public void testEnsureLeadingSeparatorWithoutSlash() {
    String result = utils.ensureLeadingSeparator( "folder/file.txt" );

    assertEquals( "/folder/file.txt", result );
  }

  @Test
  public void testEnsureLeadingSeparatorWithSlash() {
    String result = utils.ensureLeadingSeparator( "/folder/file.txt" );

    assertEquals( "/folder/file.txt", result );
  }

  @Test
  public void testEnsureLeadingSeparatorWithEmptyString() {
    String result = utils.ensureLeadingSeparator( "" );

    assertEquals( "/", result );
  }

  @Test
  public void testEnsureLeadingSeparatorWithOnlySlash() {
    String result = utils.ensureLeadingSeparator( "/" );

    assertEquals( "/", result );
  }

  // ==================== ensureLeadingSeparator() StringBuilder Tests ====================

  @Test
  public void testEnsureLeadingSeparatorBuilderWithoutSlash() {
    StringBuilder builder = new StringBuilder( "folder/file.txt" );

    utils.ensureLeadingSeparator( builder );

    assertEquals( "/folder/file.txt", builder.toString() );
  }

  @Test
  public void testEnsureLeadingSeparatorBuilderWithSlash() {
    StringBuilder builder = new StringBuilder( "/folder/file.txt" );

    utils.ensureLeadingSeparator( builder );

    assertEquals( "/folder/file.txt", builder.toString() );
  }

  @Test
  public void testEnsureLeadingSeparatorBuilderWithEmptyBuilder() {
    StringBuilder builder = new StringBuilder();

    utils.ensureLeadingSeparator( builder );

    assertEquals( "/", builder.toString() );
  }

  // ==================== ensureTrailingSeparator() String Tests ====================

  @Test
  public void testEnsureTrailingSeparatorWithoutSlash() {
    String result = utils.ensureTrailingSeparator( "/folder/file.txt" );

    assertEquals( "/folder/file.txt/", result );
  }

  @Test
  public void testEnsureTrailingSeparatorWithSlash() {
    String result = utils.ensureTrailingSeparator( "/folder/file.txt/" );

    assertEquals( "/folder/file.txt/", result );
  }

  @Test
  public void testEnsureTrailingSeparatorWithEmptyString() {
    String result = utils.ensureTrailingSeparator( "" );

    assertEquals( "/", result );
  }

  @Test
  public void testEnsureTrailingSeparatorWithOnlySlash() {
    String result = utils.ensureTrailingSeparator( "/" );

    assertEquals( "/", result );
  }

  // ==================== ensureTrailingSeparator() StringBuilder Tests ====================

  @Test
  public void testEnsureTrailingSeparatorBuilderWithoutSlash() {
    StringBuilder builder = new StringBuilder( "/folder/file.txt" );

    utils.ensureTrailingSeparator( builder );

    assertEquals( "/folder/file.txt/", builder.toString() );
  }

  @Test
  public void testEnsureTrailingSeparatorBuilderWithSlash() {
    StringBuilder builder = new StringBuilder( "/folder/file.txt/" );

    utils.ensureTrailingSeparator( builder );

    assertEquals( "/folder/file.txt/", builder.toString() );
  }

  @Test
  public void testEnsureTrailingSeparatorBuilderWithEmptyBuilder() {
    StringBuilder builder = new StringBuilder();

    utils.ensureTrailingSeparator( builder );

    assertEquals( "", builder.toString() );
  }

  // ==================== getFirstSegmentOfPath() Tests ====================

  @Test
  public void testGetFirstSegmentOfPathWithMultipleSegments() {
    String result = utils.getFirstSegmentOfPath( "/container/folder/file.txt" );

    assertEquals( "container", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithSingleSegment() {
    String result = utils.getFirstSegmentOfPath( "/container" );

    assertEquals( "container", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithoutLeadingSlash() {
    String result = utils.getFirstSegmentOfPath( "container/folder" );

    assertEquals( "container", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithOnlySegmentName() {
    String result = utils.getFirstSegmentOfPath( "container" );

    assertEquals( "container", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithEmptyString() {
    String result = utils.getFirstSegmentOfPath( "" );

    assertEquals( "", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithOnlySlash() {
    String result = utils.getFirstSegmentOfPath( "/" );

    assertEquals( "", result );
  }

  @Test
  public void testGetFirstSegmentOfPathWithTrailingSlash() {
    String result = utils.getFirstSegmentOfPath( "/container/" );

    assertEquals( "container", result );
  }

  // ==================== getPathAfterFirstSegment() Tests ====================

  @Test
  public void testGetPathAfterFirstSegmentWithMultipleSegments() {
    String result = utils.getPathAfterFirstSegment( "/container/folder/file.txt" );

    assertEquals( "/folder/file.txt", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithTwoSegments() {
    String result = utils.getPathAfterFirstSegment( "/container/folder" );

    assertEquals( "/folder", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithSingleSegment() {
    String result = utils.getPathAfterFirstSegment( "/container" );

    assertEquals( "", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithoutLeadingSlash() {
    String result = utils.getPathAfterFirstSegment( "container/folder/file.txt" );

    assertEquals( "/folder/file.txt", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithOnlySegmentName() {
    String result = utils.getPathAfterFirstSegment( "container" );

    assertEquals( "", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithEmptyString() {
    String result = utils.getPathAfterFirstSegment( "" );

    assertEquals( "", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithOnlySlash() {
    String result = utils.getPathAfterFirstSegment( "/" );

    assertEquals( "", result );
  }

  @Test
  public void testGetPathAfterFirstSegmentWithTrailingSlash() {
    String result = utils.getPathAfterFirstSegment( "/container/folder/" );

    assertEquals( "/folder/", result );
  }

  // ==================== Integration/Complex Scenario Tests ====================

  @Test
  public void testComplexPathManipulation() {
    StringBuilder builder = new StringBuilder();

    utils.ensureLeadingSeparator( builder );
    utils.appendPath( builder, "container" );
    utils.appendPath( builder, "/data" );
    utils.appendPath( builder, "subfolder/" );
    utils.ensureTrailingSeparator( builder );

    assertEquals( "/container/data/subfolder/", builder.toString() );
  }

  @Test
  public void testPathSegmentExtraction() {
    String path = "/container/data/subfolder/file.txt";

    String firstSegment = utils.getFirstSegmentOfPath( path );
    String afterFirst = utils.getPathAfterFirstSegment( path );

    assertEquals( "container", firstSegment );
    assertEquals( "/data/subfolder/file.txt", afterFirst );
  }

  @Test
  public void testDescendantCheckWithNormalizedPaths() {
    String descendant = "/base/folder/file";
    String base = "base/folder";

    String normalizedBase = utils.ensureLeadingSeparator( base );
    boolean isDescendant = utils.isDescendantOrSelf( descendant, normalizedBase );

    assertTrue( "Should handle path normalization", isDescendant );
  }

  @Test
  public void testTrimAndEnsureSeparatorIdempotence() {
    String original = "/folder/file.txt";

    String trimmed = utils.trimLeadingSeparator( original );
    String ensured = utils.ensureLeadingSeparator( trimmed );

    assertEquals( "Should return to original", original, ensured );
  }

  @Test
  public void testMultipleAppendOperations() {
    StringBuilder builder = new StringBuilder( "/root" );

    utils.appendPath( builder, "level1" );
    utils.appendPath( builder, "level2" );
    utils.appendPath( builder, "level3" );
    utils.appendPath( builder, "file.txt" );

    assertEquals( "/root/level1/level2/level3/file.txt", builder.toString() );
  }
}
