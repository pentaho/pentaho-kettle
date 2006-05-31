/*
 *
 *
 */

package be.ibridge.kettle.job.entry.job;

import be.ibridge.kettle.core.LocalVariables;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.exception.KettleException;
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
    
    private Thread    parentThread;
	
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
        
        this.parentThread = Thread.currentThread();
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
        
        this.parentThread = Thread.currentThread();
    }
	
	public void run()
	{
		try
		{
            // This JobEntryRunner is a replacement for the Job thread.
            // The job thread is never started because we simpy want to wait for the result.
            // That is the reason why it's not in the same namespace as the parent (job, chef, etc.)
            //
            LocalVariables.getInstance().createKettleVariables(Thread.currentThread().toString(), parentThread.toString(), false);
            
			result = job.execute(entryNr+1, result);
		}
		catch(KettleException e)
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
