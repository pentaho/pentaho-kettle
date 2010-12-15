/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
/*
 *
 *
 */

package org.pentaho.di.job.entries.job;

import org.pentaho.di.core.Result;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;

/**
 * 
 * 
 * @author Matt
 * @since  6-apr-2005
 */
public class JobEntryJobRunner implements Runnable
{
    private static Class<?> PKG = Job.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
  
	private Job       job;
	private Result    result;
	private LogChannelInterface log;
	private int       entryNr;
	private boolean   finished;
    
    /**
     * 
     */
    public JobEntryJobRunner(Job job, Result result, int entryNr, LogChannelInterface log)
    {
        this.job = job;
        this.result = result;
        this.log = log;
        this.entryNr = entryNr;
        finished=false;
    }
	
	public void run()
	{
		try
		{
            if (job.isStopped() || job.getParentJob()!=null && job.getParentJob().isStopped()) return;
            
            // This JobEntryRunner is a replacement for the Job thread.
            // The job thread is never started because we simply want to wait for the result.
            
			result = job.execute(entryNr+1, result);
		}
		catch(KettleException e)
		{
			log.logError("An error occurred executing this job entry : "+e.getMessage());
			result.setResult(false);
			result.setNrErrors(1);
		}
		finally 
		{
            try {
                job.fireJobListeners();
            } catch(KettleException e) {
                result.setNrErrors(1);
                result.setResult(false);
                log.logError(BaseMessages.getString(PKG, "Job.Log.ErrorExecJob", e.getMessage()), e);
            }
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
	public LogChannelInterface getLog()
	{
		return log;
	}
	
	/**
	 * @param log The log to set.
	 */
	public void setLog(LogChannelInterface log)
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
		while (!isFinished() && !job.isStopped())
		{
			try { Thread.sleep(0,1); }
			catch(InterruptedException e) { }
		}
	}
}
