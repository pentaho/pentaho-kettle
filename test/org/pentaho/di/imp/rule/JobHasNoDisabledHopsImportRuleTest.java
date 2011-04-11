package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.JobHasNoDisabledHopsImportRule;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;

public class JobHasNoDisabledHopsImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    // Create a job to test.
    //
    JobMeta jobMeta = new JobMeta();

    // Add 3 dummy steps connected with hops.
    //
    JobEntryCopy lastCopy = null;
    for (int i=0;i<3;i++) {
      JobEntrySpecial dummy = new JobEntrySpecial();
      dummy.setDummy(true);
      dummy.setName("dummy"+(i+1));
      
      JobEntryCopy copy = new JobEntryCopy(dummy);
      copy.setLocation(50+i*50,50);
      copy.setDrawn();
      jobMeta.addJobEntry(copy);
      
      if (lastCopy!=null) {
        JobHopMeta hop = new JobHopMeta(lastCopy, copy);
        jobMeta.addJobHop(hop);
      }
      lastCopy = copy;
    }
   
    // Load the plugin to test from the registry.
    //
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "JobHasNoDisabledHops");
    assertNotNull("The 'job has no disabled hops' rule could not be found in the plugin registry!", plugin);
  
    JobHasNoDisabledHopsImportRule rule = (JobHasNoDisabledHopsImportRule) registry.loadClass(plugin);
    assertNotNull("The 'job has no disabled hops' class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);
    
    List<ImportValidationFeedback> feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has no disabled hops'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    jobMeta.getJobHop(0).setEnabled(false);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has no disabled hops'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't expect any feedback from the 'job has no disabled hops' while disabled", feedback.isEmpty());
  }
}
