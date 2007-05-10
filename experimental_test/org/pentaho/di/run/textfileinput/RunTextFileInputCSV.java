package org.pentaho.di.run.textfileinput;

import junit.framework.TestCase;

import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunTextFileInputCSV extends TestCase
{
    public void testTableInput() throws KettleXMLException
    {
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        TransMeta transMeta = new TransMeta("experimental_test/org/pentaho/di/run/textfileinput/TextFileInputCSV.ktr");
        System.out.println("Name of transformation: "+transMeta.getName());
        System.out.println("Transformation description: "+Const.NVL(transMeta.getDescription(), ""));

        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        Result result = trans.getResult();
        
        assertTrue(result.getNrErrors()==0);
        assertEquals(result.getNrLinesRead(), 1000000);
        
        long stopTime = System.currentTimeMillis();
        
        double seconds = (double)(stopTime - startTime) / 1000;
        long   records = result.getNrLinesRead();
        double speed = (double)records / (seconds);
        
        System.out.println("records : "+records);
        System.out.println("runtime : "+seconds);
        System.out.println("speed   : "+speed);
    }
}
