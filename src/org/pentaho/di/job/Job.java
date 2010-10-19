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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.www.AddExportServlet;
import org.pentaho.di.www.AddJobServlet;
import org.pentaho.di.www.SocketRepository;
import org.pentaho.di.www.StartJobServlet;
import org.pentaho.di.www.WebResult;


/**
 * This class executes a JobInfo object.
 * 
 * @author Matt Casters
 * @since  07-apr-2003
 * 
 */
public class Job extends Thread implements VariableSpace, NamedParams, HasLogChannelInterface, LoggingObjectInterface
{
	private static Class<?> PKG = Job.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String	CONFIGURATION_IN_EXPORT_FILENAME	= "__job_execution_configuration__.xml";
	
	private LogChannelInterface log;
	private LogLevel logLevel = DefaultLogLevel.getLogLevel();
	private String containerObjectId;
	private JobMeta jobMeta;
	private Repository rep;
    private AtomicInteger errors;

	private VariableSpace variables = new Variables();
	
    /** The job that's launching this (sub-) job. This gives us access to the whole chain, including the parent variables, etc. */
    private Job parentJob;
    
    /** The parent logging interface to reference */
    private LoggingObjectInterface parentLoggingObject;
    
	/**
	 * Keep a list of the job entries that were executed. org.pentaho.di.core.logging.CentralLogStore.getInstance()
	 */
	private JobTracker jobTracker;
	
	/** A flat list of results in THIS job, in the order of execution of job entries */
	private List<JobEntryResult> jobEntryResults;
	
	private Date      startDate, endDate, currentDate, logDate, depDate;
	
	private AtomicBoolean active;

	private AtomicBoolean stopped;
    
    private long    batchId;
    
    /** This is the batch ID that is passed from job to job to transformation, if nothing is passed, it's the job's batch id */
    private long    passedBatchId;
    
    /**
     * The rows that were passed onto this job by a previous transformation.  
     * These rows are passed onto the first job entry in this job (on the result object)
     */
    private List<RowMetaAndData> sourceRows;
    
    /**
     * The result of the job, after execution.
     */
    private Result result;
    private AtomicBoolean initialized;
    
    private boolean interactive;
    
    private List<JobListener> jobListeners;
    
    private List<JobEntryListener> jobEntryListeners;
    
	private Map<JobEntryCopy, JobEntryTrans> activeJobEntryTransformations;
	private Map<JobEntryCopy, JobEntryJob>   activeJobEntryJobs;

    /**
     * Parameters of the job.
     */
    private NamedParams namedParams = new NamedParamsDefault();
    
    private AtomicBoolean finished;
	private SocketRepository	socketRepository;

    public Job(String name, String file, String args[]) {
        this();
        jobMeta = new JobMeta();
        if (name != null)
            setName(name + " (" + super.getName() + ")");
        jobMeta.setName(name);
        jobMeta.setFilename(file);
        jobMeta.setArguments(args);
    
        init();
        this.log = new LogChannel(this);
    }
    
  public void init() {
    jobListeners = new ArrayList<JobListener>();
    jobEntryListeners = new ArrayList<JobEntryListener>();

    activeJobEntryTransformations = new HashMap<JobEntryCopy, JobEntryTrans>();
    activeJobEntryJobs = new HashMap<JobEntryCopy, JobEntryJob>();

    active = new AtomicBoolean(false);
    stopped = new AtomicBoolean(false);
    jobTracker = new JobTracker(jobMeta);
    jobEntryResults = new ArrayList<JobEntryResult>();
    initialized = new AtomicBoolean(false);
    finished = new AtomicBoolean(false);
    errors = new AtomicInteger(0);
    batchId = -1;
    passedBatchId = -1;

    result = null;
  }

	public Job(Repository repository, JobMeta jobMeta)
	{
		this(repository, jobMeta, null);
	}
	
	public Job(Repository repository, JobMeta jobMeta, LoggingObjectInterface parentLogging)
	{
        this.rep        = repository;
        this.jobMeta    = jobMeta;
        this.parentLoggingObject = parentLogging;
        
        init();
        
        jobTracker = new JobTracker(jobMeta);

        this.log = new LogChannel(this, parentLogging);
        this.logLevel = log.getLogLevel();
        this.containerObjectId = log.getContainerObjectId();
	}

    /**
     *  Empty constructor, for Class.newInstance()
     */
    public Job()
    {
      init();
    	this.log = new LogChannel(this);
    	this.logLevel = log.getLogLevel();
    }
    
    @Override
    public String toString() {
    	if (jobMeta==null || Const.isEmpty(jobMeta.getName())) {
    		return getName();
    	} else {
    		return jobMeta.getName();
    	}
    }
    
