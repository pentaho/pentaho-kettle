//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.MetricsLogTable;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.reflection.StringSearcher;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLFormatter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.RemoteStep;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepIOMetaInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaChangeListenerInterface;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.jobexecutor.JobExecutorMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.pentaho.di.trans.steps.missing.MissingTrans;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.di.trans.steps.singlethreader.SingleThreaderMeta;
import org.pentaho.di.trans.steps.transexecutor.TransExecutorMeta;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class defines information about a transformation and offers methods to save and load it from XML or a PDI
 * database repository, as well as methods to alter a transformation by adding/removing databases, steps, hops, etc.
 *
 * @since 20-jun-2003
 * @author Matt Casters
 */
public class TransMeta extends AbstractMeta
    implements XMLInterface, Comparator<TransMeta>, Comparable<TransMeta>, Cloneable, ResourceExportInterface,
    RepositoryElementInterface, LoggingObjectInterface {

  /** The package name, used for internationalization of messages. */
  private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!

  /** A constant specifying the tag value for the XML node of the transformation. */
  public static final String XML_TAG = "transformation";

  /**
   * A constant used by the logging operations to indicate any logged messages are related to transformation meta-data.
   */
  public static final String STRING_TRANSMETA = "Transformation metadata";

  /** A constant specifying the repository element type as a Transformation. */
  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.TRANSFORMATION;

  public static final int BORDER_INDENT = 20;
  /** The list of steps associated with the transformation. */
  protected List<StepMeta> steps;

  /** The list of hops associated with the transformation. */
  protected List<TransHopMeta> hops;

  /** The list of dependencies associated with the transformation. */
  protected List<TransDependency> dependencies;

  /** The list of cluster schemas associated with the transformation. */
  protected List<ClusterSchema> clusterSchemas;

  /** The list of partition schemas associated with the transformation. */
  private List<PartitionSchema> partitionSchemas;

  /** The version string for the transformation. */
  protected String trans_version;

  /** The status of the transformation. */
  protected int trans_status;

  /** The transformation logging table associated with the transformation. */
  protected TransLogTable transLogTable;

  /** The performance logging table associated with the transformation. */
  protected PerformanceLogTable performanceLogTable;

  /** The step logging table associated with the transformation. */
  protected StepLogTable stepLogTable;

  /** The metricslogging table associated with the transformation. */
  protected MetricsLogTable metricsLogTable;

  /** The size of the current rowset. */
  protected int sizeRowset;

  /** The meta-data for the database connection associated with "max date" auditing information. */
  protected DatabaseMeta maxDateConnection;

  /** The table name associated with "max date" auditing information. */
  protected String maxDateTable;

  /** The field associated with "max date" auditing information. */
  protected String maxDateField;

  /** The amount by which to increase the "max date" value. */
  protected double maxDateOffset;

  /** The maximum date difference used for "max date" auditing and limiting job sizes. */
  protected double maxDateDifference;

  /**
   * The list of arguments to the transformation.
   *
   * @deprecated Moved to Trans
   * */
  @Deprecated
  protected String[] arguments;

  /**
   * A table of named counters.
   *
   * @deprecated Moved to Trans
   */
  @Deprecated
  protected Hashtable<String, Counter> counters;

  /** Indicators for changes in steps, databases, hops, and notes. */
  protected boolean changed_steps, changed_hops;

  /** The database cache. */
  protected DBCache dbCache;

  /** The time (in nanoseconds) to wait when the input buffer is empty. */
  protected int sleepTimeEmpty;

  /** The time (in nanoseconds) to wait when the input buffer is full. */
  protected int sleepTimeFull;

  /** The previous result. */
  protected Result previousResult;

  /**
   * The result rows.
   *
   * @deprecated
   * */
  @Deprecated
  protected List<RowMetaAndData> resultRows;

  /**
   * The result files.
   *
   * @deprecated
   * */
  @Deprecated
  protected List<ResultFile> resultFiles;

  /** Whether the transformation is using unique connections. */
  protected boolean usingUniqueConnections;

  /** Whether the feedback is shown. */
  protected boolean feedbackShown;

  /** The feedback size. */
  protected int feedbackSize;

  /**
   * Flag to indicate thread management usage. Set to default to false from version 2.5.0 on. Before that it was enabled
   * by default.
   */
  protected boolean usingThreadPriorityManagment;

  /** The slave-step-copy/partition distribution. Only used for slave transformations in a clustering environment. */
  protected SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution;

  /** Just a flag indicating that this is a slave transformation - internal use only, no GUI option. */
  protected boolean slaveTransformation;

  /** Whether the transformation is capturing step performance snap shots. */
  protected boolean capturingStepPerformanceSnapShots;

  /** The step performance capturing delay. */
  protected long stepPerformanceCapturingDelay;

  /** The step performance capturing size limit. */
  protected String stepPerformanceCapturingSizeLimit;

  /** The steps fields cache. */
  protected Map<String, RowMetaInterface> stepsFieldsCache;

  /** The loop cache. */
  protected Map<String, Boolean> loopCache;

  /** The previous step cache */
  protected Map<String, List<StepMeta>> previousStepCache;

  /** The log channel interface. */
  protected LogChannelInterface log;

  /** The list of StepChangeListeners */
  protected List<StepMetaChangeListenerInterface> stepChangeListeners;

  protected byte[] keyForSessionKey;
  boolean isKeyPrivate;
  private ArrayList<MissingTrans> missingTrans;

  /**
   * The TransformationType enum describes the various types of transformations in terms of execution, including Normal,
   * Serial Single-Threaded, and Single-Threaded.
   */
  public enum TransformationType {

    /** A normal transformation. */
    Normal( "Normal", BaseMessages.getString( PKG, "TransMeta.TransformationType.Normal" ) ),

      /** A serial single-threaded transformation. */
      SerialSingleThreaded( "SerialSingleThreaded", BaseMessages.getString(
        PKG, "TransMeta.TransformationType.SerialSingleThreaded" ) ),

      /** A single-threaded transformation. */
      SingleThreaded( "SingleThreaded", BaseMessages
        .getString( PKG, "TransMeta.TransformationType.SingleThreaded" ) );

    /** The code corresponding to the transformation type. */
    private final String code;

    /** The description of the transformation type. */
    private final String description;

    /**
     * Instantiates a new transformation type.
     *
     * @param code
     *          the code
     * @param description
     *          the description
     */
    TransformationType( String code, String description ) {
      this.code = code;
      this.description = description;
    }

    /**
     * Gets the code corresponding to the transformation type.
     *
     * @return the code
     */
    public String getCode() {
      return code;
    }

    /**
     * Gets the description of the transformation type.
     *
     * @return the description
     */
    public String getDescription() {
      return description;
    }

    /**
     * Gets the transformation type by code.
     *
     * @param transTypeCode
     *          the trans type code
     * @return the transformation type by code
     */
    public static TransformationType getTransformationTypeByCode( String transTypeCode ) {
      if ( transTypeCode != null ) {
        for ( TransformationType type : values() ) {
          if ( type.code.equalsIgnoreCase( transTypeCode ) ) {
            return type;
          }
        }
      }
      return Normal;
    }

    /**
     * Gets the transformation types descriptions.
     *
     * @return the transformation types descriptions
     */
    public static String[] getTransformationTypesDescriptions() {
      String[] desc = new String[values().length];
      for ( int i = 0; i < values().length; i++ ) {
        desc[i] = values()[i].getDescription();
      }
      return desc;
    }
  }

  /** The transformation type. */
  protected TransformationType transformationType;

  // //////////////////////////////////////////////////////////////////////////

  /** A list of localized strings corresponding to string descriptions of the undo/redo actions. */
  public static final String[] desc_type_undo = {
    "",
    BaseMessages.getString( PKG, "TransMeta.UndoTypeDesc.UndoChange" ),
    BaseMessages.getString( PKG, "TransMeta.UndoTypeDesc.UndoNew" ),
    BaseMessages.getString( PKG, "TransMeta.UndoTypeDesc.UndoDelete" ),
    BaseMessages.getString( PKG, "TransMeta.UndoTypeDesc.UndoPosition" ) };

  /** A constant specifying the tag value for the XML node of the transformation information. */
  protected static final String XML_TAG_INFO = "info";

  /** A constant specifying the tag value for the XML node of the order of steps. */
  public static final String XML_TAG_ORDER = "order";

  /** A constant specifying the tag value for the XML node of the notes. */
  public static final String XML_TAG_NOTEPADS = "notepads";

  /** A constant specifying the tag value for the XML node of the transformation parameters. */
  public static final String XML_TAG_PARAMETERS = "parameters";

  /** A constant specifying the tag value for the XML node of the transformation dependencies. */
  protected static final String XML_TAG_DEPENDENCIES = "dependencies";

  /** A constant specifying the tag value for the XML node of the transformation's partition schemas. */
  public static final String XML_TAG_PARTITIONSCHEMAS = "partitionschemas";

  /** A constant specifying the tag value for the XML node of the slave servers. */
  public static final String XML_TAG_SLAVESERVERS = "slaveservers";

  /** A constant specifying the tag value for the XML node of the cluster schemas. */
  public static final String XML_TAG_CLUSTERSCHEMAS = "clusterschemas";

  /** A constant specifying the tag value for the XML node of the steps' error-handling information. */
  public static final String XML_TAG_STEP_ERROR_HANDLING = "step_error_handling";

  /**
   * Builds a new empty transformation. The transformation will have default logging capability and no variables, and
   * all internal meta-data is cleared to defaults.
   */
  public TransMeta() {
    clear();
    initializeVariablesFrom( null );
  }

  /**
   * Builds a new empty transformation with a set of variables to inherit from.
   *
   * @param parent
   *          the variable space to inherit from
   */
  public TransMeta( VariableSpace parent ) {
    clear();
    initializeVariablesFrom( parent );
  }

  public TransMeta( String filename, String name ) {
    clear();
    setFilename( filename );
    this.name = name;
    initializeVariablesFrom( null );
  }

  /**
   * Constructs a new transformation specifying the filename, name and arguments.
   *
   * @param filename
   *          The filename of the transformation
   * @param name
   *          The name of the transformation
   * @param arguments
   *          The arguments as Strings
   * @deprecated passing in arguments (a runtime argument) into the metadata is deprecated, pass it to Trans
   */
  @Deprecated
  public TransMeta( String filename, String name, String[] arguments ) {
    clear();
    setFilename( filename );
    this.name = name;
    this.arguments = arguments;
    initializeVariablesFrom( null );
  }

  /**
   * Compares two transformation on name, filename, repository directory, etc.
   * The comparison algorithm is as follows:<br/>
   * <ol>
   * <li>The first transformation's filename is checked first; if it has none, the transformation comes from a
   * repository. If the second transformation does not come from a repository, -1 is returned.</li>
   * <li>If the transformations are both from a repository, the transformations' names are compared. If the first
   * transformation has no name and the second one does, a -1 is returned.
   * If the opposite is true, a 1 is returned.</li>
   * <li>If they both have names they are compared as strings. If the result is non-zero it is returned. Otherwise the
   * repository directories are compared using the same technique of checking empty values and then performing a string
   * comparison, returning any non-zero result.</li>
   * <li>If the names and directories are equal, the object revision strings are compared using the same technique of
   * checking empty values and then performing a string comparison, this time ultimately returning the result of the
   * string compare.</li>
   * <li>If the first transformation does not come from a repository and the second one does, a 1 is returned. Otherwise
   * the transformation names and filenames are subsequently compared using the same technique of checking empty values
   * and then performing a string comparison, ultimately returning the result of the filename string comparison.
   * </ol>
   *
   * @param t1
   *          the first transformation to compare
   * @param t2
   *          the second transformation to compare
   * @return 0 if the two transformations are equal, 1 or -1 depending on the values (see description above)
   *
   */
  @Override
  public int compare( TransMeta t1, TransMeta t2 ) {
    return super.compare( t1, t2 );
  }

  /**
   * Compares this transformation's meta-data to the specified transformation's meta-data. This method simply calls
   * compare(this, o)
   *
   * @param o
   *          the o
   * @return the int
   * @see #compare(TransMeta, TransMeta)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo( TransMeta o ) {
    return compare( this, o );
  }

  /**
   * Checks whether this transformation's meta-data object is equal to the specified object. If the specified object is
   * not an instance of TransMeta, false is returned. Otherwise the method returns whether a call to compare() indicates
   * equality (i.e. compare(this, (TransMeta)obj)==0).
   *
   * @param obj
   *          the obj
   * @return true, if successful
   * @see #compare(TransMeta, TransMeta)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( Object obj ) {
    if ( !( obj instanceof TransMeta ) ) {
      return false;
    }

    return compare( this, (TransMeta) obj ) == 0;
  }

  /**
   * Clones the transformation meta-data object.
   *
   * @return a clone of the transformation meta-data object
   * @see java.lang.Object#clone()
   */
  @Override
  public Object clone() {
    return realClone( true );
  }

  /**
   * Perform a real clone of the transformation meta-data object, including cloning all lists and copying all values. If
   * the doClear parameter is true, the clone will be cleared of ALL values before the copy. If false, only the copied
   * fields will be cleared.
   *
   * @param doClear
   *          Whether to clear all of the clone's data before copying from the source object
   * @return a real clone of the calling object
   */
  public Object realClone( boolean doClear ) {

    try {
      TransMeta transMeta = (TransMeta) super.clone();
      if ( doClear ) {
        transMeta.clear();
      } else {
        // Clear out the things we're replacing below
        transMeta.databases = new ArrayList<>();
        transMeta.steps = new ArrayList<>();
        transMeta.hops = new ArrayList<>();
        transMeta.notes = new ArrayList<>();
        transMeta.dependencies = new ArrayList<>();
        transMeta.partitionSchemas = new ArrayList<>();
        transMeta.slaveServers = new ArrayList<>();
        transMeta.clusterSchemas = new ArrayList<>();
        transMeta.namedParams = new NamedParamsDefault();
        transMeta.stepChangeListeners = new ArrayList<>();
      }
      for ( DatabaseMeta db : databases ) {
        transMeta.addDatabase( (DatabaseMeta) db.clone() );
      }
      for ( StepMeta step : steps ) {
        transMeta.addStep( (StepMeta) step.clone() );
      }
      // PDI-15799: Step references are original yet. Set them to the clones.
      for ( StepMeta step : transMeta.getSteps() ) {
        final StepMetaInterface stepMetaInterface = step.getStepMetaInterface();
        if ( stepMetaInterface != null ) {
          final StepIOMetaInterface stepIOMeta = stepMetaInterface.getStepIOMeta();
          if ( stepIOMeta != null ) {
            for ( StreamInterface stream : stepIOMeta.getInfoStreams() ) {
              String streamStepName = stream.getStepname();
              if ( streamStepName != null ) {
                StepMeta streamStepMeta = transMeta.findStep( streamStepName );
                stream.setStepMeta( streamStepMeta );
              }
            }
          }
        }
      }
      for ( TransHopMeta hop : hops ) {
        transMeta.addTransHop( (TransHopMeta) hop.clone() );
      }
      for ( NotePadMeta note : notes ) {
        transMeta.addNote( (NotePadMeta) note.clone() );
      }
      for ( TransDependency dep : dependencies ) {
        transMeta.addDependency( (TransDependency) dep.clone() );
      }
      for ( SlaveServer slave : slaveServers ) {
        transMeta.getSlaveServers().add( (SlaveServer) slave.clone() );
      }
      for ( ClusterSchema schema : clusterSchemas ) {
        transMeta.getClusterSchemas().add( schema.clone() );
      }
      for ( PartitionSchema schema : partitionSchemas ) {
        transMeta.getPartitionSchemas().add( (PartitionSchema) schema.clone() );
      }
      for ( String key : listParameters() ) {
        transMeta.addParameterDefinition( key, getParameterDefault( key ), getParameterDescription( key ) );
      }

      return transMeta;
    } catch ( Exception e ) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Clears the transformation's meta-data, including the lists of databases, steps, hops, notes, dependencies,
   * partition schemas, slave servers, and cluster schemas. Logging information and timeouts are reset to defaults, and
   * recent connection info is cleared.
   */
  @Override
  public void clear() {
    setObjectId( null );
    steps = new ArrayList<>();
    hops = new ArrayList<>();
    dependencies = new ArrayList<>();
    partitionSchemas = new ArrayList<>();
    clusterSchemas = new ArrayList<>();
    namedParams = new NamedParamsDefault();
    stepChangeListeners = new ArrayList<>();

    slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution();

    trans_status = -1;
    trans_version = null;

    transLogTable = TransLogTable.getDefault( this, this, steps );
    performanceLogTable = PerformanceLogTable.getDefault( this, this );
    stepLogTable = StepLogTable.getDefault( this, this );
    metricsLogTable = MetricsLogTable.getDefault( this, this );

    sizeRowset = Const.ROWS_IN_ROWSET;
    sleepTimeEmpty = Const.TIMEOUT_GET_MILLIS;
    sleepTimeFull = Const.TIMEOUT_PUT_MILLIS;

    maxDateConnection = null;
    maxDateTable = null;
    maxDateField = null;
    maxDateOffset = 0.0;

    maxDateDifference = 0.0;

    undo = new ArrayList<>();
    max_undo = Const.MAX_UNDO;
    undo_position = -1;

    counters = new Hashtable<>();
    resultRows = null;

    super.clear();

    // LOAD THE DATABASE CACHE!
    dbCache = DBCache.getInstance();

    resultRows = new ArrayList<>();
    resultFiles = new ArrayList<>();

    feedbackShown = true;
    feedbackSize = Const.ROWS_UPDATE;

    // Thread priority:
    // - set to false in version 2.5.0
    // - re-enabling in version 3.0.1 to prevent excessive locking (PDI-491)
    //
    usingThreadPriorityManagment = true;

    // The performance monitoring options
    //
    capturingStepPerformanceSnapShots = false;
    stepPerformanceCapturingDelay = 1000; // every 1 seconds
    stepPerformanceCapturingSizeLimit = "100"; // maximum 100 data points

    stepsFieldsCache = new HashMap<>();
    loopCache = new HashMap<>();
    previousStepCache = new HashMap<>();
    transformationType = TransformationType.Normal;

    log = LogChannel.GENERAL;
  }

  /**
   * Add a new step to the transformation. Also marks that the transformation's steps have changed.
   *
   * @param stepMeta
   *          The meta-data for the step to be added.
   */
  public void addStep( StepMeta stepMeta ) {
    steps.add( stepMeta );
    stepMeta.setParentTransMeta( this );
    StepMetaInterface iface = stepMeta.getStepMetaInterface();
    if ( iface instanceof StepMetaChangeListenerInterface ) {
      addStepChangeListener( (StepMetaChangeListenerInterface) iface );
    }
    changed_steps = true;
    clearCaches();
  }

  /**
   * Add a new step to the transformation if that step didn't exist yet. Otherwise, replace the step. This method also
   * marks that the transformation's steps have changed.
   *
   * @param stepMeta
   *          The meta-data for the step to be added.
   */
  public void addOrReplaceStep( StepMeta stepMeta ) {
    int index = steps.indexOf( stepMeta );
    if ( index < 0 ) {
      index = steps.add( stepMeta ) ? 0 : index;
    } else {
      StepMeta previous = getStep( index );
      previous.replaceMeta( stepMeta );
    }
    stepMeta.setParentTransMeta( this );
    StepMetaInterface iface = stepMeta.getStepMetaInterface();
    if ( index != -1 && iface instanceof StepMetaChangeListenerInterface ) {
      addStepChangeListener( index, (StepMetaChangeListenerInterface) iface );
    }
    changed_steps = true;
    clearCaches();
  }

  /**
   * Add a new hop to the transformation. The hop information (source and target steps, e.g.) should be configured in
   * the TransHopMeta object before calling addTransHop(). Also marks that the transformation's hops have changed.
   *
   * @param hi
   *          The hop meta-data to be added.
   */
  public void addTransHop( TransHopMeta hi ) {
    hops.add( hi );
    changed_hops = true;
    clearCaches();
  }

  /**
   * Add a new dependency to the transformation.
   *
   * @param td
   *          The transformation dependency to be added.
   */
  public void addDependency( TransDependency td ) {
    dependencies.add( td );
  }

  /**
   * Add a new step to the transformation at the specified index. This method sets the step's parent transformation to
   * the this transformation, and marks that the transformations' steps have changed.
   *
   * @param p
   *          The index into the step list
   * @param stepMeta
   *          The step to be added.
   */
  public void addStep( int p, StepMeta stepMeta ) {
    steps.add( p, stepMeta );
    stepMeta.setParentTransMeta( this );
    changed_steps = true;
    StepMetaInterface iface = stepMeta.getStepMetaInterface();
    if ( iface instanceof StepMetaChangeListenerInterface ) {
      addStepChangeListener( p, (StepMetaChangeListenerInterface) stepMeta.getStepMetaInterface() );
    }
    clearCaches();
  }

  /**
   * Add a new hop to the transformation on a certain location (i.e. the specified index). Also marks that the
   * transformation's hops have changed.
   *
   * @param p
   *          the index into the hop list
   * @param hi
   *          The hop to be added.
   */
  public void addTransHop( int p, TransHopMeta hi ) {
    try {
      hops.add( p, hi );
    } catch ( IndexOutOfBoundsException e ) {
      hops.add( hi );
    }
    changed_hops = true;
    clearCaches();
  }

  /**
   * Add a new dependency to the transformation on a certain location (i.e. the specified index).
   *
   * @param p
   *          The index into the dependencies list.
   * @param td
   *          The transformation dependency to be added.
   */
  public void addDependency( int p, TransDependency td ) {
    dependencies.add( p, td );
  }

  /**
   * Get a list of defined steps in this transformation.
   *
   * @return an ArrayList of defined steps.
   */
  public List<StepMeta> getSteps() {
    return steps;
  }

  /**
   * Retrieves a step on a certain location (i.e. the specified index).
   *
   * @param i
   *          The index into the steps list.
   * @return The desired step's meta-data.
   */
  public StepMeta getStep( int i ) {
    return steps.get( i );
  }

  /**
   * Get a list of defined hops in this transformation.
   *
   * @return a list of defined hops.
   */
  public List<TransHopMeta> getTransHops() {
    return Collections.unmodifiableList( hops );
  }

  /**
   * Retrieves a hop on a certain location (i.e. the specified index).
   *
   * @param i
   *          The index into the hops list.
   * @return The desired hop's meta-data.
   */
  public TransHopMeta getTransHop( int i ) {
    return hops.get( i );
  }

  /**
   * Retrieves a dependency on a certain location (i.e. the specified index).
   *
   * @param i
   *          The index into the dependencies list.
   * @return The dependency object.
   */
  public TransDependency getDependency( int i ) {
    return dependencies.get( i );
  }

  /**
   * Removes a step from the transformation on a certain location (i.e. the specified index). Also marks that the
   * transformation's steps have changed.
   *
   * @param i
   *          The index
   */
  public void removeStep( int i ) {
    if ( i < 0 || i >= steps.size() ) {
      return;
    }

    StepMeta removeStep = steps.get( i );
    StepMetaInterface iface = removeStep.getStepMetaInterface();
    if ( iface instanceof StepMetaChangeListenerInterface ) {
      removeStepChangeListener( (StepMetaChangeListenerInterface) iface );
    }

    steps.remove( i );

    if ( removeStep.getStepMetaInterface() instanceof MissingTrans ) {
      removeMissingTrans( (MissingTrans) removeStep.getStepMetaInterface() );
    }

    changed_steps = true;
    clearCaches();
  }

  /**
   * Removes a hop from the transformation on a certain location (i.e. the specified index). Also marks that the
   * transformation's hops have changed.
   *
   * @param i
   *          The index into the hops list
   */
  public void removeTransHop( int i ) {
    if ( i < 0 || i >= hops.size() ) {
      return;
    }

    hops.remove( i );
    changed_hops = true;
    clearCaches();
  }

  /**
   * Removes a hop from the transformation. Also marks that the
   * transformation's hops have changed.
   *
   * @param hop
   *          The hop to remove from the list of hops
   */
  public void removeTransHop( TransHopMeta hop ) {
    hops.remove( hop );
    changed_hops = true;
    clearCaches();
  }

  /**
   * Removes a dependency from the transformation on a certain location (i.e. the specified index).
   *
   * @param i
   *          The location
   */
  public void removeDependency( int i ) {
    if ( i < 0 || i >= dependencies.size() ) {
      return;
    }
    dependencies.remove( i );
  }

  /**
   * Clears all the dependencies from the transformation.
   */
  public void removeAllDependencies() {
    dependencies.clear();
  }

  /**
   * Gets the number of steps in the transformation.
   *
   * @return The number of steps in the transformation.
   */
  public int nrSteps() {
    return steps.size();
  }

  /**
   * Gets the number of hops in the transformation.
   *
   * @return The number of hops in the transformation.
   */
  public int nrTransHops() {
    return hops.size();
  }

  /**
   * Gets the number of dependencies in the transformation.
   *
   * @return The number of dependencies in the transformation.
   */
  public int nrDependencies() {
    return dependencies.size();
  }

  /**
   * Gets the number of stepChangeListeners in the transformation.
   *
   * @return The number of stepChangeListeners in the transformation.
   */
  public int nrStepChangeListeners() {
    return stepChangeListeners.size();
  }

  /**
   * Changes the content of a step on a certain position. This is accomplished by setting the step's metadata at the
   * specified index to the specified meta-data object. The new step's parent transformation is updated to be this
   * transformation.
   *
   * @param i
   *          The index into the steps list
   * @param stepMeta
   *          The step meta-data to set
   */
  public void setStep( int i, StepMeta stepMeta ) {
    StepMetaInterface iface = stepMeta.getStepMetaInterface();
    if ( iface instanceof StepMetaChangeListenerInterface ) {
      addStepChangeListener( i, (StepMetaChangeListenerInterface) stepMeta.getStepMetaInterface() );
    }
    steps.set( i, stepMeta );
    stepMeta.setParentTransMeta( this );
    clearCaches();
  }

  /**
   * Changes the content of a hop on a certain position. This is accomplished by setting the hop's metadata at the
   * specified index to the specified meta-data object.
   *
   * @param i
   *          The index into the hops list
   * @param hi
   *          The hop meta-data to set
   */
  public void setTransHop( int i, TransHopMeta hi ) {
    hops.set( i, hi );
    clearCaches();
  }

  /**
   * Gets the list of used steps, which are the steps that are connected by hops.
   *
   * @return a list with all the used steps
   */
  public List<StepMeta> getUsedSteps() {
    List<StepMeta> list = new ArrayList<>();

    for ( StepMeta stepMeta : steps ) {
      if ( isStepUsedInTransHops( stepMeta ) ) {
        list.add( stepMeta );
      }
    }
    if ( list.isEmpty() && getSteps().size() == 1 ) {
      list = getSteps();
    }

    return list;
  }

  /**
   * Searches the list of steps for a step with a certain name.
   *
   * @param name
   *          The name of the step to look for
   * @return The step information or null if no nothing was found.
   */
  public StepMeta findStep( String name ) {
    return findStep( name, null );
  }

  /**
   * Searches the list of steps for a step with a certain name while excluding one step.
   *
   * @param name
   *          The name of the step to look for
   * @param exclude
   *          The step information to exclude.
   * @return The step information or null if nothing was found.
   */
  public StepMeta findStep( String name, StepMeta exclude ) {
    if ( name == null ) {
      return null;
    }

    int excl = -1;
    if ( exclude != null ) {
      excl = indexOfStep( exclude );
    }

    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      if ( i != excl && stepMeta.getName().equalsIgnoreCase( name ) ) {
        return stepMeta;
      }
    }
    return null;
  }

  /**
   * Searches the list of hops for a hop with a certain name.
   *
   * @param name
   *          The name of the hop to look for
   * @return The hop information or null if nothing was found.
   */
  public TransHopMeta findTransHop( String name ) {
    int i;

    for ( i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.toString().equalsIgnoreCase( name ) ) {
        return hi;
      }
    }
    return null;
  }

  /**
   * Search all hops for a hop where a certain step is at the start.
   *
   * @param fromstep
   *          The step at the start of the hop.
   * @return The hop or null if no hop was found.
   */
  public TransHopMeta findTransHopFrom( StepMeta fromstep ) {
    int i;
    for ( i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.getFromStep() != null && hi.getFromStep().equals( fromstep ) ) { // return the first
        return hi;
      }
    }
    return null;
  }

  public List<TransHopMeta> findAllTransHopFrom( StepMeta fromstep ) {
    return hops.stream()
      .filter( hop ->  hop.getFromStep() != null && hop.getFromStep().equals( fromstep ) )
      .collect( Collectors.toList() );
  }
  /**
   * Find a certain hop in the transformation.
   *
   * @param hi
   *          The hop information to look for.
   * @return The hop or null if no hop was found.
   */
  public TransHopMeta findTransHop( TransHopMeta hi ) {
    return findTransHop( hi.getFromStep(), hi.getToStep() );
  }

  /**
   * Search all hops for a hop where a certain step is at the start and another is at the end.
   *
   * @param from
   *          The step at the start of the hop.
   * @param to
   *          The step at the end of the hop.
   * @return The hop or null if no hop was found.
   */
  public TransHopMeta findTransHop( StepMeta from, StepMeta to ) {
    return findTransHop( from, to, false );
  }

  /**
   * Search all hops for a hop where a certain step is at the start and another is at the end.
   *
   * @param from
   *          The step at the start of the hop.
   * @param to
   *          The step at the end of the hop.
   * @param disabledToo
   *          the disabled too
   * @return The hop or null if no hop was found.
   */
  public TransHopMeta findTransHop( StepMeta from, StepMeta to, boolean disabledToo ) {
    for ( int i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.isEnabled() || disabledToo ) {
        if ( hi.getFromStep() != null && hi.getToStep() != null && hi.getFromStep().equals( from ) && hi.getToStep()
            .equals( to ) ) {
          return hi;
        }
      }
    }
    return null;
  }

  /**
   * Search all hops for a hop where a certain step is at the end.
   *
   * @param tostep
   *          The step at the end of the hop.
   * @return The hop or null if no hop was found.
   */
  public TransHopMeta findTransHopTo( StepMeta tostep ) {
    int i;
    for ( i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.getToStep() != null && hi.getToStep().equals( tostep ) ) { // Return the first!
        return hi;
      }
    }
    return null;
  }

  /**
   * Determines whether or not a certain step is informative. This means that the previous step is sending information
   * to this step, but only informative. This means that this step is using the information to process the actual stream
   * of data. We use this in StreamLookup, TableInput and other types of steps.
   *
   * @param this_step
   *          The step that is receiving information.
   * @param prev_step
   *          The step that is sending information
   * @return true if prev_step if informative for this_step.
   */
  public boolean isStepInformative( StepMeta this_step, StepMeta prev_step ) {
    String[] infoSteps = this_step.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
    if ( infoSteps == null ) {
      return false;
    }
    for ( int i = 0; i < infoSteps.length; i++ ) {
      if ( prev_step.getName().equalsIgnoreCase( infoSteps[i] ) ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Counts the number of previous steps for a step name.
   *
   * @param stepname
   *          The name of the step to start from
   * @return The number of preceding steps.
   * @deprecated
   */
  @Deprecated
  public int findNrPrevSteps( String stepname ) {
    return findNrPrevSteps( findStep( stepname ), false );
  }

  /**
   * Counts the number of previous steps for a step name taking into account whether or not they are informational.
   *
   * @param stepname
   *          The name of the step to start from
   * @param info
   *          true if only the informational steps are desired, false otherwise
   * @return The number of preceding steps.
   * @deprecated
   */
  @Deprecated
  public int findNrPrevSteps( String stepname, boolean info ) {
    return findNrPrevSteps( findStep( stepname ), info );
  }

  /**
   * Find the number of steps that precede the indicated step.
   *
   * @param stepMeta
   *          The source step
   *
   * @return The number of preceding steps found.
   */
  public int findNrPrevSteps( StepMeta stepMeta ) {
    return findNrPrevSteps( stepMeta, false );
  }

  /**
   * Find the previous step on a certain location (i.e. the specified index).
   *
   * @param stepname
   *          The source step name
   * @param nr
   *          the index into the step list
   *
   * @return The preceding step found.
   * @deprecated
   */
  @Deprecated
  public StepMeta findPrevStep( String stepname, int nr ) {
    return findPrevStep( findStep( stepname ), nr );
  }

  /**
   * Find the previous step on a certain location taking into account the steps being informational or not.
   *
   * @param stepname
   *          The name of the step
   * @param nr
   *          The index into the step list
   * @param info
   *          true if only the informational steps are desired, false otherwise
   * @return The step information
   * @deprecated
   */
  @Deprecated
  public StepMeta findPrevStep( String stepname, int nr, boolean info ) {
    return findPrevStep( findStep( stepname ), nr, info );
  }

  /**
   * Find the previous step on a certain location (i.e. the specified index).
   *
   * @param stepMeta
   *          The source step information
   * @param nr
   *          the index into the hops list
   *
   * @return The preceding step found.
   */
  public StepMeta findPrevStep( StepMeta stepMeta, int nr ) {
    return findPrevStep( stepMeta, nr, false );
  }

  /**
   * Count the number of previous steps on a certain location taking into account the steps being informational or not.
   *
   * @param stepMeta
   *          The name of the step
   * @param info
   *          true if only the informational steps are desired, false otherwise
   * @return The number of preceding steps
   * @deprecated please use method findPreviousSteps
   */
  @Deprecated
  public int findNrPrevSteps( StepMeta stepMeta, boolean info ) {
    int count = 0;
    int i;

    for ( i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;
      TransHopMeta hi = getTransHop( i );
      if ( hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals( stepMeta ) ) {
        // Check if this previous step isn't informative (StreamValueLookup)
        // We don't want fields from this stream to show up!
        if ( info || !isStepInformative( stepMeta, hi.getFromStep() ) ) {
          count++;
        }
      }
    }
    return count;
  }

  /**
   * Find the previous step on a certain location taking into account the steps being informational or not.
   *
   * @param stepMeta
   *          The step
   * @param nr
   *          The index into the hops list
   * @param info
   *          true if we only want the informational steps.
   * @return The preceding step information
   * @deprecated please use method findPreviousSteps
   */
  @Deprecated
  public StepMeta findPrevStep( StepMeta stepMeta, int nr, boolean info ) {
    int count = 0;
    int i;

    for ( i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals( stepMeta ) ) {
        if ( info || !isStepInformative( stepMeta, hi.getFromStep() ) ) {
          if ( count == nr ) {
            return hi.getFromStep();
          }
          count++;
        }
      }
    }
    return null;
  }

  /**
   * Get the list of previous steps for a certain reference step. This includes the info steps.
   *
   * @param stepMeta
   *          The reference step
   * @return The list of the preceding steps, including the info steps.
   */
  public List<StepMeta> findPreviousSteps( StepMeta stepMeta ) {
    return findPreviousSteps( stepMeta, true );
  }

  /**
   * Get the previous steps on a certain location taking into account the steps being informational or not.
   *
   * @param stepMeta
   *          The name of the step
   * @param info
   *          true if we only want the informational steps.
   * @return The list of the preceding steps
   */
  public List<StepMeta> findPreviousSteps( StepMeta stepMeta, boolean info ) {
    if ( stepMeta == null ) {
      return new ArrayList<>();
    }

    String cacheKey = getStepMetaCacheKey( stepMeta, info );
    List<StepMeta> previousSteps = previousStepCache.get( cacheKey );
    if ( previousSteps == null ) {
      previousSteps = new ArrayList<>();
      for ( TransHopMeta hi : hops ) {
        if ( hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals( stepMeta ) ) {
          // Check if this previous step isn't informative (StreamValueLookup)
          // We don't want fields from this stream to show up!
          if ( info || !isStepInformative( stepMeta, hi.getFromStep() ) ) {
            previousSteps.add( hi.getFromStep() );
          }
        }
      }
      previousStepCache.put( cacheKey, previousSteps );
    }
    return previousSteps;
  }

  /**
   * Get the informational steps for a certain step. An informational step is a step that provides information for
   * lookups, etc.
   *
   * @param stepMeta
   *          The name of the step
   * @return An array of the informational steps found
   */
  public StepMeta[] getInfoStep( StepMeta stepMeta ) {
    String[] infoStepName = stepMeta.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
    if ( infoStepName == null ) {
      return null;
    }

    StepMeta[] infoStep = new StepMeta[infoStepName.length];
    for ( int i = 0; i < infoStep.length; i++ ) {
      infoStep[i] = findStep( infoStepName[i] );
    }

    return infoStep;
  }

  /**
   * Find the the number of informational steps for a certain step.
   *
   * @param stepMeta
   *          The step
   * @return The number of informational steps found.
   */
  public int findNrInfoSteps( StepMeta stepMeta ) {
    if ( stepMeta == null ) {
      return 0;
    }

    int count = 0;

    for ( int i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi == null || hi.getToStep() == null ) {
        log.logError( BaseMessages.getString( PKG, "TransMeta.Log.DestinationOfHopCannotBeNull" ) );
      }
      if ( hi != null && hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals( stepMeta ) ) {
        // Check if this previous step isn't informative (StreamValueLookup)
        // We don't want fields from this stream to show up!
        if ( isStepInformative( stepMeta, hi.getFromStep() ) ) {
          count++;
        }
      }
    }
    return count;
  }

  /**
   * Find the informational fields coming from an informational step into the step specified.
   *
   * @param stepname
   *          The name of the step
   * @return A row containing fields with origin.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getPrevInfoFields( String stepname ) throws KettleStepException {
    return getPrevInfoFields( findStep( stepname ) );
  }

  /**
   * Find the informational fields coming from an informational step into the step specified.
   *
   * @param stepMeta
   *          The receiving step
   * @return A row containing fields with origin.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getPrevInfoFields( StepMeta stepMeta ) throws KettleStepException {
    for ( int i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;
      TransHopMeta hi = getTransHop( i );

      if ( hi.isEnabled() && hi.getToStep().equals( stepMeta ) ) {
        StepMeta infoStep = hi.getFromStep();
        if ( isStepInformative( stepMeta, infoStep ) ) {
          RowMetaInterface row = getPrevStepFields( infoStep );
          return getThisStepFields( infoStep, stepMeta, row );
        }
      }
    }
    return new RowMeta();
  }

  /**
   * Find the number of succeeding steps for a certain originating step.
   *
   * @param stepMeta
   *          The originating step
   * @return The number of succeeding steps.
   * @deprecated use {@link #getNextSteps(StepMeta)}
   */
  @Deprecated
  public int findNrNextSteps( StepMeta stepMeta ) {
    int count = 0;
    int i;
    for ( i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi.isEnabled() && hi.getFromStep().equals( stepMeta ) ) {
        count++;
      }
    }
    return count;
  }

  /**
   * Find the succeeding step at a location for an originating step.
   *
   * @param stepMeta
   *          The originating step
   * @param nr
   *          The location
   * @return The step found.
   * @deprecated use {@link #getNextSteps(StepMeta)}
   */
  @Deprecated
  public StepMeta findNextStep( StepMeta stepMeta, int nr ) {
    int count = 0;
    int i;

    for ( i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi.isEnabled() && hi.getFromStep().equals( stepMeta ) ) {
        if ( count == nr ) {
          return hi.getToStep();
        }
        count++;
      }
    }
    return null;
  }

  /**
   * Retrieve an array of preceding steps for a certain destination step. This includes the info steps.
   *
   * @param stepMeta
   *          The destination step
   * @return An array containing the preceding steps.
   */
  public StepMeta[] getPrevSteps( StepMeta stepMeta ) {
    List<StepMeta> prevSteps = previousStepCache.get( getStepMetaCacheKey( stepMeta, true ) );
    if ( prevSteps == null ) {
      prevSteps = new ArrayList<>();
      for ( int i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;
        TransHopMeta hopMeta = getTransHop( i );
        if ( hopMeta.isEnabled() && hopMeta.getToStep().equals( stepMeta ) ) {
          prevSteps.add( hopMeta.getFromStep() );
        }
      }
    }

    return prevSteps.toArray( new StepMeta[prevSteps.size()] );
  }

  /**
   * Retrieve an array of succeeding step names for a certain originating step name.
   *
   * @param stepname
   *          The originating step name
   * @return An array of succeeding step names
   */
  public String[] getPrevStepNames( String stepname ) {
    return getPrevStepNames( findStep( stepname ) );
  }

  /**
   * Retrieve an array of preceding steps for a certain destination step.
   *
   * @param stepMeta
   *          The destination step
   * @return an array of preceding step names.
   */
  public String[] getPrevStepNames( StepMeta stepMeta ) {
    StepMeta[] prevStepMetas = getPrevSteps( stepMeta );
    String[] retval = new String[prevStepMetas.length];
    for ( int x = 0; x < prevStepMetas.length; x++ ) {
      retval[x] = prevStepMetas[x].getName();
    }

    return retval;
  }

  /**
   * Retrieve an array of succeeding steps for a certain originating step.
   *
   * @param stepMeta
   *          The originating step
   * @return an array of succeeding steps.
   * @deprecated use findNextSteps instead
   */
  @Deprecated
  public StepMeta[] getNextSteps( StepMeta stepMeta ) {
    List<StepMeta> nextSteps = new ArrayList<>();
    for ( int i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi.isEnabled() && hi.getFromStep().equals( stepMeta ) ) {
        nextSteps.add( hi.getToStep() );
      }
    }

    return nextSteps.toArray( new StepMeta[nextSteps.size()] );
  }

  /**
   * Retrieve a list of succeeding steps for a certain originating step.
   *
   * @param stepMeta
   *          The originating step
   * @return an array of succeeding steps.
   */
  public List<StepMeta> findNextSteps( StepMeta stepMeta ) {
    List<StepMeta> nextSteps = new ArrayList<>();
    for ( int i = 0; i < nrTransHops(); i++ ) { // Look at all the hops;

      TransHopMeta hi = getTransHop( i );
      if ( hi.isEnabled() && hi.getFromStep().equals( stepMeta ) ) {
        nextSteps.add( hi.getToStep() );
      }
    }

    return nextSteps;
  }

  /**
   * Retrieve an array of succeeding step names for a certain originating step.
   *
   * @param stepMeta
   *          The originating step
   * @return an array of succeeding step names.
   */
  public String[] getNextStepNames( StepMeta stepMeta ) {
    StepMeta[] nextStepMeta = getNextSteps( stepMeta );
    String[] retval = new String[nextStepMeta.length];
    for ( int x = 0; x < nextStepMeta.length; x++ ) {
      retval[x] = nextStepMeta[x].getName();
    }

    return retval;
  }

  /**
   * Find the step that is located on a certain point on the canvas, taking into account the icon size.
   *
   * @param x
   *          the x-coordinate of the point queried
   * @param y
   *          the y-coordinate of the point queried
   * @param iconsize
   *          the iconsize
   * @return The step information if a step is located at the point. Otherwise, if no step was found: null.
   */
  public StepMeta getStep( int x, int y, int iconsize ) {
    int i, s;
    s = steps.size();
    for ( i = s - 1; i >= 0; i-- ) { // Back to front because drawing goes from start to end
      StepMeta stepMeta = steps.get( i );
      if ( partOfTransHop( stepMeta ) || stepMeta.isDrawn() ) { // Only consider steps from active or inactive hops!
        Point p = stepMeta.getLocation();
        if ( p != null ) {
          if ( x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize + 20 ) {
            return stepMeta;
          }
        }
      }
    }
    return null;
  }

  /**
   * Determines whether or not a certain step is part of a hop.
   *
   * @param stepMeta
   *          The step queried
   * @return true if the step is part of a hop.
   */
  public boolean partOfTransHop( StepMeta stepMeta ) {
    int i;
    for ( i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.getFromStep() == null || hi.getToStep() == null ) {
        return false;
      }
      if ( hi.getFromStep().equals( stepMeta ) || hi.getToStep().equals( stepMeta ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the fields that are emitted by a certain step name.
   *
   * @param stepname
   *          The stepname of the step to be queried.
   * @return A row containing the fields emitted.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getStepFields( String stepname ) throws KettleStepException {
    StepMeta stepMeta = findStep( stepname );
    if ( stepMeta != null ) {
      return getStepFields( stepMeta );
    } else {
      return null;
    }
  }

  /**
   * Returns the fields that are emitted by a certain step.
   *
   * @param stepMeta
   *          The step to be queried.
   * @return A row containing the fields emitted.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getStepFields( StepMeta stepMeta ) throws KettleStepException {
    return getStepFields( stepMeta, null );
  }

  /**
   * Gets the fields for each of the specified steps and merges them into a single set
   *
   * @param stepMeta
   *          the step meta
   * @return an interface to the step fields
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getStepFields( StepMeta[] stepMeta ) throws KettleStepException {
    RowMetaInterface fields = new RowMeta();

    for ( int i = 0; i < stepMeta.length; i++ ) {
      RowMetaInterface flds = getStepFields( stepMeta[i] );
      if ( flds != null && !flds.isEmpty() ) {
        fields.mergeRowMeta( flds, stepMeta[i].getName() );
      }
    }
    return fields;
  }

  /**
   * Returns the fields that are emitted by a certain step.
   *
   * @param stepMeta
   *          The step to be queried.
   * @param monitor
   *          The progress monitor for progress dialog. (null if not used!)
   * @return A row containing the fields emitted.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getStepFields( StepMeta stepMeta, ProgressMonitorListener monitor ) throws KettleStepException {
    setRepositoryOnMappingSteps();
    return getStepFields( stepMeta, null, monitor );
  }

  /**
   * Returns the fields that are emitted by a certain step.
   *
   * @param stepMeta
   *          The step to be queried.
   * @param targetStep
   *          the target step
   * @param monitor
   *          The progress monitor for progress dialog. (null if not used!)
   * @return A row containing the fields emitted.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getStepFields( StepMeta stepMeta, StepMeta targetStep, ProgressMonitorListener monitor ) throws KettleStepException {
    RowMetaInterface row = new RowMeta();

    if ( stepMeta == null ) {
      return row;
    }

    String fromToCacheEntry = stepMeta.getName() + ( targetStep != null ? ( "-" + targetStep.getName() ) : "" );
    RowMetaInterface rowMeta = stepsFieldsCache.get( fromToCacheEntry );
    if ( rowMeta != null ) {
      return rowMeta;
    }

    // See if the step is sending ERROR rows to the specified target step.
    //
    if ( targetStep != null && stepMeta.isSendingErrorRowsToStep( targetStep ) ) {
      // The error rows are the same as the input rows for
      // the step but with the selected error fields added
      //
      row = getPrevStepFields( stepMeta );

      // Add to this the error fields...
      StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
      row.addRowMeta( stepErrorMeta.getErrorFields() );

      // Store this row in the cache
      //
      stepsFieldsCache.put( fromToCacheEntry, row );

      return row;
    }

    // Resume the regular program...

    List<StepMeta> prevSteps = findPreviousSteps( stepMeta, false );

    int nrPrevious = prevSteps.size();

    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.FromStepALookingAtPreviousStep", stepMeta.getName(),
          String.valueOf( nrPrevious ) ) );
    }
    for ( int i = 0; i < prevSteps.size(); i++ ) {
      StepMeta prevStepMeta = prevSteps.get( i );

      if ( monitor != null ) {
        monitor.subTask(
            BaseMessages.getString( PKG, "TransMeta.Monitor.CheckingStepTask.Title", prevStepMeta.getName() ) );
      }

      RowMetaInterface add = getStepFields( prevStepMeta, stepMeta, monitor );
      if ( add == null ) {
        add = new RowMeta();
      }
      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.FoundFieldsToAdd" ) + add.toString() );
      }
      if ( i == 0 ) {
        row.addRowMeta( add );
      } else {
        // See if the add fields are not already in the row
        for ( int x = 0; x < add.size(); x++ ) {
          ValueMetaInterface v = add.getValueMeta( x );
          ValueMetaInterface s = row.searchValueMeta( v.getName() );
          if ( s == null ) {
            row.addValueMeta( v );
          }
        }
      }
    }

    if ( nrPrevious == 0 && stepMeta.getRemoteInputSteps().size() > 0 ) {
      // Also check the remote input steps (clustering)
      // Typically, if there are any, row is still empty at this point
      // We'll also be at a starting point in the transformation
      //
      for ( RemoteStep remoteStep : stepMeta.getRemoteInputSteps() ) {
        RowMetaInterface inputFields = remoteStep.getRowMeta();
        for ( ValueMetaInterface inputField : inputFields.getValueMetaList() ) {
          if ( row.searchValueMeta( inputField.getName() ) == null ) {
            row.addValueMeta( inputField );
          }
        }
      }
    }

    // Finally, see if we need to add/modify/delete fields with this step "name"
    rowMeta = getThisStepFields( stepMeta, targetStep, row, monitor );

    // Store this row in the cache
    //
    stepsFieldsCache.put( fromToCacheEntry, rowMeta );

    return rowMeta;
  }

  /**
   * Find the fields that are entering a step with a certain name.
   *
   * @param stepname
   *          The name of the step queried
   * @return A row containing the fields (w/ origin) entering the step
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getPrevStepFields( String stepname ) throws KettleStepException {
    return getPrevStepFields( findStep( stepname ) );
  }

  /**
   * Find the fields that are entering a certain step.
   *
   * @param stepMeta
   *          The step queried
   * @return A row containing the fields (w/ origin) entering the step
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getPrevStepFields( StepMeta stepMeta ) throws KettleStepException {
    return getPrevStepFields( stepMeta, null );
  }

  /**
   * Find the fields that are entering a certain step.
   *
   * @param stepMeta
   *          The step queried
   * @param monitor
   *          The progress monitor for progress dialog. (null if not used!)
   * @return A row containing the fields (w/ origin) entering the step
   * @throws KettleStepException
   *           the kettle step exception
   */


  public RowMetaInterface getPrevStepFields( StepMeta stepMeta, ProgressMonitorListener monitor ) throws KettleStepException {
    return getPrevStepFields( stepMeta, null, monitor );
  }

  public RowMetaInterface getPrevStepFields(
    StepMeta stepMeta, final String stepName, ProgressMonitorListener  monitor )
    throws KettleStepException {
    clearStepFieldsCachce();
    RowMetaInterface row = new RowMeta();

    if ( stepMeta == null ) {
      return null;
    }
    List<StepMeta> prevSteps = findPreviousSteps( stepMeta );
    int nrPrevSteps = prevSteps.size();
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.FromStepALookingAtPreviousStep", stepMeta.getName(),
          String.valueOf( nrPrevSteps ) ) );
    }
    StepMeta prevStepMeta = null;
    for ( int i = 0; i < nrPrevSteps; i++ ) {
      prevStepMeta = prevSteps.get( i );
      if  ( stepName != null && !stepName.equalsIgnoreCase( prevStepMeta.getName() ) ) {
        continue;
      }

      if ( monitor != null ) {
        monitor.subTask(
            BaseMessages.getString( PKG, "TransMeta.Monitor.CheckingStepTask.Title", prevStepMeta.getName() ) );
      }

      RowMetaInterface add = getStepFields( prevStepMeta, stepMeta, monitor );

      if ( log.isDebug() ) {
        log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.FoundFieldsToAdd2" ) + add.toString() );
      }
      if ( i == 0 ) {
        // we expect all input streams to be of the same layout!

        row.addRowMeta( add ); // recursive!
      } else {
        // See if the add fields are not already in the row
        for ( int x = 0; x < add.size(); x++ ) {
          ValueMetaInterface v = add.getValueMeta( x );
          ValueMetaInterface s = row.searchValueMeta( v.getName() );
          if ( s == null ) {
            row.addValueMeta( v );
          }
        }
      }
    }
    return row;
  }

  /**
   * Return the fields that are emitted by a step with a certain name.
   *
   * @param stepname
   *          The name of the step that's being queried.
   * @param row
   *          A row containing the input fields or an empty row if no input is required.
   * @return A Row containing the output fields.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getThisStepFields( String stepname, RowMetaInterface row ) throws KettleStepException {
    return getThisStepFields( findStep( stepname ), null, row );
  }

  /**
   * Returns the fields that are emitted by a step.
   *
   * @param stepMeta
   *          : The StepMeta object that's being queried
   * @param nextStep
   *          : if non-null this is the next step that's call back to ask what's being sent
   * @param row
   *          : A row containing the input fields or an empty row if no input is required.
   * @return A Row containing the output fields.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getThisStepFields( StepMeta stepMeta, StepMeta nextStep, RowMetaInterface row ) throws KettleStepException {
    return getThisStepFields( stepMeta, nextStep, row, null );
  }

  /**
   * Returns the fields that are emitted by a step.
   *
   * @param stepMeta
   *          : The StepMeta object that's being queried
   * @param nextStep
   *          : if non-null this is the next step that's call back to ask what's being sent
   * @param row
   *          : A row containing the input fields or an empty row if no input is required.
   * @param monitor
   *          the monitor
   * @return A Row containing the output fields.
   * @throws KettleStepException
   *           the kettle step exception
   */
  public RowMetaInterface getThisStepFields( StepMeta stepMeta, StepMeta nextStep, RowMetaInterface row,
      ProgressMonitorListener monitor ) throws KettleStepException {
    // Then this one.
    if ( log.isDebug() ) {
      log.logDebug( BaseMessages
          .getString( PKG, "TransMeta.Log.GettingFieldsFromStep", stepMeta.getName(), stepMeta.getStepID() ) );
    }
    String name = stepMeta.getName();

    if ( monitor != null ) {
      monitor.subTask( BaseMessages.getString( PKG, "TransMeta.Monitor.GettingFieldsFromStepTask.Title", name ) );
    }

    StepMetaInterface stepint = stepMeta.getStepMetaInterface();
    RowMetaInterface[] inform = null;
    StepMeta[] lu = getInfoStep( stepMeta );
    if ( Utils.isEmpty( lu ) ) {
      inform = new RowMetaInterface[] { stepint.getTableFields(), };
    } else {
      inform = new RowMetaInterface[lu.length];
      for ( int i = 0; i < lu.length; i++ ) {
        inform[i] = getStepFields( lu[i] );
      }
    }

    setRepositoryOnMappingSteps();

    // Go get the fields...
    //
    RowMetaInterface before = row.clone();
    RowMetaInterface[] clonedInfo = cloneRowMetaInterfaces( inform );
    compatibleGetStepFields( stepint, row, name, clonedInfo, nextStep, this );
    if ( !isSomethingDifferentInRow( before, row ) ) {
      stepint.getFields( before, name, clonedInfo, nextStep, this, repository, metaStore );
      // pass the clone object to prevent from spoiling data by other steps
      row = before;
    }

    return row;
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleGetStepFields( StepMetaInterface stepint, RowMetaInterface row, String name,
      RowMetaInterface[] inform, StepMeta nextStep, VariableSpace space ) throws KettleStepException {

    stepint.getFields( row, name, inform, nextStep, space );

  }

  private boolean isSomethingDifferentInRow( RowMetaInterface before, RowMetaInterface after ) {
    if ( before.size() != after.size() ) {
      return true;
    }
    for ( int i = 0; i < before.size(); i++ ) {
      ValueMetaInterface beforeValueMeta = before.getValueMeta( i );
      ValueMetaInterface afterValueMeta = after.getValueMeta( i );
      if ( stringsDifferent( beforeValueMeta.getName(), afterValueMeta.getName() ) ) {
        return true;
      }
      if ( beforeValueMeta.getType() != afterValueMeta.getType() ) {
        return true;
      }
      if ( beforeValueMeta.getLength() != afterValueMeta.getLength() ) {
        return true;
      }
      if ( beforeValueMeta.getPrecision() != afterValueMeta.getPrecision() ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getOrigin(), afterValueMeta.getOrigin() ) ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getComments(), afterValueMeta.getComments() ) ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getConversionMask(), afterValueMeta.getConversionMask() ) ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getStringEncoding(), afterValueMeta.getStringEncoding() ) ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getDecimalSymbol(), afterValueMeta.getDecimalSymbol() ) ) {
        return true;
      }
      if ( stringsDifferent( beforeValueMeta.getGroupingSymbol(), afterValueMeta.getGroupingSymbol() ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean stringsDifferent( String one, String two ) {
    if ( one == null && two == null ) {
      return false;
    }
    if ( one == null && two != null ) {
      return true;
    }
    if ( one != null && two == null ) {
      return true;
    }
    return !one.equals( two );
  }

  /**
   * Set the Repository object on the Mapping step That way the mapping step can determine the output fields for
   * repository hosted mappings... This is the exception to the rule so we don't pass this through the getFields()
   * method. TODO: figure out a way to make this more generic.
   */
  private void setRepositoryOnMappingSteps() {

    for ( StepMeta step : steps ) {
      if ( step.getStepMetaInterface() instanceof MappingMeta ) {
        ( (MappingMeta) step.getStepMetaInterface() ).setRepository( repository );
        ( (MappingMeta) step.getStepMetaInterface() ).setMetaStore( metaStore );
      }
      if ( step.getStepMetaInterface() instanceof SingleThreaderMeta ) {
        ( (SingleThreaderMeta) step.getStepMetaInterface() ).setRepository( repository );
        ( (SingleThreaderMeta) step.getStepMetaInterface() ).setMetaStore( metaStore );
      }
      if ( step.getStepMetaInterface() instanceof JobExecutorMeta ) {
        ( (JobExecutorMeta) step.getStepMetaInterface() ).setRepository( repository );
        ( (JobExecutorMeta) step.getStepMetaInterface() ).setMetaStore( metaStore );
      }
      if ( step.getStepMetaInterface() instanceof TransExecutorMeta ) {
        ( (TransExecutorMeta) step.getStepMetaInterface() ).setRepository( repository );
        ( (TransExecutorMeta) step.getStepMetaInterface() ).setMetaStore( metaStore );
      }
    }
  }

  /**
   * Checks if the transformation is using the specified partition schema.
   *
   * @param partitionSchema
   *          the partition schema
   * @return true if the transformation is using the partition schema, false otherwise
   */
  public boolean isUsingPartitionSchema( PartitionSchema partitionSchema ) {
    // Loop over all steps and see if the partition schema is used.
    for ( int i = 0; i < nrSteps(); i++ ) {
      StepPartitioningMeta stepPartitioningMeta = getStep( i ).getStepPartitioningMeta();
      if ( stepPartitioningMeta != null ) {
        PartitionSchema check = stepPartitioningMeta.getPartitionSchema();
        if ( check != null && check.equals( partitionSchema ) ) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Checks if the transformation is using a cluster schema.
   *
   * @return true if a cluster schema is used on one or more steps in this transformation, false otherwise
   */
  public boolean isUsingAClusterSchema() {
    return isUsingClusterSchema( null );
  }

  /**
   * Checks if the transformation is using the specified cluster schema.
   *
   * @param clusterSchema
   *          the cluster schema to check
   * @return true if the specified cluster schema is used on one or more steps in this transformation
   */
  public boolean isUsingClusterSchema( ClusterSchema clusterSchema ) {
    // Loop over all steps and see if the partition schema is used.
    for ( int i = 0; i < nrSteps(); i++ ) {
      ClusterSchema check = getStep( i ).getClusterSchema();
      if ( check != null && ( clusterSchema == null || check.equals( clusterSchema ) ) ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the transformation is using the specified slave server.
   *
   * @param slaveServer
   *          the slave server
   * @return true if the transformation is using the slave server, false otherwise
   * @throws KettleException
   *           if any errors occur while checking for the slave server
   */
  public boolean isUsingSlaveServer( SlaveServer slaveServer ) throws KettleException {
    // Loop over all steps and see if the slave server is used.
    for ( int i = 0; i < nrSteps(); i++ ) {
      ClusterSchema clusterSchema = getStep( i ).getClusterSchema();
      if ( clusterSchema != null ) {
        for ( SlaveServer check : clusterSchema.getSlaveServers() ) {
          if ( check.equals( slaveServer ) ) {
            return true;
          }
        }
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the transformation is referenced by a repository.
   *
   * @return true if the transformation is referenced by a repository, false otherwise
   */
  public boolean isRepReference() {
    return isRepReference( getFilename(), this.getName() );
  }

  /**
   * Checks if the transformation is referenced by a file. If the transformation is not referenced by a repository, it
   * is assumed to be referenced by a file.
   *
   * @return true if the transformation is referenced by a file, false otherwise
   * @see #isRepReference()
   */
  public boolean isFileReference() {
    return !isRepReference( getFilename(), this.getName() );
  }

  /**
   * Checks (using the exact filename and transformation name) if the transformation is referenced by a repository. If
   * referenced by a repository, the exact filename should be empty and the exact transformation name should be
   * non-empty.
   *
   * @param exactFilename
   *          the exact filename
   * @param exactTransname
   *          the exact transformation name
   * @return true if the transformation is referenced by a repository, false otherwise
   */
  public static boolean isRepReference( String exactFilename, String exactTransname ) {
    return Utils.isEmpty( exactFilename ) && !Utils.isEmpty( exactTransname );
  }

  /**
   * Checks (using the exact filename and transformation name) if the transformation is referenced by a file. If
   * referenced by a repository, the exact filename should be non-empty and the exact transformation name should be
   * empty.
   *
   * @param exactFilename
   *          the exact filename
   * @param exactTransname
   *          the exact transformation name
   * @return true if the transformation is referenced by a file, false otherwise
   * @see #isRepReference(String, String)
   */
  public static boolean isFileReference( String exactFilename, String exactTransname ) {
    return !isRepReference( exactFilename, exactTransname );
  }

  /**
   * Finds the location (index) of the specified hop.
   *
   * @param hi
   *          The hop queried
   * @return The location of the hop, or -1 if nothing was found.
   */
  public int indexOfTransHop( TransHopMeta hi ) {
    return hops.indexOf( hi );
  }

  /**
   * Finds the location (index) of the specified step.
   *
   * @param stepMeta
   *          The step queried
   * @return The location of the step, or -1 if nothing was found.
   */
  public int indexOfStep( StepMeta stepMeta ) {
    return steps.indexOf( stepMeta );
  }

  /**
   * Gets the file type. For TransMeta, this returns a value corresponding to Transformation
   *
   * @return the file type
   * @see org.pentaho.di.core.EngineMetaInterface#getFileType()
   */
  @Override
  public String getFileType() {
    return LastUsedFile.FILE_TYPE_TRANSFORMATION;
  }

  /**
   * Gets the transformation filter names.
   *
   * @return the filter names
   * @see org.pentaho.di.core.EngineMetaInterface#getFilterNames()
   */
  @Override
  public String[] getFilterNames() {
    return Const.getTransformationFilterNames();
  }

  /**
   * Gets the transformation filter extensions. For TransMeta, this method returns the value of
   * {@link Const#STRING_TRANS_FILTER_EXT}
   *
   * @return the filter extensions
   * @see org.pentaho.di.core.EngineMetaInterface#getFilterExtensions()
   */
  @Override
  public String[] getFilterExtensions() {
    return Const.STRING_TRANS_FILTER_EXT;
  }

  /**
   * Gets the default extension for a transformation. For TransMeta, this method returns the value of
   * {@link Const#STRING_TRANS_DEFAULT_EXT}
   *
   * @return the default extension
   * @see org.pentaho.di.core.EngineMetaInterface#getDefaultExtension()
   */
  @Override
  public String getDefaultExtension() {
    return Const.STRING_TRANS_DEFAULT_EXT;
  }

  /**
   * Gets the XML representation of this transformation.
   *
   * @return the XML representation of this transformation
   * @throws KettleException
   *           if any errors occur during generation of the XML
   * @see org.pentaho.di.core.xml.XMLInterface#getXML()
   */
  @Override
  public String getXML() throws KettleException {
    return getXML( true, true, true, true, true );
  }

  /**
   * Gets the XML representation of this transformation, including or excluding step, database, slave server, cluster,
   * or partition information as specified by the parameters
   *
   * @param includeSteps
   *          whether to include step data
   * @param includeDatabase
   *          whether to include database data
   * @param includeSlaves
   *          whether to include slave server data
   * @param includeClusters
   *          whether to include cluster data
   * @param includePartitions
   *          whether to include partition data
   * @return the XML representation of this transformation
   * @throws KettleException
   *           if any errors occur during generation of the XML
   */
  public String getXML( boolean includeSteps, boolean includeDatabase, boolean includeSlaves, boolean includeClusters,
      boolean includePartitions ) throws KettleException {
    return getXML( true, true, true, true, true, true, true, true, true, true );
  }

  /**
   * Gets the XML representation of this transformation, including or excluding step, database, slave server, cluster,
   * or partition information as specified by the parameters
   *
   * @param includeSteps
   *          whether to include step data
   * @param includeDatabase
   *          whether to include database data
   * @param includeSlaves
   *          whether to include slave server data
   * @param includeClusters
   *          whether to include cluster data
   * @param includePartitions
   *          whether to include partition data
   * @param includeNamedParameters
   *          whether to include named parameters data
   * @param includeLog
   *          whether to include log data
   * @param includeDependencies
   *          whether to include dependencies data
   * @param includeNotePads
   *          whether to include notepads data
   * @param includeAttributeGroups
   *          whether to include attributes map data
   * @return the XML representation of this transformation
   * @throws KettleException
   *           if any errors occur during generation of the XML
   */
  public String getXML( boolean includeSteps, boolean includeDatabase, boolean includeSlaves, boolean includeClusters,
    boolean includePartitions, boolean includeNamedParameters, boolean includeLog, boolean includeDependencies,
    boolean includeNotePads, boolean includeAttributeGroups ) throws KettleException {

    //Clear the embedded named clusters.  We will be repopulating from steps that used named clusters
    getNamedClusterEmbedManager().clear();

    Props props = null;
    if ( Props.isInitialized() ) {
      props = Props.getInstance();
    }

    StringBuilder retval = new StringBuilder( 800 );

    retval.append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

    retval.append( "  " ).append( XMLHandler.openTag( XML_TAG_INFO ) ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "name", name ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "description", description ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extended_description", extendedDescription ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trans_version", trans_version ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "trans_type", transformationType.getCode() ) );

    if ( trans_status >= 0 ) {
      retval.append( "    " ).append( XMLHandler.addTagValue( "trans_status", trans_status ) );
    }
    retval.append( "    " ).append( XMLHandler.addTagValue( "directory",
            directory != null ? directory.getPath() : RepositoryDirectory.DIRECTORY_SEPARATOR ) );


    if ( includeNamedParameters ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_PARAMETERS ) ).append( Const.CR );
      String[] parameters = listParameters();
      for ( int idx = 0; idx < parameters.length; idx++ ) {
        retval.append( "      " ).append( XMLHandler.openTag( "parameter" ) ).append( Const.CR );
        retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameters[idx] ) );
        retval.append( "        " )
                .append( XMLHandler.addTagValue( "default_value", getParameterDefault( parameters[idx] ) ) );
        retval.append( "        " )
                .append( XMLHandler.addTagValue( "description", getParameterDescription( parameters[idx] ) ) );
        retval.append( "      " ).append( XMLHandler.closeTag( "parameter" ) ).append( Const.CR );
      }
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_PARAMETERS ) ).append( Const.CR );
    }

    if ( includeLog ) {
      retval.append( "    " ).append( XMLHandler.openTag( "log" ) ).append( Const.CR );

      // Add the metadata for the various logging tables
      //
      retval.append( transLogTable.getXML() );
      retval.append( performanceLogTable.getXML() );
      retval.append( channelLogTable.getXML() );
      retval.append( stepLogTable.getXML() );
      retval.append( metricsLogTable.getXML() );

      retval.append( "    " ).append( XMLHandler.closeTag( "log" ) ).append( Const.CR );
    }

    retval.append( "    " ).append( XMLHandler.openTag( "maxdate" ) ).append( Const.CR );
    retval.append( "      " )
        .append( XMLHandler.addTagValue( "connection", maxDateConnection == null ? "" : maxDateConnection.getName() ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "table", maxDateTable ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "field", maxDateField ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "offset", maxDateOffset ) );
    retval.append( "      " ).append( XMLHandler.addTagValue( "maxdiff", maxDateDifference ) );
    retval.append( "    " ).append( XMLHandler.closeTag( "maxdate" ) ).append( Const.CR );

    retval.append( "    " ).append( XMLHandler.addTagValue( "size_rowset", sizeRowset ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "sleep_time_empty", sleepTimeEmpty ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "sleep_time_full", sleepTimeFull ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "unique_connections", usingUniqueConnections ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "feedback_shown", feedbackShown ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "feedback_size", feedbackSize ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "using_thread_priorities", usingThreadPriorityManagment ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "shared_objects_file", sharedObjectsFile ) );

    // Performance monitoring
    //
    retval.append( "    " )
        .append( XMLHandler.addTagValue( "capture_step_performance", capturingStepPerformanceSnapShots ) );
    retval.append( "    " )
        .append( XMLHandler.addTagValue( "step_performance_capturing_delay", stepPerformanceCapturingDelay ) );
    retval.append( "    " )
        .append( XMLHandler.addTagValue( "step_performance_capturing_size_limit", stepPerformanceCapturingSizeLimit ) );

    if ( includeDependencies ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_DEPENDENCIES ) ).append( Const.CR );
      for ( int i = 0; i < nrDependencies(); i++ ) {
        TransDependency td = getDependency( i );
        retval.append( td.getXML() );
      }
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_DEPENDENCIES ) ).append( Const.CR );
    }

    // The partitioning schemas...
    //
    if ( includePartitions ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_PARTITIONSCHEMAS ) ).append( Const.CR );
      for ( int i = 0; i < partitionSchemas.size(); i++ ) {
        PartitionSchema partitionSchema = partitionSchemas.get( i );
        retval.append( partitionSchema.getXML() );
      }
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_PARTITIONSCHEMAS ) ).append( Const.CR );
    }
    // The slave servers...
    //
    if ( includeSlaves ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_SLAVESERVERS ) ).append( Const.CR );
      for ( int i = 0; i < slaveServers.size(); i++ ) {
        SlaveServer slaveServer = slaveServers.get( i );
        retval.append( slaveServer.getXML() );
      }
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_SLAVESERVERS ) ).append( Const.CR );
    }

    // The cluster schemas...
    //
    if ( includeClusters ) {
      retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_CLUSTERSCHEMAS ) ).append( Const.CR );
      for ( int i = 0; i < clusterSchemas.size(); i++ ) {
        ClusterSchema clusterSchema = clusterSchemas.get( i );
        retval.append( clusterSchema.getXML() );
      }
      retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_CLUSTERSCHEMAS ) ).append( Const.CR );
    }

    retval.append( "    " ).append( XMLHandler.addTagValue( "created_user", createdUser ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "created_date", XMLHandler.date2string( createdDate ) ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "modified_user", modifiedUser ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "modified_date", XMLHandler.date2string( modifiedDate ) ) );

    try {
      retval.append( "    " ).append( XMLHandler.addTagValue( "key_for_session_key", keyForSessionKey ) );
    } catch ( Exception ex ) {
      log.logError( "Unable to decode key", ex );
    }
    retval.append( "    " ).append( XMLHandler.addTagValue( "is_key_private", isKeyPrivate ) );

    retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG_INFO ) ).append( Const.CR );

    if ( includeNotePads ) {
      retval.append( "  " ).append( XMLHandler.openTag( XML_TAG_NOTEPADS ) ).append( Const.CR );
      if ( notes != null ) {
        for ( int i = 0; i < nrNotes(); i++ ) {
          NotePadMeta ni = getNote( i );
          retval.append( ni.getXML() );
        }
      }
      retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG_NOTEPADS ) ).append( Const.CR );
    }

    // The database connections...
    if ( includeDatabase ) {
      for ( int i = 0; i < nrDatabases(); i++ ) {
        DatabaseMeta dbMeta = getDatabase( i );
        //PDI-20078 - If props == null, it means transformation is running on the slave server. For the
        // method areOnlyUsedConnectionsSavedToXMLInServer to return false, the "STRING_ONLY_USED_DB_TO_XML"
        // needs to have "N" in the server startup script file
        if ( isDatabaseConnectionUsed( dbMeta ) || ( props != null && !props.areOnlyUsedConnectionsSavedToXML() ) || ( props == null && !areOnlyUsedConnectionsSavedToXMLInServer() ) ) {
          retval.append( dbMeta.getXML() );
        }
      }
    }

    if ( includeSteps ) {
      retval.append( "  " ).append( XMLHandler.openTag( XML_TAG_ORDER ) ).append( Const.CR );
      for ( int i = 0; i < nrTransHops(); i++ ) {
        TransHopMeta transHopMeta = getTransHop( i );
        retval.append( transHopMeta.getXML() );
      }
      retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG_ORDER ) ).append( Const.CR );

      /* The steps... */
      for ( int i = 0; i < nrSteps(); i++ ) {
        StepMeta stepMeta = getStep( i );
        if ( stepMeta.getStepMetaInterface() instanceof HasRepositoryInterface ) {
          ( (HasRepositoryInterface) stepMeta.getStepMetaInterface() ).setRepository( repository );
        }
        retval.append( stepMeta.getXML() );
      }

      /* The error handling metadata on the steps */
      retval.append( "  " ).append( XMLHandler.openTag( XML_TAG_STEP_ERROR_HANDLING ) ).append( Const.CR );
      for ( int i = 0; i < nrSteps(); i++ ) {
        StepMeta stepMeta = getStep( i );

        if ( stepMeta.getStepErrorMeta() != null ) {
          retval.append( stepMeta.getStepErrorMeta().getXML() );
        }
      }
      retval.append( "  " ).append( XMLHandler.closeTag( XML_TAG_STEP_ERROR_HANDLING ) ).append( Const.CR );
    }

    // The slave-step-copy/partition distribution. Only used for slave transformations in a clustering environment.
    retval.append( slaveStepCopyPartitionDistribution.getXML() );

    // Is this a slave transformation or not?
    retval.append( "  " ).append( XMLHandler.addTagValue( "slave_transformation", slaveTransformation ) );

    // Also store the attribute groups
    //
    if ( includeAttributeGroups ) {
      retval.append( AttributesUtil.getAttributesXml( attributesMap ) );
    }
    retval.append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return XMLFormatter.format( retval.toString() );
  }

  public boolean areOnlyUsedConnectionsSavedToXMLInServer() {
    String show = System.getProperty( Const.STRING_ONLY_USED_DB_TO_XML, "Y" );
    return "Y".equalsIgnoreCase( show ); // Default: save only used connections
  }

  /**
   * Parses a file containing the XML that describes the transformation. No default connections are loaded since no
   * repository is available at this time. Since the filename is set, internal variables are being set that relate to
   * this.
   *
   * @param fname
   *          The filename
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname ) throws KettleXMLException, KettleMissingPluginsException {
    this( fname, true );
  }

  /**
   * Parses a file containing the XML that describes the transformation. No default connections are loaded since no
   * repository is available at this time. Since the filename is set, variables are set in the specified variable space
   * that relate to this.
   *
   * @param fname
   *          The filename
   * @param parentVariableSpace
   *          the parent variable space
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, VariableSpace parentVariableSpace ) throws KettleXMLException,
    KettleMissingPluginsException {
    this( fname, null, true, parentVariableSpace );
  }

  /**
   * Parses a file containing the XML that describes the transformation. No default connections are loaded since no
   * repository is available at this time.
   *
   * @param fname
   *          The filename
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, boolean setInternalVariables ) throws KettleXMLException,
    KettleMissingPluginsException {
    this( fname, null, setInternalVariables );
  }

  /**
   * Parses a file containing the XML that describes the transformation.
   *
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default set of connections from, null if no repository is available
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, Repository rep ) throws KettleXMLException, KettleMissingPluginsException {
    this( fname, rep, true );
  }

  /**
   * Parses a file containing the XML that describes the transformation.
   *
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default set of connections from, null if no repository is available
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, Repository rep, boolean setInternalVariables ) throws KettleXMLException,
    KettleMissingPluginsException {
    this( fname, rep, setInternalVariables, null );
  }

  /**
   * Parses a file containing the XML that describes the transformation.
   *
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default set of connections from, null if no repository is available
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace ) throws KettleXMLException, KettleMissingPluginsException {
    this( fname, rep, setInternalVariables, parentVariableSpace, null );
  }

  /**
   * Parses a file containing the XML that describes the transformation.
   *
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default set of connections from, null if no repository is available
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @param prompter
   *          the changed/replace listener or null if there is none
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace,
      OverwritePrompter prompter ) throws KettleXMLException, KettleMissingPluginsException {
    this( fname, null, rep, setInternalVariables, parentVariableSpace, prompter );
  }

  /**
   * Parses a file containing the XML that describes the transformation.
   *
   * @param fname
   *          The filename
   * @param metaStore
   *          the metadata store to reference (or null if there is none)
   * @param rep
   *          The repository to load the default set of connections from, null if no repository is available
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @param prompter
   *          the changed/replace listener or null if there is none
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( String fname, IMetaStore metaStore, Repository rep, boolean setInternalVariables,
                    VariableSpace parentVariableSpace, OverwritePrompter prompter )
    throws KettleXMLException, KettleMissingPluginsException {
    // if fname is not provided, there's not much we can do, throw an exception
    if ( StringUtils.isBlank( fname ) ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "TransMeta.Exception.MissingXMLFilePath" ) );
    }
    this.metaStore = metaStore;
    this.repository = rep;

    // OK, try to load using the VFS stuff...
    Document doc = null;
    try {
      if (parentVariableSpace == null ) {
        parentVariableSpace = new Variables();
      }

      final FileObject transFile = KettleVFS.getFileObject( fname, parentVariableSpace );
      if ( !transFile.exists() ) {
        throw new KettleXMLException( BaseMessages.getString( PKG, "TransMeta.Exception.InvalidXMLPath", fname ) );
      }
      doc = XMLHandler.loadXMLFile( transFile );
    } catch ( KettleXMLException ke ) {
      // if we have a KettleXMLException, simply re-throw it
      throw ke;
    } catch ( KettleException | FileSystemException e ) {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", fname ), e );
    }

    if ( doc != null ) {
      // Root node:
      Node transnode = XMLHandler.getSubNode( doc, XML_TAG );

      if ( transnode == null ) {
        throw new KettleXMLException( BaseMessages.getString(
          PKG, "TransMeta.Exception.NotValidTransformationXML", fname ) );
      }

      // Load from this node...
      loadXML( transnode, fname, metaStore, rep, setInternalVariables, parentVariableSpace, prompter );

    } else {
      throw new KettleXMLException( BaseMessages.getString(
        PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", fname ) );
    }
  }

  /**
   * Instantiates a new transformation meta-data object.
   *
   * @param xmlStream
   *          the XML input stream from which to read the transformation definition
   * @param rep
   *          the repository
   * @param setInternalVariables
   *          whether to set internal variables as a result of the creation
   * @param parentVariableSpace
   *          the parent variable space
   * @param prompter
   *          a GUI component that will prompt the user if the new transformation will overwrite an existing one
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified stream
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( InputStream xmlStream, Repository rep, boolean setInternalVariables,
                    VariableSpace parentVariableSpace, OverwritePrompter prompter )
    throws KettleXMLException, KettleMissingPluginsException {
    Document doc = XMLHandler.loadXMLFile( xmlStream, null, false, false );
    Node transnode = XMLHandler.getSubNode( doc, XML_TAG );
    loadXML( transnode, rep, setInternalVariables, parentVariableSpace, prompter );
  }

  /**
   * Parse a file containing the XML that describes the transformation. Specify a repository to load default list of
   * database connections from and to reference in mappings etc.
   *
   * @param transnode
   *          The XML node to load from
   * @param rep
   *          the repository to reference.
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public TransMeta( Node transnode, Repository rep ) throws KettleXMLException, KettleMissingPluginsException {
    loadXML( transnode, rep, false );
  }

  /**
   * Parses an XML DOM (starting at the specified Node) that describes the transformation.
   *
   * @param transnode
   *          The XML node to load from
   * @param rep
   *          The repository to load the default list of database connections from (null if no repository is available)
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public void loadXML( Node transnode, Repository rep, boolean setInternalVariables ) throws KettleXMLException,
    KettleMissingPluginsException {
    loadXML( transnode, rep, setInternalVariables, null );
  }

  /**
   * Parses an XML DOM (starting at the specified Node) that describes the transformation.
   *
   * @param transnode
   *          The XML node to load from
   * @param rep
   *          The repository to load the default list of database connections from (null if no repository is available)
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public void loadXML( Node transnode, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace )
    throws KettleXMLException, KettleMissingPluginsException {
    loadXML( transnode, rep, setInternalVariables, parentVariableSpace, null );
  }

  /**
   * Parses an XML DOM (starting at the specified Node) that describes the transformation.
   *
   * @param transnode
   *          The XML node to load from
   * @param rep
   *          The repository to load the default list of database connections from (null if no repository is available)
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @param prompter
   *          the changed/replace listener or null if there is none
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public void loadXML( Node transnode, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace,
      OverwritePrompter prompter ) throws KettleXMLException, KettleMissingPluginsException {
    loadXML( transnode, null, rep, setInternalVariables, parentVariableSpace, prompter );
  }

  /**
   * Parses an XML DOM (starting at the specified Node) that describes the transformation.
   *
   * @param transnode
   *          The XML node to load from
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default list of database connections from (null if no repository is available)
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @param prompter
   *          the changed/replace listener or null if there is none
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public void loadXML( Node transnode, String fname, Repository rep, boolean setInternalVariables,
                       VariableSpace parentVariableSpace, OverwritePrompter prompter )
    throws KettleXMLException, KettleMissingPluginsException {
    loadXML( transnode, fname, null, rep, setInternalVariables, parentVariableSpace, prompter );
  }

  /**
   * Parses an XML DOM (starting at the specified Node) that describes the transformation.
   *
   * @param transnode
   *          The XML node to load from
   * @param fname
   *          The filename
   * @param rep
   *          The repository to load the default list of database connections from (null if no repository is available)
   * @param setInternalVariables
   *          true if you want to set the internal variables based on this transformation information
   * @param parentVariableSpace
   *          the parent variable space to use during TransMeta construction
   * @param prompter
   *          the changed/replace listener or null if there is none
   * @throws KettleXMLException
   *           if any errors occur during parsing of the specified file
   * @throws KettleMissingPluginsException
   *           in case missing plugins were found (details are in the exception in that case)
   */
  public void loadXML( Node transnode, String fname, IMetaStore metaStore, Repository rep, boolean setInternalVariables,
                       VariableSpace parentVariableSpace, OverwritePrompter prompter )
    throws KettleXMLException, KettleMissingPluginsException {

    KettleMissingPluginsException
      missingPluginsException =
      new KettleMissingPluginsException(
        BaseMessages.getString( PKG, "TransMeta.MissingPluginsFoundWhileLoadingTransformation.Exception" ) );

    this.metaStore = metaStore; // Remember this as the primary meta store.

    try {

      Props props = null;
      if ( Props.isInitialized() ) {
        props = Props.getInstance();
      }

      initializeVariablesFrom( parentVariableSpace );

      try {
        // Clear the transformation
        clear();

        // If we are not using a repository, we are getting the transformation from a file
        // Set the filename here so it can be used in variables for ALL aspects of the transformation FIX: PDI-8890
        if ( null == rep ) {
          setFilename( fname );
        } else {
          // Set the repository here so it can be used in variables for ALL aspects of the job FIX: PDI-16441
          setRepository( rep );
        }

        // Read all the database connections from the repository to make sure that we don't overwrite any there by
        // loading from XML.
        //
        try {
          sharedObjectsFile = XMLHandler.getTagValue( transnode, "info", "shared_objects_file" );
          sharedObjects = rep != null ? rep.readTransSharedObjects( this ) : readSharedObjects();
        } catch ( Exception e ) {
          log
            .logError( BaseMessages.getString( PKG, "TransMeta.ErrorReadingSharedObjects.Message", e.toString() ) );
          log.logError( Const.getStackTracker( e ) );
        }

        // Call the extension point after the shared objects are loaded
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransSharedObjectsLoaded.id, this );

        // Load the database connections, slave servers, cluster schemas & partition schemas into this object.
        //
        importFromMetaStore();

        // Handle connections
        int n = XMLHandler.countNodes( transnode, DatabaseMeta.XML_TAG );
        Set<String> privateTransformationDatabases = new HashSet<>( n );
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.WeHaveConnections", String.valueOf( n ) ) );
        }
        for ( int i = 0; i < n; i++ ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.LookingAtConnection" ) + i );
          }
          Node nodecon = XMLHandler.getSubNodeByNr( transnode, DatabaseMeta.XML_TAG, i );

          DatabaseMeta dbcon = new DatabaseMeta( nodecon );
          dbcon.shareVariablesWith( this );
          if ( !dbcon.isShared() ) {
            privateTransformationDatabases.add( dbcon.getName() );
          }

          DatabaseMeta exist = findDatabase( dbcon.getName() );
          if ( exist == null ) {
            addDatabase( dbcon );
          } else {
            if ( !exist.isShared() ) { // otherwise, we just keep the shared connection.
              if ( shouldOverwrite( prompter, props, BaseMessages.getString( PKG,
                  "TransMeta.Message.OverwriteConnectionYN", dbcon.getName() ), BaseMessages.getString( PKG,
                  "TransMeta.Message.OverwriteConnection.DontShowAnyMoreMessage" ) ) ) {
                int idx = indexOfDatabase( exist );
                removeDatabase( idx );
                addDatabase( idx, dbcon );
              }
            }
          }
        }
        setPrivateDatabases( privateTransformationDatabases );

        // Read the notes...
        Node notepadsnode = XMLHandler.getSubNode( transnode, XML_TAG_NOTEPADS );
        int nrnotes = XMLHandler.countNodes( notepadsnode, NotePadMeta.XML_TAG );
        for ( int i = 0; i < nrnotes; i++ ) {
          Node notepadnode = XMLHandler.getSubNodeByNr( notepadsnode, NotePadMeta.XML_TAG, i );
          NotePadMeta ni = new NotePadMeta( notepadnode );
          notes.add( ni );
        }

        // Handle Steps
        int s = XMLHandler.countNodes( transnode, StepMeta.XML_TAG );

        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.ReadingSteps" ) + s + " steps..." );
        }
        for ( int i = 0; i < s; i++ ) {
          Node stepnode = XMLHandler.getSubNodeByNr( transnode, StepMeta.XML_TAG, i );

          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.LookingAtStep" ) + i );
          }

          StepMeta stepMeta = new StepMeta( stepnode, databases, metaStore );
          stepMeta.setParentTransMeta( this ); // for tracing, retain hierarchy

          if ( stepMeta.isMissing() ) {
            addMissingTrans( (MissingTrans) stepMeta.getStepMetaInterface() );
          }
          // Check if the step exists and if it's a shared step.
          // If so, then we will keep the shared version, not this one.
          // The stored XML is only for backup purposes.
          //
          StepMeta check = findStep( stepMeta.getName() );
          if ( check != null ) {
            if ( !check.isShared() ) {
              // Don't overwrite shared objects

              addOrReplaceStep( stepMeta );
            } else {
              check.setDraw( stepMeta.isDrawn() ); // Just keep the drawn flag and location
              check.setLocation( stepMeta.getLocation() );
            }
          } else {
            addStep( stepMeta ); // simply add it.
          }
        }

        // Read the error handling code of the steps...
        //
        Node errorHandlingNode = XMLHandler.getSubNode( transnode, XML_TAG_STEP_ERROR_HANDLING );
        int nrErrorHandlers = XMLHandler.countNodes( errorHandlingNode, StepErrorMeta.XML_ERROR_TAG );
        for ( int i = 0; i < nrErrorHandlers; i++ ) {
          Node stepErrorMetaNode = XMLHandler.getSubNodeByNr( errorHandlingNode, StepErrorMeta.XML_ERROR_TAG, i );
          StepErrorMeta stepErrorMeta = new StepErrorMeta( this, stepErrorMetaNode, steps );
          if ( stepErrorMeta.getSourceStep() != null ) {
            stepErrorMeta.getSourceStep().setStepErrorMeta( stepErrorMeta ); // a bit of a trick, I know.
          }
        }

        // Have all StreamValueLookups, etc. reference the correct source steps...
        //
        for ( int i = 0; i < nrSteps(); i++ ) {
          StepMeta stepMeta = getStep( i );
          StepMetaInterface sii = stepMeta.getStepMetaInterface();
          if ( sii != null ) {
            sii.searchInfoAndTargetSteps( steps );
          }
        }

        // Handle Hops
        //
        Node ordernode = XMLHandler.getSubNode( transnode, XML_TAG_ORDER );
        n = XMLHandler.countNodes( ordernode, TransHopMeta.XML_HOP_TAG );

        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.WeHaveHops" ) + n + " hops..." );
        }
        for ( int i = 0; i < n; i++ ) {
          if ( log.isDebug() ) {
            log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.LookingAtHop" ) + i );
          }
          Node hopnode = XMLHandler.getSubNodeByNr( ordernode, TransHopMeta.XML_HOP_TAG, i );

          TransHopMeta hopinf = new TransHopMeta( hopnode, steps );
          hopinf.setErrorHop( isErrorNode( errorHandlingNode, hopnode ) );
          addTransHop( hopinf );
        }

        //
        // get transformation info:
        //
        Node infonode = XMLHandler.getSubNode( transnode, XML_TAG_INFO );

        // Name
        //
        setName( XMLHandler.getTagValue( infonode, "name" ) );

        // description
        //
        description = XMLHandler.getTagValue( infonode, "description" );

        // extended description
        //
        extendedDescription = XMLHandler.getTagValue( infonode, "extended_description" );

        // trans version
        //
        trans_version = XMLHandler.getTagValue( infonode, "trans_version" );

        // trans status
        //
        trans_status = Const.toInt( XMLHandler.getTagValue( infonode, "trans_status" ), -1 );

        String transTypeCode = XMLHandler.getTagValue( infonode, "trans_type" );
        transformationType = TransformationType.getTransformationTypeByCode( transTypeCode );

        // Optionally load the repository directory...
        //
        if ( rep != null ) {
          String directoryPath = XMLHandler.getTagValue( infonode, "directory" );
          if ( directoryPath != null ) {
            directory = rep.findDirectory( directoryPath );
            if ( directory == null ) { // not found
              directory = new RepositoryDirectory(); // The root as default
            }
          }
        }

        // Read logging table information
        //
        Node logNode = XMLHandler.getSubNode( infonode, "log" );
        if ( logNode != null ) {

          // Backward compatibility...
          //
          Node transLogNode = XMLHandler.getSubNode( logNode, TransLogTable.XML_TAG );
          if ( transLogNode == null ) {
            // Load the XML
            //
            transLogTable.findField( TransLogTable.ID.LINES_READ )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "read" ) ) );
            transLogTable.findField( TransLogTable.ID.LINES_WRITTEN )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "write" ) ) );
            transLogTable.findField( TransLogTable.ID.LINES_INPUT )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "input" ) ) );
            transLogTable.findField( TransLogTable.ID.LINES_OUTPUT )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "output" ) ) );
            transLogTable.findField( TransLogTable.ID.LINES_UPDATED )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "update" ) ) );
            transLogTable.findField( TransLogTable.ID.LINES_REJECTED )
                .setSubject( findStep( XMLHandler.getTagValue( infonode, "log", "rejected" ) ) );

            transLogTable.setConnectionName( XMLHandler.getTagValue( infonode, "log", "connection" ) );
            transLogTable.setSchemaName( XMLHandler.getTagValue( infonode, "log", "schema" ) );
            transLogTable.setTableName( XMLHandler.getTagValue( infonode, "log", "table" ) );
            transLogTable.findField( TransLogTable.ID.ID_BATCH )
                .setEnabled( "Y".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "log", "use_batchid" ) ) );
            transLogTable.findField( TransLogTable.ID.LOG_FIELD )
                .setEnabled( "Y".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "log", "USE_LOGFIELD" ) ) );
            transLogTable.setLogSizeLimit( XMLHandler.getTagValue( infonode, "log", "size_limit_lines" ) );
            transLogTable.setLogInterval( XMLHandler.getTagValue( infonode, "log", "interval" ) );
            transLogTable.findField( TransLogTable.ID.CHANNEL_ID ).setEnabled( false );
            transLogTable.findField( TransLogTable.ID.LINES_REJECTED ).setEnabled( false );
            performanceLogTable.setConnectionName( transLogTable.getConnectionName() );
            performanceLogTable.setTableName( XMLHandler.getTagValue( infonode, "log", "step_performance_table" ) );
          } else {
            transLogTable.loadXML( transLogNode, databases, steps );
          }
          Node perfLogNode = XMLHandler.getSubNode( logNode, PerformanceLogTable.XML_TAG );
          if ( perfLogNode != null ) {
            performanceLogTable.loadXML( perfLogNode, databases, steps );
          }
          Node channelLogNode = XMLHandler.getSubNode( logNode, ChannelLogTable.XML_TAG );
          if ( channelLogNode != null ) {
            channelLogTable.loadXML( channelLogNode, databases, steps );
          }
          Node stepLogNode = XMLHandler.getSubNode( logNode, StepLogTable.XML_TAG );
          if ( stepLogNode != null ) {
            stepLogTable.loadXML( stepLogNode, databases, steps );
          }
          Node metricsLogNode = XMLHandler.getSubNode( logNode, MetricsLogTable.XML_TAG );
          if ( metricsLogNode != null ) {
            metricsLogTable.loadXML( metricsLogNode, databases, steps );
          }
        }

        // Maxdate range options...
        String maxdatcon = XMLHandler.getTagValue( infonode, "maxdate", "connection" );
        maxDateConnection = findDatabase( maxdatcon );
        maxDateTable = XMLHandler.getTagValue( infonode, "maxdate", "table" );
        maxDateField = XMLHandler.getTagValue( infonode, "maxdate", "field" );
        String offset = XMLHandler.getTagValue( infonode, "maxdate", "offset" );
        maxDateOffset = Const.toDouble( offset, 0.0 );
        String mdiff = XMLHandler.getTagValue( infonode, "maxdate", "maxdiff" );
        maxDateDifference = Const.toDouble( mdiff, 0.0 );

        // Check the dependencies as far as dates are concerned...
        // We calculate BEFORE we run the MAX of these dates
        // If the date is larger then enddate, startdate is set to MIN_DATE
        //
        Node depsNode = XMLHandler.getSubNode( infonode, XML_TAG_DEPENDENCIES );
        int nrDeps = XMLHandler.countNodes( depsNode, TransDependency.XML_TAG );

        for ( int i = 0; i < nrDeps; i++ ) {
          Node depNode = XMLHandler.getSubNodeByNr( depsNode, TransDependency.XML_TAG, i );

          TransDependency transDependency = new TransDependency( depNode, databases );
          if ( transDependency.getDatabase() != null && transDependency.getFieldname() != null ) {
            addDependency( transDependency );
          }
        }

        // Read the named parameters.
        Node paramsNode = XMLHandler.getSubNode( infonode, XML_TAG_PARAMETERS );
        int nrParams = XMLHandler.countNodes( paramsNode, "parameter" );

        for ( int i = 0; i < nrParams; i++ ) {
          Node paramNode = XMLHandler.getSubNodeByNr( paramsNode, "parameter", i );

          String paramName = XMLHandler.getTagValue( paramNode, "name" );
          String defaultValue = XMLHandler.getTagValue( paramNode, "default_value" );
          String descr = XMLHandler.getTagValue( paramNode, "description" );

          addParameterDefinition( paramName, defaultValue, descr );
        }

        // Read the partitioning schemas
        //
        Node partSchemasNode = XMLHandler.getSubNode( infonode, XML_TAG_PARTITIONSCHEMAS );
        int nrPartSchemas = XMLHandler.countNodes( partSchemasNode, PartitionSchema.XML_TAG );
        for ( int i = 0; i < nrPartSchemas; i++ ) {
          Node partSchemaNode = XMLHandler.getSubNodeByNr( partSchemasNode, PartitionSchema.XML_TAG, i );
          PartitionSchema partitionSchema = new PartitionSchema( partSchemaNode );

          // Check if the step exists and if it's a shared step.
          // If so, then we will keep the shared version, not this one.
          // The stored XML is only for backup purposes.
          //
          PartitionSchema check = findPartitionSchema( partitionSchema.getName() );
          if ( check != null ) {
            if ( !check.isShared() ) {
              // we don't overwrite shared objects.
              if ( shouldOverwrite( prompter, props, BaseMessages
                  .getString( PKG, "TransMeta.Message.OverwritePartitionSchemaYN", partitionSchema.getName() ),
                  BaseMessages.getString( PKG, "TransMeta.Message.OverwriteConnection.DontShowAnyMoreMessage" ) ) ) {
                addOrReplacePartitionSchema( partitionSchema );
              }
            }
          } else {
            partitionSchemas.add( partitionSchema );
          }

        }

        // Have all step partitioning meta-data reference the correct schemas that we just loaded
        //
        for ( int i = 0; i < nrSteps(); i++ ) {
          StepPartitioningMeta stepPartitioningMeta = getStep( i ).getStepPartitioningMeta();
          if ( stepPartitioningMeta != null ) {
            stepPartitioningMeta.setPartitionSchemaAfterLoading( partitionSchemas );
          }
          StepPartitioningMeta targetStepPartitioningMeta = getStep( i ).getTargetStepPartitioningMeta();
          if ( targetStepPartitioningMeta != null ) {
            targetStepPartitioningMeta.setPartitionSchemaAfterLoading( partitionSchemas );
          }
        }

        // Read the slave servers...
        //
        Node slaveServersNode = XMLHandler.getSubNode( infonode, XML_TAG_SLAVESERVERS );
        int nrSlaveServers = XMLHandler.countNodes( slaveServersNode, SlaveServer.XML_TAG );
        for ( int i = 0; i < nrSlaveServers; i++ ) {
          Node slaveServerNode = XMLHandler.getSubNodeByNr( slaveServersNode, SlaveServer.XML_TAG, i );
          SlaveServer slaveServer = new SlaveServer( slaveServerNode );
          if ( slaveServer.getName() == null ) {
            log.logError( BaseMessages.getString( PKG, "TransMeta.Log.WarningWhileCreationSlaveServer", slaveServer.getName() ) );
            continue;
          }
          slaveServer.shareVariablesWith( this );

          // Check if the object exists and if it's a shared object.
          // If so, then we will keep the shared version, not this one.
          // The stored XML is only for backup purposes.
          SlaveServer check = findSlaveServer( slaveServer.getName() );
          if ( check != null ) {
            if ( !check.isShared() ) {
              // we don't overwrite shared objects.
              if ( shouldOverwrite( prompter, props,
                  BaseMessages.getString( PKG, "TransMeta.Message.OverwriteSlaveServerYN", slaveServer.getName() ),
                  BaseMessages.getString( PKG, "TransMeta.Message.OverwriteConnection.DontShowAnyMoreMessage" ) ) ) {
                addOrReplaceSlaveServer( slaveServer );
              }
            }
          } else {
            slaveServers.add( slaveServer );
          }
        }

        // Read the cluster schemas
        //
        Node clusterSchemasNode = XMLHandler.getSubNode( infonode, XML_TAG_CLUSTERSCHEMAS );
        int nrClusterSchemas = XMLHandler.countNodes( clusterSchemasNode, ClusterSchema.XML_TAG );
        for ( int i = 0; i < nrClusterSchemas; i++ ) {
          Node clusterSchemaNode = XMLHandler.getSubNodeByNr( clusterSchemasNode, ClusterSchema.XML_TAG, i );
          ClusterSchema clusterSchema = new ClusterSchema( clusterSchemaNode, slaveServers );
          clusterSchema.shareVariablesWith( this );

          // Check if the object exists and if it's a shared object.
          // If so, then we will keep the shared version, not this one.
          // The stored XML is only for backup purposes.
          ClusterSchema check = findClusterSchema( clusterSchema.getName() );
          if ( check != null ) {
            if ( !check.isShared() ) {
              // we don't overwrite shared objects.
              if ( shouldOverwrite( prompter, props,
                  BaseMessages.getString( PKG, "TransMeta.Message.OverwriteClusterSchemaYN", clusterSchema.getName() ),
                  BaseMessages.getString( PKG, "TransMeta.Message.OverwriteConnection.DontShowAnyMoreMessage" ) ) ) {
                addOrReplaceClusterSchema( clusterSchema );
              }
            }
          } else {
            clusterSchemas.add( clusterSchema );
          }
        }

        // Have all step clustering schema meta-data reference the correct cluster schemas that we just loaded
        //
        for ( int i = 0; i < nrSteps(); i++ ) {
          getStep( i ).setClusterSchemaAfterLoading( clusterSchemas );
        }

        String srowset = XMLHandler.getTagValue( infonode, "size_rowset" );
        sizeRowset = Const.toInt( srowset, Const.ROWS_IN_ROWSET );
        sleepTimeEmpty =
            Const.toInt( XMLHandler.getTagValue( infonode, "sleep_time_empty" ), Const.TIMEOUT_GET_MILLIS );
        sleepTimeFull = Const.toInt( XMLHandler.getTagValue( infonode, "sleep_time_full" ), Const.TIMEOUT_PUT_MILLIS );
        usingUniqueConnections = "Y".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "unique_connections" ) );

        feedbackShown = !"N".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "feedback_shown" ) );
        feedbackSize = Const.toInt( XMLHandler.getTagValue( infonode, "feedback_size" ), Const.ROWS_UPDATE );
        usingThreadPriorityManagment =
            !"N".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "using_thread_priorities" ) );

        // Performance monitoring for steps...
        //
        capturingStepPerformanceSnapShots =
            "Y".equalsIgnoreCase( XMLHandler.getTagValue( infonode, "capture_step_performance" ) );
        stepPerformanceCapturingDelay =
            Const.toLong( XMLHandler.getTagValue( infonode, "step_performance_capturing_delay" ), 1000 );
        stepPerformanceCapturingSizeLimit = XMLHandler.getTagValue( infonode, "step_performance_capturing_size_limit" );

        // Created user/date
        createdUser = XMLHandler.getTagValue( infonode, "created_user" );
        String createDate = XMLHandler.getTagValue( infonode, "created_date" );
        if ( createDate != null ) {
          createdDate = XMLHandler.stringToDate( createDate );
        }

        // Changed user/date
        modifiedUser = XMLHandler.getTagValue( infonode, "modified_user" );
        String modDate = XMLHandler.getTagValue( infonode, "modified_date" );
        if ( modDate != null ) {
          modifiedDate = XMLHandler.stringToDate( modDate );
        }

        Node partitionDistNode = XMLHandler.getSubNode( transnode, SlaveStepCopyPartitionDistribution.XML_TAG );
        if ( partitionDistNode != null ) {
          slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution( partitionDistNode );
        } else {
          slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution(); // leave empty
        }

        // Is this a slave transformation?
        //
        slaveTransformation = "Y".equalsIgnoreCase( XMLHandler.getTagValue( transnode, "slave_transformation" ) );
        if ( log.isDebug() ) {
          log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.NumberOfStepsReaded" ) + nrSteps() );
          log.logDebug( BaseMessages.getString( PKG, "TransMeta.Log.NumberOfHopsReaded" ) + nrTransHops() );
        }
        sortSteps();

        // Load the attribute groups map
        //
        attributesMap = AttributesUtil.loadAttributes( XMLHandler.getSubNode( transnode, AttributesUtil.XML_TAG ) );

        keyForSessionKey = XMLHandler.stringToBinary( XMLHandler.getTagValue( infonode, "key_for_session_key" ) );
        isKeyPrivate = "Y".equals( XMLHandler.getTagValue( infonode, "is_key_private" ) );

      } catch ( KettleXMLException xe ) {
        throw new KettleXMLException( BaseMessages.getString( PKG, "TransMeta.Exception.ErrorReadingTransformation" ),
            xe );
      } catch ( KettleException e ) {
        throw new KettleXMLException( e );
      } finally {
        initializeVariablesFrom( null );
        if ( setInternalVariables ) {
          setInternalKettleVariables();
        }

        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationMetaLoaded.id, this );
      }
    } catch ( Exception e ) {
      // See if we have missing plugins to report, those take precedence!
      //
      if ( !missingPluginsException.getMissingPluginDetailsList().isEmpty() ) {
        throw missingPluginsException;
      } else {
        throw new KettleXMLException( BaseMessages.getString( PKG, "TransMeta.Exception.ErrorReadingTransformation" ),
            e );
      }
    } finally {
      if ( !missingPluginsException.getMissingPluginDetailsList().isEmpty() ) {
        throw missingPluginsException;
      }
    }
  }

  public byte[] getKey() {
    return keyForSessionKey;
  }

  public void setKey( byte[] key ) {
    this.keyForSessionKey = key;
  }

  public boolean isPrivateKey() {
    return isKeyPrivate;
  }

  public void setPrivateKey( boolean privateKey ) {
    this.isKeyPrivate = privateKey;
  }

  @Override
  public boolean loadSharedObject( SharedObjectInterface object ) {
    if ( !super.loadSharedObject( object ) ) {
      if ( object instanceof StepMeta ) {
        StepMeta stepMeta = (StepMeta) object;
        addOrReplaceStep( stepMeta );
      } else if ( object instanceof PartitionSchema ) {
        PartitionSchema partitionSchema = (PartitionSchema) object;
        addOrReplacePartitionSchema( partitionSchema );
      } else if ( object instanceof ClusterSchema ) {
        ClusterSchema clusterSchema = (ClusterSchema) object;
        clusterSchema.shareVariablesWith( this );
        addOrReplaceClusterSchema( clusterSchema );
      } else {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets a List of all the steps that are used in at least one active hop. These steps will be used to execute the
   * transformation. The others will not be executed.<br/>
   * Update 3.0 : we also add those steps that are not linked to another hop, but have at least one remote input or
   * output step defined.
   *
   * @param all
   *          true if you want to get ALL the steps from the transformation, false otherwise
   * @return A List of steps
   */
  public List<StepMeta> getTransHopSteps( boolean all ) {
    List<StepMeta> st = new ArrayList<>();
    int idx;

    for ( int x = 0; x < nrTransHops(); x++ ) {
      TransHopMeta hi = getTransHop( x );
      if ( hi.isEnabled() || all ) {
        idx = st.indexOf( hi.getFromStep() ); // FROM
        if ( idx < 0 ) {
          st.add( hi.getFromStep() );
        }

        idx = st.indexOf( hi.getToStep() ); // TO
        if ( idx < 0 ) {
          st.add( hi.getToStep() );
        }
      }
    }

    // Also, add the steps that need to be painted, but are not part of a hop
    for ( int x = 0; x < nrSteps(); x++ ) {
      StepMeta stepMeta = getStep( x );
      if ( stepMeta.isDrawn() && !isStepUsedInTransHops( stepMeta ) ) {
        st.add( stepMeta );
      }
      if ( !stepMeta.getRemoteInputSteps().isEmpty() || !stepMeta.getRemoteOutputSteps().isEmpty() ) {
        if ( !st.contains( stepMeta ) ) {
          st.add( stepMeta );
        }
      }
    }

    return st;
  }

  /**
   * Checks if a step has been used in a hop or not.
   *
   * @param stepMeta
   *          The step queried.
   * @return true if a step is used in a hop (active or not), false otherwise
   */
  public boolean isStepUsedInTransHops( StepMeta stepMeta ) {
    TransHopMeta fr = findTransHopFrom( stepMeta );
    TransHopMeta to = findTransHopTo( stepMeta );
    return fr != null || to != null;
  }

  /**
   * Checks if any selected step has been used in a hop or not.
   *
   * @return true if a step is used in a hop (active or not), false otherwise
   */
  public boolean isAnySelectedStepUsedInTransHops() {
    List<StepMeta> selectedSteps = getSelectedSteps();
    int i = 0;
    while ( i < selectedSteps.size() ) {
      StepMeta stepMeta = selectedSteps.get( i );
      if ( isStepUsedInTransHops( stepMeta ) ) {
        return true;
      }
      i++;
    }
    return false;
  }

  /**
   * Clears the different changed flags of the transformation.
   *
   */
  @Override
  public void clearChanged() {
    changed_steps = false;
    changed_hops = false;

    for ( int i = 0; i < nrSteps(); i++ ) {
      getStep( i ).setChanged( false );
      if ( getStep( i ).getStepPartitioningMeta() != null ) {
        getStep( i ).getStepPartitioningMeta().hasChanged( false );
      }
    }
    for ( int i = 0; i < nrTransHops(); i++ ) {
      getTransHop( i ).setChanged( false );
    }
    for ( int i = 0; i < partitionSchemas.size(); i++ ) {
      partitionSchemas.get( i ).setChanged( false );
    }
    for ( int i = 0; i < clusterSchemas.size(); i++ ) {
      clusterSchemas.get( i ).setChanged( false );
    }

    super.clearChanged();
  }

  /**
   * Checks whether or not the steps have changed.
   *
   * @return true if the steps have been changed, false otherwise
   */
  public boolean haveStepsChanged() {
    if ( changed_steps ) {
      return true;
    }

    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      if ( stepMeta.hasChanged() ) {
        return true;
      }
      if ( stepMeta.getStepPartitioningMeta() != null && stepMeta.getStepPartitioningMeta().hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not any of the hops have been changed.
   *
   * @return true if a hop has been changed, false otherwise
   */
  public boolean haveHopsChanged() {
    if ( changed_hops ) {
      return true;
    }

    for ( int i = 0; i < nrTransHops(); i++ ) {
      TransHopMeta hi = getTransHop( i );
      if ( hi.hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether or not any of the partitioning schemas have been changed.
   *
   * @return true if the partitioning schemas have been changed, false otherwise
   */
  public boolean havePartitionSchemasChanged() {
    for ( int i = 0; i < partitionSchemas.size(); i++ ) {
      PartitionSchema ps = partitionSchemas.get( i );
      if ( ps.hasChanged() ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether or not any of the clustering schemas have been changed.
   *
   * @return true if the clustering schemas have been changed, false otherwise
   */
  public boolean haveClusterSchemasChanged() {
    for ( int i = 0; i < clusterSchemas.size(); i++ ) {
      ClusterSchema cs = clusterSchemas.get( i );
      if ( cs.hasChanged() ) {
        return true;
      }
    }

    return false;
  }

  /**
   * Checks whether or not the transformation has changed.
   *
   * @return true if the transformation has changed, false otherwise
   */
  @Override
  public boolean hasChanged() {
    if ( super.hasChanged() ) {
      return true;
    }
    if ( haveStepsChanged() ) {
      return true;
    }
    if ( haveHopsChanged() ) {
      return true;
    }
    if ( havePartitionSchemasChanged() ) {
      return true;
    }
    return haveClusterSchemasChanged();

  }

  private boolean isErrorNode( Node errorHandingNode, Node checkNode ) {
    if ( errorHandingNode != null ) {
      NodeList errors = errorHandingNode.getChildNodes();

      Node nodeHopFrom = XMLHandler.getSubNode( checkNode, TransHopMeta.XML_FROM_TAG );
      Node nodeHopTo = XMLHandler.getSubNode( checkNode, TransHopMeta.XML_TO_TAG );

      int i = 0;
      while ( i < errors.getLength() ) {

        Node errorNode = errors.item( i );

        if ( !StepErrorMeta.XML_ERROR_TAG.equals( errorNode.getNodeName() ) ) {
          i++;
          continue;
        }

        Node errorSourceNode = XMLHandler.getSubNode( errorNode, StepErrorMeta.XML_SOURCE_STEP_TAG );
        Node errorTagetNode = XMLHandler.getSubNode( errorNode, StepErrorMeta.XML_TARGET_STEP_TAG );

        String sourceContent = errorSourceNode.getTextContent().trim();
        String tagetContent = errorTagetNode.getTextContent().trim();

        if ( sourceContent.equals( nodeHopFrom.getTextContent().trim() )
            && tagetContent.equals( nodeHopTo.getTextContent().trim() ) ) {
          return true;
        }
        i++;
      }
    }
    return false;
  }

  /**
   * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all the
   * previous steps. If you keep going backward and find the step, there is a loop. Both the informational and the
   * normal steps need to be checked for loops!
   *
   * @param stepMeta
   *          The step position to start looking
   *
   * @return true if a loop has been found, false if no loop is found.
   */
  public boolean hasLoop( StepMeta stepMeta ) {
    clearLoopCache();
    return hasLoop( stepMeta, null );
  }

  /**
   * @deprecated use {@link #hasLoop(StepMeta, StepMeta)}}
   */
  @Deprecated
  public boolean hasLoop( StepMeta stepMeta, StepMeta lookup, boolean info ) {
    return hasLoop( stepMeta, lookup, new HashSet<StepMeta>() );
  }

  /**
   * Checks for loop.
   *
   * @param stepMeta  the stepmeta
   * @param lookup the lookup
   * @return true, if successful
   */

  public boolean hasLoop( StepMeta stepMeta, StepMeta lookup ) {
    return hasLoop( stepMeta, lookup, new HashSet<StepMeta>() );
  }

  /**
   * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all the
   * previous steps. If you keep going backward and find the original step again, there is a loop.
   *
   * @param stepMeta
   *          The step position to start looking
   * @param lookup
   *          The original step when wandering around the transformation.
   * @param checkedEntries
   *          Already checked entries
   *
   * @return true if a loop has been found, false if no loop is found.
   */
  private boolean hasLoop( StepMeta stepMeta, StepMeta lookup, HashSet<StepMeta> checkedEntries ) {
    String cacheKey =
            stepMeta.getName() + " - " + ( lookup != null ? lookup.getName() : "" );

    Boolean hasLoop = loopCache.get( cacheKey );

    if ( hasLoop != null ) {
      return hasLoop;
    }

    hasLoop = false;

    checkedEntries.add( stepMeta );

    List<StepMeta> prevSteps = findPreviousSteps( stepMeta, true );
    int nr = prevSteps.size();
    for ( int i = 0; i < nr; i++ ) {
      StepMeta prevStepMeta = prevSteps.get( i );
      if ( prevStepMeta != null && ( prevStepMeta.equals( lookup )
              || ( !checkedEntries.contains( prevStepMeta ) && hasLoop( prevStepMeta, lookup == null ? stepMeta : lookup, checkedEntries ) ) ) ) {
        hasLoop = true;
        break;
      }
    }

    loopCache.put( cacheKey, hasLoop );
    return hasLoop;
  }

  /**
   * Mark all steps in the transformation as selected.
   *
   */
  public void selectAll() {
    int i;
    for ( i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      stepMeta.setSelected( true );
    }
    for ( i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      ni.setSelected( true );
    }

    setChanged();
    notifyObservers( "refreshGraph" );
  }

  /**
   * Clear the selection of all steps.
   *
   */
  public void unselectAll() {
    int i;
    for ( i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      stepMeta.setSelected( false );
    }
    for ( i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      ni.setSelected( false );
    }
  }

  /**
   * Get an array of all the selected step locations.
   *
   * @return The selected step locations.
   */
  public Point[] getSelectedStepLocations() {
    List<Point> points = new ArrayList<>();

    for ( StepMeta stepMeta : getSelectedSteps() ) {
      Point p = stepMeta.getLocation();
      points.add( new Point( p.x, p.y ) ); // explicit copy of location
    }

    return points.toArray( new Point[points.size()] );
  }

  /**
   * Get an array of all the selected note locations.
   *
   * @return The selected note locations.
   */
  public Point[] getSelectedNoteLocations() {
    List<Point> points = new ArrayList<>();

    for ( NotePadMeta ni : getSelectedNotes() ) {
      Point p = ni.getLocation();
      points.add( new Point( p.x, p.y ) ); // explicit copy of location
    }

    return points.toArray( new Point[points.size()] );
  }

  /**
   * Gets a list of the selected steps.
   *
   * @return A list of all the selected steps.
   */
  public List<StepMeta> getSelectedSteps() {
    List<StepMeta> selection = new ArrayList<>();
    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.isSelected() ) {
        selection.add( stepMeta );
      }

    }
    return selection;
  }

  /**
   * Gets an array of all the selected step names.
   *
   * @return An array of all the selected step names.
   */
  public String[] getSelectedStepNames() {
    List<StepMeta> selection = getSelectedSteps();
    String[] retval = new String[selection.size()];
    for ( int i = 0; i < retval.length; i++ ) {
      StepMeta stepMeta = selection.get( i );
      retval[i] = stepMeta.getName();
    }
    return retval;
  }

  /**
   * Gets an array of the locations of an array of steps.
   *
   * @param steps
   *          An array of steps
   * @return an array of the locations of an array of steps
   */
  public int[] getStepIndexes( List<StepMeta> steps ) {
    int[] retval = new int[steps.size()];

    for ( int i = 0; i < steps.size(); i++ ) {
      retval[i] = indexOfStep( steps.get( i ) );
    }

    return retval;
  }

  /**
   * Gets the maximum size of the canvas by calculating the maximum location of a step.
   *
   * @return Maximum coordinate of a step in the transformation + (100,100) for safety.
   */
  public Point getMaximum() {
    int maxx = 0, maxy = 0;
    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      Point loc = stepMeta.getLocation();
      if ( loc.x > maxx ) {
        maxx = loc.x;
      }
      if ( loc.y > maxy ) {
        maxy = loc.y;
      }
    }
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta notePadMeta = getNote( i );
      Point loc = notePadMeta.getLocation();
      if ( loc.x + notePadMeta.width > maxx ) {
        maxx = loc.x + notePadMeta.width;
      }
      if ( loc.y + notePadMeta.height > maxy ) {
        maxy = loc.y + notePadMeta.height;
      }
    }

    return new Point( maxx + 100, maxy + 100 );
  }

  /**
   * Gets the minimum point on the canvas of a transformation.
   *
   * @return Minimum coordinate of a step in the transformation
   */
  public Point getMinimum() {
    int minx = Integer.MAX_VALUE, miny = Integer.MAX_VALUE;
    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      Point loc = stepMeta.getLocation();
      if ( loc.x < minx ) {
        minx = loc.x;
      }
      if ( loc.y < miny ) {
        miny = loc.y;
      }
    }
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta notePadMeta = getNote( i );
      Point loc = notePadMeta.getLocation();
      if ( loc.x < minx ) {
        minx = loc.x;
      }
      if ( loc.y < miny ) {
        miny = loc.y;
      }
    }

    if ( minx > BORDER_INDENT && minx != Integer.MAX_VALUE ) {
      minx -= BORDER_INDENT;
    } else {
      minx = 0;
    }
    if ( miny > BORDER_INDENT && miny != Integer.MAX_VALUE ) {
      miny -= BORDER_INDENT;
    } else {
      miny = 0;
    }

    return new Point( minx, miny );
  }

  /**
   * Gets the names of all the steps.
   *
   * @return An array of step names.
   */
  public String[] getStepNames() {
    String[] retval = new String[nrSteps()];

    for ( int i = 0; i < nrSteps(); i++ ) {
      retval[i] = getStep( i ).getName();
    }

    return retval;
  }

  /**
   * Gets all the steps as an array.
   *
   * @return An array of all the steps in the transformation.
   */
  public StepMeta[] getStepsArray() {
    StepMeta[] retval = new StepMeta[nrSteps()];

    for ( int i = 0; i < nrSteps(); i++ ) {
      retval[i] = getStep( i );
    }

    return retval;
  }

  /**
   * Looks in the transformation to find a step in a previous location starting somewhere.
   *
   * @param startStep
   *          The starting step
   * @param stepToFind
   *          The step to look for backward in the transformation
   * @return true if we can find the step in an earlier location in the transformation.
   */
  public boolean findPrevious( StepMeta startStep, StepMeta stepToFind ) {
    String key = startStep.getName() + " - " + stepToFind.getName();
    Boolean result = loopCache.get( key );
    if ( result != null ) {
      return result;
    }

    // Normal steps
    //
    List<StepMeta> previousSteps = findPreviousSteps( startStep, false );
    for ( int i = 0; i < previousSteps.size(); i++ ) {
      StepMeta stepMeta = previousSteps.get( i );
      if ( stepMeta.equals( stepToFind ) ) {
        loopCache.put( key, true );
        return true;
      }

      boolean found = findPrevious( stepMeta, stepToFind ); // Look further back in the tree.
      if ( found ) {
        loopCache.put( key, true );
        return true;
      }
    }

    // Info steps
    List<StepMeta> infoSteps = findPreviousSteps( startStep, true );
    for ( int i = 0; i < infoSteps.size(); i++ ) {
      StepMeta stepMeta = infoSteps.get( i );
      if ( stepMeta.equals( stepToFind ) ) {
        loopCache.put( key, true );
        return true;
      }

      boolean found = findPrevious( stepMeta, stepToFind ); // Look further back in the tree.
      if ( found ) {
        loopCache.put( key, true );
        return true;
      }
    }

    loopCache.put( key, false );
    return false;
  }

  /**
   * Puts the steps in alphabetical order.
   */
  public void sortSteps() {
    try {
      Collections.sort( steps );
    } catch ( Exception e ) {
      log.logError( BaseMessages.getString( PKG, "TransMeta.Exception.ErrorOfSortingSteps" ) + e );
      log.logError( Const.getStackTracker( e ) );
    }
  }

  /**
   * Sorts all the hops in the transformation.
   */
  public void sortHops() {
    Collections.sort( hops );
  }

  /** The previous count. */
  private long prevCount;

  /**
   * Puts the steps in a more natural order: from start to finish. For the moment, we ignore splits and joins. Splits
   * and joins can't be listed sequentially in any case!
   *
   * @return a map containing all the previous steps per step
   */
  public Map<StepMeta, Map<StepMeta, Boolean>> sortStepsNatural() {
    long startTime = System.currentTimeMillis();

    prevCount = 0;

    // First create a map where all the previous steps of another step are kept...
    //
    final Map<StepMeta, Map<StepMeta, Boolean>> stepMap = new HashMap<>();

    // Also cache the previous steps
    //
    final Map<StepMeta, List<StepMeta>> previousCache = new HashMap<>();

    // Cache calculation of steps before another
    //
    Map<StepMeta, Map<StepMeta, Boolean>> beforeCache = new HashMap<>();

    for ( StepMeta stepMeta : steps ) {
      // What are the previous steps? (cached version for performance)
      //
      List<StepMeta> prevSteps = previousCache.get( stepMeta );
      if ( prevSteps == null ) {
        prevSteps = findPreviousSteps( stepMeta );
        prevCount++;
        previousCache.put( stepMeta, prevSteps );
      }

      // Now get the previous steps recursively, store them in the step map
      //
      for ( StepMeta prev : prevSteps ) {
        Map<StepMeta, Boolean> beforePrevMap = updateFillStepMap( previousCache, beforeCache, stepMeta, prev );
        stepMap.put( stepMeta, beforePrevMap );

        // Store it also in the beforeCache...
        //
        beforeCache.put( prev, beforePrevMap );
      }
    }

    Collections.sort( steps, new Comparator<StepMeta>() {

      @Override
      public int compare( StepMeta o1, StepMeta o2 ) {

        Map<StepMeta, Boolean> beforeMap = stepMap.get( o1 );
        if ( beforeMap != null ) {
          if ( beforeMap.get( o2 ) == null ) {
            return -1;
          } else {
            return 1;
          }
        } else {
          return o1.getName().compareToIgnoreCase( o2.getName() );
        }
      }
    } );

    long endTime = System.currentTimeMillis();
    log.logBasic(
        BaseMessages.getString( PKG, "TransMeta.Log.TimeExecutionStepSort", ( endTime - startTime ), prevCount ) );

    return stepMap;
  }

  /**
   * Fills a map with all steps previous to the given step. This method uses a caching technique, so if a map is
   * provided that contains the specified previous step, it is immediately returned to avoid unnecessary processing.
   * Otherwise, the previous steps are determined and added to the map recursively, and a cache is constructed for later
   * use.
   *
   * @param previousCache
   *          the previous cache, must be non-null
   * @param beforeCache
   *          the before cache, must be non-null
   * @param originStepMeta
   *          the origin step meta
   * @param previousStepMeta
   *          the previous step meta
   * @return the map
   */
  private Map<StepMeta, Boolean> updateFillStepMap( Map<StepMeta, List<StepMeta>> previousCache,
      Map<StepMeta, Map<StepMeta, Boolean>> beforeCache, StepMeta originStepMeta, StepMeta previousStepMeta ) {

    // See if we have a hash map to store step occurrence (located before the step)
    //
    Map<StepMeta, Boolean> beforeMap = beforeCache.get( previousStepMeta );
    if ( beforeMap == null ) {
      beforeMap = new HashMap<>();
    } else {
      return beforeMap; // Nothing left to do here!
    }

    // Store the current previous step in the map
    //
    beforeMap.put( previousStepMeta, Boolean.TRUE );

    // Figure out all the previous steps as well, they all need to go in there...
    //
    List<StepMeta> prevSteps = previousCache.get( previousStepMeta );
    if ( prevSteps == null ) {
      prevSteps = findPreviousSteps( previousStepMeta );
      prevCount++;
      previousCache.put( previousStepMeta, prevSteps );
    }

    // Now, get the previous steps for stepMeta recursively...
    // We only do this when the beforeMap is not known yet...
    //
    for ( StepMeta prev : prevSteps ) {
      Map<StepMeta, Boolean> beforePrevMap = updateFillStepMap( previousCache, beforeCache, originStepMeta, prev );

      // Keep a copy in the cache...
      //
      beforeCache.put( prev, beforePrevMap );

      // Also add it to the new map for this step...
      //
      beforeMap.putAll( beforePrevMap );
    }

    return beforeMap;
  }

  /**
   * Sorts the hops in a natural way: from beginning to end.
   */
  public void sortHopsNatural() {
    // Loop over the hops...
    for ( int j = 0; j < nrTransHops(); j++ ) {
      // Buble sort: we need to do this several times...
      for ( int i = 0; i < nrTransHops() - 1; i++ ) {
        TransHopMeta one = getTransHop( i );
        TransHopMeta two = getTransHop( i + 1 );

        StepMeta a = two.getFromStep();
        StepMeta b = one.getToStep();

        if ( !findPrevious( a, b ) && !a.equals( b ) ) {
          setTransHop( i + 1, one );
          setTransHop( i, two );
        }
      }
    }
  }

  /**
   * Determines the impact of the different steps in a transformation on databases, tables and field.
   *
   * @param impact
   *          An ArrayList of DatabaseImpact objects.
   * @param monitor
   *          a progress monitor listener to be updated as the transformation is analyzed
   * @throws KettleStepException
   *           if any errors occur during analysis
   */
  public void analyseImpact( List<DatabaseImpact> impact, ProgressMonitorListener monitor ) throws KettleStepException {
    if ( monitor != null ) {
      monitor
        .beginTask( BaseMessages.getString( PKG, "TransMeta.Monitor.DeterminingImpactTask.Title" ), nrSteps() );
    }
    boolean stop = false;
    for ( int i = 0; i < nrSteps() && !stop; i++ ) {
      if ( monitor != null ) {
        monitor.subTask(
            BaseMessages.getString( PKG, "TransMeta.Monitor.LookingAtStepTask.Title" ) + ( i + 1 ) + "/" + nrSteps() );
      }
      StepMeta stepMeta = getStep( i );

      RowMetaInterface prev = getPrevStepFields( stepMeta );
      StepMetaInterface stepint = stepMeta.getStepMetaInterface();
      RowMetaInterface inform = null;
      StepMeta[] lu = getInfoStep( stepMeta );
      if ( lu != null ) {
        inform = getStepFields( lu );
      } else {
        inform = stepint.getTableFields();
      }

      compatibleAnalyseImpactStep( impact, stepint, this, stepMeta, prev, inform );
      stepint.analyseImpact( impact, this, stepMeta, prev, null, null, inform, repository, metaStore );

      if ( monitor != null ) {
        monitor.worked( 1 );
        stop = monitor.isCanceled();
      }
    }

    if ( monitor != null ) {
      monitor.done();
    }
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleAnalyseImpactStep( List<DatabaseImpact> impact, StepMetaInterface stepint, TransMeta transMeta,
      StepMeta stepMeta, RowMetaInterface prev, RowMetaInterface inform ) throws KettleStepException {
    stepint.analyseImpact( impact, transMeta, stepMeta, prev, null, null, inform );
  }

  /**
   * Proposes an alternative stepname when the original already exists.
   *
   * @param stepname
   *          The stepname to find an alternative for
   * @return The suggested alternative stepname.
   */
  public String getAlternativeStepname( String stepname ) {
    String newname = stepname;
    StepMeta stepMeta = findStep( newname );
    int nr = 1;
    while ( stepMeta != null ) {
      nr++;
      newname = stepname + " " + nr;
      stepMeta = findStep( newname );
    }

    return newname;
  }

  /**
   * Builds a list of all the SQL statements that this transformation needs in order to work properly.
   *
   * @return An ArrayList of SQLStatement objects.
   * @throws KettleStepException
   *           if any errors occur during SQL statement generation
   */
  public List<SQLStatement> getSQLStatements() throws KettleStepException {
    return getSQLStatements( null );
  }

  /**
   * Builds a list of all the SQL statements that this transformation needs in order to work properly.
   *
   * @param monitor
   *          a progress monitor listener to be updated as the SQL statements are generated
   * @return An ArrayList of SQLStatement objects.
   * @throws KettleStepException
   *           if any errors occur during SQL statement generation
   */
  public List<SQLStatement> getSQLStatements( ProgressMonitorListener monitor ) throws KettleStepException {
    if ( monitor != null ) {
      monitor.beginTask( BaseMessages.getString( PKG, "TransMeta.Monitor.GettingTheSQLForTransformationTask.Title" ), nrSteps() + 1 );
    }
    List<SQLStatement> stats = new ArrayList<>();

    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      if ( monitor != null ) {
        monitor.subTask(
            BaseMessages.getString( PKG, "TransMeta.Monitor.GettingTheSQLForStepTask.Title", "" + stepMeta ) );
      }
      RowMetaInterface prev = getPrevStepFields( stepMeta );
      SQLStatement sqlCompat = compatibleStepMetaGetSQLStatements( stepMeta.getStepMetaInterface(), stepMeta, prev );
      if ( sqlCompat.getSQL() != null || sqlCompat.hasError() ) {
        stats.add( sqlCompat );
      }
      SQLStatement
          sql =
          stepMeta.getStepMetaInterface().getSQLStatements( this, stepMeta, prev, repository, metaStore );
      if ( sql.getSQL() != null || sql.hasError() ) {
        stats.add( sql );
      }
      if ( monitor != null ) {
        monitor.worked( 1 );
      }
    }

    // Also check the sql for the logtable...
    //
    if ( monitor != null ) {
      monitor.subTask( BaseMessages.getString( PKG, "TransMeta.Monitor.GettingTheSQLForTransformationTask.Title2" ) );
    }
    if ( transLogTable.getDatabaseMeta() != null && ( !Utils.isEmpty( transLogTable.getTableName() ) || !Utils
        .isEmpty( performanceLogTable.getTableName() ) ) ) {
      try {
        for ( LogTableInterface logTable : new LogTableInterface[] { transLogTable, performanceLogTable,
          channelLogTable, stepLogTable, } ) {
          if ( logTable.getDatabaseMeta() != null && !Utils.isEmpty( logTable.getTableName() ) ) {

            Database db = null;
            try {
              db = new Database( this, transLogTable.getDatabaseMeta() );
              db.shareVariablesWith( this );
              db.connect();

              RowMetaInterface fields = logTable.getLogRecord( LogStatus.START, null, null ).getRowMeta();
              String
                  schemaTable =
                  logTable.getDatabaseMeta()
                      .getQuotedSchemaTableCombination( logTable.getSchemaName(), logTable.getTableName() );
              String sql = db.getDDL( schemaTable, fields );
              if ( !Utils.isEmpty( sql ) ) {
                SQLStatement stat = new SQLStatement( "<this transformation>", transLogTable.getDatabaseMeta(), sql );
                stats.add( stat );
              }
            } catch ( Exception e ) {
              throw new KettleDatabaseException(
                  "Unable to connect to logging database [" + logTable.getDatabaseMeta() + "]", e );
            } finally {
              if ( db != null ) {
                db.disconnect();
              }
            }
          }
        }
      } catch ( KettleDatabaseException dbe ) {
        SQLStatement stat = new SQLStatement( "<this transformation>", transLogTable.getDatabaseMeta(), null );
        stat.setError(
            BaseMessages.getString( PKG, "TransMeta.SQLStatement.ErrorDesc.ErrorObtainingTransformationLogTableInfo" )
                + dbe.getMessage() );
        stats.add( stat );
      }
    }
    if ( monitor != null ) {
      monitor.worked( 1 );
    }
    if ( monitor != null ) {
      monitor.done();
    }

    return stats;
  }

  @SuppressWarnings( "deprecation" )
  private SQLStatement compatibleStepMetaGetSQLStatements( StepMetaInterface stepMetaInterface, StepMeta stepMeta,
      RowMetaInterface prev ) throws KettleStepException {
    return stepMetaInterface.getSQLStatements( this, stepMeta, prev );
  }

  /**
   * Get the SQL statements (needed to run this transformation) as a single String.
   *
   * @return the SQL statements needed to run this transformation
   * @throws KettleStepException
   *           if any errors occur during SQL statement generation
   */
  public String getSQLStatementsString() throws KettleStepException {
    String sql = "";
    List<SQLStatement> stats = getSQLStatements();
    for ( int i = 0; i < stats.size(); i++ ) {
      SQLStatement stat = stats.get( i );
      if ( !stat.hasError() && stat.hasSQL() ) {
        sql += stat.getSQL();
      }
    }

    return sql;
  }

  /**
   * Checks all the steps and fills a List of (CheckResult) remarks.
   *
   * @param remarks
   *          The remarks list to add to.
   * @param only_selected
   *          true to check only the selected steps, false for all steps
   * @param monitor
   *          a progress monitor listener to be updated as the SQL statements are generated
   */
  @Deprecated
  public void checkSteps( List<CheckResultInterface> remarks, boolean only_selected, ProgressMonitorListener monitor ) {
    checkSteps( remarks, only_selected, monitor, this, null, null );
  }

  /**
   * Checks all the steps and fills a List of (CheckResult) remarks.
   *
   * @param remarks
   *          The remarks list to add to.
   * @param only_selected
   *          true to check only the selected steps, false for all steps
   * @param monitor
   *          a progress monitor listener to be updated as the SQL statements are generated
   */
  public void checkSteps( List<CheckResultInterface> remarks, boolean only_selected, ProgressMonitorListener monitor,
      VariableSpace space, Repository repository, IMetaStore metaStore ) {
    try {
      remarks.clear(); // Start with a clean slate...

      Map<ValueMetaInterface, String> values = new Hashtable<>();
      String[] stepnames;
      StepMeta[] steps;
      List<StepMeta> selectedSteps = getSelectedSteps();
      if ( !only_selected || selectedSteps.isEmpty() ) {
        stepnames = getStepNames();
        steps = getStepsArray();
      } else {
        stepnames = getSelectedStepNames();
        steps = selectedSteps.toArray( new StepMeta[selectedSteps.size()] );
      }

      ExtensionPointHandler.callExtensionPoint( getLogChannel(), KettleExtensionPoint.BeforeCheckSteps.id,
          new CheckStepsExtension( remarks, space, this, steps, repository, metaStore ) );

      boolean stop_checking = false;

      if ( monitor != null ) {
        monitor.beginTask( BaseMessages.getString( PKG, "TransMeta.Monitor.VerifyingThisTransformationTask.Title" ),
            steps.length + 2 );
      }

      for ( int i = 0; i < steps.length && !stop_checking; i++ ) {
        if ( monitor != null ) {
          monitor.subTask( BaseMessages.getString( PKG, "TransMeta.Monitor.VerifyingStepTask.Title", stepnames[i] ) );
        }

        StepMeta stepMeta = steps[i];

        int nrinfo = findNrInfoSteps( stepMeta );
        StepMeta[] infostep = null;
        if ( nrinfo > 0 ) {
          infostep = getInfoStep( stepMeta );
        }

        RowMetaInterface info = null;
        if ( infostep != null ) {
          try {
            info = getStepFields( infostep );
          } catch ( KettleStepException kse ) {
            info = null;
            CheckResult
                cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                    "TransMeta.CheckResult.TypeResultError.ErrorOccurredGettingStepInfoFields.Description",
                    "" + stepMeta, Const.CR + kse.getMessage() ), stepMeta );
            remarks.add( cr );
          }
        }

        // The previous fields from non-informative steps:
        RowMetaInterface prev = null;
        try {
          prev = getPrevStepFields( stepMeta );
        } catch ( KettleStepException kse ) {
          CheckResult
              cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
                  .getString( PKG, "TransMeta.CheckResult.TypeResultError.ErrorOccurredGettingInputFields.Description",
                      "" + stepMeta, Const.CR + kse.getMessage() ), stepMeta );
          remarks.add( cr );
          // This is a severe error: stop checking...
          // Otherwise we wind up checking time & time again because nothing gets put in the database
          // cache, the timeout of certain databases is very long... (Oracle)
          stop_checking = true;
        }

        if ( isStepUsedInTransHops( stepMeta ) || getSteps().size() == 1 ) {
          // Get the input & output steps!
          // Copy to arrays:
          String[] input = getPrevStepNames( stepMeta );
          String[] output = getNextStepNames( stepMeta );

          // Check step specific info...
          ExtensionPointHandler.callExtensionPoint( getLogChannel(), KettleExtensionPoint.BeforeCheckStep.id,
              new CheckStepsExtension( remarks, space, this, new StepMeta[] { stepMeta }, repository, metaStore ) );
          stepMeta.check( remarks, this, prev, input, output, info, space, repository, metaStore );
          ExtensionPointHandler.callExtensionPoint( getLogChannel(), KettleExtensionPoint.AfterCheckStep.id,
              new CheckStepsExtension( remarks, space, this, new StepMeta[] { stepMeta }, repository, metaStore ) );

          // See if illegal characters etc. were used in field-names...
          if ( prev != null ) {
            for ( int x = 0; x < prev.size(); x++ ) {
              ValueMetaInterface v = prev.getValueMeta( x );
              String name = v.getName();
              if ( name == null ) {
                values.put( v,
                    BaseMessages.getString( PKG, "TransMeta.Value.CheckingFieldName.FieldNameIsEmpty.Description" ) );
              } else if ( name.indexOf( ' ' ) >= 0 ) {
                values.put( v, BaseMessages
                    .getString( PKG, "TransMeta.Value.CheckingFieldName.FieldNameContainsSpaces.Description" ) );
              } else {
                char[] list =
                  new char[] { '.', ',', '-', '/', '+', '*', '\'', '\t', '"', '|', '@', '(', ')', '{', '}', '!',
                    '^' };
                for ( int c = 0; c < list.length; c++ ) {
                  if ( name.indexOf( list[c] ) >= 0 ) {
                    values.put( v, BaseMessages.getString( PKG,
                        "TransMeta.Value.CheckingFieldName.FieldNameContainsUnfriendlyCodes.Description",
                        String.valueOf( list[c] ) ) );
                  }
                }
              }
            }

            // Check if 2 steps with the same name are entering the step...
            if ( prev.size() > 1 ) {
              String[] fieldNames = prev.getFieldNames();
              String[] sortedNames = Const.sortStrings( fieldNames );

              String prevName = sortedNames[0];
              for ( int x = 1; x < sortedNames.length; x++ ) {
                // Checking for doubles
                if ( prevName.equalsIgnoreCase( sortedNames[x] ) ) {
                  // Give a warning!!
                  CheckResult
                      cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
                          .getString( PKG, "TransMeta.CheckResult.TypeResultWarning.HaveTheSameNameField.Description",
                              prevName ), stepMeta );
                  remarks.add( cr );
                } else {
                  prevName = sortedNames[x];
                }
              }
            }
          } else {
            CheckResult
                cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
                    .getString( PKG, "TransMeta.CheckResult.TypeResultError.CannotFindPreviousFields.Description" )
                    + stepMeta.getName(), stepMeta );
            remarks.add( cr );
          }
        } else {
          CheckResult
              cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING,
                  BaseMessages.getString( PKG, "TransMeta.CheckResult.TypeResultWarning.StepIsNotUsed.Description" ),
                  stepMeta );
          remarks.add( cr );
        }

        // Also check for mixing rows...
        try {
          checkRowMixingStatically( stepMeta, null );
        } catch ( KettleRowException e ) {
          CheckResult cr = new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, e.getMessage(), stepMeta );
          remarks.add( cr );
        }

        if ( monitor != null ) {
          monitor.worked( 1 ); // progress bar...
          if ( monitor.isCanceled() ) {
            stop_checking = true;
          }
        }
      }

      // Also, check the logging table of the transformation...
      if ( monitor == null || !monitor.isCanceled() ) {
        if ( monitor != null ) {
          monitor.subTask( BaseMessages.getString( PKG, "TransMeta.Monitor.CheckingTheLoggingTableTask.Title" ) );
        }
        if ( transLogTable.getDatabaseMeta() != null ) {
          Database logdb = new Database( this, transLogTable.getDatabaseMeta() );
          logdb.shareVariablesWith( this );
          try {
            logdb.connect();
            CheckResult
                cr =
                new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
                    BaseMessages.getString( PKG, "TransMeta.CheckResult.TypeResultOK.ConnectingWorks.Description" ),
                    null );
            remarks.add( cr );

            if ( transLogTable.getTableName() != null ) {
              if ( logdb.checkTableExists( transLogTable.getSchemaName(), transLogTable.getTableName() ) ) {
                cr =
                    new CheckResult( CheckResultInterface.TYPE_RESULT_OK, BaseMessages
                        .getString( PKG, "TransMeta.CheckResult.TypeResultOK.LoggingTableExists.Description",
                            transLogTable.getTableName() ), null );
                remarks.add( cr );

                RowMetaInterface fields = transLogTable.getLogRecord( LogStatus.START, null, null ).getRowMeta();
                String sql = logdb.getDDL( transLogTable.getTableName(), fields );
                if ( sql == null || sql.length() == 0 ) {
                  cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
                          BaseMessages.getString( PKG, "TransMeta.CheckResult.TypeResultOK.CorrectLayout.Description" ),
                          null );
                  remarks.add( cr );
                } else {
                  cr =
                      new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString( PKG,
                          "TransMeta.CheckResult.TypeResultError.LoggingTableNeedsAdjustments.Description" ) + Const.CR
                          + sql, null );
                  remarks.add( cr );
                }

              } else {
                cr =
                    new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
                        .getString( PKG, "TransMeta.CheckResult.TypeResultError.LoggingTableDoesNotExist.Description" ),
                        null );
                remarks.add( cr );
              }
            } else {
              cr =
                  new CheckResult( CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages
                      .getString( PKG, "TransMeta.CheckResult.TypeResultError.LogTableNotSpecified.Description" ),
                      null );
              remarks.add( cr );
            }
          } catch ( KettleDatabaseException dbe ) {
            // Ignore errors
          } finally {
            logdb.disconnect();
          }
        }
        if ( monitor != null ) {
          monitor.worked( 1 );
        }

      }

      if ( monitor != null ) {
        monitor.subTask( BaseMessages
            .getString( PKG, "TransMeta.Monitor.CheckingForDatabaseUnfriendlyCharactersInFieldNamesTask.Title" ) );
      }
      if ( values.size() > 0 ) {
        for ( Map.Entry<ValueMetaInterface, String> entry : values.entrySet() ) {
          String message = entry.getValue();
          ValueMetaInterface v = entry.getKey();
          CheckResult
              cr =
              new CheckResult( CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages
                  .getString( PKG, "TransMeta.CheckResult.TypeResultWarning.Description", v.getName(), message,
                      v.getOrigin() ), findStep( v.getOrigin() ) );
          remarks.add( cr );
        }
      } else {
        CheckResult
            cr =
            new CheckResult( CheckResultInterface.TYPE_RESULT_OK,
                BaseMessages.getString( PKG, "TransMeta.CheckResult.TypeResultOK.Description" ), null );
        remarks.add( cr );
      }
      if ( monitor != null ) {
        monitor.worked( 1 );
      }
      ExtensionPointHandler.callExtensionPoint( getLogChannel(), KettleExtensionPoint.AfterCheckSteps.id,
          new CheckStepsExtension( remarks, space, this, steps, repository, metaStore ) );
    } catch ( Exception e ) {
      log.logError( Const.getStackTracker( e ) );
      throw new RuntimeException( e );
    }

  }

  /**
   * Gets the result rows.
   *
   * @return a list containing the result rows.
   * @deprecated Moved to Trans to make this class stateless
   */
  @Deprecated
  public List<RowMetaAndData> getResultRows() {
    return resultRows;
  }

  /**
   * Sets the list of result rows.
   *
   * @param resultRows
   *          The list of result rows to set.
   * @deprecated Moved to Trans to make this class stateless
   */
  @Deprecated
  public void setResultRows( List<RowMetaAndData> resultRows ) {
    this.resultRows = resultRows;
  }

  /**
   * Gets the repository directory path and name of the transformation.
   *
   * @return The repository directory path plus the name of the transformation
   */
  public String getPathAndName() {
    if ( getRepositoryDirectory().isRoot() ) {
      return getRepositoryDirectory().getPath() + getName();
    } else {
      return getRepositoryDirectory().getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + getName();
    }
  }

  /**
   * Gets the arguments used for this transformation.
   *
   * @return an array of String arguments for the transformation
   * @deprecated moved to Trans
   */
  @Deprecated
  public String[] getArguments() {
    return arguments;
  }

  /**
   * Sets the arguments used for this transformation.
   *
   * @param arguments
   *          The arguments to set.
   * @deprecated moved to Trans
   */
  @Deprecated
  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  /**
   * Gets the counters (database sequence values, e.g.) for the transformation.
   *
   * @return a named table of counters.
   * @deprecated moved to Trans
   */
  @Deprecated
  public Hashtable<String, Counter> getCounters() {
    return counters;
  }

  /**
   * Sets the counters (database sequence values, e.g.) for the transformation.
   *
   * @param counters
   *          The counters to set.
   * @deprecated moved to Trans
   */
  @Deprecated
  public void setCounters( Hashtable<String, Counter> counters ) {
    this.counters = counters;
  }

  /**
   * Gets a list of dependencies for the transformation
   *
   * @return a list of the dependencies for the transformation
   */
  public List<TransDependency> getDependencies() {
    return dependencies;
  }

  /**
   * Sets the dependencies for the transformation.
   *
   * @param dependencies
   *          The dependency list to set.
   */
  public void setDependencies( List<TransDependency> dependencies ) {
    this.dependencies = dependencies;
  }

  /**
   * Gets the database connection associated with "max date" processing. The connection, along with a specified table
   * and field, allows for the filtering of the number of rows to process in a transformation by time, such as only
   * processing the rows/records since the last time the transformation ran correctly. This can be used for auditing and
   * throttling data during warehousing operations.
   *
   * @return Returns the meta-data associated with the most recent database connection.
   */
  public DatabaseMeta getMaxDateConnection() {
    return maxDateConnection;
  }

  /**
   * Sets the database connection associated with "max date" processing.
   *
   * @param maxDateConnection
   *          the database meta-data to set
   * @see #getMaxDateConnection()
   */
  public void setMaxDateConnection( DatabaseMeta maxDateConnection ) {
    this.maxDateConnection = maxDateConnection;
  }

  /**
   * Gets the maximum date difference between start and end dates for row/record processing. This can be used for
   * auditing and throttling data during warehousing operations.
   *
   * @return the maximum date difference
   */
  public double getMaxDateDifference() {
    return maxDateDifference;
  }

  /**
   * Sets the maximum date difference between start and end dates for row/record processing.
   *
   * @param maxDateDifference
   *          The date difference to set.
   * @see #getMaxDateDifference()
   */
  public void setMaxDateDifference( double maxDateDifference ) {
    this.maxDateDifference = maxDateDifference;
  }

  /**
   * Gets the date field associated with "max date" processing. This allows for the filtering of the number of rows to
   * process in a transformation by time, such as only processing the rows/records since the last time the
   * transformation ran correctly. This can be used for auditing and throttling data during warehousing operations.
   *
   * @return a string representing the date for the most recent database connection.
   * @see #getMaxDateConnection()
   */
  public String getMaxDateField() {
    return maxDateField;
  }

  /**
   * Sets the date field associated with "max date" processing.
   *
   * @param maxDateField
   *          The date field to set.
   * @see #getMaxDateField()
   */
  public void setMaxDateField( String maxDateField ) {
    this.maxDateField = maxDateField;
  }

  /**
   * Gets the amount by which to increase the "max date" difference. This is used in "max date" processing, and can be
   * used to provide more fine-grained control of the date range. For example, if the end date specifies a minute for
   * which the data is not complete, you can "roll-back" the end date by one minute by
   *
   * @return Returns the maxDateOffset.
   * @see #setMaxDateOffset(double)
   */
  public double getMaxDateOffset() {
    return maxDateOffset;
  }

  /**
   * Sets the amount by which to increase the end date in "max date" processing. This can be used to provide more
   * fine-grained control of the date range. For example, if the end date specifies a minute for which the data is not
   * complete, you can "roll-back" the end date by one minute by setting the offset to -60.
   *
   * @param maxDateOffset
   *          The maxDateOffset to set.
   */
  public void setMaxDateOffset( double maxDateOffset ) {
    this.maxDateOffset = maxDateOffset;
  }

  /**
   * Gets the database table providing a date to be used in "max date" processing. This allows for the filtering of the
   * number of rows to process in a transformation by time, such as only processing the rows/records since the last time
   * the transformation ran correctly.
   *
   * @return Returns the maxDateTable.
   * @see #getMaxDateConnection()
   */
  public String getMaxDateTable() {
    return maxDateTable;
  }

  /**
   * Sets the table name associated with "max date" processing.
   *
   * @param maxDateTable
   *          The maxDateTable to set.
   * @see #getMaxDateTable()
   */
  public void setMaxDateTable( String maxDateTable ) {
    this.maxDateTable = maxDateTable;
  }

  /**
   * Gets the size of the rowsets.
   *
   * @return Returns the size of the rowsets.
   */
  public int getSizeRowset() {
    String rowSetSize = getVariable( Const.KETTLE_TRANS_ROWSET_SIZE );
    int altSize = Const.toInt( rowSetSize, 0 );
    if ( altSize > 0 ) {
      return altSize;
    } else {
      return sizeRowset;
    }
  }

  /**
   * Sets the size of the rowsets. This method allows you to change the size of the buffers between the connected steps
   * in a transformation. <b>NOTE:</b> Do not change this parameter unless you are running low on memory, for example.
   *
   * @param sizeRowset
   *          The sizeRowset to set.
   */
  public void setSizeRowset( int sizeRowset ) {
    this.sizeRowset = sizeRowset;
  }

  /**
   * Gets the database cache object.
   *
   * @return the database cache object.
   */
  public DBCache getDbCache() {
    return dbCache;
  }

  /**
   * Sets the database cache object.
   *
   * @param dbCache
   *          the database cache object to set
   */
  public void setDbCache( DBCache dbCache ) {
    this.dbCache = dbCache;
  }

  /**
   * Gets the version of the transformation.
   *
   * @return The version of the transformation
   */
  public String getTransversion() {
    return trans_version;
  }

  /**
   * Sets the version of the transformation.
   *
   * @param n
   *          The new version description of the transformation
   */
  public void setTransversion( String n ) {
    trans_version = n;
  }

  /**
   * Sets the status of the transformation.
   *
   * @param n
   *          The new status description of the transformation
   */
  public void setTransstatus( int n ) {
    trans_status = n;
  }

  /**
   * Gets the status of the transformation.
   *
   * @return The status of the transformation
   */
  public int getTransstatus() {
    return trans_status;
  }

  /**
   * Gets a textual representation of the transformation. If its name has been set, it will be returned, otherwise the
   * classname is returned.
   *
   * @return the textual representation of the transformation.
   */
  @Override
  public String toString() {
    if ( !Utils.isEmpty( filename ) ) {
      if ( Utils.isEmpty( name ) ) {
        return filename;
      } else {
        return filename + " : " + name;
      }
    }

    if ( name != null ) {
      if ( directory != null ) {
        String path = directory.getPath();
        if ( path.endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR ) ) {
          return path + name;
        } else {
          return path + RepositoryDirectory.DIRECTORY_SEPARATOR + name;
        }
      } else {
        return name;
      }
    } else {
      return TransMeta.class.getName();
    }
  }

  /**
   * Cancels queries opened for checking & fieldprediction.
   *
   * @throws KettleDatabaseException
   *           if any errors occur during query cancellation
   */
  public void cancelQueries() throws KettleDatabaseException {
    for ( int i = 0; i < nrSteps(); i++ ) {
      getStep( i ).getStepMetaInterface().cancelQueries();
    }
  }

  /**
   * Gets the arguments (and their values) used by this transformation. If argument values are supplied by parameter,
   * the values will used for the arguments. If the values are null or empty, the method will attempt to use argument
   * values from a previous execution.
   *
   * @param arguments
   *          the values for the arguments
   * @return A row with the used arguments (and their values) in it.
   */
  public Map<String, String> getUsedArguments( String[] arguments ) {
    Map<String, String> transArgs = new HashMap<>();

    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMetaInterface smi = getStep( i ).getStepMetaInterface();
      Map<String, String> stepArgs = smi.getUsedArguments(); // Get the command line arguments that this step uses.
      if ( stepArgs != null ) {
        transArgs.putAll( stepArgs );
      }
    }

    // OK, so perhaps, we can use the arguments from a previous execution?
    String[] saved = Props.isInitialized() ? Props.getInstance().getLastArguments() : null;

    // Set the default values on it...
    // Also change the name to "Argument 1" .. "Argument 10"
    //
    for ( String argument : transArgs.keySet() ) {
      String value = "";
      int argNr = Const.toInt( argument, -1 );
      if ( arguments != null && argNr > 0 && argNr <= arguments.length ) {
        value = Const.NVL( arguments[argNr - 1], "" );
      }
      if ( value.length() == 0 ) { // try the saved option...

        if ( argNr > 0 && argNr < saved.length && saved[argNr] != null ) {
          value = saved[argNr - 1];
        }
      }
      transArgs.put( argument, value );
    }

    return transArgs;
  }

  /**
   * Gets the amount of time (in nano-seconds) to wait while the input buffer is empty.
   *
   * @return the number of nano-seconds to wait while the input buffer is empty.
   */
  public int getSleepTimeEmpty() {
    return sleepTimeEmpty;
  }

  /**
   * Gets the amount of time (in nano-seconds) to wait while the input buffer is full.
   *
   * @return the number of nano-seconds to wait while the input buffer is full.
   */
  public int getSleepTimeFull() {
    return sleepTimeFull;
  }

  /**
   * Sets the amount of time (in nano-seconds) to wait while the input buffer is empty.
   *
   * @param sleepTimeEmpty
   *          the number of nano-seconds to wait while the input buffer is empty.
   */
  public void setSleepTimeEmpty( int sleepTimeEmpty ) {
    this.sleepTimeEmpty = sleepTimeEmpty;
  }

  /**
   * Sets the amount of time (in nano-seconds) to wait while the input buffer is full.
   *
   * @param sleepTimeFull
   *          the number of nano-seconds to wait while the input buffer is full.
   */
  public void setSleepTimeFull( int sleepTimeFull ) {
    this.sleepTimeFull = sleepTimeFull;
  }

  /**
   * This method asks all steps in the transformation whether or not the specified database connection is used. The
   * connection is used in the transformation if any of the steps uses it or if it is being used to log to.
   *
   * @param databaseMeta
   *          The connection to check
   * @return true if the connection is used in this transformation.
   */
  public boolean isDatabaseConnectionUsed( DatabaseMeta databaseMeta ) {
    for ( int i = 0; i < nrSteps(); i++ ) {
      StepMeta stepMeta = getStep( i );
      DatabaseMeta[] dbs = stepMeta.getStepMetaInterface().getUsedDatabaseConnections();
      for ( int d = 0; d < dbs.length; d++ ) {
        if ( dbs[d].equals( databaseMeta ) ) {
          return true;
        }
      }
    }

    return transLogTable.getDatabaseMeta() != null && transLogTable.getDatabaseMeta().equals( databaseMeta );

  }

  /**
   * Gets a list of all the strings used in this transformation. The parameters indicate which collections to search and
   * which to exclude.
   *
   * @param searchSteps
   *          true if steps should be searched, false otherwise
   * @param searchDatabases
   *          true if databases should be searched, false otherwise
   * @param searchNotes
   *          true if notes should be searched, false otherwise
   * @param includePasswords
   *          true if passwords should be searched, false otherwise
   * @return a list of search results for strings used in the transformation.
   */
  public List<StringSearchResult> getStringList( boolean searchSteps, boolean searchDatabases, boolean searchNotes,
      boolean includePasswords ) {
    List<StringSearchResult> stringList = new ArrayList<>();

    if ( searchSteps ) {
      // Loop over all steps in the transformation and see what the used vars are...
      for ( int i = 0; i < nrSteps(); i++ ) {
        StepMeta stepMeta = getStep( i );
        stringList.add( new StringSearchResult( stepMeta.getName(), stepMeta, this,
            BaseMessages.getString( PKG, "TransMeta.SearchMetadata.StepName" ) ) );
        if ( stepMeta.getDescription() != null ) {
          stringList.add( new StringSearchResult( stepMeta.getDescription(), stepMeta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.StepDescription" ) ) );
        }
        StepMetaInterface metaInterface = stepMeta.getStepMetaInterface();
        StringSearcher.findMetaData( metaInterface, 1, stringList, stepMeta, this );
      }
    }

    // Loop over all steps in the transformation and see what the used vars are...
    if ( searchDatabases ) {
      for ( int i = 0; i < nrDatabases(); i++ ) {
        DatabaseMeta meta = getDatabase( i );
        stringList.add( new StringSearchResult( meta.getName(), meta, this,
            BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseConnectionName" ) ) );
        if ( meta.getHostname() != null ) {
          stringList.add( new StringSearchResult( meta.getHostname(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseHostName" ) ) );
        }
        if ( meta.getDatabaseName() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabaseName(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseName" ) ) );
        }
        if ( meta.getUsername() != null ) {
          stringList.add( new StringSearchResult( meta.getUsername(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseUsername" ) ) );
        }
        if ( meta.getPluginId() != null ) {
          stringList.add( new StringSearchResult( meta.getPluginId(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseTypeDescription" ) ) );
        }
        if ( meta.getDatabasePortNumberString() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabasePortNumberString(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabasePort" ) ) );
        }
        if ( meta.getServername() != null ) {
          stringList.add( new StringSearchResult( meta.getServername(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabaseServer" ) ) );
        }

          if ( meta.getPassword() != null ) {
            stringList.add( new StringSearchResult( Strings.repeat("*", meta.getPassword().length()), meta, this,
                BaseMessages.getString( PKG, "TransMeta.SearchMetadata.DatabasePassword" ) ) );

        }
      }
    }

    // Loop over all steps in the transformation and see what the used vars are...
    if ( searchNotes ) {
      for ( int i = 0; i < nrNotes(); i++ ) {
        NotePadMeta meta = getNote( i );
        if ( meta.getNote() != null ) {
          stringList.add( new StringSearchResult( meta.getNote(), meta, this,
              BaseMessages.getString( PKG, "TransMeta.SearchMetadata.NotepadText" ) ) );
        }
      }
    }

    return stringList;
  }

  /**
   * Get a list of all the strings used in this transformation. The parameters indicate which collections to search and
   * which to exclude.
   *
   * @param searchSteps
   *          true if steps should be searched, false otherwise
   * @param searchDatabases
   *          true if databases should be searched, false otherwise
   * @param searchNotes
   *          true if notes should be searched, false otherwise
   * @return a list of search results for strings used in the transformation.
   */
  public List<StringSearchResult> getStringList( boolean searchSteps, boolean searchDatabases, boolean searchNotes ) {
    return getStringList( searchSteps, searchDatabases, searchNotes, false );
  }

  /**
   * Gets a list of the used variables in this transformation.
   *
   * @return a list of the used variables in this transformation.
   */
  public List<String> getUsedVariables() {
    // Get the list of Strings.
    List<StringSearchResult> stringList = getStringList( true, true, false, true );

    List<String> varList = new ArrayList<>();

    // Look around in the strings, see what we find...
    for ( int i = 0; i < stringList.size(); i++ ) {
      StringSearchResult result = stringList.get( i );
      StringUtil.getUsedVariables( result.getString(), varList, false );
    }

    return varList;
  }

  /**
   * Gets the previous result.
   *
   * @return the previous Result.
   * @deprecated this was moved to Trans to keep the metadata stateless
   */
  @Deprecated
  public Result getPreviousResult() {
    return previousResult;
  }

  /**
   * Sets the previous result.
   *
   * @param previousResult
   *          The previous Result to set.
   * @deprecated this was moved to Trans to keep the metadata stateless
   */
  @Deprecated
  public void setPreviousResult( Result previousResult ) {
    this.previousResult = previousResult;
  }

  /**
   * Gets a list of the files in the result.
   *
   * @return a list of ResultFiles.
   *
   * @deprecated this was moved to Trans to keep the metadata stateless
   */
  @Deprecated
  public List<ResultFile> getResultFiles() {
    return resultFiles;
  }

  /**
   * Sets the list of the files in the result.
   *
   * @param resultFiles
   *          The list of ResultFiles to set.
   * @deprecated this was moved to Trans to keep the metadata stateless
   */
  @Deprecated
  public void setResultFiles( List<ResultFile> resultFiles ) {
    this.resultFiles = resultFiles;
  }

  /**
   * Gets a list of partition schemas for this transformation.
   *
   * @return a list of PartitionSchemas
   */
  public List<PartitionSchema> getPartitionSchemas() {
    return partitionSchemas;
  }

  /**
   * Sets the list of partition schemas for this transformation.
   *
   * @param partitionSchemas
   *          the list of PartitionSchemas to set
   */
  public void setPartitionSchemas( List<PartitionSchema> partitionSchemas ) {
    this.partitionSchemas = partitionSchemas;
  }

  /**
   * Gets the partition schemas' names.
   *
   * @return a String array containing the available partition schema names.
   */
  public String[] getPartitionSchemasNames() {
    String[] names = new String[partitionSchemas.size()];
    for ( int i = 0; i < names.length; i++ ) {
      names[i] = partitionSchemas.get( i ).getName();
    }
    return names;
  }

  /**
   * Checks if is feedback shown.
   *
   * @return true if feedback is shown, false otherwise
   */
  public boolean isFeedbackShown() {
    return feedbackShown;
  }

  /**
   * Sets whether the feedback should be shown.
   *
   * @param feedbackShown
   *          true if feedback should be shown, false otherwise
   */
  public void setFeedbackShown( boolean feedbackShown ) {
    this.feedbackShown = feedbackShown;
  }

  /**
   * Gets the feedback size.
   *
   * @return the feedback size
   */
  public int getFeedbackSize() {
    return feedbackSize;
  }

  /**
   * Sets the feedback size.
   *
   * @param feedbackSize
   *          the feedback size to set
   */
  public void setFeedbackSize( int feedbackSize ) {
    this.feedbackSize = feedbackSize;
  }

  /**
   * Checks if the transformation is using unique database connections.
   *
   * @return true if the transformation is using unique database connections, false otherwise
   */
  public boolean isUsingUniqueConnections() {
    return usingUniqueConnections;
  }

  /**
   * Sets whether the transformation is using unique database connections.
   *
   * @param usingUniqueConnections
   *          true if the transformation is using unique database connections, false otherwise
   */
  public void setUsingUniqueConnections( boolean usingUniqueConnections ) {
    this.usingUniqueConnections = usingUniqueConnections;
  }

  /**
   * Gets a list of the cluster schemas used by the transformation.
   *
   * @return a list of ClusterSchemas
   */
  public List<ClusterSchema> getClusterSchemas() {
    return clusterSchemas;
  }

  /**
   * Sets list of the cluster schemas used by the transformation.
   *
   * @param clusterSchemas
   *          the list of ClusterSchemas to set
   */
  public void setClusterSchemas( List<ClusterSchema> clusterSchemas ) {
    this.clusterSchemas = clusterSchemas;
  }

  /**
   * Gets the cluster schema names.
   *
   * @return a String array containing the cluster schemas' names
   */
  public String[] getClusterSchemaNames() {
    String[] names = new String[clusterSchemas.size()];
    for ( int i = 0; i < names.length; i++ ) {
      names[i] = clusterSchemas.get( i ).getName();
    }
    return names;
  }

  /**
   * Find a partition schema using its name.
   *
   * @param name
   *          The name of the partition schema to look for.
   * @return the partition with the specified name of null if nothing was found
   */
  public PartitionSchema findPartitionSchema( String name ) {
    for ( int i = 0; i < partitionSchemas.size(); i++ ) {
      PartitionSchema schema = partitionSchemas.get( i );
      if ( schema.getName().equalsIgnoreCase( name ) ) {
        return schema;
      }
    }
    return null;
  }

  /**
   * Find a clustering schema using its name.
   *
   * @param name
   *          The name of the clustering schema to look for.
   * @return the cluster schema with the specified name of null if nothing was found
   */
  public ClusterSchema findClusterSchema( String name ) {
    for ( int i = 0; i < clusterSchemas.size(); i++ ) {
      ClusterSchema schema = clusterSchemas.get( i );
      if ( schema.getName().equalsIgnoreCase( name ) ) {
        return schema;
      }
    }
    return null;
  }

  /**
   * Add a new partition schema to the transformation if that didn't exist yet. Otherwise, replace it.
   *
   * @param partitionSchema
   *          The partition schema to be added.
   */
  public void addOrReplacePartitionSchema( PartitionSchema partitionSchema ) {
    int index = partitionSchemas.indexOf( partitionSchema );
    if ( index < 0 ) {
      partitionSchemas.add( partitionSchema );
    } else {
      PartitionSchema previous = partitionSchemas.get( index );
      previous.replaceMeta( partitionSchema );
    }
    setChanged();
  }

  /**
   * Add a new partition schemas to the transformation if that didn't exist yet. Otherwise, replace it.
   *
   * @param partitionSchemas
   *          List of partition schema to be added.
   */
  public void addOrReplacePartitionSchema( List<PartitionSchema> partitionSchemas ) {
    for ( PartitionSchema partitionSchema : partitionSchemas ) {
      addOrReplacePartitionSchema( partitionSchema );
    }
  }

  /**
   * Add a new cluster schema to the transformation if that didn't exist yet. Otherwise, replace it.
   *
   * @param clusterSchema
   *          The cluster schema to be added.
   */
  public void addOrReplaceClusterSchema( ClusterSchema clusterSchema ) {
    int index = clusterSchemas.indexOf( clusterSchema );
    if ( index < 0 ) {
      clusterSchemas.add( clusterSchema );
    } else {
      ClusterSchema previous = clusterSchemas.get( index );
      previous.replaceMeta( clusterSchema );
    }
    setChanged();
  }

  @Override protected List<SharedObjectInterface> getAllSharedObjects() {
    List<SharedObjectInterface> shared = super.getAllSharedObjects();
    shared.addAll( steps );
    shared.addAll( partitionSchemas );
    shared.addAll( clusterSchemas );
    return shared;
  }

  /**
   * Checks whether the transformation is using thread priority management.
   *
   * @return true if the transformation is using thread priority management, false otherwise
   */
  public boolean isUsingThreadPriorityManagment() {
    return usingThreadPriorityManagment;
  }

  /**
   * Sets whether the transformation is using thread priority management.
   *
   * @param usingThreadPriorityManagment
   *          true if the transformation is using thread priority management, false otherwise
   */
  public void setUsingThreadPriorityManagment( boolean usingThreadPriorityManagment ) {
    this.usingThreadPriorityManagment = usingThreadPriorityManagment;
  }

  /**
   * Check a step to see if there are no multiple steps to read from. If so, check to see if the receiving rows are all
   * the same in layout. We only want to ONLY use the DBCache for this to prevent GUI stalls.
   *
   * @param stepMeta
   *          the step to check
   * @param monitor
   *          the monitor
   * @throws KettleRowException
   *           in case we detect a row mixing violation
   */
  public void checkRowMixingStatically( StepMeta stepMeta, ProgressMonitorListener monitor ) throws KettleRowException {
    List<StepMeta> prevSteps = findPreviousSteps( stepMeta );
    int nrPrevious = prevSteps.size();
    if ( nrPrevious > 1 ) {
      RowMetaInterface referenceRow = null;
      // See if all previous steps send out the same rows...
      for ( int i = 0; i < nrPrevious; i++ ) {
        StepMeta previousStep = prevSteps.get( i );
        try {
          RowMetaInterface row = getStepFields( previousStep, monitor ); // Throws KettleStepException
          if ( referenceRow == null ) {
            referenceRow = row;
          } else if ( !stepMeta.getStepMetaInterface().excludeFromRowLayoutVerification() ) {
            BaseStep.safeModeChecking( referenceRow, row );
          }
        } catch ( KettleStepException e ) {
          // We ignore this one because we are in the process of designing the transformation, anything intermediate can
          // go wrong.
        }
      }
    }
  }

  /**
   * Sets the internal kettle variables.
   *
   * @param var
   *          the new internal kettle variables
   */
  @Override
  public void setInternalKettleVariables( VariableSpace var ) {
    setInternalFilenameKettleVariables( var );
    setInternalNameKettleVariable( var );

    // The name of the directory in the repository
    //
    variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY,
        directory != null ? directory.getPath() : "" );

    boolean hasRepoDir = getRepositoryDirectory() != null && getRepository() != null;

    if ( hasRepoDir ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY,
          variables.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY ) );
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY,
          variables.getVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY ) );
    }

    // Here we don't remove the job specific parameters, as they may come in handy.
    //
    if ( variables.getVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY ) == null ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "Parent Job File Directory" );
    }
    if ( variables.getVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME ) == null ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "Parent Job Filename" );
    }
    if ( variables.getVariable( Const.INTERNAL_VARIABLE_JOB_NAME ) == null ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_NAME, "Parent Job Name" );
    }
    if ( variables.getVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY ) == null ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "Parent Job Repository Directory" );
    }

    setInternalEntryCurrentDirectory();

  }

  /**
   * Sets the internal name kettle variable.
   *
   * @param var
   *          the new internal name kettle variable
   */
  @Override
  protected void setInternalNameKettleVariable( VariableSpace var ) {
    // The name of the transformation
    //
    variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, Const.NVL( name, "" ) );
  }

  /**
   * Sets the internal filename kettle variables.
   *
   * @param var
   *          the new internal filename kettle variables
   */
  @Override
  protected void setInternalFilenameKettleVariables( VariableSpace var ) {
    // If we have a filename that's defined, set variables. If not, clear them.
    //
    if ( !Utils.isEmpty( filename ) ) {
      try {
        FileObject fileObject = KettleVFS.getFileObject( filename, var );
        FileName fileName = fileObject.getName();

        // The filename of the transformation
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the transformation
        FileName fileDir = fileName.getParent();
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI() );
      } catch ( Exception e ) {
        //log.logError( "Unexpected error setting internal filename variables!", e );

        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "" );
        variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "" );
      }
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "" );
      variables.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "" );
    }

    setInternalEntryCurrentDirectory();

  }

  protected void setInternalEntryCurrentDirectory() {
    variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, variables.getVariable(
      repository != null ?  Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY
        : filename != null ? Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY
        : Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY ) );
  }


  /**
   * Finds the mapping input step with the specified name. If no mapping input step is found, null is returned
   *
   * @param stepname
   *          the name to search for
   * @return the step meta-data corresponding to the desired mapping input step, or null if no step was found
   * @throws KettleStepException
   *           if any errors occur during the search
   */
  public StepMeta findMappingInputStep( String stepname ) throws KettleStepException {
    if ( !Utils.isEmpty( stepname ) ) {
      StepMeta stepMeta = findStep( stepname ); // TODO verify that it's a mapping input!!
      if ( stepMeta == null ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "TransMeta.Exception.StepNameNotFound", stepname ) );
      }
      return stepMeta;
    } else {
      // Find the first mapping input step that fits the bill.
      StepMeta stepMeta = null;
      for ( StepMeta mappingStep : steps ) {
        if ( mappingStep.getStepID().equals( "MappingInput" ) ) {
          if ( stepMeta == null ) {
            stepMeta = mappingStep;
          } else if ( stepMeta != null ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TransMeta.Exception.OnlyOneMappingInputStepAllowed", "2" ) );
          }
        }
      }
      if ( stepMeta == null ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "TransMeta.Exception.OneMappingInputStepRequired" ) );
      }
      return stepMeta;
    }
  }

  /**
   * Finds the mapping output step with the specified name. If no mapping output step is found, null is returned.
   *
   * @param stepname
   *          the name to search for
   * @return the step meta-data corresponding to the desired mapping input step, or null if no step was found
   * @throws KettleStepException
   *           if any errors occur during the search
   */
  public StepMeta findMappingOutputStep( String stepname ) throws KettleStepException {
    if ( !Utils.isEmpty( stepname ) ) {
      StepMeta stepMeta = findStep( stepname ); // TODO verify that it's a mapping output step.
      if ( stepMeta == null ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "TransMeta.Exception.StepNameNotFound", stepname ) );
      }
      return stepMeta;
    } else {
      // Find the first mapping output step that fits the bill.
      StepMeta stepMeta = null;
      for ( StepMeta mappingStep : steps ) {
        if ( mappingStep.getStepID().equals( "MappingOutput" ) ) {
          if ( stepMeta == null ) {
            stepMeta = mappingStep;
          } else if ( stepMeta != null ) {
            throw new KettleStepException( BaseMessages.getString(
              PKG, "TransMeta.Exception.OnlyOneMappingOutputStepAllowed", "2" ) );
          }
        }
      }
      if ( stepMeta == null ) {
        throw new KettleStepException( BaseMessages.getString(
          PKG, "TransMeta.Exception.OneMappingOutputStepRequired" ) );
      }
      return stepMeta;
    }
  }

  /**
   * Gets a list of the resource dependencies.
   *
   * @return a list of ResourceReferences
   */
  public List<ResourceReference> getResourceDependencies() {
    return steps.stream()
      .flatMap( (StepMeta stepMeta) -> stepMeta.getResourceDependencies( this ).stream() )
      .collect( Collectors.toList() );
  }

  /**
   * Exports the specified objects to a flat-file system, adding content with filename keys to a set of definitions. The
   * supplied resource naming interface allows the object to name appropriately without worrying about those parts of
   * the implementation specific details.
   *
   * @param space
   *          the variable space to use
   * @param definitions
   * @param resourceNamingInterface
   * @param repository
   *          The repository to optionally load other resources from (to be converted to XML)
   * @param metaStore
   *          the metaStore in which non-kettle metadata could reside.
   *
   * @return the filename of the exported resource
   */
  @Override
  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
      ResourceNamingInterface resourceNamingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {

    try {
      // Handle naming for both repository and XML bases resources...
      //
      String baseName;
      String originalPath;
      String fullname;
      String extension = "ktr";
      if ( Utils.isEmpty( getFilename() ) ) {
        // Assume repository...
        //
        originalPath = directory.getPath();
        baseName = getName();
        fullname =
          directory.getPath()
            + ( directory.getPath().endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR )
              ? "" : RepositoryDirectory.DIRECTORY_SEPARATOR ) + getName() + "." + extension; //
      } else {
        // Assume file
        //
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( getFilename() ), space );
        originalPath = fileObject.getParent().getURL().toString();
        baseName = fileObject.getName().getBaseName();
        fullname = fileObject.getURL().toString();
      }

      String
          exportFileName =
          resourceNamingInterface
              .nameResource( baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.TRANSFORMATION );
      ResourceDefinition definition = definitions.get( exportFileName );
      if ( definition == null ) {
        // If we do this once, it will be plenty :-)
        //
        TransMeta transMeta = (TransMeta) this.realClone( false );
        // transMeta.copyVariablesFrom(space);

        // Add used resources, modify transMeta accordingly
        // Go through the list of steps, etc.
        // These critters change the steps in the cloned TransMeta
        // At the end we make a new XML version of it in "exported"
        // format...

        // loop over steps, databases will be exported to XML anyway.
        //
        for ( StepMeta stepMeta : transMeta.getSteps() ) {
          stepMeta.exportResources( space, definitions, resourceNamingInterface, repository, metaStore );
        }

        // Change the filename, calling this sets internal variables
        // inside of the transformation.
        //
        transMeta.setFilename( exportFileName );

        // All objects get re-located to the root folder
        //
        transMeta.setRepositoryDirectory( new RepositoryDirectory() );

        // Set a number of parameters for all the data files referenced so far...
        //
        Map<String, String> directoryMap = resourceNamingInterface.getDirectoryMap();
        if ( directoryMap != null ) {
          for ( Map.Entry<String, String> entry : directoryMap.entrySet() ) {
            transMeta.addParameterDefinition( entry.getValue(), entry.getKey(), "Data file path discovered during export" );
          }
        }

        // At the end, add ourselves to the map...
        //
        String transMetaContent = transMeta.getXML();

        definition = new ResourceDefinition( exportFileName, transMetaContent );

        // Also remember the original filename (if any), including variables etc.
        //
        if ( Utils.isEmpty( this.getFilename() ) ) { // Repository
          definition.setOrigin( fullname );
        } else {
          definition.setOrigin( this.getFilename() );
        }

        definitions.put( fullname, definition );
      }
      return exportFileName;
    } catch ( FileSystemException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename() ), e );
    } catch ( KettleFileException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename() ), e );
    }
  }

  /**
   * Gets the slave step copy partition distribution.
   *
   * @return the SlaveStepCopyPartitionDistribution
   */
  public SlaveStepCopyPartitionDistribution getSlaveStepCopyPartitionDistribution() {
    return slaveStepCopyPartitionDistribution;
  }

  /**
   * Sets the slave step copy partition distribution.
   *
   * @param slaveStepCopyPartitionDistribution
   *          the slaveStepCopyPartitionDistribution to set
   */
  public void setSlaveStepCopyPartitionDistribution(
      SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution ) {
    this.slaveStepCopyPartitionDistribution = slaveStepCopyPartitionDistribution;
  }

  /**
   * Finds the first used cluster schema.
   *
   * @return the first used cluster schema
   */
  public ClusterSchema findFirstUsedClusterSchema() {
    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.getClusterSchema() != null ) {
        return stepMeta.getClusterSchema();
      }
    }
    return null;
  }

  /**
   * Checks whether the transformation is a slave transformation.
   *
   * @return true if the transformation is a slave transformation, false otherwise
   */
  public boolean isSlaveTransformation() {
    return slaveTransformation;
  }

  /**
   * Sets whether the transformation is a slave transformation.
   *
   * @param slaveTransformation
   *          true if the transformation is a slave transformation, false otherwise
   */
  public void setSlaveTransformation( boolean slaveTransformation ) {
    this.slaveTransformation = slaveTransformation;
  }

  /**
   * Checks whether the transformation is capturing step performance snapshots.
   *
   * @return true if the transformation is capturing step performance snapshots, false otherwise
   */
  public boolean isCapturingStepPerformanceSnapShots() {
    return capturingStepPerformanceSnapShots;
  }

  /**
   * Sets whether the transformation is capturing step performance snapshots.
   *
   * @param capturingStepPerformanceSnapShots
   *          true if the transformation is capturing step performance snapshots, false otherwise
   */
  public void setCapturingStepPerformanceSnapShots( boolean capturingStepPerformanceSnapShots ) {
    this.capturingStepPerformanceSnapShots = capturingStepPerformanceSnapShots;
  }

  /**
   * Gets the step performance capturing delay.
   *
   * @return the step performance capturing delay
   */
  public long getStepPerformanceCapturingDelay() {
    return stepPerformanceCapturingDelay;
  }

  /**
   * Sets the step performance capturing delay.
   *
   * @param stepPerformanceCapturingDelay
   *          the stepPerformanceCapturingDelay to set
   */
  public void setStepPerformanceCapturingDelay( long stepPerformanceCapturingDelay ) {
    this.stepPerformanceCapturingDelay = stepPerformanceCapturingDelay;
  }

  /**
   * Gets the step performance capturing size limit.
   *
   * @return the step performance capturing size limit
   */
  public String getStepPerformanceCapturingSizeLimit() {
    return stepPerformanceCapturingSizeLimit;
  }

  /**
   * Sets the step performance capturing size limit.
   *
   * @param stepPerformanceCapturingSizeLimit
   *          the step performance capturing size limit to set
   */
  public void setStepPerformanceCapturingSizeLimit( String stepPerformanceCapturingSizeLimit ) {
    this.stepPerformanceCapturingSizeLimit = stepPerformanceCapturingSizeLimit;
  }

  /**
   * Clears the step fields and loop caches.
   */
  public void clearCaches() {
    clearStepFieldsCachce();
    clearLoopCache();
    clearPreviousStepCache();
  }

  /**
   * Clears the step fields cachce.
   */
  private void clearStepFieldsCachce() {
    stepsFieldsCache.clear();
  }

  /**
   * Clears the loop cache.
   */
  private void clearLoopCache() {
    loopCache.clear();
  }

  @VisibleForTesting
  void clearPreviousStepCache() {
    previousStepCache.clear();
  }

  /**
   * Gets the repository element type.
   *
   * @return the repository element type
   * @see org.pentaho.di.repository.RepositoryElementInterface#getRepositoryElementType()
   */
  @Override
  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }

  /**
   * Gets the log channel.
   *
   * @return the log channel
   */
  public LogChannelInterface getLogChannel() {
    return log;
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
   * Gets the object type.
   *
   * @return the object type
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectType()
   */
  @Override
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.TRANSMETA;
  }

  /**
   * Gets the log table for the transformation.
   *
   * @return the log table for the transformation
   */
  public TransLogTable getTransLogTable() {
    return transLogTable;
  }

  /**
   * Sets the log table for the transformation.
   *
   * @param transLogTable the log table to set
   */
  public void setTransLogTable( TransLogTable transLogTable ) {
    this.transLogTable = transLogTable;
  }

  /**
   * Gets the performance log table for the transformation.
   *
   * @return the performance log table for the transformation
   */
  public PerformanceLogTable getPerformanceLogTable() {
    return performanceLogTable;
  }

  /**
   * Sets the performance log table for the transformation.
   *
   * @param performanceLogTable
   *          the performance log table to set
   */
  public void setPerformanceLogTable( PerformanceLogTable performanceLogTable ) {
    this.performanceLogTable = performanceLogTable;
  }

  /**
   * Gets the step log table for the transformation.
   *
   * @return the step log table for the transformation
   */
  public StepLogTable getStepLogTable() {
    return stepLogTable;
  }

  /**
   * Sets the step log table for the transformation.
   *
   * @param stepLogTable
   *          the step log table to set
   */
  public void setStepLogTable( StepLogTable stepLogTable ) {
    this.stepLogTable = stepLogTable;
  }

  /**
   * Gets a list of the log tables (transformation, step, performance, channel) for the transformation.
   *
   * @return a list of LogTableInterfaces for the transformation
   */
  public List<LogTableInterface> getLogTables() {
    List<LogTableInterface> logTables = new ArrayList<>();
    logTables.add( transLogTable );
    logTables.add( stepLogTable );
    logTables.add( performanceLogTable );
    logTables.add( channelLogTable );
    logTables.add( metricsLogTable );
    return logTables;
  }

  /**
   * Gets the transformation type.
   *
   * @return the transformationType
   */
  public TransformationType getTransformationType() {
    return transformationType;
  }

  /**
   * Sets the transformation type.
   *
   * @param transformationType
   *          the transformationType to set
   */
  public void setTransformationType( TransformationType transformationType ) {
    this.transformationType = transformationType;
  }

  /**
   * Utility method to write the XML of this transformation to a file, mostly for testing purposes.
   *
   * @param filename
   *          The filename to save to
   * @throws KettleXMLException
   *           in case something goes wrong.
   */
  public void writeXML( String filename ) throws KettleXMLException {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream( filename );
      fos.write( XMLHandler.getXMLHeader().getBytes( Const.XML_ENCODING ) );
      fos.write( getXML().getBytes( Const.XML_ENCODING ) );
    } catch ( Exception e ) {
      throw new KettleXMLException( "Unable to save to XML file '" + filename + "'", e );
    } finally {
      if ( fos != null ) {
        try {
          fos.close();
        } catch ( IOException e ) {
          throw new KettleXMLException( "Unable to close file '" + filename + "'", e );
        }
      }
    }
  }

  /**
   * Checks whether the transformation has repository references.
   *
   * @return true if the transformation has repository references, false otherwise
   */
  public boolean hasRepositoryReferences() {
    for ( StepMeta stepMeta : steps ) {
      if ( stepMeta.getStepMetaInterface().hasRepositoryReferences() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Looks up the references after a repository import.
   *
   * @param repository
   *          the repository to reference.
   * @throws KettleException
   *           the kettle exception
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    for ( StepMeta stepMeta : steps ) {
      stepMeta.getStepMetaInterface().lookupRepositoryReferences( repository );
    }
  }

  /**
   * @return the metricsLogTable
   */
  public MetricsLogTable getMetricsLogTable() {
    return metricsLogTable;
  }

  /**
   * @param metricsLogTable
   *          the metricsLogTable to set
   */
  public void setMetricsLogTable( MetricsLogTable metricsLogTable ) {
    this.metricsLogTable = metricsLogTable;
  }

  @Override
  public boolean isGatheringMetrics() {
    return log.isGatheringMetrics();
  }

  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
    log.setGatheringMetrics( gatheringMetrics );
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return log.isForcingSeparateLogging();
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
    log.setForcingSeparateLogging( forcingSeparateLogging );
  }

  public void addStepChangeListener( StepMetaChangeListenerInterface listener ) {
    stepChangeListeners.add( listener );
  }

  public void addStepChangeListener( int p, StepMetaChangeListenerInterface list ) {
    int indexListener = -1;
    int indexListenerRemove = -1;
    StepMeta rewriteStep = steps.get( p );
    StepMetaInterface iface = rewriteStep.getStepMetaInterface();
    if ( iface instanceof StepMetaChangeListenerInterface ) {
      for ( StepMetaChangeListenerInterface listener : stepChangeListeners ) {
        indexListener++;
        if ( listener.equals( iface ) ) {
          indexListenerRemove = indexListener;
        }
      }
      if ( indexListenerRemove >= 0 ) {
        stepChangeListeners.add( indexListenerRemove, list );
      } else if ( stepChangeListeners.size() == 0 && p == 0 ) {
        stepChangeListeners.add( list );
      }
    }
  }

  public void removeStepChangeListener( StepMetaChangeListenerInterface list ) {
    int indexListener = -1;
    int indexListenerRemove = -1;
    for ( StepMetaChangeListenerInterface listener : stepChangeListeners ) {
      indexListener++;
      if ( listener.equals( list ) ) {
        indexListenerRemove = indexListener;
      }
    }
    if ( indexListenerRemove >= 0 ) {
      stepChangeListeners.remove( indexListenerRemove );
    }
  }

  public void notifyAllListeners( StepMeta oldMeta, StepMeta newMeta ) {
    for ( StepMetaChangeListenerInterface listener : stepChangeListeners ) {
      listener.onStepChange( this, oldMeta, newMeta );
    }
  }

  public boolean containsStepMeta( StepMeta stepMeta ) {
    return steps.contains( stepMeta );
  }

  public List<MissingTrans> getMissingTrans() {
    return missingTrans;
  }

  public void addMissingTrans( MissingTrans trans ) {
    if ( missingTrans == null ) {
      missingTrans = new ArrayList<>();
    }
    missingTrans.add( trans );
  }

  public void removeMissingTrans( MissingTrans trans ) {
    if ( missingTrans != null && trans != null && missingTrans.contains( trans ) ) {
      missingTrans.remove( trans );
    }
  }

  @Override
  public boolean hasMissingPlugins() {
    return missingTrans != null && !missingTrans.isEmpty();
  }

  @Override
  public NamedClusterEmbedManager getNamedClusterEmbedManager( ) {
    if ( namedClusterEmbedManager == null ) {
      namedClusterEmbedManager = new NamedClusterEmbedManager( this, getLogChannel() );
    }
    return namedClusterEmbedManager;
  }

  /**
   *
   * @return
   */
  public int getCacheVersion() throws KettleException {
    HashCodeBuilder hashCodeBuilder =  new HashCodeBuilder( 17, 31 )
        // info
        .append( this.getName() )
        .append( this.getTransformationType() )
        .append( this.getSizeRowset() )
        .append( this.getSleepTimeEmpty() )
        .append( this.getSleepTimeFull() )
        .append( this.isUsingUniqueConnections() )
        .append( this.isFeedbackShown() )
        .append( this.getFeedbackSize() )
        .append( this.isUsingThreadPriorityManagment() )
        .append( this.getSharedObjectsFile() )
        .append( this.isCapturingStepPerformanceSnapShots() )
        .append( this.getStepPerformanceCapturingDelay() )
        .append( this.getStepPerformanceCapturingSizeLimit() )

        .append( this.getMaxDateConnection() )
        .append( this.getMaxDateTable() )
        .append( this.getMaxDateField() )
        .append( this.getMaxDateOffset() )
        .append( this.getMaxDateDifference() )

        .append( this.getDependencies() )
        .append( this.getPartitionSchemas() )
        .append( this.getSlaveServers() )
        .append( this.getClusterSchemas() )
        .append( this.getSlaveStepCopyPartitionDistribution() )
        .append( this.isSlaveTransformation() )

        .append( this.nrTransHops() )

        // steps
        .append( this.getSteps().size() )
        .append( this.getStepNames() )

        // hops
        .append( this.hops );

    List<StepMeta> steps = this.getSteps();

    for ( StepMeta step : steps ) {
      hashCodeBuilder
          .append( step.getName() )
          .append( step.getStepMetaInterface().getXML() )
          .append( step.getClusterSchema() )
          .append( step.getRemoteInputSteps() )
          .append( step.getRemoteOutputSteps() )
          .append( step.isDoingErrorHandling() );
    }
    return hashCodeBuilder.toHashCode();
  }

  private static String getStepMetaCacheKey( StepMeta stepMeta, boolean info ) {
    return String.format( "%1$b-%2$s-%3$s", info, stepMeta.getStepID(), stepMeta.toString() );
  }

  private static RowMetaInterface[] cloneRowMetaInterfaces( RowMetaInterface[] inform ) {
    RowMetaInterface[] cloned = inform.clone();
    for ( int i = 0; i < cloned.length; i++ ) {
      if ( cloned[i] != null ) {
        cloned[i] = cloned[i].clone();
      }
    }
    return cloned;
  }
}
