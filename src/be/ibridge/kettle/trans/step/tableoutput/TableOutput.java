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
 
package be.ibridge.kettle.trans.step.tableoutput;

import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseBatchException;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Writes rows to a database table.
 * 
 * @author Matt
 * @since 6-apr-2003
 */
public class TableOutput extends BaseStep implements StepInterface
{
	private TableOutputMeta meta;
	private TableOutputData data;
		
	public TableOutput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(TableOutputMeta)smi;
		data=(TableOutputData)sdi;

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

			if ((linesOutput>0) && (linesOutput%Const.ROWS_UPDATE)==0 && linesOutput>0) logBasic("linenr "+linesOutput);
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
		throws KettleException
	{
	    debug="start";

		if (r==null) // Stop: last line or error encountered 
		{
			logDetailed("Last line inserted: stop");
			return false;
		} 

        PreparedStatement insertStatement = null;
        
        String tableName = null;
        Value removedValue = null;
        
        if ( meta.isTableNameInField() )
        {
            // Cache the position of the table name field
            if (data.indexOfTableNameField<0)
            {
                data.indexOfTableNameField = r.searchValueIndex(meta.getTableNameField());
                if (data.indexOfTableNameField<0)
                {
                    String message = "Unable to find table name field ["+meta.getTableNameField()+"] in input row";
                    log.logError(toString(), message);
                    throw new KettleStepException(message);
                }
            }
            tableName = r.getValue(data.indexOfTableNameField).getString();
            if (!meta.isTableNameInTable())
            {
                removedValue = r.getValue(data.indexOfTableNameField);
                r.removeValue(data.indexOfTableNameField);
            }
        }
        else
        if (  meta.isPartitioningEnabled() && 
            ( meta.isPartitioningDaily() || meta.isPartitioningMonthly()) &&
            ( meta.getPartitioningField()!=null && meta.getPartitioningField().length()>0 )
           )
        {
            // Initialize some stuff!
            if (data.indexOfPartitioningField<0)
            {
                data.indexOfPartitioningField = r.searchValueIndex(meta.getPartitioningField());
                if (data.indexOfPartitioningField<0)
                {
                    throw new KettleStepException("Unable to find field ["+meta.getPartitioningField()+"] in the input row!");
                }

                if (meta.isPartitioningDaily())
                {
                    data.dateFormater = new SimpleDateFormat("yyyyMMdd");
                }
                else
                {
                    data.dateFormater = new SimpleDateFormat("yyyyMM");
                }
            }
            
            Value partitioningValue = r.getValue(data.indexOfPartitioningField);
            if (!partitioningValue.isDate() || partitioningValue.isNull())
            {
                throw new KettleStepException("Sorry, the partitioning field needs to contain a data value and can't be empty!");
            }
            
            tableName+="_"+data.dateFormater.format(partitioningValue.getDate());
        }
        else
        {
            tableName = Const.replEnv( meta.getTablename() );
        }
        
        if (tableName==null || tableName.length()==0)
        {
            throw new KettleStepException("The tablename is not defined (empty)");
        }

        insertStatement = (PreparedStatement) data.preparedStatements.get(tableName);
        if (insertStatement==null)
        {
            debug="prepareInsert for table ["+tableName+"]";
            String sql = data.db.getInsertStatement(tableName, r);
            logDetailed("Prepared statement : "+sql);
            insertStatement = data.db.prepareSQL(sql);
            data.preparedStatements.put(tableName, insertStatement);
        }
        
		try
		{
            debug="setValuesInsert";
			data.db.setValues(r, insertStatement);
		    debug="insertRow";
			data.db.insertRow(insertStatement, data.batchMode);
			linesOutput++;
		}
		catch(KettleDatabaseBatchException be)
		{
			data.db.clearBatch(insertStatement);
		    data.db.rollback();
			throw new KettleException("Error batch inserting rows into table ["+tableName+"]", be);
		}
		catch(KettleDatabaseException dbe)
		{
		    debug="Normal exception";
		    if (meta.ignoreErrors())
		    {
		        if (data.warnings<20)
		        {
		            logBasic("WARNING: Coudln't insert row into table: "+r+Const.CR+dbe.getMessage());
		        }
		        else
		        if (data.warnings==20)
		        {
		            logBasic("FINAL WARNING (no more then 20 displayed): Coudln't insert row into table: "+r+Const.CR+dbe.getMessage());
		        }
		        data.warnings++;
		    }
		    else
		    {
		        setErrors(getErrors()+1);
		        data.db.rollback();
		        throw new KettleException("Error inserting row into table ["+tableName+"] with values: "+r, dbe);
		    }
		}
		
        if (meta.isTableNameInField() && !meta.isTableNameInTable())
        {
            debug="add value of table name field";
            r.addValue(data.indexOfTableNameField, removedValue);
        }

	    debug="end";
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TableOutputMeta)smi;
		data=(TableOutputData)sdi;

		if (super.init(smi, sdi))
		{
			try
			{
                data.batchMode = meta.getCommitSize()>0 && meta.useBatchUpdate();
                
				data.db=new Database(meta.getDatabase());
				
				data.db.connect();

				logBasic("Connected to database ["+meta.getDatabase()+"] (commit="+meta.getCommitSize()+")");
				data.db.setCommit(meta.getCommitSize());
				
                if (!meta.isPartitioningEnabled() && !meta.isTableNameInField())
                {
                    if (meta.truncateTable() && getCopy()==0) // Only the first one truncates!!!
    				{
    					data.db.truncateTable(Const.replEnv( meta.getTablename() ));
    				}
                }
                
				return true;
			}
			catch(KettleException e)
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
		meta=(TableOutputMeta)smi;
		data=(TableOutputData)sdi;

		try
		{
            Iterator preparedStatements = data.preparedStatements.values().iterator();
            while (preparedStatements.hasNext())
            {
                PreparedStatement insertStatement = (PreparedStatement) preparedStatements.next();
                data.db.insertFinished(insertStatement, data.batchMode);
            }
		}
		catch(KettleDatabaseBatchException be)
		{
		    logError("Unexpected batch update error committing the database connection: "+be.toString());
			setErrors(1);
			stopAll();
		}
		catch(Exception dbe)
		{
		    dbe.printStackTrace();
			logError("Unexpected error committing the database connection: "+dbe.toString());
			setErrors(1);
			stopAll();
		}
		
		data.db.disconnect();
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
			logError("Unexpected error encountered ["+debug+"] : "+e.toString());
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
