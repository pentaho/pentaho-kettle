 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/
 

package org.pentaho.di.trans.steps.gettablenames;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;


/**
 * Return tables name list from Database connection
 *  *  
 * @author Samatar
 * @since 03-Juin-2008
 *
 */

public class GetTableNames extends BaseStep implements StepInterface
{
	private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	
    private GetTableNamesMeta meta;
    private GetTableNamesData data;
    
    public GetTableNames(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
    {
        super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
    }
    
    /**
	 * Build an empty row based on the meta-data...
	 * 
	 * @return
	 */

	private Object[] buildEmptyRow()
	{
        Object[] rowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
 
		 return rowData;
	}
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
    {
        meta=(GetTableNamesMeta)smi;
        data=(GetTableNamesData)sdi;
        
        String tableNames[] = data.db.getTablenames();
    	
        if(meta.isIncludeTable())
        {
			String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Table");
			
			for(int i=0; i<tableNames.length && !isStopped();i++)
			{
		        Object[] outputRow = buildEmptyRow();
		    	int outputIndex = 0;
		    	
				String tableName = tableNames[i];	
	        	outputRow[outputIndex++]=tableName;
	        	
	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
	    		{
	    			outputRow[outputIndex++]=ObjectType;
	    		}	    		
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
	    		{
	    			outputRow[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(tableName));
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
	
	            if (checkFeedback(getLinesRead())) 
	            {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRow)));
			}
		}
		// Views
		if(meta.isIncludeView())
        {
            try{
            	String viewNames[]=data.db.getViews();
            	String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.View");
    	        for(int i=0; i<viewNames.length && !isStopped();i++)
    	        {
    	            Object[] outputRow = buildEmptyRow();
    	        	int outputIndex = 0;
    	        	
    	        	String viewName=viewNames[i];
    	        	outputRow[outputIndex++]=viewName;
    	    		
    	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
    	    		{
        	        	outputRow[outputIndex++]=ObjectType;
    	    		}
    	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
    	    		{
    	    			outputRow[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(viewName));
    	    		}
    	    		data.rownr++;
    	    		putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
    	    		
    	            if (checkFeedback(getLinesRead())) 
    	            {
    	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
    	            }
    	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRow))); //$NON-NLS-1$  
    	        }
            }catch(Exception e){};
        }
		 if(meta.isIncludeProcedure())
         {
	        String procNames[]=data.db.getProcedures();
	        String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Procedure");
	        for(int i=0; i<procNames.length && !isStopped();i++)
	        {
	            Object[] outputRow = buildEmptyRow();
	        	int outputIndex = 0;
	        	
	        	String procName=procNames[i];
	        	outputRow[outputIndex++]=procName;

	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
	    		{
	    			outputRow[outputIndex++]=ObjectType;
	    		}
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
	    		{
	    			outputRow[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(procName));
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
	    		
	            if (checkFeedback(getLinesRead())) 
	            {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRow))); //$NON-NLS-1$  
	        }
	     }
        if(meta.isIncludeSyonym())
        {
	        String Synonyms[]=data.db.getSynonyms();
	        String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Synonym");
	        for(int i=0; i<Synonyms.length && !isStopped();i++)
	        {
	            Object[] outputRow = buildEmptyRow();
	        	int outputIndex = 0;
	        	
	        	String Synonym=Synonyms[i];
	        	
	        	outputRow[outputIndex++]=Synonym;

	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
	    		{
	    			outputRow[outputIndex++]=ObjectType;
	    		}
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
	    		{
	    			outputRow[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(Synonym));
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRow);  // copy row to output rowset(s);
	    		
	            if (checkFeedback(getLinesRead())) 
	            {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRow))); //$NON-NLS-1$   
	        }
	   }

        setOutputDone();
        return false;
    }
    
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(GetTableNamesMeta)smi;
        data=(GetTableNamesData)sdi;

        if (super.init(smi, sdi))
        {
        	if(Const.isEmpty(meta.getTablenameFieldName()))
        	{
        		log.logError(toString(), BaseMessages.getString(PKG, "GetTableNames.Error.TablenameFieldNameMissing"));
        		return false;
        	}
        	data.realTableNameFieldName=environmentSubstitute(meta.getTablenameFieldName());
        	data.realObjectTypeFieldName=environmentSubstitute(meta.getObjectTypeFieldName());
        	data.realIsSystemObjectFieldName=environmentSubstitute(meta.isSystemObjectFieldName());
        	if(!meta.isIncludeTable() && !meta.isIncludeView() && !meta.isIncludeProcedure() && !meta.isIncludeSyonym())
        	{
        		log.logError(toString(), BaseMessages.getString(PKG, "GetTableNames.Error.includeAtLeastOneType"));
        		return false;
        	}
        	
        	try 
        	{
				 // Create the output row meta-data
	            data.outputRowMeta = new RowMeta();
	            meta.getFields(data.outputRowMeta, getStepname(), null, null, this); // get the metadata populated  
        	}
            catch(Exception e)
			{
				logError("Error initializing step: "+e.toString());
				logError(Const.getStackTracker(e));
				return false;
			}
            
            data.db=new Database(meta.getDatabase()); 
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

                if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.Log.ConnectedToDB")); //$NON-NLS-1$
                
                return true;
            }
            catch(KettleException e)
            {
                logError(BaseMessages.getString(PKG, "GetTableNames.Log.DBException")+e.getMessage()); //$NON-NLS-1$
                if(data.db!=null) data.db.disconnect();
            }
        }
        return false;
    }
        
    public void dispose(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta = (GetTableNamesMeta)smi;
        data = (GetTableNamesData)sdi;
        if(data.db!=null)
        {
        	data.db.disconnect();
        	data.db=null;
        }
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