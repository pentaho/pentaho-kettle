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
 
package org.pentaho.di.trans.steps.groupby;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
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


/**
 * Groups informations based on aggregation rules. (sum, count, ...)
 * 
 * @author Matt
 * @since 2-jun-2003
 */
public class GroupBy extends BaseStep implements StepInterface
{
	private static Class<?> PKG = GroupByMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

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
        meta=(GroupByMeta)smi;
        data=(GroupByData)sdi;
        
        Object[] r=getRow();    // get row!
        
		if (first)
		{
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
			data.counts     = new long[meta.getSubjectField().length];
			data.subjectnrs = new int[meta.getSubjectField().length];

			data.cumulativeSumSourceIndexes = new ArrayList<Integer>();
			data.cumulativeSumTargetIndexes = new ArrayList<Integer>();
			
			data.cumulativeAvgSourceIndexes = new ArrayList<Integer>();
			data.cumulativeAvgTargetIndexes = new ArrayList<Integer>();
			
			for (int i=0;i<meta.getSubjectField().length;i++)
			{
				data.subjectnrs[i] = data.inputRowMeta.indexOfValue(meta.getSubjectField()[i]);
				if (data.subjectnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "GroupBy.Log.AggregateSubjectFieldCouldNotFound",meta.getSubjectField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
					setErrors(1);
					stopAll();
					return false;
				}
				
				if (meta.getAggregateType()[i]==GroupByMeta.TYPE_GROUP_CUMULATIVE_SUM)
				{
					data.cumulativeSumSourceIndexes.add(data.subjectnrs[i]);
					
					// The position of the target in the output row is the input row size + i
					//
					data.cumulativeSumTargetIndexes.add(data.inputRowMeta.size()+i);
				}
				if (meta.getAggregateType()[i]==GroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE)
				{
					data.cumulativeAvgSourceIndexes.add(data.subjectnrs[i]);

					// The position of the target in the output row is the input row size + i
					//
					data.cumulativeAvgTargetIndexes.add(data.inputRowMeta.size()+i);
				}
				
			}
			
			data.previousSums = new Object[data.cumulativeSumTargetIndexes.size()];
			
			data.previousAvgSum = new Object[data.cumulativeAvgTargetIndexes.size()];
			data.previousAvgCount = new long[data.cumulativeAvgTargetIndexes.size()];
			
			data.groupnrs = new int[meta.getGroupField().length];
			for (int i=0;i<meta.getGroupField().length;i++)
			{
				data.groupnrs[i] = data.inputRowMeta.indexOfValue(meta.getGroupField()[i]);
				if (data.groupnrs[i]<0)
				{
					logError(BaseMessages.getString(PKG, "GroupBy.Log.GroupFieldCouldNotFound",meta.getGroupField()[i])); //$NON-NLS-1$ //$NON-NLS-2$
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
        
        if (first || data.newBatch) {
          // Create a new group aggregate (init)
          //
          newAggregate(r);
        }
        
        if (first) {
          // for speed: groupMeta+aggMeta
          //
          data.groupAggMeta = new RowMeta();
          data.groupAggMeta.addRowMeta(data.groupMeta);
          data.groupAggMeta.addRowMeta(data.aggMeta);
        }
        
		if (r==null)  // no more input to be expected... (or none received in the first place)
		{
		    handleLastOfGroup();
			setOutputDone();
			return false;
		}
        
        if (first || data.newBatch)
        {
        	first=false;
        	data.newBatch = false;
        	
        	data.previous = data.inputRowMeta.cloneRow(r); // copy the row to previous
		}
		else
		{
			calcAggregate(data.previous);
			//System.out.println("After calc, agg="+agg);

			if (meta.passAllRows())
			{
                addToBuffer(data.previous);
			}
		}
				
		// System.out.println("Check for same group...");
        
		if (!sameGroup(data.previous, r))
		{
            // System.out.println("Different group!");
            
            if (meta.passAllRows())
            {
                // System.out.println("Close output...");
                
                // Not the same group: close output (if any)
                closeOutput();

                // System.out.println("getAggregateResult()");

                // Get all rows from the buffer!
                data.groupResult = getAggregateResult();

                // System.out.println("dump rows from the buffer");

                Object[] row = getRowFromBuffer();
                
                long lineNr=0;
                while (row!=null)
                {
                    int size = data.inputRowMeta.size();

                    row=RowDataUtil.addRowData(row, size, data.groupResult);
                	size+=data.groupResult.length;
                	
                	lineNr++;
                    
                    if (meta.isAddingLineNrInGroup() && !Const.isEmpty(meta.getLineNrInGroupField()))
                    {
                    	Object lineNrValue= new Long(lineNr);
                    	// ValueMetaInterface lineNrValueMeta = new ValueMeta(meta.getLineNrInGroupField(), ValueMetaInterface.TYPE_INTEGER);
                    	// lineNrValueMeta.setLength(9);
                    	row=RowDataUtil.addValueData(row, size, lineNrValue);
                    	size++;
                    }
                    
                    addCumulativeSums(row);
                    addCumulativeAverages(row);
                    
                    putRow(data.outputRowMeta, row);
                    row = getRowFromBuffer();
                }
                closeInput();
            }
            else
            {
    			Object[] result = buildResult(data.previous);
    			if (result!=null) {
    				putRow(data.groupAggMeta, result);        // copy row to possible alternate rowset(s).
    			}
            }
            newAggregate(r);       // Create a new group aggregate (init)
		}

		data.previous=data.inputRowMeta.cloneRow(r);

		if (checkFeedback(getLinesRead())) 
		{
			if(log.isBasic()) logBasic(BaseMessages.getString(PKG, "GroupBy.LineNumber")+getLinesRead()); //$NON-NLS-1$
		}
			
		return true;
	}
	
	private void handleLastOfGroup() throws KettleException {
      if (meta.passAllRows())  // ALL ROWS
      {
          if (data.previous!=null)
          {
              calcAggregate(data.previous);
              addToBuffer(data.previous);
          }
          data.groupResult = getAggregateResult();

          Object[] row = getRowFromBuffer();
          
          
          long lineNr=0;
          while (row!=null)
          {
              int size = data.inputRowMeta.size();
              row=RowDataUtil.addRowData(row, size, data.groupResult);
              size+=data.groupResult.length;
              lineNr++;
              
              if (meta.isAddingLineNrInGroup() && !Const.isEmpty(meta.getLineNrInGroupField()))
              {
                  Object lineNrValue= new Long(lineNr);
                  // ValueMetaInterface lineNrValueMeta = new ValueMeta(meta.getLineNrInGroupField(), ValueMetaInterface.TYPE_INTEGER);
                  // lineNrValueMeta.setLength(9);
                  row=RowDataUtil.addValueData(row, size, lineNrValue);
                  size++;
              }
              
              addCumulativeSums(row);
              addCumulativeAverages(row);
              
              putRow(data.outputRowMeta, row);
              row = getRowFromBuffer();
          }
          closeInput();
      }
      else   // JUST THE GROUP + AGGREGATE
      {
          // Don't forget the last set of rows...
          if (data.previous!=null) 
          {
              calcAggregate(data.previous);
          } 
          Object[] result = buildResult(data.previous);
          if (result!=null) {
              putRow(data.groupAggMeta, result);
          }
      }
	}

  private void addCumulativeSums(Object[] row) throws KettleValueException {

		// We need to adjust this row with cumulative averages?
        //
        for (int i=0;i<data.cumulativeSumSourceIndexes.size();i++)
        {
        	int sourceIndex = data.cumulativeSumSourceIndexes.get(i);
        	Object previousTarget = data.previousSums[i];
        	Object sourceValue = row[sourceIndex];
        	
        	int targetIndex = data.cumulativeSumTargetIndexes.get(i);
        	
        	ValueMetaInterface sourceMeta = data.inputRowMeta.getValueMeta(sourceIndex);
    		ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta(targetIndex);

    		// If the first values where null, or this is the first time around, just take the source value...
    		//
        	if (targetMeta.isNull(previousTarget))
        	{
        		row[targetIndex]=sourceMeta.convertToNormalStorageType(sourceValue);
        	}
        	else
        	{
        		// If the source value is null, just take the previous target value
        		//
        		if (sourceMeta.isNull(sourceValue))
        		{
        			row[targetIndex] = previousTarget;
        		}
        		else
        		{
            		row[targetIndex] = ValueDataUtil.plus(targetMeta, data.previousSums[i], sourceMeta, row[sourceIndex]);
        		}
        	}
        	data.previousSums[i] = row[targetIndex];
        }

	}
	
	private void addCumulativeAverages(Object[] row) throws KettleValueException {

		// We need to adjust this row with cumulative sums
        //
        for (int i=0;i<data.cumulativeAvgSourceIndexes.size();i++)
        {
        	int sourceIndex = data.cumulativeAvgSourceIndexes.get(i);
        	Object previousTarget = data.previousAvgSum[i];
        	Object sourceValue = row[sourceIndex];
        	
        	int targetIndex = data.cumulativeAvgTargetIndexes.get(i);
        	
        	ValueMetaInterface sourceMeta = data.inputRowMeta.getValueMeta(sourceIndex);
    		ValueMetaInterface targetMeta = data.outputRowMeta.getValueMeta(targetIndex);

    		// If the first values where null, or this is the first time around, just take the source value...
    		//
    		Object sum = null;
    		
        	if (targetMeta.isNull(previousTarget))
        	{
        		sum=sourceMeta.convertToNormalStorageType(sourceValue);
        	}
        	else
        	{
        		// If the source value is null, just take the previous target value
        		//
        		if (sourceMeta.isNull(sourceValue))
        		{
        			sum = previousTarget;
        		}
        		else
        		{
        			if (sourceMeta.isInteger())
        			{
        				sum = ValueDataUtil.plus(data.valueMetaInteger, data.previousAvgSum[i], sourceMeta, row[sourceIndex]);
        			}
        			else
        			{
        				sum = ValueDataUtil.plus(targetMeta, data.previousAvgSum[i], sourceMeta, row[sourceIndex]);
        			}
        		}
        	}
        	data.previousAvgSum[i] = sum;

        	if (!sourceMeta.isNull(sourceValue)) data.previousAvgCount[i]++;
        	
        	if (sourceMeta.isInteger()) {
        		// Change to number as the exception
        		//
        		if (sum==null)
        		{
        			row[targetIndex] = null;
        		}
        		else 
        		{
        			row[targetIndex] = new Double( ((Long)sum).doubleValue() / data.previousAvgCount[i] );
        		}
        	}
        	else
        	{
        		row[targetIndex] = ValueDataUtil.divide(targetMeta, sum, data.valueMetaInteger, data.previousAvgCount[i]);
        	}
        }

	}

	// Is the row r of the same group as previous?
	private boolean sameGroup(Object[] previous, Object[] r) throws KettleValueException
	{
		return data.inputRowMeta.compare(previous, r, data.groupnrs) == 0;
	}
	
	// Calculate the aggregates in the row...
	@SuppressWarnings("unchecked")
	private void calcAggregate(Object[] r) throws KettleValueException
	{
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			Object subj = r[data.subjectnrs[i]];
			ValueMetaInterface subjMeta=data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
			Object value = data.agg[i];
			ValueMetaInterface valueMeta=data.aggMeta.getValueMeta(i);
			
			//System.out.println("  calcAggregate value, i="+i+", agg.size()="+agg.size()+", subj="+subj+", value="+value);
			
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            :
					data.agg[i]=ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
					break; 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
					if (!subjMeta.isNull(subj)) {
						data.agg[i]=ValueDataUtil.sum(valueMeta, value, subjMeta, subj);
						data.counts[i]++;
					}
					break; 
				case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
          if (!subjMeta.isNull(subj)) {
  					data.counts[i]++;
  					double n = data.counts[i];
  					double x = subjMeta.getNumber(subj);
  					double sum = (Double)value;
  					double mean = data.mean[i];
  					
  					double delta = x - mean;
  					mean = mean + (delta/n);
  					sum = sum + delta*(x-mean);
  					
  					data.mean[i] = mean;
  					data.agg[i] = sum;
          }
					break; 
				case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT :
				  if (!subjMeta.isNull(subj)) {
				    if (data.distinctObjs == null) {
				      data.distinctObjs = new Set[meta.getSubjectField().length];
				    }
				    if (data.distinctObjs[i] == null) {
				      data.distinctObjs[i] = new TreeSet<Object>();
				    }
				    Object obj = subjMeta.convertToNormalStorageType(subj);
				    if (!data.distinctObjs[i].contains(obj)) {
				      data.distinctObjs[i].add(obj);
				      data.agg[i] = (Long)value + 1;
				    }
				  }
				case GroupByMeta.TYPE_GROUP_COUNT_ALL      :
					if (!subjMeta.isNull(subj)) {
						data.counts[i]++;
					}
					break;
				case GroupByMeta.TYPE_GROUP_MIN            :
				  if(subjMeta.isSortedDescending()) {
				    // Account for negation in ValueMeta.compare() - See PDI-2302
				    if (subjMeta.compare(value,valueMeta,subj)<0) data.agg[i]=subj;
				  } else {
				    if (subjMeta.compare(subj,valueMeta,value)<0) data.agg[i]=subj;
				  }
					break; 
				case GroupByMeta.TYPE_GROUP_MAX            :
				  if(subjMeta.isSortedDescending()) {
				    // Account for negation in ValueMeta.compare() - See PDI-2302
				    if (subjMeta.compare(value,valueMeta,subj)>0) data.agg[i]=subj;
				  } else {
				    if (subjMeta.compare(subj,valueMeta,value)>0) data.agg[i]=subj;
				  }
					break; 
                case GroupByMeta.TYPE_GROUP_FIRST          :
                    if (!(subj==null) && value==null) data.agg[i]=subj;
                    break; 
                case GroupByMeta.TYPE_GROUP_LAST           : 
                    if (!(subj==null)) data.agg[i]=subj; 
                    break; 
                case GroupByMeta.TYPE_GROUP_FIRST_INCL_NULL:
                	// This is on purpose. The calculation of the 
                	// first field is done when setting up a new group
                	// This is just the field of the first row
                    // if (linesWritten==0) value.setValue(subj);
                    break; 
                case GroupByMeta.TYPE_GROUP_LAST_INCL_NULL : 
                	data.agg[i]=subj; 
                    break; 
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA   :
                    if (!(subj==null)) 
                    {
                    	String vString=valueMeta.getString(value);
                        if (vString.length()>0) vString=vString+", "; //$NON-NLS-1$
                        data.agg[i]=vString+subjMeta.getString(subj);
                    }
                    break; 
                case GroupByMeta.TYPE_GROUP_CONCAT_STRING   :
                    if (!(subj==null)) 
                    {
                    	String separator="";
                    	if(!Const.isEmpty(meta.getValueField()[i])) separator=environmentSubstitute(meta.getValueField()[i]);
                    	String vString=valueMeta.getString(value);
                        if (vString.length()>0) vString=vString+separator; //$NON-NLS-1$
                        data.agg[i]=vString+subjMeta.getString(subj);
                    }
                    
                    break; 
				default: break;
			}
		}
	}

