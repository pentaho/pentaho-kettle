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
 
package org.pentaho.di.trans.steps.uniquerows;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Removes the same consequetive rows from the input stream(s).
 *  
 * @author Matt
 * @since 2-jun-2003
 */
public class UniqueRows extends BaseStep implements StepInterface
{
	private UniqueRowsMeta meta;
	private UniqueRowsData data;
	
	public UniqueRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(UniqueRowsMeta)getStepMeta().getStepMetaInterface();
		data=(UniqueRowsData)stepDataInterface; // create new data object.
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UniqueRowsMeta)smi;
		data=(UniqueRowsData)sdi;

		Object[] r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.previous!=null) 
			{
				Object[] outputRow = addCounter(getInputRowMeta(), data.previous, data.counter);
				putRow(data.outputRowMeta, outputRow);
			} 
			setOutputDone();
			return false;
		}

		if (first)
		{
            first=false;
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
			data.previous=getInputRowMeta().cloneRow(r); // copy the row
			
			// Cache lookup of fields
			data.fieldnrs=new int[meta.getCompareFields().length];
			data.ascending=new boolean[meta.getCompareFields().length];
            data.caseInsensitive=new boolean[meta.getCaseInsensitive().length];
            
			for (int i=0;i<meta.getCompareFields().length;i++)
			{
			    data.ascending[i] = false;
				data.fieldnrs[i] = getInputRowMeta().indexOfValue(meta.getCompareFields()[i]);
                data.caseInsensitive[i] = meta.getCaseInsensitive()[i];
				if (data.fieldnrs[i]<0)
				{
					logError(Messages.getString("UniqueRows.Log.CouldNotFindFieldInRow",meta.getCompareFields()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
			}
		}
		
		boolean isEqual = false;
		
		if (meta.getCompareFields()==null || meta.getCompareFields().length==0)
		{
		    // Compare the complete row...
		    isEqual = getInputRowMeta().compare(r, data.previous)==0;
		}
		else
		{
		    isEqual = getInputRowMeta().compare(r, data.previous, data.fieldnrs)==0;
		}
		if (!isEqual)
		{
			Object[] outputRow = addCounter(getInputRowMeta(), data.previous, data.counter);
			putRow(data.outputRowMeta, outputRow); // copy row to possible alternate rowset(s).
			data.previous=getInputRowMeta().cloneRow(r);
			data.counter=1;
		}
		else
		{
			data.counter++;
		}

        if (checkFeedback(linesRead)) logBasic(Messages.getString("UniqueRows.Log.LineNumber")+linesRead); //$NON-NLS-1$
			
		return true;
	}
	
	private Object[] addCounter(RowMetaInterface inputRowMeta, Object[] r, long count)
	{
		if (meta.isCountRows())
		{
            Object[] outputRow = RowDataUtil.addValueData(r, inputRowMeta.size(), new Long(count));
            
            return outputRow;
		}
        else
        {
            return r; // nothing to do
        }
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UniqueRowsMeta)smi;
		data=(UniqueRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
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
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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