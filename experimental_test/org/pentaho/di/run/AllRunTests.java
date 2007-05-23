package org.pentaho.di.run;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.run.abort.RunAbort;
import org.pentaho.di.run.addsequence.RunAddSequence;
import org.pentaho.di.run.calculator.RunCalculator;
import org.pentaho.di.run.combinationlookup.RunCombinationLookup;
import org.pentaho.di.run.constant.RunConstant;
import org.pentaho.di.run.databaselookup.RunDatabaseLookup;
import org.pentaho.di.run.dimensionlookup.RunDimensionLookup;
import org.pentaho.di.run.excelinput.RunExcelInput;
import org.pentaho.di.run.filterrows.RunFilterRows;
import org.pentaho.di.run.rowgenerator.RunRowGenerator;
import org.pentaho.di.run.scriptvalues_mod.RunScriptValuesMod;
import org.pentaho.di.run.selectvalues.RunSelectValues;
import org.pentaho.di.run.sort.RunSortRows;
import org.pentaho.di.run.streamlookup.RunStreamLookup;
import org.pentaho.di.run.systemdata.RunSystemData;
import org.pentaho.di.run.tableinput.RunTableInput;
import org.pentaho.di.run.tableoutput.RunTableOutput;
import org.pentaho.di.run.textfileinput.RunTextFileInput;
import org.pentaho.di.run.textfileoutput.RunTextFileOutput;
import org.pentaho.di.run.uniquerows.RunUniqueRows;

import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.util.EnvUtil;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllRunTests
{
    public static DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);
    
    private static void createTables() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE CSV_TABLE;");
            // System.out.println("Table CSV_TABLE dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table CSV_TABLE not dropped: "+e.getMessage());
        }
        h2db.execStatement(
                "CREATE TABLE CSV_TABLE"+
                "("+
                "  id INTEGER"+
                ", name VARCHAR(9)"+
                ", firstname VARCHAR(13)"+
                ", zip INTEGER"+
                ", city VARCHAR(8)"+
                ", birthdate TIMESTAMP"+
                ", street VARCHAR(11)"+
                ", housenr SMALLINT"+
                ", stateCode VARCHAR(9)"+
                ", state VARCHAR(30)"+
                ")"+
                ";");
        // System.out.println("Table CSV_TABLE created.");

        try
        {
            h2db.execStatement("DROP TABLE SIMPLE_TABLE;");
            // System.out.println("Table SIMPLE_TABLE dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table SIMPLE_TABLE not dropped: "+e.getMessage());
        }
        h2db.execStatement(
                "CREATE TABLE SIMPLE_TABLE"+
                "("+
                "  stringField VARCHAR(30)"+
                ", dateField TIMESTAMP"+
                ", boolField CHAR(1)"+
                ", numField DOUBLE"+
                ", intField DOUBLE"+
                ", id INTEGER"+
                ")"+
                ";");
        // System.out.println("Table SIMPLE_TABLE created");
        h2db.disconnect();
    }
    
    public static Test suite() throws KettleDatabaseException
    {
        createTables();
        
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
        suite.addTestSuite(RunDimensionLookup.class);
        suite.addTestSuite(RunExcelInput.class);
        suite.addTestSuite(RunCombinationLookup.class);
        suite.addTestSuite(RunScriptValuesMod.class);
        
        //$JUnit-END$
        return suite;
    }

}
