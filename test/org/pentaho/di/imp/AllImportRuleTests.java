/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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