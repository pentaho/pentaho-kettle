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
 
package org.pentaho.di.trans.steps.ifnull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
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
 * Sets a field value to a constant if it is null
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class IfNull extends BaseStep implements StepInterface
{
	private static Class<?> PKG = IfNullMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private IfNullMeta meta;
	private IfNullData data;
	
	public IfNull(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(IfNullMeta)smi;
		data=(IfNullData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;

			// What's the format of the output row?
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			// Create convert meta-data objects that will contain Date & Number formatters
            data.convertRowMeta = data.outputRowMeta.clone();
            
            // For String to <type> conversions, we allocate a conversion meta data row as well...
			//
			data.convertRowMeta = data.outputRowMeta.clone();
			for (int i=0;i<data.convertRowMeta.size();i++) {
				data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
			}
			
			if(meta.isSelectFields())
			{
				// Consider only selected fields
				if(meta.getFieldName()!=null && meta.getFieldName().length>0)
				{
					data.fieldnrs=new int[meta.getFieldName().length];
					data.defaultValues= new String[meta.getFieldName().length];
					data.defaultMasks= new String[meta.getFieldName().length];
					
					for (int i=0;i<meta.getFieldName().length;i++)
					{
						data.fieldnrs[i]=data.outputRowMeta.indexOfValue(meta.getFieldName()[i]);
						if (data.fieldnrs[i]<0)
						{
							logError(BaseMessages.getString(PKG, "IfNull.Log.CanNotFindField",meta.getFieldName()[i]));
							throw new KettleException(BaseMessages.getString(PKG, "IfNull.Log.CanNotFindField",meta.getFieldName()[i]));
						}
						data.defaultValues[i]=environmentSubstitute(meta.getReplaceValue()[i]); 
						data.defaultMasks[i]=environmentSubstitute(meta.getReplaceMask()[i]); 
		 			}
				}else
					throw new KettleException(BaseMessages.getString(PKG, "IfNull.Log.SelectFieldsEmpty"));
			}else if(meta.isSelectValuesType())
			{
				// Consider only select value types
				if(meta.getTypeName()!=null && meta.getTypeName().length>0)
				{
					// return the real default values
					data.defaultValues= new String[meta.getTypeName().length];
					data.defaultMasks= new String[meta.getTypeName().length];
			
					// return all type codes
					HashSet<String> AlllistTypes = new HashSet<String>();
					for(int i=0; i<ValueMetaInterface.typeCodes.length;i++){
						AlllistTypes.add(ValueMetaInterface.typeCodes[i]);
					}
					

					for (int i=0;i<meta.getTypeName().length;i++){
						if(!AlllistTypes.contains(meta.getTypeName()[i]))
								throw new KettleException(BaseMessages.getString(PKG, "IfNull.Log.CanNotFindValueType",meta.getTypeName()[i]));

						data.ListTypes.put(meta.getTypeName()[i], i);
						data.defaultValues[i]=environmentSubstitute(meta.getTypeReplaceValue()[i]); 
						data.defaultMasks[i]=environmentSubstitute(meta.getTypeReplaceMask()[i]); 
					}
				
					HashSet<Integer> fieldsSelectedIndex = new HashSet<Integer>();
					for(int i=0;i<data.outputRowMeta.size();i++)
					{
						 ValueMetaInterface fieldMeta= data.outputRowMeta.getValueMeta(i);
						 if(data.ListTypes.containsKey(fieldMeta.getTypeDesc()))
						 {
							 fieldsSelectedIndex.add(i);
						 }
					}
					data.fieldnrs=new int[fieldsSelectedIndex.size()];
			        List<Integer> entries = new ArrayList<Integer>(fieldsSelectedIndex);
			        Integer fieldnr[] = (Integer[]) entries.toArray(new Integer[entries.size()]);
			        for(int i=0;i<fieldnr.length;i++)
					{
						data.fieldnrs[i]=fieldnr[i];
					}	
				}else throw new KettleException(BaseMessages.getString(PKG, "IfNull.Log.SelectValueTypesEmpty"));
			
			}else
			{
				data.realReplaceByValue=environmentSubstitute(meta.getReplaceAllByValue());
				data.realconversionMask=environmentSubstitute(meta.getReplaceAllMask());
				
				// Consider all fields in input stream
				data.fieldnrs=new int[data.outputRowMeta.size()];
				for(int i=0;i<data.outputRowMeta.size();i++)
				{
					data.fieldnrs[i]=i;
				}
			}
			data.fieldnr=data.fieldnrs.length;
		} // end if first

		try
		{
			updateFields(r);
		
			 putRow(data.outputRowMeta, r);  // copy row to output rowset(s);
		 
		}  catch(Exception e)
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
        		logError(BaseMessages.getString(PKG, "IfNull.Log.ErrorInStep",e.getMessage())); //$NON-NLS-1$
        		e.printStackTrace();
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(data.outputRowMeta, r, 1, errorMessage, null, "IFNULL001");
        	}
        }
		return true;
	}
	
	private void updateFields(Object[] r) throws Exception
	{
		// Loop through fields
		for(int i=0;i<data.fieldnr;i++){
			ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(data.fieldnrs[i]);
			if(data.outputRowMeta.getValueMeta(data.fieldnrs[i]).isNull(r[data.fieldnrs[i]]))
			{
				 if(meta.isSelectValuesType()){
					 ValueMetaInterface fieldMeta= data.outputRowMeta.getValueMeta(data.fieldnrs[i]);
					 int pos=data.ListTypes.get(fieldMeta.getTypeDesc());
					 data.realReplaceByValue=data.defaultValues[pos];	
					 data.realconversionMask=data.defaultMasks[pos];
					 replaceNull(r,sourceValueMeta, data.fieldnrs[i], data.defaultValues[pos], data.defaultMasks[pos]);
				 }else if(meta.isSelectFields()) {
					 replaceNull(r,sourceValueMeta, data.fieldnrs[i], data.defaultValues[i], data.defaultMasks[i]);
				 }else { // all
					 if (data.outputRowMeta.getValueMeta(data.fieldnrs[i]).isDate()) {
						 replaceNull(r,sourceValueMeta, data.fieldnrs[i], data.realReplaceByValue, data.realconversionMask);
					 } else { // don't use any special date format when not a date
						 replaceNull(r,sourceValueMeta, data.fieldnrs[i], data.realReplaceByValue, null);
					 }
				 }
				 
			}
		}
	}
		
	public void replaceNull(Object[] row, ValueMetaInterface sourceValueMeta, int i, String realReplaceByValue, String realconversionMask) throws Exception
	{
		// DO CONVERSION OF THE DEFAULT VALUE ...
		// Entered by user
		ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(i);
		if(!Const.isEmpty(realconversionMask)) sourceValueMeta.setConversionMask(realconversionMask);
		row[i] = targetValueMeta.convertData(sourceValueMeta, realReplaceByValue);
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(IfNullMeta)smi;
		data=(IfNullData)sdi;
		
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
    	BaseStep.runStepThread(this, meta, data);
	}
}
