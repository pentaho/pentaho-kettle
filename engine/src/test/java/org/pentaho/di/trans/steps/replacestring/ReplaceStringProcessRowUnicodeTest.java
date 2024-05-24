/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.replacestring;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <p>There're four (4) main properties that affect the way the <code>processRow</code> method of {@link ReplaceString}
 * works; as so, there're plenty combinations for any set of "input string"/"Match string".</p>
 * <p>This class has a full set of tests to check several scenarios for invoking the processRow method for Unicode
 * "Input strings".</p>
 * <p>For Non Unicode "Input strings", check the {@link ReplaceStringProcessRowNoUnicodeTest} as they were originally
 * created with the same structure and the same tests. Comparing both of them should be very straightforward.</p>
 */
public class ReplaceStringProcessRowUnicodeTest {

  private StepMockHelper<ReplaceStringMeta, ReplaceStringData> stepMockHelper;

  /**
   * <p>The string to use as a replacement for any found match.</p>
   */
  private static final String TESTPROCESSROW_REPLACE_BY_STRING = "matched";

  /**
   * <p>The Unicode latin small letter 'a' as a string.</p>
   */
  private static final String UNICODE_LATIN_SMALL_LETTER_A =
    new String( new byte[] { 0, 97 }, StandardCharsets.UTF_16BE );
  /**
   * <p>The Unicode latin capital letter 'a' as a string.</p>
   */
  private static final String UNICODE_LATIN_CAPITAL_LETTER_A =
    new String( new byte[] { 0, 65 }, StandardCharsets.UTF_16BE );
  /**
   * <p>The Unicode fullwidth latin capital letter 'a' as a string.</p>
   */
  private static final String UNICODE_FULLWIDTH_LATIN_CAPITAL_LETTER_A =
    new String( new byte[] { -1, 65 }, StandardCharsets.UTF_16BE );
  /**
   * <p>The Unicode fullwidth latin small letter 'a' as a string.</p>
   */
  private static final String UNICODE_FULLWIDTH_LATIN_SMALL_LETTER_A =
    new String( new byte[] { -1, 33 }, StandardCharsets.UTF_16BE );

  /**
   * <p>The string to be used as input.</p>
   * <p>The string is composed of the following letters:</p>
   * <ul>
   * <li>latin small letter 'a'</li>
   * <li>latin capital letter 'a'</li>
   * <li>fullwidth latin capital letter 'a'</li>
   * <li>fullwidth latin small letter 'a'</li>
   * </ul>
   */
  private static final String TESTPROCESSROW_STRING =
    UNICODE_LATIN_SMALL_LETTER_A + UNICODE_LATIN_CAPITAL_LETTER_A + UNICODE_FULLWIDTH_LATIN_CAPITAL_LETTER_A
      + UNICODE_FULLWIDTH_LATIN_SMALL_LETTER_A;

  /**
   * <p>Just the fullwidth latin small letter 'a'.</p>
   */
  private static final String TESTPROCESSROW_SIMPLE_MATCH_PATTERN = UNICODE_FULLWIDTH_LATIN_SMALL_LETTER_A;

  /**
   * <p>The Regex expression for one or more fullwidth latin small letter 'a'.</p>
   */
  private static final String TESTPROCESSROW_REGEX_MATCH_PATTERN = UNICODE_FULLWIDTH_LATIN_SMALL_LETTER_A + '+';

  /**
   * <p>A Regex expression that matches the entire string.</p>
   * <p>It matches/expects a latin small letter 'a', two random characters and a fullwidth latin small letter 'a'.</p>
   */
  private static final String TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN = UNICODE_LATIN_SMALL_LETTER_A + ".."
    + UNICODE_FULLWIDTH_LATIN_SMALL_LETTER_A;

