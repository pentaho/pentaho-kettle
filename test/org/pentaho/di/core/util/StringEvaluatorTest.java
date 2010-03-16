package org.pentaho.di.core.util;

import java.util.Date;

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
		assertEquals(evaluator.getStringEvaluationResults().size(), 1);
		StringEvaluationResult result = evaluator.getStringEvaluationResults().get(0);
		
		assertEquals("Not a date detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_DATE);
		Date minDate = result.getConversionMeta().getDate(result.getMin());
		assertEquals(minDate.getTime(), 1262259296000L);
		
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
		assertEquals(evaluator.getStringEvaluationResults().size(), 5);
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
		assertEquals(evaluator.getStringEvaluationResults().size(), 1);
		StringEvaluationResult result = evaluator.getStringEvaluationResults().get(0);
		assertEquals("Not a number detetected", result.getConversionMeta().getType(), ValueMetaInterface.TYPE_NUMBER);
		
		int nrEmpty = result.getNrNull();
		assertEquals(nrEmpty, 1);
		assertEquals(evaluator.getValues().size(), series4.length);
	}
}
