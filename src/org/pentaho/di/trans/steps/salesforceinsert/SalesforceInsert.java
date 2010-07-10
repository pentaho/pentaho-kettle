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
 
package org.pentaho.di.trans.steps.salesforceinsert;

import java.util.ArrayList;

import com.sforce.soap.partner.sobject.SObject;

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
 * @author jstairs,Samatar
 * @since 10-06-2007
 */
public class SalesforceInsert extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SalesforceInsertMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SalesforceInsertMeta meta;
	private SalesforceInsertData data;
		
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

	public SalesforceInsert(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
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
				throw new KettleException(BaseMessages.getString(PKG, "SalesforceInsertDialog.FieldsMissing.DialogMessage"));
			}
 
			// Create the output row meta-data
			data.inputRowMeta = getInputRowMeta().clone();
	        data.outputRowMeta = data.inputRowMeta.clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			// Build the mapping of input position to field name
			data.fieldnrs = new int[meta.getUpdateStream().length];
			for (int i = 0; i < meta.getUpdateStream().length; i++)
			{
				data.fieldnrs[i] = getInputRowMeta().indexOfValue(meta.getUpdateStream()[i]);
				if (data.fieldnrs[i] < 0)
				{
					throw new KettleException(BaseMessages.getString(PKG, "SalesforceInsert.CanNotFindField", meta.getUpdateStream()[i]));
				}
			 }
		}

		try 
		{	
			writeToSalesForce(outputRowData);

		} 
		catch(Exception e)
		{
			throw new KettleStepException(BaseMessages.getString(PKG, "SalesforceInsert.log.Exception", e.getMessage()), e);
		} 
	    return true; 
	}		
	
	private void writeToSalesForce(Object[] rowData) throws KettleException
	{
		try {			

			if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceInsert.WriteToSalesforce", data.iBufferPos, meta.getBatchSizeInt()));
			
			// if there is room in the buffer
			if ( data.iBufferPos < meta.getBatchSizeInt()) {
				ArrayList<MessageElement> insertfields = new ArrayList<MessageElement>();
				for ( int i = 0; i < data.nrfields; i++) {
					if(!data.inputRowMeta.isNull(rowData, data.fieldnrs[i])) {
						insertfields.add(newMessageElement( meta.getUpdateLookup()[i], rowData[data.fieldnrs[i]]));
					}
				}				
				
				//build the SObject
				SObject	sobjPass = new SObject();
				sobjPass.set_any((MessageElement[])insertfields.toArray(new MessageElement[insertfields.size()]));
				sobjPass.setType(data.realModule);

				//Load the buffer array
				data.sfBuffer[data.iBufferPos] = sobjPass;
				data.outputBuffer[data.iBufferPos] = rowData;
				data.iBufferPos++;
			}
			
			if ( data.iBufferPos >= meta.getBatchSizeInt()) {
				if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceInsert.CallingFlushBuffer"));
				flushBuffers();
			}
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceInsert.Error", e.getMessage()));	
		}
	}
	
	private void flushBuffers() throws KettleException
	{	
		try {
			// create the object(s) by sending the array to the web service
			data.saveResult = data.connection.insert(data.sfBuffer);
			for (int j = 0; j < data.saveResult.length; j++) {
				if (data.saveResult[j].isSuccess()) {
					// Row was inserted
					String id=data.saveResult[j].getId();
					if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "SalesforceInsert.RowInserted", id));

					// write out the row with the SalesForce ID
					Object[] newRow = RowDataUtil.resizeArray(data.outputBuffer[j], data.outputRowMeta.size());
					
					if(data.realSalesforceFieldName!=null) {
						int newIndex = getInputRowMeta().size();
						newRow[newIndex++] = id;
					}
					if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceInsert.NewRow", newRow[0]));
					
					//putRow(data.outputRowMeta, data.outputRowMeta.cloneRow(newRow));  // copy row to output rowset(s);
					putRow(data.outputRowMeta, newRow);  // copy row to output rowset(s);
					incrementLinesOutput();
					
				    if (checkFeedback(getLinesInput()))
				    {
				    	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceInsert.log.LineRow", getLinesInput()));
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
				         errorMessage = "";
				         for (int i = 0; i < data.saveResult[j].getErrors().length; i++) {
								// get the next error
								com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[i];
								errorMessage+= BaseMessages.getString(PKG, "SalesforceInsert.Error.FlushBuffer", 
										new Integer(j), err.getStatusCode(), err.getMessage());
						}
					}
					else 
					{
						if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SalesforceInsert.ErrorFound")); 
						//for (int i = 0; i < data.saveResult[j].getErrors().length; i++) {
						
						// Only show the first error
							com.sforce.soap.partner.Error err = data.saveResult[j].getErrors()[0];
							throw new KettleException(BaseMessages.getString(PKG, "SalesforceInsert.Error.FlushBuffer", 
									new Integer(j), err.getStatusCode(), err.getMessage()));	
					}
					
					if (sendToErrorRow) {
						   // Simply add this row to the error row
						if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "SalesforceInsert.PassingRowToErrorStep"));
						   putError(getInputRowMeta(), data.outputBuffer[j], 1, errorMessage, null, "SalesforceInsert001");
					}
				} 
				
			} 
			
			// reset the buffers
			data.sfBuffer = new SObject[meta.getBatchSizeInt()];
			data.outputBuffer = new Object[meta.getBatchSizeInt()][];
			data.iBufferPos = 0;
			
		} catch (Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "SalesforceInsert.FailedToInsertObject", e.getMessage()));
		} finally{
			if(data.saveResult!=null) data.saveResult=null;
		}

	} 
	
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SalesforceInsertMeta)smi;
		data=(SalesforceInsertData)sdi;
		
		if (super.init(smi, sdi))
		{
			
			try
		    {
				data.realModule=environmentSubstitute(meta.getModule());
				// Check if module is specified 
				if (Const.isEmpty(data.realModule))
				{    
					log.logError(BaseMessages.getString(PKG, "SalesforceInsertDialog.ModuleMissing.DialogMessage"));
					return false;
				}
				 
				String realUser = environmentSubstitute(meta.getUserName());
				 // Check if username is specified 
				if (Const.isEmpty(realUser))
				{
					log.logError(BaseMessages.getString(PKG, "SalesforceInsertDialog.UsernameMissing.DialogMessage"));
					return false;
				}

				String salesfoceIdFieldname= environmentSubstitute(meta.getSalesforceIDFieldName());
				if(!Const.isEmpty(salesfoceIdFieldname)) {
					data.realSalesforceFieldName=salesfoceIdFieldname;
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
				logError(BaseMessages.getString(PKG, "SalesforceInsert.Log.ErrorOccurredDuringStepInitialize")+ke.getMessage()); //$NON-NLS-1$
			}
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi){
		meta=(SalesforceInsertMeta)smi;
		data=(SalesforceInsertData)sdi;
		try{
			if(data.outputBuffer!=null) data.outputBuffer=null;
			if(data.sfBuffer!=null) data.sfBuffer=null;
			if(data.connection!=null) data.connection.close();
		}catch(Exception e){};
		super.dispose(smi, sdi);
	}

}