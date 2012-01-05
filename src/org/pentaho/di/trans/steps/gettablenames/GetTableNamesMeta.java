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

package org.pentaho.di.trans.steps.gettablenames;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;
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

/*
 * Created on 03-Juin-2008
 * 
 */

public class GetTableNamesMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = GetTableNamesMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	
    /** database connection */
    private DatabaseMeta database;
    private String schemaname;
    /** function result: new value name */
    private String       tablenamefieldname;
    private String sqlcreationfieldname;
    private String objecttypefieldname;
    private String issystemobjectfieldname;
    
    private boolean includeCatalog;
    private boolean includeSchema;
    private boolean includeTable;
    private boolean includeView;
    private boolean includeProcedure;
    private boolean includeSynonym;
    private boolean addSchemaInOutput;
    private boolean dynamicSchema;
    private String schenameNameField;

    public GetTableNamesMeta()
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
     * @return Returns the resultName.
     */
    public String getTablenameFieldName()
    {
        return tablenamefieldname;
    }

    /**
     * @param tablenamefieldname The tablenamefieldname to set.
     */
    public void setTablenameFieldName(String tablenamefieldname)
    {
        this.tablenamefieldname = tablenamefieldname;
    }
    
    /**
     * @return Returns the resultName.
     */
    public String getSQLCreationFieldName()
    {
        return sqlcreationfieldname;
    }

    /**
     * @param sqlcreationfieldname The sqlcreationfieldname to set.
     */
    public void setSQLCreationFieldName(String sqlcreationfieldname)
    {
        this.sqlcreationfieldname = sqlcreationfieldname;
    }
    /**
     * @return Returns the resultName.
     */
    public String getSchemaName()
    {
        return schemaname;
    }

    /**
     * @param schemaname The schemaname to set.
     */
    public void setSchemaName(String schemaname)
    {
        this.schemaname = schemaname;
    }
    /**
     * @param objecttypefieldname The objecttypefieldname to set.
     */
    public void setObjectTypeFieldName(String objecttypefieldname)
    {
        this.objecttypefieldname = objecttypefieldname;
    }
    /**
     * @param issystemobjectfieldname The issystemobjectfieldname to set.
     */
    public void setIsSystemObjectFieldName(String issystemobjectfieldname)
    {
        this.issystemobjectfieldname = issystemobjectfieldname;
    }
    
    /**
     * @return Returns the objecttypefieldname.
     */
    public String getObjectTypeFieldName()
    {
        return objecttypefieldname;
    }
    /**
     * @return Returns the issystemobjectfieldname.
     */
    public String isSystemObjectFieldName()
    {
        return issystemobjectfieldname;
    }
    
    /**
     * @return Returns the schenameNameField.
     */
    public String getSchemaFieldName()
    {
        return schenameNameField;
    }
    /**
     * @param schenameNameField teh schenameNameField to set.
     */
    public void setSchemaFieldName(String schenameNameField)
    {
        this.schenameNameField= schenameNameField;
    }
    
    public void setIncludeTable(boolean includetable)
    {
    	this.includeTable=includetable;
    }
    public boolean isIncludeTable()
    {
    	return this.includeTable;
    }
    public void setIncludeSchema(boolean includeSchema)
    {
    	this.includeSchema=includeSchema;
    }
    public boolean isIncludeSchema()
    {
    	return this.includeSchema;
    }
    public void setIncludeCatalog(boolean includeCatalog)
    {
    	this.includeCatalog=includeCatalog;
    }
    public boolean isIncludeCatalog()
    {
    	return this.includeCatalog;
    }
    public void setIncludeView(boolean includeView)
    {
    	this.includeView=includeView;
    }
    public boolean isIncludeView()
    {
    	return this.includeView;
    }
    public void setIncludeProcedure(boolean includeProcedure)
    {
    	this.includeProcedure=includeProcedure;
    }
    public boolean isIncludeProcedure()
    {
    	return this.includeProcedure;
    }
    public void setIncludeSynonym(boolean includeSynonym)
    {
    	this.includeSynonym=includeSynonym;
    }
    public boolean isIncludeSynonym()
    {
    	return this.includeSynonym;
    }
    
    public void setDynamicSchema(boolean dynamicSchema)
    {
    	this.dynamicSchema=dynamicSchema;
    }
    public boolean isDynamicSchema()
    {
    	return this.dynamicSchema;
    }
    public void setAddSchemaInOut(boolean addSchemaInOutput)
    {
    	this.addSchemaInOutput=addSchemaInOutput;
    }
    public boolean isAddSchemaInOut()
    {
    	return this.addSchemaInOutput;
    }
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) 
    throws KettleXMLException {
		readData(stepnode, databases);
	}

    public Object clone()
    {
        GetTableNamesMeta retval = (GetTableNamesMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        database = null;
        schemaname=null;
        includeCatalog=false;
        includeSchema=false;
        includeTable=true;
        includeProcedure=true;
        includeView=true;
        includeSynonym=true;
        addSchemaInOutput=false;
        tablenamefieldname = "tablename"; //$NON-NLS-1$
        sqlcreationfieldname=null;
        objecttypefieldname="type";
        issystemobjectfieldname="is system";
        dynamicSchema=false;
        schenameNameField=null;
    }

	public void getFields(RowMetaInterface r, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		String realtablename=space.environmentSubstitute(tablenamefieldname);
		if (!Const.isEmpty(realtablename))
		{
			ValueMetaInterface v = new ValueMeta(realtablename, ValueMeta.TYPE_STRING); 
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		 
		String realObjectType=space.environmentSubstitute(objecttypefieldname);
		if (!Const.isEmpty(realObjectType))
		{
			ValueMetaInterface v = new ValueMeta(realObjectType, ValueMeta.TYPE_STRING); 
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		String sysobject=space.environmentSubstitute(issystemobjectfieldname);
		if (!Const.isEmpty(sysobject))
		{
			ValueMetaInterface v = new ValueMeta(sysobject, ValueMeta.TYPE_BOOLEAN); 
			v.setOrigin(name);
			r.addValueMeta(v);
		}
		
		String realSQLCreation=space.environmentSubstitute(sqlcreationfieldname);
		if (!Const.isEmpty(realSQLCreation))
		{
			ValueMetaInterface v = new ValueMeta(realSQLCreation, ValueMeta.TYPE_STRING); 
			v.setLength(500);
			v.setPrecision(-1);
			v.setOrigin(name);
			r.addValueMeta(v);
		}
    }

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("connection", database == null ? "" : database.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("    " + XMLHandler.addTagValue("schemaname", schemaname));
        retval.append("    " + XMLHandler.addTagValue("tablenamefieldname", tablenamefieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("objecttypefieldname", objecttypefieldname)); 
        retval.append("    " + XMLHandler.addTagValue("issystemobjectfieldname", issystemobjectfieldname)); 
        retval.append("    " + XMLHandler.addTagValue("sqlcreationfieldname", sqlcreationfieldname)); 
        
        
        retval.append("    " + XMLHandler.addTagValue("includeCatalog", includeCatalog));
        retval.append("    " + XMLHandler.addTagValue("includeSchema", includeSchema));
        retval.append("    " + XMLHandler.addTagValue("includeTable", includeTable));
        retval.append("    " + XMLHandler.addTagValue("includeView", includeView));
        retval.append("    " + XMLHandler.addTagValue("includeProcedure", includeProcedure));
        retval.append("    " + XMLHandler.addTagValue("includeSynonym", includeSynonym));
        retval.append("    " + XMLHandler.addTagValue("addSchemaInOutput", addSchemaInOutput));
        retval.append("    " + XMLHandler.addTagValue("dynamicSchema", dynamicSchema));
        retval.append("    " + XMLHandler.addTagValue("schenameNameField", schenameNameField));
        
        return retval.toString();
    }

	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
	{
        try
        {

            String con = XMLHandler.getTagValue(stepnode, "connection"); //$NON-NLS-1$
            database = DatabaseMeta.findDatabase(databases, con);
            schemaname = XMLHandler.getTagValue(stepnode, "schemaname"); 
            tablenamefieldname = XMLHandler.getTagValue(stepnode, "tablenamefieldname"); //$NON-NLS-1$
            objecttypefieldname = XMLHandler.getTagValue(stepnode, "objecttypefieldname"); 
            sqlcreationfieldname = XMLHandler.getTagValue(stepnode, "sqlcreationfieldname"); 
            
            issystemobjectfieldname = XMLHandler.getTagValue(stepnode, "issystemobjectfieldname"); 
            includeCatalog  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeCatalog"));
            includeSchema  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeSchema"));
            includeTable  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeTable"));
            includeView  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeView"));
            includeProcedure  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeProcedure"));
            includeSynonym  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "includeSynonym"));
            addSchemaInOutput  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "addSchemaInOutput"));
            dynamicSchema  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "dynamicSchema"));
            schenameNameField = XMLHandler.getTagValue(stepnode, "schenameNameField"); 
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) 
	throws KettleException {
		try
		{
			database = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);
			schemaname = rep.getStepAttributeString(id_step, "schemaname");
			tablenamefieldname = rep.getStepAttributeString(id_step, "tablenamefieldname"); //$NON-NLS-1$
            objecttypefieldname = rep.getStepAttributeString(id_step, "objecttypefieldname");
            sqlcreationfieldname = rep.getStepAttributeString(id_step, "sqlcreationfieldname");
            
            issystemobjectfieldname = rep.getStepAttributeString(id_step, "issystemobjectfieldname");
            includeCatalog = rep.getStepAttributeBoolean(id_step, "includeCatalog"); 
            includeSchema = rep.getStepAttributeBoolean(id_step, "includeSchema"); 
            includeTable = rep.getStepAttributeBoolean(id_step, "includeTable"); 
            includeView = rep.getStepAttributeBoolean(id_step, "includeView");
            includeProcedure = rep.getStepAttributeBoolean(id_step, "includeProcedure");
            includeSynonym = rep.getStepAttributeBoolean(id_step, "includeSynonym");
            addSchemaInOutput = rep.getStepAttributeBoolean(id_step, "addSchemaInOutput");
            dynamicSchema = rep.getStepAttributeBoolean(id_step, "dynamicSchema");
            schenameNameField = rep.getStepAttributeString(id_step, "schenameNameField");
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException
	{
        try
        {
    		rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", database);
    		rep.saveStepAttribute(id_transformation, id_step, "schemaname", schemaname);
    		rep.saveStepAttribute(id_transformation, id_step, "tablenamefieldname", tablenamefieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "objecttypefieldname", objecttypefieldname);
            rep.saveStepAttribute(id_transformation, id_step, "sqlcreationfieldname", sqlcreationfieldname);
            
            rep.saveStepAttribute(id_transformation, id_step, "issystemobjectfieldname", issystemobjectfieldname);
            // Also, save the step-database relationship!
            if (database != null) rep.insertStepDatabase(id_transformation, id_step, database.getObjectId());
            rep.saveStepAttribute(id_transformation, id_step, "includeCatalog", includeCatalog);
            rep.saveStepAttribute(id_transformation, id_step, "includeSchema", includeSchema);
            rep.saveStepAttribute(id_transformation, id_step, "includeTable", includeTable);
            rep.saveStepAttribute(id_transformation, id_step, "includeView", includeView);
            rep.saveStepAttribute(id_transformation, id_step, "includeProcedure", includeProcedure);
            rep.saveStepAttribute(id_transformation, id_step, "includeSynonym", includeSynonym);
            rep.saveStepAttribute(id_transformation, id_step, "addSchemaInOutput", addSchemaInOutput);
            rep.saveStepAttribute(id_transformation, id_step, "dynamicSchema", dynamicSchema);
            rep.saveStepAttribute(id_transformation, id_step, "schenameNameField", schenameNameField);
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "GetTableNamesMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        if (database == null)
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.InvalidConnection"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(tablenamefieldname))
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.TablenameFieldNameOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
       
        // See if we have input streams leading to this step!
        if (input.length > 0 && !isDynamicSchema())
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
        else
        	cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "GetTableNamesMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
        remarks.add(cr);   
        

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new GetTableNames(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new GetTableNamesData();
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
