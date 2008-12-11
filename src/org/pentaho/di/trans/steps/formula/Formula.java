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

import java.math.BigDecimal;
import java.util.Date;

import org.jfree.formula.lvalues.LValue;
import org.jfree.formula.lvalues.TypeValuePair;
import org.jfree.formula.parser.FormulaParser;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



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

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first = false;
            
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);

            data.tempRowMeta = getInputRowMeta().clone();
            meta.getAllFields(data.tempRowMeta, getStepname(), null, null, this, true);

            // Create the context
            data.context = new RowForumulaContext(data.tempRowMeta);
            data.parser = new FormulaParser();
            
            data.nrRemoved = 0;
            for (int i=0;i<meta.getFormula().length;i++)
            {
            	if (meta.getFormula()[i].isRemovedFromResult())
            	{
            		data.nrRemoved++;
            	}
            }
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+getLinesRead()+" : "+r);

        Object[] outputRowData = calcFields(getInputRowMeta(), r);		
		putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).

        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+getLinesWritten()+" : "+r);        
        if (checkFeedback(getLinesRead())) logBasic("Linenr "+getLinesRead());

		return true;
	}

    private Object[] calcFields(RowMetaInterface rowMeta, Object[] r) throws KettleValueException
    {
        try
        {
        	Object[] tempRowData = RowDataUtil.createResizedCopy(r, rowMeta.size() + meta.getFormula().length);
        	int tempIndex = rowMeta.size();
        	
        	// Assign this tempRowData to the formula context
        	//
        	data.context.setRowData(tempRowData);
        	
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
                    
                    Object value  = null;
                    Object formulaResult = result.getValue();
                    if (formulaResult instanceof String)
                    {
                        value = (String)formulaResult;
                    }
                    else if (formulaResult instanceof Number)
                    {
                        value = new Double(((Number)formulaResult).doubleValue());
                    }
                    else if (formulaResult instanceof Integer)
                    {
                        value = new Long( ((Integer)formulaResult).intValue() );
                    }
                    else if (formulaResult instanceof Long)
                    {
                        value = (Long)formulaResult;
                    }
                    else if (formulaResult instanceof Date)
                    {
                        value = (Date)formulaResult;
                    }
                    else if (formulaResult instanceof BigDecimal)
                    {
                        value = (BigDecimal)formulaResult;
                    }
                    else if (formulaResult instanceof byte[])
                    {
                        value = (byte[])formulaResult;
                    }
                    else if (formulaResult instanceof Boolean)
                    {
                        value = (Boolean)formulaResult;
                    }
                    else
                    {
                        value = formulaResult.toString();
                    }
                    
                    // We're done, store it in the row with all the data, including the temporary data...
                    //
                    tempRowData[tempIndex++] = value;
                }
            }
            
            if (data.nrRemoved==0) return tempRowData; // This is a valid result.
            
            // Copy over the values we want.
            // Only keep those we're interested in.
            //
            Object[] outputRowData = RowDataUtil.createResizedCopy(r, data.outputRowMeta.size());
            int outputIndex=rowMeta.size();
    
            // OK, see which fields we need to keep from the result?
            //
            for (int i=meta.getFormula().length-1;i>=0;i--)
            {
                FormulaMetaFunction fn = meta.getFormula()[i];
                if (!fn.isRemovedFromResult())
                {
                	outputRowData[outputIndex++] = tempRowData[rowMeta.size() + i];
                }
            }
            
            return outputRowData;
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
