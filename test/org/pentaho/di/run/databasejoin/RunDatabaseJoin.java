package org.pentaho.di.run.databasejoin;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunDatabaseJoin extends RunTestCase
{
    private static void createIndex() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX IDX_CSV_TABLE_ID;", 
                "CREATE INDEX IDX_CSV_TABLE_ID ON CSV_TABLE(id);"
            );
    }

    private static void dropIndex() throws Exception
    {
        AllRunTests.executeStatementsOnOldAndNew(
                "DROP INDEX IDX_CSV_TABLE_ID;", 
                null
            );
    }

    public void test__DATABASE_JOIN_00() throws Exception
    {
        System.out.println();
        System.out.println("DATABASE JOIN");
        System.out.println("==================");
        System.out.println();
        createIndex();
    }
    
    public void test__DATABASE_JOIN_01_Simple() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/databasejoin/DBJoinSimple.ktr", 
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
    
    
    public void test__DATABASE_JOIN_99() throws Exception
    {
        System.out.println();
        dropIndex();
    }
}
