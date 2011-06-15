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
 

package org.pentaho.di.trans.steps.tableinput;

import java.sql.ResultSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Reads information from a database table by using freehand SQL
 * 
 * @author Matt
 * @since 8-apr-2003
 */
public class TableInput extends BaseStep implements StepInterface
{
	private static Class<?> PKG = TableInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private TableInputMeta meta;
	private TableInputData data;
	
	public TableInput(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private RowMetaAndData readStartDate() throws KettleException
    {
		if (log.isDetailed()) logDetailed("Reading from step [" + data.infoStream.getStepname() + "]");

        RowMetaInterface parametersMeta = new RowMeta();
        Object[] parametersData = new Object[] {};

        RowSet rowSet = findInputRowSet(data.infoStream.getStepname());
        if (rowSet!=null) 
        {
	        Object[] rowData = getRowFrom(rowSet); // rows are originating from "lookup_from"
	        while (rowData!=null)
	        {
	            parametersData = RowDataUtil.addRowData(parametersData, parametersMeta.size(), rowData);
	            parametersMeta.addRowMeta(rowSet.getRowMeta());
	            
	            rowData = getRowFrom(rowSet); // take all input rows if needed!
	        }
	        
	        if (parametersMeta.size()==0)
	        {
	            throw new KettleException("Expected to read parameters from step ["+data.infoStream.getStepname()+"] but none were found.");
	        }
        }
        else
        {
            throw new KettleException("Unable to find rowset to read from, perhaps step ["+data.infoStream.getStepname()+"] doesn't exist. (or perhaps you are trying a preview?)");
        }
	
        RowMetaAndData parameters = new RowMetaAndData(parametersMeta, parametersData);

        return parameters;
    }	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (first) // we just got started
		{
            Object[] parameters;
            RowMetaInterface parametersMeta;
			first=false;
            
			// Make sure we read data from source steps...
            if (data.infoStream.getStepMeta()!=null)
            {
                if (meta.isExecuteEachInputRow())
                {
                	if (log.isDetailed()) logDetailed("Reading single row from stream [" + data.infoStream.getStepname() + "]");
                	data.rowSet = findInputRowSet(data.infoStream.getStepname());
                	if (data.rowSet==null) {
                    throw new KettleException("Unable to find rowset to read from, perhaps step ["+data.infoStream.getStepname()+"] doesn't exist. (or perhaps you are trying a preview?)");
                	}
                    parameters = getRowFrom(data.rowSet);
                    parametersMeta = data.rowSet.getRowMeta();
                }
                else
                {
                	if (log.isDetailed()) logDetailed("Reading query parameters from stream [" + data.infoStream.getStepname() + "]");
                    RowMetaAndData rmad = readStartDate(); // Read values in lookup table (look)
                    parameters = rmad.getData();
                    parametersMeta = rmad.getRowMeta();
                }
                if (parameters!=null)
                {
                    if (log.isDetailed()) logDetailed("Query parameters found = " + parametersMeta.getString(parameters));
                }
            }
            else
            {
                parameters = new Object[] {};
                parametersMeta = new RowMeta();
			}
            
            if (meta.isExecuteEachInputRow() && ( parameters==null || parametersMeta.size()==0) )
            {
                setOutputDone(); // signal end to receiver(s)
                return false; // stop immediately, nothing to do here.
            }
            
            boolean success = doQuery(parametersMeta, parameters);
            if (!success) 
            { 
                return false; 
            }
		}
        else
        {
            if (data.thisrow!=null) // We can expect more rows
            {
                data.nextrow=data.db.getRow(data.rs, meta.isLazyConversionActive()); 
                if (data.nextrow!=null) incrementLinesInput();
            }
        }

    	if (data.thisrow == null) // Finished reading?
        {
            boolean done = false;
            if (meta.isExecuteEachInputRow()) // Try to get another row from the input stream
            {
                Object[] nextRow = getRowFrom(data.rowSet);
                if (nextRow == null) // Nothing more to get!
                {
                    done = true;
                }
                else
                {
                    // First close the previous query, otherwise we run out of cursors!
                    closePreviousQuery();
                    
                    boolean success = doQuery(data.rowSet.getRowMeta(), nextRow); // OK, perform a new query
                    if (!success) 
                    { 
                        return false; 
                    }
                    
                    if ( data.thisrow != null )
                    {
                        putRow(data.rowMeta, data.thisrow); // fill the rowset(s). (wait for empty)
                        data.thisrow = data.nextrow;

                        if (checkFeedback(getLinesInput())) 
                        {
                        	if(log.isBasic()) logBasic("linenr " + getLinesInput());
                        }
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
        else
        {
            putRow(data.rowMeta, data.thisrow); // fill the rowset(s). (wait for empty)
            data.thisrow = data.nextrow;

            if (checkFeedback(getLinesInput())) 
            {
            	if(log.isBasic()) logBasic("linenr " + getLinesInput());
            }
        }
		
		return true;
	}
    
    private void closePreviousQuery() throws KettleDatabaseException {
        if(data.db!=null) {
        	data.db.closeQuery(data.rs);
        }
    }

    private boolean doQuery(RowMetaInterface parametersMeta, Object[] parameters) throws KettleDatabaseException
    {
        boolean success = true;

        // Open the query with the optional parameters received from the source steps.
        String sql = null;
        if (meta.isVariableReplacementActive()) sql = environmentSubstitute(meta.getSQL());
        else sql = meta.getSQL();
        
        if (log.isDetailed()) logDetailed("SQL query : "+sql);
        if (parametersMeta.isEmpty()) {
        	data.rs = data.db.openQuery(sql, null, null, ResultSet.FETCH_FORWARD, meta.isLazyConversionActive());
        } else {
        	data.rs = data.db.openQuery(sql, parametersMeta, parameters, ResultSet.FETCH_FORWARD, meta.isLazyConversionActive());
        }
        if (data.rs == null)
        {
            logError("Couldn't open Query [" + sql + "]");
            setErrors(1);
            stopAll();
            success = false;
        }
        else
        {
            // Keep the metadata
            data.rowMeta = data.db.getReturnRowMeta();
            
            // 	Set the origin on the row metadata...
            if (data.rowMeta!=null) {
            	for (ValueMetaInterface valueMeta : data.rowMeta.getValueMetaList()) {
            		valueMeta.setOrigin(getStepname());
            	}
            }
            
            // Get the first row...
            data.thisrow = data.db.getRow(data.rs);
            if (data.thisrow != null)
            {
                incrementLinesInput();
                data.nextrow = data.db.getRow(data.rs);
                if (data.nextrow != null) incrementLinesInput();
            }
        }
        return success;
    }

	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		if(log.isBasic()) logBasic("Finished reading query, closing connection.");
		try
		{
		    closePreviousQuery();
		}
		catch(KettleException e)
		{
			logError("Unexpected error closing query : "+e.toString());
		    setErrors(1);
		    stopAll();
		}
		finally 
		{
			if (data.db!=null) {
            	data.db.disconnect();
			}
		}

		super.dispose(smi, sdi);
	}
	
	/** Stop the running query */
	public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(TableInputMeta)smi;
        data=(TableInputData)sdi;

        setStopped(true);
        
        if (data.db!=null && !data.isCanceled)
        {
          synchronized(data.db) { 
            data.db.cancelQuery();
          }
        	data.isCanceled=true;
        }
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(TableInputMeta)smi;
		data=(TableInputData)sdi;

		if (super.init(smi, sdi))
		{
			// Verify some basic things first...
			//
			boolean passed=true;
			if (Const.isEmpty(meta.getSQL())) {
				logError(BaseMessages.getString(PKG, "TableInput.Exception.SQLIsNeeded"));
				passed=false;
			}

			if (meta.getDatabaseMeta()==null) {
				logError(BaseMessages.getString(PKG, "TableInput.Exception.DatabaseConnectionsIsNeeded"));
				passed=false;
			}
			if (!passed) return false;

	        data.infoStream = meta.getStepIOMeta().getInfoStreams().get(0);
	        if(meta.getDatabaseMeta()==null) {
        		logError(BaseMessages.getString(PKG, "TableInput.Init.ConnectionMissing", getStepname()));
        		return false;
        	}
			data.db=new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
			
			data.db.setQueryLimit(Const.toInt(environmentSubstitute(meta.getRowLimit()),0));

			try
			{
                if (getTransMeta().isUsingUniqueConnections())
                {
                    synchronized (getTrans()) { data.db.connect(getTrans().getThreadName(), getPartitionID()); }
                }
                else
                {
                    data.db.connect(getPartitionID());
                }

                if (meta.getDatabaseMeta().isRequiringTransactionsOnQueries())
                {
                    data.db.setCommit(100); // needed for PGSQL it seems...
                }
                if (log.isDetailed()) logDetailed("Connected to database...");

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

  public boolean isWaitingForData() {
    return true;
  }

}