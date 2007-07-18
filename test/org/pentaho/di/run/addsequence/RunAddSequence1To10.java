package org.pentaho.di.run.addsequence;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunAddSequence1To10 extends RunTestCase
{
    public void test_ADD_SEQUENCE_03_AddTo10() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/addsequence/AddSequenceTo10.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*10
        );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
