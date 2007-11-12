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

package org.pentaho.di.trans.steps.tableoutput;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
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



/*
 * Created on 2-jun-2003
 *
 */
 
public class TableOutputMeta extends BaseStepMeta implements StepMetaInterface
{
	private DatabaseMeta databaseMeta;
    private String       schemaName;
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
    
    private boolean      returningGeneratedKeys;
    private String       generatedKeyField;

    /**
	 * @return Returns the generatedKeyField.
	 */
	public String getGeneratedKeyField() {
		return generatedKeyField;
	}

	/**
	 * @param generatedKeyField The generatedKeyField to set.
	 */
	public void setGeneratedKeyField(String generatedKeyField) {
		this.generatedKeyField = generatedKeyField;
	}

	/**
	 * @return Returns the returningGeneratedKeys.
	 */
	public boolean isReturningGeneratedKeys() {
		return returningGeneratedKeys;
	}

	/**
	 * @param returningGeneratedKeys The returningGeneratedKeys to set.
	 */
	public void setReturningGeneratedKeys(boolean returningGeneratedKeys) {
		this.returningGeneratedKeys = returningGeneratedKeys;
	}

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
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
    
    
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
		try
		{
			String commit;
		
			String con = XMLHandler.getTagValue(stepnode, "connection");
			databaseMeta      = DatabaseMeta.findDatabase(databases, con);
            schemaName    = XMLHandler.getTagValue(stepnode, "schema");
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
            
            returningGeneratedKeys = "Y".equalsIgnoreCase( XMLHandler.getTagValue(stepnode, "return_keys"));
            generatedKeyField   = XMLHandler.getTagValue(stepnode, "return_field");
        }
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		databaseMeta = null;
		tablename      = "";
		commitSize = 100;
        
        partitioningEnabled = false;
        partitioningMonthly = true;
        partitioningField   = "";
        tableNameInTable    = true;
        tableNameField      = "";
        
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection",    databaseMeta==null?"":databaseMeta.getName()));
        retval.append("    "+XMLHandler.addTagValue("schema",        schemaName));
		retval.append("    "+XMLHandler.addTagValue("table",         tablename));
		retval.append("    "+XMLHandler.addTagValue("commit",        commitSize));
		retval.append("    "+XMLHandler.addTagValue("truncate",      truncateTable));
		retval.append("    "+XMLHandler.addTagValue("ignore_errors", ignoreErrors));
		retval.append("    "+XMLHandler.addTagValue("use_batch",     useBatchUpdate));

        retval.append("    "+XMLHandler.addTagValue("partitioning_enabled",   partitioningEnabled));
        retval.append("    "+XMLHandler.addTagValue("partitioning_field",     partitioningField));
        retval.append("    "+XMLHandler.addTagValue("partitioning_daily",     partitioningDaily));
        retval.append("    "+XMLHandler.addTagValue("partitioning_monthly",   partitioningMonthly));
        
        retval.append("    "+XMLHandler.addTagValue("tablename_in_field", tableNameInField));
        retval.append("    "+XMLHandler.addTagValue("tablename_field", tableNameField));
        retval.append("    "+XMLHandler.addTagValue("tablename_in_table", tableNameInTable));

		retval.append("    "+XMLHandler.addTagValue("return_keys", returningGeneratedKeys));
        retval.append("    "+XMLHandler.addTagValue("return_field", generatedKeyField));

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection"); 
			databaseMeta = DatabaseMeta.findDatabase( databases, id_connection);
            schemaName       =      rep.getStepAttributeString (id_step, "schema");
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
            
