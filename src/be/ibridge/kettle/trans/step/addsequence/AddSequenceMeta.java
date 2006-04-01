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
 
package be.ibridge.kettle.trans.step.addsequence;

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
 * Created on 13-mei-2003
 *
 */

public class AddSequenceMeta extends BaseStepMeta implements StepMetaInterface
{
	private String       valuename;	

	private boolean      useDatabase;
	private DatabaseMeta database;
	private String       sequenceName;

	private boolean      useCounter;
	private long         startAt;
	private long         incrementBy;
	private long         maxValue;

	public AddSequenceMeta()
	{
		super();
	}

	/**
	 * @return Returns the connection.
	 */
	public DatabaseMeta getDatabase()
	{
		return database;
	}
	
	/**
	 * @param connection The connection to set.
	 */
	public void setDatabase(DatabaseMeta connection)
	{
		this.database = connection;
	}
	
	/**
	 * @return Returns the incrementBy.
	 */
	public long getIncrementBy()
	{
		return incrementBy;
	}
	
	/**
	 * @param incrementBy The incrementBy to set.
	 */
	public void setIncrementBy(long incrementBy)
	{
		this.incrementBy = incrementBy;
	}
	
	/**
	 * @return Returns the maxValue.
	 */
	public long getMaxValue()
	{
		return maxValue;
	}
	
	/**
	 * @param maxValue The maxValue to set.
	 */
	public void setMaxValue(long maxValue)
	{
		this.maxValue = maxValue;
	}
	
	/**
	 * @return Returns the sequenceName.
	 */
	public String getSequenceName()
	{
		return sequenceName;
	}
	
	/**
	 * @param sequenceName The sequenceName to set.
	 */
	public void setSequenceName(String sequenceName)
	{
		this.sequenceName = sequenceName;
	}
	
	/**
	 * @return Returns the start of the sequence.
	 */
	public long getStartAt()
	{
		return startAt;
	}
	
	/**
	 * @param startAt The starting point of the sequence to set.
	 */
	public void setStartAt(long startAt)
	{
		this.startAt = startAt;
	}
	
	/**
	 * @return Returns the useCounter.
	 */
	public boolean isCounterUsed()
	{
		return useCounter;
	}
	
	/**
	 * @param useCounter The useCounter to set.
	 */
	public void setUseCounter(boolean useCounter)
	{
		this.useCounter = useCounter;
	}
	
	/**
	 * @return Returns the useDatabase.
	 */
	public boolean isDatabaseUsed()
	{
		return useDatabase;
	}
	
	/**
	 * @param useDatabase The useDatabase to set.
	 */
	public void setUseDatabase(boolean useDatabase)
	{
		this.useDatabase = useDatabase;
	}
	
	/**
	 * @return Returns the valuename.
	 */
	public String getValuename()
	{
		return valuename;
	}
	
