package org.pentaho.di.imp.rule;

import java.util.List;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.TransformationHasDescriptionImportRule;
import org.pentaho.di.trans.TransMeta;

import junit.framework.TestCase;

public class TransformationHasDescriptionImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    TransMeta transMeta = new TransMeta();
    transMeta.setDescription("This transformation is used for testing an import rule");
   
    PluginRegistry registry = PluginRegistry.getInstance();
    
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "TransformationHasDescription");
    assertNotNull("The 'transformation has description' rule could not be found in the plugin registry!", plugin);
    
    TransformationHasDescriptionImportRule rule = (TransformationHasDescriptionImportRule) registry.loadClass(plugin);
    assertNotNull("The 'transformation has description rule' class could not be loaded by the plugin registry!", plugin);

    rule.setMinLength(20);
    rule.setEnabled(true);

    List<ImportValidationFeedback> feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    rule.setMinLength(2000);
    rule.setEnabled(true);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't expect any feedback from the 'transformation has description rule' while disabled", feedback.isEmpty());
    
  }
}