  /**
   * <p>Regex expression that splits the input string into two groups.</p>
   * <p>It matches/expects one or more letters 'a', followed by one or more letters that not 'a'.</p>
   *
   * @see #TESTPROCESSROW_REGEX_CONCATENATE_PATTERN
   */
  private static final String TESTPROCESSROW_REGEX_SPLIT_PATTERN =
    "([" + UNICODE_LATIN_SMALL_LETTER_A + "]+)([^" + UNICODE_LATIN_SMALL_LETTER_A + "]+)";

  /**
   * <p>The separator to be used in the concatenation of the identified groups.</p>
   *
   * @see #TESTPROCESSROW_REGEX_CONCATENATE_PATTERN
   */
  private static final char TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR = '-';

  /**
   * <p>Regex expression to concatenate the identified groups.</p>
   * <p>Concatenates the two groups by the same order, separating both with a {@value
   * #TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR}.</p>
   *
   * @see #TESTPROCESSROW_REGEX_SPLIT_PATTERN
   * @see #TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR
   */
  private static final String TESTPROCESSROW_REGEX_CONCATENATE_PATTERN =
    "$1" + TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR + "$2";

  @Before
  public void setUp() throws Exception {
    stepMockHelper = new StepMockHelper<>( "REPLACE STRING TEST", ReplaceStringMeta.class, ReplaceStringData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
      stepMockHelper.logChannelInterface );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString() );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), any( Object[].class ) );
    verify( stepMockHelper.logChannelInterface, never() ).logError( anyString(), (Throwable) any() );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void tearDown() throws Exception {
    stepMockHelper.cleanUp();
  }

  @Test
  public void testProcessRow_NullInput() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, null, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match each of the two last characters (small and capital letters)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 2 ) + "matchedmatched",
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString = prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
      TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match only the last character (capital letter)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 1 ) + TESTPROCESSROW_REPLACE_BY_STRING,
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString = prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
      TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExNo_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, false, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the two last characters (small and capital letters)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 2 ) + "matchedmatched",
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_RegexSplitAndConcatenate()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_SPLIT_PATTERN,
        TESTPROCESSROW_REGEX_CONCATENATE_PATTERN );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    String[] splits =
      StringUtils.split( (String) replaceString.getRow()[ 1 ], TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR );
    assertEquals( 2, splits.length );
    assertEquals( 2, splits[ 0 ].length() );
    assertEquals( 2, splits[ 1 ].length() );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match, as one, the two last characters (small and capital letters)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 2 ) + TESTPROCESSROW_REPLACE_BY_STRING,
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordNo_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_RegexSplitAndConcatenate()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_SPLIT_PATTERN,
        TESTPROCESSROW_REGEX_CONCATENATE_PATTERN );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    String[] splits =
      StringUtils.split( (String) replaceString.getRow()[ 1 ], TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR );
    assertEquals( 2, splits.length );
    assertEquals( 2, splits[ 0 ].length() );
    assertEquals( 2, splits[ 1 ].length() );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveNo_WholeWordYes_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, false, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should only match the last character (capital letter)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 1 ) + TESTPROCESSROW_REPLACE_BY_STRING,
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_RegexSplitAndConcatenate()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_SPLIT_PATTERN,
        TESTPROCESSROW_REGEX_CONCATENATE_PATTERN );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    String[] splits =
      StringUtils.split( (String) replaceString.getRow()[ 1 ], TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR );
    assertEquals( 2, splits.length );
    assertEquals( 1, splits[ 0 ].length() );
    assertEquals( 3, splits[ 1 ].length() );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should only match the last character (capital letter)
    assertEquals(
      TESTPROCESSROW_STRING.substring( 0, TESTPROCESSROW_STRING.length() - 1 ) + TESTPROCESSROW_REPLACE_BY_STRING,
      replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordNo_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, false, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_SimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_SimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_SIMPLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeSimpleMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeSimpleMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_STRING, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_RegexSplitAndConcatenate()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_SPLIT_PATTERN,
        TESTPROCESSROW_REGEX_CONCATENATE_PATTERN );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    String[] splits =
      StringUtils.split( (String) replaceString.getRow()[ 1 ], TESTPROCESSROW_REGEX_CONCATENATE_SEPARATOR );
    assertEquals( 2, splits.length );
    assertEquals( 1, splits[ 0 ].length() );
    assertEquals( 3, splits[ 1 ].length() );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_RegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_RegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should return the original string
    assertEquals( TESTPROCESSROW_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeRegexMatch() throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN,
        TESTPROCESSROW_REPLACE_BY_STRING );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    assertNotNull( replaceString.getRow()[ 1 ] );
    // Should match the whole string
    assertEquals( TESTPROCESSROW_REPLACE_BY_STRING, replaceString.getRow()[ 1 ] );
  }

  @Test
  public void testProcessRow_UseRegExYes_CaseSensitiveYes_WholeWordYes_UnicodeYes_WholeRegexMatch_NullReplaceBy()
    throws Exception {
    ReplaceStringData data = new ReplaceStringData();
    ReplaceStringMeta meta = getReplaceStringMeta( stepMockHelper, true, true, true, true );

    ReplaceString replaceString =
      prepareTestProcessRow( data, meta, TESTPROCESSROW_STRING, TESTPROCESSROW_REGEX_WHOLE_MATCH_PATTERN, null );

    replaceString.processRow( meta, data );

    assertNotNull( replaceString.getRow() );
    assertEquals( 2, replaceString.getRow().length );
    // Should return null
    assertNull( replaceString.getRow()[ 1 ] );
  }

  private ReplaceString prepareTestProcessRow( ReplaceStringData data, ReplaceStringMeta meta, String unicodeString,
                                               String matchPattern, String matchReplacer ) throws Exception {
    ReplaceString replaceString = Mockito.spy(
      new ReplaceString( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans ) );
    RowMetaInterface inputRowMeta = new RowMeta();
    Object[] _row = new Object[] { unicodeString, null };
    doReturn( _row ).when( replaceString ).getRow();
    inputRowMeta.addValueMeta( 0, new ValueMetaString( "string" ) );

    doReturn( new String[] { "string" } ).when( meta ).getFieldInStream();
    doReturn( new String[] { "output" } ).when( meta ).getFieldOutStream();
    doReturn( new String[] { matchPattern } ).when( meta ).getReplaceString();
    doReturn( new String[] { StringUtils.EMPTY } ).when( meta ).getFieldReplaceByString();
    doReturn( new String[] { matchReplacer } ).when( meta ).getReplaceByString();
    doReturn( new boolean[] { false } ).when( meta ).isSetEmptyString();

    replaceString.init( meta, data );
    replaceString.setInputRowMeta( inputRowMeta );
    data.outputRowMeta = inputRowMeta;
    data.inputFieldsNr = 1;
    data.numFields = 2;
    data.inStreamNrs = new int[] { 0 };
    data.replaceFieldIndex = new int[] { -1 };
    data.outStreamNrs = new String[] { StringUtils.EMPTY, "1" };
    data.replaceByString = new String[] { "1" };
    data.setEmptyString = new boolean[] { false, false };

    return replaceString;
  }

  private ReplaceStringMeta getReplaceStringMeta( StepMockHelper<ReplaceStringMeta, ReplaceStringData> stepMockHelper,
                                                  boolean parmUseRegEx, boolean parmCaseSensitive,
                                                  boolean parmWholeWord, boolean parmUnicode ) {
    ReplaceStringMeta meta = stepMockHelper.processRowsStepMetaInterface;

    doReturn( new boolean[] { parmUseRegEx } ).when( meta ).getUseRegEx();
    doReturn( new boolean[] { parmCaseSensitive } ).when( meta ).getCaseSensitive();
    doReturn( new boolean[] { parmWholeWord } ).when( meta ).getWholeWord();
    doReturn( new boolean[] { parmUnicode } ).when( meta ).isUnicode();

    return meta;
  }
}
