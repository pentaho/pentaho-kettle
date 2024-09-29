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

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public class DateDetector {

  private static final String LOCALE_en_US = "en_US";

  @SuppressWarnings( "serial" )
  static final BidiMap DATE_FORMAT_TO_REGEXPS_US = new DualHashBidiMap() {
    {
      put( "MM-dd-yyyy", "^[0-1]?[0-9]-[0-3]?[0-9]-\\d{4}$" );
      put( "dd/MM/yyyy", "^[0-3]?[0-9]/[0-1]?[0-9]/\\d{4}$" );
      put( "MM-dd-yy", "^[0-1]?[0-9]-[0-3]?[0-9]-\\d{2}$" );
      put( "dd/MM/yy", "^[0-3]?[0-9]/[0-1]?[0-9]/\\d{2}$" );
      put( "yyyyMMdd", "^\\d{8}$" );
      put( "dd-MM-yy", "^\\d{1,2}-\\d{1,2}-\\d{2}$" );
      put( "dd-MM-yyyy", "^\\d{1,2}-\\d{1,2}-\\d{4}$" );
      put( "dd.MM.yy", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}$" );
      put( "dd.MM.yyyy", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$" );
      put( "MM/dd/yy", "^\\d{1,2}/\\d{1,2}/\\d{2}$" );
      put( "MM/dd/yyyy", "^\\d{1,2}/\\d{1,2}/\\d{4}$" );
      put( "yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$" );
      put( "yyyy.MM.dd", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$" );
      put( "yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$" );
      put( "dd MMM yyyy", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$" );
      put( "dd MMMM yyyy", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$" );
      put( "yyyyMMddHHmm", "^\\d{12}$" );
      put( "yyyyMMdd HHmm", "^\\d{8}\\s\\d{4}$" );
      put( "dd-MM-yy HH:mm", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd-MM-yyyy HH:mm", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "dd.MM.yy HH:mm", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd.MM.yyyy HH:mm", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "MM/dd/yy HH:mm", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "MM/dd/yyyy HH:mm", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy-MM-dd HH:mm", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy.MM.dd HH:mm", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy/MM/dd HH:mm", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd MMM yyyy HH:mm", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "dd MMMM yyyy HH:mm", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyyMMddHHmmss", "^\\d{14}$" );
      put( "yyyyMMdd HHmmss", "^\\d{8}\\s\\d{6}$" );
      put( "dd-MM-yy HH:mm:ss", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd-MM-yyyy HH:mm:ss", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd.MM.yy HH:mm:ss", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd.MM.yyyy HH:mm:ss", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "MM/dd/yy HH:mm:ss", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "MM/dd/yyyy HH:mm:ss", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy.MM.dd HH:mm:ss", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd MMM yyyy HH:mm:ss", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd MMMM yyyy HH:mm:ss", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd-MM-yy HH:mm:ss.SSS", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd-MM-yyyy HH:mm:ss.SSS", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd.MM.yy HH:mm:ss.SSS", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd.MM.yyyy HH:mm:ss.SSS", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "MM/dd/yy HH:mm:ss.SSS", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "MM/dd/yyyy HH:mm:ss.SSS", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy-MM-dd HH:mm:ss.SSS", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy.MM.dd HH:mm:ss.SSS", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy/MM/dd HH:mm:ss.SSS", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd MMM yyyy HH:mm:ss.SSS", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd MMMM yyyy HH:mm:ss.SSS", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
    }
  };

  @SuppressWarnings( "serial" )
  static final BidiMap DATE_FORMAT_TO_REGEXPS = new DualHashBidiMap() {
    {
      put( "MM-dd-yyyy", "^[0-1]?[0-9]-[0-3]?[0-9]-\\d{4}$" );
      put( "dd/MM/yyyy", "^[0-3]?[0-9]/[0-1]?[0-9]/\\d{4}$" );
      put( "MM-dd-yy", "^[0-1]?[0-9]-[0-3]?[0-9]-\\d{2}$" );
      put( "dd/MM/yy", "^[0-3]?[0-9]/[0-1]?[0-9]/\\d{2}$" );
      put( "yyyyMMdd", "^\\d{8}$" );
      put( "dd-MM-yy", "^\\d{1,2}-\\d{1,2}-\\d{2}$" );
      put( "dd-MM-yyyy", "^\\d{1,2}-\\d{1,2}-\\d{4}$" );
      put( "dd.MM.yy", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}$" );
      put( "dd.MM.yyyy", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}$" );
      put( "dd/MM/yy", "^\\d{1,2}/\\d{1,2}/\\d{2}$" );
      put( "dd/MM/yyyy", "^\\d{1,2}/\\d{1,2}/\\d{4}$" );
      put( "yyyy-MM-dd", "^\\d{4}-\\d{1,2}-\\d{1,2}$" );
      put( "yyyy.MM.dd", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}$" );
      put( "yyyy/MM/dd", "^\\d{4}/\\d{1,2}/\\d{1,2}$" );
      put( "dd MMM yyyy", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$" );
      put( "dd MMMM yyyy", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$" );
      put( "yyyyMMddHHmm", "^\\d{12}$" );
      put( "yyyyMMdd HHmm", "^\\d{8}\\s\\d{4}$" );
      put( "dd-MM-yy HH:mm", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd-MM-yyyy HH:mm", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "dd.MM.yy HH:mm", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd.MM.yyyy HH:mm", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "dd/MM/yy HH:mm", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd/MM/yyyy HH:mm", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy-MM-dd HH:mm", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy.MM.dd HH:mm", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyy/MM/dd HH:mm", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$" );
      put( "dd MMM yyyy HH:mm", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "dd MMMM yyyy HH:mm", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$" );
      put( "yyyyMMddHHmmss", "^\\d{14}$" );
      put( "yyyyMMdd HHmmss", "^\\d{8}\\s\\d{6}$" );
      put( "dd-MM-yy HH:mm:ss", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd-MM-yyyy HH:mm:ss", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd.MM.yy HH:mm:ss", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd.MM.yyyy HH:mm:ss", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd/MM/yy HH:mm:ss", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd/MM/yyyy HH:mm:ss", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy-MM-dd HH:mm:ss", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy.MM.dd HH:mm:ss", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "yyyy/MM/dd HH:mm:ss", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd MMM yyyy HH:mm:ss", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd MMMM yyyy HH:mm:ss", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$" );
      put( "dd-MM-yy HH:mm:ss.SSS", "^\\d{1,2}-\\d{1,2}-\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd-MM-yyyy HH:mm:ss.SSS", "^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd.MM.yy HH:mm:ss.SSS", "^\\d{1,2}\\.\\d{1,2}\\.\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd.MM.yyyy HH:mm:ss.SSS", "^\\d{1,2}\\.\\d{1,2}\\.\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd/MM/yy HH:mm:ss.SSS", "^\\d{1,2}/\\d{1,2}/\\d{2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd/MM/yyyy HH:mm:ss.SSS", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy-MM-dd HH:mm:ss.SSS", "^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy.MM.dd HH:mm:ss.SSS", "^\\d{4}\\.\\d{1,2}\\.\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "yyyy/MM/dd HH:mm:ss.SSS", "^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd MMM yyyy HH:mm:ss.SSS", "^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
      put( "dd MMMM yyyy HH:mm:ss.SSS", "^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}$" );
    }
  };

  // util class, hide constructor
  private DateDetector() {
  };

  /**
   * 
   * @param dateFormat - date format for get regexp
   * @return regexp for given date format
   */
  public static String getRegexpByDateFormat( String dateFormat ) {
    return getRegexpByDateFormat( dateFormat, null );
  }

  /**
   * 
   * @param dateFormat - date format for get regexp by locale
   * @return regexp for given date format
   */
  public static String getRegexpByDateFormat( String dateFormat, String locale ) {
    if ( locale != null && LOCALE_en_US.equalsIgnoreCase( locale ) ) {
      return (String) DATE_FORMAT_TO_REGEXPS_US.get( dateFormat );
    }
    return (String) DATE_FORMAT_TO_REGEXPS.get( dateFormat );
  }

  /**
   * 
   * @param regex - regexp for parse date format from string
   * <br>
   * <b>NOTES:</b> if regex could be used for US and EU locale. 
   * It returns europeans locale. For en_US locale please use
   * 
   *  {@link #getDateFormatByRegex( String regex, String locale ) }
   *
   * @return {@link java.lang.String} string wich represented Date Format
   */
  public static String getDateFormatByRegex( String regex ) {
    return getDateFormatByRegex( regex, null );
  }

  /**
   * 
   * @param regex
   *          - regexp for parse date format from string by locale
   * @return {@link java.lang.String} string wich represented Date Format
   */
  public static String getDateFormatByRegex( String regex, String locale ) {
    if ( locale != null && LOCALE_en_US.equalsIgnoreCase( locale ) ) {
      return (String) DATE_FORMAT_TO_REGEXPS_US.getKey( regex );
    }
    return (String) DATE_FORMAT_TO_REGEXPS.getKey( regex );
  }

  /**
   * 
   * @param dateString
   *          date string for parse
   * @return {@link java.util.Date} converted from dateString by detected format
   * @throws ParseException
   *           - if we can not detect date format for string or we can not parse date string
   */
  public static Date getDateFromString( String dateString ) throws ParseException {
    String dateFormat = detectDateFormat( dateString );
    if ( dateFormat == null ) {
      throw new ParseException( "Unknown date format.", 0 );
    }
    return getDateFromStringByFormat( dateString, dateFormat );
  }

  /**
   * 
   * @param dateString
   *          date string for parse
   * @return {@link java.util.Date} converted from dateString by detected format
   * @throws ParseException
   *           - if we can not detect date format for string or we can not parse date string
   */
  public static Date getDateFromString( String dateString, String locale ) throws ParseException {
    String dateFormat = detectDateFormat( dateString, locale );
    if ( dateFormat == null ) {
      throw new ParseException( "Unknown date format.", 0 );
    }
    return getDateFromStringByFormat( dateString, dateFormat );
  }

  /**
   * 
   * @param dateString
   *          date string for parse
   * @param dateFormat
   *          format which should be applied for string
   * @return {@link java.util.Date} converted from dateString by format
   * @throws ParseException
   *           if we can not parse date string
   */
  public static Date getDateFromStringByFormat( String dateString, String dateFormat ) throws ParseException {
    if ( dateFormat == null ) {
      throw new ParseException( "Unknown date format. Format is null. ", 0 );
    }
    if ( dateString == null ) {
      throw new ParseException( "Unknown date string. Date string is null. ", 0 );
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( dateFormat );
    simpleDateFormat.setLenient( false ); // Don't automatically convert invalid date.
    return simpleDateFormat.parse( dateString );
  }

  /**
   * 
   * @param dateString
   *          - date string for detect date format
   * @return {@link java.lang.String} string which represented Date Format or null
   * 
   */
  public static String detectDateFormat( String dateString ) {
    return detectDateFormat( dateString, null );
  }

  /**
   * 
   * @param dateString
   *          - date string for detect date format
   * @return {@link java.lang.String} string which represented Date Format or null
   */
  public static String detectDateFormat( String dateString, String locale ) {
    if ( dateString == null ) {
      return null;
    }
    for ( Object regexp : getDateFormatToRegExps( locale ).values() ) {
      if ( dateString.toLowerCase().matches( (String) regexp ) ) {
        return (String) getDateFormatToRegExps( locale ).getKey( regexp );
      }
    }
    return null;
  }

  /**
   * Finds a date format that matches the date value given. Will try the desiredKey format before attempting others. The
   * first to match is returned.
   * 
   * @param dateString
   *          the literal value of the date (eg: "01/01/2001")
   * @param locale
   *          the locale in play
   * @param desiredKey
   *          the desired format (should be a valid key to DATE_FORMAT_TO_REGEXPS)
   * @return The key to the format that matched or null if none found.
   */
  public static String detectDateFormatBiased( String dateString, String locale, String desiredKey ) {
    if ( dateString == null ) {
      return null;
    }
    String regex = (String) getDateFormatToRegExps( locale ).get( desiredKey );
    if ( regex != null && dateString.toLowerCase().matches( regex ) ) {
      return desiredKey;
    } else {
      return detectDateFormat( dateString, locale );
    }
  }

  public static BidiMap getDateFormatToRegExps( String locale ) {
    if ( locale == null || LOCALE_en_US.equalsIgnoreCase( locale ) ) {
      return DATE_FORMAT_TO_REGEXPS_US;
    } else {
      return DATE_FORMAT_TO_REGEXPS;
    }
  }

  /**
   * 
   * @param dateString - string for check
   * @param dateFormat - format for check
   * @return true if we can parse string by format without exception
   */
  public static boolean isValidDate( String dateString, String dateFormat ) {
    try {
      getDateFromStringByFormat( dateString, dateFormat );
      return true;
    } catch ( ParseException e ) {
      return false;
    }
  }

  /**
   * @param dateString - string for check
   * @return true if we can parse string without exception
   */
  public static boolean isValidDate( String dateString ) {
    try {
      getDateFromString( dateString );
      return true;
    } catch ( ParseException e ) {
      return false;
    }
  }

  /**
   * 
   * @param dateFormat - format which we will try to apply for string
   * @param dateString - string which contains date
   * @return true if we found that we know dateFormat and it applied for given string
   */
  public static boolean isValidDateFormatToStringDate( String dateFormat, String dateString ) {
    String detectedDateFormat = detectDateFormat( dateString );
    if ( ( dateFormat != null ) && ( dateFormat.equals( detectedDateFormat ) ) ) {
      return true;
    }
    return false;
  }

  /**
   * 
   * @param dateFormat - format which we will try to apply for string
   * @param dateString - string which contains date
   * @param locale - locale for date format
   * @return true if we found that we know dateFormat and it applied for given string
   */
  public static boolean isValidDateFormatToStringDate( String dateFormat, String dateString, String locale ) {
    String detectedDateFormat =
        dateFormat != null ? detectDateFormatBiased( dateString, locale, dateFormat ) : detectDateFormat( dateString,
            locale );
    if ( ( dateFormat != null ) && ( dateFormat.equals( detectedDateFormat ) ) ) {
      return true;
    }
    return false;
  }
}
