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
 
package org.pentaho.di.trans.steps.salesforcedelete;


import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.salesforceinput.SalesforceConnection;
import org.pentaho.di.i18n.BaseMessages;



/**
 * Read data from Salesforce module, convert them to rows and writes these to one or more output streams.
 * 
 * @author jstairs,Samatar
 * @since 10-06-2007
 */
public class SalesforceDelete extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SalesforceDeleteMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SalesforceDeleteMeta meta;
	private SalesforceDeleteData data;
		

	public SalesforceDelete(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{

	 	// get one row ... This does some basic initialization of the objects, including loading the info coming in
		Object[] outputRowData = getRow(); 
		
		if(outputRowData==null)
		{
			if ( data.iBufferPos > 0 ) 
			{
				flushBuffers();
			}
			setOutputDone();
			return false;
		}
		
		// If we haven't looked at a row before then do some basic setup.
		if(first)
		{
			first=false;
			
			data.deleteId = new String[meta.getBatchSizeInt()];
			data.outputBuffer = new Object[meta.getBatchSizeInt()][];
			 
			// Create the output row meta-data
	        data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// Check deleteKeyField
			String realFieldName= environmentSubstitute(meta.getDeleteField());
			if(Const.isEmpty(realFieldName)) {
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceDelete.Error.DeleteKeyFieldMissing"));
			}
			
			// return the index of the field in the input stream
			data.indexOfKeyField= getInputRowMeta().indexOfValue(realFieldName);
			if(data.indexOfKeyField<0) {
				// the field is unreachable!
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceDelete.Error.CanNotFindFDeleteKeyField", realFieldName));
			}
		}

		try 
		{	
			writeToSalesForce(outputRowData);
		} 
		catch(Exception e)
		{
			throw new KettleStepException(BaseMessages.getString(PKG, "SalesforceDelete.log.Exception"), e);
		} 
	    return true; 
	}		
	
	private void writeToSalesForce(Object[] rowData) throws KettleException
	{
		try {			

			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceDelete.Log.WriteToSalesforce", data.iBufferPos , meta.getBatchSizeInt()));
			
			// if there is room in the buffer
			if ( data.iBufferPos < meta.getBatchSizeInt()) {

				//Load the buffer array
				data.deleteId[data.iBufferPos] = getInputRowMeta().getString(rowData,data.indexOfKeyField);
				data.outputBuffer[data.iBufferPos] = rowData;
				data.iBufferPos++;
			}
			
			if ( data.iBufferPos >= meta.getBatchSizeInt()) {
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceDelete.Log.CallingFlush"));
				flushBuffers();
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceDelete.Error.WriteToSalesforce", e.getMessage()));	
		}
	}
	
	private void flushBuffers() throws KettleException
	{
		
		try {
			// create the object(s) by sending the array to the web service
			data.deleteResult = data.connection.delete(data.deleteId);
			for (int j = 0; j < data.deleteResult.length; j++) {
				if (data.deleteResult[j].isSuccess()) {

					putRow(data.outputRowMeta, data.outputBuffer[j]);  // copy row to output rowset(s);
					incrementLinesOutput();
					
				    if (checkFeedback(getLinesInput()))
				    {
				    	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceDelete.log.LineRow",""+ getLinesInput()));
				    }

				} else {
					// there were errors during the create call, go through the
					// errors
					// array and write them to the screen
			        boolean sendToErrorRow=false;
					String errorMessage = null;
					
					if (getStepMeta().isDoingErrorHandling())
					{
				         sendToErrorRow = true;
				         errorMessage = null;
				         for (int i = 0; i < data.deleteResult[j].getErrors().length; i++) {
								// get the next error
								com.sforce.soap.partner.Error err = data.deleteResult[j].getErrors()[i];
								errorMessage = errorMessage 
										+ ": Errors were found on item "
										+ new Integer(j).toString()
										+ " Error code is: "
										+ err.getStatusCode().toString()
										+ " Error message: " + err.getMessage();
						}
					}
					else 
					{
						if(log.isDetailed()) logDetailed("Found error from SalesForce and raising the exception");
						
						// for (int i = 0; i < data.deleteResult[j].getErrors().length; i++) {
						//	Just throw the first error...
						///
						
							com.sforce.soap.partner.Error err = data.deleteResult[j].getErrors()[0];
							throw new KettleException("Errors were found on item "
									+ new Integer(j).toString()
									+ " Error code is: "
									+ err.getStatusCode().toString()
									+ " Error message: " + err.getMessage());
							
						// } // for error messages
					}
					
					if (sendToErrorRow) {
						   // Simply add this row to the error row
						if(log.isDetailed()) logDetailed("Passing row to error step");
						   putError(getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceDelete001");
						}
				} 
				
			} 
			
			// reset the buffers
			data.deleteId = new String[meta.getBatchSizeInt()];
			data.outputBuffer = new Object[meta.getBatchSizeInt()][];
			data.iBufferPos = 0;
			
		} catch (Exception e) {
			throw new KettleException("\nFailed to upsert object, error message was: \n"+ e.getMessage());
		} finally {
			if(data.deleteResult!=null) data.deleteResult=null;
		}

	} 
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SalesforceDeleteMeta)smi;
		data=(SalesforceDeleteData)sdi;
		
		if (super.init(smi, sdi))
		{
			
			try
		    {
				data.realModule=environmentSubstitute(meta.getModule());
				// Check if module is specified 
				if (Const.isEmpty(data.realModule))
				{    
					log.logError(BaseMessages.getString(PKG, "SalesforceDeleteDialog.ModuleMissing.DialogMessage"));
					return false;
				}
				 
				String realUser = environmentSubstitute(meta.getUserName());
				 // Check if username is specified 
				if (Const.isEmpty(realUser))
				{
					log.logError(BaseMessages.getString(PKG, "SalesforceDeleteDialog.UsernameMissing.DialogMessage"));
					return false;
				}
				
				// initialize variables
				data.realURL=environmentSubstitute(meta.getTargetURL());
				// create a Salesforce connection
				data.connection= new SalesforceConnection(log, data.realURL, realUser,
					environmentSubstitute(meta.getPassword()));
				
				// Now connect ...
				data.connection.connect();

				 return true;
			}
			catch(KettleException ke)
			{
				logError(BaseMessages.getString(PKG, "SalesforceDelete.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		meta=(SalesforceDeleteMeta)smi;
		data=(SalesforceDeleteData)sdi;
		try{
			if(data.outputBuffer!=null) data.outputBuffer=null;
			if(data.deleteId!=null) data.deleteId=null;
			if(data.connection!=null) data.connection.close();
		}catch(Exception e){};
		super.dispose(smi, sdi);
	}

}