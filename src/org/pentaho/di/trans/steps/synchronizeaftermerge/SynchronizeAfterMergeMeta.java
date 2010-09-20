 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.trans.steps.synchronizeaftermerge;


import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/*
 * Created on 13-10-2008
 *
 */
public class SynchronizeAfterMergeMeta extends BaseStepMeta implements StepMetaInterface
{
    /** what's the lookup schema? */
    private String schemaName;
    
    /** what's the lookup table? */
	private String tableName;
	
    /** database connection */
	private DatabaseMeta databaseMeta;
	
    /** which field in input stream to compare with? */
	private String keyStream[];
	
    /** field in table */
	private String keyLookup[];
	
    /** Comparator: =, <>, BETWEEN, ... */
	private String keyCondition[];
	
    /** Extra field for between... */
	private String keyStream2[];
	
    /** Field value to update after lookup */
	private String updateLookup[];
	
    /** Stream name to update value with */
	private String updateStream[];
	
    /** boolean indicating if field needs to be updated */
	private Boolean update[];

	/** Commit size for inserts/updates */
	private int    commitSize;
	
	private boolean tablenameInField;
	
	private String tablenameField;
	
	private String operationOrderField;
	
	private boolean      useBatchUpdate;
	
	private boolean     performLookup;
	
	private String OrderInsert;
	private String OrderUpdate;
	private String OrderDelete;

	public SynchronizeAfterMergeMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
    /**
     * @param useBatchUpdate The useBatchUpdate flag to set.
     */
    public void setUseBatchUpdate(boolean useBatchUpdate)
    {
        this.useBatchUpdate = useBatchUpdate;
    }
    
    /**
     * @return Returns the useBatchUpdate flag.
     */
    public boolean useBatchUpdate()
    {
        return useBatchUpdate;
    }
    
	
    /**
     * @param performLookup The performLookup flag to set.
     */
    public void setPerformLookup(boolean performLookup)
    {
        this.performLookup = performLookup;
    }
    /**
     * @return Returns the performLookup flag.
     */
    public boolean isPerformLookup()
    {
        return performLookup;
    }
    
	public boolean istablenameInField()
	{
		return tablenameInField;
	}
	
	public void settablenameInField(boolean tablenamefield)
	{
		this.tablenameInField=tablenamefield;
	}
	
	
	public String gettablenameField()
	{
		return tablenameField;
	}
	
	public String getOperationOrderField()
	{
		return operationOrderField;
	}
	public String getOrderInsert()
	{
		return OrderInsert;
	}
	public String getOrderUpdate()
	{
		return OrderUpdate;
	}
	public String getOrderDelete()
	{
		return OrderDelete;
	}
	public void setOrderInsert(String insert)
	{
		this.OrderInsert=insert;
	}
	public void setOrderUpdate(String update)
	{
		this.OrderUpdate=update;
	}
	public void setOrderDelete(String delete)
	{
		this.OrderDelete=delete;
	}
	
	public void setOperationOrderField(String operationOrderField)
	{
		this.operationOrderField=operationOrderField;
	}
	
	
	public void settablenameField(String tablenamefield)
	{
		this.tablenameField=tablenamefield;
	}
	
    /**
     * @return Returns the commitSize.
     */
    public int getCommitSize()
    {
        return commitSize;
    }
    
    /**
     * @param commitSize The commitSize to set.
     */
    public void setCommitSize(int commitSize)
    {
        this.commitSize = commitSize;
    }
    
    /**
     * @return Returns the database.
     */
    public DatabaseMeta getDatabaseMeta()
    {
        return databaseMeta;
    }
    
    /**
     * @param database The database to set.
     */
    public void setDatabaseMeta(DatabaseMeta database)
    {
        this.databaseMeta = database;
    }
    
    /**
     * @return Returns the keyCondition.
     */
    public String[] getKeyCondition()
    {
        return keyCondition;
    }
    
    /**
     * @param keyCondition The keyCondition to set.
     */
    public void setKeyCondition(String[] keyCondition)
    {
        this.keyCondition = keyCondition;
    }
    
