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

package org.pentaho.di.trans.steps.multimerge;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
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
import org.pentaho.di.trans.step.errorhandling.StreamInterface;


/**
 * Merge rows from 2 sorted streams and output joined rows with matched key fields.
 * Use this instead of hash join is both your input streams are too big to fit in
 * memory. Note that both the inputs must be sorted on the join key.
 * 
 * This is a first prototype implementation that only handles two streams and
 * inner join. It also always outputs all values from both streams. Ideally, we should:
 *   1) Support any number of incoming streams
 *   2) Allow user to choose the join type (inner, outer) for each stream
 *   3) Allow user to choose which fields to push to next step
 *   4) Have multiple output ports as follows:
 *      a) Containing matched records
 *      b) Unmatched records for each input port
 *   5) Support incoming rows to be sorted either on ascending or descending order.
 *      The currently implementation only supports ascending
 * @author Biswapesh
 * @since 24-nov-2006
 */

public class MultiMergeJoin extends BaseStep implements StepInterface
{
	private static Class<?> PKG = MultiMergeJoinMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private MultiMergeJoinMeta meta;
	private MultiMergeJoinData data;
	
            
	public MultiMergeJoin(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(MultiMergeJoinMeta)smi;
		data=(MultiMergeJoinData)sdi;

        if (first)
        {
            first = false;
            
            // Find the RowSet to read from
            //
            List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
            int streamSize = infoStreams.size();
            data.rowSets = new RowSet[streamSize];
            data.rows = new Object[streamSize][];
            data.metas = new RowMetaInterface[streamSize];
            data.rowLengths = new int[streamSize];
            data.queue = new PriorityQueue<MultiMergeJoinData.QueueEntry>(streamSize, new MultiMergeJoinData.QueueComparator(data));
            data.results = new ArrayList<List<Object[]>>(streamSize);
            data.queueEntries = new MultiMergeJoinData.QueueEntry[streamSize];
            data.drainIndices = new int[streamSize];
            for (int i = 0; i < streamSize; i++) {
                data.queueEntries[i] = new MultiMergeJoinData.QueueEntry();
                data.queueEntries[i].index = i;
                data.results.add(new ArrayList<Object[]>());
                data.rowSets[i] = findInputRowSet(infoStreams.get(i).getStepname());
                if (data.rowSets[i]==null) {
            	    throw new KettleException( BaseMessages.getString(PKG, "MultiMergeJoin.Exception.UnableToFindSpecifiedStep", infoStreams.get(0).getStepname()) );
                }
                data.rows[i] = getRowFrom(data.rowSets[i]);
                if (data.rows[i] == null) {
                	data.metas[i] = data.rowSets[i].getRowMeta();
                } else {
                    data.queueEntries[i].row = data.rows[i];                       
                	  data.metas[i]=getTransMeta().getStepFields(infoStreams.get(i).getStepname());
                }
                data.rowLengths[i] = data.metas[i].size();
            }
            
            //
            data.outputRowMeta=new RowMeta(); 
            for (int i = 0; i < streamSize; i++) {
                data.outputRowMeta.mergeRowMeta( data.metas[i].clone() );
            }

            data.keyNrs = new int[streamSize][];
            
            for (int j = 0; j < streamSize; j++)
            {
                if (data.rows[j]!=null)
                {
                    /*// Find the key indexes:
                    data.keyNrs[j] = new int[meta.getKeyFields().length];
                    for (int i=0;i<meta.getKeyFields().length; i++)
                    {
                        data.keyNrs[j][i] = data.metas[j].indexOfValue(meta.getKeyFields()[i]);
                        if (data.keyNrs[j][i]<0)
                        {
                            String message = BaseMessages.getString(PKG, "MultiMergeJoin.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                            logError(message);
                            throw new KettleStepException(message);
                        }
                    }*/
                	String[] keyFields = meta.getKeyFields()[j].split(",");
                	data.keyNrs[j] = new int[keyFields.length];
                	for (int i=0;i<keyFields.length; i++)
                    {
                        data.keyNrs[j][i] = data.metas[j].indexOfValue(keyFields[i]);
                        if (data.keyNrs[j][i]<0)
                        {
                            String message = BaseMessages.getString(PKG, "MultiMergeJoin.Exception.UnableToFindFieldInReferenceStream",meta.getKeyFields()[i]);  //$NON-NLS-1$ //$NON-NLS-2$
                            logError(message);
                            throw new KettleStepException(message);
                        }
                    }                	
                    data.queue.add(data.queueEntries[j]);
                }
            }

            data.dummy = new Object[streamSize][];
            for (int i = 0; i < streamSize; i++) {
                // Calculate dummy... defaults to null
                data.dummy[i]=RowDataUtil.allocateRowData( data.metas[i].size());
            }
        }

        if (log.isRowLevel()) {
        	String metaString = BaseMessages.getString(PKG, "MultiMergeJoin.Log.DataInfo",data.metas[0].getString(data.rows[0])+"");
        	for (int i = 1; i < data.metas.length; i++)
        		metaString += data.metas[i].getString(data.rows[i]);
        	logRowlevel(metaString); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /*
         * We can stop processing if any of the following is true:
         *   a) All streams are empty
         *   b) Any stream is empty and join type is INNER
         */
        int streamSize = data.metas.length;
        if (data.optional) {
           if (data.queue.isEmpty()) {
               setOutputDone();
               return false;
           }
           MultiMergeJoinData.QueueEntry minEntry = data.queue.poll();
           int drainSize = 1;
           data.rows[minEntry.index] = minEntry.row;
           data.drainIndices[0] = minEntry.index;
           MultiMergeJoinData.QueueComparator comparator = (MultiMergeJoinData.QueueComparator) data.queue.comparator();
           while (!data.queue.isEmpty() && comparator.compare(data.queue.peek(), minEntry) == 0) {
               MultiMergeJoinData.QueueEntry entry = data.queue.poll();
               data.rows[entry.index] = entry.row;
               data.drainIndices[drainSize++]=entry.index; 
           }
	         int index;
     	     Object[] row = null;
           // rows from nonempty input streams match: get all equal rows and  create result set
           for (int i = 0; i < drainSize; i++) {
               index = data.drainIndices[i];
               data.results.get(index).add(data.rows[index]);
               while (!isStopped() && ((row = getRowFrom(data.rowSets[index])) != null &&
                                      data.metas[index].compare(data.rows[index], row,
                                         data.keyNrs[index]) == 0)) {
                   data.results.get(index).add(row);
               }
               if (isStopped()) return false;
               if (row != null)
               {
                   data.queueEntries[index].row = row;
                   data.queue.add(data.queueEntries[index]);
               }
           }
           for (int i = 0; i < streamSize; i++)
           {
               data.drainIndices[i] = 0;
               if (data.results.get(i).isEmpty())
                 data.results.get(i).add(data.dummy[i]);
           }
           
           int current = 0;

           while (true) {
               for (int i = 0; i < streamSize; i++) {
                   data.rows[i] = data.results.get(i).get(data.drainIndices[i]);
               }
               row = RowDataUtil.createResizedCopy(data.rows, data.rowLengths);
               
               putRow(data.outputRowMeta, row);
               
               while (++data.drainIndices[current] >= data.results.get(current).size()) {
                    data.drainIndices[current] = 0;
			             if (++current >= streamSize)
                       break;
               }
               if (current >= streamSize)
                   break;
               current = 0;
	         }
           for (int i = 0; i < streamSize; i++) {
               data.results.get(i).clear();
           }
        } else {
           if (data.queue.size() < streamSize) {
               data.queue.clear();
               for (int i = 0; i < streamSize; i++)
        	   while (data.rows[i]!=null && !isStopped()) data.rows[i] = getRowFrom(data.rowSets[i]);
               setOutputDone();
               return false;
           }
           
           MultiMergeJoinData.QueueEntry minEntry = data.queue.poll();
           int drainSize = 1;
           data.rows[minEntry.index] = minEntry.row;
           data.drainIndices[0] = minEntry.index;
           MultiMergeJoinData.QueueComparator comparator = (MultiMergeJoinData.QueueComparator) data.queue.comparator();
           while (!data.queue.isEmpty() && comparator.compare(data.queue.peek(), minEntry) == 0) {
               MultiMergeJoinData.QueueEntry entry = data.queue.poll();
               data.rows[entry.index] = entry.row;
               data.drainIndices[drainSize++]=entry.index; 
           }
           Object[] row = null;
           if (data.queue.isEmpty()) {
               // rows from all input streams match: get all equal rows and  create result set
               for (int i = 0; i < streamSize; i++) {
                   data.results.get(i).add(data.rows[i]);
                   while (!isStopped() && ((row = getRowFrom(data.rowSets[i])) != null &&
                                          data.metas[i].compare(data.rows[i], row,
                                             data.keyNrs[i]) == 0)) {
                       data.results.get(i).add(row);
                   }
                   if (isStopped()) return false;
                   if (row != null)
                   {
                       data.queueEntries[i].row = row;
                       data.queue.add(data.queueEntries[i]);
                   }
               }
               for (int i = 0; i < streamSize; i++)
               {
                   data.drainIndices[i] = 0;
               }
                   
               int current = 0;
               while (true) {
                   for (int i = 0; i < streamSize; i++) {
                     data.rows[i] = data.results.get(i).get(data.drainIndices[i]);
                   }
                   row = RowDataUtil.createResizedCopy(data.rows, data.rowLengths);
                   
                   putRow(data.outputRowMeta, row);
                   while (++data.drainIndices[current] >= data.results.get(current).size()) {
                       data.drainIndices[current] = 0;
			                 if (++current >= streamSize)
                           break;
                   }
		               if (current >= streamSize)
                       break;
		               current = 0;
               }
               for (int i = 0; i < streamSize; i++) {
                 data.results.get(i).clear();
               }
           } else {
               // mismatch found and no results can be generated
               
               for (int i = 0; i < drainSize; i++) {
                  int index = data.drainIndices[i];
                  while (!isStopped() && ((row = getRowFrom(data.rowSets[index])) != null &&
                                          data.metas[index].compare(data.rows[index], row,
                                             data.keyNrs[index]) == 0)) {
                  }
                  if (isStopped() || row == null)
                      break;
                  data.queueEntries[index].row = row;
                  data.queue.add(data.queueEntries[index]);
               }
               if (isStopped()) return false;
           }
        }
        if (checkFeedback(getLinesRead())) logBasic(BaseMessages.getString(PKG, "MultiMergeJoin.LineNumber")+getLinesRead()); //$NON-NLS-1$
		return true;
	}
		
	/**
     * @see StepInterface#init( org.pentaho.di.trans.step.StepMetaInterface , org.pentaho.di.trans.step.StepDataInterface)
     */
    public boolean init(StepMetaInterface smi, StepDataInterface sdi)
    {
		meta=(MultiMergeJoinMeta)smi;
		data=(MultiMergeJoinData)sdi;

        if (super.init(smi, sdi))
        {
            List<StreamInterface> infoStreams = meta.getStepIOMeta().getInfoStreams();
            for (int i = 0; i < infoStreams.size(); i++) {
                if (infoStreams.get(i).getStepMeta()==null)
                {
                    logError(BaseMessages.getString(PKG, "MultiMergeJoin.Log.BothTrueAndFalseNeeded")); //$NON-NLS-1$
                    return false;
                }
            }
            String joinType = meta.getJoinType();
            for (int i = 0; i < MultiMergeJoinMeta.join_types.length; ++i)
            {
            	if (joinType.equalsIgnoreCase(MultiMergeJoinMeta.join_types[i]))
            	{
            		data.optional = MultiMergeJoinMeta.optionals[i];
            		return true;
            	}
            }
           	logError(BaseMessages.getString(PKG, "MultiMergeJoin.Log.InvalidJoinType", meta.getJoinType())); //$NON-NLS-1$
               return false;
        }
        return true;
    }

    /**
     * Checks whether incoming rows are join compatible. This essentially
     * means that the keys being compared should be of the same datatype
     * and both rows should have the same number of keys specified
     * @param row1 Reference row
     * @param row2 Row to compare to
     * 
     * @return true when templates are compatible.
     */ 
    protected boolean isInputLayoutValid(RowMetaInterface[] rows)
    {
        if (rows!=null)
        {
            // Compare the key types
        	String keyFields[] = meta.getKeyFields();
          /*  int nrKeyFields = keyFields.length;

            for (int i=0;i<nrKeyFields;i++)
            {
            	  ValueMetaInterface v1 = rows[0].searchValueMeta(keyFields[i]);
                if (v1 == null)
                {
                	return false;
                }
                for (int j = 1; j < rows.length; j++) {
                    ValueMetaInterface v2 = rows[j].searchValueMeta(keyFields[i]);
                    if (v2 == null)
                    {
                      	return false;
                    }          
                    if ( v1.getType()!=v2.getType() )
                    {
                       	return false;
                    }
                }
            }*/
        	//check 1 : keys are configured for each stream
        	if(rows.length != keyFields.length)
        	{
        		logError("keys are not configured for all the streams ");
        		return false;
        	}
        	// check:2 No of keys are same for each stream
        	int prevCount = 0;
        	
        	List<String[]> keyList = new ArrayList<String[]>();
        	for(int i=0 ; i< keyFields.length ; i++)
        	{
        		String[] keys = keyFields[i].split(",");
        		keyList.add(keys);
        		int count = keys.length;
        		if(i!=0 && prevCount != count)
        		{
        			logError("Number of keys do not match ");
        			return false;
        		}
        		else
        		{
        		 prevCount = count;	
        		}
        	}
        	
        	//check:3 compare the key types
        	for (int i=0;i<prevCount;i++)
            {
				ValueMetaInterface preValue = null;
				for (int j = 0; j < rows.length; j++) {
					ValueMetaInterface v = rows[j].searchValueMeta(keyList
							.get(j)[i]);
					if (v == null) {
						return false;
					}
					if (j != 0 && v.getType() != preValue.getType()) {
						logError("key data type do not match ");
						return false;
					} else {
						preValue = v;
					}
				}
			}
        }
        // we got here, all seems to be ok.
        return true;
    }

}
