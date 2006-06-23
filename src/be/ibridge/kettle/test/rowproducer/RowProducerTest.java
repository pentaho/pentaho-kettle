package be.ibridge.kettle.test.rowproducer;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.RowProducer;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.step.injector.InjectorMeta;

public class RowProducerTest 
{
    private static final String STEPNAME_STARTPOINT = "Start point";
    private static final int    NR_ROWS             = 150000;
    
    public static void main(String[] args) throws KettleException
    {
        // Init stuff...
        EnvUtil.environmentInit();
        LogWriter log = LogWriter.getInstance();
        StepLoader stepLoader = StepLoader.getInstance();
        if (!stepLoader.read())
        {
            throw new RuntimeException("Unable to load steps and plugins");
        }
        
        Row row = createRow();
        // 
        InjectorMeta startMeta = new InjectorMeta();
        startMeta.allocate(row.size());
        for (int i=0;i<row.size();i++)
        {
            Value v = row.getValue(i);
            startMeta.getName()[i]      = v.getName();
            startMeta.getType()[i]      = v.getType();
            startMeta.getLength()[i]    = v.getLength();
            startMeta.getPrecision()[i] = v.getPrecision();
        }
        
        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(startMeta, STEPNAME_STARTPOINT);
        
        Trans trans = new Trans(log, transMeta);
        trans.prepareExecution(null);
        final RowProducer rp = trans.addRowProducer(STEPNAME_STARTPOINT, 0);
        trans.startThreads();
        
        // OK the transformation is now running in a different thread in the background, let's feed the first step some rows of data.
        // To prevent blocking, you can do it also in a different thread.
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                for (int i=0;i<NR_ROWS;i++)
                {
                    rp.putRow(createRow());
                }
                
                // Finished processing
                rp.finished();
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        
        // Wait until the transformation is finished.
        // That would also mean that thread has finished.
        //
        trans.waitUntilFinished();
    }

    private static Row createRow()
    {
        Row row = new Row();
        
        row.addValue(new Value("field1", "aaaaa"));
        row.addValue(new Value("field2", new Date()));
        row.addValue(new Value("field3", 123L));
        row.addValue(new Value("field4", true));
        row.addValue(new Value("field5", new BigDecimal(123.45)));
        
        return row;
    }
}
