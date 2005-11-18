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

package be.ibridge.kettle.trans.step.dbproc;

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
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
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

public class DBProcMeta extends BaseStepMeta implements StepMetaInterface
{
	/** database connection */
	private DatabaseMeta database;
	
	/** proc.-name to be called */
	private String procedure;        
	
	/** function arguments */
	private String argument[];            
	
	/** IN / OUT / INOUT */
	private String argumentDirection[];   
	
	/** value type for OUT */
	private int    argumentType[];        
	
	/** function result: new value name */
	private String resultName;       
	
	/** function result: new value type */
	private int    resultType;       
	
	
	public DBProcMeta()
	{
		super(); // allocate BaseStepMeta
	}

	
	
	/**
	 * @return Returns the argument.
	 */
	public String[] getArgument()
	{
		return argument;
	}
	
	/**
	 * @param argument The argument to set.
	 */
	public void setArgument(String[] argument)
	{
		this.argument = argument;
	}
	
	/**
	 * @return Returns the argumentDirection.
	 */
	public String[] getArgumentDirection()
	{
		return argumentDirection;
	}
	
	/**
	 * @param argumentDirection The argumentDirection to set.
	 */
	public void setArgumentDirection(String[] argumentDirection)
	{
		this.argumentDirection = argumentDirection;
	}
	
	/**
	 * @return Returns the argumentType.
	 */
	public int[] getArgumentType()
	{
		return argumentType;
	}
	
	/**
	 * @param argumentType The argumentType to set.
	 */
	public void setArgumentType(int[] argumentType)
	{
		this.argumentType = argumentType;
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
	 * @return Returns the procedure.
	 */
	public String getProcedure()
	{
		return procedure;
	}
	
	/**
	 * @param procedure The procedure to set.
	 */
	public void setProcedure(String procedure)
	{
		this.procedure = procedure;
	}
	
	/**
	 * @return Returns the resultName.
	 */
	public String getResultName()
	{
		return resultName;
	}
	
	/**
	 * @param resultName The resultName to set.
	 */
	public void setResultName(String resultName)
	{
		this.resultName = resultName;
	}
	
	/**
	 * @return Returns the resultType.
	 */
	public int getResultType()
	{
		return resultType;
	}
	
	/**
	 * @param resultType The resultType to set.
	 */
	public void setResultType(int resultType)
	{
		this.resultType = resultType;
	}
	
	
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public void allocate(int nrargs)
	{
		argument          = new String[nrargs];
		argumentDirection       = new String[nrargs];
		argumentType      = new int   [nrargs];
	}

	public Object clone()
	{
		DBProcMeta retval = (DBProcMeta)super.clone();
		int nrargs    = argument.length;

		retval.allocate(nrargs);

		for (int i=0;i<nrargs;i++)
		{
			retval.argument    [i] = argument[i];
			retval.argumentDirection [i] = argumentDirection[i];
			retval.argumentType[i] = argumentType[i];
		}

		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			int i;
			int nrargs;
			
			String con = XMLHandler.getTagValue(stepnode, "connection");
			database = Const.findDatabase(databases, con);
			procedure  = XMLHandler.getTagValue(stepnode, "procedure");
	
			Node lookup = XMLHandler.getSubNode(stepnode, "lookup");
			nrargs    = XMLHandler.countNodes(lookup, "arg");
	
			allocate(nrargs);
	
			for (i=0;i<nrargs;i++)
			{
				Node anode = XMLHandler.getSubNodeByNr(lookup, "arg", i);
				
				argument    [i] = XMLHandler.getTagValue(anode, "name");
				argumentDirection [i] = XMLHandler.getTagValue(anode, "direction");
				argumentType[i] = Value.getType(XMLHandler.getTagValue(anode, "type"));
			}
			
			resultName = XMLHandler.getTagValue(stepnode, "result", "name"); //Optional, can be null
			resultType = Value.getType(XMLHandler.getTagValue(stepnode, "result", "type"));
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to read step information from XML", e);
		}
	}

