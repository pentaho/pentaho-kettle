package org.pentaho.di.run;

import org.pentaho.di.core.trans.StepLoader;
import org.pentaho.di.core.trans.Trans;
import org.pentaho.di.core.trans.TransMeta;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunTableInput
{
    public static void main(String[] args) throws KettleXMLException
    {
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        TransMeta transMeta = new TransMeta("D:\\Temp\\TableInput.ktr");
        System.out.println("Name of transformation: "+transMeta.getName());
        
        long startTime = System.currentTimeMillis();
        
        // OK, now run this transFormation.
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.execute(null);
        
        trans.waitUntilFinished();
        
        long stopTime = System.currentTimeMillis();
        
        double seconds = (double)(stopTime - startTime) / 1000;
        long   records = 1110110L;
        double speed = (double)records / (seconds);
        
        System.out.println("records : "+records);
        System.out.println("runtime : "+seconds);
        System.out.println("speed   : "+speed);
    }
}
