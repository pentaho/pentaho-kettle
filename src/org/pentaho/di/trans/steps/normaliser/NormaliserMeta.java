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

package org.pentaho.di.trans.steps.normaliser;

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
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInjectionInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Node;

/*
 * Created on 30-okt-2003
 *
 */

/*

DATE      PRODUCT1_NR  PRODUCT1_SL  PRODUCT2_NR PRODUCT2_SL PRODUCT3_NR PRODUCT3_SL 
20030101            5          100           10         250           4         150
          
DATE      PRODUCT    Sales   Number  
20030101  PRODUCT1     100        5
20030101  PRODUCT2     250       10
20030101  PRODUCT3     150        4

--> we need a mapping of fields with occurances.  (PRODUCT1_NR --> "PRODUCT1", PRODUCT1_SL --> "PRODUCT1", ...)
--> List of Fields with the type and the new fieldname to fill
--> PRODUCT1_NR, "PRODUCT1", Number
--> PRODUCT1_SL, "PRODUCT1", Sales
--> PRODUCT2_NR, "PRODUCT2", Number
--> PRODUCT2_SL, "PRODUCT2", Sales
--> PRODUCT3_NR, "PRODUCT3", Number
--> PRODUCT3_SL, "PRODUCT3", Sales

--> To parse this, we loop over the occurances of type: "PRODUCT1", "PRODUCT2" and "PRODUCT3"
--> For each of the occurance, we insert a record.

**/
 
