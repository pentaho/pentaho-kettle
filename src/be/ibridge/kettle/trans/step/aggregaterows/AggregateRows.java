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
 
package be.ibridge.kettle.trans.step.aggregaterows;

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
 * Aggregates rows
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class AggregateRows extends BaseStep implements StepInterface
{
	private AggregateRowsMeta meta;
	private AggregateRowsData data;
	
	public AggregateRows(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	private synchronized void AddAggregate(Row r)
	{
		int i;
		Value val=null;
		
		for (i=0;i<data.fieldnrs.length;i++)
		{
			val = r.getValue(data.fieldnrs[i]);
			
			if (!val.isNull()) 
			{
				data.counts[i]++; // only count non-zero values!
				switch(meta.getAggregateType()[i])
				{
				case AggregateRowsMeta.TYPE_AGGREGATE_SUM:
				case AggregateRowsMeta.TYPE_AGGREGATE_AVERAGE:
					if (data.values[i]==null) 
					{
						data.values[i]=new Value(val);
					}
					else
					{
						data.values[i].setValue(data.values[i].getNumber()+val.getNumber());
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_MIN:
					if (data.values[i]==null) 
					{
						data.values[i]=new Value(val);
					}
					else
					{
						if (val.compare(data.values[i])<0) data.values[i]=val;
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_MAX:
					if (data.values[i]==null) 
					{
						data.values[i]=new Value(val);
					}
					else
					{
						if (val.compare(data.values[i])>0) data.values[i]=val; 
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_NONE:
				case AggregateRowsMeta.TYPE_AGGREGATE_FIRST:
					if (data.values[i]==null)
					{
						data.values[i]=new Value(val);
					}
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_LAST:
					data.values[i]=new Value(val);
					break;
				}
			}
			else
			{
				// At least put something in it. Suppose all values are NULL?
				if (data.values[i]==null) 
				{
					data.values[i]=new Value(meta.getFieldName()[i], val.getType());
					data.values[i].setNull();
				} 
			}
		}
	}
	
	// End of the road, build a row to output!
	private synchronized Row buildAggregate()
	{
		int i;
		
		Row agg = new Row();
		
		for (i=0;i<data.fieldnrs.length;i++)
		{
			Value val;
			switch(meta.getAggregateType()[i])
			{
				case AggregateRowsMeta.TYPE_AGGREGATE_SUM:
				case AggregateRowsMeta.TYPE_AGGREGATE_MIN:
				case AggregateRowsMeta.TYPE_AGGREGATE_MAX:
				case AggregateRowsMeta.TYPE_AGGREGATE_FIRST:
				case AggregateRowsMeta.TYPE_AGGREGATE_LAST:
				case AggregateRowsMeta.TYPE_AGGREGATE_NONE:
					val=data.values[i];
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_COUNT:
					val=new Value(meta.getFieldName()[i], (double)data.counts[i]);
					break;
				case AggregateRowsMeta.TYPE_AGGREGATE_AVERAGE:
					val = data.values[i];
					val.setValue( val.getNumber()/data.counts[i] );
					break;
				default: val=new Value(meta.getFieldName()[i], Value.VALUE_TYPE_NONE);
					break;
			}
			if (meta.getFieldNewName()!=null && val!=null) val.setName(meta.getFieldNewName()[i]);
			if (val!=null) agg.addValue(val);
		}
		return agg;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AggregateRowsMeta)smi;
		data=(AggregateRowsData)sdi;

		Row r=null;
		
		r=getRow();    // get row, set busy!
		if (r==null)  // no more input to be expected...
		{
			Row agg = buildAggregate(); // build a resume
			putRow(agg);
			setOutputDone();
			return false;
		}
		
		if (linesRead==0)
		{
			logError("Start of aggreg.rows, row="+r.toString());
			logError("Start of aggreg.rows, row.size()="+r.size());
			for (int i=0;i<meta.getFieldName().length;i++) 
			{
				data.fieldnrs[i]=r.searchValueIndex(meta.getFieldName()[i]);
				if (data.fieldnrs[i]<0)
				{
					logError("Couldn't find field '"+meta.getFieldName()[i]+"' in row!");
					setErrors(1);
					stopAll();
					return false;
				}
				data.counts[i]=0L;
			} 
		}
		
		AddAggregate(r);
		
		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
		
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
			data.values=new Value[nrfields];
			data.counts=new long[nrfields];
			
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
			logError("Unexpected error in '"+debug+"' : "+e.toString());
			setErrors(1);
			stopAll();
		}
		finally
		{
		    dispose(meta, data);
			markStop();
		    logSummary();
		}
	}
}
