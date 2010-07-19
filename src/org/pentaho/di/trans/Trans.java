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


package org.pentaho.di.trans;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.logging.Log4jStringAppender;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.Job;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInitThread;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.di.www.AddExportServlet;
import org.pentaho.di.www.AddTransServlet;
import org.pentaho.di.www.PrepareExecutionTransServlet;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SocketRepository;
import org.pentaho.di.www.StartExecutionTransServlet;
import org.pentaho.di.www.WebResult;


/**
 * This class is responsible for the execution of Transformations.
 * It loads, instantiates, initializes, runs, monitors the execution of the transformation contained in the TransInfo object you feed it.
 *
 * @author Matt
 * @since 07-04-2003
 *
 */
public class Trans implements VariableSpace, NamedParams
{
    public static final String REPLAY_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$
    
	private static LogWriter log = LogWriter.getInstance();
	
	/**
	 * The transformation metadata to execute
	 */
	private TransMeta transMeta;
	
	/**
	 * The repository we are referencing.
	 */
	private Repository repository;

    /** The job that's launching this transformation. This gives us access to the whole chain, including the parent variables, etc. */
    private Job parentJob;
    
    /**
     * The transformation that is executing this transformation in case of mappings.
     */
    private Trans parentTrans;
    
    /**
     * The name of the mapping step that executes this transformation in case this is a mapping 
     */
    private String mappingStepName;

	/**
	 * Indicates that we want to monitor the running transformation in a GUI
	 */
	private boolean monitored;
	
	/**
	 * Indicates that we are running in preview mode...
	 */
	private boolean preview;

	private Date      startDate, endDate, currentDate, logDate, depDate;
    private Date      jobStartDate, jobEndDate;
    
    private long      batchId;
    
    /** This is the batch ID that is passed from job to job to transformation, if nothing is passed, it's the transformation's batch id */
    private long      passedBatchId;
    
    private VariableSpace variables = new Variables();

	/**
	 * A list of all the row sets
	 */
	private List<RowSet> rowsets;

	/**
	 * A list of all the steps
	 */
	private List<StepMetaDataCombi> steps;

	public  int class_nr;

	/**
	 * The replayDate indicates that this transformation is a replay
	 * transformation for a transformation executed on replayDate. If replayDate
	 * is null, the transformation is not a replay.
	 */
	private Date replayDate;

	public final static int TYPE_DISP_1_1    = 1;
	public final static int TYPE_DISP_1_N    = 2;
	public final static int TYPE_DISP_N_1    = 3;
	public final static int TYPE_DISP_N_N    = 4;
    public final static int TYPE_DISP_N_M    = 5;

    public static final String STRING_FINISHED     = "Finished";
    public static final String STRING_RUNNING      = "Running";
    public static final String STRING_PREPARING    = "Preparing executing";
    public static final String STRING_INITIALIZING = "Initializing";
    public static final String STRING_WAITING      = "Waiting";
    public static final String STRING_STOPPED      = "Stopped";
    public static final String STRING_HALTING      = "Halting";

	public static final String	CONFIGURATION_IN_EXPORT_FILENAME	= "__job_execution_configuration__.xml";

	private boolean safeModeEnabled;

    private Log4jStringAppender stringAppender;
    
    private String threadName;
    
    private boolean preparing;
    private boolean initializing;
    private boolean running;
    private final AtomicBoolean finished;
    private AtomicBoolean paused;
    private AtomicBoolean stopped;

    private boolean readyToStart;    
    
    private Map<String,List<StepPerformanceSnapShot>> stepPerformanceSnapShots;

    private Timer stepPerformanceSnapShotTimer;
    
    private List<TransListener> transListeners;
    
    private int nrOfFinishedSteps;
    
    private NamedParams namedParams = new NamedParamsDefault();

	private SocketRepository	socketRepository;
    
	/**
	 * Initialize a transformation from transformation meta-data defined in memory
	 * @param transMeta the transformation meta-data to use.
	 */
	public Trans(TransMeta transMeta)
	{
		this.transMeta=transMeta;
        
		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationIsPreloaded")); //$NON-NLS-1$
		if (log.isDebug()) log.logDebug(toString(), Messages.getString("Trans.Log.NumberOfStepsToRun",String.valueOf(transMeta.nrSteps()) ,String.valueOf(transMeta.nrTransHops()))); //$NON-NLS-1$ //$NON-NLS-2$
		initializeVariablesFrom(transMeta);
		copyParametersFrom(transMeta);
		transMeta.activateParameters();
		
        // This is needed for e.g. database 'unique' connections.
        threadName = Thread.currentThread().getName();
        transListeners = new ArrayList<TransListener>();
        
        finished = new AtomicBoolean(false);
        paused = new AtomicBoolean(false);
        stopped = new AtomicBoolean(false);
	}

	public String getName()
	{
		if (transMeta==null) return null;

		return transMeta.getName();
	}

