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

package org.pentaho.di.core.row.value.timestamp;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * User: Dzmitry Stsiapanau Date: 3/13/14 Time: 6:32 PM
 */
public class SimpleTimestampFormat extends SimpleDateFormat {

  private static final long serialVersionUID = -848077738238548608L;

  /**
   * Cached nanosecond positions in specified pattern.
   */
  private int startNanosecondPatternPosition;
  private int endNanosecondPatternPosition;

  /**
   * Flag noticed that specified pattern can be succesfully operated by parent <code>SimpleDateFormat</code>
   */
  private boolean compatibleToSuperPattern = true;

  /**
   * The pattern string of this formatter.  This is always a non-localized pattern.  May not be null.  See parent class
   * documentation for details.
   *
   * @serial
   */
  private String originalPattern;

  /**
   * Cached nanoseconds formatter.
   */
  private DecimalFormat nanoseconds;

  /**
   * Localized nanosecond letter.
   */
  private char patternNanosecond;

  /**
   * Letter which is used to specify in pattern nanoseconds component. Was extended from parent
   * <code>SimpleDateFormat</code> millisecond, so it is still could be internationalized.
   */
  private static final int PATTERN_MILLISECOND_POSITION = 8; // S

  /**
   * Internal <code>SimpleDateFormat</code> instances are used in formatting.
   */
  private static final String DEFAULT_TIMESTAMP_FORMAT_FOR_TIMESTAMP = "yyyy-MM-dd HH:mm:ss";
  private static final String DEFAULT_MILLISECOND_DATE_FORMAT = "SSS";

  private static final SimpleDateFormat defaultTimestampFormat =
    new SimpleDateFormat( DEFAULT_TIMESTAMP_FORMAT_FOR_TIMESTAMP, Locale.US );

  private static final SimpleDateFormat defaultMillisecondDateFormat =
    new SimpleDateFormat( DEFAULT_MILLISECOND_DATE_FORMAT, Locale.US );

  /**
   * Nanoseconds placeholder to specify unformatted nanoseconds position after formatting <code>Date</code> part part of
   * the <code>Timestamp</code>.
   */
  private static final String NANOSECOND_PLACEHOLDER = "NANO";
  private static final char FORMATTER_ESCAPE_CHARACTER = '\'';
  private static final String ESCAPED_NANOSECOND_PLACEHOLDER =
    FORMATTER_ESCAPE_CHARACTER + "NANO" + FORMATTER_ESCAPE_CHARACTER;

  /**
   * Default format of the <code>Timestamp</code> object for sql.
   */
  public static final String DEFAULT_TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";
  /**
    * Fields for advantages of using locale version from  JRE 1.7 and for JRE 1.6 compatibility
    */
  private static Method getDefaultLocaleMethod;
  private static Class<?> localeCategoryClass;
  private static Object formatCategory;
  private static boolean formatCategoryLocaleAvailable = true;

  static {
    try {
      localeCategoryClass = Class.forName( "java.util.Locale$Category" );
      final Class<?> localeClass = Class.forName( "java.util.Locale" );
      final Class<?>[] paramTypes = new Class<?>[] { localeCategoryClass };
      getDefaultLocaleMethod = localeClass.getMethod( "getDefault", paramTypes );
      final java.lang.reflect.Field formatField = localeCategoryClass.getField( "FORMAT" );
      //we pass null because the FORMAT is an enumeration constant(the same applies for class variables)
      formatCategory = formatField.get( null );
    } catch ( Exception e ) {
      formatCategoryLocaleAvailable = false;
    }
  }

  /**
   * Sets the date and time format symbols of this date format.
   *
   * @param newFormatSymbols the new date and time format symbols
   * @throws NullPointerException if the given newFormatSymbols is null
   * @see #getDateFormatSymbols
   */
  @Override
  public void setDateFormatSymbols( DateFormatSymbols newFormatSymbols ) {
    patternNanosecond = newFormatSymbols.getLocalPatternChars().charAt( PATTERN_MILLISECOND_POSITION );
    super.setDateFormatSymbols( newFormatSymbols );
  }

