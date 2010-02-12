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

package org.pentaho.di.trans.steps.sql;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
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




/*******************************************************************************
 * Contains meta-data to execute arbitrary SQL, optionally each row again.
 * 
 * Created on 10-sep-2005
 */

public class ExecSQLMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = ExecSQLMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DatabaseMeta databaseMeta;

	private String sql;

	private boolean executedEachInputRow;

	private String[] arguments;

	private String updateField;

	private String insertField;

	private String deleteField;

	private String readField;
	
	private boolean singleStatement;
    
  private boolean replaceVariables;

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
	 * @param database
	 *            The database to set.
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
	 * @param sql
	 *            The sql to set.
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
	 * @param arguments
	 *            The arguments to set.
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
	 * @param executedEachInputRow
	 *            The executedEachInputRow to set.
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
	 * @param deleteField
	 *            The deleteField to set.
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
	 * @param insertField
	 *            The insertField to set.
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
	 * @param readField
	 *            The readField to set.
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
	 * @param updateField
	 *            The updateField to set.
	 */
	public void setUpdateField(String updateField)
	{
		this.updateField = updateField;
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		ExecSQLMeta retval = (ExecSQLMeta) super.clone();
		return retval;
	}

	public void allocate(int nrargs)
	{
		arguments = new String[nrargs];
	}

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
		try
		{
			String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta = DatabaseMeta.findDatabase(databases, con);
			String eachRow = XMLHandler.getTagValue(stepnode, "execute_each_row"); //$NON-NLS-1$
			executedEachInputRow = "Y".equalsIgnoreCase(eachRow); //$NON-NLS-1$
			singleStatement = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "single_statement")); //$NON-NLS-1$
			replaceVariables = "Y".equals(XMLHandler.getTagValue(stepnode, "replace_variables"));
			sql = XMLHandler.getTagValue(stepnode, "sql"); //$NON-NLS-1$

			insertField = XMLHandler.getTagValue(stepnode, "insert_field"); //$NON-NLS-1$
			updateField = XMLHandler.getTagValue(stepnode, "update_field"); //$NON-NLS-1$			
			deleteField = XMLHandler.getTagValue(stepnode, "delete_field"); //$NON-NLS-1$
			readField = XMLHandler.getTagValue(stepnode, "read_field"); //$NON-NLS-1$

			Node argsnode = XMLHandler.getSubNode(stepnode, "arguments"); //$NON-NLS-1$
			int nrArguments = XMLHandler.countNodes(argsnode, "argument"); //$NON-NLS-1$
			allocate(nrArguments);
			for (int i = 0; i < nrArguments; i++)
			{
				Node argnode = XMLHandler.getSubNodeByNr(argsnode, "argument", i); //$NON-NLS-1$
				arguments[i] = XMLHandler.getTagValue(argnode, "name"); //$NON-NLS-1$
			}
		} catch (Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "ExecSQLMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		databaseMeta = null;
		sql = ""; //$NON-NLS-1$
		arguments = new String[0];
	}

	public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		RowMetaAndData add = ExecSQL.getResultRow(new Result(), getUpdateField(), getInsertField(), getDeleteField(),
				getReadField());
		
		r.mergeRowMeta(add.getRowMeta());
	}

	public String getXML()
	{
		StringBuffer retval = new StringBuffer(300);

		retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta == null ? "" : databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("execute_each_row", executedEachInputRow)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("single_statement", singleStatement)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("replace_variables",   replaceVariables));        
		retval.append("    ").append(XMLHandler.addTagValue("sql", sql)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    ").append(XMLHandler.addTagValue("insert_field", insertField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("update_field", updateField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("delete_field", deleteField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("read_field", readField)); //$NON-NLS-1$ //$NON-NLS-2$

		retval.append("    <arguments>").append(Const.CR); //$NON-NLS-1$
		for (int i = 0; i < arguments.length; i++)
		{
			retval.append("       <argument>").append(XMLHandler.addTagValue("name", arguments[i], false)).append("</argument>").append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		retval.append("    </arguments>").append(Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
			executedEachInputRow = rep.getStepAttributeBoolean(id_step, "execute_each_row"); //$NON-NLS-1$
			singleStatement = rep.getStepAttributeBoolean(id_step, "single_statement"); //$NON-NLS-1$
			replaceVariables = rep.getStepAttributeBoolean(id_step, "replace_variables"); //$NON-NLS-1$
			sql = rep.getStepAttributeString(id_step, "sql"); //$NON-NLS-1$

			insertField = rep.getStepAttributeString(id_step, "insert_field"); //$NON-NLS-1$
			updateField = rep.getStepAttributeString(id_step, "update_field"); //$NON-NLS-1$
			deleteField = rep.getStepAttributeString(id_step, "delete_field"); //$NON-NLS-1$
			readField = rep.getStepAttributeString(id_step, "read_field"); //$NON-NLS-1$

			int nrargs = rep.countNrStepAttributes(id_step, "arg_name"); //$NON-NLS-1$
			allocate(nrargs);

			for (int i = 0; i < nrargs; i++)
			{
				arguments[i] = rep.getStepAttributeString(id_step, i, "arg_name"); //$NON-NLS-1$
			}
		} catch (Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "ExecSQLMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "sql", sql); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "execute_each_row", executedEachInputRow); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "single_statement", singleStatement); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "replace_variables", replaceVariables); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step, "insert_field", insertField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "update_field", updateField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "delete_field", deleteField); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "read_field", readField); //$NON-NLS-1$

			// Also, save the step-database relationship!
			if (databaseMeta != null)
				rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());

			for (int i = 0; i < arguments.length; i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "arg_name", arguments[i]); //$NON-NLS-1$
			}
		} catch (Exception e)
		{
			throw new KettleException(
					BaseMessages.getString(PKG, "ExecSQLMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
		}
	}
	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		if (databaseMeta != null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.ConnectionExists"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			Database db = new Database(loggingObject, databaseMeta);
			db.shareVariablesWith(transMeta);
			databases = new Database[] { db }; // keep track of it for
												// cancelling purposes...

			try
			{
				db.connect();
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.DBConnectionOK"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);

				if (sql != null && sql.length() != 0)
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.SQLStatementEntered"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				} else
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.SQLStatementMissing"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
				}
			} catch (KettleException e)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.ErrorOccurred") + e.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			} finally
			{
				db.disconnect();
			}
		} else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.ConnectionNeeded"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}

		// If it's executed each row, make sure we have input
		if (executedEachInputRow)
		{
			if (input.length > 0)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.StepReceivingInfoOK"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			} else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		} else
		{
			if (input.length > 0)
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.SQLOnlyExecutedOnce"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			} else
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLMeta.CheckResult.InputReceivedOKForSQLOnlyExecuteOnce"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr,
			TransMeta transMeta, Trans trans)
	{
		return new ExecSQL(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExecSQLData();
	}

	public void analyseImpact(List<DatabaseImpact> impact, TransMeta transMeta, StepMeta stepMeta, RowMeta prev,
			String input[], String output[], RowMeta info) throws KettleStepException
	{
		DatabaseImpact ii = new DatabaseImpact(DatabaseImpact.TYPE_IMPACT_READ_WRITE, transMeta.getName(),
				stepMeta.getName(), databaseMeta.getDatabaseName(), 
				BaseMessages.getString(PKG, "ExecSQLMeta.DatabaseMeta.Unknown.Label"), //$NON-NLS-1$
				BaseMessages.getString(PKG, "ExecSQLMeta.DatabaseMeta.Unknown2.Label"), //$NON-NLS-1$
				BaseMessages.getString(PKG, "ExecSQLMeta.DatabaseMeta.Unknown3.Label"), //$NON-NLS-1$
				stepMeta.getName(), sql, BaseMessages.getString(PKG, "ExecSQLMeta.DatabaseMeta.Title") //$NON-NLS-1$
		);
		impact.add(ii);
	}

	public DatabaseMeta[] getUsedDatabaseConnections()
	{
		if (databaseMeta != null)
		{
			return new DatabaseMeta[] { databaseMeta };
		} else
		{
			return super.getUsedDatabaseConnections();
		}
	}
	
    /**
     * @return Returns the variableReplacementActive.
     */
    public boolean isReplaceVariables()
    {
        return replaceVariables;
    }

    /**
     * @param variableReplacementActive The variableReplacementActive to set.
     */
    public void setVariableReplacementActive(boolean variableReplacementActive)
    {
        this.replaceVariables = variableReplacementActive;
    }
    public boolean supportsErrorHandling()
    {
        return true;
    }

	/**
	 * @return the singleStatement
	 */
	public boolean isSingleStatement() {
		return singleStatement;
	}

	/**
	 * @param singleStatement the singleStatement to set
	 */
	public void setSingleStatement(boolean singleStatement) {
		this.singleStatement = singleStatement;
	}
}
