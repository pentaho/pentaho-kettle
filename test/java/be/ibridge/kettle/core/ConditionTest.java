 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.core;

import be.ibridge.kettle.core.value.Value;
import junit.framework.TestCase;

/**
 * Test class for the basic functionality of Condition.
 *
 * @author Sven Boden
 */
public class ConditionTest extends TestCase
{
	/**
	 * General test. This is to see how far we get with simple cases.
	 * Can also be used showcase on Condition.
	 */
	public void testGeneral()
	{
		Row r = new Row();
		r.addValue(new Value("A", "aaaa"));
		r.addValue(new Value("B", false));
		r.addValue(new Value("C", 12.34));
		r.addValue(new Value("D", 77L));

		// Check if field D is equal to a certain value.
		Condition cb1 = new Condition(Condition.OPERATOR_NONE, "D", Condition.FUNC_EQUAL, null, new Value("other", 77L));
		assertTrue(cb1.evaluate(r));
		
		cb1.setID(100L);
		assertEquals(100L, cb1.getID());
		assertFalse(cb1.isEmpty());
		assertEquals(0, cb1.nrConditions());

		Condition cb2 = new Condition("A", Condition.FUNC_SMALLER, null, new Value("other", "bbb"));
		assertTrue(cb2.evaluate(r));

		Condition two = new Condition();
		two.addCondition(cb1);
		two.addCondition(cb2);
		cb2.setOperator(Condition.OPERATOR_XOR);
		assertFalse(two.evaluate(r));
		assertEquals(2, two.nrConditions());

		Condition cb3 = new Condition("B", Condition.FUNC_EQUAL, null, new Value("other", false));		
		assertTrue(cb3.evaluate(r));

		Condition cb4 = new Condition("C", Condition.FUNC_EQUAL, null, new Value("other", 12.34));
		assertTrue(cb4.evaluate(r));

		Condition two2 = new Condition();
		two2.addCondition(cb3);
		two2.addCondition(cb4);
		cb4.setOperator(Condition.OPERATOR_AND);
		assertTrue(two2.evaluate(r));

		Condition three = new Condition();

		three.addCondition(two);
		three.addCondition(two2);
		two2.setOperator(Condition.OPERATOR_XOR);
		three.setOperator(Condition.OPERATOR_NOT);
		assertTrue(three.evaluate(r));				
	}
	
	public void testNegation()
	{
		Row r = new Row();
		r.addValue(new Value("A", "aaaa"));
		r.addValue(new Value("B", false));
		r.addValue(new Value("C", 12.34));
		r.addValue(new Value("D", 77L));

		// Check if field D is equal to a certain value.
		Condition cb1 = new Condition(Condition.OPERATOR_NONE, "D", Condition.FUNC_EQUAL, null, new Value("other", 77L));
		assertFalse(cb1.isNegated());
		cb1.setNegated(true);
		assertTrue(cb1.isNegated());
		assertFalse(cb1.evaluate(r));
		
		cb1.negate();
		assertFalse(cb1.isNegated());
		assertTrue(cb1.evaluate(r));
	}

	/**
	 * Test the getXML() method.
	 */
	public void testGetXML()
	{
		Condition cb1 = new Condition(Condition.OPERATOR_NONE, "D", Condition.FUNC_EQUAL, null, new Value("other", 77L));
		assertEquals("              D = [ 77]" + Const.CR, cb1.toString());

		Condition cb2 = new Condition("A", Condition.FUNC_SMALLER, null, new Value("other", "bbb"));
		assertEquals("              A < [bbb]" + Const.CR, cb2.toString());

		Condition two = new Condition();
		two.addCondition(cb1);
		two.addCondition(cb2);
		cb2.setOperator(Condition.OPERATOR_XOR);
		assertEquals("XOR           A < [bbb]" + Const.CR, cb2.toString());
		assertEquals("(" + Const.CR + "                D = [ 77]" + Const.CR + "  XOR           A < [bbb]" + Const.CR + ")" + Const.CR, two.toString());
	}
}
