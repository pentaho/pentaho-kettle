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
 
package be.ibridge.kettle.trans.step.calculator;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleValueException;
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
public class Calculator extends BaseStep implements StepInterface
{
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
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;


		Row r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

        if (log.isRowLevel()) log.logRowlevel(toString(), "Read row #"+linesRead+" : "+r);
        
        try
        {
	        calcFields(r);		
			putRow(r);     // copy row to possible alternate rowset(s).
	
	        if (log.isRowLevel()) log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);        
	        if (checkFeedback(linesRead)) logBasic("Linenr "+linesRead);
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
        		logError(Messages.getString("Calculator.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;

        	}
        	if (sendToErrorRow)
        	{
        	   // Simply add this row to the error row
        	   putError(r, 1, errorMessage, null, "CAL001");
        	}



		}
		return true;
	}

    private void calcFields(Row r) throws KettleValueException
    {
        int rowSize = r.size();

        if (meta.getCalculation()!=null)
        for (int i=0;i<meta.getCalculation().length;i++)
        {
            CalculatorMetaFunction fn = meta.getCalculation()[i];
            if (fn.getFieldName()!=null && fn.getFieldName().length()>0)
            {
                Value value  = null;

                Value fieldA = null;
                Value fieldB = null;
                Value fieldC = null;

                if (fn.getCalcType()!=CalculatorMetaFunction.CALC_CONSTANT)
                {
                    if (!Const.isEmpty(fn.getFieldA()))
                    {
                        Integer idxA = (Integer)data.indexCache.get(fn.getFieldA());
                        if (idxA==null) idxA = new Integer( r.searchValueIndex(fn.getFieldA()) );
                        if (idxA.intValue()<0) throw new KettleValueException("Field ["+fn.getFieldA()+"] can't be found in the input row!");
                        data.indexCache.put(fn.getFieldA(), idxA);
                        fieldA = r.getValue(idxA.intValue());
                    }
    
                    if (!Const.isEmpty(fn.getFieldB()))
                    {
                        Integer idxB = (Integer)data.indexCache.get(fn.getFieldB());
                        if (idxB==null) idxB = new Integer( r.searchValueIndex(fn.getFieldB()) );
                        if (idxB.intValue()<0) throw new KettleValueException("Field ["+fn.getFieldB()+"] can't be found in the input row!");
                        data.indexCache.put(fn.getFieldB(), idxB);
                        fieldB = r.getValue(idxB.intValue());
                    }

                    if (!Const.isEmpty(fn.getFieldC()))
                    {
                        Integer idxC = (Integer)data.indexCache.get(fn.getFieldC());
                        if (idxC==null) idxC = new Integer( r.searchValueIndex(fn.getFieldC()) );
                        if (idxC.intValue()<0) throw new KettleValueException("Field ["+fn.getFieldC()+"] can't be found in the input row!");
                        data.indexCache.put(fn.getFieldC(), idxC);
                        fieldC = r.getValue(idxC.intValue());
                    }
                }

                switch(fn.getCalcType())
                {
                case CalculatorMetaFunction.CALC_NONE: 
                    break;
                case CalculatorMetaFunction.CALC_ADD                :  // A + B
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.plus(fieldB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_SUBTRACT           :   // A - B
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.minus(fieldB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_MULTIPLY           :   // A * B
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.multiply(fieldB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_DIVIDE             :   // A / B
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.setType(fn.getValueType());
                        value.divide(fieldB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_SQUARE             :   // A * A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.multiply(fieldA);
                    }
                    break;
                case CalculatorMetaFunction.CALC_SQUARE_ROOT        :   // SQRT( A )
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.sqrt();
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_1          :   // 100 * A / B 
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.multiply(100);
                        value.divide(fieldB);
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_2          :  // A - ( A * B / 100 )
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Value value2 = new Value(fn.getFieldName(), fieldA);
                        value2.multiply(fieldB);
                        value2.divide(100);
                        value.minus(value2);
                    }
                    break;
                case CalculatorMetaFunction.CALC_PERCENT_3          :  // A + ( A * B / 100 )
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Value value2 = new Value(fn.getFieldName(), fieldA);
                        value2.multiply(fieldB);
                        value2.divide(100);
                        value.plus(value2);
                    }
                    break;
                case CalculatorMetaFunction.CALC_COMBINATION_1      :  // A + B * C
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Value value2 = new Value(fn.getFieldName(), fieldB);
                        value2.multiply(fieldC);
                        value.plus(value2);
                    }
                    break;
                case CalculatorMetaFunction.CALC_COMBINATION_2      :  // SQRT( A*A + B*B )
                    {
                        value = new Value(fn.getFieldName(), fieldA);  // A*A
                        value.multiply(fieldA);
                        Value value2 = new Value(fn.getFieldName(), fieldB); // B*B
                        value2.multiply(fieldB);
                        value.plus(value2);
                        value.sqrt();
                    }
                    break;
                case CalculatorMetaFunction.CALC_ROUND_1            :  // ROUND( A )
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.round();
                    }
                    break;
                case CalculatorMetaFunction.CALC_ROUND_2            :  //  ROUND( A , B )
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.round((int)fieldB.getInteger());
                    }
                    break;
                case CalculatorMetaFunction.CALC_CONSTANT           : // Set field to constant value...
                    {
                        value = new Value(fn.getFieldName(), fn.getFieldA());
                        value.convertString(fn.getValueType());
                    }
                    break;
                case CalculatorMetaFunction.CALC_NVL                : // Replace null values with another value
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.nvl(fieldB);
                    }
                    break;                    
                case CalculatorMetaFunction.CALC_ADD_DAYS           : // Add B days to date field A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.add_days(fieldB.getInteger());
                    }
                    break;
               case CalculatorMetaFunction.CALC_YEAR_OF_DATE           : // What is the year (Integer) of a date?
                    {
                       value = new Value(fn.getFieldName(), fieldA);
                       Calendar calendar = Calendar.getInstance();
                       Date date = fieldA.getDate();
                       if (date!=null)
                       {
                           calendar.setTime(date);
                           value.setValue(calendar.get(Calendar.YEAR));
                       }
                       else
                       {
                           throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                       }
                    }
                    break;
                case CalculatorMetaFunction.CALC_MONTH_OF_DATE           : // What is the month (Integer) of a date?
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Calendar calendar = Calendar.getInstance();
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.MONTH)+1);
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_YEAR           : // What is the day of year (Integer) of a date?
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Calendar calendar = Calendar.getInstance();
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.DAY_OF_YEAR));
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_MONTH           : // What is the day of month (Integer) of a date?
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Calendar calendar = Calendar.getInstance();
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.DAY_OF_MONTH));
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_DAY_OF_WEEK           : // What is the day of week (Integer) of a date?
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Calendar calendar = Calendar.getInstance();
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.DAY_OF_WEEK));
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_WEEK_OF_YEAR    : // What is the week of year (Integer) of a date?
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        Calendar calendar = Calendar.getInstance();
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.WEEK_OF_YEAR));
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_WEEK_OF_YEAR_ISO8601   : // What is the week of year (Integer) of a date ISO8601 style?
                    {
                        value = new Value(fn.getFieldName(), fieldA);                        
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                        	// Calendar should not be 'promoted' to class level as it's not
                        	// thread safe (read the java docs).
                        	Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                        	calendar.setMinimalDaysInFirstWeek(4);
                        	calendar.setFirstDayOfWeek(Calendar.MONDAY);
                            calendar.setTime(date);
                            value.setValue(calendar.get(Calendar.WEEK_OF_YEAR));
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;                    
                case CalculatorMetaFunction.CALC_YEAR_OF_DATE_ISO8601     : // What is the year (Integer) of a date ISO8601 style?
                    {
                        value = new Value(fn.getFieldName(), fieldA);                        
                        Date date = fieldA.getDate();
                        if (date!=null)
                        {
                       	    // Calendar should not be 'promoted' to class level as it's not
                    	    // thread safe (read the java docs).
                    	    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
                    	    calendar.setMinimalDaysInFirstWeek(4);
                    	    calendar.setFirstDayOfWeek(Calendar.MONDAY);
                            calendar.setTime(date);
                            
                            int week  = calendar.get(Calendar.WEEK_OF_YEAR);
                            int month = calendar.get(Calendar.MONTH);
                            int year = calendar.get(Calendar.YEAR);
                            // fix up for the year taking into account ISO8601 weeks
                            if ( week >= 52 && month == 0  ) year--;
                            if ( week <= 2  && month == 11 ) year++;
                            value.setValue(year);
                        }
                        else
                        {
                            throw new KettleValueException("Unable to get date from field ["+fieldA+"]");
                        }
                    }
                    break;
                case CalculatorMetaFunction.CALC_BYTE_TO_HEX_ENCODE   : // Byte to Hex encode string field A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.byteToHexEncode();
                    }
                    break;
                case CalculatorMetaFunction.CALC_HEX_TO_BYTE_DECODE   : // Hex to Byte decode string field A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.hexToByteDecode();
                    }
                    break;                    
                case CalculatorMetaFunction.CALC_CHAR_TO_HEX_ENCODE   : // Char to Hex encode string field A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.charToHexEncode();
                    }
                    break;
                case CalculatorMetaFunction.CALC_HEX_TO_CHAR_DECODE   : // Hex to Char decode string field A
                    {
                        value = new Value(fn.getFieldName(), fieldA);
                        value.hexToCharDecode();
                    }
                    break;                    
                default:
                    throw new KettleValueException("Unknown calculation type #"+fn.getCalcType());
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
        for (int i=meta.getCalculation().length-1;i>=0;i--)
        {
            CalculatorMetaFunction fn = meta.getCalculation()[i];
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

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(CalculatorMeta)smi;
		data=(CalculatorData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
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
