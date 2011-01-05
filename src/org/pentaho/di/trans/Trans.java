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

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.logging.CentralLogStore;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.resource.ResourceUtil;
import org.pentaho.di.resource.TopLevelResource;
import org.pentaho.di.trans.cluster.TransSplitter;
import org.pentaho.di.trans.performance.StepPerformanceSnapShot;
import org.pentaho.di.trans.step.RunThread;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInitThread;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
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
public class Trans implements VariableSpace, NamedParams, HasLogChannelInterface, LoggingObjectInterface
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final String REPLAY_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss"; //$NON-NLS-1$
    
	private LogChannelInterface log;
	private LogLevel logLevel = LogLevel.BASIC;
	private String containerObjectId;
	
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
     * The parent logging object interface (this could be a transformation or a job
     */
	  private LoggingObjectInterface	parent;

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
    public static final String STRING_PAUSED       = "Paused";
    public static final String STRING_PREPARING    = "Preparing executing";
    public static final String STRING_INITIALIZING = "Initializing";
    public static final String STRING_WAITING      = "Waiting";
    public static final String STRING_STOPPED      = "Stopped";
    public static final String STRING_HALTING      = "Halting";

	public static final String	CONFIGURATION_IN_EXPORT_FILENAME	= "__job_execution_configuration__.xml";

	private boolean safeModeEnabled;

    private String threadName;
    
    private boolean preparing;
    private boolean initializing;
    private boolean running;
    private final AtomicBoolean finished;
    private AtomicBoolean paused;
    private AtomicBoolean stopped;
    
    private AtomicInteger errors;

    private boolean readyToStart;    
    
    private Map<String,List<StepPerformanceSnapShot>> stepPerformanceSnapShots;

    private Timer stepPerformanceSnapShotTimer;
    
    private List<TransListener> transListeners;

    private List<TransStoppedListener> transStoppedListeners;
    
    private int nrOfFinishedSteps;
    private int nrOfActiveSteps;
    
    private NamedParams namedParams = new NamedParamsDefault();

	private SocketRepository	socketRepository;

	private Database transLogTableDatabaseConnection;
	
	private AtomicInteger stepPerformanceSnapshotSeqNr;

	private int	lastWrittenStepPerformanceSequenceNr;

	private int	lastStepPerformanceSnapshotSeqNrAdded;

	private Map<String, Trans> activeSubtransformations;

  private int stepPerformanceSnapshotSizeLimit;
	
	public Trans() {
		finished = new AtomicBoolean(false);
	    paused = new AtomicBoolean(false);
	    stopped = new AtomicBoolean(false);

		transListeners = new ArrayList<TransListener>();
        transStoppedListeners = new ArrayList<TransStoppedListener>();

		// This is needed for e.g. database 'unique' connections.
        threadName = Thread.currentThread().getName();
        errors = new AtomicInteger(0);
        
        stepPerformanceSnapshotSeqNr = new AtomicInteger(0);
        lastWrittenStepPerformanceSequenceNr = 0;
        
        activeSubtransformations = new HashMap<String, Trans>();
	}

	/**
	 * Initialize a transformation from transformation meta-data defined in memory
	 * @param transMeta the transformation meta-data to use.
	 */
	public Trans(TransMeta transMeta)
	{
		this(transMeta, null);
	}
	
	/**
	 * Initialize a transformation from transformation meta-data defined in memory.
	 * Also take into account the parent log channel interface (job or transformation) for logging lineage purposes.
	 * 
	 * @param transMeta the transformation meta-data to use.
	 * @param parent the parent job that is executing this transformation
	 */
	public Trans(TransMeta transMeta, LoggingObjectInterface parent)
	{
		this();
		this.transMeta = transMeta;
		this.parent = parent;
		
		this.log = new LogChannel(this, parent);
		this.logLevel = log.getLogLevel();
		this.containerObjectId = log.getContainerObjectId();
		
		if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.TransformationIsPreloaded")); //$NON-NLS-1$
		if (log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "Trans.Log.NumberOfStepsToRun",String.valueOf(transMeta.nrSteps()) ,String.valueOf(transMeta.nrTransHops()))); //$NON-NLS-1$ //$NON-NLS-2$
		initializeVariablesFrom(transMeta);
		copyParametersFrom(transMeta);
		transMeta.activateParameters();
	}
		
	public LogChannelInterface getLogChannel() {
		return log;
	}
	
	public void setLog(LogChannelInterface log) {
		this.log = log;
	}

	public String getName()
	{
		if (transMeta==null) return null;

		return transMeta.getName();
	}

	public Trans(VariableSpace parentVariableSpace, Repository rep, String name, String dirname, String filename) throws KettleException
	{
		this();
		try
		{
			if (rep!=null)
			{
			  RepositoryDirectoryInterface repdir = rep.findDirectory(dirname);
				if (repdir!=null)
				{
					this.transMeta = rep.loadTransformation(name, repdir, null, false, null); // reads last version
				}
				else
				{
					throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToLoadTransformation",name,dirname)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
			else
			{
			  transMeta = new TransMeta(filename, false);
			}
			
			this.log = new LogChannel(this);
			
			transMeta.initializeVariablesFrom(parentVariableSpace);
			initializeVariablesFrom(parentVariableSpace);
			transMeta.copyParametersFrom(this);
			transMeta.activateParameters();		
		}
		catch(KettleException e)
		{
			throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToOpenTransformation",name), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
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
				log.logBasic(BaseMessages.getString(PKG, "Trans.Log.DispacthingStartedForFilename",transMeta.getFilename())); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		else
		{
			log.logBasic(BaseMessages.getString(PKG, "Trans.Log.DispacthingStartedForTransformation",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (transMeta.getArguments()!=null)
		{
		    if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.NumberOfArgumentsDetected", String.valueOf(transMeta.getArguments().length) )); //$NON-NLS-1$
		}

		if (isSafeModeEnabled())
		{
		    if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.SafeModeIsEnabled",transMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		}

		if (getReplayDate() != null) {
			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(BaseMessages.getString(PKG, "Trans.Log.ThisIsAReplayTransformation") //$NON-NLS-1$
					+ df.format(getReplayDate()));
		} else {
		    if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.ThisIsNotAReplayTransformation")); //$NON-NLS-1$
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
			log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.FoundDefferentSteps",String.valueOf(hopsteps.size())));	 //$NON-NLS-1$ //$NON-NLS-2$
			log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.AllocatingRowsets")); //$NON-NLS-1$
		}
		// First allocate all the rowsets required!
		// Note that a mapping doesn't receive ANY input or output rowsets...
		//
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta thisStep=hopsteps.get(i);
			if (thisStep.isMapping()) continue; // handled and allocated by the mapping step itself.
			
			if(log.isDetailed()) 
				log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.AllocateingRowsetsForStep",String.valueOf(i),thisStep.getName())); //$NON-NLS-1$ //$NON-NLS-2$

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
                
                // Are we re-partitioning?
                boolean repartitioning = !thisStep.isPartitioned() && nextStep.isPartitioned();
                
                int nrCopies;
                if(log.isDetailed()) 
                	log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.copiesInfo",String.valueOf(thisCopies),String.valueOf(nextCopies))); //$NON-NLS-1$ //$NON-NLS-2$
				int dispatchType;
				     if (thisCopies==1 && nextCopies==1) { dispatchType=TYPE_DISP_1_1; nrCopies = 1; }
				else if (thisCopies==1 && nextCopies >1) { dispatchType=TYPE_DISP_1_N; nrCopies = nextCopies; }
				else if (thisCopies >1 && nextCopies==1) { dispatchType=TYPE_DISP_N_1; nrCopies = thisCopies; }
				else if (thisCopies==nextCopies && !repartitioning)         { dispatchType=TYPE_DISP_N_N; nrCopies = nextCopies; } // > 1!
				else                                     { dispatchType=TYPE_DISP_N_M; nrCopies = nextCopies; } // Allocate a rowset for each destination step

				// Allocate the rowsets
				//
                if (dispatchType!=TYPE_DISP_N_M)
                {
    				for (int c=0;c<nrCopies;c++)
    				{
    					RowSet rowSet;
    					switch(transMeta.getTransformationType()) {
    					case Normal: rowSet = new BlockingRowSet(transMeta.getSizeRowset()); break;
    					case SerialSingleThreaded: rowSet = new SingleRowRowSet(); break;
                        case SingleThreaded: rowSet = new QueueRowSet(); break;
    					default: 
    					  throw new KettleException("Unhandled transformation type: "+transMeta.getTransformationType());
    					}
    						
    					switch(dispatchType)
    					{
    					case TYPE_DISP_1_1: rowSet.setThreadNameFromToCopy(thisStep.getName(), 0, nextStep.getName(), 0); break;
    					case TYPE_DISP_1_N: rowSet.setThreadNameFromToCopy(thisStep.getName(), 0, nextStep.getName(), c); break;
    					case TYPE_DISP_N_1: rowSet.setThreadNameFromToCopy(thisStep.getName(), c, nextStep.getName(), 0); break;
    					case TYPE_DISP_N_N: rowSet.setThreadNameFromToCopy(thisStep.getName(), c, nextStep.getName(), c); break;
                        }
    					rowsets.add(rowSet);
    					if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.TransformationAllocatedNewRowset",rowSet.toString())); //$NON-NLS-1$ //$NON-NLS-2$
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
                            BlockingRowSet rowSet=new BlockingRowSet(transMeta.getSizeRowset());
                            rowSet.setThreadNameFromToCopy(thisStep.getName(), s, nextStep.getName(), t);
                            rowsets.add(rowSet);
                            if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.TransformationAllocatedNewRowset",rowSet.toString())); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                    }
                }
			}
			log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.AllocatedRowsets",String.valueOf(rowsets.size()),String.valueOf(i),thisStep.getName())+" "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.AllocatingStepsAndStepData")); //$NON-NLS-1$
        
		// Allocate the steps & the data...
		//
		for (int i=0;i<hopsteps.size();i++)
		{
			StepMeta stepMeta=hopsteps.get(i);
			String stepid = stepMeta.getStepID();

			if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.TransformationIsToAllocateStep",stepMeta.getName(),stepid)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// How many copies are launched of this step?
			int nrCopies=stepMeta.getCopies();

            if (log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "Trans.Log.StepHasNumberRowCopies",String.valueOf(nrCopies))); //$NON-NLS-1$

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
					step.initializeVariablesFrom(this);
					step.setUsingThreadPriorityManagment(transMeta.isUsingThreadPriorityManagment());
					
                    // If the step is partitioned, set the partitioning ID and some other things as well...
                    if (stepMeta.isPartitioned())
                    {
                    	List<String> partitionIDs = stepMeta.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
                        if (partitionIDs!=null && partitionIDs.size()>0) 
                        {
                        	step.setPartitionID(partitionIDs.get(c)); // Pass the partition ID to the step
                        }
                    }

					// Save the step too
					combi.step = step;

					if(combi.step instanceof LoggingObjectInterface) {
					  combi.step.getLogChannel().setLogLevel(logLevel);
					}
					
					// Add to the bunch...
					steps.add(combi);

					if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.TransformationHasAllocatedANewStep",stepMeta.getName(),String.valueOf(c))); //$NON-NLS-1$ //$NON-NLS-2$
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
            	combi.step.identifyErrorOutput();
            	
            }
        }

		// Now (optionally) write start log record!
        calculateBatchIdAndDateRange();
		beginProcessing();

        // Set the partition-to-rowset mapping
        //
        for (int i=0;i<steps.size();i++)
        {
            StepMetaDataCombi sid = steps.get(i);

            StepMeta stepMeta = sid.stepMeta;
            StepInterface baseStep = sid.step;

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
            
            // For partitioning to a set of remove steps (repartitioning from a master to a set or remote output steps)
            //
            StepPartitioningMeta targetStepPartitioningMeta = baseStep.getStepMeta().getTargetStepPartitioningMeta();
            if (targetStepPartitioningMeta!=null) {
              baseStep.setRepartitioning(targetStepPartitioningMeta.getMethodType());
            }
        }

        preparing=false;
        initializing = true;

        if (log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.InitialisingSteps", String.valueOf(steps.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        StepInitThread initThreads[] = new StepInitThread[steps.size()];
        Thread[] threads = new Thread[steps.size()];

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
                log.logError(Const.getStackTracker(ex));
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
                log.logError(BaseMessages.getString(PKG, "Trans.Log.StepFailedToInit", combi.stepname+"."+combi.copy));
                combi.data.setStatus(StepExecutionStatus.STATUS_STOPPED);
                ok=false;
            }
            else
            {
                combi.data.setStatus(StepExecutionStatus.STATUS_IDLE);
                if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.StepInitialized", combi.stepname+"."+combi.copy));
            }
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
                    combi.data.setStatus(StepExecutionStatus.STATUS_HALTED);
                }
                else
                {
                    combi.data.setStatus(StepExecutionStatus.STATUS_STOPPED);
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
            if (preview) {
            	String logText = CentralLogStore.getAppender().getBuffer(getLogChannelId(), true).toString();
            	throw new KettleException(BaseMessages.getString(PKG, "Trans.Log.FailToInitializeAtLeastOneStep")+Const.CR+logText); //$NON-NLS-1
            } else {
            	throw new KettleException(BaseMessages.getString(PKG, "Trans.Log.FailToInitializeAtLeastOneStep")+Const.CR); //$NON-NLS-1
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
    	nrOfActiveSteps=0;
    	
        for (int i=0;i<steps.size();i++)
        {
            final StepMetaDataCombi sid = steps.get(i);
            sid.step.markStart();
            sid.step.initBeforeStart();
            
            // also attach a Step Listener to detect when we're done...
            //
            StepListener stepListener = new StepListener() 
	            {
                    public void stepActive(Trans trans, StepMeta stepMeta, StepInterface step) {
                      nrOfActiveSteps++;
                      if (nrOfActiveSteps==1) {
                        // Transformation goes from in-active to active...
                        //
                        for (TransListener listener : transListeners) {
                          listener.transActive(Trans.this);
                        }
                      }
                    }
            
                    public void stepIdle(Trans trans, StepMeta stepMeta, StepInterface step) {
                      nrOfActiveSteps--;
                      if (nrOfActiveSteps==0) {
                        // Transformation goes from active to in-active...
                        //
                        for (TransListener listener : transListeners) {
                          listener.transIdle(Trans.this);
                        }                        
                      }
                    }

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
								
						        if (transMeta.isUsingUniqueConnections()) {
						            trans.closeUniqueDatabaseConnections(getResult());
						        }
						        
								try {
									fireTransFinishedListeners();
								} catch(Exception e) {
									step.setErrors(step.getErrors()+1L);
									log.logError(getName()+" : "+BaseMessages.getString(PKG, "Trans.Log.UnexpectedErrorAtTransformationEnd"), e); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
							
							// If a step fails with an error, we want to kill/stop the others too...
							//
							if (step.getErrors()>0) {
	
								log.logMinimal(getName(), BaseMessages.getString(PKG, "Trans.Log.TransformationDetectedErrors")); //$NON-NLS-1$ //$NON-NLS-2$
								log.logMinimal(getName(), BaseMessages.getString(PKG, "Trans.Log.TransformationIsKillingTheOtherSteps")); //$NON-NLS-1$
	
								killAllNoWait();
							}
						}
					}
				};
			sid.step.addStepListener(stepListener);
        }

    	if (transMeta.isCapturingStepPerformanceSnapShots()) 
    	{
    		stepPerformanceSnapshotSeqNr = new AtomicInteger(0);
    		stepPerformanceSnapShots = new ConcurrentHashMap<String, List<StepPerformanceSnapShot>>();
    		
    		// Calculate the maximum number of snapshots to be kept in memory
    		//
    		String limitString = environmentSubstitute(transMeta.getStepPerformanceCapturingSizeLimit());
        if (Const.isEmpty(limitString)) {
          limitString = EnvUtil.getSystemProperty(Const.KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT);
        }
        stepPerformanceSnapshotSizeLimit = Const.toInt(limitString, 0);
        
    		// Set a timer to collect the performance data from the running threads...
    		//
    		stepPerformanceSnapShotTimer = new Timer("stepPerformanceSnapShot Timer: " + transMeta.getName());
    		TimerTask timerTask = new TimerTask() {
				public void run() {
				  if (!isFinished()) {
					addStepPerformanceSnapShot();
				  }
				}
			};
    		stepPerformanceSnapShotTimer.schedule(timerTask, 100, transMeta.getStepPerformanceCapturingDelay());
    	}
    	
        // Now start a thread to monitor the running transformation...
        //
        finished.set(false);
        paused.set(false);
        stopped.set(false);
        
		TransListener transListener = new TransAdapter() {
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
        
        switch(transMeta.getTransformationType()) {
        case Normal:
        	
	        // Now start all the threads...
	    	//
	        for (int i=0;i<steps.size();i++)
	        {
	        	StepMetaDataCombi combi = steps.get(i);
	        	RunThread runThread = new RunThread(combi);
	        	Thread thread = new Thread(runThread);
	        	thread.setName(getName()+" - "+combi.stepname);
	        	thread.start();
	        }
	        break;
	    
        case SerialSingleThreaded:
        	new Thread(new Runnable() {
				public void run() {
					try {
						// Always disable thread priority management, it will always slow us down...
						//
						for (StepMetaDataCombi combi : steps) {
							combi.step.setUsingThreadPriorityManagment(false);
						}
						
						//
			        	// This is a single threaded version...
			        	//
						
						// Sort the steps from start to finish...
						//
						Collections.sort(steps, new Comparator<StepMetaDataCombi>() {
							public int compare(StepMetaDataCombi c1, StepMetaDataCombi c2) {
								
								boolean c1BeforeC2 = transMeta.findPrevious(c2.stepMeta, c1.stepMeta);
								if (c1BeforeC2) {
									return -1;
								} else {
									return 1;
								}
							}
						});
						
			        	boolean[] stepDone = new boolean[steps.size()];
			        	int nrDone = 0;
			        	while (nrDone<steps.size() && !isStopped()) {
			        		for (int i=0;i<steps.size() && !isStopped();i++) {
			        			StepMetaDataCombi combi = steps.get(i);
			        			if (!stepDone[i]) {
			        				// if (combi.step.canProcessOneRow() || !combi.step.isRunning()) {
				        				boolean cont = combi.step.processRow(combi.meta, combi.data);
				        				if (!cont) {
				        					stepDone[i] = true;
				        					nrDone++;
				        				}
			        				// }
			        			}
			        		}
			        	}
					} catch(Exception e) {
						errors.addAndGet(1);
						log.logError("Error executing single threaded", e);
					} finally {
						for (int i=0;i<steps.size();i++) {
		        			StepMetaDataCombi combi = steps.get(i);
							combi.step.dispose(combi.meta, combi.data);
							combi.step.markStop();
						}
					}
				}
			}).start();
			break;
			
        case SingleThreaded :
          // Don't do anything, this needs to be handled by the transformation executor!
          //
          break;

        	
        }
        
        
        
        if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.TransformationHasAllocated",String.valueOf(steps.size()),String.valueOf(rowsets.size()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    /**
     * 	Fire the listeners (if any are registered)
	 *	
     */
    protected void fireTransFinishedListeners() throws KettleException {
    	
		for (TransListener transListener : transListeners)
		{
			transListener.transFinished(this);
		}

	}
    
	protected void addStepPerformanceSnapShot() {
		
		if (stepPerformanceSnapShots==null) return; // Race condition somewhere?
		
		boolean pausedAndNotEmpty = isPaused() && !stepPerformanceSnapShots.isEmpty(); 
		boolean stoppedAndNotEmpty = isStopped() && !stepPerformanceSnapShots.isEmpty(); 
		
    	if (transMeta.isCapturingStepPerformanceSnapShots() && !pausedAndNotEmpty && !stoppedAndNotEmpty)
    	{
	        // get the statistics from the steps and keep them...
	    	//
    		int seqNr = stepPerformanceSnapshotSeqNr.incrementAndGet();
	        for (int i=0;i<steps.size();i++)
	        {
	            StepMeta stepMeta = steps.get(i).stepMeta;
	            StepInterface step = steps.get(i).step;
	            
	            StepPerformanceSnapShot snapShot = new StepPerformanceSnapShot(
	            		seqNr,
	            		getBatchId(),
	            		new Date(),
	            		getName(),
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
  	            snapShot.diff(previous, step.rowsetInputSize(), step.rowsetOutputSize());
                synchronized(stepPerformanceSnapShots) {
    	            snapShotList.add(snapShot);

    	            if (stepPerformanceSnapshotSizeLimit>0 && snapShotList.size()>stepPerformanceSnapshotSizeLimit) {
    	              snapShotList.remove(0);
    	            }
	            }
	        }
	        
	        lastStepPerformanceSnapshotSeqNrAdded = stepPerformanceSnapshotSeqNr.get();
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
		log.logBasic(si.getStepname(), BaseMessages.getString(PKG, "Trans.Log.FinishedProcessing",String.valueOf(si.getLinesInput()),String.valueOf(si.getLinesOutput()),String.valueOf(si.getLinesRead()))+BaseMessages.getString(PKG, "Trans.Log.FinishedProcessing2",String.valueOf(si.getLinesWritten()),String.valueOf(si.getLinesUpdated()),String.valueOf(si.getErrors()))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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
			log.logError(BaseMessages.getString(PKG, "Trans.Log.TransformationError")+e.toString()); //$NON-NLS-1$
            log.logError(Const.getStackTracker(e)); //$NON-NLS-1$
		}
	}

	public int getErrors()
	{
		int nrErrors = errors.get();
		
        if (steps==null) return nrErrors;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			if (sid.step.getErrors()!=0L) nrErrors+=sid.step.getErrors();
		}
		if (nrErrors>0) log.logError(BaseMessages.getString(PKG, "Trans.Log.TransformationErrorsDetected")); //$NON-NLS-1$

		return nrErrors;
	}

	public int getEnded()
	{
		int nrEnded=0;

		if (steps==null) return 0;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepDataInterface data = sid.data;
            
			if ((sid.step!=null && !sid.step.isRunning()) ||  // Should normally not be needed anymore, status is kept in data.
                    data.getStatus()==StepExecutionStatus.STATUS_FINISHED || // Finished processing 
                    data.getStatus()==StepExecutionStatus.STATUS_HALTED ||   // Not launching because of init error
                    data.getStatus()==StepExecutionStatus.STATUS_STOPPED     // Stopped because of an error
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
			
			if (log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "Trans.Log.LookingAtStep")+sid.step.getStepname()); //$NON-NLS-1$
			
			// If thr is a mapping, this is cause for an endless loop
			//
			while (sid.step.isRunning())
			{
				sid.step.stopAll();
				try
				{
					Thread.sleep(20);
				}
				catch(Exception e)
				{
					log.logError(BaseMessages.getString(PKG, "Trans.Log.TransformationErrors")+e.toString()); //$NON-NLS-1$
					return;
				}
			}
			
			if (!sid.step.isRunning()) nrStepsFinished++;
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
			StepInterface step = sid.step;
			
			if (log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "Trans.Log.LookingAtStep")+step.getStepname()); //$NON-NLS-1$
			
			step.stopAll();
			try
			{
				Thread.sleep(20);
			}
			catch(Exception e)
			{
				log.logError(BaseMessages.getString(PKG, "Trans.Log.TransformationErrors")+e.toString()); //$NON-NLS-1$
				return;
			}
		}
	}

	public void printStats(int seconds)
	{
		log.logBasic(" "); //$NON-NLS-1$
		if (steps==null) return;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepInterface step = sid.step;
			long proc = step.getProcessed();
			if (seconds!=0)
			{
				if (step.getErrors()==0)
				{
					log.logBasic(BaseMessages.getString(PKG, "Trans.Log.ProcessSuccessfullyInfo",step.getStepname(),"."+step.getCopy(),String.valueOf(proc),String.valueOf((proc/seconds)))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(BaseMessages.getString(PKG, "Trans.Log.ProcessErrorInfo",step.getStepname(),"."+step.getCopy(),String.valueOf(step.getErrors()),String.valueOf(proc),String.valueOf(proc/seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
			}
			else
			{
				if (step.getErrors()==0)
				{
					log.logBasic(BaseMessages.getString(PKG, "Trans.Log.ProcessSuccessfullyInfo",step.getStepname(),"."+step.getCopy(),String.valueOf(proc),seconds!=0 ? String.valueOf((proc/seconds)) : "-")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
				}
				else
				{
					log.logError(BaseMessages.getString(PKG, "Trans.Log.ProcessErrorInfo2",step.getStepname(),"."+step.getCopy(),String.valueOf(step.getErrors()),String.valueOf(proc),String.valueOf(seconds))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
				}
			}
		}
	}

	public long getLastProcessed()
	{
		if (steps==null || steps.size()==0) return 0L;
		StepMetaDataCombi sid = steps.get(steps.size()-1);
		return sid.step.getProcessed();
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
			StepInterface rt = sid.step;
			rt.setStopped(true);
			rt.resumeRunning();

			// Cancel queries etc. by force...
			StepInterface si = (StepInterface)rt;
            try
            {
                si.stopRunning(sid.meta, sid.data);
            }
            catch(Exception e)
            {
                log.logError("Something went wrong while trying to stop the transformation: "+e.toString());
                log.logError(Const.getStackTracker(e));
            }
            
            sid.data.setStatus(StepExecutionStatus.STATUS_STOPPED);
		}
		
		//if it is stopped it is not paused
		paused.set(false);
		stopped.set(true);
		
		// Fire the stopped listener...
		//
		for (TransStoppedListener listener : transStoppedListeners) {
		  listener.transStopped(this);
		}
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
			if ( sid.step.isRunning() ) nr++;
		}
		return nr;
	}

	public StepInterface getRunThread(int i)
	{
		if (steps==null) return null;
		return steps.get(i).step;
	}

	public StepInterface getRunThread(String name, int copy)
	{
		if (steps==null) return null;

		for(int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepInterface step = sid.step;
			if (step.getStepname().equalsIgnoreCase(name) && step.getCopy()==copy)
			{
				return step;
			}
		}

		return null;
	}
	
	public void calculateBatchIdAndDateRange() throws KettleTransException {
		
		TransLogTable transLogTable = transMeta.getTransLogTable(); 

		currentDate = new Date();
		logDate     = new Date();
		startDate   = Const.MIN_DATE;
		endDate     = currentDate;
		
		DatabaseMeta logConnection = transLogTable.getDatabaseMeta();
		String logTable = environmentSubstitute(transLogTable.getActualTableName());
		String logSchema = environmentSubstitute(transLogTable.getActualSchemaName());

		try
        {
			if (logConnection!=null)
			{
				
	      String logSchemaAndTable = logConnection.getQuotedSchemaTableCombination(logSchema, logTable);
				if ( Const.isEmpty(logTable) )
				{
				    // It doesn't make sense to start database logging without a table
					// to log to.
					throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.NoLogTableDefined")); //$NON-NLS-1$ //$NON-NLS-2$
				}
	            if (Const.isEmpty(transMeta.getName()) && logConnection!=null && logTable!=null)
	            {
	                throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.NoTransnameAvailableForLogging"));
	            }
			    transLogTableDatabaseConnection = new Database(this, logConnection);
			    transLogTableDatabaseConnection.shareVariablesWith(this);
			    if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.OpeningLogConnection",""+logConnection)); //$NON-NLS-1$ //$NON-NLS-2$
			    transLogTableDatabaseConnection.connect();
				
				// See if we have to add a batch id...
				// Do this first, before anything else to lock the complete table exclusively
				//
				if (transLogTable.isBatchIdUsed())
				{
					Long id_batch = logConnection.getNextBatchId(transLogTableDatabaseConnection, logSchema, logTable, transLogTable.getKeyField().getFieldName());
					setBatchId( id_batch.longValue() );
				}

				//
				// Get the date range from the logging table: from the last end_date to now. (currentDate)
				//
                Object[] lastr= transLogTableDatabaseConnection.getLastLogDate(logSchemaAndTable, transMeta.getName(), false, LogStatus.END); //$NON-NLS-1$
				if (lastr!=null && lastr.length>0)
				{
                    startDate = (Date) lastr[0]; 
                    if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.StartDateFound")+startDate); //$NON-NLS-1$
				}

				//
				// OK, we have a date-range.
				// However, perhaps we need to look at a table before we make a final judgment?
				//
				if (transMeta.getMaxDateConnection()!=null &&
					transMeta.getMaxDateTable()!=null && transMeta.getMaxDateTable().length()>0 &&
					transMeta.getMaxDateField()!=null && transMeta.getMaxDateField().length()>0
					)
				{
					if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.LookingForMaxdateConnection",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
					DatabaseMeta maxcon = transMeta.getMaxDateConnection();
					if (maxcon!=null)
					{
						Database maxdb = new Database(this, maxcon);
						maxdb.shareVariablesWith(this);
						try
						{
							if(log.isDetailed())  log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.OpeningMaximumDateConnection")); //$NON-NLS-1$
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
									if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.LastDateFoundOnTheMaxdateConnection")+r1); //$NON-NLS-1$
									endDate.setTime( (long)( maxvalue.getTime() + ( transMeta.getMaxDateOffset()*1000 ) ));
								}
							}
							else
							{
								if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.NoLastDateFoundOnTheMaxdateConnection")); //$NON-NLS-1$
							}
						}
						catch(KettleException e)
						{
							throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.ErrorConnectingToDatabase",""+transMeta.getMaxDateConnection()), e); //$NON-NLS-1$ //$NON-NLS-2$
						}
						finally
						{
							maxdb.disconnect();
						}
					}
					else
					{
						throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.MaximumDateConnectionCouldNotBeFound",""+transMeta.getMaxDateConnection())); //$NON-NLS-1$ //$NON-NLS-2$
					}
				}

				// Determine the last date of all dependend tables...
				// Get the maximum in depdate...
				if (transMeta.nrDependencies()>0)
				{
					if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.CheckingForMaxDependencyDate")); //$NON-NLS-1$
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
							Database depdb = new Database(this, depcon);
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
										if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.FoundDateFromTable",td.getTablename(),"."+td.getFieldname()," = "+maxvalue.toString())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
										if ( maxvalue.getTime() > maxdepdate.getTime() )
										{
											maxdepdate=maxvalue;
										}
									}
									else
									{
										throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
									}
								}
								else
								{
									throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.UnableToGetDependencyInfoFromDB",td.getDatabase().getName()+".",td.getTablename()+".",td.getFieldname())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
								}
							}
							catch(KettleException e)
							{
								throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.ErrorInDatabase",""+td.getDatabase()), e); //$NON-NLS-1$ //$NON-NLS-2$
							}
							finally
							{
								depdb.disconnect();
							}
						}
						else
						{
							throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.ConnectionCouldNotBeFound",""+td.getDatabase())); //$NON-NLS-1$ //$NON-NLS-2$
						}
						if(log.isDetailed()) log.logDetailed(BaseMessages.getString(PKG, "Trans.Log.Maxdepdate")+(XMLHandler.date2string(maxdepdate))); //$NON-NLS-1$ //$NON-NLS-2$
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
            
				
        }
		catch(KettleException e)
		{
			throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.ErrorCalculatingDateRange", logTable), e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		finally
		{
			// Be careful, We DO NOT close the trans log table database connection!!!
			// It's closed later in beginProcessing() to prevent excessive connect/disconnect repetitions.
		}

	}

	//
	// Handle logging at start
	public void beginProcessing() throws KettleTransException
	{
		TransLogTable transLogTable = transMeta.getTransLogTable(); 
        int intervalInSeconds = Const.toInt( environmentSubstitute(transLogTable.getLogInterval()), -1);

        try
		{
			String logTable = transLogTable.getActualTableName();

			SimpleDateFormat df = new SimpleDateFormat(REPLAY_DATE_FORMAT);
			log.logBasic(BaseMessages.getString(PKG, "Trans.Log.TransformationCanBeReplayed") + df.format(currentDate)); //$NON-NLS-1$

            try
            {
                if (transLogTableDatabaseConnection!=null && !Const.isEmpty(logTable) && !Const.isEmpty(transMeta.getName()))
                {
                	transLogTableDatabaseConnection.writeLogRecord(transLogTable, LogStatus.START, this, null);
                    
                    // If we need to do periodic logging, make sure to install a timer for this...
                    //
                    if (intervalInSeconds>0) {
	                    final Timer timer = new Timer(getName()+" - interval logging timer");
	                    TimerTask timerTask = new TimerTask() {
	            			public void run() {
	            				try {
	            					endProcessing();
	            				} catch(Exception e) {
	            					log.logError(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformIntervalLogging"), e);
	            					// Also stop the show...
	            					//
	            					errors.incrementAndGet();
	            					stopAll();
	            				}
	            			}
	                    };
	                    timer.schedule(timerTask, intervalInSeconds*1000, intervalInSeconds*1000);
	                    
	                    addTransListener(new TransAdapter() {
	    					public void transFinished(Trans trans) {
	    						timer.cancel();						
	    					}
	    				});
                    }
                    
                    // Add a listener to make sure that the last record is also written when transformation finishes...
                    //
                    addTransListener(new TransAdapter() {
    					public void transFinished(Trans trans) throws KettleException {
    						try {
	    						endProcessing();

	    						lastWrittenStepPerformanceSequenceNr = writeStepPerformanceLogRecords(lastWrittenStepPerformanceSequenceNr, LogStatus.END);

    						} catch(KettleException e) {
    							throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformLoggingAtTransEnd"), e);
    						}
    					}
    				});
                    
                }

                // If we need to write out the step logging information, do so at the end of the transformation too...
                //
                StepLogTable stepLogTable = transMeta.getStepLogTable();
                if (stepLogTable.isDefined()) {
                    addTransListener(new TransAdapter() {
    					public void transFinished(Trans trans) throws KettleException {
    						try {
    							writeStepLogInformation();
    						} catch(KettleException e) {
    							throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformLoggingAtTransEnd"), e);
    						}
    					}
    				});
                }
                
                // If we need to write the log channel hierarchy and lineage information, add a listener for that too... 
                //
                ChannelLogTable channelLogTable = transMeta.getChannelLogTable();
                if (channelLogTable.isDefined()) {
                    addTransListener(new TransAdapter() {
    					public void transFinished(Trans trans) throws KettleException {
    						try {
    							writeLogChannelInformation();
    						} catch(KettleException e) {
    							throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformLoggingAtTransEnd"), e);
    						}
    					}
    				});
                }
                
                // See if we need to write the step performance records at intervals too...
                //
                PerformanceLogTable performanceLogTable = transMeta.getPerformanceLogTable();
                int perfLogInterval = Const.toInt( environmentSubstitute(performanceLogTable.getLogInterval()), -1); 
                if (performanceLogTable.isDefined() && perfLogInterval>0) {
                    final Timer timer = new Timer(getName()+" - step performance log interval timer"); // $NON-NLS-1$
                    TimerTask timerTask = new TimerTask() {
            			public void run() {
            				try {
            					lastWrittenStepPerformanceSequenceNr = writeStepPerformanceLogRecords(lastWrittenStepPerformanceSequenceNr, LogStatus.RUNNING);
            				} catch(Exception e) {
            					log.logError(BaseMessages.getString(PKG, "Trans.Exception.UnableToPerformIntervalPerformanceLogging"), e);
            					// Also stop the show...
            					//
            					errors.incrementAndGet();
            					stopAll();
            				}
            			}
                    };
                    timer.schedule(timerTask, perfLogInterval*1000, perfLogInterval*1000);
                    
                    addTransListener(new TransAdapter() {
    					public void transFinished(Trans trans) {
    						timer.cancel();						
    					}
    				});
                }
            }
			catch(KettleException e)
			{
				throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.ErrorWritingLogRecordToTable", logTable), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			finally
			{
				// If we use interval logging, we keep the connection open for performance reasons...
				//
				if (transLogTableDatabaseConnection!=null && (intervalInSeconds<=0)) {
					transLogTableDatabaseConnection.disconnect();
					transLogTableDatabaseConnection = null;
				}
			}
		}
		catch(KettleException e)
		{
			throw new KettleTransException(BaseMessages.getString(PKG, "Trans.Exception.UnableToBeginProcessingTransformation"), e); //$NON-NLS-1$
		}
	}

	protected void writeLogChannelInformation() throws KettleException {
		Database db = null;
		ChannelLogTable channelLogTable = transMeta.getChannelLogTable();
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

	protected void writeStepLogInformation() throws KettleException {
		Database db = null;
		StepLogTable stepLogTable = transMeta.getStepLogTable();
		try {
			db = new Database(this, stepLogTable.getDatabaseMeta());
			db.shareVariablesWith(this);
			db.connect();
			
			for (StepMetaDataCombi combi : steps) {
				db.writeLogRecord(stepLogTable, LogStatus.START, combi, null);
			}
			
		} catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.UnableToWriteStepInformationToLogTable"), e);
		} finally {
			db.disconnect();
		}
		
	}

	public Result getResult()
	{
		if (steps==null) return null;

		Result result = new Result();
		result.setNrErrors(errors.longValue());
		TransLogTable transLogTable = transMeta.getTransLogTable();

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepInterface step = sid.step;

			result.setNrErrors(result.getNrErrors()+sid.step.getErrors());
			result.getResultFiles().putAll(step.getResultFiles());
			
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_READ))) result.setNrLinesRead(result.getNrLinesRead()+ step.getLinesRead());
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_INPUT))) result.setNrLinesInput(result.getNrLinesInput() + step.getLinesInput());
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_WRITTEN))) result.setNrLinesWritten(result.getNrLinesWritten()+step.getLinesWritten());
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_OUTPUT))) result.setNrLinesOutput(result.getNrLinesOutput()+step.getLinesOutput());
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_UPDATED))) result.setNrLinesUpdated(result.getNrLinesUpdated()+step.getLinesUpdated());
			if (step.getStepname().equals(transLogTable.getSubjectString(TransLogTable.ID.LINES_REJECTED))) result.setNrLinesRejected(result.getNrLinesRejected()+step.getLinesRejected());
		}

		result.setRows( transMeta.getResultRows() );
		result.setStopped( isStopped() );
		result.setLogChannelId(log.getLogChannelId());

		return result;
	}

	//
	// Handle logging at end
	//
	private synchronized boolean endProcessing() throws KettleException
	{
		LogStatus status;
		
		if (isFinished()) {
			if (isStopped()) {
				status = LogStatus.STOP;
			} else {
				status = LogStatus.END;
			}
		} else if (isPaused()) {
			status = LogStatus.PAUSED;
		} else {
			status = LogStatus.RUNNING;
		}

		TransLogTable transLogTable = transMeta.getTransLogTable(); 
        int intervalInSeconds = Const.toInt( environmentSubstitute(transLogTable.getLogInterval()), -1);
		
		logDate     = new Date();

		// OK, we have some logging to do...
		//
		DatabaseMeta logcon = transMeta.getTransLogTable().getDatabaseMeta();
		String logTable = transMeta.getTransLogTable().getActualTableName();
		if (logcon!=null)
		{
			Database ldb = null;
			
			try
			{
				// Let's not reconnect/disconnect all the time for performance reasons!
				//
				if (transLogTableDatabaseConnection==null) {
					ldb = new Database(this, logcon);
					ldb.shareVariablesWith(this);
					ldb.connect();
					transLogTableDatabaseConnection=ldb;
				} else {
					ldb = transLogTableDatabaseConnection;
				}

				// Write to the standard transformation log table...
				//
				if (!Const.isEmpty(logTable)) {
                	ldb.writeLogRecord(transLogTable, status, this, null);
				}
				
				// Also time-out the log records in here...
				//
				if (status.equals(LogStatus.END) || status.equals(LogStatus.STOP)) {
					ldb.cleanupLogRecords(transLogTable);
				}
			}
			catch(Exception e)
			{
				throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.ErrorWritingLogRecordToTable", transMeta.getTransLogTable().getActualTableName()), e); //$NON-NLS-1$ //$NON-NLS-2$
			}
			finally
			{
				if (intervalInSeconds<=0 || (status.equals(LogStatus.END) || status.equals(LogStatus.STOP)) ) {
					ldb.disconnect();
					transLogTableDatabaseConnection = null; // disconnected
				}
			}
		}
		return true;
	}

	private int writeStepPerformanceLogRecords(int startSequenceNr, LogStatus status) throws KettleException {
		int lastSeqNr = 0;
		Database ldb = null;
		PerformanceLogTable performanceLogTable = transMeta.getPerformanceLogTable();
		
		if (!performanceLogTable.isDefined() || !transMeta.isCapturingStepPerformanceSnapShots() || stepPerformanceSnapShots==null || stepPerformanceSnapShots.isEmpty()) {
			return 0; // nothing to do here!
		}
		
		try {
			ldb = new Database(this, performanceLogTable.getDatabaseMeta());
			ldb.shareVariablesWith(this);
			ldb.connect();
			
			// Write to the step performance log table...
			//
			RowMetaInterface rowMeta = performanceLogTable.getLogRecord(LogStatus.START, null, null).getRowMeta();
			ldb.prepareInsert(rowMeta, performanceLogTable.getActualSchemaName(), performanceLogTable.getActualTableName());
			
			synchronized(stepPerformanceSnapShots) {
				Iterator<List<StepPerformanceSnapShot>> iterator = stepPerformanceSnapShots.values().iterator();
				while(iterator.hasNext()) {
					List<StepPerformanceSnapShot> snapshots = iterator.next();
					synchronized(snapshots) {
					  Iterator<StepPerformanceSnapShot> snapshotsIterator = snapshots.iterator();
					  while(snapshotsIterator.hasNext()) {
					    StepPerformanceSnapShot snapshot=snapshotsIterator.next();
              if (snapshot.getSeqNr()>=startSequenceNr && snapshot.getSeqNr()<=lastStepPerformanceSnapshotSeqNrAdded) {
                
                RowMetaAndData row = performanceLogTable.getLogRecord(LogStatus.START, snapshot, null);
                
                ldb.setValuesInsert(row.getRowMeta(), row.getData());
                ldb.insertRow(true);
              }
              lastSeqNr = snapshot.getSeqNr();
					  }
					}
				}
			}
			
			ldb.insertFinished(true);
			
			// Finally, see if the log table needs cleaning up...
			//
			if (status.equals(LogStatus.END)) {
				ldb.cleanupLogRecords(performanceLogTable);
			}

		} catch(Exception e) {
			throw new KettleException(BaseMessages.getString(PKG, "Trans.Exception.ErrorWritingStepPerformanceLogRecordToTable"), e);
		} finally {
			if (ldb!=null) {
				ldb.disconnect();
			}
		}
		
		return lastSeqNr+1;
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
	        		database.closeConnectionOnly();
	        		
	        		// Remove the database from the list...
	        		//
	        		map.removeConnection(database.getConnectionGroup(), database.getPartitionId(), database);
        		}
        		catch(Exception e) {
        			log.logError(BaseMessages.getString(PKG, "Trans.Exception.ErrorHandlingTransformationTransaction", database.toString()), e);
        			result.setNrErrors(result.getNrErrors()+1);
        		}
        	}
        }
	}

	public StepInterface findRunThread(String stepname)
	{
		if (steps==null) return null;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepInterface step = sid.step;
			if (step.getStepname().equalsIgnoreCase(stepname)) return step;
		}
		return null;
	}
	
	public List<StepInterface> findBaseSteps(String stepname)
	{
		List<StepInterface> baseSteps = new ArrayList<StepInterface>();
		
		if (steps==null) return baseSteps;

		for (int i=0;i<steps.size();i++)
		{
			StepMetaDataCombi sid = steps.get(i);
			StepInterface stepInterface = sid.step;
			if (stepInterface.getStepname().equalsIgnoreCase(stepname)) {
				baseSteps.add(stepInterface);
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
            StepInterface rt = sid.step;
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
        RowSet rowSet;
        switch(transMeta.getTransformationType()) {
        case Normal:
          rowSet = new BlockingRowSet(transMeta.getSizeRowset());
          break;
        case SerialSingleThreaded:
          rowSet = new SingleRowRowSet();
          break;
        case SingleThreaded:
          rowSet = new QueueRowSet();
          break;
        default:
          throw new KettleException("Unhandled transformation type: "+transMeta.getTransformationType());
        }

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
      this.logLevel = parentJob.getLogLevel();
      this.log.setLogLevel(logLevel);
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
            if (sid.data.getStatus()==StepExecutionStatus.STATUS_HALTED) return true;
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
	            else if (isPaused()) {
	            	message = STRING_PAUSED;
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
            
            // Keep track of the various Carte object IDs
            //
            final Map<TransMeta, String> carteObjectMap = transSplitter.getCarteObjectMap();
            
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
                    carteObjectMap.put(master, webResult.getId());
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
                              carteObjectMap.put(slaveTrans, webResult.getId());
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
                        String masterReply = masterServer.execService(PrepareExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(master.getName(), "UTF-8")+"&xml=Y");
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
                        String slaveReply = slaves[i].execService(PrepareExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(slaveTrans.getName(), "UTF-8")+"&xml=Y");
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
                        String masterReply = masterServer.execService(StartExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(master.getName(), "UTF-8")+"&xml=Y");
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
                        String slaveReply = slaves[i].execService(StartExecutionTransServlet.CONTEXT_PATH+"/?name="+URLEncoder.encode(slaveTrans.getName(), "UTF-8")+"&xml=Y");
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
	public static final long monitorClusteredTransformation(LogChannelInterface log, TransSplitter transSplitter, Job parentJob)
	{
		return monitorClusteredTransformation(log, transSplitter, parentJob, 1); // monitor every 1 seconds
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
    public static final long monitorClusteredTransformation(LogChannelInterface log, TransSplitter transSplitter, Job parentJob, int sleepTimeSeconds)
    {
        long errors = 0L;

        //
        // See if the remote transformations have finished.
        // We could just look at the master, but I doubt that that is enough in all situations.
        //
        SlaveServer[] slaveServers = transSplitter.getSlaveTargets(); // <-- ask these guys
        TransMeta[] slaves = transSplitter.getSlaves();
        Map<TransMeta, String> carteObjectMap = transSplitter.getCarteObjectMap();

        SlaveServer masterServer;
		try {
			masterServer = transSplitter.getMasterServer();
		} catch (KettleException e) {
			log.logError("Error getting the master server", e);
			masterServer = null;
			errors++;
		}
        TransMeta masterTransMeta = transSplitter.getMaster();
        // TransMeta transMeta = transSplitter.getOriginalTransformation();

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
                	String carteObjectId = carteObjectMap.get(slaves[s]);
                    SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus(slaves[s].getName(), carteObjectId, 0); 
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed("Slave transformation on '"+slaveServers[s]+"' is still running.");
                    	allFinished = false;
                    }
                    else {
                    	if(log.isDetailed()) log.logDetailed("Slave transformation on '"+slaveServers[s]+"' has finished.");
                    }
                    errors+=transStatus.getNrStepErrors();
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError("Unable to contact slave server '"+slaveServers[s].getName()+"' to check slave transformation : "+e.toString());
                }
            }

            // Check the master too
            if (allFinished && errors==0 && masterTransMeta!=null && masterTransMeta.nrSteps()>0)
            {
                try
                {
                	String carteObjectId = carteObjectMap.get(masterTransMeta);
                    SlaveServerTransStatus transStatus = masterServer.getTransStatus(masterTransMeta.getName(), carteObjectId, 0);
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed("Master transformation is still running.");
                    	allFinished = false;
                    }
                    else {
                    	if(log.isDetailed()) log.logDetailed("Master transformation has finished.");
                    }
                    Result result = transStatus.getResult(transSplitter.getOriginalTransformation());
                    errors+=result.getNrErrors();
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError("Unable to contact master server '"+masterServer.getName()+"' to check master transformation : "+e.toString());
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
                    	String carteObjectId = carteObjectMap.get(slaves[s]);
                    	WebResult webResult = slaveServers[s].stopTransformation(slaves[s].getName(), carteObjectId);
                        if (!WebResult.STRING_OK.equals(webResult.getResult()))
                        {
                            log.logError("Unable to stop slave transformation '"+slaves[s].getName()+"' : "+webResult.getMessage());
                        }
                    }
                    catch(Exception e)
                    {
                        errors+=1;
                        log.logError("Unable to contact slave server '"+slaveServers[s].getName()+"' to stop transformation : "+e.toString());
                    }
                }

                try
                {
                	String carteObjectId = carteObjectMap.get(masterTransMeta);
                	WebResult webResult = masterServer.stopTransformation(masterTransMeta.getName(), carteObjectId);
                    if (!WebResult.STRING_OK.equals(webResult.getResult()))
                    {
                        log.logError("Unable to stop master transformation '"+masterServer.getName()+"' : "+webResult.getMessage());
                    }
                }
                catch(Exception e)
                {
                    errors+=1;
                    log.logError("Unable to contact master server '"+masterServer.getName()+"' to stop the master : "+e.toString());
                }
            }

            //
            // Keep waiting until all transformations have finished
            // If needed, we stop them again and again until they yield.
            //
            if (!allFinished)
            {
                // Not finished or error: wait a bit longer
            	if(log.isDetailed()) log.logDetailed("Clustered transformation is still running, waiting a few seconds...");
                try { Thread.sleep(sleepTimeSeconds*2000); } catch(Exception e) {} // Check all slaves every x seconds. 
            }
        }
        
        log.logBasic("All transformations in the cluster have finished.");
        
        errors+=cleanupCluster(log, transSplitter);
        
        return errors;
    }
    
    public static int cleanupCluster(LogChannelInterface log, TransSplitter transSplitter) {
      
      SlaveServer[] slaveServers  = transSplitter.getSlaveTargets();
      TransMeta[] slaves = transSplitter.getSlaves();
      SlaveServer masterServer;
      try {
        masterServer = transSplitter.getMasterServer();
      } catch (KettleException e) {
        log.logError("Unable to obtain the master server from the cluster", e);
        return 1;
      }
      TransMeta masterTransMeta = transSplitter.getMaster();
      int errors = 0;
      
      // All transformations have finished, with or without error.
      // Now run a cleanup on all the transformation on the master and the slaves.
      //
      // Slaves first...
      //
      for (int s=0;s<slaveServers.length;s++)
      {
          try
          {
            cleanupSlaveServer(transSplitter, slaveServers[s], slaves[s].getName());
          }
          catch(Exception e)
          {
              errors++;
              log.logError("Unable to contact slave server '"+slaveServers[s].getName()+"' to clean up slave transformation", e);
          }
      }

      // Clean up  the master too
      //
      if (masterTransMeta!=null && masterTransMeta.nrSteps()>0)
      {
          try
          {
            cleanupSlaveServer(transSplitter, masterServer, masterTransMeta.getName());
          }
          catch(Exception e)
          {
              errors++;
              log.logError("Unable to contact master server '"+masterServer.getName()+"' to clean up master transformation", e);
          }
      }   
      
      return errors;
    }

    public static void cleanupSlaveServer(TransSplitter transSplitter, SlaveServer slaveServer, String transName) throws KettleException {
      try {
        String carteObjectId = transSplitter.getCarteObjectMap().get(slaveServer);
        WebResult webResult = slaveServer.cleanupTransformation(transName, carteObjectId);
        if (!WebResult.STRING_OK.equals(webResult.getResult()))
        {
            throw new KettleException("Unable to run clean-up on slave server '"+slaveServer+"' for transformation '"+transName+"' : "+webResult.getMessage());
        }
      } catch(Exception e) {
        throw new KettleException("Unexpected error contacting slave server '"+slaveServer+"' to clear up transformation '"+transName+"'", e);
      }
    }

    public static final Result getClusteredTransformationResult(LogChannelInterface log, TransSplitter transSplitter, Job parentJob)
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
			log.logError("Error getting the master server", e);
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
            	SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus(slaves[s].getName(), "", 0);
            	Result transResult = transStatus.getResult(slaves[s]);
            	
            	result.add(transResult);
            }
            catch(Exception e)
            {
    			result.setNrErrors(result.getNrErrors()+1);
                log.logError("Unable to contact slave server '"+slaveServers[s].getName()+"' to get result of slave transformation : "+e.toString());
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
            	SlaveServerTransStatus transStatus = masterServer.getTransStatus(master.getName(), "", 0);
            	Result transResult = transStatus.getResult(master);
            	
            	result.add(transResult);
            }
            catch(Exception e)
            {
            	result.setNrErrors(result.getNrErrors()+1);
                log.logError("Unable to contact master server '"+masterServer.getName()+"' to get result of master transformation : "+e.toString());
            }
        }
        
        
        return result;
    }
    
	
    /**
     * Send the transformation for execution to a carte slave server
     * @param transMeta
     * @param executionConfiguration
     * @param repository
     * @return The carte object ID on the server.
     * @throws KettleException
     */
	public static String sendToSlaveServer(TransMeta transMeta, TransExecutionConfiguration executionConfiguration, Repository repository) throws KettleException
	{
	  String carteObjectId;
		SlaveServer slaveServer = executionConfiguration.getRemoteServer();

		if (slaveServer == null)
			throw new KettleException("No slave server specified");
		if (Const.isEmpty(transMeta.getName()))
			throw new KettleException( "The transformation needs a name to uniquely identify it by on the remote server.");

		try
		{
			// Inject certain internal variables to make it more intuitive. 
			// 
		  Map <String, String> vars = new HashMap<String, String>();
      
      for (String var : Const.INTERNAL_TRANS_VARIABLES) vars.put(var, transMeta.getVariable(var));
      for (String var : Const.INTERNAL_JOB_VARIABLES) vars.put(var, transMeta.getVariable(var));

      executionConfiguration.getVariables().putAll(vars);
      slaveServer.injectVariables(executionConfiguration.getVariables());
      
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
				carteObjectId=webResult.getId();
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
        carteObjectId=webResult.getId();
			}
	
			// Prepare the transformation
			//
			String reply = slaveServer.execService(PrepareExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(transMeta.getName(), "UTF-8") + "&xml=Y&id="+carteObjectId);
			WebResult webResult = WebResult.fromXMLString(reply);
			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException("There was an error preparing the transformation for excution on the remote server: "+ Const.CR + webResult.getMessage());
			}

			// Start the transformation
			//
			reply = slaveServer.execService(StartExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(transMeta.getName(), "UTF-8") + "&xml=Y&id="+carteObjectId);
			webResult = WebResult.fromXMLString(reply);

			if (!webResult.getResult().equalsIgnoreCase(WebResult.STRING_OK))
			{
				throw new KettleException( "There was an error starting the transformation on the remote server: " + Const.CR + webResult.getMessage());
			}
			
			return carteObjectId;
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
            catch(KettleFileException e)
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
        variables.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, transMeta.getRepositoryDirectory()!=null?transMeta.getRepositoryDirectory().getPath():"");
        
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
		if (transMeta!=null) {
		  transMeta.setRepository(repository);
		}
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
	
	public void setTransStoppedListeners(List<TransStoppedListener> transStoppedListeners) {
      this.transStoppedListeners = transStoppedListeners;
    }

	public List<TransStoppedListener> getTransStoppedListeners() {
      return transStoppedListeners;
    }
	
	public void addTransStoppedListener(TransStoppedListener transStoppedListener) {
	    transStoppedListeners.add(transStoppedListener);
	}

	public boolean isPaused() {
		return paused.get();
	}

	public boolean isStopped() {
		return stopped.get();
	}

	public static void monitorRemoteTransformation(LogChannelInterface log, String carteObjectId, String transName, SlaveServer remoteSlaveServer) {
		monitorRemoteTransformation(log, carteObjectId, transName, remoteSlaveServer, 5);
	}
	
	public static void monitorRemoteTransformation(LogChannelInterface log, String carteObjectId, String transName, SlaveServer remoteSlaveServer, int sleepTimeSeconds) {
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
                    SlaveServerTransStatus transStatus = remoteSlaveServer.getTransStatus(transName, carteObjectId, 0);
                    if (transStatus.isRunning()) {
                    	if(log.isDetailed()) log.logDetailed(transName, "Remote transformation is still running.");
                    	allFinished = false;
                    } else {
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
            	if (log.isDetailed()) log.logDetailed(transName, "The remote transformation is still running, waiting a few seconds...");
                try { Thread.sleep(sleepTimeSeconds*1000); } catch(Exception e) {} // Check all slaves every x seconds. 
            }
        }
        
        log.logMinimal(transName, "The remote transformation has finished.");
        
        // Clean up the remote transformation
        //
        try
        {
            WebResult webResult = remoteSlaveServer.cleanupTransformation(transName, carteObjectId);
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
    this.logLevel = parentTrans.getLogLevel();
    this.log.setLogLevel(logLevel);
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
	
	public String getObjectName() {
		return getName();
	}

	public String getObjectCopy() {
		return null;
	}

	public String getFilename() {
		if (transMeta==null) return null;
		return transMeta.getFilename();
	}

	public String getLogChannelId() {
		return log.getLogChannelId();
	}

	public ObjectId getObjectId() {
		if (transMeta==null) return null;
		return transMeta.getObjectId();
	}

	public ObjectRevision getObjectRevision() {
		if (transMeta==null) return null;
		return transMeta.getObjectRevision();
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.TRANS;
	}

	public LoggingObjectInterface getParent() {
		return parent;
	}

	public RepositoryDirectoryInterface getRepositoryDirectory() {
		if (transMeta==null) return null;
		return transMeta.getRepositoryDirectory();
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
	
	public Map<String, Trans> getActiveSubtransformations() {
		return activeSubtransformations;
	}

  /**
   * @return the carteObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the carteObjectId to set
   */
  public void setContainerObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;

  }
}