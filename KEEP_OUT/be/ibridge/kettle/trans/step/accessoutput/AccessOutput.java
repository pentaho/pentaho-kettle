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
 
package be.ibridge.kettle.trans.step.accessoutput;


import java.io.File;
import java.io.IOException;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.util.StringUtil;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

import com.healthmarketscience.jackcess.Database;


/**
 * Writes rows to a database table.
 * 
 * @author Matt
 * @since 6-apr-2003
 */
public class AccessOutput extends BaseStep implements StepInterface
{
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

		Row r;
		
		r=getRow();    // this also waits for a previous step to be finished.
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

		try
		{
			writeToTable(r);
			putRow(r);       // in case we want it go further...

            if (checkFeedback(linesOutput)) logBasic("linenr "+linesOutput);
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

	private boolean writeToTable(Row r)
	{
		if (r==null) // Stop: last line or error encountered 
		{
			if (log.isDetailed()) logDetailed("Last line inserted: stop");
			return false;
		} 

        if (first)
        {
            first=false;
            
            // First open or create the table
            try
            {
                String realTablename = StringUtil.environmentSubstitute(meta.getTablename());
                data.table = data.db.getTable(realTablename);
                if (data.table==null)
                {
                    if (meta.isTableCreated())
                    {
                        // Create the table
                        data.columns = AccessOutputMeta.getColumns(r);
                        data.db.createTable(realTablename, data.columns);
                        data.table = data.db.getTable(realTablename);
                    }
                    else
                    {
                        logError(Messages.getString("AccessOutput.Error.TableDoesNotExist", realTablename));
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
                logError(Messages.getString("AccessOutput.Exception.UnexpectedErrorCreatingTable", e.toString()));
                logError(Const.getStackTracker(e));
                setErrors(1);
                stopAll();
                return false;
            }
        }
        
        // Let's write a row to the database.
        Object[] columnValues = AccessOutputMeta.createObjectsForRow(r);
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
            logError(Messages.getString("AccessOutput.Exception.UnexpectedErrorWritingRow", r.toString()));
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
			try
			{
                String realFilename = StringUtil.environmentSubstitute(meta.getFilename());
                File file = new File(realFilename);
                
                // First open or create the access file
                if (!file.exists())
                {
                    if (meta.isFileCreated())
                    {
                        data.db = Database.create(file);
                    }
                    else
                    {
                        logError(Messages.getString("AccessOutput.InitError.FileDoesNotExist", realFilename));
                        return false;
                    }
                }
                else
                {
                    data.db = Database.open(file);
                }
                
				return true;
			}
			catch(Exception e)
			{
				logError("An error occurred intialising this step: "+e.getMessage());
				stopAll();
				setErrors(1);
			}
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AccessOutputMeta)smi;
		data=(AccessOutputData)sdi;

		try
		{
            // Put the last records in the table as well!
            data.table.addRows(data.rows);
            
            // Just for good measure.
            data.rows.clear();
            
            data.db.close();
		}
		catch(IOException e)
		{
		    logError("Error closing the database: "+e.toString());
			setErrors(1);
			stopAll();
		}

        super.dispose(smi, sdi);
	}
	

	/**
	 * Run is were the action happens!
	 */
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : "+e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
