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

package org.pentaho.di.trans.steps.uniquerowsbyhashset;

import java.util.List;
import java.util.Map;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
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

public class UniqueRowsByHashSetMeta extends BaseStepMeta implements StepMetaInterface
{
	private static Class<?> PKG = UniqueRowsByHashSetMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    /** Whether to compare strictly by hash value or to store the row values for strict equality checking */
    private boolean storeValues;
    
	/**The fields to compare for duplicates, null means all*/
	private String compareFields[];
	
    private boolean rejectDuplicateRow;
    private String errorDescription;

	public UniqueRowsByHashSetMeta()
	{
		super(); // allocate BaseStepMeta
	}
    
    /**
     * @param compareField The compareField to set.
     */
    public void setCompareFields(String[] compareField)
    {
        this.compareFields = compareField;
    }
    
    public boolean getStoreValues()
    {
        return storeValues;
    }
    
    public void setStoreValues(boolean storeValues)
    {
        this.storeValues = storeValues;
    }
    
    /**
     * @return Returns the compareField.
     */
    public String[] getCompareFields()
    {
        return compareFields;
    }
    
	public void allocate(int nrfields)
	{
		compareFields = new String[nrfields];
	}

    /**
     * @param rejectDuplicateRow The rejectDuplicateRow to set.
     */
    public void setRejectDuplicateRow(boolean rejectDuplicateRow)
    {
        this.rejectDuplicateRow = rejectDuplicateRow;
    }
	/**
     * @return Returns the rejectDuplicateRow.
     */
    public boolean isRejectDuplicateRow()
    {
        return rejectDuplicateRow;
    }
    
    /**
     * @param errorDescription The errorDescription to set.
     */
    public void setErrorDescription(String errorDescription)
    {
        this.errorDescription = errorDescription;
    }
	
	/**
	 * @return Returns the errorDescription.
	 */
	public String getErrorDescription()
	{
	    return errorDescription;
	}
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public Object clone()
	{
		UniqueRowsByHashSetMeta retval = (UniqueRowsByHashSetMeta) super.clone();
		
		int nrfields   = compareFields.length;
		
		retval.allocate(nrfields);
		
		for (int i=0;i<nrfields;i++)
		{
			retval.getCompareFields()[i] = compareFields[i]; 
		}

		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
		    storeValues = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "store_values")); //$NON-NLS-1$ //$NON-NLS-2$
			rejectDuplicateRow = "Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "reject_duplicate_row"));
			errorDescription = XMLHandler.getTagValue(stepnode, "error_description"); //$NON-NLS-1$
			
		    Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
		    int nrfields   = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(nrfields);
			
			for (int i=0;i<nrfields;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				compareFields[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
			}

		}
		catch(Exception e)
		{
			throw new KettleXMLException(BaseMessages.getString(PKG, "UniqueRowsByHashSetMeta.Exception.UnableToLoadStepInfoFromXML"), e); //$NON-NLS-1$
		}
	}

	public void setDefault()
	{
		rejectDuplicateRow=false;
		errorDescription=null;
		int nrfields = 0;
		
		allocate(nrfields);		
		
		for (int i=0;i<nrfields;i++)
		{
			compareFields[i] = "field"+i; //$NON-NLS-1$
		}
	}

	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
	{
	}

	public String getXML()
	{
		StringBuffer retval=new StringBuffer();

        retval.append("      "+XMLHandler.addTagValue("store_values",  storeValues)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("      "+XMLHandler.addTagValue("reject_duplicate_row",  rejectDuplicateRow));
		retval.append("      "+XMLHandler.addTagValue("error_description", errorDescription));
		retval.append("    <fields>"); //$NON-NLS-1$
		for (int i=0;i<compareFields.length;i++)
		{
			retval.append("      <field>"); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("name",  compareFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"); //$NON-NLS-1$
		}
		retval.append("      </fields>"); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
            storeValues  = rep.getStepAttributeBoolean(id_step, "store_values"); //$NON-NLS-1$
			rejectDuplicateRow  = rep.getStepAttributeBoolean(id_step, "reject_duplicate_row");
			errorDescription = rep.getStepAttributeString (id_step, "error_description"); //$NON-NLS-1$
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				compareFields[i] = rep.getStepAttributeString (id_step, i, "field_name"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UniqueRowsByHashSetMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
		throws KettleException
	{
		try
		{
			rep.saveStepAttribute(id_transformation, id_step, "reject_duplicate_row",    rejectDuplicateRow);
			rep.saveStepAttribute(id_transformation, id_step, "error_description",  errorDescription);
			for (int i=0;i<compareFields.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name", compareFields[i]); //$NON-NLS-1$
			}
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "UniqueRowsByHashSetMeta.Exception.UnableToSaveStepInfo"), e); //$NON-NLS-1$
		}
	}

	
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;

		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "UniqueRowsByHashSetMeta.CheckResult.StepReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "UniqueRowsByHashSetMeta.CheckResult.NoInputReceivedFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new UniqueRowsByHashSet(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new UniqueRowsByHashSetData();
	}
    public boolean supportsErrorHandling()
    {
        return  isRejectDuplicateRow();
    }
}
