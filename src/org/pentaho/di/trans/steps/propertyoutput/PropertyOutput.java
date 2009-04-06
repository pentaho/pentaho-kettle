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

package org.pentaho.di.trans.steps.propertyoutput;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.addsequence.Messages;
import org.pentaho.di.core.vfs.KettleVFS;


import org.apache.commons.vfs.FileObject;
import java.util.Properties;
import java.io.OutputStream;



/**
 * Output rows to Properties file and create a file.
 * 
 * @author Samatar
 * @since 13-Apr-2008
 */
 
public class PropertyOutput extends BaseStep implements StepInterface
{
	private PropertyOutputMeta meta;
	private PropertyOutputData data;
	

	public PropertyOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(PropertyOutputMeta)smi;
		data=(PropertyOutputData)sdi;
		
		
		Object[] r=getRow();    // this also waits for a previous step to be finished.
	

		if (r==null)  // no more input to be expected...
		{
			if(!first)
			{
				// Save properties to file
				saveProperties(data.pro);
			}
			setOutputDone();
			return false;
		}
		
		if(first)
		{
			first=false;
			data.inputRowMeta = getInputRowMeta();
            data.outputRowMeta = data.inputRowMeta.clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
			// Let's take the index of Key field ...	
			data.indexOfKeyField =data.inputRowMeta.indexOfValue(meta.getKeyField());
			if (data.indexOfKeyField<0)
			{
				// The field is unreachable !
				logError(Messages.getString("PropertyOutput.Log.ErrorFindingField", meta.getKeyField()));
				throw new KettleException(Messages.getString("PropertyOutput.Log.ErrorFindingField",meta.getKeyField()));
			}
            
			// Let's take the index of Key field ...	
			data.indexOfValueField =data.inputRowMeta.indexOfValue(meta.getValueField());
			if (data.indexOfValueField<0)
			{
				// The field is unreachable !
				logError(Messages.getString("PropertyOutput.Log.ErrorFindingField", meta.getValueField()));
				throw new KettleException(Messages.getString("PropertyOutput.Log.ErrorFindingField",meta.getValueField()));
			}
            
			// Let's check for filename...
			
			try{
			data.file=KettleVFS.getFileObject(buildFilename());
			}catch(Exception e){throw new KettleException(e);}
			
			// Check if filename is empty..
			if(data.file==null)
			{
				logError(Messages.getString("PropertyOutput.Log.FilenameEmpty"));
				throw new KettleException(Messages.getString("PropertyOutput.Log.FilenameEmpty"));	
			}
			// Create parent folder if needed...
			createParentFolder();
			
	
		} // end first
		
		
		boolean sendToErrorRow=false;
        String errorMessage = null;
		String propkey=null;
		String propvalue=null;
		
		// Get value field
        if(data.indexOfKeyField>-1) propkey= data.inputRowMeta.getString(r,data.indexOfKeyField);
        if(data.indexOfValueField>-1) propvalue= data.inputRowMeta.getString(r,data.indexOfValueField);

        try
        {

            if(!data.KeySet.contains(propkey))
            {
            	if(log.isDetailed()) 
            	{
            		log.logDetailed(toString(),Messages.getString("PropertyOutput.Log.Key", propkey));
            		log.logDetailed(toString(),Messages.getString("PropertyOutput.Log.Value" ,propvalue));
            	}
                // Update property
                data.pro.setProperty(propkey, propvalue);
            	putRow(data.outputRowMeta, r);       // in case we want it to go further...
            	incrementLinesOutput();

            	if (checkFeedback(getLinesRead())) 
            	{
            		if(log.isBasic()) logBasic("linenr "+getLinesRead());
            	}
            	data.KeySet.add(propkey);
            }
            	
		}
		catch(KettleStepException e)
		{
			if (getStepMeta().isDoingErrorHandling())
            {
                sendToErrorRow = true;
                errorMessage = e.toString();
            }
			else
			{
				logError(Messages.getString("PropertyOutputMeta.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			
			  if (sendToErrorRow)
			  {
                putError(data.outputRowMeta, r, 1L, errorMessage, null, "PROPSOUTPUTO001");
			  }
		}	
		
		return true;
	}
	
	private void createParentFolder() throws KettleException
	{
		
		if(meta.isCreateParentFolder())
		{
			FileObject parentfolder=null;
			try
			{	
				// Do we need to create parent folder ?
			
				// Check for parent folder
    			// Get parent folder
	    		parentfolder=data.file.getParent();	    		
	    		if(!parentfolder.exists())	
	    		{
	    			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("PropertyOutput.Log.ParentFolderExists",parentfolder.getName().toString()));
	    			parentfolder.createFolder();
	    			if(log.isDetailed()) log.logDetailed(toString(),Messages.getString("PropertyOutput.Log.CanNotCreateParentFolder",parentfolder.getName().toString()));
	    		}		
			}
			catch (Exception e) {					
				logError(Messages.getString("PropertyOutput.Log.CanNotCreateParentFolder", parentfolder.getName().toString()));
				throw new KettleException(Messages.getString("PropertyOutput.Log.CanNotCreateParentFolder",parentfolder.getName().toString()));
			
			}
		 finally {
	         	if ( parentfolder != null )
	         	{
	         		try  {
	         			parentfolder.close();
	         		}
	         		catch ( Exception ex ) {};
	         	}
		 }
		} 	
	}
   private void saveProperties(Properties p) throws KettleException{

        OutputStream propsFile=null;

        try {
            propsFile = KettleVFS.getOutputStream(data.file,false);
            p.store(propsFile, environmentSubstitute(meta.getComment()));
            propsFile.close();
            
			if( meta.AddToResult())
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file, getTransMeta().getName(), getStepname());
				resultFile.setComment(Messages.getString("PropertyOutput.Log.FileAddedResult"));
				addResultFile(resultFile);
			}   
            
        } catch (Exception e) {
        	throw new KettleException(e);
        }
        finally
        {
        	if(propsFile!=null)
        	{
        		try{        	
        			propsFile.close();
        			propsFile=null;
        		}catch(Exception e){}
        	}
        }
    }
	public String buildFilename()
	{
		return meta.buildFilename(this,getCopy());
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(PropertyOutputMeta)smi;
		data=(PropertyOutputData)sdi;

		if (super.init(smi, sdi))
		{
			data.KeySet.clear();
			return true;
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(PropertyOutputMeta)smi;
		data=(PropertyOutputData)sdi;
		if(data.file!=null)
		{
     		try  {
     			data.file.close();
     			data.file=null;
     		}
     		catch ( Exception ex ) {};
		}
		setOutputDone();
		super.dispose(smi, sdi);
	}
	

	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}
}
