package org.pentaho.di.run.selectvalues;

import junit.framework.TestCase;

import org.pentaho.di.trans.StepLoader;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;

public class RunSelectValuesDelete extends TestCase
{
    public void testSelectValuesDelete() throws KettleXMLException
    {
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_ERROR);
        
        TransMeta transMeta = new TransMeta("experimental_test/org/pentaho/di/run/selectvalues/SelectValuesDelete.ktr");
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
        
        RowGeneratorMeta rowGeneratorMeta = (RowGeneratorMeta) transMeta.findStep("Generate Rows").getStepMetaInterface();
        
        double seconds = (double)(stopTime - startTime) / 1000;
        long   records = Long.parseLong( rowGeneratorMeta.getRowLimit() );
        double speed = (double)records / (seconds);
        
        System.out.println("records : "+records);
        System.out.println("runtime : "+seconds);
        System.out.println("speed   : "+speed);
    }
}
