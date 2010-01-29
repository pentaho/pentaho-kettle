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

package org.pentaho.di.trans.steps.dynamicsqlrow;

import java.sql.ResultSet;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.dynamicsqlrow.Messages;
import org.pentaho.di.core.Const;


/**
 * Run dynamic SQL.
 * SQL is defined in a field.
 * 
 * @author Samatar
 * @since 13-10-2008
 */
public class DynamicSQLRow extends BaseStep implements StepInterface
{
	private DynamicSQLRowMeta meta;
	private DynamicSQLRowData data;
	
	
	public DynamicSQLRow(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void lookupValues(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		boolean loadFromBuffer=true;
		if (first)
		{
			first=false;
			data.outputRowMeta = rowMeta.clone();
			meta.getFields(data.outputRowMeta, getStepname(), new RowMetaInterface[] { meta.getTableFields(), }, null, this);
			
			loadFromBuffer=false;
		}
		
		if (log.isDetailed()) logDetailed(Messages.getString("DynamicSQLRow.Log.CheckingRow")+rowMeta.getString(rowData)); //$NON-NLS-1$
		
		
		// get dynamic SQL statement
		String sql=getInputRowMeta().getString(rowData,data.indexOfSQLField);
		
		if(log.isDebug()) log.logDebug(toString(), Messages.getString("DynamicSQLRow.Log.SQLStatement",sql));
		
		if(meta.isQueryOnlyOnChange())
		{
			if(loadFromBuffer)
			{
				if(!data.previousSQL.equals(sql))	loadFromBuffer=false;
			}
		}else
			loadFromBuffer=false;
		
		
		if(loadFromBuffer)
		{
			incrementLinesInput();

			if(!data.skipPreviousRow)
			{
				Object[] newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
				int newIndex = rowMeta.size();
				RowMetaInterface addMeta = data.db.getReturnRowMeta();

				// read from Buffer
				for (int p=0;p<data.previousrowbuffer.size();p++)
				{
					Object[] getBufferRow=(Object[])data.previousrowbuffer.get(p);
					for (int i=0;i<addMeta.size();i++) 
					{
						newRow[newIndex++] = getBufferRow[i];
					}
					putRow(data.outputRowMeta,data.outputRowMeta.cloneRow(newRow));
				}
			}
		}else
		{
			if(meta.isQueryOnlyOnChange()) data.previousrowbuffer.clear();
			
			// Set the values on the prepared statement (for faster exec.)
			ResultSet rs = data.db.openQuery(sql);
			
			// Get a row from the database...
			Object[] add = data.db.getRow(rs);
			RowMetaInterface addMeta = data.db.getReturnRowMeta();
			
			incrementLinesInput();
			
			int counter = 0;
			while (add!=null && (meta.getRowLimit()==0 || counter<meta.getRowLimit()))
			{
				counter++;
	
				Object[] newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
				int newIndex = rowMeta.size();
				for (int i=0;i<addMeta.size();i++) {
					newRow[newIndex++] = add[i];
				}
				// we have to clone, otherwise we only get the last new value
				putRow(data.outputRowMeta, data.outputRowMeta.cloneRow(newRow));
				
				if(meta.isQueryOnlyOnChange())
				{
					// add row to the previous rows buffer
					data.previousrowbuffer.add(add);
					data.skipPreviousRow=false;
				}
				
				if (log.isRowLevel()) logRowlevel(Messages.getString("DynamicSQLRow.Log.PutoutRow")+data.outputRowMeta.getString(newRow)); //$NON-NLS-1$
				
				
				// Get a new row
				if (meta.getRowLimit()==0 || counter<meta.getRowLimit()) 
				{
					add = data.db.getRow(rs);
					incrementLinesInput();
				}
			}
			
			// Nothing found? Perhaps we have to put something out after all?
			if (counter==0 && meta.isOuterJoin())
			{
				if (data.notfound==null)
				{
					data.notfound = new Object[data.db.getReturnRowMeta().size()];
				}
				Object[] newRow = RowDataUtil.resizeArray(rowData, data.outputRowMeta.size());
				int newIndex = rowMeta.size();
				for (int i=0;i<data.notfound.length;i++) {
					newRow[newIndex++] = data.notfound[i];
				}
				putRow(data.outputRowMeta, newRow);
				
				if(meta.isQueryOnlyOnChange())
				{
					// add row to the previous rows buffer
					data.previousrowbuffer.add(data.notfound);
					data.skipPreviousRow=false;
				}
			} else
			{
				if(meta.isQueryOnlyOnChange() && counter==0 && !meta.isOuterJoin())
				{
					data.skipPreviousRow=true;
				}
			}
			
			if(data.db!=null) data.db.closeQuery(rs);
		}
	
		// Save current parameters value as previous ones	
		if(meta.isQueryOnlyOnChange())
		{
			data.previousSQL= sql;
		}
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DynamicSQLRowMeta)smi;
		data=(DynamicSQLRowData)sdi;

		Object[] r=getRow();       // Get row from input rowset & set row busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		if (first)
		{
			if(Const.isEmpty(meta.getSQLFieldName()))
				throw new KettleException(Messages.getString("DynamicSQLRow.Exception.SQLFieldNameEmpty"));
			
			if(Const.isEmpty(meta.getSql()))
				throw new KettleException(Messages.getString("DynamicSQLRow.Exception.SQLEmpty"));

			// cache the position of the field			
			if (data.indexOfSQLField<0)
			{	
				data.indexOfSQLField =getInputRowMeta().indexOfValue(meta.getSQLFieldName());
				if (data.indexOfSQLField<0)
				{
					// The field is unreachable !
					throw new KettleException(Messages.getString("DynamicSQLRow.Exception.FieldNotFound",meta.getSQLFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		try
		{
			lookupValues(getInputRowMeta(), r); 
 
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isDetailed()) logDetailed(Messages.getString("DynamicSQLRow.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
            }
		}
		catch(KettleException e)
		{
			boolean sendToErrorRow=false;
			String errorMessage = null;
			
			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("DynamicSQLRow.Log.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(getInputRowMeta(), r, 1, errorMessage, null, "DynamicSQLRow001");
			}
		}		
			
		return true;
	}
    
	   /** Stop the running query */
    public synchronized void stopRunning(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(DynamicSQLRowMeta)smi;
        data=(DynamicSQLRowData)sdi;

        if (data.db!=null && !data.isCanceled)
        {
            data.db.cancelQuery();
            setStopped(true);
            data.isCanceled=true;
        }
    }

	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DynamicSQLRowMeta)smi;
		data=(DynamicSQLRowData)sdi;

		if (super.init(smi, sdi))
		{
			data.db=new Database(meta.getDatabaseMeta());
			data.db.shareVariablesWith(this);
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
				
                data.db.setCommit(100); // we never get a commit, but it just turns off auto-commit.

                if(log.isDetailed()) logDetailed(Messages.getString("DynamicSQLRow.Log.ConnectedToDB")); //$NON-NLS-1$
	
				data.db.setQueryLimit(meta.getRowLimit());
				
				return true;
			}
			catch(KettleException e)
			{
				logError(Messages.getString("DynamicSQLRow.Log.DatabaseError")+e.getMessage()); //$NON-NLS-1$
				if(data.db!=null) data.db.disconnect();
			}
		}
		
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DynamicSQLRowMeta)smi;
	    data = (DynamicSQLRowData)sdi;
	    
	    if(data.db!=null) data.db.disconnect();
	    
	    super.dispose(smi, sdi);
	}
	

    //
    // Run is were the action happens!
    public void run()
    {
    	BaseStep.runStepThread(this, meta, data);
    }
	
	public String toString()
	{
		return this.getClass().getName();
	}
}
