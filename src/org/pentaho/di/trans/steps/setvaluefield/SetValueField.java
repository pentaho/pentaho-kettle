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
package org.pentaho.di.trans.steps.setvaluefield;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Set value field with another value field.
 * 
 * @author Samatar 
 * @since 10-11-2008
 */
public class SetValueField extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SetValueFieldMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SetValueFieldMeta meta;
	private SetValueFieldData data;
	
	public SetValueField(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SetValueFieldMeta)smi;
		data=(SetValueFieldData)sdi;
		
	    // Get one row from one of the rowsets...
		Object[] r = getRow();
		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		

		if (first)
		{
		    first=false;
			// What's the format of the output row?
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
		    data.indexOfField     = new int[meta.getFieldName().length];
		    data.indexOfReplaceByValue = new int[meta.getFieldName().length];
		    for (int i=0;i<meta.getFieldName().length;i++)
		    {
				// Check if this field was specified only one time
				for (int j=0;j<meta.getFieldName().length;j++)
				{
					if(meta.getFieldName()[j].equals(meta.getFieldName()[i]))
					{
						if(j!=i) throw new KettleException(BaseMessages.getString(PKG, "SetValueField.Log.FieldSpecifiedMoreThatOne",meta.getFieldName()[i],""+i,""+j));
					}
				}
				
		        data.indexOfField [i] = data.outputRowMeta.indexOfValue(environmentSubstitute(meta.getFieldName()[i]));
				if (data.indexOfField [i]<0)
				{
					throw new KettleStepException(BaseMessages.getString(PKG, "SetValueField.Log.CouldNotFindFieldInRow",meta.getFieldName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				}
				String sourceField=environmentSubstitute(meta.getReplaceByFieldValue()[i]);
				if(Const.isEmpty(sourceField))
				{
					throw new KettleStepException(BaseMessages.getString(PKG, "SetValueField.Log.ReplaceByValueFieldMissing",""+i));
				}
			    data.indexOfReplaceByValue[i] = data.outputRowMeta.indexOfValue(sourceField);
			    if (data.indexOfReplaceByValue[i]<0)
				{
				   throw new KettleStepException(BaseMessages.getString(PKG, "SetValueField.Log.CouldNotFindFieldInRow",sourceField)); 
				}
			   // Compare fields type
			   ValueMetaInterface SourceValue=getInputRowMeta().getValueMeta(data.indexOfField[i]);
			   ValueMetaInterface ReplaceByValue=getInputRowMeta().getValueMeta(data.indexOfReplaceByValue[i]);
			   
			   if(SourceValue.getType()!=ReplaceByValue.getType())
			   {
				   String err=BaseMessages.getString(PKG, "SetValueField.Log.FieldsTypeDifferent",
						   SourceValue.getName()+" ("+SourceValue.getTypeDesc()+")", 
						  ReplaceByValue.getName()+" ("+ReplaceByValue.getTypeDesc()+")");
				   throw new KettleStepException(err);
			   }
		    } 
		}
		try
		{
			for (int i=0;i<data.indexOfField.length;i++)
			{
				r[data.indexOfField [i]]=r[data.indexOfReplaceByValue[i]];
			}
			putRow(data.outputRowMeta, r);  // copy row to output rowset(s);
		} catch (KettleException e) 
		{
	        boolean sendToErrorRow=false;
	        String errorMessage = null;
	        
			if (getStepMeta().isDoingErrorHandling())
			{
                 sendToErrorRow = true;
                 errorMessage = e.toString();
        	}
        	else
        	{
        		logError(BaseMessages.getString(PKG, "SetValueField.Log.ErrorInStep",e.getMessage())); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
        	}
        	if (sendToErrorRow){
        	   // Simply add this row to the error row
        	   putError(data.outputRowMeta, r, 1, errorMessage, null, "SetValueField001");
        	}
		}
		return true;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetValueFieldMeta)smi;
		data=(SetValueFieldData)sdi;

		super.dispose(smi, sdi);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SetValueFieldMeta)smi;
		data=(SetValueFieldData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}

}
