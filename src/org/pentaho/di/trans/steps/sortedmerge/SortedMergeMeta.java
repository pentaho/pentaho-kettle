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
 
package org.pentaho.di.trans.steps.sortedmerge;

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
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransMeta.TransformationType;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;


/*
 * Created on 02-jun-2003
 *
 */

public class SortedMergeMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = SortedMergeMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** order by which fields? */
    private String  fieldName[];
    /** false : descending, true=ascending */
    private boolean ascending[];

	public SortedMergeMeta()
	{
		super(); // allocate BaseStepMeta
	}

	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException
	{
		readData(stepnode);
	}

    public void allocate(int nrfields)
    {
        fieldName = new String[nrfields]; // order by
        ascending = new boolean[nrfields];
    }
    
    public void setDefault()
    {       
        int nrfields = 0;
        
        allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            fieldName[i]="field"+i;
        }
    }
     
    public Object clone()
    {
        SortedMergeMeta retval = (SortedMergeMeta)super.clone();

        int nrfields = fieldName.length;

        retval.allocate(nrfields);
        
        for (int i=0;i<nrfields;i++)
        {
            retval.fieldName   [i] = fieldName[i];
            retval.ascending[i] = ascending[i]; 
        }
        
        return retval;
    }
	
    private void readData(Node stepnode) throws KettleXMLException
    {
        try
        {
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields = XMLHandler.countNodes(fields, "field");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i);

                fieldName[i] = XMLHandler.getTagValue(fnode, "name");
                String asc = XMLHandler.getTagValue(fnode, "ascending");
                if (asc.equalsIgnoreCase("Y"))
                    ascending[i] = true;
                else
                    ascending[i] = false;
            }
        }
        catch (Exception e)
        {
            throw new KettleXMLException("Unable to load step info from XML", e);
        }
    }
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();

        retval.append("    <fields>"+Const.CR);
        for (int i=0;i<fieldName.length;i++)
        {
            retval.append("      <field>"+Const.CR);
            retval.append("        "+XMLHandler.addTagValue("name",      fieldName[i]));
            retval.append("        "+XMLHandler.addTagValue("ascending", ascending[i]));
            retval.append("        </field>"+Const.CR);
        }
        retval.append("      </fields>"+Const.CR);

        return retval.toString();   
    }

    public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException
    {
        try
        {
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfields);

            for (int i = 0; i < nrfields; i++)
            {
                fieldName[i] = rep.getStepAttributeString(id_step, i, "field_name");
                ascending[i] = rep.getStepAttributeBoolean(id_step, i, "field_ascending");
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
            for (int i = 0; i < fieldName.length; i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "field_name", fieldName[i]);
                rep.saveStepAttribute(id_transformation, id_step, i, "field_ascending", ascending[i]);
            }
        }
        catch (Exception e)
        {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // Set the sorted properties: ascending/descending
        for (int i=0;i<fieldName.length;i++)
        {
            int idx = inputRowMeta.indexOfValue(fieldName[i]);
            if (idx>=0)
            {
                ValueMetaInterface valueMeta = inputRowMeta.getValueMeta(idx);
                valueMeta.setSortedDescending(!ascending[i]);
                
                // TODO: add case insensivity
            }
        }
        
    }
	
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
    {
        CheckResult cr;
        
        if (prev!=null && prev.size()>0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
            remarks.add(cr);
            
            String  error_message="";
            boolean error_found=false;
            
            // Starting from selected fields in ...
            for (int i=0;i< fieldName.length;i++)
            {
                int idx = prev.indexOfValue(fieldName[i]);
                if (idx<0)
                {
                    error_message+="\t\t"+fieldName[i]+Const.CR;
                    error_found=true;
                } 
            }
            if (error_found) 
            {
                error_message=BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.SortKeysNotFound", error_message);

                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, error_message, stepMeta);
                remarks.add(cr);
            }
            else
            {
                if (fieldName.length>0)
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.AllSortKeysFound"), stepMeta);
                    remarks.add(cr);
                }
                else
                {
                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.NoSortKeysEntered"), stepMeta);
                    remarks.add(cr);
                }
            }
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.NoFields"), stepMeta);
            remarks.add(cr);
        }
        
        // See if we have input streams leading to this step!
        if (input.length>0)
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.ExpectedInputOk"), stepMeta);
            remarks.add(cr);
        }
        else
        {
            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "SortedMergeMeta.CheckResult.ExpectedInputError"), stepMeta);
            remarks.add(cr);
        }
    }

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new SortedMerge(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new SortedMergeData();
	}

    /**
     * @return the ascending
     */
    public boolean[] getAscending()
    {
        return ascending;
    }

    /**
     * @param ascending the ascending to set
     */
    public void setAscending(boolean[] ascending)
    {
        this.ascending = ascending;
    }

    /**
     * @return the fieldName
     */
    public String[] getFieldName()
    {
        return fieldName;
    }

    /**
     * @param fieldName the fieldName to set
     */
    public void setFieldName(String[] fieldName)
    {
        this.fieldName = fieldName;
    }

    public TransformationType[] getSupportedTransformationTypes() {
      return new TransformationType[] { TransformationType.Normal, };
    }
}
