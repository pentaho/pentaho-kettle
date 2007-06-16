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
 * Test class for the basic functionality of Row.
 *
 * Rows on the same stream in KETTLE are always assumed to be
 * of the same width.
 *
 * @author Sven Boden
 */
public class RectangleTest extends TestCase
{
	/**
	 * Test contains().
	 */
	public void testContains()
	{
		Rectangle r = new Rectangle(100, 100, 40, 60);
		
		// end points
		assertTrue(r.contains(100,100));
		assertFalse(r.contains(140,100));
		assertFalse(r.contains(100,160));
		assertFalse(r.contains(140,160));

		// just inside
		assertTrue(r.contains(101,101));
		assertTrue(r.contains(139,101));
		assertTrue(r.contains(101,159));
		assertTrue(r.contains(139,159));
		
		// on all sides
		assertFalse(r.contains(99,101));
		assertFalse(r.contains(141,101));
		assertFalse(r.contains(101,99));
		assertFalse(r.contains(139,161));

		
		// end points
		assertTrue(r.contains(new Point(100,100)));
		assertFalse(r.contains(new Point(140,100)));
		assertFalse(r.contains(new Point(100,160)));
		assertFalse(r.contains(new Point(140,160)));

		// just inside
		assertTrue(r.contains(new Point(101,101)));
		assertTrue(r.contains(new Point(139,101)));
		assertTrue(r.contains(new Point(101,159)));
		assertTrue(r.contains(new Point(139,159)));
		
		// on all sides
		assertFalse(r.contains(new Point(99,101)));
		assertFalse(r.contains(new Point(141,101)));
		assertFalse(r.contains(new Point(101,99)));
		assertFalse(r.contains(new Point(139,161)));	
	}

	/**
	 * Test equals().
	 */
	public void testEquals()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		Rectangle r2 = new Rectangle(100, 100, 40, 60);
		Rectangle r3 = new Rectangle(100, 120, 40, 60);
		
		// end points
		assertTrue(r1.equals(r1));
		assertFalse(r1.equals(new Point(1, 1)));
		assertTrue(r1.equals(r2));
		assertFalse(r1.equals(r3));
	}

	/**
	 * Test hashCode().
	 */
	public void testHashCode()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		Rectangle r2 = new Rectangle(100, 120, 40, 80);
		
		assertEquals(20,  r1.hashCode());
		assertEquals(100, r2.hashCode());
	}

	/**
	 * Test intersection1().
	 */
	public void testIntersection1()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		
		Rectangle r3 = new Rectangle(130, 130, 80, 90); // overlap with r1
		
		Rectangle r4 = new Rectangle(40,  100, 40, 60);
		Rectangle r5 = new Rectangle(180, 100, 40, 60);
		Rectangle r6 = new Rectangle(100,  30, 40, 60);
		Rectangle r7 = new Rectangle(100, 180, 40, 60);

		assertEquals(new Rectangle(100,100,40,60), new Rectangle(100,100,40,60).intersection(r1));
		assertEquals(new Rectangle(130,130,10,30), new Rectangle(100,100,40,60).intersection(r3));
		assertEquals(new Rectangle(0,100,0,60),    new Rectangle(100,100,40,60).intersection(r4));
		assertEquals(new Rectangle(0,100,0,60),    new Rectangle(100,100,40,60).intersection(r5));
		assertEquals(new Rectangle(100,0,40,0),    new Rectangle(100,100,40,60).intersection(r6));
		assertEquals(new Rectangle(100,0,40,0),    new Rectangle(100,100,40,60).intersection(r7));
	}

	/**
	 * Test intersection2().
	 */
	public void testIntersection2()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		
		Rectangle r3 = new Rectangle(130, 130, 80, 90); // overlap with r1
		
		Rectangle r4 = new Rectangle(40,  100, 40, 60);
		Rectangle r5 = new Rectangle(180, 100, 40, 60);
		Rectangle r6 = new Rectangle(100,  30, 40, 60);
		Rectangle r7 = new Rectangle(100, 180, 40, 60);

		assertTrue(r1.intersects(r1));
		assertTrue(r1.intersects(r3));
		assertFalse(r1.intersects(r4));
		assertFalse(r1.intersects(r5));
		assertFalse(r1.intersects(r6));
		assertFalse(r1.intersects(r7));
	}

	/**
	 * Test toIsEmpty().
	 */
	public void testIsEmpty()
	{
		/* Rectangle r1 = */ new Rectangle(100, 100, 40, 60);
		/* Rectangle r2 = */ new Rectangle(130, 130, 80, 90);
		/* Rectangle r3 = */ new Rectangle(0,  0, 0, 0);
		/* Rectangle r4 = */ new Rectangle(-10,  -40, 0, -61);
	}	

	/**
	 * Test toString().
	 */
	public void testToString()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		Rectangle r2 = new Rectangle(130, 130, 80, 90);
		Rectangle r3 = new Rectangle(100, 100, -10, -20);
		Rectangle r4 = new Rectangle(100, 100, -10, 20);
		Rectangle r5 = new Rectangle(100, 100, 10, -20);
		Rectangle r6 = new Rectangle(-10, 100, 10, 20);
		Rectangle r7 = new Rectangle(10, -100, 10, 20);
		Rectangle r8 = new Rectangle(10, 100, -10, 20);

		assertFalse(r1.isEmpty());
		assertFalse(r2.isEmpty());
		assertTrue(r3.isEmpty());
		assertTrue(r4.isEmpty());
		assertTrue(r5.isEmpty());
		assertFalse(r6.isEmpty());
		assertFalse(r7.isEmpty());
		assertTrue(r8.isEmpty());
	}
	
	/**
	 * Test union().
	 */
	public void testUnion()
	{
		Rectangle r1 = new Rectangle(100, 100, 40, 60);
		
		Rectangle r3 = new Rectangle(130, 130, 80, 90); // overlap with r1
		
		Rectangle r4 = new Rectangle(40,  100, 40, 60);
		Rectangle r5 = new Rectangle(180, 100, 40, 60);
		Rectangle r6 = new Rectangle(100,  30, 40, 60);
		Rectangle r7 = new Rectangle(100, 180, 40, 60);

		assertEquals(new Rectangle(100,100,40,60),   new Rectangle(100,100,40,60).union(r1));
		assertEquals(new Rectangle(100,100,110,120), new Rectangle(100,100,40,60).union(r3));
		assertEquals(new Rectangle(40,100,100,60),   new Rectangle(100,100,40,60).union(r4));
		assertEquals(new Rectangle(100,100,120,60),  new Rectangle(100,100,40,60).union(r5));
		assertEquals(new Rectangle(100,30,40,130),   new Rectangle(100,100,40,60).union(r6));
		assertEquals(new Rectangle(100,100,40,140),  new Rectangle(100,100,40,60).union(r7));
	}
}
