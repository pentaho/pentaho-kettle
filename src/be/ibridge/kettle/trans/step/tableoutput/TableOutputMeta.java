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

package be.ibridge.kettle.trans.step.tableoutput;

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


/*
 * Created on 2-jun-2003
 *
 */
 
public class TableOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private DatabaseMeta database;
	private String       tablename;
	private int          commitSize;
	private boolean      truncateTable;
	private boolean      ignoreErrors;
	private boolean      useBatchUpdate;
    
    private boolean      partitioningEnabled;
    private String       partitioningField;
    private boolean      partitioningDaily;
    private boolean      partitioningMonthly;
    	
    private boolean      tableNameInField;
    private String       tableNameField;
    private boolean      tableNameInTable;

    /**
     * @return Returns the tableNameInTable.
     */
    public boolean isTableNameInTable()
    {
        return tableNameInTable;
    }

    /**
     * @param tableNameInTable The tableNameInTable to set.
     */
    public void setTableNameInTable(boolean tableNameInTable)
    {
        this.tableNameInTable = tableNameInTable;
    }

    /**
     * @return Returns the tableNameField.
     */
    public String getTableNameField()
    {
        return tableNameField;
    }

    /**
     * @param tableNameField The tableNameField to set.
     */
    public void setTableNameField(String tableNameField)
    {
        this.tableNameField = tableNameField;
    }

    /**
     * @return Returns the tableNameInField.
     */
    public boolean isTableNameInField()
    {
        return tableNameInField;
    }

    /**
     * @param tableNameInField The tableNameInField to set.
     */
    public void setTableNameInField(boolean tableNameInField)
    {
        this.tableNameInField = tableNameInField;
    }

    
    /**
     * @return Returns the partitioningDaily.
     */
    public boolean isPartitioningDaily()
    {
        return partitioningDaily;
    }

    /**
     * @param partitioningDaily The partitioningDaily to set.
     */
    public void setPartitioningDaily(boolean partitioningDaily)
    {
        this.partitioningDaily = partitioningDaily;
    }

    /**
     * @return Returns the partitioningMontly.
     */
    public boolean isPartitioningMonthly()
    {
        return partitioningMonthly;
    }

    /**
     * @param partitioningMontly The partitioningMontly to set.
     */
    public void setPartitioningMonthly(boolean partitioningMontly)
    {
        this.partitioningMonthly = partitioningMontly;
    }

    /**
     * @return Returns the partitioningEnabled.
     */
    public boolean isPartitioningEnabled()
    {
        return partitioningEnabled;
    }

    /**
     * @param partitioningEnabled The partitioningEnabled to set.
     */
    public void setPartitioningEnabled(boolean partitioningEnabled)
    {
        this.partitioningEnabled = partitioningEnabled;
    }

    /**
     * @return Returns the partitioningField.
     */
    public String getPartitioningField()
    {
        return partitioningField;
    }

    /**
     * @param partitioningField The partitioningField to set.
     */
    public void setPartitioningField(String partitioningField)
    {
        this.partitioningField = partitioningField;
    }

    
    public TableOutputMeta()
	{
		super(); // allocate BaseStepMeta
		useBatchUpdate=true;
		commitSize=100;
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		TableOutputMeta retval = (TableOutputMeta)super.clone();
		return retval;
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
     * @return Returns the truncate table flag.
     */
    public boolean truncateTable()
    {
        return truncateTable;
    }
    
    /**
     * @param truncateTable The truncate table flag to set.
     */
    public void setTruncateTable(boolean truncateTable)
    {
        this.truncateTable = truncateTable;
    }
    
    /**
     * @param ignoreErrors The ignore errors flag to set.
     */
    public void setIgnoreErrors(boolean ignoreErrors)
    {
        this.ignoreErrors = ignoreErrors;
    }
    
    /**
     * @return Returns the ignore errors flag.
     */
    public boolean ignoreErrors()
    {
        return ignoreErrors;
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
    
    
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String commit;
		
			String con = XMLHandler.getTagValue(stepnode, "connection");
			database      = Const.findDatabase(databases, con);
			tablename     = XMLHandler.getTagValue(stepnode, "table");
			commit        = XMLHandler.getTagValue(stepnode, "commit");
			commitSize    = Const.toInt(commit, 0);
			truncateTable = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "truncate"));
			ignoreErrors  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "ignore_errors"));
			useBatchUpdate= "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_batch"));

            partitioningEnabled  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "partitioning_enabled"));
            partitioningField    = XMLHandler.getTagValue(stepnode, "partitioning_field");
            partitioningDaily    = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "partitioning_daily"));
            partitioningMonthly  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "partitioning_monthly"));
            
            tableNameInField = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "tablename_in_field"));
            tableNameField   = XMLHandler.getTagValue(stepnode, "tablename_field");
            tableNameInTable = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "tablename_in_table"));
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		database = null;
		tablename      = "";
		commitSize = 0;
        
        partitioningEnabled = false;
        partitioningMonthly = true;
        partitioningField   = "";
        tableNameInTable    = true;
        tableNameField      = "";
        
	}

	public String getXML()
	{
		String retval=new String();
		
		retval+="    "+XMLHandler.addTagValue("connection",    database==null?"":database.getName());
		retval+="    "+XMLHandler.addTagValue("table",         tablename);
		retval+="    "+XMLHandler.addTagValue("commit",        commitSize);
		retval+="    "+XMLHandler.addTagValue("truncate",      truncateTable);
		retval+="    "+XMLHandler.addTagValue("ignore_errors", ignoreErrors);
		retval+="    "+XMLHandler.addTagValue("use_batch",     useBatchUpdate);

        retval+="    "+XMLHandler.addTagValue("partitioning_enabled",   partitioningEnabled);
        retval+="    "+XMLHandler.addTagValue("partitioning_field",     partitioningField);
        retval+="    "+XMLHandler.addTagValue("partitioning_daily",     partitioningDaily);
        retval+="    "+XMLHandler.addTagValue("partitioning_monthly",   partitioningMonthly);
        
        retval+="    "+XMLHandler.addTagValue("tablename_in_field", tableNameInField);
        retval+="    "+XMLHandler.addTagValue("tablename_field", tableNameField);
        retval+="    "+XMLHandler.addTagValue("tablename_in_table", tableNameInTable);
        
		return retval;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection"); 
			database = Const.findDatabase( databases, id_connection);
			tablename        =      rep.getStepAttributeString (id_step, "table");
			commitSize       = (int)rep.getStepAttributeInteger(id_step, "commit");
			truncateTable    =      rep.getStepAttributeBoolean(id_step, "truncate"); 
			ignoreErrors     =      rep.getStepAttributeBoolean(id_step, "ignore_errors"); 
			useBatchUpdate   =      rep.getStepAttributeBoolean(id_step, "use_batch"); 
            
            partitioningEnabled   = rep.getStepAttributeBoolean(id_step, "partitioning_enabled"); 
            partitioningField     = rep.getStepAttributeString (id_step, "partitioning_field"); 
            partitioningDaily     = rep.getStepAttributeBoolean(id_step, "partitioning_daily"); 
            partitioningMonthly   = rep.getStepAttributeBoolean(id_step, "partitioning_monthly"); 

            tableNameInField      = rep.getStepAttributeBoolean(id_step, "tablename_in_field"); 
            tableNameField        = rep.getStepAttributeString (id_step, "tablename_field"); 
            tableNameInTable      = rep.getStepAttributeBoolean(id_step, "tablename_in_table"); 
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
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   database==null?-1:database.getID());
			rep.saveStepAttribute(id_transformation, id_step, "table",       	 tablename);
			rep.saveStepAttribute(id_transformation, id_step, "commit",          commitSize);
			rep.saveStepAttribute(id_transformation, id_step, "truncate",        truncateTable);
			rep.saveStepAttribute(id_transformation, id_step, "ignore_errors",   ignoreErrors);
			rep.saveStepAttribute(id_transformation, id_step, "use_batch",       useBatchUpdate);
			
            rep.saveStepAttribute(id_transformation, id_step, "partitioning_enabled", partitioningEnabled);
            rep.saveStepAttribute(id_transformation, id_step, "partitioning_field",   partitioningField);
            rep.saveStepAttribute(id_transformation, id_step, "partitioning_daily",   partitioningDaily);
            rep.saveStepAttribute(id_transformation, id_step, "partitioning_monthly", partitioningMonthly);
            
            rep.saveStepAttribute(id_transformation, id_step, "tablename_in_field", tableNameInField);
            rep.saveStepAttribute(id_transformation, id_step, "tablename_field" ,   tableNameField);
            rep.saveStepAttribute(id_transformation, id_step, "tablename_in_table", tableNameInTable);

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
		if (database!=null)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Connection exists", stepMeta);
			remarks.add(cr);

			Database db = new Database(database);
			try
			{
				db.connect();
				
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Connection to database OK", stepMeta);
				remarks.add(cr);

				if (tablename!=null && tablename.length()!=0)
				{
					// Check if this table exists...
					if (db.checkTableExists(tablename))
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Table ["+tablename+"] exists and is accessible", stepMeta);
						remarks.add(cr);

						Row r = db.getTableFields(tablename);
						if (r!=null)
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Table ["+tablename+"] is readeable and we got the fields from it.", stepMeta);
							remarks.add(cr);

							String error_message = "";
							boolean error_found = false;
							// OK, we have the table fields.
							// Now see what we can find as previous step...
							if (prev!=null && prev.size()>0)
							{
								cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepMeta);
								remarks.add(cr);
	
								// Starting from prev...
								for (int i=0;i<prev.size();i++)
								{
									Value pv = prev.getValue(i);
									int idx = r.searchValueIndex(pv.getName());
									if (idx<0) 
									{
										error_message+="\t\t"+pv.getName()+" ("+pv.getTypeDesc()+")"+Const.CR;
										error_found=true;
									} 
								}
								if (error_found) 
								{
									error_message="Fields in input stream, not found in output table:"+Const.CR+Const.CR+error_message;
	
									cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
									remarks.add(cr);
								}
								else
								{
									cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields, coming from previous steps, are found in the output table", stepMeta);
									remarks.add(cr);
								}
	
								// Starting from table fields in r...
								for (int i=0;i<r.size();i++)
								{
									Value rv = r.getValue(i);
									int idx = prev.searchValueIndex(rv.getName());
									if (idx<0) 
									{
										error_message+="\t\t"+rv.getName()+" ("+rv.getTypeDesc()+")"+Const.CR;
										error_found=true;
									} 
								}
								if (error_found) 
								{
									error_message="Fields in table, not found in input stream:"+Const.CR+Const.CR+error_message;
	
									cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
									remarks.add(cr);
								}
								else
								{
									cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All fields in the table are found in the input stream, coming from previous steps", stepMeta);
									remarks.add(cr);
								}
							}
							else
							{
								cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Couldn't find fields from previous steps, check the hops...!", stepMeta);
								remarks.add(cr);
							}
						}
						else
						{
							cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Couldn't read the table info, please check the table-name & permissions.", stepMeta);
							remarks.add(cr);
						}
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Table ["+tablename+"] doesn't exist or can't be read on this database connection.", stepMeta);
						remarks.add(cr);
					}
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No table name was entered in this step.", stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "An error occurred: "+e.getMessage(), stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new TableOutputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new TableOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new TableOutputData();
	}

	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		if (truncateTable)
		{
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_TRUNCATE, 
											transMeta.getName(),
											stepMeta.getName(),
											database.getDatabaseName(),
											tablename,
											"",
											"",
											"",
											"",
											"Truncate of table"
											);
			impact.add(ii);

		}
		// The values that are entering this step are in "prev":
		if (prev!=null)
		{
			for (int i=0;i<prev.size();i++)
			{
				Value v = prev.getValue(i);
				DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_WRITE, 
												transMeta.getName(),
												stepMeta.getName(),
												database.getDatabaseName(),
												tablename,
												v.getName(),
												v.getName(),
                                                v!=null?v.getOrigin():"?",
												"",
												"Type = "+v.toStringMeta()
												);
				impact.add(ii);
			}
		}
	}

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, Row prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), database, null); // default: nothing to do!
	
		if (database!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (tablename!=null && tablename.length()>0)
				{
					Database db = new Database(database);
					try
					{
						db.connect();
						
						String cr_table = db.getDDL(tablename, prev);
						
						// Empty string means: nothing to do: set it to null...
						if (cr_table==null || cr_table.length()==0) cr_table=null;
						
						retval.setSQL(cr_table);
					}
					catch(KettleDatabaseException dbe)
					{
						retval.setError("I was unable to connect to the database to verify the status of the table: "+dbe.getMessage());
					}
					finally
					{
						db.disconnect();
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

}
