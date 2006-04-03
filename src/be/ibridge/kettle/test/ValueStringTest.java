package be.ibridge.kettle.test;

import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;
import be.ibridge.kettle.core.value.ValueString;

public class ValueStringTest extends TestCase {
	private ValueString vs;
	public ValueStringTest(String arg0) {
		super(arg0);
		vs = new ValueString();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/*
	 * Test method for 'be.ibridge.kettle.core.value.ValueString.getDate()'
	 */
	public void testGetDate() {

	}

	/*
	 * Test method for 'be.ibridge.kettle.core.value.ValueString.setDate(Date)'
	 */
	public void testSetDate() {
		vs.setDate(new Date());
		System.out.println(vs.getString());
		Assert.assertTrue(vs.getString().length()==23);
	}

}
