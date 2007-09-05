package org.pentaho.di.run.combinationlookup;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunCombinationLookup extends RunTestCase
{
    private static void createTables() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE COMB_NO_HASH", 
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
        
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP TABLE COMB_HASH;",
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
    }
    
    public void test__DIMENSION_LOOKUP_00() throws Exception
    {
        System.out.println();
        System.out.println("COMBINATION LOOKUP");
        System.out.println("==================");
        System.out.println();
        createTables();
    }
    
    public void test__COMBINATION_LOOKUP_01_NoHashcode_NoRemove() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );

        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__COMBINATION_LOOKUP_02_NoHashcode_Remove() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );

        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__COMBINATION_LOOKUP_03_Hashcode_NoRemove() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcode.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );

        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__COMBINATION_LOOKUP_04_Hashcode_Remove() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/combinationlookup/CombinationLookupNoHashcodeRemove.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        assertTrue( timedTransRunner.runOldAndNew() );

        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
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
