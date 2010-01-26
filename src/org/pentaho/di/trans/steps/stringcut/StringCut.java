/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.stringcut;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Cut strings.
 * 
 * @author Samatar Hassan
 * @since 30 September 2008
 */
public class StringCut extends BaseStep implements StepInterface {
	private static Class<?> PKG = StringCutMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private StringCutMeta meta;

	private StringCutData data;

	public StringCut(StepMeta stepMeta, StepDataInterface stepDataInterface,
			int copyNr, TransMeta transMeta, Trans trans) {
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	private String CutString(String string, int cutFrom, int cutTo) {
		String rcode = string;

		if(!Const.isEmpty(rcode))
		{
			int lenCode=rcode.length();
			
			if((cutFrom>=0 && cutTo>=0) && cutFrom>lenCode) 
				rcode=null;		
			else if((cutFrom>=0 && cutTo>=0) && (cutTo<cutFrom)) 
				rcode=null;
			else if((cutFrom<0 && cutTo<0) && cutFrom<-lenCode) 
				rcode=null;
			else if((cutFrom<0 && cutTo<0) && (cutFrom<cutTo)) 
				rcode=null;
			else
			{
				if(cutTo>lenCode) cutTo=lenCode;
				if(cutTo<0 && cutFrom ==0  && (-cutTo)>lenCode) cutTo=-(lenCode);
				if(cutTo<0 && cutFrom<0 && (-cutTo)>lenCode) cutTo=-(lenCode);
				
				if (cutFrom >= 0 && cutTo>0) 
					rcode = rcode.substring(cutFrom,cutTo);
				else if (cutFrom < 0 && cutTo<0)
					rcode = rcode.substring(rcode.length()+cutTo,lenCode+cutFrom);
				else if (cutFrom ==0 && cutTo<0) {
					int intFrom=rcode.length()+cutTo;
					rcode = rcode.substring(intFrom,lenCode);
				}
			}
		}

		return rcode;
	}

	private synchronized Object[] getOneRow(RowMetaInterface rowMeta,Object[] row) 
	throws KettleException {
		Object[] RowData = new Object[data.outputRowMeta.size()];

		// Copy the input fields.
		System.arraycopy(row, 0, RowData, 0, rowMeta.size());
		int length = meta.getFieldInStream().length;

		for (int i = 0; i < length; i++) {
				String value=CutString((String) row[data.inStreamNrs[i]],data.cutFrom[i],data.cutTo[i]);
				if(Const.isEmpty(data.outStreamNrs[i])) 
					RowData[data.inStreamNrs[i]]=value;
				else
					RowData[data.inputFieldsNr+i]=value;	
		}
		return RowData;
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
			throws KettleException {
		meta = (StringCutMeta) smi;
		data = (StringCutData) sdi;

		Object[] r = getRow(); // Get row from input rowset & set row busy!
		if (r == null) // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		if (first) {
			first = false;
			// What's the format of the output row?
			data.outputRowMeta = getInputRowMeta().clone();
			data.inputFieldsNr=data.outputRowMeta.size();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			data.inStreamNrs = new int[meta.getFieldInStream().length];
			for (int i = 0; i < meta.getFieldInStream().length; i++) {
				data.inStreamNrs[i] = getInputRowMeta().indexOfValue(meta.getFieldInStream()[i]);
				if (data.inStreamNrs[i] < 0) // couldn't find field!
					throw new KettleStepException(BaseMessages.getString(PKG, "StringCut.Exception.FieldRequired", meta.getFieldInStream()[i])); //$NON-NLS-1$ //$NON-NLS-2$
				
				// check field type
				if(getInputRowMeta().getValueMeta(data.inStreamNrs[i]).getType()!=ValueMeta.TYPE_STRING)
					throw new KettleStepException(BaseMessages.getString(PKG, "StringCut.Exception.FieldTypeNotString", meta.getFieldInStream()[i]));
			}
			
			data.outStreamNrs = new String[meta.getFieldInStream().length];
			for (int i = 0; i < meta.getFieldInStream().length; i++) {
				data.outStreamNrs[i] = environmentSubstitute(meta.getFieldOutStream()[i]);
			}
			
			data.cutFrom = new int[meta.getFieldInStream().length];
			data.cutTo = new int[meta.getFieldInStream().length];
			for (int i = 0; i < meta.getFieldInStream().length; i++) {
				if (Const.isEmpty(meta.getCutFrom()[i]))
					data.cutFrom[i] = 0;
				else
					data.cutFrom[i]= Const.toInt(meta.getCutFrom()[i],0);
				
				 if (Const.isEmpty(meta.getCutTo()[i])) 
					data.cutTo[i] = 0;
				else
					data.cutTo[i] =  Const.toInt(meta.getCutTo()[i],0);
				 
			} // end for
		} // end if first
		
        
		try 
		{
			Object[] output = getOneRow(getInputRowMeta(),r);
			putRow(data.outputRowMeta, output);

			if (checkFeedback(getLinesRead()))
			{
				if(log.isDetailed())
					logDetailed(BaseMessages.getString(PKG, "StringCut.Log.LineNumber") + getLinesRead()); //$NON-NLS-1$
				
			}
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
        		logError(BaseMessages.getString(PKG, "StringCut.Log.ErrorInStep",e.getMessage())); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
        	}
        	if (sendToErrorRow){
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, null, "StringCut001");
        	}
		}
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
		boolean rCode = true;

		meta = (StringCutMeta) smi;
		data = (StringCutData) sdi;

		if (super.init(smi, sdi)) {

			return rCode;
		}
		return false;
	}

	public void dispose(StepMetaInterface smi, StepDataInterface sdi) {
		meta = (StringCutMeta) smi;
		data = (StringCutData) sdi;

		super.dispose(smi, sdi);
	}

}