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
                
        
        if(meta.isDynamicSchema()) {
        	// Grab one row from previous step ...
        	data.readrow=getRow();
	
		   if (data.readrow==null) {
	          setOutputDone();
	          return false;
	       }
        }

        if(first)
        {
        	first=false;

	        if(meta.isDynamicSchema()) {
	        	data.inputRowMeta = getInputRowMeta();
				data.outputRowMeta = data.inputRowMeta.clone();
	        	// Get total previous fields
	        	data.totalpreviousfields=data.inputRowMeta.size();
	        	
	        	// Check is filename field is provided
				if (Const.isEmpty(meta.getSchemaFieldName())) {
					logError(BaseMessages.getString(PKG, "GetTableNames.Log.NoSchemaField"));
					throw new KettleException(BaseMessages.getString(PKG, "GetTableNames.Log.NoSchemaField"));
				}
				
	            
	            // cache the position of the field			
				if (data.indexOfSchemaField<0) {	
					data.indexOfSchemaField =data.inputRowMeta.indexOfValue(meta.getSchemaFieldName());
					if (data.indexOfSchemaField<0) {
						// The field is unreachable !
						logError(BaseMessages.getString(PKG, "GetTableNames.Log.ErrorFindingField")+ "[" + meta.getSchemaFieldName()+"]"); //$NON-NLS-1$ //$NON-NLS-2$
						throw new KettleException(BaseMessages.getString(PKG, "GetTableNames.Exception.CouldnotFindField",meta.getSchemaFieldName())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}   
	        	
	        }else{
	        	data.outputRowMeta = new RowMeta();
	        }
	        
	        meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
	        
        }
        
        if(meta.isDynamicSchema()) {
        	// Get value of dynamic schema ...
        	data.realSchemaName=data.inputRowMeta.getString(data.readrow,data.indexOfSchemaField);
        }

        
		Object[] outputRow = buildEmptyRow();
		if(meta.isDynamicSchema()) {
			System.arraycopy(data.readrow, 0, outputRow, 0, data.readrow.length);
		}
		
		
		// Catalogs
		if(meta.isIncludeCatalog()) {
			String ObjectType=BaseMessages.getString(PKG, "GetTableNames.ObjectType.Catalog");
	        // Views
			String catalogsNames[]= data.db.getCatalogs();

	        
			for(int i=0; i<catalogsNames.length && !isStopped();i++) {
		        
        		// Clone current input row
				Object[] outputRowCatalog = outputRow;
    			
		    	int outputIndex = data.totalpreviousfields;
		    	
				String catalogName = catalogsNames[i];	
				outputRowCatalog[outputIndex++]=catalogName;
	        	
	    		if(!Const.isEmpty(data.realObjectTypeFieldName)) {
	    			outputRowCatalog[outputIndex++]=ObjectType;
	    		}	    		
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName)) {
	    			outputRowCatalog[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(catalogName));
	    		}
	    		if(!Const.isEmpty(data.realSQLCreationFieldName)) {
	    			outputRowCatalog[outputIndex++]=null;
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRowCatalog);  // copy row to output rowset(s);
	
	            if (checkFeedback(getLinesRead())) {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowCatalog)));
			}
		}
        
		
		// Schemas
        if(meta.isIncludeSchema()) {
			String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Schema");
	        // Views
			String schemaNames[]= new String[] {};
			if(!Const.isEmpty(data.realSchemaName))
				schemaNames = new String[]{data.realSchemaName};
			else
				schemaNames = data.db.getSchemas();
	        
			for(int i=0; i<schemaNames.length && !isStopped();i++) {
		        
        		// Clone current input row
				Object[] outputRowSchema = outputRow;
    			
		    	int outputIndex = data.totalpreviousfields;
		    	
				String schemaName = schemaNames[i];	
				outputRowSchema[outputIndex++]=schemaName;
	        	
	    		if(!Const.isEmpty(data.realObjectTypeFieldName)) {
	    			outputRowSchema[outputIndex++]=ObjectType;
	    		}	    		
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName)) {
	    			outputRowSchema[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(schemaName));
	    		}
	    		if(!Const.isEmpty(data.realSQLCreationFieldName)) {
	    			outputRowSchema[outputIndex++]=null;
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRowSchema);  // copy row to output rowset(s);
	
	            if (checkFeedback(getLinesRead())) {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowSchema)));
			}
		}
        
        
        if(meta.isIncludeTable()) {
            // Tables
            String tableNames[] = data.db.getTablenames(data.realSchemaName, meta.isAddSchemaInOut());
           
			String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Table");
			
			for(int i=0; i<tableNames.length && !isStopped();i++) {
		        Object[] outputRowTable = outputRow;
    			
		    	int outputIndex = data.totalpreviousfields;
		    	
				String tableName = tableNames[i];	
				outputRowTable[outputIndex++]=tableName;
	        	
	    		if(!Const.isEmpty(data.realObjectTypeFieldName)) {
	    			outputRowTable[outputIndex++]=ObjectType;
	    		}	    		
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName)) {
	    			outputRowTable[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(tableName));
	    		}
	    		// Get primary key
	    		String pk=null;
	    		String[] pkc=data.db.getPrimaryKeyColumnNames(tableName);
	    		if(pkc!=null && pkc.length==1) {
	    			pk=pkc[0];
	    			pkc=null;
	    		}
	    		// return sql creation
	    		// handle simple primary key (one field)
				String sql = data.db.getCreateTableStatement(tableName, data.db.getTableFields(tableName), null, false, pk, true);

				if(pkc!=null) {
					// add composite primary key (several fields in primary key)
					int IndexOfLastClosedBracket=sql.lastIndexOf(")");
					if(IndexOfLastClosedBracket>-1) {
						sql=sql.substring(0, IndexOfLastClosedBracket);
						sql+=", PRIMARY KEY (";
						for(int k=0; k<pkc.length; k++)
						{
							if(k>0) sql+=", ";
							sql+=pkc[k];
						}
						sql+=")" + Const.CR+")"+ Const.CR+";";
					}
				}
				if(!Const.isEmpty(data.realSQLCreationFieldName)) {
	    			outputRowTable[outputIndex++]=sql;
	    		}
				
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRowTable);  // copy row to output rowset(s);
	
	            if (checkFeedback(getLinesRead()))  {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowTable)));
			}
		}
        
        
		// Views
		if(meta.isIncludeView())
        {
            try{
            	String viewNames[]=data.db.getViews(data.realSchemaName, meta.isAddSchemaInOut());
            	String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.View");
    	        for(int i=0; i<viewNames.length && !isStopped();i++)
    	        {
    	            Object[] outputRowView = outputRow;
    	        	int outputIndex = data.totalpreviousfields;
    	        	
    	        	String viewName=viewNames[i];
    	        	outputRowView[outputIndex++]=viewName;
    	    		
    	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
    	    		{
    	    			outputRowView[outputIndex++]=ObjectType;
    	    		}
    	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
    	    		{
    	    			outputRowView[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(viewName));
    	    		}
    	    		
    				if(!Const.isEmpty(data.realSQLCreationFieldName)) {
    					outputRowView[outputIndex++]=null;
    	    		}
    	    		data.rownr++;
    	    		putRow(data.outputRowMeta, outputRowView);  // copy row to output rowset(s);
    	    		
    	            if (checkFeedback(getLinesRead())) 
    	            {
    	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
    	            }
    	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowView))); //$NON-NLS-1$  
    	        }
            }catch(Exception e){};
        }
		 if(meta.isIncludeProcedure())
         {
	        String procNames[]=data.db.getProcedures();
	        String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Procedure");
	        for(int i=0; i<procNames.length && !isStopped();i++)
	        {
	            Object[] outputRowProc = outputRow;
	        	int outputIndex = data.totalpreviousfields;
	        	
	        	String procName=procNames[i];
	        	outputRowProc[outputIndex++]=procName;

	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
	    		{
	    			outputRowProc[outputIndex++]=ObjectType;
	    		}
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
	    		{
	    			outputRowProc[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(procName));
	    		}
	    		if(!Const.isEmpty(data.realSQLCreationFieldName)) {
	    			outputRowProc[outputIndex++]=null;
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRowProc);  // copy row to output rowset(s);
	    		
	            if (checkFeedback(getLinesRead())) 
	            {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowProc))); //$NON-NLS-1$  
	        }
	     }
        if(meta.isIncludeSynonym())
        {
	        String Synonyms[]=data.db.getSynonyms(data.realSchemaName, meta.isAddSchemaInOut());
	        String ObjectType=BaseMessages.getString(PKG, "GetTableNamesDialog.ObjectType.Synonym");
	        for(int i=0; i<Synonyms.length && !isStopped();i++)
	        {
	            Object[] outputRowSyn = outputRow;
	        	int outputIndex = data.totalpreviousfields;
	        	
	        	String Synonym=Synonyms[i];
	        	
	        	outputRowSyn[outputIndex++]=Synonym;

	    		if(!Const.isEmpty(data.realObjectTypeFieldName))
	    		{
	    			outputRowSyn[outputIndex++]=ObjectType;
	    		}
	    		if(!Const.isEmpty(data.realIsSystemObjectFieldName))
	    		{
	    			outputRowSyn[outputIndex++]=Boolean.valueOf(data.db.isSystemTable(Synonym));
	    		}
	    		if(!Const.isEmpty(data.realSQLCreationFieldName)) {
	    			outputRowSyn[outputIndex++]=null;
	    		}
	    		data.rownr++;
	    		putRow(data.outputRowMeta, outputRowSyn);  // copy row to output rowset(s);
	    		
	            if (checkFeedback(getLinesRead())) 
	            {
	            	if(log.isDetailed()) logDetailed(BaseMessages.getString(PKG, "GetTableNames.LineNumber",""+getLinesRead())); //$NON-NLS-1$
	            }
	            
	            if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "GetTableNames.Log.PutoutRow",data.outputRowMeta.getString(outputRowSyn))); //$NON-NLS-1$   
	        }
	   }

        if(!meta.isDynamicSchema()) {
        	setOutputDone();
        	return false;
        }else {
        	return true;
        }
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
        	String realSchemaName=environmentSubstitute(meta.getSchemaName());
        	if(!Const.isEmpty(realSchemaName)) data.realSchemaName=realSchemaName;
        	data.realTableNameFieldName=environmentSubstitute(meta.getTablenameFieldName());
        	data.realObjectTypeFieldName=environmentSubstitute(meta.getObjectTypeFieldName());
        	data.realIsSystemObjectFieldName=environmentSubstitute(meta.isSystemObjectFieldName());
        	data.realSQLCreationFieldName=environmentSubstitute(meta.getSQLCreationFieldName());
        	if(!meta.isIncludeSchema() && !meta.isIncludeTable() && !meta.isIncludeView() && !meta.isIncludeProcedure() && !meta.isIncludeSynonym())
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
            
            data.db=new Database(this, meta.getDatabase()); 
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

}