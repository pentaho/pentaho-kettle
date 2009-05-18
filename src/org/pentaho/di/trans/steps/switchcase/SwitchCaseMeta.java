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

package org.pentaho.di.trans.steps.switchcase;

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
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/*
 * Created on 14-may-2008
 *
 */
public class SwitchCaseMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SwitchCaseMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static final String XML_TAG_CASE_VALUES = "cases";
	private static final String XML_TAG_CASE_VALUE = "case";
	
	/** The field to switch over */
	private String fieldname;

	/** The case value type to help parse numeric and date-time data */
	private int caseValueType;
	/** The case value format to help parse numeric and date-time data */
	private String caseValueFormat;
	/** The decimal symbol to help parse numeric data */
	private String caseValueDecimal;
	/** The grouping symbol to help parse numeric data */
	private String caseValueGroup;
	
	/** The values to switch over */
	private String[] caseValues;
	
	/** The case target step names  (only used during serialization) */
	private String[] caseTargetStepnames;
	
	/** The case target steps */
	private StepMeta[] caseTargetSteps;
	
	/** The default target step name (only used during serialization) */
	private String defaultTargetStepname;
	
	/** The default target step */
	private StepMeta defaultTargetStep;
	
	/** True if the comparison is a String.contains instead of equals */
    private boolean isContains;

	public SwitchCaseMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int nrCases)
	{
		caseValues = new String[nrCases];
		caseTargetStepnames = new String[nrCases];
		caseTargetSteps = new StepMeta[nrCases];
	}

	public Object clone()
	{
		SwitchCaseMeta retval = (SwitchCaseMeta)super.clone();

		return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer(200);

		retval.append(XMLHandler.addTagValue("fieldname", fieldname));		             //$NON-NLS-1$
        retval.append(XMLHandler.addTagValue("use_contains", isContains));                   //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("case_value_type", ValueMeta.getTypeDesc(caseValueType)));	 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("case_value_format", caseValueFormat));	 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("case_value_decimal", caseValueDecimal));	 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("case_value_group", caseValueGroup));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("default_target_step", defaultTargetStep==null ? null : defaultTargetStep.getName())); //$NON-NLS-1$
		
		retval.append(XMLHandler.openTag(XML_TAG_CASE_VALUES));
		for (int i=0;i<caseValues.length;i++) {
			retval.append(XMLHandler.openTag(XML_TAG_CASE_VALUE));
			retval.append(XMLHandler.addTagValue("value", caseValues[i]));
			retval.append(XMLHandler.addTagValue("target_step", caseTargetSteps[i]!=null ? caseTargetSteps[i].getName() : null));
			retval.append(XMLHandler.closeTag(XML_TAG_CASE_VALUE));
		}
		retval.append(XMLHandler.closeTag(XML_TAG_CASE_VALUES));

		return retval.toString();
	}

	private void readData(Node stepnode) throws KettleXMLException
	{
		try
		{
			fieldname = XMLHandler.getTagValue(stepnode, "fieldname"); //$NON-NLS-1$
            isContains = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "use_contains")); //$NON-NLS-1$
			caseValueType = ValueMeta.getType(XMLHandler.getTagValue(stepnode, "case_value_type")); //$NON-NLS-1$
			caseValueFormat = XMLHandler.getTagValue(stepnode, "case_value_format"); //$NON-NLS-1$
			caseValueDecimal = XMLHandler.getTagValue(stepnode, "case_value_decimal"); //$NON-NLS-1$
			caseValueGroup = XMLHandler.getTagValue(stepnode, "case_value_group"); //$NON-NLS-1$
			
			defaultTargetStepname = XMLHandler.getTagValue(stepnode, "default_target_step"); // $NON-NLS-1$
			
			Node casesNode = XMLHandler.getSubNode(stepnode, XML_TAG_CASE_VALUES);
			int nrCases = XMLHandler.countNodes(casesNode, XML_TAG_CASE_VALUE);
			allocate(nrCases);
			for (int i=0;i<nrCases;i++) {
				Node caseNode = XMLHandler.getSubNodeByNr(casesNode, XML_TAG_CASE_VALUE, i);
				caseValues[i] = XMLHandler.getTagValue(caseNode, "value");
				caseTargetStepnames[i] = XMLHandler.getTagValue(caseNode, "target_step");
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception..UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		allocate(0);
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
		try
		{
			fieldname        = rep.getStepAttributeString (id_step, "fieldname");  //$NON-NLS-1$
            isContains       = rep.getStepAttributeBoolean (id_step, "use_contains");  //$NON-NLS-1$
			caseValueType    = ValueMeta.getType(rep.getStepAttributeString (id_step, "case_value_type"));  //$NON-NLS-1$
			caseValueFormat  = rep.getStepAttributeString (id_step, "case_value_format");  //$NON-NLS-1$
			caseValueDecimal = rep.getStepAttributeString (id_step, "case_value_decimal");  //$NON-NLS-1$
			caseValueGroup   = rep.getStepAttributeString (id_step, "case_value_group");  //$NON-NLS-1$
			
			defaultTargetStepname = rep.getStepAttributeString(id_step, "default_target_step"); // $NON-NLS-1$
			
			int nrCases = rep.countNrStepAttributes(id_step, "case_value");
			allocate(nrCases);
			
			for (int i=0;i<nrCases;i++) {
				caseValues[i] = rep.getStepAttributeString(id_step, i, "case_value");
				caseTargetStepnames[i] = rep.getStepAttributeString(id_step, i, "case_target_step");
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step) throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "use_contains", isContains); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "case_value_type", ValueMeta.getTypeDesc(caseValueType)); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "case_value_format", caseValueFormat); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "case_value_decimal", caseValueDecimal); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "case_value_group", caseValueGroup); //$NON-NLS-1$
			
			rep.saveStepAttribute(id_transformation, id_step, "default_target_step", defaultTargetStep==null ? null : defaultTargetStep.getName());
			
			for (int i=0;i<caseValues.length;i++) {
				rep.saveStepAttribute(id_transformation, id_step, i, "case_value", caseValues[i]);
				rep.saveStepAttribute(id_transformation, id_step, i, "case_target_step", caseTargetSteps[i]!=null ? caseTargetSteps[i].getName() : null);
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}
	
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		for (int i=0;i<caseTargetStepnames.length;i++) {
			caseTargetSteps[i] = StepMeta.findStep(steps, caseTargetStepnames[i]); 
		}
		defaultTargetStep = StepMeta.findStep(steps, defaultTargetStepname);
	}

    /**
     * @return true if this step chooses both target steps
     */
	public boolean chosesTargetSteps()
	{
	    return true;
	}

	public String[] getTargetSteps()
	{
		List<String> names = new ArrayList<String>();
		for (StepMeta stepMeta : caseTargetSteps) {
			if (stepMeta!=null) {
				names.add(stepMeta.getName());
			}
		}
		if (defaultTargetStep!=null) {
			names.add(defaultTargetStep.getName());
		}
        return names.toArray(new String[names.size()]);
	}
    
    /**
     * @param targetSteps The target step(s) to set
     */
    public void setTargetSteps(StepMeta[] targetSteps)
    {
    	caseTargetSteps=targetSteps;
    }
    
	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if ( caseTargetSteps!=null )
		{
			for (String stepname : caseTargetStepnames) {
				
				int index= Const.indexOfString(stepname, output);
				if ( index < 0 )
				{
					cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
							             BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.TargetStepInvalid", "false", stepname), 
							             stepinfo);
					remarks.add(cr);
				}
			}
		}

		if (Const.isEmpty(fieldname))
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.NoFieldSpecified"), stepinfo);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.FieldSpecified"), stepinfo); //$NON-NLS-1$
		}
		remarks.add(cr);		
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new SwitchCase(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SwitchCaseData();
	}

	/**
	 * @return the fieldname
	 */
	public String getFieldname() {
		return fieldname;
	}

	/**
	 * @param fieldname the fieldname to set
	 */
	public void setFieldname(String fieldname) {
		this.fieldname = fieldname;
	}

	/**
	 * @return the caseValueFormat
	 */
	public String getCaseValueFormat() {
		return caseValueFormat;
	}

	/**
	 * @param caseValueFormat the caseValueFormat to set
	 */
	public void setCaseValueFormat(String caseValueFormat) {
		this.caseValueFormat = caseValueFormat;
	}

	/**
	 * @return the caseValueDecimal
	 */
	public String getCaseValueDecimal() {
		return caseValueDecimal;
	}

	/**
	 * @param caseValueDecimal the caseValueDecimal to set
	 */
	public void setCaseValueDecimal(String caseValueDecimal) {
		this.caseValueDecimal = caseValueDecimal;
	}

	/**
	 * @return the caseValueGroup
	 */
	public String getCaseValueGroup() {
		return caseValueGroup;
	}

	/**
	 * @param caseValueGroup the caseValueGroup to set
	 */
	public void setCaseValueGroup(String caseValueGroup) {
		this.caseValueGroup = caseValueGroup;
	}

	/**
	 * @return the caseValues
	 */
	public String[] getCaseValues() {
		return caseValues;
	}

	/**
	 * @param caseValues the caseValues to set
	 */
	public void setCaseValues(String[] caseValues) {
		this.caseValues = caseValues;
	}

	/**
	 * @return the caseTargetStepnames
	 */
	public String[] getCaseTargetStepnames() {
		return caseTargetStepnames;
	}

	/**
	 * @param caseTargetStepnames the caseTargetStepnames to set
	 */
	public void setCaseTargetStepnames(String[] caseTargetStepnames) {
		this.caseTargetStepnames = caseTargetStepnames;
	}

	/**
	 * @return the caseTargetSteps
	 */
	public StepMeta[] getCaseTargetSteps() {
		return caseTargetSteps;
	}

	/**
	 * @param caseTargetSteps the caseTargetSteps to set
	 */
	public void setCaseTargetSteps(StepMeta[] caseTargetSteps) {
		this.caseTargetSteps = caseTargetSteps;
	}

	/**
	 * @return the caseValueType
	 */
	public int getCaseValueType() {
		return caseValueType;
	}

	/**
	 * @param caseValueType the caseValueType to set
	 */
	public void setCaseValueType(int caseValueType) {
		this.caseValueType = caseValueType;
	}

	/**
	 * @return the defaultTargetStepname
	 */
	public String getDefaultTargetStepname() {
		return defaultTargetStepname;
	}

	/**
	 * @param defaultTargetStepname the defaultTargetStepname to set
	 */
	public void setDefaultTargetStepname(String defaultTargetStepname) {
		this.defaultTargetStepname = defaultTargetStepname;
	}

	/**
	 * @return the defaultTargetStep
	 */
	public StepMeta getDefaultTargetStep() {
		return defaultTargetStep;
	}

	/**
	 * @param defaultTargetStep the defaultTargetStep to set
	 */
	public void setDefaultTargetStep(StepMeta defaultTargetStep) {
		this.defaultTargetStep = defaultTargetStep;
	}

    public boolean isContains()
    {
        return isContains;
    }

    public void setContains(boolean isContains)
    {
        this.isContains = isContains;
    }
}
