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


package org.pentaho.di.trans.steps.sapinput;

import java.util.ArrayList;
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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.sapinput.sap.SAPFunction;
import org.w3c.dom.Node;

/*
 * Created on 2-jun-2003
 *
 */
public class SapInputMeta extends BaseStepMeta implements StepMetaInterface
{
	private static final String	XML_TAG_PARAMETERS	= "parameters";

	private static final String	XML_TAG_PARAMETER	= "parameter";

	private static final String	XML_TAG_FIELDS	= "fields";

	private static final String	XML_TAG_FIELD	= "field";

	private static final String	XML_TAG_FUNCTION	= "function";

	/**
	 * The connection to the database
	 */
	private DatabaseMeta databaseMeta;
	
	/**
	 * The function to use
	 */
	private SAPFunction function;
	
	/**
	 * The list of SAP parameters
	 */
	private List<SapParameter> parameters;
	
	/**
	 * The list of output fields
	 */
	private List<SapOutputField> outputFields;
	
	public SapInputMeta()
	{
		super();
		
		parameters = new ArrayList<SapParameter>();
		outputFields = new ArrayList<SapOutputField>();
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
	 * @return Returns the function.
	 */
	public SAPFunction getFunction()
	{
		return function;
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode, databases);
	}

	public Object clone()
	{
		SapInputMeta retval = (SapInputMeta)super.clone();
		return retval;
	}
	
