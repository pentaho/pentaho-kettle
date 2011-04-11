package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.TransformationHasTransLogConfiguredImportRule;
import org.pentaho.di.trans.TransMeta;

public class TransformationHasTransLogConfiguredImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    TransMeta transMeta = new TransMeta();
    DatabaseMeta logDbMeta = new DatabaseMeta("LOGDB", "MYSQL", "JDBC", "localhost", "test", "3306", "foo", "bar");
    transMeta.addDatabase(logDbMeta);
    TransLogTable logTable = transMeta.getTransLogTable();
   
    PluginRegistry registry = PluginRegistry.getInstance();
    
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "TransformationHasTransLogConfigured");
    assertNotNull("The 'transformation has trans log table configured' rule could not be found in the plugin registry!", plugin);
    
    TransformationHasTransLogConfiguredImportRule rule = (TransformationHasTransLogConfiguredImportRule) registry.loadClass(plugin);
    assertNotNull("The 'transformation has trans log table configured' class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);

    List<ImportValidationFeedback> feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has trans log table configured'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    logTable.setTableName("SCHEMA");
    logTable.setTableName("LOGTABLE");
    logTable.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    // Make the rules stricter!
    //
    rule.setTableName("SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    // Break the rule
    //
    rule.setSchemaName("INCORRECT_SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setSchemaName("SCHEMA");
    rule.setTableName("INCORRECT_LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    
    rule.setSchemaName("SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName("INCORRECT_DATABASE");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    // No feedback expected!
    //
    rule.setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't expect any feedback from the 'transformation has trans log table configured' since the rule is not enabled", feedback.isEmpty());
  }
}
