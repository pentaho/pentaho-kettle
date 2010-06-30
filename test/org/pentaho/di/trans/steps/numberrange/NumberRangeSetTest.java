package org.pentaho.di.trans.steps.numberrange;

import java.util.Arrays;

import org.pentaho.di.core.exception.KettleException;

import junit.framework.TestCase;

public class NumberRangeSetTest extends TestCase {

	private static final String VALUE_LESS_10 = "Less than 10";
	private static final String VALUE_10_20 = "10-20";
	private static final String VALUE_TO_20 = "Up to 20";
	private static final String VALUE_MORE_20 = "More than 20";
	private static final String FALLBACK_VALUE = "unknown";

	private NumberRangeSet numberRange;

	public void setUp() throws Exception {
		NumberRangeRule rule1 = new NumberRangeRule(0, 10, VALUE_LESS_10);
		NumberRangeRule rule2 = new NumberRangeRule(10, 20, VALUE_10_20);
		NumberRangeRule rule3 = new NumberRangeRule(0, 20, VALUE_TO_20);
		NumberRangeRule rule4 = new NumberRangeRule(20, Integer.MAX_VALUE, VALUE_MORE_20);

		numberRange = new NumberRangeSet(Arrays.asList(rule1, rule2, rule3,	rule4), FALLBACK_VALUE);
	}

	public void testEvaluateDouble() throws Exception {
		setUp();
		
		assertEquals("Wrong values calculated for 0", VALUE_LESS_10
				+ NumberRangeSet.getMultiValueSeparator() + VALUE_TO_20, numberRange
				.evaluateDouble(0));
		assertEquals("Wrong values calculated for negative value", "",
				numberRange.evaluateDouble(-10));
		assertEquals(
				"Wrong values calculated for value below first lower bound",
				VALUE_LESS_10 + NumberRangeSet.getMultiValueSeparator() + VALUE_TO_20,
				numberRange.evaluateDouble(9.999));

		assertEquals("Wrong values calculated for first lower bound",
				VALUE_10_20 + NumberRangeSet.getMultiValueSeparator() + VALUE_TO_20,
				numberRange.evaluateDouble(10));
		assertEquals("Wrong values calculated within range", VALUE_10_20
				+ NumberRangeSet.getMultiValueSeparator() + VALUE_TO_20, numberRange
				.evaluateDouble(15));
		assertEquals("Wrong values calculated at upper bound", VALUE_MORE_20, numberRange.evaluateDouble(20));
		assertEquals("Wrong values calculated above upper bound",
				VALUE_MORE_20, numberRange.evaluateDouble(50));
	}

	public void testEvaluate() throws Exception {
		setUp();
		
		assertEquals("Wrong values calculated if double is passed",
				VALUE_LESS_10 + NumberRangeSet.getMultiValueSeparator() + VALUE_TO_20, numberRange.evaluate("0"));
		try {
			numberRange.evaluate("10 EUR");
			assertTrue("String to number worked for '10 EUR' when it was expected to fail", false);
		} catch(KettleException e) {
			// expected exception
		}

		try {
			numberRange.evaluate("");
			assertTrue("String to number worked for an empty when it was expected to fail", false);
		} catch(KettleException e) {
			// expected exception
		}

		assertEquals("Didn't handle null value", FALLBACK_VALUE, numberRange.evaluate((String)null));
	}

}