            returningGeneratedKeys= rep.getStepAttributeBoolean(id_step, "return_keys");
            generatedKeyField     = rep.getStepAttributeString (id_step, "return_field");
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   databaseMeta==null?-1:databaseMeta.getID());
            rep.saveStepAttribute(id_transformation, id_step, "schema",          schemaName);
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

            rep.saveStepAttribute(id_transformation, id_step, "return_keys", returningGeneratedKeys);
            rep.saveStepAttribute(id_transformation, id_step, "return_field", generatedKeyField);
            
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
			
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
    	// Just add the returning key field...
		if (returningGeneratedKeys && generatedKeyField!=null && generatedKeyField.length()>0)
		{
			ValueMetaInterface key = new ValueMeta(generatedKeyField, ValueMetaInterface.TYPE_INTEGER);
			key.setOrigin(origin);
			row.addValueMeta(key);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		if (databaseMeta!=null)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.ConnectionExists"), stepMeta);
			remarks.add(cr);

			Database db = new Database(databaseMeta);
			db.shareVariablesWith(transMeta);
			try
			{
				db.connect();
				
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.ConnectionOk"), stepMeta);
				remarks.add(cr);

				if (!Const.isEmpty(tablename))
				{
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tablename);
					// Check if this table exists...
					if (db.checkTableExists(schemaTable))
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.TableAccessible", schemaTable), stepMeta);
						remarks.add(cr);

						RowMetaInterface r = db.getTableFields(schemaTable);
						if (r!=null)
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.TableOk", schemaTable), stepMeta);
							remarks.add(cr);

							String error_message = "";
							boolean error_found = false;
							// OK, we have the table fields.
							// Now see what we can find as previous step...
							if (prev!=null && prev.size()>0)
							{
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
								remarks.add(cr);
	
								// Starting from prev...
								for (int i=0;i<prev.size();i++)
								{
									ValueMetaInterface pv = prev.getValueMeta(i);
									int idx = r.indexOfValue(pv.getName());
									if (idx<0) 
									{
										error_message+="\t\t"+pv.getName()+" ("+pv.getTypeDesc()+")"+Const.CR;
										error_found=true;
									} 
								}
								if (error_found) 
								{
									error_message=Messages.getString("TableOutputMeta.CheckResult.FieldsNotFoundInOutput", error_message);
	
									cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
									remarks.add(cr);
								}
								else
								{
									cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.AllFieldsFoundInOutput"), stepMeta);
									remarks.add(cr);
								}
	
								// Starting from table fields in r...
								for (int i=0;i<r.size();i++)
								{
									ValueMetaInterface rv = r.getValueMeta(i);
									int idx = prev.indexOfValue(rv.getName());
									if (idx<0) 
									{
										error_message+="\t\t"+rv.getName()+" ("+rv.getTypeDesc()+")"+Const.CR;
										error_found=true;
									} 
								}
								if (error_found) 
								{
									error_message=Messages.getString("TableOutputMeta.CheckResult.FieldsNotFound", error_message);
	
									cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, error_message, stepMeta);
									remarks.add(cr);
								}
								else
								{
									cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.AllFieldsFound"), stepMeta);
									remarks.add(cr);
								}
							}
							else
							{
								cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.NoFields"), stepMeta);
								remarks.add(cr);
							}
						}
						else
						{
							cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.TableNotAccessible"), stepMeta);
							remarks.add(cr);
						}
					}
					else
					{
						cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.TableError", schemaTable), stepMeta);
						remarks.add(cr);
					}
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.NoTableName"), stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.UndefinedError", e.getMessage()), stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.NoConnection"), stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("TableOutputMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("TableOutputMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new TableOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new TableOutputData();
	}
	
	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		if (truncateTable)
		{
			DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_TRUNCATE, 
											transMeta.getName(),
											stepMeta.getName(),
											databaseMeta.getDatabaseName(),
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
				ValueMetaInterface v = prev.getValueMeta(i);
				DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_WRITE, 
												transMeta.getName(),
												stepMeta.getName(),
												databaseMeta.getDatabaseName(),
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

	public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev)
	{
		SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!
	
		if (databaseMeta!=null)
		{
			if (prev!=null && prev.size()>0)
			{
				if (!Const.isEmpty(tablename))
				{
					Database db = new Database(databaseMeta);
					db.shareVariablesWith(transMeta);
					try
					{
						db.connect();
						
                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tablename);
                        String cr_table = db.getDDL(schemaTable, prev);
						
						// Empty string means: nothing to do: set it to null...
						if (cr_table==null || cr_table.length()==0) cr_table=null;
						
						retval.setSQL(cr_table);
					}
					catch(KettleDatabaseException dbe)
					{
						retval.setError(Messages.getString("TableOutputMeta.Error.ErrorConnecting", dbe.getMessage()));
					}
					finally
					{
						db.disconnect();
					}
				}
				else
				{
					retval.setError(Messages.getString("TableOutputMeta.Error.NoTable"));
				}
			}
			else
			{
				retval.setError(Messages.getString("TableOutputMeta.Error.NoInput"));
			}
		}
		else
		{
			retval.setError(Messages.getString("TableOutputMeta.Error.NoConnection"));
		}

		return retval;
	}

    public RowMetaInterface getRequiredFields() throws KettleException
    {
        if (databaseMeta!=null)
        {
            Database db = new Database(databaseMeta);
            try
            {
                db.connect();
                
                if (!Const.isEmpty(tablename))
                {
                    String schemaTable = databaseMeta.getQuotedSchemaTableCombination(schemaName, tablename);
                    
                    // Check if this table exists...
                    if (db.checkTableExists(schemaTable))
                    {
                        return db.getTableFields(schemaTable);
                    }
                    else
                    {
                        throw new KettleException(Messages.getString("TableOutputMeta.Exception.TableNotFound"));
                    }
                }
                else
                {
                    throw new KettleException(Messages.getString("TableOutputMeta.Exception.TableNotSpecified"));
                }
            }
            catch(Exception e)
            {
                throw new KettleException(Messages.getString("TableOutputMeta.Exception.ErrorGettingFields"), e);
            }
            finally
            {
                db.disconnect();
            }
        }
        else
        {
            throw new KettleException(Messages.getString("TableOutputMeta.Exception.ConnectionNotDefined"));
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
