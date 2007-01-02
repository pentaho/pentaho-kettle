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
        if (args.length==0)
        {
            System.err.println("Usage: Test3692   <transformation file>   <nr of iterations>");
            return;
        }
        // init...
        EnvUtil.environmentInit();
        StepLoader.getInstance().read();
        JobEntryLoader.getInstance().read();
        LogWriter log = LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        TransMeta transMeta = new TransMeta(args[0]);
        
        int iterations = 10000;
        if (args.length>1) Integer.parseInt(args[1]);
        Trans trans = new Trans(log, transMeta);
        for (int i=0;i<iterations;i++)
        {
            trans.execute(null);
        }
    }
}
