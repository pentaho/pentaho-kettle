package org.pentaho.di.run.databaselookup;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunDatabaseLookup extends TestCase
{
    private static void createIndex() throws KettleException
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX IDX_CSV_TABLE_ID;", 
                "CREATE INDEX IDX_CSV_TABLE_ID ON CSV_TABLE(id);"
            );
    }

    private static void dropIndex() throws KettleException
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX IDX_CSV_TABLE_ID;", 
                null
            );
    }

    public void test__DATABASE_LOOKUP_00() throws KettleException
    {
        System.out.println();
        System.out.println("DATABASE LOOKUP");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DATABASE_LOOKUP_01_Simple() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/databaselookup/DBLookupIDLookup.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DATABASE_LOOKUP_02_NoCache() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/databaselookup/DBLookupIDLookupNoCaching.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DATABASE_LOOKUP_03_Cache50k() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/databaselookup/DBLookupIDLookupCache50k.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test__DATABASE_LOOKUP_99() throws KettleException
    {
        System.out.println();
        dropIndex();
    }
}
