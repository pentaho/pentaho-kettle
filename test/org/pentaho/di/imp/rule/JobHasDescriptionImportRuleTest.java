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

package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.JobHasDescriptionImportRule;
import org.pentaho.di.job.JobMeta;

public class JobHasDescriptionImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    JobMeta jobMeta = new JobMeta();
    jobMeta.setDescription("This job is used for testing an import rule");
   
    PluginRegistry registry = PluginRegistry.getInstance();
    
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "JobHasDescription");
    assertNotNull("The 'job has description' rule could not be found in the plugin registry!", plugin);
    
    JobHasDescriptionImportRule rule = (JobHasDescriptionImportRule) registry.loadClass(plugin);
    assertNotNull("The 'job has description rule' class could not be loaded by the plugin registry!", plugin);

    rule.setMinLength(20);
    rule.setEnabled(true);

    List<ImportValidationFeedback> feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    rule.setMinLength(2000);
    rule.setEnabled(true);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't expect any feedback from the 'job has description rule' while disabled", feedback.isEmpty());
  }
}
