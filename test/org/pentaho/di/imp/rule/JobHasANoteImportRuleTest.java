package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.JobHasANoteImportRule;
import org.pentaho.di.job.JobMeta;

public class JobHasANoteImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    // Create a job to test.
    //
    JobMeta jobMeta = new JobMeta();
    NotePadMeta note = new NotePadMeta("A note documenting the transformation", 50, 50, 200, 50);
    jobMeta.addNote(note);

    // Load the plugin to test from the registry.
    //
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "JobHasANote");
    assertNotNull("The 'job has a note' rule could not be found in the plugin registry!", plugin);
  
    JobHasANoteImportRule rule = (JobHasANoteImportRule) registry.loadClass(plugin);
    assertNotNull("The 'job has a note' rule class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);
    
    List<ImportValidationFeedback> feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has a note'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    jobMeta.removeNote(0);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has a note' rule", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't expect any feedback from the 'job has no note' rule while disabled", feedback.isEmpty());
  }
}
