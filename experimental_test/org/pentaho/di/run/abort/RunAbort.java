package org.pentaho.di.run.abort;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunAbort extends TestCase
{
    public void test_ABORT_00()
    {
        System.out.println();
        System.out.println("ABORT");
        System.out.println("==================");
    }
    

    public void test_ABORT_01_AbortAboveLimit() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/abort/AbortAboveLimit.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==1);  // abort step must abort in this case
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==1);  // abort step must abort in this case

    }
    
    public void test_ABORT_02_AbortBelowLimit() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/abort/AbortBelowLimit.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1
            );
        timedTransRunner.runOldAndNew();

        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);  // abort step must abort in this case
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);  // abort step must abort in this case
        
    }    
}