public class NormaliserMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = NormaliserMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String typeField;    // Name of the new type-field.
	private String fieldName[];      // Names of the selected fields. ex. "PRODUCT1_NR"
	private String fieldValue[]; // Value of the type: ex.            "PRODUCT1"
	private String fieldNorm[];  // new normalised field              "Number"
	
	public NormaliserMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the typeField.
     */
    public String getTypeField()
    {
        return typeField;
    }
    
    /**
     * @param typeField The typeField to set.
     */
    public void setTypeField(String typeField)
    {
        this.typeField = typeField;
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
     * @return Returns the fieldValue.
     */
    public String[] getFieldValue()
    {
        return fieldValue;
    }
    
    /**
     * @param fieldValue The fieldValue to set.
     */
    public void setFieldValue(String[] fieldValue)
    {
        this.fieldValue = fieldValue;
    }
    
    /**
     * @return Returns the fieldNorm.
     */
    public String[] getFieldNorm()
    {
        return fieldNorm;
    }
    
    /**
     * @param fieldNorm The fieldNorm to set.
     */
    public void setFieldNorm(String[] fieldNorm)
    {
        this.fieldNorm = fieldNorm;
    }
	
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleXMLException {
		readData(stepnode);
	}

	public void allocate(int nrfields)
	{
		fieldName      = new String[nrfields];
		fieldValue = new String[nrfields];
		fieldNorm  = new String[nrfields];
	}

	public Object clone()
	{
		NormaliserMeta retval = (NormaliserMeta)super.clone();

		int nrfields   = fieldName.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.fieldName     [i] = fieldName[i]; 
			retval.fieldValue[i] = fieldValue[i];
			retval.fieldNorm [i] = fieldNorm[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			typeField  = XMLHandler.getTagValue(stepnode, "typefield"); //$NON-NLS-1$
			
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				fieldName     [i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
				fieldValue[i] = XMLHandler.getTagValue(fnode, "value"); //$NON-NLS-1$
				fieldNorm [i] = XMLHandler.getTagValue(fnode, "norm"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "NormaliserMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		typeField = "typefield"; //$NON-NLS-1$
		
		int nrfields = 0;
	
		allocate(nrfields);		
		
		for (int i=0;i<nrfields;i++)
		{
			fieldName     [i] = "field"+i; //$NON-NLS-1$
			fieldValue[i] = "value"+i; //$NON-NLS-1$
			fieldNorm [i] = "value"+i; //$NON-NLS-1$
		}
	}
	
	@Override
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep,
			VariableSpace space) throws KettleStepException {
	
		// Get a unique list of the occurrences of the type
		//
		List<String> norm_occ = new ArrayList<String>();
		List<String> field_occ  = new ArrayList<String>();
		int maxlen=0;
		for (int i=0;i<fieldNorm.length;i++)
		{
			if (!norm_occ.contains(fieldNorm[i])) 
			{
				norm_occ.add(fieldNorm[i]);
				field_occ.add(fieldName[i]);
			} 
			
			if (fieldValue[i].length()>maxlen) maxlen=fieldValue[i].length();
		}

		// Then add the type field!
		//
		ValueMetaInterface typefield_value = new ValueMeta(typeField, ValueMetaInterface.TYPE_STRING);
		typefield_value.setOrigin(name);
		typefield_value.setLength(maxlen);
		row.addValueMeta(typefield_value);

		// Loop over the distinct list of fieldNorm[i]
		// Add the new fields that need to be created. 
		// Use the same data type as the original fieldname...
		//
		for (int i=0;i<norm_occ.size();i++)
		{
			String normname = (String)norm_occ.get(i);
			String fieldname =(String)field_occ.get(i);
			ValueMetaInterface v = row.searchValueMeta(fieldname).clone();
			v.setName(normname);
			v.setOrigin(name);
			row.addValueMeta(v);
		}
		
		// Now remove all the normalized fields...
		//
		for (int i=0;i<fieldName.length;i++) {
			int idx = row.indexOfValue(fieldName[i]);
			if (idx>=0) row.removeValueMeta(idx);
		}
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();
		
		retval.append("   "+XMLHandler.addTagValue("typefield", typeField)); //$NON-NLS-1$ //$NON-NLS-2$
		
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<fieldName.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",  fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("value", fieldValue[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("norm",  fieldNorm[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"); //$NON-NLS-1$
		}
		retval.append("      </fields>"); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
		
		try
		{
			typeField = rep.getStepAttributeString(id_step, "typefield"); //$NON-NLS-1$
			
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i]       =  rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
				fieldValue[i]  =  rep.getStepAttributeString (id_step, i, "field_value"); //$NON-NLS-1$
				fieldNorm[i]   =  rep.getStepAttributeString (id_step, i, "field_norm"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "NormaliserMeta.Exception.UnexpectedErrorReadingStepInfoFromRepository"), e); //$NON-NLS-1$
		}
	}

	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "typefield", typeField); //$NON-NLS-1$
	
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_value",     fieldValue[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "field_norm",      fieldNorm[i]); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "NormaliserMeta.Exception.UnableToSaveStepInfoToRepository")+id_step, e); //$NON-NLS-1$
		}
	}

	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
			String[] input, String[] output, RowMetaInterface info) {

		String error_message=""; //$NON-NLS-1$
		CheckResult cr;
		
		// Look up fields in the input stream <prev>
		if (prev!=null && prev.size()>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.StepReceivingFieldsOK",prev.size()+""), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
			
			boolean first=true;
			error_message = ""; //$NON-NLS-1$
			boolean error_found = false;
			
			for (int i=0;i<fieldName.length;i++)
			{
				String lufield = fieldName[i];

				ValueMetaInterface v = prev.searchValueMeta(lufield);
				if (v==null)
				{
					if (first)
					{
						first=false;
						error_message+=BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.FieldsNotFound")+Const.CR; //$NON-NLS-1$
					}
					error_found=true;
					error_message+="\t\t"+lufield+Const.CR;  //$NON-NLS-1$
				}
			}
			if (error_found)
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			}
			else
			{
				cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.AllFieldsFound"), stepMeta); //$NON-NLS-1$
			}
			remarks.add(cr);
		}
		else
		{
			error_message=BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.CouldNotReadFieldsFromPreviousStep")+Const.CR; //$NON-NLS-1$
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, error_message, stepMeta);
			remarks.add(cr);
		}

		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.StepReceivingInfoOK"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "NormaliserMeta.CheckResult.NoInputReceivedError"), stepMeta); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new Normaliser(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new NormaliserData();
	}

	@Override
	public StepMetaInjectionInterface getStepMetaInjectionInterface() {
	  return new NormaliserMetaInjection(this);
	}
}
