package org.pentaho.di.run.abort;

import junit.framework.TestCase;

import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunAbort extends TestCase
{
    public void testAbortAboveLimit() throws KettleXMLException
    {
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_ERROR);
        
        TransMeta transMeta = new TransMeta("experimental_test/org/pentaho/di/run/abort/AbortAboveLimit.ktr");
        System.out.println("Name of transformation: "+transMeta.getName());
        System.out.println("Transformation description: "+Const.NVL(transMeta.getDescription(), ""));
        
        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        Result result = trans.getResult();
        assertTrue(result.getNrErrors()==1);  // abort step must abort in this case
        
        long stopTime = System.currentTimeMillis();
        
        double seconds = (double)(stopTime - startTime) / 1000;
        long   records = result.getNrLinesRead();
        double speed = (double)records / (seconds);
        
        System.out.println("records : "+records);
        System.out.println("runtime : "+seconds);
        System.out.println("speed   : "+speed);
    }
    
    public void testAbortBelowLimit() throws KettleXMLException
    {
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_ERROR);
        
        TransMeta transMeta = new TransMeta("experimental_test/org/pentaho/di/run/abort/AbortBelowLimit.ktr");
        System.out.println("Name of transformation: "+transMeta.getName());
        System.out.println("Transformation description: "+Const.NVL(transMeta.getDescription(), ""));
        
        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        Result result = trans.getResult();
        assertTrue(result.getNrErrors()==0);
        
        long stopTime = System.currentTimeMillis();
        
        double seconds = (double)(stopTime - startTime) / 1000;
        long   records = result.getNrLinesRead();
        double speed = (double)records / (seconds);
        
        System.out.println("records : "+records);
        System.out.println("runtime : "+seconds);
        System.out.println("speed   : "+speed);
    }    
}