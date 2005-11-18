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

import java.util.ArrayList;
import java.util.Date;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Result;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.Database;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleDatabaseException;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleJobException;
import be.ibridge.kettle.core.value.Value;
import be.ibridge.kettle.job.entry.JobEntryCopy;
import be.ibridge.kettle.job.entry.JobEntryInterface;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.StepLoader;


/**
 * This class executes a JobInfo object.
 * 
 * @author Matt
 * @since  07-apr-2003
 * 
 */
public class Job extends Thread
{
	private LogWriter log;
	private JobMeta jobinfo;
	private Repository rep;
	
	/**
	 * Keep a list of the job entries that were executed.
	 */
	private ArrayList jobEntryResults;
	
	private Date      startDate, endDate, currentDate, logDate, depDate;
	
	private boolean active, stopped;
	
	public Job(LogWriter lw, String name, String file, String args[])
	{
		this.log=lw;
		
		jobinfo = new JobMeta(log);
		jobinfo.setName(name);
		jobinfo.setFilename(file);
		jobinfo.arguments=args;
		active=false;
		stopped=false;
	}

	public Job(LogWriter lw, StepLoader steploader, Repository rep, JobMeta ti)
	{
		this.log        = lw;
		this.rep        = rep;
		this.jobinfo    = ti;
		
		active=false;
		stopped=false;
	}
	
	public void open(Repository rep, String fname, String jobname, String dirname)
		throws KettleException
	{
		this.rep = rep;
		if (rep!=null)
		{
			jobinfo = new JobMeta(log, rep, jobname, rep.getDirectoryTree().findDirectory(dirname));
		}
		else
		{
			jobinfo = new JobMeta(log, fname);
		}
	}

	public String getJobname()
	{
		if (jobinfo==null) return null;
		
		return jobinfo.getName();
	}

	public void setRepository(Repository rep)
	{
		this.rep=rep;
	}
	
	// Threads main loop: called by Thread.start();
	public void run()
	{
		try
		{
			execute(); // Run the job
			endProcessing("end");
		}
		catch(KettleJobException je)
		{
			System.out.println("A serious error occurred!"+Const.CR+je.getMessage());
		}
	}

	public Result execute()
		throws KettleJobException
	{
		if (jobEntryResults!=null) jobEntryResults.clear(); else jobEntryResults=new ArrayList();
		active=true;
		Result res = execute(1, null, null, null, "start");
		
		// Save this result...
		JobEntryResult jer = new JobEntryResult(res);
		jer.setComment("Job execution ended");
		jobEntryResults.add(jer);
		
		active=false;
		return res;	
	}

	/**
	 * Execute called by JobEntryJob: don't clear the jobEntryResults...
	 * @param nr
	 * @param result
	 * @return Result of the job execution
	 * @throws KettleJobException
	 */
	public Result execute(int nr, Result result)
		throws KettleJobException
	{
		Result res =  execute(nr, result, null, null, "start of job entry");

		return res;
	}
	
	private Result execute(int nr, Result prev_result, JobEntryCopy startpoint, JobEntryCopy previous, String comment)
		throws KettleJobException
	{
		Result res = null;
		JobEntryResult jer = new JobEntryResult();
		jer.setJobName(getJobinfo().getName());

		if (stopped)
		{
			res=new Result(nr);
			res.stopped=true;
			return res;
		}
		
		log.logDetailed(toString(), "exec("+nr+", "+(prev_result!=null?prev_result.getNrErrors():0)+", "+(startpoint!=null?startpoint.toString():"null")+")");
		
		// Where do we start?
		if (startpoint == null)
		{
			beginProcessing();
			startpoint = jobinfo.findJobEntry(JobMeta.STRING_SPECIAL_START, 0);
			if (startpoint == null) 
			{
				log.logError(toString(), "Couldn't find starting point in this job.");
				return prev_result;
			}
			else
			{
				// for the stats......
				jer.setComment("Start of job");
			}
		}
		jer.setThisJobEntry(startpoint);
		jer.setPrevJobEntry(previous);
		jer.setComment(comment);
		
		// What entry is next?
		JobEntryInterface jei = startpoint.getEntry();
		
		// Execute this entry...
		Result result = jei.execute(prev_result, nr, rep, this);
		
		// Save the result as well...
		jer.setResult((Result)result.clone());
		jobEntryResults.add(jer.clone());
				
		// Try all next job entries.
		// Launch only those where the hopinfo indicates true or false
		int nrNext = jobinfo.findNrNextChefGraphEntries(startpoint);
		for (int i=0;i<nrNext && !isStopped();i++)
		{
			// The next entry is...
			JobEntryCopy nextEntry = jobinfo.findNextChefGraphEntry(startpoint, i);
			
			// See if we need to execute this...
			JobHopMeta hi = jobinfo.findJobHop(startpoint, nextEntry);

			// The next comment...
			String nextComment = null;
			if (hi.isUnconditional()) 
			{
				nextComment = "Followed unconditional link";
			}
			else
			{
				if (result.getResult())
					nextComment = "Followed link after succes!";
				else
					nextComment = "Followed link after failure!";
			}

			// 
			// If the link is unconditional, execute the next job entry (entries).
			// If the startpoint was an evaluation and the link color is correct: green or red, execute the next job entry...
			//
			if (  hi.isUnconditional() || 
				( startpoint.evaluates() && ( ! ( hi.getEvaluation() ^ result.getResult() ) ) )
			   ) 
			{				
				// Start this next step!
				log.logBasic(jobinfo.toString(), "Starting entry ["+nextEntry.getName()+"]");
				// Pass along the previous result, perhaps the next job can use it...
				res = execute(nr+1, result, nextEntry, startpoint, nextComment);
				
				log.logBasic(jobinfo.toString(), "Finished jobentry ["+nextEntry.getName()+"] (result="+res.getResult()+")");
			}
		}
		
		// Perhaps we don't have next steps??
		// In this case, return the previous result.
		if (res==null)
		{
			res=prev_result;
		}

		return res;
	}
	
