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
 
package org.pentaho.di.trans.steps.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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



/**
 * Contains the meta-data for the Validator step: calculates predefined formula's
 * 
 * Created on 08-sep-2005
 */

public class ValidatorMeta extends BaseStepMeta implements StepMetaInterface
{	
	private static Class<?> PKG = ValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** The calculations to be performed */
    private List<Validation> validations;
    
    /** Checkbox to have all rules validated, with all the errors in the output */
    private boolean validatingAll;
    
    /**
     * If enabled, it concatenates all encountered errors with the selected separator
     */
    private boolean concatenatingErrors;
    
    /**
     * The concatenation separator
     */
    private String concatenationSeparator;
    
    public ValidatorMeta()
	{
		super(); // allocate BaseStepMeta
	}
    
    public void allocate(int nrValidations)
    {
    	validations = new ArrayList<Validation>(nrValidations);
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
        int nrCalcs   = XMLHandler.countNodes(stepnode,   Validation.XML_TAG);
        allocate(nrCalcs);
        validatingAll = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "validate_all"));
        concatenatingErrors = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "concat_errors"));
        concatenationSeparator = XMLHandler.getTagValue(stepnode, "concat_separator");

        for (int i=0;i<nrCalcs;i++)
        {
            Node calcnode = XMLHandler.getSubNodeByNr(stepnode, Validation.XML_TAG, i);
            validations.add( new Validation(calcnode) );
        }
	}
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);
       
        retval.append(XMLHandler.addTagValue("validate_all", validatingAll));
        retval.append(XMLHandler.addTagValue("concat_errors", concatenatingErrors));
        retval.append(XMLHandler.addTagValue("concat_separator", concatenationSeparator));
        
        for (int i=0;i<validations.size();i++)
        {
            retval.append("       ").append(validations.get(i).getXML()).append(Const.CR);
        }
        
        return retval.toString();
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	ValidatorMeta m = (ValidatorMeta)obj;
            return (getXML() == m.getXML());
        }

        return false;
    }        
    
	public Object clone()
	{
		ValidatorMeta retval = (ValidatorMeta) super.clone();
        if (validations!=null)
        {
            retval.allocate(validations.size());
            for (int i=0;i<validations.size();i++) {
            	retval.validations.add(validations.get(i).clone());
            }
        }
        else
        {
            retval.allocate(0);
        }
		return retval;
	}

	public void setDefault()
	{
		validations = new ArrayList<Validation>();
		concatenationSeparator="|";
	}

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	{
        int nrValidationFields = rep.countNrStepAttributes(id_step, "validator_field_name");
        allocate(nrValidationFields);
        validatingAll = rep.getStepAttributeBoolean(id_step, "validate_all");
        concatenatingErrors = rep.getStepAttributeBoolean(id_step, "concat_errors");
        concatenationSeparator  = rep.getStepAttributeString(id_step, "concat_separator");
        
        for (int i=0;i<nrValidationFields;i++)
        {
        	validations.add( new Validation(rep, id_step, i) );
        }
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		rep.saveStepAttribute(id_transformation, id_step, "validate_all", validatingAll);
		rep.saveStepAttribute(id_transformation, id_step, "concat_errors", concatenatingErrors);
		rep.saveStepAttribute(id_transformation, id_step, "concat_separator", concatenationSeparator);
		
        for (int i=0;i<validations.size();i++)
        {
        	validations.get(i).saveRep(rep, id_transformation, id_step, i);
        }
	}
    
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "ValidatorMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ValidatorMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "ValidatorMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "ValidatorMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Validator(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new ValidatorData();
	}

	public boolean supportsErrorHandling() {
		return true;
	}


	/**
	 * @return the validations
	 */
	public List<Validation> getValidations() {
		return validations;
	}


	/**
	 * @param validations the validations to set
	 */
	public void setValidations(List<Validation> validations) {
		this.validations = validations;
	}

    public boolean excludeFromRowLayoutVerification()
    {
        return true;
    }


	/**
	 * @return the validatingAll
	 */
	public boolean isValidatingAll() {
		return validatingAll;
	}


	/**
	 * @param validatingAll the validatingAll to set
	 */
	public void setValidatingAll(boolean validatingAll) {
		this.validatingAll = validatingAll;
	}

	/**
	 * @return the concatenatingErrors
	 */
	public boolean isConcatenatingErrors() {
		return concatenatingErrors;
	}

	/**
	 * @param concatenatingErrors the concatenatingErrors to set
	 */
	public void setConcatenatingErrors(boolean concatenatingErrors) {
		this.concatenatingErrors = concatenatingErrors;
	}

	/**
	 * @return the concatenationSeparator
	 */
	public String getConcatenationSeparator() {
		return concatenationSeparator;
	}

	/**
	 * @param concatenationSeparator the concatenationSeparator to set
	 */
	public void setConcatenationSeparator(String concatenationSeparator) {
		this.concatenationSeparator = concatenationSeparator;
	}
	
	/**
     * Returns the Input/Output metadata for this step.
     */
    public StepIOMetaInterface getStepIOMeta() {
    	if (ioMeta==null) {

    		ioMeta = new StepIOMeta(true, true, false, false, true, false);

    		// Add the info sources...
    		//
    		for (Validation validation : validations) {
				StreamInterface stream = new Stream(
						StreamType.INFO,
						validation.getSourcingStep(), 
						BaseMessages.getString(PKG, "ValidatorMeta.InfoStream.ValidationInput.Description", Const.NVL(validation.getName(), "")), 
						StreamIcon.INFO, 
						validation
					);
				ioMeta.addStream(stream);
    		}
    	}
    	
    	return ioMeta;
    }
    
	@Override
	public void searchInfoAndTargetSteps(List<StepMeta> steps) {
		for (StreamInterface stream : getStepIOMeta().getInfoStreams()) {
			Validation validation = (Validation) stream.getSubject();
			StepMeta stepMeta = StepMeta.findStep(steps, validation.getSourcingStepName());
			validation.setSourcingStep(stepMeta);
		}
		resetStepIoMeta();
	}
    
    private static StreamInterface newValidation = new Stream(StreamType.INFO, null, BaseMessages.getString(PKG, "ValidatorMeta.NewValidation.Description"), StreamIcon.INFO, null);
    
    public List<StreamInterface> getOptionalStreams() {
    	List<StreamInterface> list = new ArrayList<StreamInterface>();
    	
    	list.add( newValidation );
    	
    	return list;
    }
    
    public void handleStreamSelection(StreamInterface stream) {
    	if (stream==newValidation) {
    		
			// Add the info..
			//
    		Validation validation = new Validation();
    		validation.setName(stream.getStepname());
    		validation.setSourcingStep(stream.getStepMeta());
    		validation.setSourcingValues(true);
			validations.add(validation);
    	}
    	
    	resetStepIoMeta(); // force stepIo to be recreated when it is next needed.
    }
}