	public void setDefault()
	{
		databaseMeta = null;
		function  = null;
		
		
	}

    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException 
    {
    	row.clear(); // TODO: add an option to also include the input data...
    	
    	for (SapOutputField field : outputFields) {
    		
    		ValueMetaInterface valueMeta = new ValueMeta(field.getNewName(), field.getTargetType());
    		valueMeta.setOrigin(origin);
    		row.addValueMeta(valueMeta);
    	}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName()));
		
		retval.append("    ").append(XMLHandler.openTag(XML_TAG_FUNCTION)).append(Const.CR);
		if (function!=null && !Const.isEmpty(function.getName())) {
			retval.append("    "+XMLHandler.addTagValue("name", function.getName()));
			retval.append("    "+XMLHandler.addTagValue("description", function.getDescription()));
			retval.append("    "+XMLHandler.addTagValue("group", function.getGroup()));
			retval.append("    "+XMLHandler.addTagValue("application", function.getApplication()));
			retval.append("    "+XMLHandler.addTagValue("host", function.getHost()));
		}
		retval.append("    ").append(XMLHandler.closeTag(XML_TAG_FUNCTION)).append(Const.CR);

		retval.append("    ").append(XMLHandler.openTag(XML_TAG_PARAMETERS)).append(Const.CR);
		for (SapParameter parameter : parameters) {
			retval.append("    ").append(XMLHandler.openTag(XML_TAG_PARAMETER));
			retval.append(XMLHandler.addTagValue("field_name", parameter.getFieldName(), false));
			retval.append(XMLHandler.addTagValue("sap_type", parameter.getSapType().getCode(), false));
			retval.append(XMLHandler.addTagValue("table_name", parameter.getTableName(), false));
			retval.append(XMLHandler.addTagValue("parameter_name", parameter.getParameterName(), false));
			retval.append(XMLHandler.addTagValue("target_type", ValueMeta.getTypeDesc(parameter.getTargetType()), false));
			retval.append("    ").append(XMLHandler.closeTag(XML_TAG_PARAMETER)).append(Const.CR);
		}
		retval.append("    ").append(XMLHandler.closeTag(XML_TAG_PARAMETERS)).append(Const.CR);
	    
		retval.append("    ").append(XMLHandler.openTag(XML_TAG_FIELDS)).append(Const.CR);
		for (SapOutputField parameter : outputFields) {
			retval.append("    ").append(XMLHandler.openTag(XML_TAG_FIELD));
			retval.append(XMLHandler.addTagValue("field_name", parameter.getSapFieldName(), false));
			retval.append(XMLHandler.addTagValue("sap_type", parameter.getSapType().getCode(), false));
			retval.append(XMLHandler.addTagValue("table_name", parameter.getTableName(), false));
			retval.append(XMLHandler.addTagValue("new_name", parameter.getNewName(), false));
			retval.append(XMLHandler.addTagValue("target_type", ValueMeta.getTypeDesc(parameter.getTargetType()), false));
			retval.append("    ").append(XMLHandler.closeTag(XML_TAG_FIELD)).append(Const.CR);
		}
		retval.append("    ").append(XMLHandler.closeTag(XML_TAG_FIELDS)).append(Const.CR);

		return retval.toString();
	}
	
	private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try {
			databaseMeta = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(stepnode, "connection"));
			
			String functionName = XMLHandler.getTagValue(stepnode, XML_TAG_FUNCTION, "name");
			String functionDescription = XMLHandler.getTagValue(stepnode, XML_TAG_FUNCTION, "description");
			String functionGroup = XMLHandler.getTagValue(stepnode, XML_TAG_FUNCTION, "group");
			String functionApplication = XMLHandler.getTagValue(stepnode, XML_TAG_FUNCTION, "application");
			String functionHost = XMLHandler.getTagValue(stepnode, XML_TAG_FUNCTION, "host");
			
			if (!Const.isEmpty(functionName)) {
				function = new SAPFunction(functionName, functionDescription, functionGroup, functionApplication, functionHost);
			} else {
				function = null;
			}
			
			Node paramsNode = XMLHandler.getSubNode(stepnode, XML_TAG_PARAMETERS);
			int nrParameters = XMLHandler.countNodes(paramsNode, XML_TAG_PARAMETER);
			for (int i=0;i<nrParameters;i++) {
				Node paramNode = XMLHandler.getSubNodeByNr(paramsNode, XML_TAG_PARAMETER, i);
				String fieldName = XMLHandler.getTagValue(paramNode, "field_name");
				SapType sapType = SapType.findTypeForCode( XMLHandler.getTagValue(paramNode, "sap_type") );
				String tableName = XMLHandler.getTagValue(paramNode, "table_name");
				int targetType = ValueMeta.getType( XMLHandler.getTagValue(paramNode, "target_type") );
				String parameterName = XMLHandler.getTagValue(paramNode, "parameter_name");
				parameters.add( new SapParameter(fieldName, sapType, tableName, parameterName, targetType) );
			}

			Node fieldsNode = XMLHandler.getSubNode(stepnode, XML_TAG_FIELDS);
			int nrFields = XMLHandler.countNodes(fieldsNode, XML_TAG_FIELD);
			for (int i=0;i<nrFields;i++) {
				Node fieldNode = XMLHandler.getSubNodeByNr(fieldsNode, XML_TAG_FIELD, i);
				String sapFieldName = XMLHandler.getTagValue(fieldNode, "field_name");
				SapType sapType = SapType.findTypeForCode( XMLHandler.getTagValue(fieldNode, "sap_type") );
				String tableName = XMLHandler.getTagValue(fieldNode, "table_name");
				int targetType = ValueMeta.getType( XMLHandler.getTagValue(fieldNode, "target_type") );
				String newName = XMLHandler.getTagValue(fieldNode, "new_name");
				outputFields.add( new SapOutputField(sapFieldName, sapType, tableName, newName, targetType) );
			}

		} catch (Exception e) {
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	{
		try
		{
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			if (function!=null && !Const.isEmpty(function.getName())) {
				rep.saveStepAttribute(id_transformation, id_step, "function_name", function.getName());
				rep.saveStepAttribute(id_transformation, id_step, "function_description", function.getDescription());
				rep.saveStepAttribute(id_transformation, id_step, "function_group", function.getGroup());
				rep.saveStepAttribute(id_transformation, id_step, "function_application", function.getApplication());
				rep.saveStepAttribute(id_transformation, id_step, "function_host", function.getHost());
			}
			
			for (int i=0;i<parameters.size();i++) {
				SapParameter parameter = parameters.get(i);
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_field_name", parameter.getFieldName());
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_sap_type", parameter.getSapType()==null ? null : parameter.getSapType().getCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_table_name", parameter.getTableName());
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_name", parameter.getParameterName());
				rep.saveStepAttribute(id_transformation, id_step, i, "parameter_target_type", ValueMeta.getTypeDesc( parameter.getTargetType()));
			}
			
			for (int i=0;i<outputFields.size();i++) {
				SapOutputField field = outputFields.get(i);
				rep.saveStepAttribute(id_transformation, id_step, i, "field_sap_field_name", field.getSapFieldName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_sap_type", field.getSapType()==null ? null : field.getSapType().getCode());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_table_name", field.getTableName());
				rep.saveStepAttribute(id_transformation, id_step, i, "field_target_type", ValueMeta.getTypeDesc( field.getTargetType()));
				rep.saveStepAttribute(id_transformation, id_step, i, "field_new_name", field.getNewName());
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);  //$NON-NLS-1$
			
			String functionName = rep.getStepAttributeString(id_step, "function_name");
			String functionDescription = rep.getStepAttributeString(id_step, "function_description");
			String functionGroup = rep.getStepAttributeString(id_step, "function_group");
			String functionApplication = rep.getStepAttributeString(id_step, "function_application");
			String functionHost = rep.getStepAttributeString(id_step, "function_host");

			if (!Const.isEmpty(functionName)) {
				function = new SAPFunction(functionName, functionDescription, functionGroup, functionApplication, functionHost);
			} else {
				function = null;
			}
			
			int nrParameters = rep.countNrStepAttributes(id_step, "parameter_field_name");
			for (int i=0;i<nrParameters;i++) {
				String fieldName = rep.getStepAttributeString(id_step, i, "parameter_field_name");
				SapType sapType = SapType.findTypeForCode( rep.getStepAttributeString(id_step, i, "parameter_sap_type") );
				String tableName = rep.getStepAttributeString(id_step, i, "parameter_table_name");
				int targetType = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "parameter_target_type") );
				String parameterName = rep.getStepAttributeString(id_step, i, "parameter_name");
				parameters.add( new SapParameter(fieldName, sapType, tableName, parameterName, targetType) );
			}

			int nrFields = rep.countNrStepAttributes(id_step, "field_sap_field_name");
			for (int i=0;i<nrFields;i++) {
				String sapFieldName = rep.getStepAttributeString(id_step, i, "field_sap_field_name");
				SapType sapType = SapType.findTypeForCode( rep.getStepAttributeString(id_step, i, "field_sap_type") );
				String tableName = rep.getStepAttributeString(id_step, i, "field_table_name");
				int targetType = ValueMeta.getType( rep.getStepAttributeString(id_step, i, "field_target_type") );
				String newName = rep.getStepAttributeString(id_step, i, "field_new_name");
				outputFields.add( new SapOutputField(sapFieldName, sapType, tableName, newName, targetType) );
			}
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (databaseMeta!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Connection exists", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Please select or create a connection to use", stepMeta);
			remarks.add(cr);
		}
		
		if (function!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Function selected", stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Please select a function to use", stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SapInput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SapInputData();
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

	/**
	 * @return the parameters
	 */
	public List<SapParameter> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(List<SapParameter> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the outputFields
	 */
	public List<SapOutputField> getOutputFields() {
		return outputFields;
	}

	/**
	 * @param outputFields the outputFields to set
	 */
	public void setOutputFields(List<SapOutputField> outputFields) {
		this.outputFields = outputFields;
	}

	/**
	 * @param function the function to set
	 */
	public void setFunction(SAPFunction function) {
		this.function = function;
	}
}
