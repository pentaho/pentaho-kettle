/*
 *
 *
 */

package be.ibridge.kettle.job.entry.job;

import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.job.Job;

/**
 * 
 * 
 * @author Matt
 * @since  6-apr-2005
 */
public class JobEntryJobRunner implements Runnable
{
	private Job       job;
	private Result    result;
	private LogWriter log;
	private int       entryNr;
	private boolean   finished;
	
	/**
	 * @deprecated
	 */
	public JobEntryJobRunner(Job job, Result result, LogWriter log, int entryNr)
	{
		this.job = job;
		this.result = result;
		this.log = log;
		this.entryNr = entryNr;
		finished=false;
	}
    
    /**
     * 
     */
    public JobEntryJobRunner(Job job, Result result, int entryNr)
    {
        this.job = job;
        this.result = result;
        this.log = LogWriter.getInstance();
        this.entryNr = entryNr;
        finished=false;
    }
	
	public void run()
	{
		try
		{
			result = job.execute(entryNr+1, result);
		}
		catch(KettleJobException e)
		{
			log.logError(toString(), "An error occurred executing this job entry : "+e.getMessage());
			result.setNrErrors(1);
		}
		finished=true;
	}
	
	/**
	 * @param result The result to set.
	 */
	public void setResult(Result result)
	{
		this.result = result;
	}
	
	/**
	 * @return Returns the result.
	 */
	public Result getResult()
	{
		return result;
	}
	
	/**
	 * @return Returns the log.
	 */
	public LogWriter getLog()
	{
		return log;
	}
	
	/**
	 * @param log The log to set.
	 */
	public void setLog(LogWriter log)
	{
		this.log = log;
	}	

	/**
	 * @return Returns the job.
	 */
	public Job getJob()
	{
		return job;
	}
	
	/**
	 * @param job The job to set.
	 */
	public void setJob(Job job)
	{
		this.job = job;
	}
	
	/**
	 * @return Returns the entryNr.
	 */
	public int getEntryNr()
	{
		return entryNr;
	}
	
	/**
	 * @param entryNr The entryNr to set.
	 */
	public void setEntryNr(int entryNr)
	{
		this.entryNr = entryNr;
	}
	
	/**
	 * @return Returns the finished.
	 */
	public boolean isFinished()
	{
		return finished;
	}
	
	public void waitUntilFinished()
	{
		while (!isFinished())
		{
			try { Thread.sleep(100); }
			catch(InterruptedException e) { }
		}
	}
}
