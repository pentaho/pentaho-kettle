package org.pentaho.di.run.denormaliser;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

public class RunDenormaliser extends TestCase
{
    public void test_DENORMALISER_00()
    {
        System.out.println();
        System.out.println("DE-NORMALISER");
        System.out.println("==================");
    }
    
    public void test_DENORMALISER_01_MULTIKEY() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/denormaliser/DenormaliserMultiKey.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }

}
