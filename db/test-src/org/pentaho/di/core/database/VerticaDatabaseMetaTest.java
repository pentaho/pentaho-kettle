package org.pentaho.di.core.database;

import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * Tests for the Vertica Database Meta classes.
 * @author sflatley
 *
 */
public class VerticaDatabaseMetaTest {
	
	/**
	 * Tests the supportsTimeStampToDateConversion method.
	 */
	@Test
	public void testSupportsTimeStampToDateConversion() {
		DatabaseInterface databaseInterface = new VerticaDatabaseMeta();
        assertFalse(databaseInterface.supportsTimeStampToDateConversion());
        
		databaseInterface = new Vertica5DatabaseMeta();
        assertFalse(databaseInterface.supportsTimeStampToDateConversion());
        
	}
}