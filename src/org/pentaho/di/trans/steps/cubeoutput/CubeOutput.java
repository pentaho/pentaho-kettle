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
import org.pentaho.di.core.vfs.KettleVFS;
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

		if(first) // Always run this code once, even if stream is empty (r==null)
		{
			if (getInputRowMeta() != null)
			{
				data.outputMeta = getInputRowMeta().clone();
			}
			else
			{
				// If the stream is empty, then row metadata probably hasn't been received. In this case, use
				// the design-time algorithm to calculate the output metadata.
				data.outputMeta = getTransMeta().getPrevStepFields(getStepMeta());
			}
			
			// If input stream is empty, but file was already opened in init(), then
			// write metadata so as to create a valid, empty cube file.
			if(r==null && data.oneFileOpened)
			{
				result=writeHeaderToFile();
				if (!result)
				{
					setErrors(1);
					stopAll();
					return false;
				}
			}
		}
			
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
					PrepareFile();
					data.oneFileOpened=true;
				}
				catch(IOException ioe)
				{
					logError(Messages.getString("CubeOutput.Log.ErrorOpeningCubeOutputFile")+ioe.toString()); //$NON-NLS-1$
					setErrors(1);
					return false;
				}
			}
			
			result=writeHeaderToFile();
			if (!result)
			{
				setErrors(1);
				stopAll();
				return false;
			}
			
			first = false;
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
        	if(log.isBasic()) logBasic(Messages.getString("CubeOutput.Log.LineNumber")+getLinesOutput()); //$NON-NLS-1$
        }
		
		return result;
	}

	private synchronized boolean writeHeaderToFile()
	{
		try
		{	
			data.outputMeta.writeMeta(data.dos);
		}
		catch(Exception e)
		{
			logError(Messages.getString("CubeOutput.Log.ErrorWritingLine")+e.toString()); //$NON-NLS-1$
			return false;
		}
		
		return true;
	}
	
	private synchronized boolean writeRowToFile(Object[] r)
	{
		try
		{	
   			// Write data to the cube file...
			data.outputMeta.writeData(data.dos, r);
		}
		catch(Exception e)
		{
			logError(Messages.getString("CubeOutput.Log.ErrorWritingLine")+e.toString()); //$NON-NLS-1$
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
					PrepareFile();
					data.oneFileOpened=true;
					return true;
				}
				catch(IOException ioe)
				{
					logError(Messages.getString("CubeOutput.Log.ErrorOpeningCubeOutputFile")+ioe.toString()); //$NON-NLS-1$
				}
			}else return true;
				
		}
		return false;
	}
    private void PrepareFile() throws IOException
    {
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
	            logError(Messages.getString("CubeOutput.Log.ErrorClosingFile")+meta.getFilename()); //$NON-NLS-1$
	            setErrors(1);
	            stopAll();
	        }
    	}

        super.dispose(smi, sdi);
    }
	
	//
	// Run is were the action happens!
	public void run()
	{
    	BaseStep.runStepThread(this, meta, data);
	}	
}