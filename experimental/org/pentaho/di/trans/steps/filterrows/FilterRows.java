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

package org.pentaho.di.trans.steps.filterrows;

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;



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
	
	private synchronized boolean keepRow(RowMetaInterface rowMeta, Object[] row) throws KettleException
	{
		try
		{
	        return meta.getCondition().evaluate(rowMeta, row);
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

		boolean keep;
		
		Object[] r=getRow();       // Get next useable row from input rowset(s)!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            data.outputRowMeta = (RowMetaInterface) getInputRowMeta().clone();
            meta.getFields(getInputRowMeta(), getStepname(), null);
        }

		keep=keepRow(getInputRowMeta(), r); // Keep this row?
		if (!meta.chosesTargetSteps())
		{
			if (keep)
			{
				putRow(data.outputRowMeta, r);       // copy row to output rowset(s);
			}
		}
		else
		{
		    if (keep)
		    {
		        //System.out.println("Sending row to true  :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(data.outputRowMeta, r, meta.getSendTrueStepname());
		    }
		    else
		    {
		        //System.out.println("Sending row to false :"+info.getSendTrueStepname()+" : "+r);
		        putRowTo(data.outputRowMeta, r, meta.getSendFalseStepname());
		    }
		}
		
        if (checkFeedback(linesRead)) logBasic(Messages.getString("FilterRows.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
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
			logError(Messages.getString("FilterRows.Log.UnexpectedErrorIn")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
