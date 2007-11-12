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

package org.pentaho.di.trans.steps.mergerows;

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
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
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

public class MergeRowsMeta extends BaseStepMeta implements StepMetaInterface
{
	private String referenceStepName;
	private StepMeta referenceStepMeta;

	private String compareStepName;  
	private StepMeta compareStepMeta;

    private String flagField;

    private String   keyFields[];
    private String   valueFields[];

	/**
     * @return Returns the keyFields.
     */
    public String[] getKeyFields()
    {
        return keyFields;
    }

    /**
     * @param keyFields The keyFields to set.
     */
    public void setKeyFields(String[] keyFields)
    {
        this.keyFields = keyFields;
    }

    /**
     * @return Returns the valueFields.
     */
    public String[] getValueFields()
    {
        return valueFields;
    }

    /**
     * @param valueFields The valueFields to set.
     */
    public void setValueFields(String[] valueFields)
    {
        this.valueFields = valueFields;
    }

    public MergeRowsMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	/**
     * @return Returns the sendFalseStepname.
     */
    public String getCompareStepName()
    {
		if (compareStepMeta!=null && 
		        compareStepMeta.getName()!=null &&
		        compareStepMeta.getName().length()>0
			   ) 
				return compareStepMeta.getName();
			return null;
   }
 
	/**
     * @return Returns the sendTrueStepname.
     */
    public String getReferenceStepName()
    {
		if (referenceStepMeta!=null && 
		        referenceStepMeta.getName()!=null &&
		        referenceStepMeta.getName().length()>0
			   ) 
				return referenceStepMeta.getName();
			return null;
   }
    

    /**
     * @param sendFalseStepname The sendFalseStepname to set.
     */
    public void setCompareStepName(String sendFalseStepname)
    {
        this.compareStepName = sendFalseStepname;
    }
    
    /**
     * @param sendTrueStepname The sendTrueStepname to set.
     */
    public void setReferenceStepName(String sendTrueStepname)
    {
        this.referenceStepName = sendTrueStepname;
    }
    
    /**
     * @return Returns the sendFalseStep.
     */
    public StepMeta getCompareStepMeta()
    {
        return compareStepMeta;
    }
    
    /**
     * @return Returns the sendTrueStep.
     */
    public StepMeta getReferenceStepMeta()
    {
        return referenceStepMeta;
    }
    
    /**
     * @param sendFalseStep The sendFalseStep to set.
     */
    public void setCompareStepMeta(StepMeta sendFalseStep)
    {
        this.compareStepMeta = sendFalseStep;
    }
	
    /**
     * @param sendTrueStep The sendTrueStep to set.
     */
    public void setReferenceStepMeta(StepMeta sendTrueStep)
    {
        this.referenceStepMeta = sendTrueStep;
    }
	
    /**
     * @return Returns the flagField.
     */
    public String getFlagField()
    {
        return flagField;
    }

    /**
     * @param flagField The flagField to set.
     */
    public void setFlagField(String flagField)
    {
        this.flagField = flagField;
    }

	public void allocate(int nrKeys, int nrValues)
	{
        keyFields = new String[nrKeys];
        valueFields = new String[nrValues];
	}

	public Object clone()
	{
		MergeRowsMeta retval = (MergeRowsMeta)super.clone();

        return retval;
	}
	
	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

        retval.append("    <keys>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<keyFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
        }
        retval.append("    </keys>"+Const.CR); //$NON-NLS-1$
        
