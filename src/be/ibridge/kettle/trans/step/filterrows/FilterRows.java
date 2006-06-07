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

package be.ibridge.kettle.trans.step.filterrows;

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
 * Filters input rows base on conditions.
 * 
 * @author Matt
 * @since 16-apr-2003, 07-nov-2004 (rewrite)
 */

public class FilterRows extends BaseStep implements StepInterface
{
	private FilterRowsMeta meta;
	private FilterRowsData data;
	
	public FilterRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized boolean keepRow(Row row) throws KettleException
	{
		try
		{
			debug=Messages.getString("FilterRows.0"); //$NON-NLS-1$
			boolean ret=meta.getCondition().evaluate(row);
	        debug=Messages.getString("FilterRows.Debug.AfterEvaluateCondition"); //$NON-NLS-1$
			return ret;
		}
		catch(Exception e)
		{
			String message = Messages.getString("FilterRows.Exception.UnexpectedErrorFoundInEvaluationFuction")+e.toString();  //$NON-NLS-1$
			logError(message);
			logError(Messages.getString("FilterRows.Log.ErrorOccurredForRow")+row); //$NON-NLS-1$
			logError(Const.getStackTracker(e));
			throw new KettleException(message, e);
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FilterRowsMeta)smi;
		data=(FilterRowsData)sdi;

		Row r=null;
		boolean keep;
		
		r=getRow();       // Get next useable row from input rowset(s)!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		keep=keepRow(r); // Keep this row?
		if (!meta.chosesTargetSteps())
		{
			if (keep)
			{
				putRow(r);       // copy row to output rowset(s);
			}
		}
		else
		{
		    if (keep)
		    {
		        //System.out.println("Sending row to true  :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(r, meta.getSendTrueStepname());
		    }
		    else
		    {
		        //System.out.println("Sending row to false :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(r, meta.getSendFalseStepname());
		    }
		}
		
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("FilterRows.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}

	/**
     * @see StepInterface#init( be.ibridge.kettle.trans.step.StepMetaInterface , be.ibridge.kettle.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(FilterRowsMeta)smi;
		data=(FilterRowsData)sdi;

        if (super.init(smi, sdi))
        {
            if (meta.getSendTrueStepname()!=null ^ meta.getSendFalseStepname()!=null)
            {
                logError(Messages.getString("FilterRows.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
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
			logBasic(Messages.getString("FilterRows.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("FilterRows.Log.UnexpectedErrorIn")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
