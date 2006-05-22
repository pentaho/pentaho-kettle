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

package be.ibridge.kettle.job;

import java.util.Date;

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.job.entry.JobEntryCopy;


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
	 * @param thisJobEntry The thisJobEntry to set.
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

