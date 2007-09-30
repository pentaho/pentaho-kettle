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
package be.ibridge.kettle.trans.step.databasejoin;

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


public class DatabaseJoinMeta extends BaseStepMeta implements StepMetaInterface
{
	/** database connection */
	private DatabaseMeta databaseMeta; 
	
	/** SQL Statement */
	private String sql;              
	
	/** Number of rows to return (0=ALL) */
	private int rowLimit;           
	
	/** false: don't return rows where nothing is found
	    true: at least return one source row, the rest is NULL */
	private boolean outerJoin;      
	                                
	/** Fields to use as parameters (fill in the ? markers) */
	private String parameterField[];     
	
	/** Type of the paramenters */
	private int    parameterType[];      

	
	public DatabaseJoinMeta()
	{
		super(); // allocate BaseStepMeta
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
	 * @return Returns the outerJoin.
	 */
	public boolean isOuterJoin()
	{
		return outerJoin;
	}
	
	/**
	 * @param outerJoin The outerJoin to set.
	 */
	public void setOuterJoin(boolean outerJoin)
	{
		this.outerJoin = outerJoin;
	}
	
	/**
	 * @return Returns the parameterField.
	 */
	public String[] getParameterField()
	{
		return parameterField;
	}
	
	/**
	 * @param parameterField The parameterField to set.
	 */
	public void setParameterField(String[] parameterField)
	{
		this.parameterField = parameterField;
	}
	
	/**
	 * @return Returns the parameterType.
	 */
	public int[] getParameterType()
	{
		return parameterType;
	}
	
	/**
	 * @param parameterType The parameterType to set.
	 */
	public void setParameterType(int[] parameterType)
	{
		this.parameterType = parameterType;
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
	public String getSql()
	{
		return sql;
	}
	
	/**
	 * @param sql The sql to set.
	 */
	public void setSql(String sql)
	{
		this.sql = sql;
	}
	
	
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		parameterField=null;
		parameterType =null;
		outerJoin=false;
		
		readData(stepnode, databases);
	}

	public void allocate(int nrparam)
	{
		parameterField   = new String[nrparam];
		parameterType    = new int   [nrparam];
	}

	public Object clone()
	{
		DatabaseJoinMeta retval = (DatabaseJoinMeta)super.clone();
		
		int nrparam  = parameterField.length;

		retval.allocate(nrparam);
		
		for (int i=0;i<nrparam;i++)
		{
			retval.parameterField  [i] = parameterField[i];
			retval.parameterType   [i] = parameterType[i];
		}

		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{		
		try
		{
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			sql        = XMLHandler.getTagValue(stepnode, "sql"); //$NON-NLS-1$
			outerJoin = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "outer_join")); //$NON-NLS-1$ //$NON-NLS-2$
			
			rowLimit   = Const.toInt(XMLHandler.getTagValue(stepnode, "rowlimit"), 0); //$NON-NLS-1$
			
			Node param = XMLHandler.getSubNode(stepnode, "parameter"); //$NON-NLS-1$
			int nrparam  = XMLHandler.countNodes(param, "field"); //$NON-NLS-1$
	
			allocate(nrparam);
					
			for (int i=0;i<nrparam;i++)
			{
				Node pnode = XMLHandler.getSubNodeByNr(param, "field", i); //$NON-NLS-1$
				parameterField  [i] = XMLHandler.getTagValue(pnode, "name"); //$NON-NLS-1$
				String ptype    = XMLHandler.getTagValue(pnode, "type"); //$NON-NLS-1$
				parameterType   [i] = Value.getType(ptype);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("DatabaseJoinMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		databaseMeta = null;
		rowLimit   = 0;
		sql = ""; //$NON-NLS-1$
		outerJoin=false;
		parameterField=null;
		parameterType=null;
		outerJoin=false;
		
		int nrparam  = 0;
		
		allocate(nrparam);
		
		for (int i=0;i<nrparam;i++)
		{
			parameterField  [i] = "param"+i; //$NON-NLS-1$
			parameterType   [i] = Value.VALUE_TYPE_NUMBER;
		}
	}
	
	public Row getParameterRow(Row fields)
	{
		Row param = new Row();
		
		if ( fields != null )
		{
		    for (int i=0;i<parameterField.length;i++)
		    {
			    Value v = fields.searchValue(parameterField[i]);
			    if (v!=null) param.addValue(v);
		    }
		}
		return param;
	}

	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;
				
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		if (databaseMeta==null) return row;
		
		Database db = new Database(databaseMeta);
        databases = new Database[] { db }; // Keep track of this one for cancelQuery
		
		// Which fields are parameters?
		Row param = getParameterRow(info);
		
		// First try without connecting to the database... (can be  S L O W)
		// See if it's in the cache...
		Row add =null;
		try
		{
			add = db.getQueryFields(sql, true, param);
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleStepException(Messages.getString("DatabaseJoinMeta.Exception.UnableToDetermineQueryFields")+Const.CR+sql, dbe); //$NON-NLS-1$
		}

		if (add!=null)  // Cache hit, just return it this...
		{
			for (int i=0;i<add.size();i++)
			{
				Value v=add.getValue(i);
				v.setOrigin(name);
			}
			row.addRow(	add );
		}
		else
			
        // No cache hit, connect to the database, do it the hard way...
		try 
		{
			db.connect();
			add = db.getQueryFields(sql, true, param);
			if (add==null) return row;
			for (int i=0;i<add.size();i++)
			{
				Value v=add.getValue(i);
				v.setOrigin(name);
			}
			row.addRow(	add );
			db.disconnect();
		}
		catch(KettleDatabaseException dbe)
		{
			throw new KettleStepException(Messages.getString("DatabaseJoinMeta.Exception.ErrorObtainingFields"), dbe); //$NON-NLS-1$
		}
		
		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    "+XMLHandler.addTagValue("rowlimit", rowLimit)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("sql", sql)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("outer_join", outerJoin)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <parameter>"+Const.CR); //$NON-NLS-1$
		for (int i=0;i<parameterField.length;i++)
		{
			retval.append("      <field>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name", parameterField[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("type", Value.getTypeDesc(parameterType[i]))); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"+Const.CR); //$NON-NLS-1$
		}
		retval.append("      </parameter>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			databaseMeta       = DatabaseMeta.findDatabase( databases, id_connection);
			rowLimit         = (int)rep.getStepAttributeInteger(id_step, "rowlimit"); //$NON-NLS-1$
			sql              =      rep.getStepAttributeString (id_step, "sql");  //$NON-NLS-1$
			outerJoin       =      rep.getStepAttributeBoolean(id_step, "outer_join");  //$NON-NLS-1$
	
			int nrparam = rep.countNrStepAttributes(id_step, "parameter_field"); //$NON-NLS-1$
			
			allocate(nrparam);
			
			for (int i=0;i<nrparam;i++)
			{
				parameterField[i]   = rep.getStepAttributeString(id_step, i, "parameter_field"); //$NON-NLS-1$
				String stype    = rep.getStepAttributeString(id_step, i, "parameter_type"); //$NON-NLS-1$
				parameterType[i]    = Value.getType(stype);
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("DatabaseJoinMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   databaseMeta==null?-1:databaseMeta.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "rowlimit",        rowLimit); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sql",             sql); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "outer_join",      outerJoin); //$NON-NLS-1$
			
			for (int i=0;i<parameterField.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_field", parameterField[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_type",  Value.getTypeDesc( parameterType[i] )); //$NON-NLS-1$
			}
			
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("DatabaseJoinMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
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
				if (sql!=null && sql.length()!=0)
				{
					Row param = getParameterRow(prev);
					
					error_message = ""; //$NON-NLS-1$
					
					Row r = db.getQueryFields(sql, true, param);
					if (r!=null)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseJoinMeta.CheckResult.QueryOK"), stepinfo); //$NON-NLS-1$
						remarks.add(cr);
					}
					else
					{
						error_message=Messages.getString("DatabaseJoinMeta.CheckResult.InvalidDBQuery"); //$NON-NLS-1$
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
					
					int q = db.countParameters(sql);
					if (q!=parameterField.length)
					{
						error_message=Messages.getString("DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion")+Const.CR; //$NON-NLS-1$
						error_message+=Messages.getString("DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion2")+q+Const.CR; //$NON-NLS-1$
						error_message+=Messages.getString("DatabaseJoinMeta.CheckResult.DismatchBetweenParametersAndQuestion3")+parameterField.length; //$NON-NLS-1$
						
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
						remarks.add(cr);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseJoinMeta.CheckResult.NumberOfParamCorrect")+q+")", stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
						remarks.add(cr);
					}
				}
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = ""; //$NON-NLS-1$
					boolean error_found = false;
					
					for (int i=0;i<parameterField.length;i++)
					{
						Value v = prev.searchValue(parameterField[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+=Messages.getString("DatabaseJoinMeta.CheckResult.MissingFields")+Const.CR; //$NON-NLS-1$
							}
							error_found=true;
							error_message+="\t\t"+parameterField[i]+Const.CR;  //$NON-NLS-1$
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseJoinMeta.CheckResult.AllFieldsFound"), stepinfo); //$NON-NLS-1$
					}
					remarks.add(cr);
				}
				else
				{
					error_message=Messages.getString("DatabaseJoinMeta.CheckResult.CounldNotReadFields")+Const.CR; //$NON-NLS-1$
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				error_message = Messages.getString("DatabaseJoinMeta.CheckResult.ErrorOccurred")+e.getMessage(); //$NON-NLS-1$
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
			error_message = Messages.getString("DatabaseJoinMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("DatabaseJoinMeta.CheckResult.ReceivingInfo"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("DatabaseJoinMeta.CheckResult.NoInputReceived"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}

	}
	
	public Row getTableFields()
	{
		LogWriter log = LogWriter.getInstance();
		
		// Build a dummy parameter row...
		Row param = new Row();
		for (int i=0;i<parameterField.length;i++)
		{
			param.addValue( new Value(parameterField[i], parameterType[i]) );
		}
		
		Row fields = null;
		if (databaseMeta!=null)
		{
			Database db = new Database(databaseMeta);
            databases = new Database[] { db }; // Keep track of this one for cancelQuery

			try
			{
				db.connect();
				fields = db.getQueryFields(sql, true, param);
			}
			catch(KettleDatabaseException dbe)
			{
				log.logError(toString(), Messages.getString("DatabaseJoinMeta.Log.DatabaseErrorOccurred")+dbe.getMessage()); //$NON-NLS-1$
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
		return new DatabaseJoinDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new DatabaseJoin(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DatabaseJoinData();
	}


	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
		throws KettleStepException
	{
		// Find the lookupfields...
		Row out = getFields(null, stepinfo.getName(), info);
		if (out!=null)
		{
			for (int i=0;i<out.size();i++)
			{
				Value outvalue = out.getValue(i);
				DatabaseImpact di = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ, 
												transMeta.getName(),
												stepinfo.getName(),
												databaseMeta.getDatabaseName(),
												"", //$NON-NLS-1$
												outvalue.getName(),
												outvalue.getName(),
												stepinfo.getName(),
												sql,
												Messages.getString("DatabaseJoinMeta.DatabaseImpact.Title") //$NON-NLS-1$
												);
				impact.add(di);

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
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
