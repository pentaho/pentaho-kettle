package org.pentaho.di.run.update;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunUpdate extends RunTestCase
{
    private static void createIndex() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX idx_CSV_TABLE_lookup;", 
                "CREATE INDEX idx_CSV_TABLE_lookup ON CSV_TABLE(id);"
                );
    }

    public void test__UPDATE_00() throws Exception
    {
        System.out.println();
        System.out.println("UPDATE");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__UPDATE_01_SimpleTest() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/update/UpdateRowsSimple.ktr", 
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
    
    public void test__UPDATE_02_SimpleTestWithUpdates() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/update/UpdateRowsSimpleWithUpdates.ktr", 
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
}
