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
 
package org.pentaho.di.trans.steps.salesforceoutput;

import com.sforce.soap.partner.sobject.SObject;
import com.sforce.soap.partner.UpsertResult;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
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
 * @author Samatar
 * @since 10-06-2007
 */
public class SalesforceOutput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SalesforceOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SalesforceOutputMeta meta;
	private SalesforceOutputData data;
		
	private MessageElement newMessageElement(String name, Object value) throws Exception {

			MessageElement me = new MessageElement("", name); 
			me.setObjectValue(value);
			Element e = me.getAsDOM();
			e.removeAttribute("xsi:type");
			e.removeAttribute("xmlns:ns1");
			e.removeAttribute("xmlns:xsd");
			e.removeAttribute("xmlns:xsi");

			me = new MessageElement(e);
			return me;
	}

	public SalesforceOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
			
			data.sfBuffer = new SObject[meta.getBatchSizeInt()];
			data.outputBuffer = new Object[meta.getBatchSizeInt()][];
			 
			// get total fields in the grid
			data.nrfields = meta.getUpdateLookup().length;
				
			// Check if field list is filled 
			if (data.nrfields==0)
			{
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceOutputDialog.FieldsMissing.DialogMessage"));
			}
 
			// Create the output row meta-data
	        data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			// Set the formatRowMeta as the meta data for the input rows
			data.formatRowMeta = getInputRowMeta().clone();
			
			 // Build the mapping of input position to field name
			 data.fieldnrs = new int[meta.getUpdateStream().length];
			 for (int i = 0; i < meta.getUpdateStream().length; i++)
			 {
				data.fieldnrs[i] = data.formatRowMeta.indexOfValue(meta.getUpdateStream()[i]);
				if (data.fieldnrs[i] < 0)
				{
					throw new KettleException("Field [" + meta.getUpdateStream()[i]+ "] couldn't be found in the input stream!");
				}
			 }
		}

		try 
		{	
			writeToSalesForce(outputRowData);

		} 
		catch(Exception e)
		{
			throw new KettleStepException(BaseMessages.getString(PKG, "SalesforceOutput.log.Exception"), e);
		} 
	    return true; 
	}		
	
	private void writeToSalesForce(Object[] rowData) throws KettleException
	{
		try {			

			if (log.isDetailed()) logDetailed("Called writeToSalesForce with " + data.iBufferPos + " out of " + meta.getBatchSizeInt());
			
			// if there is room in the buffer
			if ( data.iBufferPos < meta.getBatchSizeInt()) {
				// build the XML node
				MessageElement[] arNode = new MessageElement[data.nrfields];
				int index=0;
				for ( int i = 0; i < data.nrfields; i++) {
					arNode[index++] = newMessageElement( meta.getUpdateLookup()[i], rowData[data.fieldnrs[i]]);
				}				
				
				//build the SObject
				SObject	sobjPass = new SObject();
				sobjPass.set_any(arNode);
				sobjPass.setType(data.Module);
				
				//Load the buffer array
				data.sfBuffer[data.iBufferPos] = sobjPass;
				data.outputBuffer[data.iBufferPos] = rowData;
				data.iBufferPos++;
			} // if for space in buffer
			
			if ( data.iBufferPos >= meta.getBatchSizeInt()) {
				if (log.isDetailed()) logDetailed("Calling flush buffer from writeToSalesForce");
				flushBuffers();
			}
		} catch (Exception e) {
			throw new KettleException("\nFailed in writeToSalesForce: "+ e.getMessage());	
		}
	}
	
	private void flushBuffers() throws KettleException
	{
		
		try {
			// create the object(s) by sending the array to the web service
			UpsertResult[] sr = data.connection.upsert(meta.getUpsertField(), data.sfBuffer);
			for (int j = 0; j < sr.length; j++) {
				if (sr[j].isSuccess()) {
					
					if (log.isDetailed()) logDetailed("An account was create with an id of: " + sr[j].getId());
					
					// write out the row with the SalesForce ID
					Object[] newRow = RowDataUtil.resizeArray(data.outputBuffer[j], data.outputRowMeta.size());
					int newIndex = getInputRowMeta().size();
					newRow[newIndex++] = sr[j].getId();
					if (log.isDetailed()) logDetailed("The new row has an id value of : " + newRow[0]);
					
					putRow(data.outputRowMeta, data.outputRowMeta.cloneRow(newRow));  // copy row to output rowset(s);
					incrementLinesOutput();
					
				    if (checkFeedback(getLinesInput()))
				    {
				    	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceOutput.log.LineRow",""+ getLinesInput()));
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
				         for (int i = 0; i < sr[j].getErrors().length; i++) {
								// get the next error
								com.sforce.soap.partner.Error err = sr[j].getErrors()[i];
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
						for (int i = 0; i < sr[j].getErrors().length; i++) {
							// get the next error
							com.sforce.soap.partner.Error err = sr[j].getErrors()[i];
							throw new KettleException("Errors were found on item "
									+ new Integer(j).toString()
									+ " Error code is: "
									+ err.getStatusCode().toString()
									+ " Error message: " + err.getMessage());
						} // for error messages
					}  // end of determining if pass error messages on or not.
					
					if (sendToErrorRow) {
						   // Simply add this row to the error row
						if(log.isDetailed()) logDetailed("Passing row to error step");
						   putError(getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceOutput001");
						}
				} // error handling
				
			} // for
			
			// reset the buffers
			data.sfBuffer = new SObject[meta.getBatchSizeInt()];
			data.outputBuffer = new Object[meta.getBatchSizeInt()][];
			data.iBufferPos = 0;
			
		} catch (Exception e) {
			throw new KettleException("\nFailed to upsert object, error message was: \n"+ e.getMessage());
		} 

	} // flushBuffers
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SalesforceOutputMeta)smi;
		data=(SalesforceOutputData)sdi;
		
		if (super.init(smi, sdi))
		{
			
			try
		    {
				data.Module=environmentSubstitute(meta.getModule());
				// Check if module is specified 
				if (Const.isEmpty(data.Module))
				{    
					log.logError(BaseMessages.getString(PKG, "SalesforceOutputDialog.ModuleMissing.DialogMessage"));
					return false;
				}
				 
				String realUser = environmentSubstitute(meta.getUserName());
				 // Check if username is specified 
				if (Const.isEmpty(realUser))
				{
					log.logError(BaseMessages.getString(PKG, "SalesforceOutputDialog.UsernameMissing.DialogMessage"));
					return false;
				}

				// initialize variables
				data.realURL=environmentSubstitute(meta.getTargetURL());
				// create a Salesforce connection
				data.connection= new SalesforceConnection(log, data.realURL, realUser,
					environmentSubstitute(meta.getPassword()), 0);
				
				// Now connect ...
				data.connection.connect();

				 return true;
			}
			catch(KettleException ke)
			{
				logError(BaseMessages.getString(PKG, "InsertUpdate.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		meta=(SalesforceOutputMeta)smi;
		data=(SalesforceOutputData)sdi;
		try{
			if(data.connection!=null) data.connection.close();
		}catch(Exception e){};
		super.dispose(smi, sdi);
	}
	//
	// Run is were the action happens!	
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}