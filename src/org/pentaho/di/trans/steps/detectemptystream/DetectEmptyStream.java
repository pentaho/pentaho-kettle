/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.detectemptystream;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Detect empty stream.  Pass one row data to the next steps.
 * 
 * @author Samatar
 * @since 30-08-2008
 */
public class DetectEmptyStream extends BaseStep implements StepInterface
{
	private static Class<?> PKG = DetectEmptyStreamMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DetectEmptyStreamData data;
	
	public DetectEmptyStream(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	/**
	 * Build an empty row based on the meta-data.
	 * 
	 * @return
	 */
    private Object[] buildOneRow() throws KettleStepException
    {
       // return previous fields name
       Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
       return outputRowData;
    }
    
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		data=(DetectEmptyStreamData)sdi;

		Object[] r = getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			if(first)
			{
				// input stream is empty !
				data.outputRowMeta=getTransMeta().getPrevStepFields(getStepMeta());
				putRow(data.outputRowMeta, buildOneRow());     // copy row to possible alternate rowset(s).

		        if (checkFeedback(getLinesRead())) 
		        {
		        	if (log.isBasic()) {
		        		logBasic(BaseMessages.getString(PKG, "DetectEmptyStream.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
		        	}
		        }   
			}
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
		}
			
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		data=(DetectEmptyStreamData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
}