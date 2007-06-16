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
 
package org.pentaho.di.trans.steps.rowgenerator;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.StringUtil;

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
	
    public static final RowMetaAndData buildRow(RowGeneratorMeta meta, RowGeneratorData data, ArrayList remarks)
    {
        RowMetaInterface rowMeta=new RowMeta();
        Object[] rowData = new Object[meta.getFieldName().length];
        
        for (int i=0;i<meta.getFieldName().length;i++)
        {
            int valtype = ValueMeta.getType(meta.getFieldType()[i]); 
            if (meta.getFieldName()[i]!=null)
            {
                ValueMetaInterface value=new ValueMeta(meta.getFieldName()[i], valtype); // build a value!
                value.setLength(meta.getFieldLength()[i]);
                value.setPrecision(meta.getFieldPrecision()[i]);
                String stringValue = meta.getValue()[i];
                
                // If the value is empty: consider it to be NULL.
                if (stringValue==null || stringValue.length()==0)
                {
                    rowData[i]=null;
                    
                    if ( value.getType() == ValueMetaInterface.TYPE_NONE )
                    {
                        String message = Messages.getString("RowGenerator.CheckResult.SpecifyTypeError", value.getName(), stringValue);
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));                    
                    }
                }
                else
                {
                    switch(value.getType())
                    {
                    case ValueMetaInterface.TYPE_NUMBER:
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
                            
                            rowData[i] = new Double( data.nf.parse(stringValue).doubleValue() );
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("RowGenerator.BuildRow.Error.Parsing.Number", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_STRING:
                        rowData[i] = stringValue;
                        break;
                        
                    case ValueMetaInterface.TYPE_DATE:
                        try
                        {
                            if (meta.getFieldFormat()[i]!=null)
                            {
                                data.daf.applyPattern(meta.getFieldFormat()[i]);
                                data.daf.setDateFormatSymbols(data.dafs);
                            }
                            
                            rowData[i] = data.daf.parse(stringValue);
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("RowGenerator.BuildRow.Error.Parsing.Date", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_INTEGER:
                        try
                        {
                            rowData[i] = new Long( Long.parseLong(stringValue) );
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("RowGenerator.BuildRow.Error.Parsing.Integer", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
    
                    case ValueMetaInterface.TYPE_BIGNUMBER:
                        try
                        {
                            rowData[i] = new BigDecimal(stringValue);
                        }
                        catch(Exception e)
                        {
                            String message = Messages.getString("RowGenerator.BuildRow.Error.Parsing.BigNumber", value.getName(), stringValue, e.toString() );
                            remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                        }
                        break;
                        
                    case ValueMetaInterface.TYPE_BOOLEAN:
                        rowData[i] = new Boolean( "Y".equalsIgnoreCase(stringValue) || "TRUE".equalsIgnoreCase(stringValue) );
                        break;
                        
                    case ValueMetaInterface.TYPE_BINARY:                    
                        rowData[i] = stringValue.getBytes();                        
                        break;                        
                        
                    default:
                        String message = Messages.getString("RowGenerator.CheckResult.SpecifyTypeError", value.getName(), stringValue);
                        remarks.add(new CheckResult(CheckResult.TYPE_RESULT_ERROR, message, null));
                    }
                }
                // Now add value to the row!
                // This is in fact a copy from the fields row, but now with data.
                rowMeta.addValueMeta(value); 
            }
        }
        
        return new RowMetaAndData(rowMeta, rowData);
    }
    	
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(RowGeneratorMeta)smi;
        data=(RowGeneratorData)sdi;

		Object[] r=null;
		boolean retval=true;
		
		if (linesWritten<data.rowLimit)
		{
			r=data.constants.getRowMeta().cloneRow(data.constants.getData());
        }
		else
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		putRow(data.constants.getRowMeta(), r);

        if (log.isRowLevel())
        {
            log.logRowlevel(toString(), Messages.getString("RowGenerator.Log.Wrote.Row", Long.toString(linesWritten), r.toString()) );
        }
        
        if (checkFeedback(linesRead)) logBasic( Messages.getString("RowGenerator.Log.LineNr", Long.toString(linesWritten) ) );
		
		return retval;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(RowGeneratorMeta)smi;
        data=(RowGeneratorData)sdi;
        
        if (super.init(smi, sdi))
        {
            // Determine the number of rows to generate...
            data.rowLimit = Const.toLong(StringUtil.environmentSubstitute(meta.getRowLimit()), -1L);
            
            if (data.rowLimit<0L) // Unable to parse
            {
                logError(Messages.getString("RowGenerator.Wrong.RowLimit.Number"));
                return false; // fail
            }
            
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
                    logError(cr.getText());
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
			logBasic(Messages.getString("RowGenerator.Log.StartToRun"));
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError("Unexpected error : "+e.toString());
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