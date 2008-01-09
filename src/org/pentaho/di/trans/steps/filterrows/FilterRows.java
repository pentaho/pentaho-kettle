 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans.steps.filterrows;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

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
			String message = Messages.getString("FilterRows.Exception.UnexpectedErrorFoundInEvaluationFuction");  //$NON-NLS-1$
			logError(message);
			logError(Messages.getString("FilterRows.Log.ErrorOccurredForRow")+rowMeta.getString(row)); //$NON-NLS-1$
			logError(Const.getStackTracker(e));
			throw new KettleException(message, e);
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FilterRowsMeta)smi;
		data=(FilterRowsData)sdi;

		boolean keep;
		
		Object[] r=getRow();       // Get next usable row from input rowset(s)!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
        	first = false;
        	
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(getInputRowMeta(), getStepname(), null, null, this);
            
            // Cache the position of the RowSet for the output.
            //
            if (data.chosesTargetSteps)
            {
            	data.trueRowSet = findOutputRowSet(getStepname(), getCopy(), meta.getSendTrueStepname(), 0);
            	if ( data.trueRowSet == null )
            	{
            		throw new KettleException(Messages.getString("FilterRows.Log.TargetStepInvalid", meta.getSendTrueStepname()));
            	}
            	
            	data.falseRowSet = findOutputRowSet(getStepname(), getCopy(), meta.getSendFalseStepname(), 0);
            	if ( data.falseRowSet == null )
            	{
            		throw new KettleException(Messages.getString("FilterRows.Log.TargetStepInvalid", meta.getSendFalseStepname()));
            	}            	
            }
        }

		keep=keepRow(getInputRowMeta(), r); // Keep this row?
		if (!data.chosesTargetSteps)
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
		        putRowTo(data.outputRowMeta, r, data.trueRowSet);
		    }
		    else
		    {
		        //System.out.println("Sending row to false :"+info.getSendFalseStepname()+" : "+r);
		        putRowTo(data.outputRowMeta, r, data.falseRowSet);
		    }
		}
		
        if (checkFeedback(linesRead)) logBasic(Messages.getString("FilterRows.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}

	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
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
            	data.chosesTargetSteps = meta.getSendTrueStepname()!=null && meta.getSendFalseStepname()!=null;
            	
                return true;
            } 
        }
        return false;
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}