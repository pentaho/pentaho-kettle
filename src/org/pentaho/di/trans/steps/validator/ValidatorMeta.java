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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;



/**
 * Contains the meta-data for the Validator step: calculates predefined formula's
 * 
 * Created on 08-sep-2005
 */

public class ValidatorMeta extends BaseStepMeta implements StepMetaInterface
{
    /** The calculations to be performed */
    private ValidatorField[] validatorField;
    
    public ValidatorMeta()
	{
		super(); // allocate BaseStepMeta
	}

    
    public void allocate(int nrValidations)
    {
        validatorField = new ValidatorField[nrValidations];
    }
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
        int nrCalcs   = XMLHandler.countNodes(stepnode,   ValidatorField.XML_TAG);
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            Node calcnode = XMLHandler.getSubNodeByNr(stepnode, ValidatorField.XML_TAG, i);
            validatorField[i] = new ValidatorField(calcnode);
        }
	}
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer(300);
       
        if (validatorField!=null)
        for (int i=0;i<validatorField.length;i++)
        {
            retval.append("       ").append(validatorField[i].getXML()).append(Const.CR);
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
        if (validatorField!=null)
        {
            retval.allocate(validatorField.length);
            for (int i=0;i<validatorField.length;i++) retval.getValidatorField()[i] = validatorField[i].clone();
        }
        else
        {
            retval.allocate(0);
        }
		return retval;
	}

	public void setDefault()
	{
        validatorField = new ValidatorField[0]; 
	}

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
        int nrValidationFields = rep.countNrStepAttributes(id_step, "validator_field_name");
        allocate(nrValidationFields);
        for (int i=0;i<nrValidationFields;i++)
        {
            validatorField[i] = new ValidatorField(rep, id_step, i);
        }
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
        for (int i=0;i<validatorField.length;i++)
        {
            validatorField[i].saveRep(rep, id_transformation, id_step, i);
        }
	}
    
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("ValidatorMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ValidatorMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("ValidatorMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("ValidatorMeta.CheckResult.ExpectedInputError"), stepMeta);
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


	/**
	 * @return the validatorField
	 */
	public ValidatorField[] getValidatorField() {
		return validatorField;
	}


	/**
	 * @param validatorField the validatorField to set
	 */
	public void setValidatorField(ValidatorField[] validatorField) {
		this.validatorField = validatorField;
	}

	public boolean supportsErrorHandling() {
		return true;
	}

}
