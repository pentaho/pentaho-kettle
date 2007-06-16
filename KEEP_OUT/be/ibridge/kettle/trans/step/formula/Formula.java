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

import java.math.BigDecimal;
import java.util.Date;

import org.jfree.formula.lvalues.LValue;
import org.jfree.formula.lvalues.TypeValuePair;
import org.jfree.formula.parser.FormulaParser;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
import be.ibridge.kettle.core.formula.RowForumulaContext;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * Calculate new field values using pre-defined functions. 
 * 
 * @author Matt
 * @since 8-sep-2005
 */
public class Formula extends BaseStep implements StepInterface
{
	private FormulaMeta meta;
	private FormulaData data;

	public Formula(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(FormulaMeta)smi;
		data=(FormulaData)sdi;

		Row r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first = false;
            
            // Create the context
            data.context = new RowForumulaContext(r);
            data.parser = new FormulaParser();
        }
        else
        {
            data.context.setRow(r);
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+linesRead+" : "+r);

        calcFields(r);		
		putRow(r);     // copy row to possible alternate rowset(s).

        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);        
        if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);

		return true;
	}

    private void calcFields(Row r) throws KettleValueException
    {
        try
        {
            int rowSize = r.size();
    
            if (meta.getFormula()!=null)
            for (int i=0;i<meta.getFormula().length;i++)
            {
                FormulaMetaFunction fn = meta.getFormula()[i];
                if (!Const.isEmpty( fn.getFieldName()))
                {
                    if (data.lValue[i]==null)
                    {
                        data.lValue[i] = data.parser.parse(meta.getFormula()[i].getFormula());
                        data.lValue[i].initialize(data.context);
                    }
                    
                    // Now compute the result.
                    TypeValuePair result = data.lValue[i].evaluate();
                    
                    Value value  = null;
                    Object formulaResult = result.getValue();
                    if (formulaResult instanceof String)
                    {
                        value = new Value(fn.getFieldName(), (String)formulaResult);
                    }
                    else if (formulaResult instanceof Number)
                    {
                        value = new Value(fn.getFieldName(), ((Number)formulaResult).doubleValue());
                    }
                    else if (formulaResult instanceof Integer)
                    {
                        value = new Value(fn.getFieldName(), (long)((Integer)formulaResult).intValue());
                    }
                    else if (formulaResult instanceof Long)
                    {
                        value = new Value(fn.getFieldName(), ((Long)formulaResult).longValue());
                    }
                    else if (formulaResult instanceof Date)
                    {
                        value = new Value(fn.getFieldName(), (Date)formulaResult);
                    }
                    else if (formulaResult instanceof BigDecimal)
                    {
                        value = new Value(fn.getFieldName(), (BigDecimal)formulaResult);
                    }
                    else if (formulaResult instanceof byte[])
                    {
                        value = new Value(fn.getFieldName(), (byte[])formulaResult);
                    }
                    else if (formulaResult instanceof Boolean)
                    {
                        value = new Value(fn.getFieldName(), ((Boolean)formulaResult).booleanValue());
                    }
                    else
                    {
                        value = new Value(fn.getFieldName(), formulaResult.toString());
                    }
                    
                    if (value!=null)
                    {
                        if (fn.getValueType()!=Value.VALUE_TYPE_NONE) 
                        {
                            value.setType(fn.getValueType());
                            value.setLength(fn.getValueLength(), fn.getValuePrecision());
                        }
                        r.addValue(value); // add to the row!
                    }
                }
            }
    
            int inpFieldsRemoved = 0;
            // OK, see which fields we need to remove from the result?
            for (int i=meta.getFormula().length-1;i>=0;i--)
            {
                FormulaMetaFunction fn = meta.getFormula()[i];
                if (fn.isRemovedFromResult())
                {
                    // get the index of the value...
                    Integer idx = (Integer) data.indexCache.get(fn.getFieldName());
                    if (idx!=null)
                    {
                    	int y = idx.intValue();
                    	if ( y < rowSize )
                    	{
                            // value from the original row.
               	            r.removeValue(idx.intValue());
               	            inpFieldsRemoved++;
                    	}
                    	else
                    	{
                    		// calculated value used in calculation
                    		r.removeValue(idx.intValue() - inpFieldsRemoved);
                    	}
                    }
                    else
                    {
                        // calculated value not used in other calculation
                        r.removeValue(rowSize+i); 
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new KettleValueException(e);
        }
    }

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(FormulaMeta)smi;
		data=(FormulaData)sdi;
		
		if (super.init(smi, sdi))
		{
            // Add init code here.
            
            // Create a set of LValues to put the parsed results in...
            data.lValue = new LValue[meta.getFormula().length];
            
		    return true;
		}
		return false;
	}
	
	//
	// Run is were the action happens!
	public void run()
	{
		try
		{
			logBasic("Starting to run...");
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error in "+" : "+e.toString());
            logError(Const.getStackTracker(e));
            setErrors(1);
			stopAll();
		}
		finally
		{
			dispose(meta, data);
			logSummary();
			markStop();
		}
	}
}
