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

import be.ibridge.kettle.chef.JobTracker;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.KettleVariables;
import be.ibridge.kettle.core.LocalVariables;
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
import be.ibridge.kettle.job.entry.special.JobEntrySpecial;
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
	private JobMeta jobMeta;
	private Repository rep;
	
    /** The job that's launching this (sub-) job. This gives us access to the whole chain, including the parent variables, etc. */
    private Job parentJob;
    
	/**
	 * Keep a list of the job entries that were executed.
	 */
	private JobTracker jobTracker;
	
	private Date      startDate, endDate, currentDate, logDate, depDate;
	
	private boolean active, stopped;
    
    /**
     * The rows that were passed onto this job by a previous transformation.  
     * These rows are passed onto the first job entry in this job (on the result object)
     */
    private ArrayList sourceRows;
    
    
    private Thread parentThread;
    
    /**
     * The result of the job, after execution.
     */
    private Result result;
    private boolean initialized;
    
    public Job(LogWriter lw, String name, String file, String args[])
    {
        init(lw, name, file, args);
    }
    
	public void init(LogWriter lw, String name, String file, String args[])
	{
		this.log=lw;
		
        if (name!=null) setName(name+" ("+super.getName()+")");
        
		jobMeta = new JobMeta(log);
		jobMeta.setName(name);
		jobMeta.setFilename(file);
		jobMeta.setArguments(args);
		active=false;
		stopped=false;
        jobTracker = new JobTracker(jobMeta);
        parentThread = Thread.currentThread();  // It _is_ before you start to run the job.
        initialized=false;
	}

	public Job(LogWriter lw, StepLoader steploader, Repository rep, JobMeta ti)
	{
        open(lw, steploader, rep, ti);
        if (ti.getName()!=null) setName(ti.getName()+" ("+super.getName()+")");
	}
    
    // Empty constructor, for Class.newInstance()
    public Job()
    {
        parentThread = Thread.currentThread();  // It _is_ before you start to run the job.
    }
    
    public void open(LogWriter lw, StepLoader steploader, Repository rep, JobMeta ti)
    {
        this.log        = lw;
        this.rep        = rep;
        this.jobMeta    = ti;
        
        if (ti.getName()!=null) setName(ti.getName()+" ("+super.getName()+")");
        
        active=false;
        stopped=false;
        jobTracker = new JobTracker(jobMeta);
        parentThread = Thread.currentThread();  // It _is_ before you start to run the job.
    }
	
	public void open(Repository rep, String fname, String jobname, String dirname) throws KettleException
	{
		this.rep = rep;
		if (rep!=null)
		{
			jobMeta = new JobMeta(log, rep, jobname, rep.getDirectoryTree().findDirectory(dirname));
		}
		else
		{
			jobMeta = new JobMeta(log, fname, rep);
		}
        
        if (jobMeta.getName()!=null) setName(jobMeta.getName()+" ("+super.getName()+")");
	}
    
    
    public static final Job createJobWithNewClassLoader() throws KettleException
    {
        try
        {
            // Load the class.
            Class jobClass = Const.createNewClassLoader().loadClass(Job.class.getName()); 

            // create the class
            // Try to instantiate this one...
            Job job = (Job)jobClass.newInstance();
            
            // Done!
            return job;
        }   
        catch(Exception e)
        {
            String message = "Error allocating new Job : "+e.toString();
            LogWriter.getInstance().logError("Create Job in new ClassLoader", message);
            LogWriter.getInstance().logError("Create Job in new ClassLoader", Const.getStackTracker(e));
            throw new KettleException(message, e);
        }
    }
    

	public String getJobname()
	{
		if (jobMeta==null) return null;
		
		return jobMeta.getName();
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
            // Now that we're running, add the Kettle variables automatically...
            // Create a new variable name space as we want jobs to have their own set of variables.
            //
            LocalVariables.getInstance().createKettleVariables(getName(), parentThread.getName(), false);
            initialized = true;
            
            execute(); // Run the job
			endProcessing("end");
		}
		catch(KettleException je)
		{
			System.out.println("A serious error occurred!"+Const.CR+je.getMessage());
		}
	}

	public Result execute() throws KettleException
    {
        // Start the tracking...
        JobEntryResult jerStart = new JobEntryResult(null, "Start of job execution", "start", null);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerStart));

        active = true;

        // Where do we start?
        JobEntryCopy startpoint;
        beginProcessing();
        startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
        if (startpoint == null) { throw new KettleJobException("Couldn't find starting point in this job."); }
        JobEntrySpecial jes = (JobEntrySpecial) startpoint.getEntry();
        Result res = null;
        boolean isFirst = true;
        while (jes.isRepeat() || isFirst && !isStopped())
        {
            isFirst = false;
            res = execute(0, null, startpoint, null, "start");
        }
        // Save this result...
        JobEntryResult jerEnd = new JobEntryResult(res, "Job execution ended", "end", null);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerEnd));

        active = false;
        return res;
    }

	/**
	 * Execute called by JobEntryJob: don't clear the jobEntryResults...
	 * @param nr
	 * @param result
	 * @return Result of the job execution
	 * @throws KettleJobException
	 */
	public Result execute(int nr, Result result) throws KettleException
	{
        // Where do we start?
        JobEntryCopy startpoint;

        startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
        if (startpoint == null) 
        {
            throw new KettleJobException("Couldn't find starting point in this job.");
        }

		Result res =  execute(nr, result, startpoint, null, "start of job entry");

		return res;
	}
	
	private Result execute(int nr, Result prev_result, JobEntryCopy startpoint, JobEntryCopy previous, String reason) throws KettleException
	{
		Result res = null;
       
		if (stopped)
		{
			res=new Result(nr);
			res.stopped=true;
			return res;
		}
		
		log.logDetailed(toString(), "exec("+nr+", "+(prev_result!=null?prev_result.getNrErrors():0)+", "+(startpoint!=null?startpoint.toString():"null")+")");
		
		// What entry is next?
		JobEntryInterface jei = startpoint.getEntry();

        // Track the fact that we are going to launch the next job entry...
        JobEntryResult jerBefore = new JobEntryResult(null, "Job entry started", reason, startpoint);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerBefore));

        Result prevResult = prev_result;
        // If we don't have any result yet or if there are no result rows available, use the results from the parent job
        if (prev_result==null)
        {
            prevResult = new Result();
            prevResult.setRows(sourceRows);
        }
        else
        {
            if (prevResult.getRows()==null)
            {
                prevResult.setRows(sourceRows);
            }
        }

        // Execute this entry...
        Result result = jei.execute(prevResult, nr, rep, this);
		
		// Save this result as well...
        JobEntryResult jerAfter = new JobEntryResult(result, "Job entry ended", null, startpoint);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerAfter));
				
		// Try all next job entries.
		// Launch only those where the hopinfo indicates true or false
		int nrNext = jobMeta.findNrNextChefGraphEntries(startpoint);
		for (int i=0;i<nrNext && !isStopped();i++)
		{
			// The next entry is...
			JobEntryCopy nextEntry = jobMeta.findNextChefGraphEntry(startpoint, i);
			
			// See if we need to execute this...
			JobHopMeta hi = jobMeta.findJobHop(startpoint, nextEntry);

			// The next comment...
			String nextComment = null;
			if (hi.isUnconditional()) 
			{
				nextComment = "Followed unconditional link";
			}
			else
			{
				if (result.getResult())
                {
					nextComment = "Followed link after succes!";
                }
				else
                {
					nextComment = "Followed link after failure!";
                }
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
				log.logBasic(jobMeta.toString(), "Starting entry ["+nextEntry.getName()+"]");
                
                // Pass along the previous result, perhaps the next job can use it...
                // However, set the number of errors back to 0
                result.setNrErrors(0);
                
                // Now execute!
                try
                {
                    res = execute(nr+1, result, nextEntry, startpoint, nextComment);
                }
                catch(Throwable e)
                {
                    log.logError(toString(), Const.getStackTracker(e));
                    throw new KettleException("Unexpected error occurred while launching entry ["+nextEntry.toString()+"]", e);
                }
				
				log.logBasic(jobMeta.toString(), "Finished jobentry ["+nextEntry.getName()+"] (result="+res.getResult()+")");
			}
		}
		
		// Perhaps we don't have next steps??
		// In this case, return the previous result.
		if (res==null)
		{
			res=prevResult;
		}

		return res;
	}
	
	/**
	 Wait until this job has finished.
	*/
	public void waitUntilFinished(long maxMiliseconds)
	{
        long time = 0L;
        while (isAlive() && time<maxMiliseconds)
        {
            try { Thread.sleep(10); time+=10; } catch(InterruptedException e) {}
        }
	}

	public int getErrors()
	{	
		int errors=0;
		return errors;
	}

	//
	// Handle logging at start
	public boolean beginProcessing() throws KettleJobException
	{
		currentDate = new Date();
		logDate     = new Date();
		startDate   = Const.MIN_DATE;
		endDate     = currentDate;
		
		DatabaseMeta logcon = jobMeta.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();
				Row lastr = ldb.getLastLogDate(jobMeta.getLogTable(), jobMeta.getName(), true, "end");
				if (lastr!=null && lastr.size()>0)
				{
					Value last = lastr.getValue(0); // #0: last enddate
					if (last!=null && !last.isNull())
					{
						startDate = last.getDate();
					}
				}

				depDate = currentDate;
                
                // See if we have to add a batch id...
                Value id_batch = new Value("ID_JOB", (long)1);
                if (jobMeta.isBatchIdUsed())
                {
                    ldb.getNextValue(null, jobMeta.getLogTable(), id_batch);
                    jobMeta.setBatchId( id_batch.getInteger() );
                }
				
				ldb.writeLogRecord(jobMeta.getLogTable(), jobMeta.isBatchIdUsed(), jobMeta.getBatchId(), true, jobMeta.getName(), "start", 
				                   0L, 0L, 0L, 0L, 0L, 0L, 
				                   startDate, endDate, logDate, depDate, currentDate,
								   log.getString()
								   );
				ldb.disconnect();
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleJobException("Unable to begin processing by logging start in logtable "+jobMeta.getLogTable(), dbe);
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
	public boolean endProcessing(String status) throws KettleJobException
	{
		long read=0L, written=0L, updated=0L, errors=0L, input=0L, output=0L;
		
		logDate     = new Date();

		/*
		 * Sums errors, read, written, etc.
		 */		

		DatabaseMeta logcon = jobMeta.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			try
			{
				ldb.connect();
				ldb.writeLogRecord(jobMeta.getLogTable(), jobMeta.isBatchIdUsed(), jobMeta.getBatchId(), true, jobMeta.getName(), status, 
				                   read,written,updated,input,output,errors, 
				                   startDate, endDate, logDate, depDate, currentDate,
								   log.getString()
								   );
			}
			catch(KettleDatabaseException dbe)
			{
				throw new KettleJobException("Unable to end processing by writing log record to table "+jobMeta.getLogTable(), dbe);
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
	public JobMeta getJobMeta()
	{
		return jobMeta;
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
		
	public String toString()
	{
        return super.toString();
        /*
        if (jobMeta!=null)
        {
            return Const.NVL(jobMeta.getName(), Const.NVL(jobMeta.getFilename(), super.getName()) );
        }
		return this.getClass().getName();
        */
	}
    
    public Thread getThread()
    {
        return (Thread)this;
    }
    
    /**
     * @return Returns the jobTracker.
     */
    public JobTracker getJobTracker()
    {
        return jobTracker;
    }

    /**
     * @param jobTracker The jobTracker to set.
     */
    public void setJobTracker(JobTracker jobTracker)
    {
        this.jobTracker = jobTracker;
    }

    public void setSourceRows(ArrayList sourceRows)
    {
        this.sourceRows = sourceRows;
    }
	
    public ArrayList getSourceRows()
    {
        return sourceRows;
    }

    /**
     * @return Returns the parentJob.
     */
    public Job getParentJob()
    {
        return parentJob;
    }

    /**
     * @param parentJob The parentJob to set.
     */
    public void setParentJob(Job parentJob)
    {
        this.parentJob = parentJob;
    }

    public Result getResult()
    {
        return result;
    }
    
    public void setResult(Result result)
    {
        this.result = result;
    }

    public KettleVariables getKettleVariables()
    {
       return LocalVariables.getKettleVariables(getName());
    }

    /**
     * @return Returns the initialized.
     */
    public boolean isInitialized()
    {
        return initialized;
    }
}


