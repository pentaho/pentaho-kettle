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

import junit.framework.TestCase;

/**
 * Test class for the basic functionality of Const.
 *
 * @author Sven Boden
 */
public class ConstTest extends TestCase
{
	private boolean isArraySorted(String [] arr)
	{
		if ( arr.length < 2) return true;

	    for (int idx = 0; idx < arr.length - 1; idx++)
	    {
	        if (arr[idx].compareTo(arr[idx + 1]) > 0)
	        	return false;
	    }

	    return true;
	}

	/**
	 * Test sortString().
	 */
	public void testSortStrings()
	{
		String arr1[] = { "Red", "Blue", "Black", "Black", "Green" };
		String arr2[] = { "aaa", "zzz", "yyy", "sss", "ttt", "t" };
		String arr3[] = { "A", "B", "C", "D" };

		String results[] = Const.sortStrings(arr1);
		assertTrue(isArraySorted(arr1));
		assertTrue(isArraySorted(results));

		results = Const.sortStrings(arr2);
		assertTrue(isArraySorted(arr2));
		assertTrue(isArraySorted(results));

		results = Const.sortStrings(arr3);
		assertTrue(isArraySorted(arr3));
		assertTrue(isArraySorted(results));		
	}

	public void testIsEmpty()
	{
	    assertEquals(true, Const.isEmpty((String)null));
	    assertEquals(true, Const.isEmpty(""));
	    assertEquals(false, Const.isEmpty("test"));
	}

	public void testIsEmptyStringBuffer()
	{
	    assertEquals(true, Const.isEmpty((StringBuffer)null));
	    assertEquals(true, Const.isEmpty(new StringBuffer("")));
	    assertEquals(false, Const.isEmpty(new StringBuffer("test")));
	}

	public void testNVL()
	{
		assertNull(Const.NVL(null, null));
	    assertEquals("test", Const.NVL("test", "test1"));	    
	    assertEquals("test", Const.NVL("test", null));
	    assertEquals("test1", Const.NVL(null, "test1"));	    
	}	

	public void testNrSpacesBefore()
	{
		try  
		{
			Const.nrSpacesBefore(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}

		assertEquals(0, Const.nrSpacesBefore(""));
		assertEquals(1, Const.nrSpacesBefore(" "));
		assertEquals(3, Const.nrSpacesBefore("   "));
		assertEquals(0, Const.nrSpacesBefore("test"));
		assertEquals(0, Const.nrSpacesBefore("test  "));
		assertEquals(3, Const.nrSpacesBefore("   test"));
		assertEquals(4, Const.nrSpacesBefore("    test  "));
	}

	public void testNrSpacesAfter()
	{
		try  
		{
			Const.nrSpacesAfter(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}

		assertEquals(0, Const.nrSpacesAfter(""));
		assertEquals(1, Const.nrSpacesAfter(" "));
		assertEquals(3, Const.nrSpacesAfter("   "));
		assertEquals(0, Const.nrSpacesAfter("test"));
		assertEquals(2, Const.nrSpacesAfter("test  "));
		assertEquals(0, Const.nrSpacesAfter("   test"));
		assertEquals(2, Const.nrSpacesAfter("    test  "));
	}

	public void testLtrim()
	{
		try  
		{
			Const.ltrim(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}
		assertEquals("", Const.ltrim(""));
		assertEquals("", Const.ltrim("  "));
		assertEquals("test ", Const.ltrim("test "));
		assertEquals("test ", Const.ltrim("  test "));
	}	

	public void testRtrim()
	{
		try  
		{
			Const.rtrim(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}
		assertEquals("", Const.rtrim(""));
		assertEquals("", Const.rtrim("  "));
		assertEquals("test", Const.rtrim("test "));
		assertEquals("test ", Const.ltrim("  test "));
	}		

	public void testTrim()
	{
		try  
		{
			Const.trim(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}
		assertEquals("", Const.trim(""));
		assertEquals("", Const.trim("  "));
		assertEquals("test", Const.trim("test "));
		assertEquals("test", Const.trim("  test "));
	}		

	public void testOnlySpaces()
	{
		try  
		{
			Const.onlySpaces(null);
			fail("Expected NullPointerException");
		}
		catch (NullPointerException ex)
		{}
		assertEquals(true, Const.onlySpaces(""));
		assertEquals(true, Const.onlySpaces("  "));
		assertEquals(false, Const.onlySpaces("   test "));		
	}
}
