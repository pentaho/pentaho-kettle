/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.columnexists;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
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
import org.w3c.dom.Node;

/*
 * Created on 03-Juin-2008
 * 
 */

public class ColumnExistsMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = ColumnExistsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** database connection */
    private DatabaseMeta database;
    
    private String schemaname;
    
    private String tablename;

    /** dynamic tablename */
    private String       tablenamefield;
    
    /** dynamic columnname */
    private String columnnamefield;


    /** function result: new value name */
    private String       resultfieldname;

    private boolean istablenameInfield;

    public ColumnExistsMeta()
    {
        super(); // allocate BaseStepMeta
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
     * @return Returns the tablenamefield.
     */
    public String getDynamicTablenameField()
    {
        return tablenamefield;
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
     * @return Returns the schemaname.
     */
    public String getSchemaname()
    {
        return schemaname;
    }
    /**
     * @param schemaname The schemaname to set.
     */
    public void setSchemaname(String schemaname)
    {
        this.schemaname = schemaname;
    }
    
    /**
     * @param tablenamefield The tablenamefield to set.
     */
    public void setDynamicTablenameField(String tablenamefield)
    {
        this.tablenamefield = tablenamefield;
    }

    /**
     * @return Returns the columnnamefield.
     */
    public String getDynamicColumnnameField()
    {
        return columnnamefield;
    }
    /**
     * @param columnnamefield The columnnamefield to set.
     */
    public void setDynamicColumnnameField(String columnnamefield)
    {
        this.columnnamefield = columnnamefield;
    }

    /**
     * @return Returns the resultName.
     */
    public String getResultFieldName()
    {
        return resultfieldname;
    }

    /**
     * @param resultfieldname The resultfieldname to set.
     */
    public void setResultFieldName(String resultfieldname)
    {
        this.resultfieldname = resultfieldname;
    }

    public boolean isTablenameInField()
    {
    	return istablenameInfield;
    }
    /**
     * @param istablenameInfieldin the istablenameInfield to set
     */
    public void setTablenameInField(boolean istablenameInfield)
    {
    	this.istablenameInfield=istablenameInfield;
    }

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode, databases);
    }

    public Object clone()
    {
        ColumnExistsMeta retval = (ColumnExistsMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        database = null;
        schemaname=null;
        tablename=null; 
        istablenameInfield=false;
        resultfieldname = "result"; //$NON-NLS-1$
    }
	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{    	
        // Output field (String)
		 if (!Const.isEmpty(resultfieldname))
	     {
			 ValueMetaInterface v = new ValueMeta(space.environmentSubstitute(resultfieldname), ValueMeta.TYPE_BOOLEAN);
			 v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("    " + XMLHandler.addTagValue("connection", database == null ? "" : database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    " + XMLHandler.addTagValue("tablename", tablename));
        retval.append("    " + XMLHandler.addTagValue("schemaname", schemaname));
        retval.append("    " + XMLHandler.addTagValue("istablenameInfield",   istablenameInfield));
        retval.append("    " + XMLHandler.addTagValue("tablenamefield", tablenamefield)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("columnnamefield", columnnamefield)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        return retval.toString();
    }

    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases)
	throws KettleXMLException
	{
	try
	{
            String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
            database = DatabaseMeta.findDatabase(databases, con);
            tablename = XMLHandler.getTagValue(stepnode, "tablename");
            schemaname = XMLHandler.getTagValue(stepnode, "schemaname");
            istablenameInfield = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "istablenameInfield"));
            tablenamefield = XMLHandler.getTagValue(stepnode, "tablenamefield"); //$NON-NLS-1$
            columnnamefield = XMLHandler.getTagValue(stepnode, "columnnamefield"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname"); // Optional, can be null //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "ColumnExistsMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			database = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
            tablename = rep.getStepAttributeString(id_step, "tablename");
            schemaname = rep.getStepAttributeString(id_step, "schemaname");
            istablenameInfield =  rep.getStepAttributeBoolean(id_step, "istablenameInfield");
            tablenamefield = rep.getStepAttributeString(id_step, "tablenamefield"); //$NON-NLS-1$
            columnnamefield = rep.getStepAttributeString(id_step, "columnnamefield"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "ColumnExistsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
        	rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", database);
            rep.saveStepAttribute(id_transformation, id_step, "tablename", tablename);
            rep.saveStepAttribute(id_transformation, id_step, "schemaname", schemaname);
            rep.saveStepAttribute(id_transformation, id_step, "istablenameInfield",    istablenameInfield);
            rep.saveStepAttribute(id_transformation, id_step, "tablenamefield", tablenamefield); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "columnnamefield", columnnamefield); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$

            // Also, save the step-database relationship!
            if (database != null) rep.insertStepDatabase(id_transformation, id_step, database.getObjectId());
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "ColumnExistsMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        if (database == null)
        {
            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(resultfieldname))
        {
            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
        }
        remarks.add(cr);
        if(istablenameInfield)
        {
	        if (Const.isEmpty(tablenamefield))
	        {
	            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.TableFieldMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
	        }
	        else
	        {
	            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.TableFieldOK"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
	        }
            remarks.add(cr);
        }else
        {
	        if (Const.isEmpty(tablename))
	        {
	            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.TablenameMissing"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
	        }
	        else
	        {
	            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.TablenameOK"); //$NON-NLS-1$
	            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
	        }
            remarks.add(cr);
        }
        
        if (Const.isEmpty(columnnamefield))
        {
            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.ColumnNameFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.ColumnNameFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
        }
        remarks.add(cr);
        
        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ColumnExistsMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
        }
        remarks.add(cr);
    }
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new ColumnExists(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new ColumnExistsData();
    }

    public DatabaseMeta[] getUsedDatabaseConnections()
    {
        if (database != null)
        {
            return new DatabaseMeta[] { database };
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
