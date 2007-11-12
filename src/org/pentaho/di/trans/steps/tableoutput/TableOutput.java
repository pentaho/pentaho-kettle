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
 
package org.pentaho.di.trans.steps.tableoutput;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseBatchException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

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

		Object[] r=getRow();    // this also waits for a previous step to be finished.
		if (r==null)  // no more input to be expected...
		{
			return false;
		}
        
        if (first)
        {
            first=false;
            data.outputRowMeta = getInputRowMeta().clone();
            data.insertRowMeta = getInputRowMeta().clone();
        }
        
		try
		{
			Object[] outputRowData = writeToTable(getInputRowMeta(), r);
            
            if (outputRowData!=null)
            {
                putRow(data.outputRowMeta, outputRowData ); // in case we want it go further...
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

	private Object[] writeToTable(RowMetaInterface rowMeta, Object[] r) throws KettleException
	{
        Object[] outputRowData = r; // just return the input
        
		if (r==null) // Stop: last line or error encountered 
		{
			if (log.isDetailed()) logDetailed("Last line inserted: stop");
			return null;
		}

        PreparedStatement insertStatement = null;
        
        String tableName = null;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;
        boolean rowIsSafe = false;
        int[] updateCounts = null;
        List<Exception> exceptionsList = null;
        boolean batchProblem = false;
        Object generatedKey = null;
        
        if ( meta.isTableNameInField() )
        {
            // Cache the position of the table name field
            if (data.indexOfTableNameField<0)
            {
                data.indexOfTableNameField = rowMeta.indexOfValue(meta.getTableNameField());
                if (data.indexOfTableNameField<0)
                {
                    String message = "Unable to find table name field ["+meta.getTableNameField()+"] in input row";
                    log.logError(toString(), message);
                    throw new KettleStepException(message);
                }
                if (!meta.isTableNameInTable())
                {
                    data.insertRowMeta.removeValueMeta(data.indexOfTableNameField);
                }
            }
            tableName = rowMeta.getString(r, data.indexOfTableNameField);
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
                data.indexOfPartitioningField = rowMeta.indexOfValue(meta.getPartitioningField());
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
            
            ValueMetaInterface partitioningValue = rowMeta.getValueMeta(data.indexOfPartitioningField);
            if (!partitioningValue.isDate() || r[data.indexOfPartitioningField]==null)
            {
                throw new KettleStepException("Sorry, the partitioning field needs to contain a data value and can't be empty!");
            }
            
            Object partitioningValueData = rowMeta.getDate(r, data.indexOfPartitioningField); 
            tableName=environmentSubstitute(meta.getTablename())+"_"+data.dateFormater.format((Date)partitioningValueData);
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
            		              environmentSubstitute(meta.getSchemaName()), 
            		              tableName,
                                  data.insertRowMeta);
            if (log.isDetailed()) logDetailed("Prepared statement : "+sql);
            insertStatement = data.db.prepareSQL(sql, meta.isReturningGeneratedKeys());
            data.preparedStatements.put(schemaTable, insertStatement);
        }
        
		try
		{
			data.db.setValues(data.insertRowMeta, r, insertStatement);
			rowIsSafe = data.db.insertRow(insertStatement, data.batchMode);
			
			// See if we need to get back the keys as well...
			if (meta.isReturningGeneratedKeys())
			{
				RowMetaAndData extraKeys = data.db.getGeneratedKeys(insertStatement);

				if ( extraKeys.getRowMeta().size()>0 )
				{
  				    // Send out the good word!
  				    // Only 1 key at the moment. (should be enough for now :-)
				    generatedKey = extraKeys.getRowMeta().getInteger(extraKeys.getData(), 0);
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
    		    	Exception exception = be.getExceptionsList().get(x);
    		    	if (exception.getMessage()!=null) msg.append(exception.getMessage()).append(Const.CR);
    		    }
    		    throw new KettleException(msg.toString(), be);
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
    		            logBasic("WARNING: Couldn't insert row into table: "+rowMeta.getString(r)+Const.CR+dbe.getMessage());
    		        }
    		        else
    		        if (data.warnings==20)
    		        {
    		            logBasic("FINAL WARNING (no more then 20 displayed): Couldn't insert row into table: "+rowMeta.getString(r)+Const.CR+dbe.getMessage());
    		        }
    		        data.warnings++;
    		    }
    		    else
    		    {
    		        setErrors(getErrors()+1);
    		        data.db.rollback();
    		        throw new KettleException("Error inserting row into table ["+tableName+"] with values: "+rowMeta.getString(r), dbe);
    		    }
            }
		}
        
        // We need to add a key
        if (generatedKey!=null)
        {
            outputRowData = RowDataUtil.addValueData(outputRowData, rowMeta.size(), generatedKey);
        }
		
        if (data.batchMode)
        {
            if (sendToErrorRow) 
            {
                if (batchProblem)
                {
                    data.batchBuffer.add(outputRowData);
                    outputRowData = null;

                    processBatchException(errorMessage, updateCounts, exceptionsList);
                }
                else
                {
                    // Simply add this row to the error row
                    putError(data.outputRowMeta, outputRowData, 1L, errorMessage, null, "TOP001");
                    outputRowData=null;
                }
            }
            else
            {
                data.batchBuffer.add(r);
                outputRowData=null;
                
                if (rowIsSafe) // A commit was done and the rows are all safe (no error)
                {
                    for (int i=0;i<data.batchBuffer.size();i++)
                    {
                        Object[] row = (Object[]) data.batchBuffer.get(i);
                        putRow(data.outputRowMeta, row);
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
                putError(data.outputRowMeta, outputRowData, 1, errorMessage, null, "TOP001");
                outputRowData=null;
            }
        }
        
		return outputRowData;
	}
	
	private void processBatchException(String errorMessage, int[] updateCounts, List<Exception> exceptionsList) throws KettleException
    {
        // There was an error with the commit
        // We should put all the failing rows out there...
        //
        if (updateCounts!=null)
        {
            int errNr = 0;
            for (int i=0;i<updateCounts.length;i++)
            {
                Object[] row = (Object[]) data.batchBuffer.get(i);
                if (updateCounts[i]>0)
                {
                    // send the error foward
                    putRow(data.outputRowMeta, row);
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
                    putError(data.outputRowMeta, row, 1L, exMessage, null, "TOP0002");
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
                Object[] row = (Object[]) data.batchBuffer.get(i);
                putError(data.outputRowMeta, row, 1L, errorMessage, null, "TOP0003");
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
				data.db.shareVariablesWith(this);
				
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
                	data.tableName = environmentSubstitute(meta.getTablename());                
                	
                    // Only the first one truncates in a non-partitioned step copy
                    //
                    if (meta.truncateTable() && ( getCopy()==0 || !Const.isEmpty(getPartitionID())) )
    				{                	
    					data.db.truncateTable(environmentSubstitute(meta.getSchemaName()), 
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
            for (PreparedStatement insertStatement : data.preparedStatements.values())
            {
                data.db.insertFinished(insertStatement, data.batchMode);
            }
            for (int i=0;i<data.batchBuffer.size();i++)
            {
                Object[] row = (Object[]) data.batchBuffer.get(i);
                putRow(data.outputRowMeta, row);
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
                    logError("Unexpected error processing batch error", e);
                    setErrors(1);
                    stopAll();
                }
            }
            else
            {
                logError("Unexpected batch update error committing the database connection.", be);
    			setErrors(1);
    			stopAll();
            }
		}
		catch(Exception dbe)
		{
			logError("Unexpected error committing the database connection.", dbe);
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
                    logError("Unexpected error rolling back the database connection.", e);
                }
            }
            
		    data.db.disconnect();
            super.dispose(smi, sdi);
        }        
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : ", t); //$NON-NLS-1$ //$NON-NLS-2$
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