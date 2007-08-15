package org.pentaho.di.run;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.run.abort.RunAbort;
import org.pentaho.di.run.accessoutput.RunAccessOutput;
import org.pentaho.di.run.addsequence.RunAddSequence;
import org.pentaho.di.run.calculator.RunCalculator;
import org.pentaho.di.run.combinationlookup.RunCombinationLookup;
import org.pentaho.di.run.constant.RunConstant;
import org.pentaho.di.run.databasejoin.RunDatabaseJoin;
import org.pentaho.di.run.databaselookup.RunDatabaseLookup;
import org.pentaho.di.run.delete.RunDelete;
import org.pentaho.di.run.denormaliser.RunDenormaliser;
import org.pentaho.di.run.dimensionlookup.RunDimensionLookup;
import org.pentaho.di.run.excelinput.RunExcelInput;
import org.pentaho.di.run.exceloutput.RunExcelOutput;
import org.pentaho.di.run.filterrows.RunFilterRows;
import org.pentaho.di.run.getfilenames.RunGetFileNames;
import org.pentaho.di.run.groupby.RunGroupBy;
import org.pentaho.di.run.insertupdate.RunInsertUpdate;
import org.pentaho.di.run.joinrows.RunJoinRows;
import org.pentaho.di.run.mergejoin.RunMergeJoin;
import org.pentaho.di.run.rowgenerator.RunRowGenerator;
import org.pentaho.di.run.scriptvalues_mod.RunScriptValuesMod;
import org.pentaho.di.run.selectvalues.RunSelectValues;
import org.pentaho.di.run.sort.RunSortRows;
import org.pentaho.di.run.sortedmerge.RunSortedMerge;
import org.pentaho.di.run.streamlookup.RunStreamLookup;
import org.pentaho.di.run.systemdata.RunSystemData;
import org.pentaho.di.run.tableinput.RunTableInput;
import org.pentaho.di.run.tableoutput.RunTableOutput;
import org.pentaho.di.run.textfileinput.RunTextFileInput;
import org.pentaho.di.run.textfileoutput.RunTextFileOutput;
import org.pentaho.di.run.uniquerows.RunUniqueRows;
import org.pentaho.di.run.update.RunUpdate;

public class AllRunTests
{
    private static String OLD_TARGET_CONNECTION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
        "<connection>"+
        "  <name>TARGET</name>"+
        "  <server/>"+
        "  <type>GENERIC</type>"+
        "  <access>Native</access>"+
        "  <database>&#47;test</database>"+
        "  <port/>"+
        "  <username/>"+
        "  <password>Encrypted </password>"+
        "  <servername/>"+
        "  <data_tablespace/>"+
        "  <index_tablespace/>"+
        "  <attributes>"+
        "    <attribute><code>CUSTOM_DRIVER_CLASS</code><attribute>org.apache.derby.jdbc.EmbeddedDriver</attribute></attribute>"+
        "    <attribute><code>CUSTOM_URL</code><attribute>jdbc:derby:test&#47;derbyOld;create=true</attribute></attribute>"+
        "  </attributes>"+
        "</connection>";

    private static String NEW_TARGET_CONNECTION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+
        "<connection>"+
        "  <name>TARGET</name>"+
        "  <server/>"+
        "  <type>GENERIC</type>"+
        "  <access>Native</access>"+
        "  <database>&#47;test</database>"+
        "  <port/>"+
        "  <username/>"+
        "  <password>Encrypted </password>"+
        "  <servername/>"+
        "  <data_tablespace/>"+
        "  <index_tablespace/>"+
        "  <attributes>"+
        "    <attribute><code>CUSTOM_DRIVER_CLASS</code><attribute>org.apache.derby.jdbc.EmbeddedDriver</attribute></attribute>"+
        "    <attribute><code>CUSTOM_URL</code><attribute>jdbc:derby:test&#47;derbyNew;create=true</attribute></attribute>"+
        "  </attributes>"+
        "</connection>";

    public static DatabaseMeta getNewTargetDatabase() throws KettleXMLException
    {
        return new DatabaseMeta(NEW_TARGET_CONNECTION_XML);
    }

