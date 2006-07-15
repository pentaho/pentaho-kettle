 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
/*
 * Created on 26-apr-2003
 *
 */
package be.ibridge.kettle.trans.step.databaselookup;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.DatabaseImpact;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


public class DatabaseLookupMeta extends BaseStepMeta implements StepMetaInterface
{
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
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String dtype;
			String csize;
			
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = Const.findDatabase(databases, con);
			cached      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "cache")); //$NON-NLS-1$ //$NON-NLS-2$
			csize      = XMLHandler.getTagValue(stepnode, "cache_size"); //$NON-NLS-1$
			cacheSize=Const.toInt(csize, 0);
			tablename      = XMLHandler.getTagValue(stepnode, "lookup", "table"); //$NON-NLS-1$ //$NON-NLS-2$
	
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
				returnValueDefaultType[i] = Value.getType(dtype);
				if (returnValueDefaultType[i]<0)
				{
					//logError("unknown default value type: "+dtype+" for value "+value[i]+", default to type: String!");
					returnValueDefaultType[i]=Value.VALUE_TYPE_STRING;
				}
			}		
			orderByClause = XMLHandler.getTagValue(lookup, "orderby"); //Optional, can by null //$NON-NLS-1$
            failingOnMultipleResults = "Y".equalsIgnoreCase(XMLHandler.getTagValue(lookup, "fail_on_multiple"));  //$NON-NLS-1$ //$NON-NLS-2$
            eatingRowOnLookupFailure = "Y".equalsIgnoreCase(XMLHandler.getTagValue(lookup, "eat_row_on_failure"));  //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("DatabaseLookupMeta.ERROR0001.UnableToLoadStepFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		streamKeyField1	 = null;
		returnValueField = null;
		databaseMeta     = null;
		cached           = false;
		cacheSize        = 0;
		tablename        = Messages.getString("DatabaseLookupMeta.Default.TableName"); //$NON-NLS-1$

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			tableKeyField[i]   = Messages.getString("DatabaseLookupMeta.Default.KeyFieldPrefix"); //$NON-NLS-1$
			keyCondition[i]    = Messages.getString("DatabaseLookupMeta.Default.KeyCondition"); //$NON-NLS-1$
			streamKeyField1[i] = Messages.getString("DatabaseLookupMeta.Default.KeyStreamField1"); //$NON-NLS-1$
			streamKeyField2[i] = Messages.getString("DatabaseLookupMeta.Default.KeyStreamField2"); //$NON-NLS-1$
		}

		for (int i=0;i<nrvalues;i++)
		{
			returnValueField[i]=Messages.getString("DatabaseLookupMeta.Default.ReturnFieldPrefix")+i; //$NON-NLS-1$
			returnValueNewName[i]=Messages.getString("DatabaseLookupMeta.Default.ReturnNewNamePrefix")+i; //$NON-NLS-1$
			returnValueDefault[i]=Messages.getString("DatabaseLookupMeta.Default.ReturnDefaultValuePrefix")+i; //$NON-NLS-1$
			returnValueDefaultType[i]=Value.VALUE_TYPE_STRING;
		}
		
		orderByClause = ""; //$NON-NLS-1$
        failingOnMultipleResults = false;
        eatingRowOnLookupFailure = false;
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		if (info==null)
		{
			for (int i=0;i<returnValueNewName.length;i++)
			{
				Value v=new Value(returnValueNewName[i], returnValueDefaultType[i]);
				v.setOrigin(name);
				row.addValue(v);
			}
		}
		else
		{
			for (int i=0;i<returnValueNewName.length;i++)
			{
				Value v=info.searchValue(returnValueField[i]);
				if (v!=null)
				{
					v.setName(returnValueNewName[i]);
					v.setOrigin(name);
					row.addValue(v);
				}
			}
		}

		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    "+XMLHandler.addTagValue("cache", cached)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("cache_size", cacheSize)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    <lookup>"+Const.CR); //$NON-NLS-1$
		retval.append("      "+XMLHandler.addTagValue("table", tablename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("orderby", orderByClause)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      "+XMLHandler.addTagValue("fail_on_multiple", failingOnMultipleResults)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      "+XMLHandler.addTagValue("eat_row_on_failure", eatingRowOnLookupFailure)); //$NON-NLS-1$ //$NON-NLS-2$

        
		for (int i=0;i<streamKeyField1.length;i++)
		{
			retval.append("      <key>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name", streamKeyField1[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("field", tableKeyField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("condition", keyCondition[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("name2", streamKeyField2[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </key>"+Const.CR); //$NON-NLS-1$
		}

		for (int i=0;i<returnValueField.length;i++)
		{
			retval.append("      <value>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name", returnValueField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("rename", returnValueNewName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("default", returnValueDefault[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("type", Value.getTypeDesc(returnValueDefaultType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </value>"+Const.CR); //$NON-NLS-1$
		}

		retval.append("      </lookup>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			databaseMeta       = Const.findDatabase( databases, id_connection);
			
			cached                   =      rep.getStepAttributeBoolean(id_step, "cache"); //$NON-NLS-1$
			cacheSize                = (int)rep.getStepAttributeInteger(id_step, "cache_size"); //$NON-NLS-1$
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
				returnValueDefaultType[i] = Value.getType( rep.getStepAttributeString (id_step, i, "return_value_type") ); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("DatabaseLookupMeta.ERROR0002.UnexpectedErrorReadingFromTheRepository"), e); //$NON-NLS-1$
		}
	}
	

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",      databaseMeta==null?-1:databaseMeta.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache",              cached); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",         cacheSize); //$NON-NLS-1$
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
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      Value.getTypeDesc(returnValueDefaultType[i])); //$NON-NLS-1$
			}	
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("DatabaseLookupMeta.ERROR0003.UnableToSaveStepToRepository")+id_step, e); //$NON-NLS-1$
		}

	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = ""; //$NON-NLS-1$
		
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
            databases = new Database[] { db }; // Keep track of this one for cancelQuery

			try
			{
				db.connect();
				
				if (tablename!=null && tablename.length()!=0)
				{
					boolean first=true;
					boolean error_found=false;
					error_message = ""; //$NON-NLS-1$
					
					Row r = db.getTableFields(tablename);
					if (r!=null)
					{
						// Check the keys used to do the lookup...
						
						for (int i=0;i<tableKeyField.length;i++)
						{
							String lufield = tableKeyField[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("DatabaseLookupMeta.Check.MissingCompareFieldsInLookupTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseLookupMeta.Check.AllLookupFieldsFoundInTable"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);
						
						// Also check the returned values!
						
						for (int i=0;i<returnValueField.length;i++)
						{
							String lufield = returnValueField[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+=Messages.getString("DatabaseLookupMeta.Check.MissingReturnFieldsInLookupTable")+Const.CR; //$NON-NLS-1$
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseLookupMeta.Check.AllReturnFieldsFoundInTable"), stepinfo); //$NON-NLS-1$
						}
						remarks.add(cr);

					}
					else
					{
						error_message=Messages.getString("DatabaseLookupMeta.Check.CouldNotReadTableInfo"); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
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
						Value v = prev.searchValue(streamKeyField1[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("DatabaseLookupMeta.Check.MissingFieldsNotFoundInInput")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+streamKeyField1[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseLookupMeta.Check.AllFieldsFoundInInput"), stepinfo); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=Messages.getString("DatabaseLookupMeta.Check.CouldNotReadFromPreviousSteps")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(KettleDatabaseException dbe)
			{
				error_message = Messages.getString("DatabaseLookupMeta.Check.DatabaseErrorWhileChecking")+dbe.getMessage(); //$NON-NLS-1$
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			error_message = Messages.getString("DatabaseLookupMeta.Check.MissingConnectionError"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseLookupMeta.Check.StepIsReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("DatabaseLookupMeta.Check.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public Row getTableFields()
	{
		LogWriter log = LogWriter.getInstance();
		Row fields = null;
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
            databases = new Database[] { db }; // Keep track of this one for cancelQuery

			try
			{
				db.connect();
				fields = db.getTableFields(tablename);
			}
			catch(KettleDatabaseException dbe)
			{
				log.logError(toString(), Messages.getString("DatabaseLookupMeta.ERROR0004.ErrorGettingTableFields")+dbe.getMessage()); //$NON-NLS-1$
			}
			finally
			{
				db.disconnect();
			}
		}
		return fields;
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new DatabaseLookupDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new DatabaseLookup(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DatabaseLookupData();
	}

	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		// The keys are read-only...
		for (int i=0;i<streamKeyField1.length;i++)
		{
			Value v = prev.searchValue(streamKeyField1[i]);
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
											transMeta.getName(),
											stepinfo.getName(),
											databaseMeta.getDatabaseName(),
											tablename,
											tableKeyField[i],
											streamKeyField1[i],
											v!=null?v.getOrigin():"?", //$NON-NLS-1$
											"", //$NON-NLS-1$
											Messages.getString("DatabaseLookupMeta.Impact.Key") //$NON-NLS-1$
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
											Messages.getString("DatabaseLookupMeta.Impact.ReturnValue") //$NON-NLS-1$
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
}
