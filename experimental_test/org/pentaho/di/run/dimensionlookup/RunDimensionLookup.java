package org.pentaho.di.run.dimensionlookup;

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

public class RunDimensionLookup extends TestCase
{
    private static DatabaseMeta h2meta = new DatabaseMeta("H2 local", "H2", "JDBC", null, "experimental_test/testdata", null, null, null);

    private static void createIndex() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE DIM_CUSTOMER;");
            // System.out.println("Table DIM_CUSTOMER dropped");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }
        // System.out.println("Creating table DIM_CUSTOMER ...");
        
        h2db.execStatement(
                "CREATE TABLE DIM_CUSTOMER" + 
                "(" + 
                "    customer_tk IDENTITY" + 
                "    , version INTEGER" + 
                "    , date_from TIMESTAMP" + 
                "    , date_to TIMESTAMP" + 
                "    , id INTEGER" + 
                "    , name VARCHAR(30)" + 
                "    , firstname VARCHAR(30)" + 
                "    , zip INTEGER" + 
                "    , city VARCHAR(30)" + 
                "    , birthdate TIMESTAMP" + 
                "    , street VARCHAR(11)" + 
                "    , housenr INTEGER" + 
                "    , stateCode VARCHAR(9)" + 
                "    , state VARCHAR(30)" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_lookup" + 
                "     ON DIM_CUSTOMER" + 
                "    ( " + 
                "      id" + 
                "    )" + 
                "    ;" + Const.CR +
                "    CREATE INDEX idx_DIM_CUSTOMER_tk" + 
                "     ON DIM_CUSTOMER" + 
                "    ( " + 
                "      customer_tk" + 
                "    )" + 
                "    ;"
                );
        // System.out.println("Table DIM_CUSTOMER created.");

        h2db.disconnect();
    }

    /*
    private static void dropTable() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("DROP TABLE DIM_CUSTOMER;");
            System.out.println("Table DIM_CUSTOMER dropped");
        }
        catch(KettleDatabaseException e)
        {
            System.out.println("Table DIM_CUSTOMER not dropped : "+e.getMessage());
        }

        h2db.disconnect();
    }
    */

    private static void truncateDimensionTable() throws KettleDatabaseException
    {
        EnvUtil.environmentInit();
        
        Database h2db = new Database(h2meta);
        h2db.connect();
        try
        {
            h2db.execStatement("TRUNCATE TABLE DIM_CUSTOMER;");
            // System.out.println("Table DIM_CUSTOMER truncated");
        }
        catch(KettleDatabaseException e)
        {
            // System.out.println("Table DIM_CUSTOMER not truncated : "+e.getMessage());
        }

        h2db.disconnect();
    }

    private static double oldInitialLoadRuntime;
    private static double oldUpdate20kRunTime;
    private static double newInitialLoadRuntime;
    private static double newUpdate20kRunTime;
    
    public void test__DIMENSION_LOOKUP_00() throws KettleDatabaseException
    {
        System.out.println();
        System.out.println("DIMENSION LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DIMENSION_LOOKUP_01_InitialLoadOld() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupInitialLoad.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                250000
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine();

        oldInitialLoadRuntime = timedTransRunner.getOldRunTime();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
    }

    public void test__DIMENSION_LOOKUP_02_Update20kOld() throws KettleXMLException, KettleDatabaseException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/dimensionlookup/DimensionLookupUpdate20k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
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
