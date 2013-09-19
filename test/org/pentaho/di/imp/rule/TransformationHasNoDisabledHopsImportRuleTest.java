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
import org.pentaho.di.imp.rules.TransformationHasNoDisabledHopsImportRule;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;

public class TransformationHasNoDisabledHopsImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    // Create a transformation to test.
    //
    TransMeta transMeta = new TransMeta();

    // Add 3 dummy steps connected with hops.
    //
    StepMeta lastStep = null;
    for (int i=0;i<3;i++) {
      DummyTransMeta dummyTransMeta = new DummyTransMeta();
      StepMeta stepMeta = new StepMeta("dummy"+(i+1), dummyTransMeta);
      stepMeta.setLocation(50+i*50,50);
      stepMeta.setDraw(true);
      transMeta.addStep(stepMeta);
      if (lastStep!=null) {
        TransHopMeta hop = new TransHopMeta(lastStep, stepMeta);
        transMeta.addTransHop(hop);
      }
      lastStep=stepMeta;
    }
   
    // Load the plugin to test from the registry.
    //
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "TransformationHasNoDisabledHops");
    assertNotNull("The 'transformation has no disabled hops' rule could not be found in the plugin registry!", plugin);
  
    TransformationHasNoDisabledHopsImportRule rule = (TransformationHasNoDisabledHopsImportRule) registry.loadClass(plugin);
    assertNotNull("The 'transformation has no disabled hops' class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);
    
    List<ImportValidationFeedback> feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has no disabled hops'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    transMeta.getTransHop(0).setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has no disabled hops'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't expect any feedback from the 'transformation has no disabled hops' while disabled", feedback.isEmpty());
    
  }
}
