/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.pentaho.di.core.row.ValueMetaInterface;

public class StringEvaluatorTest extends TestCase {
	
	private String[] series1 = new String[] { "Foo", "Bar", "One", "", "Two", "Three", };
	
	public void testSeries1() throws Exception {
		StringEvaluator evaluator = new StringEvaluator(false);
		for (String string : series1) {
			evaluator.evaluateString(string);
		}
		
		assertEquals(evaluator.getCount(), series1.length);
		assertEquals(evaluator.getMaxLength(), 5);
		
		// We expect to find nothing.  This means that we have to revert to a String data type.
		//
		assertEquals(evaluator.getStringEvaluationResults().size(), 0);
	}
	
	private String[] series2 = new String[] { "2009/12/31 12:34:56", "2010/02/14 23:22:01", };
	
	public void testSeries2() throws Exception {
		StringEvaluator evaluator = new StringEvaluator(false);
		for (String string : series2) {
			evaluator.evaluateString(string);
		}
		
		assertEquals(evaluator.getCount(), series2.length);
		assertEquals(evaluator.getMaxLength(), 19);
		StringEvaluationResult result = evaluator.getStringEvaluationResults().get(0);
		
		assertEquals("Not a date detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_DATE);
		Date minDate = result.getConversionMeta().getDate(result.getMin());
		assertEquals(minDate.getTime(), 1262280896000L);
		
		int nrEmpty = result.getNrNull();
		assertEquals(nrEmpty, 0);
		assertEquals(evaluator.getValues().size(), series2.length);
	}
	
	private String[] series3 = new String[] { "1234,56", "12394,26", "1934,34", "19245,23", "" };
	
	public void testSeries3() throws Exception {
		StringEvaluator evaluator = new StringEvaluator(false);
		for (String string : series3) {
			evaluator.evaluateString(string);
		}
		
		assertEquals(evaluator.getCount(), series3.length);
		assertEquals(evaluator.getMaxLength(), 8);
		StringEvaluationResult result = evaluator.getStringEvaluationResults().get(0);
		assertEquals("Not a number detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER);
		
		int nrEmpty = result.getNrNull();
		assertEquals(nrEmpty, 1);
		assertEquals(evaluator.getValues().size(), series3.length);
	}
	
	private String[] series4 = new String[] { "01234,56     ", "             ", "98765,43     ", "12394,26     ", "01934,34     ", "19245,23     ", "00045,67     ", };
	
	public void testSeries4() throws Exception {
		StringEvaluator evaluator = new StringEvaluator(true);
		for (String string : series4) {
			evaluator.evaluateString(string);
		}
		
		assertEquals(evaluator.getCount(), series4.length);
		assertEquals(evaluator.getMaxLength(), 13);
		StringEvaluationResult result = evaluator.getStringEvaluationResults().get(0);
		assertEquals("Not a number detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER);
		
		int nrEmpty = result.getNrNull();
		assertEquals(nrEmpty, 1);
		assertEquals(evaluator.getValues().size(), series4.length);
	}

  public void testCurrencyData() {
    StringEvaluator eval = new StringEvaluator(true);
    String[] values = new String[]{ "$300.00", "$3,400", "$23.00", "($0.50)" };
    for (String value : values) {
      eval.evaluateString(value);
    }
    assertEquals(values.length, eval.getCount());
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals("Not a number detetected", ValueMetaInterface.TYPE_NUMBER, result.getConversionMeta().getType());
    assertEquals("Precision not correct", 2, result.getConversionMeta().getPrecision());
    assertEquals("Currency format mask is incorrect", "$#,##0.00;($#,##0.00)", result.getConversionMeta().getConversionMask());
  }

  public void testCurrencyData_UK() {
    Locale orig = Locale.getDefault();
    Locale.setDefault(Locale.UK);
    StringEvaluator eval = new StringEvaluator(true);

    DecimalFormat currencyFormat = ((DecimalFormat) NumberFormat.getCurrencyInstance());
    System.out.println("UK Locale currency format: " + currencyFormat.toLocalizedPattern());
    try {
      currencyFormat.parse("-£400.059");
    } catch (ParseException e) {
      fail();
    }
    String[] values = new String[]{ "£400.019", "£3,400.029", "£23.00", "-£400.059" };
    for (String value : values) {
      eval.evaluateString(value);
    }
    assertEquals(values.length, eval.getCount());
    StringEvaluationResult result = eval.getAdvicedResult();

    Locale.setDefault(orig);

    assertEquals("Not a number detetected", ValueMetaInterface.TYPE_NUMBER, result.getConversionMeta().getType());
    assertEquals("Precision not correct", 2, result.getConversionMeta().getPrecision());
    assertEquals("Currency format mask is incorrect", "£#,##0.00", result.getConversionMeta().getConversionMask());
  }

  public void testCustomDateFormats() {
    List<String> dates = Arrays.asList(new String[] {"MM/dd/yyyy"});
    List<String> numbers = Arrays.asList(new String[] {"#,##0.###"});

    StringEvaluator eval = new StringEvaluator(true, numbers, dates);
    String[] goodDateValues = new String[]{ "01/01/2000", "02/02/2000", "03/03/2000" };
    String[] badDateValues = new String[]{ "01-01-2000", "02-02-2000", "03-03-2000" };

    for (String value : goodDateValues) {
      eval.evaluateString(value);
    }
    assertEquals(goodDateValues.length, eval.getCount());
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals("Not a date detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_DATE);

    eval = new StringEvaluator(true, numbers, dates);
    for (String value : badDateValues) {
      eval.evaluateString(value);
    }
    assertEquals(badDateValues.length, eval.getCount());
    result = eval.getAdvicedResult();
    assertFalse("Date detetected", result.getConversionMeta().getType() == ValueMetaInterface.TYPE_DATE);
  }

  public void testCustomNumberFormats() {
    List<String> dates = Arrays.asList(new String[] {"MM/dd/yyyy"});
    List<String> numbers = Arrays.asList(new String[] {"#"});

    StringEvaluator eval = new StringEvaluator(true, numbers, dates);
    String[] goodValues = new String[]{ "200.00", "999.99", "4,309.88" };
    String[] badValues = new String[]{ "9 00", "$30.00", "3.999,00" };

    for (String value : goodValues) {
      eval.evaluateString(value);
    }
    assertEquals(goodValues.length, eval.getCount());
    StringEvaluationResult result = eval.getAdvicedResult();
    assertEquals("Not a number detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER);

    eval = new StringEvaluator(true, numbers, dates);
    for (String value : badValues) {
      eval.evaluateString(value);
    }
    assertEquals(badValues.length, eval.getCount());
    result = eval.getAdvicedResult();
    assertFalse("Number detetected", result.getConversionMeta().getType() == ValueMetaInterface.TYPE_NUMBER);
  }

  public void testDeterminePrecision() {
    assertEquals(4, StringEvaluator.determinePrecision("#.0000"));
    assertEquals(4, StringEvaluator.determinePrecision("0.#### $"));
    assertEquals(0, StringEvaluator.determinePrecision(null));
    assertEquals(4, StringEvaluator.determinePrecision("0.##00 $"));
    assertEquals(4, StringEvaluator.determinePrecision("##,##0.#0## $"));

  }

}