	public void setDefault()
	{
		int i;
		int nrargs;
		
		database = null;

		nrargs    = 0;

		allocate(nrargs);

		for (i=0;i<nrargs;i++)
		{
			argument    [i]="arg"+i;
			argumentDirection [i]="IN";
			argumentType[i]=Value.VALUE_TYPE_NUMBER;
		}

		resultName = "result";
		resultType = Value.VALUE_TYPE_NUMBER;
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		int i;
		for (i=0;i<argument.length;i++)
		{
			if (argumentDirection[i].equalsIgnoreCase("OUT") || argumentDirection[i].equalsIgnoreCase("INOUT"))
			{
				Value v=new Value(argument[i], argumentType[i]);
				v.setOrigin(name);
				row.addValue(v);
			}
		}
		if (resultName!=null)
		{
			Value v=new Value(resultName, resultType);
			v.setOrigin(name);
			row.addValue(v);
		}

		return row;
	}

	public String getXML()
	{
		String retval="";
		int i;
		
		retval+="    "+XMLHandler.addTagValue("connection", database==null?"":database.getName());
		retval+="    "+XMLHandler.addTagValue("procedure", procedure);
		retval+="    <lookup>"+Const.CR;

		for (i=0;i<argument.length;i++)
		{
			retval+="      <arg>"+Const.CR;
			retval+="        "+XMLHandler.addTagValue("name",      argument[i]);
			retval+="        "+XMLHandler.addTagValue("direction", argumentDirection[i]);
			retval+="        "+XMLHandler.addTagValue("type",      Value.getTypeDesc(argumentType[i]));
			retval+="        </arg>"+Const.CR;
		}

		retval+="      </lookup>"+Const.CR;

		retval+="    <result>"+Const.CR;
		retval+="      "+XMLHandler.addTagValue("name", resultName);
		retval+="      "+XMLHandler.addTagValue("type", Value.getTypeDesc(resultType));
		retval+="      </result>"+Const.CR;

		return retval;
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection =   rep.getStepAttributeInteger(id_step, "id_connection"); 
			database = Const.findDatabase( databases, id_connection);
			procedure = rep.getStepAttributeString(id_step, "procedure");
	
			int nrargs = rep.countNrStepAttributes(id_step, "arg_name");
			allocate(nrargs);
			
			for (int i=0;i<nrargs;i++)
			{
				argument[i]     = rep.getStepAttributeString(id_step, i, "arg_name");
				argumentDirection[i]  = rep.getStepAttributeString(id_step, i, "arg_direction");
				argumentType[i] = Value.getType( rep.getStepAttributeString(id_step, i, "arg_type") );
			}
			
			resultName =                rep.getStepAttributeString(id_step, "result_name");
			resultType = Value.getType( rep.getStepAttributeString(id_step, "result_type") );
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
			rep.saveStepAttribute(id_transformation, id_step, "procedure",     procedure);
	
			for (int i=0;i<argument.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "arg_name",      argument[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "arg_direction", argumentDirection[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "arg_type",      Value.getTypeDesc(argumentType[i]));
			}
			
			rep.saveStepAttribute(id_transformation, id_step, "result_name",     resultName);
			rep.saveStepAttribute(id_transformation, id_step, "result_type",     Value.getTypeDesc(resultType));
			
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
				
				// Look up fields in the input stream <prev>
				if (prev!=null && prev.size()>0)
				{
					boolean first=true;
					error_message = "";
					boolean error_found = false;
					
					for (int i=0;i<argument.length;i++)
					{
						Value v = prev.searchValue(argument[i]);
						if (v==null)
						{
							if (first)
							{
								first=false;
								error_message+="Missing arguments, not found in input from previous steps:"+Const.CR;
							}
							error_found=true;
							error_message+="\t\t"+argument[i]+Const.CR; 
						}
						else // Argument exists in input stream: same type?
						{
							if (v.getType()!=argumentType[i] &&
							   !(v.isNumeric() && Value.isNumeric(argumentType[i])) 
							    )
							{
								error_found=true;
								error_message+="\t\t"+argument[i]+" (found but wrong type: "+v.getTypeDesc()+" vs. "+Value.getTypeDesc(argumentType[i])+")"+Const.CR; 
							}
						}
					}
					if (error_found)
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
					}
					else
					{
						cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "All arguments found in the input stream.", stepMeta);
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
				error_message = "A an error occurred: "+e.getMessage();
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
				remarks.add(cr);
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

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new DBProcDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new DBProc(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new DBProcData();
	}

}
