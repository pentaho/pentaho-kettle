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
 
package org.pentaho.di.trans.steps.dbproc;

import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Retrieves values from a database by calling database stored procedures or functions
 *  
 * @author Matt
 * @since 26-apr-2003
 *
 */
public class DBProc extends BaseStep implements StepInterface
{
	private static Class<?> PKG = DBProcMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DBProcMeta meta;
	private DBProcData data;
	
	public DBProc(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private Object[] runProc(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		if (first)
		{
        	first=false;
        	
			// get the RowMeta for the output 
        	// 
			data.outputMeta = data.inputRowMeta.clone();
			meta.getFields(data.outputMeta, getStepname(), null, null, this);
        	
			data.argnrs=new int[meta.getArgument().length];
			for (int i=0;i<meta.getArgument().length;i++)
			{
				if (!meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) // IN or INOUT //$NON-NLS-1$
				{
					data.argnrs[i]=rowMeta.indexOfValue(meta.getArgument()[i]);
					if (data.argnrs[i]<0)
					{
						logError(BaseMessages.getString(PKG, "DBProc.Log.ErrorFindingField")+meta.getArgument()[i]+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleStepException(BaseMessages.getString(PKG, "DBProc.Exception.CouldnotFindField",meta.getArgument()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
				else
				{
					data.argnrs[i]=-1;
				}
			}
			
			data.db.setProcLookup(meta.getProcedure(), meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), 
			                      meta.getResultName(), meta.getResultType());
		}

		Object[] outputRowData = RowDataUtil.resizeArray(rowData, data.outputMeta.size());
		int outputIndex = rowMeta.size();

		data.db.setProcValues(rowMeta, rowData, data.argnrs, meta.getArgumentDirection(), !Const.isEmpty(meta.getResultName())); 

		RowMetaAndData add=data.db.callProcedure(meta.getArgument(), meta.getArgumentDirection(), meta.getArgumentType(), meta.getResultName(), meta.getResultType());
		int addIndex = 0;
		
		// Function return?
		if (!Const.isEmpty(meta.getResultName())) {
			outputRowData[outputIndex++]=add.getData()[addIndex++]; //first is the function return
		} 
		
        // We are only expecting the OUT and INOUT arguments here.
        // The INOUT values need to replace the value with the same name in the row.
        //
		for (int i = 0; i < data.argnrs.length; i++) {
			if (meta.getArgumentDirection()[i].equalsIgnoreCase("OUT")) {
				// add
				outputRowData[outputIndex++] = add.getData()[addIndex++]; 
			} else if (meta.getArgumentDirection()[i].equalsIgnoreCase("INOUT")) {
				// replace
				outputRowData[data.argnrs[i]]=add.getData()[addIndex];
				addIndex++;
			}
			// IN not taken
		}
		return outputRowData;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;
		
		boolean sendToErrorRow=false;
		String errorMessage = null;

		// A procedure/function could also have no input at all
		// However, we would still need to know how many times it gets executed.
		// In short: the procedure gets executed once for every input row.
		//
		Object[] r;
		
		if (data.readsRows)
		{
			r=getRow();       // Get row from input rowset & set row busy!
			if (r==null)  // no more input to be expected...
			{
				setOutputDone();
				return false;
			}
			data.inputRowMeta = getInputRowMeta();
		}
		else
		{
			r=new Object[] {}; // empty row
            incrementLinesRead();
            data.inputRowMeta = new RowMeta(); // empty row metadata too
            data.readsRows=true; // make it drop out of the loop at the next entrance to this method
		}
		
		try
		{
			Object[] outputRowData = runProc(data.inputRowMeta, r); // add new values to the row in rowset[0].
			putRow(data.outputMeta, outputRowData);  // copy row to output rowset(s);
				
            if (checkFeedback(getLinesRead())) 
            {
            	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "DBProc.LineNumber")+getLinesRead()); //$NON-NLS-1$
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
			
				logError(BaseMessages.getString(PKG, "DBProc.ErrorInStepRunning")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
	        }
			
			 if (sendToErrorRow)
	         {
				 // Simply add this row to the error row
	             putError(getInputRowMeta(), r, 1, errorMessage, null, "DBP001");
	         }
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(DBProcMeta)smi;
		data=(DBProcData)sdi;

		if (super.init(smi, sdi))
		{
			data.readsRows = false;
	        List<StepMeta> previous = getTransMeta().findPreviousSteps(getStepMeta());
			if (previous!=null && previous.size()>0)
			{
				data.readsRows = true;
			}

			data.db=new Database(this, meta.getDatabase());
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

                if (!meta.isAutoCommit())
                {
                    if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "DBProc.Log.AutoCommit")); //$NON-NLS-1$
                    data.db.setCommit(9999);
                }
                if (log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "DBProc.Log.ConnectedToDB")); //$NON-NLS-1$
				
				return true;
			}
			catch(KettleException e)
			{
				logError(BaseMessages.getString(PKG, "DBProc.Log.DBException")+e.getMessage()); //$NON-NLS-1$
				if (data.db!=null) {
                	data.db.disconnect();
				}
			}
		}
		return false;
	}
		
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
	    meta = (DBProcMeta)smi;
	    data = (DBProcData)sdi;
	    
        try
        {
            if (!meta.isAutoCommit())
            {
                data.db.commit();
            }
        }
        catch(KettleDatabaseException e)
        {
            logError(BaseMessages.getString(PKG, "DBProc.Log.CommitError")+e.getMessage());
        }
        finally
        {
        	if (data.db!=null) {
            	data.db.disconnect();
        	}
        }
	    
	    super.dispose(smi, sdi);
	}

	public String toString()
	{
		return this.getClass().getName();
	}

}