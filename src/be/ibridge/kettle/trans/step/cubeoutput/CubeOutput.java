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
 
package be.ibridge.kettle.trans.step.cubeoutput;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


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
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CubeOutputMeta)smi;
		data=(CubeOutputData)sdi;

		Row r;
		boolean result=true;
		
		r=getRow();       // This also waits for a row to be finished.
		
		if (r==null)
		{
			setOutputDone();
            return false;
		}
		
		result=writeRowToFile(r);
		if (!result)
		{
			setErrors(1);
			stopAll();
			return false;
		}
		
		putRow(r);       // in case we want it to go further...
		
		if ((linesOutput>0) && (linesOutput%Const.ROWS_UPDATE)==0)logBasic("linenr "+linesOutput);
		
		return result;
	}

	private synchronized boolean writeRowToFile(Row r)
	{
		try
		{	
			if (first)
			{	
				// Write data + meta-data to the cube file...
				r.write(data.dos);
				first=false;
			}
			else
			{
				// Write data to the cube file...
				r.writeData(data.dos);
			}
		}
		catch(Exception e)
		{
			logError("Error writing line :"+e.toString());
			return false;
		}

		linesOutput++;
		
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CubeOutputMeta)smi;
		data=(CubeOutputData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				File fil=new File(meta.getFilename());
				data.fos=new GZIPOutputStream(new FileOutputStream(fil));
				data.dos=new DataOutputStream(data.fos);
			
				debug="start";

				return true;
			}
			catch(IOException ioe)
			{
				logError("Error opening cube output file: "+ioe.toString());
			}
		}
		return false;
	}
    
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        try
        {
            super.dispose(smi, sdi);
            if (data.dos!=null) data.dos.close();
            if (data.fos!=null) data.fos.close();
        }
        catch(IOException e)
        {
            logError("Error closing file "+meta.getFilename());
            setErrors(1);
            stopAll();
        }

    }
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			markStop();
		    logSummary();
		}
	}	
}