  private void init( String pattern, DateFormatSymbols formatSymbols, Boolean compiledPattern ) {
    originalPattern = pattern;
    String datePattern = pattern;

    super.setDateFormatSymbols( formatSymbols );
    patternNanosecond = formatSymbols.getLocalPatternChars().charAt( PATTERN_MILLISECOND_POSITION );
    StringBuilder sb = new StringBuilder();

    startNanosecondPatternPosition = datePattern.indexOf( patternNanosecond );
    endNanosecondPatternPosition = datePattern.lastIndexOf( patternNanosecond );
    initNanosecondsFormat();

    if ( startNanosecondPatternPosition != -1 ) {
      sb.append( datePattern.substring( 0, startNanosecondPatternPosition ) );
      sb.append( FORMATTER_ESCAPE_CHARACTER );
      sb.append( NANOSECOND_PLACEHOLDER );
      sb.append( FORMATTER_ESCAPE_CHARACTER );
      sb.append( datePattern.substring( endNanosecondPatternPosition + 1 ) );
      datePattern = sb.toString();
      sb.setLength( 0 );
    }

    String patternToApply;
    if ( startNanosecondPatternPosition == -1
      || endNanosecondPatternPosition - startNanosecondPatternPosition < 3 ) {

      compatibleToSuperPattern = true;
      patternToApply = originalPattern;

    } else {
      compatibleToSuperPattern = false;
      patternToApply = datePattern;

    }
    if ( compiledPattern ) {
      super.applyLocalizedPattern( patternToApply );
    } else {
      super.applyPattern( patternToApply );
    }
  }

  /**
   * Constructs a <code>SimpleTimestampFormat</code> using the given pattern and the default date format symbols for the
   * default locale. <b>Note:</b> This constructor may not support all locales. For full coverage, use the factory
   * methods in the {@link SimpleTimestampFormat} class.
   *
   * @param pattern the pattern describing the date and time format
   * @throws NullPointerException     if the given pattern is null
   * @throws IllegalArgumentException if the given pattern is invalid
   */
  public SimpleTimestampFormat( String pattern ) {
    this( pattern, getCompatibleLocale() );
  }

  private static Locale getCompatibleLocale() {
    Locale locale = null;
    if ( formatCategoryLocaleAvailable ) {
      try {
        locale = (Locale) getDefaultLocaleMethod.invoke( localeCategoryClass, formatCategory );
      } catch ( Exception ignored ) {
      //ignored
      }
    }

    //for jre 6
    if ( locale == null ) {
      locale = Locale.getDefault();
    }
    return locale;
  }

  /**
   * Constructs a <code>SimpleTimestampFormat</code> using the given pattern and the default date format symbols for the
   * given locale. <b>Note:</b> This constructor may not support all locales. For full coverage, use the factory methods
   * in the {@link SimpleTimestampFormat} class.
   *
   * @param pattern the pattern describing the date and time format
   * @param locale  the locale whose date format symbols should be used
   * @throws NullPointerException     if the given pattern or locale is null
   * @throws IllegalArgumentException if the given pattern is invalid
   */
  public SimpleTimestampFormat( String pattern, Locale locale ) {
    this( pattern, DateFormatSymbols.getInstance( locale ) );
  }

  /**
   * Constructs a <codeSimpleTimestampFormat</code> using the given pattern and date format symbols.
   *
   * @param pattern       the pattern describing the date and time format
   * @param formatSymbols the date format symbols to be used for formatting
   * @throws NullPointerException     if the given pattern or formatSymbols is null
   * @throws IllegalArgumentException if the given pattern is invalid
   */
  public SimpleTimestampFormat( String pattern, DateFormatSymbols formatSymbols ) {
    super( pattern, formatSymbols );
    init( pattern, formatSymbols, false );
  }

  /**
   * Formats the given <code>Date</code> or <code></>Timestamp</code> into a date/time string and appends the result to
   * the given <code>StringBuffer</code>.
   *
   * @param timestamp  the date-time value to be formatted into a date-time string.
   * @param toAppendTo where the new date-time text is to be appended.
   * @param pos        the formatting position. On input: an alignment field, if desired. On output: the offsets of the
   *                   alignment field.
   * @return the formatted date-time string.
   * @throws NullPointerException if the given {@code timestamp} is {@code null}.
   */
  @Override
  public StringBuffer format( Date timestamp, StringBuffer toAppendTo, FieldPosition pos ) {
    if ( compatibleToSuperPattern ) {
      return super.format( timestamp, toAppendTo, pos );
    }

    StringBuffer dateBuffer;
    String nan;

    if ( timestamp instanceof Timestamp ) {
      Timestamp tmp = (Timestamp) timestamp;
      Date date = new Date( tmp.getTime() );
      dateBuffer = super.format( date, toAppendTo, pos );
      nan = formatNanoseconds( tmp.getNanos() );

    } else {
      dateBuffer = super.format( timestamp, toAppendTo, pos );
      String milliseconds = defaultMillisecondDateFormat.format( timestamp );
      nan = formatNanoseconds( Integer.valueOf( milliseconds ) * Math.pow( 10, 6 ) );
    }

    int placeholderPosition = replaceHolder( dateBuffer, false );
    return dateBuffer.insert( pos.getBeginIndex() + placeholderPosition, nan );
  }

