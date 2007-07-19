package org.pentaho.di.run;

import junit.framework.TestCase;

public class RunTestCase extends TestCase {

	// this value scales the size of (most of) the performance tests
	// set this to 100000 for the baseline tests
	// set it to a small value (e.g. 100) to verify that the performance tests are successful
	
	protected final int rowCount = 100;
}
