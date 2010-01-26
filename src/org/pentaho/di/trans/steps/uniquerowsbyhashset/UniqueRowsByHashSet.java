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
 
package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class UniqueRowsByHashSet extends BaseStep implements StepInterface
{
	private static Class<?> PKG = UniqueRowsByHashSetMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private UniqueRowsByHashSetMeta meta;
	private UniqueRowsByHashSetData data;
	
	public UniqueRowsByHashSet(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(UniqueRowsByHashSetMeta)getStepMeta().getStepMetaInterface();
		data=(UniqueRowsByHashSetData)stepDataInterface; // create new data object.
	}
	
   private boolean isUniqueRow(Object[] row)
    {
        return data.seen.add(new RowKey(row, data));
    }

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(UniqueRowsByHashSetMeta)smi;
		data=(UniqueRowsByHashSetData)sdi;

		Object[] r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
		    data.clearHashSet();
			setOutputDone();
			return false;
		}

		if (first)
		{
            first=false;
            
            data.inputRowMeta = getInputRowMeta().clone();
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            data.storeValues = meta.getStoreValues();
            
			// Cache lookup of fields
			data.fieldnrs=new int[meta.getCompareFields().length];
            
			for (int i=0;i<meta.getCompareFields().length;i++)
			{
				data.fieldnrs[i] = getInputRowMeta().indexOfValue(meta.getCompareFields()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "UniqueRowsByHashSet.Log.CouldNotFindFieldInRow",meta.getCompareFields()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
				if(data.sendDuplicateRows)
				{
					data.compareFields=data.compareFields==null?meta.getCompareFields()[i]:data.compareFields+","+meta.getCompareFields()[i];
				}
			}
			if(data.sendDuplicateRows && !Const.isEmpty(meta.getErrorDescription())) 
				data.realErrorDescription=environmentSubstitute(meta.getErrorDescription());
		}
		
		if (isUniqueRow(r))
			putRow(data.outputRowMeta, r);
		else
		{
			 incrementLinesRejected();
		    if(data.sendDuplicateRows) {
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, data.realErrorDescription,data.compareFields==""?null:data.compareFields, "UNRH001");
		    }
		}
		
		
        if (checkFeedback(getLinesRead())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "UniqueRowsByHashSet.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
        }
			
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(UniqueRowsByHashSetMeta)smi;
		data=(UniqueRowsByHashSetData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
			data.sendDuplicateRows=getStepMeta().getStepErrorMeta()!=null &&  meta.supportsErrorHandling();
		    return true;
		}
		return false;
	}

}