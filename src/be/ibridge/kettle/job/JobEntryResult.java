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

import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.job.entry.JobEntryCopy;


/**
 * This class holds the result of a job entry after it was executed.
 * Things we want to keep track of are:<p>
 * --> result of the execution (Result)<p>
 * --> next step to execute<p>
 * --> ...<p>
 * 
 * @author Matt
 * @since  16-mrt-2005
 */
public class JobEntryResult implements Cloneable
{
	private Result result;
	private JobEntryCopy thisJobEntry;
	private JobEntryCopy prevJobEntry;
	private String comment;
	private String jobName;
	
	/**
	 * Creates a new empty job entry result...
	 */
	public JobEntryResult()
	{
	}

	/**
	 * Creates a new job entry result...
	 */
	public JobEntryResult(Result result)
	{
		this.result = result;
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
	public void setThisJobEntry(JobEntryCopy thisJobEntry)
	{
		this.thisJobEntry = thisJobEntry;
	}
	
	/**
	 * @return Returns the thisJobEntry.
	 */
	public JobEntryCopy getThisJobEntry()
	{
		return thisJobEntry;
	}
	
	/**
	 * @param nextJobEntry The nextJobEntry to set.
	 */
	public void setPrevJobEntry(JobEntryCopy nextJobEntry)
	{
		this.prevJobEntry = nextJobEntry;
	}
	
	/**
	 * @return Returns the nextJobEntry.
	 */
	public JobEntryCopy getPrevJobEntry()
	{
		return prevJobEntry;
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
	 * @param jobName The jobName to set.
	 */
	public void setJobName(String jobName)
	{
		this.jobName = jobName;
	}
	
	/**
	 * @return Returns the jobName.
	 */
	public String getJobName()
	{
		return jobName;
	}
	
}