	/**
	 * @param valuename The valuename to set.
	 */
	public void setValuename(String valuename)
	{
		this.valuename = valuename;
	}
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}
	
	public Object clone()
	{
		Object retval = super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			valuename  = XMLHandler.getTagValue(stepnode, "valuename");
			
			useDatabase = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_database"));
			String conn  = XMLHandler.getTagValue(stepnode, "connection");
			database   = Const.findDatabase(databases, conn);
			sequenceName      = XMLHandler.getTagValue(stepnode, "seqname");
			
			useCounter  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_counter"));
			startAt     = Const.toLong(XMLHandler.getTagValue(stepnode, "start_at"), 1);
			incrementBy = Const.toLong(XMLHandler.getTagValue(stepnode, "increment_by"), 1);
			maxValue    = Const.toLong(XMLHandler.getTagValue(stepnode, "max_value"), 999999999L);
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Error loading step info from XML", e);
		}
	}
	
	public void setDefault()
	{
		valuename  = "valuename";
		
		useDatabase = false;
		sequenceName    = "SEQ_";
		database = null;
		
		useCounter  = true;
		startAt     = 1L;
		incrementBy = 1L;
		maxValue    = 9999999L;
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		Value v=new Value(valuename, Value.VALUE_TYPE_INTEGER);
		v.setLength(9,0);
		v.setOrigin(name);
		row.addValue( v );
		
		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("      "+XMLHandler.addTagValue("valuename", valuename));
		retval.append("      "+XMLHandler.addTagValue("use_database", useDatabase));
		retval.append("      "+XMLHandler.addTagValue("connection", database==null?"":database.getName()));
		retval.append("      "+XMLHandler.addTagValue("seqname", sequenceName));

		retval.append("      "+XMLHandler.addTagValue("use_counter", useCounter));
		retval.append("      "+XMLHandler.addTagValue("start_at", startAt));
		retval.append("      "+XMLHandler.addTagValue("increment_by", incrementBy));
		retval.append("      "+XMLHandler.addTagValue("max_value", maxValue));
		
		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			valuename        =      rep.getStepAttributeString (id_step, "valuename");
	
			useDatabase     =      rep.getStepAttributeBoolean(id_step, "use_database"); 
			long id_connection =    rep.getStepAttributeInteger(id_step, "id_connection"); 
			database = Const.findDatabase( databases, id_connection);
			sequenceName          =      rep.getStepAttributeString (id_step, "seqname");
	
			useCounter      =      rep.getStepAttributeBoolean(id_step, "use_counter"); 
			startAt         =      rep.getStepAttributeInteger(id_step, "start_at"); 
			incrementBy     =      rep.getStepAttributeInteger(id_step, "increment_by"); 
			maxValue        =      rep.getStepAttributeInteger(id_step, "max_value");
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to read step information from the repository for id_step="+id_step, e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "valuename",       valuename);
			
			rep.saveStepAttribute(id_transformation, id_step, "use_database",    useDatabase);
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   database==null?-1:database.getID());
			rep.saveStepAttribute(id_transformation, id_step, "seqname",         sequenceName);
					
			rep.saveStepAttribute(id_transformation, id_step, "use_counter",     useCounter);
			rep.saveStepAttribute(id_transformation, id_step, "start_at",        startAt);
			rep.saveStepAttribute(id_transformation, id_step, "increment_by",    incrementBy);
			rep.saveStepAttribute(id_transformation, id_step, "max_value",       maxValue);
			
			// Also, save the step-database relationship!
			if (database!=null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step info to the repository for id_step="+id_step, e);
		}
	}


	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (useDatabase)
		{
			Database db = new Database(database);
			try
			{
				db.connect();
				if (db.checkSequenceExists(sequenceName))
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Sequence exits.", stepMeta);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "The sequence ["+sequenceName+"] couldn't be found", stepMeta);
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "Unable to connect to database to verify sequence because of an error: "+Const.CR+e.getMessage(), stepMeta);
			}
			finally
			{
				db.disconnect();
			}
			remarks.add(cr);
		}
		
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
	
		if (useDatabase) // Otherwise, don't bother!
		{
			if (database!=null)
			{
				Database db = new Database(database);
				try
				{
					db.connect();
					if (!db.checkSequenceExists(sequenceName))
					{
						String cr_table = db.getCreateSequenceStatement(sequenceName, startAt, incrementBy, maxValue, true);
						retval.setSQL(cr_table);
					}
					else
					{
						retval.setSQL(null); // Empty string means: nothing to do: set it to null...
					}
				}
				catch(KettleException e)
				{
					retval.setError("I was unable to connect to the database to verify the status of the table."+Const.CR+e.getMessage());
				}
				finally
				{
					db.disconnect();
				}
			}
			else
			{
				retval.setError("There is no connection defined in this step.");
			}
		}

		return retval;
	}

	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new AddSequenceDialog(shell, info, transMeta, name);
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new AddSequence(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new AddSequenceData();
	}
}
