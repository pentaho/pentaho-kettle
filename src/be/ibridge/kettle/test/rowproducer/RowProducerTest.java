package be.ibridge.kettle.test.rowproducer;

import java.math.BigDecimal;
import java.util.Date;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.RowProducer;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.step.dummytrans.DummyTransMeta;

public class RowProducerTest 
{
    private static final String STEPNAME_STARTPOINT = "Start point";
    
    public static void main(String[] args) throws KettleException
    {
        // Init stuff...
        LogWriter log = LogWriter.getInstance();
        StepLoader stepLoader = StepLoader.getInstance();
        if (!stepLoader.read())
        {
            throw new RuntimeException("Unable to load steps and plugins");
        }
        
        // 
        DummyTransMeta meta = new DummyTransMeta();
        
        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(meta, STEPNAME_STARTPOINT);
        
        Trans trans = new Trans(log, transMeta);
        trans.prepareExecution(null);
        RowProducer rp = trans.addRowProducer(STEPNAME_STARTPOINT, 0);
        trans.startThreads();
        
        // OK the transformation is now running in a different thread in the background, let's feed the first step some rows of data.
        for (int i=0;i<3;i++)
        {
            rp.putRow(createRow());
        }
        // Finished processing
        rp.finished();
        
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
