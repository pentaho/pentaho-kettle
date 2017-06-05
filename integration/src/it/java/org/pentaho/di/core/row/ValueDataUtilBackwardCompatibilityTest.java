/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.row;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;

/**
 * Not yet completely finished.
 *
 * @author sboden
 *
 */
public class ValueDataUtilBackwardCompatibilityTest extends TestCase {
  private static String yyyy_MM_dd = "yyyy-MM-dd";

  private static final String SYS_PROPERTY_ROUND_2_MODE = "ROUND_2_MODE";
  private static final int OBSOLETE_ROUND_2_MODE = BigDecimal.ROUND_HALF_EVEN;
  private static final int DEFAULT_ROUND_2_MODE = Const.ROUND_HALF_CEILING;
  private static final int ORIGINAL_ROUND_2_MODE = getRound2Mode();

  @Override
  protected void setUp() throws Exception {
    assertEquals(DEFAULT_ROUND_2_MODE, getRound2Mode());
    setRound2Mode( OBSOLETE_ROUND_2_MODE );
    assertEquals(OBSOLETE_ROUND_2_MODE, getRound2Mode());
  }

  @Override
  protected void tearDown() throws Exception {
    setRound2Mode( ORIGINAL_ROUND_2_MODE );
    assertEquals(DEFAULT_ROUND_2_MODE, getRound2Mode());
  }

