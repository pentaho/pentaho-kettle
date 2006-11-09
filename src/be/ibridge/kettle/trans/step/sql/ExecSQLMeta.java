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


package be.ibridge.kettle.trans.step.sql;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.exception.KettleXMLException;
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


/***
 * Contains meta-data to execute arbitrary SQL, optionally each row again.
 * 
 * Created on 10-sep-2005
 */
 
public class ExecSQLMeta extends BaseStepMeta implements StepMetaInterface
{
	private DatabaseMeta databaseMeta;
	private String       sql;

    private boolean      executedEachInputRow;
    private String[]     arguments;
    
    private String       updateField;
    private String       insertField;
    private String       deleteField;
    private String       readField;
	
	public ExecSQLMeta()
	{
		super();
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

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return Returns the executedEachInputRow.
     */
    public boolean isExecutedEachInputRow()
    {
        return executedEachInputRow;
    }

    /**
     * @param executedEachInputRow The executedEachInputRow to set.
     */
    public void setExecutedEachInputRow(boolean executedEachInputRow)
    {
        this.executedEachInputRow = executedEachInputRow;
    }
    
    
    


    /**
     * @return Returns the deleteField.
     */
    public String getDeleteField()
    {
        return deleteField;
    }

    /**
     * @param deleteField The deleteField to set.
     */
    public void setDeleteField(String deleteField)
    {
        this.deleteField = deleteField;
    }

    /**
     * @return Returns the insertField.
     */
    public String getInsertField()
    {
        return insertField;
    }

    /**
     * @param insertField The insertField to set.
     */
    public void setInsertField(String insertField)
    {
        this.insertField = insertField;
    }

    /**
     * @return Returns the readField.
     */
    public String getReadField()
    {
        return readField;
    }

    /**
     * @param readField The readField to set.
     */
    public void setReadField(String readField)
    {
        this.readField = readField;
    }

    /**
     * @return Returns the updateField.
     */
    public String getUpdateField()
    {
        return updateField;
    }

    /**
     * @param updateField The updateField to set.
     */
    public void setUpdateField(String updateField)
    {
        this.updateField = updateField;
    }

    public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		ExecSQLMeta retval = (ExecSQLMeta)super.clone();
		return retval;
	}
    
