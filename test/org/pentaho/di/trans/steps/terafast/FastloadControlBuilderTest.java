package org.pentaho.di.trans.steps.terafast;

import org.apache.commons.lang.SystemUtils;

import junit.framework.TestCase;

public class FastloadControlBuilderTest extends TestCase {
	
	public void testErrorTablesBuilding() {
		
		FastloadControlBuilder fastloadControlBuilder = new FastloadControlBuilder();
		String expectedResult = "BEGIN LOADING myTable ERRORFILES MyDB.error1,MyDB.error2;"+SystemUtils.LINE_SEPARATOR;
		fastloadControlBuilder.beginLoading("MyDB", "myTable");
		assertEquals(expectedResult, fastloadControlBuilder.toString());
		
		//  Create a new FastloadControlBuilder or we will be appending to the
		//  fastloadControlBuilder's previous test
		fastloadControlBuilder = new FastloadControlBuilder();
		expectedResult = "BEGIN LOADING myTable ERRORFILES error1,error2;"+SystemUtils.LINE_SEPARATOR;
		fastloadControlBuilder.beginLoading(null, "myTable");
		assertEquals(expectedResult, fastloadControlBuilder.toString());
		
		//  Create a new FastloadControlBuilder or we will be appending to the
		//  fastloadControlBuilder's previous test
		fastloadControlBuilder = new FastloadControlBuilder();
		expectedResult = "BEGIN LOADING myTable ERRORFILES error1,error2;"+SystemUtils.LINE_SEPARATOR;
		fastloadControlBuilder.beginLoading("", "myTable");
		assertEquals(expectedResult, fastloadControlBuilder.toString());
	}

}
