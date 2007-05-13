package org.pentaho.di.run.calculator;

import junit.framework.TestCase;

import org.pentaho.di.run.TimedTransRunner;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleXMLException;

public class RunCalculate_A_plus_B extends TestCase
{
    public void testRun() throws KettleXMLException
    {
        TimedTransRunner timedTransRunner = new TimedTransRunner(
                "experimental_test/org/pentaho/di/run/calculator/Calculate_A_plus_B.ktr", 
                LogWriter.LOG_LEVEL_NOTHING, 
                1000000
            );
        timedTransRunner.runOldAndNew();
    }
}
