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
	
	public synchronized boolean addSequence(Row row)
		throws KettleException
	{
		Value next = null;
		
		if (meta.isDatabaseUsed())
		{
			try
			{
				next = data.getDb().getNextSequenceValue(meta.getSequenceName(), meta.getValuename());
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleStepException("Error reading next value of sequence ["+meta.getSequenceName()+"] from database", dbe);
			}
		}
		else
		if (meta.isCounterUsed())
		{
			Long prev = (Long)getTransMeta().getCounters().get(data.getLookup());
			
			//System.out.println("Found prev value: "+prev.longValue()+", increment_by = "+info.increment_by+", max_value = "+info.max_value);
			long nval = prev.longValue() + meta.getIncrementBy();
			if (meta.getIncrementBy()>0 && nval>meta.getMaxValue()) nval=meta.getStartAt();
			if (meta.getIncrementBy()<0 && nval<meta.getMaxValue()) nval=meta.getStartAt();
			getTransMeta().getCounters().put(data.getLookup(), new Long(nval)); 

			next = new Value(meta.getValuename(), prev.longValue());
			//System.out.println("Next value: "+next);
		}
		else
		{
			// This should never happen, but if it does, don't continue!!!
			throw new KettleStepException("No method is specified in this step!");
		}
		
		if (next!=null)
		{
			row.addValue(next);
		}
		else
		{
			throw new KettleStepException("Couldn't find next value for sequence : "+meta.getValuename());
		}

		return true;
	}
	
	
	public boolean processRow(StepMetaInterface smi, StepDataInterface sdi)
	{
		meta=(AddSequenceMeta)smi;
		data=(AddSequenceData)sdi;

		Row r=null;
		
		r=getRow();       // Get row from input rowset & set row busy!		
		if (r==null)  // no more input to be expected...
		{
			setOutputDone();
			return false;
		}

        log.logRowlevel(toString(), "Read row #"+linesRead+" : "+r);

		try
		{
			addSequence(r); // add new values to the row in rowset[0].			
			putRow(r);       // copy row to output rowset(s);
			
            log.logRowlevel(toString(), "Wrote row #"+linesWritten+" : "+r);
			if ((linesRead>0) && (linesRead>0) && (linesRead%Const.ROWS_UPDATE)==0) logBasic("linenr "+linesRead);
		}
		catch(KettleException e)
		{
			logError("Because of an error, this step can't continue: "+e.getMessage());
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
				data.setDb( new Database(meta.getDatabase()) );
				try
				{
					data.getDb().connect();
					logBasic("Connected to database...");
					return true;
				}
				catch(KettleDatabaseException dbe)
				{
					logError("Step couldn't connect to the database: "+dbe.getMessage());
				}
			}
			else
			if (meta.isCounterUsed())
			{
				data.setLookup( "@@sequence:"+meta.getValuename() );

				if (getTransMeta().getCounters()!=null)
				{
					Hashtable counters = getTransMeta().getCounters();
					counters.put(data.getLookup(), new Long(meta.getStartAt()));
					return true;
				}
				else
				{
					logError("Sorry, Transformation counters hash table not allocated! (internal error)");
				}
			}
			else
			{
				logError("You need to select to create a sequence either using a database or an internal counter.");
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
