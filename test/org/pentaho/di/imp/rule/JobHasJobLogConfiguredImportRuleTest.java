package org.pentaho.di.imp.rule;

import java.util.List;

import junit.framework.TestCase;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.plugins.ImportRulePluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.imp.rules.JobHasJobLogConfiguredImportRule;
import org.pentaho.di.job.JobMeta;

public class JobHasJobLogConfiguredImportRuleTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    KettleEnvironment.init();
  }
  
  public void testRule() throws Exception {
    
    JobMeta jobMeta = new JobMeta();
    DatabaseMeta logDbMeta = new DatabaseMeta("LOGDB", "MYSQL", "JDBC", "localhost", "test", "3306", "foo", "bar");
    jobMeta.addDatabase(logDbMeta);
    JobLogTable logTable = jobMeta.getJobLogTable();
   
    PluginRegistry registry = PluginRegistry.getInstance();
    
    PluginInterface plugin = registry.findPluginWithId(ImportRulePluginType.class, "JobHasJobLogConfigured");
    assertNotNull("The 'job has job log table configured' rule could not be found in the plugin registry!", plugin);
    
    JobHasJobLogConfiguredImportRule rule = (JobHasJobLogConfiguredImportRule) registry.loadClass(plugin);
    assertNotNull("The 'job has job log table configured' class could not be loaded by the plugin registry!", plugin);

    rule.setEnabled(true);

    List<ImportValidationFeedback> feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has job log table configured'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    logTable.setTableName("SCHEMA");
    logTable.setTableName("LOGTABLE");
    logTable.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    // Make the rules stricter!
    //
    rule.setTableName("SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An approval ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.APPROVAL);

    // Break the rule
    //
    rule.setSchemaName("INCORRECT_SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    rule.setSchemaName("SCHEMA");
    rule.setTableName("INCORRECT_LOGTABLE");
    rule.setConnectionName(logDbMeta.getName());
    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);
    
    rule.setSchemaName("SCHEMA");
    rule.setTableName("LOGTABLE");
    rule.setConnectionName("INCORRECT_DATABASE");
    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't get any feedback from the 'job has description rule'", !feedback.isEmpty());
    assertTrue("An error ruling was expected", feedback.get(0).getResultType()==ImportValidationResultType.ERROR);

    // No feedback expected!
    //
    rule.setEnabled(false);

    feedback = rule.verifyRule(jobMeta);
    assertTrue("We didn't expect any feedback from the 'job has job log table configured' since the rule is not enabled", feedback.isEmpty());
  }
}
