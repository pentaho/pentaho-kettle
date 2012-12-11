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

package org.pentaho.di.trans.steps.numberrange;

import junit.framework.TestCase;

public class NumberRangeRuleTest extends TestCase {
	
	private static final String VALUE_10_20 = "10-20";
	private static NumberRangeRule numberRangeRule;

	public void testEvaluate() {
		numberRangeRule = new NumberRangeRule(10,20, VALUE_10_20);

		assertEquals("Missed value in middle of range", VALUE_10_20, numberRangeRule.evaluate(15));
		assertEquals("Missed value within lower bound", VALUE_10_20, numberRangeRule.evaluate(10));
		assertEquals("Missed value outside lower bound", null, numberRangeRule.evaluate(9.999));
		assertEquals("Missed value within upper bound", VALUE_10_20, numberRangeRule.evaluate(19.9999));
		assertEquals("Missed value outside upper bound", null, numberRangeRule.evaluate(20));
		assertEquals("Missed value at 0", null, numberRangeRule.evaluate(0));
		assertEquals("Missed value at max integer", null, numberRangeRule.evaluate(Integer.MAX_VALUE));
		assertEquals("Missed negative value", null, numberRangeRule.evaluate(-20));
	}

}
