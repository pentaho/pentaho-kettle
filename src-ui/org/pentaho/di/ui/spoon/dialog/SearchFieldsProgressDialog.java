/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.ui.spoon.dialog;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.ui.spoon.dialog.Messages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

/**
 * 
 * 
 * @author Matt
 * @since  10-mrt-2005
 */
public class SearchFieldsProgressDialog implements IRunnableWithProgress
{
	private StepMeta  stepInfo;
	private boolean   before;
	private TransMeta transMeta;
	private RowMetaInterface fields;
	
	public SearchFieldsProgressDialog(TransMeta transMeta, StepMeta stepMeta, boolean before)
	{
		this.transMeta = transMeta;
		this.stepInfo  = stepMeta;
		this.before    = before;
		this.fields    = null;
	}
	
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
	{
		int size = transMeta.findNrPrevSteps(stepInfo);

		try
		{
			if (before)
			{
				monitor.beginTask(Messages.getString("SearchFieldsProgressDialog.Dialog.SearchInputFields.Message"), size); //Searching for input fields...
				fields = transMeta.getPrevStepFields(stepInfo, monitor);
			}
			else
			{
				monitor.beginTask(Messages.getString("SearchFieldsProgressDialog.Dialog.SearchOutputFields.Message"), size); //Searching for output fields...
				fields = transMeta.getStepFields(stepInfo, monitor);
			}
		}
		catch(KettleStepException kse)
		{
			LogWriter.getInstance().logError(toString(), Messages.getString("SearchFieldsProgressDialog.Log.UnableToGetFields", stepInfo.toString(), kse.getMessage())); //"Search fields progress dialog","Unable to get fields for step ["+stepInfo+"] : "+kse.getMessage()
			throw new InvocationTargetException(kse, Messages.getString("SearchFieldsProgressDialog.Log.UnableToGetFields", stepInfo.toString(), kse.getMessage())); //"Unable to get fields for step ["+stepInfo+"] : "+kse.getMessage()
		}

		monitor.done();
	}

	/**
	 * @return Returns the before.
	 */
	public boolean isBefore()
	{
		return before;
	}
	
	/**
	 * @param before The before to set.
	 */
	public void setBefore(boolean before)
	{
		this.before = before;
	}
	
	/**
	 * @return Returns the fields.
	 */
	public RowMetaInterface getFields()
	{
		return fields;
	}
	
	/**
	 * @param fields The fields to set.
	 */
	public void setFields(RowMetaInterface fields)
	{
		this.fields = fields;
	}
	
	/**
	 * @return Returns the stepInfo.
	 */
	public StepMeta getStepInfo()
	{
		return stepInfo;
	}
	
	/**
	 * @param stepInfo The stepInfo to set.
	 */
	public void setStepInfo(StepMeta stepInfo)
	{
		this.stepInfo = stepInfo;
	}
	
	/**
	 * @return Returns the transMeta.
	 */
	public TransMeta getTransMeta()
	{
		return transMeta;
	}
	
	/**
	 * @param transMeta The transMeta to set.
	 */
	public void setTransMeta(TransMeta transMeta)
	{
		this.transMeta = transMeta;
	}
}