  private String formatNanoseconds( Double v ) {
    return formatNanoseconds( v.intValue() );
  }

  private String formatNanoseconds( Integer nanos ) {
    String nan = nanoseconds.format( nanos );
    return nan.substring( 0, endNanosecondPatternPosition - startNanosecondPatternPosition + 1 );
  }

  private void initNanosecondsFormat() {
    StringBuilder nanos = new StringBuilder();
    for ( int i = startNanosecondPatternPosition; i <= endNanosecondPatternPosition; i++ ) {
      nanos.append( '0' );
    }
    nanoseconds = new DecimalFormat( nanos.toString() );
  }

  private int replaceHolder( StringBuffer dateBuffer, Boolean inPattern ) {
    String placeHolder = inPattern ? ESCAPED_NANOSECOND_PLACEHOLDER : NANOSECOND_PLACEHOLDER;
    int placeholderPosition = dateBuffer.indexOf( placeHolder );
    if ( placeholderPosition == -1 ) {
      return 0;
    }
    dateBuffer.delete( placeholderPosition, placeholderPosition + placeHolder.length() );
    return placeholderPosition;
  }

  /**
   * See <code>SimpleDateFormat</code> description. This is dummy method to deprecate using parent implementation for
   * <code>Timestamp</code> until it is not fully implemented.
   */
  @Override
  public AttributedCharacterIterator formatToCharacterIterator( Object obj ) {
    if ( obj instanceof Timestamp ) {
      throw new IllegalArgumentException(
        "This functionality for Timestamp object has not been implemented yet" );
    }
    if ( compatibleToSuperPattern ) {
      return super.formatToCharacterIterator( obj );
    } else {
      throw new IllegalArgumentException(
        "This functionality for specified format pattern has not been implemented yet" );
    }
  }

  /**
   * Parses text from a string to produce a <code>Timestamp</code>.
   * <p/>
   * The method attempts to parse text starting at the index given by <code>pos</code>. If parsing succeeds, then the
   * index of <code>pos</code> is updated to the index after the last character used (parsing does not necessarily use
   * all characters up to the end of the string), and the parsed date is returned. The updated <code>pos</code> can be
   * used to indicate the starting point for the next call to this method. If an error occurs, then the index of
   * <code>pos</code> is not changed, the error index of <code>pos</code> is set to the index of the character where the
   * error occurred, and null is returned.
   * <p/>
   * <p>This parsing operation uses the {@link SimpleDateFormat#calendar calendar} to produce a {@code Date}. All of the
   * {@code calendar}'s date-time fields are {@linkplain java.util.Calendar#clear() cleared} before parsing, and the
   * {@code calendar}'s default values of the date-time fields are used for any missing date-time information. For
   * example, the year value of the parsed {@code Date} is 1970 with {@link java.util.GregorianCalendar} if no year
   * value is given from the parsing operation.  The {@code TimeZone} value may be overwritten, depending on the given
   * pattern and the time zone value in {@code text}. Any {@code TimeZone} value that has previously been set by a call
   * to {@link #setTimeZone(java.util.TimeZone) setTimeZone} may need to be restored for further operations.
   *
   * @param text A <code>String</code>, part of which should be parsed.
   * @param pos  A <code>ParsePosition</code> object with index and error index information as described above.
   * @return A <code>Date</code> parsed from the string. In case of error, returns null.
   * @throws NullPointerException if <code>text</code> or <code>pos</code> is null.
   */
  @Override
  public Date parse( String text, ParsePosition pos ) {
    String timestampFormatDate;
    Date tempDate;
    if ( compatibleToSuperPattern ) {
      tempDate = super.parse( text, pos );
      return new Timestamp( tempDate.getTime() );
    }

    StringBuilder dateText = new StringBuilder( text.substring( pos.getIndex() ) );
    ParsePosition positionError = new ParsePosition( 0 );
    tempDate = super.parse( dateText.toString(), positionError );
    if ( tempDate != null ) {
      pos.setErrorIndex( pos.getIndex() );
      return null;
    }

    int startNanosecondsPosition = positionError.getErrorIndex();
    int endNanosecondsPosition =
      endNanosecondPatternPosition - startNanosecondPatternPosition + 1 + startNanosecondsPosition;
    endNanosecondsPosition =
      ( endNanosecondsPosition >= dateText.length() ) ? dateText.length() : endNanosecondsPosition;
    String nanoseconds = String.valueOf( dateText.subSequence( startNanosecondsPosition, endNanosecondsPosition ) );
    dateText.delete( startNanosecondsPosition, endNanosecondsPosition );
    ParsePosition position = new ParsePosition( 0 );
    dateText.append( NANOSECOND_PLACEHOLDER );
    tempDate = super.parse( dateText.toString(), position );
    if ( tempDate == null ) {
      pos.setErrorIndex( position.getErrorIndex() );
      return null;
    }

    timestampFormatDate = defaultTimestampFormat.format( tempDate );
    String result = timestampFormatDate + '.' + nanoseconds;
    Timestamp res = Timestamp.valueOf( timestampFormatDate + '.' + nanoseconds );
    pos.setIndex( pos.getIndex() + result.length() );
    return res;
  }

