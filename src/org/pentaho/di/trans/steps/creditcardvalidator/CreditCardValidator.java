/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. A copy of the license, 
 * is included with the binaries and source code. The Original Code is Samatar.  
 * The Initial Developer is Samatar.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an 
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. 
 * Please refer to the license for the specific language governing your rights 
 * and limitations.
 ***************************************************************************************/
package org.pentaho.di.trans.steps.creditcardvalidator;

import org.pentaho.di.core.Const;
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
 * Check if a Credit Card is valid
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class CreditCardValidator extends BaseStep implements StepInterface
{
	private static Class<?> PKG = CreditCardValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private CreditCardValidatorMeta meta;
    private CreditCardValidatorData data;
    
    public CreditCardValidator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(CreditCardValidatorMeta)smi;
        data=(CreditCardValidatorData)sdi;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
           
        boolean isValid=false;
        String cardType=null;
        String unValid=null;
        
     
    	if(first)
    	{
    		first=false;
			
   			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
    		// Check if field is provided
			if (Const.isEmpty(meta.getDynamicField())) {
				logError(BaseMessages.getString(PKG, "CreditCardValidator.Error.CardFieldMissing"));
				throw new KettleException(BaseMessages.getString(PKG, "CreditCardValidator.Error.CardFieldMissing"));
			}
			
			// cache the position of the field			
			if (data.indexOfField<0) {	
				data.indexOfField =getInputRowMeta().indexOfValue(meta.getDynamicField());
				if (data.indexOfField<0) {
					// The field is unreachable !
					throw new KettleException(BaseMessages.getString(PKG, "CreditCardValidator.Exception.CouldnotFindField",meta.getDynamicField())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			data.realResultFieldname=environmentSubstitute(meta.getResultFieldName());
			if(Const.isEmpty(data.realResultFieldname)) {
				throw new KettleException(BaseMessages.getString(PKG, "CreditCardValidator.Exception.ResultFieldMissing")); 
			}
			data.realCardTypeFieldname=environmentSubstitute(meta.getCardType());
			data.realNotValidMsgFieldname=environmentSubstitute(meta.getNotValidMsg());
			
			
    	}// End If first 
        	
        	
    	Object[] outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		for (int i = 0; i < data.NrPrevFields; i++)
		{
			outputRow[i] = r[i];
		}
		try
	    {
        	// get field
        	String fieldvalue= getInputRowMeta().getString(r,data.indexOfField);	
        	if(meta.isOnlyDigits()) fieldvalue=Const.getDigitsOnly(fieldvalue);

    		ReturnIndicator rt = new ReturnIndicator();
    		rt=CreditCardVerifier.CheckCC(fieldvalue);
    		// Check if Card is Valid?
    		isValid=rt.CardValid;	
    		// include Card Type?
    		if(!Const.isEmpty(data.realCardTypeFieldname)) cardType=rt.CardType;
    		// include Not valid message?
    		if(!Const.isEmpty(data.realNotValidMsgFieldname)) unValid=rt.UnValidMsg;
    		       		
        	// add card is Valid
    		outputRow[data.NrPrevFields]= isValid;
    		int rowIndex=data.NrPrevFields;
    		rowIndex++;
    		

    		// add card type?
    		if(!Const.isEmpty(data.realCardTypeFieldname)) {
    			outputRow[rowIndex++]=cardType;
    		}
    		// add not valid message?
    		if(!Const.isEmpty(data.realNotValidMsgFieldname)) {
    			outputRow[rowIndex++]=unValid;		
    		}
    		
			 //	add new values to the row.
	        putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
                
	        if(log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "CreditCardValidator.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
	        
        }
        catch(Exception e)
        {
        	if (getStepMeta().isDoingErrorHandling())
        	{
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	}
        	else
        	{
	            logError(BaseMessages.getString(PKG, "CreditCardValidator.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "CreditCardValidator001");
        	}
        }
        
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(CreditCardValidatorMeta)smi;
        data=(CreditCardValidatorData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		logError(BaseMessages.getString(PKG, "CreditCardValidator.Error.ResultFieldMissing"));
        		return false;
        	}
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (CreditCardValidatorMeta)smi;
        data = (CreditCardValidatorData)sdi;
       
        super.dispose(smi, sdi);
    }
}
