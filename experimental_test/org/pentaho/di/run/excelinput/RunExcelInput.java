package org.pentaho.di.run.excelinput;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunExcelInput extends TestCase
{
    public void test_EXCEL_INPUT_00()
    {
        System.out.println();
        System.out.println("EXCEL INPUT");
        System.out.println("==================");
    }
    
    public void test_EXCEL_INPUT_01() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/excelinput/ExcelInput.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                10000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
