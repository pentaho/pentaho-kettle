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

package be.ibridge.kettle.trans.step.mergerows;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
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
	
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
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

        retval.append("    <keys>"+Const.CR);
        for (int i=0;i<keyFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("key", keyFields[i]));
        }
        retval.append("    </keys>"+Const.CR);
        
        retval.append("    <values>"+Const.CR);
        for (int i=0;i<valueFields.length;i++)
        {
            retval.append("      "+XMLHandler.addTagValue("value", valueFields[i]));
        }
        retval.append("    </values>"+Const.CR);

        retval.append(XMLHandler.addTagValue("flag_field", flagField));        

		retval.append(XMLHandler.addTagValue("reference", getReferenceStepName()));		
		retval.append(XMLHandler.addTagValue("compare", getCompareStepName()));		
		retval.append("    <compare>"+Const.CR);
				
		retval.append("    </compare>"+Const.CR);

		return retval.toString();
	}

	private void readData(Node stepnode)
		throws KettleXMLException
	{
		try
		{ 
            
            Node keysnode   = XMLHandler.getSubNode(stepnode, "keys");
            Node valuesnode = XMLHandler.getSubNode(stepnode, "values");
            
		    int nrKeys   = XMLHandler.countNodes(keysnode, "key");
            int nrValues = XMLHandler.countNodes(valuesnode, "value");
            
            allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++) 
            {
                Node keynode = XMLHandler.getSubNodeByNr(keysnode, "key", i);
                keyFields[i] = XMLHandler.getNodeValue(keynode);
            }
            
            for (int i=0;i<nrValues;i++) 
            {
                Node valuenode = XMLHandler.getSubNodeByNr(valuesnode, "value", i);
                valueFields[i] = XMLHandler.getNodeValue(valuenode);
            }
            
            flagField = XMLHandler.getTagValue(stepnode, "flag_field");
            
			compareStepName = XMLHandler.getTagValue(stepnode, "compare");
			referenceStepName = XMLHandler.getTagValue(stepnode, "reference");
		}
		catch(Exception e)
		{
			throw new KettleXMLException("Unable to load step info from XML", e);
		}
	}
	
	public void setDefault()
	{
        allocate(0,0);
	}
    
    public String[] getInfoSteps()
    {
        return new String[] { referenceStepName, compareStepName }; 
    }


	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
		try
		{
            int nrKeys = rep.countNrStepAttributes(id_step, "key_field");
            int nrValues = rep.countNrStepAttributes(id_step, "value_field");
            
			allocate(nrKeys, nrValues);
            
            for (int i=0;i<nrKeys;i++)
            {
                keyFields[i] = rep.getStepAttributeString(id_step, i, "key_field");
            }
            for (int i=0;i<nrValues;i++)
            {
                valueFields[i] = rep.getStepAttributeString(id_step, i, "value_field");
            }

            flagField  =   rep.getStepAttributeString (id_step, "flag_field"); 

			referenceStepName  =   rep.getStepAttributeString (id_step, "reference"); 
			compareStepName =      rep.getStepAttributeString (id_step, "compare"); 
		}
		catch(Exception e)
		{
			throw new KettleException("Unexpected error reading step information from the repository", e);
		}
	}

	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
		try
		{
            for (int i=0;i<keyFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "key_field", keyFields[i]);
            }

            for (int i=0;i<valueFields.length;i++)
            {
                rep.saveStepAttribute(id_transformation, id_step, i, "value_field", valueFields[i]);
            }

            rep.saveStepAttribute(id_transformation, id_step, "flag_field", flagField);

			rep.saveStepAttribute(id_transformation, id_step, "reference", getReferenceStepName());
			rep.saveStepAttribute(id_transformation, id_step, "compare", getCompareStepName());
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	public void searchInfoAndTargetSteps(ArrayList steps)
	{
		referenceStepMeta  = TransMeta.findStep(steps, referenceStepName);
		compareStepMeta = TransMeta.findStep(steps, compareStepName);
	}

	public boolean chosesTargetSteps()
	{
	    return false;
	}

	public String[] getTargetSteps()
	{
	    return null;
	}
    
    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        Value flagFieldValue = new Value(flagField, Value.VALUE_TYPE_STRING);
        flagFieldValue.setOrigin(name);
        
        r.addValue(flagFieldValue);
        
        return r;
    }

	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
        /*
		CheckResult cr;
		
		if (getReferenceStepName()!=null && getCompareStepName()!=null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Both 'true' and 'false' steps are specified.  I know how to split the incoming stream(s) of data.", stepinfo);
			remarks.add(cr);
		}
		else
		if (getReferenceStepName()==null && getCompareStepName()==null)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Neither 'true' and 'false' steps are specified.  Only when the condition is true, rows are sent to the next steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Please specify both the 'true' AND 'false' steps.  That way Kettle know for sure where to send the data after computing the condition.", stepinfo);
			remarks.add(cr);
		}
        */
        
        CheckResult cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "This step is not yet verified: not yet implemented.", stepinfo);
        remarks.add(cr);

	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new MergeRowsDialog(shell, info, transMeta, name);
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
