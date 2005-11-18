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


package be.ibridge.kettle.trans.step.insertupdate;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.SQLStatement;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
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


/*
 * Created on 26-apr-2003
 *
 */

public class InsertUpdateMeta extends BaseStepMeta implements StepMetaInterface
{
    /** what's the lookup table? */
	private String tableName;
	
    /** database connection */
	private DatabaseMeta database;
	
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
	
    /** Commit size for inserts/updates */
	private int    commitSize;
	
	public InsertUpdateMeta()
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
     * @return Returns the database.
     */
    public DatabaseMeta getDatabase()
    {
        return database;
    }
    
    /**
     * @param database The database to set.
     */
    public void setDatabase(DatabaseMeta database)
    {
        this.database = database;
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
    
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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
		InsertUpdateMeta retval = (InsertUpdateMeta)super.clone();
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
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String csize;
			int nrkeys, nrvalues;
			
			String con = XMLHandler.getTagValue(stepnode, "connection");
			database = Const.findDatabase(databases, con);
			csize      = XMLHandler.getTagValue(stepnode, "commit");
			commitSize=Const.toInt(csize, 0);
			tableName      = XMLHandler.getTagValue(stepnode, "lookup", "table");
	
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup");
			nrkeys    = XMLHandler.countNodes(lookup, "key");
			nrvalues  = XMLHandler.countNodes(lookup, "value");
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				Node knode = XMLHandler.getSubNodeByNr(lookup, "key", i);
				
				keyStream         [i] = XMLHandler.getTagValue(knode, "name");
				keyLookup   [i] = XMLHandler.getTagValue(knode, "field");
				keyCondition[i] = XMLHandler.getTagValue(knode, "condition");
				if (keyCondition[i]==null) keyCondition[i]="=";
				keyStream2        [i] = XMLHandler.getTagValue(knode, "name2");
			}
	
			for (int i=0;i<nrvalues;i++)
			{
				Node vnode = XMLHandler.getSubNodeByNr(lookup, "value", i);
				
				updateLookup[i]        = XMLHandler.getTagValue(vnode, "name");
				updateStream[i]    = XMLHandler.getTagValue(vnode, "rename");
				if (updateStream[i]==null) updateStream[i]=updateLookup[i]; // default: the same name!
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}

	public void setDefault()
	{
		keyStream        = null;
		updateLookup      = null;
		database = null;
		commitSize     = 0;
		tableName      = "lookup table";

		int nrkeys   = 0;
		int nrvalues = 0;

		allocate(nrkeys, nrvalues);
		
		for (int i=0;i<nrkeys;i++)
		{
			keyLookup[i]   = "age";
			keyCondition[i]= "BETWEEN";
			keyStream[i]         = "age_from";
			keyStream2[i]        = "age_to";
		}

		for (int i=0;i<nrvalues;i++)
		{
			updateLookup[i]="return field #"+i;
			updateStream[i]="new name #"+i;
		}
	}

	public String getXML()
	{
		String retval="";
		int i;
		
		retval+="    "+XMLHandler.addTagValue("connection", database==null?"":database.getName());
		retval+="    "+XMLHandler.addTagValue("commit", commitSize);
		retval+="    <lookup>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("table", tableName);

		for (i=0;i<keyStream.length;i++)
		{
			retval+="      <key>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name", keyStream[i]);
			retval+="        "+XMLHandler.addTagValue("field", keyLookup[i]);
			retval+="        "+XMLHandler.addTagValue("condition", keyCondition[i]);
			retval+="        "+XMLHandler.addTagValue("name2", keyStream2[i]);
			retval+="        </key>"+Const.CR;
		}

		for (i=0;i<updateLookup.length;i++)
		{
			retval+="      <value>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name", updateLookup[i]);
			retval+="        "+XMLHandler.addTagValue("rename", updateStream[i]);
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
			database = Const.findDatabase( databases, id_connection);
			
			commitSize     = (int)rep.getStepAttributeInteger(id_step, "commit");
			tableName      =      rep.getStepAttributeString(id_step, "table");
	
			int nrkeys   = rep.countNrStepAttributes(id_step, "key_name");
			int nrvalues = rep.countNrStepAttributes(id_step, "value_name");
			
			allocate(nrkeys, nrvalues);
			
			for (int i=0;i<nrkeys;i++)
			{
				keyStream[i]          = rep.getStepAttributeString(id_step, i, "key_name");
				keyLookup[i]    = rep.getStepAttributeString(id_step, i, "key_field");
				keyCondition[i] = rep.getStepAttributeString(id_step, i, "key_condition");
				keyStream2[i]         = rep.getStepAttributeString(id_step, i, "key_name2");
			}
			
