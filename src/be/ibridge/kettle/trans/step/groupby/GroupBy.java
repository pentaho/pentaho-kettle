 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** It belongs to, is maintained by and is copyright 1999-2005 by     **
 **                                                                   **
 **      i-Bridge bvba                                                **
 **      Fonteinstraat 70                                             **
 **      9400 OKEGEM                                                  **
 **      Belgium                                                      **
 **      http://www.kettle.be                                         **
 **      info@kettle.be                                               **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.trans.step.groupby;

import java.math.BigDecimal;

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
 * Groups informations based on aggregation rules. (sum, count, ...)
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class GroupBy extends BaseStep implements StepInterface
{
	private GroupByMeta meta;
	private GroupByData data;
	
	public GroupBy(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(GroupByMeta)getStepMeta().getStepMetaInterface();
		data=(GroupByData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		debug="processRow";
		
		Row r=getRow();    // get row!
		if (r==null)  // no more input to be expected...
		{
			// Don't forget the last set of rows...
			if (data.previous!=null) 
			{
				if (meta.passAllRows())
				{
					Row normal = buildRegular(data.previous);
					putRow(normal);
				}
				
				calcAggregate(data.previous);
				
				Row result = buildResult(data.previous);
				putRow(result);				
			} 
			setOutputDone();
			return false;
		}
		
		// System.out.println("r = "+r);
		
		if (first)
		{
			data.counts     = new long[meta.getSubjectField().length];
			data.subjectnrs = new int[meta.getSubjectField().length];
			for (int i=0;i<meta.getSubjectField().length;i++)
			{
				data.subjectnrs[i] = r.searchValueIndex(meta.getSubjectField()[i]);
				if (data.subjectnrs[i]<0)
				{
					logError("Aggregate subject field ["+meta.getSubjectField()[i]+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}
			}
			data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = r.searchValueIndex(meta.getGroupField()[i]);
				if (data.groupnrs[i]<0)
				{
					logError("Grouping field ["+meta.getGroupField()[i]+"] couldn't be found!");
					setErrors(1);
					stopAll();
					return false;
				}				
			}

			data.previous=new Row(r); // copy the row to previous
			newAggregate(r);         // Create a new group aggregate (init)
			
			// System.out.println("FIRST, agg="+agg);
			
			first=false;
		}
		else
		{
			calcAggregate(data.previous);
			//System.out.println("After calc, agg="+agg);

			if (meta.passAllRows())
			{
				Row normal = buildRegular(data.previous);
				putRow(normal);
			}
		}

				
		
		if (!sameGroup(data.previous, r))
		{
			Row result = buildResult(data.previous);
			putRow(result);        // copy row to possible alternate rowset(s).
			newAggregate(r);       // Create a new group aggregate (init)
		}

		data.previous=new Row(r);

		if ((linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("Linenr "+linesRead);
			
		return true;
	}
	
	// Is the row r of the same group as previous?
	private boolean sameGroup(Row previous, Row r)
	{
		debug="sameGroup";
		
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value prev = previous.getValue(data.groupnrs[i]);
			Value curr = r.getValue(data.groupnrs[i]);
			
			if (!prev.equals(curr)) return false;
		}
		
		return true;
	}
	
	// Calculate the aggregates in the row...
	private void calcAggregate(Row r)
	{
		debug="calcAggregate start";
		
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			debug="calcAggregate start loop";
			Value subj = r.getValue(data.subjectnrs[i]);
			Value value = data.agg.getValue(i);
			
			//System.out.println("  calcAggregate value, i="+i+", agg.size()="+agg.size()+", subj="+subj+", value="+value);
			
			debug="calcAggregate switch";
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            :
					value.plus(subj);
					break; 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
					value.plus(subj);
				data.counts[i]++;
					break; 
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      :
					data.counts[i]++;
					break;
				case GroupByMeta.TYPE_GROUP_MIN            :
					if (subj.compare(value)<0) value.setValue(subj);
					break; 
				case GroupByMeta.TYPE_GROUP_MAX            : 
					if (subj.compare(value)>0) value.setValue(subj);
					break; 
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    if (!subj.isNull()) 
                    {
                        if (value.getStringLength()>0) value.setValue(value.getString()+", ");
                        value.setValue(value.getString()+subj.getString());
                    }
                    break; 
				default: break;
			}
		}
		debug="calcAggregate end";
	}

	// Initialize a group..
	private void newAggregate(Row r)
	{
		debug="newAggregate";
		
		// Put all the counters at 0
		for (int i=0;i<data.counts.length;i++) data.counts[i]=0;
		
		data.agg = new Row();
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Value subj = r.getValue(data.subjectnrs[i]);
			Value v=null;
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            : 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      :
                    v = new Value(meta.getAggregateField()[i], subj.isNumeric()?subj.getType():Value.VALUE_TYPE_NUMBER);
                    switch(subj.getType())
                    {
                    case Value.VALUE_TYPE_BIGNUMBER: v.setValue(new BigDecimal(0)); break;
                    case Value.VALUE_TYPE_INTEGER:   v.setValue(0L); break;
                    case Value.VALUE_TYPE_NUMBER:    
                    default:                         v.setValue(0.0); break;
                    }
					break; 
				case GroupByMeta.TYPE_GROUP_MIN            : 
				case GroupByMeta.TYPE_GROUP_MAX            : 
					v = new Value(meta.getAggregateField()[i], subj.getType());
					v.setValue(subj);
					break;
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    v = new Value(meta.getAggregateField()[i], Value.VALUE_TYPE_STRING);
                    v.setValue("");
                    break; 
				default: break;
			}
            
            if (meta.getAggregateType()[i]!=GroupByMeta.TYPE_GROUP_COUNT_ALL)
            {
                v.setLength(subj.getLength(), subj.getPrecision());
            }
			if (v!=null) data.agg.addValue(v);
		}
	}
	
	private Row buildResult(Row r) throws KettleValueException
	{
		debug="buildResult";
		
		Row result = new Row();
		debug="buildResult 1";
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value gr = r.getValue(data.groupnrs[i]);
			result.addValue(gr);
		}
		debug="buildResult 2";
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Value ag = data.agg.getValue(i);
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            : break; 
				case GroupByMeta.TYPE_GROUP_AVERAGE        : ag.divide(new Value("c", data.counts[i])); break; 
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      : ag.setValue(data.counts[i]); break;
				case GroupByMeta.TYPE_GROUP_MIN            : break; 
				case GroupByMeta.TYPE_GROUP_MAX            : break; 
				default: break;
			}
			result.addValue(ag);
		}
		debug="buildResult 3";
		// At the end, add the flag?
		if (meta.getPassFlagField()!=null && meta.getPassFlagField().length()>0)
		{
			Value flag = new Value(meta.getPassFlagField(), Value.VALUE_TYPE_BOOLEAN);
			flag.setValue(true);
			result.addValue(flag);
		}
		debug="buildResult end";
		return result;
	}

	private Row buildRegular(Row r)
	{
		debug="buildRegular start";
		
		Row result = new Row();

        // Group
		for (int i=0;i<data.groupnrs.length;i++)
		{
			Value gr = r.getValue(data.groupnrs[i]);
			result.addValue(gr);
		}

        // Subjects
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Value ag = r.getValue(data.subjectnrs[i]);
			// Rename to the new aggregate names!
			ag.setName(meta.getAggregateField()[i]);
			result.addValue(ag);
		}
                
		// At the end, add the flag?

		if (meta.getPassFlagField()!=null && meta.getPassFlagField().length()>0)
		{
			Value flag = new Value(meta.getPassFlagField(), Value.VALUE_TYPE_BOOLEAN);
			flag.setValue(false);
			result.addValue(flag);
		}

		debug="buildRegular end";
		return result;
	}
		
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GroupByMeta)smi;
		data=(GroupByData)sdi;
		
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
