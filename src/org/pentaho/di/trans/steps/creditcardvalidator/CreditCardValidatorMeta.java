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

package org.pentaho.di.trans.steps.creditcardvalidator;

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

public class CreditCardValidatorMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = CreditCardValidatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** dynamic field */
    private String    fieldname;
    
    private String cardtype;
    
    private String notvalidmsg;
    
    /** function result: new value name */
    private String    resultfieldname;
    
    private boolean onlydigits;
    
    public CreditCardValidatorMeta()
    {
        super(); // allocate BaseStepMeta
    }

    /**
     * @return Returns the fieldname.
     */
    public String getDynamicField()
    {
        return this.fieldname;
    }

    /**
     * @param fieldname The fieldname to set.
     */
    public void setDynamicField(String fieldname)
    {
        this.fieldname = fieldname;
    }

    /**
     * @return Returns the resultName.
     */
    public String getResultFieldName()
    {
        return resultfieldname;
    }
    
    public void setOnlyDigits(boolean onlydigits)
    {
    	this.onlydigits=onlydigits;
    }
    
    public boolean isOnlyDigits()
    {
    	return this.onlydigits;
    }


    /**
     * @param resultfieldname The resultfieldname to set.
     */
    public void setResultFieldName(String resultfieldname)
    {
        this.resultfieldname = resultfieldname;
    }
    
    /**
     * @param cardtype The cardtype to set.
     */
    public void setCardType(String cardtype)
    {
        this.cardtype = cardtype;
    }

    /**
     * @return Returns the cardtype.
     */
    public String getCardType()
    {
        return cardtype;
    }
    /**
     * @param notvalidmsg The notvalidmsg to set.
     */
    public void setNotValidMsg(String notvalidmsg)
    {
        this.notvalidmsg = notvalidmsg;
    }
    /**
     * @return Returns the notvalidmsg.
     */
    public String getNotValidMsg()
    {
        return notvalidmsg;
    }
    
    
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleXMLException
	{
		readData(stepnode);
	}
 

    public Object clone()
    {
        CreditCardValidatorMeta retval = (CreditCardValidatorMeta) super.clone();
       
        return retval;
    }

    public void setDefault()
    {
        resultfieldname = "result"; //$NON-NLS-1$
        onlydigits=false;
        cardtype="card type";
        notvalidmsg="not valid message";
    }

	public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface info[], StepMeta nextStep, VariableSpace space) throws KettleStepException
	{ 
        String realresultfieldname=space.environmentSubstitute(resultfieldname);
        if (!Const.isEmpty(realresultfieldname))
        {
            ValueMetaInterface v = new ValueMeta(realresultfieldname, ValueMeta.TYPE_BOOLEAN);
			v.setOrigin(name);
			inputRowMeta.addValueMeta(v);
        }
        String realcardtype=space.environmentSubstitute(cardtype);
        if (!Const.isEmpty(realcardtype))
	     {  
	        ValueMetaInterface v = new ValueMeta(realcardtype, ValueMeta.TYPE_STRING);
			v.setOrigin(name);
			inputRowMeta.addValueMeta(v);
	     }
        String realnotvalidmsg=space.environmentSubstitute(notvalidmsg);
        if (!Const.isEmpty(notvalidmsg))
	     {  
		     ValueMetaInterface v = new ValueMeta(realnotvalidmsg, ValueMeta.TYPE_STRING);
		     v.setOrigin(name);
			 inputRowMeta.addValueMeta(v);
	     }
    }
	

    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    " + XMLHandler.addTagValue("fieldname", fieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("resultfieldname", resultfieldname)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    " + XMLHandler.addTagValue("cardtype", cardtype));
        retval.append("    ").append(XMLHandler.addTagValue("onlydigits", onlydigits));
        retval.append("    " + XMLHandler.addTagValue("notvalidmsg", notvalidmsg));
        
        return retval.toString();
    }

	private void readData(Node stepnode) throws KettleXMLException
	{
	try
	{
			fieldname = XMLHandler.getTagValue(stepnode, "fieldname"); //$NON-NLS-1$
            resultfieldname = XMLHandler.getTagValue(stepnode, "resultfieldname");
            cardtype = XMLHandler.getTagValue(stepnode, "cardtype");  
            notvalidmsg = XMLHandler.getTagValue(stepnode, "notvalidmsg");
            onlydigits  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "onlydigits"));
            
        }
        catch (Exception e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "CreditCardValidatorMeta.Exception.UnableToReadStepInfo"), e); //$NON-NLS-1$
        }
    }

	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
	throws KettleException
	{
        try
        {
        	fieldname = rep.getStepAttributeString(id_step, "fieldname"); //$NON-NLS-1$
            resultfieldname = rep.getStepAttributeString(id_step, "resultfieldname"); //$NON-NLS-1$
            cardtype = rep.getStepAttributeString(id_step, "cardtype"); //$NON-NLS-1$
            notvalidmsg = rep.getStepAttributeString(id_step, "notvalidmsg"); //$NON-NLS-1$
            onlydigits  = rep.getStepAttributeBoolean(id_step, "onlydigits");
            
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "CreditCardValidatorMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
        }
    }

    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
    {
        try
        {
            rep.saveStepAttribute(id_transformation, id_step, "fieldname", fieldname); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "resultfieldname", resultfieldname); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "cardtype", cardtype);
			rep.saveStepAttribute(id_transformation, id_step, "notvalidmsg", notvalidmsg);
			rep.saveStepAttribute(id_transformation, id_step, "onlydigits",onlydigits);
			
        }
        catch (Exception e)
        {
            throw new KettleException(BaseMessages.getString(PKG, "CreditCardValidatorMeta.Exception.UnableToSaveStepInfo") + id_step, e); //$NON-NLS-1$
        }
    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
        CheckResult cr;
        String error_message = ""; //$NON-NLS-1$

        String realresultfieldname=transMeta.environmentSubstitute(resultfieldname);
        if (Const.isEmpty(realresultfieldname))
        {
            error_message = BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.ResultFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.ResultFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        if (Const.isEmpty(fieldname))
        {
            error_message = BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.CardFieldMissing"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
            remarks.add(cr);
        }
        else
        {
            error_message = BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.CardFieldOK"); //$NON-NLS-1$
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, error_message, stepMeta);
            remarks.add(cr);
        }
        // See if we have input streams leading to this step!
        if (input.length > 0)
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "CreditCardValidatorMeta.CheckResult.NoInpuReceived"), stepMeta); //$NON-NLS-1$
            remarks.add(cr);
        }

    }

    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
    {
        return new CreditCardValidator(stepMeta, stepDataInterface, cnr, transMeta, trans);
    }

    public StepDataInterface getStepData()
    {
        return new CreditCardValidatorData();
    }

    public boolean supportsErrorHandling()
    {
        return true;
    }

}
