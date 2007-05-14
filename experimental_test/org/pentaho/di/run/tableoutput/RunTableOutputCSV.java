package org.pentaho.di.run.tableoutput;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunTableOutputCSV extends TestCase
{
    public void testTableOutputFromCSV() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/tableoutput/TableOutputFromCSV.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public static void main(String[] args) throws KettleXMLException
    {
        new RunTableOutputCSV().testTableOutputFromCSV();
    }
}
