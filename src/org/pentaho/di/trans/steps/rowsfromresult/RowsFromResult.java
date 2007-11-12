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

package org.pentaho.di.trans.steps.rowsfromresult;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
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
 * Reads results from a previous transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class RowsFromResult extends BaseStep implements StepInterface
{
	private RowsFromResultMeta meta;

	private RowsFromResultData data;

	public RowsFromResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);

		meta = (RowsFromResultMeta) getStepMeta().getStepMetaInterface();
		data = (RowsFromResultData) stepDataInterface;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Result previousResult = getTransMeta().getPreviousResult();
		if (previousResult == null || linesRead >= previousResult.getRows().size())
		{
			setOutputDone();
			return false;
		}
        RowMetaAndData row = previousResult.getRows().get((int) linesRead);
        linesRead++;
        
		data = (RowsFromResultData) sdi;
		
		// We don't get the meta-data from the previous steps (there aren't any) but from the previous transformation or job
		//
		data.outputRowMeta = row.getRowMeta();
		
		// copy row to possible alternate rowset(s).
		//
		putRow(data.outputRowMeta, row.getData());

		if (checkFeedback(linesRead)) logBasic(Messages.getString("RowsFromResult.Log.LineNumber") + linesRead); //$NON-NLS-1$

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (RowsFromResultMeta) smi;
		data = (RowsFromResultData) sdi;

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
		catch (Throwable t)
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