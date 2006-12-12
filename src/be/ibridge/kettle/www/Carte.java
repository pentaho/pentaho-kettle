package be.ibridge.kettle.www;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.util.EnvUtil;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.StepLoader;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransConfiguration;
import be.ibridge.kettle.trans.TransExecutionConfiguration;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.TransPreviewFactory;
import be.ibridge.kettle.trans.step.rowgenerator.RowGeneratorMeta;

public class Carte
{
    public static void main(String[] args) throws Exception
    {
        init();
        
        TransformationMap map = new TransformationMap(Thread.currentThread().getName());
        
        Trans trans = generateTestTransformation();
        map.addTransformation(trans.getName(), trans, new TransConfiguration(trans.getTransMeta(), new TransExecutionConfiguration()));
        
        int port = WebServer.PORT;
        if (args.length>=1)
        {
            try
            {
                port = Integer.parseInt(args[0]);
            }
            catch(Exception e)
            {
                System.out.println("Unable to parse port ["+args[0]+"], using port ["+port+"]");
            }
        }
        new WebServer(map, port);
    }

    private static void init() throws Exception
    {
        EnvUtil.environmentInit();
        LogWriter.getInstance( LogWriter.LOG_LEVEL_BASIC );
        
        StepLoader stepLoader = StepLoader.getInstance();
        if (!stepLoader.read())
        {
            throw new Exception("Unable to load steps & plugins");
        }
    }
    
    private static Trans generateTestTransformation()
    {
        RowGeneratorMeta A = new RowGeneratorMeta();
        A.allocate(3);
        A.setRowLimit("100000000");

        A.getFieldName()[0]   = "ID"; 
        A.getFieldType()[0]   = Value.getTypeDesc(Value.VALUE_TYPE_INTEGER);
        A.getFieldLength()[0] = 7; 
        A.getValue()[0]       = "1234"; 
        
        A.getFieldName()[1]   = "Name"; 
        A.getFieldType()[1]   = Value.getTypeDesc(Value.VALUE_TYPE_STRING);
        A.getFieldLength()[1] = 35; 
        A.getValue()[1]       = "Some name"; 

        A.getFieldName()[2]   = "Last updated"; 
        A.getFieldType()[2]   = Value.getTypeDesc(Value.VALUE_TYPE_DATE);
        A.getFieldFormat()[2] = "yyyy/MM/dd"; 
        A.getValue()[2]       = "2006/11/13"; 

        TransMeta transMeta = TransPreviewFactory.generatePreviewTransformation(A, "A");
        transMeta.setName("Row generator test");
        transMeta.setSizeRowset(2500);
        transMeta.setFeedbackSize(250000);
        transMeta.setUsingThreadPriorityManagment(false);

        return new Trans(LogWriter.getInstance(), transMeta);
        
    }
}
