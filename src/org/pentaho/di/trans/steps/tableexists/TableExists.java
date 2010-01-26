/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.tableexists;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Check if a table exists in a Database
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class TableExists extends BaseStep implements StepInterface
{
	private static Class<?> PKG = TableExistsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    private TableExistsMeta meta;
    private TableExistsData data;
    
    public TableExists(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
   
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(TableExistsMeta)smi;
        data=(TableExistsData)sdi;
        
        boolean sendToErrorRow=false;
        String errorMessage = null;

        Object[] r = getRow();      // Get row from input rowset & set row busy!
        if (r==null)  // no more input to be expected...
        {
            setOutputDone();
            return false;
        }
           
        boolean tablexists=false;
        try
        {
        	if(first)
        	{
        		first=false;
    			data.outputRowMeta = getInputRowMeta().clone();
    			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
    			
        		// Check is tablename field is provided
				if (Const.isEmpty(meta.getDynamicTablenameField()))
				{
					logError(BaseMessages.getString(PKG, "TableExists.Error.TablenameFieldMissing"));
					throw new KettleException(BaseMessages.getString(PKG, "TableExists.Error.TablenameFieldMissing"));
				}
				
				// cache the position of the field			
				if (data.indexOfTablename<0)
				{	
					data.indexOfTablename =getInputRowMeta().indexOfValue(meta.getDynamicTablenameField());
					if (data.indexOfTablename<0)
					{
						// The field is unreachable !
						logError(BaseMessages.getString(PKG, "TableExists.Exception.CouldnotFindField")+ "[" + meta.getDynamicTablenameField()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleException(BaseMessages.getString(PKG, "TableExists.Exception.CouldnotFindField",meta.getDynamicTablenameField())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}
        	}// End If first 
        	
        	// get tablename
        	String tablename= getInputRowMeta().getString(r,data.indexOfTablename);	
        	
        	if(data.realSchemaname!=null)
        		tablename = data.db.getDatabaseMeta().getQuotedSchemaTableCombination(data.realSchemaname, tablename);
        	else
        		tablename = data.db.getDatabaseMeta().quoteField(tablename);
        		
        	// Check if table exists on the specified connection
        	tablexists=data.db.checkTableExists(tablename);
        	
			Object[] outputRowData =RowDataUtil.addValueData(r, getInputRowMeta().size(),tablexists);
			
			 //	add new values to the row.
	        putRow(data.outputRowMeta, outputRowData);  // copy row to output rowset(s);

	        if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "TableExists.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
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
	            logError(BaseMessages.getString(PKG, "TableExists.ErrorInStepRunning" + " : "+ e.getMessage()));
	            throw new KettleStepException(BaseMessages.getString(PKG, "TableExists.Log.ErrorInStep"), e);
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, meta.getResultFieldName(), "TableExistsO01");
        	}
        }
            
        return true;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(TableExistsMeta)smi;
        data=(TableExistsData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getResultFieldName()))
        	{
        		logError(BaseMessages.getString(PKG, "TableExists.Error.ResultFieldMissing"));
        		return false;
        	}
        	
            data.db=new Database(this, meta.getDatabase());
            data.db.shareVariablesWith(this);
            if(!Const.isEmpty(meta.getSchemaname())) 
            	data.realSchemaname= environmentSubstitute(meta.getSchemaname());
        	
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

                if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "TableExists.Log.ConnectedToDB")); //$NON-NLS-1$
                
                return true;
            }
            catch(KettleException e)
            {
                logError(BaseMessages.getString(PKG, "TableExists.Log.DBException")+e.getMessage()); //$NON-NLS-1$
                if (data.db!=null) {
                	data.db.disconnect();
                }
            }
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (TableExistsMeta)smi;
        data = (TableExistsData)sdi;
        if(data.db!=null) {
        	data.db.disconnect();
        }
        super.dispose(smi, sdi);
    }
}
