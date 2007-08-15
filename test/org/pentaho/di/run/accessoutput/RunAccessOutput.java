package org.pentaho.di.run.accessoutput;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunAccessOutput extends RunTestCase
{
    public void test_ACCESS_OUTPUT_00()
    {
        System.out.println();
        System.out.println("ACCESS_OUTPUT");
        System.out.println("==================");
    }
    
    public void test_ACCESS_OUTPUT_01_SIMPLE() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/accessoutput/AccessOutputSimple.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                100000 // Fixed: size of file.
            );
        
        timedTransRunner.init();
        
        assertTrue( timedTransRunner.runOldEngine(true) );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        deleteOldResultFiles(oldResult, new String[] { ".mdb" } );

        assertTrue( timedTransRunner.runNewEngine(false) );

        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
        
        deleteNewResultFiles(newResult, new String[] { ".mdb" });
        
        timedTransRunner.compareResults();
    }
}
