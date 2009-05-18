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
	
	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
            storeValues  = rep.getStepAttributeBoolean(id_step, "store_values"); //$NON-NLS-1$
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
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
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
}
