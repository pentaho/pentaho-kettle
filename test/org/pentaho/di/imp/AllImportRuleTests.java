package org.pentaho.di.imp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.imp.rule.DatabaseConfigurationImportRuleTest;
import org.pentaho.di.imp.rule.JobHasDescriptionImportRuleTest;
import org.pentaho.di.imp.rule.JobHasJobLogConfiguredImportRuleTest;
import org.pentaho.di.imp.rule.TransformationHasDescriptionImportRuleTest;
import org.pentaho.di.imp.rule.TransformationHasTransLogConfiguredImportRuleTest;


/**
 * Regression tests for the PDI framework.
 * 
 * @author sboden
 */
public class AllImportRuleTests
{   
    public static Test suite() throws Exception
    {
        TestSuite suite = new TestSuite("Run import rule tests");

        suite.addTestSuite(DatabaseConfigurationImportRuleTest.class);
        suite.addTestSuite(JobHasDescriptionImportRuleTest.class);
        suite.addTestSuite(TransformationHasDescriptionImportRuleTest.class);
        suite.addTestSuite(JobHasJobLogConfiguredImportRuleTest.class);
        suite.addTestSuite(TransformationHasTransLogConfiguredImportRuleTest.class);
       
        return suite;
    }
}