    public static be.ibridge.kettle.core.database.DatabaseMeta getOldTargetDatabase() throws be.ibridge.kettle.core.exception.KettleXMLException
    {
        return new be.ibridge.kettle.core.database.DatabaseMeta(OLD_TARGET_CONNECTION_XML);
    }

    public static void executeStatementsOnOldAndNew(String ignoreErrorStatements, String statements) throws Exception
    {
        executeStatementsOnOld(ignoreErrorStatements, statements);
        executeStatementsOnNew(ignoreErrorStatements, statements);
    }

    public static void executeStatementsOnOld(String ignoreErrorStatements, String statements) throws Exception
    {
        be.ibridge.kettle.core.util.EnvUtil.environmentInit();
        be.ibridge.kettle.core.database.Database target = new be.ibridge.kettle.core.database.Database(getOldTargetDatabase());
        target.connect();
        
        if (!Const.isEmpty(ignoreErrorStatements))
        {
            try
            {
                target.execStatements(ignoreErrorStatements);
                // System.out.println("Table CSV_TABLE dropped");
            }
            catch(be.ibridge.kettle.core.exception.KettleDatabaseException e)
            {
                // System.out.println("Error running 'ingore error' statements: "+ignoreErrorStatements+":"+e.getMessage());
            }
        }

        if (!Const.isEmpty(statements))
        {
            target.execStatements(statements);
        }
        
        target.disconnect();
    }

    public static void executeStatementsOnNew(String ignoreErrorStatements, String statements) throws KettleException
    {
        EnvUtil.environmentInit();
        Database target = new Database(getNewTargetDatabase());
        target.connect();
        
        if (!Const.isEmpty(ignoreErrorStatements))
        {
            try
            {
                target.execStatements(ignoreErrorStatements);
                // System.out.println("Table CSV_TABLE dropped");
            }
            catch(KettleDatabaseException e)
            {
                // System.out.println("Error running 'ingore error' statements on new target database: "+ignoreErrorStatements+":"+e.getMessage());
            }
        }

        if (!Const.isEmpty(statements))
        {
            target.execStatements(statements);
        }
        
        target.disconnect();
    }
    
    public static Test suite() throws KettleException
    {
        TestSuite suite = new TestSuite("Run performance tests");
        //$JUnit-BEGIN$
        
        suite.addTestSuite(RunTableOutput.class);
        suite.addTestSuite(RunAbort.class);
        suite.addTestSuite(RunAddSequence.class);
        suite.addTestSuite(RunCalculator.class);
        suite.addTestSuite(RunConstant.class);
        suite.addTestSuite(RunFilterRows.class);
        suite.addTestSuite(RunRowGenerator.class);
        suite.addTestSuite(RunSelectValues.class);
        suite.addTestSuite(RunSortRows.class);
        suite.addTestSuite(RunStreamLookup.class);
        suite.addTestSuite(RunSystemData.class);
        suite.addTestSuite(RunTableInput.class);
        suite.addTestSuite(RunTextFileInput.class);
        suite.addTestSuite(RunTextFileOutput.class);
        suite.addTestSuite(RunUniqueRows.class);
        suite.addTestSuite(RunDatabaseLookup.class);
        suite.addTestSuite(RunDatabaseJoin.class);
        suite.addTestSuite(RunDimensionLookup.class);
        suite.addTestSuite(RunExcelInput.class);
        suite.addTestSuite(RunCombinationLookup.class);
        suite.addTestSuite(RunScriptValuesMod.class);
        suite.addTestSuite(RunJoinRows.class);
        suite.addTestSuite(RunUpdate.class);
        suite.addTestSuite(RunInsertUpdate.class);
        suite.addTestSuite(RunDelete.class);
        suite.addTestSuite(RunGetFileNames.class);
        suite.addTestSuite(RunGroupBy.class);
        suite.addTestSuite(RunMergeJoin.class);
        suite.addTestSuite(RunSortedMerge.class);
        suite.addTestSuite(RunExcelOutput.class);
        suite.addTestSuite(RunDenormaliser.class);
        suite.addTestSuite(RunAccessOutput.class);
        
        //$JUnit-END$
        return suite;
    }

}
