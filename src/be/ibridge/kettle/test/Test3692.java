package be.ibridge.kettle.test;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.job.JobEntryLoader;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;

public class Test3692
{
    public static void main(String[] args) throws KettleXMLException
    {
        // init...
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        JobEntryLoader.getInstance().read();
        LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        TransMeta transMeta = new TransMeta(args[0]);
        for (int i=0;i<1000;i++)
        {
            Trans trans = new Trans(log, transMeta);
            trans.execute(null);
        }
    }
}
