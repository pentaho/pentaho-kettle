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
    private String       schemaName;
	private String       sequenceName;

	private boolean      useCounter;
    private String       counterName;  
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
			valuename  = XMLHandler.getTagValue(stepnode, "valuename"); //$NON-NLS-1$
			
			useDatabase = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_database")); //$NON-NLS-1$ //$NON-NLS-2$
			String conn  = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			database   = DatabaseMeta.findDatabase(databases, conn);
            schemaName        = XMLHandler.getTagValue(stepnode, "schema"); //$NON-NLS-1$
			sequenceName      = XMLHandler.getTagValue(stepnode, "seqname"); //$NON-NLS-1$
			
			useCounter  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_counter")); //$NON-NLS-1$ //$NON-NLS-2$
            counterName = XMLHandler.getTagValue(stepnode, "counter_name"); //$NON-NLS-1$
			startAt     = Const.toLong(XMLHandler.getTagValue(stepnode, "start_at"), 1); //$NON-NLS-1$
			incrementBy = Const.toLong(XMLHandler.getTagValue(stepnode, "increment_by"), 1); //$NON-NLS-1$
			maxValue    = Const.toLong(XMLHandler.getTagValue(stepnode, "max_value"), 999999999L); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("AddSequenceMeta.Exception.ErrorLoadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		valuename  = "valuename"; //$NON-NLS-1$
		
		useDatabase = false;
        schemaName    = ""; //$NON-NLS-1$
		sequenceName    = "SEQ_"; //$NON-NLS-1$
		database = null;
		
		useCounter  = true;
        counterName = null;
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
		
		retval.append("      "+XMLHandler.addTagValue("valuename", valuename)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("use_database", useDatabase)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("connection", database==null?"":database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("      "+XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("seqname", sequenceName)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("      "+XMLHandler.addTagValue("use_counter", useCounter)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      "+XMLHandler.addTagValue("counter_name", counterName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("start_at", startAt)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("increment_by", incrementBy)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("max_value", maxValue)); //$NON-NLS-1$ //$NON-NLS-2$
		
		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			valuename          =      rep.getStepAttributeString (id_step, "valuename"); //$NON-NLS-1$
	
			useDatabase        =      rep.getStepAttributeBoolean(id_step, "use_database");  //$NON-NLS-1$
			long id_connection =    rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			database = DatabaseMeta.findDatabase( databases, id_connection);
            schemaName         =      rep.getStepAttributeString (id_step, "schema"); //$NON-NLS-1$
			sequenceName       =      rep.getStepAttributeString (id_step, "seqname"); //$NON-NLS-1$
	
			useCounter      =      rep.getStepAttributeBoolean(id_step, "use_counter");  //$NON-NLS-1$
            counterName     =      rep.getStepAttributeString (id_step, "counter_name");  //$NON-NLS-1$
			startAt         =      rep.getStepAttributeInteger(id_step, "start_at");  //$NON-NLS-1$
			incrementBy     =      rep.getStepAttributeInteger(id_step, "increment_by");  //$NON-NLS-1$
			maxValue        =      rep.getStepAttributeInteger(id_step, "max_value"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("AddSequenceMeta.Exception.UnableToReadStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "valuename",       valuename); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "use_database",    useDatabase); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   database==null?-1:database.getID()); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "schema",          schemaName); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "seqname",         sequenceName); //$NON-NLS-1$
					
			rep.saveStepAttribute(id_transformation, id_step, "use_counter",     useCounter); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "counter_name",    counterName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "start_at",        startAt); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "increment_by",    incrementBy); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "max_value",       maxValue); //$NON-NLS-1$
			
			// Also, save the step-database relationship!
			if (database!=null) rep.insertStepDatabase(id_transformation, id_step, database.getID());
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("AddSequenceMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
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
				if (db.checkSequenceExists(schemaName, sequenceName))
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddSequenceMeta.CheckResult.SequenceExists.Title"), stepMeta); //$NON-NLS-1$
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AddSequenceMeta.CheckResult.SequenceCouldNotBeFound.Title",sequenceName), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AddSequenceMeta.CheckResult.UnableToConnectDB.Title")+Const.CR+e.getMessage(), stepMeta); //$NON-NLS-1$
			}
			finally
			{
				db.disconnect();
			}
			remarks.add(cr);
		}
		
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("AddSequenceMeta.CheckResult.StepIsReceving.Title"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("AddSequenceMeta.CheckResult.NoInputReceived.Title"), stepMeta); //$NON-NLS-1$
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
					if (!db.checkSequenceExists(schemaName, sequenceName))
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
					retval.setError(Messages.getString("AddSequenceMeta.ErrorMessage.UnableToConnectDB")+Const.CR+e.getMessage()); //$NON-NLS-1$
				}
				finally
				{
					db.disconnect();
				}
			}
			else
			{
				retval.setError(Messages.getString("AddSequenceMeta.ErrorMessage.NoConnectionDefined")); //$NON-NLS-1$
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
    
    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (database!=null) 
        {
            return new DatabaseMeta[] { database };
        }
        else
        {
            return super.getUsedDatabaseConnections();
        }
    }

    /**
     * @return the counterName
     */
    public String getCounterName()
    {
        return counterName;
    }

    /**
     * @param counterName the counterName to set
     */
    public void setCounterName(String counterName)
    {
        this.counterName = counterName;
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
