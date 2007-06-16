package org.pentaho.di.run.filterrows;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunFilterRows extends TestCase
{
    public void test_FILTER_ROWS_00()
    {
        System.out.println();
        System.out.println("FILTER ROWS");
        System.out.println("==================");
    }
    
    public void test_FILTER_ROWS_01_Simple() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/filterrows/FilterRowsSimple.ktr", 
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
