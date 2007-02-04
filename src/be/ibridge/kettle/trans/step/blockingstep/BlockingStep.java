package be.ibridge.kettle.trans.step.blockingstep;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

public class BlockingStep extends BaseStep implements StepInterface {

    private StepMetaInterface meta;
    private StepDataInterface data;
    private Row lastRow;
    
    public BlockingStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(BlockingStepMeta)smi;
		data=(BlockingStepData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	} 
    
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {
        Row r=getRow();       // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            if(lastRow != null) {
                putRow(lastRow);
            }
            setOutputDone();
            return false;
        }
        
        lastRow = r;
        return true;
    }

    //
    // Run is were the action happens!
    public void run()
    {
        try
        {
        	logBasic("Starting to run...");
            while (processRow(meta, data) && !isStopped());
        }
        catch(Exception e)
        {
            logError("Unexpected error in '"+" : "+e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
        }
        finally
        {
        	dispose(meta, data);
            logSummary();
            markStop();
        }
    }
}