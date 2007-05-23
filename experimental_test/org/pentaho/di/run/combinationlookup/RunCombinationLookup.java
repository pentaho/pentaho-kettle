package org.pentaho.di.run.combinationlookup;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunCombinationLookup extends TestCase
{
    private static DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);

    private static void createTables() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE COMB_NO_HASH;");
            // System.out.println("Table COMB_NO_HASH dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table COMB_NO_HASH not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table COMB_NO_HASH ...");
        
        h2db.execStatements(
                "CREATE TABLE COMB_NO_HASH" + 
                "(" + 
                "      comb_tk IDENTITY" + 
                "    , stateCode VARCHAR(9)" + 
                "    , state VARCHAR(30)" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE UNIQUE INDEX idx_COMB_NO_HASH_pk" + 
                "     ON COMB_NO_HASH" + 
                "    ( " + 
                "      comb_tk" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_COMB_NO_HASH_lookup" + 
                "     ON COMB_NO_HASH" + 
                "    ( " + 
                "      stateCode, state" + 
                "    )" + 
                "    ;"
                );
        // System.out.println("Table COMB_NO_HASH (re-)created.");

        
        try
        {
            h2db.execStatement("DROP TABLE COMB_HASH;");
            // System.out.println("Table COMB_HASH dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table COMB_HASH not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table COMB_HASH ...");
        
        h2db.execStatements(
                "CREATE TABLE COMB_HASH" + 
                "(" + 
                "      comb_tk IDENTITY" + 
                "    , stateCode VARCHAR(9)" + 
                "    , state VARCHAR(30)" + 
                "    , hashcode BIGINT" +
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE UNIQUE INDEX idx_COMB_HASH_pk" + 
                "     ON COMB_NO_HASH" + 
                "    ( " + 
                "      comb_tk" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_COMB_HASH_lookup" + 
                "     ON COMB_HASH" + 
                "    ( " + 
                "      hashcode" + 
                "    )" + 
                "    ;"
                );
        // System.out.println("Tables COMB_NO_HASH and COMB_HASH (re-)created.");

        h2db.disconnect();
    }
    
    public void test__DIMENSION_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("COMBINATION LOOKUP");
        System.out.println("==================");
        System.out.println();
    }
    
    public void test__COMBINATION_LOOKUP_01_NoHashcode_NoRemove() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.init();
        createTables();
        timedTransRunner.runOldEngine();
        createTables();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__COMBINATION_LOOKUP_02_NoHashcode_Remove() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.init();
        createTables();
        timedTransRunner.runOldEngine();
        createTables();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__COMBINATION_LOOKUP_03_Hashcode_NoRemove() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.init();
        createTables();
        timedTransRunner.runOldEngine();
        createTables();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__COMBINATION_LOOKUP_04_Hashcode_Remove() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.init();
        createTables();
        timedTransRunner.runOldEngine();
        createTables();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    /*
    public void test__COMBINATION_LOOKUP_99() throws KettleDatabaseException
    {
        System.out.println();
        dropTable();
    }
    */
}
