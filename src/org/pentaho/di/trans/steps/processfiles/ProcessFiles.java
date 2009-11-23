
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
package org.pentaho.di.trans.steps.processfiles;


import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;

import org.apache.commons.vfs.FileType;

/**
 * Copy, move or delete file
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class ProcessFiles extends BaseStep implements StepInterface
{
    private ProcessFilesMeta meta;
    private ProcessFilesData data;
    
    public ProcessFiles(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(ProcessFilesMeta)smi;
        data=(ProcessFilesData)sdi;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
    	if(first)
    	{
    		first=false;
    		// Check is source filename field is provided
			if (Const.isEmpty(meta.getDynamicSourceFileNameField()))
			{
				throw new KettleException(Messages.getString("ProcessFiles.Error.SourceFilenameFieldMissing"));
			}
    		// Check is target filename field is provided
			if (meta.getOperationType()!=ProcessFilesMeta.OPERATION_TYPE_DELETE 
					&& Const.isEmpty(meta.getDynamicTargetFileNameField()))
			{
				throw new KettleException(Messages.getString("ProcessFiles.Error.TargetFilenameFieldMissing"));
			}
			
			// cache the position of the source filename field			
			if (data.indexOfSourceFilename<0)
			{	
				data.indexOfSourceFilename =getInputRowMeta().indexOfValue(meta.getDynamicSourceFileNameField());
				if (data.indexOfSourceFilename<0)
				{
					// The field is unreachable !
					throw new KettleException(Messages.getString("ProcessFiles.Exception.CouldnotFindField",meta.getDynamicSourceFileNameField())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			// cache the position of the source filename field			
			if(meta.getOperationType()!=ProcessFilesMeta.OPERATION_TYPE_DELETE 
					&& data.indexOfTargetFilename<0)
			{	
				data.indexOfTargetFilename =getInputRowMeta().indexOfValue(meta.getDynamicTargetFileNameField());
				if (data.indexOfTargetFilename<0)
				{
					// The field is unreachable !
					throw new KettleException(Messages.getString("ProcessFiles.Exception.CouldnotFindField",meta.getDynamicTargetFileNameField())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			
			if(meta.simulate)
			{
				if(log.isBasic()) log.logBasic(toString(),Messages.getString("ProcessFiles.Log.SimulationModeON"));
			}
    	}// End If first 
        try
        {        	
        	// get source filename
        	String sourceFilename= getInputRowMeta().getString(r,data.indexOfSourceFilename);
        	
        	if(Const.isEmpty(sourceFilename))
        	{
        		log.logError(toString(), Messages.getString("ProcessFiles.Error.SourceFileEmpty"));
        		throw new KettleException(Messages.getString("ProcessFiles.Error.SourceFileEmpty"));
        	}
        	data.sourceFile=KettleVFS.getFileObject(sourceFilename, getTransMeta());
        	boolean targetFileExists=false;
        	
        	if(!data.sourceFile.exists())
        	{
        		log.logError(toString(), Messages.getString("ProcessFiles.Error.SourceFileNotExist",sourceFilename));
        		throw new KettleException(Messages.getString("ProcessFiles.Error.SourceFileNotExist",sourceFilename));
        	}
        	if(data.sourceFile.getType()!=FileType.FILE)
        	{
        		log.logError(toString(), Messages.getString("ProcessFiles.Error.SourceFileNotFile",sourceFilename));
        		throw new KettleException(Messages.getString("ProcessFiles.Error.SourceFileNotFile",sourceFilename));
        	}
        	String targetFilename=null;
        	if (meta.getOperationType()!=ProcessFilesMeta.OPERATION_TYPE_DELETE)
        	{
        		// get value for target filename
        		targetFilename=getInputRowMeta().getString(r,data.indexOfTargetFilename);
        		
            	if(Const.isEmpty(targetFilename))
            	{
            		log.logError(toString(), Messages.getString("ProcessFiles.Error.TargetFileEmpty"));
            		throw new KettleException(Messages.getString("ProcessFiles.Error.TargetFileEmpty"));
            	}
            	data.targetFile=KettleVFS.getFileObject(targetFilename, getTransMeta());
				if(data.targetFile.exists())
				{
					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("ProcessFiles.Log.TargetFileExists",targetFilename));
				}
				else
				{
					// let's check parent folder
					FileObject parentFolder=data.targetFile.getParent();
					if(!parentFolder.exists())
					{
						if(!meta.isCreateParentFolder())
							throw new KettleException(Messages.getString("ProcessFiles.Error.TargetParentFolderNotExists",parentFolder.toString()));
						else
							parentFolder.createFolder();
					}
					if(parentFolder!=null) parentFolder.close();
				}
        	}
  
    		switch (meta.getOperationType()) 
    		{
	    		case ProcessFilesMeta.OPERATION_TYPE_COPY:
	    			if(((meta.isOverwriteTargetFile() && targetFileExists) || !targetFileExists)&& !meta.simulate)
	    				data.targetFile.copyFrom(data.sourceFile, new TextOneToOneFileSelector());
	    			if(log.isDetailed()) 
	    				log.logDetailed(toString(), Messages.getString("ProcessFiles.Log.SourceFileCopied",sourceFilename,targetFilename));
	    			break;
	    		case ProcessFilesMeta.OPERATION_TYPE_MOVE:
	    			if(((meta.isOverwriteTargetFile() && targetFileExists) || !targetFileExists)&& !meta.simulate)
	    				data.sourceFile.moveTo(KettleVFS.getFileObject(targetFilename, getTransMeta()));
	    			if(log.isDetailed()) 
	    				log.logDetailed(toString(), Messages.getString("ProcessFiles.Log.SourceFileMoved",sourceFilename,targetFilename));
	    			break;
	    		case ProcessFilesMeta.OPERATION_TYPE_DELETE:
	    			if(!meta.simulate)
	    			{
	    				if(!data.sourceFile.delete()) 
	    					throw new KettleException(Messages.getString("ProcessFiles.Error.CanNotDeleteFile",data.sourceFile.toString()));
	    			}
	    			if(log.isDetailed()) 
						log.logDetailed(toString(), Messages.getString("ProcessFiles.Log.SourceFileDeleted",sourceFilename));
	    			break;
	    		default:
	    			
	    			break;
    		}
        		
        		
			// add filename to result filenames?
			if(meta.isaddTargetFileNametoResult() && meta.getOperationType()!=ProcessFilesMeta.OPERATION_TYPE_DELETE && data.sourceFile.getType()==FileType.FILE)
			{
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.targetFile, getTransMeta().getName(), getStepname());
				resultFile.setComment(Messages.getString("ProcessFiles.Log.FileAddedResult"));
				addResultFile(resultFile);
				
				if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("ProcessFiles.Log.FilenameAddResult",data.sourceFile.toString()));
			}

			putRow(getInputRowMeta(), r);     // copy row to possible alternate rowset(s).

            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isBasic()) logBasic(Messages.getString("ProcessFiles.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
        }
        catch(Exception e)
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
	            logError(Messages.getString("ProcessFiles.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	            setOutputDone();  // signal end to receiver(s)
	            return false;
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, null, "ProcessFiles001");
        	}
        }
            
        return true;
    }
	private class TextOneToOneFileSelector implements FileSelector 
	{		 
		public boolean includeFile(FileSelectInfo info) throws Exception
		{
			return true;	
		}
		public boolean traverseDescendents(FileSelectInfo info) 
		{
			return false;
		}
	}
	
	 
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(ProcessFilesMeta)smi;
        data=(ProcessFilesData)sdi;

        if (super.init(smi, sdi))
        {
            return true;
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (ProcessFilesMeta)smi;
        data = (ProcessFilesData)sdi;
        if(data.sourceFile!=null)
        {
        	try{
        		data.sourceFile.close();
        		data.sourceFile=null;
        	}catch(Exception e){}
        	
        }
        if(data.targetFile!=null)
        {
        	try{
        		data.targetFile.close();
        		data.targetFile=null;
        	}catch(Exception e){}
        	
        }
        super.dispose(smi, sdi);
    }
    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }	
    public String toString()
    {
        return this.getClass().getName();
    }
}