	//
	// Wait until all RunThreads have finished.
	// 
	public void waitUntilFinished()
	{
	}

	public int getErrors()
	{	
		int errors=0;
		return errors;
	}

	//
	// Handle logging at start
	public boolean beginProcessing()
		throws KettleJobException
	{
		currentDate = new Date();
		logDate     = new Date();
		startDate   = Const.MIN_DATE;
		endDate     = currentDate;
		
		DatabaseMeta logcon = jobinfo.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();
				Row lastr = ldb.getLastLogDate(jobinfo.logtable, jobinfo.getName(), true, "end");
				if (lastr!=null && lastr.size()>0)
				{
					Value last = lastr.getValue(0); // #0: last enddate
					if (last!=null && !last.isNull())
					{
						startDate = last.getDate();
					}
				}

				depDate = currentDate;
				
				ldb.writeLogRecord(jobinfo.logtable, false, 0, true, jobinfo.getName(), "start", 
				                   0L, 0L, 0L, 0L, 0L, 0L, 
				                   startDate, endDate, logDate, depDate,
								   log.getString()
								   );
				ldb.disconnect();
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleJobException("Unable to begin processing by logging start in logtable "+jobinfo.logtable, dbe);
			}
			finally
			{
				ldb.disconnect();
			}
		}
		return true;
	}
	
	//
	// Handle logging at end
	public boolean endProcessing(String status)
		throws KettleJobException
	{
		long read=0L, written=0L, updated=0L, errors=0L, input=0L, output=0L;
		
		logDate     = new Date();

		/*
		 * Sums errors, read, written, etc.
		 */		

		DatabaseMeta logcon = jobinfo.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();
				ldb.writeLogRecord(jobinfo.logtable, false, 0, true, jobinfo.getName(), status, 
				                   read,written,updated,input,output,errors, 
				                   startDate, endDate, logDate, depDate,
								   log.getString()
								   );
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleJobException("Unable to end processing by writing log record to table "+jobinfo.logtable, dbe);
			}
			finally
			{
				ldb.disconnect();
			}
		}
		return true;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	// Stop all activity!
	public void stopAll()
	{
		stopped=true;
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	/**
	 * @return Returns the stopped status of this Job...
	 */
	public boolean isStopped()
	{
		return stopped;
	}
	
	/**
	 * @return Returns the startDate.
	 */
	public Date getStartDate()
	{
		return startDate;
	}
	
	/**
	 * @return Returns the endDate.
	 */
	public Date getEndDate()
	{
		return endDate;
	}
	
	/**
	 * @return Returns the currentDate.
	 */
	public Date getCurrentDate()
	{
		return currentDate;
	}
	
	/**
	 * @return Returns the depDate.
	 */
	public Date getDepDate()
	{
		return depDate;
	}

	/**
	 * @return Returns the logDate.
	 */
	public Date getLogDate()
	{
		return logDate;
	}

	/**
	 * @return Returns the jobinfo.
	 */
	public JobMeta getJobinfo()
	{
		return jobinfo;
	}
	
	/**
	 * @return Returns the log.
	 */
	public LogWriter getLog()
	{
		return log;
	}
	
	/**
	 * @return Returns the rep.
	 */
	public Repository getRep()
	{
		return rep;
	}	
	
	/**
	 * @param jobEntryResults The jobEntryResults to set.
	 */
	public void setJobEntryResults(ArrayList jobEntryResults)
	{
		this.jobEntryResults = jobEntryResults;
	}
	
	/**
	 * @return Returns the jobEntryResults.
	 */
	public ArrayList getJobEntryResults()
	{
		return jobEntryResults;
	}
	
	public JobEntryResult getLastJobEntryResult()
	{
		if (jobEntryResults.size()>0)
		{
			return (JobEntryResult)jobEntryResults.get(jobEntryResults.size()-1);
		}
		return null;
	}
	
	public String toString()
	{
		return this.getClass().getName();
	}
	
}