  /**
   * Get value of private static field ValueDataUtil.ROUND_2_MODE.
   * 
   * @return
   */
  private static int getRound2Mode() {
    int value = -1;
    try {
      Class<ValueDataUtil> cls = ValueDataUtil.class;
      Field f = cls.getDeclaredField( SYS_PROPERTY_ROUND_2_MODE );
      f.setAccessible( true );
      value = (Integer) f.get( null );
      f.setAccessible( false );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
    return value;
  }

  /**
   * Set new value of value of private static field ValueDataUtil.ROUND_2_MODE.
   * 
   * @param newValue
   */
  private static void setRound2Mode( int newValue ) {
    try {
      Class<ValueDataUtil> cls = ValueDataUtil.class;
      Field f = cls.getDeclaredField( SYS_PROPERTY_ROUND_2_MODE );
      f.setAccessible( true );
      f.set( null, newValue );
      f.setAccessible( false );
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  
  
  // private enum DateCalc {WORKING_DAYS, DATE_DIFF};

  /**
   * @deprecated Use {@link Const#ltrim(String)} instead
   * @throws KettleValueException
   */
  @Deprecated
  public void testLeftTrim() throws KettleValueException {
    assertEquals( "", ValueDataUtil.leftTrim( "" ) );
    assertEquals( "string", ValueDataUtil.leftTrim( "string" ) );
    assertEquals( "string", ValueDataUtil.leftTrim( " string" ) );
    assertEquals( "string", ValueDataUtil.leftTrim( "  string" ) );
    assertEquals( "string", ValueDataUtil.leftTrim( "   string" ) );
    assertEquals( "string", ValueDataUtil.leftTrim( "     string" ) );

    assertEquals( "string ", ValueDataUtil.leftTrim( " string " ) );
    assertEquals( "string  ", ValueDataUtil.leftTrim( "  string  " ) );
    assertEquals( "string   ", ValueDataUtil.leftTrim( "   string   " ) );
    assertEquals( "string    ", ValueDataUtil.leftTrim( "    string    " ) );

    assertEquals( "", ValueDataUtil.leftTrim( " " ) );
    assertEquals( "", ValueDataUtil.leftTrim( "  " ) );
    assertEquals( "", ValueDataUtil.leftTrim( "   " ) );
  }

  /**
   * @deprecated Use {@link Const#rtrim(String)} instead
   * @throws KettleValueException
   */
  @Deprecated
  public void testRightTrim() throws KettleValueException {
    assertEquals( "", ValueDataUtil.rightTrim( "" ) );
    assertEquals( "string", ValueDataUtil.rightTrim( "string" ) );
    assertEquals( "string", ValueDataUtil.rightTrim( "string " ) );
    assertEquals( "string", ValueDataUtil.rightTrim( "string  " ) );
    assertEquals( "string", ValueDataUtil.rightTrim( "string   " ) );
    assertEquals( "string", ValueDataUtil.rightTrim( "string    " ) );

    assertEquals( " string", ValueDataUtil.rightTrim( " string " ) );
    assertEquals( "  string", ValueDataUtil.rightTrim( "  string  " ) );
    assertEquals( "   string", ValueDataUtil.rightTrim( "   string   " ) );
    assertEquals( "    string", ValueDataUtil.rightTrim( "    string    " ) );

    assertEquals( "", ValueDataUtil.rightTrim( " " ) );
    assertEquals( "", ValueDataUtil.rightTrim( "  " ) );
    assertEquals( "", ValueDataUtil.rightTrim( "   " ) );
  }

  /**
   * @deprecated Use {@link Const#isSpace(char)} instead
   * @throws KettleValueException
   */
  @Deprecated
  public void testIsSpace() throws KettleValueException {
    assertTrue( ValueDataUtil.isSpace( ' ' ) );
    assertTrue( ValueDataUtil.isSpace( '\t' ) );
    assertTrue( ValueDataUtil.isSpace( '\r' ) );
    assertTrue( ValueDataUtil.isSpace( '\n' ) );

    assertFalse( ValueDataUtil.isSpace( 'S' ) );
    assertFalse( ValueDataUtil.isSpace( 'b' ) );
  }

  /**
   * @deprecated Use {@link Const#trim(String)} instead
   * @throws KettleValueException
   */
  @Deprecated
  public void testTrim() throws KettleValueException {
    assertEquals( "", ValueDataUtil.trim( "" ) );
    assertEquals( "string", ValueDataUtil.trim( "string" ) );
    assertEquals( "string", ValueDataUtil.trim( "string " ) );
    assertEquals( "string", ValueDataUtil.trim( "string  " ) );
    assertEquals( "string", ValueDataUtil.trim( "string   " ) );
    assertEquals( "string", ValueDataUtil.trim( "string    " ) );

    assertEquals( "string", ValueDataUtil.trim( " string " ) );
    assertEquals( "string", ValueDataUtil.trim( "  string  " ) );
    assertEquals( "string", ValueDataUtil.trim( "   string   " ) );
    assertEquals( "string", ValueDataUtil.trim( "    string    " ) );

    assertEquals( "string", ValueDataUtil.trim( " string" ) );
    assertEquals( "string", ValueDataUtil.trim( "  string" ) );
    assertEquals( "string", ValueDataUtil.trim( "   string" ) );
    assertEquals( "string", ValueDataUtil.trim( "    string" ) );

    assertEquals( "", ValueDataUtil.rightTrim( " " ) );
    assertEquals( "", ValueDataUtil.rightTrim( "  " ) );
    assertEquals( "", ValueDataUtil.rightTrim( "   " ) );
  }

  public void testDateDiff_A_GT_B() {
    Object daysDiff =
      calculate( "2010-05-12", "2010-01-01", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_DATE_DIFF );
    assertEquals( new Long( 131 ), daysDiff );
  }

  public void testDateDiff_A_LT_B() {
    Object daysDiff =
      calculate( "2010-12-31", "2011-02-10", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_DATE_DIFF );
    assertEquals( new Long( -41 ), daysDiff );
  }

  public void testWorkingDaysDays_A_GT_B() {
    Object daysDiff =
      calculate(
        "2010-05-12", "2010-01-01", ValueMetaInterface.TYPE_DATE,
        CalculatorMetaFunction.CALC_DATE_WORKING_DIFF );
    assertEquals( new Long( 93 ), daysDiff );
  }

  public void testWorkingDaysDays_A_LT_B() {
    Object daysDiff =
      calculate(
        "2010-12-31", "2011-02-10", ValueMetaInterface.TYPE_DATE,
        CalculatorMetaFunction.CALC_DATE_WORKING_DIFF );
    assertEquals( new Long( -29 ), daysDiff );
  }

  @Test
  public void testPlus() throws KettleValueException {
    
    long longValue = 1;
    
    assertEquals( longValue, ValueDataUtil.plus( new ValueMetaInteger(), longValue, new ValueMetaString(), StringUtils.EMPTY ) );
  
  }
  
  public void testAdd() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "3.0" ), calculate(
      "1", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "0.0" ), calculate(
      "2", "-2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "30.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "-50.0" ), calculate(
      "-100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "3" ), calculate(
      "1", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "0" ), calculate(
      "2", "-2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "30" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "-50" ), calculate(
      "-100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "2.0" ).compareTo( (BigDecimal) calculate(
      "1", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "0.0" ).compareTo( (BigDecimal) calculate(
      "2", "-2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "30.0" ).compareTo( (BigDecimal) calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "-50.0" ).compareTo( (BigDecimal) calculate(
      "-100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
  }

  public void testAdd3() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "6.0" ), calculate(
      "1", "2", "3", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "10.0" ), calculate(
      "2", "-2", "10", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "27.0" ), calculate(
      "10", "20", "-3", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "-55.0" ), calculate(
      "-100", "50", "-5", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_ADD3 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "3" ), calculate(
      "1", "1", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "10" ), calculate(
      "2", "-2", "10", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "27" ), calculate(
      "10", "20", "-3", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "-55" ), calculate(
      "-100", "50", "-5", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_ADD3 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "6.0" ).compareTo( (BigDecimal) calculate(
      "1", "2", "3", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "10.0" ).compareTo( (BigDecimal) calculate(
      "2", "-2", "10", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "27.0" ).compareTo( (BigDecimal) calculate(
      "10", "20", "-3", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "-55.0" ).compareTo( (BigDecimal) calculate(
      "-100", "50", "-5", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
  }

  public void testSubtract() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "10.0" ), calculate(
      "20", "10", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) );
    assertEquals( Double.valueOf( "-10.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "10" ), calculate(
      "20", "10", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_SUBTRACT ) );
    assertEquals( Long.valueOf( "-10" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_SUBTRACT ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "10" ).compareTo( (BigDecimal) calculate(
      "20", "10", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) ) );
    assertEquals( 0, new BigDecimal( "-10" ).compareTo( (BigDecimal) calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) ) );
  }

  public void testDivide() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "2.0" ), calculate(
      "2", "1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "2.0" ), calculate(
      "4", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "0.5" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "2.0" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "2" ), calculate(
      "2", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "4", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_DIVIDE ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate(
      "2", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate(
      "4", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "0.5" ) ), calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate(
      "100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
  }

  public void testPercent1() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "10.0" ), calculate(
      "10", "100", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "100.0" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "50.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "200.0" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "10" ), calculate(
      "10", "100", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "100" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "50" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "200" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_1 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Long.valueOf( "10" ) ), calculate(
      "10", "100", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "100" ) ), calculate(
      "2", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "50" ) ), calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "200" ) ), calculate(
      "100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
  }

  public void testPercent2() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "0.99" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "1.96" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "8.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "50.0" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "8" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "50" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_2 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Double.valueOf( "0.99" ) ), calculate(
      "1", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1.99" ) ), calculate(
      "2", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "9.995" ) ), calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "99.98" ) ), calculate(
      "100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
  }

  public void testPercent3() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.01" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "2.04" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "12.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "150.0" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "12" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "150" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_PERCENT_3 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1.01" ).compareTo( (BigDecimal) calculate(
      "1", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "2.01" ).compareTo( (BigDecimal) calculate(
      "2", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "10.005" ).compareTo( (BigDecimal) calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "100.02" ).compareTo( (BigDecimal) calculate(
      "100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
  }

  public void testCombination1() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "2.0" ), calculate(
      "1", "1", "1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "22.0" ), calculate(
      "2", "2", "10", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "70.0" ), calculate(
      "10", "20", "3", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "350" ), calculate(
      "100", "50", "5", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "2" ), calculate(
      "1", "1", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "22" ), calculate(
      "2", "2", "10", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "70" ), calculate(
      "10", "20", "3", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "350" ), calculate(
      "100", "50", "5", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_1 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "2.0" ).compareTo( (BigDecimal) calculate(
      "1", "1", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "22.0" ).compareTo( (BigDecimal) calculate(
      "2", "2", "10", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "70.0" ).compareTo( (BigDecimal) calculate(
      "10", "20", "3", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "350.0" ).compareTo( (BigDecimal) calculate(
      "100", "50", "5", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
  }

  public void testCombination2() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.4142135623730951" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "2.8284271247461903" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "22.360679774997898" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "111.80339887498948" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate(
      "1", "1", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "2", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "10" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "100" ), calculate(
      "100", "50", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_COMBINATION_2 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1.4142135623730951" ).compareTo( (BigDecimal) calculate(
      "1", "1", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "2.8284271247461903" ).compareTo( (BigDecimal) calculate(
      "2", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "22.360679774997898" ).compareTo( (BigDecimal) calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "111.80339887498948" ).compareTo( (BigDecimal) calculate(
      "100", "50", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
  }

  public void testRound() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.0" ), calculate( "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Double.valueOf( "103.0" ), calculate( "103.01", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Double.valueOf( "1235.0" ), calculate( "1234.6", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    // half
    assertEquals( Double.valueOf( "1235.0" ), calculate( "1234.5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Double.valueOf( "1236.0" ), calculate( "1235.5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Double.valueOf( "-1234.0" ), calculate( "-1234.5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Double.valueOf( "-1235.0" ), calculate( "-1235.5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Long.valueOf( "2" ), calculate( "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( Long.valueOf( "-103" ), calculate( "-103", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1.0" ) ), calculate( "1", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "103.0" ) ), calculate( "103.01",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1235.0" ) ), calculate( "1234.6",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    // half
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1235.0" ) ), calculate( "1234.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1236.0" ) ), calculate( "1235.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-1234.0" ) ), calculate( "-1234.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-1235.0" ) ), calculate( "-1235.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
  }

  public void testRound2() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.0" ), calculate( "1", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "2.1" ), calculate( "2.06", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "103.0" ), calculate( "103.01", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "12.35" ), calculate( "12.346", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    // scale < 0
    assertEquals( Double.valueOf( "10.0" ), calculate( "12.0", "-1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    // half
    assertEquals( Double.valueOf( "12.34" ), calculate( "12.345", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "12.36" ), calculate( "12.355", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "-12.34" ), calculate( "-12.345", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "-12.36" ), calculate( "-12.355", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "2" ), calculate( "2", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "103" ), calculate( "103", "3", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "12" ), calculate( "12", "4", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    // scale < 0
    //assertEquals( Long.valueOf( "100" ), calculate( "120", "-2", ValueMetaInterface.TYPE_INTEGER,
    //    CalculatorMetaFunction.CALC_ROUND_2 ) );
    // half
    //assertEquals( Long.valueOf( "12340" ), calculate( "12345", "-1", ValueMetaInterface.TYPE_INTEGER,
    //    CalculatorMetaFunction.CALC_ROUND_2 ) );
    //assertEquals( Long.valueOf( "12360" ), calculate( "12355", "-1", ValueMetaInterface.TYPE_INTEGER,
    //    CalculatorMetaFunction.CALC_ROUND_2 ) );
    //assertEquals( Long.valueOf( "-12340" ), calculate( "-12345", "-1", ValueMetaInterface.TYPE_INTEGER,
    //    CalculatorMetaFunction.CALC_ROUND_2 ) );
    //assertEquals( Long.valueOf( "-12360" ), calculate( "-12355", "-1", ValueMetaInterface.TYPE_INTEGER,
    //    CalculatorMetaFunction.CALC_ROUND_2 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1.0" ) ), calculate( "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "2.1" ) ), calculate( "2.06", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "103.0" ) ), calculate( "103.01", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "12.35" ) ), calculate( "12.346", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    // scale < 0
    assertEquals( BigDecimal.valueOf( Double.valueOf( "10.0" ) ), calculate( "12.0", "-1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    // half
    assertEquals( BigDecimal.valueOf( Double.valueOf( "12.34" ) ), calculate( "12.345", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "12.36" ) ), calculate( "12.355", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-12.34" ) ), calculate( "-12.345", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-12.36" ) ), calculate( "-12.355", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
  }

  public void testNVL() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.0" ), calculate(
      "1", "", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Double.valueOf( "2.0" ), calculate(
      "", "2", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Double.valueOf( "10.0" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle string types
    assertEquals( "1", calculate( "1", "", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( "2", calculate( "", "2", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( "10", calculate( "10", "20", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate(
      "1", "", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Long.valueOf( "2" ), calculate(
      "", "2", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Long.valueOf( "10" ), calculate(
      "10", "20", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1" ).compareTo( (BigDecimal) calculate(
      "1", "", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( 0, new BigDecimal( "2" ).compareTo( (BigDecimal) calculate(
      "", "2", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( 0, new BigDecimal( "10" ).compareTo( (BigDecimal) calculate(
      "10", "20", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) );

    // boolean
    assertEquals( true, calculate( "true", "", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( false, calculate( "", "false", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( false, calculate(
      "false", "true", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle date
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( yyyy_MM_dd );

    try {
      assertEquals( simpleDateFormat.parse( "2012-04-11" ), calculate(
        "2012-04-11", "", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );
      assertEquals( simpleDateFormat.parse( "2012-11-04" ), calculate(
        "", "2012-11-04", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );
      assertEquals( simpleDateFormat.parse( "1965-07-01" ), calculate(
        "1965-07-01", "1967-04-11", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );
      assertNull( calculate( "", "", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );

    } catch ( ParseException pe ) {
      fail( pe.getMessage() );
    }
    // assertEquals(0, calculate("", "2012-11-04", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL)));
    // assertEquals(0, calculate("2012-11-04", "2010-04-11", ValueMetaInterface.TYPE_DATE,
    // CalculatorMetaFunction.CALC_NVL)));
    // assertEquals(null, calculate("", "", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL));

    // binary
    ValueMeta stringValueMeta = new ValueMeta( "string", ValueMeta.TYPE_STRING );
    try {
      byte[] data = stringValueMeta.getBinary( "101" );
      byte[] calculated =
        (byte[]) calculate( "101", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      data = stringValueMeta.getBinary( "011" );
      calculated = (byte[]) calculate( "", "011", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      data = stringValueMeta.getBinary( "110" );
      calculated =
        (byte[]) calculate( "110", "011", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      calculated = (byte[]) calculate( "", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertNull( calculated );

      // assertEquals(binaryValueMeta.convertData(new ValueMeta("dummy", ValueMeta.TYPE_STRING), "101"),
      // calculate("101", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL));
    } catch ( KettleValueException kve ) {
      fail( kve.getMessage() );
    }
  }

  private Object calculate( String string_dataA, int valueMetaInterfaceType,
      int calculatorMetaFunction ) {
      return calculate( string_dataA, null, null, valueMetaInterfaceType, calculatorMetaFunction );
    }

  private Object calculate( String string_dataA, String string_dataB, int valueMetaInterfaceType,
      int calculatorMetaFunction ) {
      return calculate( string_dataA, string_dataB, null, valueMetaInterfaceType, calculatorMetaFunction );
    }

  private Object calculate( String string_dataA, String string_dataB, String string_dataC,
    int valueMetaInterfaceType, int calculatorMetaFunction ) {

    try {

      //
      ValueMeta parameterValueMeta = new ValueMeta( "parameter", ValueMeta.TYPE_STRING );

      // We create the meta information for
      ValueMeta valueMetaA = createValueMeta( "data_A", valueMetaInterfaceType );
      ValueMeta valueMetaB = createValueMeta( "data_B", valueMetaInterfaceType );
      ValueMeta valueMetaC = createValueMeta( "data_C", valueMetaInterfaceType );

      Object dataA = null;
      Object dataB = null;
      Object dataC = null;

      if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_NUMBER ) {
        dataA = ( !Utils.isEmpty( string_dataA ) ? Double.valueOf( string_dataA ) : null );
        dataB = ( !Utils.isEmpty( string_dataB ) ? Double.valueOf( string_dataB ) : null );
        dataC = ( !Utils.isEmpty( string_dataC ) ? Double.valueOf( string_dataC ) : null );
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_INTEGER ) {
        dataA = ( !Utils.isEmpty( string_dataA ) ? Long.valueOf( string_dataA ) : null );
        dataB = ( !Utils.isEmpty( string_dataB ) ? Long.valueOf( string_dataB ) : null );
        dataC = ( !Utils.isEmpty( string_dataC ) ? Long.valueOf( string_dataC ) : null );
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_DATE ) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( yyyy_MM_dd );
        try {
          dataA = ( !Utils.isEmpty( string_dataA ) ? simpleDateFormat.parse( string_dataA ) : null );
          dataB = ( !Utils.isEmpty( string_dataB ) ? simpleDateFormat.parse( string_dataB ) : null );
          dataC = ( !Utils.isEmpty( string_dataC ) ? simpleDateFormat.parse( string_dataC ) : null );
        } catch ( ParseException pe ) {
          fail( pe.getMessage() );
          return null;
        }
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BIGNUMBER ) {
        dataA = ( !Utils.isEmpty( string_dataA ) ? BigDecimal.valueOf( Double.valueOf( string_dataA ) ) : null );
        dataB = ( !Utils.isEmpty( string_dataB ) ? BigDecimal.valueOf( Double.valueOf( string_dataB ) ) : null );
        dataC = ( !Utils.isEmpty( string_dataC ) ? BigDecimal.valueOf( Double.valueOf( string_dataC ) ) : null );
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_STRING ) {
        dataA = ( !Utils.isEmpty( string_dataA ) ? string_dataA : null );
        dataB = ( !Utils.isEmpty( string_dataB ) ? string_dataB : null );
        dataC = ( !Utils.isEmpty( string_dataC ) ? string_dataC : null );
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BINARY ) {
        ValueMeta binaryValueMeta = new ValueMeta( "binary_data", ValueMeta.TYPE_BINARY );

        dataA =
          ( !Utils.isEmpty( string_dataA )
            ? binaryValueMeta.convertData( parameterValueMeta, string_dataA ) : null );
        dataB =
          ( !Utils.isEmpty( string_dataB )
            ? binaryValueMeta.convertData( parameterValueMeta, string_dataB ) : null );
        dataC =
          ( !Utils.isEmpty( string_dataC )
            ? binaryValueMeta.convertData( parameterValueMeta, string_dataC ) : null );
      } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BOOLEAN ) {
        if ( !Utils.isEmpty( string_dataA ) ) {
          dataA = ( string_dataA.equalsIgnoreCase( "true" ) ? true : false );
        } else {
          dataA = null;
        }
        if ( !Utils.isEmpty( string_dataB ) ) {
          dataB = ( string_dataB.equalsIgnoreCase( "true" ) ? true : false );
        } else {
          dataB = null;
        }
        if ( !Utils.isEmpty( string_dataC ) ) {
          dataC = ( string_dataC.equalsIgnoreCase( "true" ) ? true : false );
        } else {
          dataC = null;
        }
      } else {
        fail( "Invalid ValueMetaInterface type." );
        return null;
      }

      if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_ADD ) {
        return ValueDataUtil.plus( valueMetaA, dataA, valueMetaB, dataB );
      }
      if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_ADD3 ) {
        return ValueDataUtil.plus3( valueMetaA, dataA, valueMetaB, dataB, valueMetaC, dataC );
      }
      if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_SUBTRACT ) {
        return ValueDataUtil.minus( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_DIVIDE ) {
        return ValueDataUtil.divide( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_PERCENT_1 ) {
        return ValueDataUtil.percent1( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_PERCENT_2 ) {
        return ValueDataUtil.percent2( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_PERCENT_3 ) {
        return ValueDataUtil.percent3( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_COMBINATION_1 ) {
        return ValueDataUtil.combination1( valueMetaA, dataA, valueMetaB, dataB, valueMetaC, dataC );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_COMBINATION_2 ) {
        return ValueDataUtil.combination2( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_ROUND_1 ) {
        return ValueDataUtil.round( valueMetaA, dataA );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_ROUND_2 ) {
        return ValueDataUtil.round( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_NVL ) {
        return ValueDataUtil.nvl( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_DATE_DIFF ) {
        return ValueDataUtil.DateDiff( valueMetaA, dataA, valueMetaB, dataB, "" );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_DATE_WORKING_DIFF ) {
        return ValueDataUtil.DateWorkingDiff( valueMetaA, dataA, valueMetaB, dataB );
      } else {
        fail( "Invalid CalculatorMetaFunction specified." );
        return null;
      }
    } catch ( KettleValueException kve ) {
      fail( kve.getMessage() );
      return null;
    }
  }

  private ValueMeta createValueMeta( String name, int valueType ) {
    ValueMeta valueMeta = new ValueMeta( name, valueType );
    return valueMeta;
  }
  
  public static void assertEquals(Object expected, Object actual) {
    assertEquals("", expected, actual);
  }
  public static void assertEquals(String msg, Object expected, Object actual) {
    if (expected instanceof BigDecimal && actual instanceof BigDecimal) {
      if (((BigDecimal)expected).compareTo( (BigDecimal)actual ) != 0) {
        Assert.assertEquals( msg, expected, actual );
      }
    } else {
      Assert.assertEquals( msg, expected, actual );
    }
  }
}
