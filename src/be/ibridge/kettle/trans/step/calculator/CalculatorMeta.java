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
 
package be.ibridge.kettle.trans.step.calculator;

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


/**
 * Contains the meta-data for the Calculator step: calculates predefined formula's
 * 
 * Created on 08-sep-2005
 */

public class CalculatorMeta extends BaseStepMeta implements StepMetaInterface
{
    /** The calculations to be performed */
    private CalculatorMetaFunction[] calculation;
    
    public CalculatorMeta()
	{
		super(); // allocate BaseStepMeta
	}

    public CalculatorMetaFunction[] getCalculation()
    {
        return calculation;
    }
    
    public void setCalculation(CalculatorMetaFunction[] calcTypes)
    {
        this.calculation = calcTypes;
    }
    
    public void allocate(int nrCalcs)
    {
        calculation = new CalculatorMetaFunction[nrCalcs];
    }
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
        int nrCalcs   = XMLHandler.countNodes(stepnode,   CalculatorMetaFunction.XML_TAG);
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            Node calcnode = XMLHandler.getSubNodeByNr(stepnode, CalculatorMetaFunction.XML_TAG, i);
            calculation[i] = new CalculatorMetaFunction(calcnode);
        }
	}
    
    public String getXML()
    {
        String xml="";
        
        if (calculation!=null)
        for (int i=0;i<calculation.length;i++)
        {
            xml+="       "+calculation[i].getXML()+Const.CR;
        }
        
        return xml;
    }

	public Object clone()
	{
		CalculatorMeta retval = (CalculatorMeta) super.clone();
        if (calculation!=null)
        {
            retval.allocate(calculation.length);
            for (int i=0;i<calculation.length;i++) retval.getCalculation()[i] = (CalculatorMetaFunction) calculation[i].clone();
        }
        else
        {
            retval.allocate(0);
        }
		return retval;
	}

	public void setDefault()
	{
        calculation = new CalculatorMetaFunction[0]; 
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
        int nrCalcs     = rep.countNrStepAttributes(id_step, "field_name");
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            calculation[i] = new CalculatorMetaFunction(rep, id_step, i);
        }
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
        for (int i=0;i<calculation.length;i++)
        {
            calculation[i].saveRep(rep, id_transformation, id_step, i);
        }
	}
	
    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        Row row;
        if (r==null) row=new Row(); // give back values
        else         row=r;         // add to the existing row of values...
        
        for (int i=0;i<calculation.length;i++)
        {
            CalculatorMetaFunction fn = calculation[i];
            if (!fn.isRemovedFromResult())
            {
                if (fn.getFieldName()!=null && fn.getFieldName().length()>0) // It's a new field!
                {
                    Value v = new Value(fn.getFieldName(), fn.getValueType());
                    v.setLength(fn.getValueLength(), fn.getValuePrecision());
                    v.setOrigin(name);
                    row.addValue(v);
                }
            }
        }
    
        return row;

    }
    
	public void check(ArrayList remarks, StepMeta stepinfo, Row prev, String input[], String output[], Row info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving "+prev.size()+" fields", stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new CalculatorDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Calculator(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new CalculatorData();
	}


}
