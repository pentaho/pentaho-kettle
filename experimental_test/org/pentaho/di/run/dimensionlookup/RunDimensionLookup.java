package org.pentaho.di.run.dimensionlookup;

import junit.framework.TestCase;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunDimensionLookup extends TestCase
{
    private static void createIndex() throws KettleException
    {
        EnvUtil.environmentInit();
        
        Database target = new Database(AllRunTests.getNewTargetDatabase());
        target.connect();
        try
        {
            target.execStatements("DROP TABLE DIM_CUSTOMER;");
            // System.out.println("Table DIM_CUSTOMER dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table DIM_CUSTOMER ...");
        
        target.execStatements(
                "CREATE TABLE DIM_CUSTOMER" + Const.CR + 
                "(" +  Const.CR + 
                "    customer_tk BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1)" +  Const.CR + 
                "    , version INTEGER" +  Const.CR + 
                "    , date_from TIMESTAMP" +  Const.CR + 
                "    , date_to TIMESTAMP" +  Const.CR + 
                "    , id INTEGER" +  Const.CR + 
                "    , name VARCHAR(30)" +  Const.CR + 
                "    , firstname VARCHAR(30)" +  Const.CR + 
                "    , zip INTEGER" +  Const.CR + 
                "    , city VARCHAR(30)" +  Const.CR + 
                "    , birthdate TIMESTAMP" +  Const.CR + 
                "    , street VARCHAR(11)" +  Const.CR + 
                "    , housenr INTEGER" +  Const.CR + 
                "    , stateCode VARCHAR(9)" +  Const.CR + 
                "    , state VARCHAR(30)" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_lookup" +  Const.CR + 
                "     ON DIM_CUSTOMER" +  Const.CR + 
                "    ( " +  Const.CR + 
                "      id" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_tk" +  Const.CR + 
                "     ON DIM_CUSTOMER" +  Const.CR + 
                "    ( " +  Const.CR + 
                "      customer_tk" +  Const.CR + 
                "    )" +  Const.CR + 
                "    ;"
                );
        // System.out.println("Table DIM_CUSTOMER created.");

        target.disconnect();
    }

    private static void truncateDimensionTable() throws KettleException
    {
        EnvUtil.environmentInit();
        
        Database target = new Database(AllRunTests.getNewTargetDatabase());
        target.connect();
        try
        {
            target.execStatements("TRUNCATE TABLE DIM_CUSTOMER;");
            // System.out.println("Table DIM_CUSTOMER truncated");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table DIM_CUSTOMER not truncated : "+e.getMessage());
        }

        target.disconnect();
    }

    private static double oldInitialLoadRuntime;
    private static double oldUpdate20kRunTime;
    private static double newInitialLoadRuntime;
    private static double newUpdate20kRunTime;
    
    public void test__DIMENSION_LOOKUP_00() throws KettleException
    {
        System.out.println();
        System.out.println("DIMENSION LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DIMENSION_LOOKUP_01_InitialLoadOld() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupInitialLoad.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine();

        oldInitialLoadRuntime = timedTransRunner.getOldRunTime();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_02_Update20kOld() throws KettleException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupUpdate20k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                20000
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine();

        oldUpdate20kRunTime = timedTransRunner.getOldRunTime();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        truncateDimensionTable();
    }

    public void test__DIMENSION_LOOKUP_03_InitialLoadNew() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupInitialLoad.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.init();
        timedTransRunner.runNewEngine(true);

        newInitialLoadRuntime = timedTransRunner.getNewRunTime();
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
        
        // Report on the speed increase...
        TimedTransRunner.compareResults(oldInitialLoadRuntime, newInitialLoadRuntime);
    }
    
    public void test__DIMENSION_LOOKUP_04_Update20kNew() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupUpdate20k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                20000
            );
        timedTransRunner.init();
        timedTransRunner.runNewEngine(true);
        
        newUpdate20kRunTime = timedTransRunner.getNewRunTime();

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
        
        // Report on the speed increase...
        TimedTransRunner.compareResults(oldUpdate20kRunTime, newUpdate20kRunTime);
    }

    public void test__DIMENSION_LOOKUP_05_TkLookupCacheOff() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCacheOff.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DIMENSION_LOOKUP_06_TkLookup() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookup.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DIMENSION_LOOKUP_07_TkLookupCache25k() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCache25k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_08_TkLookupCache50k() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCache50k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_09_TkLookupCacheAll() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupTKLookupCacheAll.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    /*
    public void test__DIMENSION_LOOKUP_99() throws KettleDatabaseException
    {
        System.out.println();
        dropTable();
    }
    */
}
