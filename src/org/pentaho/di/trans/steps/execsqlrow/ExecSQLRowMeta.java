/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.execsqlrow;

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
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.sql.ExecSQL;
import org.w3c.dom.Node;


/***
 * Contains meta-data to execute arbitrary SQL from a specified field.
 * 
 * Created on 10-sep-2008
 */
 
public class ExecSQLRowMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = ExecSQLRowMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DatabaseMeta databaseMeta;
	private String       sqlField;

    private String       updateField;
    private String       insertField;
    private String       deleteField;
    private String       readField;
    
	/** Commit size for inserts/updates */
	private int    commitSize; 
	
	private boolean  sqlFromfile;
	
	public ExecSQLRowMeta()
	{
		super();
	}
    /**
     * @return Returns the sqlFromfile.
     */
    public boolean isSqlFromfile()
    {
        return sqlFromfile;
    }
    
    /**
     * @param sqlFromfile The sqlFromfile to set.
     */
    public void setSqlFromfile(boolean sqlFromfile)
    {
        this.sqlFromfile = sqlFromfile;
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
     * @return Returns the sqlField.
     */
    public String getSqlFieldName()
    {
        return sqlField;
    }

    /**
     * @param sql The sqlField to sqlField.
     */
    public void setSqlFieldName(String sqlField)
    {
        this.sqlField = sqlField;
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

    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		ExecSQLRowMeta retval = (ExecSQLRowMeta)super.clone();
		return retval;
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
		try
		{
			String csize;
			String con            = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
			databaseMeta          = DatabaseMeta.findDatabase(databases, con);
			csize      = XMLHandler.getTagValue(stepnode, "commit"); //$NON-NLS-1$
			commitSize=Const.toInt(csize, 0);
            sqlField                   = XMLHandler.getTagValue(stepnode, "sql_field"); //$NON-NLS-1$

			insertField           = XMLHandler.getTagValue(stepnode, "insert_field"); //$NON-NLS-1$
			updateField           = XMLHandler.getTagValue(stepnode, "update_field"); //$NON-NLS-1$			
			deleteField           = XMLHandler.getTagValue(stepnode, "delete_field"); //$NON-NLS-1$
			readField             = XMLHandler.getTagValue(stepnode, "read_field"); //$NON-NLS-1$
            sqlFromfile        = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "sqlFromfile"));
        }
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "ExecSQLRowMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		sqlFromfile=false;
		commitSize   = 1;
		databaseMeta = null;
		sqlField     = null; //$NON-NLS-1$
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
        retval.append("    ").append(XMLHandler.addTagValue("commit", commitSize));
		retval.append("    ").append(XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		retval.append("    ").append(XMLHandler.addTagValue("sql_field",        sqlField)); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append("    ").append(XMLHandler.addTagValue("insert_field",  insertField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("update_field",  updateField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("delete_field",  deleteField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("read_field",    readField)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("sqlFromfile",        sqlFromfile));

		return retval.toString();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
			commitSize     		= (int)rep.getStepAttributeInteger(id_step, "commit");
            sqlField              = rep.getStepAttributeString (id_step, "sql_field"); //$NON-NLS-1$

            insertField           = rep.getStepAttributeString (id_step, "insert_field"); //$NON-NLS-1$
            updateField           = rep.getStepAttributeString (id_step, "update_field"); //$NON-NLS-1$
            deleteField           = rep.getStepAttributeString (id_step, "delete_field"); //$NON-NLS-1$
            readField             = rep.getStepAttributeString (id_step, "read_field"); //$NON-NLS-1$
            sqlFromfile              = rep.getStepAttributeBoolean(id_step, "sqlFromfile");
           
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "ExecSQLRowMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "commit",        commitSize);
			rep.saveStepAttribute(id_transformation, id_step, "sql_field",             sqlField); //$NON-NLS-1$
 
            rep.saveStepAttribute(id_transformation, id_step, "insert_field", insertField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "update_field", updateField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "delete_field", deleteField); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "read_field",   readField); //$NON-NLS-1$
            
			// Also, save the step-database relationship!
			if (databaseMeta!=null) rep.insertStepDatabase(id_transformation, id_step, databaseMeta.getObjectId());
			
			rep.saveStepAttribute(id_transformation, id_step, "sqlFromfile",             sqlFromfile);
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "ExecSQLRowMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.ConnectionExists"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);

			Database db = new Database(loggingObject, databaseMeta);
            databases = new Database[] { db }; // keep track of it for cancelling purposes...

			try
			{
				db.connect();
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.DBConnectionOK"), stepMeta); //$NON-NLS-1$
				remarks.add(cr);

				if (sqlField!=null && sqlField.length()!=0)
					cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.SQLFieldNameEntered"), stepMeta); //$NON-NLS-1$
				else
					cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.SQLFieldNameMissing"), stepMeta); //$NON-NLS-1$
					remarks.add(cr);
			}
			catch(KettleException e)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.ErrorOccurred")+e.getMessage(), stepMeta); //$NON-NLS-1$
				remarks.add(cr);
			}
			finally
			{
				db.disconnect();
			}
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.ConnectionNeeded"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}

            if (input.length>0)
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.StepReceivingInfoOK"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
            else
            {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ExecSQLRowMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
                remarks.add(cr);
            }
      
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ExecSQLRow(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ExecSQLRowData();
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
