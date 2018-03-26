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

package org.pentaho.di.core.row;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.value.ValueMetaBinary;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;

public class ValueDataUtilTest {
  private static String yyyy_MM_dd = "yyyy-MM-dd";

  @BeforeClass
  public static void setUpBeforeClass() throws KettleException {
    KettleEnvironment.init( false );
  }

  // private enum DateCalc {WORKING_DAYS, DATE_DIFF};

  /**
   * @deprecated Use {@link Const#ltrim(String)} instead
   * @throws KettleValueException
   */
  @Deprecated
  @Test
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
  @Test
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
  @Test
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
  @Test
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

  @Test
  public void testDateDiff_A_GT_B() {
    Object daysDiff =
        calculate( "2010-05-12", "2010-01-01", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_DATE_DIFF );
    assertEquals( new Long( 131 ), daysDiff );
  }

  @Test
  public void testDateDiff_A_LT_B() {
    Object daysDiff =
        calculate( "2010-12-31", "2011-02-10", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_DATE_DIFF );
    assertEquals( new Long( -41 ), daysDiff );
  }

  @Test
  public void testWorkingDaysDays_A_GT_B() {
    Object daysDiff =
        calculate( "2010-05-12", "2010-01-01", ValueMetaInterface.TYPE_DATE,
            CalculatorMetaFunction.CALC_DATE_WORKING_DIFF );
    assertEquals( new Long( 94 ), daysDiff );
  }

  @Test
  public void testWorkingDaysDays_A_LT_B() {
    Object daysDiff =
        calculate( "2010-12-31", "2011-02-10", ValueMetaInterface.TYPE_DATE,
            CalculatorMetaFunction.CALC_DATE_WORKING_DIFF );
    assertEquals( new Long( -30 ), daysDiff );
  }

  @Test
  public void testPlus() throws KettleValueException {

    long longValue = 1;

    assertEquals( longValue, ValueDataUtil.plus( new ValueMetaInteger(), longValue, new ValueMetaString(),
        StringUtils.EMPTY ) );

  }

