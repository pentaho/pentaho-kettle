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

package be.ibridge.kettle.trans.step.setvariable;

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
 * Sets environment variables based on content in certain fields of a single input row.
 * 
 * Created on 27-apr-2006
 */

public class SetVariableMeta extends BaseStepMeta implements StepMetaInterface
{
    public static final int VARIABLE_TYPE_JVM              = 0;
    public static final int VARIABLE_TYPE_PARENT_JOB       = 1;
    public static final int VARIABLE_TYPE_GRAND_PARENT_JOB = 2;
    public static final int VARIABLE_TYPE_ROOT_JOB         = 3;
    
    private static final String variableTypeCode[] = { "JVM", "PARENT_JOB", "GP_JOB", "ROOT_JOB" };
    private static final String variableTypeDesc[] = 
        { 
            "Valid in the Java Virtual Machine", 
            "Valid in the parent job", 
            "Valid in the grand-parent job", 
            "Valid in the root job" 
        };
    
	private String fieldName[];
	private String variableName[];
    private int variableType[];
	
	public SetVariableMeta()
	{
		super(); // allocate BaseStepMeta
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
     * @param fieldValue The fieldValue to set.
     */
    public void setVariableName(String[] fieldValue)
    {
        this.variableName = fieldValue;
    }
    
    /**
     * @return Returns the fieldValue.
     */
    public String[] getVariableName()
    {
        return variableName;
    }
    
    /**
     * @return Returns the local variable flag: true if this variable is only valid in the parents job.
     */
    public int[] getVariableType()
    {
        return variableType;
    }

    /**
     * @param variableType The variable type, see also VARIABLE_TYPE_...
     * @return the variable type code for this variable type
     */
    public static final String getVariableTypeCode(int variableType)
    {
        return variableTypeCode[variableType];
    }
    
    /**
     * @param variableType The variable type, see also VARIABLE_TYPE_...
     * @return the variable type description for this variable type
     */
    public static final String getVariableTypeDescription(int variableType)
    {
        return variableTypeDesc[variableType];
    }

    /**
     * @param variableType The code or description of the variable type 
     * @return The variable type
     */
    public static final int getVariableType(String variableType)
    {
        for (int i=0;i<variableTypeCode.length;i++)
        {
            if (variableTypeCode[i].equalsIgnoreCase(variableType)) return i;
        }
        for (int i=0;i<variableTypeDesc.length;i++)
        {
            if (variableTypeDesc[i].equalsIgnoreCase(variableType)) return i;
        }
        return VARIABLE_TYPE_JVM;
    }

    /**
     * @param localVariable The localVariable to set.
     */
    public void setVariableType(int[] localVariable)
    {
        this.variableType = localVariable;
    }
    
    public static final String[] getVariableTypeDescriptions()
    {
        return variableTypeDesc;
    }
    

	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
		readData(stepnode);
	}

	public void allocate(int count)
	{
		fieldName  = new String[count];
		variableName = new String[count];
        variableType = new int[count];
	}

	public Object clone()
	{
		SetVariableMeta retval = (SetVariableMeta)super.clone();

		int count=fieldName.length;
		
		retval.allocate(count);
				
		for (int i=0;i<count;i++)
		{
			retval.fieldName[i]  = fieldName[i];
			retval.variableName[i] = variableName[i];
            retval.variableType[i] = variableType[i];
		}
		
		return retval;
	}
	
	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{
			Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
			int count= XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
			
			allocate(count);
					
			for (int i=0;i<count;i++)
			{
				Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
				
				fieldName[i]  = XMLHandler.getTagValue(fnode, "field_name"); //$NON-NLS-1$
				variableName[i] = XMLHandler.getTagValue(fnode, "variable_name"); //$NON-NLS-1$
                variableType[i] = getVariableType(XMLHandler.getTagValue(fnode, "variable_type")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleXMLException(Messages.getString("SetVariableMeta.RuntimeError.UnableToReadXML.SETVARIABLE0004"), e); //$NON-NLS-1$
		}
	}
	
	public void setDefault()
	{
		int count=0;
		
		allocate(count);

		for (int i=0;i<count;i++)
		{
			fieldName[i] = "field"+i; //$NON-NLS-1$
			variableName[i] = ""; //$NON-NLS-1$
            variableType[i] = VARIABLE_TYPE_JVM;
		}
	}

	public Row getFields(Row r, String name, Row info)
	{
		Row row;
		if (r==null) row=new Row(); // give back values
		else         row=r;         // add to the existing row of values...

		return row;
	}

	public String getXML()
	{
        StringBuffer retval = new StringBuffer();

		retval.append("    <fields>"+Const.CR); //$NON-NLS-1$
		
		for (int i=0;i<fieldName.length;i++)
		{
			retval.append("      <field>"+Const.CR); //$NON-NLS-1$
			retval.append("        "+XMLHandler.addTagValue("field_name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
			retval.append("        "+XMLHandler.addTagValue("variable_name", variableName[i])); //$NON-NLS-1$ //$NON-NLS-2$
            retval.append("        "+XMLHandler.addTagValue("variable_type", getVariableTypeCode(variableType[i]))); //$NON-NLS-1$
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
			int nrfields = rep.countNrStepAttributes(id_step, "field_name"); //$NON-NLS-1$
			
			allocate(nrfields);
	
			for (int i=0;i<nrfields;i++)
			{
				fieldName[i] =          rep.getStepAttributeString(id_step, i, "field_name"); //$NON-NLS-1$
				variableName[i] = 		rep.getStepAttributeString(id_step, i, "variable_name"); //$NON-NLS-1$
                variableType[i] = getVariableType(rep.getStepAttributeString(id_step, i, "variable_type")); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SetVariableMeta.RuntimeError.UnableToReadRepository.SETVARIABLE0005"), e); //$NON-NLS-1$
		}
	}


    public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
			for (int i=0;i<fieldName.length;i++)
			{
				rep.saveStepAttribute(id_transformation, id_step, i, "field_name",      fieldName[i]); //$NON-NLS-1$
				rep.saveStepAttribute(id_transformation, id_step, i, "variable_name",     variableName[i]); //$NON-NLS-1$
                rep.saveStepAttribute(id_transformation, id_step, i, "variable_type",   getVariableTypeCode(variableType[i])); //$NON-NLS-1$
			}
		}
		catch(Exception e)
		{
			throw new KettleException(Messages.getString("SetVariableMeta.RuntimeError.UnableToSaveRepository.SETVARIABLE0006", ""+id_step), e); //$NON-NLS-1$
		}

	}

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("SetVariableMeta.CheckResult.NotReceivingFieldsFromPreviousSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SetVariableMeta.CheckResult.ReceivingFieldsFromPreviousSteps", ""+prev.size()), stepinfo); //$NON-NLS-1$ //$NON-NLS-2$
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("SetVariableMeta.CheckResult.ReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("SetVariableMeta.CheckResult.NotReceivingInfoFromOtherSteps"), stepinfo); //$NON-NLS-1$
			remarks.add(cr);
		}
	}

	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new SetVariableDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans)
	{
		return new SetVariable(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	public StepDataInterface getStepData()
	{
		return new SetVariableData();
	}
}
