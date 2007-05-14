package org.pentaho.di.run.addsequence;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunAddSequence extends TestCase
{
    public void testAddSequenceToEmptyRow() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/addsequence/AddSequence.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void testAddSequenceTo5() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/addsequence/AddSequenceTo5.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                1000000
            );
        timedTransRunner.runOldAndNew();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void testAddSequenceTo10() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/addsequence/AddSequenceTo10.ktr", 
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
