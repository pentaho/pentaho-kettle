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
 
package org.pentaho.di.trans.steps.memgroupby;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueDataUtil;
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
import org.pentaho.di.trans.steps.groupby.GroupByMeta;
import org.pentaho.di.trans.steps.memgroupby.MemoryGroupByData.HashEntry;


/**
 * Groups informations based on aggregation rules. (sum, count, ...)
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class MemoryGroupBy extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MemoryGroupByMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MemoryGroupByMeta meta;
	private MemoryGroupByData data;
	
	public MemoryGroupBy(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
		
		meta=(MemoryGroupByMeta)getStepMeta().getStepMetaInterface();
		data=(MemoryGroupByData)stepDataInterface;
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
        meta=(MemoryGroupByMeta)smi;
        data=(MemoryGroupByData)sdi;
        
        Object[] r=getRow();  // get row!
        if (first) {

        	// What is the output looking like?
        	// 
        	data.inputRowMeta = getInputRowMeta();
        	
        	// In case we have 0 input rows, we still want to send out a single row aggregate
        	// However... the problem then is that we don't know the layout from receiving it from the previous step over the row set.
        	// So we need to calculated based on the metadata...
        	//
        	if (data.inputRowMeta==null)
        	{
        		data.inputRowMeta = getTransMeta().getPrevStepFields(getStepMeta());
        	}

        	data.outputRowMeta = data.inputRowMeta.clone();
        	meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        	
        	// Do all the work we can beforehand
        	// Calculate indexes, loop up fields, etc.
        	//
			data.subjectnrs = new int[meta.getSubjectField().length];

			for (int i=0;i<meta.getSubjectField().length;i++)
			{
				data.subjectnrs[i] = data.inputRowMeta.indexOfValue(meta.getSubjectField()[i]);
				if (data.subjectnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "MemoryGroupBy.Log.AggregateSubjectFieldCouldNotFound",meta.getSubjectField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
			}
						
			data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = data.inputRowMeta.indexOfValue(meta.getGroupField()[i]);
				if (data.groupnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "MemoryGroupBy.Log.GroupFieldCouldNotFound",meta.getGroupField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}				
			}
			
			// Create a metadata value for the counter Integers
			//
			data.valueMetaInteger = new ValueMeta("count", ValueMetaInterface.TYPE_INTEGER);
			data.valueMetaNumber = new ValueMeta("sum", ValueMetaInterface.TYPE_NUMBER);
			
			// Initialize the group metadata
			//
			initGroupMeta(data.inputRowMeta);
			
			
    }
        
    if (first) {
			// Only calculate data.aggMeta here, not for every new aggregate.
			//
			newAggregate(r, null);

			// for speed: groupMeta+aggMeta
			//
			data.groupAggMeta=new RowMeta();
			data.groupAggMeta.addRowMeta(data.groupMeta);
			data.groupAggMeta.addRowMeta(data.aggMeta);
		}
		    
    // Here is where we start to do the real work...
    //
    if (r==null)  // no more input to be expected... (or none received in the first place)
		{
      handleLastOfGroup();
			
			setOutputDone();
			return false;
		}
    
    if (first || data.newBatch) {
      first = false;
      data.newBatch = false;
    }
    
		addToAggregate(r);

		if (checkFeedback(getLinesRead())) 
		{
			if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "MemoryGroupBy.LineNumber")+getLinesRead()); //$NON-NLS-1$
		}
			
		return true;
	}

	
  private void handleLastOfGroup() throws KettleException {
    // Dump the content of the map...
    //
    for (HashEntry entry : data.map.keySet()) {
      Aggregate aggregate = data.map.get(entry);
      Object[] aggregateResult = getAggregateResult(aggregate);

      Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
      int index = 0;
      for (int i = 0; i < data.groupMeta.size(); i++) {
        outputRowData[index++] = entry.getGroupData()[i];
      }
      for (int i = 0; i < data.aggMeta.size(); i++) {
        outputRowData[index++] = aggregateResult[i];
      }
      putRow(data.outputRowMeta, outputRowData);
    }

  }

  // Calculate the aggregates in the row...
	@SuppressWarnings("unchecked")
	private void addToAggregate(Object[] r) throws KettleException
	{
		// First, look up the row in the map...
		//
		Object[] groupData = new Object[data.groupMeta.size()];
		for (int i=0;i<data.groupnrs.length;i++) {
			ValueMetaInterface valueMeta = data.groupMeta.getValueMeta(i);
			groupData[i] = valueMeta.convertToNormalStorageType( r[data.groupnrs[i]] );
		}
		HashEntry entry = data.getHashEntry(groupData);
		
		Aggregate aggregate = data.map.get(entry);
		if (aggregate == null) {
			// Create a new value...
			//
			aggregate = new Aggregate();
			newAggregate(r, aggregate);
			
			// Store it in the map!
			//
			data.map.put(entry, aggregate);
		}
		
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Object subj = r[data.subjectnrs[i]];
			ValueMetaInterface subjMeta=data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
			Object value = aggregate.agg[i];
			ValueMetaInterface valueMeta=data.aggMeta.getValueMeta(i);
			
			switch(meta.getAggregateType()[i])
			{
				case MemoryGroupByMeta.TYPE_GROUP_SUM            :
					aggregate.agg[i]=ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
					break; 
				case MemoryGroupByMeta.TYPE_GROUP_AVERAGE        :
					if (!subjMeta.isNull(subj)) {
						aggregate.agg[i]=ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
						aggregate.counts[i]++;
					}
					break; 
				case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
					if (aggregate.mean == null) {
						aggregate.mean = new double[meta.getSubjectField().length];
					}
					aggregate.counts[i]++;
					double n = aggregate.counts[i];
					double x = subjMeta.getNumber(subj);
					double sum = (Double)value;
					double mean = aggregate.mean[i];
					
					double delta = x - mean;
					mean = mean + (delta/n);
					sum = sum + delta*(x-mean);
					
					aggregate.mean[i] = mean;
					aggregate.agg[i] = sum;
					break; 
				case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT :
				  if (!subjMeta.isNull(subj)) {
				    if (aggregate.distinctObjs == null) {
				    	aggregate.distinctObjs = new Set[meta.getSubjectField().length];
				    }
				    if (aggregate.distinctObjs[i] == null) {
				    	aggregate.distinctObjs[i] = new TreeSet<Object>();
				    }
				    Object obj = subjMeta.convertToNormalStorageType(subj);
				    if (!aggregate.distinctObjs[i].contains(obj)) {
				    	aggregate.distinctObjs[i].add(obj);
				    	aggregate.agg[i] = (Long)value + 1;
				    }
				  }
				case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL      :
					if (!subjMeta.isNull(subj)) {
						aggregate.counts[i]++;
					}
					break;
				case MemoryGroupByMeta.TYPE_GROUP_MIN            :
					if (subjMeta.compare(subj,valueMeta,value)<0) {
						aggregate.agg[i]=subj; 
					}
					break; 
				case MemoryGroupByMeta.TYPE_GROUP_MAX            : 
					if (subjMeta.compare(subj,valueMeta,value)>0) {
						aggregate.agg[i]=subj; 
					}
					break; 
                case MemoryGroupByMeta.TYPE_GROUP_FIRST          :
                    if (!subjMeta.isNull(subj) && value==null) {
                    	aggregate.agg[i]=subj;
                    }
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_LAST           : 
                    if (!subjMeta.isNull(subj)) {
                    	aggregate.agg[i]=subj; 
                    }
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
                	if (aggregate.counts[i]==0) {
                		aggregate.agg[i]=subj;
                		aggregate.counts[i]++;
                	}
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_LAST_INCL_NULL : 
                	aggregate.agg[i]=subj; 
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    if (!(subj==null)) 
                    {
                      StringBuilder sb = (StringBuilder) value;
                      if(sb.length()>0){
                          sb.append(", ");
                      }
                      sb.append(subjMeta.getString(subj));
                    }
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_CONCAT_STRING   :
                    if (!(subj==null)) 
                    {
                    	String separator="";
                    	if(!Const.isEmpty(meta.getValueField()[i])) separator=environmentSubstitute(meta.getValueField()[i]);
                      StringBuilder sb = (StringBuilder) value;
                      if (sb.length() > 0) {
                        sb.append(separator);
                      }
                      sb.append(subjMeta.getString(subj));
                    }
                    
                    break; 
				default: break;
			}
		}
	}

	// Initialize a group..
	private void newAggregate(Object[] r, Aggregate aggregate) throws KettleException
	{
		if (aggregate==null) {
			data.aggMeta=new RowMeta();	
		} else {
			aggregate.counts = new long[data.subjectnrs.length];
			
			// Put all the counters at 0
			for (int i=0;i<aggregate.counts.length;i++) {
				aggregate.counts[i]=0;
			}
			aggregate.distinctObjs = null;
			aggregate.agg = new Object[data.subjectnrs.length];
			aggregate.mean = new double[data.subjectnrs.length]; // sets all doubles to 0.0
		}
		
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			ValueMetaInterface subjMeta=data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
			Object v=null;
			ValueMetaInterface vMeta=null;
			switch(meta.getAggregateType()[i])
			{
				case MemoryGroupByMeta.TYPE_GROUP_SUM            : 
				case MemoryGroupByMeta.TYPE_GROUP_AVERAGE        :
				case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_NUMBER);
                    v=new Double(0.0);
                    break;
				case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT  :
				case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL       :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_INTEGER);
                    v=new Long(0L);                   
					break; 
				case MemoryGroupByMeta.TYPE_GROUP_FIRST           :
				case MemoryGroupByMeta.TYPE_GROUP_LAST            :
				case MemoryGroupByMeta.TYPE_GROUP_FIRST_INCL_NULL :
				case MemoryGroupByMeta.TYPE_GROUP_LAST_INCL_NULL  :					
				case MemoryGroupByMeta.TYPE_GROUP_MIN             : 
				case MemoryGroupByMeta.TYPE_GROUP_MAX             : 
					vMeta = subjMeta.clone();
					vMeta.setName(meta.getAggregateField()[i]);
					v = r==null ? null : r[data.subjectnrs[i]]; 
					break;
                case MemoryGroupByMeta.TYPE_GROUP_CONCAT_COMMA    :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_STRING);
                    v = new StringBuilder();
                    break; 
                case MemoryGroupByMeta.TYPE_GROUP_CONCAT_STRING   :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_STRING);
                    v = new StringBuilder();
                    break; 
				default: 
					throw new KettleException("Unknown data type for aggregation : "+meta.getAggregateField()[i]);
					// TODO raise an error here because we cannot continue successfully maybe the UI should validate this
					// break;
			}
            
            if (meta.getAggregateType()[i]!=MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL && 
                meta.getAggregateType()[i]!=MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT)
            {
            	vMeta.setLength(subjMeta.getLength(), subjMeta.getPrecision());
            }
			if (aggregate==null) {
				data.aggMeta.addValueMeta(vMeta);
			} else {
				if (v!=null) {
					aggregate.agg[i]=v;
				}
			}
		}
	}

	private void initGroupMeta(RowMetaInterface previousRowMeta) throws KettleValueException
	{
		data.groupMeta=new RowMeta();
		data.entryMeta = new RowMeta();
		
		for (int i=0;i<data.groupnrs.length;i++)
		{
			ValueMetaInterface valueMeta = previousRowMeta.getValueMeta(data.groupnrs[i]);
			data.groupMeta.addValueMeta(valueMeta);
			
			ValueMetaInterface normalMeta = valueMeta.clone();
			normalMeta.setStorageType(ValueMetaInterface.STORAGE_TYPE_NORMAL);
		}

        
		return;
	}

    private Object[] getAggregateResult(Aggregate aggregate) throws KettleValueException
    {
        Object[] result = new Object[data.subjectnrs.length];

        if (data.subjectnrs!=null)
        {
            for (int i=0;i<data.subjectnrs.length;i++)
            {
                Object ag = aggregate.agg[i];
                switch(meta.getAggregateType()[i])
                {
                    case MemoryGroupByMeta.TYPE_GROUP_SUM            : break; 
                    case MemoryGroupByMeta.TYPE_GROUP_AVERAGE        :
                    		ag=ValueDataUtil.divide(data.aggMeta.getValueMeta(i), ag, 
                    				new ValueMeta("c",ValueMetaInterface.TYPE_INTEGER), new Long(aggregate.counts[i])); 
                    		break;  //$NON-NLS-1$
                    case MemoryGroupByMeta.TYPE_GROUP_COUNT_ALL      : ag=new Long(aggregate.counts[i]); break;
                    case MemoryGroupByMeta.TYPE_GROUP_COUNT_DISTINCT : break;
                    case MemoryGroupByMeta.TYPE_GROUP_MIN            : break; 
                    case MemoryGroupByMeta.TYPE_GROUP_MAX            : break; 
                    case MemoryGroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
                    	double sum = (Double)ag / aggregate.counts[i];
                    	ag = Double.valueOf( Math.sqrt( sum ) );
                    	break;
                    case GroupByMeta.TYPE_GROUP_CONCAT_COMMA:;
                    case GroupByMeta.TYPE_GROUP_CONCAT_STRING: 
                      ag = ((StringBuilder) ag).toString();
                      break;
                    default: break;
                }
                result[i]=ag;
            }
        }

        return result;

    }
		    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(MemoryGroupByMeta)smi;
		data=(MemoryGroupByData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.map = new HashMap<HashEntry, Aggregate>(5000);
            
            return true;
		}
		return false;
	}

	 public void batchComplete() throws KettleException {
	   // Empty the hash table
	   //
	   handleLastOfGroup();
	    
	   // Clear the complete cache...
	   //
	   data.map.clear();
	    
	   data.newBatch=true;
	 }
}