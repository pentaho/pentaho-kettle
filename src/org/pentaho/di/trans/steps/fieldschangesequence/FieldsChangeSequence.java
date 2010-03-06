 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package org.pentaho.di.trans.steps.fieldschangesequence;

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



/**
 * Add sequence to each input row.
 * 
 * @author Samatar
 * @since 30-06-2008
 */

public class FieldsChangeSequence extends BaseStep implements StepInterface
{
	private static Class<?> PKG = FieldsChangeSequenceMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private FieldsChangeSequenceMeta meta;
	private FieldsChangeSequenceData data;
	
	public FieldsChangeSequence(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FieldsChangeSequenceMeta)smi;
		data=(FieldsChangeSequenceData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			// get the RowMeta
			data.previousMeta = getInputRowMeta().clone();
			data.nextIndexField = data.previousMeta .size();
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			if(meta.getFieldName()==null || meta.getFieldName().length>0)
			{
				data.fieldnr=meta.getFieldName().length;
				data.fieldnrs=new int[data.fieldnr];
				data.previousValues=new Object[data.fieldnr];
				
				for (int i=0;i<data.fieldnr;i++)
				{
					data.fieldnrs[i]=data.previousMeta.indexOfValue(meta.getFieldName()[i] );
					if (data.fieldnrs[i]<0)
					{
						logError(BaseMessages.getString(PKG, "FieldsChangeSequence.Log.CanNotFindField",meta.getFieldName()[i]));
						throw new KettleException(BaseMessages.getString(PKG, "FieldsChangeSequence.Log.CanNotFindField",meta.getFieldName()[i]));
					}
	 			}
			}else
			{
				data.fieldnr=data.previousMeta.size();
				data.fieldnrs=new int[data.fieldnr];
				data.previousValues=new Object[data.fieldnr];
				for(int i=0;i<data.previousMeta.size();i++)
				{
					data.fieldnrs[i]=i;
				}
			}
			
			data.startAt=Const.toInt(environmentSubstitute(meta.getStart()), 1);
			data.incrementBy=Const.toInt(environmentSubstitute(meta.getIncrement()), 1);
			data.seq=data.startAt;
		} // end if first
		

		try
		{
			boolean change=false;
			
		   	// Loop through fields
			for(int i=0;i<data.fieldnr;i++)
			{
				if(!first)
				{
					if(!data.previousValues[i].equals(r[data.fieldnrs[i]])) change=true;
				}
				data.previousValues[i]=r[data.fieldnrs[i]];
			}
			if(first) first=false;
			
			if(change) data.seq=data.startAt;
			
		    if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "FieldsChangeSequence.Log.ReadRow")+getLinesRead()+" : "+getInputRowMeta().getString(r)); //$NON-NLS-1$ //$NON-NLS-2$

			
			// build a value!	
			r[data.nextIndexField]=data.seq;
			
			putRow(data.outputRowMeta, r);     // copy row to possible alternate rowset(s).
			data.seq+=data.incrementBy;		
			
	        if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "FieldsChangeSequence.Log.WriteRow")+getLinesWritten()+" : "+getInputRowMeta().getString(r)); //$NON-NLS-1$ //$NON-NLS-2$
			
	        if (checkFeedback(getLinesRead())) 
			{
				if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "FieldsChangeSequence.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			}
			
		}catch(Exception e) {
	        boolean sendToErrorRow=false;
	        String errorMessage = null;
        	if (getStepMeta().isDoingErrorHandling())
        	{
                sendToErrorRow = true;
                errorMessage = e.toString();
        	}
        	else
        	{
	            logError(BaseMessages.getString(PKG, "FieldsChangeSequence.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(),r, 1, errorMessage, meta.getResultFieldName(), "FieldsChangeSequence001");
        	}
        }
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FieldsChangeSequenceMeta)smi;
		data=(FieldsChangeSequenceData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FieldsChangeSequenceMeta)smi;
		data=(FieldsChangeSequenceData)sdi;

		data.previousValues=null;
		data.fieldnrs=null;
		super.dispose(smi, sdi);
	}	
}
