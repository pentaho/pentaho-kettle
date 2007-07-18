package org.pentaho.di.run.abort;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunAbort extends RunTestCase
{
    public void test_ABORT_00()
    {
        System.out.println();
        System.out.println("ABORT");
        System.out.println("==================");
    }
    

    public void test_ABORT_01_AbortAboveLimit() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/abort/AbortAboveLimit.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==1);  // abort step must abort in this case
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==1);  // abort step must abort in this case

    }
    
    public void test_ABORT_02_AbortBelowLimit() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/abort/AbortBelowLimit.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1
            );
        assertTrue( timedTransRunner.runOldAndNew() );

        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);  // abort step must abort in this case
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);  // abort step must abort in this case
        
    }    
}