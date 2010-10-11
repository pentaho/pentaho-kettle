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
 
package org.pentaho.di.trans.steps.janino;

import java.math.BigDecimal;
import java.util.Date;

import org.codehaus.janino.ExpressionEvaluator;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
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
public class Janino extends BaseStep implements StepInterface
{
	private JaninoMeta meta;
	private JaninoData data;

	public Janino(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(JaninoMeta)smi;
		data=(JaninoData)sdi;

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

            // Calculate replace indexes...
            //
            data.replaceIndex = new int[meta.getFormula().length];
            for (int i=0;i<meta.getFormula().length;i++) {
            	JaninoMetaFunction fn = meta.getFormula()[i];
            	if (!Const.isEmpty(fn.getReplaceField())) {
            		data.replaceIndex[i] = getInputRowMeta().indexOfValue(fn.getReplaceField());
            		if (data.replaceIndex[i]<0) {
            			throw new KettleException("Unknown field specified to replace with a formula result: ["+fn.getReplaceField()+"]");
            		}
            	} else {
            		data.replaceIndex[i] = -1;
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
        	Object[] outputRowData = RowDataUtil.createResizedCopy(r, data.outputRowMeta.size());
        	int tempIndex = rowMeta.size();
        	        	
        	// Initialize evaluators etc.  Only do it once.
        	//
        	if (data.expressionEvaluators==null) {
        		data.expressionEvaluators = new ExpressionEvaluator[meta.getFormula().length];
        		
        		String[] parameterNames = new String[data.outputRowMeta.size()];
        		Class<?>[] parameterTypes = new Class[data.outputRowMeta.size()];
        		for (int i=0;i<data.outputRowMeta.size();i++) {
        			switch(data.outputRowMeta.getValueMeta(i).getType()) {
        			case ValueMetaInterface.TYPE_STRING    : parameterTypes[i] = String.class; break;
        			case ValueMetaInterface.TYPE_NUMBER    : parameterTypes[i] = Double.class; break;
        			case ValueMetaInterface.TYPE_INTEGER   : parameterTypes[i] = Long.class; break;
        			case ValueMetaInterface.TYPE_DATE      : parameterTypes[i] = Date.class; break;
        			case ValueMetaInterface.TYPE_BIGNUMBER : parameterTypes[i] = BigDecimal.class; break;
        			case ValueMetaInterface.TYPE_BOOLEAN   : parameterTypes[i] = Boolean.class; break;
        			case ValueMetaInterface.TYPE_BINARY    : parameterTypes[i] = byte[].class; break;
        			default: parameterTypes[i] = String.class; break;
        			}
        			parameterNames[i] = data.outputRowMeta.getValueMeta(i).getName();
        		}
        		        		
                for (int i=0;i<meta.getFormula().length;i++) {
                    JaninoMetaFunction fn = meta.getFormula()[i];
                    if (!Const.isEmpty( fn.getFieldName())) {
                    	
                    	// Create the expression evaluator: is relatively slow so we do it only for the first row...
                    	//
                    	data.expressionEvaluators[i] = new ExpressionEvaluator();
                    	data.expressionEvaluators[i].setParameters(parameterNames, parameterTypes);
                    	data.expressionEvaluators[i].setReturnType(Object.class);
                    	data.expressionEvaluators[i].setThrownExceptions(new Class[] { Exception.class });
                    	data.expressionEvaluators[i].cook(fn.getFormula());
                    } else {
                    	throw new KettleException("Unable to find field name for formula ["+Const.NVL(fn.getFormula(), "")+"]");
                    }
                }                            
        	}
        	
            for (int i=0;i<meta.getFormula().length;i++)
            {
                JaninoMetaFunction fn = meta.getFormula()[i];

                // This method can only accept the specified number of values...
                //
                Object[] rowData = new Object[data.outputRowMeta.size()];
                System.arraycopy(outputRowData, 0, rowData, 0, rowData.length);
                
                Object formulaResult  = data.expressionEvaluators[i].evaluate(rowData);
   
                // Calculate the return type on the first row...
                //
                if (data.returnType[i]<0) {
                    if (formulaResult instanceof String) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_STRING;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_STRING) {
                    		throw new KettleValueException("Please specify a String type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else if (formulaResult instanceof Integer) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_INTEGER;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_INTEGER) {
                    		throw new KettleValueException("Please specify an Integer type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else if (formulaResult instanceof Long) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_LONG;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_INTEGER) {
                    		throw new KettleValueException("Please specify an Integer type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else if (formulaResult instanceof BigDecimal) { //BigDecimal must be before Number since this is also instanceof Number
                    	data.returnType[i] = JaninoData.RETURN_TYPE_BIGDECIMAL;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_BIGNUMBER) {
                    		throw new KettleValueException("Please specify a BigNumber type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}                   	
                    } else if (formulaResult instanceof Number) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_NUMBER;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_NUMBER) {
                    		throw new KettleValueException("Please specify a Number type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else if (formulaResult instanceof Date) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_DATE;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_DATE) {
                    		throw new KettleValueException("Please specify a Date type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}

                    } else if (formulaResult instanceof byte[]) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_BYTE_ARRAY;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_BINARY) {
                    		throw new KettleValueException("Please specify a Binary type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else if (formulaResult instanceof Boolean) {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_BOOLEAN;
                    	if (fn.getValueType()!=ValueMetaInterface.TYPE_BOOLEAN) {
                    		throw new KettleValueException("Please specify a Boolean type to parse ["+formulaResult.getClass().getName()+"] for field ["+fn.getFieldName()+"] as a result of formula ["+fn.getFormula()+"]");
                    	}
                    } else {
                    	data.returnType[i] = JaninoData.RETURN_TYPE_STRING;
                    }
                }

                Object value;
                if (formulaResult==null) {
                	value=null;
                } else {
	                switch(data.returnType[i]) {
	                case JaninoData.RETURN_TYPE_STRING  : value = formulaResult.toString(); break;
	                case JaninoData.RETURN_TYPE_NUMBER  : value = new Double(((Number)formulaResult).doubleValue()); break;
	                case JaninoData.RETURN_TYPE_INTEGER : value = new Long( ((Integer)formulaResult).intValue() ); break;
	                case JaninoData.RETURN_TYPE_LONG : value = (Long)formulaResult; break;
	                case JaninoData.RETURN_TYPE_DATE : value = (Date)formulaResult; break;
	                case JaninoData.RETURN_TYPE_BIGDECIMAL : value = (BigDecimal)formulaResult; break;
	                case JaninoData.RETURN_TYPE_BYTE_ARRAY : value = (byte[])formulaResult; break;
	                case JaninoData.RETURN_TYPE_BOOLEAN : value = (Boolean)formulaResult; break;
	                default: value = null;
	                }
                }
                    
                // We're done, store it in the row with all the data, including the temporary data...
                //
                if (data.replaceIndex[i]<0) {
                	outputRowData[tempIndex++] = value;
                } else {
                	outputRowData[data.replaceIndex[i]] = value;
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
		meta=(JaninoMeta)smi;
		data=(JaninoData)sdi;
		
		if (super.init(smi, sdi))
		{
            // Add init code here.
			
			// Return data type discovery is expensive, let's discover them one time only.
			//
            data.returnType = new int[meta.getFormula().length];
			for (int i=0;i<meta.getFormula().length;i++) {
				data.returnType[i] = -1;
			}
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