        retval.append("    <values>"+Const.CR); //$NON-NLS-1$
        for (int i=0;i<valueFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("value", valueFields[i])); //$NON-NLS-1$ //$NON-NLS-2$
        }
        retval.append("    </values>"+Const.CR); //$NON-NLS-1$

        retval.append(XMLHandler.addTagValue("flag_field", flagField));         //$NON-NLS-1$

		retval.append(XMLHandler.addTagValue("reference", getReferenceStepName()));		 //$NON-NLS-1$
		retval.append(XMLHandler.addTagValue("compare", getCompareStepName()));		 //$NON-NLS-1$
		retval.append("    <compare>"+Const.CR); //$NON-NLS-1$
				
		retval.append("    </compare>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
            
            Node keysnode   = XMLHandler.getSubNode(stepnode, "keys"); //$NON-NLS-1$
            Node valuesnode = XMLHandler.getSubNode(stepnode, "values"); //$NON-NLS-1$
            
		    int nrKeys   = XMLHandler.countNodes(keysnode, "key"); //$NON-NLS-1$
            int nrValues = XMLHandler.countNodes(valuesnode, "value"); //$NON-NLS-1$
            
            allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysnode, "key", i); //$NON-NLS-1$
                keyFields[i] = XMLHandler.getNodeValue(keynode);
            }
            
            for (int i=0;i<nrValues;i++) 
            {
                Node valuenode = XMLHandler.getSubNodeByNr(valuesnode, "value", i); //$NON-NLS-1$
                valueFields[i] = XMLHandler.getNodeValue(valuenode);
            }
            
            flagField = XMLHandler.getTagValue(stepnode, "flag_field"); //$NON-NLS-1$
            
			compareStepName = XMLHandler.getTagValue(stepnode, "compare"); //$NON-NLS-1$
			referenceStepName = XMLHandler.getTagValue(stepnode, "reference"); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("MergeRowsMeta.Exception.UnableToLoadStepInfo"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
        flagField = "flagfield";
        allocate(0,0);
	}
    
    public String[] getInfoSteps()
    {
        if (referenceStepMeta!=null && compareStepMeta!=null)
        {
            return new String[] { referenceStepMeta.getName(), compareStepMeta.getName(), };
        }
        else
        {
            return null;
        }
    }

    /**
     * @param infoSteps The info-step(s) to set
     */
    public void setInfoSteps(StepMeta[] infoSteps)
    {
        if (infoSteps!=null && infoSteps.length==2)
        {
            referenceStepMeta = infoSteps[0];
            compareStepMeta = infoSteps[1];
        }
    }

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
		throws KettleException
	{
		try
		{
            int nrKeys = rep.countNrStepAttributes(id_step, "key_field"); //$NON-NLS-1$
            int nrValues = rep.countNrStepAttributes(id_step, "value_field"); //$NON-NLS-1$
            
			allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++)
            {
                keyFields[i] = rep.getStepAttributeString(id_step, i, "key_field"); //$NON-NLS-1$
            }
            for (int i=0;i<nrValues;i++)
            {
                valueFields[i] = rep.getStepAttributeString(id_step, i, "value_field"); //$NON-NLS-1$
            }

            flagField  =   rep.getStepAttributeString (id_step, "flag_field");  //$NON-NLS-1$

			referenceStepName  =   rep.getStepAttributeString (id_step, "reference");  //$NON-NLS-1$
			compareStepName =      rep.getStepAttributeString (id_step, "compare");  //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeRowsMeta.Exception.UnexpectedErrorReadingStepInfo"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            for (int i=0;i<keyFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "key_field", keyFields[i]); //$NON-NLS-1$
            }

            for (int i=0;i<valueFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "value_field", valueFields[i]); //$NON-NLS-1$
            }

            rep.saveStepAttribute(id_transformation, id_step, "flag_field", flagField); //$NON-NLS-1$

			rep.saveStepAttribute(id_transformation, id_step, "reference", getReferenceStepName()); //$NON-NLS-1$
			rep.saveStepAttribute(id_transformation, id_step, "compare", getCompareStepName()); //$NON-NLS-1$
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("MergeRowsMeta.Exception.UnableToSaveStepInfo")+id_step, e); //$NON-NLS-1$
		}
	}
	
	/**
	 * @param steps optionally search the info step in a list of steps
	 */
	public void searchInfoAndTargetSteps(List<StepMeta> steps)
	{
		referenceStepMeta  = StepMeta.findStep(steps, referenceStepName);
		compareStepMeta = StepMeta.findStep(steps, compareStepName);
	}

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public void getFields(RowMetaInterface r, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        // We don't have any input fields here in "r" as they are all info fields.
        // So we just merge in the info fields.
        //
        if (info!=null)
        {
            boolean found=false;
            for (int i=0;i<info.length && !found;i++) 
            {
                if (info[i]!=null)
                {
                    r.mergeRowMeta(info[i]);
                    found=true;
                }
            }
        }
        
        if (Const.isEmpty(flagField)) throw new KettleStepException(Messages.getString("MergeRowsMeta.Exception.FlagFieldNotSpecified"));
        ValueMetaInterface flagFieldValue = new ValueMeta(flagField, ValueMetaInterface.TYPE_STRING);
        flagFieldValue.setOrigin(name);
        r.addValueMeta(flagFieldValue);

    }

    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		
		if (getReferenceStepName()!=null && getCompareStepName()!=null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MergeRowsMeta.CheckResult.SourceStepsOK"), stepinfo);
			remarks.add(cr);
		}
		else
		if (getReferenceStepName()==null && getCompareStepName()==null)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("MergeRowsMeta.CheckResult.SourceStepsMissing"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("MergeRowsMeta.CheckResult.OneSourceStepMissing"), stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface,  int cnr, TransMeta tr, Trans trans)
	{
		return new MergeRows(stepMeta, stepDataInterface, cnr, tr, trans);
	}

	public StepDataInterface getStepData()
	{
		return new MergeRowsData();
	}

}
