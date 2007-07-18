package org.pentaho.di.run.excelinput;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunExcelInput extends RunTestCase
{
    public void test_EXCEL_INPUT_00()
    {
        System.out.println();
        System.out.println("EXCEL INPUT");
        System.out.println("==================");
    }
    
    public void test_EXCEL_INPUT_01() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/excelinput/ExcelInput.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                10000 // this is the number of rows in the spreadsheet
            );
        assertTrue( timedTransRunner.runOldAndNew() );
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
