 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 
package org.pentaho.di.trans.steps.cubeoutput;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Outputs a stream/series of rows to a file, effectively building a sort of (compressed) microcube.
 * 
 * @author Matt
 * @since 4-apr-2003
 */

public class CubeOutput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = CubeOutputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CubeOutputMeta meta;
	private CubeOutputData data;
	
	public CubeOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CubeOutputMeta)smi;
		data=(CubeOutputData)sdi;

		Object[] r;
		boolean result=true;
		
		r=getRow();       // This also waits for a row to be finished.
		
		if (r==null)
		{
			setOutputDone();
            return false;
		}
		if(first)
		{
			if(meta.isDoNotOpenNewFileInit())
			{
				try
				{
					prepareFile();
					data.oneFileOpened=true;
				}
				catch(KettleFileException ioe)
				{
					logError(BaseMessages.getString(PKG, "CubeOutput.Log.ErrorOpeningCubeOutputFile")+ioe.toString()); //$NON-NLS-1$
					setErrors(1);
					return false;
				}
			}
		}
		result=writeRowToFile(r);
		if (!result)
		{
			setErrors(1);
			stopAll();
			return false;
		}
		
		putRow(data.outputMeta, r);       // in case we want it to go further...
		
        if (checkFeedback(getLinesOutput())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "CubeOutput.Log.LineNumber")+getLinesOutput()); //$NON-NLS-1$
        }
		
		return result;
	}

	private synchronized boolean writeRowToFile(Object[] r)
	{
		try
		{	
			if (first)
			{
				data.outputMeta = getInputRowMeta().clone();
				// Write meta-data to the cube file...
				data.outputMeta.writeMeta(data.dos);
				first=false;
			}
   			// Write data to the cube file...
			data.outputMeta.writeData(data.dos, r);
		}
		catch(Exception e)
		{
			logError(BaseMessages.getString(PKG, "CubeOutput.Log.ErrorWritingLine")+e.toString()); //$NON-NLS-1$
			return false;
		}

		incrementLinesOutput();
		
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CubeOutputMeta)smi;
		data=(CubeOutputData)sdi;

		if (super.init(smi, sdi))
		{
			if(!meta.isDoNotOpenNewFileInit())
			{
				try
				{
					prepareFile();
					data.oneFileOpened=true;
					return true;
				}
				catch(KettleFileException ioe)
				{
					logError(BaseMessages.getString(PKG, "CubeOutput.Log.ErrorOpeningCubeOutputFile")+ioe.toString()); //$NON-NLS-1$
				}
			}else return true;
				
		}
		return false;
	}
    private void prepareFile() throws KettleFileException
    {
    	try {
			String filename=environmentSubstitute(meta.getFilename());
		    if(meta.isAddToResultFiles())
	        {
				// Add this to the result file names...
				ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename, getTransMeta()), getTransMeta().getName(), getStepname());
				resultFile.setComment("This file was created with a cube file output step");
	            addResultFile(resultFile);
	        }
	
		    
			data.fos=KettleVFS.getOutputStream(filename, getTransMeta(), false);
			data.zip=new GZIPOutputStream(data.fos);
			data.dos=new DataOutputStream(data.zip);
    	}
    	catch(Exception e) {
    		throw new KettleFileException(e);
    	}
    }
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
    	if(data.oneFileOpened)
    	{
	        try
	        {
	            if (data.dos!=null) 
	            {
	            	data.dos.close();
	            	data.dos=null;
	            }
	            if (data.zip!=null)
	            {
					data.zip.close();
					data.zip=null;
				}
	            if (data.fos!=null)
	            {
	            	data.fos.close();
	            	data.fos=null;
	            }
	        }
	        catch(IOException e)
	        {
	            logError(BaseMessages.getString(PKG, "CubeOutput.Log.ErrorClosingFile")+meta.getFilename()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	        }
    	}

        super.dispose(smi, sdi);
    }	
}