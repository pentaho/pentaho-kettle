package org.pentaho.di.run.scriptvalues_mod;

import junit.framework.TestCase;

import org.pentaho.di.run.AllRunTests;
import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunScriptValuesMod extends TestCase
{
    public void test_SCRIPT_VALUES_MOD_00()
    {
        System.out.println();
        System.out.println("SCRIPT VALUES MOD");
        System.out.println("==================");
    }
    
    public void test_SCRIPT_VALUES_MOD_01_CSV() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/scriptvalues_mod/ScriptValuesMod1.ktr", 
                LogWriter.LOG_LEVEL_ERROR, 
                AllRunTests.getOldTargetDatabase(),
                AllRunTests.getNewTargetDatabase(),
                250000
            );
        timedTransRunner.init();
        timedTransRunner.runOldEngine(true);
        timedTransRunner.runNewEngine(false);
        timedTransRunner.compareResults();
        
        Result oldResult = timedTransRunner.getOldResult();
        assertTrue(oldResult.getNrErrors()==0);
        
        Result newResult = timedTransRunner.getNewResult();
        assertTrue(newResult.getNrErrors()==0);
    }
}
