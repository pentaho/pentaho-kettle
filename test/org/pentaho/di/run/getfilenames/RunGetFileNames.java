package org.pentaho.di.run.getfilenames;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunGetFileNames extends RunTestCase
{
    public void test_FILTER_ROWS_00()
    {
        System.out.println();
        System.out.println("GET FILE NAMES");
        System.out.println("==================");
    }
    
    public void test_GET_FILE_NAMES_01_Simple() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/getfilenames/GetFileNames.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        assertTrue(oldResult.getNrLinesRead()==1);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
        assertTrue(newResult.getNrLinesRead()==1);
    }
}
