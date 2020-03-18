/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.junit.rules.RestorePDIEnvironment;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * User: Dzmitry Stsiapanau Date: 3/17/14 Time: 4:46 PM
 */
public class SimpleTimestampFormatTest {
  @ClassRule public static RestorePDIEnvironment env = new RestorePDIEnvironment();

  private static Locale formatLocale;
  private Set<Locale> locales =
    new HashSet<>( Arrays.asList( Locale.US, Locale.GERMANY, Locale.JAPANESE, Locale.CHINESE ) );
  private ResourceBundle tdb;

  private static String stringNinePrecision = "2014-03-15 15:30:45.123456789";
  private static String stringFourPrecision = "2014-03-15 15:30:45.1234";
  private static String stringThreePrecision = "2014-03-15 15:30:45.123";
  private static String stringWithoutPrecision = "2014-03-15 15:30:45";
  private static String stringWithoutPrecisionWithDot = "2014-11-15 15:30:45.000";
  private Timestamp timestampNinePrecision = Timestamp.valueOf( stringNinePrecision );
  private Timestamp timestampFourPrecision = Timestamp.valueOf( stringFourPrecision );
  private Timestamp timestampThreePrecision = Timestamp.valueOf( stringThreePrecision );
  private Timestamp timestampWithoutPrecision = Timestamp.valueOf( stringWithoutPrecision );
  private Timestamp timestampWithoutPrecisionWithDot = Timestamp.valueOf( stringWithoutPrecisionWithDot );
  private Date dateThreePrecision = new Date( timestampThreePrecision.getTime() );
  private Date dateWithoutPrecision = new Date( timestampWithoutPrecision.getTime() );

  private AtomicBoolean testFailed = new AtomicBoolean( false );

  @Before
  public void setUp() throws Exception {
    formatLocale = Locale.getDefault( Locale.Category.FORMAT );
  }

  @After
  public void tearDown() throws Exception {
    Locale.setDefault( Locale.Category.FORMAT, formatLocale );
  }

