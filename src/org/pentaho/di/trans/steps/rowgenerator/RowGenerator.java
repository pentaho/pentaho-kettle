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
 
package org.pentaho.di.trans.steps.rowgenerator;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Generates a number of (empty or the same) rows
 * 
 * @author Matt
 * @since 4-apr-2003
 */
public class RowGenerator extends BaseStep implements StepInterface
{
	private static Class<?> PKG = RowGeneratorMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private RowGeneratorMeta meta;
	private RowGeneratorData data;
	
	public RowGenerator(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(RowGeneratorMeta)getStepMeta().getStepMetaInterface();
		data=(RowGeneratorData)stepDataInterface;
	}
	
    public static final RowMetaAndData buildRow(RowGeneratorMeta meta, List<CheckResultInterface> remarks, String origin)
    {
        RowMetaInterface rowMeta=new RowMeta();
        Object[] rowData = RowDataUtil.allocateRowData(meta.getFieldName().length);
        
        for (int i=0;i<meta.getFieldName().length;i++)
        {
            int valtype = ValueMeta.getType(meta.getFieldType()[i]); 
            if (meta.getFieldName()[i]!=null)
            {
                ValueMetaInterface valueMeta=new ValueMeta(meta.getFieldName()[i], valtype); // build a value!
                valueMeta.setLength(meta.getFieldLength()[i]);
                valueMeta.setPrecision(meta.getFieldPrecision()[i]);
                valueMeta.setConversionMask(meta.getFieldFormat()[i]);
                valueMeta.setGroupingSymbol(meta.getGroup()[i]);
                valueMeta.setDecimalSymbol(meta.getDecimal()[i]);
                valueMeta.setOrigin(origin);

                ValueMetaInterface stringMeta = valueMeta.clone();
                stringMeta.setType(ValueMetaInterface.TYPE_STRING);
                
                String stringValue = meta.getValue()[i];
                
                // If the value is empty: consider it to be NULL.
                if (Const.isEmpty(stringValue))
                {
                    rowData[i]=null;
                    
                    if ( valueMeta.getType() == ValueMetaInterface.TYPE_NONE )
                    {
                        String message = BaseMessages.getString(PKG, "RowGenerator.CheckResult.SpecifyTypeError", valueMeta.getName(), stringValue);
                        remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));                    
                    }
                }
                else
                {
                	// Convert the data from String to the specified type ...
                	//
                	try {
                		rowData[i]=valueMeta.convertData(stringMeta, stringValue);
                	}
                	catch(KettleValueException e) {
                		switch(valueMeta.getType()) {
                		case ValueMetaInterface.TYPE_NUMBER:
	                		{
	                            String message = BaseMessages.getString(PKG, "RowGenerator.BuildRow.Error.Parsing.Number", valueMeta.getName(), stringValue, e.toString() );
	                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
	                		}
                            break;
                		case ValueMetaInterface.TYPE_DATE:
	                		{
	                            String message = BaseMessages.getString(PKG, "RowGenerator.BuildRow.Error.Parsing.Date", valueMeta.getName(), stringValue, e.toString() );
	                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
	                		}
                            break;
                		case ValueMetaInterface.TYPE_INTEGER:
	                		{
	                            String message = BaseMessages.getString(PKG, "RowGenerator.BuildRow.Error.Parsing.Integer", valueMeta.getName(), stringValue, e.toString() );
	                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
	                		}
	                		break;
                        case ValueMetaInterface.TYPE_BIGNUMBER:
	                        {
	                            String message = BaseMessages.getString(PKG, "RowGenerator.BuildRow.Error.Parsing.BigNumber", valueMeta.getName(), stringValue, e.toString() );
	                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
	                        }
	                        break;
                        default:
                        	// Boolean and binary don't throw errors normally, so it's probably an unspecified error problem...
	                        {
	                            String message = BaseMessages.getString(PKG, "RowGenerator.CheckResult.SpecifyTypeError", valueMeta.getName(), stringValue);
	                            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, message, null));
	                        }
                        	break;
                		}
                	}
                }
                // Now add value to the row!
                // This is in fact a copy from the fields row, but now with data.
                rowMeta.addValueMeta(valueMeta); 
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
		
		if (first) {
		  first=false;
		  getRow();
		}
		
		if (data.rowsWritten<data.rowLimit)
		{
			r=data.outputRowMeta.cloneRow(data.outputRowData);
        }
		else
		{
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
		
		putRow(data.outputRowMeta, r);
		data.rowsWritten++;
		
        if (log.isRowLevel())
        {
            logRowlevel(BaseMessages.getString(PKG, "RowGenerator.Log.Wrote.Row", Long.toString(data.rowsWritten), data.outputRowMeta.getString(r)) );
        }
        
        if (checkFeedback(data.rowsWritten)) 
        {
        	if(log.isBasic()) logBasic( BaseMessages.getString(PKG, "RowGenerator.Log.LineNr", Long.toString(data.rowsWritten) ) );
        }
		
		return retval;
	}

    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
        meta=(RowGeneratorMeta)smi;
        data=(RowGeneratorData)sdi;
        
        if (super.init(smi, sdi))
        {
            // Determine the number of rows to generate...
            data.rowLimit = Const.toLong(environmentSubstitute(meta.getRowLimit()), -1L);
            data.rowsWritten = 0L;
            
            if (data.rowLimit<0L) // Unable to parse
            {
                logError(BaseMessages.getString(PKG, "RowGenerator.Wrong.RowLimit.Number"));
                return false; // fail
            }
            
            // Create a row (constants) with all the values in it...
            List<CheckResultInterface> remarks = new ArrayList<CheckResultInterface>(); // stores the errors...
            RowMetaAndData outputRow = buildRow(meta, remarks, getStepname());
            if (!remarks.isEmpty()) 
            { 
                for (int i=0;i<remarks.size();i++)
                {
                    CheckResult cr = (CheckResult) remarks.get(i);
                    logError(cr.getText());
                }
                return false;
            }
            data.outputRowData = outputRow.getData();
            data.outputRowMeta = outputRow.getRowMeta();
            return true;
        }
        return false;
    }
    
    @Override
    public boolean canProcessOneRow() {
    	return true;
    }
}