    public static final Job createJobWithNewClassLoader() throws KettleException
    {
        try
        {
            // Load the class.
            Class<?> jobClass = Const.createNewClassLoader().loadClass(Job.class.getName()); 

            // create the class
            // Try to instantiate this one...
            Job job = (Job)jobClass.newInstance();
            
            // Done!
            return job;
        }   
        catch(Exception e)
        {
            String message = BaseMessages.getString(PKG, "Job.Log.ErrorAllocatingNewJob", e.toString());
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
			stopped=new AtomicBoolean(false);
			finished=new AtomicBoolean(false);
            initialized = new AtomicBoolean(true);
    
            // Create a new variable name space as we want jobs to have their own set of variables.
            // initialize from parentJob or null
            //
            variables.initializeVariablesFrom(parentJob);
            setInternalKettleVariables(variables);
            copyParametersFrom(jobMeta);
            activateParameters();
            
            // Run the job
            //
            result = execute(); 
		}
		catch(Throwable je)
		{
			log.logError(BaseMessages.getString(PKG, "Job.Log.ErrorExecJob", je.getMessage()));
            log.logError(Const.getStackTracker(je));
            //
            // we don't have result object because execute() threw a curve-ball.
            // So we create a new error object.
            //
            result = new Result();
            result.setNrErrors(1L);
            result.setResult(false);
            addErrors(1);  // This can be before actual execution
            active.set(false);
            finished.set(true);
            stopped.set(false);
		}
		finally
		{
			try {
				fireJobListeners();
			} catch(KettleException e) {
				result.setNrErrors(1);
				result.setResult(false);
				log.logError(BaseMessages.getString(PKG, "Job.Log.ErrorExecJob", e.getMessage()), e);
			}
		}
	}

	
	/**
	 * Execute a job without previous results.  This is a job entry point (not recursive)<br>
	 * <br>
	 * @return the result of the execution
	 * 
	 * @throws KettleException
	 */
	private Result execute() throws KettleException
    {
		finished.set(false);
		stopped.set(false);

        log.logMinimal(BaseMessages.getString(PKG, "Job.Comment.JobStarted"));

        // Start the tracking...
        JobEntryResult jerStart = new JobEntryResult(null, null, BaseMessages.getString(PKG, "Job.Comment.JobStarted"), BaseMessages.getString(PKG, "Job.Reason.Started"), null, 0, null);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerStart));

        active.set(true);

        // Where do we start?
        JobEntryCopy startpoint;
        beginProcessing();
        startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
        if (startpoint == null) { throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.CounldNotFindStartingPoint")); }
        JobEntrySpecial jes = (JobEntrySpecial) startpoint.getEntry();
        Result res = null;
        boolean isFirst = true;
        while ( (jes.isRepeat() || isFirst) && !isStopped())
        {
            isFirst = false;
            res = execute(0, null, startpoint, null, BaseMessages.getString(PKG, "Job.Reason.Started"));
        }
        // Save this result...
        JobEntryResult jerEnd = new JobEntryResult(res, jes.getLogChannelId(), BaseMessages.getString(PKG, "Job.Comment.JobFinished"), BaseMessages.getString(PKG, "Job.Reason.Finished"), null, 0, null);
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerEnd));
        log.logMinimal(BaseMessages.getString(PKG, "Job.Comment.JobFinished"));
        
        active.set(false);
        finished.set(true);
        
		return res;
    }

	/**
	 * Execute a job with previous results passed in.<br>
	 * <br>
	 * Execute called by JobEntryJob: don't clear the jobEntryResults.
	 * @param nr The job entry number
	 * @param result the result of the previous execution
	 * @return Result of the job execution
	 * @throws KettleJobException
	 */
	public Result execute(int nr, Result result) throws KettleException
	{
		finished.set(false);
        active.set(true);
        initialized.set(true);

        // Where do we start?
        JobEntryCopy startpoint;

        // Perhaps there is already a list of input rows available?
        if (getSourceRows()!=null)
        {
            result.setRows(getSourceRows());
        }
        
        startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
        if (startpoint == null) 
        {
            throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.CounldNotFindStartingPoint"));
        }


		Result res =  execute(nr, result, startpoint, null, BaseMessages.getString(PKG, "Job.Reason.StartOfJobentry"));
		
        active.set(false);
        
		return res;
	}
	
	/**
	 * Sets the finished flag.<b>
	 * Then launch all the job listeners and call the jobFinished method for each.<br>
	 * 
	 * @see JobListener#jobFinished(Job)
	 */
	private void fireJobListeners() throws KettleException {
		for (JobListener jobListener : jobListeners) {
			jobListener.jobFinished(this);
		}
	}
	
	/**
	 * Execute a job entry recursively and move to the next job entry automatically.<br>
	 * Uses a back-tracking algorithm.<br>
	 * 
	 * @param nr
	 * @param prev_result
	 * @param jobEntryCopy
	 * @param previous
	 * @param reason
	 * @return
	 * @throws KettleException
	 */
	private Result execute(final int nr, Result prev_result, final JobEntryCopy jobEntryCopy, JobEntryCopy previous, String reason) throws KettleException
	{
		Result res = null;
       
		if (stopped.get())
		{
			res=new Result(nr);
			res.stopped=true;
			return res;
		}
		
		if(log.isDetailed()) log.logDetailed("exec("+nr+", "+(prev_result!=null?prev_result.getNrErrors():0)+", "+(jobEntryCopy!=null?jobEntryCopy.toString():"null")+")");
		
		// What entry is next?
		JobEntryInterface jobEntryInterface = jobEntryCopy.getEntry();
		jobEntryInterface.getLogChannel().setLogLevel(logLevel);

        // Track the fact that we are going to launch the next job entry...
        JobEntryResult jerBefore = new JobEntryResult(null, null, BaseMessages.getString(PKG, "Job.Comment.JobStarted"), reason, jobEntryCopy.getName(), jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerBefore));

        Result prevResult = null;
        if ( prev_result != null )
        {
            prevResult = (Result)prev_result.clone();
        }
        else
        {
            prevResult = new Result();
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(jobEntryInterface.getClass().getClassLoader());
        // Execute this entry...
        JobEntryInterface cloneJei = (JobEntryInterface)jobEntryInterface.clone();
        ((VariableSpace)cloneJei).copyVariablesFrom(this);
        cloneJei.setRepository(rep);
        cloneJei.setParentJob(this);
        final long start = System.currentTimeMillis();
                
        cloneJei.getLogChannel().logDetailed("Starting job entry");
        for (JobEntryListener jobEntryListener : jobEntryListeners) {
        	jobEntryListener.beforeExecution(this, jobEntryCopy, cloneJei);
        }
        if (interactive) {
			if (jobEntryCopy.isTransformation()) {
				getActiveJobEntryTransformations().put(jobEntryCopy, (JobEntryTrans)cloneJei);
			}
			if (jobEntryCopy.isJob()) {
				getActiveJobEntryJobs().put(jobEntryCopy, (JobEntryJob)cloneJei);
			}
        }
        final Result result = cloneJei.execute(prevResult, nr);
        final long end = System.currentTimeMillis();
        if (interactive) {
			if (jobEntryCopy.isTransformation()) {
				getActiveJobEntryTransformations().remove(jobEntryCopy);
			}
			if (jobEntryCopy.isJob()) {
				getActiveJobEntryJobs().remove(jobEntryCopy);
			}
        }

        if (cloneJei instanceof JobEntryTrans)
        {
        	String throughput = result.getReadWriteThroughput((int)((end-start) / 1000));
        	if (throughput != null) {
        		log.logMinimal(throughput);
        	}
        }
        for (JobEntryListener jobEntryListener : jobEntryListeners) {
        	jobEntryListener.afterExecution(this, jobEntryCopy, cloneJei, result);
        }

        Thread.currentThread().setContextClassLoader(cl);
		addErrors((int)result.getNrErrors());
		
		// Also capture the logging text after the execution...
		//
		Log4jBufferAppender appender = CentralLogStore.getAppender();
		StringBuffer logTextBuffer = appender.getBuffer(cloneJei.getLogChannel().getLogChannelId(), false);
		result.setLogText( logTextBuffer.toString() );
		
        // Save this result as well...
		//
        JobEntryResult jerAfter = new JobEntryResult(result, cloneJei.getLogChannel().getLogChannelId(), BaseMessages.getString(PKG, "Job.Comment.JobFinished"), null, jobEntryCopy.getName(), jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
        jobTracker.addJobTracker(new JobTracker(jobMeta, jerAfter));
        jobEntryResults.add(jerAfter);
			
		// Try all next job entries.
        //
        // Keep track of all the threads we fired in case of parallel execution...
        // Keep track of the results of these executions too.
        //
        final List<Thread> threads = new ArrayList<Thread>();
        final List<Result> threadResults = new ArrayList<Result>(); 
        final List<KettleException> threadExceptions = new ArrayList<KettleException>(); 
        final List<JobEntryCopy> threadEntries= new ArrayList<JobEntryCopy>(); 
        
		// Launch only those where the hop indicates true or false
        //
		int nrNext = jobMeta.findNrNextJobEntries(jobEntryCopy);
		for (int i=0;i<nrNext && !isStopped();i++)
		{
			// The next entry is...
			final JobEntryCopy nextEntry = jobMeta.findNextJobEntry(jobEntryCopy, i);
			
			// See if we need to execute this...
			final JobHopMeta hi = jobMeta.findJobHop(jobEntryCopy, nextEntry);

			// The next comment...
			final String nextComment;
			if (hi.isUnconditional()) 
			{
				nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedUnconditional");
			}
			else
			{
				if (result.getResult())
                {
					nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedSuccess");
                }
				else
                {
					nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedFailure");
                }
			}

			// 
			// If the link is unconditional, execute the next job entry (entries).
			// If the start point was an evaluation and the link color is correct: green or red, execute the next job entry...
			//
			if (  hi.isUnconditional() || ( jobEntryCopy.evaluates() && ( ! ( hi.getEvaluation() ^ result.getResult() ) ) ) ) 
			{				
				// Start this next step!
				if(log.isBasic()) log.logBasic(BaseMessages.getString(PKG, "Job.Log.StartingEntry",nextEntry.getName()));
                
                // Pass along the previous result, perhaps the next job can use it...
                // However, set the number of errors back to 0 (if it should be reset)
				// When an evaluation is executed the errors e.g. should not be reset.
				if ( nextEntry.resetErrorsBeforeExecution() )
				{
                    result.setNrErrors(0);
				}
                
                // Now execute!
				// 
            	// if (we launch in parallel, fire the execution off in a new thread...
            	//
            	if (jobEntryCopy.isLaunchingInParallel())
            	{
            		threadEntries.add(nextEntry);
            		
            		Runnable runnable = new Runnable() {
            			public void run() {
            				try {
            					Result threadResult = execute(nr+1, result, nextEntry, jobEntryCopy, nextComment);
            					threadResults.add(threadResult);
            				}
            				catch(Throwable e)
                            {
                            	log.logError(Const.getStackTracker(e));
                            	threadExceptions.add(new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedError",nextEntry.toString()), e));
                            	Result threadResult = new Result();
                            	threadResult.setResult(false);
                            	threadResult.setNrErrors(1L);
                            	threadResults.add(threadResult);
                            }
            			}
            		};
            		Thread thread = new Thread(runnable);
            		threads.add(thread);
            		thread.start();
            		if(log.isBasic()) log.logBasic(BaseMessages.getString(PKG, "Job.Log.LaunchedJobEntryInParallel",nextEntry.getName()));
            	}
            	else
            	{
                    try
                    {
	            		// Same as before: blocks until it's done
	            		//
	            		res = execute(nr+1, result, nextEntry, jobEntryCopy, nextComment);
                    }
                    catch(Throwable e)
                    {
                    	log.logError(Const.getStackTracker(e));
                    	throw new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedError",nextEntry.toString()), e);
                    }
                    if(log.isBasic()) log.logBasic(BaseMessages.getString(PKG, "Job.Log.FinishedJobEntry",nextEntry.getName(),res.getResult()+""));
            	}
			}
		}
		
		// OK, if we run in parallel, we need to wait for all the job entries to finish...
		//
		if (jobEntryCopy.isLaunchingInParallel())
		{
			for (int i=0;i<threads.size();i++)
			{
				Thread thread = threads.get(i);
				JobEntryCopy nextEntry = threadEntries.get(i);
				
				try 
				{
					thread.join();
				} 
				catch (InterruptedException e) 
				{
	                log.logError(jobMeta.toString(), BaseMessages.getString(PKG, "Job.Log.UnexpectedErrorWhileWaitingForJobEntry",nextEntry.getName()));
	                threadExceptions.add(new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedErrorWhileWaitingForJobEntry",nextEntry.getName()), e));
				}
			}
			// if(log.isBasic()) log.logBasic(BaseMessages.getString(PKG, "Job.Log.FinishedJobEntry",startpoint.getName(),res.getResult()+""));
		}
		
		// Perhaps we don't have next steps??
		// In this case, return the previous result.
		if (res==null)
		{
			res=prevResult;
		}

		// See if there where any errors in the parallel execution
		//
		if (threadExceptions.size()>0) 
		{
			res.setResult(false);
			res.setNrErrors(threadExceptions.size());
			
			for (KettleException e : threadExceptions) 
			{
				log.logError(jobMeta.toString(), e.getMessage(), e);
			}
			
			// Now throw the first Exception for good measure...
			//
			throw threadExceptions.get(0);
		}
		
		// In parallel execution, we aggregate all the results, simply add them to the previous result...
		//
		for (Result threadResult : threadResults)
		{
			res.add(threadResult);
		}
		
		// If there have been errors, logically, we need to set the result to "false"...
		//
		if (res.getNrErrors()>0) {
			res.setResult(false);
		}
		
		return res;
	}
	
    /**
     Wait until this job has finished.
    */
    public void waitUntilFinished()
    {
        waitUntilFinished(-1L);
    }
    
	/**
	 Wait until this job has finished.
     @param maxMiliseconds the maximum number of ms to wait
	*/
	public void waitUntilFinished(long maxMiliseconds)
	{
		try
		{
			while (isAlive())
			{
				Thread.sleep(0,1);
			}
		}
		catch(InterruptedException e) 
		{
			
		}
		/*
        long time = 0L;
        while (isAlive() && (time<maxMiliseconds || maxMiliseconds<0))
        {
            try { Thread.sleep(1); time+=1; } catch(InterruptedException e) {}
        }
        */
	}

	/**
	 * Get the number of errors that happened in the job.
	 * 
	 * @return nr of error that have occurred during execution. 
	 *         During execution of a job the number can change.
	 */
	public int getErrors()
	{	
		return errors.get();
	}

	/**
	 * Set the number of occured errors to 0.
	 */
	public void resetErrors()
	{
	    errors.set(0);
	}

	/**
	 * Add a number of errors to the total number of erros that occured
	 * during execution.
	 *
	 * @param nrToAdd nr of errors to add.
	 */
	public void addErrors(int nrToAdd)
	{
	    if ( nrToAdd > 0 )
	    {
	        errors.addAndGet(nrToAdd);
	    }
	}

	/**
	 * Handle logging at start
	 * 
	 * @return true if it went OK.
	 * 
	 * @throws KettleException
	 */
  public synchronized boolean beginProcessing() throws KettleException {
    currentDate = new Date();
    logDate = new Date();
    startDate = Const.MIN_DATE;
    endDate = currentDate;

    resetErrors();

    final JobLogTable jobLogTable = jobMeta.getJobLogTable();
    int intervalInSeconds = Const.toInt(environmentSubstitute(jobLogTable.getLogInterval()), -1);

    if (jobLogTable.isDefined()) {

      DatabaseMeta logcon = jobMeta.getJobLogTable().getDatabaseMeta();
      String schemaName = environmentSubstitute(jobMeta.getJobLogTable().getSchemaName());
      String tableName = environmentSubstitute(jobMeta.getJobLogTable().getTableName());
      Database ldb = new Database(this, logcon);
      ldb.shareVariablesWith(this);
      try {
        boolean lockedTable = false;
        ldb.connect();

        // Enable transactions to make table locking possible
        //
        ldb.setCommit(10);

        // See if we have to add a batch id...
        Long id_batch = new Long(1);
        if (jobMeta.getJobLogTable().isBatchIdUsed()) {
          // Make sure we lock that table to avoid concurrency issues
          //
          ldb.lockTables(new String[] { tableName, });
          lockedTable = true;

          // Now insert value -1 to create a real write lock blocking the other
          // requests.. FCFS
          //
          String sql = "INSERT INTO " + logcon.quoteField(tableName) + "(" + logcon.quoteField(jobLogTable.getKeyField().getFieldName()) + ") values (-1)";
          ldb.execStatement(sql);

          // Now this next lookup will stall on the other connections
          //
          id_batch = ldb.getNextValue(null, schemaName, tableName, jobLogTable.getKeyField().getFieldName());

          setBatchId(id_batch.longValue());
          if (getPassedBatchId() <= 0) {
            setPassedBatchId(id_batch.longValue());
          }
        }

        Object[] lastr = ldb.getLastLogDate(tableName, jobMeta.getName(), true, LogStatus.END); // $NON-NLS-1$
        if (!Const.isEmpty(lastr)) {
          Date last;
          try {
            last = ldb.getReturnRowMeta().getDate(lastr, 0);
          } catch (KettleValueException e) {
            throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.ConversionError", "" + tableName), e);
          }
          if (last != null) {
            startDate = last;
          }
        }

        depDate = currentDate;

        ldb.writeLogRecord(jobMeta.getJobLogTable(), LogStatus.START, this, null);

        if (lockedTable) {

          // Remove the -1 record again...
          //
          String sql = "DELETE FROM " + logcon.quoteField(tableName) + " WHERE " + logcon.quoteField(jobLogTable.getKeyField().getFieldName()) + "= -1";
          ldb.execStatement(sql);

          ldb.unlockTables(new String[] { tableName, });
        }
        ldb.disconnect();

        // If we need to do periodic logging, make sure to install a timer for
        // this...
        //
        if (intervalInSeconds > 0) {
          final Timer timer = new Timer(getName() + " - interval logging timer");
          TimerTask timerTask = new TimerTask() {
            public void run() {
              try {
                endProcessing();
              } catch (Exception e) {
                log.logError(BaseMessages.getString(PKG, "Job.Exception.UnableToPerformIntervalLogging"), e);
                // Also stop the show...
                //

                errors.incrementAndGet();
                stopAll();
              }
            }
          };
          timer.schedule(timerTask, intervalInSeconds * 1000, intervalInSeconds * 1000);

          addJobListener(new JobListener() {
            public void jobFinished(Job job) {
              timer.cancel();
            }
          });
        }

        // Add a listener at the end of the job to take of writing the final job
        // log record...
        //
        addJobListener(new JobListener() {
          public void jobFinished(Job job) {
            try {
              endProcessing();
            } catch (Exception e) {
              log.logError(BaseMessages.getString(PKG, "Job.Exception.UnableToWriteToLoggingTable", jobLogTable.toString()), e);
            }
          }
        });

      } catch (KettleDatabaseException dbe) {
        addErrors(1); // This is even before actual execution
        throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.UnableToProcessLoggingStart", "" + tableName), dbe);
      } finally {
        ldb.disconnect();
      }
    }
    
    // If we need to write out the job entry logging information, do so at the end of the job:
    //
    JobEntryLogTable jobEntryLogTable = jobMeta.getJobEntryLogTable();
    if (jobEntryLogTable.isDefined()) {
        addJobListener(new JobListener() {
            public void jobFinished(Job job) throws KettleException {
                try {
                    writeJobEntryLogInformation();
                } catch(KettleException e) {
                    throw new KettleException(BaseMessages.getString(PKG, "Job.Exception.UnableToPerformJobEntryLoggingAtJobEnd"), e);
                }
            }
        });
    }


    // If we need to write the log channel hierarchy and lineage information,
    // add a listener for that too...
    //
    ChannelLogTable channelLogTable = jobMeta.getChannelLogTable();
    if (channelLogTable.isDefined()) {
      addJobListener(new JobListener() {

        public void jobFinished(Job job) throws KettleException {
          try {
            writeLogChannelInformation();
          } catch (KettleException e) {
            throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformLoggingAtTransEnd"), e);
          }
        }
      });
    }

    return true;
  }
	
	//
	// Handle logging at end
	private boolean endProcessing() throws KettleJobException
	{
		LogStatus status;
		if (!isActive()) {
			if (isStopped()) {
				status = LogStatus.STOP;
			} else {
				status = LogStatus.END;
			}
		} else {
			status = LogStatus.RUNNING;
		}
		try
		{
	        if (errors.get()==0 && result!=null && !result.getResult()) {
	        	errors.incrementAndGet();
	        }
			
			logDate     = new Date();
	
			/*
			 * Sums errors, read, written, etc.
			 */		
			
			JobLogTable jobLogTable = jobMeta.getJobLogTable();
			if (jobLogTable.isDefined()) {

				String tableName = jobMeta.getJobLogTable().getTableName();
				DatabaseMeta logcon = jobMeta.getJobLogTable().getDatabaseMeta();

				Database ldb = new Database(this, logcon);
				ldb.shareVariablesWith(this);
				try
				{
					ldb.connect();
					ldb.writeLogRecord(jobMeta.getJobLogTable(), status, this, null);
				}
				catch(KettleDatabaseException dbe)
				{
					addErrors(1);
					throw new KettleJobException("Unable to end processing by writing log record to table "+tableName, dbe);
				}
				finally
				{
					ldb.disconnect();
				}
			}
	        
			return true;
		}
		catch(Exception e)
		{
			throw new KettleJobException(e); // In case something else goes wrong.
		}
	}
	
	protected void writeLogChannelInformation() throws KettleException {
		Database db = null;
		ChannelLogTable channelLogTable = jobMeta.getChannelLogTable();
		try {
			db = new Database(this, channelLogTable.getDatabaseMeta());
			db.shareVariablesWith(this);
			db.connect();
			
			List<LoggingHierarchy> loggingHierarchyList = getLoggingHierarchy();
			for (LoggingHierarchy loggingHierarchy : loggingHierarchyList) {
				db.writeLogRecord(channelLogTable, LogStatus.START, loggingHierarchy, null);
			}
			
			// Also time-out the log records in here...
			//
			db.cleanupLogRecords(channelLogTable);

		} catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToWriteLogChannelInformationToLogTable"), e);
		} finally {
			db.disconnect();
		}
	}

    protected void writeJobEntryLogInformation() throws KettleException {
      Database db = null;
      JobEntryLogTable jobEntryLogTable = jobMeta.getJobEntryLogTable();
      try {
        db = new Database(this, jobEntryLogTable.getDatabaseMeta());
        db.shareVariablesWith(this);
        db.connect();
        
        for (JobEntryCopy copy : jobMeta.getJobCopies()) {
          db.writeLogRecord(jobEntryLogTable, LogStatus.END, copy, this);
        }
  
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(PKG, "Job.Exception.UnableToJobEntryInformationToLogTable"), e);
      } finally {
        db.disconnect();
      }
    }
  	
	public boolean isActive()
	{
		return active.get();
	}
	
	// Stop all activity!
	public void stopAll()
	{
		stopped.set(true);
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped.set(stopped);
	}
	
	/**
	 * @return Returns the stopped status of this Job...
	 */
	public boolean isStopped()
	{
		return stopped.get();
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
	 * @return Returns the rep.
	 */
	public Repository getRep()
	{
		return rep;
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

    public void setSourceRows(List<RowMetaAndData> sourceRows)
    {
        this.sourceRows = sourceRows;
    }
	
    public List<RowMetaAndData> getSourceRows()
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
      this.logLevel = parentJob.getLogLevel();
      this.log.setLogLevel(logLevel);
      this.containerObjectId = log.getContainerObjectId();
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

    /**
     * @return Returns the initialized.
     */
    public boolean isInitialized()
    {
        return initialized.get();
    }

    /**
     * @return Returns the batchId.
     */
    public long getBatchId()
    {
        return batchId;
    }

    /**
     * @param batchId The batchId to set.
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }


    /**
     * @return the jobBatchId
     */
    public long getPassedBatchId()
    {
        return passedBatchId;
    }

    /**
     * @param jobBatchId the jobBatchId to set
     */
    public void setPassedBatchId(long jobBatchId)
    {
        this.passedBatchId = jobBatchId;
    }
    
    public void setInternalKettleVariables(VariableSpace var)
    {
        if (jobMeta != null && jobMeta.getFilename() !=null) // we have a finename that's defined.
        {
            try
            {
                FileObject fileObject = KettleVFS.getFileObject(jobMeta.getFilename(), this);
                FileName fileName = fileObject.getName();
                
                // The filename of the transformation
                var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName());

                // The directory of the transformation
                FileName fileDir = fileName.getParent();
                var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI());
            }
            catch(Exception e)
            {
                var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "");
                var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "");
            }
        }
        else
        {
            var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, ""); //$NON-NLS-1$
            var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, ""); //$NON-NLS-1$
        }

        // The name of the job
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_NAME, Const.NVL(jobMeta.getName(), "")); //$NON-NLS-1$

        // The name of the directory in the repository
        var.setVariable(Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, jobMeta.getRepositoryDirectory() != null ? jobMeta.getRepositoryDirectory().getPath() : ""); //$NON-NLS-1$
        
        // Undefine the transformation specific variables
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null);
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null);
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, null);
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, null);
    }    
    
	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}

	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}
	
	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}

	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	public boolean getBooleanValueOfVariable(String variableName, boolean defaultValue) {
		if (!Const.isEmpty(variableName))
		{
			String value = environmentSubstitute(variableName);
			if (!Const.isEmpty(value))
			{
				return ValueMeta.convertStringToBoolean(value);
			}
		}
		return defaultValue;
	}

	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;		
	}

	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}	
	
    public String getStatus()
    {
        String message;
        
        if (!initialized.get())
        {
            message = Trans.STRING_WAITING;
        }
        else
        {
        	if (active.get()) 
        	{
        		if (stopped.get()) 
        		{
        			message = Trans.STRING_HALTING;
        		}
        		else
        		{
        			message = Trans.STRING_RUNNING;
        		}
        	}
        	else
        	{
        		if (stopped.get()) 
        		{
        			message = Trans.STRING_STOPPED;
        		}
        		else
        		{
	        		message = Trans.STRING_FINISHED;
        		}
                if (result!=null && result.getNrErrors()>0)
                {
                	message+=" (with errors)";
                }
            }
        }
        
        return message;
    }
    
	public static String sendToSlaveServer(JobMeta jobMeta, JobExecutionConfiguration executionConfiguration, Repository repository) throws KettleException
	{
		String carteObjectId;
		SlaveServer slaveServer = executionConfiguration.getRemoteServer();

		if (slaveServer == null)
			throw new KettleException(BaseMessages.getString(PKG, "Job.Log.NoSlaveServerSpecified"));
		if (Const.isEmpty(jobMeta.getName()))
			throw new KettleException(BaseMessages.getString(PKG, "Job.Log.UniqueJobName"));

		try
		{
			// Inject certain internal variables to make it more intuitive. 
			// 
			for (String var : Const.INTERNAL_TRANS_VARIABLES) executionConfiguration.getVariables().put(var, jobMeta.getVariable(var));
			for (String var : Const.INTERNAL_JOB_VARIABLES) executionConfiguration.getVariables().put(var, jobMeta.getVariable(var));

			if (executionConfiguration.isPassingExport()) {
				// First export the job... slaveServer.getVariable("MASTER_HOST")
				//
				FileObject tempFile = KettleVFS.createTempFile("jobExport", ".zip", System.getProperty("java.io.tmpdir"), jobMeta);
				
				TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface(tempFile.getName().toString(), jobMeta, jobMeta, repository, executionConfiguration.getXML(), CONFIGURATION_IN_EXPORT_FILENAME);
				
				// Send the zip file over to the slave server...
				//
				String result = slaveServer.sendExport(topLevelResource.getArchiveName(), AddExportServlet.TYPE_JOB, topLevelResource.getBaseResourceName());
				WebResult webResult = WebResult.fromXMLString(result);
				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
				{
					throw new KettleException("There was an error passing the exported job to the remote server: " + Const.CR+ webResult.getMessage());
				}
				carteObjectId = webResult.getId();
			} else {
				String xml = new JobConfiguration(jobMeta, executionConfiguration).getXML();
				
				String reply = slaveServer.sendXML(xml, AddJobServlet.CONTEXT_PATH + "/?xml=Y");
				WebResult webResult = WebResult.fromXMLString(reply);
				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
				{
					throw new KettleException("There was an error posting the job on the remote server: " + Const.CR+ webResult.getMessage());
				}
				carteObjectId = webResult.getId();
			}

			// Start the job
			//
			String reply = slaveServer.execService(StartJobServlet.CONTEXT_PATH + "/?name="+ URLEncoder.encode(jobMeta.getName(), "UTF-8") + "&xml=Y&id="+carteObjectId);
			WebResult webResult = WebResult.fromXMLString(reply);
			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException("There was an error starting the job on the remote server: " + Const.CR + webResult.getMessage());
			}
			return carteObjectId;
		} 
		catch (Exception e)
		{
			throw new KettleException(e);
		}
	}

	/**
	 * Add a job listener to the job
	 * @param jobListener the job listener to add
	 */
	public void addJobListener(JobListener jobListener) {
		jobListeners.add(jobListener);
	}
	
	public void addJobEntryListener(JobEntryListener jobEntryListener) {
		jobEntryListeners.add(jobEntryListener);
	}

	/**
	 * Remove a job listener from the job
	 * @param jobListener the job listener to remove
	 */
	public void removeJobListener(JobListener jobListener) {
		jobListeners.remove(jobListener);
	}
	
	/**
	 * Remove a job entry listener from the job
	 * @param jobListener the job entry listener to remove
	 */
	public void removeJobEntryListener(JobEntryListener jobEntryListener) {
		jobEntryListeners.remove(jobEntryListener);
	}
	
	public List<JobEntryListener> getJobEntryListeners() {
		return jobEntryListeners;
	}
	
	public List<JobListener> getJobListeners() {
		return jobListeners;
	}
	
	/**
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished.get();
	}

	/**
	 * @param finished the finished to set
	 */
	public void setFinished(boolean finished) {
		this.finished.set(finished);
	}

	public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
		namedParams.addParameterDefinition(key, defValue, description);		
	}

	public String getParameterDescription(String key) throws UnknownParamException {
		return namedParams.getParameterDescription(key);
	}
	
	public String getParameterDefault(String key) throws UnknownParamException {
		return namedParams.getParameterDefault(key);
	}	

	public String getParameterValue(String key) throws UnknownParamException {
		return namedParams.getParameterValue(key);
	}

	public String[] listParameters() {
		return namedParams.listParameters();
	}

	public void setParameterValue(String key, String value) throws UnknownParamException {
		namedParams.setParameterValue(key, value);
	}

	public void eraseParameters() {
		namedParams.eraseParameters();		
	}
	
	public void clearParameters() {
		namedParams.clearParameters();		
	}	

	public void activateParameters() {
		String[] keys = listParameters();
		
		for ( String key : keys )  {
			String value;
			try {
				value = getParameterValue(key);
			} catch (UnknownParamException e) {
				value = "";
			}
			String defValue;
			try {
				defValue = getParameterDefault(key);
			} catch (UnknownParamException e) {
				defValue = "";
			}
			
			if ( Const.isEmpty(value) )  {
				setVariable(key, Const.NVL(defValue, ""));
			}
			else  {
				setVariable(key, Const.NVL(value, ""));
			}
		}		 		
	}

	public void copyParametersFrom(NamedParams params) {
		namedParams.copyParametersFrom(params);
	}

	public void setSocketRepository(SocketRepository socketRepository) {
		this.socketRepository = socketRepository;
	}
	
	public SocketRepository getSocketRepository() {
		return socketRepository;
	}
	
	public LogChannelInterface getLogChannel() {
		return log;
	}

	public String getObjectName() {
		return getJobname();
	}
	
	public String getObjectCopy() {
		return null;
	}

	public String getFilename() {
		if (jobMeta==null) return null;
		return jobMeta.getFilename();
	}

	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	public ObjectId getObjectId() {
		if (jobMeta==null) return null;
		return jobMeta.getObjectId();
	}

	public ObjectRevision getObjectRevision() {
		if (jobMeta==null) return null;
		return jobMeta.getObjectRevision();
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.JOB;
	}

	public LoggingObjectInterface getParent() {
		return parentLoggingObject;
	}

	public RepositoryDirectoryInterface getRepositoryDirectory() {
		if (jobMeta==null) return null;
		return jobMeta.getRepositoryDirectory();
	}
	
  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
    log.setLogLevel(logLevel);
  }
	
	public List<LoggingHierarchy> getLoggingHierarchy() {
		List<LoggingHierarchy> hierarchy = new ArrayList<LoggingHierarchy>();
		List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren(getLogChannelId());
		for (String childId : childIds) {
			LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(childId);
			if (loggingObject!=null) {
				hierarchy.add(new LoggingHierarchy(getLogChannelId(), batchId, loggingObject));
			}
		}
		
		return hierarchy;
	}

	/**
	 * @return the interactive
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * @param interactive the interactive to set
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * @return the activeJobEntryTransformations
	 */
	public Map<JobEntryCopy, JobEntryTrans> getActiveJobEntryTransformations() {
		return activeJobEntryTransformations;
	}

	/**
	 * @return the activeJobEntryJobs
	 */
	public Map<JobEntryCopy, JobEntryJob> getActiveJobEntryJobs() {
		return activeJobEntryJobs;
	}

	/**
	 * @return A flat list of results in THIS job, in the order of execution of job entries
	 */
	public List<JobEntryResult> getJobEntryResults() {
		return jobEntryResults;
	}

  /**
   * @return the carteObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the execution container object id to set
   */
  public void setContainerObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }
  
  public LoggingObjectInterface getParentLoggingObject() {
    return parentLoggingObject;
  }
}