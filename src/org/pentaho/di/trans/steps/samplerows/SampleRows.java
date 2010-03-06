/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Samatar HASSAN.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.trans.steps.samplerows;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;

/**
 * Sample rows. Filter rows based on line number
 * 
 * @author Samatar
 * @since 2-jun-2003
 */

public class SampleRows extends BaseStep implements StepInterface
{
	private static Class<?> PKG = SampleRowsMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private SampleRowsMeta meta;
	private SampleRowsData data;
	
	public SampleRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(SampleRowsMeta)smi;
		data=(SampleRowsData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
		if(first)
		{
			first=false;
			
			String realRange=environmentSubstitute(meta.getLinesRange());
			data.addlineField=(!Const.isEmpty(environmentSubstitute(meta.getLineNumberField())));
			
			// get the RowMeta
			data.previousRowMeta = getInputRowMeta().clone();
			data.NrPrevFields=data.previousRowMeta.size();
			data.outputRowMeta = data.previousRowMeta;
			if(data.addlineField) meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			String[] rangePart = realRange.split(",");

			for (int i = 0; i < rangePart.length; i++) 
			{
				if (rangePart[i].matches("\\d+")) 
				{
					String part=rangePart[i];
					if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SampleRows.Log.RangeValue",part));
					int vpart=Integer.valueOf(part);
					if(vpart>data.maxLine) data.maxLine=vpart;
					data.range.add(vpart);

				} else if (rangePart[i].matches("\\d+\\.\\.\\d+")) 
				{
					String[] rangeMultiPart = rangePart[i].split("\\.\\.");
					for (int j = Integer.valueOf(rangeMultiPart[0]).intValue(); j < Integer.valueOf(rangeMultiPart[1]).intValue() + 1; j++) 
					{
						if(log.isDebug()) logDebug(BaseMessages.getString(PKG, "SampleRows.Log.RangeValue",""+j));
						int vpart=Integer.valueOf(j);
						if(vpart>data.maxLine) data.maxLine=vpart;
						data.range.add(vpart);
					}
				} 
			}
		}// end if first
		
		if(data.addlineField)
		{
			data.outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
			for (int i = 0; i < data.NrPrevFields; i++)
			{
				data.outputRow[i] = r[i];
			}
		}else data.outputRow =r;
		
		if(data.range.contains((int)getLinesRead()))
		{
			if(data.addlineField)	data.outputRow[data.NrPrevFields]=getLinesRead();
			putRow(data.outputRowMeta, data.outputRow);      // copy row to possible alternate rowset(s).

			if (log.isRowLevel()) logRowlevel(BaseMessages.getString(PKG, "SampleRows.Log.LineNumber",getLinesRead()+" : "+getInputRowMeta().getString(r)));
		}
			
		if(data.maxLine>0 && getLinesRead()>=data.maxLine)
		{
			setOutputDone();
			return false;
		}
		
		return true;
	}


	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(SampleRowsMeta)smi;
		data=(SampleRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
		    // Add init code here.
		    return true;
		}
		return false;
	}
	
}
