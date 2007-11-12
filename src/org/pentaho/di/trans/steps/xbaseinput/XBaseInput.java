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
 
package org.pentaho.di.trans.steps.xbaseinput;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads data from an XBase (dBase, foxpro, ...) file.
 * 
 * @author Matt
 * @since 8-sep-2004
 */
public class XBaseInput extends BaseStep implements StepInterface
{
	private XBaseInputMeta meta;
	private XBaseInputData data;
		
	public XBaseInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;
        
        // See if we need to get a list of files from input...
        if (first) // we just got started
        {
            first = false;
            
            // The output row meta data, what does it look like?
            //
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            if (meta.isAcceptingFilenames())
            {
                // Read the files from the specified input stream...
                data.files.getFiles().clear();
                
                int idx = -1;
                
                RowSet rowSet = findInputRowSet(meta.getAcceptingStepName());
                Object[] fileRowData = getRowFrom(rowSet);
                while (fileRowData!=null)
                {
                	RowMetaInterface fileRowMeta = rowSet.getRowMeta();
                    if (idx<0)
                    {
                        idx = fileRowMeta.indexOfValue(meta.getAcceptingField());
                        if (idx<0)
                        {
                            logError(Messages.getString("XBaseInput.Log.Error.UnableToFindFilenameField", meta.getAcceptingField()));
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    try
                    {
                    	String filename = fileRowMeta.getString(fileRowData, idx);
                        data.files.addFile( KettleVFS.getFileObject(filename) );
                    }
                    catch(Exception e)
                    {
                        throw new KettleException(e);
                    }
                    
                    // Grab another row
                    //
                    fileRowData = getRowFrom(rowSet);
                }
                
                if (data.files.nrOfFiles()==0)
                {
                    logBasic(Messages.getString("XBaseInput.Log.Error.NoFilesSpecified"));
                    return false;
                }
            }

            // Open the first file & read the required rows in the buffer, stop
            // if it fails, exception will stop processLoop
            //
            openNextFile();
        }
        
        // Allocate the output row in advance, because we possibly want to add a few extra fields...
        //
        Object[] row = data.xbi.getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
        while (row==null && data.fileNr < data.files.nrOfFiles()) // No more rows left in this file
        {
            openNextFile();
            row = data.xbi.getRow( RowDataUtil.allocateRowData(data.outputRowMeta.size()) );
        }
        
        if (row==null) 
        {           
            setOutputDone();  // signal end to receiver(s)
            return false; // end of data or error.
        }
        
        // OK, so we have read a line: increment the input counter
		linesInput++;
		int outputIndex = data.fields.size();

        // Possibly add a filename...
        if (meta.includeFilename())
        {
            row[outputIndex++] = data.file_dbf.getName().getURI();
        }

        // Possibly add a row number...
        if (meta.isRowNrAdded())
        {
            row[outputIndex++] = new Long(linesInput);
        }

		putRow(data.outputRowMeta, row);        // fill the rowset(s). (wait for empty)

        if (checkFeedback(linesInput)) logBasic(Messages.getString("XBaseInput.Log.LineNr")+linesInput); //$NON-NLS-1$

        if (meta.getRowLimit()>0 && linesInput>=meta.getRowLimit())  // limit has been reached: stop now.
        {
            setOutputDone();
            return false;
        }
        
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;

	    if (super.init(smi, sdi))
	    {
            data.files  = meta.getTextFileList(this);
            data.fileNr = 0;
            
            if (data.files.nrOfFiles()==0 && !meta.isAcceptingFilenames())
            {
                logError(Messages.getString("XBaseInput.Log.Error.NoFilesSpecified"));
                return false;
            }
            return true;
	    }
		return false;
	}
	
	private void openNextFile() throws KettleException
    {
        // Close the last file before opening the next...
        if (data.xbi!=null)
        {
            logBasic(Messages.getString("XBaseInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
            data.xbi.close();
        }
        
        // Replace possible environment variables...
        data.file_dbf = data.files.getFile(data.fileNr);
        data.fileNr++;
                
        try
        {
            data.xbi=new XBase(data.file_dbf.getContent().getInputStream());
            data.xbi.setDbfFile(data.file_dbf.getName().getURI());
            if (!Const.isEmpty(meta.getCharactersetName())) {
            	data.xbi.getReader().setCharactersetName(meta.getCharactersetName());
            }
            data.xbi.open();
            
            logBasic(Messages.getString("XBaseInput.Log.OpenedXBaseFile")+" : ["+data.xbi+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            data.fields = data.xbi.getFields();
            
            // Add this to the result file names...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, data.file_dbf, getTransMeta().getName(), getStepname());
            resultFile.setComment(Messages.getString("XBaseInput.ResultFile.Comment"));
            addResultFile(resultFile);
        }
        catch(Exception e)
        {
            logError(Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile1")+data.file_dbf+Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            throw new KettleException(e);
        }
    }

    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
        closeLastFile();
        
		super.dispose(smi, sdi);
	}

	private void closeLastFile()
    {
        logBasic(Messages.getString("XBaseInput.Log.FinishedReadingRecords")); //$NON-NLS-1$
        data.xbi.close();    
    }

    //
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic(Messages.getString("XBaseInput.Log.StartingToRun"));		 //$NON-NLS-1$
			while (!isStopped() && processRow(meta, data) );
		}
		catch(Exception e)
		{
			logError(Messages.getString("XBaseInput.Log.Error.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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
