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

package org.pentaho.di.trans.steps.rowstoresult;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Writes results to a next transformation in a Job
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class RowsToResult extends BaseStep implements StepInterface
{
	private static Class<?> PKG = RowsToResult.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private RowsToResultMeta meta;

	private RowsToResultData data;

	public RowsToResult(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr,
			TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta = (RowsToResultMeta) smi;
		data = (RowsToResultData) sdi;

		Object[] r = getRow(); // get row, set busy!
		if (r == null) // no more input to be expected...
		{
			getTransMeta().getResultRows().addAll(data.rows);

			setOutputDone();
			return false;
		}

		// Add all rows to rows buffer...
		data.rows.add( new RowMetaAndData(getInputRowMeta(), r) );
		data.outputRowMeta = getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
		putRow(data.outputRowMeta, r); // copy row to possible alternate
										// rowset(s).

		if (checkFeedback(getLinesRead()))
			logBasic(BaseMessages.getString(PKG, "RowsToResult.Log.LineNumber") + getLinesRead()); //$NON-NLS-1$

		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (RowsToResultMeta) smi;
		data = (RowsToResultData) sdi;

		if (super.init(smi, sdi))
		{
			// Add init code here.
			return true;
		}
		return false;
	}

}