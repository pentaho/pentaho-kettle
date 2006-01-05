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


package be.ibridge.kettle.trans.step.tableinput;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
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
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		TableInputMeta retval = (TableInputMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String limit;
            String perRow;
		
			String con            = XMLHandler.getTagValue(stepnode, "connection");
			databaseMeta          = Const.findDatabase(databases, con);
			sql                   = XMLHandler.getTagValue(stepnode, "sql");
			limit                 = XMLHandler.getTagValue(stepnode, "limit");
			rowLimit              = Const.toInt(limit, 0);
			lookupFromStepname    = XMLHandler.getTagValue(stepnode, "lookup");
            perRow                = XMLHandler.getTagValue(stepnode, "execute_each_row");
            executeEachInputRow   = "Y".equals(perRow);
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

	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;
		boolean param=false;
				
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		if (databaseMeta==null) return row;
		
		Database db = new Database(databaseMeta);
        databases = new Database[] { db }; // keep track of it for cancelling purposes...

		// First try without connecting to the database... (can be  S L O W)
		Row add=null;
		try
		{
			add = db.getQueryFields(sql, param, info);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleStepException("Unable to get queryfields for SQL: "+Const.CR+sql, dbe);
		}

		if (add!=null)
		{
			for (int i=0;i<add.size();i++)
			{
				Value v=add.getValue(i);
				v.setOrigin(name);
			}
			row.addRow(	add );
		}
		else
		{
			try
			{
				db.connect();
				
				if (getLookupStepname()!=null) param=true;
				
				add = db.getQueryFields(sql, param, info);
				
				if (add==null) return row;
				for (int i=0;i<add.size();i++)
				{
					Value v=add.getValue(i);
					v.setOrigin(name);
				}
				row.addRow(	add );
			}
			catch(KettleException ke)
			{
				throw new KettleStepException("Unable to get queryfields for SQL: "+Const.CR+sql, ke);
			}
			finally
			{
				db.disconnect();
			}
		}
			
		return row;
	}

	public String getXML()
	{
		String xml="";
		
		xml+="    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName());
		xml+="    "+XMLHandler.addTagValue("sql",        sql);
		xml+="    "+XMLHandler.addTagValue("limit",      rowLimit);
		xml+="    "+XMLHandler.addTagValue("lookup",     getLookupStepname());
        xml+="    "+XMLHandler.addTagValue("execute_each_row",   executeEachInputRow);

		return xml;
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection = rep.getStepAttributeInteger(id_step, "id_connection"); 
			databaseMeta = Const.findDatabase( databases, id_connection);
			
			sql                   =      rep.getStepAttributeString (id_step, "sql");
			rowLimit              = (int)rep.getStepAttributeInteger(id_step, "limit");
			lookupFromStepname    =      rep.getStepAttributeString (id_step, "lookup"); 
            executeEachInputRow   =      rep.getStepAttributeBoolean(id_step, "execute_each_row");
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
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Connection exists", stepMeta);
			remarks.add(cr);

			Database db = new Database(databaseMeta);
            databases = new Database[] { db }; // keep track of it for cancelling purposes...

			try
			{
				db.connect();
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Connection to database OK", stepMeta);
				remarks.add(cr);

				if (sql!=null && sql.length()!=0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "SQL statement is entered", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "SQL statement is missing.", stepMeta);
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta);
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
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Previous step to read info from ["+getLookupStepname()+"] is found.", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Previous step to read info from ["+getLookupStepname()+"] is not found.", stepMeta);
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
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "This step is expecting and receiving "+info.size()+" fields of input from the previous step.", stepMeta);
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "This step is receiving "+info.size()+" but not the expected "+count+" fields of input from the previous step.", stepMeta);
					remarks.add(cr);
				}
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Input step name is not recognized!", stepMeta);
				remarks.add(cr);
			}
		}
		else
		{
			if (input.length>0)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Step is not expecting info from input steps.", stepMeta);
				remarks.add(cr);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "No input expected, no input provided.", stepMeta);
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

	public void searchInfoAndTargetSteps(ArrayList steps)
	{
	    lookupFromStep = TransMeta.findStep(steps, lookupFromStepname);
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new TableInputDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new TableInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new TableInputData();
	}

	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
		throws KettleStepException
	{
		// Find the lookupfields...
		Row out = getFields(null, stepMeta.getName(), info);
		if (out!=null)
		{
			for (int i=0;i<out.size();i++)
			{
				Value outvalue = out.getValue(i);
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
}
