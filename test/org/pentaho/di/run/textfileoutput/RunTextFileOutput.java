package org.pentaho.di.run.textfileoutput;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunTextFileOutput extends RunTestCase
{
    public void test_TEXT_FILE_OUTPUT_00()
    {
        System.out.println();
        System.out.println("TEXT FILE OUTPUT");
        System.out.println("==================");
    }
    
    public void test_TEXT_FILE_OUTPUT_01_InputOutputCSV() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/textfileoutput/TextFileInputOutputCSV.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_TEXT_FILE_OUTPUT_02_CSVFromTable() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/textfileoutput/TextFileOutputFromTable.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_TEXT_FILE_OUTPUT_03_CSVFromGenerator() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/textfileoutput/TextFileOutputFromGenerator.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000
            );
        
        timedTransRunner.init();
        timedTransRunner.runOldEngine();
        timedTransRunner.runNewEngine();
        timedTransRunner.compareResults();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
