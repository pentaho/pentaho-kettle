package be.ibridge.kettle.test.value;

import java.util.Date;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.step.rowgenerator.RowGeneratorMeta;

public class ValueSpeedTest3
{
    // private static final int ITERATIONS = 250000;
    private static final int ITERATIONS = 10000000;
    
    public static void main(String[] args)
    {
        EnvUtil.environmentInit();
        LogWriter.getInstance(LogWriter.LOG_LEVEL_BASIC);
        
        // Let's create rows now and see what happens...
        //
        RowGeneratorMeta meta = getRowGeneratorMeta();
        /*
        StepMeta stepMeta = new StepMeta("generator", meta);
        RowGeneratorData data = new RowGeneratorData();
        RowGenerator generator = new RowGenerator(stepMeta, data, 0, new TransMeta(), null);
        ArrayList list = new ArrayList();
        generator.setOutputRowSets(list); // no output steps.
        
        generator.init(meta, data);
        
        Date t1 = new Date();
        for (int i=0;i<ITERATIONS;i++)
        {
            generator.processRow(meta, data);
        }
        Date t2 = new Date();
        System.out.println("Total time spent: "+getTime(t1, t2));
        */
        
        // Now generate a new transformation and see how fast that runs.
        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(meta, "generator");
        transMeta.setFeedbackShown(false);
        transMeta.setFeedbackSize(100000);
        transMeta.setSizeRowset(50000);
        transMeta.setUsingThreadPriorityManagment(false);
        Trans trans = new Trans(LogWriter.getInstance(), transMeta);
        trans.setMonitored(true);
        trans.prepareExecution(null);
        
        Date t3 = new Date();
        trans.startThreads();
        trans.waitUntilFinished();
        Date t4 = new Date();
        System.out.println("Total time spent: "+getTime(t3, t4));
    }

    private static RowGeneratorMeta getRowGeneratorMeta()
   {
        RowGeneratorMeta meta = new RowGeneratorMeta();
        meta.allocate(6);
        
        // Strings
        int idx = 0;
        meta.getFieldType()[idx] = "String";
        meta.getFieldName()[idx] = "A";
        meta.getValue()[idx] = "abracadabrastring";
        meta.getFieldLength()[idx] = 40;

        // Numbers
        idx++;
        meta.getFieldType()[idx] = "Number";
        meta.getFieldName()[idx] = "B";
        meta.getValue()[idx] = "87343843,23";
        meta.getFieldLength()[idx] = 12;
        meta.getFieldPrecision()[idx] = 4;

        // Dates
        idx++;
        meta.getFieldType()[idx] = "Date";
        meta.getFieldName()[idx] = "C";
        meta.getValue()[idx] = "2006/12/31 23:59:59";
        meta.getFieldFormat()[idx] = "yyyy/MM/dd HH:mm:ss";

        // BigNumber
        idx++;
        meta.getFieldType()[idx] = "BigNumber";
        meta.getFieldName()[idx] = "D";
        meta.getValue()[idx] = "1239434.3943493";
        meta.getFieldLength()[idx] = 56;
        meta.getFieldPrecision()[idx] = 12;

        // Boolean
        idx++;
        meta.getFieldType()[idx] = "Boolean";
        meta.getFieldName()[idx] = "E";
        meta.getValue()[idx] = "Y";

        // INTEGER
        idx++;
        meta.getFieldType()[idx] = "Integer";
        meta.getFieldName()[idx] = "F";
        meta.getValue()[idx] = "3498349";
        meta.getFieldLength()[idx] = 7;
        
        meta.setRowLimit(Integer.toString(ITERATIONS));
        
        return meta;
    }

    private static String getTime(Date t1, Date t2)
    {
        return Double.toString( ((double)t2.getTime()-(double)t1.getTime())/1000 );
    }
}
