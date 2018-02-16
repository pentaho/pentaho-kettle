//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Queue;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.BlockingBatchingRowSet;
import org.pentaho.di.core.BlockingRowSet;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.ExecutorInterface;
import org.pentaho.di.core.ExtensionDataInterface;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.QueueRowSet;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.SingleRowRowSet;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTransactionListener;
import org.pentaho.di.core.database.map.DatabaseConnectionMap;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.HasLogChannelInterface;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingMetric;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.core.logging.MetricsLogTable;
import org.pentaho.di.core.logging.MetricsRegistry;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.metrics.MetricsDuration;
import org.pentaho.di.core.metrics.MetricsSnapshotInterface;
import org.pentaho.di.core.metrics.MetricsUtil;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.DelegationListener;
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
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.BaseStepData.StepExecutionStatus;
import org.pentaho.di.trans.step.RunThread;
import org.pentaho.di.trans.step.StepAdapter;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInitThread;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepListener;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaDataCombi;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.mappinginput.MappingInput;
import org.pentaho.di.trans.steps.mappingoutput.MappingOutput;
import org.pentaho.di.www.PrepareExecutionTransServlet;
import org.pentaho.di.www.RegisterPackageServlet;
import org.pentaho.di.www.RegisterTransServlet;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.pentaho.di.www.SocketRepository;
import org.pentaho.di.www.StartExecutionTransServlet;
import org.pentaho.di.www.WebResult;
import org.pentaho.metastore.api.IMetaStore;

import static org.pentaho.di.trans.Trans.BitMaskStatus.FINISHED;
import static org.pentaho.di.trans.Trans.BitMaskStatus.RUNNING;
import static org.pentaho.di.trans.Trans.BitMaskStatus.STOPPED;
import static org.pentaho.di.trans.Trans.BitMaskStatus.PREPARING;
import static org.pentaho.di.trans.Trans.BitMaskStatus.INITIALIZING;
import static org.pentaho.di.trans.Trans.BitMaskStatus.PAUSED;
import static org.pentaho.di.trans.Trans.BitMaskStatus.BIT_STATUS_SUM;


/**
 * This class represents the information and operations associated with the concept of a Transformation. It loads,
 * instantiates, initializes, runs, and monitors the execution of the transformation contained in the specified
 * TransInfo object.
 *
 * @author Matt
 * @since 07-04-2003
 *
 */
