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

/**
 * Reads a micro-cube type of data-file from disk.
 * It's just a binary (compressed) representation of a buch of rows.
 * 
 * @author Matt
 * @since 8-apr-2003
 */

package org.pentaho.di.trans.steps.cubeinput;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;

import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleEOFException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


public class CubeInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = CubeInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private CubeInputMeta meta;
	private CubeInputData data;
	
	public CubeInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CubeInputMeta)smi;
		data=(CubeInputData)sdi;

		try
		{
            Object[] r = data.meta.readData(data.dis);
            putRow(data.meta, r);  // fill the rowset(s). (sleeps if full)
            incrementLinesInput();
			
			if (meta.getRowLimit()>0 && getLinesInput()>=meta.getRowLimit()) // finished!
			{
				setOutputDone();
				return false;
			}
		}
		catch(KettleEOFException eof)
		{
			setOutputDone();
			return false;
		} 
		catch (SocketTimeoutException e) 
		{
			throw new KettleException(e); // shouldn't happen on files
		}

        if (checkFeedback(getLinesInput())) 
        {
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "CubeInput.Log.LineNumber")+getLinesInput()); //$NON-NLS-1$
        }

		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CubeInputMeta)smi;
		data=(CubeInputData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
				String filename=environmentSubstitute(meta.getFilename());
				
				// Add filename to result filenames ?
				if(meta.isAddResultFile())
				{
					ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, KettleVFS.getFileObject(filename, getTransMeta()), getTransMeta().getName(), toString());
					resultFile.setComment("File was read by a Cube Input step");
					addResultFile(resultFile);
				}
				
				data.fis=KettleVFS.getInputStream(filename, this);
				data.zip = new GZIPInputStream(data.fis);
				data.dis = new DataInputStream(data.zip);
				
				try
				{
					data.meta = new RowMeta(data.dis);
					return true;
				}
				catch(KettleFileException kfe)
				{
					logError(BaseMessages.getString(PKG, "CubeInput.Log.UnableToReadMetadata"), kfe); //$NON-NLS-1$
					return false;
				}
			}
			catch(Exception e)
			{
				logError(BaseMessages.getString(PKG, "CubeInput.Log.ErrorReadingFromDataCube"), e); //$NON-NLS-1$
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (CubeInputMeta)smi;
	    data = (CubeInputData)sdi;
	    
		try
		{
			if(data.dis!=null)
			{
				data.dis.close();
				data.dis=null;
			}
			if(data.zip!=null)
			{
				data.zip.close();
				data.zip=null;
			}
			if(data.fis!=null)
			{
				data.fis.close();
				data.fis=null;
			}
		}
		catch(IOException e)
		{
			logError(BaseMessages.getString(PKG, "CubeInput.Log.ErrorClosingCube")+e.toString()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
		}
	    
	    super.dispose(smi, sdi);
	}
	
	@Override
	public boolean isWaitingForData() {
	// TODO Auto-generated method stub
	return super.isWaitingForData();
	}
	
}