  @Test
  public void testAdd() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "3.0" ), calculate( "1", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "0.0" ), calculate( "2", "-2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "30.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Double.valueOf( "-50.0" ), calculate( "-100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "3" ), calculate( "1", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "0" ), calculate( "2", "-2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "30" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD ) );
    assertEquals( Long.valueOf( "-50" ), calculate( "-100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "2.0" ).compareTo( (BigDecimal) calculate( "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "0.0" ).compareTo( (BigDecimal) calculate( "2", "-2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "30.0" ).compareTo( (BigDecimal) calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
    assertEquals( 0, new BigDecimal( "-50.0" ).compareTo( (BigDecimal) calculate( "-100", "50",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD ) ) );
  }

  @Test
  public void testAdd3() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "6.0" ), calculate( "1", "2", "3", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "10.0" ), calculate( "2", "-2", "10", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "27.0" ), calculate( "10", "20", "-3", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Double.valueOf( "-55.0" ), calculate( "-100", "50", "-5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ADD3 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "3" ), calculate( "1", "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "10" ), calculate( "2", "-2", "10", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "27" ), calculate( "10", "20", "-3", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD3 ) );
    assertEquals( Long.valueOf( "-55" ), calculate( "-100", "50", "-5", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ADD3 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "6.0" ).compareTo( (BigDecimal) calculate( "1", "2", "3",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "10.0" ).compareTo( (BigDecimal) calculate( "2", "-2", "10",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "27.0" ).compareTo( (BigDecimal) calculate( "10", "20", "-3",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
    assertEquals( 0, new BigDecimal( "-55.0" ).compareTo( (BigDecimal) calculate( "-100", "50", "-5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ADD3 ) ) );
  }

  @Test
  public void testSubtract() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "10.0" ), calculate( "20", "10", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_SUBTRACT ) );
    assertEquals( Double.valueOf( "-10.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_SUBTRACT ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "10" ), calculate( "20", "10", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_SUBTRACT ) );
    assertEquals( Long.valueOf( "-10" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_SUBTRACT ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "10" ).compareTo( (BigDecimal) calculate( "20", "10",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) ) );
    assertEquals( 0, new BigDecimal( "-10" ).compareTo( (BigDecimal) calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_SUBTRACT ) ) );
  }

  @Test
  public void testDivide() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "2.0" ), calculate( "2", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "2.0" ), calculate( "4", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "0.5" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Double.valueOf( "2.0" ), calculate( "100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "2" ), calculate( "2", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "2" ), calculate( "4", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "0" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( Long.valueOf( "2" ), calculate( "100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_DIVIDE ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate( "2", "1", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate( "4", "2", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "0.5" ) ), calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_DIVIDE ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "2" ) ), calculate( "100", "50", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_DIVIDE ) );
  }

  @Test
  public void testPercent1() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "10.0" ), calculate( "10", "100", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "100.0" ), calculate( "2", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "50.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Double.valueOf( "200.0" ), calculate( "100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "10" ), calculate( "10", "100", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "100" ), calculate( "2", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "50" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( Long.valueOf( "200" ), calculate( "100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Long.valueOf( "10" ) ), calculate( "10", "100",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "100" ) ), calculate( "2", "2", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "50" ) ), calculate( "10", "20", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_PERCENT_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "200" ) ), calculate( "100", "50",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_1 ) );
  }

  @Test
  public void testPercent2() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "0.99" ), calculate( "1", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "1.96" ), calculate( "2", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "8.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Double.valueOf( "50.0" ), calculate( "100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "2" ), calculate( "2", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "8" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( Long.valueOf( "50" ), calculate( "100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_2 ) );

    // Test Kettle big Number types
    assertEquals( BigDecimal.valueOf( Double.valueOf( "0.99" ) ), calculate( "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "1.96" ) ), calculate( "2", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( new BigDecimal("8.00"), calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
    assertEquals( new BigDecimal("50.00"), calculate( "100", "50",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_2 ) );
  }

  @Test
  public void testPercent3() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.01" ), calculate( "1", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "2.04" ), calculate( "2", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "12.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Double.valueOf( "150.0" ), calculate( "100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "2" ), calculate( "2", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "12" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );
    assertEquals( Long.valueOf( "150" ), calculate( "100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_PERCENT_3 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1.01" ).compareTo( (BigDecimal) calculate( "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "2.04" ).compareTo( (BigDecimal) calculate( "2", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "12" ).compareTo( (BigDecimal) calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
    assertEquals( 0, new BigDecimal( "150" ).compareTo( (BigDecimal) calculate( "100", "50",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_PERCENT_3 ) ) );
  }

  @Test
  public void testCombination1() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "2.0" ), calculate( "1", "1", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "22.0" ), calculate( "2", "2", "10", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "70.0" ), calculate( "10", "20", "3", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Double.valueOf( "350" ), calculate( "100", "50", "5", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "2" ), calculate( "1", "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "22" ), calculate( "2", "2", "10", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "70" ), calculate( "10", "20", "3", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );
    assertEquals( Long.valueOf( "350" ), calculate( "100", "50", "5", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_1 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "2.0" ).compareTo( (BigDecimal) calculate( "1", "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "22.0" ).compareTo( (BigDecimal) calculate( "2", "2", "10",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "70.0" ).compareTo( (BigDecimal) calculate( "10", "20", "3",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
    assertEquals( 0, new BigDecimal( "350.0" ).compareTo( (BigDecimal) calculate( "100", "50", "5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_1 ) ) );
  }

  @Test
  public void testCombination2() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.4142135623730951" ), calculate( "1", "1", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "2.8284271247461903" ), calculate( "2", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "22.360679774997898" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Double.valueOf( "111.80339887498948" ), calculate( "100", "50", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", "1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "2" ), calculate( "2", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "10" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );
    assertEquals( Long.valueOf( "100" ), calculate( "100", "50", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_COMBINATION_2 ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1.4142135623730951" ).compareTo( (BigDecimal) calculate( "1", "1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "2.8284271247461903" ).compareTo( (BigDecimal) calculate( "2", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "22.360679774997898" ).compareTo( (BigDecimal) calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
    assertEquals( 0, new BigDecimal( "111.80339887498948" ).compareTo( (BigDecimal) calculate( "100", "50",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_COMBINATION_2 ) ) );
  }

  @Test
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
    assertEquals( BigDecimal.ONE, calculate( "1", ValueMetaInterface.TYPE_BIGNUMBER,
        CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "103" ) ), calculate( "103.01",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "1235" ) ), calculate( "1234.6",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    // half
    assertEquals( BigDecimal.valueOf( Long.valueOf( "1235" ) ), calculate( "1234.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "1236" ) ), calculate( "1235.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "-1234" ) ), calculate( "-1234.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
    assertEquals( BigDecimal.valueOf( Long.valueOf( "-1235" ) ), calculate( "-1235.5",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_1 ) );
  }

  @Test
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
    assertEquals( Double.valueOf( "12.35" ), calculate( "12.345", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "12.36" ), calculate( "12.355", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "-12.34" ), calculate( "-12.345", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Double.valueOf( "-12.35" ), calculate( "-12.355", "2", ValueMetaInterface.TYPE_NUMBER,
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
    assertEquals( Long.valueOf( "100" ), calculate( "120", "-2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    // half
    assertEquals( Long.valueOf( "12350" ), calculate( "12345", "-1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "12360" ), calculate( "12355", "-1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "-12340" ), calculate( "-12345", "-1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( Long.valueOf( "-12350" ), calculate( "-12355", "-1", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_ROUND_2 ) );

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
    assertEquals( BigDecimal.valueOf( Double.valueOf( "10.0" ) ).setScale( -1 ), calculate( "12.0", "-1",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    // half
    assertEquals( BigDecimal.valueOf( Double.valueOf( "12.35" ) ), calculate( "12.345", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "12.36" ) ), calculate( "12.355", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-12.34" ) ), calculate( "-12.345", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
    assertEquals( BigDecimal.valueOf( Double.valueOf( "-12.35" ) ), calculate( "-12.355", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_ROUND_2 ) );
  }

  @Test
  public void testNVL() {

    // Test Kettle number types
    assertEquals( Double.valueOf( "1.0" ), calculate( "1", "", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Double.valueOf( "2.0" ), calculate( "", "2", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Double.valueOf( "10.0" ), calculate( "10", "20", ValueMetaInterface.TYPE_NUMBER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle string types
    assertEquals( "1", calculate( "1", "", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( "2", calculate( "", "2", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( "10", calculate( "10", "20", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle Integer (Java Long) types
    assertEquals( Long.valueOf( "1" ), calculate( "1", "", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Long.valueOf( "2" ), calculate( "", "2", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( Long.valueOf( "10" ), calculate( "10", "20", ValueMetaInterface.TYPE_INTEGER,
        CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle big Number types
    assertEquals( 0, new BigDecimal( "1" ).compareTo( (BigDecimal) calculate( "1", "",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( 0, new BigDecimal( "2" ).compareTo( (BigDecimal) calculate( "", "2",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( 0, new BigDecimal( "10" ).compareTo( (BigDecimal) calculate( "10", "20",
        ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_NVL ) );

    // boolean
    assertEquals( true, calculate( "true", "", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( false, calculate( "", "false", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( false, calculate( "false", "true", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );
    assertEquals( null, calculate( "", "", ValueMetaInterface.TYPE_BOOLEAN, CalculatorMetaFunction.CALC_NVL ) );

    // Test Kettle date
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat( yyyy_MM_dd );

    try {
      assertEquals( simpleDateFormat.parse( "2012-04-11" ), calculate( "2012-04-11", "", ValueMetaInterface.TYPE_DATE,
          CalculatorMetaFunction.CALC_NVL ) );
      assertEquals( simpleDateFormat.parse( "2012-11-04" ), calculate( "", "2012-11-04", ValueMetaInterface.TYPE_DATE,
          CalculatorMetaFunction.CALC_NVL ) );
      assertEquals( simpleDateFormat.parse( "1965-07-01" ), calculate( "1965-07-01", "1967-04-11",
          ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );
      assertNull( calculate( "", "", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL ) );

    } catch ( ParseException pe ) {
      fail( pe.getMessage() );
    }
    // assertEquals(0, calculate("", "2012-11-04", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL)));
    // assertEquals(0, calculate("2012-11-04", "2010-04-11", ValueMetaInterface.TYPE_DATE,
    // CalculatorMetaFunction.CALC_NVL)));
    // assertEquals(null, calculate("", "", ValueMetaInterface.TYPE_DATE, CalculatorMetaFunction.CALC_NVL));

    // binary
    ValueMetaInterface stringValueMeta = new ValueMetaString( "string" );
    try {
      byte[] data = stringValueMeta.getBinary( "101" );
      byte[] calculated =
          (byte[]) calculate( "101", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      data = stringValueMeta.getBinary( "011" );
      calculated = (byte[]) calculate( "", "011", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      data = stringValueMeta.getBinary( "110" );
      calculated = (byte[]) calculate( "110", "011", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertTrue( Arrays.equals( data, calculated ) );

      calculated = (byte[]) calculate( "", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL );
      assertNull( calculated );

      // assertEquals(binaryValueMeta.convertData(new ValueMeta("dummy", ValueMeta.TYPE_STRING), "101"),
      // calculate("101", "", ValueMetaInterface.TYPE_BINARY, CalculatorMetaFunction.CALC_NVL));
    } catch ( KettleValueException kve ) {
      fail( kve.getMessage() );
    }
  }

  @Test
  public void testRemainder() throws Exception {
    assertNull( calculate( null, null, ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( null, "3", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( "10", null, ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Long( "1" ),
      calculate( "10", "3", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Long( "-1" ),
      calculate( "-10", "3", ValueMetaInterface.TYPE_INTEGER, CalculatorMetaFunction.CALC_REMAINDER ) );

    Double comparisonDelta = new Double( "0.0000000000001" );
    assertNull( calculate( null, null, ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( null, "4.1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( "17.8", null, ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Double( "1.4" ).doubleValue(),
      ( (Double) calculate( "17.8", "4.1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER )
      ).doubleValue(),
      comparisonDelta.doubleValue() );
    assertEquals( new Double( "1.4" ).doubleValue(),
      ( (Double) calculate( "17.8", "-4.1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER )
      ).doubleValue(),
      comparisonDelta.doubleValue() );

    assertEquals( new Double( "-1.4" ).doubleValue(),
      ( (Double) calculate( "-17.8", "-4.1", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER )
      ).doubleValue(),
      comparisonDelta.doubleValue() );

    assertNull( calculate( null, null, ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( null, "16.12", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertNull( calculate( "-144.144", null, ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );

    assertEquals( new BigDecimal( "-15.184" ),
      calculate( "-144.144", "16.12", ValueMetaInterface.TYPE_BIGNUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Double( "2.6000000000000005" ).doubleValue(),
      calculate( "12.5", "3.3", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Double( "4.0" ).doubleValue(),
      calculate( "12.5", "4.25", ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
    assertEquals( new Long( "1" ).longValue(),
      calculate( "10", "3.3",null,
        ValueMetaInterface.TYPE_INTEGER, ValueMetaInterface.TYPE_NUMBER, ValueMetaInterface.TYPE_NUMBER, CalculatorMetaFunction.CALC_REMAINDER ) );
  }

  @Test
  public void testSumWithNullValues() throws Exception {
    ValueMetaInterface metaA = new ValueMetaInteger();
    metaA.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );
    ValueMetaInterface metaB = new ValueMetaInteger();
    metaA.setStorageType( ValueMetaInterface.STORAGE_TYPE_NORMAL );

    assertNull( ValueDataUtil.sum( metaA, null, metaB, null ) );

    Long valueB = new Long( 2 );
    ValueDataUtil.sum( metaA, null, metaB, valueB );
  }

  @Test
  public void testSumConvertingStorageTypeToNormal() throws Exception {
    ValueMetaInterface metaA = mock( ValueMetaInteger.class );
    metaA.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );

    ValueMetaInterface metaB = new ValueMetaInteger();
    metaB.setStorageType( ValueMetaInterface.STORAGE_TYPE_BINARY_STRING );
    Object valueB = "2";

    when( metaA.convertData( metaB, valueB ) ).thenAnswer( new Answer<Long>() {
      @Override
      public Long answer( InvocationOnMock invocation ) throws Throwable {
        return new Long( 2 );
      }
    } );

    Object returnValue = ValueDataUtil.sum( metaA, null, metaB, valueB );
    verify( metaA ).convertData( metaB, valueB );
    assertEquals( 2L, returnValue );
    assertEquals( metaA.getStorageType(), ValueMetaInterface.STORAGE_TYPE_NORMAL );
  }

  @Test
  public void testJaro() {
    assertEquals(new Double("0.0"), calculate("abcd", "defg", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_JARO ) );
    assertEquals(new Double("0.44166666666666665"), calculate("elephant", "hippo", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_JARO ) );
    assertEquals(new Double("0.8666666666666667"), calculate("hello", "hallo", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_JARO ) );
  }

  @Test
  public void testJaroWinkler() {
    assertEquals(new Double("0.0"), calculate("abcd", "defg", ValueMetaInterface.TYPE_STRING, CalculatorMetaFunction.CALC_JARO_WINKLER ) );
  }

  private Object calculate( String string_dataA, int valueMetaInterfaceType, int calculatorMetaFunction ) {
    return calculate( string_dataA, null, null, valueMetaInterfaceType, calculatorMetaFunction );
  }

  private Object calculate( String string_dataA, String string_dataB, int valueMetaInterfaceType,
      int calculatorMetaFunction ) {
    return calculate( string_dataA, string_dataB, null, valueMetaInterfaceType, calculatorMetaFunction );
  }

  private Object createObject( String string_value, int valueMetaInterfaceType, ValueMetaInterface parameterValueMeta) throws KettleValueException {
    if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_NUMBER ) {
      return ( !Utils.isEmpty( string_value ) ? Double.valueOf( string_value ) : null );
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_INTEGER ) {
      return ( !Utils.isEmpty( string_value ) ? Long.valueOf( string_value ) : null );
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_DATE ) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat( yyyy_MM_dd );
      try {
        return ( !Utils.isEmpty( string_value ) ? simpleDateFormat.parse( string_value ) : null );
      } catch ( ParseException pe ) {
        fail( pe.getMessage() );
        return null;
      }
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BIGNUMBER ) {
      return ( !Utils.isEmpty( string_value ) ? BigDecimal.valueOf( Double.valueOf( string_value ) ) : null );
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_STRING ) {
      return ( !Utils.isEmpty( string_value ) ? string_value : null );
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BINARY ) {
      ValueMetaInterface binaryValueMeta = new ValueMetaBinary( "binary_data" );
      return
        ( !Utils.isEmpty( string_value ) ? binaryValueMeta.convertData( parameterValueMeta, string_value ) : null );
    } else if ( valueMetaInterfaceType == ValueMetaInterface.TYPE_BOOLEAN ) {
      if ( !Utils.isEmpty( string_value ) ) {
        return ( string_value.equalsIgnoreCase( "true" ) ? true : false );
      } else {
        return null;
      }
    } else {
      fail( "Invalid ValueMetaInterface type." );
      return null;
    }
  }

  private Object calculate( String string_dataA, String string_dataB, String string_dataC, int valueMetaInterfaceTypeABC,
                            int calculatorMetaFunction ) {
    return calculate( string_dataA, string_dataB, string_dataC,
      valueMetaInterfaceTypeABC, valueMetaInterfaceTypeABC, valueMetaInterfaceTypeABC, calculatorMetaFunction );
  }

  private Object calculate( String string_dataA, String string_dataB, String string_dataC,
      int valueMetaInterfaceTypeA, int valueMetaInterfaceTypeB, int valueMetaInterfaceTypeC,
      int calculatorMetaFunction ) {

    try {

      //
      ValueMetaInterface parameterValueMeta = new ValueMetaString( "parameter" );

      // We create the meta information for
      ValueMetaInterface valueMetaA = createValueMeta( "data_A", valueMetaInterfaceTypeA );
      ValueMetaInterface valueMetaB = createValueMeta( "data_B", valueMetaInterfaceTypeB );
      ValueMetaInterface valueMetaC = createValueMeta( "data_C", valueMetaInterfaceTypeC );

      Object dataA = createObject( string_dataA, valueMetaInterfaceTypeA, parameterValueMeta);
      Object dataB = createObject( string_dataB, valueMetaInterfaceTypeB, parameterValueMeta);
      Object dataC = createObject( string_dataC, valueMetaInterfaceTypeC, parameterValueMeta);

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
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_REMAINDER ) {
        return ValueDataUtil.remainder( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_JARO ) {
        return ValueDataUtil.getJaro_Similitude( valueMetaA, dataA, valueMetaB, dataB );
      } else if ( calculatorMetaFunction == CalculatorMetaFunction.CALC_JARO_WINKLER ) {
        return ValueDataUtil.getJaroWinkler_Similitude( valueMetaA, dataA, valueMetaB, dataB );
      } else {
        fail( "Invalid CalculatorMetaFunction specified." );
        return null;
      }
    } catch ( KettleValueException kve ) {
      fail( kve.getMessage() );
      return null;
    }
  }

  private ValueMetaInterface createValueMeta( String name, int valueType ) {
    try {
      return ValueMetaFactory.createValueMeta( name, valueType );
    } catch ( KettlePluginException e ) {
      throw new RuntimeException( e );
    }
  }
}
