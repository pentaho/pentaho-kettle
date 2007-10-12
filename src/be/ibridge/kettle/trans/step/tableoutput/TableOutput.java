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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseBatchException;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.util.StringUtil;
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
			return false;
		}

		try
		{
			writeToTable(r);
            
            if (!r.isIgnored())
            {
                putRow(r); // in case we want it go further...
                linesOutput++;
            }

            if (checkFeedback(linesRead)) logBasic("linenr "+linesRead);
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

	private boolean writeToTable(Row r) throws KettleException
	{
		if (r==null) // Stop: last line or error encountered 
		{
			if (log.isDetailed()) logDetailed("Last line inserted: stop");
			return false;
		} 

        PreparedStatement insertStatement = null;
        
        String tableName = null;
        Value removedValue = null;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;
        boolean rowIsSafe = false;
        int[] updateCounts = null;
        List exceptionsList = null;
        boolean batchProblem = false;
        
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
            
            tableName=StringUtil.environmentSubstitute(meta.getTablename())+"_"+data.dateFormater.format(partitioningValue.getDate());
        }
        else
        {
            tableName  = data.tableName;
        }
        
        if (Const.isEmpty(tableName))
        {
            throw new KettleStepException("The tablename is not defined (empty)");
        }
          
        String schemaTable = data.db.getDatabaseMeta().getQuotedSchemaTableCombination(meta.getSchemaName(), tableName);
        insertStatement = (PreparedStatement) data.preparedStatements.get(schemaTable);
        if (insertStatement==null)
        {
            String sql = data.db.getInsertStatement(
            		              StringUtil.environmentSubstitute(meta.getSchemaName()), 
            		              tableName, r);
            if (log.isDetailed()) logDetailed("Prepared statement : "+sql);
            insertStatement = data.db.prepareSQL(sql, meta.isReturningGeneratedKeys());
            data.preparedStatements.put(schemaTable, insertStatement);
        }
        
		try
		{
			data.db.setValues(r, insertStatement);
			rowIsSafe = data.db.insertRow(insertStatement, data.batchMode);
			
			// See if we need to get back the keys as well...
			if (meta.isReturningGeneratedKeys())
			{
				Row extraKeys = data.db.getGeneratedKeys(insertStatement);

				if ( extraKeys != null )
				{
  				    // Send out the good word!
  				    // Only 1 key at the moment. (should be enough for now :-)
				    Value keyVal = extraKeys.getValue(0);
				    keyVal.setName(meta.getGeneratedKeyField());
				    r.addValue(keyVal);
				}
				else
				{
					// we have to throw something here, else we don't know what the
					// type is of the returned key(s) and we would violate our own rule
					// that a hop should always contain rows of the same type.
					throw new KettleStepException("No generated keys while \"return generated keys\" is active!");
				}
			}
        }
		catch(KettleDatabaseBatchException be)
		{
            errorMessage = be.toString();
            batchProblem = true;
            sendToErrorRow = true;
            updateCounts = be.getUpdateCounts();
            exceptionsList = be.getExceptionsList();
            
            if (getStepMeta().isDoingErrorHandling())
            {
                data.db.clearBatch(insertStatement);
                data.db.commit();
            }
            else
            {
    			data.db.clearBatch(insertStatement);
    		    data.db.rollback();
    		    StringBuffer msg = new StringBuffer("Error batch inserting rows into table ["+tableName+"].");
    		    msg.append(Const.CR);
    		    msg.append("Errors encountered (first 10):").append(Const.CR);
    		    for (int x = 0 ; x < be.getExceptionsList().size() && x < 10 ; x++)
    		    {
    		    	Exception exception = (Exception) be.getExceptionsList().get(x);
    		    	if (exception.getMessage()!=null) msg.append(exception.getMessage()).append(Const.CR);
    		    }
            }
		}
		catch(KettleDatabaseException dbe)
		{
            if (getStepMeta().isDoingErrorHandling())
            {
                sendToErrorRow = true;
                errorMessage = dbe.toString();
            }
            else
            {
    		    if (meta.ignoreErrors())
    		    {
    		        if (data.warnings<20)
    		        {
    		            logBasic("WARNING: Couldn't insert row into table: "+r+Const.CR+dbe.getMessage());
    		        }
    		        else
    		        if (data.warnings==20)
    		        {
    		            logBasic("FINAL WARNING (no more then 20 displayed): Couldn't insert row into table: "+r+Const.CR+dbe.getMessage());
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
		}
		
        if (meta.isTableNameInField() && !meta.isTableNameInTable())
        {
            r.addValue(data.indexOfTableNameField, removedValue);
        }
        
        if (data.batchMode)
        {
            if (sendToErrorRow) 
            {
                if (batchProblem)
                {
                    data.batchBuffer.add(r);
                    r.setIgnore();

                    processBatchException(errorMessage, updateCounts, exceptionsList);
                }
                else
                {
                    // Simply add this row to the error row
                    putError(r, 1L, errorMessage, null, "TOP001");
                    r.setIgnore();
                }
            }
            else
            {
                data.batchBuffer.add(r);
                r.setIgnore();
                
                if (rowIsSafe) // A commit was done and the rows are all safe (no error)
                {
                    for (int i=0;i<data.batchBuffer.size();i++)
                    {
                        Row row = (Row) data.batchBuffer.get(i);
                        putRow(row);
                        linesOutput++;
                    }
                    // Clear the buffer
                    data.batchBuffer.clear();
                }
            }
        }
        else
        {
            if (sendToErrorRow)
            {
                putError(r, 1, errorMessage, null, "TOP001");
                r.setIgnore();
            }
        }
        
		return true;
	}
	
	private void processBatchException(String errorMessage, int[] updateCounts, List exceptionsList) throws KettleStepException
    {
        // There was an error with the commit
        // We should put all the failing rows out there...
        //
        if (updateCounts!=null)
        {
            int errNr = 0;
            for (int i=0;i<updateCounts.length;i++)
            {
                Row row = (Row) data.batchBuffer.get(i);
                if (updateCounts[i]>0)
                {
                    // send the error foward
                    putRow(row);
                    linesOutput++;
                }
                else
                {
                    String exMessage = errorMessage;
                    if (errNr<exceptionsList.size())
                    {
                        SQLException se = (SQLException) exceptionsList.get(errNr);
                        errNr++;
                        exMessage = se.toString();
                    }
                    putError(row, 1L, exMessage, null, "TOP0002");
                }
            }
        }
        else
        {
            // If we don't have update counts, it probably means the DB doesn't support it.
            // In this case we don't have a choice but to consider all inserted rows to be error rows.
            // 
            for (int i=0;i<data.batchBuffer.size();i++)
            {
                Row row = (Row) data.batchBuffer.get(i);
                putError(row, 1L, errorMessage, null, "TOP0003");
            }
        }
        
        // Clear the buffer afterwards...
        data.batchBuffer.clear();
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
                
				data.db=new Database(meta.getDatabaseMeta());
				
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }
                
				logBasic("Connected to database ["+meta.getDatabaseMeta()+"] (commit="+meta.getCommitSize()+")");
				data.db.setCommit(meta.getCommitSize());
				
                if (!meta.isPartitioningEnabled() && !meta.isTableNameInField())
                {    
                	data.tableName = StringUtil.environmentSubstitute(meta.getTablename());                
                	
                    // Only the first one truncates in a non-partitioned step copy
                    //
                    if (meta.truncateTable() && ( getCopy()==0 || !Const.isEmpty(getPartitionID())) )
    				{                	
    					data.db.truncateTable(StringUtil.environmentSubstitute(meta.getSchemaName()), 
    							              data.tableName);
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
            for (int i=0;i<data.batchBuffer.size();i++)
            {
                Row row = (Row) data.batchBuffer.get(i);
                putRow(row);
                linesOutput++;
            }
            // Clear the buffer
            data.batchBuffer.clear();            
		}
		catch(KettleDatabaseBatchException be)
		{
            if (getStepMeta().isDoingErrorHandling())
            {
                // Right at the back we are expesriencing a batch commit problem...
                // OK, we have the numbers...
                try
                {
                    processBatchException(be.toString(), be.getUpdateCounts(), be.getExceptionsList());
                }
                catch(KettleException e)
                {
                    logError("Unexpected error processing batch error : "+e.toString());
                    setErrors(1);
                    stopAll();
                }
            }
            else
            {
                logError("Unexpected batch update error committing the database connection: "+be.toString());
    			setErrors(1);
    			stopAll();
            }
		}
		catch(Exception dbe)
		{
			logError("Unexpected error committing the database connection: "+dbe.toString());
            logError(Const.getStackTracker(dbe));
			setErrors(1);
			stopAll();
		}
		finally
        {
            setOutputDone();

            if (getErrors()>0)
            {
                try
                {
                    data.db.rollback();
                }
                catch(KettleDatabaseException e)
                {
                    logError("Unexpected error rolling back the database connection: "+e.toString());
                }
            }
            
		    data.db.disconnect();
            super.dispose(smi, sdi);
        }        
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