			for (int i=0;i<nrvalues;i++)
			{
				updateLookup[i]        = rep.getStepAttributeString(id_step, i, "value_name");
				updateStream[i]    = rep.getStepAttributeString(id_step, i, "value_rename");
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
			rep.saveStepAttribute(id_transformation, id_step, "id_connection", database==null?-1:database.getID());
			rep.saveStepAttribute(id_transformation, id_step, "commit",        commitSize);
			rep.saveStepAttribute(id_transformation, id_step, "table",         tableName);
	
			for (int i=0;i<keyStream.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name",      keyStream[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "key_field",     keyLookup[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "key_condition", keyCondition[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "key_name2",     keyStream2[i]);
			}
	
			for (int i=0;i<updateLookup.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "value_name",    updateLookup[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "value_rename",  updateStream[i]);
			}
			
			// Also, save the step-database relationship!
			if (database!=null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		String error_message = "";
		
		if (database!=null)
		{
			Database db = new Database(database);
			try
			{
				db.connect();
				
				if (tableName!=null && tableName.length()!=0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Table name is filled in.", stepMeta);
					remarks.add(cr);
			
					boolean first=true;
					boolean error_found=false;
					error_message = "";
					
					// Check fields in table
					Row r = db.getTableFields(tableName);
					if (r!=null)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Table exists and we can read data from it.", stepMeta);
						remarks.add(cr);
			
						for (int i=0;i<keyLookup.length;i++)
						{
							String lufield = keyLookup[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+="Missing compare fields in target table:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR; 
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All lookup fields found in the table.", stepMeta);
						}
						remarks.add(cr);
						
						// How about the fields to insert/update in the table?
						first=true;
						error_found=false;
						error_message = "";

						for (int i=0;i<updateLookup.length;i++)
						{
							String lufield = updateLookup[i];

							Value v = r.searchValue(lufield);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+="Missing fields to update/insert in target table:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+lufield+Const.CR; 
							}
						}
						if (error_found)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All insert/update fields found in the table.", stepMeta);
						}
						remarks.add(cr);
					}
					else
					{
						error_message="Couldn't read the table info, please check the table-name & permissions.";
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
					remarks.add(cr);
			
					boolean first=true;
					error_message = "";
					boolean error_found = false;
					
					for (int i=0;i<keyStream.length;i++)
					{
						Value v = prev.searchValue(keyStream[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+="Missing fields, not found in input from previous steps:"+Const.CR;
							}
							error_found=true;
							error_message+="\t\t"+keyStream[i]+Const.CR; 
						}
					}
					for (int i=0;i<keyStream2.length;i++)
					{
						if (keyStream2[i]!=null && keyStream2[i].length()>0)
						{
							Value v = prev.searchValue(keyStream2[i]);
							if (v==null)
							{
								if (first)
								{
									first=false;
									error_message+="Missing fields, not found in input from previous steps:"+Const.CR;
								}
								error_found=true;
								error_message+="\t\t"+keyStream[i]+Const.CR; 
							}
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields found in the input stream.", stepMeta);
					}
					remarks.add(cr);

					// How about the fields to insert/update the table with?
					first=true;
					error_found=false;
					error_message = "";

					for (int i=0;i<updateStream.length;i++)
					{
						String lufield = updateStream[i];

						Value v = prev.searchValue(lufield);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+="Missing input stream fields to update/insert the target table with:"+Const.CR;
							}
							error_found=true;
							error_message+="\t\t"+lufield+Const.CR; 
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All insert/update fields found in the input stream.", stepMeta);
					}
					remarks.add(cr);
				}
				else
				{
					error_message="Couldn't read fields from the previous step."+Const.CR;
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = "A database error occurred: "+e.getMessage();
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
			error_message = "Please select or create a connection!";
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}
	}
	
	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), database, null); // default: nothing to do!
	
		if (database!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (tableName!=null && tableName.length()>0)
				{
					Database db = new Database(database);
					try
					{
						db.connect();
						
						String cr_table = db.getDDL(tableName, 
													prev, 
													null, 
													false, 
													null,
													true
													);
						
						String cr_index = "";
						String idx_fields[] = null;
						
						if (keyLookup!=null && keyLookup.length>0)
						{
							idx_fields = new String[keyLookup.length];
							for (int i=0;i<keyLookup.length;i++) idx_fields[i] = keyLookup[i];
						}
						else
						{
							retval.setError("No key fields are specified.  Please specify the fields use as lookup key for this table.");
						}

						// Key lookup dimensions...
						if (idx_fields!=null && idx_fields.length>0 && 
							!db.checkIndexExists(tableName, idx_fields)
						   )
						{
							String indexname = "idx_"+tableName+"_lookup";
							cr_index = db.getCreateIndexStatement(tableName, indexname, idx_fields, false, false, false, true);
						}
						
						String sql = cr_table+cr_index;
						if (sql.length()==0) retval.setSQL(null); else retval.setSQL(sql);
					}
					catch(KettleException e)
					{
						retval.setError("An error occurred: "+e.getMessage());
					}
				}
				else
				{
					retval.setError("No table is defined on this connection.");
				}
			}
			else
			{
				retval.setError("Not receiving any fields from previous steps. Check the previous steps for errors & the connecting hops.");
			}
		}
		else
		{
			retval.setError("There is no connection defined in this step.");
		}

		return retval;
	}
	
	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String[] input,
			String[] output, Row info)
            throws KettleStepException
    {
        if (prev != null)
        {
            // Lookup: we do a lookup on the natural keys 
            for (int i = 0; i < keyLookup.length; i++)
            {
                Value v = prev.searchValue(keyStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ, transMeta.getName(), stepMeta.getName(), database
                        .getDatabaseName(), tableName, keyLookup[i], keyStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta());
                impact.add(ii);
            }

            // Insert update fields : read/write
            for (int i = 0; i < updateLookup.length; i++)
            {
                Value v = prev.searchValue(updateStream[i]);

                DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(), stepMeta.getName(), database
                        .getDatabaseName(), tableName, updateLookup[i], updateStream[i], v!=null?v.getOrigin():"?", "", "Type = " + v.toStringMeta());
                impact.add(ii);
            }
        }
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new InsertUpdateDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new InsertUpdate(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new InsertUpdateData();
	}

}
