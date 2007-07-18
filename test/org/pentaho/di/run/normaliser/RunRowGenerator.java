package org.pentaho.di.run.normaliser;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

public class RunRowGenerator extends TestCase
{
    public void test_ROWGENERATOR_00()
    {
        System.out.println();
        System.out.println("ROW NORMALISER");
        System.out.println("==================");
    }
    
    public void test_NORMALISER_01_Simple() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/normaliser/NormaliserSimple.ktr", 
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
