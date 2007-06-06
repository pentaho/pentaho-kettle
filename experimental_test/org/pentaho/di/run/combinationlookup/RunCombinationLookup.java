package org.pentaho.di.run.combinationlookup;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunCombinationLookup extends TestCase
{
    private static void createTables() throws KettleException
    {
        EnvUtil.environmentInit();
        
        Database target = new Database(AllRunTests.getNewTargetDatabase());
        target.connect();
        try
        {
            target.execStatements("DROP TABLE COMB_NO_HASH;");
            // System.out.println("Table COMB_NO_HASH dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table COMB_NO_HASH not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table COMB_NO_HASH ...");
        
        target.execStatements(
                "CREATE TABLE COMB_NO_HASH" + Const.CR + 
                "(" + Const.CR + 
                "      comb_tk BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)" + Const.CR + 
                "    , stateCode VARCHAR(9)" + Const.CR + 
                "    , state VARCHAR(30)" + Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR +
                "    CREATE UNIQUE INDEX idx_COMB_NO_HASH_pk" + Const.CR + 
                "     ON COMB_NO_HASH" + Const.CR + 
                "    ( " + Const.CR + 
                "      comb_tk" + Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_COMB_NO_HASH_lookup" + Const.CR + 
                "     ON COMB_NO_HASH" + Const.CR + 
                "    ( " + Const.CR + 
                "      stateCode, state" + Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR 
                );
        // System.out.println("Table COMB_NO_HASH (re-)created.");

        
        try
        {
            target.execStatements("DROP TABLE COMB_HASH;");
            // System.out.println("Table COMB_HASH dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table COMB_HASH not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table COMB_HASH ...");
        
        target.execStatements(
                "CREATE TABLE COMB_HASH" + Const.CR + 
                "(" + Const.CR + 
                "      comb_tk BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)" + Const.CR + 
                "    , stateCode VARCHAR(9)" + Const.CR + 
                "    , state VARCHAR(30)" + Const.CR + 
                "    , hashcode BIGINT" +Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR +
                "    CREATE UNIQUE INDEX idx_COMB_HASH_pk" + Const.CR + 
                "     ON COMB_NO_HASH" + Const.CR + 
                "    ( " + Const.CR + 
                "      comb_tk" + Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_COMB_HASH_lookup" + Const.CR + 
                "     ON COMB_HASH" + Const.CR + 
                "    ( " + Const.CR + 
                "      hashcode" + Const.CR + 
                "    )" + Const.CR + 
                "    ;" + Const.CR 
                );
        // System.out.println("Tables COMB_NO_HASH and COMB_HASH (re-)created.");

        target.disconnect();
    }
    
    public void test__DIMENSION_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("COMBINATION LOOKUP");
        System.out.println("==================");
        System.out.println();
    }
    
    public void test__COMBINATION_LOOKUP_01_NoHashcode_NoRemove() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
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

    public void test__COMBINATION_LOOKUP_02_NoHashcode_Remove() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
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
    
    public void test__COMBINATION_LOOKUP_03_Hashcode_NoRemove() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
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

    public void test__COMBINATION_LOOKUP_04_Hashcode_Remove() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
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
