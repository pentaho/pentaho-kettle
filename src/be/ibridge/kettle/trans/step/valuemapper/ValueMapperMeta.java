 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/

package be.ibridge.kettle.trans.step.valuemapper;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Maps String values of a certain field to new values
 * 
 * Created on 03-apr-2006
 */

public class ValueMapperMeta extends BaseStepMeta implements StepMetaInterface
{
    private String fieldToUse;
    private String targetField;
    
	private String sourceValue[];
	private String targetValue[];
	
	public ValueMapperMeta()
	{
		super(); // allocate BaseStepMeta
	}
	
	/**
     * @return Returns the fieldName.
     */
    public String[] getSourceValue()
    {
        return sourceValue;
    }
    
    /**
     * @param fieldName The fieldName to set.
     */
    public void setSourceValue(String[] fieldName)
    {
        this.sourceValue = fieldName;
    }
 
    /**
     * @return Returns the fieldValue.
     */
    public String[] getTargetValue()
    {
        return targetValue;
    }
    
    /**
     * @param fieldValue The fieldValue to set.
     */
    public void setTargetValue(String[] fieldValue)
    {
        this.targetValue = fieldValue;
    }
 	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int count)
	{
		sourceValue  = new String[count];
		targetValue = new String[count];
	}

	public Object clone()
	{
		ValueMapperMeta retval = (ValueMapperMeta)super.clone();

		int count=sourceValue.length;
		
		retval.allocate(count);
				
		for (int i=0;i<count;i++)
		{
			retval.sourceValue[i]  = sourceValue[i];
			retval.targetValue[i] = targetValue[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
            fieldToUse = XMLHandler.getTagValue(stepnode, "field_to_use"); //$NON-NLS-1$
            targetField = XMLHandler.getTagValue(stepnode, "target_field"); //$NON-NLS-1$
            
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int count= XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(count);
					
			for (int i=0;i<count;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				sourceValue[i]  = XMLHandler.getTagValue(fnode, "source_value"); //$NON-NLS-1$
				targetValue[i] = XMLHandler.getTagValue(fnode, "target_value"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToReadXML.VALUEMAPPER0004"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		int count=0;
		
		allocate(count);

		for (int i=0;i<count;i++)
		{
			sourceValue[i] = "field"+i; //$NON-NLS-1$
			targetValue[i] = ""; //$NON-NLS-1$
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		if (getTargetField()!=null && getTargetField().length()>0)
        {
		    Value extra = new Value(getTargetField(), Value.VALUE_TYPE_STRING);
            // Lengths etc?
            // Take the max length of all the strings...
            int maxlen = -1;
            for (int i=0;i<targetValue.length;i++)
            {
                if (targetValue[i]!=null && targetValue[i].length()>maxlen) maxlen=targetValue[i].length();
            }
            extra.setLength(maxlen);
            extra.setOrigin(name);
            row.addValue(extra);
        }
		
		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

        retval.append("    "+XMLHandler.addTagValue("field_to_use", fieldToUse)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    "+XMLHandler.addTagValue("target_field", targetField)); //$NON-NLS-1$ //$NON-NLS-2$
        
		retval.append("    <fields>"+Const.CR); //$NON-NLS-1$
		
		for (int i=0;i<sourceValue.length;i++)
		{
			retval.append("      <field>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("source_value", sourceValue[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("target_value", targetValue[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        </field>"+Const.CR); //$NON-NLS-1$
		}
		retval.append("      </fields>"+Const.CR); //$NON-NLS-1$

		return retval.toString();
	}
	
	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
            fieldToUse = rep.getStepAttributeString(id_step, "field_to_use"); //$NON-NLS-1$
            targetField = rep.getStepAttributeString(id_step, "target_field"); //$NON-NLS-1$
            
			int nrfields = rep.countNrStepAttributes(id_step, "source_value"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				sourceValue[i] =          rep.getStepAttributeString(id_step, i, "source_value"); //$NON-NLS-1$
				targetValue[i] = 		rep.getStepAttributeString(id_step, i, "target_value"); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToReadRepository.VALUEMAPPER0005"), e); //$NON-NLS-1$
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            rep.saveStepAttribute(id_transformation, id_step, "field_to_use",  fieldToUse); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, "target_field",  targetField); //$NON-NLS-1$
            
			for (int i=0;i<sourceValue.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "source_value",      sourceValue[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "target_value",     targetValue[i]); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("ValueMapperMeta.RuntimeError.UnableToSaveRepository.VALUEMAPPER0006", ""+id_step), e); //$NON-NLS-1$
		}

	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("ValueMapperMeta.CheckResult.NotReceivingFieldsFromPreviousSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ValueMapperMeta.CheckResult.ReceivingFieldsFromPreviousSteps", ""+prev.size()), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("ValueMapperMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("ValueMapperMeta.CheckResult.NotReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new ValueMapperDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new ValueMapper(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new ValueMapperData();
	}

    /**
     * @return Returns the fieldToUse.
     */
    public String getFieldToUse()
    {
        return fieldToUse;
    }

    /**
     * @param fieldToUse The fieldToUse to set.
     */
    public void setFieldToUse(String fieldToUse)
    {
        this.fieldToUse = fieldToUse;
    }

    /**
     * @return Returns the targetField.
     */
    public String getTargetField()
    {
        return targetField;
    }

    /**
     * @param targetField The targetField to set.
     */
    public void setTargetField(String targetField)
    {
        this.targetField = targetField;
    }

}
