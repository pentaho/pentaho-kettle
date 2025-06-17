/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.core.util;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for StringEvaluator functionality.
 *
 * @author Alexander Buloichik
 */
public class StringEvaluatorTest {

  private StringEvaluator evaluator;
  private static Locale defaultLocale;

  @BeforeClass
  public static void setUpClass() {
    defaultLocale = Locale.getDefault();
  }

  @Before
  public void setUp() {
    evaluator = new StringEvaluator();
    Locale.setDefault(Locale.US);
  }

  @AfterClass
  public static void tearDown(){
    Locale.setDefault(defaultLocale);
  }

  /////////////////////////////////////
  // common
  ////////////////////////////////////
  @Test
  public void testEmpty() {
    evaluator.evaluateString( "" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );

    evaluator.evaluateString( "  " );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );
  }

  @Test
  public void testGetValues_WithoutDublicate() {
    List<String> strings = Arrays.asList( "name1", "name2" );
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertEquals( strings.size(), evaluator.getValues().size() );
    List<String> actualStrings = new ArrayList<String>( evaluator.getValues() );
    Collections.sort( strings );
    Collections.sort( actualStrings );

    Iterator<String> exIterator = strings.iterator();
    Iterator<String> iterator = actualStrings.iterator();
    while ( iterator.hasNext() ) {
      assertEquals( exIterator.next(), iterator.next() );
    }
  }

  @Test
  public void testGetValues_Duplicate() {
    String dublicatedString = "name1";
    List<String> strings = Arrays.asList( dublicatedString );
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    evaluator.evaluateString( dublicatedString );
    assertEquals( strings.size(), evaluator.getValues().size() );
    Iterator<String> exIterator = strings.iterator();
    Iterator<String> iterator = evaluator.getValues().iterator();
    while ( iterator.hasNext() ) {
      assertEquals( exIterator.next(), iterator.next() );
    }
  }

  @Test
  public void testGetCount() {
    List<String> strings = Arrays.asList( "02/29/2000", "03/29/2000" );
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertEquals( strings.size(), evaluator.getCount() );
  }

  @Test
  public void testGetAdvicedResult_NullInput() {
    String expectedNull = "";
    List<String> strings = Arrays.asList( expectedNull );
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    StringEvaluationResult result = evaluator.getAdvicedResult();
    assertEquals( 1, result.getNrNull() );
  }

  @Test
  public void testGetAdvicedResult_MinMaxInput() {
    String expectedMin = "500";
    String expectedMax = "1000";
    List<String> strings = Arrays.asList( expectedMax, expectedMin );
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    StringEvaluationResult result = evaluator.getAdvicedResult();
    assertEquals( Long.parseLong( expectedMax ), result.getMax() );
    assertEquals( Long.parseLong( expectedMin ), result.getMin() );
    assertEquals( expectedMax.length(), evaluator.getMaxLength() );
  }

  /////////////////////////////////////
  // mixed types
  ////////////////////////////////////
  @Test
  public void testIntegerWithNumber() {
    List<String> strings = Arrays.asList( "1", "1.1" );
    String mask = "#.#";
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isNumeric() );
    assertEquals( mask, evaluator.getAdvicedResult().getConversionMeta().getConversionMask() );
  }

  /////////////////////////////////////
  // number types
  ////////////////////////////////////
  @Test
  public void testNumberWithPoint() {
    testNumber( "#.#", "1.1" );
  }

  @Test
  public void testNumberWithDecimals() {
    testNumber( "#.##", "1.11" );
  }

  @Test
  public void testColumnOfVaryingScale() {
    testNumber( "#.###", "1.1", "1.999", "1.23" );
  }

  @Test
  public void testMixedNumberForms() {
    testNumber( "#,###.########", "1010.10101010", "10.01", "4,309.88" );
  }

  @Test
  public void testNegCurrency() {
    testNumber( "#,###.##", "-1,234,567.00", "-10.01", "-4,309.88" );
  }

  @Test
  public void testNegInteger() {
    testNumber( "#,###", "-1,234,567", "-10", "-4,309" );
  }

  @Test
  public void testParenedNegative() {
    testNumber( "#,###.000;(#,###.000)", "(1.2)", "2", "(1,222.333)" );
  }

  @Test
  public void testParenedNegativeInteger() {
    testNumber( "#,###;(#,###)", "(1)", "10,375", "(1,222)", "0", "2 " );
  }

  @Test
  public void testExponentialIntegerStuff() {
    testNumber( "###.####E0", "1.00E+00", "123E100", "1.2356E-100" );
  }

  @Test
  public void testExponentialInteger() {
    testNumber( "#", "123E16" );
  }

  @Test
  public void testHugeExponentialInteger() {
    testNumber( "###.#E0", "123E100" );
  }

  @Test
  public void testExponentialFraction() {
    testNumber("###.##E0", "-1.232E1" );
  }

  @Test
  public void testMixOfExponentsFractionsAndIntegers() {
    //The -456.230 forces 3 digit fractions even though the last digit was 0.  Why would have the 0 in the text
    // if we didn't want that placeholder?
    testNumber("###.###E0", "1.232E1", "-456.230", "123" );
  }

  @Test
  public void testMixOfExponentsFractionsIntegersAndGrouping() {
    testNumber("#,###.###", "1.232E1", "-456.230", "123", "1,111.1" );
  }

  @Test
  public void testMixOfExponentsFractionsIntegersAndGrouping2() {
    testNumber("#,###.###", "1.232E1", "-456.234", "123", "1,111.1" );
  }

  @Test
  public void testNumbersThatCouldBeEUorUS() {
    testNumber("#.######", "-81.706865", "-81.718452" );
  }

  @Test
  public void testMixOfExponentsFractionsIntegersAndGrouping3() {
    // Adding the (1234.1) to the values forces the parens mask to parse the value.  Since the parens mask is intended
    // for currency we use 0's in the fraction instead of #'s.  See the default masks.  Note that the comma group
    // separator do not force commas to exist in the number.  If it did then there would be no solution for (1234.1).
    testNumber("#,###.000;(#,###.000)", "1.232E1", "-456.234", "123", "1,111.1", "(1234.1)" );
  }

  @Test
  public void testEmptySet() {
    //There is a mask for parens with and without $ but there is no mask supporting both forms simultaneously
    testNumber(null, "(1234.1)", "($1234.1)" );
  }

  @Test
  public void testExponentNonPreference() {
    testNumber("#.###", "1.234" );
  }

  @Test
  public void testExponentPreference1() {
    testNumber("###.###E0", "1.234E0" );
  }

  @Test
  public void testExponentPreference2() {
    testNumber("###.###E0", "1234E-3" );
  }

  @Test
  public void testExponentPreference3() {
    testNumber("###.###E0", "1234E-33" );
  }

  @Test
  public void testPercent() {
    testNumber("#.##%", "123%" );
  }

  @Test
  public void testNumberWithGroupAndPoint() {
    testNumber( "#,###.#", "1,111,111.1" );
  }

  @Test
  public void testNumbers() {
    testNumber( "#,###.#", "1,111,111.1", "1,111" );
  }

  @Test
  public void testStringAsNumber() {
    evaluator.evaluateString( "1" );
    evaluator.evaluateString( "1.111111111" );
    evaluator.evaluateString( "1,111" );
    evaluator.evaluateString( "1,111.11111111" );
    evaluator.evaluateString( "1,111,111.1111" );
    evaluator.evaluateString( "1,111.111.1111" );
    evaluator.evaluateString( "1,111,111.111.111" );
    assertTrue( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isString() );
  }

  private void testNumber( String mask, String... strings ) {
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    if ( mask == null ){
      assertTrue( evaluator.getStringEvaluationResults().isEmpty() );
      assertTrue( evaluator.getAdvicedResult().getConversionMeta().getType() == 2 );
    } else {
      assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
      assertTrue( evaluator.getAdvicedResult().getConversionMeta().isNumeric() );
      assertEquals( mask, evaluator.getAdvicedResult().getConversionMeta().getConversionMask() );
    }
  }

  /////////////////////////////////////
  // integer types
  ////////////////////////////////////
  @Test
  public void testInteger() {
    testInteger( "#", "1" );
  }

  @Test
  public void testIntegerWithGroup() {
    testInteger( "#", "1111" );
  }

  private void testInteger( String mask, String... strings ) {
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isInteger() );
    assertTrue( mask.equals( evaluator.getAdvicedResult().getConversionMeta().getConversionMask() ) );
  }
  /////////////////////////////////////
  // currency types
  ////////////////////////////////////
  @Test
  public void testCurrency() {
    testCurrencyBasic( "+123", "-123", "(123)"  );
  }

  private void testCurrencyBasic( String... strings ) {
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isNumeric() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().getConversionMask().contains( "(" ) );
  }

  /////////////////////////////////////
  // boolean types
  ////////////////////////////////////
  @Test
  public void testBooleanY() {
    testBoolean( "Y" );
  }

  @Test
  public void testBooleanN() {
    testBoolean( "N" );
  }

  @Test
  public void testBooleanTrue() {
    testBoolean( "True" );
  }

  @Test
  public void testBooleanFalse() {
    testBoolean( "False" );
  }

  private void testBoolean( String... strings ) {
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isBoolean() );
  }

  /////////////////////////////////////
  // Date types, use default USA format
  ////////////////////////////////////
  @Test
  public void testDate() {
    testDefaultDateFormat( "MM/dd/yyyy", "10/10/2000" );
  }

  @Test
  public void testDateArray() {
    testDefaultDateFormat( "MM/dd/yyyy", "10/10/2000", "11/10/2000", "12/10/2000"  );
  }

  @Test
  public void testTimeStamp() {
    testDefaultDateFormat( "MM/dd/yyyy HH:mm:ss", "10/10/2000 00:00:00" );
  }

  @Test
  public void testTimeStampSeconds() {
    testDefaultDateFormat( "MM/dd/yyyy HH:mm:ss", "10/10/2000 00:00:00" );
  }

  private void testDefaultDateFormat( String maskEn, String... strings ) {
    for ( String string : strings ) {
      evaluator.evaluateString( string );
    }
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isDate() );
    String actualMask = evaluator.getAdvicedResult().getConversionMeta().getConversionMask();
    assertTrue( maskEn.equals( actualMask ) );
  }

  @Test
  public void testDate2YearDigits() {
    testDefaultDateFormat( "MM/dd/yy", "10/10/20", "11/10/20", "12/10/20" );
  }

  @Test
  public void testCustomDateFormat() {
    String sampleFormat = "MM/dd/yyyy HH:mm:ss";
    ArrayList<String> dateFormats = new ArrayList<String>();
    dateFormats.add( sampleFormat );
    StringEvaluator evaluator = new StringEvaluator( true, new ArrayList<String>(), dateFormats );
    evaluator.evaluateString( "02/29/2000 00:00:00"  );
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isDate() );
    assertTrue( sampleFormat.equals( evaluator.getAdvicedResult().getConversionMeta().getConversionMask() ) );
  }

  @Test
  public void testAdviceedOneDateFormat() {
    String sampleLongFormat = "MM/dd/yyyy HH:mm:ss";
    String sampleShortFormat = "MM/dd/yy HH:mm:ss";
    ArrayList<String> dateFormats = new ArrayList<String>();
    dateFormats.add( sampleLongFormat );
    dateFormats.add( sampleShortFormat );
    StringEvaluator evaluator = new StringEvaluator( true, new ArrayList<String>(), dateFormats );
    evaluator.evaluateString( "02/29/20 00:00:00"  );
    assertFalse( evaluator.getStringEvaluationResults().isEmpty() );
    assertTrue( evaluator.getAdvicedResult().getConversionMeta().isDate() );
    assertFalse( sampleLongFormat.equals( evaluator.getAdvicedResult().getConversionMeta().getConversionMask() ) );
    //should advice short format
    assertTrue( sampleShortFormat.equals( evaluator.getAdvicedResult().getConversionMeta().getConversionMask() ) );
  }

}