public class Trans implements VariableSpace, NamedParams, HasLogChannelInterface, LoggingObjectInterface,
    ExecutorInterface, ExtensionDataInterface {

  /** The package name, used for internationalization of messages. */
  private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!

  /** The replay date format. */
  public static final String REPLAY_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

  /** The log channel interface. */
  protected LogChannelInterface log;

  /** The log level. */
  protected LogLevel logLevel = LogLevel.BASIC;

  /** The container object id. */
  protected String containerObjectId;

  /** The log commit size. */
  protected int logCommitSize = 10;

  /** The transformation metadata to execute. */
  protected TransMeta transMeta;

  /**
   * The repository we are referencing.
   */
  protected Repository repository;

  /**
   * The MetaStore to use
   */
  protected IMetaStore metaStore;

  /**
   * The job that's launching this transformation. This gives us access to the whole chain, including the parent
   * variables, etc.
   */
  private Job parentJob;

  /**
   * The transformation that is executing this transformation in case of mappings.
   */
  private Trans parentTrans;

  /** The parent logging object interface (this could be a transformation or a job). */
  private LoggingObjectInterface parent;

  /** The name of the mapping step that executes this transformation in case this is a mapping. */
  private String mappingStepName;

  /** Indicates that we want to monitor the running transformation in a GUI. */
  private boolean monitored;

  /**
   * Indicates that we are running in preview mode...
   */
  private boolean preview;

  /** The date objects for logging information about the transformation such as start and end time, etc. */
  private Date startDate, endDate, currentDate, logDate, depDate;

  /** The job start and end date. */
  private Date jobStartDate, jobEndDate;

  /** The batch id. */
  private long batchId;

  /**
   * This is the batch ID that is passed from job to job to transformation, if nothing is passed, it's the
   * transformation's batch id.
   */
  private long passedBatchId;

  /** The variable bindings for the transformation. */
  private VariableSpace variables = new Variables();

  /** A list of all the row sets. */
  public List<RowSet> rowsets;

  /** A list of all the steps. */
  private List<StepMetaDataCombi> steps;

  /** The class number. */
  public int class_nr;

  /**
   * The replayDate indicates that this transformation is a replay transformation for a transformation executed on
   * replayDate. If replayDate is null, the transformation is not a replay.
   */
  private Date replayDate;

  /** Constant indicating a dispatch type of 1-to-1. */
  public static final int TYPE_DISP_1_1 = 1;

  /** Constant indicating a dispatch type of 1-to-N. */
  public static final int TYPE_DISP_1_N = 2;

  /** Constant indicating a dispatch type of N-to-1. */
  public static final int TYPE_DISP_N_1 = 3;

  /** Constant indicating a dispatch type of N-to-N. */
  public static final int TYPE_DISP_N_N = 4;

  /** Constant indicating a dispatch type of N-to-M. */
  public static final int TYPE_DISP_N_M = 5;

  /** Constant indicating a transformation status of Finished. */
  public static final String STRING_FINISHED = "Finished";

  /** Constant indicating a transformation status of Finished (with errors). */
  public static final String STRING_FINISHED_WITH_ERRORS = "Finished (with errors)";

  /** Constant indicating a transformation status of Running. */
  public static final String STRING_RUNNING = "Running";

  /** Constant indicating a transformation status of Paused. */
  public static final String STRING_PAUSED = "Paused";

  /** Constant indicating a transformation status of Preparing for execution. */
  public static final String STRING_PREPARING = "Preparing executing";

  /** Constant indicating a transformation status of Initializing. */
  public static final String STRING_INITIALIZING = "Initializing";

  /** Constant indicating a transformation status of Waiting. */
  public static final String STRING_WAITING = "Waiting";

  /** Constant indicating a transformation status of Stopped. */
  public static final String STRING_STOPPED = "Stopped";

  /** Constant indicating a transformation status of Halting. */
  public static final String STRING_HALTING = "Halting";

  /** Constant specifying a filename containing XML to inject into a ZIP file created during resource export. */
  public static final String CONFIGURATION_IN_EXPORT_FILENAME = "__job_execution_configuration__.xml";

  /** Whether safe mode is enabled. */
  private boolean safeModeEnabled;

  /** The thread name. */
  @Deprecated
  private String threadName;

  /** The transaction ID */
  private String transactionId;

  /** Int value for storage trans statuses*/
  private AtomicInteger status;

  /**
   * <p>This enum stores bit masks which are used to manipulate with
   * statuses over field {@link Trans#status}
   */
  enum BitMaskStatus {
    RUNNING( 1 ),
    INITIALIZING( 2 ),
    PREPARING( 4 ),
    STOPPED( 8 ),
    FINISHED( 16 ),
    PAUSED( 32 );

    private final int mask;
    //the sum of status masks
    public static final int BIT_STATUS_SUM = 63;

    BitMaskStatus( int mask ) {
      this.mask = mask;
    }

  }

  /** The number of errors that have occurred during execution of the transformation. */
  private AtomicInteger errors;

  /** Whether the transformation is ready to start. */
  private boolean readyToStart;

  /** Step performance snapshots. */
  private Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots;

  /** The step performance snapshot timer. */
  private Timer stepPerformanceSnapShotTimer;

  /** A list of listeners attached to the transformation. */
  private List<TransListener> transListeners;

  /** A list of stop-event listeners attached to the transformation. */
  private List<TransStoppedListener> transStoppedListeners;

  /** In case this transformation starts to delegate work to a local transformation or job */
  private List<DelegationListener> delegationListeners;

  /** The number of finished steps. */
  private int nrOfFinishedSteps;

  /** The number of active steps. */
  private int nrOfActiveSteps;

  /** The named parameters. */
  private NamedParams namedParams = new NamedParamsDefault();

  /** The socket repository. */
  private SocketRepository socketRepository;

  /** The transformation log table database connection. */
  private Database transLogTableDatabaseConnection;

  /** The step performance snapshot sequence number. */
  private AtomicInteger stepPerformanceSnapshotSeqNr;

  /** The last written step performance sequence number. */
  private int lastWrittenStepPerformanceSequenceNr;

  /** The last step performance snapshot sequence number added. */
  private int lastStepPerformanceSnapshotSeqNrAdded;

  /** The active subtransformations. */
  private Map<String, Trans> activeSubtransformations;

  /** The active subjobs */
  private Map<String, Job> activeSubjobs;

  /** The step performance snapshot size limit. */
  private int stepPerformanceSnapshotSizeLimit;

  /** The servlet print writer. */
  private PrintWriter servletPrintWriter;

  /** The trans finished blocking queue. */
  private ArrayBlockingQueue<Object> transFinishedBlockingQueue;

  /** The name of the executing server */
  private String executingServer;

  /** The name of the executing user */
  private String executingUser;

  private Result previousResult;

  protected List<RowMetaAndData> resultRows;

  protected List<ResultFile> resultFiles;

  /** The command line arguments for the transformation. */
  protected String[] arguments;

  /**
   * A table of named counters.
   */
  protected Hashtable<String, Counter> counters;

  private HttpServletResponse servletResponse;

  private HttpServletRequest servletRequest;

  private Map<String, Object> extensionDataMap;

  private ExecutorService heartbeat = null; // this transformations's heartbeat scheduled executor

  /**
   * Instantiates a new transformation.
   */
  public Trans() {
    status = new AtomicInteger();

    transListeners = Collections.synchronizedList( new ArrayList<TransListener>() );
    transStoppedListeners = Collections.synchronizedList( new ArrayList<TransStoppedListener>() );
    delegationListeners = new ArrayList<>();

    // Get a valid transactionId in case we run database transactional.
    transactionId = calculateTransactionId();
    threadName = transactionId; // / backward compatibility but deprecated!

    errors = new AtomicInteger( 0 );

    stepPerformanceSnapshotSeqNr = new AtomicInteger( 0 );
    lastWrittenStepPerformanceSequenceNr = 0;

    activeSubtransformations = new ConcurrentHashMap<>();
    activeSubjobs = new HashMap<>();

    resultRows = new ArrayList<>();
    resultFiles = new ArrayList<>();
    counters = new Hashtable<>();

    extensionDataMap = new HashMap<>();
  }

  /**
   * Initializes a transformation from transformation meta-data defined in memory.
   *
   * @param transMeta
   *          the transformation meta-data to use.
   */
  public Trans( TransMeta transMeta ) {
    this( transMeta, null );
  }

  /**
   * Initializes a transformation from transformation meta-data defined in memory. Also take into account the parent log
   * channel interface (job or transformation) for logging lineage purposes.
   *
   * @param transMeta
   *          the transformation meta-data to use.
   * @param parent
   *          the parent job that is executing this transformation
   */
  public Trans( TransMeta transMeta, LoggingObjectInterface parent ) {
    this();
    this.transMeta = transMeta;
    setParent( parent );

    initializeVariablesFrom( transMeta );
    copyParametersFrom( transMeta );
    transMeta.activateParameters();

    // Get a valid transactionId in case we run database transactional.
    transactionId = calculateTransactionId();
    threadName = transactionId; // / backward compatibility but deprecated!
  }

  /**
   * Sets the parent logging object.
   *
   * @param parent
   *          the new parent
   */
  public void setParent( LoggingObjectInterface parent ) {
    this.parent = parent;

    this.log = new LogChannel( this, parent );
    this.logLevel = log.getLogLevel();
    this.containerObjectId = log.getContainerObjectId();

    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.TransformationIsPreloaded" ) );
    }
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "Trans.Log.NumberOfStepsToRun", String.valueOf( transMeta.nrSteps() ),
          String.valueOf( transMeta.nrTransHops() ) ) );
    }

  }

  /**
   * Sets the default log commit size.
   */
  private void setDefaultLogCommitSize() {
    String propLogCommitSize = this.getVariable( "pentaho.log.commit.size" );
    if ( propLogCommitSize != null ) {
      // override the logCommit variable
      try {
        logCommitSize = Integer.parseInt( propLogCommitSize );
      } catch ( Exception ignored ) {
        logCommitSize = 10; // ignore parsing error and default to 10
      }
    }

  }

  /**
   * Gets the log channel interface for the transformation.
   *
   * @return the log channel
   * @see org.pentaho.di.core.logging.HasLogChannelInterface#getLogChannel()
   */
  @Override
  public LogChannelInterface getLogChannel() {
    return log;
  }

  /**
   * Sets the log channel interface for the transformation.
   *
   * @param log
   *          the new log channel interface
   */
  public void setLog( LogChannelInterface log ) {
    this.log = log;
  }

  /**
   * Gets the name of the transformation.
   *
   * @return the transformation name
   */
  public String getName() {
    if ( transMeta == null ) {
      return null;
    }

    return transMeta.getName();
  }

  /**
   * Instantiates a new transformation using any of the provided parameters including the variable bindings, a
   * repository, a name, a repository directory name, and a filename. This is a multi-purpose method that supports
   * loading a transformation from a file (if the filename is provided but not a repository object) or from a repository
   * (if the repository object, repository directory name, and transformation name are specified).
   *
   * @param parent
   *          the parent variable space and named params
   * @param rep
   *          the repository
   * @param name
   *          the name of the transformation
   * @param dirname
   *          the dirname the repository directory name
   * @param filename
   *          the filename containing the transformation definition
   * @throws KettleException
   *           if any error occurs during loading, parsing, or creation of the transformation
   */
  public <Parent extends VariableSpace & NamedParams> Trans( Parent parent, Repository rep, String name, String dirname,
      String filename ) throws KettleException {
    this();
    try {
      if ( rep != null ) {
        RepositoryDirectoryInterface repdir = rep.findDirectory( dirname );
        if ( repdir != null ) {
          this.transMeta = rep.loadTransformation( name, repdir, null, false, null ); // reads last version
        } else {
          throw new KettleException( BaseMessages.getString( PKG, "Trans.Exception.UnableToLoadTransformation", name,
              dirname ) );
        }
      } else {
        transMeta = new TransMeta( filename, false );
      }

      this.log = LogChannel.GENERAL;

      transMeta.initializeVariablesFrom( parent );
      initializeVariablesFrom( parent );
      // PDI-3064 do not erase parameters from meta!
      // instead of this - copy parameters to actual transformation
      this.copyParametersFrom( parent );
      this.activateParameters();

      this.setDefaultLogCommitSize();

      // Get a valid transactionId in case we run database transactional.
      transactionId = calculateTransactionId();
      threadName = transactionId; // / backward compatibility but deprecated!
    } catch ( KettleException e ) {
      throw new KettleException( BaseMessages.getString( PKG, "Trans.Exception.UnableToOpenTransformation", name ), e );
    }
  }

  /**
   * Executes the transformation. This method will prepare the transformation for execution and then start all the
   * threads associated with the transformation and its steps.
   *
   * @param arguments
   *          the arguments
   * @throws KettleException
   *           if the transformation could not be prepared (initialized)
   */
  public void execute( String[] arguments ) throws KettleException {
    prepareExecution( arguments );
    startThreads();
  }

  /**
   * Prepares the transformation for execution. This includes setting the arguments and parameters as well as preparing
   * and tracking the steps and hops in the transformation.
   *
   * @param arguments
   *          the arguments to use for this transformation
   * @throws KettleException
   *           in case the transformation could not be prepared (initialized)
   */
  public void prepareExecution( String[] arguments ) throws KettleException {
    setPreparing( true );
    startDate = null;
    setRunning( false );

    log.snap( Metrics.METRIC_TRANSFORMATION_EXECUTION_START );
    log.snap( Metrics.METRIC_TRANSFORMATION_INIT_START );

    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationPrepareExecution.id, this );

    transMeta.disposeEmbeddedMetastoreProvider();
    if ( transMeta.getMetastoreLocatorOsgi() != null ) {
      transMeta.setEmbeddedMetastoreProviderKey(
        transMeta.getMetastoreLocatorOsgi().setEmbeddedMetastore( transMeta.getEmbeddedMetaStore() ) );
    }

    checkCompatibility();

    // Set the arguments on the transformation...
    //
    if ( arguments != null ) {
      setArguments( arguments );
    }

    activateParameters();
    transMeta.activateParameters();

    if ( transMeta.getName() == null ) {
      if ( transMeta.getFilename() != null ) {
        log.logBasic( BaseMessages.getString( PKG, "Trans.Log.DispacthingStartedForFilename", transMeta
            .getFilename() ) );
      }
    } else {
      log.logBasic( BaseMessages.getString( PKG, "Trans.Log.DispacthingStartedForTransformation", transMeta
          .getName() ) );
    }

    if ( getArguments() != null ) {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.NumberOfArgumentsDetected", String.valueOf(
            getArguments().length ) ) );
      }
    }

    if ( isSafeModeEnabled() ) {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.SafeModeIsEnabled", transMeta.getName() ) );
      }
    }

    if ( getReplayDate() != null ) {
      SimpleDateFormat df = new SimpleDateFormat( REPLAY_DATE_FORMAT );
      log.logBasic( BaseMessages.getString( PKG, "Trans.Log.ThisIsAReplayTransformation" ) + df.format(
          getReplayDate() ) );
    } else {
      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.ThisIsNotAReplayTransformation" ) );
      }
    }

    // setInternalKettleVariables(this); --> Let's not do this, when running
    // without file, for example remote, it spoils the fun

    // extra check to see if the servlet print writer has some value in case
    // folks want to test it locally...
    //
    if ( servletPrintWriter == null ) {
      String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", null );
      if ( encoding == null ) {
        servletPrintWriter = new PrintWriter( new OutputStreamWriter( System.out ) );
      } else {
        try {
          servletPrintWriter = new PrintWriter( new OutputStreamWriter( System.out, encoding ) );
        } catch ( UnsupportedEncodingException ex ) {
          servletPrintWriter = new PrintWriter( new OutputStreamWriter( System.out ) );
        }
      }
    }

    // Keep track of all the row sets and allocated steps
    //
    steps = new ArrayList<>();
    rowsets = new ArrayList<>();

    List<StepMeta> hopsteps = transMeta.getTransHopSteps( false );

    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.FoundDefferentSteps", String.valueOf( hopsteps
          .size() ) ) );
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.AllocatingRowsets" ) );
    }
    // First allocate all the rowsets required!
    // Note that a mapping doesn't receive ANY input or output rowsets...
    //
    for ( int i = 0; i < hopsteps.size(); i++ ) {
      StepMeta thisStep = hopsteps.get( i );
      if ( thisStep.isMapping() ) {
        continue; // handled and allocated by the mapping step itself.
      }

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.AllocateingRowsetsForStep", String.valueOf( i ),
            thisStep.getName() ) );
      }

      List<StepMeta> nextSteps = transMeta.findNextSteps( thisStep );
      int nrTargets = nextSteps.size();

      for ( int n = 0; n < nrTargets; n++ ) {
        // What's the next step?
        StepMeta nextStep = nextSteps.get( n );
        if ( nextStep.isMapping() ) {
          continue; // handled and allocated by the mapping step itself.
        }

        // How many times do we start the source step?
        int thisCopies = thisStep.getCopies();

        if ( thisCopies < 0 ) {
          // This can only happen if a variable is used that didn't resolve to a positive integer value
          //
          throw new KettleException( BaseMessages.getString( PKG, "Trans.Log.StepCopiesNotCorrectlyDefined", thisStep
              .getName() ) );
        }

        // How many times do we start the target step?
        int nextCopies = nextStep.getCopies();

        // Are we re-partitioning?
        boolean repartitioning;
        if ( thisStep.isPartitioned() ) {
          repartitioning = !thisStep.getStepPartitioningMeta().equals( nextStep.getStepPartitioningMeta() );
        } else {
          repartitioning = nextStep.isPartitioned();
        }

        int nrCopies;
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.copiesInfo", String.valueOf( thisCopies ), String
              .valueOf( nextCopies ) ) );
        }
        int dispatchType;
        if ( thisCopies == 1 && nextCopies == 1 ) {
          dispatchType = TYPE_DISP_1_1;
          nrCopies = 1;
        } else if ( thisCopies == 1 && nextCopies > 1 ) {
          dispatchType = TYPE_DISP_1_N;
          nrCopies = nextCopies;
        } else if ( thisCopies > 1 && nextCopies == 1 ) {
          dispatchType = TYPE_DISP_N_1;
          nrCopies = thisCopies;
        } else if ( thisCopies == nextCopies && !repartitioning ) {
          dispatchType = TYPE_DISP_N_N;
          nrCopies = nextCopies;
        } else {
          // > 1!
          dispatchType = TYPE_DISP_N_M;
          nrCopies = nextCopies;
        } // Allocate a rowset for each destination step

        // Allocate the rowsets
        //
        if ( dispatchType != TYPE_DISP_N_M ) {
          for ( int c = 0; c < nrCopies; c++ ) {
            RowSet rowSet;
            switch ( transMeta.getTransformationType() ) {
              case Normal:
                // This is a temporary patch until the batching rowset has proven
                // to be working in all situations.
                // Currently there are stalling problems when dealing with small
                // amounts of rows.
                //
                Boolean batchingRowSet =
                    ValueMetaString.convertStringToBoolean( System.getProperty( Const.KETTLE_BATCHING_ROWSET ) );
                if ( batchingRowSet != null && batchingRowSet.booleanValue() ) {
                  rowSet = new BlockingBatchingRowSet( transMeta.getSizeRowset() );
                } else {
                  rowSet = new BlockingRowSet( transMeta.getSizeRowset() );
                }
                break;

              case SerialSingleThreaded:
                rowSet = new SingleRowRowSet();
                break;

              case SingleThreaded:
                rowSet = new QueueRowSet();
                break;

              default:
                throw new KettleException( "Unhandled transformation type: " + transMeta.getTransformationType() );
            }

            switch ( dispatchType ) {
              case TYPE_DISP_1_1:
                rowSet.setThreadNameFromToCopy( thisStep.getName(), 0, nextStep.getName(), 0 );
                break;
              case TYPE_DISP_1_N:
                rowSet.setThreadNameFromToCopy( thisStep.getName(), 0, nextStep.getName(), c );
                break;
              case TYPE_DISP_N_1:
                rowSet.setThreadNameFromToCopy( thisStep.getName(), c, nextStep.getName(), 0 );
                break;
              case TYPE_DISP_N_N:
                rowSet.setThreadNameFromToCopy( thisStep.getName(), c, nextStep.getName(), c );
                break;
              default:
                break;
            }
            rowsets.add( rowSet );
            if ( log.isDetailed() ) {
              log.logDetailed( BaseMessages.getString( PKG, "Trans.TransformationAllocatedNewRowset", rowSet
                  .toString() ) );
            }
          }
        } else {
          // For each N source steps we have M target steps
          //
          // From each input step we go to all output steps.
          // This allows maximum flexibility for re-partitioning,
          // distribution...
          for ( int s = 0; s < thisCopies; s++ ) {
            for ( int t = 0; t < nextCopies; t++ ) {
              BlockingRowSet rowSet = new BlockingRowSet( transMeta.getSizeRowset() );
              rowSet.setThreadNameFromToCopy( thisStep.getName(), s, nextStep.getName(), t );
              rowsets.add( rowSet );
              if ( log.isDetailed() ) {
                log.logDetailed( BaseMessages.getString( PKG, "Trans.TransformationAllocatedNewRowset", rowSet
                    .toString() ) );
              }
            }
          }
        }
      }
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.AllocatedRowsets", String.valueOf( rowsets.size() ),
          String.valueOf( i ), thisStep.getName() ) + " " );
    }

    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.AllocatingStepsAndStepData" ) );
    }

    // Allocate the steps & the data...
    //
    for ( int i = 0; i < hopsteps.size(); i++ ) {
      StepMeta stepMeta = hopsteps.get( i );
      String stepid = stepMeta.getStepID();

      if ( log.isDetailed() ) {
        log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.TransformationIsToAllocateStep", stepMeta.getName(),
            stepid ) );
      }

      // How many copies are launched of this step?
      int nrCopies = stepMeta.getCopies();

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Trans.Log.StepHasNumberRowCopies", String.valueOf( nrCopies ) ) );
      }

      // At least run once...
      for ( int c = 0; c < nrCopies; c++ ) {
        // Make sure we haven't started it yet!
        if ( !hasStepStarted( stepMeta.getName(), c ) ) {
          StepMetaDataCombi combi = new StepMetaDataCombi();

          combi.stepname = stepMeta.getName();
          combi.copy = c;

          // The meta-data
          combi.stepMeta = stepMeta;
          combi.meta = stepMeta.getStepMetaInterface();

          // Allocate the step data
          StepDataInterface data = combi.meta.getStepData();
          combi.data = data;

          // Allocate the step
          StepInterface step = combi.meta.getStep( stepMeta, data, c, transMeta, this );

          // Copy the variables of the transformation to the step...
          // don't share. Each copy of the step has its own variables.
          //
          step.initializeVariablesFrom( this );
          step.setUsingThreadPriorityManagment( transMeta.isUsingThreadPriorityManagment() );

          // Pass the connected repository & metaStore to the steps runtime
          //
          step.setRepository( repository );
          step.setMetaStore( metaStore );

          // If the step is partitioned, set the partitioning ID and some other
          // things as well...
          if ( stepMeta.isPartitioned() ) {
            List<String> partitionIDs = stepMeta.getStepPartitioningMeta().getPartitionSchema().getPartitionIDs();
            if ( partitionIDs != null && partitionIDs.size() > 0 ) {
              step.setPartitionID( partitionIDs.get( c ) ); // Pass the partition ID
                                                            // to the step
            }
          }

          // Save the step too
          combi.step = step;

          // Pass logging level and metrics gathering down to the step level.
          // /
          if ( combi.step instanceof LoggingObjectInterface ) {
            LogChannelInterface logChannel = combi.step.getLogChannel();
            logChannel.setLogLevel( logLevel );
            logChannel.setGatheringMetrics( log.isGatheringMetrics() );
          }

          // Add to the bunch...
          steps.add( combi );

          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.TransformationHasAllocatedANewStep", stepMeta
                .getName(), String.valueOf( c ) ) );
          }
        }
      }
    }

    // Now we need to verify if certain rowsets are not meant to be for error
    // handling...
    // Loop over the steps and for every step verify the output rowsets
    // If a rowset is going to a target step in the steps error handling
    // metadata, set it to the errorRowSet.
    // The input rowsets are already in place, so the next step just accepts the
    // rows.
    // Metadata wise we need to do the same trick in TransMeta
    //
    for ( int s = 0; s < steps.size(); s++ ) {
      StepMetaDataCombi combi = steps.get( s );
      if ( combi.stepMeta.isDoingErrorHandling() ) {
        combi.step.identifyErrorOutput();

      }
    }

    // Now (optionally) write start log record!
    // Make sure we synchronize appropriately to avoid duplicate batch IDs.
    //
    Object syncObject = this;
    if ( parentJob != null ) {
      syncObject = parentJob; // parallel execution in a job
    }
    if ( parentTrans != null ) {
      syncObject = parentTrans; // multiple sub-transformations
    }
    synchronized ( syncObject ) {
      calculateBatchIdAndDateRange();
      beginProcessing();
    }

    // Set the partition-to-rowset mapping
    //
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );

      StepMeta stepMeta = sid.stepMeta;
      StepInterface baseStep = sid.step;

      baseStep.setPartitioned( stepMeta.isPartitioned() );

      // Now let's take a look at the source and target relation
      //
      // If this source step is not partitioned, and the target step is: it
      // means we need to re-partition the incoming data.
      // If both steps are partitioned on the same method and schema, we don't
      // need to re-partition
      // If both steps are partitioned on a different method or schema, we need
      // to re-partition as well.
      // If both steps are not partitioned, we don't need to re-partition
      //
      boolean isThisPartitioned = stepMeta.isPartitioned();
      PartitionSchema thisPartitionSchema = null;
      if ( isThisPartitioned ) {
        thisPartitionSchema = stepMeta.getStepPartitioningMeta().getPartitionSchema();
      }

      boolean isNextPartitioned = false;
      StepPartitioningMeta nextStepPartitioningMeta = null;
      PartitionSchema nextPartitionSchema = null;

      List<StepMeta> nextSteps = transMeta.findNextSteps( stepMeta );
      int nrNext = nextSteps.size();
      for ( int p = 0; p < nrNext; p++ ) {
        StepMeta nextStep = nextSteps.get( p );
        if ( nextStep.isPartitioned() ) {
          isNextPartitioned = true;
          nextStepPartitioningMeta = nextStep.getStepPartitioningMeta();
          nextPartitionSchema = nextStepPartitioningMeta.getPartitionSchema();
        }
      }

      baseStep.setRepartitioning( StepPartitioningMeta.PARTITIONING_METHOD_NONE );

      // If the next step is partitioned differently, set re-partitioning, when
      // running locally.
      //
      if ( ( !isThisPartitioned && isNextPartitioned ) || ( isThisPartitioned && isNextPartitioned
          && !thisPartitionSchema.equals( nextPartitionSchema ) ) ) {
        baseStep.setRepartitioning( nextStepPartitioningMeta.getMethodType() );
      }

      // For partitioning to a set of remove steps (repartitioning from a master
      // to a set or remote output steps)
      //
      StepPartitioningMeta targetStepPartitioningMeta = baseStep.getStepMeta().getTargetStepPartitioningMeta();
      if ( targetStepPartitioningMeta != null ) {
        baseStep.setRepartitioning( targetStepPartitioningMeta.getMethodType() );
      }
    }

    setPreparing( false );
    setInitializing( true );

    // Do a topology sort... Over 150 step (copies) things might be slowing down too much.
    //
    if ( isMonitored() && steps.size() < 150 ) {
      doTopologySortOfSteps();
    }

    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.InitialisingSteps", String.valueOf( steps.size() ) ) );
    }

    StepInitThread[] initThreads = new StepInitThread[steps.size()];
    Thread[] threads = new Thread[steps.size()];

    // Initialize all the threads...
    //
    for ( int i = 0; i < steps.size(); i++ ) {
      final StepMetaDataCombi sid = steps.get( i );

      // Do the init code in the background!
      // Init all steps at once, but ALL steps need to finish before we can
      // continue properly!
      //
      initThreads[i] = new StepInitThread( sid, log );

      // Put it in a separate thread!
      //
      threads[i] = new Thread( initThreads[i] );
      threads[i].setName( "init of " + sid.stepname + "." + sid.copy + " (" + threads[i].getName() + ")" );

      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.StepBeforeInitialize.id, initThreads[i] );

      threads[i].start();
    }

    for ( int i = 0; i < threads.length; i++ ) {
      try {
        threads[i].join();
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.StepAfterInitialize.id, initThreads[i] );
      } catch ( Exception ex ) {
        log.logError( "Error with init thread: " + ex.getMessage(), ex.getMessage() );
        log.logError( Const.getStackTracker( ex ) );
      }
    }

    setInitializing( false );
    boolean ok = true;

    // All step are initialized now: see if there was one that didn't do it
    // correctly!
    //
    for ( int i = 0; i < initThreads.length; i++ ) {
      StepMetaDataCombi combi = initThreads[i].getCombi();
      if ( !initThreads[i].isOk() ) {
        log.logError( BaseMessages.getString( PKG, "Trans.Log.StepFailedToInit", combi.stepname + "." + combi.copy ) );
        combi.data.setStatus( StepExecutionStatus.STATUS_STOPPED );
        ok = false;
      } else {
        combi.data.setStatus( StepExecutionStatus.STATUS_IDLE );
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.StepInitialized", combi.stepname + "."
              + combi.copy ) );
        }
      }
    }

    if ( !ok ) {
      // Halt the other threads as well, signal end-of-the line to the outside
      // world...
      // Also explicitly call dispose() to clean up resources opened during
      // init();
      //
      for ( int i = 0; i < initThreads.length; i++ ) {
        StepMetaDataCombi combi = initThreads[i].getCombi();

        // Dispose will overwrite the status, but we set it back right after
        // this.
        combi.step.dispose( combi.meta, combi.data );

        if ( initThreads[i].isOk() ) {
          combi.data.setStatus( StepExecutionStatus.STATUS_HALTED );
        } else {
          combi.data.setStatus( StepExecutionStatus.STATUS_STOPPED );
        }
      }

      // Just for safety, fire the trans finished listeners...
      try {
        fireTransFinishedListeners();
      } catch ( KettleException e ) {
        // listeners produces errors
        log.logError( BaseMessages.getString( PKG, "Trans.FinishListeners.Exception" ) );
        // we will not pass this exception up to prepareExecuton() entry point.
      } finally {
        // Flag the transformation as finished even if exception was thrown
        setFinished( true );
      }

      // Pass along the log during preview. Otherwise it becomes hard to see
      // what went wrong.
      //
      if ( preview ) {
        String logText = KettleLogStore.getAppender().getBuffer( getLogChannelId(), true ).toString();
        throw new KettleException( BaseMessages.getString( PKG, "Trans.Log.FailToInitializeAtLeastOneStep" ) + Const.CR
            + logText );
      } else {
        throw new KettleException( BaseMessages.getString( PKG, "Trans.Log.FailToInitializeAtLeastOneStep" )
            + Const.CR );
      }
    }

    log.snap( Metrics.METRIC_TRANSFORMATION_INIT_STOP );

    KettleEnvironment.setExecutionInformation( this, repository );

    setReadyToStart( true );
  }

  @SuppressWarnings( "deprecation" )
  private void checkCompatibility() {
    // If we don't have a previous result and transMeta does have one, someone has been using a deprecated method.
    //
    if ( transMeta.getPreviousResult() != null && getPreviousResult() == null ) {
      setPreviousResult( transMeta.getPreviousResult() );
    }

    // If we don't have arguments set and TransMeta has, someone has been using a deprecated method.
    //
    if ( transMeta.getArguments() != null && getArguments() == null ) {
      setArguments( transMeta.getArguments() );
    }
  }

  /**
   * Starts the threads prepared by prepareThreads(). Before you start the threads, you can add RowListeners to them.
   *
   * @throws KettleException
   *           if there is a communication error with a remote output socket.
   */
  public void startThreads() throws KettleException {
    // Now prepare to start all the threads...
    //
    nrOfFinishedSteps = 0;
    nrOfActiveSteps = 0;

    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationStartThreads.id, this );

    fireTransStartedListeners();

    for ( int i = 0; i < steps.size(); i++ ) {
      final StepMetaDataCombi sid = steps.get( i );
      sid.step.markStart();
      sid.step.initBeforeStart();

      // also attach a Step Listener to detect when we're done...
      //
      StepListener stepListener = new StepListener() {
        @Override
        public void stepActive( Trans trans, StepMeta stepMeta, StepInterface step ) {
          nrOfActiveSteps++;
          if ( nrOfActiveSteps == 1 ) {
            // Transformation goes from in-active to active...
            // PDI-5229 sync added
            synchronized ( transListeners ) {
              for ( TransListener listener : transListeners ) {
                listener.transActive( Trans.this );
              }
            }
          }
        }

        @Override
        public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface step ) {
          synchronized ( Trans.this ) {
            nrOfFinishedSteps++;

            if ( nrOfFinishedSteps >= steps.size() ) {
              // Set the finished flag
              //
              setFinished( true );

              // Grab the performance statistics one last time (if enabled)
              //
              addStepPerformanceSnapShot();

              try {
                fireTransFinishedListeners();
              } catch ( Exception e ) {
                step.setErrors( step.getErrors() + 1L );
                log.logError( getName() + " : " + BaseMessages.getString( PKG,
                    "Trans.Log.UnexpectedErrorAtTransformationEnd" ), e );
              }
            }

            // If a step fails with an error, we want to kill/stop the others
            // too...
            //
            if ( step.getErrors() > 0 ) {

              log.logMinimal( BaseMessages.getString( PKG, "Trans.Log.TransformationDetectedErrors" ) );
              log.logMinimal( BaseMessages.getString( PKG, "Trans.Log.TransformationIsKillingTheOtherSteps" ) );

              killAllNoWait();
            }
          }
        }
      };
      // Make sure this is called first!
      //
      if ( sid.step instanceof BaseStep ) {
        ( (BaseStep) sid.step ).getStepListeners().add( 0, stepListener );
      } else {
        sid.step.addStepListener( stepListener );
      }
    }

    if ( transMeta.isCapturingStepPerformanceSnapShots() ) {
      stepPerformanceSnapshotSeqNr = new AtomicInteger( 0 );
      stepPerformanceSnapShots = new ConcurrentHashMap<>();

      // Calculate the maximum number of snapshots to be kept in memory
      //
      String limitString = environmentSubstitute( transMeta.getStepPerformanceCapturingSizeLimit() );
      if ( Utils.isEmpty( limitString ) ) {
        limitString = EnvUtil.getSystemProperty( Const.KETTLE_STEP_PERFORMANCE_SNAPSHOT_LIMIT );
      }
      stepPerformanceSnapshotSizeLimit = Const.toInt( limitString, 0 );

      // Set a timer to collect the performance data from the running threads...
      //
      stepPerformanceSnapShotTimer = new Timer( "stepPerformanceSnapShot Timer: " + transMeta.getName() );
      TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
          if ( !isFinished() ) {
            addStepPerformanceSnapShot();
          }
        }
      };
      stepPerformanceSnapShotTimer.schedule( timerTask, 100, transMeta.getStepPerformanceCapturingDelay() );
    }

    // Now start a thread to monitor the running transformation...
    //
    setFinished( false );
    setPaused( false );
    setStopped( false );

    transFinishedBlockingQueue = new ArrayBlockingQueue<>( 10 );

    TransListener transListener = new TransAdapter() {
      @Override
      public void transFinished( Trans trans ) {

        try {
          shutdownHeartbeat( trans != null ? trans.heartbeat : null );

          ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationFinish.id, trans );
        } catch ( KettleException e ) {
          throw new RuntimeException( "Error calling extension point at end of transformation", e );
        }

        // First of all, stop the performance snapshot timer if there is is
        // one...
        //
        if ( transMeta.isCapturingStepPerformanceSnapShots() && stepPerformanceSnapShotTimer != null ) {
          stepPerformanceSnapShotTimer.cancel();
        }

        transMeta.disposeEmbeddedMetastoreProvider();

        setFinished( true );
        setRunning( false ); // no longer running

        log.snap( Metrics.METRIC_TRANSFORMATION_EXECUTION_STOP );

        // If the user ran with metrics gathering enabled and a metrics logging table is configured, add another
        // listener...
        //
        MetricsLogTable metricsLogTable = transMeta.getMetricsLogTable();
        if ( metricsLogTable.isDefined() ) {
          try {
            writeMetricsInformation();
          } catch ( Exception e ) {
            log.logError( "Error writing metrics information", e );
            errors.incrementAndGet();
          }
        }

        // Close the unique connections when running database transactionally.
        // This will commit or roll back the transaction based on the result of this transformation.
        //
        if ( transMeta.isUsingUniqueConnections() ) {
          trans.closeUniqueDatabaseConnections( getResult() );
        }

        // release unused vfs connections
        KettleVFS.freeUnusedResources();
      }
    };
    // This should always be done first so that the other listeners achieve a clean state to start from (setFinished and
    // so on)
    //
    transListeners.add( 0, transListener );

    setRunning( true );

    switch ( transMeta.getTransformationType() ) {
      case Normal:

        // Now start all the threads...
        //
        for ( int i = 0; i < steps.size(); i++ ) {
          final StepMetaDataCombi combi = steps.get( i );
          RunThread runThread = new RunThread( combi );
          Thread thread = new Thread( runThread );
          thread.setName( getName() + " - " + combi.stepname );
          ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.StepBeforeStart.id, combi );
          // Call an extension point at the end of the step
          //
          combi.step.addStepListener( new StepAdapter() {

            @Override
            public void stepFinished( Trans trans, StepMeta stepMeta, StepInterface step ) {
              try {
                ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.StepFinished.id, combi );
              } catch ( KettleException e ) {
                throw new RuntimeException( "Unexpected error in calling extension point upon step finish", e );
              }
            }

          } );

          thread.start();
        }
        break;

      case SerialSingleThreaded:
        new Thread( new Runnable() {
          @Override
          public void run() {
            try {
              // Always disable thread priority management, it will always slow us
              // down...
              //
              for ( StepMetaDataCombi combi : steps ) {
                combi.step.setUsingThreadPriorityManagment( false );
              }

              //
              // This is a single threaded version...
              //

              // Sort the steps from start to finish...
              //
              Collections.sort( steps, new Comparator<StepMetaDataCombi>() {
                @Override
                public int compare( StepMetaDataCombi c1, StepMetaDataCombi c2 ) {

                  boolean c1BeforeC2 = transMeta.findPrevious( c2.stepMeta, c1.stepMeta );
                  if ( c1BeforeC2 ) {
                    return -1;
                  } else {
                    return 1;
                  }
                }
              } );

              boolean[] stepDone = new boolean[steps.size()];
              int nrDone = 0;
              while ( nrDone < steps.size() && !isStopped() ) {
                for ( int i = 0; i < steps.size() && !isStopped(); i++ ) {
                  StepMetaDataCombi combi = steps.get( i );
                  if ( !stepDone[i] ) {
                    // if (combi.step.canProcessOneRow() ||
                    // !combi.step.isRunning()) {
                    boolean cont = combi.step.processRow( combi.meta, combi.data );
                    if ( !cont ) {
                      stepDone[i] = true;
                      nrDone++;
                    }
                    // }
                  }
                }
              }
            } catch ( Exception e ) {
              errors.addAndGet( 1 );
              log.logError( "Error executing single threaded", e );
            } finally {
              for ( int i = 0; i < steps.size(); i++ ) {
                StepMetaDataCombi combi = steps.get( i );
                combi.step.dispose( combi.meta, combi.data );
                combi.step.markStop();
              }
            }
          }
        } ).start();
        break;

      case SingleThreaded:
        // Don't do anything, this needs to be handled by the transformation
        // executor!
        //
        break;
      default:
        break;

    }

    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationStart.id, this );

    heartbeat = startHeartbeat( getHeartbeatIntervalInSeconds() );

    if ( steps.isEmpty() ) {
      fireTransFinishedListeners();
    }

    if ( log.isDetailed() ) {
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.TransformationHasAllocated", String.valueOf( steps
          .size() ), String.valueOf( rowsets.size() ) ) );
    }
  }

  /**
   * Make attempt to fire all registered listeners if possible.
   *
   * @throws KettleException
   *           if any errors occur during notification
   */
  protected void fireTransFinishedListeners() throws KettleException {
    // PDI-5229 sync added
    synchronized ( transListeners ) {
      if ( transListeners.size() == 0 ) {
        return;
      }
      // prevent Exception from one listener to block others execution
      List<KettleException> badGuys = new ArrayList<>( transListeners.size() );
      for ( TransListener transListener : transListeners ) {
        try {
          transListener.transFinished( this );
        } catch ( KettleException e ) {
          badGuys.add( e );
        }
      }
      if ( transFinishedBlockingQueue != null ) {
        // Signal for the the waitUntilFinished blocker...
        transFinishedBlockingQueue.add( new Object() );
      }
      if ( !badGuys.isEmpty() ) {
        // FIFO
        throw new KettleException( badGuys.get( 0 ) );
      }
    }
  }

  /**
   * Fires the start-event listeners (if any are registered).
   *
   * @throws KettleException
   *           if any errors occur during notification
   */
  protected void fireTransStartedListeners() throws KettleException {
    // PDI-5229 sync added
    synchronized ( transListeners ) {
      for ( TransListener transListener : transListeners ) {
        transListener.transStarted( this );
      }
    }
  }

  /**
   * Adds a step performance snapshot.
   */
  protected void addStepPerformanceSnapShot() {

    if ( stepPerformanceSnapShots == null ) {
      return; // Race condition somewhere?
    }

    boolean pausedAndNotEmpty = isPaused() && !stepPerformanceSnapShots.isEmpty();
    boolean stoppedAndNotEmpty = isStopped() && !stepPerformanceSnapShots.isEmpty();

    if ( transMeta.isCapturingStepPerformanceSnapShots() && !pausedAndNotEmpty && !stoppedAndNotEmpty ) {
      // get the statistics from the steps and keep them...
      //
      int seqNr = stepPerformanceSnapshotSeqNr.incrementAndGet();
      for ( int i = 0; i < steps.size(); i++ ) {
        StepMeta stepMeta = steps.get( i ).stepMeta;
        StepInterface step = steps.get( i ).step;

        StepPerformanceSnapShot snapShot =
            new StepPerformanceSnapShot( seqNr, getBatchId(), new Date(), getName(), stepMeta.getName(), step.getCopy(),
                step.getLinesRead(), step.getLinesWritten(), step.getLinesInput(), step.getLinesOutput(), step
                    .getLinesUpdated(), step.getLinesRejected(), step.getErrors() );
        List<StepPerformanceSnapShot> snapShotList = stepPerformanceSnapShots.get( step.toString() );
        StepPerformanceSnapShot previous;
        if ( snapShotList == null ) {
          snapShotList = new ArrayList<>();
          stepPerformanceSnapShots.put( step.toString(), snapShotList );
          previous = null;
        } else {
          previous = snapShotList.get( snapShotList.size() - 1 ); // the last one...
        }
        // Make the difference...
        //
        snapShot.diff( previous, step.rowsetInputSize(), step.rowsetOutputSize() );
        synchronized ( stepPerformanceSnapShots ) {
          snapShotList.add( snapShot );

          if ( stepPerformanceSnapshotSizeLimit > 0 && snapShotList.size() > stepPerformanceSnapshotSizeLimit ) {
            snapShotList.remove( 0 );
          }
        }
      }

      lastStepPerformanceSnapshotSeqNrAdded = stepPerformanceSnapshotSeqNr.get();
    }
  }

  /**
   * This method performs any cleanup operations, typically called after the transformation has finished. Specifically,
   * after ALL the slave transformations in a clustered run have finished.
   */
  public void cleanup() {
    // Close all open server sockets.
    // We can only close these after all processing has been confirmed to be finished.
    //
    if ( steps == null ) {
      return;
    }

    for ( StepMetaDataCombi combi : steps ) {
      combi.step.cleanup();
    }
  }

  /**
   * Logs a summary message for the specified step.
   *
   * @param si
   *          the step interface
   */
  public void logSummary( StepInterface si ) {
    log.logBasic( si.getStepname(), BaseMessages.getString( PKG, "Trans.Log.FinishedProcessing", String.valueOf( si
        .getLinesInput() ), String.valueOf( si.getLinesOutput() ), String.valueOf( si.getLinesRead() ) ) + BaseMessages
            .getString( PKG, "Trans.Log.FinishedProcessing2", String.valueOf( si.getLinesWritten() ), String.valueOf( si
                .getLinesUpdated() ), String.valueOf( si.getErrors() ) ) );
  }

  /**
   * Waits until all RunThreads have finished.
   */
  public void waitUntilFinished() {
    try {
      if ( transFinishedBlockingQueue == null ) {
        return;
      }
      boolean wait = true;
      while ( wait ) {
        wait = transFinishedBlockingQueue.poll( 1, TimeUnit.DAYS ) == null;
        if ( wait ) {
          // poll returns immediately - this was hammering the CPU with poll checks. Added
          // a sleep to let the CPU breathe
          Thread.sleep( 1 );
        }
      }
    } catch ( InterruptedException e ) {
      throw new RuntimeException( "Waiting for transformation to be finished interrupted!", e );
    }
  }

  /**
   * Gets the number of errors that have occurred during execution of the transformation.
   *
   * @return the number of errors
   */
  public int getErrors() {
    int nrErrors = errors.get();

    if ( steps == null ) {
      return nrErrors;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      if ( sid.step.getErrors() != 0L ) {
        nrErrors += sid.step.getErrors();
      }
    }
    if ( nrErrors > 0 ) {
      log.logError( BaseMessages.getString( PKG, "Trans.Log.TransformationErrorsDetected" ) );
    }

    return nrErrors;
  }

  /**
   * Gets the number of steps in the transformation that are in an end state, such as Finished, Halted, or Stopped.
   *
   * @return the number of ended steps
   */
  public int getEnded() {
    int nrEnded = 0;

    if ( steps == null ) {
      return 0;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepDataInterface data = sid.data;

      if ( ( sid.step != null && !sid.step.isRunning() )
          // Should normally not be needed anymore, status is kept in data.
          || data.getStatus() == StepExecutionStatus.STATUS_FINISHED || // Finished processing
          data.getStatus() == StepExecutionStatus.STATUS_HALTED || // Not launching because of init error
          data.getStatus() == StepExecutionStatus.STATUS_STOPPED // Stopped because of an error
      ) {
        nrEnded++;
      }
    }

    return nrEnded;
  }

  /**
   * Checks if the transformation is finished\.
   *
   * @return true if the transformation is finished, false otherwise
   */
  public boolean isFinished() {
    int exist = status.get() & FINISHED.mask;
    return exist != 0;
  }

  protected void setFinished( boolean finished ) {
    status.updateAndGet( v -> finished ? v | FINISHED.mask : ( BIT_STATUS_SUM ^ FINISHED.mask ) & v );
  }

  public boolean isFinishedOrStopped() {
    return isFinished() || isStopped();
  }

  /**
   * Attempts to stops all running steps and subtransformations. If all steps have finished, the transformation is
   * marked as Finished.
   *
   * @deprecated Deprecated as of 8.0. Seems unused; will be to remove in 8.1 (ccaspanello)
   */
  @Deprecated
  public void killAll() {
    if ( steps == null ) {
      return;
    }

    int nrStepsFinished = 0;

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Trans.Log.LookingAtStep" ) + sid.step.getStepname() );
      }

      // If thr is a mapping, this is cause for an endless loop
      //
      while ( sid.step.isRunning() ) {
        sid.step.stopAll();
        try {
          Thread.sleep( 20 );
        } catch ( Exception e ) {
          log.logError( BaseMessages.getString( PKG, "Trans.Log.TransformationErrors" ) + e.toString() );
          return;
        }
      }

      if ( !sid.step.isRunning() ) {
        nrStepsFinished++;
      }
    }

    if ( nrStepsFinished == steps.size() ) {
      setFinished( true );
    }
  }

  /**
   * Asks all steps to stop but doesn't wait around for it to happen. This is a special method for use with mappings.
   */
  private void killAllNoWait() {
    if ( steps == null ) {
      return;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface step = sid.step;

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "Trans.Log.LookingAtStep" ) + step.getStepname() );
      }

      step.stopAll();
      try {
        Thread.sleep( 20 );
      } catch ( Exception e ) {
        log.logError( BaseMessages.getString( PKG, "Trans.Log.TransformationErrors" ) + e.toString() );
        return;
      }
    }
  }

  /**
   * Logs the execution statistics for the transformation for the specified time interval. If the total length of
   * execution is supplied as the interval, then the statistics represent the average throughput (lines
   * read/written/updated/rejected/etc. per second) for the entire execution.
   *
   * @param seconds
   *          the time interval (in seconds)
   */
  public void printStats( int seconds ) {
    log.logBasic( " " );
    if ( steps == null ) {
      return;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface step = sid.step;
      long proc = step.getProcessed();
      if ( seconds != 0 ) {
        if ( step.getErrors() == 0 ) {
          log.logBasic( BaseMessages.getString( PKG, "Trans.Log.ProcessSuccessfullyInfo", step.getStepname(), "." + step
              .getCopy(), String.valueOf( proc ), String.valueOf( ( proc / seconds ) ) ) );
        } else {
          log.logError( BaseMessages.getString( PKG, "Trans.Log.ProcessErrorInfo", step.getStepname(), "." + step
              .getCopy(), String.valueOf( step.getErrors() ), String.valueOf( proc ), String.valueOf( proc
                  / seconds ) ) );
        }
      } else {
        if ( step.getErrors() == 0 ) {
          log.logBasic( BaseMessages.getString( PKG, "Trans.Log.ProcessSuccessfullyInfo", step.getStepname(), "." + step
              .getCopy(), String.valueOf( proc ), seconds != 0 ? String.valueOf( ( proc / seconds ) ) : "-" ) );
        } else {
          log.logError( BaseMessages.getString( PKG, "Trans.Log.ProcessErrorInfo2", step.getStepname(), "." + step
              .getCopy(), String.valueOf( step.getErrors() ), String.valueOf( proc ), String.valueOf( seconds ) ) );
        }
      }
    }
  }

  /**
   * Gets a representable metric of the "processed" lines of the last step.
   *
   * @return the number of lines processed by the last step
   */
  public long getLastProcessed() {
    if ( steps == null || steps.size() == 0 ) {
      return 0L;
    }
    StepMetaDataCombi sid = steps.get( steps.size() - 1 );
    return sid.step.getProcessed();
  }

  /**
   * Finds the RowSet with the specified name.
   *
   * @param rowsetname
   *          the rowsetname
   * @return the row set, or null if none found
   */
  public RowSet findRowSet( String rowsetname ) {
    // Start with the transformation.
    for ( int i = 0; i < rowsets.size(); i++ ) {
      // log.logDetailed("DIS: looking for RowSet ["+rowsetname+"] in nr "+i+" of "+threads.size()+" threads...");
      RowSet rs = rowsets.get( i );
      if ( rs.getName().equalsIgnoreCase( rowsetname ) ) {
        return rs;
      }
    }

    return null;
  }

  /**
   * Finds the RowSet between two steps (or copies of steps).
   *
   * @param from
   *          the name of the "from" step
   * @param fromcopy
   *          the copy number of the "from" step
   * @param to
   *          the name of the "to" step
   * @param tocopy
   *          the copy number of the "to" step
   * @return the row set, or null if none found
   */
  public RowSet findRowSet( String from, int fromcopy, String to, int tocopy ) {
    // Start with the transformation.
    for ( int i = 0; i < rowsets.size(); i++ ) {
      RowSet rs = rowsets.get( i );
      if ( rs.getOriginStepName().equalsIgnoreCase( from ) && rs.getDestinationStepName().equalsIgnoreCase( to ) && rs
          .getOriginStepCopy() == fromcopy && rs.getDestinationStepCopy() == tocopy ) {
        return rs;
      }
    }

    return null;
  }

  /**
   * Checks whether the specified step (or step copy) has started.
   *
   * @param sname
   *          the step name
   * @param copy
   *          the copy number
   * @return true the specified step (or step copy) has started, false otherwise
   */
  public boolean hasStepStarted( String sname, int copy ) {
    // log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
    // log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      boolean started = ( sid.stepname != null && sid.stepname.equalsIgnoreCase( sname ) ) && sid.copy == copy;
      if ( started ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Stops all steps from running, and alerts any registered listeners.
   */
  public void stopAll() {
    if ( steps == null ) {
      return;
    }

    // log.logDetailed("DIS: Checking wether of not ["+sname+"]."+cnr+" has started!");
    // log.logDetailed("DIS: hasStepStarted() looking in "+threads.size()+" threads");
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface rt = sid.step;
      rt.setStopped( true );
      rt.resumeRunning();

      // Cancel queries etc. by force...
      StepInterface si = rt;
      try {
        si.stopRunning( sid.meta, sid.data );
      } catch ( Exception e ) {
        log.logError( "Something went wrong while trying to stop the transformation: " + e.toString() );
        log.logError( Const.getStackTracker( e ) );
      }

      sid.data.setStatus( StepExecutionStatus.STATUS_STOPPED );
    }

    // if it is stopped it is not paused
    setPaused( false );
    setStopped( true );

    // Fire the stopped listener...
    //
    synchronized ( transStoppedListeners ) {
      for ( TransStoppedListener listener : transStoppedListeners ) {
        listener.transStopped( this );
      }
    }
  }

  /**
   * Gets the number of steps in this transformation.
   *
   * @return the number of steps
   */
  public int nrSteps() {
    if ( steps == null ) {
      return 0;
    }
    return steps.size();
  }

  /**
   * Gets the number of active (i.e. not finished) steps in this transformation
   *
   * @return the number of active steps
   */
  public int nrActiveSteps() {
    if ( steps == null ) {
      return 0;
    }

    int nr = 0;
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      // without also considering a step status of not finished,
      // the step execution results grid shows empty while
      // the transformation has steps still running.
      // if ( sid.step.isRunning() ) nr++;
      if ( sid.step.isRunning() || sid.step.getStatus() != StepExecutionStatus.STATUS_FINISHED ) {
        nr++;
      }
    }
    return nr;
  }

  /**
   * Checks whether the transformation steps are running lookup.
   *
   * @return a boolean array associated with the step list, indicating whether that step is running a lookup.
   */
  public boolean[] getTransStepIsRunningLookup() {
    if ( steps == null ) {
      return null;
    }

    boolean[] tResult = new boolean[steps.size()];
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      tResult[i] = ( sid.step.isRunning() || sid.step.getStatus() != StepExecutionStatus.STATUS_FINISHED );
    }
    return tResult;
  }

  /**
   * Checks the execution status of each step in the transformations.
   *
   * @return an array associated with the step list, indicating the status of that step.
   */
  public StepExecutionStatus[] getTransStepExecutionStatusLookup() {
    if ( steps == null ) {
      return null;
    }

    // we need this snapshot for the TransGridDelegate refresh method to handle the
    // difference between a timed refresh and continual step status updates
    int totalSteps = steps.size();
    StepExecutionStatus[] tList = new StepExecutionStatus[totalSteps];
    for ( int i = 0; i < totalSteps; i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      tList[i] = sid.step.getStatus();
    }
    return tList;
  }

  /**
   * Gets the run thread for the step at the specified index.
   *
   * @param i
   *          the index of the desired step
   * @return a StepInterface object corresponding to the run thread for the specified step
   */
  public StepInterface getRunThread( int i ) {
    if ( steps == null ) {
      return null;
    }
    return steps.get( i ).step;
  }

  /**
   * Gets the run thread for the step with the specified name and copy number.
   *
   * @param name
   *          the step name
   * @param copy
   *          the copy number
   * @return a StepInterface object corresponding to the run thread for the specified step
   */
  public StepInterface getRunThread( String name, int copy ) {
    if ( steps == null ) {
      return null;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface step = sid.step;
      if ( step.getStepname().equalsIgnoreCase( name ) && step.getCopy() == copy ) {
        return step;
      }
    }

    return null;
  }

  /**
   * Calculate the batch id and date range for the transformation.
   *
   * @throws KettleTransException
   *           if there are any errors during calculation
   */
  public void calculateBatchIdAndDateRange() throws KettleTransException {

    TransLogTable transLogTable = transMeta.getTransLogTable();

    currentDate = new Date();
    logDate = new Date();
    startDate = Const.MIN_DATE;
    endDate = currentDate;

    DatabaseMeta logConnection = transLogTable.getDatabaseMeta();
    String logTable = environmentSubstitute( transLogTable.getActualTableName() );
    String logSchema = environmentSubstitute( transLogTable.getActualSchemaName() );

    try {
      if ( logConnection != null ) {

        String logSchemaAndTable = logConnection.getQuotedSchemaTableCombination( logSchema, logTable );
        if ( Utils.isEmpty( logTable ) ) {
          // It doesn't make sense to start database logging without a table
          // to log to.
          throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.NoLogTableDefined" ) );
        }
        if ( Utils.isEmpty( transMeta.getName() ) && logConnection != null && logTable != null ) {
          throw new KettleException( BaseMessages.getString( PKG, "Trans.Exception.NoTransnameAvailableForLogging" ) );
        }
        transLogTableDatabaseConnection = new Database( this, logConnection );
        transLogTableDatabaseConnection.shareVariablesWith( this );
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.OpeningLogConnection", "" + logConnection ) );
        }
        transLogTableDatabaseConnection.connect();
        transLogTableDatabaseConnection.setCommit( logCommitSize );

        // See if we have to add a batch id...
        // Do this first, before anything else to lock the complete table exclusively
        //
        if ( transLogTable.isBatchIdUsed() ) {
          Long id_batch =
              logConnection.getNextBatchId( transLogTableDatabaseConnection, logSchema, logTable, transLogTable
                  .getKeyField().getFieldName() );
          setBatchId( id_batch.longValue() );
        }

        //
        // Get the date range from the logging table: from the last end_date to now. (currentDate)
        //
        Object[] lastr =
            transLogTableDatabaseConnection.getLastLogDate( logSchemaAndTable, transMeta.getName(), false,
                LogStatus.END );
        if ( lastr != null && lastr.length > 0 ) {
          startDate = (Date) lastr[0];
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.StartDateFound" ) + startDate );
          }
        }

        //
        // OK, we have a date-range.
        // However, perhaps we need to look at a table before we make a final judgment?
        //
        if ( transMeta.getMaxDateConnection() != null && transMeta.getMaxDateTable() != null && transMeta
            .getMaxDateTable().length() > 0 && transMeta.getMaxDateField() != null && transMeta.getMaxDateField()
                .length() > 0 ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.LookingForMaxdateConnection", "" + transMeta
                .getMaxDateConnection() ) );
          }
          DatabaseMeta maxcon = transMeta.getMaxDateConnection();
          if ( maxcon != null ) {
            Database maxdb = new Database( this, maxcon );
            maxdb.shareVariablesWith( this );
            try {
              if ( log.isDetailed() ) {
                log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.OpeningMaximumDateConnection" ) );
              }
              maxdb.connect();
              maxdb.setCommit( logCommitSize );

              //
              // Determine the endDate by looking at a field in a table...
              //
              String sql = "SELECT MAX(" + transMeta.getMaxDateField() + ") FROM " + transMeta.getMaxDateTable();
              RowMetaAndData r1 = maxdb.getOneRow( sql );
              if ( r1 != null ) {
                // OK, we have a value, what's the offset?
                Date maxvalue = r1.getRowMeta().getDate( r1.getData(), 0 );
                if ( maxvalue != null ) {
                  if ( log.isDetailed() ) {
                    log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.LastDateFoundOnTheMaxdateConnection" )
                        + r1 );
                  }
                  endDate.setTime( (long) ( maxvalue.getTime() + ( transMeta.getMaxDateOffset() * 1000 ) ) );
                }
              } else {
                if ( log.isDetailed() ) {
                  log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.NoLastDateFoundOnTheMaxdateConnection" ) );
                }
              }
            } catch ( KettleException e ) {
              throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.ErrorConnectingToDatabase",
                  "" + transMeta.getMaxDateConnection() ), e );
            } finally {
              maxdb.disconnect();
            }
          } else {
            throw new KettleTransException( BaseMessages.getString( PKG,
                "Trans.Exception.MaximumDateConnectionCouldNotBeFound", "" + transMeta.getMaxDateConnection() ) );
          }
        }

        // Determine the last date of all dependend tables...
        // Get the maximum in depdate...
        if ( transMeta.nrDependencies() > 0 ) {
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.CheckingForMaxDependencyDate" ) );
          }
          //
          // Maybe one of the tables where this transformation is dependent on has changed?
          // If so we need to change the start-date!
          //
          depDate = Const.MIN_DATE;
          Date maxdepdate = Const.MIN_DATE;
          if ( lastr != null && lastr.length > 0 ) {
            Date dep = (Date) lastr[1]; // #1: last depdate
            if ( dep != null ) {
              maxdepdate = dep;
              depDate = dep;
            }
          }

          for ( int i = 0; i < transMeta.nrDependencies(); i++ ) {
            TransDependency td = transMeta.getDependency( i );
            DatabaseMeta depcon = td.getDatabase();
            if ( depcon != null ) {
              Database depdb = new Database( this, depcon );
              try {
                depdb.connect();
                depdb.setCommit( logCommitSize );

                String sql = "SELECT MAX(" + td.getFieldname() + ") FROM " + td.getTablename();
                RowMetaAndData r1 = depdb.getOneRow( sql );
                if ( r1 != null ) {
                  // OK, we have a row, get the result!
                  Date maxvalue = (Date) r1.getData()[0];
                  if ( maxvalue != null ) {
                    if ( log.isDetailed() ) {
                      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.FoundDateFromTable", td.getTablename(),
                          "." + td.getFieldname(), " = " + maxvalue.toString() ) );
                    }
                    if ( maxvalue.getTime() > maxdepdate.getTime() ) {
                      maxdepdate = maxvalue;
                    }
                  } else {
                    throw new KettleTransException( BaseMessages.getString( PKG,
                        "Trans.Exception.UnableToGetDependencyInfoFromDB", td.getDatabase().getName() + ".", td
                            .getTablename() + ".", td.getFieldname() ) );
                  }
                } else {
                  throw new KettleTransException( BaseMessages.getString( PKG,
                      "Trans.Exception.UnableToGetDependencyInfoFromDB", td.getDatabase().getName() + ".", td
                          .getTablename() + ".", td.getFieldname() ) );
                }
              } catch ( KettleException e ) {
                throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.ErrorInDatabase", "" + td
                    .getDatabase() ), e );
              } finally {
                depdb.disconnect();
              }
            } else {
              throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.ConnectionCouldNotBeFound",
                  "" + td.getDatabase() ) );
            }
            if ( log.isDetailed() ) {
              log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.Maxdepdate" ) + ( XMLHandler.date2string(
                  maxdepdate ) ) );
            }
          }

          // OK, so we now have the maximum depdate;
          // If it is larger, it means we have to read everything back in again.
          // Maybe something has changed that we need!
          //
          if ( maxdepdate.getTime() > depDate.getTime() ) {
            depDate = maxdepdate;
            startDate = Const.MIN_DATE;
          }
        } else {
          depDate = currentDate;
        }
      }

      // OK, now we have a date-range. See if we need to set a maximum!
      if ( transMeta.getMaxDateDifference() > 0.0 && // Do we have a difference specified?
          startDate.getTime() > Const.MIN_DATE.getTime() // Is the startdate > Minimum?
      ) {
        // See if the end-date is larger then Start_date + DIFF?
        Date maxdesired = new Date( startDate.getTime() + ( (long) transMeta.getMaxDateDifference() * 1000 ) );

        // If this is the case: lower the end-date. Pick up the next 'region' next time around.
        // We do this to limit the workload in a single update session (e.g. for large fact tables)
        //
        if ( endDate.compareTo( maxdesired ) > 0 ) {
          endDate = maxdesired;
        }
      }

    } catch ( KettleException e ) {
      throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.ErrorCalculatingDateRange",
          logTable ), e );
    }

    // Be careful, We DO NOT close the trans log table database connection!!!
    // It's closed later in beginProcessing() to prevent excessive connect/disconnect repetitions.

  }

  /**
   * Begin processing. Also handle logging operations related to the start of the transformation
   *
   * @throws KettleTransException
   *           the kettle trans exception
   */
  public void beginProcessing() throws KettleTransException {
    TransLogTable transLogTable = transMeta.getTransLogTable();
    int intervalInSeconds = Const.toInt( environmentSubstitute( transLogTable.getLogInterval() ), -1 );

    try {
      String logTable = transLogTable.getActualTableName();

      SimpleDateFormat df = new SimpleDateFormat( REPLAY_DATE_FORMAT );
      log.logDetailed( BaseMessages.getString( PKG, "Trans.Log.TransformationCanBeReplayed" ) + df.format(
          currentDate ) );

      try {
        if ( transLogTableDatabaseConnection != null && !Utils.isEmpty( logTable ) && !Utils.isEmpty( transMeta
            .getName() ) ) {
          transLogTableDatabaseConnection.writeLogRecord( transLogTable, LogStatus.START, this, null );

          // Pass in a commit to release transaction locks and to allow a user to actually see the log record.
          //
          if ( !transLogTableDatabaseConnection.isAutoCommit() ) {
            transLogTableDatabaseConnection.commitLog( true, transLogTable );
          }

          // If we need to do periodic logging, make sure to install a timer for this...
          //
          if ( intervalInSeconds > 0 ) {
            final Timer timer = new Timer( getName() + " - interval logging timer" );
            TimerTask timerTask = new TimerTask() {
              @Override
              public void run() {
                try {
                  endProcessing();
                } catch ( Exception e ) {
                  log.logError( BaseMessages.getString( PKG, "Trans.Exception.UnableToPerformIntervalLogging" ), e );
                  // Also stop the show...
                  //
                  errors.incrementAndGet();
                  stopAll();
                }
              }
            };
            timer.schedule( timerTask, intervalInSeconds * 1000, intervalInSeconds * 1000 );

            addTransListener( new TransAdapter() {
              @Override
              public void transFinished( Trans trans ) {
                timer.cancel();
              }
            } );
          }

          // Add a listener to make sure that the last record is also written when transformation finishes...
          //
          addTransListener( new TransAdapter() {
            @Override
            public void transFinished( Trans trans ) throws KettleException {
              try {
                endProcessing();

                lastWrittenStepPerformanceSequenceNr =
                    writeStepPerformanceLogRecords( lastWrittenStepPerformanceSequenceNr, LogStatus.END );

              } catch ( KettleException e ) {
                throw new KettleException( BaseMessages.getString( PKG,
                    "Trans.Exception.UnableToPerformLoggingAtTransEnd" ), e );
              }
            }
          } );

        }

        // If we need to write out the step logging information, do so at the end of the transformation too...
        //
        StepLogTable stepLogTable = transMeta.getStepLogTable();
        if ( stepLogTable.isDefined() ) {
          addTransListener( new TransAdapter() {
            @Override
            public void transFinished( Trans trans ) throws KettleException {
              try {
                writeStepLogInformation();
              } catch ( KettleException e ) {
                throw new KettleException( BaseMessages.getString( PKG,
                    "Trans.Exception.UnableToPerformLoggingAtTransEnd" ), e );
              }
            }
          } );
        }

        // If we need to write the log channel hierarchy and lineage information, add a listener for that too...
        //
        ChannelLogTable channelLogTable = transMeta.getChannelLogTable();
        if ( channelLogTable.isDefined() ) {
          addTransListener( new TransAdapter() {
            @Override
            public void transFinished( Trans trans ) throws KettleException {
              try {
                writeLogChannelInformation();
              } catch ( KettleException e ) {
                throw new KettleException( BaseMessages.getString( PKG,
                    "Trans.Exception.UnableToPerformLoggingAtTransEnd" ), e );
              }
            }
          } );
        }

        // See if we need to write the step performance records at intervals too...
        //
        PerformanceLogTable performanceLogTable = transMeta.getPerformanceLogTable();
        int perfLogInterval = Const.toInt( environmentSubstitute( performanceLogTable.getLogInterval() ), -1 );
        if ( performanceLogTable.isDefined() && perfLogInterval > 0 ) {
          final Timer timer = new Timer( getName() + " - step performance log interval timer" );
          TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
              try {
                lastWrittenStepPerformanceSequenceNr =
                    writeStepPerformanceLogRecords( lastWrittenStepPerformanceSequenceNr, LogStatus.RUNNING );
              } catch ( Exception e ) {
                log.logError( BaseMessages.getString( PKG,
                    "Trans.Exception.UnableToPerformIntervalPerformanceLogging" ), e );
                // Also stop the show...
                //
                errors.incrementAndGet();
                stopAll();
              }
            }
          };
          timer.schedule( timerTask, perfLogInterval * 1000, perfLogInterval * 1000 );

          addTransListener( new TransAdapter() {
            @Override
            public void transFinished( Trans trans ) {
              timer.cancel();
            }
          } );
        }
      } catch ( KettleException e ) {
        throw new KettleTransException( BaseMessages.getString( PKG, "Trans.Exception.ErrorWritingLogRecordToTable",
            logTable ), e );
      } finally {
        // If we use interval logging, we keep the connection open for performance reasons...
        //
        if ( transLogTableDatabaseConnection != null && ( intervalInSeconds <= 0 ) ) {
          transLogTableDatabaseConnection.disconnect();
          transLogTableDatabaseConnection = null;
        }
      }
    } catch ( KettleException e ) {
      throw new KettleTransException( BaseMessages.getString( PKG,
          "Trans.Exception.UnableToBeginProcessingTransformation" ), e );
    }
  }

  /**
   * Writes log channel information to a channel logging table (if one has been configured).
   *
   * @throws KettleException
   *           if any errors occur during logging
   */
  protected void writeLogChannelInformation() throws KettleException {
    Database db = null;
    ChannelLogTable channelLogTable = transMeta.getChannelLogTable();

    // PDI-7070: If parent trans or job has the same channel logging info, don't duplicate log entries
    Trans t = getParentTrans();
    if ( t != null ) {
      if ( channelLogTable.equals( t.getTransMeta().getChannelLogTable() ) ) {
        return;
      }
    }

    Job j = getParentJob();

    if ( j != null ) {
      if ( channelLogTable.equals( j.getJobMeta().getChannelLogTable() ) ) {
        return;
      }
    }
    // end PDI-7070

    try {
      db = new Database( this, channelLogTable.getDatabaseMeta() );
      db.shareVariablesWith( this );
      db.connect();
      db.setCommit( logCommitSize );

      List<LoggingHierarchy> loggingHierarchyList = getLoggingHierarchy();
      for ( LoggingHierarchy loggingHierarchy : loggingHierarchyList ) {
        db.writeLogRecord( channelLogTable, LogStatus.START, loggingHierarchy, null );
      }

      // Also time-out the log records in here...
      //
      db.cleanupLogRecords( channelLogTable );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "Trans.Exception.UnableToWriteLogChannelInformationToLogTable" ), e );
    } finally {
      if ( !db.isAutoCommit() ) {
        db.commit( true );
      }
      db.disconnect();
    }
  }

  /**
   * Writes step information to a step logging table (if one has been configured).
   *
   * @throws KettleException
   *           if any errors occur during logging
   */
  protected void writeStepLogInformation() throws KettleException {
    Database db = null;
    StepLogTable stepLogTable = getTransMeta().getStepLogTable();
    try {
      db = createDataBase( stepLogTable.getDatabaseMeta() );
      db.shareVariablesWith( this );
      db.connect();
      db.setCommit( logCommitSize );

      for ( StepMetaDataCombi combi : getSteps() ) {
        db.writeLogRecord( stepLogTable, LogStatus.START, combi, null );
      }

      db.cleanupLogRecords( stepLogTable );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "Trans.Exception.UnableToWriteStepInformationToLogTable" ), e );
    } finally {
      if ( !db.isAutoCommit() ) {
        db.commit( true );
      }
      db.disconnect();
    }

  }

  protected Database createDataBase( DatabaseMeta meta ) {
    return new Database( this, meta );
  }

  protected synchronized void writeMetricsInformation() throws KettleException {
    //
    List<MetricsDuration> metricsList =
        MetricsUtil.getDuration( log.getLogChannelId(), Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_START );
    if ( ( log != null ) && ( log.isDebug() ) && !metricsList.isEmpty() ) {
      log.logDebug( metricsList.get( 0 ).toString() );
    }

    metricsList =
        MetricsUtil.getDuration( log.getLogChannelId(), Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_START );
    if ( ( log != null ) && ( log.isDebug() ) && !metricsList.isEmpty() ) {
      log.logDebug( metricsList.get( 0 ).toString() );
    }

    long total = 0;
    metricsList =
        MetricsUtil.getDuration( log.getLogChannelId(), Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_START );
    if ( ( log != null ) && ( log.isDebug() ) && metricsList != null && !metricsList.isEmpty() ) {
      for ( MetricsDuration duration : metricsList ) {
        total += duration.getDuration();
        log.logDebug( "   - " + duration.toString() + "  Total=" + total );
      }
    }

    Database db = null;
    MetricsLogTable metricsLogTable = transMeta.getMetricsLogTable();
    try {
      db = new Database( this, metricsLogTable.getDatabaseMeta() );
      db.shareVariablesWith( this );
      db.connect();
      db.setCommit( logCommitSize );

      List<String> logChannelIds = LoggingRegistry.getInstance().getLogChannelChildren( getLogChannelId() );
      for ( String logChannelId : logChannelIds ) {
        Queue<MetricsSnapshotInterface> snapshotList =
            MetricsRegistry.getInstance().getSnapshotLists().get( logChannelId );
        if ( snapshotList != null ) {
          Iterator<MetricsSnapshotInterface> iterator = snapshotList.iterator();
          while ( iterator.hasNext() ) {
            MetricsSnapshotInterface snapshot = iterator.next();
            db.writeLogRecord( metricsLogTable, LogStatus.START, new LoggingMetric( batchId, snapshot ), null );
          }
        }

        Map<String, MetricsSnapshotInterface> snapshotMap =
            MetricsRegistry.getInstance().getSnapshotMaps().get( logChannelId );
        if ( snapshotMap != null ) {
          synchronized ( snapshotMap ) {
            Iterator<MetricsSnapshotInterface> iterator = snapshotMap.values().iterator();
            while ( iterator.hasNext() ) {
              MetricsSnapshotInterface snapshot = iterator.next();
              db.writeLogRecord( metricsLogTable, LogStatus.START, new LoggingMetric( batchId, snapshot ), null );
            }
          }
        }
      }

      // Also time-out the log records in here...
      //
      db.cleanupLogRecords( metricsLogTable );
    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "Trans.Exception.UnableToWriteMetricsInformationToLogTable" ), e );
    } finally {
      if ( !db.isAutoCommit() ) {
        db.commit( true );
      }
      db.disconnect();
    }
  }

  /**
   * Gets the result of the transformation. The Result object contains such measures as the number of errors, number of
   * lines read/written/input/output/updated/rejected, etc.
   *
   * @return the Result object containing resulting measures from execution of the transformation
   */
  public Result getResult() {
    if ( steps == null ) {
      return null;
    }

    Result result = new Result();
    result.setNrErrors( errors.longValue() );
    result.setResult( errors.longValue() == 0 );
    TransLogTable transLogTable = transMeta.getTransLogTable();

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface step = sid.step;

      result.setNrErrors( result.getNrErrors() + sid.step.getErrors() );
      result.getResultFiles().putAll( step.getResultFiles() );

      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_READ ) ) ) {
        result.setNrLinesRead( result.getNrLinesRead() + step.getLinesRead() );
      }
      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_INPUT ) ) ) {
        result.setNrLinesInput( result.getNrLinesInput() + step.getLinesInput() );
      }
      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_WRITTEN ) ) ) {
        result.setNrLinesWritten( result.getNrLinesWritten() + step.getLinesWritten() );
      }
      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_OUTPUT ) ) ) {
        result.setNrLinesOutput( result.getNrLinesOutput() + step.getLinesOutput() );
      }
      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_UPDATED ) ) ) {
        result.setNrLinesUpdated( result.getNrLinesUpdated() + step.getLinesUpdated() );
      }
      if ( step.getStepname().equals( transLogTable.getSubjectString( TransLogTable.ID.LINES_REJECTED ) ) ) {
        result.setNrLinesRejected( result.getNrLinesRejected() + step.getLinesRejected() );
      }
    }

    result.setRows( resultRows );
    if ( !Utils.isEmpty( resultFiles ) ) {
      result.setResultFiles( new HashMap<String, ResultFile>() );
      for ( ResultFile resultFile : resultFiles ) {
        result.getResultFiles().put( resultFile.toString(), resultFile );
      }
    }
    result.setStopped( isStopped() );
    result.setLogChannelId( log.getLogChannelId() );

    return result;
  }

  /**
   * End processing. Also handle any logging operations associated with the end of a transformation
   *
   * @return true if all end processing is successful, false otherwise
   * @throws KettleException
   *           if any errors occur during processing
   */
  private synchronized boolean endProcessing() throws KettleException {
    LogStatus status;

    if ( isFinished() ) {
      if ( isStopped() ) {
        status = LogStatus.STOP;
      } else {
        status = LogStatus.END;
      }
    } else if ( isPaused() ) {
      status = LogStatus.PAUSED;
    } else {
      status = LogStatus.RUNNING;
    }

    TransLogTable transLogTable = transMeta.getTransLogTable();
    int intervalInSeconds = Const.toInt( environmentSubstitute( transLogTable.getLogInterval() ), -1 );

    logDate = new Date();

    // OK, we have some logging to do...
    //
    DatabaseMeta logcon = transMeta.getTransLogTable().getDatabaseMeta();
    String logTable = transMeta.getTransLogTable().getActualTableName();
    if ( logcon != null ) {
      Database ldb = null;

      try {
        // Let's not reconnect/disconnect all the time for performance reasons!
        //
        if ( transLogTableDatabaseConnection == null ) {
          ldb = new Database( this, logcon );
          ldb.shareVariablesWith( this );
          ldb.connect();
          ldb.setCommit( logCommitSize );
          transLogTableDatabaseConnection = ldb;
        } else {
          ldb = transLogTableDatabaseConnection;
        }

        // Write to the standard transformation log table...
        //
        if ( !Utils.isEmpty( logTable ) ) {
          ldb.writeLogRecord( transLogTable, status, this, null );
        }

        // Also time-out the log records in here...
        //
        if ( status.equals( LogStatus.END ) || status.equals( LogStatus.STOP ) ) {
          ldb.cleanupLogRecords( transLogTable );
        }

        // Commit the operations to prevent locking issues
        //
        if ( !ldb.isAutoCommit() ) {
          ldb.commitLog( true, transMeta.getTransLogTable() );
        }
      } catch ( KettleDatabaseException e ) {
        // PDI-9790 error write to log db is transaction error
        log.logError( BaseMessages.getString( PKG, "Database.Error.WriteLogTable", logTable ), e );
        errors.incrementAndGet();
        // end PDI-9790
      } catch ( Exception e ) {
        throw new KettleException( BaseMessages.getString( PKG, "Trans.Exception.ErrorWritingLogRecordToTable",
            transMeta.getTransLogTable().getActualTableName() ), e );
      } finally {
        if ( intervalInSeconds <= 0 || ( status.equals( LogStatus.END ) || status.equals( LogStatus.STOP ) ) ) {
          ldb.disconnect();
          transLogTableDatabaseConnection = null; // disconnected
        }
      }
    }
    return true;
  }

  /**
   * Write step performance log records.
   *
   * @param startSequenceNr
   *          the start sequence numberr
   * @param status
   *          the logging status. If this is End, perform cleanup
   * @return the new sequence number
   * @throws KettleException
   *           if any errors occur during logging
   */
  private int writeStepPerformanceLogRecords( int startSequenceNr, LogStatus status ) throws KettleException {
    int lastSeqNr = 0;
    Database ldb = null;
    PerformanceLogTable performanceLogTable = transMeta.getPerformanceLogTable();

    if ( !performanceLogTable.isDefined() || !transMeta.isCapturingStepPerformanceSnapShots()
        || stepPerformanceSnapShots == null || stepPerformanceSnapShots.isEmpty() ) {
      return 0; // nothing to do here!
    }

    try {
      ldb = new Database( this, performanceLogTable.getDatabaseMeta() );
      ldb.shareVariablesWith( this );
      ldb.connect();
      ldb.setCommit( logCommitSize );

      // Write to the step performance log table...
      //
      RowMetaInterface rowMeta = performanceLogTable.getLogRecord( LogStatus.START, null, null ).getRowMeta();
      ldb.prepareInsert( rowMeta, performanceLogTable.getActualSchemaName(), performanceLogTable.getActualTableName() );

      synchronized ( stepPerformanceSnapShots ) {
        Iterator<List<StepPerformanceSnapShot>> iterator = stepPerformanceSnapShots.values().iterator();
        while ( iterator.hasNext() ) {
          List<StepPerformanceSnapShot> snapshots = iterator.next();
          synchronized ( snapshots ) {
            Iterator<StepPerformanceSnapShot> snapshotsIterator = snapshots.iterator();
            while ( snapshotsIterator.hasNext() ) {
              StepPerformanceSnapShot snapshot = snapshotsIterator.next();
              if ( snapshot.getSeqNr() >= startSequenceNr && snapshot
                  .getSeqNr() <= lastStepPerformanceSnapshotSeqNrAdded ) {

                RowMetaAndData row = performanceLogTable.getLogRecord( LogStatus.START, snapshot, null );

                ldb.setValuesInsert( row.getRowMeta(), row.getData() );
                ldb.insertRow( true );
              }
              lastSeqNr = snapshot.getSeqNr();
            }
          }
        }
      }

      ldb.insertFinished( true );

      // Finally, see if the log table needs cleaning up...
      //
      if ( status.equals( LogStatus.END ) ) {
        ldb.cleanupLogRecords( performanceLogTable );
      }

    } catch ( Exception e ) {
      throw new KettleException( BaseMessages.getString( PKG,
          "Trans.Exception.ErrorWritingStepPerformanceLogRecordToTable" ), e );
    } finally {
      if ( ldb != null ) {
        ldb.disconnect();
      }
    }

    return lastSeqNr + 1;
  }

  /**
   * Close unique database connections. If there are errors in the Result, perform a rollback
   *
   * @param result
   *          the result of the transformation execution
   */
  private void closeUniqueDatabaseConnections( Result result ) {

    // Don't close any connections if the parent job is using the same transaction
    //
    if ( parentJob != null && transactionId != null && parentJob.getTransactionId() != null && transactionId.equals(
        parentJob.getTransactionId() ) ) {
      return;
    }

    // Don't close any connections if the parent transformation is using the same transaction
    //
    if ( parentTrans != null && parentTrans.getTransMeta().isUsingUniqueConnections() && transactionId != null
        && parentTrans.getTransactionId() != null && transactionId.equals( parentTrans.getTransactionId() ) ) {
      return;
    }

    // First we get all the database connections ...
    //
    DatabaseConnectionMap map = DatabaseConnectionMap.getInstance();
    synchronized ( map ) {
      List<Database> databaseList = new ArrayList<>( map.getMap().values() );
      for ( Database database : databaseList ) {
        if ( database.getConnectionGroup().equals( getTransactionId() ) ) {
          try {
            // This database connection belongs to this transformation.
            // Let's roll it back if there is an error...
            //
            if ( result.getNrErrors() > 0 ) {
              try {
                database.rollback( true );
                log.logBasic( BaseMessages.getString( PKG, "Trans.Exception.TransactionsRolledBackOnConnection",
                    database.toString() ) );
              } catch ( Exception e ) {
                throw new KettleDatabaseException( BaseMessages.getString( PKG,
                    "Trans.Exception.ErrorRollingBackUniqueConnection", database.toString() ), e );
              }
            } else {
              try {
                database.commit( true );
                log.logBasic( BaseMessages.getString( PKG, "Trans.Exception.TransactionsCommittedOnConnection", database
                    .toString() ) );
              } catch ( Exception e ) {
                throw new KettleDatabaseException( BaseMessages.getString( PKG,
                    "Trans.Exception.ErrorCommittingUniqueConnection", database.toString() ), e );
              }
            }
          } catch ( Exception e ) {
            log.logError( BaseMessages.getString( PKG, "Trans.Exception.ErrorHandlingTransformationTransaction",
                database.toString() ), e );
            result.setNrErrors( result.getNrErrors() + 1 );
          } finally {
            try {
              // This database connection belongs to this transformation.
              database.closeConnectionOnly();
            } catch ( Exception e ) {
              log.logError( BaseMessages.getString( PKG, "Trans.Exception.ErrorHandlingTransformationTransaction",
                  database.toString() ), e );
              result.setNrErrors( result.getNrErrors() + 1 );
            } finally {
              // Remove the database from the list...
              //
              map.removeConnection( database.getConnectionGroup(), database.getPartitionId(), database );
            }
          }
        }
      }

      // Who else needs to be informed of the rollback or commit?
      //
      List<DatabaseTransactionListener> transactionListeners = map.getTransactionListeners( getTransactionId() );
      if ( result.getNrErrors() > 0 ) {
        for ( DatabaseTransactionListener listener : transactionListeners ) {
          try {
            listener.rollback();
          } catch ( Exception e ) {
            log.logError( BaseMessages.getString( PKG, "Trans.Exception.ErrorHandlingTransactionListenerRollback" ),
                e );
            result.setNrErrors( result.getNrErrors() + 1 );
          }
        }
      } else {
        for ( DatabaseTransactionListener listener : transactionListeners ) {
          try {
            listener.commit();
          } catch ( Exception e ) {
            log.logError( BaseMessages.getString( PKG, "Trans.Exception.ErrorHandlingTransactionListenerCommit" ), e );
            result.setNrErrors( result.getNrErrors() + 1 );
          }
        }
      }

    }
  }

  /**
   * Find the run thread for the step with the specified name.
   *
   * @param stepname
   *          the step name
   * @return a StepInterface object corresponding to the run thread for the specified step
   */
  public StepInterface findRunThread( String stepname ) {
    if ( steps == null ) {
      return null;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface step = sid.step;
      if ( step.getStepname().equalsIgnoreCase( stepname ) ) {
        return step;
      }
    }
    return null;
  }

  /**
   * Find the base steps for the step with the specified name.
   *
   * @param stepname
   *          the step name
   * @return the list of base steps for the specified step
   */
  public List<StepInterface> findBaseSteps( String stepname ) {
    List<StepInterface> baseSteps = new ArrayList<>();

    if ( steps == null ) {
      return baseSteps;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface stepInterface = sid.step;
      if ( stepInterface.getStepname().equalsIgnoreCase( stepname ) ) {
        baseSteps.add( stepInterface );
      }
    }
    return baseSteps;
  }

  /**
   * Find the executing step copy for the step with the specified name and copy number
   *
   * @param stepname
   *          the step name
   * @param copynr
   * @return the executing step found or null if no copy could be found.
   */
  public StepInterface findStepInterface( String stepname, int copyNr ) {
    if ( steps == null ) {
      return null;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface stepInterface = sid.step;
      if ( stepInterface.getStepname().equalsIgnoreCase( stepname ) && sid.copy == copyNr ) {
        return stepInterface;
      }
    }
    return null;
  }

  /**
   * Find the available executing step copies for the step with the specified name
   *
   * @param stepname
   *          the step name
   * @param copynr
   * @return the list of executing step copies found or null if no steps are available yet (incorrect usage)
   */
  public List<StepInterface> findStepInterfaces( String stepname ) {
    if ( steps == null ) {
      return null;
    }

    List<StepInterface> list = new ArrayList<>();

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface stepInterface = sid.step;
      if ( stepInterface.getStepname().equalsIgnoreCase( stepname ) ) {
        list.add( stepInterface );
      }
    }
    return list;
  }

  /**
   * Find the data interface for the step with the specified name.
   *
   * @param name
   *          the step name
   * @return the step data interface
   */
  public StepDataInterface findDataInterface( String name ) {
    if ( steps == null ) {
      return null;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      StepInterface rt = sid.step;
      if ( rt.getStepname().equalsIgnoreCase( name ) ) {
        return sid.data;
      }
    }
    return null;
  }

  /**
   * Gets the start date/time object for the transformation.
   *
   * @return Returns the startDate.
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * Gets the end date/time object for the transformation.
   *
   * @return Returns the endDate.
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Checks whether the running transformation is being monitored.
   *
   * @return true the running transformation is being monitored, false otherwise
   */
  public boolean isMonitored() {
    return monitored;
  }

  /**
   * Sets whether the running transformation should be monitored.
   *
   * @param monitored
   *          true if the running transformation should be monitored, false otherwise
   */
  public void setMonitored( boolean monitored ) {
    this.monitored = monitored;
  }

  /**
   * Gets the meta-data for the transformation.
   *
   * @return Returns the transformation meta-data
   */
  public TransMeta getTransMeta() {
    return transMeta;
  }

  /**
   * Sets the meta-data for the transformation.
   *
   * @param transMeta
   *          The transformation meta-data to set.
   */
  public void setTransMeta( TransMeta transMeta ) {
    this.transMeta = transMeta;
  }

  /**
   * Gets the current date/time object.
   *
   * @return the current date
   */
  public Date getCurrentDate() {
    return currentDate;
  }

  /**
   * Gets the dependency date for the transformation. A transformation can have a list of dependency fields. If any of
   * these fields have a maximum date higher than the dependency date of the last run, the date range is set to to (-oo,
   * now). The use-case is the incremental population of Slowly Changing Dimensions (SCD).
   *
   * @return Returns the dependency date
   */
  public Date getDepDate() {
    return depDate;
  }

  /**
   * Gets the date the transformation was logged.
   *
   * @return the log date
   */
  public Date getLogDate() {
    return logDate;
  }

  /**
   * Gets the rowsets for the transformation.
   *
   * @return a list of rowsets
   */
  public List<RowSet> getRowsets() {
    return rowsets;
  }

  /**
   * Gets a list of steps in the transformation.
   *
   * @return a list of the steps in the transformation
   */
  public List<StepMetaDataCombi> getSteps() {
    return steps;
  }

  protected void setSteps( List<StepMetaDataCombi> steps ) {
    this.steps = steps;
  }

  /**
   * Gets a string representation of the transformation.
   *
   * @return the string representation of the transformation
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    if ( transMeta == null || transMeta.getName() == null ) {
      return getClass().getSimpleName();
    }

    // See if there is a parent transformation. If so, print the name of the parent here as well...
    //
    StringBuilder string = new StringBuilder( 50 );

    // If we're running as a mapping, we get a reference to the calling (parent) transformation as well...
    //
    if ( getParentTrans() != null ) {
      string.append( '[' ).append( getParentTrans().toString() ).append( ']' ).append( '.' );
    }

    // When we run a mapping we also set a mapping step name in there...
    //
    if ( !Utils.isEmpty( mappingStepName ) ) {
      string.append( '[' ).append( mappingStepName ).append( ']' ).append( '.' );
    }

    string.append( transMeta.getName() );

    return string.toString();
  }

  /**
   * Gets the mapping inputs for each step in the transformation.
   *
   * @return an array of MappingInputs
   */
  public MappingInput[] findMappingInput() {
    if ( steps == null ) {
      return null;
    }

    List<MappingInput> list = new ArrayList<>();

    // Look in threads and find the MappingInput step thread...
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi smdc = steps.get( i );
      StepInterface step = smdc.step;
      if ( step.getStepID().equalsIgnoreCase( "MappingInput" ) ) {
        list.add( (MappingInput) step );
      }
    }
    return list.toArray( new MappingInput[list.size()] );
  }

  /**
   * Gets the mapping outputs for each step in the transformation.
   *
   * @return an array of MappingOutputs
   */
  public MappingOutput[] findMappingOutput() {
    List<MappingOutput> list = new ArrayList<>();

    if ( steps != null ) {
      // Look in threads and find the MappingInput step thread...
      for ( int i = 0; i < steps.size(); i++ ) {
        StepMetaDataCombi smdc = steps.get( i );
        StepInterface step = smdc.step;
        if ( step.getStepID().equalsIgnoreCase( "MappingOutput" ) ) {
          list.add( (MappingOutput) step );
        }
      }
    }
    return list.toArray( new MappingOutput[list.size()] );
  }

  /**
   * Find the StepInterface (thread) by looking it up using the name.
   *
   * @param stepname
   *          The name of the step to look for
   * @param copy
   *          the copy number of the step to look for
   * @return the StepInterface or null if nothing was found.
   */
  public StepInterface getStepInterface( String stepname, int copy ) {
    if ( steps == null ) {
      return null;
    }

    // Now start all the threads...
    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      if ( sid.stepname.equalsIgnoreCase( stepname ) && sid.copy == copy ) {
        return sid.step;
      }
    }

    return null;
  }

  /**
   * Gets the replay date. The replay date is used to indicate that the transformation was replayed (re-tried, run
   * again) with that particular replay date. You can use this in Text File/Excel Input to allow you to save error line
   * numbers into a file (SOURCE_FILE.line for example) During replay, only the lines that have errors in them are
   * passed to the next steps, the other lines are ignored. This is for the use case: if the document contained errors
   * (bad dates, chars in numbers, etc), you simply send the document back to the source (the user/departement that
   * created it probably) and when you get it back, re-run the last transformation.
   *
   * @return the replay date
   */
  public Date getReplayDate() {
    return replayDate;
  }

  /**
   * Sets the replay date. The replay date is used to indicate that the transformation was replayed (re-tried, run
   * again) with that particular replay date. You can use this in Text File/Excel Input to allow you to save error line
   * numbers into a file (SOURCE_FILE.line for example) During replay, only the lines that have errors in them are
   * passed to the next steps, the other lines are ignored. This is for the use case: if the document contained errors
   * (bad dates, chars in numbers, etc), you simply send the document back to the source (the user/departement that
   * created it probably) and when you get it back, re-run the last transformation.
   *
   * @param replayDate
   *          the new replay date
   */
  public void setReplayDate( Date replayDate ) {
    this.replayDate = replayDate;
  }

  /**
   * Turn on safe mode during running: the transformation will run slower but with more checking enabled.
   *
   * @param safeModeEnabled
   *          true for safe mode
   */
  public void setSafeModeEnabled( boolean safeModeEnabled ) {
    this.safeModeEnabled = safeModeEnabled;
  }

  /**
   * Checks whether safe mode is enabled.
   *
   * @return Returns true if the safe mode is enabled: the transformation will run slower but with more checking enabled
   */
  public boolean isSafeModeEnabled() {
    return safeModeEnabled;
  }

  /**
   * This adds a row producer to the transformation that just got set up. It is preferable to run this BEFORE execute()
   * but after prepareExecution()
   *
   * @param stepname
   *          The step to produce rows for
   * @param copynr
   *          The copynr of the step to produce row for (normally 0 unless you have multiple copies running)
   * @return the row producer
   * @throws KettleException
   *           in case the thread/step to produce rows for could not be found.
   * @see Trans#execute(String[])
   * @see Trans#prepareExecution(String[])
   */
  public RowProducer addRowProducer( String stepname, int copynr ) throws KettleException {
    StepInterface stepInterface = getStepInterface( stepname, copynr );
    if ( stepInterface == null ) {
      throw new KettleException( "Unable to find thread with name " + stepname + " and copy number " + copynr );
    }

    // We are going to add an extra RowSet to this stepInterface.
    RowSet rowSet;
    switch ( transMeta.getTransformationType() ) {
      case Normal:
        rowSet = new BlockingRowSet( transMeta.getSizeRowset() );
        break;
      case SerialSingleThreaded:
        rowSet = new SingleRowRowSet();
        break;
      case SingleThreaded:
        rowSet = new QueueRowSet();
        break;
      default:
        throw new KettleException( "Unhandled transformation type: " + transMeta.getTransformationType() );
    }

    // Add this rowset to the list of active rowsets for the selected step
    stepInterface.addRowSetToInputRowSets( rowSet );

    return new RowProducer( stepInterface, rowSet );
  }

  /**
   * Gets the parent job, or null if there is no parent.
   *
   * @return the parent job, or null if there is no parent
   */
  public Job getParentJob() {
    return parentJob;
  }

  /**
   * Sets the parent job for the transformation.
   *
   * @param parentJob
   *          The parent job to set
   */
  public void setParentJob( Job parentJob ) {
    this.logLevel = parentJob.getLogLevel();
    this.log.setLogLevel( logLevel );
    this.parentJob = parentJob;

    transactionId = calculateTransactionId();
  }

  /**
   * Finds the StepDataInterface (currently) associated with the specified step.
   *
   * @param stepname
   *          The name of the step to look for
   * @param stepcopy
   *          The copy number (0 based) of the step
   * @return The StepDataInterface or null if non found.
   */
  public StepDataInterface getStepDataInterface( String stepname, int stepcopy ) {
    if ( steps == null ) {
      return null;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      if ( sid.stepname.equals( stepname ) && sid.copy == stepcopy ) {
        return sid.data;
      }
    }
    return null;
  }

  /**
   * Checks whether the transformation has any steps that are halted.
   *
   * @return true if one or more steps are halted, false otherwise
   */
  public boolean hasHaltedSteps() {
    // not yet 100% sure of this, if there are no steps... or none halted?
    if ( steps == null ) {
      return false;
    }

    for ( int i = 0; i < steps.size(); i++ ) {
      StepMetaDataCombi sid = steps.get( i );
      if ( sid.data.getStatus() == StepExecutionStatus.STATUS_HALTED ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the job start date.
   *
   * @return the job start date
   */
  public Date getJobStartDate() {
    return jobStartDate;
  }

  /**
   * Gets the job end date.
   *
   * @return the job end date
   */
  public Date getJobEndDate() {
    return jobEndDate;
  }

  /**
   * Sets the job end date.
   *
   * @param jobEndDate
   *          the jobEndDate to set
   */
  public void setJobEndDate( Date jobEndDate ) {
    this.jobEndDate = jobEndDate;
  }

  /**
   * Sets the job start date.
   *
   * @param jobStartDate
   *          the jobStartDate to set
   */
  public void setJobStartDate( Date jobStartDate ) {
    this.jobStartDate = jobStartDate;
  }

  /**
   * Get the batch ID that is passed from the parent job to the transformation. If nothing is passed, it's the
   * transformation's batch ID
   *
   * @return the parent job's batch ID, or the transformation's batch ID if there is no parent job
   */
  public long getPassedBatchId() {
    return passedBatchId;
  }

  /**
   * Sets the passed batch ID of the transformation from the batch ID of the parent job.
   *
   * @param jobBatchId
   *          the jobBatchId to set
   */
  public void setPassedBatchId( long jobBatchId ) {
    this.passedBatchId = jobBatchId;
  }

  /**
   * Gets the batch ID of the transformation.
   *
   * @return the batch ID of the transformation
   */
  public long getBatchId() {
    return batchId;
  }

  /**
   * Sets the batch ID of the transformation.
   *
   * @param batchId
   *          the batch ID to set
   */
  public void setBatchId( long batchId ) {
    this.batchId = batchId;
  }

  /**
   * Gets the name of the thread that contains the transformation.
   *
   * @deprecated use {@link #getTransactionId()}
   * @return the thread name
   */
  @Deprecated
  public String getThreadName() {
    return threadName;
  }

  /**
   * Sets the thread name for the transformation.
   *
   * @deprecated use {@link #setTransactionId(String)}
   * @param threadName
   *          the thread name
   */
  @Deprecated
  public void setThreadName( String threadName ) {
    this.threadName = threadName;
  }

  /**
   * Gets the status of the transformation (Halting, Finished, Paused, etc.)
   *
   * @return the status of the transformation
   */
  public String getStatus() {
    String message;

    if ( isRunning() ) {
      if ( isStopped() ) {
        message = STRING_HALTING;
      } else {
        if ( isPaused() ) {
          message = STRING_PAUSED;
        } else {
          message = STRING_RUNNING;
        }
      }
    } else if ( isFinished() ) {
      message = STRING_FINISHED;
      if ( getResult().getNrErrors() > 0 ) {
        message += " (with errors)";
      }
    } else if ( isStopped() ) {
      message = STRING_STOPPED;
    } else if ( isPreparing() ) {
      message = STRING_PREPARING;
    } else if ( isInitializing() ) {
      message = STRING_INITIALIZING;
    } else {
      message = STRING_WAITING;
    }

    return message;
  }

  /**
   * Checks whether the transformation is initializing.
   *
   * @return true if the transformation is initializing, false otherwise
   */
  public boolean isInitializing() {
    int exist = status.get() & INITIALIZING.mask;
    return exist != 0;
  }

  /**
   * Sets whether the transformation is initializing.
   *
   * @param initializing
   *          true if the transformation is initializing, false otherwise
   */
  public void setInitializing( boolean initializing ) {
    status.updateAndGet( v -> initializing ? v | INITIALIZING.mask : ( BIT_STATUS_SUM ^ INITIALIZING.mask ) & v );
  }

  /**
   * Checks whether the transformation is preparing for execution.
   *
   * @return true if the transformation is preparing for execution, false otherwise
   */
  public boolean isPreparing() {
    int exist = status.get() & PREPARING.mask;
    return exist != 0;
  }

  /**
   * Sets whether the transformation is preparing for execution.
   *
   * @param preparing
   *          true if the transformation is preparing for execution, false otherwise
   */
  public void setPreparing( boolean preparing ) {
    status.updateAndGet( v -> preparing ? v | PREPARING.mask : ( BIT_STATUS_SUM ^ PREPARING.mask ) & v );
  }

  /**
   * Checks whether the transformation is running.
   *
   * @return true if the transformation is running, false otherwise
   */
  public boolean isRunning() {
    int exist = status.get() & RUNNING.mask;
    return exist != 0;
  }

  /**
   * Sets whether the transformation is running.
   *
   * @param running
   *          true if the transformation is running, false otherwise
   */
  public void setRunning( boolean running ) {
    status.updateAndGet( v -> running ? v | RUNNING.mask : ( BIT_STATUS_SUM ^ RUNNING.mask ) & v );
  }

  /**
   * Execute the transformation in a clustered fashion. The transformation steps are split and collected in a
   * TransSplitter object
   *
   * @param transMeta
   *          the transformation's meta-data
   * @param executionConfiguration
   *          the execution configuration
   * @return the transformation splitter object
   * @throws KettleException
   *           the kettle exception
   */
  public static TransSplitter executeClustered( final TransMeta transMeta,
      final TransExecutionConfiguration executionConfiguration ) throws KettleException {
    if ( Utils.isEmpty( transMeta.getName() ) ) {
      throw new KettleException( "The transformation needs a name to uniquely identify it by on the remote server." );
    }

    TransSplitter transSplitter = new TransSplitter( transMeta );
    transSplitter.splitOriginalTransformation();

    // Pass the clustered run ID to allow for parallel execution of clustered transformations
    //
    executionConfiguration.getVariables().put( Const.INTERNAL_VARIABLE_CLUSTER_RUN_ID, transSplitter
        .getClusteredRunId() );

    executeClustered( transSplitter, executionConfiguration );
    return transSplitter;
  }

  /**
   * Executes an existing TransSplitter, with the transformation already split.
   *
   * @param transSplitter
   *          the trans splitter
   * @param executionConfiguration
   *          the execution configuration
   * @throws KettleException
   *           the kettle exception
   * @see org.pentaho.di.ui.spoon.delegates.SpoonTransformationDelegate
   */
  public static void executeClustered( final TransSplitter transSplitter,
      final TransExecutionConfiguration executionConfiguration ) throws KettleException {
    try {
      // Send the transformations to the servers...
      //
      // First the master and the slaves...
      //
      TransMeta master = transSplitter.getMaster();
      final SlaveServer[] slaves = transSplitter.getSlaveTargets();
      final Thread[] threads = new Thread[slaves.length];
      final Throwable[] errors = new Throwable[slaves.length];

      // Keep track of the various Carte object IDs
      //
      final Map<TransMeta, String> carteObjectMap = transSplitter.getCarteObjectMap();

      //
      // Send them all on their way...
      //
      SlaveServer masterServer = null;
      List<StepMeta> masterSteps = master.getTransHopSteps( false );
      if ( masterSteps.size() > 0 ) { // If there is something that needs to be done on the master...
        masterServer = transSplitter.getMasterServer();
        if ( executionConfiguration.isClusterPosting() ) {
          TransConfiguration transConfiguration = new TransConfiguration( master, executionConfiguration );
          Map<String, String> variables = transConfiguration.getTransExecutionConfiguration().getVariables();
          variables.put( Const.INTERNAL_VARIABLE_CLUSTER_SIZE, Integer.toString( slaves.length ) );
          variables.put( Const.INTERNAL_VARIABLE_CLUSTER_MASTER, "Y" );

          // Parameters override the variables but they need to pass over the configuration too...
          //
          Map<String, String> params = transConfiguration.getTransExecutionConfiguration().getParams();
          TransMeta ot = transSplitter.getOriginalTransformation();
          for ( String param : ot.listParameters() ) {
            String value =
                Const.NVL( ot.getParameterValue( param ), Const.NVL( ot.getParameterDefault( param ), ot.getVariable(
                    param ) ) );
            params.put( param, value );
          }

          String masterReply =
              masterServer.sendXML( transConfiguration.getXML(), RegisterTransServlet.CONTEXT_PATH + "/?xml=Y" );
          WebResult webResult = WebResult.fromXMLString( masterReply );
          if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
            throw new KettleException( "An error occurred sending the master transformation: " + webResult
                .getMessage() );
          }
          carteObjectMap.put( master, webResult.getId() );
        }
      }

      // Then the slaves...
      // These are started in a background thread.
      //
      for ( int i = 0; i < slaves.length; i++ ) {
        final int index = i;

        final TransMeta slaveTrans = transSplitter.getSlaveTransMap().get( slaves[i] );

        if ( executionConfiguration.isClusterPosting() ) {
          Runnable runnable = new Runnable() {
            @Override
            public void run() {
              try {
                // Create a copy for local use... We get race-conditions otherwise...
                //
                TransExecutionConfiguration slaveTransExecutionConfiguration =
                    (TransExecutionConfiguration) executionConfiguration.clone();
                TransConfiguration transConfiguration =
                    new TransConfiguration( slaveTrans, slaveTransExecutionConfiguration );

                Map<String, String> variables = slaveTransExecutionConfiguration.getVariables();
                variables.put( Const.INTERNAL_VARIABLE_SLAVE_SERVER_NUMBER, Integer.toString( index ) );
                variables.put( Const.INTERNAL_VARIABLE_SLAVE_SERVER_NAME, slaves[index].getName() );
                variables.put( Const.INTERNAL_VARIABLE_CLUSTER_SIZE, Integer.toString( slaves.length ) );
                variables.put( Const.INTERNAL_VARIABLE_CLUSTER_MASTER, "N" );

                // Parameters override the variables but they need to pass over the configuration too...
                //
                Map<String, String> params = slaveTransExecutionConfiguration.getParams();
                TransMeta ot = transSplitter.getOriginalTransformation();
                for ( String param : ot.listParameters() ) {
                  String value =
                      Const.NVL( ot.getParameterValue( param ), Const.NVL( ot.getParameterDefault( param ), ot
                          .getVariable( param ) ) );
                  params.put( param, value );
                }

                String slaveReply =
                    slaves[index].sendXML( transConfiguration.getXML(), RegisterTransServlet.CONTEXT_PATH + "/?xml=Y" );
                WebResult webResult = WebResult.fromXMLString( slaveReply );
                if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
                  throw new KettleException( "An error occurred sending a slave transformation: " + webResult
                      .getMessage() );
                }
                carteObjectMap.put( slaveTrans, webResult.getId() );
              } catch ( Throwable t ) {
                errors[index] = t;
              }
            }
          };
          threads[i] = new Thread( runnable );
        }
      }

      // Start the slaves
      for ( int i = 0; i < threads.length; i++ ) {
        if ( threads[i] != null ) {
          threads[i].start();
        }
      }

      // Wait until the slaves report back...
      // Sending the XML over is the heaviest part
      // Later we can do the others as well...
      //
      for ( int i = 0; i < threads.length; i++ ) {
        if ( threads[i] != null ) {
          threads[i].join();
          if ( errors[i] != null ) {
            throw new KettleException( errors[i] );
          }
        }
      }

      if ( executionConfiguration.isClusterPosting() ) {
        if ( executionConfiguration.isClusterPreparing() ) {
          // Prepare the master...
          if ( masterSteps.size() > 0 ) { // If there is something that needs to be done on the master...
            String carteObjectId = carteObjectMap.get( master );
            String masterReply =
                masterServer.execService( PrepareExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(
                    master.getName(), "UTF-8" ) + "&id=" + URLEncoder.encode( carteObjectId, "UTF-8" ) + "&xml=Y" );
            WebResult webResult = WebResult.fromXMLString( masterReply );
            if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
              throw new KettleException(
                  "An error occurred while preparing the execution of the master transformation: " + webResult
                      .getMessage() );
            }
          }

          // Prepare the slaves
          // WG: Should these be threaded like the above initialization?
          for ( int i = 0; i < slaves.length; i++ ) {
            TransMeta slaveTrans = transSplitter.getSlaveTransMap().get( slaves[i] );
            String carteObjectId = carteObjectMap.get( slaveTrans );
            String slaveReply =
                slaves[i].execService( PrepareExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(
                    slaveTrans.getName(), "UTF-8" ) + "&id=" + URLEncoder.encode( carteObjectId, "UTF-8" ) + "&xml=Y" );
            WebResult webResult = WebResult.fromXMLString( slaveReply );
            if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
              throw new KettleException( "An error occurred while preparing the execution of a slave transformation: "
                  + webResult.getMessage() );
            }
          }
        }

        if ( executionConfiguration.isClusterStarting() ) {
          // Start the master...
          if ( masterSteps.size() > 0 ) { // If there is something that needs to be done on the master...
            String carteObjectId = carteObjectMap.get( master );
            String masterReply =
                masterServer.execService( StartExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(
                    master.getName(), "UTF-8" ) + "&id=" + URLEncoder.encode( carteObjectId, "UTF-8" ) + "&xml=Y" );
            WebResult webResult = WebResult.fromXMLString( masterReply );
            if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
              throw new KettleException( "An error occurred while starting the execution of the master transformation: "
                  + webResult.getMessage() );
            }
          }

          // Start the slaves
          // WG: Should these be threaded like the above initialization?
          for ( int i = 0; i < slaves.length; i++ ) {
            TransMeta slaveTrans = transSplitter.getSlaveTransMap().get( slaves[i] );
            String carteObjectId = carteObjectMap.get( slaveTrans );
            String slaveReply =
                slaves[i].execService( StartExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode(
                    slaveTrans.getName(), "UTF-8" ) + "&id=" + URLEncoder.encode( carteObjectId, "UTF-8" ) + "&xml=Y" );
            WebResult webResult = WebResult.fromXMLString( slaveReply );
            if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
              throw new KettleException( "An error occurred while starting the execution of a slave transformation: "
                  + webResult.getMessage() );
            }
          }
        }
      }
    } catch ( KettleException ke ) {
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( "There was an error during transformation split", e );
    }
  }

  /**
   * Monitors a clustered transformation every second, after all the transformations in a cluster schema are running.
   * <br>
   * Now we should verify that they are all running as they should.<br>
   * If a transformation has an error, we should kill them all.<br>
   * This should happen in a separate thread to prevent blocking of the UI.<br>
   * <br>
   * When the master and slave transformations have all finished, we should also run<br>
   * a cleanup on those transformations to release sockets, etc.<br>
   * <br>
   *
   * @param log
   *          the log interface channel
   * @param transSplitter
   *          the transformation splitter object
   * @param parentJob
   *          the parent job when executed in a job, otherwise just set to null
   * @return the number of errors encountered
   */
  public static final long monitorClusteredTransformation( LogChannelInterface log, TransSplitter transSplitter,
      Job parentJob ) {
    return monitorClusteredTransformation( log, transSplitter, parentJob, 1 ); // monitor every 1 seconds
  }

  /**
   * Monitors a clustered transformation every second, after all the transformations in a cluster schema are running.
   * <br>
   * Now we should verify that they are all running as they should.<br>
   * If a transformation has an error, we should kill them all.<br>
   * This should happen in a separate thread to prevent blocking of the UI.<br>
   * <br>
   * When the master and slave transformations have all finished, we should also run<br>
   * a cleanup on those transformations to release sockets, etc.<br>
   * <br>
   *
   * @param log
   *          the subject to use for logging
   * @param transSplitter
   *          the transformation splitter object
   * @param parentJob
   *          the parent job when executed in a job, otherwise just set to null
   * @param sleepTimeSeconds
   *          the sleep time in seconds in between slave transformation status polling
   * @return the number of errors encountered
   */
  public static final long monitorClusteredTransformation( LogChannelInterface log, TransSplitter transSplitter,
      Job parentJob, int sleepTimeSeconds ) {
    long errors = 0L;

    //
    // See if the remote transformations have finished.
    // We could just look at the master, but I doubt that that is enough in all
    // situations.
    //
    SlaveServer[] slaveServers = transSplitter.getSlaveTargets(); // <-- ask
                                                                  // these guys
    TransMeta[] slaves = transSplitter.getSlaves();
    Map<TransMeta, String> carteObjectMap = transSplitter.getCarteObjectMap();

    SlaveServer masterServer;
    try {
      masterServer = transSplitter.getMasterServer();
    } catch ( KettleException e ) {
      log.logError( "Error getting the master server", e );
      masterServer = null;
      errors++;
    }
    TransMeta masterTransMeta = transSplitter.getMaster();

    boolean allFinished = false;
    while ( !allFinished && errors == 0 && ( parentJob == null || !parentJob.isStopped() ) ) {
      allFinished = true;
      errors = 0L;

      // Slaves first...
      //
      for ( int s = 0; s < slaveServers.length && allFinished && errors == 0; s++ ) {
        try {
          String carteObjectId = carteObjectMap.get( slaves[s] );
          SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus( slaves[s].getName(), carteObjectId, 0 );
          if ( transStatus.isRunning() ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "Slave transformation on '" + slaveServers[s] + "' is still running." );
            }
            allFinished = false;
          } else {
            if ( log.isDetailed() ) {
              log.logDetailed( "Slave transformation on '" + slaveServers[s] + "' has finished." );
            }
          }
          errors += transStatus.getNrStepErrors();
        } catch ( Exception e ) {
          errors += 1;
          log.logError( "Unable to contact slave server '" + slaveServers[s].getName()
              + "' to check slave transformation : " + e.toString() );
        }
      }

      // Check the master too
      if ( allFinished && errors == 0 && masterTransMeta != null && masterTransMeta.nrSteps() > 0 ) {
        try {
          String carteObjectId = carteObjectMap.get( masterTransMeta );
          SlaveServerTransStatus transStatus =
              masterServer.getTransStatus( masterTransMeta.getName(), carteObjectId, 0 );
          if ( transStatus.isRunning() ) {
            if ( log.isDetailed() ) {
              log.logDetailed( "Master transformation is still running." );
            }
            allFinished = false;
          } else {
            if ( log.isDetailed() ) {
              log.logDetailed( "Master transformation has finished." );
            }
          }
          Result result = transStatus.getResult( transSplitter.getOriginalTransformation() );
          errors += result.getNrErrors();
        } catch ( Exception e ) {
          errors += 1;
          log.logError( "Unable to contact master server '" + masterServer.getName()
              + "' to check master transformation : " + e.toString() );
        }
      }

      if ( ( parentJob != null && parentJob.isStopped() ) || errors != 0 ) {
        //
        // Stop all slaves and the master on the slave servers
        //
        for ( int s = 0; s < slaveServers.length && allFinished && errors == 0; s++ ) {
          try {
            String carteObjectId = carteObjectMap.get( slaves[s] );
            WebResult webResult = slaveServers[s].stopTransformation( slaves[s].getName(), carteObjectId );
            if ( !WebResult.STRING_OK.equals( webResult.getResult() ) ) {
              log.logError( "Unable to stop slave transformation '" + slaves[s].getName() + "' : " + webResult
                  .getMessage() );
            }
          } catch ( Exception e ) {
            errors += 1;
            log.logError( "Unable to contact slave server '" + slaveServers[s].getName() + "' to stop transformation : "
                + e.toString() );
          }
        }

        try {
          String carteObjectId = carteObjectMap.get( masterTransMeta );
          WebResult webResult = masterServer.stopTransformation( masterTransMeta.getName(), carteObjectId );
          if ( !WebResult.STRING_OK.equals( webResult.getResult() ) ) {
            log.logError( "Unable to stop master transformation '" + masterServer.getName() + "' : " + webResult
                .getMessage() );
          }
        } catch ( Exception e ) {
          errors += 1;
          log.logError( "Unable to contact master server '" + masterServer.getName() + "' to stop the master : " + e
              .toString() );
        }
      }

      //
      // Keep waiting until all transformations have finished
      // If needed, we stop them again and again until they yield.
      //
      if ( !allFinished ) {
        // Not finished or error: wait a bit longer
        if ( log.isDetailed() ) {
          log.logDetailed( "Clustered transformation is still running, waiting a few seconds..." );
        }
        try {
          Thread.sleep( sleepTimeSeconds * 2000 );
        } catch ( Exception e ) {
          // Ignore errors
        } // Check all slaves every x seconds.
      }
    }

    log.logBasic( "All transformations in the cluster have finished." );

    errors += cleanupCluster( log, transSplitter );

    return errors;
  }

  /**
   * Cleanup the cluster, including the master and all slaves, and return the number of errors that occurred.
   *
   * @param log
   *          the log channel interface
   * @param transSplitter
   *          the TransSplitter object
   * @return the number of errors that occurred in the clustered transformation
   */
  public static int cleanupCluster( LogChannelInterface log, TransSplitter transSplitter ) {

    SlaveServer[] slaveServers = transSplitter.getSlaveTargets();
    TransMeta[] slaves = transSplitter.getSlaves();
    SlaveServer masterServer;
    try {
      masterServer = transSplitter.getMasterServer();
    } catch ( KettleException e ) {
      log.logError( "Unable to obtain the master server from the cluster", e );
      return 1;
    }
    TransMeta masterTransMeta = transSplitter.getMaster();
    int errors = 0;

    // All transformations have finished, with or without error.
    // Now run a cleanup on all the transformation on the master and the slaves.
    //
    // Slaves first...
    //
    for ( int s = 0; s < slaveServers.length; s++ ) {
      try {
        cleanupSlaveServer( transSplitter, slaveServers[s], slaves[s] );
      } catch ( Exception e ) {
        errors++;
        log.logError( "Unable to contact slave server '" + slaveServers[s].getName()
            + "' to clean up slave transformation", e );
      }
    }

    // Clean up the master too
    //
    if ( masterTransMeta != null && masterTransMeta.nrSteps() > 0 ) {
      try {
        cleanupSlaveServer( transSplitter, masterServer, masterTransMeta );
      } catch ( Exception e ) {
        errors++;
        log.logError( "Unable to contact master server '" + masterServer.getName()
            + "' to clean up master transformation", e );
      }

      // Also de-allocate all ports used for this clustered transformation on the master.
      //
      try {
        // Deallocate all ports belonging to this clustered run, not anything else
        //
        masterServer.deAllocateServerSockets( transSplitter.getOriginalTransformation().getName(), transSplitter
            .getClusteredRunId() );
      } catch ( Exception e ) {
        errors++;
        log.logError( "Unable to contact master server '" + masterServer.getName()
            + "' to clean up port sockets for transformation'" + transSplitter.getOriginalTransformation().getName()
            + "'", e );
      }
    }

    return errors;
  }

  /**
   * Cleanup the slave server as part of a clustered transformation.
   *
   * @param transSplitter
   *          the TransSplitter object
   * @param slaveServer
   *          the slave server
   * @param slaveTransMeta
   *          the slave transformation meta-data
   * @throws KettleException
   *           if any errors occur during cleanup
   */
  public static void cleanupSlaveServer( TransSplitter transSplitter, SlaveServer slaveServer,
      TransMeta slaveTransMeta ) throws KettleException {
    String transName = slaveTransMeta.getName();
    try {
      String carteObjectId = transSplitter.getCarteObjectMap().get( slaveTransMeta );
      WebResult webResult = slaveServer.cleanupTransformation( transName, carteObjectId );
      if ( !WebResult.STRING_OK.equals( webResult.getResult() ) ) {
        throw new KettleException( "Unable to run clean-up on slave server '" + slaveServer + "' for transformation '"
            + transName + "' : " + webResult.getMessage() );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unexpected error contacting slave server '" + slaveServer
          + "' to clear up transformation '" + transName + "'", e );
    }
  }

  /**
   * Gets the clustered transformation result.
   *
   * @param log
   *          the log channel interface
   * @param transSplitter
   *          the TransSplitter object
   * @param parentJob
   *          the parent job
   * @return the clustered transformation result
   */
  public static final Result getClusteredTransformationResult( LogChannelInterface log, TransSplitter transSplitter,
      Job parentJob ) {
    return getClusteredTransformationResult( log, transSplitter, parentJob, false );
  }

  /**
   * Gets the clustered transformation result.
   *
   * @param log
   *          the log channel interface
   * @param transSplitter
   *          the TransSplitter object
   * @param parentJob
   *          the parent job
   * @param loggingRemoteWork
   *          log remote execution logs locally
   * @return the clustered transformation result
   */
  public static final Result getClusteredTransformationResult( LogChannelInterface log, TransSplitter transSplitter,
      Job parentJob, boolean loggingRemoteWork ) {
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
    } catch ( KettleException e ) {
      log.logError( "Error getting the master server", e );
      masterServer = null;
      result.setNrErrors( result.getNrErrors() + 1 );
    }
    TransMeta master = transSplitter.getMaster();

    // Slaves first...
    //
    for ( int s = 0; s < slaveServers.length; s++ ) {
      try {
        // Get the detailed status of the slave transformation...
        //
        SlaveServerTransStatus transStatus = slaveServers[s].getTransStatus( slaves[s].getName(), "", 0 );
        Result transResult = transStatus.getResult( slaves[s] );

        result.add( transResult );

        if ( loggingRemoteWork ) {
          log.logBasic( "-- Slave : " + slaveServers[s].getName() );
          log.logBasic( transStatus.getLoggingString() );
        }
      } catch ( Exception e ) {
        result.setNrErrors( result.getNrErrors() + 1 );
        log.logError( "Unable to contact slave server '" + slaveServers[s].getName()
            + "' to get result of slave transformation : " + e.toString() );
      }
    }

    // Clean up the master too
    //
    if ( master != null && master.nrSteps() > 0 ) {
      try {
        // Get the detailed status of the slave transformation...
        //
        SlaveServerTransStatus transStatus = masterServer.getTransStatus( master.getName(), "", 0 );
        Result transResult = transStatus.getResult( master );

        result.add( transResult );

        if ( loggingRemoteWork ) {
          log.logBasic( "-- Master : " + masterServer.getName() );
          log.logBasic( transStatus.getLoggingString() );
        }
      } catch ( Exception e ) {
        result.setNrErrors( result.getNrErrors() + 1 );
        log.logError( "Unable to contact master server '" + masterServer.getName()
            + "' to get result of master transformation : " + e.toString() );
      }
    }

    return result;
  }

  /**
   * Send the transformation for execution to a Carte slave server.
   *
   * @param transMeta
   *          the transformation meta-data
   * @param executionConfiguration
   *          the transformation execution configuration
   * @param repository
   *          the repository
   * @return The Carte object ID on the server.
   * @throws KettleException
   *           if any errors occur during the dispatch to the slave server
   */
  public static String sendToSlaveServer( TransMeta transMeta, TransExecutionConfiguration executionConfiguration,
      Repository repository, IMetaStore metaStore ) throws KettleException {
    String carteObjectId;
    SlaveServer slaveServer = executionConfiguration.getRemoteServer();

    if ( slaveServer == null ) {
      throw new KettleException( "No slave server specified" );
    }
    if ( Utils.isEmpty( transMeta.getName() ) ) {
      throw new KettleException( "The transformation needs a name to uniquely identify it by on the remote server." );
    }

    // Inject certain internal variables to make it more intuitive.
    //
    Map<String, String> vars = new HashMap<>();

    for ( String var : Const.INTERNAL_TRANS_VARIABLES ) {
      vars.put( var, transMeta.getVariable( var ) );
    }
    for ( String var : Const.INTERNAL_JOB_VARIABLES ) {
      vars.put( var, transMeta.getVariable( var ) );
    }

    executionConfiguration.getVariables().putAll( vars );
    slaveServer.injectVariables( executionConfiguration.getVariables() );

    slaveServer.getLogChannel().setLogLevel( executionConfiguration.getLogLevel() );

    try {
      if ( executionConfiguration.isPassingExport() ) {

        // First export the job...
        //
        FileObject tempFile = KettleVFS.createTempFile( "transExport", KettleVFS.Suffix.ZIP, transMeta );

        TopLevelResource topLevelResource =
            ResourceUtil.serializeResourceExportInterface( tempFile.getName().toString(), transMeta, transMeta,
                repository, metaStore, executionConfiguration.getXML(), CONFIGURATION_IN_EXPORT_FILENAME );

        // Send the zip file over to the slave server...
        //
        String result = slaveServer.sendExport(
            topLevelResource.getArchiveName(),
            RegisterPackageServlet.TYPE_TRANS,
            topLevelResource.getBaseResourceName() );
        WebResult webResult = WebResult.fromXMLString( result );
        if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
          throw new KettleException( "There was an error passing the exported transformation to the remote server: "
              + Const.CR + webResult.getMessage() );
        }
        carteObjectId = webResult.getId();
      } else {

        // Now send it off to the remote server...
        //
        String xml = new TransConfiguration( transMeta, executionConfiguration ).getXML();
        String reply = slaveServer.sendXML( xml, RegisterTransServlet.CONTEXT_PATH + "/?xml=Y" );
        WebResult webResult = WebResult.fromXMLString( reply );
        if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
          throw new KettleException( "There was an error posting the transformation on the remote server: " + Const.CR
              + webResult.getMessage() );
        }
        carteObjectId = webResult.getId();
      }

      // Prepare the transformation
      //
      String reply =
          slaveServer.execService( PrepareExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode( transMeta
              .getName(), "UTF-8" ) + "&xml=Y&id=" + carteObjectId );
      WebResult webResult = WebResult.fromXMLString( reply );
      if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
        throw new KettleException( "There was an error preparing the transformation for excution on the remote server: "
            + Const.CR + webResult.getMessage() );
      }

      // Start the transformation
      //
      reply =
          slaveServer.execService( StartExecutionTransServlet.CONTEXT_PATH + "/?name=" + URLEncoder.encode( transMeta
              .getName(), "UTF-8" ) + "&xml=Y&id=" + carteObjectId );
      webResult = WebResult.fromXMLString( reply );

      if ( !webResult.getResult().equalsIgnoreCase( WebResult.STRING_OK ) ) {
        throw new KettleException( "There was an error starting the transformation on the remote server: " + Const.CR
            + webResult.getMessage() );
      }

      return carteObjectId;
    } catch ( KettleException ke ) {
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Checks whether the transformation is ready to start (i.e. execution preparation was successful)
   *
   * @return true if the transformation was prepared for execution successfully, false otherwise
   * @see org.pentaho.di.trans.Trans#prepareExecution(String[])
   */
  public boolean isReadyToStart() {
    return readyToStart;
  }

  protected void setReadyToStart( boolean ready ) {
    readyToStart = ready;
  }


  /**
   * Sets the internal kettle variables.
   *
   * @param var
   *          the new internal kettle variables
   */
  public void setInternalKettleVariables( VariableSpace var ) {
    if ( transMeta != null && !Utils.isEmpty( transMeta.getFilename() ) ) { // we have a finename that's defined.
      try {
        FileObject fileObject = KettleVFS.getFileObject( transMeta.getFilename(), var );
        FileName fileName = fileObject.getName();

        // The filename of the transformation
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the transformation
        FileName fileDir = fileName.getParent();
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI() );
      } catch ( KettleFileException e ) {
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "" );
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "" );
      }
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "" );
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "" );
    }

    boolean hasRepoDir = transMeta.getRepositoryDirectory() != null && transMeta.getRepository() != null;

    // The name of the transformation
    variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, Const.NVL( transMeta.getName(), "" ) );

    // setup fallbacks
    if ( hasRepoDir ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, variables.getVariable(
          Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, variables.getVariable(
          Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY ) );
    }

    // TODO PUT THIS INSIDE OF THE "IF"
    // The name of the directory in the repository
    variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, transMeta
        .getRepositoryDirectory() != null ? transMeta.getRepositoryDirectory().getPath() : "" );

    // Here we don't clear the definition of the job specific parameters, as they may come in handy.
    // A transformation can be called from a job and may inherit the job internal variables
    // but the other around is not possible.

    if ( hasRepoDir ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, variables.getVariable(
          Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
      if ( "/".equals( variables.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) ) ) {
        variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, "" );
      }
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, variables.getVariable(
          Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY ) );
    }
  }

  /**
   * Copies variables from a given variable space to this transformation.
   *
   * @param space
   *          the variable space
   * @see org.pentaho.di.core.variables.VariableSpace#copyVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  /**
   * Substitutes any variable values into the given string, and returns the resolved string.
   *
   * @param aString
   *          the string to resolve against environment variables
   * @return the string after variables have been resolved/susbstituted
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String)
   */
  @Override
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  /**
   * Substitutes any variable values into each of the given strings, and returns an array containing the resolved
   * string(s).
   *
   * @param aString
   *          an array of strings to resolve against environment variables
   * @return the array of strings after variables have been resolved/susbstituted
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String[])
   */
  @Override
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData )
    throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  /**
   * Gets the parent variable space.
   *
   * @return the parent variable space
   * @see org.pentaho.di.core.variables.VariableSpace#getParentVariableSpace()
   */
  @Override
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  /**
   * Sets the parent variable space.
   *
   * @param parent
   *          the new parent variable space
   * @see org.pentaho.di.core.variables.VariableSpace#setParentVariableSpace(
   *      org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  /**
   * Gets the value of the specified variable, or returns a default value if no such variable exists.
   *
   * @param variableName
   *          the variable name
   * @param defaultValue
   *          the default value
   * @return the value of the specified variable, or returns a default value if no such variable exists
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String, java.lang.String)
   */
  @Override
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  /**
   * Gets the value of the specified variable, or returns a default value if no such variable exists.
   *
   * @param variableName
   *          the variable name
   * @return the value of the specified variable, or returns a default value if no such variable exists
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String)
   */
  @Override
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  /**
   * Returns a boolean representation of the specified variable after performing any necessary substitution. Truth
   * values include case-insensitive versions of "Y", "YES", "TRUE" or "1".
   *
   * @param variableName
   *          the variable name
   * @param defaultValue
   *          the default value
   * @return a boolean representation of the specified variable after performing any necessary substitution
   * @see org.pentaho.di.core.variables.VariableSpace#getBooleanValueOfVariable(java.lang.String, boolean)
   */
  @Override
  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Utils.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Utils.isEmpty( value ) ) {
        return ValueMetaString.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  /**
   * Sets the values of the transformation's variables to the values from the parent variables.
   *
   * @param parent
   *          the parent
   * @see org.pentaho.di.core.variables.VariableSpace#initializeVariablesFrom(
   *      org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  /**
   * Gets a list of variable names for the transformation.
   *
   * @return a list of variable names
   * @see org.pentaho.di.core.variables.VariableSpace#listVariables()
   */
  @Override
  public String[] listVariables() {
    return variables.listVariables();
  }

  /**
   * Sets the value of the specified variable to the specified value.
   *
   * @param variableName
   *          the variable name
   * @param variableValue
   *          the variable value
   * @see org.pentaho.di.core.variables.VariableSpace#setVariable(java.lang.String, java.lang.String)
   */
  @Override
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  /**
   * Shares a variable space from another variable space. This means that the object should take over the space used as
   * argument.
   *
   * @param space
   *          the variable space
   * @see org.pentaho.di.core.variables.VariableSpace#shareVariablesWith(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  /**
   * Injects variables using the given Map. The behavior should be that the properties object will be stored and at the
   * time the VariableSpace is initialized (or upon calling this method if the space is already initialized). After
   * injecting the link of the properties object should be removed.
   *
   * @param prop
   *          the property map
   * @see org.pentaho.di.core.variables.VariableSpace#injectVariables(java.util.Map)
   */
  @Override
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  /**
   * Pauses the transformation (pause all steps).
   */
  public void pauseRunning() {
    setPaused( true );
    for ( StepMetaDataCombi combi : steps ) {
      combi.step.pauseRunning();
    }
  }

  /**
   * Resumes running the transformation after a pause (resume all steps).
   */
  public void resumeRunning() {
    for ( StepMetaDataCombi combi : steps ) {
      combi.step.resumeRunning();
    }
    setPaused( false );
  }

  /**
   * Checks whether the transformation is being previewed.
   *
   * @return true if the transformation is being previewed, false otherwise
   */
  public boolean isPreview() {
    return preview;
  }

  /**
   * Sets whether the transformation is being previewed.
   *
   * @param preview
   *          true if the transformation is being previewed, false otherwise
   */
  public void setPreview( boolean preview ) {
    this.preview = preview;
  }

  /**
   * Gets the repository object for the transformation.
   *
   * @return the repository
   */
  public Repository getRepository() {

    if ( repository == null ) {
      // Does the transmeta have a repo?
      // This is a valid case, when a non-repo trans is attempting to retrieve
      // a transformation in the repository.
      if ( transMeta != null ) {
        return transMeta.getRepository();
      }
    }
    return repository;
  }

  /**
   * Sets the repository object for the transformation.
   *
   * @param repository
   *          the repository object to set
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
    if ( transMeta != null ) {
      transMeta.setRepository( repository );
    }
  }

  /**
   * Gets a named list (map) of step performance snapshots.
   *
   * @return a named list (map) of step performance snapshots
   */
  public Map<String, List<StepPerformanceSnapShot>> getStepPerformanceSnapShots() {
    return stepPerformanceSnapShots;
  }

  /**
   * Sets the named list (map) of step performance snapshots.
   *
   * @param stepPerformanceSnapShots
   *          a named list (map) of step performance snapshots to set
   */
  public void setStepPerformanceSnapShots( Map<String, List<StepPerformanceSnapShot>> stepPerformanceSnapShots ) {
    this.stepPerformanceSnapShots = stepPerformanceSnapShots;
  }

  /**
   * Gets a list of the transformation listeners. Please do not attempt to modify this list externally. Returned list is
   * mutable only for backward compatibility purposes.
   *
   * @return the transListeners
   */
  public List<TransListener> getTransListeners() {
    return transListeners;
  }

  /**
   * Sets the list of transformation listeners.
   *
   * @param transListeners
   *          the transListeners to set
   */
  public void setTransListeners( List<TransListener> transListeners ) {
    this.transListeners = Collections.synchronizedList( transListeners );
  }

  /**
   * Adds a transformation listener.
   *
   * @param transListener
   *          the trans listener
   */
  public void addTransListener( TransListener transListener ) {
    // PDI-5229 sync added
    synchronized ( transListeners ) {
      transListeners.add( transListener );
    }
  }

  /**
   * Sets the list of stop-event listeners for the transformation.
   *
   * @param transStoppedListeners
   *          the list of stop-event listeners to set
   */
  public void setTransStoppedListeners( List<TransStoppedListener> transStoppedListeners ) {
    this.transStoppedListeners = Collections.synchronizedList( transStoppedListeners );
  }

  /**
   * Gets the list of stop-event listeners for the transformation. This is not concurrent safe. Please note this is
   * mutable implementation only for backward compatibility reasons.
   *
   * @return the list of stop-event listeners
   */
  public List<TransStoppedListener> getTransStoppedListeners() {
    return transStoppedListeners;
  }

  /**
   * Adds a stop-event listener to the transformation.
   *
   * @param transStoppedListener
   *          the stop-event listener to add
   */
  public void addTransStoppedListener( TransStoppedListener transStoppedListener ) {
    transStoppedListeners.add( transStoppedListener );
  }

  /**
   * Checks if the transformation is paused.
   *
   * @return true if the transformation is paused, false otherwise
   */
  public boolean isPaused() {
    int exist = status.get() & PAUSED.mask;
    return exist != 0;
  }

  public void setPaused( boolean paused ) {
    status.updateAndGet( v -> paused ? v | PAUSED.mask : ( BIT_STATUS_SUM ^ PAUSED.mask ) & v );
  }

  /**
   * Checks if the transformation is stopped.
   *
   * @return true if the transformation is stopped, false otherwise
   */
  public boolean isStopped() {
    int exist = status.get() & STOPPED.mask;
    return exist != 0;
  }

  public void setStopped( boolean stopped ) {
    status.updateAndGet( v -> stopped ? v | STOPPED.mask : ( BIT_STATUS_SUM ^ STOPPED.mask ) & v );
  }

  /**
   * Monitors a remote transformation every 5 seconds.
   *
   * @param log
   *          the log channel interface
   * @param carteObjectId
   *          the Carte object ID
   * @param transName
   *          the transformation name
   * @param remoteSlaveServer
   *          the remote slave server
   */
  public static void monitorRemoteTransformation( LogChannelInterface log, String carteObjectId, String transName,
      SlaveServer remoteSlaveServer ) {
    monitorRemoteTransformation( log, carteObjectId, transName, remoteSlaveServer, 5 );
  }

  /**
   * Monitors a remote transformation at the specified interval.
   *
   * @param log
   *          the log channel interface
   * @param carteObjectId
   *          the Carte object ID
   * @param transName
   *          the transformation name
   * @param remoteSlaveServer
   *          the remote slave server
   * @param sleepTimeSeconds
   *          the sleep time (in seconds)
   */
  public static void monitorRemoteTransformation( LogChannelInterface log, String carteObjectId, String transName,
      SlaveServer remoteSlaveServer, int sleepTimeSeconds ) {
    long errors = 0;
    boolean allFinished = false;
    while ( !allFinished && errors == 0 ) {
      allFinished = true;
      errors = 0L;

      // Check the remote server
      if ( allFinished && errors == 0 ) {
        try {
          SlaveServerTransStatus transStatus = remoteSlaveServer.getTransStatus( transName, carteObjectId, 0 );
          if ( transStatus.isRunning() ) {
            if ( log.isDetailed() ) {
              log.logDetailed( transName, "Remote transformation is still running." );
            }
            allFinished = false;
          } else {
            if ( log.isDetailed() ) {
              log.logDetailed( transName, "Remote transformation has finished." );
            }
          }
          Result result = transStatus.getResult();
          errors += result.getNrErrors();
        } catch ( Exception e ) {
          errors += 1;
          log.logError( transName, "Unable to contact remote slave server '" + remoteSlaveServer.getName()
              + "' to check transformation status : " + e.toString() );
        }
      }

      //
      // Keep waiting until all transformations have finished
      // If needed, we stop them again and again until they yield.
      //
      if ( !allFinished ) {
        // Not finished or error: wait a bit longer
        if ( log.isDetailed() ) {
          log.logDetailed( transName, "The remote transformation is still running, waiting a few seconds..." );
        }
        try {
          Thread.sleep( sleepTimeSeconds * 1000 );
        } catch ( Exception e ) {
          // Ignore errors
        } // Check all slaves every x seconds.
      }
    }

    log.logMinimal( transName, "The remote transformation has finished." );

    // Clean up the remote transformation
    //
    try {
      WebResult webResult = remoteSlaveServer.cleanupTransformation( transName, carteObjectId );
      if ( !WebResult.STRING_OK.equals( webResult.getResult() ) ) {
        log.logError( transName, "Unable to run clean-up on remote transformation '" + transName + "' : " + webResult
            .getMessage() );
        errors += 1;
      }
    } catch ( Exception e ) {
      errors += 1;
      log.logError( transName, "Unable to contact slave server '" + remoteSlaveServer.getName()
          + "' to clean up transformation : " + e.toString() );
    }
  }

  /**
   * Adds a parameter definition to this transformation.
   *
   * @param key
   *          the name of the parameter
   * @param defValue
   *          the default value for the parameter
   * @param description
   *          the description of the parameter
   * @throws DuplicateParamException
   *           the duplicate param exception
   * @see org.pentaho.di.core.parameters.NamedParams#addParameterDefinition(java.lang.String, java.lang.String,
   *      java.lang.String)
   */
  @Override
  public void addParameterDefinition( String key, String defValue, String description ) throws DuplicateParamException {
    namedParams.addParameterDefinition( key, defValue, description );
  }

  /**
   * Gets the default value of the specified parameter.
   *
   * @param key
   *          the name of the parameter
   * @return the default value of the parameter
   * @throws UnknownParamException
   *           if the parameter does not exist
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDefault(java.lang.String)
   */
  @Override
  public String getParameterDefault( String key ) throws UnknownParamException {
    return namedParams.getParameterDefault( key );
  }

  /**
   * Gets the description of the specified parameter.
   *
   * @param key
   *          the name of the parameter
   * @return the parameter description
   * @throws UnknownParamException
   *           if the parameter does not exist
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDescription(java.lang.String)
   */
  @Override
  public String getParameterDescription( String key ) throws UnknownParamException {
    return namedParams.getParameterDescription( key );
  }

  /**
   * Gets the value of the specified parameter.
   *
   * @param key
   *          the name of the parameter
   * @return the parameter value
   * @throws UnknownParamException
   *           if the parameter does not exist
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterValue(java.lang.String)
   */
  @Override
  public String getParameterValue( String key ) throws UnknownParamException {
    return namedParams.getParameterValue( key );
  }

  /**
   * Gets a list of the parameters for the transformation.
   *
   * @return an array of strings containing the names of all parameters for the transformation
   * @see org.pentaho.di.core.parameters.NamedParams#listParameters()
   */
  @Override
  public String[] listParameters() {
    return namedParams.listParameters();
  }

  /**
   * Sets the value for the specified parameter.
   *
   * @param key
   *          the name of the parameter
   * @param value
   *          the name of the value
   * @throws UnknownParamException
   *           if the parameter does not exist
   * @see org.pentaho.di.core.parameters.NamedParams#setParameterValue(java.lang.String, java.lang.String)
   */
  @Override
  public void setParameterValue( String key, String value ) throws UnknownParamException {
    namedParams.setParameterValue( key, value );
  }

  /**
   * Remove all parameters.
   *
   * @see org.pentaho.di.core.parameters.NamedParams#eraseParameters()
   */
  @Override
  public void eraseParameters() {
    namedParams.eraseParameters();
  }

  /**
   * Clear the values of all parameters.
   *
   * @see org.pentaho.di.core.parameters.NamedParams#clearParameters()
   */
  @Override
  public void clearParameters() {
    namedParams.clearParameters();
  }

  /**
   * Activates all parameters by setting their values. If no values already exist, the method will attempt to set the
   * parameter to the default value. If no default value exists, the method will set the value of the parameter to the
   * empty string ("").
   *
   * @see org.pentaho.di.core.parameters.NamedParams#activateParameters()
   */
  @Override
  public void activateParameters() {
    String[] keys = listParameters();

    for ( String key : keys ) {
      String value;
      try {
        value = getParameterValue( key );
      } catch ( UnknownParamException e ) {
        value = "";
      }

      String defValue;
      try {
        defValue = getParameterDefault( key );
      } catch ( UnknownParamException e ) {
        defValue = "";
      }

      if ( Utils.isEmpty( value ) ) {
        setVariable( key, Const.NVL( defValue, "" ) );
      } else {
        setVariable( key, Const.NVL( value, "" ) );
      }
    }
  }

  /**
   * Copy parameters from a NamedParams object.
   *
   * @param params
   *          the NamedParams object from which to copy the parameters
   * @see org.pentaho.di.core.parameters.NamedParams#copyParametersFrom(org.pentaho.di.core.parameters.NamedParams)
   */
  @Override
  public void copyParametersFrom( NamedParams params ) {
    namedParams.copyParametersFrom( params );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#mergeParametersWith(org.pentaho.di.core.parameters.NamedParams, boolean replace)
   */
  @Override
  public void mergeParametersWith( NamedParams params, boolean replace ) {
    namedParams.mergeParametersWith( params, replace );
  }

  /**
   * Gets the parent transformation, which is null if no parent transformation exists.
   *
   * @return a reference to the parent transformation's Trans object, or null if no parent transformation exists
   */
  public Trans getParentTrans() {
    return parentTrans;
  }

  /**
   * Sets the parent transformation.
   *
   * @param parentTrans
   *          the parentTrans to set
   */
  public void setParentTrans( Trans parentTrans ) {
    this.logLevel = parentTrans.getLogLevel();
    this.log.setLogLevel( logLevel );
    this.parentTrans = parentTrans;

    transactionId = calculateTransactionId();
  }

  /**
   * Gets the mapping step name.
   *
   * @return the name of the mapping step that created this transformation
   */
  public String getMappingStepName() {
    return mappingStepName;
  }

  /**
   * Sets the mapping step name.
   *
   * @param mappingStepName
   *          the name of the mapping step that created this transformation
   */
  public void setMappingStepName( String mappingStepName ) {
    this.mappingStepName = mappingStepName;
  }

  /**
   * Sets the socket repository.
   *
   * @param socketRepository
   *          the new socket repository
   */
  public void setSocketRepository( SocketRepository socketRepository ) {
    this.socketRepository = socketRepository;
  }

  /**
   * Gets the socket repository.
   *
   * @return the socket repository
   */
  public SocketRepository getSocketRepository() {
    return socketRepository;
  }

  /**
   * Gets the object name.
   *
   * @return the object name
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectName()
   */
  @Override
  public String getObjectName() {
    return getName();
  }

  /**
   * Gets the object copy. For Trans, this always returns null
   *
   * @return null
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectCopy()
   */
  @Override
  public String getObjectCopy() {
    return null;
  }

  /**
   * Gets the filename of the transformation, or null if no filename exists
   *
   * @return the filename
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getFilename()
   */
  @Override
  public String getFilename() {
    if ( transMeta == null ) {
      return null;
    }
    return transMeta.getFilename();
  }

  /**
   * Gets the log channel ID.
   *
   * @return the log channel ID
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogChannelId()
   */
  @Override
  public String getLogChannelId() {
    return log.getLogChannelId();
  }

  /**
   * Gets the object ID.
   *
   * @return the object ID
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectId()
   */
  @Override
  public ObjectId getObjectId() {
    if ( transMeta == null ) {
      return null;
    }
    return transMeta.getObjectId();
  }

  /**
   * Gets the object revision.
   *
   * @return the object revision
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectRevision()
   */
  @Override
  public ObjectRevision getObjectRevision() {
    if ( transMeta == null ) {
      return null;
    }
    return transMeta.getObjectRevision();
  }

  /**
   * Gets the object type. For Trans, this always returns LoggingObjectType.TRANS
   *
   * @return the object type
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectType()
   */
  @Override
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.TRANS;
  }

  /**
   * Gets the parent logging object interface.
   *
   * @return the parent
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getParent()
   */
  @Override
  public LoggingObjectInterface getParent() {
    return parent;
  }

  /**
   * Gets the repository directory.
   *
   * @return the repository directory
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getRepositoryDirectory()
   */
  @Override
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    if ( transMeta == null ) {
      return null;
    }
    return transMeta.getRepositoryDirectory();
  }

  /**
   * Gets the log level.
   *
   * @return the log level
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogLevel()
   */
  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the log level.
   *
   * @param logLevel
   *          the new log level
   */
  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
    log.setLogLevel( logLevel );
  }

  /**
   * Gets the logging hierarchy.
   *
   * @return the logging hierarchy
   */
  public List<LoggingHierarchy> getLoggingHierarchy() {
    List<LoggingHierarchy> hierarchy = new ArrayList<>();
    List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren( getLogChannelId() );
    for ( String childId : childIds ) {
      LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject( childId );
      if ( loggingObject != null ) {
        hierarchy.add( new LoggingHierarchy( getLogChannelId(), batchId, loggingObject ) );
      }
    }

    return hierarchy;
  }

  /**
   *  Use:
   *  {@link #addActiveSubTransformation(String, Trans),
   *  {@link #getActiveSubTransformation(String)},
   *  {@link #removeActiveSubTransformation(String)}
   *
   *  instead
   */
  @Deprecated
  public Map<String, Trans> getActiveSubtransformations() {
    return activeSubtransformations;
  }

  public void addActiveSubTransformation( final String subTransName, Trans subTrans ) {
    activeSubtransformations.put( subTransName, subTrans );
  }

  public Trans removeActiveSubTransformation( final String subTransName ) {
    return activeSubtransformations.remove( subTransName );
  }

  public Trans getActiveSubTransformation( final String subTransName ) {
    return activeSubtransformations.get( subTransName );
  }

  /**
   * Gets the active sub-jobs.
   *
   * @return a map (by name) of the active sub-jobs
   */
  public Map<String, Job> getActiveSubjobs() {
    return activeSubjobs;
  }

  /**
   * Gets the container object ID.
   *
   * @return the Carte object ID
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * Sets the container object ID.
   *
   * @param containerObjectId
   *          the Carte object ID to set
   */
  public void setContainerObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Gets the registration date. For Trans, this always returns null
   *
   * @return null
   */
  @Override
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * Sets the servlet print writer.
   *
   * @param servletPrintWriter
   *          the new servlet print writer
   */
  public void setServletPrintWriter( PrintWriter servletPrintWriter ) {
    this.servletPrintWriter = servletPrintWriter;
  }

  /**
   * Gets the servlet print writer.
   *
   * @return the servlet print writer
   */
  public PrintWriter getServletPrintWriter() {
    return servletPrintWriter;
  }

  /**
   * Gets the name of the executing server.
   *
   * @return the executingServer
   */
  @Override
  public String getExecutingServer() {
    if ( executingServer == null ) {
      setExecutingServer( Const.getHostname() );
    }
    return executingServer;
  }

  /**
   * Sets the name of the executing server.
   *
   * @param executingServer
   *          the executingServer to set
   */
  @Override
  public void setExecutingServer( String executingServer ) {
    this.executingServer = executingServer;
  }

  /**
   * Gets the name of the executing user.
   *
   * @return the executingUser
   */
  @Override
  public String getExecutingUser() {
    return executingUser;
  }

  /**
   * Sets the name of the executing user.
   *
   * @param executingUser
   *          the executingUser to set
   */
  @Override
  public void setExecutingUser( String executingUser ) {
    this.executingUser = executingUser;
  }

  @Override
  public boolean isGatheringMetrics() {
    return log != null && log.isGatheringMetrics();
  }

  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    if ( log != null ) {
      log.setGatheringMetrics( gatheringMetrics );
    }
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return log != null && log.isForcingSeparateLogging();
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    if ( log != null ) {
      log.setForcingSeparateLogging( forcingSeparateLogging );
    }
  }

  public List<ResultFile> getResultFiles() {
    return resultFiles;
  }

  public void setResultFiles( List<ResultFile> resultFiles ) {
    this.resultFiles = resultFiles;
  }

  public List<RowMetaAndData> getResultRows() {
    return resultRows;
  }

  public void setResultRows( List<RowMetaAndData> resultRows ) {
    this.resultRows = resultRows;
  }

  public Result getPreviousResult() {
    return previousResult;
  }

  public void setPreviousResult( Result previousResult ) {
    this.previousResult = previousResult;
  }

  public Hashtable<String, Counter> getCounters() {
    return counters;
  }

  public void setCounters( Hashtable<String, Counter> counters ) {
    this.counters = counters;
  }

  public String[] getArguments() {
    return arguments;
  }

  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  /**
   * Clear the error in the transformation, clear all the rows from all the row sets, to make sure the transformation
   * can continue with other data. This is intended for use when running single threaded.
   */
  public void clearError() {
    setStopped( false );
    errors.set( 0 );
    setFinished( false );
    for ( StepMetaDataCombi combi : steps ) {
      StepInterface step = combi.step;
      for ( RowSet rowSet : step.getInputRowSets() ) {
        rowSet.clear();
      }
      step.setStopped( false );
    }
  }

  /**
   * Gets the transaction ID for the transformation.
   *
   * @return the transactionId
   */
  public String getTransactionId() {
    return transactionId;
  }

  /**
   * Sets the transaction ID for the transformation.
   *
   * @param transactionId
   *          the transactionId to set
   */
  public void setTransactionId( String transactionId ) {
    this.transactionId = transactionId;
  }

  /**
   * Calculates the transaction ID for the transformation.
   *
   * @return the calculated transaction ID for the transformation.
   */
  public String calculateTransactionId() {
    if ( getTransMeta() != null && getTransMeta().isUsingUniqueConnections() ) {
      if ( parentJob != null && parentJob.getTransactionId() != null ) {
        return parentJob.getTransactionId();
      } else if ( parentTrans != null && parentTrans.getTransMeta().isUsingUniqueConnections() ) {
        return parentTrans.getTransactionId();
      } else {
        return DatabaseConnectionMap.getInstance().getNextTransactionId();
      }
    } else {
      return Thread.currentThread().getName();
    }
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
    if ( transMeta != null ) {
      transMeta.setMetaStore( metaStore );
    }
  }

  /**
   * Sets encoding of HttpServletResponse according to System encoding.Check if system encoding is null or an empty and
   * set it to HttpServletResponse when not and writes error to log if null. Throw IllegalArgumentException if input
   * parameter is null.
   *
   * @param response
   *          the HttpServletResponse to set encoding, mayn't be null
   */
  public void setServletReponse( HttpServletResponse response ) {
    if ( response == null ) {
      throw new IllegalArgumentException( "Response is not valid: " + response );
    }
    String encoding = System.getProperty( "KETTLE_DEFAULT_SERVLET_ENCODING", null );
    // true if encoding is null or an empty (also for the next kin of strings: " ")
    if ( !StringUtils.isBlank( encoding ) ) {
      try {
        response.setCharacterEncoding( encoding.trim() );
        response.setContentType( "text/html; charset=" + encoding );
      } catch ( Exception ex ) {
        LogChannel.GENERAL.logError( "Unable to encode data with encoding : '" + encoding + "'", ex );
      }
    }
    this.servletResponse = response;
  }

  public HttpServletResponse getServletResponse() {
    return servletResponse;
  }

  public void setServletRequest( HttpServletRequest request ) {
    this.servletRequest = request;
  }

  public HttpServletRequest getServletRequest() {
    return servletRequest;
  }

  public List<DelegationListener> getDelegationListeners() {
    return delegationListeners;
  }

  public void setDelegationListeners( List<DelegationListener> delegationListeners ) {
    this.delegationListeners = delegationListeners;
  }

  public void addDelegationListener( DelegationListener delegationListener ) {
    delegationListeners.add( delegationListener );
  }

  public synchronized void doTopologySortOfSteps() {
    // The bubble sort algorithm in contrast to the QuickSort or MergeSort
    // algorithms
    // does indeed cover all possibilities.
    // Sorting larger transformations with hundreds of steps might be too slow
    // though.
    // We should consider caching TransMeta.findPrevious() results in that case.
    //
    transMeta.clearCaches();

    //
    // Cocktail sort (bi-directional bubble sort)
    //
    // Original sort was taking 3ms for 30 steps
    // cocktail sort takes about 8ms for the same 30, but it works :)
    //
    int stepsMinSize = 0;
    int stepsSize = steps.size();

    // Noticed a problem with an immediate shrinking iteration window
    // trapping rows that need to be sorted.
    // This threshold buys us some time to get the sorting close before
    // starting to decrease the window size.
    //
    // TODO: this could become much smarter by tracking row movement
    // and reacting to that each outer iteration verses
    // using a threshold.
    //
    // After this many iterations enable trimming inner iteration
    // window on no change being detected.
    //
    int windowShrinkThreshold = (int) Math.round( stepsSize * 0.75 );

    // give ourselves some room to sort big lists. the window threshold should
    // stop us before reaching this anyway.
    //
    int totalIterations = stepsSize * 2;

    boolean isBefore = false;
    boolean forwardChange = false;
    boolean backwardChange = false;

    boolean lastForwardChange = true;
    boolean keepSortingForward = true;

    StepMetaDataCombi one = null;
    StepMetaDataCombi two = null;

    for ( int x = 0; x < totalIterations; x++ ) {

      // Go forward through the list
      //
      if ( keepSortingForward ) {
        for ( int y = stepsMinSize; y < stepsSize - 1; y++ ) {
          one = steps.get( y );
          two = steps.get( y + 1 );

          if ( one.stepMeta.equals( two.stepMeta ) ) {
            isBefore = one.copy > two.copy;
          } else {
            isBefore = transMeta.findPrevious( one.stepMeta, two.stepMeta );
          }
          if ( isBefore ) {
            // two was found to be positioned BEFORE one so we need to
            // switch them...
            //
            steps.set( y, two );
            steps.set( y + 1, one );
            forwardChange = true;

          }
        }
      }

      // Go backward through the list
      //
      for ( int z = stepsSize - 1; z > stepsMinSize; z-- ) {
        one = steps.get( z );
        two = steps.get( z - 1 );

        if ( one.stepMeta.equals( two.stepMeta ) ) {
          isBefore = one.copy > two.copy;
        } else {
          isBefore = transMeta.findPrevious( one.stepMeta, two.stepMeta );
        }
        if ( !isBefore ) {
          // two was found NOT to be positioned BEFORE one so we need to
          // switch them...
          //
          steps.set( z, two );
          steps.set( z - 1, one );
          backwardChange = true;
        }
      }

      // Shrink stepsSize(max) if there was no forward change
      //
      if ( x > windowShrinkThreshold && !forwardChange ) {

        // should we keep going? check the window size
        //
        stepsSize--;
        if ( stepsSize <= stepsMinSize ) {
          break;
        }
      }

      // shrink stepsMinSize(min) if there was no backward change
      //
      if ( x > windowShrinkThreshold && !backwardChange ) {

        // should we keep going? check the window size
        //
        stepsMinSize++;
        if ( stepsMinSize >= stepsSize ) {
          break;
        }
      }

      // End of both forward and backward traversal.
      // Time to see if we should keep going.
      //
      if ( !forwardChange && !backwardChange ) {
        break;
      }

      //
      // if we are past the first iteration and there has been no change twice,
      // quit doing it!
      //
      if ( keepSortingForward && x > 0 && !lastForwardChange && !forwardChange ) {
        keepSortingForward = false;
      }
      lastForwardChange = forwardChange;
      forwardChange = false;
      backwardChange = false;

    } // finished sorting
  }

  @Override
  public Map<String, Object> getExtensionDataMap() {
    return extensionDataMap;
  }

  protected ExecutorService startHeartbeat( final long intervalInSeconds ) {

    ScheduledExecutorService heartbeat = Executors.newSingleThreadScheduledExecutor( new ThreadFactory() {

      @Override
      public Thread newThread( Runnable r ) {
        Thread thread = new Thread( r, "Transformation Heartbeat Thread for: " + getName() );
        thread.setDaemon( true );
        return thread;
      }
    } );

    heartbeat.scheduleAtFixedRate( new Runnable() {
      @Override
      public void run() {
        try {

          if ( Trans.this.isFinished() ) {
            log.logBasic( "Shutting down heartbeat signal for " + getName() );
            shutdownHeartbeat( Trans.this.heartbeat );
            return;
          }

          log.logDebug( "Triggering heartbeat signal for " + getName() + " at every " + intervalInSeconds
              + " seconds" );
          ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationHeartbeat.id, Trans.this );

        } catch ( KettleException e ) {
          log.logError( e.getMessage(), e );
        }
      }
    }, intervalInSeconds /* initial delay */, intervalInSeconds /* interval delay */, TimeUnit.SECONDS );

    return heartbeat;
  }

  protected void shutdownHeartbeat( ExecutorService heartbeat ) {

    if ( heartbeat != null ) {

      try {
        heartbeat.shutdownNow(); // prevents waiting tasks from starting and attempts to stop currently executing ones

      } catch ( Throwable t ) {
        /* do nothing */
      }
    }
  }

  private int getHeartbeatIntervalInSeconds() {

    TransMeta meta = this.getTransMeta();

    // 1 - check if there's a user defined value ( transformation-specific ) heartbeat periodic interval;
    // 2 - check if there's a default defined value ( transformation-specific ) heartbeat periodic interval;
    // 3 - use default Const.HEARTBEAT_PERIODIC_INTERVAL_IN_SECS if none of the above have been set

    try {

      if ( meta != null ) {

        return Const.toInt( meta.getParameterValue( Const.VARIABLE_HEARTBEAT_PERIODIC_INTERVAL_SECS ), Const.toInt( meta
            .getParameterDefault( Const.VARIABLE_HEARTBEAT_PERIODIC_INTERVAL_SECS ),
            Const.HEARTBEAT_PERIODIC_INTERVAL_IN_SECS ) );
      }

    } catch ( Exception e ) {
      /* do nothing, return Const.HEARTBEAT_PERIODIC_INTERVAL_IN_SECS */
    }

    return Const.HEARTBEAT_PERIODIC_INTERVAL_IN_SECS;
  }
}
