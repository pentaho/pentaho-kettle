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
        
        // What if the user didn't specify a data type?
        // In that case we look for the default data type
        // 
        if (fn.getValueType()==ValueMetaInterface.TYPE_NONE)
        {
            int defaultResultType = ValueMetaInterface.TYPE_NONE;
            
            switch(fn.getCalcType())
            {
            case CalculatorMetaFunction.CALC_NONE:  break;
            case CalculatorMetaFunction.CALC_ADD                :  // A + B
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_SUBTRACT           :   // A - B
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_MULTIPLY           :   // A * B
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_DIVIDE             :   // A / B
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_SQUARE             :   // A * A
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_SQUARE_ROOT        :   // SQRT( A )
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_PERCENT_1          :   // 100 * A / B 
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_PERCENT_2          :  // A - ( A * B / 100 )
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_PERCENT_3          :  // A + ( A * B / 100 )
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_COMBINATION_1      :  // A + B * C
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_COMBINATION_2      :  // SQRT( A*A + B*B )
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_ROUND_1            :  // ROUND( A )
                defaultResultType = ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_ROUND_2            :  //  ROUND( A , B )
                defaultResultType = ValueMetaInterface.TYPE_NUMBER;
                break;
            case CalculatorMetaFunction.CALC_CONSTANT           : // Set field to constant value...
                defaultResultType = ValueMetaInterface.TYPE_STRING;
                break;
            case CalculatorMetaFunction.CALC_NVL                : // Replace null values with another value
                break;                    
            case CalculatorMetaFunction.CALC_ADD_DAYS           : // Add B days to date field A
                defaultResultType = ValueMetaInterface.TYPE_DATE;
                break;
           case CalculatorMetaFunction.CALC_YEAR_OF_DATE           : // What is the year (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_MONTH_OF_DATE           : // What is the month (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_DAY_OF_YEAR           : // What is the day of year (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_DAY_OF_MONTH           : // What is the day of month (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_DAY_OF_WEEK           : // What is the day of week (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_WEEK_OF_YEAR    : // What is the week of year (Integer) of a date?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_WEEK_OF_YEAR_ISO8601   : // What is the week of year (Integer) of a date ISO8601 style?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;                    
            case CalculatorMetaFunction.CALC_YEAR_OF_DATE_ISO8601     : // What is the year (Integer) of a date ISO8601 style?
                defaultResultType=ValueMetaInterface.TYPE_INTEGER;
                break;
            case CalculatorMetaFunction.CALC_BYTE_TO_HEX_ENCODE   : // Byte to Hex encode string field A
                defaultResultType=ValueMetaInterface.TYPE_STRING;
                break;
            case CalculatorMetaFunction.CALC_HEX_TO_BYTE_DECODE   : // Hex to Byte decode string field A
                defaultResultType=ValueMetaInterface.TYPE_STRING;
                break;
            case CalculatorMetaFunction.CALC_CHAR_TO_HEX_ENCODE   : // Char to Hex encode string field A
                defaultResultType=ValueMetaInterface.TYPE_STRING;
                break;
            case CalculatorMetaFunction.CALC_HEX_TO_CHAR_DECODE   : // Hex to Char decode string field A
                defaultResultType=ValueMetaInterface.TYPE_STRING;
                break;                    
            default:
                break;
            }
            
            v.setType(defaultResultType);
        }
        
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
