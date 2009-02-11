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
 
package be.ibridge.kettle.trans.step.addsequence;

import java.util.Hashtable;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;


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
	
	public boolean addSequence(Row row) throws KettleException
	{
		Value next = null;
		
		if (meta.isDatabaseUsed())
		{
			try
			{
				next = data.getDb().getNextSequenceValue(meta.getSchemaName(), meta.getSequenceName(), meta.getValuename());
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleStepException(Messages.getString("AddSequence.Exception.ErrorReadingSequence",meta.getSequenceName()), dbe); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		if (meta.isCounterUsed())
		{
			Long prev = (Long)getTransMeta().getCounters().get(data.getLookup());
			
			//System.out.println("Found prev value: "+prev.longValue()+", increment_by = "+info.increment_by+", max_value = "+info.max_value);
			long nval = prev.longValue() + meta.getIncrementBy();
			if (meta.getIncrementBy()>0 && meta.getMaxValue()>meta.getStartAt() && nval>meta.getMaxValue()) nval=meta.getStartAt();
			if (meta.getIncrementBy()<0 && meta.getMaxValue()<meta.getStartAt() && nval<meta.getMaxValue()) nval=meta.getStartAt();
			getTransMeta().getCounters().put(data.getLookup(), new Long(nval)); 

			next = new Value(meta.getValuename(), prev.longValue());
			//System.out.println("Next value: "+next);
		}
		else
		{
			// This should never happen, but if it does, don't continue!!!
			throw new KettleStepException(Messages.getString("AddSequence.Exception.NoSpecifiedMethod")); //$NON-NLS-1$
		}
		
		if (next!=null)
		{
			row.addValue(next);
		}
		else
		{
			throw new KettleStepException(Messages.getString("AddSequence.Exception.CouldNotFindNextValueForSequence")+meta.getValuename()); //$NON-NLS-1$
		}

		return true;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException
	{
		meta=(AddSequenceMeta)smi;
		data=(AddSequenceData)sdi;
		
		 boolean sendToErrorRow=false;
		 String errorMessage = null;


		Row r=null;
		
		r=getRow();       // Get row from input rowset & set row busy!		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

        if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("AddSequence.Log.ReadRow")+linesRead+" : "+r); //$NON-NLS-1$ //$NON-NLS-2$

		try
		{
			addSequence(r); // add new values to the row in rowset[0].			
			putRow(r);       // copy row to output rowset(s);
			
            if (log.isRowLevel()) log.logRowlevel(toString(), Messages.getString("AddSequence.Log.WriteRow")+linesWritten+" : "+r); //$NON-NLS-1$ //$NON-NLS-2$
			if (checkFeedback(linesRead)) logBasic(Messages.getString("AddSequence.Log.LineNumber")+linesRead); //$NON-NLS-1$
		}
		catch(KettleException e)
		{
			if (getStepMeta().isDoingErrorHandling())
			{
		          sendToErrorRow = true;
		          errorMessage = e.toString();
			}
			else
			{
				logError(Messages.getString("AddSequence.Log.ErrorInStep")+e.getMessage()); //$NON-NLS-1$
				setErrors(1);
				stopAll();
				setOutputDone();  // signal end to receiver(s)
				return false;
			}
			if (sendToErrorRow)
			{
			   // Simply add this row to the error row
			   putError(r, 1, errorMessage, null, "ADDSEQ001");
			}

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
				data.setDb( new Database(meta.getDatabase()) );
				try
				{
					if (getTransMeta().isUsingUniqueConnections()) 
					{
						synchronized (getTrans()) { data.getDb().connect(getTrans().getThreadName(), getPartitionID()); }
					} 
					else 
					{
						data.getDb().connect(getPartitionID()); 
					}
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
					Hashtable counters = getTransMeta().getCounters();
					counters.put(data.getLookup(), new Long(meta.getStartAt()));
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
	//
	public void run()
	{		
		try
		{
			logBasic(Messages.getString("AddSequence.Log.StartingToRun")); //$NON-NLS-1$
			while (processRow(meta, data) && !isStopped());
		}
		catch(Exception e)
		{
			logError(Messages.getString("AddSequence.Log.UnexpectedError")+" : "+e.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            logError(Const.getStackTracker(e));
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
