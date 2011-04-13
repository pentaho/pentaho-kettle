package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.TransformationHasANoteImportRule;
import org.pentaho.di.trans.TransMeta;

public class TransformationHasANoteImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    // Create a transformation to test.
    //
    TransMeta transMeta = new TransMeta();
    NotePadMeta note = new NotePadMeta("A note documenting the transformation", 50, 50, 200, 50);
    transMeta.addNote(note);

    // Load the plugin to test from the registry.
    //
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "TransformationHasANote");
    assertNotNull("The 'transformation has a note' rule could not be found in the plugin registry!", plugin);
  
    TransformationHasANoteImportRule rule = (TransformationHasANoteImportRule) registry.loadClass(plugin);
    assertNotNull("The 'transformation has a note' class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);
    
    List<ImportValidationFeedback> feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has a note' rule", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    transMeta.removeNote(0);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has a note' rule", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't expect any feedback from the 'transformation has a note' rule while disabled", feedback.isEmpty());
    
  }
}
