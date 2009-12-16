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
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepIOMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.errorhandling.Stream;
import org.pentaho.di.trans.step.errorhandling.StreamIcon;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.step.errorhandling.StreamInterface.StreamType;
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
	

	/** The targets to switch over */
	private List<SwitchCaseTarget> caseTargets;
		
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

	public void allocate()
	{
		caseTargets = new ArrayList<SwitchCaseTarget>();
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
		for (SwitchCaseTarget target : caseTargets) {
			retval.append(XMLHandler.openTag(XML_TAG_CASE_VALUE));
			retval.append(XMLHandler.addTagValue("value", target.caseValue));
			retval.append(XMLHandler.addTagValue("target_step", target.caseTargetStep!=null ? target.caseTargetStep.getName() : null));
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
			allocate();
			for (int i=0;i<nrCases;i++) {
				Node caseNode = XMLHandler.getSubNodeByNr(casesNode, XML_TAG_CASE_VALUE, i);
				SwitchCaseTarget target = new SwitchCaseTarget();
				target.caseValue = XMLHandler.getTagValue(caseNode, "value");
				target.caseTargetStepname = XMLHandler.getTagValue(caseNode, "target_step");
				caseTargets.add(target);
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception..UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		allocate();
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
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
			allocate();
			for (int i=0;i<nrCases;i++) {
				SwitchCaseTarget target = new SwitchCaseTarget();
				target.caseValue = rep.getStepAttributeString(id_step, i, "case_value");
				target.caseTargetStepname = rep.getStepAttributeString(id_step, i, "case_target_step");
				caseTargets.add(target);
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception.UnexpectedErrorInReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
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

			for (int i=0;i<caseTargets.size();i++) {
				SwitchCaseTarget target = caseTargets.get(i);
				rep.saveStepAttribute(id_transformation, id_step, i, "case_value", target.caseValue);
				rep.saveStepAttribute(id_transformation, id_step, i, "case_target_step", target.caseTargetStep!=null ? target.caseTargetStep.getName() : null);
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "SwitchCaseMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
		// Default: nothing changes to rowMeta
	}

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		StepIOMetaInterface ioMeta = getStepIOMeta();
		for (StreamInterface stream : ioMeta.getTargetStreams()) {
			SwitchCaseTarget target = (SwitchCaseTarget) stream.getSubject();
	
			if ( target.caseTargetStep==null )
			{
				cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, 
						             BaseMessages.getString(PKG, "SwitchCaseMeta.CheckResult.TargetStepInvalid", "false", target.caseTargetStepname), 
						             stepinfo);
				remarks.add(cr);
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
    
	/**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, false, false, false, false, true);

    		// Add the targets...
    		//
    		for (SwitchCaseTarget target : caseTargets) {
				StreamInterface stream = new Stream(
						StreamType.TARGET, 
						target.caseTargetStep, 
						BaseMessages.getString(PKG, "SwitchCaseMeta.TargetStream.CaseTarget.Description", Const.NVL(target.caseValue, "")), 
						StreamIcon.TARGET, 
						target
					);
				ioMeta.addStream(stream);
    		}

    		// Add the default target step as a stream
    		//
    		if (getDefaultTargetStep()!=null) {
    			ioMeta.addStream( new Stream(
    					StreamType.TARGET, 
    					getDefaultTargetStep(), 
    					BaseMessages.getString(PKG, "SwitchCaseMeta.TargetStream.Default.Description"), 
    					StreamIcon.TARGET, 
    					null
    				) );
    		}
    	}
    	
    	return ioMeta;
    }
    
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getTargetStreams()) {
			SwitchCaseTarget target = (SwitchCaseTarget) stream.getSubject();
			StepMeta stepMeta = StepMeta.findStep(steps, target.caseTargetStepname);
			target.caseTargetStep = stepMeta;
		}
		defaultTargetStep = StepMeta.findStep(steps, defaultTargetStepname);
		resetStepIoMeta();
	}
    
    private static StreamInterface newDefaultStream = new Stream(StreamType.TARGET, null, BaseMessages.getString(PKG, "SwitchCaseMeta.TargetStream.Default.Description"), StreamIcon.TARGET, null);
    private static StreamInterface newCaseTargetStream = new Stream(StreamType.TARGET, null, BaseMessages.getString(PKG, "SwitchCaseMeta.TargetStream.NewCaseTarget.Description"), StreamIcon.TARGET, null);
    
    public List<StreamInterface> getOptionalStreams() {
    	List<StreamInterface> list = new ArrayList<StreamInterface>();
    	
    	if (getDefaultTargetStep()==null) {
    		list.add( newDefaultStream );
    	}
    	list.add( newCaseTargetStream );
    	
    	return list;
    }
    
    public void handleStreamSelection(StreamInterface stream) {
    	if (stream==newDefaultStream) {
			setDefaultTargetStep(stream.getStepMeta());
    	}
    	
    	if (stream==newCaseTargetStream) {
			// Add the target..
			//
			SwitchCaseTarget target = new SwitchCaseTarget();
			target.caseTargetStep=stream.getStepMeta();
			target.caseValue = stream.getStepMeta().getName();
			caseTargets.add(target);
    	}
    	
    	List<StreamInterface> targetStreams = getStepIOMeta().getTargetStreams();
    	for (int i=0;i<targetStreams.size();i++) {
    		if (stream==targetStreams.get(i)) {
    			SwitchCaseTarget target = (SwitchCaseTarget)stream.getSubject();
    			if (target==null) { // Default! 
    				setDefaultTargetStep(stream.getStepMeta());
    			} else {
    				target.caseTargetStep = stream.getStepMeta();
    			}
    		}
    	}
    	
    	resetStepIoMeta(); // force stepIo to be recreated when it is next needed.
    }

	/**
	 * @return the caseTargets
	 */
	public List<SwitchCaseTarget> getCaseTargets() {
		return caseTargets;
	}

	/**
	 * @param caseTargets the caseTargets to set
	 */
	public void setCaseTargets(List<SwitchCaseTarget> caseTargets) {
		this.caseTargets = caseTargets;
	}

}
