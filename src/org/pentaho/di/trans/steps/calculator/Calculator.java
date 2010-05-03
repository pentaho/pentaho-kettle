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
import org.pentaho.di.i18n.BaseMessages;
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
	private static Class<?> PKG = CalculatorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
                        throw new KettleStepException(BaseMessages.getString(PKG, "Calculator.Error.UnableFindField",function.getFieldName(),""+(i+1)));
                    }
                }
                else
                {
                    throw new KettleStepException(BaseMessages.getString(PKG, "Calculator.Error.NoNameField",""+(i+1)));
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
                data.fieldIndexes[i].indexC=-1;
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

        if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "Calculator.Log.ReadRow")+getLinesRead()+" : "+getInputRowMeta().getString(r));
        boolean sendToErrorRow=false;
        String errorMessage = null;
        
        try{        
	        Object[] row = calcFields(getInputRowMeta(), r);		
			putRow(data.outputRowMeta, row);     // copy row to possible alternate rowset(s).
	
	        if (log.isRowLevel()) logRowlevel("Wrote row #"+getLinesWritten()+" : "+getInputRowMeta().getString(r));
	        if (checkFeedback(getLinesRead())) 
	        {
	        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "Calculator.Log.Linenr",""+getLinesRead()));
	        }
        }
        catch(KettleException e)
        {
        	if (getStepMeta().isDoingErrorHandling())
        	{
                  sendToErrorRow = true;
                  errorMessage = e.toString();
        	}
        	else
        	{
	            logError(BaseMessages.getString(PKG, "Calculator.ErrorInStepRunning" + " : "+ e.getMessage()));
	            throw new KettleStepException(BaseMessages.getString(PKG, "Calculator.ErrorInStepRunning"), e);
        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(getInputRowMeta(), r, 1, errorMessage, null, "CALC001");
        	}
        }
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
                case CalculatorMetaFunction.CALC_COPY_OF_FIELD      : // Create a copy of field A
	                {
	                    calcData[index] = dataA;
	                }
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
                        if (metaA.isDate()) resultType=ValueMetaInterface.TYPE_INTEGER; 
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
                        calcData[index] = ValueDataUtil.percent3(metaA, dataA, metaB, dataB);
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
                case CalculatorMetaFunction.CALC_CRC32   : // CRC32
	                {
	                    calcData[index] = ValueDataUtil.ChecksumCRC32(metaA, dataA);
	                    resultType=ValueMetaInterface.TYPE_INTEGER;
	                }
                break;  
                case CalculatorMetaFunction.CALC_ADLER32   : // ADLER32
                {
                    calcData[index] = ValueDataUtil.ChecksumAdler32(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_MD5   : // MD5
                {
                    calcData[index] = ValueDataUtil.createChecksum(metaA, dataA,"MD5");
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_SHA1   : // SHA-1
                {
                    calcData[index] = ValueDataUtil.createChecksum(metaA, dataA,"SHA-1");
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_LEVENSHTEIN_DISTANCE  : // LEVENSHTEIN DISTANCE 
                {
                    calcData[index] = ValueDataUtil.getLevenshtein_Distance(metaA, dataA,metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_METAPHONE  : // METAPHONE 
                {
                    calcData[index] = ValueDataUtil.get_Metaphone(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_DOUBLE_METAPHONE  : // Double METAPHONE 
                {
                    calcData[index] = ValueDataUtil.get_Double_Metaphone(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_ABS            :  // ABS( A )
                {
                    calcData[index] = ValueDataUtil.abs(metaA, dataA);
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_TIME_FROM_DATE           : // Remove Time from field A
                {
                    calcData[index] = ValueDataUtil.removeTimeFromDate(metaA, dataA);
                }
                break;
                case CalculatorMetaFunction.CALC_DATE_DIFF                :  // DateA - DateB
                {
                    calcData[index] = ValueDataUtil.DateDiff(metaA, dataA, metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_ADD3                :  // A + B + C
                {
                    calcData[index] = ValueDataUtil.plus3(metaA, dataA, metaB, dataB, metaC, dataC);
                    if (metaA.isString() || metaB.isString()|| metaC.isString()) resultType=ValueMetaInterface.TYPE_STRING;  
                }
                break;
                case CalculatorMetaFunction.CALC_INITCAP            :  // InitCap( A )
                {
                    calcData[index] = ValueDataUtil.initCap(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_UPPER_CASE            :  // UpperCase( A )
                {
                    calcData[index] = ValueDataUtil.upperCase(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_LOWER_CASE            :  // UpperCase( A )
                {
                    calcData[index] = ValueDataUtil.lowerCase(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_MASK_XML            :  // escapeXML( A )
                {
                    calcData[index] = ValueDataUtil.escapeXML(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_USE_CDATA           :  // CDATA( A )
                {
                    calcData[index] = ValueDataUtil.useCDATA(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_CR           :  // REMOVE CR FROM A 
                {
                    calcData[index] = ValueDataUtil.removeCR(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_LF          :  // REMOVE LF FROM A 
                {
                    calcData[index] = ValueDataUtil.removeLF(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_CRLF          :  // REMOVE CRLF FROM A 
                {
                    calcData[index] = ValueDataUtil.removeCRLF(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_TAB          :  // REMOVE TAB FROM A 
                {
                    calcData[index] = ValueDataUtil.removeTAB(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_GET_ONLY_DIGITS          :  // GET ONLY DIGITS FROM A 
                {
                    calcData[index] = ValueDataUtil.getDigits(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_REMOVE_DIGITS          :  // REMOVE DIGITS FROM A 
                {
                    calcData[index] = ValueDataUtil.removeDigits(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_STRING_LEN          :  // RETURN THE LENGTH OF A 
                {
                    calcData[index] = ValueDataUtil.stringLen(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_INTEGER; 
                }
                break;
                case CalculatorMetaFunction.CALC_LOAD_FILE_CONTENT_BINARY   :  // LOAD CONTENT OF A FILE A IN A BLOB 
                {
                    calcData[index] = ValueDataUtil.loadFileContentInBinary(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_BINARY; 
                }
                break;
                case CalculatorMetaFunction.CALC_ADD_TIME_TO_DATE          : // Add time B to a date A
                {
                    calcData[index] = ValueDataUtil.addTimeToDate(metaA, dataA,metaB, dataB,metaC, dataC);
                    resultType=ValueMetaInterface.TYPE_DATE; 
                }
                break;
                case CalculatorMetaFunction.CALC_QUARTER_OF_DATE          : // What is the quarter (Integer) of a date?
                {                   
                    calcData[index] = ValueDataUtil.quarterOfDate(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_SUBSTITUTE_VARIABLE      : // variable substitution in string
                {                   
                    calcData[index] = environmentSubstitute(dataA.toString());
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_UNESCAPE_XML            :  // UnescapeXML( A )
                {
                    calcData[index] = ValueDataUtil.unEscapeXML(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_ESCAPE_HTML            :  // EscapeHTML( A )
                {
                    calcData[index] = ValueDataUtil.escapeHTML(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_UNESCAPE_HTML            :  // UnescapeHTML( A )
                {
                    calcData[index] = ValueDataUtil.unEscapeHTML(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_ESCAPE_SQL            :  // EscapeSQL( A )
                {
                    calcData[index] = ValueDataUtil.escapeSQL(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING; 
                }
                break;
                case CalculatorMetaFunction.CALC_DATE_WORKING_DIFF            :  // DateWorkingDiff( A , B)
                {
                    calcData[index] = ValueDataUtil.DateWorkingDiff(metaA, dataA, metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_INTEGER; 
                }
                break;
                case CalculatorMetaFunction.CALC_ADD_MONTHS           : // Add B months to date field A
                {
                    calcData[index] = ValueDataUtil.addMonths(metaA, dataA, metaB, dataB);
                }
                break;
                case CalculatorMetaFunction.CALC_CHECK_XML_FILE_WELL_FORMED      : // Check if file A is well formed
                {
                    calcData[index] = ValueDataUtil.isXMLFileWellFormed(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_BOOLEAN; 
                }
                break;
                case CalculatorMetaFunction.CALC_CHECK_XML_WELL_FORMED      : // Check if xml A is well formed
                {
                    calcData[index] = ValueDataUtil.isXMLWellFormed(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_BOOLEAN;
                }
                break;
                case CalculatorMetaFunction.CALC_GET_FILE_ENCODING      : // Get file encoding from a file A
                {
                    calcData[index] = ValueDataUtil.getFileEncoding(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_DAMERAU_LEVENSHTEIN  : // DAMERAULEVENSHTEIN DISTANCE 
                {
                    calcData[index] = ValueDataUtil.getDamerauLevenshtein_Distance(metaA, dataA,metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_NEEDLEMAN_WUNSH  : // NEEDLEMANWUNSH DISTANCE 
                {
                    calcData[index] = ValueDataUtil.getNeedlemanWunsch_Distance(metaA, dataA,metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_INTEGER;
                }
                break;
                case CalculatorMetaFunction.CALC_JARO  : // Jaro DISTANCE 
                {
                    calcData[index] = ValueDataUtil.getJaro_Similitude(metaA, dataA,metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_NUMBER;
                }
                break;
                case CalculatorMetaFunction.CALC_JARO_WINKLER  : // Jaro DISTANCE 
                {
                    calcData[index] = ValueDataUtil.getJaroWinkler_Similitude(metaA, dataA,metaB, dataB);
                    resultType=ValueMetaInterface.TYPE_NUMBER;
                }
                break;
                case CalculatorMetaFunction.CALC_SOUNDEX  : // SOUNDEX 
                {
                    calcData[index] = ValueDataUtil.get_SoundEx(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                case CalculatorMetaFunction.CALC_REFINED_SOUNDEX : // REFINEDSOUNDEX 
                {
                    calcData[index] = ValueDataUtil.get_RefinedSoundEx(metaA, dataA);
                    resultType=ValueMetaInterface.TYPE_STRING;
                }
                break;
                default:
                    throw new KettleValueException(BaseMessages.getString(PKG, "Calculator.Log.UnknownCalculationType")+fn.getCalcType());
                }
                
                // If we don't have a target data type, throw an error.
                // Otherwise the result is non-deterministic.
                //
                if (targetMeta.getType()==ValueMetaInterface.TYPE_NONE)
                {
                    throw new KettleValueException(BaseMessages.getString(PKG, "Calculator.Log.NoType")+(i+1)+" : "+fn.getFieldName()+" = "+fn.getCalcTypeDesc()+" / "+fn.getCalcTypeLongDesc());
                }
                
                // Convert the data to the correct target data type.
                // 
                if (calcData[index]!=null)
                {
                	if (targetMeta.getType()!=resultType) 
                    {
                        ValueMetaInterface resultMeta = new ValueMeta("result", resultType);  // $NON-NLS-1$
                        resultMeta.setConversionMask(fn.getConversionMask());
                        resultMeta.setGroupingSymbol(fn.getGroupingSymbol());
                        resultMeta.setDecimalSymbol(fn.getDecimalSymbol());
                        resultMeta.setCurrencySymbol(fn.getCurrencySymbol());
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

}