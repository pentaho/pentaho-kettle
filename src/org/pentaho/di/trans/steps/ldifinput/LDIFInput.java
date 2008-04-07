/*************************************************************************************** 
 * Copyright (C) 2007 Samatar.  All rights reserved. 
 * This software was developed by Samatar, Brahim and is provided under the terms 
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

package org.pentaho.di.trans.steps.ldifinput;

import java.util.Enumeration;

import netscape.ldap.LDAPAttribute;
import netscape.ldap.util.LDIF;
import netscape.ldap.util.LDIFAttributeContent;
import netscape.ldap.util.LDIFContent;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.ldifinput.LDIFInputField;



/**
 * Read all LDIF files, convert them to rows and writes these to one or more output streams.
 * 
 * @author Samatar
 * @since 24-05-2007
 */
public class LDIFInput extends BaseStep implements StepInterface
{
	private LDIFInputMeta meta;
	private LDIFInputData data;
	
	public LDIFInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	private Object[] getOneRow() throws KettleException
	{

		try{
			while ((data.file==null) || ((data.recordLDIF = data.InputLDIF.nextRecord())==null))
			{
		        if (!openNextFile()) return null;
			}
			
		} catch (Exception IO)
		{
			return null;
		}

		// 	Get LDIF Content
		LDIFContent contentLDIF = data.recordLDIF.getContent();
		String contentTYPE="ATTRIBUTE_CONTENT";
		

		if(contentLDIF.getType()== LDIFContent.DELETE_CONTENT)
		{
			if(log.isDetailed())
				log.logDetailed(toString(),Messages.getString("LDIFInput.Log.ContentType","DELETE_CONTENT"));
			contentTYPE="DELETE_CONTENT";
		}
		else if(contentLDIF.getType()== LDIFContent.ADD_CONTENT)
		{
			if(log.isDetailed())
				log.logDetailed(toString(),Messages.getString("LDIFInput.Log.ContentType","ADD_CONTENT"));
			contentTYPE="ADD_CONTENT";
		}
		else if(contentLDIF.getType()== LDIFContent.MODDN_CONTENT)
		{
			if(log.isDetailed())
				log.logDetailed(toString(),Messages.getString("LDIFInput.Log.ContentType","MODDN_CONTENT"));
			contentTYPE="MODDN_CONTENT";
		}
		else if(contentLDIF.getType()== LDIFContent.MODIFICATION_CONTENT)
		{
			if(log.isDetailed())
				log.logDetailed(toString(),Messages.getString("LDIFInput.Log.ContentType","MODIFICATION_CONTENT"));
			contentTYPE="MODIFICATION_CONTENT";
		}
		else 
		{
			if(log.isDetailed())
				log.logDetailed(toString(),Messages.getString("LDIFInput.Log.ContentType","ATTRIBUTE_CONTENT"));
		}
		
		
		// Get only ATTRIBUTE_CONTENT					
		LDIFAttributeContent attrContentLDIF = (LDIFAttributeContent) contentLDIF;
		data.attributes_LDIF = attrContentLDIF.getAttributes();

		 // Build an empty row based on the meta-data		  
		 Object[] outputRowData=buildEmptyRow();

		 try{	
			
				// Execute for each Input field...
				for (int i=0;i<meta.getInputFields().length;i++)
				{
					LDIFInputField ldifInputField = meta.getInputFields()[i];
					// Get the Attribut to look for
					String AttributValue = environmentSubstitute(ldifInputField.getAttribut());
					
					String Value=GetValue(data.attributes_LDIF ,AttributValue);
				
					// Do trimming
					switch (ldifInputField.getTrimType())
					{
					case LDIFInputField.TYPE_TRIM_LEFT:
						Value = Const.ltrim(Value);
						break;
					case LDIFInputField.TYPE_TRIM_RIGHT:
						Value = Const.rtrim(Value);
						break;
					case LDIFInputField.TYPE_TRIM_BOTH:
						Value = Const.trim(Value);
						break;
					default:
						break;
					}
						      
					
					// Do conversions
					//
					ValueMetaInterface targetValueMeta = data.outputRowMeta.getValueMeta(+i);
					ValueMetaInterface sourceValueMeta = data.convertRowMeta.getValueMeta(+i);
					outputRowData[i] = targetValueMeta.convertData(sourceValueMeta, Value);

					// Do we need to repeat this field if it is null?
					if (meta.getInputFields()[i].isRepeated())
					{
						if (data.previousRow!=null && Const.isEmpty(Value))
						{
							outputRowData[i] = data.previousRow[i];
						}
					}
				}    // End of loop over fields...
				int rowIndex = data.nrInputFields;
				
				// See if we need to add the filename to the row...
				if ( meta.includeFilename() && !Const.isEmpty(meta.getFilenameField()) ) {
					outputRowData[rowIndex++] = KettleVFS.getFilename(data.file);
				}
				 // See if we need to add the row number to the row...  
		        if (meta.includeRowNumber() && !Const.isEmpty(meta.getRowNumberField()))
		        {
		            outputRowData[rowIndex++] = new Long(data.rownr);
		        }
		        
				 // See if we need to add the content type to the row...  
		        if (meta.includeContentType()&& !Const.isEmpty(meta.getContentTypeField()))
		        {
		            outputRowData[rowIndex++] = contentTYPE;
		        }
				
				RowMetaInterface irow = getInputRowMeta();
				
				data.previousRow = irow==null?outputRowData:(Object[])irow.cloneRow(outputRowData); // copy it to make
				// surely the next step doesn't change it in between...
				
				 linesInput++;
				 data.rownr++;
			
		 }
		 catch (Exception e)
		 {
			 
			throw new KettleException("Unable to read row from LDIF file", e);
			
		 }
		 
		return outputRowData;
	}
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{

		Object[] r=null;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;
		 
		try{
			 // Grab one row
			 Object[] outputRowData=getOneRow();
			 if (outputRowData==null)
		     {
		        setOutputDone();  // signal end to receiver(s)
		        return false; // end of data or error.
		     }
	 
			 putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);

			  if (meta.getRowLimit()>0 && data.rownr>meta.getRowLimit())  // limit has been reached: stop now.
		      {
		            setOutputDone();
		            return false;
		      }	
		}catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
	        {
                sendToErrorRow = true;
                errorMessage = e.toString();
	        }
			else
			{
				logError(Messages.getString("LDIFInput.ErrorInStepRunning",e.getMessage())); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
				putError(getInputRowMeta(), r, 1, errorMessage, null, "LDIFInput001");
	         }
		}
		 return true;
	}		
	
	private boolean openNextFile()
	{
		try
		{
		    if (data.filenr>=data.files.nrOfFiles()) // finished processing!
            {
            	if (log.isDetailed()) logDetailed(Messages.getString("LDIFInput.Log.FinishedProcessing"));
                return false;
            }
            
		    // Is this the last file?
			data.last_file = ( data.filenr==data.files.nrOfFiles()-1);
			data.file = (FileObject) data.files.getFile(data.filenr);
			
			// Move file pointer ahead!
			data.filenr++;
						

			if (log.isDetailed()) logDetailed(Messages.getString("LDIFInput.Log.OpeningFile", data.file.toString()));
    
			if(meta.AddToResultFilename())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(Messages.getString("LDIFInput.Log.FileAddedResult"));
				addResultFile(resultFile);
			}

			data.InputLDIF = new LDIF(KettleVFS.getFilename(data.file));
	
	        if (log.isDetailed()) logDetailed(Messages.getString("LDIFInput.Log.FileOpened", data.file.toString()));

		}
		catch(Exception e)
		{
			logError(Messages.getString("LDIFInput.Log.UnableToOpenFile", ""+data.filenr, data.file.toString(), e.toString()));
			stopAll();
			setErrors(1);
			return false;
		}
		return true;
	}
	private String GetValue(LDAPAttribute[] attributes_LDIF ,String AttributValue)
	{
		String Stringvalue=null;
		int i=0;
        
		for (int j = 0; j < attributes_LDIF.length; j++) 
		{
			LDAPAttribute attribute_DIF = attributes_LDIF[j];
			if (attribute_DIF.getName().equalsIgnoreCase(AttributValue))
			{
				Enumeration valuesLDIF = attribute_DIF.getStringValues();
				
				while (valuesLDIF.hasMoreElements()) 
				{
					String valueLDIF = (String) valuesLDIF.nextElement();
					if (i==0)	
						Stringvalue=  valueLDIF;
					else
						Stringvalue= Stringvalue + data.multiValueSeparator + valueLDIF;	
					i++;
				}
			}
		}
		return Stringvalue;
	}

	/**
	 * Build an empty row based on the meta-data.
	 * 
	 * @return
	 */
	private Object[] buildEmptyRow()
	{
       Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());

	    return rowData;
	}

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDIFInputMeta)smi;
		data=(LDIFInputData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.files = meta.getFiles(this);
			if (data.files.nrOfFiles() == 0 && data.files.nrOfMissingFiles() == 0)
			{
				logError(Messages.getString("LDIFInput.Log.NoFiles"));
				return false;
			}
			try
			{
				// Create the output row meta-data
	            data.outputRowMeta = new RowMeta();
	
				meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
				
				// Create convert meta-data objects that will contain Date & Number formatters
	            data.convertRowMeta = data.outputRowMeta.clone();
	            for (int i=0;i<data.convertRowMeta.size();i++) data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);
	  
	            // For String to <type> conversions, we allocate a conversion meta data row as well...
				//
				data.convertRowMeta = data.outputRowMeta.clone();
				for (int i=0;i<data.convertRowMeta.size();i++) {
					data.convertRowMeta.getValueMeta(i).setType(ValueMetaInterface.TYPE_STRING);            
				}
	            
				data.rownr = 1L;
				data.nrInputFields=meta.getInputFields().length;
				data.multiValueSeparator=environmentSubstitute(meta.getMultiValuedSeparator());
			}catch(Exception e)
			{
				logError("Error initializing step: "+e.toString());
				logError(Const.getStackTracker(e));
				return false;
			}
			
			return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(LDIFInputMeta)smi;
		data=(LDIFInputData)sdi;
		if(data.InputLDIF!=null) data.InputLDIF=null;
		if(data.attributes_LDIF!=null) data.attributes_LDIF=null;
		if(data.recordLDIF!=null) data.recordLDIF=null;
		super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!	
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}