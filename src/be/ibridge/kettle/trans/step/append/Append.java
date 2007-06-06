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

package be.ibridge.kettle.trans.step.append;

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

/**
 * Read all rows from a hop until the end, and then read the rows from another hop.
 * 
 * @author Sven Boden
 * @since  3-june-2007
 */
public class Append extends BaseStep implements StepInterface
{   
	private AppendMeta meta;
	private AppendData data;
	
	public Append(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;

    	Row input = null;
    	if ( data.processHead )
    	{
		    input = getRowFrom(meta.getHeadStepName());
		    
		    if ( input == null )
		    {
		    	// Switch to tail processing		    
	        	data.processHead = false;
	        	data.processTail = true;
		    }
    	}
    	else if ( data.processTail )
    	{
    		input = getRowFrom(meta.getTailStepName());
		    if ( input == null )
		    {
	            setOutputDone();
	            return false;
		    }
    	}

    	if ( input != null )
    	{
            putRow(input);
    	}

        if (checkFeedback(linesRead)) logBasic(Messages.getString("AppendRows.LineNumber")+linesRead); //$NON-NLS-1$

		return true;
	}

	/**
     * @see StepInterface#init( be.ibridge.kettle.trans.step.StepMetaInterface , be.ibridge.kettle.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(AppendMeta)smi;
		data=(AppendData)sdi;

        if (super.init(smi, sdi))
        {
        	data.processHead = true;
        	data.processTail = false;
            if (meta.getHeadStepName()==null || meta.getTailStepName()==null)
            {
                logError(Messages.getString("AppendRows.Log.BothHopsAreNeeded")); //$NON-NLS-1$
            }
            else
            {
                return true;
            }            
        }
        return false;
    }

	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("AppendRows.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("AppendRows.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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