	// Initialize a group..
	private void newAggregate(Object[] r)
	{
		// Put all the counters at 0
		for (int i=0;i<data.counts.length;i++) data.counts[i]=0;
		data.distinctObjs = null;
		data.agg = new Object[data.subjectnrs.length];
		data.mean = new double[data.subjectnrs.length]; // sets all doubles to 0.0
		data.aggMeta=new RowMeta();
		
		for (int i=0;i<data.subjectnrs.length;i++)
		{
			ValueMetaInterface subjMeta=data.inputRowMeta.getValueMeta(data.subjectnrs[i]);
			Object v=null;
			ValueMetaInterface vMeta=null;
			switch(meta.getAggregateType()[i])
			{
				case GroupByMeta.TYPE_GROUP_SUM            : 
				case GroupByMeta.TYPE_GROUP_AVERAGE        :
				case GroupByMeta.TYPE_GROUP_CUMULATIVE_SUM : 
				case GroupByMeta.TYPE_GROUP_CUMULATIVE_AVERAGE :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], subjMeta.isNumeric()?subjMeta.getType():ValueMetaInterface.TYPE_NUMBER);
                    switch(subjMeta.getType())
                    {
                    case ValueMetaInterface.TYPE_BIGNUMBER: v=new BigDecimal("0"); break;
                    case ValueMetaInterface.TYPE_INTEGER:   v=new Long(0L); break;
                    case ValueMetaInterface.TYPE_NUMBER:    
                    default:                         v=new Double(0.0); break;
                    }
					break; 					
				case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_NUMBER);
                    v=new Double(0.0);
                    break;
				case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT  :
				case GroupByMeta.TYPE_GROUP_COUNT_ALL       :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_INTEGER);
                    v=new Long(0L);                   
					break; 
				case GroupByMeta.TYPE_GROUP_FIRST           :
				case GroupByMeta.TYPE_GROUP_LAST            :
				case GroupByMeta.TYPE_GROUP_FIRST_INCL_NULL :
				case GroupByMeta.TYPE_GROUP_LAST_INCL_NULL  :					
				case GroupByMeta.TYPE_GROUP_MIN             : 
				case GroupByMeta.TYPE_GROUP_MAX             : 
					vMeta = subjMeta.clone();
					vMeta.setName(meta.getAggregateField()[i]);
					v = r==null ? null : r[data.subjectnrs[i]]; 
					break;
                case GroupByMeta.TYPE_GROUP_CONCAT_COMMA    :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_STRING);
                    v = ""; //$NON-NLS-1$
                    break; 
                case GroupByMeta.TYPE_GROUP_CONCAT_STRING   :
                    vMeta = new ValueMeta(meta.getAggregateField()[i], ValueMetaInterface.TYPE_STRING);
                    v = ""; //$NON-NLS-1$
                    break; 
				default: 
					// TODO raise an error here because we cannot continue successfully maybe the UI should validate this
					break;
			}
            
            if (meta.getAggregateType()[i]!=GroupByMeta.TYPE_GROUP_COUNT_ALL && 
                meta.getAggregateType()[i]!=GroupByMeta.TYPE_GROUP_COUNT_DISTINCT)
            {
            	vMeta.setLength(subjMeta.getLength(), subjMeta.getPrecision());
            }
			if (v!=null) data.agg[i]=v;
			data.aggMeta.addValueMeta(vMeta);
		}
		
		// Also clear the cumulative data...
		//
		for (int i=0;i<data.previousSums.length;i++) data.previousSums[i]=null;
		for (int i=0;i<data.previousAvgCount.length;i++) 
		{
			data.previousAvgCount[i]=0L;
			data.previousAvgSum[i]=null;
		}
	}
	
	private Object[] buildResult(Object[] r) throws KettleValueException
	{
		Object[] result=null;
		if (r!=null || meta.isAlwaysGivingBackOneRow()) {
			result = RowDataUtil.allocateRowData(data.groupnrs.length);
			if (r!=null)
			{
				for (int i=0;i<data.groupnrs.length;i++)
				{
					result[i]=r[data.groupnrs[i]]; 
				}
			}
			
			result=RowDataUtil.addRowData(result, data.groupnrs.length, getAggregateResult());
		}
        
		return result;
	}

	private void initGroupMeta(RowMetaInterface previousRowMeta) throws KettleValueException
	{
		data.groupMeta=new RowMeta();
		for (int i=0;i<data.groupnrs.length;i++)
		{
			data.groupMeta.addValueMeta(previousRowMeta.getValueMeta(data.groupnrs[i]));
		}
        
		return;
	}

    private Object[] getAggregateResult() throws KettleValueException
    {
        Object[] result = new Object[data.subjectnrs.length];

        if (data.subjectnrs!=null)
        {
            for (int i=0;i<data.subjectnrs.length;i++)
            {
                Object ag = data.agg[i];
                switch(meta.getAggregateType()[i])
                {
                    case GroupByMeta.TYPE_GROUP_SUM            : break; 
                    case GroupByMeta.TYPE_GROUP_AVERAGE        :
                    		ag=ValueDataUtil.divide(data.aggMeta.getValueMeta(i), ag, 
                    				new ValueMeta("c",ValueMetaInterface.TYPE_INTEGER), new Long(data.counts[i])); 
                    		break;  //$NON-NLS-1$
                    case GroupByMeta.TYPE_GROUP_COUNT_ALL      : ag=new Long(data.counts[i]); break;
                    case GroupByMeta.TYPE_GROUP_COUNT_DISTINCT : break;
                    case GroupByMeta.TYPE_GROUP_MIN            : break; 
                    case GroupByMeta.TYPE_GROUP_MAX            : break; 
                    case GroupByMeta.TYPE_GROUP_STANDARD_DEVIATION :
                    	double sum = (Double)ag / data.counts[i];
                    	ag = Double.valueOf( Math.sqrt( sum ) );
                    	break;
                    default: break;
                }
                result[i]=ag;
            }
        }

        return result;

    }
		
    private void addToBuffer(Object[] row) throws KettleFileException
    {
        data.bufferList.add(row);
        if (data.bufferList.size()>5000)
        {
            if (data.rowsOnFile==0)
            {
                try
                {
                    data.tempFile = File.createTempFile(meta.getPrefix(), ".tmp", new File(environmentSubstitute(meta.getDirectory()))); //$NON-NLS-1$
                    data.fos=new FileOutputStream(data.tempFile);
                    data.dos=new DataOutputStream(data.fos);
                    data.firstRead = true;
                }
                catch(IOException e)
                {
                    throw new KettleFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCreateTemporaryFile"), e); //$NON-NLS-1$
                }
            }
            // OK, save the oldest rows to disk!
            Object[] oldest = (Object[]) data.bufferList.get(0);
            data.inputRowMeta.writeData(data.dos, oldest);
            data.bufferList.remove(0);
            data.rowsOnFile++;
        }
    }
    
    private Object[] getRowFromBuffer() throws KettleFileException
    {
        if (data.rowsOnFile>0)
        {
            if (data.firstRead)
            {
                // Open the inputstream first...
                try
                {
                    data.fis=new FileInputStream( data.tempFile );
                    data.dis=new DataInputStream( data.fis );
                    data.firstRead = false;
                }
                catch(IOException e)
                {
                    throw new KettleFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToReadBackRowFromTemporaryFile"), e); //$NON-NLS-1$
                }
            }
            
            // Read one row from the file!
            Object[] row;
			try 
			{
				row = data.inputRowMeta.readData(data.dis);
			} 
			catch (SocketTimeoutException e) 
			{
				throw new KettleFileException(e); // Shouldn't happen on files
			}
            data.rowsOnFile--;
            
            return row;
        }
        else
        {
            if (data.bufferList.size()>0)
            {
            	Object[] row = (Object[])data.bufferList.get(0);
                data.bufferList.remove(0);
                return row;
            }
            else
            {
                return null; // Nothing left!
            }
        }
    }
    
    private void closeOutput() throws KettleFileException
    {
        try
        {
            if (data.dos!=null) { data.dos.close(); data.dos=null; }
            if (data.fos!=null) { data.fos.close(); data.fos=null; }
            data.firstRead = true;
        }
        catch(IOException e)
        {
            throw new KettleFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCloseInputStream"), e); //$NON-NLS-1$
        }
    }
    
    private void closeInput() throws KettleFileException
    {
        try
        {
            if (data.fis!=null) { data.fis.close(); data.fis=null; }
            if (data.dis!=null) { data.dis.close(); data.dis=null; }
        }
        catch(IOException e)
        {
            throw new KettleFileException(BaseMessages.getString(PKG, "GroupBy.Exception.UnableToCloseInputStream"), e); //$NON-NLS-1$
        }
    }

    
	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(GroupByMeta)smi;
		data=(GroupByData)sdi;
		
		if (super.init(smi, sdi))
		{
            data.bufferList = new ArrayList<Object[]>();
            
            data.rowsOnFile = 0;
            
            return true;
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi) 
	{
		if (data.tempFile!=null) data.tempFile.delete();
		
		super.dispose(smi, sdi);
	}
	
	public void batchComplete() throws KettleException {
	  handleLastOfGroup();
	  data.newBatch=true;
	}
}