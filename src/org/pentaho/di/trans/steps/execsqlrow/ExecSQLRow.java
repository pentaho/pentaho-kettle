/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.execsqlrow;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.ValueMeta;
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
 * Execute one or more SQL statements in a script, one time or parameterised (for every row)
 * 
 * @author Matt
 * @since 10-sep-2005
 */
public class ExecSQLRow extends BaseStep implements StepInterface
{
	private static Class<?> PKG = ExecSQLRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private ExecSQLRowMeta meta;
	private ExecSQLRowData data;
	
	public ExecSQLRow(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
    
	public static final RowMetaAndData getResultRow(Result result, String upd, String ins, String del, String read)
	{
		RowMetaAndData resultRow = new RowMetaAndData();
        
		if (upd != null && upd.length() > 0)
		{
			ValueMeta meta = new ValueMeta(upd, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			resultRow.addValue(meta, new Long(result.getNrLinesUpdated()));
		}

		if (ins != null && ins.length() > 0)
		{
			ValueMeta meta = new ValueMeta(ins, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			resultRow.addValue(meta, new Long(result.getNrLinesOutput()));
		}

		if (del != null && del.length() > 0)
		{
			ValueMeta meta = new ValueMeta(del, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			resultRow.addValue(meta, new Long(result.getNrLinesDeleted()));
		}

		if (read != null && read.length() > 0)
		{
			ValueMeta meta = new ValueMeta(read, ValueMetaInterface.TYPE_INTEGER);
			meta.setLength(ValueMetaInterface.DEFAULT_INTEGER_LENGTH, 0);
			resultRow.addValue(meta, new Long(result.getNrLinesRead()));
		}

		return resultRow;
    }
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(ExecSQLRowMeta)smi;
        data=(ExecSQLRowData)sdi;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;
        
        Object[] row = getRow();
        if (row==null) // no more input to be expected...
        {
            setOutputDone();
            return false;
        }

		if (first) // we just got started
		{
            first=false;
			
            data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
            // Check is SQL field is provided
			if (Const.isEmpty(meta.getSqlFieldName()))
			{
				throw new KettleException(BaseMessages.getString(PKG, "ExecSQLRow.Error.SQLFieldFieldMissing"));
			}
			
			// cache the position of the field			
			if (data.indexOfSQLFieldname<0)
			{	
				data.indexOfSQLFieldname =this.getInputRowMeta().indexOfValue(meta.getSqlFieldName());
				if (data.indexOfSQLFieldname<0)
				{
					// The field is unreachable !
					throw new KettleException(BaseMessages.getString(PKG, "ExecSQLRow.Exception.CouldnotFindField",meta.getSqlFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
           
		}

    	// get SQL
    	String SQLScript= getInputRowMeta().getString(row,data.indexOfSQLFieldname);
    	 
		try
		{
	        if (log.isDebug()) logDebug(BaseMessages.getString(PKG, "ExecSQLRow.Log.ExecutingSQLScript")+Const.CR+SQLScript); //$NON-NLS-1$
	        data.result = data.db.execStatement(SQLScript);
	
			RowMetaAndData add = getResultRow(data.result, meta.getUpdateField(), meta.getInsertField(), meta.getDeleteField(), meta.getReadField());			
			row = RowDataUtil.addRowData(row, getInputRowMeta().size(), add.getData());
	        

	        if(meta.getCommitSize()>0)
	        {
		        if (!data.db.isAutoCommit())
		        {
		        	if(meta.getCommitSize()==1) data.db.commit();
		        	else if (getLinesWritten()%meta.getCommitSize()==0)   data.db.commit();
		        }
	        }
			
			putRow(data.outputRowMeta,row); // send it out!
			
			if (checkFeedback(getLinesWritten()))
			{
				if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "ExecSQLRow.Log.LineNumber") + getLinesWritten()); //$NON-NLS-1$
			}
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{
				logError(BaseMessages.getString(PKG, "ExecSQLRow.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), row, 1, errorMessage, null, "ExecSQLRow001");
			}
		}
		return true;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
        meta=(ExecSQLRowMeta)smi;
        data=(ExecSQLRowData)sdi;

        if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "ExecSQLRow.Log.FinishingReadingQuery")); //$NON-NLS-1$
     
        if( data.db!=null) data.db.disconnect();


		super.dispose(smi, sdi);
	}
	
	/** Stop the running query */
	public void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(ExecSQLRowMeta)smi;
        data=(ExecSQLRowData)sdi;

        if (data.db!=null) data.db.cancelQuery();
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(ExecSQLRowMeta)smi;
		data=(ExecSQLRowData)sdi;

		if (super.init(smi, sdi))
		{
			if(meta.getDatabaseMeta()==null) {
        		logError(BaseMessages.getString(PKG, "ExecSQLRow.Init.ConnectionMissing", getStepname()));
        		return false;
        	}
			data.db = new Database(this, meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
            
            // Connect to the database
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

                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "ExecSQLRow.Log.ConnectedToDB")); //$NON-NLS-1$

                if(meta.getCommitSize()>1) data.db.setCommit(meta.getCommitSize());
                return true;
            }
            catch(KettleException e)
            {
                logError(BaseMessages.getString(PKG, "ExecSQLRow.Log.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
                setErrors(1);
                stopAll();
            }
		}
		
		return false;
	}
	
}