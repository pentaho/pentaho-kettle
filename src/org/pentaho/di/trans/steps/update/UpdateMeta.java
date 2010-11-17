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

package org.pentaho.di.trans.steps.update;

import java.util.List;
import java.util.Map;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.DatabaseImpact;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/*
 * Created on 26-apr-2003
 *
 */

public class UpdateMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = UpdateMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The lookup table name */
    private String schemaName;

    /** The lookup table name */
	private String tableName;
	
	/** database connection */
	private DatabaseMeta databaseMeta;
	
	/** which field in input stream to compare with? */
	private String keyStream[];
	
	/** field in table  */
	private String keyLookup[];
	
	/** Comparator: =, <>, BETWEEN, ... */
	private String keyCondition[];
	
	/** Extra field for between... */
	private String keyStream2[];
	
	/** Field value to update after lookup */
	private String updateLookup[];
	
	/** Stream name to update value with */
	private String updateStream[];
	
	/** Commit size for inserts/updates */
	private int    commitSize; 
    
    /** update errors are ignored if this flag is set to true */
    private boolean errorIgnored;
    
    /** adds a boolean field to the output indicating success of the update */
    private String  ignoreFlagField;
    
    /** adds a boolean field to skip lookup and directly update selected fields */
    private boolean  skipLookup;
	
    /** Flag to indicate the use of batch updates, enabled by default but disabled for backward compatibility */
    private boolean      useBatchUpdate;

	public UpdateMeta()
	{
		super(); // allocate BaseStepMeta
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
     * @return Returns the skipLookup.
     */
    public boolean isSkipLookup()
    {
        return skipLookup;
    }

    /**
     * @param skipLookup The skipLookup to set.
     */
    public void setSkipLookup(boolean skipLookup)
    {
        this.skipLookup = skipLookup;
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
    
    /**
     * @return Returns the ignoreError.
     */
    public boolean isErrorIgnored()
    {
        return errorIgnored;
    }

    /**
     * @param ignoreError The ignoreError to set.
     */
    public void setErrorIgnored(boolean ignoreError)
    {
        this.errorIgnored = ignoreError;
    }

    /**
     * @return Returns the ignoreFlagField.
     */
    public String getIgnoreFlagField()
    {
        return ignoreFlagField;
    }



    /**
     * @param ignoreFlagField The ignoreFlagField to set.
     */
    public void setIgnoreFlagField(String ignoreFlagField)
    {
        this.ignoreFlagField = ignoreFlagField;
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
	}

	public Object clone()
	{
		UpdateMeta retval = (UpdateMeta)super.clone();
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
            useBatchUpdate = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_batch")); 
			skipLookup = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "skip_lookup")); 
            errorIgnored = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "error_ignored")); //$NON-NLS-1$ //$NON-NLS-2$
            ignoreFlagField = XMLHandler.getTagValue(stepnode, "ignore_flag_field"); //$NON-NLS-1$
            schemaName     = XMLHandler.getTagValue(stepnode, "lookup", "schema"); //$NON-NLS-1$ //$NON-NLS-2$
			tableName      = XMLHandler.getTagValue(stepnode, "lookup", "table"); //$NON-NLS-1$ //$NON-NLS-2$
	
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
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "UpdateMeta.Exception.UnableToReadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		skipLookup=false;
		keyStream    = null;
		updateLookup = null;
		databaseMeta = null;
		commitSize   = 100;
        schemaName   = ""; //$NON-NLS-1$
		tableName    = BaseMessages.getString(PKG, "UpdateMeta.DefaultTableName"); //$NON-NLS-1$

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			keyLookup[i]    = "age"; //$NON-NLS-1$
			keyCondition[i] = "BETWEEN"; //$NON-NLS-1$
			keyStream[i]    = "age_from"; //$NON-NLS-1$
			keyStream2[i]   = "age_to"; //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			updateLookup[i]=BaseMessages.getString(PKG, "UpdateMeta.ColumnName.ReturnField")+i; //$NON-NLS-1$
			updateStream[i]=BaseMessages.getString(PKG, "UpdateMeta.ColumnName.NewName")+i; //$NON-NLS-1$
		}
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    "+XMLHandler.addTagValue("skip_lookup", skipLookup));
		retval.append("    "+XMLHandler.addTagValue("commit", commitSize)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("use_batch",      useBatchUpdate));
        retval.append("    "+XMLHandler.addTagValue("error_ignored", errorIgnored)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("ignore_flag_field", ignoreFlagField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    <lookup>"+Const.CR); //$NON-NLS-1$
        retval.append("      "+XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$

		for (int i=0;i<keyStream.length;i++)
		{
			retval.append("      <key>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name", keyStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("field", keyLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("condition", keyCondition[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("name2", keyStream2[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </key>"+Const.CR); //$NON-NLS-1$
		}

		for (int i=0;i<updateLookup.length;i++)
		{
			retval.append("      <value>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name", updateLookup[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("rename", updateStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </value>"+Const.CR); //$NON-NLS-1$
		}

		retval.append("      </lookup>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);  //$NON-NLS-1$
			skipLookup =     rep.getStepAttributeBoolean (id_step, "skip_lookup");
			commitSize     = (int)rep.getStepAttributeInteger(id_step, "commit"); //$NON-NLS-1$
            useBatchUpdate   =      rep.getStepAttributeBoolean(id_step, "use_batch"); 
            schemaName     =      rep.getStepAttributeString(id_step, "schema"); //$NON-NLS-1$
			tableName      =      rep.getStepAttributeString(id_step, "table"); //$NON-NLS-1$
            
            errorIgnored    =     rep.getStepAttributeBoolean(id_step, "error_ignored"); //$NON-NLS-1$
            ignoreFlagField =     rep.getStepAttributeString (id_step, "ignore_flag_field"); //$NON-NLS-1$
	
			int nrkeys   = rep.countNrStepAttributes(id_step, "key_name"); //$NON-NLS-1$
			int nrvalues = rep.countNrStepAttributes(id_step, "value_name"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keyStream[i]          = rep.getStepAttributeString(id_step, i, "key_name"); //$NON-NLS-1$
				keyLookup[i]    = rep.getStepAttributeString(id_step, i, "key_field"); //$NON-NLS-1$
				keyCondition[i] = rep.getStepAttributeString(id_step, i, "key_condition"); //$NON-NLS-1$
				keyStream2[i]         = rep.getStepAttributeString(id_step, i, "key_name2"); //$NON-NLS-1$
			}
			
			for (int i=0;i<nrvalues;i++)
			{
				updateLookup[i]        = rep.getStepAttributeString(id_step, i, "value_name"); //$NON-NLS-1$
				updateStream[i]    = rep.getStepAttributeString(id_step, i, "value_rename"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UpdateMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "skip_lookup",    skipLookup);
			rep.saveStepAttribute(id_transformation, id_step, "commit",        commitSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "use_batch",       useBatchUpdate);
            rep.saveStepAttribute(id_transformation, id_step, "schema",        schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "table",         tableName); //$NON-NLS-1$

            rep.saveStepAttribute(id_transformation, id_step, "error_ignored",        errorIgnored); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "ignore_flag_field",    ignoreFlagField); //$NON-NLS-1$

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
			}
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UpdateMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
    
    public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        if (ignoreFlagField!=null && ignoreFlagField.length()>0)
        {
            ValueMetaInterface v = new ValueMeta(ignoreFlagField, ValueMetaInterface.TYPE_BOOLEAN);
            v.setOrigin(name);
            
            row.addValueMeta( v );
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		if (databaseMeta!=null)
		{
			Database db = new Database(loggingObject, databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();
				
				if (!Const.isEmpty(tableName))
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.TableNameOK"), stepinfo); //$NON-NLS-1$
					remarks.add(cr);
			
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					// Check fields in table
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
					RowMetaInterface r = db.getTableFields( schemaTable );
					if (r!=null)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.TableExists"), stepinfo); //$NON-NLS-1$
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
									error_message+=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingCompareFieldsInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.AllLookupFieldsFound"), stepinfo); //$NON-NLS-1$
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
									error_message+=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingFieldsToUpdateInTargetTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.AllFieldsToUpdateFoundInTargetTable"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);
					}
					else
					{
						error_message=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.StepReceivingDatas",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
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
								error_message+=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingFieldsInInput")+Const.CR; //$NON-NLS-1$
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
									error_message+=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingFieldsInInput2")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+keyStream[i]+Const.CR;  //$NON-NLS-1$
							}
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.AllFieldsFoundInInput"), stepinfo); //$NON-NLS-1$
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
								error_message+=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingInputStreamFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.AllFieldsFoundInInput2"), stepinfo); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingFieldsInInput3")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = BaseMessages.getString(PKG, "UpdateMeta.CheckResult.DatabaseErrorOccurred")+e.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = BaseMessages.getString(PKG, "UpdateMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "UpdateMeta.CheckResult.NoInputError"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!
	
		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (!Const.isEmpty(tableName))
				{
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);

					Database db = new Database(loggingObject, databaseMeta);
					db.shareVariablesWith(transMeta);
					try
					{
						db.connect();
						
						if ( getIgnoreFlagField()!=null && 
							 getIgnoreFlagField().length()>0)
						{
						    prev.addValueMeta(new ValueMeta(getIgnoreFlagField(), ValueMetaInterface.TYPE_BOOLEAN));
						}
						
						String cr_table = db.getDDL(schemaTable, 
													prev, 
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
							retval.setError(BaseMessages.getString(PKG, "UpdateMeta.CheckResult.MissingKeyFields")); //$NON-NLS-1$
						}

						// Key lookup dimensions...
						if (idx_fields!=null && idx_fields.length>0 && !db.checkIndexExists(schemaName, tableName, idx_fields)
						   )
						{
							String indexname = "idx_"+tableName+"_lookup"; //$NON-NLS-1$ //$NON-NLS-2$
							cr_index = db.getCreateIndexStatement(schemaName, tableName, indexname, idx_fields, false, false, false, true);
						}
						
						String sql = cr_table+cr_index;
						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError(BaseMessages.getString(PKG, "UpdateMeta.ReturnValue.ErrorOccurred")+e.getMessage()); //$NON-NLS-1$
					}
				}
				else
				{
					retval.setError(BaseMessages.getString(PKG, "UpdateMeta.ReturnValue.NoTableDefinedOnConnection")); //$NON-NLS-1$
				}
			}
			else
			{
				retval.setError(BaseMessages.getString(PKG, "UpdateMeta.ReturnValue.NotReceivingAnyFields")); //$NON-NLS-1$
			}
		}
		else
		{
			retval.setError(BaseMessages.getString(PKG, "UpdateMeta.ReturnValue.NoConnectionDefined")); //$NON-NLS-1$
		}

		return retval;
	}
	
	@Override
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

            // Update fields : read/write
            for (int i = 0; i < updateLookup.length; i++)
            {
                ValueMetaInterface v = prev.searchValueMeta(updateStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_UPDATE, transMeta.getName(), stepMeta.getName(), databaseMeta
                        .getDatabaseName(), tableName, updateLookup[i], updateStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                impact.add(ii);
            }
        }
    }

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Update(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new UpdateData();
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

    /**
     * @return the useBatchUpdate
     */
    public boolean useBatchUpdate() {
      return useBatchUpdate;
    }

    /**
     * @param useBatchUpdate the useBatchUpdate to set
     */
    public void setUseBatchUpdate(boolean useBatchUpdate) {
      this.useBatchUpdate = useBatchUpdate;
    }
    
    
}