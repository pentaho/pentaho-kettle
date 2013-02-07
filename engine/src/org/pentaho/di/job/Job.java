/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job;

import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.pentaho.di.core.ExecutorInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTransactionListener;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.JobTracker;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.CheckpointLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.Log4jBufferAppender;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableField;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
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
import org.w3c.dom.Node;


/**
 * This class executes a job as defined by a JobMeta object.
 * <p>
 * The definition of a PDI job is represented by a JobMeta object. It is typically loaded from a .kjb file, a PDI repository, or it is generated dynamically. The declared parameters of the job definition are then queried using listParameters() and assigned values using calls to setParameterValue(..).
 * 
 * @author Matt Casters
 * @since  07-apr-2003
 * 
 */
public class Job extends Thread implements VariableSpace, NamedParams, HasLogChannelInterface, LoggingObjectInterface, ExecutorInterface
{
	private static Class<?> PKG = Job.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  public static final String               CONFIGURATION_IN_EXPORT_FILENAME = "__job_execution_configuration__.xml";

  private LogChannelInterface              log;
  private LogLevel                         logLevel                         = DefaultLogLevel.getLogLevel();
  private String                           containerObjectId;
  private JobMeta                          jobMeta;
  private int                              logCommitSize                    = 10;
  private Repository                       rep;
  private AtomicInteger                    errors;

  private VariableSpace                    variables                        = new Variables();

  /**
   * The job that's launching this (sub-) job. This gives us access to the whole
   * chain, including the parent variables, etc.
   */
  private Job                              parentJob;

  /** The parent logging interface to reference */
  private LoggingObjectInterface           parentLoggingObject;

  /**
   * Keep a list of the job entries that were executed.
   * org.pentaho.di.core.logging.CentralLogStore.getInstance()
   */
  private JobTracker                       jobTracker;

  /**
   * A flat list of results in THIS job, in the order of execution of job
   * entries
   */
  private List<JobEntryResult>             jobEntryResults;

  private Date                             startDate, endDate, currentDate, logDate, depDate;

  private AtomicBoolean                    active;

  private AtomicBoolean                    stopped;

  private long                             batchId;

  /**
   * This is the batch ID that is passed from job to job to transformation, if
   * nothing is passed, it's the job's batch id
   */
  private long                             passedBatchId;

  /**
   * The rows that were passed onto this job by a previous transformation. These
   * rows are passed onto the first job entry in this job (on the result object)
   */
  private List<RowMetaAndData>             sourceRows;

  /**
   * The result of the job, after execution.
   */
  private Result                           result;
  private AtomicBoolean                    initialized;

  private boolean                          interactive;

  private List<JobListener>                jobListeners;

  private List<JobEntryListener>           jobEntryListeners;

  private Map<JobEntryCopy, JobEntryTrans> activeJobEntryTransformations;
  private Map<JobEntryCopy, JobEntryJob>   activeJobEntryJobs;

  /**
   * Parameters of the job.
   */
  private NamedParams                      namedParams                      = new NamedParamsDefault();

  private AtomicBoolean                    finished;
  private SocketRepository                 socketRepository;

  private int                              maxJobEntriesLogged;

  private JobEntryCopy                     startJobEntryCopy;

  private String                           executingServer;
  private String                           executingUser;

  private String                           transactionId;
  
  private long                             runId;
  
  private int                              runAttemptNr;
  
  private Date                             runStartDate;
  
  private JobEntryCopy                     checkpointJobEntry;

  private Result checkpointResult;

  private Map<String,String> checkpointParameters;
  
  private boolean ignoringCheckpoints;

  /**
   * Instantiates a new job.
   *
   * @param name the name
   * @param file the file
   * @param args the args
   */
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
   
  /**
   * Initializes the Job.
   */
  public void init() {
    jobListeners = new ArrayList<JobListener>();
    jobEntryListeners = new ArrayList<JobEntryListener>();

    activeJobEntryTransformations = new HashMap<JobEntryCopy, JobEntryTrans>();
    activeJobEntryJobs = new HashMap<JobEntryCopy, JobEntryJob>();

    active = new AtomicBoolean(false);
    stopped = new AtomicBoolean(false);
    jobTracker = new JobTracker(jobMeta);
    jobEntryResults = new LinkedList<JobEntryResult>();
    initialized = new AtomicBoolean(false);
    finished = new AtomicBoolean(false);
    errors = new AtomicInteger(0);
    batchId = -1;
    passedBatchId = -1;
    maxJobEntriesLogged = Const.toInt(EnvUtil.getSystemProperty(Const.KETTLE_MAX_JOB_ENTRIES_LOGGED), 1000);

    result = null;
    this.setDefaultLogCommitSize();
  }

  /**
   * Sets the default log commit size.
   */
  private void setDefaultLogCommitSize() {
    String propLogCommitSize = this.getVariable("pentaho.log.commit.size");
    if (propLogCommitSize != null) {
      // override the logCommit variable
      try {
        logCommitSize = Integer.parseInt(propLogCommitSize);
      } catch (Exception ignored) {
        logCommitSize = 10; // ignore parsing error and default to 10
      }
    }
  }

	/**
	 * Instantiates a new job.
	 *
	 * @param repository the repository
	 * @param jobMeta the job meta
	 */
	public Job(Repository repository, JobMeta jobMeta)
	{
		this(repository, jobMeta, null);
	}

	/**
	 * Instantiates a new job.
	 *
	 * @param repository the repository
	 * @param jobMeta the job meta
	 * @param parentLogging the parent logging
	 */
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
    
    /**
     * Gets the name property of the JobMeta property.
     * @return String name for the JobMeta
     */
    @Override
    public String toString() {
    	if (jobMeta==null || Const.isEmpty(jobMeta.getName())) {
    		return getName();
    	} else {
    		return jobMeta.getName();
    	}
    }
    
    /**
     * Creates the job with new class loader.
     *
     * @return the job
     * @throws KettleException the kettle exception
     */
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
    
	/**
	 * Gets the jobname.
	 * @return the jobname
	 */
	public String getJobname()
	{
		if (jobMeta==null) return null;
		
		return jobMeta.getName();
	}
	
	/**
	 * Sets the repository.
	 * @param rep the new repository
	 */
	public void setRepository(Repository rep)
	{
		this.rep=rep;
	}
	
