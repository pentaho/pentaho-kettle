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
 
package be.ibridge.kettle.trans.step.setvariable;

import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Convert Values in a certain fields to other values
 * 
 * @author Matt 
 * @since 27-apr-2006
 */
public class SetVariable extends BaseStep implements StepInterface
{
	private SetVariableMeta meta;
	private SetVariableData data;
	
	public SetVariable(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;
		
	    // Get one row from one of the rowsets...
        //
		Row r = getRow();
		if (r==null)  // means: no more input to be expected...
		{
            logBasic("Finished after "+linesWritten+" rows.");
			setOutputDone();
			return false;
		}
		
		if (first)
		{
		    first=false;

            logBasic("Setting environment variables...");

            for (int i=0;i<meta.getFieldName().length;i++)
            {
                // Set the appropriate environment variable
                String value = r.getString(meta.getFieldName()[i], "");
                System.setProperty(meta.getVariableName()[i], value);
                logBasic("Set variable "+meta.getVariableName()[i]+" to value ["+value+"]");
            }
            
            putRow(r);
            return true;
		}

        throw new KettleStepException(Messages.getString("SetVariable.RuntimeError.MoreThanOneRowReceived.SETVARIABLE0007"));
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetVariableMeta)smi;
		data=(SetVariableData)sdi;
		
		if (super.init(smi, sdi))
		{
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
			logBasic(Messages.getString("SetVariable.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("SetVariable.RuntimeError.UnexpectedError.SETVARIABLE0003", debug, e.toString())); //$NON-NLS-1$
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
