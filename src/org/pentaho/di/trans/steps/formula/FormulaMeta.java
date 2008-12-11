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
 
package org.pentaho.di.trans.steps.formula;

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
    
	public void loadXML(Node stepnode, List<DatabaseMeta> databases,  Map<String, Counter> counters) throws KettleXMLException
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

	public void readRep(Repository rep, long id_step, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException 
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
	
	@Override
	public void getFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space) throws KettleStepException {
        
		getAllFields(row, name, info, nextStep, space, false);
        
    }
	
	public void getAllFields(RowMetaInterface row, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, boolean all) throws KettleStepException {
        
        for (int i=0;i<formula.length;i++)
        {
            FormulaMetaFunction fn = formula[i];
            if (!fn.isRemovedFromResult() || all)
            {
                if (fn.getFieldName()!=null && fn.getFieldName().length()>0) // It's a new field!
                {
                    ValueMetaInterface v = new ValueMeta(fn.getFieldName(), fn.getValueType());
                    v.setLength(fn.getValueLength(), fn.getValuePrecision());
                    v.setOrigin(name);
                    row.addValueMeta(v);
                }
            }
        }
    }
    
	/**
	 * Checks the settings of this step and puts the findings in a remarks List.
	 * @param remarks The list to put the remarks in @see org.pentaho.di.core.CheckResult
	 * @param stepMeta The stepMeta to help checking
	 * @param prev The fields coming from the previous step
	 * @param input The input step names
	 * @param output The output step names
	 * @param info The fields that are used as information by the step
	 */
	public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepinfo, RowMetaInterface prev, String input[], String output[], RowMetaInterface info)
	{
		CheckResult cr;
		if (prev==null || prev.size()==0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, Messages.getString("FormulaMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FormulaMeta.CheckResult.FieldsReceived", ""+prev.size()), stepinfo);
			remarks.add(cr);
		}
		
		// See if we have input streams leading to this step!
		if (input.length>0)
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_OK, Messages.getString("FormulaMeta.CheckResult.ExpectedInputOk"), stepinfo);
			remarks.add(cr);
		}
		else
		{
			cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, Messages.getString("FormulaMeta.CheckResult.ExpectedInputError"), stepinfo);
			remarks.add(cr);
		}
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