	/**
	 *  Threads main loop: called by Thread.start();
	 */
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
            fireJobStartListeners();
            result = execute(); 
		}
		catch(Throwable je)
		{
			log.logError(BaseMessages.getString(PKG, "Job.Log.ErrorExecJob", je.getMessage()), je);
            // log.logError(Const.getStackTracker(je));
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
				fireJobFinishListeners();
			} catch(KettleException e) {
				result.setNrErrors(1);
				result.setResult(false);
				log.logError(BaseMessages.getString(PKG, "Job.Log.ErrorExecJob", e.getMessage()), e);
			}
		}
	}

	
  /**
   * Execute a job without previous results. This is a job entry point (not
   * recursive)<br>
   * <br>
   * 
   * @return the result of the execution
   * 
   * @throws KettleException
   */
  private Result execute() throws KettleException {
    try {
      log.snap(Metrics.METRIC_JOB_START);
      
      finished.set(false);
      stopped.set(false);
      KettleEnvironment.setExecutionInformation(this, rep);
  
      // Calculate the transaction ID prior to execution
      //
      transactionId = calculateTransactionId();
  
      log.logMinimal(BaseMessages.getString(PKG, "Job.Comment.JobStarted"));
  
      // Start the tracking...
      JobEntryResult jerStart = new JobEntryResult(null, null, BaseMessages.getString(PKG, "Job.Comment.JobStarted"), BaseMessages.getString(PKG, "Job.Reason.Started"), null, 0, null);
      jobTracker.addJobTracker(new JobTracker(jobMeta, jerStart));
  
      active.set(true);
  
      // Where do we start?
      JobEntryCopy startpoint;
  
      // synchronize this to a parent job if needed.
      //
      Object syncObject = this;
      if (parentJob != null) {
        syncObject = parentJob; // parallel execution in a job
      }
      
      synchronized (syncObject) {
        beginProcessing();
      }
  
      if (startJobEntryCopy == null) {
        startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
      } else {
        startpoint = startJobEntryCopy;
      }
      if (startpoint == null) {
        throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.CounldNotFindStartingPoint"));
      }
  
      Result res = null;
      JobEntryResult jerEnd = null;
  
      if (startpoint.isStart()) {
        // Perform optional looping in the special Start job entry...
        //
        long iteration = 0;
        boolean isFirst = true;
        JobEntrySpecial jes = (JobEntrySpecial) startpoint.getEntry();
        while ((jes.isRepeat() || isFirst) && !isStopped()) {
          isFirst = false;
          res = execute(0, null, startpoint, null, BaseMessages.getString(PKG, "Job.Reason.Started"));
          if (iteration > 0 && (iteration % 500) == 0) {
            System.out.println("other 500 iterations: " + iteration);
          }
          iteration++;
        }
        jerEnd = new JobEntryResult(res, jes.getLogChannelId(), BaseMessages.getString(PKG, "Job.Comment.JobFinished"), BaseMessages.getString(PKG, "Job.Reason.Finished"), null, 0, null);
      } else {
        res = execute(0, null, startpoint, null, BaseMessages.getString(PKG, "Job.Reason.Started"));
        jerEnd = new JobEntryResult(res, startpoint.getEntry().getLogChannel().getLogChannelId(), BaseMessages.getString(PKG, "Job.Comment.JobFinished"), BaseMessages.getString(PKG, "Job.Reason.Finished"), null, 0, null);
      }
      // Save this result...
      jobTracker.addJobTracker(new JobTracker(jobMeta, jerEnd));
      log.logMinimal(BaseMessages.getString(PKG, "Job.Comment.JobFinished"));
  
      active.set(false);
      finished.set(true);
  
      return res;
    } finally {
      log.snap(Metrics.METRIC_JOB_STOP);
    }
  }

  /**
   * Execute a job with previous results passed in.<br>
   * <br>
   * Execute called by JobEntryJob: don't clear the jobEntryResults.
   * 
   * @param nr
   *          The job entry number
   * @param result
   *          the result of the previous execution
   * @return Result of the job execution
   * @throws KettleJobException
   */
  public Result execute(int nr, Result result) throws KettleException {
    finished.set(false);
    active.set(true);
    initialized.set(true);
    KettleEnvironment.setExecutionInformation(this, rep);

    // Calculate the transaction ID prior to execution
    //
    transactionId = calculateTransactionId();

    // Where do we start?
    JobEntryCopy startpoint;

    // Perhaps there is already a list of input rows available?
    if (getSourceRows() != null) {
      result.setRows(getSourceRows());
    }

    startpoint = jobMeta.findJobEntry(JobMeta.STRING_SPECIAL_START, 0, false);
    if (startpoint == null) {
      throw new KettleJobException(BaseMessages.getString(PKG, "Job.Log.CounldNotFindStartingPoint"));
    }

    Result res = execute(nr, result, startpoint, null, BaseMessages.getString(PKG, "Job.Reason.StartOfJobentry"));

    active.set(false);

    return res;
  }
	
	/**
	 * Sets the finished flag.<b>
	 * Then launch all the job listeners and call the jobFinished method for each.<br>
	 * 
	 * @see JobListener#jobFinished(Job)
	 */
	public void fireJobFinishListeners() throws KettleException {
		for (JobListener jobListener : jobListeners) {
			jobListener.jobFinished(this);
		}
	}
	
	/**
   * Call all the jobStarted method for each listener.<br>
   * 
   * @see JobListener#jobStarted(Job)
   */
  public void fireJobStartListeners() throws KettleException {
    for (JobListener jobListener : jobListeners) {
      jobListener.jobStarted(this);
    }
  }
  
  /**
   * Execute a job entry recursively and move to the next job entry
   * automatically.<br>
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
  private Result execute(final int nr, Result prev_result, final JobEntryCopy jobEntryCopy, JobEntryCopy previous, String reason) throws KettleException {
    Result res = null;

    if (stopped.get()) {
      res = new Result(nr);
      res.stopped = true;
      return res;
    }
    
    // if we didn't have a previous result, create one, otherwise, copy the content...
    //
    final Result result;
    Result prevResult = null;
    if (prev_result != null) {
      prevResult = (Result) prev_result.clone();
    } else {
      prevResult = new Result();
    }

    // See if we found a checkpoint from a previous execution...
    //
    if (checkpointJobEntry!=null && checkpointJobEntry==jobEntryCopy) {
      CheckpointLogTable checkpointLogTable = jobMeta.getCheckpointLogTable();
      
      // Yes, we need to restore the previous checkpoint results at this point...
      //
      checkpointResult.setResult(true);
      checkpointResult.setNrErrors(0);
      result = prevResult = checkpointResult;
      
      // Do we need to restore the parameter values?
      //
      if ("Y".equalsIgnoreCase(environmentSubstitute(checkpointLogTable.getSaveParameters()))) {
        for (String name : checkpointParameters.keySet()) {
          setParameterValue(name, checkpointParameters.get(name));
        }
      }
      
      // Do we need to keep the result rows from the checkpoint?
      //
      if (!"Y".equalsIgnoreCase(environmentSubstitute(checkpointLogTable.getSaveResultRows()))) {
        prevResult.getRows().clear();
      }

      // Do we need to keep the result files from the checkpoint?
      //
      if (!"Y".equalsIgnoreCase(environmentSubstitute(checkpointLogTable.getSaveResultFiles()))) {
        prevResult.getResultFiles().clear();
      }
      
      // Add some basic results logging
      //
      JobEntryResult jerCheckpoint = new JobEntryResult(result, getLogChannel().getLogChannelId(), 
          BaseMessages.getString(PKG, "Job.Comment.JobRestartAtCheckpoint"), null, jobEntryCopy.getName(), jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
      jerCheckpoint.setCheckpoint(true);
      jobTracker.addJobTracker(new JobTracker(jobMeta, jerCheckpoint));
      jobEntryResults.add(jerCheckpoint);
      
      log.logBasic("Restarting from checkpoint job entry: "+checkpointJobEntry.toString());

    } else {
      if (log.isDetailed())
        log.logDetailed("exec(" + nr + ", " + (prev_result != null ? prev_result.getNrErrors() : 0) + ", " + (jobEntryCopy != null ? jobEntryCopy.toString() : "null") + ")");
  
      // Which entry is next?
      JobEntryInterface jobEntryInterface = jobEntryCopy.getEntry();
      jobEntryInterface.getLogChannel().setLogLevel(logLevel);
  
      // Track the fact that we are going to launch the next job entry...
      JobEntryResult jerBefore = new JobEntryResult(null, null, BaseMessages.getString(PKG, "Job.Comment.JobStarted"), reason, jobEntryCopy.getName(), jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy.getEntry().getFilename()));
      jobTracker.addJobTracker(new JobTracker(jobMeta, jerBefore));
  
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(jobEntryInterface.getClass().getClassLoader());
      // Execute this entry...
      JobEntryInterface cloneJei = (JobEntryInterface) jobEntryInterface.clone();
      ((VariableSpace) cloneJei).copyVariablesFrom(this);
      cloneJei.setRepository(rep);
      cloneJei.setParentJob(this);
      final long start = System.currentTimeMillis();
  
      cloneJei.getLogChannel().logDetailed("Starting job entry");
      for (JobEntryListener jobEntryListener : jobEntryListeners) {
        jobEntryListener.beforeExecution(this, jobEntryCopy, cloneJei);
      }
      if (interactive) {
        if (jobEntryCopy.isTransformation()) {
          getActiveJobEntryTransformations().put(jobEntryCopy, (JobEntryTrans) cloneJei);
        }
        if (jobEntryCopy.isJob()) {
          getActiveJobEntryJobs().put(jobEntryCopy, (JobEntryJob) cloneJei);
        }
      }
      log.snap(Metrics.METRIC_JOBENTRY_START, cloneJei.toString());
      result = cloneJei.execute(prevResult, nr);
      log.snap(Metrics.METRIC_JOBENTRY_STOP, cloneJei.toString());
      
      final long end = System.currentTimeMillis();
      if (interactive) {
        if (jobEntryCopy.isTransformation()) {
          getActiveJobEntryTransformations().remove(jobEntryCopy);
        }
        if (jobEntryCopy.isJob()) {
          getActiveJobEntryJobs().remove(jobEntryCopy);
        }
      }
  
      if (cloneJei instanceof JobEntryTrans) {
        String throughput = result.getReadWriteThroughput((int) ((end - start) / 1000));
        if (throughput != null) {
          log.logMinimal(throughput);
        }
      }
      for (JobEntryListener jobEntryListener : jobEntryListeners) {
        jobEntryListener.afterExecution(this, jobEntryCopy, cloneJei, result);
      }
  
      Thread.currentThread().setContextClassLoader(cl);
      addErrors((int) result.getNrErrors());
  
      // Also capture the logging text after the execution...
      //
      Log4jBufferAppender appender = CentralLogStore.getAppender();
      StringBuffer logTextBuffer = appender.getBuffer(cloneJei.getLogChannel().getLogChannelId(), false);
      result.setLogText(logTextBuffer.toString());
  
      // Save this result as well...
      //
      JobEntryResult jerAfter = new JobEntryResult(result, cloneJei.getLogChannel().getLogChannelId(), BaseMessages.getString(PKG, "Job.Comment.JobFinished"), null, jobEntryCopy.getName(), jobEntryCopy.getNr(), environmentSubstitute(jobEntryCopy
          .getEntry().getFilename()));
      jobTracker.addJobTracker(new JobTracker(jobMeta, jerAfter));
      jobEntryResults.add(jerAfter);
  
      // Only keep the last X job entry results in memory
      //
      if (maxJobEntriesLogged > 0 && jobEntryResults.size() > maxJobEntriesLogged + 50) {
        // Remove the oldest.
        jobEntryResults = jobEntryResults.subList(50, jobEntryResults.size()); 
      }
  
      // If this is a checkpoint, write to the checkpoint log table
      //
      if (jobEntryCopy.isCheckpoint() && jobMeta.getCheckpointLogTable().isDefined()) {
        writeCheckpointInformation(jobEntryCopy, result);
      }
    }

    // Try all next job entries.
    //
    // Keep track of all the threads we fired in case of parallel execution...
    // Keep track of the results of these executions too.
    //
    final List<Thread> threads = new ArrayList<Thread>();
    final List<Result> threadResults = new ArrayList<Result>();
    final List<KettleException> threadExceptions = new ArrayList<KettleException>();
    final List<JobEntryCopy> threadEntries = new ArrayList<JobEntryCopy>();

    // Launch only those where the hop indicates true or false
    //
    int nrNext = jobMeta.findNrNextJobEntries(jobEntryCopy);
    for (int i = 0; i < nrNext && !isStopped(); i++) {
      // The next entry is...
      final JobEntryCopy nextEntry = jobMeta.findNextJobEntry(jobEntryCopy, i);

      // See if we need to execute this...
      final JobHopMeta hi = jobMeta.findJobHop(jobEntryCopy, nextEntry);

      // The next comment...
      final String nextComment;
      if (hi.isUnconditional()) {
        nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedUnconditional");
      } else {
        if (result.getResult()) {
          nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedSuccess");
        } else {
          nextComment = BaseMessages.getString(PKG, "Job.Comment.FollowedFailure");
        }
      }

      //
      // If the link is unconditional, execute the next job entry (entries).
      // If the start point was an evaluation and the link color is correct:
      // green or red, execute the next job entry...
      //
      if (hi.isUnconditional() || (jobEntryCopy.evaluates() && (!(hi.getEvaluation() ^ result.getResult())))) {
        // Start this next step!
        if (log.isBasic())
          log.logBasic(BaseMessages.getString(PKG, "Job.Log.StartingEntry", nextEntry.getName()));

        // Pass along the previous result, perhaps the next job can use it...
        // However, set the number of errors back to 0 (if it should be reset)
        // When an evaluation is executed the errors e.g. should not be reset.
        if (nextEntry.resetErrorsBeforeExecution()) {
          result.setNrErrors(0);
        }

        // Now execute!
        //
        // if (we launch in parallel, fire the execution off in a new thread...
        //
        if (jobEntryCopy.isLaunchingInParallel()) {
          threadEntries.add(nextEntry);

          Runnable runnable = new Runnable() {
            public void run() {
              try {
                Result threadResult = execute(nr + 1, result, nextEntry, jobEntryCopy, nextComment);
                threadResults.add(threadResult);
              } catch (Throwable e) {
                log.logError(Const.getStackTracker(e));
                threadExceptions.add(new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedError", nextEntry.toString()), e));
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
          if (log.isBasic())
            log.logBasic(BaseMessages.getString(PKG, "Job.Log.LaunchedJobEntryInParallel", nextEntry.getName()));
        } else {
          try {
            // Same as before: blocks until it's done
            //
            res = execute(nr + 1, result, nextEntry, jobEntryCopy, nextComment);
          } catch (Throwable e) {
            log.logError(Const.getStackTracker(e));
            throw new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedError", nextEntry.toString()), e);
          }
          if (log.isBasic())
            log.logBasic(BaseMessages.getString(PKG, "Job.Log.FinishedJobEntry", nextEntry.getName(), res.getResult() + ""));
        }
      }
    }

    // OK, if we run in parallel, we need to wait for all the job entries to
    // finish...
    //
    if (jobEntryCopy.isLaunchingInParallel()) {
      for (int i = 0; i < threads.size(); i++) {
        Thread thread = threads.get(i);
        JobEntryCopy nextEntry = threadEntries.get(i);

        try {
          thread.join();
        } catch (InterruptedException e) {
          log.logError(jobMeta.toString(), BaseMessages.getString(PKG, "Job.Log.UnexpectedErrorWhileWaitingForJobEntry", nextEntry.getName()));
          threadExceptions.add(new KettleException(BaseMessages.getString(PKG, "Job.Log.UnexpectedErrorWhileWaitingForJobEntry", nextEntry.getName()), e));
        }
      }
      // if(log.isBasic()) log.logBasic(BaseMessages.getString(PKG,
      // "Job.Log.FinishedJobEntry",startpoint.getName(),res.getResult()+""));
    }

    // Perhaps we don't have next steps??
    // In this case, return the previous result.
    if (res == null) {
      res = prevResult;
    }

    // See if there where any errors in the parallel execution
    //
    if (threadExceptions.size() > 0) {
      res.setResult(false);
      res.setNrErrors(threadExceptions.size());

      for (KettleException e : threadExceptions) {
        log.logError(jobMeta.toString(), e.getMessage(), e);
      }

      // Now throw the first Exception for good measure...
      //
      throw threadExceptions.get(0);
    }

    // In parallel execution, we aggregate all the results, simply add them to
    // the previous result...
    //
    for (Result threadResult : threadResults) {
      res.add(threadResult);
    }

    // If there have been errors, logically, we need to set the result to
    // "false"...
    //
    if (res.getNrErrors() > 0) {
      res.setResult(false);
    }

    return res;
  }

  /**
   * Writes a checkpoint to the checkpoint log table.
   * @param jobEntryCopy
   * @param result
   * @throws KettleException
   */
  private void writeCheckpointInformation(JobEntryCopy jobEntryCopy, Result result) throws KettleException {
    Database db = null;
    CheckpointLogTable checkpointLogTable = jobMeta.getCheckpointLogTable();
    
    // Keep track in the job itself so everyone knows about this
    //
    setCheckpointJobEntry(jobEntryCopy);
    
    try {
      db = new Database(this, checkpointLogTable.getDatabaseMeta());
      db.shareVariablesWith(this);
      db.connect();
      db.setCommit(logCommitSize);

      LogStatus logStatus = LogStatus.RUNNING;
      
      // First run ever, look up a new value for the run ID.
      //
      if (runId<0) {
        runId = checkpointLogTable.getDatabaseMeta().getNextBatchId(db, 
            checkpointLogTable.getSchemaName(), 
            checkpointLogTable.getTableName(), 
            checkpointLogTable.getKeyField().getFieldName()
           );
        
        // This is the first attempt to run this job
        // 
        logStatus = LogStatus.START;
      }
      
      db.writeLogRecord(checkpointLogTable, logStatus, result, this);
      
      // Also time-out the log records in here...
      //
      db.cleanupLogRecords(checkpointLogTable);

    } catch(Exception e) {
      throw new KettleException(BaseMessages.getString(PKG, "Job.Exception.UnableToWriteCheckpointInformationToLogTable"), e);
    } finally {
      if (!db.isAutoCommit()) db.commit(true);
      db.disconnect();
    }
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
    long time = 0L;
    while (isAlive() && (time < maxMiliseconds || maxMiliseconds <= 0)) {
      try {
        Thread.sleep(1);
        time += 1;
      } catch (InterruptedException e) {
      }
    }    
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
  public boolean beginProcessing() throws KettleException {
    currentDate = new Date();
    logDate = new Date();
    startDate = Const.MIN_DATE;
    endDate = currentDate;

    resetErrors();

    final JobLogTable jobLogTable = jobMeta.getJobLogTable();
    int intervalInSeconds = Const.toInt(environmentSubstitute(jobLogTable.getLogInterval()), -1);

    if (jobLogTable.isDefined()) {

      DatabaseMeta logcon = jobMeta.getJobLogTable().getDatabaseMeta();
      String schemaName = environmentSubstitute(jobMeta.getJobLogTable().getActualSchemaName());
      String tableName = environmentSubstitute(jobMeta.getJobLogTable().getActualTableName());
      String schemaAndTable = jobMeta.getJobLogTable().getDatabaseMeta().getQuotedSchemaTableCombination(schemaName, tableName);
      Database ldb = new Database(this, logcon);
      ldb.shareVariablesWith(this);
      ldb.connect();
      ldb.setCommit(logCommitSize);

      try {
        // See if we have to add a batch id...
        Long id_batch = new Long(1);
        if (jobMeta.getJobLogTable().isBatchIdUsed()) {
          id_batch = logcon.getNextBatchId(ldb, schemaName, tableName, jobLogTable.getKeyField().getFieldName());
          setBatchId(id_batch.longValue());
          if (getPassedBatchId() <= 0) {
            setPassedBatchId(id_batch.longValue());
          }
        }

        Object[] lastr = ldb.getLastLogDate(schemaAndTable, jobMeta.getName(), true, LogStatus.END); // $NON-NLS-1$
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
        if (!ldb.isAutoCommit()) ldb.commit(true);
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

          addJobListener(new JobAdapter() {
            public void jobFinished(Job job) {
              timer.cancel();
            }
          });
        }

        // Add a listener at the end of the job to take of writing the final job
        // log record...
        //
        addJobListener(new JobAdapter() {
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
        addJobListener(new JobAdapter() {
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
      addJobListener(new JobAdapter() {

        public void jobFinished(Job job) throws KettleException {
          try {
            writeLogChannelInformation();
          } catch (KettleException e) {
            throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformLoggingAtTransEnd"), e);
          }
        }
      });
    }
    
    // If the job is running database transactional with unique connections, close them at the end...
    //
    if (jobMeta.isUsingUniqueConnections()) {
      addJobListener(new JobAdapter() {
        @Override
        public void jobFinished(Job job) throws KettleException {
          
          job.closeUniqueDatabaseConnections();
        }
      });
    }
    
    // See if we need to start at an alternate location in the job based on the information from the run ID
    //
    if (jobMeta.getCheckpointLogTable().isDefined()) {
      // Look up information from the previous attempt
      //
      lookupCheckpoint();
      
      // Make sure to write the attempt information
      //
      if (runAttemptNr>1) {
        writeCheckpointInformation(checkpointJobEntry, checkpointResult);
      }
      
      // At the end of the job, if everything ran without error, clear the job entry name from the checkpoint table.
      // That way we know next time around we can grab a new run ID for this job.
      //
      addJobListener(new JobAdapter() {
        @Override
        public void jobFinished(Job job) throws KettleException {
          // Clear the checkpoint information in the checkpoint table
          // But only in case everything ran without error in the job.
          //
          if (result!=null && result.getResult() && result.getNrErrors()==0) {
            checkpointJobEntry=null;
            writeCheckpointInformation(null, result);
          }

        }
      });
    }
    

    return true;
  }
	
  /**
   * Looks up checkpoint information and updates the Job if appropriate.
   * @throws KettleException
   */
	protected void lookupCheckpoint() throws KettleException {
	  
	  try {
  	  // Set some defaults regardless of lookup...
  	  //
  	  runId = -1;
  	  
  	  // Take start of this transformation if we have nothing else.
  	  // This will be logged by the checkpoints later on.
  	  //
  	  runStartDate = getLogDate();
  	  
  	  // No job entry to restart from by default: use the Start job entry
  	  //
  	  checkpointJobEntry = null; 
  
  	  // This is the first attempt by default
  	  //
      runAttemptNr=1;
      
      // If we don't want to use a previous checkpoint, pretend we didn't find it.
      //
      if (isIgnoringCheckpoints()) {
        return;
      }
  
  	  // Now look up some data in the check point log table...
  	  //
  	  CheckpointLogTable logTable = jobMeta.getCheckpointLogTable();
  	  String namespace = Const.NVL(getParameterValue(logTable.getNamespaceParameter()), "-");
  	  DatabaseMeta dbMeta = logTable.getDatabaseMeta();
  	  String schemaTable = dbMeta.getQuotedSchemaTableCombination(logTable.getActualSchemaName(), logTable.getActualTableName());
  	  Database db=null;
  	  try {
        db = new Database(this, dbMeta);
        db.shareVariablesWith(this);
        db.connect();
        db.setCommit(logCommitSize);
        
        // The fields to retrieve
        //
        LogTableField idJobRunField = logTable.getKeyField();
        String idJobRunFieldName = dbMeta.quoteField(idJobRunField.getFieldName());
        LogTableField jobRunStartDateField = logTable.getJobRunStartDateField();
        String jobRunStartDateFieldName = dbMeta.quoteField(jobRunStartDateField.getFieldName());
        LogTableField checkpointNameField = logTable.getCheckpointNameField();
        String checkpointNameFieldName = dbMeta.quoteField(checkpointNameField.getFieldName());
        LogTableField checkpointNrField = logTable.getCheckpointCopyNrField();
        String checkpointNrFieldName = dbMeta.quoteField(checkpointNrField.getFieldName());
        LogTableField attemptNrField = logTable.getAttemptNrField();
        String attemptNrFieldName = dbMeta.quoteField(attemptNrField.getFieldName());
        LogTableField resultXmlField = logTable.getResultXmlField();
        String resultXmlFieldName = dbMeta.quoteField(resultXmlField.getFieldName());
        LogTableField parameterXmlField = logTable.getParameterXmlField();
        String parameterXmlFieldName = dbMeta.quoteField(parameterXmlField.getFieldName());
        
        // The parameters values to pass
        //
        RowMetaAndData pars = new RowMetaAndData();
  
        LogTableField jobNameField = logTable.getNameField();
        String jobNameFieldName = dbMeta.quoteField(jobNameField.getFieldName());
        pars.addValue(idJobRunFieldName, ValueMetaInterface.TYPE_STRING, jobMeta.getName());
        LogTableField namespaceField = logTable.getNamespaceField();
        String namespaceFieldName = dbMeta.quoteField(namespaceField.getFieldName());
        pars.addValue(namespaceFieldName, ValueMetaInterface.TYPE_STRING, namespace);
        
        String sql = "SELECT "+idJobRunFieldName+", "+jobRunStartDateFieldName+", "+checkpointNameFieldName+", "+checkpointNrFieldName+", "+attemptNrFieldName+", "+resultXmlFieldName+", "+parameterXmlFieldName;
        sql+=" FROM "+schemaTable;
        sql+=" WHERE "+jobNameFieldName+" = ? AND "+namespaceFieldName+" = ? ";
        sql+=" AND "+checkpointNrFieldName+" IS NOT NULL"; // nulled at the successful end of the job so we don't need these rows.
        
        // Grab the matching rows, if more than one matches just grab the first...
        //
        PreparedStatement statement = db.prepareSQL(sql);
        ResultSet resultSet = db.openQuery(statement, pars.getRowMeta(), pars.getData());
        Object[] rowData = db.getRow(resultSet);
  
        // If there is no checkpoint found, call it a day
        //
        if (rowData==null) {
          return;
        }
        RowMetaInterface rowMeta = db.getReturnRowMeta();
        
        // Get the data from the row
        //
        int index=0;
        Long lookupRunId = rowMeta.getInteger(rowData, index++);
        Date jobRunStartDate = rowMeta.getDate(rowData, index++);
        String checkpointName = rowMeta.getString(rowData, index++);
        Long checkpointNr = rowMeta.getInteger(rowData, index++);
        Long attemptNr = rowMeta.getInteger(rowData, index++);
        String resultXml = rowMeta.getString(rowData, index++);
        String parameterXml = rowMeta.getString(rowData, index++);
        
        // Do some basic checks on the table data...
        //
        if (lookupRunId==null || jobRunStartDate==null || checkpointName==null || checkpointNr==null || attemptNr==null || resultXml==null || parameterXml==null) {
          // Nothing to checkpoint to, call it quits.
          //
          return;
        }
        
        // See if the retry period hasn't expired...
        //
        int retryPeriodInMinutes = Const.toInt(environmentSubstitute(logTable.getRunRetryPeriod()), -1);
        if (retryPeriodInMinutes>0) {
          long maxTime = runStartDate.getTime()+retryPeriodInMinutes*60*1000;
          if (getStartDate().getTime() > maxTime) {
            // retry period expired
            throw new KettleException("Retry period exceeded, please reset job ["+jobMeta.getName()+"] for namespace ["+namespace+"]");
          }
        }
        
        // Verify the max number of retries / attempts...
        //
        int maxAttempts = Const.toInt(environmentSubstitute(logTable.getMaxNrRetries()), -1);
        if (maxAttempts>0) {
          if (attemptNr+1>maxAttempts) {
            throw new KettleException("The job checkpoint system has reached the maximum number or retries after "+attemptNr+" attempts");
          }
        }
        
        // All OK, now pass the looked up data to the job
        //
        runStartDate = jobRunStartDate;
        runId = lookupRunId;
        runAttemptNr=attemptNr.intValue()+1;
        checkpointJobEntry = jobMeta.findJobEntry(checkpointName, checkpointNr.intValue(), false);
        if (checkpointJobEntry==null) {
          throw new KettleException("Unable to find checkpoint job entry with name ["+checkpointName+"] and copy number ["+checkpointNr+"]");
        }
        
        // We start at the checkpoint job entry (this is skipped though)
        //
        startJobEntryCopy = checkpointJobEntry;
        
        // The result
        //
        checkpointResult = new Result(XMLHandler.loadXMLString(resultXml, Result.XML_TAG));
        checkpointParameters = extractParameters(parameterXml);
  
  	  } catch(Exception e) {
  	    throw new KettleException("Unable to look up checkpoint information in the check point log table", e);
  	  } finally {
  	    if (db!=null) {
  	      db.disconnect();
  	    }
  	  }
	  } finally {
	    // Set variables
	    //
	    setVariable(Const.INTERNAL_VARIABLE_JOB_RUN_ID, Long.toString(runId));
      setVariable(Const.INTERNAL_VARIABLE_JOB_RUN_ATTEMPTNR, Long.toString(runAttemptNr));
	  }
  }

	/**
	 * Extracts the parameters from the passed XML.
	 * @param parameterXml
	 * @return Map<String, String> 
	 * @throws KettleException
	 */
  private Map<String, String> extractParameters(String parameterXml) throws KettleException {
    Map<String, String> map = new HashMap<String, String>();
    List<Node> parameterNodes = XMLHandler.getNodes(XMLHandler.loadXMLString(parameterXml, "pars"), "par");
    for (Node parameterNode : parameterNodes) {
      String name = XMLHandler.getTagValue(parameterNode, "name");
      String value = XMLHandler.getTagValue(parameterNode, "value");
      map.put(name,  value);
    }
    return map;
  }

  //
	// Handle logging at end
	/**
	 * End processing.
	 *
	 * @return true, if successful
	 * @throws KettleJobException the kettle job exception
	 */
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

				String tableName = jobMeta.getJobLogTable().getActualTableName();
				DatabaseMeta logcon = jobMeta.getJobLogTable().getDatabaseMeta();

				Database ldb = new Database(this, logcon);
				ldb.shareVariablesWith(this);
				try
				{
					ldb.connect();
  	            ldb.setCommit(logCommitSize);
					ldb.writeLogRecord(jobMeta.getJobLogTable(), status, this, null);
				}
				catch(KettleDatabaseException dbe)
				{
					addErrors(1);
					throw new KettleJobException("Unable to end processing by writing log record to table "+tableName, dbe);
				}
				finally
				{
				  if (!ldb.isAutoCommit()) ldb.commit(true);
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
	
	/**
	 * Write log channel information.
	 *
	 * @throws KettleException the kettle exception
	 */
	protected void writeLogChannelInformation() throws KettleException {
		Database db = null;
		ChannelLogTable channelLogTable = jobMeta.getChannelLogTable();
		
		// PDI-7070: If parent job has the same channel logging info, don't duplicate log entries
		Job j = getParentJob();
		
		if(j != null) {
			if(channelLogTable.equals(j.getJobMeta().getChannelLogTable())) 
				return;
		}
		// end PDI-7070
		
		try {
			db = new Database(this, channelLogTable.getDatabaseMeta());
			db.shareVariablesWith(this);
			db.connect();
			db.setCommit(logCommitSize);
			
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
		  if (!db.isAutoCommit()) db.commit(true);
			db.disconnect();
		}
	}

    /**
     * Write job entry log information.
     *
     * @throws KettleException the kettle exception
     */
    protected void writeJobEntryLogInformation() throws KettleException {
      Database db = null;
      JobEntryLogTable jobEntryLogTable = jobMeta.getJobEntryLogTable();
      try {
        db = new Database(this, jobEntryLogTable.getDatabaseMeta());
        db.shareVariablesWith(this);
        db.connect();
        db.setCommit(logCommitSize);
        
        for (JobEntryCopy copy : jobMeta.getJobCopies()) {
          db.writeLogRecord(jobEntryLogTable, LogStatus.START, copy, this);
        }
  
      } catch (Exception e) {
        throw new KettleException(BaseMessages.getString(PKG, "Job.Exception.UnableToJobEntryInformationToLogTable"), e);
      } finally {
        if (!db.isAutoCommit()) db.commit(true);
        db.disconnect();
      }
    }
  	
	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive()
	{
		return active.get();
	}
	
	/**
	 * Stop all activity by setting the stopped property to true.
	 */
	public void stopAll()
	{
		stopped.set(true);
	}
	
	/**
	 * Sets the stopped.
	 *
	 * @param stopped the new stopped
	 */
	public void setStopped(boolean stopped)
	{
		this.stopped.set(stopped);
	}
	
	/**
	 * Gets the stopped status of this Job.
	 * @return Returns the stopped status of this Job
	 */
	public boolean isStopped()
	{
		return stopped.get();
	}
	
	/**
	 * Gets the start date.
	 * @return Returns the startDate
	 */
	public Date getStartDate()
	{
		return startDate;
	}
	
	/**
	 * Gets the end date.
	 * @return Returns the endDate
	 */
	public Date getEndDate()
	{
		return endDate;
	}
	
	/**
	 * Gets the current date.
	 * @return Returns the currentDate
	 */
	public Date getCurrentDate()
	{
		return currentDate;
	}
	
	/**
	 * Gets the dep date.
	 * @return Returns the depDate
	 */
	public Date getDepDate()
	{
		return depDate;
	}

	/**
	 * Gets the log date.
	 * @return Returns the logDate
	 */
	public Date getLogDate()
	{
		return logDate;
	}

	/**
	 * Gets the job meta.
	 * @return Returns the JobMeta
	 */
	public JobMeta getJobMeta()
	{
		return jobMeta;
	}
		
    /**
	 * Gets the rep (repository).
	 * @return Returns the rep
	 */
	public Repository getRep()
	{
		return rep;
	}	

    /**
     * Gets the thread.
     *
     * @return the thread
     */
    public Thread getThread()
    {
        return (Thread)this;
    }
    
    /**
     * Gets the job tracker.
     * @return Returns the jobTracker
     */
    public JobTracker getJobTracker()
    {
        return jobTracker;
    }

    /**
     * Sets the job tracker.
     * @param jobTracker The jobTracker to set
     */
    public void setJobTracker(JobTracker jobTracker)
    {
        this.jobTracker = jobTracker;
    }

    /**
     * Sets the source rows.
     *
     * @param sourceRows the new source rows
     */
    public void setSourceRows(List<RowMetaAndData> sourceRows)
    {
        this.sourceRows = sourceRows;
    }

    /**
     * Gets the source rows.
     *
     * @return the source rows
     */
    public List<RowMetaAndData> getSourceRows()
    {
        return sourceRows;
    }

    /**
     * Gets the parent job.
     * @return Returns the parentJob
     */
    public Job getParentJob()
    {
        return parentJob;
    }

    /**
     * Sets the parent job.
     * @param parentJob The parentJob to set.
     */
    public void setParentJob(Job parentJob)
    {
      this.logLevel = parentJob.getLogLevel();
      this.log.setLogLevel(logLevel);
      this.containerObjectId = log.getContainerObjectId();
      this.parentJob = parentJob;
    }

    /**
     * Gets the result.
     * @return the result
     */
    public Result getResult()
    {
        return result;
    }

    /**
     * Sets the result.
     * @param result the new result
     */
    public void setResult(Result result)
    {
        this.result = result;
    }

    /**
     * Gets the boolean value of initialized.
     * @return Returns the initialized
     */
    public boolean isInitialized()
    {
        return initialized.get();
    }

    /**
     * Gets the batchId.
     * @return Returns the batchId
     */
    public long getBatchId()
    {
        return batchId;
    }

    /**
     * Sets the batchId.
     * @param batchId The batchId to set
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }


    /**
     * Gets the passedBatchId.
     * @return the passedBatchId
     */
    public long getPassedBatchId()
    {
        return passedBatchId;
    }

    /**
     * Sets the passedBatchId.
     * @param jobBatchId the jobBatchId to set
     */
    public void setPassedBatchId(long jobBatchId)
    {
        this.passedBatchId = jobBatchId;
    }
    
    /**
     * Sets the internal kettle variables.
     *
     * @param var the new internal kettle variables.
     */
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
    
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#copyVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
	 */
	public void copyVariablesFrom(VariableSpace space) 
	{
		variables.copyVariablesFrom(space);		
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String)
	 */
	public String environmentSubstitute(String aString) 
	{
		return variables.environmentSubstitute(aString);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String[])
	 */
	public String[] environmentSubstitute(String aString[]) 
	{
		return variables.environmentSubstitute(aString);
	}		

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#getParentVariableSpace()
	 */
	public VariableSpace getParentVariableSpace() 
	{
		return variables.getParentVariableSpace();
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#setParentVariableSpace(org.pentaho.di.core.variables.VariableSpace)
	 */
	public void setParentVariableSpace(VariableSpace parent) 
	{
		variables.setParentVariableSpace(parent);
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String, java.lang.String)
	 */
	public String getVariable(String variableName, String defaultValue) 
	{
		return variables.getVariable(variableName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String)
	 */
	public String getVariable(String variableName) 
	{
		return variables.getVariable(variableName);
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#getBooleanValueOfVariable(java.lang.String, boolean)
	 */
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

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#initializeVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
	 */
	public void initializeVariablesFrom(VariableSpace parent) 
	{
		variables.initializeVariablesFrom(parent);	
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#listVariables()
	 */
	public String[] listVariables() 
	{
		return variables.listVariables();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#setVariable(java.lang.String, java.lang.String)
	 */
	public void setVariable(String variableName, String variableValue) 
	{
		variables.setVariable(variableName, variableValue);		
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#shareVariablesWith(org.pentaho.di.core.variables.VariableSpace)
	 */
	public void shareVariablesWith(VariableSpace space) 
	{
		variables = space;		
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.variables.VariableSpace#injectVariables(java.util.Map)
	 */
	public void injectVariables(Map<String,String> prop) 
	{
		variables.injectVariables(prop);		
	}	

    /**
     * Gets the status.
     * @return the status
     */
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
    
	/**
	 * Send to slave server.
	 *
	 * @param jobMeta the job meta
	 * @param executionConfiguration the execution configuration
	 * @param repository the repository
	 * @return the string
	 * @throws KettleException the kettle exception
	 */
	public static String sendToSlaveServer(JobMeta jobMeta, JobExecutionConfiguration executionConfiguration, Repository repository) throws KettleException
	{
		String carteObjectId;
		SlaveServer slaveServer = executionConfiguration.getRemoteServer();

		if (slaveServer == null)
			throw new KettleException(BaseMessages.getString(PKG, "Job.Log.NoSlaveServerSpecified"));
		if (Const.isEmpty(jobMeta.getName()))
			throw new KettleException(BaseMessages.getString(PKG, "Job.Log.UniqueJobName"));

		// Align logging levels between execution configuration and remote server
		slaveServer.getLogChannel().setLogLevel(executionConfiguration.getLogLevel());
		
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
	
	/**
	 * Adds the job entry listener.
	 * @param jobEntryListener the job entry listener
	 */
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
	
	/**
	 * Gets the job entry listeners.
	 * @return the job entry listeners
	 */
	public List<JobEntryListener> getJobEntryListeners() {
		return jobEntryListeners;
	}
	
	/**
	 * Gets the job listeners.
	 * @return the job listeners
	 */
	public List<JobListener> getJobListeners() {
		return jobListeners;
	}
	
	/**
	 * Gets the boolean value of finished.
	 * @return the finished
	 */
	public boolean isFinished() {
		return finished.get();
	}

	/**
	 * Sets the value of finished.
	 * @param finished the finished to set
	 */
	public void setFinished(boolean finished) {
		this.finished.set(finished);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#addParameterDefinition(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
		namedParams.addParameterDefinition(key, defValue, description);		
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#getParameterDescription(java.lang.String)
	 */
	public String getParameterDescription(String key) throws UnknownParamException {
		return namedParams.getParameterDescription(key);
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#getParameterDefault(java.lang.String)
	 */
	public String getParameterDefault(String key) throws UnknownParamException {
		return namedParams.getParameterDefault(key);
	}	

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#getParameterValue(java.lang.String)
	 */
	public String getParameterValue(String key) throws UnknownParamException {
		return namedParams.getParameterValue(key);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#listParameters()
	 */
	public String[] listParameters() {
		return namedParams.listParameters();
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#setParameterValue(java.lang.String, java.lang.String)
	 */
	public void setParameterValue(String key, String value) throws UnknownParamException {
		namedParams.setParameterValue(key, value);
	}

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#eraseParameters()
	 */
	public void eraseParameters() {
		namedParams.eraseParameters();		
	}
	
	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#clearParameters()
	 */
	public void clearParameters() {
		namedParams.clearParameters();		
	}	

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#activateParameters()
	 */
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

	/* (non-Javadoc)
	 * @see org.pentaho.di.core.parameters.NamedParams#copyParametersFrom(org.pentaho.di.core.parameters.NamedParams)
	 */
	public void copyParametersFrom(NamedParams params) {
		namedParams.copyParametersFrom(params);
	}

	/**
	 * Sets the socket repository.
	 * @param socketRepository the new socket repository
	 */
	public void setSocketRepository(SocketRepository socketRepository) {
		this.socketRepository = socketRepository;
	}
	
	/**
	 * Gets the socket repository.
	 * @return the socket repository
	 */
	public SocketRepository getSocketRepository() {
		return socketRepository;
	}
	
	/**
	 * Gets the log channel interface.
	 * @return LogChannelInterface 
	 */
	public LogChannelInterface getLogChannel() {
		return log;
	}

	/**
	 * Gets the job name.
	 * @return jobName
	 */
	public String getObjectName() {
		return getJobname();
	}
	
	/**
	 * Always returns null for Job.
	 * @return null
	 */
	public String getObjectCopy() {
		return null;
	}

	/**
	 * Gets the file name.
	 * @return the filename
	 */
	public String getFilename() {
		if (jobMeta==null) return null;
		return jobMeta.getFilename();
	}

	/**
	 * Gets the log channel id.
	 * @return the logChannelId
	 */
	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	/**
	 * Gets the jobMeta's object id.
	 * @return ObjectId 
	 */
	public ObjectId getObjectId() {
		if (jobMeta==null) return null;
		return jobMeta.getObjectId();
	}

	/**
	 * Gets the job meta's object revision.
	 * @return ObjectRevision
	 */
	public ObjectRevision getObjectRevision() {
		if (jobMeta==null) return null;
		return jobMeta.getObjectRevision();
	}

	/**
	 * Gets LoggingObjectType.JOB, which is always the value for Job.
	 * @return LoggingObjectType LoggingObjectType.JOB
	 */
	public LoggingObjectType getObjectType() {
		return LoggingObjectType.JOB;
	}

	/**
	 * Gets parent logging object.
	 * @return parentLoggingObject
	 */
	public LoggingObjectInterface getParent() {
		return parentLoggingObject;
	}

	/**
	 * Gets the job meta's repository directory interface.
	 * @return RepositoryDirectoryInterface
	 */
	public RepositoryDirectoryInterface getRepositoryDirectory() {
		if (jobMeta==null) return null;
		return jobMeta.getRepositoryDirectory();
	}
	
  /**
   * Gets the logLevel.
   * @return logLevel
   */
  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the log level.
   * @param logLevel the new log level
   */
  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
    log.setLogLevel(logLevel);
  }
	
	/**
	 * Gets the logging hierarchy.
	 * @return the logging hierarchy
	 */
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
	 * Gets the boolean value of interactive.
	 * @return the interactive
	 */
	public boolean isInteractive() {
		return interactive;
	}

	/**
	 * Sets the value of interactive.
	 * @param interactive the interactive to set
	 */
	public void setInteractive(boolean interactive) {
		this.interactive = interactive;
	}

	/**
	 * Gets the activeJobEntryTransformations.
	 * @return the activeJobEntryTransformations
	 */
	public Map<JobEntryCopy, JobEntryTrans> getActiveJobEntryTransformations() {
		return activeJobEntryTransformations;
	}

	/**
	 * Gets the activeJobEntryJobs.
	 * @return the activeJobEntryJobs
	 */
	public Map<JobEntryCopy, JobEntryJob> getActiveJobEntryJobs() {
		return activeJobEntryJobs;
	}

	/**
	 * Gets a flat list of results in THIS job, in the order of execution of job entries.
	 * @return A flat list of results in THIS job, in the order of execution of job entries
	 */
	public List<JobEntryResult> getJobEntryResults() {
		return jobEntryResults;
	}

	  /**
	   * Gets the carteObjectId.
	   * @return the carteObjectId
	   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * Sets the execution container object id (containerObjectId).
   * @param containerObjectId the execution container object id to set
   */
  public void setContainerObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Gets the parent logging object.
   * @return the parent logging object
   */
  public LoggingObjectInterface getParentLoggingObject() {
    return parentLoggingObject;
  }
  
  /**
   * Gets the registration date. For job, this always returns null
   * @return null
   */
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * Gets the start job entry copy.
   * @return the startJobEntryCopy
   */
  public JobEntryCopy getStartJobEntryCopy() {
    return startJobEntryCopy;
  }

  /**
   * Sets the start job entry copy.
   * @param startJobEntryCopy the startJobEntryCopy to set
   */
  public void setStartJobEntryCopy(JobEntryCopy startJobEntryCopy) {
    this.startJobEntryCopy = startJobEntryCopy;
  }
   
  /**
   * Gets the executing server.
   * @return the executingServer
   */
  public String getExecutingServer() {
    return executingServer;
  }

  /**
   * Sets the executing server.
   * @param executingServer the executingServer to set
   */
  public void setExecutingServer(String executingServer) {
    this.executingServer = executingServer;
  }

  /**
   * Gets the executing user.
   * @return the executingUser
   */
  public String getExecutingUser() {
    return executingUser;
  }

  /**
   * Sets the executing user.
   * @param executingUser the executingUser to set
   */
  public void setExecutingUser(String executingUser) {
    this.executingUser = executingUser;
  }
   
  @Override
  public boolean isGatheringMetrics() {
    return log!=null && log.isGatheringMetrics();
  }
  
  @Override
  public void setGatheringMetrics(boolean gatheringMetrics) {
    if (log!=null) {
      log.setGatheringMetrics(gatheringMetrics);
    }
  }
  
  @Override
  public boolean isForcingSeparateLogging() {
    return log!=null && log.isForcingSeparateLogging();
  }
  
  @Override
  public void setForcingSeparateLogging(boolean forcingSeparateLogging) {
    if (log!=null) {
      log.setForcingSeparateLogging(forcingSeparateLogging);
    }
  }
  
  /**
   * Closes database connections.  Database connections that have transactions in use by the parent job are not closed.
   */
  private void closeUniqueDatabaseConnections() {
    
    // Don't close any connections if the parent job is using the same transaction
    // 
    if (parentJob!=null && transactionId!=null && parentJob.getTransactionId()!=null && transactionId.equals(parentJob.getTransactionId())) {
      return;
    }
    
    // First we get all the database connections ...
    //
    DatabaseConnectionMap map = DatabaseConnectionMap.getInstance();
    synchronized (map) {
      List<Database> databaseList = new ArrayList<Database>(map.getMap().values());
      for (Database database : databaseList) {
        if (database.getConnectionGroup().equals(getTransactionId())) {
          try {

            // This database connection belongs to this transformation.
            // Let's roll it back if there is an error...
            //
            if (result.getNrErrors() > 0) {
              try {
                database.rollback(true);
                
                log.logBasic(BaseMessages.getString(PKG, "Job.Exception.TransactionsRolledBackOnConnection", database.toString()));
              } catch (Exception e) {
                throw new KettleDatabaseException(BaseMessages.getString(PKG, "Job.Exception.ErrorRollingBackUniqueConnection", database.toString()), e);
              }
            } else {
              try {
                database.commit(true);

                log.logBasic(BaseMessages.getString(PKG, "Job.Exception.TransactionsCommittedOnConnection", database.toString()));
              } catch (Exception e) {
                throw new KettleDatabaseException(BaseMessages.getString(PKG, "Job.Exception.ErrorCommittingUniqueConnection", database.toString()), e);
              }
            }
          } catch (Exception e) {
            log.logError(BaseMessages.getString(PKG, "Job.Exception.ErrorHandlingJobTransaction", database.toString()), e);
            result.setNrErrors(result.getNrErrors() + 1);
          } finally {
            try {
              // This database connection belongs to this transformation.
              database.closeConnectionOnly();
            } catch (Exception e) {
              log.logError(BaseMessages.getString(PKG, "Job.Exception.ErrorHandlingJobTransaction", database.toString()), e);
              result.setNrErrors(result.getNrErrors() + 1);
            } finally {
              // Remove the database from the list...
              //
              map.removeConnection(database.getConnectionGroup(), database.getPartitionId(), database);
              
              // Remove the listeners
              //
              map.removeTransactionListeners(transactionId);
            }
          }
        }
      }
      
      // Who else needs to be informed of the rollback or commit?
      //
      List<DatabaseTransactionListener> transactionListeners = map.getTransactionListeners(getTransactionId());
      if (result.getNrErrors()>0) {
        for (DatabaseTransactionListener listener : transactionListeners) {
          try {
            listener.rollback();
          } catch(Exception e) {
            log.logError(BaseMessages.getString(PKG, "Job.Exception.ErrorHandlingTransactionListenerRollback"), e);
            result.setNrErrors(result.getNrErrors() + 1);
          }
        }
      } else {
        for (DatabaseTransactionListener listener : transactionListeners) {
          try {
            listener.commit();
          } catch(Exception e) {
            log.logError(BaseMessages.getString(PKG, "Job.Exception.ErrorHandlingTransactionListenerCommit"), e);
            result.setNrErrors(result.getNrErrors() + 1);
          }
        }
      }
      
    }
  }

  /**
   * Gets the transaction id.
   * @return the transactionId
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Sets the transaction id.
   * @param transactionId the transactionId to set
   */
  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  /**
   * Calculates and returns the transaction id.  What is returned is:<br/>
   * <ul>
   *    <li>If there is a parent job using unique connections, return parent job's transaction id.  Otherwise....</li>
   *    <li>If the parent logging object is a transformation using unique connections return the transformation's transactio id.  Otherwise...</li>
   *    <li>The next available transaction id from the Database Connection Map.</li>
   * </ul>
   *   
   * @return String transaction id
   */
  public String calculateTransactionId() {
    if (getJobMeta()!=null && getJobMeta().isUsingUniqueConnections()) {
      if (parentJob!=null && parentJob.getJobMeta().isUsingUniqueConnections()) {
        return parentJob.getTransactionId();
      } else if (parentLoggingObject!=null && (parentLoggingObject instanceof Trans) && ((Trans)parentLoggingObject).getTransMeta().isUsingUniqueConnections() ){
        return ((Trans)parentLoggingObject).getTransactionId();
      } else {
        return DatabaseConnectionMap.getInstance().getNextTransactionId();
      }
    } else {
      return null;
    }
  }

  /**
   * Gets the run id.
   * @return the runId
   */
  public long getRunId() {
    return runId;
  }

  /**
   * Sets the run id.
   * @param runId the runId to set
   */
  public void setRunId(long runId) {
    this.runId = runId;
  }

  /**
   * Gets the run attempt number.
   * @return the runAttemptNr
   */
  public int getRunAttemptNr() {
    return runAttemptNr;
  }

  /**
   * Sets the run attemt number.
   * @param runAttemptNr the runAttemptNr to set
   */
  public void setRunAttemptNr(int runAttemptNr) {
    this.runAttemptNr = runAttemptNr;
  }

  /**
   * Gets the run start date.
   * @return the runStartDate
   */
  public Date getRunStartDate() {
    return runStartDate;
  }

  /**
   * Sets the run start date.
   * @param runStartDate the runStartDate to set
   */
  public void setRunStartDate(Date runStartDate) {
    this.runStartDate = runStartDate;
  }

  /**
   * Gets the checkpoint job entry.
   * @return the checkpointJobEntry
   */
  public JobEntryCopy getCheckpointJobEntry() {
    return checkpointJobEntry;
  }

  /**
   * Sets the checkpoint job entry.
   * @param checkpointJobEntry the checkpointJobEntry to set
   */
  public void setCheckpointJobEntry(JobEntryCopy checkpointJobEntry) {
    this.checkpointJobEntry = checkpointJobEntry;
  }

  /**
   * Gets the checkpoint result.
   * @return the checkpointResult
   */
  public Result getCheckpointResult() {
    return checkpointResult;
  }

  /**
   * Sets the checkpoint result
   * @param checkpointResult the checkpointResult to set
   */
  public void setCheckpointResult(Result checkpointResult) {
    this.checkpointResult = checkpointResult;
  }

  /**
   * Gets the checkpoint parameters.
   * @return the checkpointParameters
   */
  public Map<String, String> getCheckpointParameters() {
    return checkpointParameters;
  }

  /**
   * Sets the checkpoint parameters.
   * @param checkpointParameters the checkpointParameters to set
   */
  public void setCheckpointParameters(Map<String, String> checkpointParameters) {
    this.checkpointParameters = checkpointParameters;
  }

  /**
   * Returns a boolean indicating whether or not the job is ignoring checkpoints.
   * @return the ignoringCheckpoints
   */
  public boolean isIgnoringCheckpoints() {
    return ignoringCheckpoints;
  }

  /**
   * Sets whether or the not the job should be ignoring checkpoints.
   * @param ignoringCheckpoints the ignoringCheckpoints to set
   */
  public void setIgnoringCheckpoints(boolean ignoringCheckpoints) {
    this.ignoringCheckpoints = ignoringCheckpoints;
  }
}