package org.pentaho.di.run.tableinput;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunTableInput extends TestCase
{
    public void test_TABLE_INPUT_00()
    {
        System.out.println();
        System.out.println("TABLE INPUT");
        System.out.println("==================");
    }
    
    public void test_TABLE_INPUT_01_ReadCustomers() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/tableinput/TableInput.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1110110
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