    public void allocate(int nrargs)
    {
        arguments          = new String[nrargs];
    }


	
	private void readData(Node stepnode, ArrayList databases)
		throws KettleXMLException
	{
		try
		{
			String con            = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta          = Const.findDatabase(databases, con);
            String eachRow        = XMLHandler.getTagValue(stepnode, "execute_each_row"); //$NON-NLS-1$
            executedEachInputRow  = "Y".equalsIgnoreCase( eachRow ); //$NON-NLS-1$
            System.out.println(Messages.getString("ExecSQLMeta.Log.ExecutedEachRow",executedEachInputRow+"")+eachRow+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			sql                   = XMLHandler.getTagValue(stepnode, "sql"); //$NON-NLS-1$

            Node argsnode = XMLHandler.getSubNode(stepnode, "arguments"); //$NON-NLS-1$
            int nrArguments = XMLHandler.countNodes(argsnode, "argument"); //$NON-NLS-1$
            allocate(nrArguments);
            for (int i=0;i<nrArguments;i++)
            {
                Node argnode = XMLHandler.getSubNodeByNr(argsnode, "argument", i); //$NON-NLS-1$
                arguments[i] = XMLHandler.getTagValue(argnode, "name"); //$NON-NLS-1$
            }
        }
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("ExecSQLMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		databaseMeta = null;
		sql          = ""; //$NON-NLS-1$
        arguments    = new String[0];
	}

	public Row getFields(Row r, String name, Row info)
		throws KettleStepException
	{
		Row row;
				
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...
		
		if (databaseMeta==null) return row;
        
		Row add = ExecSQL.getResultRow(new Result(), getUpdateField(), getInsertField(), getDeleteField(), getReadField());
		row.addRow(add);
	
		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    "+XMLHandler.addTagValue("execute_each_row", executedEachInputRow)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    "+XMLHandler.addTagValue("sql",        sql)); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append("    "+XMLHandler.addTagValue("insert_field",  insertField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("update_field",  updateField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("delete_field",  deleteField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("read_field",    readField)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("    <arguments>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<arguments.length;i++)
        {
            retval.append("       <argument>"+XMLHandler.addTagValue("name", arguments[i], false)+"</argument>"+Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        retval.append("    </arguments>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
			long id_connection    = rep.getStepAttributeInteger(id_step, "id_connection");  //$NON-NLS-1$
			databaseMeta          = Const.findDatabase( databases, id_connection);
            executedEachInputRow  = rep.getStepAttributeBoolean(id_step, "execute_each_row"); //$NON-NLS-1$
            sql                   = rep.getStepAttributeString (id_step, "sql"); //$NON-NLS-1$

            insertField           = rep.getStepAttributeString (id_step, "insert_field"); //$NON-NLS-1$
            updateField           = rep.getStepAttributeString (id_step, "update_field"); //$NON-NLS-1$
            deleteField           = rep.getStepAttributeString (id_step, "delete_field"); //$NON-NLS-1$
            readField             = rep.getStepAttributeString (id_step, "read_field"); //$NON-NLS-1$
            
            int nrargs = rep.countNrStepAttributes(id_step, "arg_name"); //$NON-NLS-1$
            allocate(nrargs);
            
            for (int i=0;i<nrargs;i++)
            {
                arguments[i]      = rep.getStepAttributeString(id_step, i, "arg_name"); //$NON-NLS-1$
            }
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ExecSQLMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "id_connection",   databaseMeta==null?-1:databaseMeta.getID()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "sql",             sql); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "execute_each_row", executedEachInputRow); //$NON-NLS-1$
            
            rep.saveStepAttribute(id_transformation, id_step, "insert_field", insertField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "update_field", updateField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "delete_field", deleteField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "read_field",   readField); //$NON-NLS-1$
            
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getID());
            
            for (int i=0;i<arguments.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", arguments[i]); //$NON-NLS-1$
            }
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ExecSQLMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(ArrayList remarks, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExecSQLMeta.CheckResult.ConnectionExists"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			Database db = new Database(databaseMeta);
            databases = new Database[] { db }; // keep track of it for cancelling purposes...

			try
			{
				db.connect();
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExecSQLMeta.CheckResult.DBConnectionOK"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);

				if (sql!=null && sql.length()!=0)
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExecSQLMeta.CheckResult.SQLStatementEntered"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				}
				else
				{
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExecSQLMeta.CheckResult.SQLStatementMissing"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				}
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExecSQLMeta.CheckResult.ErrorOccurred")+e.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExecSQLMeta.CheckResult.ConnectionNeeded"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
        
        // If it's executed each row, make sure we have input
        if (executedEachInputRow)
        {
            if (input.length>0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExecSQLMeta.CheckResult.StepReceivingInfoOK"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExecSQLMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
        else
        {
            if (input.length>0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ExecSQLMeta.CheckResult.SQLOnlyExecutedOnce"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ExecSQLMeta.CheckResult.InputReceivedOKForSQLOnlyExecuteOnce"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
        }
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ExecSQLDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ExecSQL(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExecSQLData();
	}

	public void analyseImpact(ArrayList impact, TransMeta transMeta, StepMeta stepMeta, Row prev, String input[], String output[], Row info)
		throws KettleStepException
	{
		DatabaseImpact ii = new DatabaseImpact( DatabaseImpact.TYPE_IMPACT_READ_WRITE, 
										transMeta.getName(),
										stepMeta.getName(),
										databaseMeta.getDatabaseName(), 
										Messages.getString("ExecSQLMeta.DatabaseMeta.Unknown.Label"),  //$NON-NLS-1$
										Messages.getString("ExecSQLMeta.DatabaseMeta.Unknown2.Label"), //$NON-NLS-1$
										Messages.getString("ExecSQLMeta.DatabaseMeta.Unknown3.Label"), //$NON-NLS-1$
										stepMeta.getName(),
										sql,
										Messages.getString("ExecSQLMeta.DatabaseMeta.Title") //$NON-NLS-1$
										);
		impact.add(ii);
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

}
