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
 
/*
 * Created on 26-apr-2003
 *
 */
package org.pentaho.di.trans.steps.databaselookup;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
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
import org.w3c.dom.Node;

public class DatabaseLookupMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = DatabaseLookupMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String[] conditionStrings = new String[] { "=", "<>", "<", "<=", ">", ">=", "LIKE", "BETWEEN", "IS NULL", "IS NOT NULL", };  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$
	
	public static final int CONDITION_EQ          = 0;
	public static final int CONDITION_NE          = 1;
	public static final int CONDITION_LT          = 2;
	public static final int CONDITION_LE          = 3;
	public static final int CONDITION_GT          = 4;
	public static final int CONDITION_GE          = 5;
	public static final int CONDITION_LIKE        = 6;
	public static final int CONDITION_BETWEEN     = 7;
	public static final int CONDITION_IS_NULL     = 8;
	public static final int CONDITION_IS_NOT_NULL = 9;
	
    /** what's the lookup schema name? */
    private String schemaName;
    
	/** what's the lookup table? */
	private String tablename;            
	
	/** database connection */
	private DatabaseMeta databaseMeta;   
	
	/** which field in input stream to compare with? */
	private String streamKeyField1[];

	/** Extra field for between... */
	private String streamKeyField2[];           

	/** Comparator: =, <>, BETWEEN, ... */
	private String keyCondition[];   
	
	/** field in table */
	private String tableKeyField[];      
	
	
	/** return these field values after lookup */
	private String returnValueField[];          
	
	/** new name for value ... */
	private String returnValueNewName[];      
	
	/** default value in case not found... */
	private String returnValueDefault[];    
	
	/** type of default value  */
	private int    returnValueDefaultType[];   
	
	/** order by clause... */
	private String orderByClause;          
	
	/** Cache values we look up --> faster */
	private boolean cached;           
	
	/** Limit the cache size to this! */
	private int     cacheSize;      
	
	/** Flag to make it load all data into the cache at startup */
	private boolean loadingAllDataInCache;
    
    /** Have the lookup fail if multiple results were found, renders the orderByClause useless */
    private boolean failingOnMultipleResults;
	
    /** Have the lookup eat the incoming row when nothing gets found */
    private boolean eatingRowOnLookupFailure;
    
	public DatabaseLookupMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
	 * @return Returns the cached.
	 */
	public boolean isCached()
	{
		return cached;
	}
	
	/**
	 * @param cached The cached to set.
	 */
	public void setCached(boolean cached)
	{
		this.cached = cached;
	}
	
	/**
	 * @return Returns the cacheSize.
	 */
	public int getCacheSize()
	{
		return cacheSize;
	}
	
	/**
	 * @param cacheSize The cacheSize to set.
	 */
	public void setCacheSize(int cacheSize)
	{
		this.cacheSize = cacheSize;
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
	 * @return Returns the orderByClause.
	 */
	public String getOrderByClause()
	{
		return orderByClause;
	}
	
	/**
	 * @param orderByClause The orderByClause to set.
	 */
	public void setOrderByClause(String orderByClause)
	{
		this.orderByClause = orderByClause;
	}
	
	/**
	 * @return Returns the returnValueDefault.
	 */
	public String[] getReturnValueDefault()
	{
		return returnValueDefault;
	}
	
	/**
	 * @param returnValueDefault The returnValueDefault to set.
	 */
	public void setReturnValueDefault(String[] returnValueDefault)
	{
		this.returnValueDefault = returnValueDefault;
	}
	
	/**
	 * @return Returns the returnValueDefaultType.
	 */
	public int[] getReturnValueDefaultType()
	{
		return returnValueDefaultType;
	}
	
	/**
	 * @param returnValueDefaultType The returnValueDefaultType to set.
	 */
	public void setReturnValueDefaultType(int[] returnValueDefaultType)
	{
		this.returnValueDefaultType = returnValueDefaultType;
	}
	
	/**
	 * @return Returns the returnValueField.
	 */
	public String[] getReturnValueField()
	{
		return returnValueField;
	}
	
	/**
	 * @param returnValueField The returnValueField to set.
	 */
	public void setReturnValueField(String[] returnValueField)
	{
		this.returnValueField = returnValueField;
	}
	
	/**
	 * @return Returns the returnValueNewName.
	 */
	public String[] getReturnValueNewName()
	{
		return returnValueNewName;
	}
	
	/**
	 * @param returnValueNewName The returnValueNewName to set.
	 */
	public void setReturnValueNewName(String[] returnValueNewName)
	{
		this.returnValueNewName = returnValueNewName;
	}
	
	/**
	 * @return Returns the streamKeyField1.
	 */
	public String[] getStreamKeyField1()
	{
		return streamKeyField1;
	}
	
	/**
	 * @param streamKeyField1 The streamKeyField1 to set.
	 */
	public void setStreamKeyField1(String[] streamKeyField1)
	{
		this.streamKeyField1 = streamKeyField1;
	}
	
	/**
	 * @return Returns the streamKeyField2.
	 */
	public String[] getStreamKeyField2()
	{
		return streamKeyField2;
	}
	
	/**
	 * @param streamKeyField2 The streamKeyField2 to set.
	 */
	public void setStreamKeyField2(String[] streamKeyField2)
	{
		this.streamKeyField2 = streamKeyField2;
	}
	
	/**
	 * @return Returns the tableKeyField.
	 */
	public String[] getTableKeyField()
	{
		return tableKeyField;
	}
	
	/**
	 * @param tableKeyField The tableKeyField to set.
	 */
	public void setTableKeyField(String[] tableKeyField)
	{
		this.tableKeyField = tableKeyField;
	}
	
	/**
	 * @return Returns the tablename.
	 */
	public String getTablename()
	{
		return tablename;
	}
	
	/**
	 * @param tablename The tablename to set.
	 */
	public void setTablename(String tablename)
	{
		this.tablename = tablename;
	}
	
    /**
     * @return Returns the failOnMultipleResults.
     */
    public boolean isFailingOnMultipleResults()
    {
        return failingOnMultipleResults;
    }

    /**
     * @param failOnMultipleResults The failOnMultipleResults to set.
     */
    public void setFailingOnMultipleResults(boolean failOnMultipleResults)
    {
        this.failingOnMultipleResults = failOnMultipleResults;
    }
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		streamKeyField1=null;
		returnValueField=null;
		
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys, int nrvalues)
	{
		streamKeyField1        = new String[nrkeys];
		tableKeyField          = new String[nrkeys];
		keyCondition           = new String[nrkeys];
		streamKeyField2        = new String[nrkeys];
		returnValueField       = new String[nrvalues];
		returnValueNewName     = new String[nrvalues];
		returnValueDefault     = new String[nrvalues];
		returnValueDefaultType = new int[nrvalues];
	}

	public Object clone()
	{
		DatabaseLookupMeta retval = (DatabaseLookupMeta)super.clone();
		
		int nrkeys   = streamKeyField1.length;
		int nrvalues = returnValueField.length;

		retval.allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			retval.streamKeyField1[i] = streamKeyField1[i];
			retval.tableKeyField[i]   = tableKeyField[i];
			retval.keyCondition[i]    = keyCondition[i];
			retval.streamKeyField2[i] = streamKeyField2[i];
		}

		for (int i=0;i<nrvalues;i++)
		{
			retval.returnValueField[i]       = returnValueField[i];
			retval.returnValueNewName[i]     = returnValueNewName[i];
			retval.returnValueDefault[i]     = returnValueDefault[i];
			retval.returnValueDefaultType[i] = returnValueDefaultType[i];
		}		

		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			String dtype;
			String csize;
			
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			cached      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "cache")); //$NON-NLS-1$ //$NON-NLS-2$
			loadingAllDataInCache = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "cache_load_all")); //$NON-NLS-1$ //$NON-NLS-2$
			csize      = XMLHandler.getTagValue(stepnode, "cache_size"); //$NON-NLS-1$
			cacheSize=Const.toInt(csize, 0);
            schemaName = XMLHandler.getTagValue(stepnode, "lookup", "schema"); //$NON-NLS-1$ //$NON-NLS-2$
			tablename = XMLHandler.getTagValue(stepnode, "lookup", "table"); //$NON-NLS-1$ //$NON-NLS-2$
	
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup"); //$NON-NLS-1$
			
			int nrkeys   = XMLHandler.countNodes(lookup, "key"); //$NON-NLS-1$
			int nrvalues = XMLHandler.countNodes(lookup, "value"); //$NON-NLS-1$
	
			allocate(nrkeys, nrvalues);
					
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i); //$NON-NLS-1$
				
				streamKeyField1         [i] = XMLHandler.getTagValue(knode, "name"); //$NON-NLS-1$
				tableKeyField   [i] = XMLHandler.getTagValue(knode, "field"); //$NON-NLS-1$
				keyCondition[i] = XMLHandler.getTagValue(knode, "condition"); //$NON-NLS-1$
				if (keyCondition[i]==null) keyCondition[i]="="; //$NON-NLS-1$
				streamKeyField2        [i] = XMLHandler.getTagValue(knode, "name2"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i); //$NON-NLS-1$
				
				returnValueField[i]        = XMLHandler.getTagValue(vnode, "name"); //$NON-NLS-1$
				returnValueNewName[i]    = XMLHandler.getTagValue(vnode, "rename"); //$NON-NLS-1$
				if (returnValueNewName[i]==null) returnValueNewName[i]=returnValueField[i]; // default: the same name!
				returnValueDefault[i]     = XMLHandler.getTagValue(vnode, "default"); //$NON-NLS-1$
				dtype           = XMLHandler.getTagValue(vnode, "type"); //$NON-NLS-1$
				returnValueDefaultType[i] = ValueMeta.getType(dtype);
				if (returnValueDefaultType[i]<0)
				{
					//logError("unknown default value type: "+dtype+" for value "+value[i]+", default to type: String!");
					returnValueDefaultType[i]=ValueMetaInterface.TYPE_STRING;
				}
			}		
			orderByClause = XMLHandler.getTagValue(lookup, "orderby"); //Optional, can by null //$NON-NLS-1$
            failingOnMultipleResults = "Y".equalsIgnoreCase(XMLHandler.getTagValue(lookup, "fail_on_multiple"));  //$NON-NLS-1$ //$NON-NLS-2$
            eatingRowOnLookupFailure = "Y".equalsIgnoreCase(XMLHandler.getTagValue(lookup, "eat_row_on_failure"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "DatabaseLookupMeta.ERROR0001.UnableToLoadStepFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		streamKeyField1	 = null;
		returnValueField = null;
		databaseMeta     = null;
		cached           = false;
		cacheSize        = 0;
        schemaName       = ""; //$NON-NLS-1$
		tablename        = BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.TableName"); //$NON-NLS-1$

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			tableKeyField[i]   = BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.KeyFieldPrefix"); //$NON-NLS-1$
			keyCondition[i]    = BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.KeyCondition"); //$NON-NLS-1$
			streamKeyField1[i] = BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.KeyStreamField1"); //$NON-NLS-1$
			streamKeyField2[i] = BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.KeyStreamField2"); //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			returnValueField[i]=BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.ReturnFieldPrefix")+i; //$NON-NLS-1$
			returnValueNewName[i]=BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.ReturnNewNamePrefix")+i; //$NON-NLS-1$
			returnValueDefault[i]=BaseMessages.getString(PKG, "DatabaseLookupMeta.Default.ReturnDefaultValuePrefix")+i; //$NON-NLS-1$
			returnValueDefaultType[i]=ValueMetaInterface.TYPE_STRING;
		}
		
		orderByClause = ""; //$NON-NLS-1$
        failingOnMultipleResults = false;
        eatingRowOnLookupFailure = false;
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		if (Const.isEmpty(info) || info[0]==null) // null or length 0 : no info from database
		{
			for (int i=0;i<returnValueNewName.length;i++)
			{
				ValueMetaInterface v=new ValueMeta(returnValueNewName[i], returnValueDefaultType[i]);
				v.setOrigin(name);
				row.addValueMeta(v);
			}
		}
		else
		{
			for (int i=0;i<returnValueNewName.length;i++)
			{
				ValueMetaInterface v=info[0].searchValueMeta(returnValueField[i]);
				if (v!=null)
				{
					v.setName(returnValueNewName[i]);
					v.setOrigin(name);
					row.addValueMeta(v);
				}
			}
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer(500);
		
		retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("cache", cached)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("cache_load_all", loadingAllDataInCache)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    <lookup>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("table", tablename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      ").append(XMLHandler.addTagValue("orderby", orderByClause)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("fail_on_multiple", failingOnMultipleResults)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("eat_row_on_failure", eatingRowOnLookupFailure)); //$NON-NLS-1$ //$NON-NLS-2$
        
		for (int i=0;i<streamKeyField1.length;i++)
		{
			retval.append("      <key>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", streamKeyField1[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("field", tableKeyField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("condition", keyCondition[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("name2", streamKeyField2[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </key>").append(Const.CR); //$NON-NLS-1$
		}

		for (int i=0;i<returnValueField.length;i++)
		{
			retval.append("      <value>").append(Const.CR); //$NON-NLS-1$
			retval.append("        ").append(XMLHandler.addTagValue("name", returnValueField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("rename", returnValueNewName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("default", returnValueDefault[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        ").append(XMLHandler.addTagValue("type", ValueMeta.getTypeDesc(returnValueDefaultType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("      </value>").append(Const.CR); //$NON-NLS-1$
		}

		retval.append("    </lookup>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);

			cached                   =      rep.getStepAttributeBoolean(id_step, "cache"); //$NON-NLS-1$
			loadingAllDataInCache    =      rep.getStepAttributeBoolean(id_step, "cache_load_all"); //$NON-NLS-1$
			cacheSize                = (int)rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
            schemaName               =      rep.getStepAttributeString (id_step, "lookup_schema");  //$NON-NLS-1$
			tablename                =      rep.getStepAttributeString (id_step, "lookup_table");  //$NON-NLS-1$
			orderByClause            =      rep.getStepAttributeString (id_step, "lookup_orderby");  //$NON-NLS-1$
            failingOnMultipleResults =      rep.getStepAttributeBoolean(id_step, "fail_on_multiple");  //$NON-NLS-1$
            eatingRowOnLookupFailure =      rep.getStepAttributeBoolean(id_step, "eat_row_on_failure");  //$NON-NLS-1$
            
			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name"); //$NON-NLS-1$
			int nrvalues = rep.countNrStepAttributes(id_step, "return_value_name"); //$NON-NLS-1$
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				streamKeyField1[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name"); //$NON-NLS-1$
				tableKeyField[i]   = rep.getStepAttributeString(id_step, i, "lookup_key_field"); //$NON-NLS-1$
				keyCondition[i]    = rep.getStepAttributeString(id_step, i, "lookup_key_condition"); //$NON-NLS-1$
				streamKeyField2[i] = rep.getStepAttributeString(id_step, i, "lookup_key_name2"); //$NON-NLS-1$
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				returnValueField[i]       = rep.getStepAttributeString (id_step, i, "return_value_name"); //$NON-NLS-1$
				returnValueNewName[i]     = rep.getStepAttributeString (id_step, i, "return_value_rename"); //$NON-NLS-1$
				returnValueDefault[i]     = rep.getStepAttributeString (id_step, i, "return_value_default"); //$NON-NLS-1$
				returnValueDefaultType[i] = ValueMeta.getType( rep.getStepAttributeString (id_step, i, "return_value_type") ); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "DatabaseLookupMeta.ERROR0002.UnexpectedErrorReadingFromTheRepository"), e); //$NON-NLS-1$
		}
	}
	

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "cache",              cached); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_load_all",     loadingAllDataInCache); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",         cacheSize); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "lookup_schema",      schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "lookup_table",       tablename); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "lookup_orderby",     orderByClause); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "fail_on_multiple",   failingOnMultipleResults); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "eat_row_on_failure", eatingRowOnLookupFailure); //$NON-NLS-1$
			
            
			for (int i=0;i<streamKeyField1.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      streamKeyField1[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     tableKeyField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_condition", keyCondition[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name2",     streamKeyField2[i]); //$NON-NLS-1$
			}
	
			for (int i=0;i<returnValueField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_name",      returnValueField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_rename",    returnValueNewName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_default",   returnValueDefault[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      ValueMeta.getTypeDesc(returnValueDefaultType[i])); //$NON-NLS-1$
			}	
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "DatabaseLookupMeta.ERROR0003.UnableToSaveStepToRepository")+id_step, e); //$NON-NLS-1$
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
            databases = new Database[] { db }; // Keep track of this one for cancelQuery

			try
			{
				db.connect();
				
				if (!Const.isEmpty(tablename))
				{
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(db.environmentSubstitute(schemaName), db.environmentSubstitute(tablename));
                    RowMetaInterface r = db.getTableFields( schemaTable );
					if (r!=null)
					{
						// Check the keys used to do the lookup...
						
						for (int i=0;i<tableKeyField.length;i++)
						{
							String lufield = tableKeyField[i];

							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.MissingCompareFieldsInLookupTable")+Const.CR; //$NON-NLS-1$
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
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.AllLookupFieldsFoundInTable"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);
						
						// Also check the returned values!
						
						for (int i=0;i<returnValueField.length;i++)
						{
							String lufield = returnValueField[i];

							ValueMetaInterface v = r.searchValueMeta(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.MissingReturnFieldsInLookupTable")+Const.CR; //$NON-NLS-1$
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
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.AllReturnFieldsFoundInTable"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);

					}
					else
					{
						error_message=BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;
					
					for (int i=0;i<streamKeyField1.length;i++)
					{
						ValueMetaInterface v = prev.searchValueMeta(streamKeyField1[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.MissingFieldsNotFoundInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+streamKeyField1[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.AllFieldsFoundInInput"), stepinfo); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.CouldNotReadFromPreviousSteps")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(KettleDatabaseException dbe)
			{
				error_message = BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.DatabaseErrorWhileChecking")+dbe.getMessage(); //$NON-NLS-1$
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
			error_message = BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.MissingConnectionError"); //$NON-NLS-1$
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.StepIsReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "DatabaseLookupMeta.Check.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public RowMetaInterface getTableFields() {
		RowMetaInterface fields = null;
		if (databaseMeta!=null)
		{
			Database db = new Database(loggingObject, databaseMeta);
            databases = new Database[] { db }; // Keep track of this one for cancelQuery

			try
			{
				db.connect();
				    String tableName = databaseMeta.environmentSubstitute(tablename);
				    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tableName);
                fields = db.getTableFields(schemaTable);
			}
			catch(KettleDatabaseException dbe)
			{
				logError(BaseMessages.getString(PKG, "DatabaseLookupMeta.ERROR0004.ErrorGettingTableFields")+dbe.getMessage()); //$NON-NLS-1$
			}
			finally
			{
				db.disconnect();
			}
		}
		return fields;
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new DatabaseLookup(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DatabaseLookupData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		// The keys are read-only...
		for (int i=0;i<streamKeyField1.length;i++)
		{
			ValueMetaInterface v = prev.searchValueMeta(streamKeyField1[i]);
			DatabaseImpact ii = new DatabaseImpact
                ( 
                    DatabaseImpact.TYPE_IMPACT_READ, 
					transMeta.getName(),
					stepinfo.getName(),
					databaseMeta.getDatabaseName(),
					tablename,
					tableKeyField[i],
					streamKeyField1[i],
					v!=null?v.getOrigin():"?", //$NON-NLS-1$
					"", //$NON-NLS-1$
					BaseMessages.getString(PKG, "DatabaseLookupMeta.Impact.Key") //$NON-NLS-1$
				);
			impact.add(ii);
		}

		// The Return fields are read-only too...
		for (int i=0;i<returnValueField.length;i++)
		{
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
											transMeta.getName(),
											stepinfo.getName(),
											databaseMeta.getDatabaseName(),
											tablename,
											returnValueField[i],
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											"", //$NON-NLS-1$
											BaseMessages.getString(PKG, "DatabaseLookupMeta.Impact.ReturnValue") //$NON-NLS-1$
											);
			impact.add(ii);
		}
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
	 * @return Returns the eatingRowOnLookupFailure.
	 */
	public boolean isEatingRowOnLookupFailure()
	{
		return eatingRowOnLookupFailure;
	}

	/**
	 * @param eatingRowOnLookupFailure The eatingRowOnLookupFailure to set.
	 */
	public void setEatingRowOnLookupFailure(boolean eatingRowOnLookupFailure)
	{
		this.eatingRowOnLookupFailure = eatingRowOnLookupFailure;
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
	 * @return the loadingAllDataInCache
	 */
	public boolean isLoadingAllDataInCache() {
		return loadingAllDataInCache;
	}

	/**
	 * @param loadingAllDataInCache the loadingAllDataInCache to set
	 */
	public void setLoadingAllDataInCache(boolean loadingAllDataInCache) {
		this.loadingAllDataInCache = loadingAllDataInCache;
	}
}