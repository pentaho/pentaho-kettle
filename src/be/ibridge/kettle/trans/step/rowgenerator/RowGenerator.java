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
 
package be.ibridge.kettle.trans.step.rowgenerator;

import java.math.BigDecimal;
import java.util.ArrayList;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


/**
 * Generates a number of (empty or the same) rows
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class RowGenerator extends BaseStep implements StepInterface
{
	private RowGeneratorMeta meta;
	private RowGeneratorData data;
	
	public RowGenerator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(RowGeneratorMeta)getStepMeta().getStepMetaInterface();
		data=(RowGeneratorData)stepDataInterface;
	}
	
    public static final Row buildRow(RowGeneratorMeta meta, RowGeneratorData data, ArrayList remarks)
    {
        Row r=new Row();
        Value value;

        for (int i=0;i<meta.getFieldName().length;i++)
        {
            int valtype = Value.getType(meta.getFieldType()[i]); 
            if (meta.getFieldName()[i]!=null)
            {
                value=new Value(meta.getFieldName()[i], valtype); // build a value!
                String stringValue = meta.getValue()[i];
                
                switch(value.getType())
                {
                case Value.VALUE_TYPE_NUMBER:
                    try
                    {
                        if (meta.getFieldFormat()[i]!=null || meta.getDecimal()[i] !=null ||
                        meta.getGroup()[i]       !=null || meta.getCurrency()[i]!=null    
                        )
                        {
                            if (meta.getFieldFormat()[i]!=null && meta.getFieldFormat()[i].length()>=1) data.df.applyPattern(meta.getFieldFormat()[i]);
                            if (meta.getDecimal()[i] !=null && meta.getDecimal()[i].length()>=1) data.dfs.setDecimalSeparator( meta.getDecimal()[i].charAt(0) );
                            if (meta.getGroup()[i]   !=null && meta.getGroup()[i].length()>=1) data.dfs.setGroupingSeparator( meta.getGroup()[i].charAt(0) );
                            if (meta.getCurrency()[i]!=null && meta.getCurrency()[i].length()>=1) data.dfs.setCurrencySymbol( meta.getCurrency()[i] );
                            
                            data.df.setDecimalFormatSymbols(data.dfs);
                        }
                        
                        value.setValue( data.nf.parse(stringValue).doubleValue() );
                    }
                    catch(Exception e)
                    {
                        String message = "Couldn't parse number field ["+value.getName()+"] with value ["+stringValue+"] -->"+e.toString();
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                    }
                    break;
                case Value.VALUE_TYPE_STRING:
                    value.setValue(stringValue);
                    break;
                case Value.VALUE_TYPE_DATE:
                    try
                    {
                        if (meta.getFieldFormat()[i]!=null)
                        {
                            data.daf.applyPattern(meta.getFieldFormat()[i]);
                            data.daf.setDateFormatSymbols(data.dafs);
                        }
                        
                        value.setValue( data.daf.parse(stringValue) );
                    }
                    catch(Exception e)
                    {
                        String message = "Couldn't parse date field ["+value.getName()+"] with value ["+stringValue+"] -->"+e.toString();
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                    }
                    break;
                    
                case Value.VALUE_TYPE_INTEGER:
                    try
                    {
                        value.setValue( Long.parseLong(stringValue) );
                    }
                    catch(Exception e)
                    {
                        String message = "Couldn't parse Integer field ["+value.getName()+"] with value ["+stringValue+"] -->"+e.toString();
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                    }
                    break;

                case Value.VALUE_TYPE_BIGNUMBER:
                    try
                    {
                        value.setValue( new BigDecimal(stringValue) );
                    }
                    catch(Exception e)
                    {
                        String message = "Couldn't parse BigNumber field ["+value.getName()+"] with value ["+stringValue+"] -->"+e.toString();
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                    }
                    break;
                    
                case Value.VALUE_TYPE_BOOLEAN:
                    value.setValue( "Y".equalsIgnoreCase(stringValue) || "TRUE".equalsIgnoreCase(stringValue));
                    break;
                    
                default:
                    String message = "Please specify the value type of field ["+value.getName()+"] with value ["+stringValue+"] -->";
                    remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));

                }
                // Now add value to the row!
                // This is in fact a copy from the fields row, but now with data.
                r.addValue(value); 
            }
        }
        
        return r;
    }
    	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(RowGeneratorMeta)smi;
        data=(RowGeneratorData)sdi;

		Row r=null;
		boolean retval=true;
		
		if (linesWritten<meta.getRowLimit())
		{
			r=new Row(data.constants); // Copy the data, otherwise it gets manipulated aferwards.
		}
		else
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		putRow(r);

        log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);
		if ((linesWritten>0) && (linesWritten%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesWritten);
		
		return retval;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(RowGeneratorMeta)smi;
        data=(RowGeneratorData)sdi;
        
        if (super.init(smi, sdi))
        {
            // Create a row (constants) with all the values in it...
            ArrayList remarks = new ArrayList(); // stores the errors...
            data.constants = buildRow(meta, data, remarks);
            if (remarks.size()==0) 
            { 
                return true;
            }
            else
            {
                for (int i=0;i<remarks.size();i++)
                {
                    CheckResult cr = (CheckResult) remarks.get(i);
                    log.logError(getStepname(), cr.getText());
                }
            }
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
			logError("Unexpected error in '"+debug+"' : "+e.toString());
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
