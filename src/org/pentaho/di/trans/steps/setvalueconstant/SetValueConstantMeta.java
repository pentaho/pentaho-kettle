/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar Hassan.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.setvalueconstant;

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



public class SetValueConstantMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SetValueConstantMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** which fields to display? */
    private String  fieldName[];
    
    /** by which value we replace */
    private String replaceValue[];
    
    private String replaceMask[];
    
    /** Flag : set empty string **/
    private boolean setEmptyString[];
    
    private boolean usevar;
    
	public SetValueConstantMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode, databases);
    }

	public Object clone()
	{	
        SetValueConstantMeta retval = (SetValueConstantMeta) super.clone();
       
        int nrfields = fieldName.length;
        retval.allocate(nrfields);

        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.replaceValue[i] = replaceValue[i];
            retval.replaceMask[i]=replaceMask[i];
            retval.setEmptyString[i]=setEmptyString[i];
        }

        
		return retval;
	}
   public void allocate(int nrfields)
    {
        fieldName = new String[nrfields]; 
        replaceValue = new String[nrfields]; 
        replaceMask = new String[nrfields]; 
        setEmptyString = new boolean[nrfields];
    }
   
 
    /**
     * @return Returns the fieldName.
     */
    public String[] getFieldName()
    {
        return fieldName;
    }
    /**
     * @param fieldName The fieldName to set.
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }
    
  
    /**
     * @return Returns the replaceValue.
     */
    public String[] getReplaceValue()
    {
        return replaceValue;
    }
    /**
     * @param fieldName The replaceValue to set.
     */
    public void setReplaceValue(String[] replaceValue)
    {
        this.replaceValue = replaceValue;
    }
    /**
     * @return Returns the replaceMask.
     */
    public String[] getReplaceMask()
    {
        return replaceMask;
    }

    /**
     * @param replaceMask The replaceMask to set.
     */
    public void setReplaceMask(String[] replaceMask)
    {
        this.replaceMask = replaceMask;
    }
	/**
	 * @return the setEmptyString
	 */
	public boolean[] isSetEmptyString() {
		return setEmptyString;
	}

	/**
	 * @param setEmptyString the setEmptyString to set
	 */
	public void setEmptyString(boolean[] setEmptyString) {
		this.setEmptyString = setEmptyString;
	}
    public void setUseVars (boolean usevar)
    {
    	this.usevar=usevar;
    }
    public boolean isUseVars ()
    {
    	return usevar;
    }
    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
    {
	  try
	    {
		  usevar  = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "usevar"));
		  Node fields = XMLHandler.getSubNode(stepnode, "fields");
          int nrfields = XMLHandler.countNodes(fields, "field");
          allocate(nrfields);
          
          for (int i = 0; i < nrfields; i++)
          {
              Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);
              fieldName[i] = XMLHandler.getTagValue(fnode, "name");
              replaceValue[i] = XMLHandler.getTagValue(fnode, "value");
              replaceMask[i] = XMLHandler.getTagValue(fnode, "mask");
              String emptyString = XMLHandler.getTagValue(fnode, "set_empty_string");
              setEmptyString[i] = !Const.isEmpty(emptyString) && "Y".equalsIgnoreCase(emptyString);
          }  
	    }
      catch (Exception e)
      {
          throw new KettleXMLException("It was not possible to load the metadata for this step from XML", e);
      }
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        retval.append("   "+XMLHandler.addTagValue("usevar",          usevar));
        retval.append("    <fields>" + Const.CR);
        for (int i = 0; i < fieldName.length; i++)
        {
            retval.append("      <field>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", fieldName[i]));
            retval.append("        " + XMLHandler.addTagValue("value", replaceValue[i]));
            retval.append("        " + XMLHandler.addTagValue("mask", replaceMask[i]));
            retval.append("        " + XMLHandler.addTagValue("set_empty_string", setEmptyString[i]));
            retval.append("        </field>" + Const.CR);
        }
        retval.append("      </fields>" + Const.CR);

        return retval.toString();
    }
	public void setDefault()
	{
		int nrfields = 0;
        allocate(nrfields);
        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i;
            replaceValue[i] = "value" + i;
            replaceMask[i] = "mask" + i;
            setEmptyString[i] = false;
        }
        usevar=false;
	}

	 public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	 {
	        try
	        {
	        	usevar  = rep.getStepAttributeBoolean(id_step, "usevar");
	            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
	            allocate(nrfields);
	           
	            for (int i = 0; i < nrfields; i++)
	            {
	                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
	                replaceValue[i] = rep.getStepAttributeString(id_step, i, "replace_value");
	                replaceMask[i] = rep.getStepAttributeString(id_step, i, "replace_mask");
	                setEmptyString[i] = rep.getStepAttributeBoolean(id_step, i, "set_empty_string", false);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unexpected error reading step information from the repository", e);
	        }
	    }
	
	    public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step) throws KettleException
	    {
	        try
	        {
	        	rep.saveStepAttribute(id_transformation, id_step, "usevar",          usevar);
	            for (int i = 0; i < fieldName.length; i++)
	            {
	                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
	                rep.saveStepAttribute(id_transformation, id_step, i, "replace_value", replaceValue[i]);
	                rep.saveStepAttribute(id_transformation, id_step, i, "replace_mask", replaceMask[i]);
	                rep.saveStepAttribute(id_transformation, id_step, i, "set_empty_string", setEmptyString[i]);
	            }
	        }
	        catch (Exception e)
	        {
	            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
	        }
	    }
	
	 public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	 {	CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			String error_message = "";
	        boolean error_found = false;

            // Starting from selected fields in ...
            for (int i = 0; i < fieldName.length; i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx < 0)
                {
                    error_message += "\t\t" + fieldName[i] + Const.CR;
                    error_found = true;
                }
            }
            if (error_found)
            {
                error_message = BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.FieldsFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.AllFieldsFound"), stepinfo);
                else
                    cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.NoFieldsEntered"), stepinfo);
                 remarks.add(cr);
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
		else
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SetValueConstantMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
		remarks.add(cr);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new SetValueConstant(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SetValueConstantData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    }
}