  @Test
  public void testFormat() {
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );
      checkFormat( "KETTLE.LONG" );
      checkFormat( "LOCALE.DATE", new SimpleTimestampFormat( new SimpleDateFormat().toPattern() ) );
      //checkFormat( "LOCALE.TIMESTAMP", new SimpleTimestampFormat( new SimpleDateFormat().toPattern() ) );
      checkFormat( "KETTLE" );
      checkFormat( "DB.DEFAULT" );
      checkFormat( "LOCALE.DEFAULT" );
    }
  }

  private void checkFormat( String patternName ) {
    SimpleTimestampFormat stf = new SimpleTimestampFormat( tdb.getString( "PATTERN." + patternName ) );
    checkFormat( patternName, stf );
  }

  private void checkFormat( String patternName, SimpleTimestampFormat stf ) {
    String localeForErrorMSG = Locale.getDefault( Locale.Category.FORMAT ).toLanguageTag();
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "TIMESTAMP.NINE." + patternName ),
      ( stf.format( timestampNinePrecision ) ) );
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "TIMESTAMP.THREE." + patternName ),
      ( stf.format( timestampThreePrecision ) ) );
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "TIMESTAMP.ZERO." + patternName ),
      ( stf.format( timestampWithoutPrecision ) ) );
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "TIMESTAMP.DOT." + patternName ), ( stf.format( timestampWithoutPrecisionWithDot ) ) );
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "DATE.THREE." + patternName ),
      ( stf.format( dateThreePrecision ) ) );
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      tdb.getString( "DATE.ZERO." + patternName ),
      ( stf.format( dateWithoutPrecision ) ) );
  }

  @Test
  public void testParse() throws Exception {
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );

      checkParseKettle();
      checkParseKettleLong();
      checkParseDbDefault();
      checkParseLocaleDefault();
      // Uncomment in case of locale timestamp format is defined for the most locales as in SimpleDateFormat()
      //checkParseLocalTimestamp();
    }
  }

  private void checkParseKettle() throws ParseException {
    String patternName = "KETTLE";
    SimpleTimestampFormat stf = new SimpleTimestampFormat( tdb.getString( "PATTERN." + patternName ) );
    String localeForErrorMSG = Locale.getDefault( Locale.Category.FORMAT ).toLanguageTag();
    parseUnit( "TIMESTAMP.NINE." + patternName, stf, localeForErrorMSG,
      timestampThreePrecision ); //ThreePrecision only for Kettle
    parseUnit( "TIMESTAMP.THREE." + patternName, stf, localeForErrorMSG, timestampThreePrecision );
    parseUnit( "TIMESTAMP.ZERO." + patternName, stf, localeForErrorMSG, timestampWithoutPrecision );
    parseUnit( "TIMESTAMP.DOT." + patternName, stf, localeForErrorMSG, timestampWithoutPrecisionWithDot );
    parseUnit( "DATE.THREE." + patternName, stf, localeForErrorMSG, dateThreePrecision );
    parseUnit( "DATE.ZERO." + patternName, stf, localeForErrorMSG, dateWithoutPrecision );
  }

  private void checkParseKettleLong() throws ParseException {
    String patternName = "KETTLE.LONG";
    SimpleTimestampFormat stf = new SimpleTimestampFormat( tdb.getString( "PATTERN." + patternName ) );
    String localeForErrorMSG = Locale.getDefault( Locale.Category.FORMAT ).toLanguageTag();
    parseUnit( "TIMESTAMP.NINE." + patternName, stf, localeForErrorMSG,
      timestampFourPrecision ); //FourPrecision only for Kettle long
    parseUnit( "TIMESTAMP.THREE." + patternName, stf, localeForErrorMSG, timestampThreePrecision );
    parseUnit( "TIMESTAMP.ZERO." + patternName, stf, localeForErrorMSG, timestampWithoutPrecision );
    parseUnit( "TIMESTAMP.DOT." + patternName, stf, localeForErrorMSG, timestampWithoutPrecisionWithDot );
    parseUnit( "DATE.THREE." + patternName, stf, localeForErrorMSG, dateThreePrecision );
    parseUnit( "DATE.ZERO." + patternName, stf, localeForErrorMSG, dateWithoutPrecision );
  }

  private void checkParseDbDefault() throws ParseException {
    String patternName = "DB.DEFAULT";
    SimpleTimestampFormat stf = new SimpleTimestampFormat( tdb.getString( "PATTERN." + patternName ) );
    String localeForErrorMSG = Locale.getDefault( Locale.Category.FORMAT ).toLanguageTag();
    parseUnit( "TIMESTAMP.NINE." + patternName, stf, localeForErrorMSG, timestampNinePrecision );
    parseUnit( "TIMESTAMP.THREE." + patternName, stf, localeForErrorMSG, timestampThreePrecision );
    parseUnit( "TIMESTAMP.ZERO." + patternName, stf, localeForErrorMSG, timestampWithoutPrecision );
    parseUnit( "TIMESTAMP.DOT." + patternName, stf, localeForErrorMSG, timestampWithoutPrecisionWithDot );
    parseUnit( "DATE.THREE." + patternName, stf, localeForErrorMSG, dateThreePrecision );
    parseUnit( "DATE.ZERO." + patternName, stf, localeForErrorMSG, dateWithoutPrecision );
  }

  private void checkParseLocaleDefault() throws ParseException {
    String patternName = "LOCALE.DEFAULT";
    SimpleTimestampFormat stf = new SimpleTimestampFormat( tdb.getString( "PATTERN." + patternName ) );
    String localeForErrorMSG = Locale.getDefault( Locale.Category.FORMAT ).toLanguageTag();
    parseUnit( "TIMESTAMP.NINE." + patternName, stf, localeForErrorMSG, timestampNinePrecision );
    parseUnit( "TIMESTAMP.THREE." + patternName, stf, localeForErrorMSG, timestampThreePrecision );
    parseUnit( "TIMESTAMP.ZERO." + patternName, stf, localeForErrorMSG, timestampWithoutPrecision );
    parseUnit( "TIMESTAMP.DOT." + patternName, stf, localeForErrorMSG, timestampWithoutPrecisionWithDot );
    parseUnit( "DATE.THREE." + patternName, stf, localeForErrorMSG, dateThreePrecision );
    parseUnit( "DATE.ZERO." + patternName, stf, localeForErrorMSG, dateWithoutPrecision );
  }

  private void parseUnit( String patternName, SimpleTimestampFormat stf, String localeForErrorMSG, Date date )
    throws ParseException {
    assertEquals( localeForErrorMSG + "=locale localized pattern= " + stf.toLocalizedPattern(),
      date, ( stf.parse( tdb.getString( patternName ) ) ) );
  }

  @Test
  public void testToPattern() {
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );
      String patternExample = tdb.getString( "PATTERN.KETTLE" );
      SimpleTimestampFormat stf = new SimpleTimestampFormat( new SimpleDateFormat().toPattern() );
      assertEquals( locale.toLanguageTag(), tdb.getString( "PATTERN.LOCALE.DATE" ), stf.toPattern() );
      stf = new SimpleTimestampFormat( patternExample, Locale.GERMANY );
      assertEquals( locale.toLanguageTag(), patternExample, stf.toPattern() );
      stf = new SimpleTimestampFormat( patternExample, Locale.US );
      assertEquals( locale.toLanguageTag(), patternExample, stf.toPattern() );
    }
  }

  @Test
  public void testToLocalizedPattern() {
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );
      SimpleTimestampFormat stf = new SimpleTimestampFormat( new SimpleDateFormat().toPattern() );
      assertEquals( locale.toLanguageTag(), tdb.getString( "PATTERN.LOCALE.COMPILED" ), stf.toLocalizedPattern() );
      String patternExample = tdb.getString( "PATTERN.KETTLE" );
      stf = new SimpleTimestampFormat( patternExample );
      assertEquals( locale.toLanguageTag(), tdb.getString( "PATTERN.LOCALE.COMPILED_DATE" ), stf.toLocalizedPattern() );
    }
  }

  @Test
  public void testApplyPattern() {
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );
      String patternExample = tdb.getString( "PATTERN.KETTLE" );
      SimpleTimestampFormat stf = new SimpleTimestampFormat( new SimpleDateFormat().toPattern() );
      assertEquals( locale.toLanguageTag(), tdb.getString( "PATTERN.LOCALE.DATE" ), stf.toPattern() );
      stf.applyPattern( patternExample );
      checkFormat( "KETTLE", stf );
    }
  }

  @Test
  public void testApplyLocalizedPattern() {
    Locale.setDefault( Locale.Category.FORMAT, Locale.US );
    SimpleTimestampFormat stf = new SimpleTimestampFormat( new SimpleDateFormat().toPattern() );
    for ( Locale locale : locales ) {
      Locale.setDefault( Locale.Category.FORMAT, locale );
      tdb = ResourceBundle.getBundle( "org/pentaho/di/core/row/value/timestamp/messages/testdates", locale );
      stf.applyLocalizedPattern( tdb.getString( "PATTERN.LOCALE.DEFAULT" ) );
      assertEquals( locale.toLanguageTag(), tdb.getString( "PATTERN.LOCALE.DEFAULT" ), stf.toLocalizedPattern() );
      checkFormat( "LOCALE.DEFAULT", stf );
    }
  }

  @Test
  public void testParseMultiThread() throws InterruptedException {
    int threadPoolSize = 10;
    Locale.setDefault( Locale.Category.FORMAT, Locale.US );

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool( threadPoolSize );

    for ( int i = 0; i < 500; i++ ) {
      TaskSimpleTimestampFormatTest task =
        new TaskSimpleTimestampFormatTest( "TaskSimpleTimestampFormatTest " + i );
      executor.execute( task );
    }
    executor.shutdown();

    if ( !executor.awaitTermination( 5000, TimeUnit.MILLISECONDS ) ) {
      Assert.fail( "Failed by timeout" );
    }
    assertFalse( "Test failed in task thread.", testFailed.get() );
  }

  class TaskSimpleTimestampFormatTest implements Runnable {
    private String name;
    private final SimpleTimestampFormat simpleTimestampFormat;

    public TaskSimpleTimestampFormatTest( String name ) {
      this.name = name;
      //this.simpleTimestampFormat = simpleTimestampFormat;
      this.simpleTimestampFormat = new SimpleTimestampFormat( "yyyy-MM-dd HH:mm:ss.SSS" );
    }

    public String getName() {
      return name;
    }

    public void run() {
      try {
        String input = "2019-07-08 10:16:01.001";
        synchronized ( simpleTimestampFormat ) {
          Date result = simpleTimestampFormat.parse( input );
          System.out.println( "Task: " + this.name + ", Result: " + result.toString() );
          if ( !input.equals( result.toString() ) ) {
            testFailed.set( true );
          }
        }
      } catch ( ParseException e ) {
        testFailed.set( true );
      }
    }
  }
}


