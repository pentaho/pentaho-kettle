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
 package org.pentaho.di.trans.steps.flattener;

import org.pentaho.di.core.exception.KettleException;
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
 * Pivots data based on key-value pairs
 * 
 * @author Matt
 * @since 17-jan-2006
 */
public class Flattener extends BaseStep implements StepInterface
{
	private static Class<?> PKG = FlattenerMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	private FlattenerMeta meta;
	private FlattenerData data;
	
	public Flattener(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(FlattenerMeta)getStepMeta().getStepMetaInterface();
		data=(FlattenerData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		Object[] r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.processed>0) 
			{
				Object[] outputRowData = createOutputRow(data.previousRow);
                
                // send out inputrow + the flattened part
				//
                putRow(data.outputRowMeta, outputRowData);
			}

			setOutputDone();
			return false;
		}
		
		if (first)
		{
			data.inputRowMeta = getInputRowMeta();
			data.outputRowMeta = data.inputRowMeta.clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
            data.fieldNr = data.inputRowMeta.indexOfValue( meta.getFieldName() );
            if (data.fieldNr<0)
            {
                logError(BaseMessages.getString(PKG, "Flattener.Log.FieldCouldNotFound",meta.getFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
                setErrors(1);
                stopAll();
                return false;
            }
            
            // Allocate the result row...
            //
            data.targetResult = new Object[meta.getTargetField().length];
            
			first=false;
		}

        // set it to value # data.processed
        //
        data.targetResult[data.processed++] = r[data.fieldNr];
        
        if (data.processed>=meta.getTargetField().length)
        {
        	Object[] outputRowData = createOutputRow(r);
        	
            // send out input row + the flattened part
            putRow(data.outputRowMeta, outputRowData);
            
            // clear the result row
            data.targetResult = new Object[meta.getTargetField().length];
            
            data.processed=0;
        }
        
        // Keep track in case we want to send out the last couple of flattened values.
        data.previousRow = r;

        if (checkFeedback(getLinesRead())) logBasic(BaseMessages.getString(PKG, "Flattener.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			
		return true;
	}
	
	private Object[] createOutputRow(Object[] rowData) {
		
		Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		int outputIndex=0;
		
		// copy the values from previous, but don't take along index 'data.fieldNr'...
		//
		for (int i=0;i<data.inputRowMeta.size();i++) {
			if (i!=data.fieldNr) {
				outputRowData[outputIndex++] = rowData[i];
			}
		}
		
		// Now add the fields we flattened...
		//
		for (int i=0;i<data.targetResult.length;i++) {
			outputRowData[outputIndex++] = data.targetResult[i];
		}
        
		return outputRowData;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FlattenerMeta)smi;
		data=(FlattenerData)sdi;
		
		if (super.init(smi, sdi))
		{
            return true;
		}
		return false;
	}
	
}