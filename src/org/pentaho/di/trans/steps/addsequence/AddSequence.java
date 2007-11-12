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
 
package org.pentaho.di.trans.steps.addsequence;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;



/**
 * Adds a sequential number to a stream of rows.
 * 
 * @author Matt
 * @since 13-mei-2003
 */
public class AddSequence extends BaseStep implements StepInterface
{
	private AddSequenceMeta meta;	
	private AddSequenceData data;
	
	public AddSequence(StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans)
	{
		super(stepMeta, stepDataInterface, copyNr, transMeta, trans);
	}
	
	public Object[] addSequence(RowMetaInterface inputRowMeta, Object[] inputRowData) throws KettleException
	{
		Object next = null;
		
		if (meta.isCounterUsed()) {
			synchronized (data.counter) {
				long prev = data.counter.getCounter();
				
				long nval = prev + meta.getIncrementBy();
				if (meta.getIncrementBy() > 0 && meta.getMaxValue() > meta.getStartAt() && nval > meta.getMaxValue())
					nval = meta.getStartAt();
				if (meta.getIncrementBy() < 0 && meta.getMaxValue() < meta.getStartAt() && nval < meta.getMaxValue())
					nval = meta.getStartAt();
				data.counter.setCounter(nval);
	
				next = prev;
			}
		} else if (meta.isDatabaseUsed()) {
			try {
				next = data.getDb().getNextSequenceValue(environmentSubstitute(meta.getSchemaName()), 
						                                 environmentSubstitute(meta.getSequenceName()), 
						                                 meta.getValuename());
			} catch (KettleDatabaseException dbe) {
				throw new KettleStepException(Messages.getString("AddSequence.Exception.ErrorReadingSequence", meta.getSequenceName()), dbe); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else {
			// This should never happen, but if it does, don't continue!!!
			throw new KettleStepException(Messages.getString("AddSequence.Exception.NoSpecifiedMethod")); //$NON-NLS-1$
		}
		
		if (next!=null)
		{
			Object[] outputRowData = inputRowData;
			if (inputRowData.length<inputRowMeta.size()+1) {
				outputRowData = RowDataUtil.resizeArray(inputRowData, inputRowMeta.size()+1);
			}
			outputRowData[inputRowMeta.size()]=next;
			return outputRowData;
		}
		else
		{
			throw new KettleStepException(Messages.getString("AddSequence.Exception.CouldNotFindNextValueForSequence")+meta.getValuename()); //$NON-NLS-1$
		}
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AddSequenceMeta)smi;
		data=(AddSequenceData)sdi;

        Object[] r=getRow();       // Get row from input rowset & set row busy!		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}
        
        if (first)
        {
            first=false;
            data.outputRowMeta = getInputRowMeta().clone();
            meta.getFields(data.outputRowMeta, getStepname(), null, null, this);
        }

        if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("AddSequence.Log.ReadRow")+linesRead+" : "+r); //$NON-NLS-1$ //$NON-NLS-2$

		try
		{
			putRow(data.outputRowMeta, addSequence(getInputRowMeta(), r)); 			
			
            if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("AddSequence.Log.WriteRow")+linesWritten+" : "+r); //$NON-NLS-1$ //$NON-NLS-2$
			if (checkFeedback(linesRead)) logBasic(Messages.getString("AddSequence.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			logError(Messages.getString("AddSequence.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
			setErrors(1);
			stopAll();
			setOutputDone();  // signal end to receiver(s)
			return false;
		}
			
		return true;
	}

	public boolean init(StepMetaInterface smi, StepDataInterface sdi)
	{		
		meta=(AddSequenceMeta)smi;
		data=(AddSequenceData)sdi;

		if (super.init(smi, sdi))
		{
			if (meta.isDatabaseUsed())
			{
				Database db = new Database(meta.getDatabase());
				db.shareVariablesWith(this);
				data.setDb( db );			
				try
				{
					data.getDb().connect(getPartitionID());
					logBasic(Messages.getString("AddSequence.Log.ConnectedDB")); //$NON-NLS-1$
					return true;
				}
				catch(KettleDatabaseException dbe)
				{
					logError(Messages.getString("AddSequence.Log.CouldNotConnectToDB")+dbe.getMessage()); //$NON-NLS-1$
				}
			}
			else
			if (meta.isCounterUsed())
			{
                if (meta.getCounterName()!=null)
                {
                    data.setLookup( "@@sequence:"+meta.getCounterName() ); //$NON-NLS-1$
                }
                else
                {
                    data.setLookup( "@@sequence:"+meta.getValuename() ); //$NON-NLS-1$
                }

				if (getTransMeta().getCounters()!=null)
				{
					//check if counter exists
					synchronized (getTransMeta().getCounters()){
						data.counter=getTransMeta().getCounters().get(data.getLookup());
						if (data.counter==null)
						{
							// create a new one
							data.counter = new Counter(meta.getStartAt(), meta.getIncrementBy(), meta.getMaxValue());
							getTransMeta().getCounters().put(data.getLookup(), data.counter);
						}
						else
						{
							// Check whether counter characteristics are the same as a previously
							// defined counter with the same name.
							if ( (data.counter.getStart() != meta.getStartAt()) ||
							   	 (data.counter.getIncrement() != meta.getIncrementBy()) ||								 
								 (data.counter.getMaximum() != meta.getMaxValue()) )
							{
								logError(Messages.getString("AddSequence.Log.CountersWithDifferentCharacteristics", data.getLookup())); //$NON-NLS-1$
								return false;
						    }
						}
					}
					return true;
				}
				else
				{
					logError(Messages.getString("AddSequence.Log.TransformationCountersHashtableNotAllocated")); //$NON-NLS-1$
				}
			}
			else
			{
				logError(Messages.getString("AddSequence.Log.NeedToSelectSequence")); //$NON-NLS-1$
			}
		}
		return false;
	}
	
	public void dispose(StepMetaInterface smi, StepDataInterface sdi)
	{
		if (meta.isCounterUsed())
		{
			getTransMeta().getCounters().remove(data.getLookup());
			data.counter=null;
		}
	    meta = (AddSequenceMeta)smi;
	    data = (AddSequenceData)sdi;
	    
	    if (meta.isDatabaseUsed())
	    {
	        data.getDb().disconnect();
	    }
	    
	    super.dispose(smi, sdi);
	}
	
	//
	// Run is were the action happens!
	public void run()
	{		
		try
		{
			logBasic(Messages.getString("System.Log.StartingToRun")); //$NON-NLS-1$
			
			while (processRow(meta, data) && !isStopped());
		}
		catch(Throwable t)
		{
			logError(Messages.getString("System.Log.UnexpectedError")+" : "); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(t));
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