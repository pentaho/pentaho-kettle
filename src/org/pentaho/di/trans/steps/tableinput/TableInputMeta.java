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


package org.pentaho.di.trans.steps.tableinput;

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
import org.pentaho.di.core.row.RowDataUtil;
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
import org.w3c.dom.Node;

/*
 * Created on 2-jun-2003
 *
 */
public class TableInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private DatabaseMeta databaseMeta;
	private String sql;
	private int rowLimit;

	/** Which step is providing the date, just the name?*/
	private String lookupFromStepname;
	
	/** The step to lookup from */
	private StepMeta lookupFromStep;    
	
    /** Should I execute once per row? */
    private boolean executeEachInputRow;
    
    private boolean variableReplacementActive;

    private boolean lazyConversionActive;

	public TableInputMeta()
	{
		super();
	}
	
    /**
     * @return Returns true if the step should be run per row
     */
    public boolean isExecuteEachInputRow()
    {
        return executeEachInputRow;
    }

    /**
     * @param oncePerRow true if the step should be run per row
     */
    public void setExecuteEachInputRow(boolean oncePerRow)
    {
        this.executeEachInputRow = oncePerRow;
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
	 * @return Returns the rowLimit.
	 */
	public int getRowLimit()
	{
		return rowLimit;
	}
	
	/**
	 * @param rowLimit The rowLimit to set.
	 */
	public void setRowLimit(int rowLimit)
	{
		this.rowLimit = rowLimit;
	}
	
	/**
	 * @return Returns the sql.
	 */
	public String getSQL()
	{
		return sql;
	}
	
	/**
	 * @param sql The sql to set.
	 */
	public void setSQL(String sql)
	{
		this.sql = sql;
	}
	
	/**
	 * @return Returns the lookupFromStep.
	 */
	public StepMeta getLookupFromStep()
	{
		return lookupFromStep;
	}
	
	/**
	 * @param lookupFromStep The lookupFromStep to set.
	 */
	public void setLookupFromStep(StepMeta lookupFromStep)
	{
		this.lookupFromStep = lookupFromStep;
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		TableInputMeta retval = (TableInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
		throws KettleXMLException
	{
		try
		{
			databaseMeta              = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(stepnode, "connection"));
			sql                       = XMLHandler.getTagValue(stepnode, "sql");
			rowLimit                  = Const.toInt(XMLHandler.getTagValue(stepnode, "limit"), 0);
			lookupFromStepname        = XMLHandler.getTagValue(stepnode, "lookup");
            executeEachInputRow       = "Y".equals(XMLHandler.getTagValue(stepnode, "execute_each_row"));
            variableReplacementActive = "Y".equals(XMLHandler.getTagValue(stepnode, "variables_active"));
            lazyConversionActive      = "Y".equals(XMLHandler.getTagValue(stepnode, "lazy_conversion_active"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}

	public void setDefault()
	{
		databaseMeta = null;
		sql        = "SELECT <values> FROM <table name> WHERE <conditions>";
		rowLimit   = 0;
	}

	/**
	 * @return the informational source steps, if any. Null is the default: none.
	 */
	public String[] getInfoSteps()
	{
	    if (getLookupStepname()==null) return null;
	    return new String[] { getLookupStepname() };
	}
    
    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
        if (infoSteps!=null && infoSteps.length>0)
        {
            lookupFromStep = infoSteps[0];
        }
    }

    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
		if (databaseMeta==null) return; // TODO: throw an exception here

        boolean param=false;

		Database db = new Database(databaseMeta);
		db.shareVariablesWith(space);
        databases = new Database[] { db }; // keep track of it for canceling purposes...

		// First try without connecting to the database... (can be  S L O W)
        String sNewSQL = sql; 
        if (isVariableReplacementActive()) sNewSQL = db.environmentSubstitute(sql); // TODO SB 

		RowMetaInterface add=null;
		try
		{
			add = db.getQueryFields(sNewSQL, param);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleStepException("Unable to get queryfields for SQL: "+Const.CR+sNewSQL, dbe);
		}

		if (add!=null)
		{
			for (int i=0;i<add.size();i++)
			{
				ValueMetaInterface v=add.getValueMeta(i);
				v.setOrigin(origin);
			}
			row.addRowMeta( add );
		}
		else
		{
			try
			{
				db.connect();
				
				RowMetaInterface paramRowMeta=null;
				Object[] paramData=null;
				
				if (getLookupStepname()!=null) 
				{
					param=true;
					if (info.length>=0 && info[0]!=null) {
						paramRowMeta=info[0];
						paramData = RowDataUtil.allocateRowData(paramRowMeta.size());
					}
				}
				
				add = db.getQueryFields(sNewSQL, param, paramRowMeta, paramData);
				
				if (add==null) return;
				for (int i=0;i<add.size();i++)
				{
					ValueMetaInterface v=add.getValueMeta(i);
					v.setOrigin(origin);
				}
				row.addRowMeta( add );
			}
			catch(KettleException ke)
			{
				throw new KettleStepException("Unable to get queryfields for SQL: "+Const.CR+sNewSQL, ke);
			}
			finally
			{
				db.disconnect();
			}
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName()));
		retval.append("    "+XMLHandler.addTagValue("sql",        sql));
		retval.append("    "+XMLHandler.addTagValue("limit",      rowLimit));
		retval.append("    "+XMLHandler.addTagValue("lookup",     getLookupStepname()));
        retval.append("    "+XMLHandler.addTagValue("execute_each_row",   executeEachInputRow));
        retval.append("    "+XMLHandler.addTagValue("variables_active",   variableReplacementActive));
        retval.append("    "+XMLHandler.addTagValue("lazy_conversion_active",   lazyConversionActive));
        
		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
			long id_connection = rep.getStepAttributeInteger(id_step, "id_connection"); 
			databaseMeta = DatabaseMeta.findDatabase( databases, id_connection);
			
			sql                       =      rep.getStepAttributeString (id_step, "sql");
			rowLimit                  = (int)rep.getStepAttributeInteger(id_step, "limit");
			lookupFromStepname        =      rep.getStepAttributeString (id_step, "lookup"); 
            executeEachInputRow       =      rep.getStepAttributeBoolean(id_step, "execute_each_row");
            variableReplacementActive =      rep.getStepAttributeBoolean(id_step, "variables_active");
            lazyConversionActive      =      rep.getStepAttributeBoolean(id_step, "lazy_conversion_active");
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
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",    databaseMeta==null?-1:databaseMeta.getID());
			rep.saveStepAttribute(id_transformation, id_step, "sql",              sql);
			rep.saveStepAttribute(id_transformation, id_step, "limit",            rowLimit);
			rep.saveStepAttribute(id_transformation, id_step, "lookup",           getLookupStepname());
            rep.saveStepAttribute(id_transformation, id_step, "execute_each_row", executeEachInputRow);
            rep.saveStepAttribute(id_transformation, id_step, "variables_active", variableReplacementActive);
            rep.saveStepAttribute(id_transformation, id_step, "lazy_conversion_active", lazyConversionActive);
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Connection exists", stepMeta);
			remarks.add(cr);

			Database db = new Database(databaseMeta);
			db.shareVariablesWith(transMeta);
            databases = new Database[] { db }; // keep track of it for cancelling purposes...

			try
			{
				db.connect();
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Connection to database OK", stepMeta);
				remarks.add(cr);

				if (sql!=null && sql.length()!=0)
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "SQL statement is entered", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "SQL statement is missing.", stepMeta);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "An error occurred: "+e.getMessage(), stepMeta);
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta);
			remarks.add(cr);
		}
		
		// See if we have an informative step...
		if (getLookupStepname()!=null)
		{
			boolean found=false;
			for (int i=0;i<input.length;i++)
			{
				if (getLookupStepname().equalsIgnoreCase(input[i])) found=true;
			}
			if (found)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Previous step to read info from ["+getLookupStepname()+"] is found.", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Previous step to read info from ["+getLookupStepname()+"] is not found.", stepMeta);
				remarks.add(cr);
			}
			
			// Count the number of ? in the SQL string:
			int count=0;
			for (int i=0;i<sql.length();i++)
			{
				char c = sql.charAt(i);
				if (c=='\'') // skip to next quote!
				{
					do
					{
						i++;
						c = sql.charAt(i);
					}
					while (c!='\'');
				}
				if (c=='?') count++;
			}
			// Verify with the number of informative fields...
			if (info!=null)
			{
				if(count == info.size())
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "This step is expecting and receiving "+info.size()+" fields of input from the previous step.", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "This step is receiving "+info.size()+" but not the expected "+count+" fields of input from the previous step.", stepMeta);
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Input step name is not recognized!", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			if (input.length>0)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Step is not expecting info from input steps.", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "No input expected, no input provided.", stepMeta);
				remarks.add(cr);
			}
			
		}
	}
	
	public String getLookupStepname()
	{
		if (lookupFromStep!=null && 
			lookupFromStep.getName()!=null &&
			lookupFromStep.getName().length()>0
		   ) 
			return lookupFromStep.getName();
		return null;
	}

	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
	    lookupFromStep = StepMeta.findStep(steps, lookupFromStepname);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new TableInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new TableInputData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info) throws KettleStepException
	{
		// Find the lookupfields...
        RowMetaInterface out = new RowMeta(); 
        // TODO: this builds, but does it work in all cases.
        getFields(out, stepMeta.getName(), new RowMetaInterface[] { info }, null, transMeta);
        
		if (out!=null)
		{
			for (int i=0;i<out.size();i++)
			{
				ValueMetaInterface outvalue = out.getValueMeta(i);
				DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
												transMeta.getName(),
												stepMeta.getName(),
												databaseMeta.getDatabaseName(),
												"",
												outvalue.getName(),
												outvalue.getName(),
												stepMeta.getName(),
												sql,
												"read from one or more database tables via SQL statement"
												);
				impact.add(ii);

			}
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
     * @return Returns the variableReplacementActive.
     */
    public boolean isVariableReplacementActive()
    {
        return variableReplacementActive;
    }

    /**
     * @param variableReplacementActive The variableReplacementActive to set.
     */
    public void setVariableReplacementActive(boolean variableReplacementActive)
    {
        this.variableReplacementActive = variableReplacementActive;
    }

	/**
	 * @return the lazyConversionActive
	 */
	public boolean isLazyConversionActive() {
		return lazyConversionActive;
	}

	/**
	 * @param lazyConversionActive the lazyConversionActive to set
	 */
	public void setLazyConversionActive(boolean lazyConversionActive) {
		this.lazyConversionActive = lazyConversionActive;
	}
}
