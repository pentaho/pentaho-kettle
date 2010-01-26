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
 
package org.pentaho.di.trans.steps.accessoutput;


import java.io.File;
import java.io.IOException;

import org.apache.commons.vfs.FileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import com.healthmarketscience.jackcess.Database;


/**
 * Writes rows to a database table.
 * 
 * @author Matt
 * @since 6-apr-2003
 */
public class AccessOutput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = AccessOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final int COMMIT_SIZE = 500;
    
	private AccessOutputMeta meta;
	private AccessOutputData data;
		
	public AccessOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AccessOutputMeta)smi;
		data=(AccessOutputData)sdi;

		Object[] r = getRow();    // this also waits for a previous step to be finished.
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		if(first && meta.isDoNotOpenNewFileInit())
		{
			try
			{
				if(!OpenFile()) return false;
                
			}
			catch(Exception e)
			{
				logError("An error occurred intialising this step: "+e.getMessage());
				stopAll();
				setErrors(1);
			}
		}
		try
		{
			writeToTable(r);
			putRow(data.outputRowMeta, r);       // in case we want it go further...

            if (checkFeedback(getLinesOutput())) 
            {
            	if(log.isBasic()) logBasic("linenr "+getLinesOutput());
            }
		}
		catch(KettleException e)
		{
			logError("Because of an error, this step can't continue: "+e.getMessage());
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}		
		
		return true;
	}

	private boolean writeToTable(Object[] rowData) throws KettleValueException
	{
		if (rowData==null) // Stop: last line or error encountered 
		{
			if (log.isDetailed()) logDetailed("Last line inserted: stop");
			return false;
		} 

        if (first)
        {
            first=false;
            
			data.outputRowMeta = getInputRowMeta();

            // First open or create the table
            try
            {
                String realTablename = environmentSubstitute(meta.getTablename());
                data.table = data.db.getTable(realTablename);
                if (data.table==null)
                {
                    if (meta.isTableCreated())
                    {
                        // Create the table
                        data.columns = AccessOutputMeta.getColumns(data.outputRowMeta);
                        data.db.createTable(realTablename, data.columns);
                        data.table = data.db.getTable(realTablename);
                    }
                    else
                    {
                        logError(BaseMessages.getString(PKG, "AccessOutput.Error.TableDoesNotExist", realTablename));
                        setErrors(1);
                        stopAll();
                        return false;
                    }
                }
                // All OK: we have an open database and a table to write to.
                //
                // Apparently it's not yet possible to remove rows from the table
                // So truncate is out for the moment as well.
                
            }
            catch(Exception e)
            {
                logError(BaseMessages.getString(PKG, "AccessOutput.Exception.UnexpectedErrorCreatingTable", e.toString()));
                logError(Const.getStackTracker(e));
                setErrors(1);
                stopAll();
                return false;
            }
        }
        
        // Let's write a row to the database.
        Object[] columnValues = AccessOutputMeta.createObjectsForRow(data.outputRowMeta, rowData);
        try
        {
            data.rows.add(columnValues);
            if (meta.getCommitSize()>0)
            {
                if (data.rows.size() >= meta.getCommitSize())
                {
                    data.table.addRows(data.rows);
                    data.rows.clear();
                }
            }
            else
            {
                data.table.addRow(columnValues);
            }
        }
        catch(IOException e)
        {
            logError(BaseMessages.getString(PKG, "AccessOutput.Exception.UnexpectedErrorWritingRow", data.outputRowMeta.getString(rowData)));
            logError(Const.getStackTracker(e));
            setErrors(1);
            stopAll();
            return false;
        }
        
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessOutputMeta)smi;
		data=(AccessOutputData)sdi;

		if (super.init(smi, sdi))
		{
			if(!meta.isDoNotOpenNewFileInit())
			{
				try
				{
					return OpenFile();
	                
				}
				catch(Exception e)
				{
					logError("An error occurred intialising this step: "+e.getMessage());
					stopAll();
					setErrors(1);
				}
			}else return true;
		}
		return false;
	}
	private boolean OpenFile() throws Exception
	{
		data.oneFileOpened=true;
        String realFilename = environmentSubstitute(meta.getFilename());
        if(log.isBasic())
        	logBasic(BaseMessages.getString(PKG, "AccessOutput.log.WritingToFile", realFilename));
        FileObject fileObject = KettleVFS.getFileObject(realFilename, getTransMeta());
        File file = new File(KettleVFS.getFilename(fileObject));
        
        // First open or create the access file
        if (!file.exists())
        {
            if (meta.isFileCreated())
            {
                data.db = Database.create(file);
            }
            else
            {
                logError(BaseMessages.getString(PKG, "AccessOutput.InitError.FileDoesNotExist", realFilename));
                return false;
            }
        }
        else
        {
            data.db = Database.open(file);
        }
        
        // Add the filename to the result object...
        //
        if(meta.isAddToResultFiles())
        {
			ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, fileObject, getTransMeta().getName(), toString());
			resultFile.setComment("This file was created with an access output step");
			addResultFile(resultFile);
        }
        
        return true;
	}
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessOutputMeta)smi;
		data=(AccessOutputData)sdi;
		if(data.oneFileOpened)
		{
			try
			{
	            // Put the last records in the table as well!
	            if (data.table!=null) {
	            	data.table.addRows(data.rows);
	            }
	            
	            // Just for good measure.
	            data.rows.clear();
	            
	            if(data.db!=null) data.db.close();
			}
			catch(IOException e)
			{
			    logError("Error closing the database: "+e.toString());
				setErrors(1);
				stopAll();
			}
		}
        super.dispose(smi, sdi);
	}	
}