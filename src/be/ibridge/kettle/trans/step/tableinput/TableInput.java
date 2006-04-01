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
 

package be.ibridge.kettle.trans.step.tableinput;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
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
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class TableInput extends BaseStep implements StepInterface
{
	private TableInputMeta meta;
	private TableInputData data;
	
	public TableInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized Row readStartDate()
    {
        logDetailed("Reading from step [" + meta.getLookupStepname() + "]");

        Row parameters = new Row();

        Row r = getRowFrom(meta.getLookupStepname()); // rows are originating from "lookup_from"

        for (int i = 0; i < r.size(); i++) // take all values from input row
        {
            Value val = r.getValue(i);

            parameters.addValue(val);
        }
        r = getRowFrom(meta.getLookupStepname()); // take all input rows if needed!
        
        return parameters;
    }	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first) // we just got started
		{
            Row parameters;
			first=false;
            
			// Make sure we read data from source steps...
            if (meta.getInfoSteps() != null)
            {
                if (meta.isExecuteEachInputRow())
                {
                    logDetailed("Reading single row from stream [" + meta.getLookupStepname() + "]");
                    parameters = getRowFrom(meta.getLookupStepname());
                    logDetailed("Query parameters found = " + parameters.toString());
                }
                else
                {
                    logDetailed("Reading query parameters from stream [" + meta.getLookupStepname() + "]");
                    parameters = readStartDate(); // Read values in lookup table (look)
                    logDetailed("Query parameters found = " + parameters.toString());
                }
            }
            else
            {
                parameters = new Row();
			}
            
            boolean success = doQuery(parameters);
            if (!success) 
            { 
                return false; 
            }
		}
        else
        {
            if (data.thisrow!=null) // We can expect more rows
            {
                data.nextrow=data.db.getRow(data.rs); 
                if (data.nextrow!=null) linesInput++;
            }
        }

    	if (data.thisrow == null) // Finished reading?
        {
            boolean done = false;
            if (meta.isExecuteEachInputRow()) // Try to get another row from the input stream
            {
                Row nextRow = getRowFrom(meta.getLookupStepname());
                if (nextRow == null) // Nothing more to get!
                {
                    done = true;
                }
                else
                {
                    closeLastQuery();
                    boolean success = doQuery(nextRow); // OK, perform a new query
                    if (!success) 
                    { 
                        return false; 
                    }
                }
            }
            else
            {
                done = true;
            }

            if (done)
            {
                setOutputDone(); // signal end to receiver(s)
                return false; // end of data or error.
            }
        }

        if (data.thisrow != null)
        {
            putRow(data.thisrow); // fill the rowset(s). (wait for empty)
            data.thisrow = data.nextrow;

            if ((linesInput > 0) && (linesInput % Const.ROWS_UPDATE) == 0) logBasic("linenr " + linesInput);
        }
		
		return true;
	}
    
    private boolean doQuery(Row parameters) throws KettleDatabaseException
    {
        boolean success = true;

        // Open the query with the optional parameters received from the source steps.
        data.rs = data.db.openQuery(meta.getSQL(), parameters);
        if (data.rs == null)
        {
            logError("Couldn't open Query [" + meta.getSQL() + "]");
            setErrors(1);
            stopAll();
            success = false;
        }
        else
        {

            // Get the first row...
            data.thisrow = data.db.getRow(data.rs);
            if (data.thisrow != null)
            {
                linesInput++;
                data.nextrow = data.db.getRow(data.rs);
                if (data.nextrow != null) linesInput++;
            }
        }
        return success;
    }

    
    private void closeLastQuery()
    {
        try
        {
            data.db.closeQuery(data.rs);
        }
        catch(KettleException e)
        {
            logError("Unexpected error closing query : "+e.toString());
            setErrors(1);
            stopAll();
        }
    }
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		logBasic("Finished reading query, closing connection.");
		closeLastQuery();
        data.db.disconnect();

		super.dispose(smi, sdi);
	}
	
	/** Stop the running query */
	public void stopRunning()
	{
	    try
	    {
	        if (data.db!=null) data.db.cancelQuery();
	    }
	    catch(KettleDatabaseException e)
	    {
	        
	    }
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TableInputMeta)smi;
		data=(TableInputData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			data.db.setQueryLimit(meta.getRowLimit());

			try
			{
				data.db.connect();
				logDetailed("Connected to database...");

				return true;
			}
			catch(KettleException e)
			{
				logError("An error occurred, processing will be stopped: "+e.getMessage());
				setErrors(1);
				stopAll();
			}
		}
		
		return false;
	}
	

	//
	// Run is were the action happens!
	//
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (!isStopped() && processRow(meta, data) );
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
			logSummary();
			markStop();
		}
	}
}
