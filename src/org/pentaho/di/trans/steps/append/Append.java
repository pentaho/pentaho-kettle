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

package org.pentaho.di.trans.steps.append;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


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
		
    	RowMetaAndData input = null;
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
    	
    	if ( data.processTail )
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
            putRow(input.getRowMeta(), input.getData());
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