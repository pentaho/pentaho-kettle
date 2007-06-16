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
 
package be.ibridge.kettle.trans.step.formula;

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
 * Contains the meta-data for the Formula step: calculates ad-hoc formula's
 * Powered by Pentaho's "libformula"
 * 
 * Created on 22-feb-2007
 */

public class FormulaMeta extends BaseStepMeta implements StepMetaInterface
{
    /** The formula calculations to be performed */
    private FormulaMetaFunction[] formula;
    
    public FormulaMeta()
	{
		super(); // allocate BaseStepMeta
	}

    public FormulaMetaFunction[] getFormula()
    {
        return formula;
    }
    
    public void setFormula(FormulaMetaFunction[] calcTypes)
    {
        this.formula = calcTypes;
    }
    
    public void allocate(int nrCalcs)
    {
        formula = new FormulaMetaFunction[nrCalcs];
    }
    
	public void loadXML(Node stepnode, ArrayList databases, Hashtable counters)
		throws KettleXMLException
	{
        int nrCalcs   = XMLHandler.countNodes(stepnode,   FormulaMetaFunction.XML_TAG);
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            Node calcnode = XMLHandler.getSubNodeByNr(stepnode, FormulaMetaFunction.XML_TAG, i);
            formula[i] = new FormulaMetaFunction(calcnode);
        }
	}
    
    public String getXML()
    {
        StringBuffer retval = new StringBuffer();
       
        if (formula!=null)
        for (int i=0;i<formula.length;i++)
        {
            retval.append("       "+formula[i].getXML()+Const.CR);
        }
        
        return retval.toString();
    }

    public boolean equals(Object obj)
    {       
        if (obj != null && (obj.getClass().equals(this.getClass())))
        {
        	FormulaMeta m = (FormulaMeta)obj;
            return (getXML() == m.getXML());
        }

        return false;
    }        
    
	public Object clone()
	{
		FormulaMeta retval = (FormulaMeta) super.clone();
        if (formula!=null)
        {
            retval.allocate(formula.length);
            for (int i=0;i<formula.length;i++) retval.getFormula()[i] = (FormulaMetaFunction) formula[i].clone();
        }
        else
        {
            retval.allocate(0);
        }
		return retval;
	}

	public void setDefault()
	{
        formula = new FormulaMetaFunction[0]; 
	}

	public void readRep(Repository rep, long id_step, ArrayList databases, Hashtable counters)
		throws KettleException
	{
        int nrCalcs     = rep.countNrStepAttributes(id_step, "field_name");
        allocate(nrCalcs);
        for (int i=0;i<nrCalcs;i++)
        {
            formula[i] = new FormulaMetaFunction(rep, id_step, i);
        }
	}
	
	public void saveRep(Repository rep, long id_transformation, long id_step)
		throws KettleException
	{
        for (int i=0;i<formula.length;i++)
        {
            formula[i].saveRep(rep, id_transformation, id_step, i);
        }
	}
	
    public Row getFields(Row r, String name, Row info) throws KettleStepException
    {
        Row row;
        if (r==null) row=new Row(); // give back values
        else         row=r;         // add to the existing row of values...
        
        for (int i=0;i<formula.length;i++)
        {
            FormulaMetaFunction fn = formula[i];
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
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CalculatorMeta.CheckResult.FieldsReceived", ""+prev.size()), stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputOk"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("CalculatorMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
	}
	
	public StepDialogInterface getDialog(Shell shell, StepMetaInterface info, TransMeta transMeta, String name)
	{
		return new FormulaDialog(shell, info, transMeta, name);
	}

	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans)
	{
		return new Formula(stepMeta, stepDataInterface, cnr, tr, trans);
	}
	
	public StepDataInterface getStepData()
	{
		return new FormulaData();
	}


}
