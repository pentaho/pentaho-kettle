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
 
package be.ibridge.kettle.trans.step.xbaseinput;

import java.io.File;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.ResultFile;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XBase;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


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
        
        Row row = null;

        // See if we need to get a list of files from input...
        if (first) // we just got started
        {
            first = false;
            
            if (meta.isAcceptingFilenames())
            {
                // Read the files from the specified input stream...
                data.files.getFiles().clear();
                
                int idx = -1;
                Row fileRow = getRowFrom(meta.getAcceptingStepName());
                while (fileRow!=null)
                {
                    if (idx<0)
                    {
                        idx = fileRow.searchValueIndex(meta.getAcceptingField());
                        if (idx<0)
                        {
                            logError(Messages.getString("XBaseInput.Log.Error.UnableToFindFilenameField", meta.getAcceptingField()));
                            setErrors(1);
                            stopAll();
                            return false;
                        }
                    }
                    Value fileValue = fileRow.getValue(idx);
                    data.files.addFile(new File(fileValue.getString()));
                    
                    // Grab another row
                    fileRow = getRowFrom(meta.getAcceptingStepName());
                }
                
                if (data.files.nrOfFiles()==0)
                {
                    logBasic(Messages.getString("XBaseInput.Log.Error.NoFilesSpecified"));
                    return false;
                }
            }

            // Open the first file & read the required rows in the buffer, stop
            // if it fails, exception whil stop processLoop
            //
            openNextFile();
        }
        
        row = data.xbi.getRow(data.fields);
        while (row==null && data.fileNr < data.files.nrOfFiles()) // No more rows left in this file
        {
            openNextFile();
            row = data.xbi.getRow(data.fields);
        }
        
        if (row==null) 
        {           
            setOutputDone();  // signal end to receiver(s)
            return false; // end of data or error.
        }
        
        // OK, so we have read a line: increment the input counter
		linesInput++;

        // Possibly add a filename...
        if (meta.includeFilename())
        {
            Value inc = new Value(meta.getFilenameField(), data.file_dbf.getPath());
            inc.setLength(100);
            row.addValue(inc);
        }

        // Possibly add a row number...
        if (meta.isRowNrAdded())
        {
            Value inc = new Value(meta.getRowNrField(), Value.VALUE_TYPE_INTEGER);
            inc.setValue(linesInput);
            inc.setLength(9);
            row.addValue(inc);
        }

		debug="Send the row to the next step."; //$NON-NLS-1$
		putRow(row);        // fill the rowset(s). (wait for empty)

		if ((linesInput>0) && (linesInput%Const.ROWS_UPDATE)==0) logBasic(Messages.getString("XBaseInput.Log.LineNr")+linesInput); //$NON-NLS-1$

		debug="End of readRowOfData."; //$NON-NLS-1$
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(XBaseInputMeta)smi;
		data=(XBaseInputData)sdi;

	    if (super.init(smi, sdi))
	    {
            data.files  = meta.getTextFileList();
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
                
        data.xbi=new XBase(data.file_dbf.getPath());
        try
        {
            data.xbi.open();
            
            logBasic(Messages.getString("XBaseInput.Log.OpenedXBaseFile")+" : ["+data.file_dbf.getPath()+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            data.fields = data.xbi.getFields();
            
            // Add this to the result file names...
            ResultFile resultFile = new ResultFile(ResultFile.FILE_TYPE_GENERAL, new File(meta.getDbfFileName()), getTransMeta().getName(), getStepname());
            resultFile.setComment(Messages.getString("XBaseInput.ResultFile.Comment"));
            addResultFile(resultFile);
        }
        catch(KettleException e)
        {
            logError(Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile1")+data.file_dbf+Messages.getString("XBaseInput.Log.Error.CouldNotOpenXBaseFile2")+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
            throw e;
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
			logError(Messages.getString("XBaseInput.Log.Error.UnexpectedError")+debug+"' : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
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
