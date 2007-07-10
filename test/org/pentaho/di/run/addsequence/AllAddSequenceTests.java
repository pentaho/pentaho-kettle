package org.pentaho.di.run.addsequence;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllAddSequenceTests {

	public static Test suite() {
        TestSuite suite = new TestSuite("Run performance tests");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(RunAddSequence1To00.class);
        suite.addTestSuite(RunAddSequence1To05.class);
        suite.addTestSuite(RunAddSequence1To10.class);
        suite.addTestSuite(RunAddSequence2To00.class);
        suite.addTestSuite(RunAddSequence2To05.class);
        suite.addTestSuite(RunAddSequence2To10.class);
        suite.addTestSuite(RunAddSequence4To00.class);
        suite.addTestSuite(RunAddSequence4To05.class);
        suite.addTestSuite(RunAddSequence4To10.class);

        return suite;
	}
}
