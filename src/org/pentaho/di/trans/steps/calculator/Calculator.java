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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
import org.pentaho.di.core.row.ValueMeta;
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
public class Calculator extends BaseStep implements StepInterface
{
    public class FieldIndexes
    {
        public int indexName;
        public int indexA;
        public int indexB;
        public int indexC;
    };    

	private CalculatorMeta meta;
	private CalculatorData data;

	public Calculator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}

	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(CalculatorMeta)smi;
		data=(CalculatorData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            data.outputRowMeta = getInputRowMeta().clone(); 
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
            
            // get all metadata, including source rows and temporary fields.
            data.calcRowMeta = meta.getAllFields(getInputRowMeta()); 
            
            data.fieldIndexes = new FieldIndexes[meta.getCalculation().length];
            List<Integer> tempIndexes = new ArrayList<Integer>();

            // Calculate the indexes of the values and arguments in the target data or temporary data
            // We do this in advance to save time later on.
            //
            for (int i=0;i<meta.getCalculation().length;i++)
            {
                CalculatorMetaFunction function = meta.getCalculation()[i];
                data.fieldIndexes[i] = new FieldIndexes();
                
                if (!Const.isEmpty(function.getFieldName())) 
                {
                    data.fieldIndexes[i].indexName = data.calcRowMeta.indexOfValue(function.getFieldName());
                    if (data.fieldIndexes[i].indexName<0)
                    {
                        // Nope: throw an exception
                        throw new KettleStepException("Unable to find the specified fieldname '"+function.getFieldName()+"' for calculation #"+(i+1));
                    }
                }
                else
                {
                    throw new KettleStepException("There is no name specified for calculated field #"+(i+1));
                }

                if (!Const.isEmpty(function.getFieldA())) 
                {
                    if (function.getCalcType()!=CalculatorMetaFunction.CALC_CONSTANT)
                    {
                        data.fieldIndexes[i].indexA = data.calcRowMeta.indexOfValue(function.getFieldA());
                        if (data.fieldIndexes[i].indexA<0)
                        {
                            // Nope: throw an exception
                            throw new KettleStepException("Unable to find the first argument field '"+function.getFieldName()+" for calculation #"+(i+1));
                        }
                    }
                    else
                    {
                        data.fieldIndexes[i].indexA = -1;
                    }
                }
                else
                {
                    throw new KettleStepException("There is no first argument specified for calculated field #"+(i+1));
                }

                if (!Const.isEmpty(function.getFieldB())) 
                {
                    data.fieldIndexes[i].indexB = data.calcRowMeta.indexOfValue(function.getFieldB());
                    if (data.fieldIndexes[i].indexB<0)
                    {
                        // Nope: throw an exception
                        throw new KettleStepException("Unable to find the second argument field '"+function.getFieldName()+" for calculation #"+(i+1));
                    }
                }
                
                if (!Const.isEmpty(function.getFieldC())) 
                {
                    data.fieldIndexes[i].indexC = data.calcRowMeta.indexOfValue(function.getFieldC());
                    if (data.fieldIndexes[i].indexC<0)
                    {
                        // Nope: throw an exception
                        throw new KettleStepException("Unable to find the third argument field '"+function.getFieldName()+" for calculation #"+(i+1));
                    }
                }
                                
                if (function.isRemovedFromResult())
                {
                    tempIndexes.add(Integer.valueOf(getInputRowMeta().size()+i));
                }
            }
            
            // Convert temp indexes to int[]
            data.tempIndexes = new int[tempIndexes.size()];
            for (int i=0;i<data.tempIndexes.length;i++)
            {
                data.tempIndexes[i] = ((Integer)tempIndexes.get(i)).intValue();
            }
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+linesRead+" : "+r);

        Object[] row = calcFields(getInputRowMeta(), r);		
		putRow(data.outputRowMeta, row);     // copy row to possible alternate rowset(s).

        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);        
        if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);

		return true;
	}


	/**
	 * @param inputRowMeta the input row metadata
	 * @param r the input row (data)
	 * @return A row including the calculations, excluding the temporary values
	 * @throws KettleValueException in case there is a calculation error.
	 */
    private Object[] calcFields(RowMetaInterface inputRowMeta, Object[] r) throws KettleValueException
    {
        // First copy the input data to the new result...
        Object[] calcData = RowDataUtil.resizeArray(r, data.calcRowMeta.size());

        for (int i=0, index=inputRowMeta.size()+i;i<meta.getCalculation().length;i++, index++)
        {
            CalculatorMetaFunction fn = meta.getCalculation()[i];
            if (!Const.isEmpty(fn.getFieldName()))
            {
                ValueMetaInterface targetMeta = data.calcRowMeta.getValueMeta(index);

                // Get the metadata & the data...
                // ValueMetaInterface metaTarget = data.calcRowMeta.getValueMeta(i);
                
                ValueMetaInterface metaA=null;
                Object dataA=null;
                
                if (data.fieldIndexes[i].indexA>=0) 
                {
                    metaA = data.calcRowMeta.getValueMeta( data.fieldIndexes[i].indexA );
                    dataA = calcData[ data.fieldIndexes[i].indexA ];
                }

                ValueMetaInterface metaB=null;
                Object dataB=null;

                if (data.fieldIndexes[i].indexB>=0) 
                {
                    metaB = data.calcRowMeta.getValueMeta( data.fieldIndexes[i].indexB );
                    dataB = calcData[ data.fieldIndexes[i].indexB ];
                }

                ValueMetaInterface metaC=null;
                Object dataC=null;

                if (data.fieldIndexes[i].indexC>=0) 
                {
                    metaC = data.calcRowMeta.getValueMeta( data.fieldIndexes[i].indexC );
                    dataC = calcData[ data.fieldIndexes[i].indexC ];
                }
                
                //The data types are those of the first argument field, convert to the target field.
                // Exceptions: 
                //  - multiply can be string
                //  - constant is string
                //  - all date functions except add days/months
                //  - hex encode / decodes
                
                int resultType;
                if (metaA!=null)
                {
                    resultType=metaA.getType();
                }
                else
                {
                    resultType=ValueMetaInterface.TYPE_NONE;
                }
                                
                switch(fn.getCalcType())
                {
                case CalculatorMetaFunction.CALC_NONE: 
                    break;
                case CalculatorMetaFunction.CALC_ADD                :  // A + B
                    {
                        calcData[index] = ValueDataUtil.plus(metaA, dataA, metaB, dataB);
                        if (metaA.isString() || metaB.isString()) resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                case CalculatorMetaFunction.CALC_SUBTRACT           :   // A - B
                    {
                        calcData[index] = ValueDataUtil.minus(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_MULTIPLY           :   // A * B
                    {
                        calcData[index] = ValueDataUtil.multiply(metaA, dataA, metaB, dataB);
                        if (metaA.isString() || metaB.isString()) resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                case CalculatorMetaFunction.CALC_DIVIDE             :   // A / B
                    {
                        calcData[index] = ValueDataUtil.divide(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_SQUARE             :   // A * A
                    {
                        calcData[index] = ValueDataUtil.multiply(metaA, dataA, metaA, dataA);
                    }
                    break;
                case CalculatorMetaFunction.CALC_SQUARE_ROOT        :   // SQRT( A )
                    {
                        calcData[index] = ValueDataUtil.sqrt(metaA, dataA);
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_1          :   // 100 * A / B 
                    {
                        calcData[index] = ValueDataUtil.percent1(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_2          :  // A - ( A * B / 100 )
                    {
                        calcData[index] = ValueDataUtil.percent2(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_3          :  // A + ( A * B / 100 )
                    {
                        calcData[index] = ValueDataUtil.percent2(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_COMBINATION_1      :  // A + B * C
                    {
                        calcData[index] = ValueDataUtil.combination1(metaA, dataA, metaB, dataB, metaC, dataC);
                    }
                    break;
                case CalculatorMetaFunction.CALC_COMBINATION_2      :  // SQRT( A*A + B*B )
                    {
                        calcData[index] = ValueDataUtil.combination2(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_ROUND_1            :  // ROUND( A )
                    {
                        calcData[index] = ValueDataUtil.round(metaA, dataA);
                    }
                    break;
                case CalculatorMetaFunction.CALC_ROUND_2            :  //  ROUND( A , B )
                    {
                        calcData[index] = ValueDataUtil.round(metaA, dataA, metaB, dataB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_CONSTANT           : // Set field to constant value...
                    {
                        calcData[index] = fn.getFieldA(); // A string
                        resultType = ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                case CalculatorMetaFunction.CALC_NVL                : // Replace null values with another value
                    {
                        calcData[index] = ValueDataUtil.nvl(metaA, dataA, metaB, dataB);
                    }
                    break;                    
                case CalculatorMetaFunction.CALC_ADD_DAYS           : // Add B days to date field A
                    {
                        calcData[index] = ValueDataUtil.addDays(metaA, dataA, metaB, dataB);
                    }
                    break;
               case CalculatorMetaFunction.CALC_YEAR_OF_DATE           : // What is the year (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.yearOfDate(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_MONTH_OF_DATE           : // What is the month (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.monthOfDate(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_YEAR           : // What is the day of year (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.dayOfYear(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_MONTH           : // What is the day of month (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.dayOfMonth(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_WEEK           : // What is the day of week (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.dayOfWeek(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_WEEK_OF_YEAR    : // What is the week of year (Integer) of a date?
                    {
                        calcData[index] = ValueDataUtil.weekOfYear(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_WEEK_OF_YEAR_ISO8601   : // What is the week of year (Integer) of a date ISO8601 style?
                    {
                        calcData[index] = ValueDataUtil.weekOfYearISO8601(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;                    
                case CalculatorMetaFunction.CALC_YEAR_OF_DATE_ISO8601     : // What is the year (Integer) of a date ISO8601 style?
                    {
                        calcData[index] = ValueDataUtil.yearOfDateISO8601(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_INTEGER;
                    }
                    break;
                case CalculatorMetaFunction.CALC_BYTE_TO_HEX_ENCODE   : // Byte to Hex encode string field A
                    {
                        calcData[index] = ValueDataUtil.byteToHexEncode(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                case CalculatorMetaFunction.CALC_HEX_TO_BYTE_DECODE   : // Hex to Byte decode string field A
                    {
                        calcData[index] = ValueDataUtil.hexToByteDecode(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                
                case CalculatorMetaFunction.CALC_CHAR_TO_HEX_ENCODE   : // Char to Hex encode string field A
                    {
                        calcData[index] = ValueDataUtil.charToHexEncode(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;
                case CalculatorMetaFunction.CALC_HEX_TO_CHAR_DECODE   : // Hex to Char decode string field A
                    {
                        calcData[index] = ValueDataUtil.hexToCharDecode(metaA, dataA);
                        resultType=ValueMetaInterface.TYPE_STRING;
                    }
                    break;                    
                default:
                    throw new KettleValueException("Unknown calculation type #"+fn.getCalcType());
                }
                
                // If we don't have a target data type, throw an error.
                // Otherwise the result is non-deterministic.
                //
                if (targetMeta.getType()==ValueMetaInterface.TYPE_NONE)
                {
                    throw new KettleValueException("No datatype is specified for calculation #"+(i+1)+" : "+fn.getFieldName()+" = "+fn.getCalcTypeDesc()+" / "+fn.getCalcTypeLongDesc());
                }
                
                // Convert the data to the correct target data type.
                // 
                if (calcData[index]!=null)
                {
                	if (targetMeta.getType()!=resultType) 
                    {
                        String resultConversionMask = null;

                		switch(resultType)
                		{
                		case ValueMetaInterface.TYPE_INTEGER : resultConversionMask = "0"; break;
                		case ValueMetaInterface.TYPE_NUMBER  : resultConversionMask = "0.0"; break;
                		case ValueMetaInterface.TYPE_DATE    : resultConversionMask = "yyyy/MM/dd HH:mm:ss.SSS"; break;
                		default: break;
                		}
                        ValueMetaInterface resultMeta = new ValueMeta("result", resultType);  // $NON-NLS-1$
                        resultMeta.setConversionMask(resultConversionMask);
                        calcData[index] = targetMeta.convertData(resultMeta, calcData[index]);
                    }
                }
            }
        }
        
        // OK, now we should refrain from adding the temporary fields to the result.
        // So we remove them.
        // 
        return RowDataUtil.removeItems(calcData, data.tempIndexes);
    }

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CalculatorMeta)smi;
		data=(CalculatorData)sdi;
		
		if (super.init(smi, sdi))
		{
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
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$

			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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