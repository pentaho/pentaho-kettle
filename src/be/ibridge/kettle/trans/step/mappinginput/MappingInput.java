 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step.mappinginput;

import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Do nothing.  Pass all input data to the next steps.
 * 
 * @author Matt
 * @since 2-jun-2003
 */

public class MappingInput extends BaseStep implements StepInterface
{
	private MappingInputMeta meta;
	private MappingInputData data;
	
	public MappingInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
    // ProcessRow is not doing anything
    // It's a placeholder for accepting rows from the parent transformation...
    // So, basically, we wait for output to be done...
    //
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MappingInputMeta)smi;
		data=(MappingInputData)sdi;

        // wait a while
        while (!data.finished && !isStopped())
        {
            try
            {
                Thread.sleep(10);
            }
            catch(InterruptedException e)
            {
                stopAll();
            }
        }
        
		return false;
	}

    public void setFinished()
    {
        data.finished = true;
    }

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MappingInputMeta)smi;
		data=(MappingInputData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.finished = false;

            return true;
		}
		return false;
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
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
