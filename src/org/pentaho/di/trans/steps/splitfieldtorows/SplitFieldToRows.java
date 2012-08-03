/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.splitfieldtorows;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

public class SplitFieldToRows extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SplitFieldToRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SplitFieldToRowsMeta meta;
	private SplitFieldToRowsData data;
	
	public SplitFieldToRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private boolean splitField(RowMetaInterface rowMeta, Object[] rowData) throws KettleException
	{
		if (first)
		{
			first = false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			String realSplitFieldName=environmentSubstitute(meta.getSplitField());
			data.fieldnr = rowMeta.indexOfValue(realSplitFieldName);
			
			int numErrors = 0;
			if (Const.isEmpty(meta.getNewFieldname()))
			{
				logError(BaseMessages.getString(PKG, "SplitFieldToRows.Log.NewFieldNameIsNull")); //$NON-NLS-1$
				numErrors++;
			}

			if (data.fieldnr < 0)
			{
				logError(BaseMessages.getString(PKG, "SplitFieldToRows.Log.CouldNotFindFieldToSplit", realSplitFieldName)); //$NON-NLS-1$
				numErrors++;
			}
			
			if (!rowMeta.getValueMeta(data.fieldnr).isString())
			{
				logError(BaseMessages.getString(PKG, "SplitFieldToRows.Log.SplitFieldNotValid",realSplitFieldName)); //$NON-NLS-1$
				numErrors++;
			}

			if(meta.includeRowNumber())	
			{
				String realRowNumberField=environmentSubstitute(meta.getRowNumberField());
				if(Const.isEmpty(realRowNumberField))
				{
					logError(BaseMessages.getString(PKG, "SplitFieldToRows.Exception.RownrFieldMissing"));
					numErrors++;
				}
			}
			
			if (numErrors > 0)
			{
				setErrors(numErrors);
				stopAll();
				return false;
			}
			
			data.splitMeta = rowMeta.getValueMeta(data.fieldnr);
		}
		
		String originalString = data.splitMeta.getString(rowData[data.fieldnr]);
		if (originalString == null) {
			originalString = "";
		}
		
		if(meta.includeRowNumber() && meta.resetRowNumber()) data.rownr=1L;
		
		String[] splitStrings = data.delimiterPattern.split(originalString);
		for (String string: splitStrings) {
			Object[] outputRow = RowDataUtil.createResizedCopy(rowData, data.outputRowMeta.size());
			outputRow[rowMeta.size()] = string;
			// Include row number in output?
			if(meta.includeRowNumber()){
				outputRow[rowMeta.size()+1]=data.rownr;
			}
			putRow(data.outputRowMeta, outputRow);
			data.rownr ++;
		}		
		
		
		return true;
	}
	
	public synchronized boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SplitFieldToRowsMeta)smi;
		data=(SplitFieldToRowsData)sdi;

		Object[] r = getRow();   // get row from rowset, wait for our turn, indicate busy!
		if (r == null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		
		boolean ok = splitField(getInputRowMeta(), r);
		if (!ok)
		{
			setOutputDone();
			return false;
		}
					
        if (checkFeedback(getLinesRead())) {
			if(log.isDetailed()) 
			{
				if(log.isDetailed()) logBasic(BaseMessages.getString(PKG, "SplitFieldToRows.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
			}
		}
			
		return true;
	}
	
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta = (SplitFieldToRowsMeta)smi;
		data = (SplitFieldToRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
			data.rownr = 1L;
			
			try {
			    if (meta.isDelimiterRegex()) {
			    	data.delimiterPattern = Pattern.compile(environmentSubstitute(meta.getDelimiter()));
			    }
			    else {
			    	data.delimiterPattern = Pattern.compile(Pattern.quote(environmentSubstitute(meta.getDelimiter())));
			    }
			}
	    	catch (PatternSyntaxException pse) {
	    		log.logError(pse.getMessage());
	    		throw pse;
	    	}
	    		
		    return true;
		}
		return false;
	}
}
