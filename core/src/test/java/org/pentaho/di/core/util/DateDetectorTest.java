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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.collections.BidiMap;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DateDetectorTest {

  private static final String SAMPLE_REGEXP = "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$";

  private static final String SAMPLE_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

  private static Date SAMPLE_DATE;

  private static String SAMPLE_DATE_STRING;

  private static final String LOCALE_en_US = "en_US";

  private static final String LOCALE_es = "es";

  private static final String SAMPLE_REGEXP_US = "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$";

  private static final String SAMPLE_DATE_FORMAT_US = "MM/dd/yyyy HH:mm:ss";

  private static Date SAMPLE_DATE_US;

  private static String SAMPLE_DATE_STRING_US;

  @BeforeClass
  public static void setUpClass() {
    SimpleDateFormat format = new SimpleDateFormat( SAMPLE_DATE_FORMAT );
    SAMPLE_DATE = new Date( 0 );
    SAMPLE_DATE_STRING = format.format( SAMPLE_DATE );

    SimpleDateFormat format_US = new SimpleDateFormat( SAMPLE_DATE_FORMAT_US );
    SAMPLE_DATE_US = new Date( 0 );
    SAMPLE_DATE_STRING_US = format_US.format( SAMPLE_DATE_US );
  }

  @Test
  public void testGetRegexpByDateFormat() {
    assertNull( DateDetector.getRegexpByDateFormat( null ) );
    assertEquals( SAMPLE_REGEXP, DateDetector.getRegexpByDateFormat( SAMPLE_DATE_FORMAT ) );
  }

  @Test
  public void testGetRegexpByDateFormatLocale() {
    assertNull( DateDetector.getRegexpByDateFormat( null, null ) );
    assertNull( DateDetector.getRegexpByDateFormat( null, LOCALE_en_US ) );
    // return null if we pass US dateformat without locale
    assertNull( DateDetector.getRegexpByDateFormat( SAMPLE_DATE_FORMAT_US ) );
    assertEquals( SAMPLE_REGEXP_US, DateDetector.getRegexpByDateFormat( SAMPLE_DATE_FORMAT_US, LOCALE_en_US ) );
  }

  @Test
  public void testGetDateFormatByRegex() {
    assertNull( DateDetector.getDateFormatByRegex( null ) );
    assertEquals( SAMPLE_DATE_FORMAT, DateDetector.getDateFormatByRegex( SAMPLE_REGEXP ) );
  }

  @Test
  public void testGetDateFormatByRegexLocale() {
    assertNull( DateDetector.getDateFormatByRegex( null, null ) );
    assertNull( DateDetector.getDateFormatByRegex( null, LOCALE_en_US ) );
    // return eu if we pass en_US regexp without locale
    assertEquals( SAMPLE_DATE_FORMAT, DateDetector.getDateFormatByRegex( SAMPLE_REGEXP_US ) );
    assertEquals( SAMPLE_DATE_FORMAT_US, DateDetector.getDateFormatByRegex( SAMPLE_REGEXP_US, LOCALE_en_US ) );
  }

  @Test
  public void testGetDateFromString() throws ParseException {
    assertEquals( SAMPLE_DATE_US, DateDetector.getDateFromString( SAMPLE_DATE_STRING_US ) );
    try {
      DateDetector.getDateFromString( null );
    } catch ( ParseException e ) {
      // expected exception
    }
  }

  @Test
  public void testGetDateFromStringLocale() throws ParseException {
    assertEquals( SAMPLE_DATE_US, DateDetector.getDateFromString( SAMPLE_DATE_STRING_US, LOCALE_en_US ) );
    try {
      DateDetector.getDateFromString( null );
    } catch ( ParseException e ) {
      // expected exception
    }
    try {
      DateDetector.getDateFromString( null, null );
    } catch ( ParseException e ) {
      // expected exception
    }
  }

  @Test
  public void testGetDateFromStringByFormat() throws ParseException {
    assertEquals( SAMPLE_DATE, DateDetector.getDateFromStringByFormat( SAMPLE_DATE_STRING, SAMPLE_DATE_FORMAT ) );
    try {
      DateDetector.getDateFromStringByFormat( SAMPLE_DATE_STRING, null );
    } catch ( ParseException e ) {
      // expected exception
    }
    try {
      DateDetector.getDateFromStringByFormat( null, SAMPLE_DATE_FORMAT );
    } catch ( ParseException e ) {
      // expected exception
    }
  }

  @Test
  public void testDetectDateFormat() {
    assertEquals( SAMPLE_DATE_FORMAT, DateDetector.detectDateFormat( SAMPLE_DATE_STRING, LOCALE_es ) );
    assertNull( DateDetector.detectDateFormat( null ) );
  }

  @Test
  public void testIsValidDate() {
    assertTrue( DateDetector.isValidDate( SAMPLE_DATE_STRING_US ) );
    assertFalse( DateDetector.isValidDate( null ) );
    assertTrue( DateDetector.isValidDate( SAMPLE_DATE_STRING, SAMPLE_DATE_FORMAT ) );
    assertFalse( DateDetector.isValidDate( SAMPLE_DATE_STRING, null ) );
  }

  @Test
  public void testIsValidDateFormatToStringDate() {
    assertTrue( DateDetector.isValidDateFormatToStringDate( SAMPLE_DATE_FORMAT_US, SAMPLE_DATE_STRING_US ) );
    assertFalse( DateDetector.isValidDateFormatToStringDate( null, SAMPLE_DATE_STRING_US ) );
    assertFalse( DateDetector.isValidDateFormatToStringDate( SAMPLE_DATE_FORMAT_US, null ) );
  }

  @Test
  public void testIsValidDateFormatToStringDateLocale() {
    assertTrue( DateDetector.isValidDateFormatToStringDate( SAMPLE_DATE_FORMAT_US, SAMPLE_DATE_STRING_US, LOCALE_en_US ) );
    assertFalse( DateDetector.isValidDateFormatToStringDate( null, SAMPLE_DATE_STRING, LOCALE_en_US ) );
    assertFalse( DateDetector.isValidDateFormatToStringDate( SAMPLE_DATE_FORMAT_US, null, LOCALE_en_US ) );
    assertTrue( DateDetector.isValidDateFormatToStringDate( SAMPLE_DATE_FORMAT_US, SAMPLE_DATE_STRING_US, null ) );
  }

  @Test
  public void testAllPatterns() {
    testPatternsFrom( DateDetector.DATE_FORMAT_TO_REGEXPS_US, LOCALE_en_US );
    testPatternsFrom( DateDetector.DATE_FORMAT_TO_REGEXPS, LOCALE_es );
  }

  private void testPatternsFrom( BidiMap formatToRegExps, String locale ) {
    Iterator iterator = formatToRegExps.keySet().iterator();
    while ( iterator.hasNext() ) {
      String pattern = (String) iterator.next();
      String dateString = buildTestDate( pattern );
      assertEquals( "Did not detect a matching date pattern using the date \"" + dateString + "\"", pattern,
          DateDetector.detectDateFormatBiased( dateString, locale, pattern ) );
    }
  }

  private String buildTestDate( String pattern ) {
    String dateString =
        pattern.replace( "dd", "31" ).replace( "yyyy", "2015" ).replace( "MMMM", "Decr" ).replace( "MMM", "Dec" )
            .replace( "MM", "12" ).replace( "yy", "15" ).replace( "HH", "12" ).replace( "mm", "00" ).replace( "ss",
                "00" ).replace( "SSS", "123" );
    System.out.println( pattern + " : " + dateString );
    return dateString;
  }

}
