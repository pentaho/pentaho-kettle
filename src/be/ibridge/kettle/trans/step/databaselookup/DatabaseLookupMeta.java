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
	
	
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		streamKeyField1=null;
		returnValueField=null;
		
		readData(stepnode, databases);
	}

	public void allocate(int nrkeys, int nrvalues)
	{
		streamKeyField1          = new String[nrkeys];
		tableKeyField            = new String[nrkeys];
		keyCondition = new String[nrkeys];
		streamKeyField2         = new String[nrkeys];
		returnValueField        = new String[nrvalues];
		returnValueNewName    = new String[nrvalues];
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
			retval.streamKeyField1         [i] = streamKeyField1[i];
			retval.tableKeyField   [i] = tableKeyField[i];
			retval.keyCondition[i] = keyCondition[i];
			retval.streamKeyField2        [i] = streamKeyField2[i];
		}

		for (int i=0;i<nrvalues;i++)
		{
			retval.returnValueField[i]        = returnValueField[i];
			retval.returnValueNewName[i]    = returnValueNewName[i];
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
			
			String con = XMLHandler.getTagValue(stepnode, "connection");
			databaseMeta = Const.findDatabase(databases, con);
			cached      = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "cache"));
			csize      = XMLHandler.getTagValue(stepnode, "cache_size");
			cacheSize=Const.toInt(csize, 0);
			tablename      = XMLHandler.getTagValue(stepnode, "lookup", "table");
	
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup");
			
			int nrkeys   = XMLHandler.countNodes(lookup, "key");
			int nrvalues = XMLHandler.countNodes(lookup, "value");
	
			allocate(nrkeys, nrvalues);
					
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i);
				
				streamKeyField1         [i] = XMLHandler.getTagValue(knode, "name");
				tableKeyField   [i] = XMLHandler.getTagValue(knode, "field");
				keyCondition[i] = XMLHandler.getTagValue(knode, "condition");
				if (keyCondition[i]==null) keyCondition[i]="=";
				streamKeyField2        [i] = XMLHandler.getTagValue(knode, "name2");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i);
				
				returnValueField[i]        = XMLHandler.getTagValue(vnode, "name");
				returnValueNewName[i]    = XMLHandler.getTagValue(vnode, "rename");
				if (returnValueNewName[i]==null) returnValueNewName[i]=returnValueField[i]; // default: the same name!
				returnValueDefault[i]     = XMLHandler.getTagValue(vnode, "default");
				dtype           = XMLHandler.getTagValue(vnode, "type");
				returnValueDefaultType[i] = Value.getType(dtype);
				if (returnValueDefaultType[i]<0)
				{
					//logError("unknown default value type: "+dtype+" for value "+value[i]+", default to type: String!");
					returnValueDefaultType[i]=Value.VALUE_TYPE_STRING;
				}
			}		
			orderByClause = XMLHandler.getTagValue(lookup, "orderby"); //Optional, can by null
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		streamKeyField1			= null;
		returnValueField		= null;
		databaseMeta  = null;
		cached       = false;
		cacheSize  = 0;
		tablename       = "lookup table";

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			tableKeyField[i]   = "age";
			keyCondition[i]= "BETWEEN";
			streamKeyField1[i]         = "age_from";
			streamKeyField2[i]        = "age_to";
		}

		for (int i=0;i<nrvalues;i++)
		{
			returnValueField[i]="return field #"+i;
			returnValueNewName[i]="new name #"+i;
			returnValueDefault[i]="default #"+i;
			returnValueDefaultType[i]=Value.VALUE_TYPE_STRING;
		}
		
		orderByClause = "";
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
		String retval="";
		int i;
		
		retval+="    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName());
		retval+="    "+XMLHandler.addTagValue("cache", cached);
		retval+="    "+XMLHandler.addTagValue("cache_size", cacheSize);
		retval+="    <lookup>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("table", tablename);
		retval+="      "+XMLHandler.addTagValue("orderby", orderByClause);

		for (i=0;i<streamKeyField1.length;i++)
		{
			retval+="      <key>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name", streamKeyField1[i]);
			retval+="        "+XMLHandler.addTagValue("field", tableKeyField[i]);
			retval+="        "+XMLHandler.addTagValue("condition", keyCondition[i]);
			retval+="        "+XMLHandler.addTagValue("name2", streamKeyField2[i]);
			retval+="        </key>"+Const.CR;
		}

		for (i=0;i<returnValueField.length;i++)
		{
			retval+="      <value>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name", returnValueField[i]);
			retval+="        "+XMLHandler.addTagValue("rename", returnValueNewName[i]);
			retval+="        "+XMLHandler.addTagValue("default", returnValueDefault[i]);
			retval+="        "+XMLHandler.addTagValue("type", Value.getTypeDesc(returnValueDefaultType[i]));
			retval+="        </value>"+Const.CR;
		}

		retval+="      </lookup>"+Const.CR;

		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection"); 
			databaseMeta       = Const.findDatabase( databases, id_connection);
			
			cached            =      rep.getStepAttributeBoolean(id_step, "cache");
			cacheSize       = (int)rep.getStepAttributeInteger(id_step, "cache_size");
			tablename            =      rep.getStepAttributeString (id_step, "lookup_table"); 
			orderByClause          =      rep.getStepAttributeString (id_step, "lookup_orderby"); 
	
			int nrkeys   = rep.countNrStepAttributes(id_step, "lookup_key_name");
			int nrvalues = rep.countNrStepAttributes(id_step, "return_value_name");
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				streamKeyField1[i]          = rep.getStepAttributeString(id_step, i, "lookup_key_name");
				tableKeyField[i]    = rep.getStepAttributeString(id_step, i, "lookup_key_field");
				keyCondition[i] = rep.getStepAttributeString(id_step, i, "lookup_key_condition");
				streamKeyField2[i]         = rep.getStepAttributeString(id_step, i, "lookup_key_name2");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				returnValueField[i]        =                rep.getStepAttributeString (id_step, i, "return_value_name");
				returnValueNewName[i]    =                rep.getStepAttributeString (id_step, i, "return_value_rename");
				returnValueDefault[i]     =                rep.getStepAttributeString (id_step, i, "return_value_default");
				returnValueDefaultType[i] = Value.getType( rep.getStepAttributeString (id_step, i, "return_value_type") );
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}
	

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   databaseMeta==null?-1:databaseMeta.getID());
			rep.saveStepAttribute(id_transformation, id_step, "cache",           cached);
			rep.saveStepAttribute(id_transformation, id_step, "cache_size",      cacheSize);
			rep.saveStepAttribute(id_transformation, id_step, "lookup_table",    tablename);
			rep.saveStepAttribute(id_transformation, id_step, "lookup_orderby",  orderByClause);
			
			for (int i=0;i<streamKeyField1.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name",      streamKeyField1[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_field",     tableKeyField[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_condition", keyCondition[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "lookup_key_name2",     streamKeyField2[i]);
			}
	
			for (int i=0;i<returnValueField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_name",      returnValueField[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_rename",    returnValueNewName[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_default",   returnValueDefault[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "return_value_type",      Value.getTypeDesc(returnValueDefaultType[i]));
			}	
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}

	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = "";
		
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
					error_message = "";
					
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
									error_message+="Missing compare fields in lookup table:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR; 
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All lookup fields found in the table.", stepinfo);
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
									error_message+="Missing return fields in lookup table:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR; 
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All return fields found in the table.", stepinfo);
						}
						remarks.add(cr);

					}
					else
					{
						error_message="Couldn't read the table info, please check the table-name & permissions.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = "";
					boolean error_found = false;
					
					for (int i=0;i<streamKeyField1.length;i++)
					{
						Value v = prev.searchValue(streamKeyField1[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+="Missing fields, not found in input from previous steps:"+Const.CR;
							}
							error_found=true;
							error_message+="\t\t"+streamKeyField1[i]+Const.CR; 
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields found in the input stream.", stepinfo);
					}
					remarks.add(cr);
				}
				else
				{
					error_message="Couldn't read fields from the previous step."+Const.CR;
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}

			
			
			
			}
			catch(KettleDatabaseException dbe)
			{
				error_message = "An error occurred: "+dbe.getMessage();
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
			error_message = "Please select or create a connection!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
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
				log.logError(toString(), "An error occurred: "+dbe.getMessage());
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
											v!=null?v.getOrigin():"?",
											"",
											"Key"
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
											"",
											"",
											"",
											"Return value"
											);
			impact.add(ii);
		}
	}

}
