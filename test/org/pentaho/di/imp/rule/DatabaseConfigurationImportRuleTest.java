package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.DatabaseConfigurationImportRule;
import org.pentaho.di.trans.TransMeta;

public class DatabaseConfigurationImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {

    // Assemble a new database.
    //
    String DBNAME="test";
    String HOSTNAME="localhost";
    String PORT="3306";
    String USERNAME="foo";
    String PASSWORD="bar";
    DatabaseMeta verifyMeta = new DatabaseMeta("LOGDB", "MYSQL", "JDBC", HOSTNAME, DBNAME, PORT, USERNAME, PASSWORD);
    
    // Create a transformation to test.
    //
    TransMeta transMeta = new TransMeta();
    transMeta.addDatabase((DatabaseMeta)verifyMeta.clone());
   
    // Load the plugin to test from the registry.
    //
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "DatabaseConfiguration");
    assertNotNull("The 'database configuration' rule could not be found in the plugin registry!", plugin);
    DatabaseConfigurationImportRule rule = (DatabaseConfigurationImportRule) registry.loadClass(plugin);
    assertNotNull("The 'database configuration' class could not be loaded by the plugin registry!", plugin);

    // Set the appropriate rule..
    //
    rule.setEnabled(true);
    
    List<ImportValidationFeedback> feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'database configuration'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setDatabaseMeta(verifyMeta);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    // Create some errors...
    //
    verifyMeta.setDBName("incorrect-test");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected validating the db name", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    verifyMeta.setDBName(DBNAME);

    verifyMeta.setHostname("incorrect-hostname");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected validating the db hostname", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    verifyMeta.setHostname(HOSTNAME);
    
    verifyMeta.setDBPort("incorrect-port");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected validating the db port", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    verifyMeta.setDBPort(PORT);

    verifyMeta.setUsername("incorrect-username");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected validating the db username", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    verifyMeta.setUsername(USERNAME);

    verifyMeta.setPassword("incorrect-password");
    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't get any feedback from the 'transformation has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected validating the db password", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    verifyMeta.setPassword(PASSWORD);

    // No feedback expected!
    //
    rule.setEnabled(false);

    feedback = rule.verifyRule(transMeta);
    assertTrue("We didn't expect any feedback from the 'transformation has trans log table configured' since disabled", feedback.isEmpty());
  }
}
