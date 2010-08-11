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
 
package org.pentaho.di.trans.steps.datagrid;

import java.util.List;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
public class DataGrid extends BaseStep implements StepInterface
{
	private static Class<?> PKG = DataGridMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private DataGridMeta meta;
	private DataGridData data;
	
	public DataGrid(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(DataGridMeta)getStepMeta().getStepMetaInterface();
		data=(DataGridData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		if (data.linesWritten>=meta.getDataLines().size()) // no more rows to be written
        {
            setOutputDone();
            return false;
        }
        
        if ( first )
        {
        	// The output meta is the original input meta + the 
        	// additional constant fields.
        	
        	first = false;
        	data.linesWritten = 0;
        	
            data.outputRowMeta = new RowMeta();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        	
        	// Use these metadata values to convert data...
        	//
        	data.convertMeta = data.outputRowMeta.clone();
        	for (ValueMetaInterface valueMeta : data.convertMeta.getValueMetaList()) {
        		valueMeta.setType(ValueMetaInterface.TYPE_STRING);
        	}
        }
        
        Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        List<String> outputLine = meta.getDataLines().get(data.linesWritten);
        
    	for (int i=0;i<data.outputRowMeta.size();i++) {
    		ValueMetaInterface valueMeta = data.outputRowMeta.getValueMeta(i);
    		ValueMetaInterface convertMeta = data.convertMeta.getValueMeta(i);
    		String valueData = outputLine.get(i);
    		
    		outputRowData[i] = valueMeta.convertDataFromString(valueData, convertMeta, null, null, 0);
    	}
    	
    	putRow(data.outputRowMeta, outputRowData);
    	data.linesWritten++;
        
        if (log.isRowLevel())
        {
            log.logRowlevel(toString(), BaseMessages.getString(PKG, "DataGrid.Log.Wrote.Row", Long.toString(getLinesWritten()), data.outputRowMeta.getString(outputRowData)) );
        }
        
        if (checkFeedback(getLinesWritten())) 
        {
        	if(log.isBasic()) logBasic( BaseMessages.getString(PKG, "DataGrid.Log.LineNr", Long.toString(getLinesWritten()) ));
        }
		
		return true;
	}
		
}