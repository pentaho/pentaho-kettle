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
package org.pentaho.di.trans.steps.ifnull;

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



public class IfNullMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = IfNullMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** which fields to display? */
    private String  fieldName[];
    
    /** by which value we replace */
    private String replaceValue[];
    
    /** which types to display? */
    private String typeName[];
    
    /** by which value we replace */
    private String typereplaceValue[];
    
    private String typereplaceMask[];
    
    private String replaceMask[];
    
    /** Flag : set empty string for type  **/
    private boolean setTypeEmptyString[];
    
    /** Flag : set empty string **/
    private boolean setEmptyString[];
    
    
    
    private boolean selectFields;
    
    private boolean selectValuesType;
    
    private String replaceAllByValue;
    
    private String replaceAllMask;
    

    /** The flag to set auto commit on or off on the connection */
    private boolean  setEmptyStringAll;

    
	public IfNullMeta()
	{
		super(); // allocate BaseStepMeta
	}

    /**
     * @return Returns the setEmptyStringAll.
     */
    public boolean isSetEmptyStringAll()
    {
        return setEmptyStringAll;
    }

    /**
     * @param setEmptyStringAll The setEmptyStringAll to set.
     */
    public void setEmptyStringAll(boolean setEmptyStringAll)
    {
        this.setEmptyStringAll = setEmptyStringAll;
    }
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
    {
        readData(stepnode, databases);
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
	
	/**
	 * @return the setTypeEmptyString
	 */
	public boolean[] isSetTypeEmptyString() {
		return setTypeEmptyString;
	}

	/**
	 * @param setTypeEmptyString the setTypeEmptyString to set
	 */
	public void setTypeEmptyString(boolean[] setTypeEmptyString) {
		this.setTypeEmptyString = setTypeEmptyString;
	}
	public Object clone()
	{	
        IfNullMeta retval = (IfNullMeta) super.clone();
       
        int nrTypes=typeName.length;
        int nrfields = fieldName.length;
        retval.allocate(nrTypes,nrfields);
        
        for (int i = 0; i < nrTypes; i++)
        {
            retval.typeName[i] = typeName[i];
            retval.typereplaceValue[i] = typereplaceValue[i];
            retval.typereplaceMask[i]= typereplaceMask[i];
		    retval.setTypeEmptyString[i]=setTypeEmptyString[i];
        }
        
        for (int i = 0; i < nrfields; i++)
        {
            retval.fieldName[i] = fieldName[i];
            retval.replaceValue[i] = replaceValue[i];
            retval.replaceMask[i]=replaceMask[i];
            retval.setEmptyString[i]=setEmptyString[i];
        }

        
		return retval;
	}
   public void allocate(int nrtypes,int nrfields)
    {
        typeName = new String[nrtypes]; 
        typereplaceValue = new String[nrtypes]; 
        typereplaceMask = new String[nrtypes];
        setTypeEmptyString = new boolean[nrtypes];
        
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
     * @return Returns the fieldName.
     */
    public String[] getTypeName()
    {
        return typeName;
    }
    /**
     * @param typeName The typeName to set.
     */
    public void setTypeName(String[] typeName)
    {
        this.typeName = typeName;
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
     * @return Returns the typereplaceValue.
     */
    public String[] getTypeReplaceValue()
    {
        return typereplaceValue;
    }
    /**
     * @return Returns the typereplaceMask.
     */
    public String[] getTypeReplaceMask()
    {
        return typereplaceMask;
    }
    /**
     * @return Returns the replaceMask.
     */
    public String[] getReplaceMask()
    {
        return replaceMask;
    }
    /**
     * @param typereplaceMask The typereplaceMask to set.
     */
    public void setTypeReplaceMask(String[] typereplaceMask)
    {
        this.typereplaceMask = typereplaceMask;
    }
    /**
     * @param replaceMask The replaceMask to set.
     */
    public void setReplaceMask(String[] replaceMask)
    {
        this.replaceMask = replaceMask;
    }
    
    
    
    /**
     * @param typereplaceValue The typereplaceValue to set.
     */
    public void setTypeReplaceValue(String[] typereplaceValue)
    {
        this.typereplaceValue = typereplaceValue;
    }
    
    public boolean isSelectFields()
    {
    	return selectFields;
    }
    public void setSelectFields(boolean selectFields)
    {
    	this.selectFields=selectFields;
    }
    public void setSelectValuesType(boolean selectValuesType)
    {
    	this.selectValuesType=selectValuesType;
    }
    public boolean isSelectValuesType()
    {
    	return selectValuesType;
    }
    public void setReplaceAllByValue(String replaceValue)
    {
    	this.replaceAllByValue=replaceValue;
    }
    public String getReplaceAllByValue()
    {
        return replaceAllByValue;
    }
    public void setReplaceAllMask(String replaceAllMask)
    {
    	this.replaceAllMask=replaceAllMask;
    }
    public String getReplaceAllMask()
    {
        return replaceAllMask;
    }
    
    
    private void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException
    {
	  try
	    {
		  selectFields = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "selectFields"));
		  selectValuesType = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "selectValuesType"));
		  replaceAllByValue = XMLHandler.getTagValue(stepnode, "replaceAllByValue");
		  replaceAllMask = XMLHandler.getTagValue(stepnode, "replaceAllMask");
		  setEmptyStringAll = !"N".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "setEmptyStringAll"));
		  
		  Node types = XMLHandler.getSubNode(stepnode, "valuetypes");
          int nrtypes = XMLHandler.countNodes(types, "valuetype");
		  Node fields = XMLHandler.getSubNode(stepnode, "fields");
          int nrfields = XMLHandler.countNodes(fields, "field");
          
          allocate(nrtypes,nrfields);
          
          for (int i = 0; i < nrtypes; i++)
          {
              Node tnode = XMLHandler.getSubNodeByNr(types, "valuetype", i);
              typeName[i] = XMLHandler.getTagValue(tnode, "name");
              typereplaceValue[i] = XMLHandler.getTagValue(tnode, "value");
              typereplaceMask[i] = XMLHandler.getTagValue(tnode, "mask");  
              String typeemptyString = XMLHandler.getTagValue(tnode, "set_type_empty_string");
	          setTypeEmptyString[i] = Const.isEmpty(typeemptyString) || "Y".equalsIgnoreCase(typeemptyString);
          }
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
          throw new KettleXMLException("It was not possibke to load the IfNull metadata from XML", e);
      }
	}
   public String getXML()
    {
        StringBuffer retval = new StringBuffer();
        
        retval.append("      " + XMLHandler.addTagValue("replaceAllByValue", replaceAllByValue));
        retval.append("      " + XMLHandler.addTagValue("replaceAllMask", replaceAllMask));
        retval.append("      " + XMLHandler.addTagValue("selectFields", selectFields));
        retval.append("      " + XMLHandler.addTagValue("selectValuesType", selectValuesType));
        retval.append("      " + XMLHandler.addTagValue("setEmptyStringAll", setEmptyStringAll));
        
        retval.append("    <valuetypes>" + Const.CR);
        for (int i = 0; i < typeName.length; i++)
        {
            retval.append("      <valuetype>" + Const.CR);
            retval.append("        " + XMLHandler.addTagValue("name", typeName[i]));
            retval.append("        " + XMLHandler.addTagValue("value", typereplaceValue[i]));
            retval.append("        " + XMLHandler.addTagValue("mask", typereplaceMask[i]));
            retval.append("        " + XMLHandler.addTagValue("set_type_empty_string", setTypeEmptyString[i]));
            retval.append("        </valuetype>" + Const.CR);
        }
        retval.append("      </valuetypes>" + Const.CR);

        
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
		replaceAllByValue=null;
		replaceAllMask=null;
		selectFields=false;
		selectValuesType=false;
		setEmptyStringAll=false;
		
		int nrfields = 0;
		int nrtypes = 0;
        allocate(nrtypes,nrfields);
        for (int i = 0; i < nrtypes; i++)
        {
            typeName[i] = "typename" + i;
            typereplaceValue[i] = "typevalue" + i;
            typereplaceMask[i] = "typemask" + i;
            setTypeEmptyString[i] = false;
        }
        for (int i = 0; i < nrfields; i++)
        {
            fieldName[i] = "field" + i;
            replaceValue[i] = "value" + i;
            replaceMask[i] = "mask" + i;
            setEmptyString[i] = false;
        }
	}

	 public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
	   {
	        try
	        {
	        	replaceAllByValue = rep.getStepAttributeString(id_step, "replaceAllByValue");
	        	replaceAllMask = rep.getStepAttributeString(id_step, "replaceAllMask");
	        	selectFields = rep.getStepAttributeBoolean(id_step, "selectFields");
	        	selectValuesType = rep.getStepAttributeBoolean(id_step, "selectValuesType");
	        	setEmptyStringAll = rep.getStepAttributeBoolean(id_step, 0, "setEmptyStringAll", false);
	        	  
	            int nrtypes = rep.countNrStepAttributes(id_step, "type_name");
	            int nrfields = rep.countNrStepAttributes(id_step, "field_name");
	            allocate(nrtypes,nrfields);
	            
	            for (int i = 0; i < nrtypes; i++)
	            {
	                typeName[i] = rep.getStepAttributeString(id_step, i, "type_name");
	                typereplaceValue[i] = rep.getStepAttributeString(id_step, i, "type_replace_value");
	                typereplaceMask[i]= rep.getStepAttributeString(id_step, i, "type_replace_mask");
	                setTypeEmptyString[i] = rep.getStepAttributeBoolean(id_step, i, "set_type_empty_string", false);
	            }
	            
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
	        	rep.saveStepAttribute(id_transformation, id_step, "replaceAllByValue", replaceAllByValue);
	        	rep.saveStepAttribute(id_transformation, id_step, "replaceAllMask", replaceAllMask);
	        	rep.saveStepAttribute(id_transformation, id_step, "selectFields", selectFields);
	        	rep.saveStepAttribute(id_transformation, id_step, "selectValuesType", selectValuesType);
	        	rep.saveStepAttribute(id_transformation, id_step, "setEmptyStringAll", setEmptyStringAll); //$NON-NLS-1$
	        	   
	            for (int i = 0; i < typeName.length; i++)
	            {
	                rep.saveStepAttribute(id_transformation, id_step, i, "type_name", typeName[i]);
	                rep.saveStepAttribute(id_transformation, id_step, i, "type_replace_value", typereplaceValue[i]);
	                rep.saveStepAttribute(id_transformation, id_step, i, "type_replace_mask", typereplaceMask[i]);
	        	    rep.saveStepAttribute(id_transformation, id_step, i, "set_type_empty_string", setTypeEmptyString[i]);
	            }
	            
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
	   {
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.NotReceivingFields"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.StepRecevingData",prev.size()+""), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
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
                error_message = BaseMessages.getString(PKG, "IfNullMeta.CheckResult.FieldsFound", error_message);

                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepinfo);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length > 0)
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.AllFieldsFound"), stepinfo);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.NoFieldsEntered"), stepinfo);
                    remarks.add(cr);
                }
            }

		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.StepRecevingData2"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "IfNullMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new IfNull(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new IfNullData();
	}
    public boolean supportsErrorHandling()
    {
        return true;
    }

}
