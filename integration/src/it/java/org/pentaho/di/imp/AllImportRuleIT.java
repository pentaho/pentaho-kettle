/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
