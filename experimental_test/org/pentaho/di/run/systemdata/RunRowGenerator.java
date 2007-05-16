package org.pentaho.di.run.systemdata;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunRowGenerator extends TestCase
{
    public void testGetSystemInfoSysdate() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/systemdata/GetSystemInfoSysdate.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void testGetSystemInfoSysdateTempfile() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/systemdata/GetSystemInfoSysdateOutputToTempfile.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
