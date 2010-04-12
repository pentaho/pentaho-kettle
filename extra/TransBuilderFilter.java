
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.Log4jFileAppender;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;



/**
 * Class created to demonstrate the creation of transformations on-the-fly.
 * 
 * @author Matt
 * 
 */
public class TransBuilderFilter
{
    /**
     * Creates a new Transformation that generates 2 different Row Generators, passes them to a filter and splits them up again.<br>
     * <br>
     * <pre>
           A--,   ,--D
           	   -C-
           B--´   `--E
     * </pre>
     * @param transformationName The name of the transformation
     * @return A new transformation
     * @throws KettleException In the rare case something goes wrong
     */
    public static final TransMeta buildFilterSample(String transformationName) throws KettleException
    {
        KettleEnvironment.init();

        try
        {
            //
            // Create a new transformation...
            //
          TransMeta transMeta =  new TransMeta();
          transMeta.setName(transformationName);
            
            // A
            //
            RowGeneratorMeta aMeta = new RowGeneratorMeta();
            aMeta.allocate(1);
            aMeta.setRowLimit("100");
            aMeta.getFieldName()[0] = "id";
            aMeta.getFieldType()[0] = "Integer";
            aMeta.getFieldFormat()[0] = "#";
            aMeta.getValue()[0] = "5";
            StepMeta aStep = new StepMeta("A", aMeta);
            aStep.setLocation(50,50);
            aStep.setDraw(true);
            transMeta.addStep(aStep);

            // B : Filter
            //
            RowGeneratorMeta bMeta = new RowGeneratorMeta();
            bMeta.allocate(1);
            bMeta.setRowLimit("100");
            bMeta.getFieldName()[0] = "id";
            bMeta.getFieldType()[0] = "Integer";
            bMeta.getFieldFormat()[0] = "#";
            bMeta.getValue()[0] = "9";
            StepMeta bStep = new StepMeta("B", bMeta);
            bStep.setLocation(50,150);
            bStep.setDraw(true);
            transMeta.addStep(bStep);
            
            // D : Text File Output
            //
            TextFileOutputMeta dMeta = new TextFileOutputMeta();
            dMeta.setDefault();
            dMeta.allocate(1);
            dMeta.setFileName("${java.io.tmpdir}/dOutput");
            dMeta.setExtension("txt");
            TextFileField dField = new TextFileField();
            dField.setName("id");
            dField.setFormat("000");
            dField.setType(ValueMetaInterface.TYPE_INTEGER);
            dMeta.getOutputFields()[0] = dField;
            StepMeta dStep = new StepMeta("D", dMeta);
            dStep.setLocation(350,50);
            dStep.setDraw(true);
            transMeta.addStep(dStep);

            // E : Text File Output
            //
            TextFileOutputMeta eMeta = new TextFileOutputMeta();
            eMeta.setDefault();
            eMeta.allocate(1);
            eMeta.setFileName("${java.io.tmpdir}/eOutput");
            eMeta.setExtension("txt");
            TextFileField eField = new TextFileField();
            eField.setName("id");
            eField.setFormat("000");
            eField.setType(ValueMetaInterface.TYPE_INTEGER);
            eMeta.getOutputFields()[0] = eField;
            StepMeta eStep = new StepMeta("E", dMeta);
            eStep.setLocation(350,150);
            eStep.setDraw(true);
            transMeta.addStep(eStep);

            // C : Filter
            //
            FilterRowsMeta cMeta = new FilterRowsMeta();
            Condition condition = new Condition();
            condition.setLeftValuename("id");
            condition.setFunction(Condition.FUNC_SMALLER);
            condition.setRightExact(new ValueMetaAndData("constant", new Long(6)));
            cMeta.setCondition(condition);
            cMeta.getStepIOMeta().getTargetStreams().get(0).setStepMeta(dStep);
            cMeta.getStepIOMeta().getTargetStreams().get(1).setStepMeta(eStep);
            StepMeta cStep = new StepMeta("C", cMeta);
            cStep.setLocation(200,100);
            cStep.setDraw(true);
            transMeta.addStep(cStep);
            
            // Add hops...
            //
            transMeta.addTransHop(new TransHopMeta(aStep, cStep));
            transMeta.addTransHop(new TransHopMeta(bStep, cStep));
            transMeta.addTransHop(new TransHopMeta(cStep, dStep));
            transMeta.addTransHop(new TransHopMeta(cStep, eStep));
            
            // OK, that's all if we don't care about UI presentation, otherwise, add coordinates to the step
            //
            return transMeta;
        }
        catch (Exception e)
        {
            throw new KettleException("An unexpected error occurred creating the new transformation", e);
        }
    }

    /**
     * 1) create a new transformation <br>
     * 2) Express the transformation as XML file <br>
     * 3) Execute the transformation <br>
     * <br>
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
    	// Bootstrap the Kettle API...
    	//
    	KettleEnvironment.init();
        
    	// Init the logging...
    	//
        Log4jFileAppender fileAppender = LogWriter.createFileAppender("TransBuilderFilter.log", true);
        LogWriter.getInstance().addAppender(fileAppender);
                
        // The parameters we want, optionally this can be 
        String transformationName = "Filter test Transformation";
       
        TransMeta transMeta = buildFilterSample(transformationName);
        System.out.println(transMeta.getXML());
        
        // Now execute the transformation...
        Trans trans = new Trans(transMeta);
        trans.setLogLevel(LogLevel.DETAILED);
        trans.execute(null);
        trans.waitUntilFinished();
    }


}
