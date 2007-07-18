package org.pentaho.di.run.mergerows;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunMergeRows extends RunTestCase
{
    public void test_MERGE_ROWS_00()
    {
        System.out.println();
        System.out.println("MERGE ROWS");
        System.out.println("==================");
    }
    
    public void test_MERGE_ROWS_01_NoCondition() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/mergerows/MergeRows.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount/100
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