  /**
   * Returns a pattern string describing this date format.
   *
   * @return a pattern string describing this date format.
   */
  @Override
  public String toPattern() {
    return originalPattern;
  }

  /**
   * Returns a localized pattern string describing this date format.
   *
   * @return a localized pattern string describing this date format.
   */
  @Override
  public String toLocalizedPattern() {
    if ( compatibleToSuperPattern ) {
      return super.toLocalizedPattern();
    } else {
      StringBuffer pattern =
        new StringBuffer( super.toLocalizedPattern() );
      int placeholderPosition = replaceHolder( pattern, true );
      for ( int i = placeholderPosition; i <= endNanosecondPatternPosition - startNanosecondPatternPosition + placeholderPosition; i++ ) {
        pattern.insert( i, patternNanosecond );
      }
      return pattern.toString();
    }

  }

  /**
   * Applies the given pattern string to this date format.
   *
   * @param pattern the new date and time pattern for this date format
   * @throws NullPointerException     if the given pattern is null
   * @throws IllegalArgumentException if the given pattern is invalid
   */
  @Override
  public void applyPattern( String pattern ) {
    DateFormatSymbols formatSymbols = super.getDateFormatSymbols();
    init( pattern, formatSymbols, false );
  }

  /**
   * Applies the given localized pattern string to this date format.
   *
   * @param pattern a String to be mapped to the new date and time format pattern for this format
   * @throws NullPointerException     if the given pattern is null
   * @throws IllegalArgumentException if the given pattern is invalid
   */
  @Override
  public void applyLocalizedPattern( String pattern ) {
    DateFormatSymbols formatSymbols = super.getDateFormatSymbols();
    init( pattern, formatSymbols, true );
  }

  /**
   * Parses text from the beginning of the given string to produce a date. The method may not use the entire text of the
   * given string.
   * <p/>
   * See the {@link #parse(String, java.text.ParsePosition)} method for more information on date parsing.
   *
   * @param source A <code>String</code> whose beginning should be parsed.
   * @return A <code>Date</code> parsed from the string.
   * @throws java.text.ParseException if the beginning of the specified string cannot be parsed.
   */
  @Override
  public Date parse( String source ) throws ParseException {
    return super.parse( source );
  }

  /**
   * Parses text from a string to produce a <code>Date</code>.
   * <p/>
   * The method attempts to parse text starting at the index given by <code>pos</code>. If parsing succeeds, then the
   * index of <code>pos</code> is updated to the index after the last character used (parsing does not necessarily use
   * all characters up to the end of the string), and the parsed date is returned. The updated <code>pos</code> can be
   * used to indicate the starting point for the next call to this method. If an error occurs, then the index of
   * <code>pos</code> is not changed, the error index of <code>pos</code> is set to the index of the character where the
   * error occurred, and null is returned.
   * <p/>
   * See the {@link #parse(String, java.text.ParsePosition)} method for more information on date parsing.
   *
   * @param source A <code>String</code>, part of which should be parsed.
   * @param pos    A <code>ParsePosition</code> object with index and error index information as described above.
   * @return A <code>Date</code> parsed from the string. In case of error, returns null.
   * @throws NullPointerException if <code>pos</code> is null.
   */
  @Override
  public Object parseObject( String source, ParsePosition pos ) {
    return parse( source, pos );
  }

}
