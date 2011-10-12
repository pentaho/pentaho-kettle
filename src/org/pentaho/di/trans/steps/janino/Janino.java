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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        if (log.isRowLevel()) logRowlevel("Read row #"+getLinesRead()+" : "+getInputRowMeta().getString(r));

        try {
	        Object[] outputRowData = calcFields(getInputRowMeta(), r);		
			putRow(data.outputRowMeta, outputRowData);     // copy row to possible alternate rowset(s).
	        if (log.isRowLevel()) {
	        	logRowlevel("Wrote row #"+getLinesWritten()+" : "+data.outputRowMeta.getString(outputRowData));        
	        }
        } catch(Exception e) {
        	if (getStepMeta().isDoingErrorHandling()) {
        		putError(getInputRowMeta(), r, 1L, e.toString(), null, "UJE001");
        	} else {
        	  throw new KettleException(e);
        	}
        }

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
        		data.argumentIndexes = new ArrayList<List<Integer>>();
        		
        		for (int i=0;i<meta.getFormula().length;i++) {
        			List<Integer> argIndexes = new ArrayList<Integer>();
        			data.argumentIndexes.add(argIndexes);
        		}

        		for (int m=0;m<meta.getFormula().length;m++) {
        			List<Integer> argIndexes = data.argumentIndexes.get(m);
            		List<String> parameterNames = new ArrayList<String>();
            		List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

	        		for (int i=0;i<data.outputRowMeta.size();i++) {
	
	        			ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(i);

            			
            			// See if the value is being used in a formula...
            			//
            			if (meta.getFormula()[m].getFormula().contains(valueMeta.getName())) {
            				// If so, add it to the indexes...
            				argIndexes.add(i);

            				Class<?> parameterType;
        					switch(valueMeta.getType()) {
    	        			case ValueMetaInterface.TYPE_STRING    : parameterType = String.class; break;
    	        			case ValueMetaInterface.TYPE_NUMBER    : parameterType = Double.class; break;
    	        			case ValueMetaInterface.TYPE_INTEGER   : parameterType = Long.class; break;
    	        			case ValueMetaInterface.TYPE_DATE      : parameterType = Date.class; break;
    	        			case ValueMetaInterface.TYPE_BIGNUMBER : parameterType = BigDecimal.class; break;
    	        			case ValueMetaInterface.TYPE_BOOLEAN   : parameterType = Boolean.class; break;
    	        			case ValueMetaInterface.TYPE_BINARY    : parameterType = byte[].class; break;
    	        			default: parameterType = String.class; break;
    	        			}
        					parameterTypes.add(parameterType);
                			parameterNames.add(valueMeta.getName());
            			}
            		}
        			
                    JaninoMetaFunction fn = meta.getFormula()[m];
                    if (!Const.isEmpty( fn.getFieldName())) {
                    	
                    	// Create the expression evaluator: is relatively slow so we do it only for the first row...
                    	//
                    	data.expressionEvaluators[m] = new ExpressionEvaluator();
                    	data.expressionEvaluators[m].setParameters(parameterNames.toArray(new String[parameterNames.size()]), parameterTypes.toArray(new Class<?>[parameterTypes.size()]));
                    	data.expressionEvaluators[m].setReturnType(Object.class);
                    	data.expressionEvaluators[m].setThrownExceptions(new Class[] { Exception.class });
                    	data.expressionEvaluators[m].cook(fn.getFormula());
                    } else {
                    	throw new KettleException("Unable to find field name for formula ["+Const.NVL(fn.getFormula(), "")+"]");
                    }
                }                            
        	}
        	
            for (int i=0;i<meta.getFormula().length;i++)
            {
                JaninoMetaFunction fn = meta.getFormula()[i];

                List<Integer> argumentIndexes = data.argumentIndexes.get(i);
                
                // This method can only accept the specified number of values...
                //
                Object[] argumentData = new Object[argumentIndexes.size()];
                for (int x=0;x<argumentIndexes.size();x++) {
                	int index = argumentIndexes.get(x);
                	ValueMetaInterface outputValueMeta = data.outputRowMeta.getValueMeta(index);
                	argumentData[x] = outputValueMeta.convertToNormalStorageType(outputRowData[index]);
                }
                // System.arraycopy(outputRowData, 0, argumentData, 0, argumentData.length);
                
                Object formulaResult  = data.expressionEvaluators[i].evaluate(argumentData);
                    
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
	
}
