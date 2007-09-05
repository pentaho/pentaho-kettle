package org.pentaho.di.run.scriptvalues_mod;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.RunTestCase;
import org.pentaho.di.run.TimedTransRunner;

public class RunScriptValuesMod extends RunTestCase
{
    public void test_SCRIPT_VALUES_MOD_00()
    {
        System.out.println();
        System.out.println("SCRIPT VALUES MOD");
        System.out.println("==================");
    }
    
    public void test_SCRIPT_VALUES_MOD_01_CSV() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/scriptvalues_mod/ScriptValuesMod1.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000*5
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine(true);
        boolean ok = timedTransRunner.runNewEngine(true);
        assertTrue( ok );
        timedTransRunner.compareResults();
        
        be.ibridge.kettle.core.Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
    
    public void test_SCRIPT_VALUES_MOD_02_CSV() throws Exception
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "test/org/pentaho/di/run/scriptvalues_mod/ScriptValuesMod2.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                rowCount*1000*5
            );
        timedTransRunner.init();
        boolean ok = timedTransRunner.runNewEngine(true);
        assertTrue( ok );
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
