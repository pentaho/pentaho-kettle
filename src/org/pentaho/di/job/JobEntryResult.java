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

package org.pentaho.di.job;

import java.util.Date;

import org.pentaho.di.core.Result;
import org.pentaho.di.job.entry.JobEntryCopy;



/**
 * This class holds the result of a job entry after it was executed.
 * Things we want to keep track of are:<p>
 * --> result of the execution (Result)<p>
 * --> ...<p>
 * 
 * @author Matt
 * @since  16-mrt-2005
 */
public class JobEntryResult implements Cloneable
{
	private Result result;
	private JobEntryCopy jobEntry;

	private String comment;
    private String reason;
    
    private Date   logDate;

	/**
	 * Creates a new empty job entry result...
	 */
	public JobEntryResult()
	{
        logDate = new Date();
	}

	/**
	 * Creates a new job entry result...
     * @param result the result of the job entry
     * @param comment an optional comment
     * @param jobEntry the job entry for which this is the result.
	 */
	public JobEntryResult(Result result, String comment, String reason, JobEntryCopy jobEntry)
	{
        this();
		if (result!=null) 
        { 
            this.result = (Result) result.clone(); 
        }
        else 
        { 
            this.result = null;
        }
        this.comment = comment;
        this.reason = reason;
        this.jobEntry = jobEntry;
	}
	
	public Object clone()
	{
		try
		{
			JobEntryResult jobEntryResult = (JobEntryResult)super.clone();
			
			if (getResult()!=null) 
				jobEntryResult.setResult((Result)getResult().clone());
			
			return jobEntryResult;
		}
		catch(CloneNotSupportedException e)
		{
			return null;
		}
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
	 * @param jobEntry The thisJobEntry to set.
	 */
	public void setJobEntry(JobEntryCopy jobEntry)
	{
		this.jobEntry = jobEntry;
	}
	
	/**
	 * @return Returns the thisJobEntry.
	 */
	public JobEntryCopy getJobEntry()
	{
		return jobEntry;
	}
	
	/**
	 * @return Returns the comment.
	 */
	public String getComment()
	{
		return comment;
	}
	
	/**
	 * @param comment The comment to set.
	 */
	public void setComment(String comment)
	{
		this.comment = comment;
	}

    /**
     * @return Returns the reason.
     */
    public String getReason()
    {
        return reason;
    }

    /**
     * @param reason The reason to set.
     */
    public void setReason(String reason)
    {
        this.reason = reason;
    }

    /**
     * @return Returns the logDate.
     */
    public Date getLogDate()
    {
        return logDate;
    }

    /**
     * @param logDate The logDate to set.
     */
    public void setLogDate(Date logDate)
    {
        this.logDate = logDate;
    }	
}

