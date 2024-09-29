/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.imp;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.imp.rule.DatabaseConfigurationImportRuleIT;
import org.pentaho.di.imp.rule.JobHasDescriptionImportRuleIT;
import org.pentaho.di.imp.rule.JobHasJobLogConfiguredImportRuleIT;
import org.pentaho.di.imp.rule.TransformationHasDescriptionImportRuleIT;
import org.pentaho.di.imp.rule.TransformationHasTransLogConfiguredImportRuleIT;

/**
 * Regression tests for the PDI framework.
 *
 * @author sboden
 */
public class AllImportRuleIT {
  public static Test suite() throws Exception {
    TestSuite suite = new TestSuite( "Run import rule tests" );

    suite.addTestSuite( DatabaseConfigurationImportRuleIT.class );
    suite.addTestSuite( JobHasDescriptionImportRuleIT.class );
    suite.addTestSuite( TransformationHasDescriptionImportRuleIT.class );
    suite.addTestSuite( JobHasJobLogConfiguredImportRuleIT.class );
    suite.addTestSuite( TransformationHasTransLogConfiguredImportRuleIT.class );

    return suite;
  }
}
