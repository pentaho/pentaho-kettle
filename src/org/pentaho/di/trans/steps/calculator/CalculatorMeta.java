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
 
package org.pentaho.di.trans.steps.calculator;

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
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
        StringBuffer retval = new StringBuffer(300);
       
        if (calculation!=null)
        for (int i=0;i<calculation.length;i++)
        {
            retval.append("       ").append(calculation[i].getXML()).append(Const.CR);
        }
        
        return retval.toString();
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	CalculatorMeta m = (CalculatorMeta)obj;
            return (getXML() == m.getXML());
        }

        return false;
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

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
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
	
    public void getFields(RowMetaInterface row, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException
    {
        for (int i=0;i<calculation.length;i++)
        {
            CalculatorMetaFunction fn = calculation[i];
            if (!fn.isRemovedFromResult())
            {
                if (!Const.isEmpty( fn.getFieldName()) ) // It's a new field!
                {
                    ValueMetaInterface v = getValueMeta(fn, origin);
                    row.addValueMeta(v);
                }
            }
        }
    }

    private ValueMetaInterface getValueMeta(CalculatorMetaFunction fn, String origin)
    {
        ValueMetaInterface v = new ValueMeta(fn.getFieldName(), fn.getValueType());
        v.setLength(fn.getValueLength());
        v.setPrecision(fn.getValuePrecision());
        v.setOrigin(origin);
        v.setComments(fn.getCalcTypeDesc());
        
        return v;
    }

    public RowMetaInterface getAllFields(RowMetaInterface inputRowMeta)
    {
        RowMetaInterface rowMeta = inputRowMeta.clone();
        
        for (int i=0;i<calculation.length;i++)
        {
            CalculatorMetaFunction fn = calculation[i];
            if (!Const.isEmpty(fn.getFieldName())) // It's a new field!
            {
                ValueMetaInterface v = getValueMeta(fn, null);
                rowMeta.addValueMeta(v);
            }
        }
        return rowMeta;
    }
    
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CalculatorMeta.CheckResult.FieldsReceived", ""+prev.size()), stepMeta);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputOk"), stepMeta);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputError"), stepMeta);
			remarks.add(cr);
		}
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
