package org.pentaho.di.run.textfileoutput;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunTextFileInputCSV extends TestCase
{
    public void test_TEXT_FILE_OUTPUT_00()
    {
        System.out.println();
        System.out.println("TEXT FILE OUTPUT");
        System.out.println("==================");
    }
    
    public void test_TEXT_FILE_OUTPUT_01_InputOutputCSV() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/textfileoutput/TextFileInputOutputCSV.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_TEXT_FILE_OUTPUT_02_CSVFromTable() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/textfileoutput/TextFileOutputFromTable.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_TEXT_FILE_OUTPUT_03_CSVFromGenerator() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/textfileoutput/TextFileOutputFromGenerator.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                100000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
