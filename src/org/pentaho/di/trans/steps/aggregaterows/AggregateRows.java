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
 
package org.pentaho.di.trans.steps.aggregaterows;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
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
 * Aggregates rows
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class AggregateRows extends BaseStep implements StepInterface
{
	private static Class<?> PKG = AggregateRows.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private AggregateRowsMeta meta;
	private AggregateRowsData data;
	
	public AggregateRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void AddAggregate(RowMetaInterface rowMeta, Object[] r) throws KettleValueException
	{
		for (int i=0;i<data.fieldnrs.length;i++)
		{
			ValueMetaInterface valueMeta = rowMeta.getValueMeta(data.fieldnrs[i]);
			Object valueData = r[data.fieldnrs[i]];
			
			if (!valueMeta.isNull(valueData)) 
			{
				data.counts[i]++; // only count non-zero values!
				switch(meta.getAggregateType()[i])
				{
				case AggregateRowsMeta.TYPE_AGGREGATE_SUM:
				case AggregateRowsMeta.TYPE_AGGREGATE_AVERAGE:
					{
						Double number = valueMeta.getNumber(valueData);
						if (data.values[i]==null) 
						{
							data.values[i]=number;
						}
						else
						{
							data.values[i] = new Double( ((Double)data.values[i]).doubleValue() + number.doubleValue() );
						}
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_MIN:
					{
						if (data.values[i]==null) 
						{
							data.values[i]=valueData;
						}
						else
						{
							if (valueMeta.compare(data.values[i], valueData)<0) data.values[i]=valueData;
						}
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_MAX:
					{
						if (data.values[i]==null) 
						{
							data.values[i]=valueData;
						}
						else
						{
							if (valueMeta.compare(data.values[i], valueData)>0) data.values[i]=valueData; 
						}
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_NONE:
				case AggregateRowsMeta.TYPE_AGGREGATE_FIRST:
					if (data.values[i]==null)
					{
						data.values[i]=valueData;
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_LAST:
					data.values[i]=valueData;
					break;
				}
			}
            
            switch(meta.getAggregateType()[i])
            {
            case AggregateRowsMeta.TYPE_AGGREGATE_FIRST_NULL: // First value, EVEN if it's NULL:
                if (data.values[i]==null)
                {
                    data.values[i]=valueData;
                }
                break;
            case AggregateRowsMeta.TYPE_AGGREGATE_LAST_NULL: // Last value, EVEN if it's NULL:
                data.values[i]=valueData;
                break;
            default: break;
            }

		}
	}
	
	// End of the road, build a row to output!
	private synchronized Object[] buildAggregate()
	{
		Object[] agg = RowDataUtil.allocateRowData(data.outputRowMeta.size());
		
		for (int i=0;i<data.fieldnrs.length;i++)
		{
			switch(meta.getAggregateType()[i])
			{
				case AggregateRowsMeta.TYPE_AGGREGATE_SUM:
				case AggregateRowsMeta.TYPE_AGGREGATE_MIN:
				case AggregateRowsMeta.TYPE_AGGREGATE_MAX:
				case AggregateRowsMeta.TYPE_AGGREGATE_FIRST:
				case AggregateRowsMeta.TYPE_AGGREGATE_LAST:
				case AggregateRowsMeta.TYPE_AGGREGATE_NONE:
	            case AggregateRowsMeta.TYPE_AGGREGATE_FIRST_NULL: // First value, EVEN if it's NULL:
	            case AggregateRowsMeta.TYPE_AGGREGATE_LAST_NULL: // Last value, EVEN if it's NULL:
					agg[i]=data.values[i];
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_COUNT:
					agg[i]=new Double(data.counts[i]);
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_AVERAGE:
					agg[i] = new Double( ((Double)data.values[i]).doubleValue() / data.counts[i] );
					break;

				default: break;
			}
		}
		return agg;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AggregateRowsMeta)smi;
		data=(AggregateRowsData)sdi;

		Object[] r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			Object[] agg = buildAggregate(); // build a resume
			putRow(data.outputRowMeta, agg);
			setOutputDone();
			return false;
		}
		
		if (first)
		{
			first=false;
			
			data.outputRowMeta = getInputRowMeta().clone();
			meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
			
			for (int i=0;i<meta.getFieldName().length;i++) 
			{
				data.fieldnrs[i]=getInputRowMeta().indexOfValue(meta.getFieldName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "AggregateRows.Log.CouldNotFindField",meta.getFieldName()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
				data.counts[i]=0L;
			} 
		}
		
		AddAggregate(getInputRowMeta(), r);
		
        if (checkFeedback(getLinesRead())) 
        	if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "AggregateRows.Log.LineNumber")+getLinesRead()); //$NON-NLS-1$
		
		return true;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AggregateRowsMeta)smi;
		data=(AggregateRowsData)sdi;
		
		if (super.init(smi, sdi))
		{
			int nrfields=meta.getFieldName().length;
			data.fieldnrs=new int[nrfields];
			data.values=new Object[nrfields];
			data.counts=new long[nrfields];
			
			return true;
		}
		return false;
		
	}

}