	public Trans(VariableSpace parentVariableSpace, Repository rep, String name, String dirname, String filename) throws KettleException
	{
		try
		{
			if (rep!=null)
			{
				RepositoryDirectory repdir = rep.getDirectoryTree().findDirectory(dirname);
				if (repdir!=null)
				{
					this.transMeta = new TransMeta(rep, name, repdir, false);
				}
				else
				{
					throw new KettleException(Messages.getString("Trans.Exception.UnableToLoadTransformation",name,dirname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			else
			{
				this.transMeta = new TransMeta(filename, false);
			}
			
			transMeta.initializeVariablesFrom(parentVariableSpace);
			initializeVariablesFrom(parentVariableSpace);
			transMeta.copyParametersFrom(this);
			transMeta.activateParameters();
			
	        // This is needed for e.g. database 'unique' connections.
	        threadName = Thread.currentThread().getName();			
		}
		catch(KettleException e)
		{
			throw new KettleException(Messages.getString("Trans.Exception.UnableToOpenTransformation",name), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		transListeners = new ArrayList<TransListener>();
		finished = new AtomicBoolean(false);
		paused = new AtomicBoolean(false);
		stopped = new AtomicBoolean(false);
	}

    /**
     * Execute this transformation.
     * @throws KettleException in case the transformation could not be prepared (initialized)
     */
    public void execute(String[] arguments) throws KettleException
    {
        prepareExecution(arguments);
        startThreads();
    }


    /**
     * Prepare the execution of the transformation.
     * @param arguments the arguments to use for this transformation
     * @throws KettleException in case the transformation could not be prepared (initialized)
     */
    public void prepareExecution(String[] arguments) throws KettleException
    {
        preparing=true;
		startDate = null;
        running = false;

		//
		// Set the arguments on the transformation...
		//
		if (arguments!=null) transMeta.setArguments(arguments);
		
		activateParameters();
		transMeta.activateParameters();

		if (transMeta.getName()==null)
		{
			if (transMeta.getFilename()!=null)
			{
				log.logBasic(toString(), Messages.getString("Trans.Log.DispacthingStartedForFilename",transMeta.getFilename())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		{
			log.logBasic(toString(), Messages.getString("Trans.Log.DispacthingStartedForTransformation",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (transMeta.getArguments()!=null)
		{
		    if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.NumberOfArgumentsDetected", String.valueOf(transMeta.getArguments().length) )); //$NON-NLS-1$
		}

		if (isSafeModeEnabled())
		{
		    if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.SafeModeIsEnabled",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (getReplayDate() != null) {
			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(toString(), Messages.getString("Trans.Log.ThisIsAReplayTransformation") //$NON-NLS-1$
					+ df.format(getReplayDate()));
		} else {
		    if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.ThisIsNotAReplayTransformation")); //$NON-NLS-1$
		}

		// setInternalKettleVariables(this);  --> Let's not do this, when running without file, for example remote, it spoils the fun
		
		// Keep track of all the row sets and allocated steps
		//
		steps	 = new ArrayList<StepMetaDataCombi>();
		rowsets	 = new ArrayList<RowSet>();

		//
		// Sort the steps & hops for visual pleasure...
		//
		if (isMonitored())
		{
			transMeta.sortStepsNatural();
		}

		List<StepMeta> hopsteps=transMeta.getTransHopSteps(false);

		if(log.isDetailed()) 
		{
			log.logDetailed(toString(), Messages.getString("Trans.Log.FoundDefferentSteps",String.valueOf(hopsteps.size())));	 //$NON-NLS-1$ //$NON-NLS-2$
			log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatingRowsets")); //$NON-NLS-1$
		}
		// First allocate all the rowsets required!
		// Note that a mapping doesn't receive ANY input or output rowsets...
		//
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta thisStep=hopsteps.get(i);
			if (thisStep.isMapping()) continue; // handled and allocated by the mapping step itself.
			
			if(log.isDetailed()) 
				log.logDetailed(toString(), Messages.getString("Trans.Log.AllocateingRowsetsForStep",String.valueOf(i),thisStep.getName())); //$NON-NLS-1$ //$NON-NLS-2$

			List<StepMeta> nextSteps = transMeta.findNextSteps(thisStep);
			int nrTargets = nextSteps.size();

			for (int n=0;n<nrTargets;n++)
			{
				// What's the next step?
				StepMeta nextStep = nextSteps.get(n);
				if (nextStep.isMapping()) continue; // handled and allocated by the mapping step itself.
				
                // How many times do we start the source step?
                int thisCopies = thisStep.getCopies();

                // How many times do we start the target step?
                int nextCopies = nextStep.getCopies();
                
                int nrCopies;
                if(log.isDetailed()) 
                	log.logDetailed(toString(), Messages.getString("Trans.Log.copiesInfo",String.valueOf(thisCopies),String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$
				int dispatchType;
				     if (thisCopies==1 && nextCopies==1) { dispatchType=TYPE_DISP_1_1; nrCopies = 1; }
				else if (thisCopies==1 && nextCopies >1) { dispatchType=TYPE_DISP_1_N; nrCopies = nextCopies; }
				else if (thisCopies >1 && nextCopies==1) { dispatchType=TYPE_DISP_N_1; nrCopies = thisCopies; }
				else if (thisCopies==nextCopies)         { dispatchType=TYPE_DISP_N_N; nrCopies = nextCopies; } // > 1!
				else                                     { dispatchType=TYPE_DISP_N_M; nrCopies = nextCopies; } // Allocate a rowset for each destination step

				// Allocate the rowsets
				//
                if (dispatchType!=TYPE_DISP_N_M)
                {
    				for (int c=0;c<nrCopies;c++)
    				{
    					RowSet rowSet=new RowSet(transMeta.getSizeRowset());
    					switch(dispatchType)
    					{
    					case TYPE_DISP_1_1: rowSet.setThreadNameFromToCopy(thisStep.getName(), 0, nextStep.getName(), 0); break;
    					case TYPE_DISP_1_N: rowSet.setThreadNameFromToCopy(thisStep.getName(), 0, nextStep.getName(), c); break;
    					case TYPE_DISP_N_1: rowSet.setThreadNameFromToCopy(thisStep.getName(), c, nextStep.getName(), 0); break;
    					case TYPE_DISP_N_N: rowSet.setThreadNameFromToCopy(thisStep.getName(), c, nextStep.getName(), c); break;
                        }
    					rowsets.add(rowSet);
    					if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.TransformationAllocatedNewRowset",rowSet.toString())); //$NON-NLS-1$ //$NON-NLS-2$
    				}
                }
                else
                {
                    // For each N source steps we have M target steps
                    //
                    // From each input step we go to all output steps.
                    // This allows maximum flexibility for re-partitioning, distribution...
                    for (int s=0;s<thisCopies;s++)
                    {
                        for (int t=0;t<nextCopies;t++)
                        {
                            RowSet rowSet=new RowSet(transMeta.getSizeRowset());
                            rowSet.setThreadNameFromToCopy(thisStep.getName(), s, nextStep.getName(), t);
                            rowsets.add(rowSet);
                            if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.TransformationAllocatedNewRowset",rowSet.toString())); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
			}
			log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatedRowsets",String.valueOf(rowsets.size()),String.valueOf(i),thisStep.getName())+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.AllocatingStepsAndStepData")); //$NON-NLS-1$
        
		// Allocate the steps & the data...
		//
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta stepMeta=hopsteps.get(i);
			String stepid = stepMeta.getStepID();

			if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationIsToAllocateStep",stepMeta.getName(),stepid)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// How many copies are launched of this step?
			int nrCopies=stepMeta.getCopies();

            if (log.isDebug()) log.logDebug(toString(), Messages.getString("Trans.Log.StepHasNumberRowCopies",String.valueOf(nrCopies))); //$NON-NLS-1$

			// At least run once...
			for (int c=0;c<nrCopies;c++)
			{
				// Make sure we haven't started it yet!
				if (!hasStepStarted(stepMeta.getName(), c))
				{
					StepMetaDataCombi combi = new StepMetaDataCombi();

					combi.stepname = stepMeta.getName();
					combi.copy     = c;

					// The meta-data
                    combi.stepMeta = stepMeta;
					combi.meta = stepMeta.getStepMetaInterface();

					// Allocate the step data
					StepDataInterface data = combi.meta.getStepData();
					combi.data = data;

					// Allocate the step
					StepInterface step=combi.meta.getStep(stepMeta, data, c, transMeta, this);
                    
					// Copy the variables of the transformation to the step...
					// don't share. Each copy of the step has its own variables.
					// 
					((BaseStep)step).initializeVariablesFrom(this);
					((BaseStep)step).setUsingThreadPriorityManagment(transMeta.isUsingThreadPriorityManagment());
					
                    // If the step is partitioned, set the partitioning ID and some other things as well...
                    if (stepMeta.isPartitioned())
                    {
                    	List<String> partitionIDs = stepMeta.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
                        if (partitionIDs!=null && partitionIDs.size()>0) 
                        {
                        	step.setPartitionID(partitionIDs.get(c)); // Pass the partition ID to the step
                        }
                    }

					// Possibly, enable safe mode in the steps...
					((BaseStep)step).setSafeModeEnabled(safeModeEnabled);

					// Save the step too
					combi.step = step;

					// Add to the bunch...
					steps.add(combi);

					if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationHasAllocatedANewStep",stepMeta.getName(),String.valueOf(c))); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
        
        // Now we need to verify if certain rowsets are not meant to be for error handling...
        // Loop over the steps and for every step verify the output rowsets
        // If a rowset is going to a target step in the steps error handling metadata, set it to the errorRowSet.
        // The input rowsets are already in place, so the next step just accepts the rows.
        // Metadata wise we need to do the same trick in TransMeta
        //
        for (int s=0;s<steps.size();s++)
        {
            StepMetaDataCombi combi = steps.get(s);
            if (combi.stepMeta.isDoingErrorHandling())
            {
                StepErrorMeta stepErrorMeta = combi.stepMeta.getStepErrorMeta();
                BaseStep baseStep = (BaseStep)combi.step;
                boolean stop=false;
                for (int rowsetNr=0;rowsetNr<baseStep.outputRowSets.size() && !stop;rowsetNr++)
                {
                    RowSet outputRowSet = baseStep.outputRowSets.get(rowsetNr);
                    if (outputRowSet.getDestinationStepName().equalsIgnoreCase(stepErrorMeta.getTargetStep().getName()))
                    {
                        // This is the rowset to move!
                        baseStep.errorRowSet = outputRowSet;
                        baseStep.outputRowSets.remove(rowsetNr);
                        stop=true;
                    }
                }
            }
        }

		// Now (optionally) write start log record!
		beginProcessing();

        // Set the partition-to-rowset mapping
        //
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);

            StepMeta stepMeta = sid.stepMeta;
            BaseStep baseStep = (BaseStep)sid.step;

            baseStep.setPartitioned(stepMeta.isPartitioned());
            
            // Now let's take a look at the source and target relation
            //
            // If this source step is not partitioned, and the target step is: it means we need to re-partition the incoming data.
            // If both steps are partitioned on the same method and schema, we don't need to re-partition
            // If both steps are partitioned on a different method or schema, we need to re-partition as well.
            // If both steps are not partitioned, we don't need to re-partition
            //
            boolean isThisPartitioned = stepMeta.isPartitioned();
            PartitionSchema thisPartitionSchema = null;
            if (isThisPartitioned) thisPartitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
            
            boolean isNextPartitioned = false;
            StepPartitioningMeta nextStepPartitioningMeta = null;
            PartitionSchema nextPartitionSchema = null;

            List<StepMeta> nextSteps = transMeta.findNextSteps(stepMeta);
            int nrNext = nextSteps.size();
	        for (int p=0;p<nrNext;p++)
	        {
	            StepMeta nextStep = nextSteps.get(p);
	            if (nextStep.isPartitioned()) 
	            {
	            	isNextPartitioned = true;
	            	nextStepPartitioningMeta = nextStep.getStepPartitioningMeta(); 
	                nextPartitionSchema = nextStepPartitioningMeta.getPartitionSchema();
	            }
	        }
            
            baseStep.setRepartitioning(StepPartitioningMeta.PARTITIONING_METHOD_NONE);
            
        	// If the next step is partitioned differently, set re-partitioning, when running locally.
        	//
            if ( (!isThisPartitioned && isNextPartitioned ) || (isThisPartitioned && isNextPartitioned && !thisPartitionSchema.equals(nextPartitionSchema)) ) {
            	baseStep.setRepartitioning(nextStepPartitioningMeta.getMethodType());
            }
        }

        preparing=false;
        initializing = true;

        if (log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.InitialisingSteps", String.valueOf(steps.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        StepInitThread initThreads[] = new StepInitThread[steps.size()];
        Thread[] threads = new Thread[steps.size()];

        // Grab the error code in case we're doing a preview
        // In that case we're going to propagate the log to the exception...
        //
        Log4jStringAppender initAppender = null;
        if (preview) {
        	initAppender = LogWriter.createStringAppender();
        	log.addAppender(initAppender);
        }
        
        // Initialize all the threads...
        //
		for (int i=0;i<steps.size();i++)
		{
			final StepMetaDataCombi sid=steps.get(i);
            
            // Do the init code in the background!
            // Init all steps at once, but ALL steps need to finish before we can continue properly!
			initThreads[i] = new StepInitThread(sid, log);
            
            // Put it in a separate thread!
			threads[i] = new Thread(initThreads[i]);
            threads[i].setName("init of "+sid.stepname+"."+sid.copy+" ("+threads[i].getName()+")");
            threads[i].start();
		}
        
        for (int i=0; i < threads.length;i++)
        {
            try {
                threads[i].join();
            } catch(Exception ex) {
                log.logError("Error with init thread: " + ex.getMessage(), ex.getMessage());
                log.logError(toString(), Const.getStackTracker(ex));
            }
        }
        
        initializing=false;
        boolean ok = true;
        
        // All step are initialized now: see if there was one that didn't do it correctly!
        for (int i=0;i<initThreads.length;i++)
        {
            StepMetaDataCombi combi = initThreads[i].getCombi();
            if (!initThreads[i].isOk()) 
            {
                log.logError(toString(), Messages.getString("Trans.Log.StepFailedToInit", combi.stepname+"."+combi.copy));
                combi.data.setStatus(StepDataInterface.STATUS_STOPPED);
                ok=false;
            }
            else
            {
                combi.data.setStatus(StepDataInterface.STATUS_IDLE);
                if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.StepInitialized", combi.stepname+"."+combi.copy));
            }
        }
        
        // That's all the log we need
        //
        if (initAppender!=null) {
        	log.removeAppender(initAppender);
        }
        
		if (!ok)
		{
            // Halt the other threads as well, signal end-of-the line to the outside world...
            // Also explicitly call dispose() to clean up resources opened during init();
            //
            for (int i=0;i<initThreads.length;i++)
            {
                StepMetaDataCombi combi = initThreads[i].getCombi();
                
                // Dispose will overwrite the status, but we set it back right after this.
                combi.step.dispose(combi.meta, combi.data);
                
                if (initThreads[i].isOk()) 
                {
                    combi.data.setStatus(StepDataInterface.STATUS_HALTED);
                }
                else
                {
                    combi.data.setStatus(StepDataInterface.STATUS_STOPPED);
                }
            }
            
            // Just for safety, fire the trans finished listeners...
            //
            fireTransFinishedListeners();
            
            // Flag the transformation as finished
            //
            finished.set(true);
            
            // Pass along the log during preview.  Otherwise it becomes hard to see what went wrong.
            //
            if (initAppender!=null) {
            	throw new KettleException(Messages.getString("Trans.Log.FailToInitializeAtLeastOneStep")+Const.CR+initAppender.getBuffer()); //$NON-NLS-1
            } else {
            	throw new KettleException(Messages.getString("Trans.Log.FailToInitializeAtLeastOneStep")+Const.CR); //$NON-NLS-1
            }
		}
        
        readyToStart=true;
	}

    /**
     * Start the threads prepared by prepareThreads();
     * Before you start the threads, you can add RowListeners to them.
     * @throws KettleException in case there is a communication error with a remote output socket.
     */
    public void startThreads() throws KettleException
    {
        // Now prepare to start all the threads...
    	// 
    	nrOfFinishedSteps=0;
    	
        for (int i=0;i<steps.size();i++)
        {
            final StepMetaDataCombi sid = steps.get(i);
            sid.step.markStart();
            sid.step.initBeforeStart();
            
            // also attach a Step Listener to detect when we're done...
            //
            StepListener stepListener = new StepListener() 
	            {
					public void stepFinished(Trans trans, StepMeta stepMeta, StepInterface step) {
						synchronized (Trans.this) {
							nrOfFinishedSteps++;
													
							if (nrOfFinishedSteps>=steps.size()) {						
								// Set the finished flag
								//
								finished.set(true);
								
								// Grab the performance statistics one last time (if enabled)
								//
								addStepPerformanceSnapShot();
								
								fireTransFinishedListeners();
							}
							
							// If a step fails with an error, we want to kill/stop the others too...
							//
							if (step.getErrors()>0) {
	
								log.logMinimal(getName(), Messages.getString("Trans.Log.TransformationDetectedErrors")); //$NON-NLS-1$ //$NON-NLS-2$
								log.logMinimal(getName(), Messages.getString("Trans.Log.TransformationIsKillingTheOtherSteps")); //$NON-NLS-1$
	
								killAllNoWait();
							}
						}
					}
				};
			sid.step.addStepListener(stepListener);
        }

    	if (transMeta.isCapturingStepPerformanceSnapShots()) 
    	{
    		stepPerformanceSnapShots = new ConcurrentHashMap<String, List<StepPerformanceSnapShot>>();
    		
    		// Set a timer to collect the performance data from the running threads...
    		//
    		stepPerformanceSnapShotTimer = new Timer();
    		TimerTask timerTask = new TimerTask() {
				public void run() {
					addStepPerformanceSnapShot();
				}
			};
    		stepPerformanceSnapShotTimer.schedule(timerTask, 100, transMeta.getStepPerformanceCapturingDelay());
    	}
    	
        // Now start a thread to monitor the running transformation...
        //
        finished.set(false);
        paused.set(false);
        stopped.set(false);
        
		TransListener transListener = new TransListener() {
				public void transFinished(Trans trans) {
					
					// First of all, stop the performance snapshot timer if there is is one...
					//
					if (transMeta.isCapturingStepPerformanceSnapShots() && stepPerformanceSnapShotTimer!=null) 
					{
						stepPerformanceSnapShotTimer.cancel();
					}
					
					finished.set(true);
					running=false; // no longer running
				}
			};
		addTransListener(transListener);
		
        running=true;
        
        // Now start all the threads...
    	//
        for (int i=0;i<steps.size();i++)
        {
            steps.get(i).step.start();
        }
        
        if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.TransformationHasAllocated",String.valueOf(steps.size()),String.valueOf(rowsets.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    /**
     * 	Fire the listeners (if any are registered)
	 *	
     */
    protected void fireTransFinishedListeners() {
    	
		for (TransListener transListener : transListeners)
		{
			transListener.transFinished(this);
		}

	}

	protected void addStepPerformanceSnapShot() {
    	if (transMeta.isCapturingStepPerformanceSnapShots())
    	{
	        // get the statistics from the steps and keep them...
	    	//
	        for (int i=0;i<steps.size();i++)
	        {
	            StepMeta stepMeta = steps.get(i).stepMeta;
	            StepInterface step = steps.get(i).step;
	            BaseStep baseStep = (BaseStep)step;
	            
	            StepPerformanceSnapShot snapShot = new StepPerformanceSnapShot(
	            		new Date(),
	            		stepMeta.getName(),
	            		step.getCopy(),
	            		step.getLinesRead(),
	            		step.getLinesWritten(),
	            		step.getLinesInput(),
	            		step.getLinesOutput(),
	            		step.getLinesUpdated(),
	            		step.getLinesRejected(),
	            		step.getErrors()
	            		);
	            List<StepPerformanceSnapShot> snapShotList = stepPerformanceSnapShots.get(step.toString());
	            StepPerformanceSnapShot previous;
	            if (snapShotList==null) {
	            	snapShotList = new ArrayList<StepPerformanceSnapShot>();
	            	stepPerformanceSnapShots.put(step.toString(), snapShotList);
	            	previous = null;
	            }
	            else {
	            	previous = snapShotList.get(snapShotList.size()-1); // the last one...
	            }
	            // Make the difference...
	            //
	            snapShot.diff(previous, baseStep.rowsetInputSize(), baseStep.rowsetOutputSize());
	            snapShotList.add(snapShot);
	        }
    	}
	}

	/**
     * Call this method after the transformation has finished.
     * Typically, after ALL the slave transformations in a clustered run have finished.
     */
    public void cleanup()
    {
    	// Close all open server sockets.
    	// We can only close these after all processing has been confirmed to be finished.
    	//
    	if (steps==null) return;
    	
    	for (StepMetaDataCombi combi : steps) {
				combi.step.cleanup();
    	}
    }

	public void logSummary(StepInterface si)
	{
		log.logBasic(si.getStepname(), Messages.getString("Trans.Log.FinishedProcessing",String.valueOf(si.getLinesInput()),String.valueOf(si.getLinesOutput()),String.valueOf(si.getLinesRead()))+Messages.getString("Trans.Log.FinishedProcessing2",String.valueOf(si.getLinesWritten()),String.valueOf(si.getLinesUpdated()),String.valueOf(si.getErrors()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}

	//
	// Wait until all RunThreads have finished.
	//
	public void waitUntilFinished()
	{
		// We do this the simple way: we attach a transformation listener to this transformation...
		//
		try
		{
			while (!finished.get())
			{
				Thread.sleep(0,1); // sleep a very short while
			}
		}
		catch(Exception e)
		{
			log.logError(toString(), Messages.getString("Trans.Log.TransformationError")+e.toString()); //$NON-NLS-1$
            log.logError(toString(), Const.getStackTracker(e)); //$NON-NLS-1$
		}
	}

	public int getErrors()
	{
        if (steps==null) return 0;

		int errors=0;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			if (sid.step.getErrors()!=0L) errors++;
		}
		if (errors>0) log.logError(toString(), Messages.getString("Trans.Log.TransformationErrorsDetected")); //$NON-NLS-1$

		return errors;
	}

	public int getEnded()
	{
		int nrEnded=0;

		if (steps==null) return 0;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			
            BaseStep thr=(BaseStep)sid.step;
            StepDataInterface data = sid.data;
            
			if ((thr!=null && !thr.isAlive()) ||  // Should normally not be needed anymore, status is kept in data.
                    data.getStatus()==StepDataInterface.STATUS_FINISHED || // Finished processing 
                    data.getStatus()==StepDataInterface.STATUS_HALTED ||   // Not launching because of init error
                    data.getStatus()==StepDataInterface.STATUS_STOPPED     // Stopped because of an error
                    )
            {
                nrEnded++;
            }
		}

		return nrEnded;
	}


	public boolean isFinished()
	{
		return finished.get();
	}

	public void killAll()
	{
		if (steps==null) return;
		
		int nrStepsFinished = 0;
		
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep thr = (BaseStep)sid.step;

			if (log.isDebug()) log.logDebug(toString(), Messages.getString("Trans.Log.LookingAtStep")+thr.getStepname()); //$NON-NLS-1$
			
			// If thr is a mapping, this is cause for an endless loop
			//
			while (thr.isAlive())
			{
				thr.stopAll();
				try
				{
					Thread.sleep(20);
				}
				catch(Exception e)
				{
					log.logError(toString(), Messages.getString("Trans.Log.TransformationErrors")+e.toString()); //$NON-NLS-1$
					return;
				}
			}
			
			if (!thr.isAlive()) nrStepsFinished++;
		}
		
		if (nrStepsFinished==steps.size()) finished.set(true);
	}
	
	/**
	 * Ask all steps to stop but don't wait around for it to happen.
	 * Special method for use with mappings.
	 */
	private void killAllNoWait()
	{
		if (steps==null) return;
		
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep thr = (BaseStep)sid.step;

			if (log.isDebug()) log.logDebug(toString(), Messages.getString("Trans.Log.LookingAtStep")+thr.getStepname()); //$NON-NLS-1$
			
			thr.stopAll();
			try
			{
				Thread.sleep(20);
			}
			catch(Exception e)
			{
				log.logError(toString(), Messages.getString("Trans.Log.TransformationErrors")+e.toString()); //$NON-NLS-1$
				return;
			}
		}
	}

	public void printStats(int seconds)
	{
		int i;
		BaseStep thr;
		long proc;

		log.logBasic(toString(), " "); //$NON-NLS-1$
		if (steps==null) return;

		for (i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			thr=(BaseStep)sid.step;
			proc=thr.getProcessed();
			if (seconds!=0)
			{
				if (thr.getErrors()==0)
				{
					log.logBasic(toString(), Messages.getString("Trans.Log.ProcessSuccessfullyInfo",thr.getStepname(),"."+thr.getCopy(),String.valueOf(proc),String.valueOf((proc/seconds)))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(toString(), Messages.getString("Trans.Log.ProcessErrorInfo",thr.getStepname(),"."+thr.getCopy(),String.valueOf(thr.getErrors()),String.valueOf(proc),String.valueOf(proc/seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
			}
			else
			{
				if (thr.getErrors()==0)
				{
					log.logBasic(toString(), Messages.getString("Trans.Log.ProcessSuccessfullyInfo",thr.getStepname(),"."+thr.getCopy(),String.valueOf(proc),seconds!=0 ? String.valueOf((proc/seconds)) : "-")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(toString(), Messages.getString("Trans.Log.ProcessErrorInfo2",thr.getStepname(),"."+thr.getCopy(),String.valueOf(thr.getErrors()),String.valueOf(proc),String.valueOf(seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
			}
		}
	}

	public long getLastProcessed()
	{
		BaseStep thr;

		if (steps==null) return 0L;

		int i=steps.size()-1;

		if (i<=0) return 0L;

		StepMetaDataCombi sid = steps.get(i);
		thr=(BaseStep)sid.step;

		return thr.getProcessed();
	}

	//
	// Find the RowSet of a step-name.
	//
	public RowSet findRowSet(String rowsetname)
	{
		// Start with the transformation.
		for (int i=0;i<rowsets.size();i++)
		{
			//log.logDetailed("DIS: looking for RowSet ["+rowsetname+"] in nr "+i+" of "+threads.size()+" threads...");
			RowSet rs=rowsets.get(i);
			if (rs.getName().equalsIgnoreCase(rowsetname)) return rs;
		}

		return null;
	}

	//
	// Find the RowSet of a step-name.
	//
	public RowSet findRowSet(String from, int fromcopy, String to, int tocopy)
	{
		// Start with the transformation.
		for (int i=0;i<rowsets.size();i++)
		{
			RowSet rs=rowsets.get(i);
			if (rs.getOriginStepName().equalsIgnoreCase(from) &&
			    rs.getDestinationStepName().equalsIgnoreCase(to) &&
			    rs.getOriginStepCopy() == fromcopy &&
			    rs.getDestinationStepCopy()   == tocopy
			    ) return rs;
		}

		return null;
	}


	//
	// Find a step by name: if it is running, return true.
	//
	public boolean hasStepStarted(String sname, int copy)
	{
		//log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
		//log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			boolean started=(sid.stepname!=null && sid.stepname.equalsIgnoreCase(sname)) && sid.copy==copy;
			if (started) return true;
		}
		return false;
	}

	//
	// Ask all steps to stop running.
	//
	public void stopAll()
	{
		if (steps==null) return;
		
		//log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
		//log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep rt=(BaseStep)sid.step;
			rt.setStopped(true);
			rt.setPaused(false);

			// Cancel queries etc. by force...
			StepInterface si = (StepInterface)rt;
            try
            {
                si.stopRunning(sid.meta, sid.data);
            }
            catch(Exception e)
            {
                log.logError(toString(), "Something went wrong while trying to stop the transformation: "+e.toString());
                log.logError(toString(), Const.getStackTracker(e));
            }
            
            sid.data.setStatus(StepDataInterface.STATUS_STOPPED);
		}
		
		//if it is stopped it is not paused
		paused.set(false);
		stopped.set(true);
	}

	public int nrSteps()
	{
		if (steps==null) return 0;
		return steps.size();
	}

	public int nrActiveSteps()
	{
		if (steps==null) return 0;

		int nr = 0;
		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			if ( sid.step.isAlive() ) nr++;
		}
		return nr;
	}

	public BaseStep getRunThread(int i)
	{
		if (steps==null) return null;
		StepMetaDataCombi sid = steps.get(i);
		return (BaseStep)sid.step;
	}

	public BaseStep getRunThread(String name, int copy)
	{
		if (steps==null) return null;

		int i;

		for( i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			if (rt.getStepname().equalsIgnoreCase(name) && rt.getCopy()==copy)
			{
				return rt;
			}
		}

		return null;
	}

	//
	// Handle logging at start
	public void beginProcessing() throws KettleTransException
	{
		try
		{
			// if (preview) return true;

			currentDate = new Date();
			logDate     = new Date();
			startDate   = Const.MIN_DATE;
			endDate     = currentDate;
			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(toString(), Messages.getString("Trans.Log.TransformationCanBeReplayed") + df.format(currentDate)); //$NON-NLS-1$
			boolean lockedTable = false;
			
            Database ldb = null;
            DatabaseMeta logcon = transMeta.getLogConnection();
			
            try
            {
            	if (logcon!=null)
    			{
    				if ( transMeta.getLogTable() == null )
    				{
    				    // It doesn't make sense to start database logging without a table
    					// to log to.
    					throw new KettleTransException(Messages.getString("Trans.Exception.NoLogTableDefined")); //$NON-NLS-1$ //$NON-NLS-2$
    				}
    				
    			    ldb = new Database(logcon);
    			    ldb.shareVariablesWith(this);
    			    if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.OpeningLogConnection",""+transMeta.getLogConnection())); //$NON-NLS-1$ //$NON-NLS-2$
					ldb.connect();

					// Use transactions!
					ldb.setCommit(100);
					
					// See if we have to add a batch id...
					// Do this first, before anything else to lock the complete table exclusively
					//
					if (transMeta.isBatchIdUsed())
					{
						// Make sure we lock the logging table!
						//
						ldb.lockTables( new String[] { transMeta.getLogTable(), } );
						lockedTable=true;
						
						// Now insert value -1 to create a real write lock blocking the other requests.. FCFS
						//
						String sql = "INSERT INTO "+logcon.quoteField(transMeta.getLogTable())+"("+logcon.quoteField("ID_BATCH")+") values (-1)";
						ldb.execStatement(sql);
						
						
						// Now this next lookup will stall on the other connections
						//
						Long id_batch = ldb.getNextValue(transMeta.getCounters(), transMeta.getLogTable(), "ID_BATCH");
						setBatchId( id_batch.longValue() );
					}

					//
					// Get the date range from the logging table: from the last end_date to now. (currentDate)
					//
                    Object[] lastr= ldb.getLastLogDate(transMeta.getLogTable(), transMeta.getName(), false, Messages.getString("Trans.Row.Status.End")); //$NON-NLS-1$
					if (lastr!=null && lastr.length>0)
					{
                        startDate = (Date) lastr[0]; 
                        if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.StartDateFound")+startDate); //$NON-NLS-1$
					}

					//
					// OK, we have a date-range.
					// However, perhaps we need to look at a table before we make a final judment?
					//
					if (transMeta.getMaxDateConnection()!=null &&
						transMeta.getMaxDateTable()!=null && transMeta.getMaxDateTable().length()>0 &&
						transMeta.getMaxDateField()!=null && transMeta.getMaxDateField().length()>0
						)
					{
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.LookingForMaxdateConnection",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
						DatabaseMeta maxcon = transMeta.getMaxDateConnection();
						if (maxcon!=null)
						{
							Database maxdb = new Database(maxcon);
							maxdb.shareVariablesWith(this);
							try
							{
								if(log.isDetailed())  log.logDetailed(toString(), Messages.getString("Trans.Log.OpeningMaximumDateConnection")); //$NON-NLS-1$
								maxdb.connect();

								//
								// Determine the endDate by looking at a field in a table...
								//
								String sql = "SELECT MAX("+transMeta.getMaxDateField()+") FROM "+transMeta.getMaxDateTable(); //$NON-NLS-1$ //$NON-NLS-2$
								RowMetaAndData r1 = maxdb.getOneRow(sql);
								if (r1!=null)
								{
									// OK, we have a value, what's the offset?
									Date maxvalue = r1.getRowMeta().getDate(r1.getData(), 0);
									if (maxvalue!=null)
									{
										if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.LastDateFoundOnTheMaxdateConnection")+r1); //$NON-NLS-1$
										endDate.setTime( (long)( maxvalue.getTime() + ( transMeta.getMaxDateOffset()*1000 ) ));
									}
								}
								else
								{
									if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.NoLastDateFoundOnTheMaxdateConnection")); //$NON-NLS-1$
								}
							}
							catch(KettleException e)
							{
								throw new KettleTransException(Messages.getString("Trans.Exception.ErrorConnectingToDatabase",""+transMeta.getMaxDateConnection()), e); //$NON-NLS-1$ //$NON-NLS-2$
							}
							finally
							{
								maxdb.disconnect();
							}
						}
						else
						{
							throw new KettleTransException(Messages.getString("Trans.Exception.MaximumDateConnectionCouldNotBeFound",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}

					// Determine the last date of all dependend tables...
					// Get the maximum in depdate...
					if (transMeta.nrDependencies()>0)
					{
						if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.CheckingForMaxDependencyDate")); //$NON-NLS-1$
						//
						// Maybe one of the tables where this transformation is dependent on has changed?
						// If so we need to change the start-date!
						//
						depDate = Const.MIN_DATE;
						Date maxdepdate = Const.MIN_DATE;
						if (lastr!=null && lastr.length>0)
						{
							Date dep = (Date) lastr[1]; // #1: last depdate
							if (dep!=null)
							{
								maxdepdate = dep;
								depDate    = dep;
							}
						}

						for (int i=0;i<transMeta.nrDependencies();i++)
						{
							TransDependency td = transMeta.getDependency(i);
							DatabaseMeta depcon = td.getDatabase();
							if (depcon!=null)
							{
								Database depdb = new Database(depcon);
								try
								{
									depdb.connect();

									String sql = "SELECT MAX("+td.getFieldname()+") FROM "+td.getTablename(); //$NON-NLS-1$ //$NON-NLS-2$
									RowMetaAndData r1 = depdb.getOneRow(sql);
									if (r1!=null)
									{
										// OK, we have a row, get the result!
										Date maxvalue = (Date) r1.getData()[0];
										if (maxvalue!=null)
										{
											if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.FoundDateFromTable",td.getTablename(),"."+td.getFieldname()," = "+maxvalue.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
											if ( maxvalue.getTime() > maxdepdate.getTime() )
											{
												maxdepdate=maxvalue;
											}
										}
										else
										{
											throw new KettleTransException(Messages.getString("Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
										}
									}
									else
									{
										throw new KettleTransException(Messages.getString("Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									}
								}
								catch(KettleException e)
								{
									throw new KettleTransException(Messages.getString("Trans.Exception.ErrorInDatabase",""+td.getDatabase()), e); //$NON-NLS-1$ //$NON-NLS-2$
								}
								finally
								{
									depdb.disconnect();
								}
							}
							else
							{
								throw new KettleTransException(Messages.getString("Trans.Exception.ConnectionCouldNotBeFound",""+td.getDatabase())); //$NON-NLS-1$ //$NON-NLS-2$
							}
							if(log.isDetailed()) log.logDetailed(toString(), Messages.getString("Trans.Log.Maxdepdate")+(XMLHandler.date2string(maxdepdate))); //$NON-NLS-1$ //$NON-NLS-2$
						}

						// OK, so we now have the maximum depdate;
						// If it is larger, it means we have to read everything back in again.
						// Maybe something has changed that we need!
						//
						if (maxdepdate.getTime() > depDate.getTime())
						{
							depDate = maxdepdate;
							startDate = Const.MIN_DATE;
						}
					}
					else
					{
						depDate = currentDate;
					}
				}

                // OK, now we have a date-range.  See if we need to set a maximum!
                if (transMeta.getMaxDateDifference()>0.0 && // Do we have a difference specified?
                    startDate.getTime() > Const.MIN_DATE.getTime() // Is the startdate > Minimum?
                    )
                {
                    // See if the end-date is larger then Start_date + DIFF?
                    Date maxdesired = new Date( startDate.getTime()+((long)transMeta.getMaxDateDifference()*1000) );

                    // If this is the case: lower the end-date. Pick up the next 'region' next time around.
                    // We do this to limit the workload in a single update session (e.g. for large fact tables)
                    //
                    if ( endDate.compareTo( maxdesired )>0) endDate = maxdesired;
                }

                if (Const.isEmpty(transMeta.getName()) && logcon!=null && transMeta.getLogTable()!=null)
                {
                    throw new KettleException(Messages.getString("Trans.Exception.NoTransnameAvailableForLogging"));
                }

                
                if (logcon!=null && transMeta.getLogTable()!=null && transMeta.getName()!=null)
                {
                    ldb.writeLogRecord(
                               transMeta.getLogTable(),
                               transMeta.isBatchIdUsed(),
                               getBatchId(),
                               false,
                               transMeta.getName(),
                               Database.LOG_STATUS_START,  //$NON-NLS-1$
                               0L, 0L, 0L, 0L, 0L, 0L,
                               startDate, endDate, logDate, depDate,currentDate,
                               null
                             );
                }
           }
			catch(KettleException e)
			{
				throw new KettleTransException(Messages.getString("Trans.Exception.ErrorWritingLogRecordToTable",transMeta.getLogTable()), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			finally
			{
				if (lockedTable) {
                	// Remove the -1 record again...
                	//
					String sql = "DELETE FROM "+logcon.quoteField(transMeta.getLogTable())+" WHERE "+logcon.quoteField("ID_BATCH")+"= -1";
					ldb.execStatement(sql);
					
                	ldb.unlockTables( new String[] { transMeta.getLogTable(), } );
                }
				if (ldb!=null) ldb.disconnect();
			}

            if (transMeta.isLogfieldUsed())
            {
                stringAppender = LogWriter.createStringAppender();
                
                // Set a max number of lines to prevent out of memory errors...
                //
                String logLimit = environmentSubstitute(transMeta.getLogSizeLimit());
                if (Const.isEmpty(logLimit)) {
                	logLimit = environmentSubstitute(Const.KETTLE_LOG_SIZE_LIMIT);
                }
                stringAppender.setMaxNrLines(Const.toInt(logLimit,0));
                
                log.addAppender(stringAppender);
                stringAppender.setBuffer(new StringBuffer(Messages.getString("Trans.Log.Start")+Const.CR));
            }
		}
		catch(KettleException e)
		{
			throw new KettleTransException(Messages.getString("Trans.Exception.UnableToBeginProcessingTransformation"), e); //$NON-NLS-1$
		}
	}

	public Result getResult()
	{
		if (steps==null) return null;

		Result result = new Result();

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep rt = (BaseStep)sid.step;

			result.setNrErrors(result.getNrErrors()+sid.step.getErrors());
			result.getResultFiles().putAll(rt.getResultFiles());

			if (transMeta.getReadStep()    !=null && rt.getStepname().equals(transMeta.getReadStep().getName()))     result.setNrLinesRead(result.getNrLinesRead()+ rt.getLinesRead());
			if (transMeta.getInputStep()   !=null && rt.getStepname().equals(transMeta.getInputStep().getName()))    result.setNrLinesInput(result.getNrLinesInput() + rt.getLinesInput());
			if (transMeta.getWriteStep()   !=null && rt.getStepname().equals(transMeta.getWriteStep().getName()))    result.setNrLinesWritten(result.getNrLinesWritten()+rt.getLinesWritten());
			if (transMeta.getOutputStep()  !=null && rt.getStepname().equals(transMeta.getOutputStep().getName()))   result.setNrLinesOutput(result.getNrLinesOutput()+rt.getLinesOutput());
			if (transMeta.getUpdateStep()  !=null && rt.getStepname().equals(transMeta.getUpdateStep().getName()))   result.setNrLinesUpdated(result.getNrLinesUpdated()+rt.getLinesUpdated());
            if (transMeta.getRejectedStep()!=null && rt.getStepname().equals(transMeta.getRejectedStep().getName())) result.setNrLinesRejected(result.getNrLinesRejected()+rt.getLinesRejected());
		}

		result.setRows( transMeta.getResultRows() );
		result.setStopped( isStopped() );

		return result;
	}

	//
	// Handle logging at end
	//
	public boolean endProcessing(String status) throws KettleException
	{
		Result result = getResult();

		if (transMeta.isUsingUniqueConnections()) {
			// Commit or roll back the transaction in the unique database connections...
			// 
			closeUniqueDatabaseConnections(result);
		}
		
		logDate     = new Date();

		// Change the logging back to stream...
		String log_string = null;
		if (transMeta.isLogfieldUsed() && stringAppender!=null)
		{
            log_string = stringAppender.getBuffer().append(Const.CR+"END"+Const.CR).toString();
            log.removeAppender(stringAppender);
		}

		// OK, we have some logging to do...
		//
		DatabaseMeta logcon = transMeta.getLogConnection();
		if (logcon!=null)
		{
			Database ldb = new Database(logcon);
			ldb.shareVariablesWith(this);
			try
			{
				ldb.connect();

				// Write to the standard transformation log table...
				//
				if (!Const.isEmpty(transMeta.getLogTable())) {
					ldb.writeLogRecord
						(
							transMeta.getLogTable(),
							transMeta.isBatchIdUsed(),
							getBatchId(),
							false,
							transMeta.getName(),
							status,
							result.getNrLinesRead(),
							result.getNrLinesWritten(),
							result.getNrLinesUpdated(),
							result.getNrLinesInput()+result.getNrFilesRetrieved(),
							result.getNrLinesOutput(),
							result.getNrErrors(),
						    startDate, endDate, logDate, depDate,currentDate,
							log_string
						);
				}
				
				// Write to the step performance log table...
				//
				if (!Const.isEmpty(transMeta.getStepPerformanceLogTable()) && transMeta.isCapturingStepPerformanceSnapShots()) {
					// Loop over the steps...
					//
					RowMetaInterface rowMeta = Database.getStepPerformanceLogrecordFields();
					ldb.prepareInsert(rowMeta, transMeta.getStepPerformanceLogTable());

					for (String key : stepPerformanceSnapShots.keySet()) {
						List<StepPerformanceSnapShot> snapshots = stepPerformanceSnapShots.get(key);
						long seqNr = 1;
						for (StepPerformanceSnapShot snapshot : snapshots) {
							Object[] row = new Object[ rowMeta.size() ];
							int outputIndex = 0;
							
							row[outputIndex++] = new Long(getBatchId());
							row[outputIndex++] = new Long(seqNr++);
							row[outputIndex++] = snapshot.getDate();
							row[outputIndex++] = transMeta.getName();
							row[outputIndex++] = snapshot.getStepName();
							row[outputIndex++] = new Long(snapshot.getStepCopy());
							row[outputIndex++] = new Long(snapshot.getLinesRead()); 
							row[outputIndex++] = new Long(snapshot.getLinesWritten()); 
							row[outputIndex++] = new Long(snapshot.getLinesUpdated()); 
							row[outputIndex++] = new Long(snapshot.getLinesInput()); 
							row[outputIndex++] = new Long(snapshot.getLinesOutput()); 
							row[outputIndex++] = new Long(snapshot.getLinesRejected()); 
							row[outputIndex++] = new Long(snapshot.getErrors()); 
							row[outputIndex++] = new Long(snapshot.getInputBufferSize()); 
							row[outputIndex++] = new Long(snapshot.getOutputBufferSize());
							
							ldb.setValuesInsert(rowMeta, row);
							ldb.insertRow(true);
						}
					}
					
					ldb.insertFinished(true);
				}
			}
			catch(Exception e)
			{
				throw new KettleException(Messages.getString("Trans.Exception.ErrorWritingLogRecordToTable")+transMeta.getLogTable()+"]", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			finally
			{
				ldb.disconnect();
			}
		}
		return true;
	}

	private void closeUniqueDatabaseConnections(Result result) {

		// First we get all the database connections ...
		//
		DatabaseConnectionMap map = DatabaseConnectionMap.getInstance();
		List<Database> databaseList = new ArrayList<Database>(map.getMap().values());
        for (Database database : databaseList) {
        	if (database.getConnectionGroup().equals(getThreadName())) {
        		try
        		{
	        		// This database connection belongs to this transformation.
	        		// Let's roll it back if there is an error...
	        		//
	        		if (result.getNrErrors()>0) {
	        			try {
	        				database.rollback(true);
	        				log.logBasic(toString(), Messages.getString("Trans.Exception.TransactionsRolledBackOnConnection", database.toString()));
	        			}
	        			catch(Exception e) {
	        				throw new KettleDatabaseException(Messages.getString("Trans.Exception.ErrorRollingBackUniqueConnection", database.toString()), e);
	        			}
	        		}
	        		else {
	        			try {
	        				database.commit(true);
	        				log.logBasic(toString(), Messages.getString("Trans.Exception.TransactionsCommittedOnConnection", database.toString()));
	        			}
	        			catch(Exception e) {
	        				throw new KettleDatabaseException(Messages.getString("Trans.Exception.ErrorCommittingUniqueConnection", database.toString()), e);
	        			}
	        		}
	        		database.closeConnectionOnly();
	        		
	        		// Remove the database from the list...
	        		//
	        		map.removeConnection(database.getConnectionGroup(), database.getPartitionId(), database);
        		}
        		catch(Exception e) {
        			log.logError(toString(), Messages.getString("Trans.Exception.ErrorHandlingTransformationTransaction", database.toString()), e);
        			result.setNrErrors(result.getNrErrors()+1);
        		}
        	}
        }
	}

	public BaseStep findRunThread(String stepname)
	{
		if (steps==null) return null;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			if (rt.getStepname().equalsIgnoreCase(stepname)) return rt;
		}
		return null;
	}
	
	public List<BaseStep> findBaseSteps(String stepname)
	{
		List<BaseStep> baseSteps = new ArrayList<BaseStep>();
		
		if (steps==null) return baseSteps;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			BaseStep rt = (BaseStep)sid.step;
			if (rt.getStepname().equalsIgnoreCase(stepname)) {
				baseSteps.add(rt);
			}
		}
		return baseSteps;
	}
    
    public StepDataInterface findDataInterface(String name)
    {
        if (steps==null) return null;

        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);
            BaseStep rt = (BaseStep)sid.step;
            if (rt.getStepname().equalsIgnoreCase(name)) return sid.data;
        }
        return null;
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
	 * @return See if the running transformation is monitored.
	 */
	public boolean isMonitored()
	{
		return monitored;
	}

	/**
	 * @param monitored Indicate we want to monitor the running transformation
	 */
	public void setMonitored(boolean monitored)
	{
		this.monitored = monitored;
	}

	/**
	 * @return Returns the transMeta.
	 */
	public TransMeta getTransMeta()
	{
		return transMeta;
	}

	/**
	 * @param transMeta The transMeta to set.
	 */
	public void setTransMeta(TransMeta transMeta)
	{
		this.transMeta = transMeta;
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
	 * @return Returns the rowsets.
	 */
	public List<RowSet> getRowsets()
	{
		return rowsets;
	}

	/**
	 * @return Returns the steps.
	 */
	public List<StepMetaDataCombi> getSteps()
	{
		return steps;
	}

	public String toString()
	{
        if (transMeta==null || transMeta.getName()==null) return getClass().getSimpleName();
        
        // See if there is a parent transformation.  If so, print the name of the parent here as well...
        //
        StringBuffer string = new StringBuffer();
        
        // If we're running as a mapping, we get a reference to the calling (parent) transformation as well...
        //
        if (getParentTrans()!=null) {
    		string.append('[').append(getParentTrans().toString()).append(']').append('.');
        } 
        
		// When we run a mapping we also set a mapping step name in there...
		//
		if (!Const.isEmpty(mappingStepName)) {
			string.append('[').append(mappingStepName).append(']').append('.');
		}
		
        string.append(transMeta.getName());
        
        return string.toString();
	}

    public MappingInput[] findMappingInput()
    {
		if (steps==null) return null;
		
		List<MappingInput> list = new ArrayList<MappingInput>();
		
        // Look in threads and find the MappingInput step thread...
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi smdc = steps.get(i);
            StepInterface step = smdc.step;
            if (step.getStepID().equalsIgnoreCase("MappingInput")) //$NON-NLS-1$
            {
                list.add((MappingInput)step);
            }
        }
        return list.toArray(new MappingInput[list.size()]);
    }

    public MappingOutput[] findMappingOutput()
    {
		List<MappingOutput> list = new ArrayList<MappingOutput>();
		
		if (steps!=null)
		{
	        // Look in threads and find the MappingInput step thread...
	        for (int i=0;i<steps.size();i++)
	        {
	            StepMetaDataCombi smdc = steps.get(i);
	            StepInterface step = smdc.step;
	            if (step.getStepID().equalsIgnoreCase("MappingOutput")) //$NON-NLS-1$
	            {
	                list.add((MappingOutput)step);
	            }
	        }
		}
        return list.toArray(new MappingOutput[list.size()]);
    }

    /**
     * Find the StepInterface (thread) by looking it up using the name
     * @param stepname The name of the step to look for
     * @param copy the copy number of the step to look for
     * @return the StepInterface or null if nothing was found.
     */
    public StepInterface getStepInterface(String stepname, int copy)
    {
		if (steps==null) return null;
		
        // Now start all the threads...
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);
            if (sid.stepname.equalsIgnoreCase(stepname) && sid.copy==copy)
            {
                return sid.step;
            }
        }

        return null;
    }

	public Date getReplayDate() {
		return replayDate;
	}

	public void setReplayDate(Date replayDate) {
		this.replayDate = replayDate;
	}

	/**
	 * Turn on safe mode during running: the transformation will run slower but with more checking enabled.
	 * @param safeModeEnabled true for safe mode
	 */
	public void setSafeModeEnabled(boolean safeModeEnabled)
	{
		this.safeModeEnabled = safeModeEnabled;
	}

	/**
	 * @return Returns true if the safe mode is enabled: the transformation will run slower but with more checking enabled
	 */
	public boolean isSafeModeEnabled()
	{
		return safeModeEnabled;
	}

    /**
     * This adds a row producer to the transformation that just got set up.
     * Preferable run this BEFORE execute() but after prepareExcution()
     *
     * @param stepname The step to produce rows for
     * @param copynr The copynr of the step to produce row for (normally 0 unless you have multiple copies running)
     * @throws KettleException in case the thread/step to produce rows for could not be found.
     */
    public RowProducer addRowProducer(String stepname, int copynr) throws KettleException
    {
        StepInterface stepInterface = getStepInterface(stepname, copynr);
        if (stepInterface==null)
        {
            throw new KettleException("Unable to find thread with name "+stepname+" and copy number "+copynr);
        }

        // We are going to add an extra RowSet to this stepInterface.
        RowSet rowSet = new RowSet(transMeta.getSizeRowset());

        // Add this rowset to the list of active rowsets for the selected step
        stepInterface.getInputRowSets().add(rowSet);

        return new RowProducer(stepInterface, rowSet);
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

    /**
     * Finds the StepDataInterface (currently) associated with the specified step  
     * @param stepname The name of the step to look for
     * @param stepcopy The copy number (0 based) of the step
     * @return The StepDataInterface or null if non found.
     */
    public StepDataInterface getStepDataInterface(String stepname, int stepcopy)
    {
		if (steps==null) return null;
		
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);
            if (sid.stepname.equals(stepname) && sid.copy==stepcopy) return sid.data;
        }
        return null;
    }

    /**
     * 
     * @return true if one or more steps are halted
     */
    public boolean hasHaltedSteps()
    {
    	// not yet 100% sure of this, if there are no steps... or none halted?
		if (steps==null) return false;
		
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);
            if (sid.data.getStatus()==StepDataInterface.STATUS_HALTED) return true;
        }
        return false;
    }

    public Date getJobStartDate()
    {
        return jobStartDate;
    }
    
    public Date getJobEndDate()
    {
        return jobEndDate;
    }

    /**
     * @param jobEndDate the jobEndDate to set
     */
    public void setJobEndDate(Date jobEndDate)
    {
        this.jobEndDate = jobEndDate;
    }

    /**
     * @param jobStartDate the jobStartDate to set
     */
    public void setJobStartDate(Date jobStartDate)
    {
        this.jobStartDate = jobStartDate;
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

    /**
     * @return the batchId
     */
    public long getBatchId()
    {
        return batchId;
    }

    /**
     * @param batchId the batchId to set
     */
    public void setBatchId(long batchId)
    {
        this.batchId = batchId;
    }

    /**
     * @return the threadName
     */
    public String getThreadName()
    {
        return threadName;
    }

    /**
     * @param threadName the threadName to set
     */
    public void setThreadName(String threadName)
    {
        this.threadName = threadName;
    }
    
    public String getStatus()
    {
        String message;
        
        if (running)
        {
            if (isStopped()) 
            {
            	message = STRING_HALTING;
            }
            else
            {
	            if (isFinished())
	            {
	                message = STRING_FINISHED;
	                if (getResult().getNrErrors()>0) message+=" (with errors)";
	            }
	            else
	            {
	                message = STRING_RUNNING;
	            }
            }
        }
        else if (isStopped())
        {
        	message = STRING_STOPPED;
        }
        else if (preparing)
        {
            message = STRING_PREPARING;
        }
        else
        if (initializing)
        {
            message = STRING_INITIALIZING;
        }
        else
        {
            message = STRING_WAITING;
        }
        
        return message;
    }

    /**
     * @return the initializing
     */
    public boolean isInitializing()
    {
        return initializing;
    }

    /**
     * @param initializing the initializing to set
     */
    public void setInitializing(boolean initializing)
    {
        this.initializing = initializing;
    }

    /**
     * @return the preparing
     */
    public boolean isPreparing()
    {
        return preparing;
    }

    /**
     * @param preparing the preparing to set
     */
    public void setPreparing(boolean preparing)
    {
        this.preparing = preparing;
    }

    /**
     * @return the running
     */
    public boolean isRunning()
    {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running)
    {
        this.running = running;
    }
    
    public static final TransSplitter executeClustered(final TransMeta transMeta, final TransExecutionConfiguration executionConfiguration) throws KettleException
    {
        if (Const.isEmpty(transMeta.getName())) throw new KettleException("The transformation needs a name to uniquely identify it by on the remote server.");
  
        TransSplitter transSplitter = new TransSplitter(transMeta);
        transSplitter.splitOriginalTransformation();
        executeClustered(transSplitter, executionConfiguration);
        return transSplitter;
    }
    
    /**
     * executes an existing transSplitter, with transformation already split.
     * 
     * See also : org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate
     * 
     * @param transSplitter
     * @param executionConfiguration
     * @throws KettleException
     */
    public static final void executeClustered(final TransSplitter transSplitter, final TransExecutionConfiguration executionConfiguration) throws KettleException 
    {
        try
        {
            // Send the transformations to the servers...
            //
            // First the master and the slaves...
        	//
            TransMeta master = transSplitter.getMaster();
            final SlaveServer[] slaves = transSplitter.getSlaveTargets();
            final Thread[]      threads = new Thread[slaves.length];
            final Throwable[]   errors = new Throwable[slaves.length];
            //
            // Send them all on their way...
            //
            SlaveServer masterServer = null;
            List<StepMeta> masterSteps = master.getTransHopSteps(false);
            if (masterSteps.size()>0) // If there is something that needs to be done on the master...
            {
                masterServer = transSplitter.getMasterServer();
                if (executionConfiguration.isClusterPosting())
                {
                	TransConfiguration transConfiguration = new TransConfiguration(master, executionConfiguration);
                	Map<String, String> variables = transConfiguration.getTransExecutionConfiguration().getVariables();
                    variables.put(Const.INTERNAL_VARIABLE_CLUSTER_SIZE, Integer.toString(slaves.length));
                    variables.put(Const.INTERNAL_VARIABLE_CLUSTER_MASTER, "Y");
                    
                    // Parameters override the variables but they need to pass over the configuration too...
                    //
                    Map<String, String> params = transConfiguration.getTransExecutionConfiguration().getParams();
                    TransMeta ot = transSplitter.getOriginalTransformation();
                    for (String param : ot.listParameters()) {
                    	String value = Const.NVL(ot.getParameterValue(param), Const.NVL(ot.getParameterDefault(param), ot.getVariable(param)));
                    	params.put(param, value);
                    }
                    
                    String masterReply = masterServer.sendXML(transConfiguration.getXML(), AddTransServlet.CONTEXT_PATH+"/?xml=Y");
                    WebResult webResult = WebResult.fromXMLString(masterReply);
                    if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                    {
                        throw new KettleException("An error occurred sending the master transformation: "+webResult.getMessage());
                    }
                }
            }
            
            // Then the slaves...
            // These are started in a background thread.
            //
            for (int i=0;i<slaves.length;i++)
            {
            	final int index = i;
            	
                final TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                
                if (executionConfiguration.isClusterPosting())
                {
                    Runnable runnable = new Runnable() {
                        public void run() {
                          try {
                              // Create a copy for local use...  We get race-conditions otherwise...
                              //
                              TransExecutionConfiguration slaveTransExecutionConfiguration = (TransExecutionConfiguration)executionConfiguration.clone();
                              TransConfiguration transConfiguration = new TransConfiguration(slaveTrans, slaveTransExecutionConfiguration);
                              
                              Map<String, String> variables = slaveTransExecutionConfiguration.getVariables();
                              variables.put(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER, Integer.toString(index));
                              variables.put(Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME, slaves[index].getName());
                              variables.put(Const.INTERNAL_VARIABLE_CLUSTER_SIZE, Integer.toString(slaves.length));
                              variables.put(Const.INTERNAL_VARIABLE_CLUSTER_MASTER, "N");
                              
                              // Parameters override the variables but they need to pass over the configuration too...
                              //
                              Map<String, String> params = slaveTransExecutionConfiguration.getParams();
                              TransMeta ot = transSplitter.getOriginalTransformation();
                              for (String param : ot.listParameters()) {
                            	String value = Const.NVL(ot.getParameterValue(param), Const.NVL(ot.getParameterDefault(param), ot.getVariable(param)));
                              	params.put(param, value);
                              }
                              
                              String slaveReply = slaves[index].sendXML(transConfiguration.getXML(), AddTransServlet.CONTEXT_PATH+"/?xml=Y");
                              WebResult webResult = WebResult.fromXMLString(slaveReply);
                              if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                              {
                                  throw new KettleException("An error occurred sending a slave transformation: "+webResult.getMessage());
                              }
                          }
                          catch(Throwable t) {
                              errors[index] = t;
                          }
                        }
                    };
                    threads[i] = new Thread(runnable);
                }
            }
            
            // Start the slaves
            for (int i=0;i<threads.length;i++) {
              if (threads[i]!=null) {
                threads[i].start();
              }
            }
            
            // Wait until the slaves report back...
            // Sending the XML over is the heaviest part
            // Later we can do the others as well...
            //
            for (int i=0;i<threads.length;i++) {
            	if (threads[i]!=null) {
            		threads[i].join();
            		if (errors[i]!=null) throw new KettleException(errors[i]);
            	}
            }
            
            if (executionConfiguration.isClusterPosting())
            {
                if (executionConfiguration.isClusterPreparing())
                {
                    // Prepare the master...
                    if (masterSteps.size()>0) // If there is something that needs to be done on the master...
                    {
                        String masterReply = masterServer.getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(master.getName(), "UTF-8")+"&xml=Y");
                        WebResult webResult = WebResult.fromXMLString(masterReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while preparing the execution of the master transformation: "+webResult.getMessage());
                        }
                    }
                    
                    // Prepare the slaves
                    // WG: Should these be threaded like the above initialization?
                    for (int i=0;i<slaves.length;i++)
                    {
                        TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                        String slaveReply = slaves[i].getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(slaveTrans.getName(), "UTF-8")+"&xml=Y");
                        WebResult webResult = WebResult.fromXMLString(slaveReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while preparing the execution of a slave transformation: "+webResult.getMessage());
                        }
                    }
                }
                
                if (executionConfiguration.isClusterStarting())
                {
                    // Start the master...
                    if (masterSteps.size()>0) // If there is something that needs to be done on the master...
                    {
                        String masterReply = masterServer.getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(master.getName(), "UTF-8")+"&xml=Y");
                        WebResult webResult = WebResult.fromXMLString(masterReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while starting the execution of the master transformation: "+webResult.getMessage());
                        }
                    }
                    
                    // Start the slaves
                    // WG: Should these be threaded like the above initialization?
                    for (int i=0;i<slaves.length;i++)
                    {
                        TransMeta slaveTrans = (TransMeta) transSplitter.getSlaveTransMap().get(slaves[i]);
                        String slaveReply = slaves[i].getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(slaveTrans.getName(), "UTF-8")+"&xml=Y");
                        WebResult webResult = WebResult.fromXMLString(slaveReply);
                        if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
                        {
                            throw new KettleException("An error occurred while starting the execution of a slave transformation: "+webResult.getMessage());
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new KettleException("There was an error during transformation split", e);
        }
    }

    /** Consider that all the transformations in a cluster schema are running now...<br>
	    Now we should verify that they are all running as they should.<br>
	    If a transformation has an error, we should kill them all..<br>
	    This should happen in a separate thread to prevent blocking of the UI.<br>
	    <br>
	    When the master and slave transformations have all finished, we should also run<br>
	    a cleanup on those transformations to release sockets, etc.<br>
	    <br>
	    
	   @param logSubject the subject to use for logging
	   @param transSplitter the transformation splitter object
	   @param parentJob the parent job when executed in a job, otherwise just set to null
	   @param sleepTimeSeconds the sleep time in seconds in between slave transformation status polling
	   @return the number of errors encountered
	*/
	public static final long monitorClusteredTransformation(String logSubject, TransSplitter transSplitter, Job parentJob)
	{
		return monitorClusteredTransformation(logSubject, transSplitter, parentJob, 1); // monitor every 1 seconds
	}
	
    /** Consider that all the transformations in a cluster schema are running now...<br>
        Now we should verify that they are all running as they should.<br>
        If a transformation has an error, we should kill them all..<br>
        This should happen in a separate thread to prevent blocking of the UI.<br>
        <br>
        When the master and slave transformations have all finished, we should also run<br>
        a cleanup on those transformations to release sockets, etc.<br>
        <br>
        
       @param logSubject the subject to use for logging
       @param transSplitter the transformation splitter object
       @param parentJob the parent job when executed in a job, otherwise just set to null
       @param sleepTimeSeconds the sleep time in seconds in between slave transformation status polling
       @return the number of errors encountered
	*/
    public static final long monitorClusteredTransformation(String logSubject, TransSplitter transSplitter, Job parentJob, int sleepTimeSeconds)
    {
        long errors = 0L;

        //
        // See if the remote transformations have finished.
        // We could just look at the master, but I doubt that that is enough in all situations.
        //
        SlaveServer[] slaveServers = transSplitter.getSlaveTargets(); // <-- ask these guys
        TransMeta[] slaves = transSplitter.getSlaves();

        SlaveServer masterServer;
		try {
			masterServer = transSplitter.getMasterServer();
		} catch (KettleException e) {
			log.logError(logSubject, "Error getting the master server", e);
			masterServer = null;
			errors++;
		}
        TransMeta masterTransMeta = transSplitter.getMaster();
        TransMeta transMeta = transSplitter.getOriginalTransformation();

        boolean allFinished = false;
        while (!allFinished && errors==0 && ( parentJob==null || !parentJob.isStopped()) )
        {
            allFinished = true;
            errors=0L;

            // Slaves first...
            //
            for (int s=0;s<slaveServers.length && allFinished && errors==0;s++)
            {
                try
                {
                    SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus(slaves[s].getName());
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed(logSubject, "Slave transformation on '"+slaveServers[s]+"' is still running.");
                    	allFinished = false;
                    }
                    else {
                    	if(log.isDetailed()) log.logDetailed(logSubject, "Slave transformation on '"+slaveServers[s]+"' has finished.");
                    }
                    errors+=transStatus.getNrStepErrors();
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError(logSubject, "Unable to contact slave server '"+slaveServers[s].getName()+"' to check slave transformation : "+e.toString());
                }
            }

            // Check the master too
            if (allFinished && errors==0 && masterTransMeta!=null && masterTransMeta.nrSteps()>0)
            {
                try
                {
                    SlaveServerTransStatus transStatus = masterServer.getTransStatus(masterTransMeta.getName());
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed(logSubject, "Master transformation is still running.");
                    	allFinished = false;
                    }
                    else {
                    	if(log.isDetailed()) log.logDetailed(logSubject, "Master transformation has finished.");
                    }
                    Result result = transStatus.getResult(transSplitter.getOriginalTransformation());
                    errors+=result.getNrErrors();
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError(logSubject, "Unable to contact master server '"+masterServer.getName()+"' to check master transformation : "+e.toString());
                }
            }

            if ((parentJob!=null && parentJob.isStopped()) || errors != 0)
            {
                //
                // Stop all slaves and the master on the slave servers
                //
                for (int s=0;s<slaveServers.length && allFinished && errors==0;s++)
                {
                    try
                    {
                        WebResult webResult = slaveServers[s].stopTransformation(slaves[s].getName());
                        if (!WebResult.STRING_OK.equals(webResult.getResult()))
                        {
                            log.logError(logSubject, "Unable to stop slave transformation '"+slaves[s].getName()+"' : "+webResult.getMessage());
                        }
                    }
                    catch(Exception e)
                    {
                        errors+=1;
                        log.logError(logSubject, "Unable to contact slave server '"+slaveServers[s].getName()+"' to stop transformation : "+e.toString());
                    }
                }

                try
                {
                    WebResult webResult = masterServer.stopTransformation(masterTransMeta.getName());
                    if (!WebResult.STRING_OK.equals(webResult.getResult()))
                    {
                        log.logError(logSubject, "Unable to stop master transformation '"+masterServer.getName()+"' : "+webResult.getMessage());
                    }
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError(logSubject, "Unable to contact master server '"+masterServer.getName()+"' to stop the master : "+e.toString());
                }
            }

            //
            // Keep waiting until all transformations have finished
            // If needed, we stop them again and again until they yield.
            //
            if (!allFinished)
            {
                // Not finished or error: wait a bit longer
            	if(log.isDetailed()) log.logDetailed(logSubject, "Clustered transformation is still running, waiting a few seconds...");
                try { Thread.sleep(sleepTimeSeconds*2000); } catch(Exception e) {} // Check all slaves every x seconds. 
            }
        }
        
        log.logBasic(logSubject, "All transformations in the cluster have finished.");
        
        // All transformations have finished, with or without error.
        // Now run a cleanup on all the transformation on the master and the slaves.
        //
        // Slaves first...
        //
        for (int s=0;s<slaveServers.length;s++)
        {
            try
            {
                WebResult webResult = slaveServers[s].cleanupTransformation(slaves[s].getName());
                if (!WebResult.STRING_OK.equals(webResult.getResult()))
                {
                    log.logError(logSubject, "Unable to run clean-up on slave transformation '"+slaves[s].getName()+"' : "+webResult.getMessage());
                    errors+=1;
                }
            }
            catch(Exception e)
            {
                errors+=1;
                log.logError(logSubject, "Unable to contact slave server '"+slaveServers[s].getName()+"' to check slave transformation : "+e.toString());
            }
        }

        // Clean up  the master too
        //
        if (masterTransMeta!=null && masterTransMeta.nrSteps()>0)
        {
            try
            {
                WebResult webResult = masterServer.cleanupTransformation(masterTransMeta.getName());
                if (!WebResult.STRING_OK.equals(webResult.getResult()))
                {
                    log.logError(logSubject, "Unable to run clean-up on master transformation '"+masterTransMeta.getName()+"' : "+webResult.getMessage());
                    errors+=1;
                }

                webResult = masterServer.deallocatePorts(transMeta.getName()); // registered under the original name?
                if (!WebResult.STRING_OK.equals(webResult.getResult()))
                {
                    log.logError(logSubject, "Unable to run clean-up on master transformation '"+masterTransMeta.getName()+"' : "+webResult.getMessage());
                    errors+=1;
                }

            }
            catch(Exception e)
            {
                errors+=1;
                log.logError(logSubject, "Unable to contact master server '"+masterServer.getName()+"' to clean up master transformation : "+e.toString());
            }
        }
        
        return errors;
    }
    
    public static final Result getClusteredTransformationResult(String logSubject, TransSplitter transSplitter, Job parentJob)
    {
    	Result result = new Result();
        //
        // See if the remote transformations have finished.
        // We could just look at the master, but I doubt that that is enough in all situations.
        //
        SlaveServer[] slaveServers = transSplitter.getSlaveTargets(); // <-- ask these guys
        TransMeta[] slaves = transSplitter.getSlaves();

        SlaveServer masterServer;
		try {
			masterServer = transSplitter.getMasterServer();
		} catch (KettleException e) {
			log.logError(logSubject, "Error getting the master server", e);
			masterServer = null;
			result.setNrErrors(result.getNrErrors()+1);
		}
        TransMeta master = transSplitter.getMaster();
        
        
        // Slaves first...
        //
        for (int s=0;s<slaveServers.length;s++)
        {
            try
            {
            	// Get the detailed status of the slave transformation...
            	//
            	SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus(slaves[s].getName());
            	Result transResult = transStatus.getResult(slaves[s]);
            	
            	result.add(transResult);
            }
            catch(Exception e)
            {
    			result.setNrErrors(result.getNrErrors()+1);
                log.logError(logSubject, "Unable to contact slave server '"+slaveServers[s].getName()+"' to get result of slave transformation : "+e.toString());
            }
        }

        // Clean up  the master too
        //
        if (master!=null && master.nrSteps()>0)
        {
            try
            {
            	// Get the detailed status of the slave transformation...
            	//
            	SlaveServerTransStatus transStatus = masterServer.getTransStatus(master.getName());
            	Result transResult = transStatus.getResult(master);
            	
            	result.add(transResult);
            }
            catch(Exception e)
            {
            	result.setNrErrors(result.getNrErrors()+1);
                log.logError(logSubject, "Unable to contact master server '"+masterServer.getName()+"' to get result of master transformation : "+e.toString());
            }
        }
        
        
        return result;
    }
    
	
	public static void sendToSlaveServer(TransMeta transMeta, TransExecutionConfiguration executionConfiguration, Repository repository) throws KettleException
	{
		SlaveServer slaveServer = executionConfiguration.getRemoteServer();

		if (slaveServer == null)
			throw new KettleException("No slave server specified");
		if (Const.isEmpty(transMeta.getName()))
			throw new KettleException( "The transformation needs a name to uniquely identify it by on the remote server.");

		try
		{
			// Inject certain internal variables to make it more intuitive. 
			// 
			for (String var : Const.INTERNAL_TRANS_VARIABLES) executionConfiguration.getVariables().put(var, transMeta.getVariable(var));
			for (String var : Const.INTERNAL_JOB_VARIABLES) executionConfiguration.getVariables().put(var, transMeta.getVariable(var));
			
			if (executionConfiguration.isPassingExport()) {
				
				// First export the job...
				//
				FileObject tempFile = KettleVFS.createTempFile("transExport", ".zip", System.getProperty("java.io.tmpdir"), transMeta);
				
				TopLevelResource topLevelResource = ResourceUtil.serializeResourceExportInterface(tempFile.getName().toString(), transMeta, transMeta, repository, executionConfiguration.getXML(), CONFIGURATION_IN_EXPORT_FILENAME);
				
				// Send the zip file over to the slave server...
				//
				String result = slaveServer.sendExport(topLevelResource.getArchiveName(), AddExportServlet.TYPE_TRANS, topLevelResource.getBaseResourceName());
				WebResult webResult = WebResult.fromXMLString(result);
				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
				{
					throw new KettleException("There was an error passing the exported transformation to the remote server: " + Const.CR+ webResult.getMessage());
				}
				
				// The remote file name is comprised in the message
				//
				String remoteFile = webResult.getMessage();
				LogWriter.getInstance().logBasic(transMeta.getName(), "Added the remote transformation to slave server ["+slaveServer.getName()+"] for remote file: "+remoteFile);
				
			} else {
				
				// Now send it off to the remote server...
				//
				String xml = new TransConfiguration(transMeta, executionConfiguration).getXML();
				String reply = slaveServer.sendXML(xml, AddTransServlet.CONTEXT_PATH + "/?xml=Y");
				WebResult webResult = WebResult.fromXMLString(reply);
				if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
				{
					throw new KettleException( "There was an error posting the transformation on the remote server: " + Const.CR + webResult.getMessage());
				}
			}
	
			// Prepare the transformation
			//
			String reply = slaveServer.getContentFromServer(PrepareExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(transMeta.getName(), "UTF-8") + "&xml=Y");
			WebResult webResult = WebResult.fromXMLString(reply);
			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException("There was an error preparing the transformation for excution on the remote server: "+ Const.CR + webResult.getMessage());
			}

			// Start the transformation
			//
			reply = slaveServer.getContentFromServer(StartExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(transMeta.getName(), "UTF-8") + "&xml=Y");
			webResult = WebResult.fromXMLString(reply);

			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException( "There was an error starting the transformation on the remote server: " + Const.CR + webResult.getMessage());
			}
		} 
		catch (Exception e)
		{
			throw new KettleException(e);
		}

	}

    /**
     * @return true if the transformation was prepared for execution successfully.
     * @see org.pentaho.di.trans.Trans#prepareExecution(String[])
     */
    public boolean isReadyToStart()
    {
        return readyToStart;
    }
    
    public void setInternalKettleVariables(VariableSpace var)
    {        
        if (transMeta != null && !Const.isEmpty(transMeta.getFilename())) // we have a finename that's defined.
        {
            try
            {
                FileObject fileObject = KettleVFS.getFileObject(transMeta.getFilename(), var);
                FileName fileName = fileObject.getName();
                
                // The filename of the transformation
                variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, fileName.getBaseName());

                // The directory of the transformation
                FileName fileDir = fileName.getParent();
                variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI());
            }
            catch(IOException e)
            {
                variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "");
                variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "");
            }
        }
        else
        {
            variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "");
            variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "");
        }
        
        // The name of the transformation
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, Const.NVL(transMeta.getName(), ""));

        // TODO PUT THIS INSIDE OF THE "IF"
        // The name of the directory in the repository
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, transMeta.getDirectory()!=null?transMeta.getDirectory().getPath():"");
        
        // Here we don't clear the definition of the job specific parameters, as they may come in handy.
        // A transformation can be called from a job and may inherit the job internal variables
        // but the other around is not possible.
        
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

	/**
	 * Pause the transformation (pause all steps)
	 */
	public void pauseRunning() {
		paused.set(true);
		for (StepMetaDataCombi combi : steps) {
			combi.step.pauseRunning();
		}
	}
	
	/**
	 * Resume running the transformation after a pause (resume all steps)
	 */
	public void resumeRunning() {
		for (StepMetaDataCombi combi : steps) {
			combi.step.resumeRunning();
		}
		paused.set(false);
	}

	/**
	 * @return the preview
	 */
	public boolean isPreview() {
		return preview;
	}

	/**
	 * @param preview the preview to set
	 */
	public void setPreview(boolean preview) {
		this.preview = preview;
	}

	/**
	 * @return the repository
	 */
	public Repository getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/**
	 * @return the stepPerformanceSnapShots
	 */
	public Map<String, List<StepPerformanceSnapShot>> getStepPerformanceSnapShots() {
		return stepPerformanceSnapShots;
	}

	/**
	 * @param stepPerformanceSnapShots the stepPerformanceSnapShots to set
	 */
	public void setStepPerformanceSnapShots(Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots) {
		this.stepPerformanceSnapShots = stepPerformanceSnapShots;
	}

	/**
	 * @return the transListeners
	 */
	public List<TransListener> getTransListeners() {
		return transListeners;
	}

	/**
	 * @param transListeners the transListeners to set
	 */
	public void setTransListeners(List<TransListener> transListeners) {
		this.transListeners = transListeners;
	} 
	
	public void addTransListener(TransListener transListener) {
		transListeners.add(transListener);
	}
	
	public boolean isPaused() {
		return paused.get();
	}

	public boolean isStopped() {
		return stopped.get();
	}

	public static void monitorRemoteTransformation(String transName, SlaveServer remoteSlaveServer) {
		monitorRemoteTransformation(transName, remoteSlaveServer, 5);
	}
	
	public static void monitorRemoteTransformation(String transName, SlaveServer remoteSlaveServer, int sleepTimeSeconds) {
		long errors=0;
        boolean allFinished = false;
        while (!allFinished && errors==0 )
        {
            allFinished = true;
            errors=0L;

            // Check the remote server
            if (allFinished && errors==0)
            {
                try
                {
                    SlaveServerTransStatus transStatus = remoteSlaveServer.getTransStatus(transName);
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed(transName, "Remote transformation is still running.");
                    	allFinished = false;
                    }
                    else {
                    	if(log.isDetailed()) log.logDetailed(transName, "Remote transformation has finished.");
                    }
                    Result result = transStatus.getResult();
                    errors+=result.getNrErrors();
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError(transName, "Unable to contact remote slave server '"+remoteSlaveServer.getName()+"' to check transformation status : "+e.toString());
                }
            }

            //
            // Keep waiting until all transformations have finished
            // If needed, we stop them again and again until they yield.
            //
            if (!allFinished)
            {
                // Not finished or error: wait a bit longer
            	if(log.isDetailed()) log.logDetailed(transName, "The remote transformation is still running, waiting a few seconds...");
                try { Thread.sleep(sleepTimeSeconds*1000); } catch(Exception e) {} // Check all slaves every x seconds. 
            }
        }
        
        log.logMinimal(transName, "The remote transformation has finished.");
        
        // Clean up the remote transformation
        //
        try
        {
            WebResult webResult = remoteSlaveServer.cleanupTransformation(transName);
            if (!WebResult.STRING_OK.equals(webResult.getResult()))
            {
                log.logError(transName, "Unable to run clean-up on remote transformation '"+transName+"' : "+webResult.getMessage());
                errors+=1;
            }
        }
        catch(Exception e)
        {
            errors+=1;
            log.logError(transName, "Unable to contact slave server '"+remoteSlaveServer.getName()+"' to clean up transformation : "+e.toString());
        }
	}

	public void addParameterDefinition(String key, String defValue, String description) throws DuplicateParamException {
		namedParams.addParameterDefinition(key, defValue, description);		
	}

	public String getParameterDefault(String key) throws UnknownParamException {
		return namedParams.getParameterDefault(key);
	}	
	
	public String getParameterDescription(String key) throws UnknownParamException {
		return namedParams.getParameterDescription(key);
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

	/**
	 * @return the parentTrans
	 */
	public Trans getParentTrans() {
		return parentTrans;
	}

	/**
	 * @param parentTrans the parentTrans to set
	 */
	public void setParentTrans(Trans parentTrans) {
		this.parentTrans = parentTrans;
	}

	/**
	 * @return the name of the mapping step that created this transformation
	 */
	public String getMappingStepName() {
		return mappingStepName;
	}

	/**
	 * @param mappingStepName the name of the mapping step that created this transformation
	 */
	public void setMappingStepName(String mappingStepName) {
		this.mappingStepName = mappingStepName;
	}

	public void setSocketRepository(SocketRepository socketRepository) {
		this.socketRepository = socketRepository;
	}
	
	public SocketRepository getSocketRepository() {
		return socketRepository;
	}
}