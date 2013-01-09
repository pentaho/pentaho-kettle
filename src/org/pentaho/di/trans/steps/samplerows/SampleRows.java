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
			
      data.considerRow = true;
			
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
		
		if (data.considerRow) {
		  
      if (data.addlineField) {
        data.outputRow = RowDataUtil.allocateRowData(data.outputRowMeta.size());
        for (int i = 0; i < data.NrPrevFields; i++) {
          data.outputRow[i] = r[i];
        }
      } else {
        data.outputRow = r;
      }
  
      if (data.range.contains((int) getLinesRead())) {
        if (data.addlineField) {
          data.outputRow[data.NrPrevFields] = getLinesRead();
        }
        
        // copy row to possible alternate rowset(s).
        //
        putRow(data.outputRowMeta, data.outputRow);
        
        if (log.isRowLevel()) {
          logRowlevel(BaseMessages.getString(PKG, "SampleRows.Log.LineNumber", getLinesRead() + " : " + getInputRowMeta().getString(r)));
        }
      }
  			
  		if(data.maxLine>0 && getLinesRead()>=data.maxLine)
  		{
  			data.considerRow = false;
  		}
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
