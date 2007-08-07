package org.pentaho.di.trans.steps.mapping;

import junit.framework.TestCase;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

public class RunMapping extends TestCase
{
    public void test_MAPPING()
    {
        System.out.println();
        System.out.println("MAPPING");
        System.out.println("==================");
    }
    
    public void test_MAPPING_INPUT_ONLY() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/filereader/use filereader.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1000
            );
        timedTransRunner.init();
        assertTrue( timedTransRunner.runNewEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_MAPPING_OUTPUT_ONLY() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/filewriter/use filewriter.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1000
            );
        timedTransRunner.init();
        assertTrue( timedTransRunner.runNewEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_MAPPING_MULTI_OUTPUT() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/trans/steps/mapping/multi_output/use filereader.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                1000
            );
        timedTransRunner.init();
        assertTrue( timedTransRunner.runNewEngine(true) );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
