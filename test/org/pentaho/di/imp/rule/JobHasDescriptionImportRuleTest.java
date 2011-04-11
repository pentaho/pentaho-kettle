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