    /**
     * @return Returns the keyLookup.
     */
    public String[] getKeyLookup()
    {
        return keyLookup;
    }
    
    /**
     * @param keyLookup The keyLookup to set.
     */
    public void setKeyLookup(String[] keyLookup)
    {
        this.keyLookup = keyLookup;
    }
    
    /**
     * @return Returns the keyStream.
     */
    public String[] getKeyStream()
    {
        return keyStream;
    }
    
    /**
     * @param keyStream The keyStream to set.
     */
    public void setKeyStream(String[] keyStream)
    {
        this.keyStream = keyStream;
    }
    
    /**
     * @return Returns the keyStream2.
     */
    public String[] getKeyStream2()
    {
        return keyStream2;
    }
    
    /**
     * @param keyStream2 The keyStream2 to set.
     */
    public void setKeyStream2(String[] keyStream2)
    {
        this.keyStream2 = keyStream2;
    }
    
    /**
     * @return Returns the tableName.
     */
    public String getTableName()
    {
        return tableName;
    }
    
    /**
     * @param tableName The tableName to set.
     */
    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }
    
    /**
     * @return Returns the updateLookup.
     */
    public String[] getUpdateLookup()
    {
        return updateLookup;
    }
    
    /**
     * @param updateLookup The updateLookup to set.
     */
    public void setUpdateLookup(String[] updateLookup)
    {
        this.updateLookup = updateLookup;
    }
    
    /**
     * @return Returns the updateStream.
     */
    public String[] getUpdateStream()
    {
        return updateStream;
    }
    
    /**
     * @param updateStream The updateStream to set.
     */
    public void setUpdateStream(String[] updateStream)
    {
        this.updateStream = updateStream;
    }
    
	public Boolean[] getUpdate() {
		return update;
	}

	public void setUpdate(Boolean[] update) {
		this.update = update;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys, int nrvalues)
	{
		keyStream          = new String[nrkeys];
		keyLookup    = new String[nrkeys];
		keyCondition = new String[nrkeys];
		keyStream2         = new String[nrkeys];
		updateLookup        = new String[nrvalues];
		updateStream    = new String[nrvalues];
		update    = new Boolean[nrvalues];
	}

	public Object clone()
	{
		SynchronizeAfterMergeMeta retval = (SynchronizeAfterMergeMeta)super.clone();
		int nrkeys    = keyStream.length;
		int nrvalues  = updateLookup.length;

		retval.allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			retval.keyStream         [i] = keyStream[i];
			retval.keyLookup   [i] = keyLookup[i];
			retval.keyCondition[i] = keyCondition[i];
			retval.keyStream2        [i] = keyStream2[i];
		}

		for (int i=0;i<nrvalues;i++)
		{
			retval.updateLookup[i]        = updateLookup[i];
			retval.updateStream[i]    = updateStream[i];
			retval.update[i]    = update[i];
		}
		return retval;
	}
	

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			String csize;
			int nrkeys, nrvalues;
			
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			csize      = XMLHandler.getTagValue(stepnode, "commit"); //$NON-NLS-1$
			commitSize=Const.toInt(csize, 0);
            schemaName      = XMLHandler.getTagValue(stepnode, "lookup", "schema"); //$NON-NLS-1$ //$NON-NLS-2$
			tableName      = XMLHandler.getTagValue(stepnode, "lookup", "table"); //$NON-NLS-1$ //$NON-NLS-2$

			useBatchUpdate = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "use_batch")); //$NON-NLS-1$ //$NON-NLS-2$
			performLookup = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "perform_lookup")); //$NON-NLS-1$ //$NON-NLS-2$
			
			tablenameInField = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "tablename_in_field"));
			tablenameField      = XMLHandler.getTagValue(stepnode,"tablename_field");
			operationOrderField      = XMLHandler.getTagValue(stepnode,"operation_order_field");
			OrderInsert      = XMLHandler.getTagValue(stepnode,"order_insert");
			OrderUpdate      = XMLHandler.getTagValue(stepnode,"order_update");
			OrderDelete      = XMLHandler.getTagValue(stepnode,"order_delete");
			
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
			nrkeys    = XMLHandler.countNodes(lookup, "key"); //$NON-NLS-1$
			nrvalues  = XMLHandler.countNodes(lookup, "value"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i); //$NON-NLS-1$
				
				keyStream         [i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				keyLookup   [i] = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
				keyCondition[i] = XMLHandler.getTagValue(knode, "condition"); //$NON-NLS-1$
				if (keyCondition[i]==null) keyCondition[i]="="; //$NON-NLS-1$
				keyStream2        [i] = XMLHandler.getTagValue(knode, "name2"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i); //$NON-NLS-1$
				
				updateLookup[i]        = XMLHandler.getTagValue(vnode, "name"); //$NON-NLS-1$
				updateStream[i]    = XMLHandler.getTagValue(vnode, "rename"); //$NON-NLS-1$
				if (updateStream[i]==null) updateStream[i]=updateLookup[i]; // default: the same name!
				String updateValue = XMLHandler.getTagValue(vnode, "update"); //$NON-NLS-1$
				if(updateValue==null) {
					//default TRUE
					update[i] = Boolean.TRUE;
				} else 
                {
                    if (updateValue.equalsIgnoreCase("Y"))
                        update[i] = Boolean.TRUE;
                    else
                        update[i] = Boolean.FALSE; 
				}
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("SynchronizeAfterMergeMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		tablenameInField=false;
		tablenameField=null;
		keyStream    = null;
		updateLookup = null;
		databaseMeta = null;
		commitSize   = 100;
        schemaName   = ""; //$NON-NLS-1$
		tableName    = Messages.getString("SynchronizeAfterMergeMeta.DefaultTableName"); //$NON-NLS-1$
		operationOrderField=null;
		OrderInsert=null;
		OrderUpdate=null;
		OrderDelete=null;
		performLookup=false;
		
		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			keyLookup[i]   = "age"; //$NON-NLS-1$
			keyCondition[i]= "BETWEEN"; //$NON-NLS-1$
			keyStream[i]         = "age_from"; //$NON-NLS-1$
			keyStream2[i]        = "age_to"; //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			updateLookup[i]=Messages.getString("SynchronizeAfterMergeMeta.ColumnName.ReturnField")+i; //$NON-NLS-1$
			updateStream[i]=Messages.getString("SynchronizeAfterMergeMeta.ColumnName.NewName")+i; //$NON-NLS-1$
			update[i]=Boolean.TRUE; //$NON-NLS-1$
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);
		
		retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("commit", commitSize)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("tablename_in_field", tablenameInField));
		retval.append("    ").append(XMLHandler.addTagValue("tablename_field", tablenameField));
		retval.append("    ").append(XMLHandler.addTagValue("use_batch", useBatchUpdate));
		retval.append("    ").append(XMLHandler.addTagValue("perform_lookup", performLookup));
		
		retval.append("    ").append(XMLHandler.addTagValue("operation_order_field", operationOrderField));
		retval.append("    ").append(XMLHandler.addTagValue("order_insert", OrderInsert));
		retval.append("    ").append(XMLHandler.addTagValue("order_update", OrderUpdate));
		retval.append("    ").append(XMLHandler.addTagValue("order_delete", OrderDelete));
		
		retval.append("    <lookup>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$

		
		for (int i=0;i<keyStream.length;i++)
		{
			retval.append("      <key>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", keyStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field", keyLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("condition", keyCondition[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("name2", keyStream2[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </key>").append(Const.CR); //$NON-NLS-1$
		}

		for (int i=0;i<updateLookup.length;i++)
		{
			retval.append("      <value>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", updateLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("rename", updateStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("update", update[i].booleanValue())); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </value>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("    </lookup>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{

		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase( databases, id_connection);
			
			commitSize     = (int)rep.getStepAttributeInteger(id_step, "commit"); //$NON-NLS-1$
            schemaName     =      rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
			tableName      =      rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$

			tablenameInField =      rep.getStepAttributeBoolean(id_step, "tablename_in_field");
			tablenameField     =      rep.getStepAttributeString(id_step, "tablename_field");
			useBatchUpdate =      rep.getStepAttributeBoolean(id_step, "use_batch");
			performLookup =      rep.getStepAttributeBoolean(id_step, "perform_lookup");
			
			operationOrderField     =      rep.getStepAttributeString(id_step, "operation_order_field");
			OrderInsert     =      rep.getStepAttributeString(id_step, "order_insert");
			OrderUpdate     =      rep.getStepAttributeString(id_step, "order_update");
			OrderDelete     =      rep.getStepAttributeString(id_step, "order_delete");
			
			int nrkeys   = rep.countNrStepAttributes(id_step, "key_name"); //$NON-NLS-1$
			int nrvalues = rep.countNrStepAttributes(id_step, "value_name"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keyStream[i]    = rep.getStepAttributeString(id_step, i, "key_name"); //$NON-NLS-1$
				keyLookup[i]    = rep.getStepAttributeString(id_step, i, "key_field"); //$NON-NLS-1$
				keyCondition[i] = rep.getStepAttributeString(id_step, i, "key_condition"); //$NON-NLS-1$
				keyStream2[i]   = rep.getStepAttributeString(id_step, i, "key_name2"); //$NON-NLS-1$
			}
			
			for (int i=0;i<nrvalues;i++)
			{
				updateLookup[i]  = rep.getStepAttributeString(id_step, i, "value_name"); //$NON-NLS-1$
				updateStream[i]  = rep.getStepAttributeString(id_step, i, "value_rename"); //$NON-NLS-1$
				update[i]        = Boolean.valueOf(rep.getStepAttributeBoolean(id_step, i, "value_update",true)); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection", databaseMeta==null?-1:databaseMeta.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "commit",        commitSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "schema",        schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",         tableName); //$NON-NLS-1$
	
			rep.saveStepAttribute(id_transformation, id_step, "tablename_in_field", tablenameInField);
			rep.saveStepAttribute(id_transformation, id_step, "tablename_field",        tablenameField);
			rep.saveStepAttribute(id_transformation, id_step, "operation_order_field",        operationOrderField);
			rep.saveStepAttribute(id_transformation, id_step, "order_insert",        OrderInsert);
			rep.saveStepAttribute(id_transformation, id_step, "order_update",        OrderUpdate);
			rep.saveStepAttribute(id_transformation, id_step, "order_delete",        OrderDelete);
			
			rep.saveStepAttribute(id_transformation, id_step, "use_batch", useBatchUpdate);
			rep.saveStepAttribute(id_transformation, id_step, "perform_lookup", performLookup);
			
			
			for (int i=0;i<keyStream.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name",      keyStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_field",     keyLookup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_condition", keyCondition[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name2",     keyStream2[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<updateLookup.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "value_name",    updateLookup[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "value_rename",  updateStream[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "value_update",  update[i].booleanValue()); //$NON-NLS-1$
			}
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();
				
				if (!Const.isEmpty(tableName))
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.TableNameOK"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
			
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					// Check fields in table
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
                    RowMetaInterface r = db.getTableFields(schemaTable);
					if (r!=null)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.TableExists"), stepMeta); //$NON-NLS-1$
						remarks.add(cr);
			
						for (int i=0;i<keyLookup.length;i++)
						{
							String lufield = keyLookup[i];
							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingCompareFieldsInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.AllLookupFieldsFound"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
						
						// How about the fields to insert/update in the table?
						first=true;
						error_found=false;
						error_message = ""; //$NON-NLS-1$

						for (int i=0;i<updateLookup.length;i++)
						{
							String lufield = updateLookup[i];
							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingFieldsToUpdateInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.AllFieldsToUpdateFoundInTargetTable"), stepMeta); //$NON-NLS-1$
						}
						remarks.add(cr);
					}
					else
					{
						error_message=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
					remarks.add(cr);
			
					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;
					
					for (int i=0;i<keyStream.length;i++)
					{
						ValueMetaInterface v = prev.searchValueMeta(keyStream[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+keyStream[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					for (int i=0;i<keyStream2.length;i++)
					{
						if (keyStream2[i]!=null && keyStream2[i].length()>0)
						{
							ValueMetaInterface v = prev.searchValueMeta(keyStream2[i]);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+keyStream[i]+Const.CR;  //$NON-NLS-1$
							}
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.AllFieldsFoundInInput"), stepMeta); //$NON-NLS-1$
					}
					remarks.add(cr);

					// How about the fields to insert/update the table with?
					first=true;
					error_found=false;
					error_message = ""; //$NON-NLS-1$


					for (int i=0;i<updateStream.length;i++)
					{
						String lufield = updateStream[i];
						ValueMetaInterface v = prev.searchValueMeta(lufield);

						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingInputStreamFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
						}	
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.AllFieldsFoundInInput2"), stepMeta); //$NON-NLS-1$
						remarks.add(cr);
					}
				
					//--------------------------> check fields in stream and tables (type)
					// Check fields in table
					
					String errorMsgDiffField="";
					boolean errorDiffLenField=false;
					String errorMsgDiffLenField="";
					boolean errorDiffField=false;
					
					RowMetaInterface r = db.getTableFields(databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName));
					if(r!=null)
					{
						for (int i=0;i<updateStream.length;i++)
						{
							String lufieldstream = updateStream[i];
							String lufieldtable=updateLookup[i];
							// get value from previous
							ValueMetaInterface vs = prev.searchValueMeta(lufieldstream);
							// get value from table fields
							ValueMetaInterface vt = r.searchValueMeta(lufieldtable);
							if(vs!=null && vt!=null)
							{
								if(!vs.getTypeDesc().equalsIgnoreCase(vt.getTypeDesc()))
								{
									errorMsgDiffField+=Const.CR+ "The input field [" + vs.getName() + "] ( Type=" 
									+ vs.getTypeDesc()
									+") is not the same as the type in the target table (Type="+vt.getTypeDesc()+")"+Const.CR;
									errorDiffField=true;
								}else
								{
									// check Length
									if((vt.getLength()<vs.getLength() || vs.getLength()==-1) && vt.getLength()!=-1)
									{
										errorMsgDiffLenField+=Const.CR+ "The input field [" + vs.getName() + "] " 
										+ "(" + vs.getTypeDesc()+ ")" + 
										" has a length ("+vs.getLength() +")"+
										" that is higher than that in the target table ("+vt.getLength()+")."+Const.CR;
										errorDiffLenField=true;
									}
								}
							}
						}
						// add error/Warning
						if (errorDiffField) 
						{
							errorMsgDiffField=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.FieldsTypeDifferent")+Const.CR+errorMsgDiffField;
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, errorMsgDiffField, stepMeta);
						}
						if (errorDiffLenField) 
						{
							errorMsgDiffLenField=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.FieldsLenDifferent")+Const.CR+errorMsgDiffLenField;
							cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, errorMsgDiffLenField, stepMeta);
						}
						remarks.add(cr);
					}
					//--------------------------> check fields in stream and tables (type)
				}
				else
				{
					error_message=Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingFieldsInInput3")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = Messages.getString("SynchronizeAfterMergeMeta.CheckResult.DatabaseErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = Messages.getString("SynchronizeAfterMergeMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SynchronizeAfterMergeMeta.CheckResult.NoInputError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev) throws KettleStepException
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!
	
		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
                // Copy the row
				RowMetaInterface tableFields = new RowMeta();
                
                // Now change the field names
                // the key fields
                if (keyLookup!=null) {
                	for (int i = 0; i < keyLookup.length; i++) {
						ValueMetaInterface v = prev.searchValueMeta(keyStream[i]);
						if (v != null) {
							ValueMetaInterface tableField = v.clone();
							tableField.setName(keyLookup[i]);
							tableFields.addValueMeta(tableField);
						} else {
							throw new KettleStepException("Unable to find field ["+keyStream[i]+ "] in the input rows");
						}
					}
                }
                // the lookup fields
                for (int i=0;i<updateLookup.length;i++)
                {
                	ValueMetaInterface v = prev.searchValueMeta(updateStream[i]);
                	 if (v!=null)
                     {
                     	ValueMetaInterface vk=tableFields.searchValueMeta(updateStream[i]);
                     	if (vk==null) { // do not add again when already added as key fields
                     		ValueMetaInterface tableField = v.clone();
                     		tableField.setName(updateLookup[i]);
                     		tableFields.addValueMeta(tableField);
                     	}
                    }
                    else
                    {
                        throw new KettleStepException("Unable to find field ["+updateStream[i]+"] in the input rows");
                    }
                }
                
				if (!Const.isEmpty(tableName))
				{
                    Database db = new Database(databaseMeta);
                    db.shareVariablesWith(transMeta);
					try
					{
						db.connect();
						
                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
						String cr_table = db.getDDL(schemaTable, 
													tableFields, 
													null, 
													false, 
													null,
													true
													);
						
						String cr_index = ""; //$NON-NLS-1$
						String idx_fields[] = null;
						
						if (keyLookup!=null && keyLookup.length>0)
						{
							idx_fields = new String[keyLookup.length];
							for (int i=0;i<keyLookup.length;i++) idx_fields[i] = keyLookup[i];
						}
						else
						{
							retval.setError(Messages.getString("SynchronizeAfterMergeMeta.CheckResult.MissingKeyFields")); //$NON-NLS-1$
						}

						// Key lookup dimensions...
						if (idx_fields!=null && idx_fields.length>0 &&  !db.checkIndexExists(schemaName, tableName, idx_fields)
						   )
						{
							String indexname = "idx_"+tableName+"_lookup"; //$NON-NLS-1$ //$NON-NLS-2$
							cr_index = db.getCreateIndexStatement(schemaTable, indexname, idx_fields, false, false, false, true);
						}
						
						String sql = cr_table+cr_index;
						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError(Messages.getString("SynchronizeAfterMergeMeta.ReturnValue.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(Messages.getString("SynchronizeAfterMergeMeta.ReturnValue.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(Messages.getString("SynchronizeAfterMergeMeta.ReturnValue.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(Messages.getString("SynchronizeAfterMergeMeta.ReturnValue.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}
	
	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info) throws KettleStepException
    {
        if (prev != null)
        {
            // Lookup: we do a lookup on the natural keys 
            for (int i = 0; i < keyLookup.length; i++)
            {
            	ValueMetaInterface v = prev.searchValueMeta(keyStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, keyLookup[i], keyStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }

            // Insert update fields : read/write
            for (int i = 0; i < updateLookup.length; i++)
            {
            	ValueMetaInterface v = prev.searchValueMeta(updateStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, updateLookup[i], updateStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }
        }
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SynchronizeAfterMerge(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SynchronizeAfterMergeData();
	}

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (databaseMeta!=null) 
        {
            return new DatabaseMeta[] { databaseMeta };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }
    
    public RowMetaInterface getRequiredFields(VariableSpace space) throws KettleException
    {
		String realTableName = space.environmentSubstitute(tableName);
		String realSchemaName = space.environmentSubstitute(schemaName);

        if (databaseMeta!=null)
        {
            Database db = new Database(databaseMeta);
            db.shareVariablesWith(space);
            try
            {
                db.connect();
                
                if (!Const.isEmpty(realTableName))
                {
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(realSchemaName, realTableName);

                    // Check if this table exists...
                    if (db.checkTableExists(schemaTable))
                    {
                        return db.getTableFields(schemaTable);
                    }
                    else
                    {
                        throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.TableNotFound"));
                    }
                }
                else
                {
                    throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.TableNotSpecified"));
                }
            }
            catch(Exception e)
            {
                throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.ErrorGettingFields"), e);
            }
            finally
            {
                db.disconnect();
            }
        }
        else
        {
            throw new KettleException(Messages.getString("SynchronizeAfterMergeMeta.Exception.ConnectionNotDefined"));
        }

    }

    /**
     * @return the schemaName
     */
    public String getSchemaName()
    {
        return schemaName;
    }

    /**
     * @param schemaName the schemaName to set
     */
    public void setSchemaName(String schemaName)
    {
        this.schemaName = schemaName;
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }

}