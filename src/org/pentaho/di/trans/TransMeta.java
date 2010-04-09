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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.Result;
import org.pentaho.di.core.ResultFile;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleRowException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.listeners.FilenameChangedListener;
import org.pentaho.di.core.listeners.NameChangedListener;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.PerformanceLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.reflection.StringSearcher;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepPartitioningMeta;
import org.pentaho.di.trans.steps.mapping.MappingMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class defines a transformation and offers methods to save and load it 
 * from XML or a PDI database repository.
 *
 * @since 20-jun-2003
 * @author Matt Casters
 */
public class TransMeta extends ChangedFlag implements XMLInterface, Comparator<TransMeta>, Comparable<TransMeta>, 
								  Cloneable, UndoInterface, 
								  HasDatabasesInterface, VariableSpace, EngineMetaInterface, 
								  ResourceExportInterface, HasSlaveServersInterface, NamedParams,
								  RepositoryElementInterface, LoggingObjectInterface
{
	private static Class<?> PKG = Trans.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

    public static final String XML_TAG = "transformation";
    
    public static final String STRING_TRANSMETA = "Transformation metadata";
    
	public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.TRANSFORMATION;

    private List<DatabaseMeta>       databases;

    private List<StepMeta>           steps;

    private List<TransHopMeta>       hops;

    private List<NotePadMeta>        notes;

    private List<TransDependency>    dependencies;
    
    private List<SlaveServer>        slaveServers;
    
    private List<ClusterSchema>      clusterSchemas;

    private List<PartitionSchema>    partitionSchemas;

    private RepositoryDirectory directory;

    private String              name;

	private String              description;

	private String              extended_description;

	private String				trans_version;

	private int					trans_status;

    private String              filename;

    private TransLogTable       transLogTable;
    private PerformanceLogTable performanceLogTable;
    private ChannelLogTable     channelLogTable;
    private StepLogTable     	stepLogTable;


    private int                 sizeRowset;

    private DatabaseMeta        maxDateConnection;

    private String              maxDateTable;

    private String              maxDateField;

    private double              maxDateOffset;

    private double              maxDateDifference;

    private String              arguments[];

    private Hashtable<String,Counter> counters;

    private boolean             changed_steps, changed_databases, changed_hops, changed_notes;

    private List<TransAction>   undo;

    private int                 max_undo;

    private int                 undo_position;

    private DBCache             dbCache;

    private ObjectId            id;

    private String              createdUser, modifiedUser;

    private Date                createdDate, modifiedDate;

    private int                 sleepTimeEmpty;

    private int                 sleepTimeFull;

	private Result              previousResult;
    private List<RowMetaAndData> resultRows;
    private List<ResultFile>     resultFiles;            
        
    private boolean             usingUniqueConnections;
    
    private boolean             feedbackShown;
    private int                 feedbackSize;
    
    /** flag to indicate thread management usage.  Set to default to false from version 2.5.0 on. Before that it was enabled by default. */
    private boolean             usingThreadPriorityManagment;
    
    /** If this is null, we load from the default shared objects file : $KETTLE_HOME/.kettle/shared.xml */
    private String              sharedObjectsFile;
    
    /** The last load of the shared objects file by this TransMet object */
    private SharedObjects       sharedObjects;
    
    private VariableSpace       variables = new Variables();
    
    /** The slave-step-copy/partition distribution.  Only used for slave transformations in a clustering environment. */
    private SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution;
    
    /** Just a flag indicating that this is a slave transformation - internal use only, no GUI option */
    private boolean slaveTransformation;
    
    /** The repository to reference in the one-off case that it is needed */
    private Repository repository;
    
    private boolean    capturingStepPerformanceSnapShots;
    
    private long       stepPerformanceCapturingDelay;
    
    private String     stepPerformanceCapturingSizeLimit;
    
    private Map<String, RowMetaInterface> stepsFieldsCache;
    private Map<String, Boolean> loopCache;
    
    private List<NameChangedListener> nameChangedListeners;
    private List<FilenameChangedListener> filenameChangedListeners;
    
    private NamedParams namedParams = new NamedParamsDefault();
   
    private LogChannelInterface log;
    
    private LogLevel logLevel = DefaultLogLevel.getLogLevel();
    
    private String containerObjectId;
    
    public enum TransformationType {
    	Normal("Normal", BaseMessages.getString(PKG, "TransMeta.TransformationType.Normal")),
    	SerialSingleThreaded("SerialSingleThreaded", BaseMessages.getString(PKG, "TransMeta.TransformationType.SerialSingleThreaded")),
    	;
    	
    	private String	code;
    	private String	description;

		private TransformationType(String code, String description) {
			this.code = code;
			this.description = description; 
		}
		
		public String getCode() {
			return code;
		}
		
		public String getDescription() {
			return description;
		}

		public static TransformationType getTransformationTypeByCode(String transTypeCode) {
			if (transTypeCode!=null) {
				for (TransformationType type : values()) {
					if (type.code.equalsIgnoreCase(transTypeCode)) {
						return type;
					}
				}
			}
			return Normal;
		}
    }
    
    private TransformationType transformationType;
    
    
    // //////////////////////////////////////////////////////////////////////////

    public static final int     TYPE_UNDO_CHANGE   = 1;

    public static final int     TYPE_UNDO_NEW      = 2;

    public static final int     TYPE_UNDO_DELETE   = 3;

    public static final int     TYPE_UNDO_POSITION = 4;

    public static final String  desc_type_undo[]   = { "", BaseMessages.getString(PKG, "TransMeta.UndoTypeDesc.UndoChange"), BaseMessages.getString(PKG, "TransMeta.UndoTypeDesc.UndoNew"), BaseMessages.getString(PKG, "TransMeta.UndoTypeDesc.UndoDelete"), BaseMessages.getString(PKG, "TransMeta.UndoTypeDesc.UndoPosition") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private static final String XML_TAG_INFO                = "info";
    private static final String XML_TAG_ORDER               = "order";
    public  static final String XML_TAG_NOTEPADS            = "notepads";
    public  static final String XML_TAG_PARAMETERS          = "parameters";
    private static final String XML_TAG_DEPENDENCIES        = "dependencies";
    public  static final String XML_TAG_PARTITIONSCHEMAS    = "partitionschemas";
    public  static final String XML_TAG_SLAVESERVERS        = "slaveservers";
    public  static final String XML_TAG_CLUSTERSCHEMAS      = "clusterschemas";
    private static final String XML_TAG_STEP_ERROR_HANDLING = "step_error_handling";
    
    /**
     * Builds a new empty transformation.
     */
    public TransMeta()
    {
        clear();
        initializeVariablesFrom(null);
        log = new LogChannel(STRING_TRANSMETA);
    }
    
    /**
     * Builds a new empty transformation with a set of variables to inherit from.
     * @param parent the variable space to inherit from
     */
    public TransMeta(VariableSpace parent)
    {
        clear();
        initializeVariablesFrom(parent);
    }

    /**
     * Constructs a new transformation specifying the filename, name and arguments.
     *
     * @param filename The filename of the transformation
     * @param name The name of the transformation
     * @param arguments The arguments as Strings
     */
    public TransMeta(String filename, String name, String arguments[])
    {
        clear();
        setFilename(filename);
        this.name = name;
        this.arguments = arguments;
        initializeVariablesFrom(null);
    }

    /**
     * Compares two transformation on name, filename, repository directory, etc.
     */
    public int compare(TransMeta t1, TransMeta t2) 
    {
    	// If we don't have a filename, the transformation comes from a repository
    	//
        if (Const.isEmpty(t1.getFilename())) {
        	
        	if (!Const.isEmpty(t2.getFilename())) return -1;

        	// First compare names...
        	//
            if (Const.isEmpty(t1.getName()) && !Const.isEmpty(t2.getName())) return -1;
            if (!Const.isEmpty(t1.getName()) && Const.isEmpty(t2.getName())) return  1;
            int cmpName = t1.getName().compareTo(t2.getName());
            if (cmpName!=0) return cmpName;
            
            // Same name, compare Repository directory...
            //
            int cmpDirectory = t1.getRepositoryDirectory().getPath().compareTo(t2.getRepositoryDirectory().getPath());
            if (cmpDirectory!=0) return cmpDirectory;
            
            // Same name, same directory, compare versions
            //
            if (t1.getObjectRevision()!=null && t2.getObjectRevision()==null) return  1; 
            if (t1.getObjectRevision()==null && t2.getObjectRevision()!=null) return -1;
            if (t1.getObjectRevision()==null && t2.getObjectRevision()==null) return  0;
            return t1.getObjectRevision().getName().compareTo(t2.getObjectRevision().getName());
            
        } else {
        	if (Const.isEmpty(t2.getFilename())) return 1;

        	// First compare names
        	//
            if (Const.isEmpty(t1.getName()) && !Const.isEmpty(t2.getName())) return -1;
            if (!Const.isEmpty(t1.getName()) && Const.isEmpty(t2.getName())) return  1;
            int cmpName = t1.getName().compareTo(t2.getName());
            if (cmpName!=0) return cmpName;

            // Same name, compare filenames...
            //
            return t1.getFilename().compareTo(t2.getFilename()); 
        }
    } 
    
    public int compareTo(TransMeta o)
    {
        return compare(this, o);
    }
    
    public boolean equals(Object obj)
    {
    	if (!(obj instanceof TransMeta))
    		return false;
    	
        return compare(this, (TransMeta)obj)==0;
    }
    
    @Override
    public Object clone() {
      return realClone(true);
    }

    public Object realClone(boolean doClear) {
      
      try {
        TransMeta transMeta = (TransMeta) super.clone();
        if (doClear) {
          transMeta.clear();
        } else {
          // Clear out the things we're replacing below
          transMeta.databases = new ArrayList<DatabaseMeta>();
          transMeta.steps = new ArrayList<StepMeta>();
          transMeta.hops = new ArrayList<TransHopMeta>();
          transMeta.notes = new ArrayList<NotePadMeta>();
          transMeta.dependencies = new ArrayList<TransDependency>();
          transMeta.partitionSchemas = new ArrayList<PartitionSchema>();
          transMeta.slaveServers = new ArrayList<SlaveServer>();
          transMeta.clusterSchemas = new ArrayList<ClusterSchema>();
          transMeta.namedParams = new NamedParamsDefault();
        }
        for (DatabaseMeta db : databases) transMeta.addDatabase((DatabaseMeta)db.clone());
        for (StepMeta step : steps) transMeta.addStep((StepMeta) step.clone());
        for (TransHopMeta hop : hops) transMeta.addTransHop((TransHopMeta) hop.clone());
        for (NotePadMeta note : notes) transMeta.addNote((NotePadMeta)note.clone());
        for (TransDependency dep : dependencies) transMeta.addDependency((TransDependency)dep.clone());
        for (SlaveServer slave : slaveServers) transMeta.getSlaveServers().add((SlaveServer)slave.clone());
        for (ClusterSchema schema : clusterSchemas) transMeta.getClusterSchemas().add((ClusterSchema)schema.clone());
        for (PartitionSchema schema : partitionSchemas) transMeta.getPartitionSchemas().add((PartitionSchema)schema.clone());
        for (String key : listParameters()) transMeta.addParameterDefinition(key, getParameterDefault(key), getParameterDescription(key));
        
        return transMeta;
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    
    /**
     * Get the database ID in the repository for this object.
     *
     * @return the database ID in the repository for this object.
     */
    public ObjectId getObjectId()
    {
        return id;
    }

    /**
     * Set the database ID for this object in the repository.
     *
     * @param id the database ID for this object in the repository.
     */
    public void setObjectId(ObjectId id)
    {
        this.id = id;
    }

    /**
     * Clears the transformation.
     */
    public void clear()
    {
    	setObjectId(null);
        databases = new ArrayList<DatabaseMeta>();
        steps = new ArrayList<StepMeta>();
        hops = new ArrayList<TransHopMeta>();
        notes = new ArrayList<NotePadMeta>();
        dependencies = new ArrayList<TransDependency>();
        partitionSchemas = new ArrayList<PartitionSchema>();
        slaveServers = new ArrayList<SlaveServer>();
        clusterSchemas = new ArrayList<ClusterSchema>();
        
        slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution();
        
        setName(null);
		description=null;
		trans_status=-1;
		trans_version=null;
		extended_description=null;
        setFilename(null);
        
        transLogTable = TransLogTable.getDefault(this, this);
        performanceLogTable = PerformanceLogTable.getDefault(this, this);
        channelLogTable = ChannelLogTable.getDefault(this, this);
        stepLogTable = StepLogTable.getDefault(this, this);
        
        sizeRowset     = Const.ROWS_IN_ROWSET;
        sleepTimeEmpty = Const.TIMEOUT_GET_MILLIS;
        sleepTimeFull  = Const.TIMEOUT_PUT_MILLIS;

        maxDateConnection = null;
        maxDateTable = null;
        maxDateField = null;
        maxDateOffset = 0.0;

        maxDateDifference = 0.0;

        undo = new ArrayList<TransAction>();
        max_undo = Const.MAX_UNDO;
        undo_position = -1;

        counters = new Hashtable<String,Counter>();
        resultRows = null;

        clearUndo();
        clearChanged();

        createdUser = "-"; //$NON-NLS-1$
		createdDate = new Date(); //$NON-NLS-1$

        modifiedUser = "-"; //$NON-NLS-1$
        modifiedDate = new Date(); //$NON-NLS-1$

        // LOAD THE DATABASE CACHE!
        dbCache = DBCache.getInstance();

        // Default directory: root
        directory = new RepositoryDirectory();
        
        resultRows = new ArrayList<RowMetaAndData>();
        resultFiles = new ArrayList<ResultFile>();
        
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
        
        stepsFieldsCache = new HashMap<String, RowMetaInterface>();
        loopCache = new HashMap<String, Boolean>();
        transformationType = TransformationType.Normal;
        
        log = new LogChannel(STRING_TRANSMETA);
    }

    public void clearUndo()
    {
        undo = new ArrayList<TransAction>();
        undo_position = -1;
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#getDatabases()
     */
    public List<DatabaseMeta> getDatabases()
    {
        return databases;
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#setDatabases(java.util.ArrayList)
     */
    public void setDatabases(List<DatabaseMeta> databases)
    {
      Collections.sort(databases, DatabaseMeta.comparator);
      this.databases = databases;
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#addDatabase(org.pentaho.di.core.database.DatabaseMeta)
     */
    public void addDatabase(DatabaseMeta databaseMeta)
    {
      databases.add(databaseMeta);
      Collections.sort(databases, DatabaseMeta.comparator);
    }
    
    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#addOrReplaceDatabase(org.pentaho.di.core.database.DatabaseMeta)
     */
    public void addOrReplaceDatabase(DatabaseMeta databaseMeta)
    {
        int index = databases.indexOf(databaseMeta);
        if (index<0)
        {
            addDatabase(databaseMeta); 
        }
        else
        {
            DatabaseMeta previous = getDatabase(index);
            previous.replaceMeta(databaseMeta);
        }
        changed_databases = true;
    }

    /**
     * Add a new step to the transformation
     *
     * @param stepMeta The step to be added.
     */
    public void addStep(StepMeta stepMeta)
    {
        steps.add(stepMeta);
        stepMeta.setParentTransMeta(this);
        changed_steps = true;
    }
    
    /**
     * Add a new step to the transformation if that step didn't exist yet.
     * Otherwise, replace the step.
     *
     * @param stepMeta The step to be added.
     */
    public void addOrReplaceStep(StepMeta stepMeta)
    {
        int index = steps.indexOf(stepMeta);
        if (index<0)
        {
            steps.add(stepMeta); 
        }
        else
        {
            StepMeta previous = getStep(index);
            previous.replaceMeta(stepMeta);
        }
        stepMeta.setParentTransMeta(this);
        changed_steps = true;
    }


    /**
     * Add a new hop to the transformation.
     *
     * @param hi The hop to be added.
     */
    public void addTransHop(TransHopMeta hi)
    {
        hops.add(hi);
        changed_hops = true;
    }

    /**
     * Add a new note to the transformation.
     *
     * @param ni The note to be added.
     */
    public void addNote(NotePadMeta ni)
    {
        notes.add(ni);
        changed_notes = true;
    }

    /**
     * Add a new dependency to the transformation.
     *
     * @param td The transformation dependency to be added.
     */
    public void addDependency(TransDependency td)
    {
        dependencies.add(td);
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#addDatabase(int, org.pentaho.di.core.database.DatabaseMeta)
     */
    public void addDatabase(int p, DatabaseMeta ci)
    {
        databases.add(p, ci);
    }

    /**
     * Add a new step to the transformation
     *
     * @param p The location
     * @param stepMeta The step to be added.
     */
    public void addStep(int p, StepMeta stepMeta)
    {
        steps.add(p, stepMeta);
        stepMeta.setParentTransMeta(this);
        changed_steps = true;
    }

    /**
     * Add a new hop to the transformation on a certain location.
     *
     * @param p the location
     * @param hi The hop to be added.
     */
    public void addTransHop(int p, TransHopMeta hi)
    {
        hops.add(p, hi);
        changed_hops = true;
    }

    /**
     * Add a new note to the transformation on a certain location.
     *
     * @param p The location
     * @param ni The note to be added.
     */
    public void addNote(int p, NotePadMeta ni)
    {
        notes.add(p, ni);
        changed_notes = true;
    }

    /**
     * Add a new dependency to the transformation on a certain location
     *
     * @param p The location.
     * @param td The transformation dependency to be added.
     */
    public void addDependency(int p, TransDependency td)
    {
        dependencies.add(p, td);
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#getDatabase(int)
     */
    public DatabaseMeta getDatabase(int i)
    {
        return databases.get(i);
    }

    /**
     * Get an ArrayList of defined steps.
     *
     * @return an ArrayList of defined steps.
     */
    public List<StepMeta> getSteps()
    {
        return steps;
    }

    /**
     * Retrieves a step on a certain location.
     *
     * @param i The location.
     * @return The step information.
     */
    public StepMeta getStep(int i)
    {
        return steps.get(i);
    }

    /**
     * Retrieves a hop on a certain location.
     *
     * @param i The location.
     * @return The hop information.
     */
    public TransHopMeta getTransHop(int i)
    {
        return hops.get(i);
    }

    /**
     * Retrieves notepad information on a certain location.
     *
     * @param i The location
     * @return The notepad information.
     */
    public NotePadMeta getNote(int i)
    {
        return notes.get(i);
    }

    /**
     * Retrieves a dependency on a certain location.
     *
     * @param i The location.
     * @return The dependency.
     */
    public TransDependency getDependency(int i)
    {
        return dependencies.get(i);
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#removeDatabase(int)
     */
    public void removeDatabase(int i)
    {
        if (i < 0 || i >= databases.size()) return;
        databases.remove(i);
        changed_databases = true;
    }

    /**
     * Removes a step from the transformation on a certain location.
     *
     * @param i The location
     */
    public void removeStep(int i)
    {
        if (i < 0 || i >= steps.size()) return;

        steps.remove(i);
        changed_steps = true;
    }

    /**
     * Removes a hop from the transformation on a certain location.
     *
     * @param i The location
     */
    public void removeTransHop(int i)
    {
        if (i < 0 || i >= hops.size()) return;

        hops.remove(i);
        changed_hops = true;
    }

    /**
     * Removes a note from the transformation on a certain location.
     *
     * @param i The location
     */
    public void removeNote(int i)
    {
        if (i < 0 || i >= notes.size()) return;
        notes.remove(i);
        changed_notes = true;
    }

    
    
    public void raiseNote(int p)
    {
    	// if valid index and not last index
    	if ((p >=0) && (p < notes.size()-1))
    	{
    		NotePadMeta note = notes.remove(p);
    		notes.add(note);
            changed_notes = true;
    	}
    }
    
    public void lowerNote(int p)
    {
    	// if valid index and not first index
    	if ((p >0) && (p < notes.size()))
    	{
    		NotePadMeta note = notes.remove(p);
    		notes.add(0, note);
            changed_notes = true;
    	}
    }
    
    /**
     * Removes a dependency from the transformation on a certain location.
     *
     * @param i The location
     */
    public void removeDependency(int i)
    {
        if (i < 0 || i >= dependencies.size()) return;
        dependencies.remove(i);
    }

    /**
     * Clears all the dependencies from the transformation.
     */
    public void removeAllDependencies()
    {
        dependencies.clear();
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#nrDatabases()
     */
    public int nrDatabases()
    {
        return databases.size();
    }

    /**
     * Count the nr of steps in the transformation.
     *
     * @return The nr of steps
     */
    public int nrSteps()
    {
        return steps.size();
    }

    /**
     * Count the nr of hops in the transformation.
     *
     * @return The nr of hops
     */
    public int nrTransHops()
    {
        return hops.size();
    }

    /**
     * Count the nr of notes in the transformation.
     *
     * @return The nr of notes
     */
    public int nrNotes()
    {
        return notes.size();
    }

    /**
     * Count the nr of dependencies in the transformation.
     *
     * @return The nr of dependencies
     */
    public int nrDependencies()
    {
        return dependencies.size();
    }

    /**
     * Changes the content of a step on a certain position
     *
     * @param i The position
     * @param stepMeta The Step
     */
    public void setStep(int i, StepMeta stepMeta)
    {
        steps.set(i, stepMeta);
        stepMeta.setParentTransMeta(this);
    }

    /**
     * Changes the content of a hop on a certain position
     *
     * @param i The position
     * @param hi The hop
     */
    public void setTransHop(int i, TransHopMeta hi)
    {
        hops.set(i, hi);
    }

    /**
     * @return a list with all the used steps
     */
    public List<StepMeta> getUsedSteps() {
    	List<StepMeta> list = new ArrayList<StepMeta>();
    	
    	for (StepMeta stepMeta : steps) {
    		if (isStepUsedInTransHops(stepMeta)) {
    			list.add(stepMeta);
    		}
    	}
    	
    	return list;
    }
    

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#findDatabase(java.lang.String)
     */
    public DatabaseMeta findDatabase(String name)
    {
        int i;
        for (i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.getName().equalsIgnoreCase(name)) { return ci; }
        }
        return null;
    }

    /**
     * Searches the list of steps for a step with a certain name
     *
     * @param name The name of the step to look for
     * @return The step information or null if no nothing was found.
     */
    public StepMeta findStep(String name)
    {
        return findStep(name, null);
    }

    /**
     * Searches the list of steps for a step with a certain name while excluding one step.
     *
     * @param name The name of the step to look for
     * @param exclude The step information to exclude.
     * @return The step information or null if nothing was found.
     */
    public StepMeta findStep(String name, StepMeta exclude)
    {
        if (name==null) return null;

        int excl = -1;
        if (exclude != null) excl = indexOfStep(exclude);

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (i != excl && stepMeta.getName().equalsIgnoreCase(name)) { return stepMeta; }
        }
        return null;
    }

    /**
     * Searches the list of hops for a hop with a certain name
     *
     * @param name The name of the hop to look for
     * @return The hop information or null if nothing was found.
     */
    public TransHopMeta findTransHop(String name)
    {
        int i;

        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.toString().equalsIgnoreCase(name)) { return hi; }
        }
        return null;
    }

    /**
     * Search all hops for a hop where a certain step is at the start.
     *
     * @param fromstep The step at the start of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHopFrom(StepMeta fromstep)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getFromStep() != null && hi.getFromStep().equals(fromstep)) // return the first
            { return hi; }
        }
        return null;
    }

    /**
     * Find a certain hop in the transformation..
     *
     * @param hi The hop information to look for.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHop(TransHopMeta hi)
    {
        return findTransHop(hi.getFromStep(), hi.getToStep());
    }
    
    /**
     * Search all hops for a hop where a certain step is at the start and another is at the end.
     *
     * @param from The step at the start of the hop.
     * @param to The step at the end of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHop(StepMeta from, StepMeta to)
    {
        return findTransHop(from, to, false);
    }    

    /**
     * Search all hops for a hop where a certain step is at the start and another is at the end.
     *
     * @param from The step at the start of the hop.
     * @param to The step at the end of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHop(StepMeta from, StepMeta to, boolean disabledToo)
    {

        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() || disabledToo)
            {
                if (hi.getFromStep() != null && hi.getToStep() != null && hi.getFromStep().equals(from) && hi.getToStep().equals(to)) { return hi; }
            }
        }
        return null;
    }

    /**
     * Search all hops for a hop where a certain step is at the end.
     *
     * @param tostep The step at the end of the hop.
     * @return The hop or null if no hop was found.
     */
    public TransHopMeta findTransHopTo(StepMeta tostep)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.getToStep().equals(tostep)) // Return the first!
            { return hi; }
        }
        return null;
    }

    /**
     * Determines whether or not a certain step is informative. This means that the previous step is sending information
     * to this step, but only informative. This means that this step is using the information to process the actual
     * stream of data. We use this in StreamLookup, TableInput and other types of steps.
     *
     * @param this_step The step that is receiving information.
     * @param prev_step The step that is sending information
     * @return true if prev_step if informative for this_step.
     */
    public boolean isStepInformative(StepMeta this_step, StepMeta prev_step)
    {
        String[] infoSteps = this_step.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
        if (infoSteps == null) return false;
        for (int i = 0; i < infoSteps.length; i++)
        {
            if (prev_step.getName().equalsIgnoreCase(infoSteps[i])) return true;
        }

        return false;
    }

    /**
     * Counts the number of previous steps for a step name.
     *
     * @param stepname The name of the step to start from
     * @return The number of preceding steps.
     */
    public int findNrPrevSteps(String stepname)
    {
        return findNrPrevSteps(findStep(stepname), false);
    }

    /**
     * Counts the number of previous steps for a step name taking into account whether or not they are informational.
     *
     * @param stepname The name of the step to start from
     * @return The number of preceding steps.
     */
    public int findNrPrevSteps(String stepname, boolean info)
    {
        return findNrPrevSteps(findStep(stepname), info);
    }

    /**
     * Find the number of steps that precede the indicated step.
     *
     * @param stepMeta The source step
     *
     * @return The number of preceding steps found.
     */
    public int findNrPrevSteps(StepMeta stepMeta)
    {
        return findNrPrevSteps(stepMeta, false);
    }

    /**
     * Find the previous step on a certain location.
     *
     * @param stepname The source step name
     * @param nr the location
     *
     * @return The preceding step found.
     */
    public StepMeta findPrevStep(String stepname, int nr)
    {
        return findPrevStep(findStep(stepname), nr);
    }

    /**
     * Find the previous step on a certain location taking into account the steps being informational or not.
     *
     * @param stepname The name of the step
     * @param nr The location
     * @param info true if we only want the informational steps.
     * @return The step information
     */
    public StepMeta findPrevStep(String stepname, int nr, boolean info)
    {
        return findPrevStep(findStep(stepname), nr, info);
    }

    /**
     * Find the previous step on a certain location.
     *
     * @param stepMeta The source step information
     * @param nr the location
     *
     * @return The preceding step found.
     */
    public StepMeta findPrevStep(StepMeta stepMeta, int nr)
    {
        return findPrevStep(stepMeta, nr, false);
    }

    /**
     * Count the number of previous steps on a certain location taking into account the steps being informational or
     * not.
     *
     * @param stepMeta The name of the step
     * @param info true if we only want the informational steps.
     * @return The number of preceding steps
     * @deprecated please use method findPreviousSteps
     */
    public int findNrPrevSteps(StepMeta stepMeta, boolean info)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                // Check if this previous step isn't informative (StreamValueLookup)
                // We don't want fields from this stream to show up!
                if (info || !isStepInformative(stepMeta, hi.getFromStep()))
                {
                    count++;
                }
            }
        }
        return count;
    }
    

    /**
     * Find the previous step on a certain location taking into account the steps being informational or not.
     *
     * @param stepMeta The step
     * @param nr The location
     * @param info true if we only want the informational steps.
     * @return The preceding step information
     * @deprecated please use method findPreviousSteps
     */
    public StepMeta findPrevStep(StepMeta stepMeta, int nr, boolean info)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                if (info || !isStepInformative(stepMeta, hi.getFromStep()))
                {
                    if (count == nr) { return hi.getFromStep(); }
                    count++;
                }
            }
        }
        return null;
    }
    
    /**
     * Get the list of previous steps for a certain reference step.  This includes the info steps.
     *
     * @param stepMeta The reference step
     * @return The list of the preceding steps, including the info steps.
     */
    public List<StepMeta> findPreviousSteps(StepMeta stepMeta)
    {
    	return findPreviousSteps(stepMeta, true);
    }
    
    /**
     * Get the previous steps on a certain location taking into account the steps being informational or
     * not.
     *
     * @param stepMeta The name of the step
     * @param info true if we only want the informational steps.
     * @return The list of the preceding steps
     */
    public List<StepMeta> findPreviousSteps(StepMeta stepMeta, boolean info)
    {
    	List<StepMeta> previousSteps = new ArrayList<StepMeta>();
    	
    	for (TransHopMeta hi : hops)
        {
            if (hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                // Check if this previous step isn't informative (StreamValueLookup)
                // We don't want fields from this stream to show up!
                if (info || !isStepInformative(stepMeta, hi.getFromStep()))
                {
                    previousSteps.add(hi.getFromStep());
                }
            }
        }
        return previousSteps;
    }

    /**
     * Get the informational steps for a certain step. An informational step is a step that provides information for
     * lookups etc.
     *
     * @param stepMeta The name of the step
     * @return The informational steps found
     */
    public StepMeta[] getInfoStep(StepMeta stepMeta)
    {
        String[] infoStepName = stepMeta.getStepMetaInterface().getStepIOMeta().getInfoStepnames();
        if (infoStepName == null) return null;

        StepMeta[] infoStep = new StepMeta[infoStepName.length];
        for (int i = 0; i < infoStep.length; i++)
        {
            infoStep[i] = findStep(infoStepName[i]);
        }

        return infoStep;
    }

    /**
     * Find the the number of informational steps for a certains step.
     *
     * @param stepMeta The step
     * @return The number of informational steps found.
     */
    public int findNrInfoSteps(StepMeta stepMeta)
    {
        if (stepMeta == null) return 0;

        int count = 0;

        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi == null || hi.getToStep() == null)
            {
                log.logError(BaseMessages.getString(PKG, "TransMeta.Log.DestinationOfHopCannotBeNull")); //$NON-NLS-1$
            }
            if (hi != null && hi.getToStep() != null && hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                // Check if this previous step isn't informative (StreamValueLookup)
                // We don't want fields from this stream to show up!
                if (isStepInformative(stepMeta, hi.getFromStep()))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Find the informational fields coming from an informational step into the step specified.
     *
     * @param stepname The name of the step
     * @return A row containing fields with origin.
     */
    public RowMetaInterface getPrevInfoFields(String stepname) throws KettleStepException
    {
        return getPrevInfoFields(findStep(stepname));
    }

    /**
     * Find the informational fields coming from an informational step into the step specified.
     *
     * @param stepMeta The receiving step
     * @return A row containing fields with origin.
     */
    public RowMetaInterface getPrevInfoFields(StepMeta stepMeta) throws KettleStepException
    {
        RowMetaInterface row = new RowMeta();

        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getToStep().equals(stepMeta))
            {
                StepMeta infoStep = hi.getFromStep();
                if (isStepInformative(stepMeta, infoStep))
                {
                    row = getPrevStepFields(infoStep);
                    getThisStepFields(infoStep, stepMeta, row);
                    return row;
                }
            }
        }
        return row;
    }

    /**
     * Find the number of succeeding steps for a certain originating step.
     *
     * @param stepMeta The originating step
     * @return The number of succeeding steps.
     * @deprecated just get the next steps as an array
     */
    public int findNrNextSteps(StepMeta stepMeta)
    {
        int count = 0;
        int i;
        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta)) count++;
        }
        return count;
    }

    /**
     * Find the succeeding step at a location for an originating step.
     *
     * @param stepMeta The originating step
     * @param nr The location
     * @return The step found.
     * @deprecated just get the next steps as an array
     */
    public StepMeta findNextStep(StepMeta stepMeta, int nr)
    {
        int count = 0;
        int i;

        for (i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta))
            {
                if (count == nr) { return hi.getToStep(); }
                count++;
            }
        }
        return null;
    }

    /**
     * Retrieve an array of preceding steps for a certain destination step. This includes the info steps.
     *
     * @param stepMeta The destination step
     * @return An array containing the preceding steps.
     */
    public StepMeta[] getPrevSteps(StepMeta stepMeta)
    {
       	List<StepMeta> prevSteps = new ArrayList<StepMeta>();
        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hopMeta = getTransHop(i);
            if (hopMeta.isEnabled() && hopMeta.getToStep().equals(stepMeta))
            {
                prevSteps.add(hopMeta.getFromStep());
            }
        }
        
        return prevSteps.toArray(new StepMeta[prevSteps.size()]);
    }

    /**
     * Retrieve an array of succeeding step names for a certain originating step name.
     *
     * @param stepname The originating step name
     * @return An array of succeeding step names
     */
    public String[] getPrevStepNames(String stepname)
    {
        return getPrevStepNames(findStep(stepname));
    }

    /**
     * Retrieve an array of preceding steps for a certain destination step.
     *
     * @param stepMeta The destination step
     * @return an array of preceding step names.
     */
    public String[] getPrevStepNames(StepMeta stepMeta)
    {
        StepMeta prevStepMetas[] = getPrevSteps(stepMeta);
        String retval[] = new String[prevStepMetas.length];
        for (int x = 0; x < prevStepMetas.length; x++)
            retval[x] = prevStepMetas[x].getName();

        return retval;
    }

    /**
     * Retrieve an array of succeeding steps for a certain originating step.
     *
     * @param stepMeta The originating step
     * @return an array of succeeding steps.
     * @deprecated use findNextSteps instead
     */
    public StepMeta[] getNextSteps(StepMeta stepMeta)
    {
    	List<StepMeta> nextSteps = new ArrayList<StepMeta>();
        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta))
            {
                nextSteps.add(hi.getToStep());
            }
        }
        
        return nextSteps.toArray(new StepMeta[nextSteps.size()]);
    }

    /**
     * Retrieve a list of succeeding steps for a certain originating step.
     *
     * @param stepMeta The originating step
     * @return an array of succeeding steps.
     */
    public List<StepMeta> findNextSteps(StepMeta stepMeta)
    {
    	List<StepMeta> nextSteps = new ArrayList<StepMeta>();
        for (int i = 0; i < nrTransHops(); i++) // Look at all the hops;
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.isEnabled() && hi.getFromStep().equals(stepMeta))
            {
                nextSteps.add(hi.getToStep());
            }
        }
        
        return nextSteps;
    }
    
    /**
     * Retrieve an array of succeeding step names for a certain originating step.
     *
     * @param stepMeta The originating step
     * @return an array of succeeding step names.
     */
    public String[] getNextStepNames(StepMeta stepMeta)
    {
        StepMeta nextStepMeta[] = getNextSteps(stepMeta);
        String retval[] = new String[nextStepMeta.length];
        for (int x = 0; x < nextStepMeta.length; x++)
            retval[x] = nextStepMeta[x].getName();

        return retval;
    }

    /**
     * Find the step that is located on a certain point on the canvas, taking into account the icon size.
     *
     * @param x the x-coordinate of the point queried
     * @param y the y-coordinate of the point queried
     * @return The step information if a step is located at the point. Otherwise, if no step was found: null.
     */
    public StepMeta getStep(int x, int y, int iconsize)
    {
        int i, s;
        s = steps.size();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            StepMeta stepMeta = steps.get(i);
            if (partOfTransHop(stepMeta) || stepMeta.isDrawn()) // Only consider steps from active or inactive hops!
            {
                Point p = stepMeta.getLocation();
                if (p != null)
                {
                    if (x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize + 20) { return stepMeta; }
                }
            }
        }
        return null;
    }

    /**
     * Find the note that is located on a certain point on the canvas.
     *
     * @param x the x-coordinate of the point queried
     * @param y the y-coordinate of the point queried
     * @return The note information if a note is located at the point. Otherwise, if nothing was found: null.
     */
    public NotePadMeta getNote(int x, int y)
    {
        int i, s;
        s = notes.size();
        for (i = s - 1; i >= 0; i--) // Back to front because drawing goes from start to end
        {
            NotePadMeta ni = notes.get(i);
            Point loc = ni.getLocation();
            Point p = new Point(loc.x, loc.y);
            if (x >= p.x && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y && y <= p.y + ni.height + 2 * Const.NOTE_MARGIN) { return ni; }
        }
        return null;
    }

    /**
     * Determines whether or not a certain step is part of a hop.
     *
     * @param stepMeta The step queried
     * @return true if the step is part of a hop.
     */
    public boolean partOfTransHop(StepMeta stepMeta)
    {
        int i;
        for (i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.getFromStep() == null || hi.getToStep() == null) return false;
            if (hi.getFromStep().equals(stepMeta) || hi.getToStep().equals(stepMeta)) return true;
        }
        return false;
    }
    

    /**
     * Returns the fields that are emitted by a certain step name
     *
     * @param stepname The stepname of the step to be queried.
     * @return A row containing the fields emitted.
     */
    public RowMetaInterface getStepFields(String stepname) throws KettleStepException
    {
        StepMeta stepMeta = findStep(stepname);
        if (stepMeta != null)
            return getStepFields(stepMeta);
        else
            return null;
    }

    /**
     * Returns the fields that are emitted by a certain step
     *
     * @param stepMeta The step to be queried.
     * @return A row containing the fields emitted.
     */
    public RowMetaInterface getStepFields(StepMeta stepMeta) throws KettleStepException
    {
        return getStepFields(stepMeta, null);
    }

    public RowMetaInterface getStepFields(StepMeta[] stepMeta) throws KettleStepException
    {
        RowMetaInterface fields = new RowMeta();

        for (int i = 0; i < stepMeta.length; i++)
        {
            RowMetaInterface flds = getStepFields(stepMeta[i]);
            if (flds != null) fields.mergeRowMeta(flds);
        }
        return fields;
    }

    /**
     * Returns the fields that are emitted by a certain step
     *
     * @param stepMeta The step to be queried.
     * @param monitor The progress monitor for progress dialog. (null if not used!)
     * @return A row containing the fields emitted.
     */
    public RowMetaInterface getStepFields(StepMeta stepMeta, ProgressMonitorListener monitor) throws KettleStepException
    {
    	clearStepFieldsCachce();
        return getStepFields(stepMeta, null, monitor);
    }
    
    /**
     * Returns the fields that are emitted by a certain step
     *
     * @param stepMeta The step to be queried.
     * @param targetStep the target step 
     * @param monitor The progress monitor for progress dialog. (null if not used!)
     * @return A row containing the fields emitted.
     */
    public RowMetaInterface getStepFields(StepMeta stepMeta, StepMeta targetStep, ProgressMonitorListener monitor) throws KettleStepException
    {
        RowMetaInterface row = new RowMeta();

        if (stepMeta == null) return row;
        
        String fromToCacheEntry = stepMeta.getName()+ ( targetStep!=null ? ("-"+targetStep.getName()) : "" );
        RowMetaInterface rowMeta = stepsFieldsCache.get(fromToCacheEntry);
        if (rowMeta!=null) {
        	return rowMeta;
        }

        // See if the step is sending ERROR rows to the specified target step.
        //
        if (targetStep!=null && stepMeta.isSendingErrorRowsToStep(targetStep))
        {
            // The error rows are the same as the input rows for 
            // the step but with the selected error fields added
            //
            row = getPrevStepFields(stepMeta);
            
            // Add to this the error fields...
            StepErrorMeta stepErrorMeta = stepMeta.getStepErrorMeta();
            row.addRowMeta(stepErrorMeta.getErrorFields());
            
            // Store this row in the cache
            //
            stepsFieldsCache.put(fromToCacheEntry, row);
            
            return row;
        }
        
        // Resume the regular program...

        if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.FromStepALookingAtPreviousStep", stepMeta.getName(), String.valueOf(findNrPrevSteps(stepMeta)) )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < findNrPrevSteps(stepMeta); i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i);

            if (monitor != null)
            {
                monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.CheckingStepTask.Title", prevStepMeta.getName() )); //$NON-NLS-1$ //$NON-NLS-2$
            }

            RowMetaInterface add = getStepFields(prevStepMeta, stepMeta, monitor);
            if (add == null) add = new RowMeta();
            if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.FoundFieldsToAdd") + add.toString()); //$NON-NLS-1$
            if (i == 0)
            {
                row.addRowMeta(add);
            }
            else
            {
                // See if the add fields are not already in the row
                for (int x = 0; x < add.size(); x++)
                {
                    ValueMetaInterface v = add.getValueMeta(x);
                    ValueMetaInterface s = row.searchValueMeta(v.getName());
                    if (s == null)
                    {
                        row.addValueMeta(v);
                    }
                }
            }
        }
        
        // Finally, see if we need to add/modify/delete fields with this step "name"
        rowMeta = getThisStepFields(stepMeta, targetStep, row, monitor);
        
        // Store this row in the cache
        //
        stepsFieldsCache.put(fromToCacheEntry, rowMeta);

        return rowMeta;
    }

    /**
     * Find the fields that are entering a step with a certain name.
     *
     * @param stepname The name of the step queried
     * @return A row containing the fields (w/ origin) entering the step
     */
    public RowMetaInterface getPrevStepFields(String stepname) throws KettleStepException
    {
    	clearStepFieldsCachce();
        return getPrevStepFields(findStep(stepname));
    }

    /**
     * Find the fields that are entering a certain step.
     *
     * @param stepMeta The step queried
     * @return A row containing the fields (w/ origin) entering the step
     */
    public RowMetaInterface getPrevStepFields(StepMeta stepMeta) throws KettleStepException
    {
    	clearStepFieldsCachce();
        return getPrevStepFields(stepMeta, null);
    }

    /**
     * Find the fields that are entering a certain step.
     *
     * @param stepMeta The step queried
     * @param monitor The progress monitor for progress dialog. (null if not used!)
     * @return A row containing the fields (w/ origin) entering the step
     */
    public RowMetaInterface getPrevStepFields(StepMeta stepMeta, ProgressMonitorListener monitor) throws KettleStepException
    {
    	clearStepFieldsCachce();

        RowMetaInterface row = new RowMeta();

        if (stepMeta == null) { return null; }

        if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.FromStepALookingAtPreviousStep", stepMeta.getName(), String.valueOf(findNrPrevSteps(stepMeta)) )); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        for (int i = 0; i < findNrPrevSteps(stepMeta); i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i);

            if (monitor != null)
            {
                monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.CheckingStepTask.Title", prevStepMeta.getName() )); //$NON-NLS-1$ //$NON-NLS-2$
            }
            
            RowMetaInterface add = getStepFields(prevStepMeta, stepMeta, monitor);
            
            if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.FoundFieldsToAdd2") + add.toString()); //$NON-NLS-1$
            if (i == 0) // we expect all input streams to be of the same layout!
            {
                row.addRowMeta(add); // recursive!
            }
            else
            {
                // See if the add fields are not already in the row
                for (int x = 0; x < add.size(); x++)
                {
                    ValueMetaInterface v = add.getValueMeta(x);
                    ValueMetaInterface s = row.searchValueMeta(v.getName());
                    if (s == null)
                    {
                        row.addValueMeta(v);
                    }
                }
            }
        }
        return row;
    }

    /**
     * Return the fields that are emitted by a step with a certain name
     *
     * @param stepname The name of the step that's being queried.
     * @param row A row containing the input fields or an empty row if no input is required.
     * @return A Row containing the output fields.
     */
    public RowMetaInterface getThisStepFields(String stepname, RowMetaInterface row) throws KettleStepException
    {
        return getThisStepFields(findStep(stepname), null, row);
    }

    /**
     * Returns the fields that are emitted by a step
     *
     * @param stepMeta : The StepMeta object that's being queried
     * @param nextStep : if non-null this is the next step that's call back to ask what's being sent
     * @param row : A row containing the input fields or an empty row if no input is required.
     *
     * @return A Row containing the output fields.
     */
    public RowMetaInterface getThisStepFields(StepMeta stepMeta, StepMeta nextStep, RowMetaInterface row) throws KettleStepException
    {
        return getThisStepFields(stepMeta, nextStep, row, null);
    }

    /**
     * Returns the fields that are emitted by a step
     *
     * @param stepMeta : The StepMeta object that's being queried
     * @param nextStep : if non-null this is the next step that's call back to ask what's being sent
     * @param row : A row containing the input fields or an empty row if no input is required.
     *
     * @return A Row containing the output fields.
     */
    public RowMetaInterface getThisStepFields(StepMeta stepMeta, StepMeta nextStep, RowMetaInterface row, ProgressMonitorListener monitor) throws KettleStepException
    {
        // Then this one.
    	if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.GettingFieldsFromStep",stepMeta.getName(), stepMeta.getStepID())); //$NON-NLS-1$ //$NON-NLS-2$
        String name = stepMeta.getName();

        if (monitor != null)
        {
            monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.GettingFieldsFromStepTask.Title", name )); //$NON-NLS-1$ //$NON-NLS-2$
        }

        StepMetaInterface stepint = stepMeta.getStepMetaInterface();
        RowMetaInterface inform[] = null;
        StepMeta[] lu = getInfoStep(stepMeta);
        if (Const.isEmpty(lu))
        {
            inform = new RowMetaInterface[] { stepint.getTableFields(), };
        }
        else
        {
            inform = new RowMetaInterface[lu.length];
            for (int i=0;i<lu.length;i++) inform[i] = getStepFields(lu[i]);
        }

        // Set the Repository object on the Mapping step
        // That way the mapping step can determine the output fields for repository hosted mappings...
        // This is the exception to the rule so we don't pass this through the getFields() method.
        //
        for (StepMeta step : steps)
        {
        	if (step.getStepMetaInterface() instanceof MappingMeta) 
        	{
        		((MappingMeta)step.getStepMetaInterface()).setRepository(repository);
        	}
        }
        
        // Go get the fields...
        //
        stepint.getFields(row, name, inform, nextStep, this);

        return row;
    }
   
    public boolean isUsingPartitionSchema(PartitionSchema partitionSchema)
    {
        // Loop over all steps and see if the partition schema is used.
        for (int i=0;i<nrSteps();i++)
        {
            StepPartitioningMeta stepPartitioningMeta = getStep(i).getStepPartitioningMeta();
            if (stepPartitioningMeta!=null)
            {
                PartitionSchema check = stepPartitioningMeta.getPartitionSchema();
                if (check!=null && check.equals(partitionSchema))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isUsingClusterSchema(ClusterSchema clusterSchema)
    {
        // Loop over all steps and see if the partition schema is used.
        for (int i=0;i<nrSteps();i++)
        {
            ClusterSchema check = getStep(i).getClusterSchema();
            if (check!=null && check.equals(clusterSchema))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isUsingSlaveServer(SlaveServer slaveServer) throws KettleException
    {
        // Loop over all steps and see if the slave server is used.
        for (int i=0;i<nrSteps();i++)
        {
            ClusterSchema clusterSchema = getStep(i).getClusterSchema();
            if (clusterSchema!=null)
            {
                for (SlaveServer check : clusterSchema.getSlaveServers())
                {
                    if (check.equals(slaveServer))
                    {
                        return true;
                    }
                }
                return true;
            }
        }
        return false;
    }
        
    public boolean isRepReference() {
    	return isRepReference(getFilename(), this.getName());
    }
    
    public boolean isFileReference() {
    	return !isRepReference(getFilename(), this.getName());
    }
    
    public static boolean isRepReference(String exactFilename, String exactTransname) {
		return Const.isEmpty(exactFilename) && !Const.isEmpty(exactTransname);
    }
    
    public static boolean isFileReference(String exactFilename, String exactTransname) {
		return !isRepReference(exactFilename, exactTransname);
    }
    

    /**
     * Find the location of hop
     *
     * @param hi The hop queried
     * @return The location of the hop, -1 if nothing was found.
     */
    public int indexOfTransHop(TransHopMeta hi)
    {
        return hops.indexOf(hi);
    }

    /**
     * Find the location of step
     *
     * @param stepMeta The step queried
     * @return The location of the step, -1 if nothing was found.
     */
    public int indexOfStep(StepMeta stepMeta)
    {
        return steps.indexOf(stepMeta);
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#indexOfDatabase(org.pentaho.di.core.database.DatabaseMeta)
     */
    public int indexOfDatabase(DatabaseMeta ci)
    {
        return databases.indexOf(ci);
    }

    /**
     * Find the location of a note
     *
     * @param ni The note queried
     * @return The location of the note, -1 if nothing was found.
     */
    public int indexOfNote(NotePadMeta ni)
    {
        return notes.indexOf(ni);
    }

	public String getFileType() {
		return LastUsedFile.FILE_TYPE_TRANSFORMATION;
	}
	
	public String[] getFilterNames() {
		return Const.getTransformationFilterNames();
	}
	
    public String[] getFilterExtensions() {
    	return Const.STRING_TRANS_FILTER_EXT;
    }

    public String getDefaultExtension() {
    	return Const.STRING_TRANS_DEFAULT_EXT;
    }

    public String getXML() throws KettleException
    {
    	return getXML(true, true, true, true, true);
    }

    public String getXML(
    		boolean includeSteps,
    		boolean includeDatabase, 
    		boolean includeSlaves,
    		boolean includeClusters,
    		boolean includePartitions) throws KettleException
    {
        Props props = null;
        if (Props.isInitialized()) props=Props.getInstance();

        StringBuilder retval = new StringBuilder(800);

        retval.append(XMLHandler.openTag(XML_TAG)).append(Const.CR); //$NON-NLS-1$

        retval.append("  ").append(XMLHandler.openTag(XML_TAG_INFO)).append(Const.CR); //$NON-NLS-1$

        retval.append("    ").append(XMLHandler.addTagValue("name", name)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("description", description)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append("    ").append(XMLHandler.addTagValue("extended_description", extended_description)); 
		retval.append("    ").append(XMLHandler.addTagValue("trans_version", trans_version));
        retval.append("    ").append(XMLHandler.addTagValue("trans_type", transformationType.getCode())); //$NON-NLS-1$

		if ( trans_status >= 0 )
		{
		    retval.append("    ").append(XMLHandler.addTagValue("trans_status", trans_status));
		}
        retval.append("    ").append(XMLHandler.addTagValue("directory", directory != null ? directory.getPath() : RepositoryDirectory.DIRECTORY_SEPARATOR)); //$NON-NLS-1$ //$NON-NLS-2$

        retval.append("    ").append(XMLHandler.openTag(XML_TAG_PARAMETERS)).append(Const.CR); //$NON-NLS-1$
        String[] parameters = listParameters();
        for (int idx = 0; idx < parameters.length; idx++)
        {
        	retval.append("        ").append(XMLHandler.openTag("parameter")).append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$
        	retval.append("            ").append(XMLHandler.addTagValue("name", parameters[idx])); //$NON-NLS-1$
        	retval.append("            ").append(XMLHandler.addTagValue("default_value", getParameterDefault(parameters[idx]))); //$NON-NLS-1$
        	retval.append("            ").append(XMLHandler.addTagValue("description", getParameterDescription(parameters[idx]))); //$NON-NLS-1$
        	retval.append("        ").append(XMLHandler.closeTag("parameter")).append(Const.CR); //$NON-NLS-1$ //$NON-NLS-2$        	
        }        
        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_PARAMETERS)).append(Const.CR); //$NON-NLS-1$
        
        retval.append("    <log>").append(Const.CR); //$NON-NLS-1$
        
        // Add the metadata for the various logging tables
        //
        retval.append(transLogTable.getXML());
        retval.append(performanceLogTable.getXML());
        retval.append(channelLogTable.getXML());
        retval.append(stepLogTable.getXML());
        
        retval.append("    </log>").append(Const.CR); //$NON-NLS-1$
        retval.append("    <maxdate>").append(Const.CR); //$NON-NLS-1$
        retval.append("      ").append(XMLHandler.addTagValue("connection", maxDateConnection == null ? "" : maxDateConnection.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append("      ").append(XMLHandler.addTagValue("table", maxDateTable)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("field", maxDateField)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("offset", maxDateOffset)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("      ").append(XMLHandler.addTagValue("maxdiff", maxDateDifference)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    </maxdate>").append(Const.CR); //$NON-NLS-1$
        
        retval.append("    ").append(XMLHandler.addTagValue("size_rowset", sizeRowset)); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append("    ").append(XMLHandler.addTagValue("sleep_time_empty", sleepTimeEmpty)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("sleep_time_full", sleepTimeFull)); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append("    ").append(XMLHandler.addTagValue("unique_connections", usingUniqueConnections)); //$NON-NLS-1$ //$NON-NLS-2$
        
        retval.append("    ").append(XMLHandler.addTagValue("feedback_shown", feedbackShown)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("feedback_size", feedbackSize)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append("    ").append(XMLHandler.addTagValue("using_thread_priorities", usingThreadPriorityManagment)); // $NON-NLS-1$
        retval.append("    ").append(XMLHandler.addTagValue("shared_objects_file", sharedObjectsFile)); // $NON-NLS-1$

		// Performance monitoring
        //
        retval.append("    ").append(XMLHandler.addTagValue("capture_step_performance", capturingStepPerformanceSnapShots)); // $NON-NLS-1$
        retval.append("    ").append(XMLHandler.addTagValue("step_performance_capturing_delay", stepPerformanceCapturingDelay)); // $NON-NLS-1$
        retval.append("    ").append(XMLHandler.addTagValue("step_performance_capturing_size_limit", stepPerformanceCapturingSizeLimit)); // $NON-NLS-1$

        retval.append("    ").append(XMLHandler.openTag(XML_TAG_DEPENDENCIES)).append(Const.CR); //$NON-NLS-1$
        for (int i = 0; i < nrDependencies(); i++)
        {
            TransDependency td = getDependency(i);
            retval.append(td.getXML());
        }
        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_DEPENDENCIES)).append(Const.CR); //$NON-NLS-1$

        // The partitioning schemas...
        //
        if (includePartitions) {
	        retval.append("    ").append(XMLHandler.openTag(XML_TAG_PARTITIONSCHEMAS)).append(Const.CR); //$NON-NLS-1$
	        for (int i = 0; i < partitionSchemas.size(); i++)
	        {
	            PartitionSchema partitionSchema = partitionSchemas.get(i);
	            retval.append(partitionSchema.getXML());
	        }
	        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_PARTITIONSCHEMAS)).append(Const.CR); //$NON-NLS-1$
        }        
        // The slave servers...
        //
        if (includeSlaves) {
	        retval.append("    ").append(XMLHandler.openTag(XML_TAG_SLAVESERVERS)).append(Const.CR); //$NON-NLS-1$
	        for (int i = 0; i < slaveServers.size(); i++)
	        {
	            SlaveServer slaveServer = slaveServers.get(i);
	            retval.append("         ").append(slaveServer.getXML()).append(Const.CR);
	        }
	        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_SLAVESERVERS)).append(Const.CR); //$NON-NLS-1$
        }

        // The cluster schemas...
        //
        if (includeClusters) {
	        retval.append("    ").append(XMLHandler.openTag(XML_TAG_CLUSTERSCHEMAS)).append(Const.CR); //$NON-NLS-1$
	        for (int i = 0; i < clusterSchemas.size(); i++)
	        {
	            ClusterSchema clusterSchema = clusterSchemas.get(i);
	            retval.append(clusterSchema.getXML());
	        }
	        retval.append("    ").append(XMLHandler.closeTag(XML_TAG_CLUSTERSCHEMAS)).append(Const.CR); //$NON-NLS-1$
        }
        
        retval.append("  ").append(XMLHandler.addTagValue("modified_user", modifiedUser));
        retval.append("  ").append(XMLHandler.addTagValue("modified_date", modifiedDate));

        retval.append("  ").append(XMLHandler.closeTag(XML_TAG_INFO)).append(Const.CR); //$NON-NLS-1$
        
        retval.append("  ").append(XMLHandler.openTag(XML_TAG_NOTEPADS)).append(Const.CR); //$NON-NLS-1$
        if (notes != null) for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            retval.append(ni.getXML());
        }
        retval.append("  ").append(XMLHandler.closeTag(XML_TAG_NOTEPADS)).append(Const.CR); //$NON-NLS-1$

        // The database connections...
        if (includeDatabase) {
	        for (int i = 0; i < nrDatabases(); i++)
	        {
	            DatabaseMeta dbMeta = getDatabase(i);
	            if (props!=null && props.areOnlyUsedConnectionsSavedToXML())
	            {
	                if (isDatabaseConnectionUsed(dbMeta)) retval.append(dbMeta.getXML());
	            }
	            else
	            {
	                retval.append(dbMeta.getXML());
	            }
	        }
        }

        if (includeSteps) {
	        retval.append("  ").append(XMLHandler.openTag(XML_TAG_ORDER)).append(Const.CR); //$NON-NLS-1$
	        for (int i = 0; i < nrTransHops(); i++)
	        {
	            TransHopMeta transHopMeta = getTransHop(i);
	            retval.append(transHopMeta.getXML());
	        }
	        retval.append("  ").append(XMLHandler.closeTag(XML_TAG_ORDER)).append(Const.CR); //$NON-NLS-1$
	
	        /* The steps... */
	        for (int i = 0; i < nrSteps(); i++)
	        {
	            StepMeta stepMeta = getStep(i);
	            retval.append(stepMeta.getXML());
	        }
        
	        /* The error handling metadata on the steps */
	        retval.append("  ").append(XMLHandler.openTag(XML_TAG_STEP_ERROR_HANDLING)).append(Const.CR);
	        for (int i = 0; i < nrSteps(); i++)
	        {
	            StepMeta stepMeta = getStep(i);
	            
	            if (stepMeta.getStepErrorMeta()!=null)
	            {
	                retval.append(stepMeta.getStepErrorMeta().getXML());
	            }
	        }
	        retval.append("  ").append(XMLHandler.closeTag(XML_TAG_STEP_ERROR_HANDLING)).append(Const.CR);
        }

        // The slave-step-copy/partition distribution.  Only used for slave transformations in a clustering environment.
        retval.append("   ").append(slaveStepCopyPartitionDistribution.getXML());

        // Is this a slave transformation or not?
        retval.append("   ").append(XMLHandler.addTagValue("slave_transformation", slaveTransformation));

        retval.append("</").append(XML_TAG+">").append(Const.CR); //$NON-NLS-1$

        return retval.toString();
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     * No default connections are loaded since no repository is available at this time.
     * Since the filename is set, internal variables are being set that relate to this.
     *
     * @param fname The filename
     */
    public TransMeta(String fname) throws KettleXMLException
    {
        this(fname, true);
    }
    
    /**
     * Parse a file containing the XML that describes the transformation.
     * No default connections are loaded since no repository is available at this time.
     * Since the filename is set, internal variables are being set that relate to this.
     *
     * @param fname The filename
     * @param parentVariableSpace
     */
    public TransMeta(String fname, VariableSpace parentVariableSpace) throws KettleXMLException
    {
        this(fname, null, true, parentVariableSpace);
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     * No default connections are loaded since no repository is available at this time.
     *
     * @param fname The filename
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     */
    public TransMeta(String fname, boolean setInternalVariables) throws KettleXMLException
    {
        this(fname, null, setInternalVariables);
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param fname The filename
     * @param rep The repository to load the default set of connections from, null if no repository is available
      */
    public TransMeta(String fname, Repository rep) throws KettleXMLException
    {
        this(fname, rep, true);
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param fname The filename
     * @param rep The repository to load the default set of connections from, null if no repository is available
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
      */
    public TransMeta(String fname, Repository rep, boolean setInternalVariables ) throws KettleXMLException
    {
    	this(fname, rep, setInternalVariables, null);
    }
    
    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param fname The filename
     * @param rep The repository to load the default set of connections from, null if no repository is available
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     * @param parentVariableSpace the parent variable space to use during TransMeta construction
      */
    public TransMeta(String fname, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace ) throws KettleXMLException
    {
    	this(fname, rep, setInternalVariables, parentVariableSpace, null);
    }
 
    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param fname The filename
     * @param rep The repository to load the default set of connections from, null if no repository is available
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     * @param parentVariableSpace the parent variable space to use during TransMeta construction
     * @param prompter the changed/replace listener or null if there is none
     */
    public TransMeta(String fname, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace, OverwritePrompter prompter ) throws KettleXMLException
    {
        // OK, try to load using the VFS stuff...
        Document doc=null;
        try
        {
            doc = XMLHandler.loadXMLFile(KettleVFS.getFileObject(fname, parentVariableSpace));
        }
        catch (KettleFileException e)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", fname), e);
        }
        
        if (doc != null)
        {
            // Clear the transformation
            clearUndo();
            clear();

            // Root node:
            Node transnode = XMLHandler.getSubNode(doc, XML_TAG); //$NON-NLS-1$

            // Load from this node...
            loadXML(transnode, rep, setInternalVariables, parentVariableSpace, prompter);

            setFilename(fname);
        }
        else
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", fname)); //$NON-NLS-1$
        }
    }
        
    public TransMeta(InputStream xmlStream, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace, OverwritePrompter prompter ) throws KettleXMLException
    {
    	loadXML( XMLHandler.loadXMLFile(xmlStream, null, false, false), rep, setInternalVariables, parentVariableSpace, prompter);
    }
    	
    /**
     * Parse a file containing the XML that describes the transformation.
     * Specify a repository to load default list of database connections from and to reference in mappings etc.
     *
     * @param transnode The XML node to load from
     * @param rep the repository to reference.
     * @throws KettleXMLException
     */
    public TransMeta(Node transnode, Repository rep) throws KettleXMLException
    {
    	loadXML(transnode, rep, false);
    }
    
    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param transnode The XML node to load from
     * @param rep The repository to load the default list of database connections from (null if no repository is available)
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     * @throws KettleXMLException
     */
    public void loadXML(Node transnode, Repository rep, boolean setInternalVariables ) throws KettleXMLException
    {
    	loadXML(transnode, rep, setInternalVariables, null);
    }

    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param transnode The XML node to load from
     * @param rep The repository to load the default list of database connections from (null if no repository is available)
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     * @param parentVariableSpace the parent variable space to use during TransMeta construction
     * @throws KettleXMLException
     */
    public void loadXML(Node transnode, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace ) throws KettleXMLException
    {
    	loadXML(transnode, rep, setInternalVariables, parentVariableSpace, null);
    }
    
    /**
     * Parse a file containing the XML that describes the transformation.
     *
     * @param transnode The XML node to load from
     * @param rep The repository to load the default list of database connections from (null if no repository is available)
     * @param setInternalVariables true if you want to set the internal variables based on this transformation information
     * @param parentVariableSpace the parent variable space to use during TransMeta construction
     * @param prompter the changed/replace listener or null if there is none
     * @throws KettleXMLException
     */
    public void loadXML(Node transnode, Repository rep, boolean setInternalVariables, VariableSpace parentVariableSpace, OverwritePrompter prompter ) throws KettleXMLException
    {
        Props props = null;
        if (Props.isInitialized())
        {
            props=Props.getInstance();
        }
        
        if (parentVariableSpace!=null) {
        	initializeVariablesFrom(parentVariableSpace);
        }
        
        try
        {
            // Clear the transformation
            clearUndo();
            clear();
            
            // Read all the database connections from the repository to make sure that we don't overwrite any there by loading from XML.
            try
            {
                sharedObjectsFile = XMLHandler.getTagValue(transnode, "info", "shared_objects_file"); //$NON-NLS-1$ //$NON-NLS-2$
                sharedObjects = rep!=null ? rep.readTransSharedObjects(this) : readSharedObjects();
            }
            catch(Exception e)
            {
                log.logError(BaseMessages.getString(PKG, "TransMeta.ErrorReadingSharedObjects.Message", e.toString()));
                log.logError(Const.getStackTracker(e));
            }

            // Handle connections
            int n = XMLHandler.countNodes(transnode, DatabaseMeta.XML_TAG); //$NON-NLS-1$
            if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.WeHaveConnections", String.valueOf(n) )); //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = 0; i < n; i++)
            {
            	if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.LookingAtConnection") + i); //$NON-NLS-1$
                Node nodecon = XMLHandler.getSubNodeByNr(transnode, DatabaseMeta.XML_TAG, i); //$NON-NLS-1$

                DatabaseMeta dbcon = new DatabaseMeta(nodecon);
                dbcon.shareVariablesWith(this);

                DatabaseMeta exist = findDatabase(dbcon.getName());
                if (exist == null)
                {
                    addDatabase(dbcon);
                }
                else
                {
                    if (!exist.isShared()) // otherwise, we just keep the shared connection.
                    {
                        boolean askOverwrite = Props.isInitialized() ? props.askAboutReplacingDatabaseConnections() : false;
                        boolean overwrite = Props.isInitialized() ? props.replaceExistingDatabaseConnections() : true;
                        if (askOverwrite)
                        {
                        	if (prompter!=null) {
	                        	overwrite = prompter.overwritePrompt(
	                        			BaseMessages.getString(PKG, "TransMeta.Message.OverwriteConnectionYN",dbcon.getName()),
	                        			BaseMessages.getString(PKG, "TransMeta.Message.OverwriteConnection.DontShowAnyMoreMessage"),
	                        			Props.STRING_ASK_ABOUT_REPLACING_DATABASES
	                        		);
                        	}
                        }
    
                        if (overwrite)
                        {
                            int idx = indexOfDatabase(exist);
                            removeDatabase(idx);
                            addDatabase(idx, dbcon);
                        }
                    }
                }
            }

            // Read the notes...
            Node notepadsnode = XMLHandler.getSubNode(transnode, XML_TAG_NOTEPADS); //$NON-NLS-1$
            int nrnotes = XMLHandler.countNodes(notepadsnode, NotePadMeta.XML_TAG); //$NON-NLS-1$
            for (int i = 0; i < nrnotes; i++)
            {
                Node notepadnode = XMLHandler.getSubNodeByNr(notepadsnode, NotePadMeta.XML_TAG, i); //$NON-NLS-1$
                NotePadMeta ni = new NotePadMeta(notepadnode);
                notes.add(ni);
            }

            // Handle Steps
            int s = XMLHandler.countNodes(transnode, StepMeta.XML_TAG); //$NON-NLS-1$

            if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.ReadingSteps") + s + " steps..."); //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = 0; i < s; i++)
            {
                Node stepnode = XMLHandler.getSubNodeByNr(transnode, StepMeta.XML_TAG, i); //$NON-NLS-1$

                if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.LookingAtStep") + i); //$NON-NLS-1$
                StepMeta stepMeta = new StepMeta(stepnode, databases, counters);
                stepMeta.setParentTransMeta(this); // for tracing, retain hierarchy
                
                // Check if the step exists and if it's a shared step.
                // If so, then we will keep the shared version, not this one.
                // The stored XML is only for backup purposes.
                //
                StepMeta check = findStep(stepMeta.getName());
                if (check!=null)
                {
                    if (!check.isShared()) // Don't overwrite shared objects
                    {
                        addOrReplaceStep(stepMeta);
                    }
                    else
                    {
                        check.setDraw(stepMeta.isDrawn()); // Just keep the drawn flag and location
                        check.setLocation(stepMeta.getLocation());
                    }
                }
                else
                {
                    addStep(stepMeta); // simply add it.
                }
            }
            
            // Read the error handling code of the steps...
            //
            Node errorHandlingNode = XMLHandler.getSubNode(transnode, XML_TAG_STEP_ERROR_HANDLING);
            int nrErrorHandlers = XMLHandler.countNodes(errorHandlingNode, StepErrorMeta.XML_TAG);
            for (int i=0;i<nrErrorHandlers;i++)
            {
                Node stepErrorMetaNode = XMLHandler.getSubNodeByNr(errorHandlingNode, StepErrorMeta.XML_TAG, i);
                StepErrorMeta stepErrorMeta = new StepErrorMeta(this, stepErrorMetaNode, steps);
                stepErrorMeta.getSourceStep().setStepErrorMeta(stepErrorMeta); // a bit of a trick, I know.
            }

            // Have all StreamValueLookups, etc. reference the correct source steps...
            //
            for (int i = 0; i < nrSteps(); i++)
            {
                StepMeta stepMeta = getStep(i);
                StepMetaInterface sii = stepMeta.getStepMetaInterface();
                if (sii != null) sii.searchInfoAndTargetSteps(steps);
            }

            // Handle Hops
            //
            Node ordernode = XMLHandler.getSubNode(transnode, XML_TAG_ORDER); //$NON-NLS-1$
            n = XMLHandler.countNodes(ordernode, TransHopMeta.XML_TAG); //$NON-NLS-1$

            if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.WeHaveHops") + n + " hops..."); //$NON-NLS-1$ //$NON-NLS-2$
            for (int i = 0; i < n; i++)
            {
            	if(log.isDebug()) log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.LookingAtHop") + i); //$NON-NLS-1$
                Node hopnode = XMLHandler.getSubNodeByNr(ordernode, TransHopMeta.XML_TAG, i); //$NON-NLS-1$

                TransHopMeta hopinf = new TransHopMeta(hopnode, steps);
                addTransHop(hopinf);
            }

            //
            // get transformation info:
            //
            Node infonode = XMLHandler.getSubNode(transnode, XML_TAG_INFO); //$NON-NLS-1$

            // Name
            //
            setName( XMLHandler.getTagValue(infonode, "name") ); //$NON-NLS-1$

			// description
            //
			description = XMLHandler.getTagValue(infonode, "description"); 

			// extended description
			//
			extended_description = XMLHandler.getTagValue(infonode, "extended_description"); 

			// trans version
			//
			trans_version = XMLHandler.getTagValue(infonode, "trans_version"); 

			// trans status
			//
			trans_status = Const.toInt(XMLHandler.getTagValue(infonode, "trans_status"),-1); 
			
			String transTypeCode = XMLHandler.getTagValue(infonode, "trans_type");
			transformationType = TransformationType.getTransformationTypeByCode(transTypeCode);

            // Optionally load the repository directory...
			//
			if (rep!=null) {
				String directoryPath = XMLHandler.getTagValue(infonode, "directory");
				if (directoryPath!=null) {
					directory = rep.loadRepositoryDirectoryTree().findDirectory(directoryPath);
					if (directory==null) { // not found
						directory = new RepositoryDirectory(); // The root as default
					}
				}
			}

            // Read logging table information
			//
			Node logNode = XMLHandler.getSubNode(infonode, "log");
			if (logNode!=null) {
				
				// Backward compatibility...
				//
				Node transLogNode = XMLHandler.getSubNode(logNode, TransLogTable.XML_TAG);
				if (transLogNode==null) {
					// Load the XML
					//
					transLogTable.findField(TransLogTable.ID.LINES_READ).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "read"))); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.LINES_WRITTEN).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "write"))); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.LINES_INPUT).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "input"))); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.LINES_OUTPUT).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "output"))); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.LINES_UPDATED).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "update"))); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.LINES_REJECTED).setSubject(findStep(XMLHandler.getTagValue(infonode, "log", "rejected"))); //$NON-NLS-1$ //$NON-NLS-2$
		            
		            transLogTable.setConnectionName(XMLHandler.getTagValue(infonode, "log", "connection")); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.setSchemaName(XMLHandler.getTagValue(infonode, "log", "schema")); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.setTableName(XMLHandler.getTagValue(infonode, "log", "table")); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.findField(TransLogTable.ID.ID_BATCH).setEnabled( "Y".equalsIgnoreCase(XMLHandler.getTagValue(infonode, "log", "use_batchid")) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		            transLogTable.findField(TransLogTable.ID.LOG_FIELD).setEnabled( "Y".equalsIgnoreCase(XMLHandler.getTagValue(infonode, "log", "USE_LOGFIELD")) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		            transLogTable.setLogSizeLimit( XMLHandler.getTagValue(infonode, "log", "size_limit_lines") ); //$NON-NLS-1$ //$NON-NLS-2$
		            transLogTable.setLogInterval( XMLHandler.getTagValue(infonode, "log", "interval") ); //$NON-NLS-1$ //$NON-NLS-2$
		            
		            performanceLogTable.setConnectionName(transLogTable.getConnectionName());
					performanceLogTable.setTableName( XMLHandler.getTagValue(infonode, "log", "step_performance_table")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					transLogTable.loadXML(transLogNode, databases, steps);
				}
				Node perfLogNode = XMLHandler.getSubNode(logNode, PerformanceLogTable.XML_TAG);
				if (perfLogNode!=null) {
					performanceLogTable.loadXML(perfLogNode, databases);
				}
				Node channelLogNode = XMLHandler.getSubNode(logNode, ChannelLogTable.XML_TAG);
				if (channelLogNode!=null) {
					channelLogTable.loadXML(channelLogNode, databases);
				}
				Node stepLogNode = XMLHandler.getSubNode(logNode, StepLogTable.XML_TAG);
				if (stepLogNode!=null) {
					stepLogTable.loadXML(stepLogNode, databases);
				}
			}


            // Maxdate range options...
            String maxdatcon = XMLHandler.getTagValue(infonode, "maxdate", "connection"); //$NON-NLS-1$ //$NON-NLS-2$
            maxDateConnection = findDatabase(maxdatcon);
            maxDateTable = XMLHandler.getTagValue(infonode, "maxdate", "table"); //$NON-NLS-1$ //$NON-NLS-2$
            maxDateField = XMLHandler.getTagValue(infonode, "maxdate", "field"); //$NON-NLS-1$ //$NON-NLS-2$
            String offset = XMLHandler.getTagValue(infonode, "maxdate", "offset"); //$NON-NLS-1$ //$NON-NLS-2$
            maxDateOffset = Const.toDouble(offset, 0.0);
            String mdiff = XMLHandler.getTagValue(infonode, "maxdate", "maxdiff"); //$NON-NLS-1$ //$NON-NLS-2$
            maxDateDifference = Const.toDouble(mdiff, 0.0);

            // Check the dependencies as far as dates are concerned...
            // We calculate BEFORE we run the MAX of these dates
            // If the date is larger then enddate, startdate is set to MIN_DATE
            //
            Node depsNode = XMLHandler.getSubNode(infonode, XML_TAG_DEPENDENCIES);
            int nrDeps = XMLHandler.countNodes(depsNode, TransDependency.XML_TAG);

            for (int i = 0; i < nrDeps; i++)
            {
                Node depNode = XMLHandler.getSubNodeByNr(depsNode, TransDependency.XML_TAG, i);

                TransDependency transDependency = new TransDependency(depNode, databases);
                if (transDependency.getDatabase() != null && transDependency.getFieldname() != null)
                {
                    addDependency(transDependency);
                }
            }

            // Read the named parameters.
            Node paramsNode = XMLHandler.getSubNode(infonode, XML_TAG_PARAMETERS);
            int nrParams = XMLHandler.countNodes(paramsNode, "parameter"); //$NON-NLS-1$

            for (int i = 0; i < nrParams; i++)
            {
                Node paramNode = XMLHandler.getSubNodeByNr(paramsNode, "parameter", i); //$NON-NLS-1$

                String paramName = XMLHandler.getTagValue(paramNode, "name"); //$NON-NLS-1$
                String defaultValue = XMLHandler.getTagValue(paramNode, "default_value"); //$NON-NLS-1$
                String descr = XMLHandler.getTagValue(paramNode, "description"); //$NON-NLS-1$
                
                addParameterDefinition(paramName, defaultValue, descr);
            }            

            // Read the partitioning schemas
            // 
            Node partSchemasNode = XMLHandler.getSubNode(infonode, XML_TAG_PARTITIONSCHEMAS); //$NON-NLS-1$
            int nrPartSchemas = XMLHandler.countNodes(partSchemasNode, PartitionSchema.XML_TAG); //$NON-NLS-1$
            for (int i = 0 ; i < nrPartSchemas ; i++)
            {
                Node partSchemaNode = XMLHandler.getSubNodeByNr(partSchemasNode, PartitionSchema.XML_TAG, i);
                PartitionSchema partitionSchema = new PartitionSchema(partSchemaNode);
                
                // Check if the step exists and if it's a shared step.
                // If so, then we will keep the shared version, not this one.
                // The stored XML is only for backup purposes.
                //
                PartitionSchema check = findPartitionSchema(partitionSchema.getName());
                if (check!=null)
                {
                    if (!check.isShared()) // we don't overwrite shared objects.
                    {
                        addOrReplacePartitionSchema(partitionSchema);
                    }
                }
                else
                {
                    partitionSchemas.add(partitionSchema);
                }
                
            }
            
            // Have all step partitioning meta-data reference the correct schemas that we just loaded
            // 
            for (int i = 0; i < nrSteps(); i++)
            {
                StepPartitioningMeta stepPartitioningMeta = getStep(i).getStepPartitioningMeta();
                if (stepPartitioningMeta!=null)
                {
                    stepPartitioningMeta.setPartitionSchemaAfterLoading(partitionSchemas);
                }
                StepPartitioningMeta targetStepPartitioningMeta = getStep(i).getTargetStepPartitioningMeta();
                if (targetStepPartitioningMeta!=null)
                {
                    targetStepPartitioningMeta.setPartitionSchemaAfterLoading(partitionSchemas);
                }
            }

            // Read the slave servers...
            // 
            Node slaveServersNode = XMLHandler.getSubNode(infonode, XML_TAG_SLAVESERVERS); //$NON-NLS-1$
            int nrSlaveServers = XMLHandler.countNodes(slaveServersNode, SlaveServer.XML_TAG); //$NON-NLS-1$
            for (int i = 0 ; i < nrSlaveServers ; i++)
            {
                Node slaveServerNode = XMLHandler.getSubNodeByNr(slaveServersNode, SlaveServer.XML_TAG, i);
                SlaveServer slaveServer = new SlaveServer(slaveServerNode);
                slaveServer.shareVariablesWith(this);
                
                // Check if the object exists and if it's a shared object.
                // If so, then we will keep the shared version, not this one.
                // The stored XML is only for backup purposes.
                SlaveServer check = findSlaveServer(slaveServer.getName());
                if (check!=null)
                {
                    if (!check.isShared()) // we don't overwrite shared objects.
                    {
                        addOrReplaceSlaveServer(slaveServer);
                    }
                }
                else
                {
                    slaveServers.add(slaveServer);
                }
            }

            // Read the cluster schemas
            // 
            Node clusterSchemasNode = XMLHandler.getSubNode(infonode, XML_TAG_CLUSTERSCHEMAS); //$NON-NLS-1$
            int nrClusterSchemas = XMLHandler.countNodes(clusterSchemasNode, ClusterSchema.XML_TAG); //$NON-NLS-1$
            for (int i = 0 ; i < nrClusterSchemas ; i++)
            {
                Node clusterSchemaNode = XMLHandler.getSubNodeByNr(clusterSchemasNode, ClusterSchema.XML_TAG, i);
                ClusterSchema clusterSchema = new ClusterSchema(clusterSchemaNode, slaveServers);
                clusterSchema.shareVariablesWith(this);
                
                // Check if the object exists and if it's a shared object.
                // If so, then we will keep the shared version, not this one.
                // The stored XML is only for backup purposes.
                ClusterSchema check = findClusterSchema(clusterSchema.getName());
                if (check!=null)
                {
                    if (!check.isShared()) // we don't overwrite shared objects.
                    {
                        addOrReplaceClusterSchema(clusterSchema);
                    }
                }
                else
                {
                    clusterSchemas.add(clusterSchema);
                }
            }
            
            // Have all step clustering schema meta-data reference the correct cluster schemas that we just loaded
            // 
            for (int i = 0; i < nrSteps(); i++)
            {
                getStep(i).setClusterSchemaAfterLoading(clusterSchemas);
            }
           
            String srowset = XMLHandler.getTagValue(infonode, "size_rowset"); //$NON-NLS-1$
            sizeRowset = Const.toInt(srowset, Const.ROWS_IN_ROWSET);
            sleepTimeEmpty = Const.toInt(XMLHandler.getTagValue(infonode, "sleep_time_empty"), Const.TIMEOUT_GET_MILLIS); //$NON-NLS-1$
            sleepTimeFull  = Const.toInt(XMLHandler.getTagValue(infonode, "sleep_time_full"), Const.TIMEOUT_PUT_MILLIS); //$NON-NLS-1$
            usingUniqueConnections = "Y".equalsIgnoreCase( XMLHandler.getTagValue(infonode, "unique_connections") ); //$NON-NLS-1$

            feedbackShown = !"N".equalsIgnoreCase( XMLHandler.getTagValue(infonode, "feedback_shown") ); //$NON-NLS-1$
            feedbackSize = Const.toInt(XMLHandler.getTagValue(infonode, "feedback_size"), Const.ROWS_UPDATE); //$NON-NLS-1$
            usingThreadPriorityManagment = !"N".equalsIgnoreCase( XMLHandler.getTagValue(infonode, "using_thread_priorities") ); //$NON-NLS-1$ 

            // Performance monitoring for steps...
            //
            capturingStepPerformanceSnapShots = "Y".equalsIgnoreCase(XMLHandler.getTagValue(infonode, "capture_step_performance")); // $NON-NLS-1$ $NON-NLS-2$
            stepPerformanceCapturingDelay = Const.toLong(XMLHandler.getTagValue(infonode, "step_performance_capturing_delay"), 1000); // $NON-NLS-1$
            stepPerformanceCapturingSizeLimit = XMLHandler.getTagValue(infonode, "step_performance_capturing_size_limit"); // $NON-NLS-1$

			// Created user/date
			createdUser = XMLHandler.getTagValue(infonode, "created_user");
			String createDate = XMLHandler.getTagValue(infonode, "created_date");
			if (createDate!=null)
			{
				createdDate = XMLHandler.stringToDate(createDate);
			}

            // Changed user/date
            modifiedUser = XMLHandler.getTagValue(infonode, "modified_user");
            String modDate = XMLHandler.getTagValue(infonode, "modified_date");
            if (modDate!=null)
            {
                modifiedDate = XMLHandler.stringToDate(modDate);
            }
            
            Node partitionDistNode = XMLHandler.getSubNode(transnode, SlaveStepCopyPartitionDistribution.XML_TAG);
            if (partitionDistNode!=null) {
            	slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution(partitionDistNode);
            }
            else {
            	slaveStepCopyPartitionDistribution = new SlaveStepCopyPartitionDistribution(); // leave empty
            }
            
            // Is this a slave transformation?
            //
            slaveTransformation = "Y".equalsIgnoreCase(XMLHandler.getTagValue(transnode, "slave_transformation"));
            if(log.isDebug()) 
            {
            	log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.NumberOfStepsReaded") + nrSteps()); //$NON-NLS-1$
            	log.logDebug(BaseMessages.getString(PKG, "TransMeta.Log.NumberOfHopsReaded") + nrTransHops()); //$NON-NLS-1$
            }
            sortSteps();
        }
        catch (KettleXMLException xe)
        {
            throw new KettleXMLException(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorReadingTransformation"), xe); //$NON-NLS-1$
        } catch (KettleException e) {
        	throw new KettleXMLException(e);
		}
        finally
        {
        	initializeVariablesFrom(null);
            if (setInternalVariables) setInternalKettleVariables();
        }
    }
    
    public SharedObjects readSharedObjects() throws KettleException
    {
        // Extract the shared steps, connections, etc. using the SharedObjects class
        //
        String soFile = environmentSubstitute(sharedObjectsFile);
        SharedObjects sharedObjects = new SharedObjects(soFile);
        if (sharedObjects.getObjectsMap().isEmpty()) {
        	log.logDetailed(BaseMessages.getString(PKG, "TransMeta.Log.EmptySharedObjectsFile", soFile));
        }
        
        // First read the databases...
        // We read databases & slaves first because there might be dependencies that need to be resolved.
        //
        for (SharedObjectInterface object : sharedObjects.getObjectsMap().values())
        {
            if (object instanceof DatabaseMeta)
            {
                DatabaseMeta databaseMeta = (DatabaseMeta) object;
                databaseMeta.shareVariablesWith(this);
                addOrReplaceDatabase(databaseMeta);
            }
            else if (object instanceof SlaveServer)
            {
                SlaveServer slaveServer = (SlaveServer) object;
                slaveServer.shareVariablesWith(this);
                addOrReplaceSlaveServer(slaveServer);
            }
            else if (object instanceof StepMeta)
            {
                StepMeta stepMeta = (StepMeta) object;
                addOrReplaceStep(stepMeta);
            }
            else if (object instanceof PartitionSchema)
            {
                PartitionSchema partitionSchema = (PartitionSchema) object;
                addOrReplacePartitionSchema(partitionSchema);
            }
            else if (object instanceof ClusterSchema)
            {
                ClusterSchema clusterSchema = (ClusterSchema) object;
                clusterSchema.shareVariablesWith(this);
                addOrReplaceClusterSchema(clusterSchema);
            }
        }
        
        return sharedObjects;
    }


    /**
     * Gives you an List of all the steps that are at least used in one active hop. These steps will be used to
     * execute the transformation. The others will not be executed.
     * Update 3.0 : we also add those steps that are not linked to another hop, but have at least one remote input or output step defined.
     *
     * @param all Set to true if you want to get ALL the steps from the transformation.
     * @return A ArrayList of steps
     */
    public List<StepMeta> getTransHopSteps(boolean all)
    {
        List<StepMeta> st = new ArrayList<StepMeta>();
        int idx;

        for (int x = 0; x < nrTransHops(); x++)
        {
            TransHopMeta hi = getTransHop(x);
            if (hi.isEnabled() || all)
            {
                idx = st.indexOf(hi.getFromStep()); // FROM
                if (idx < 0) st.add(hi.getFromStep());

                idx = st.indexOf(hi.getToStep()); // TO
                if (idx < 0) st.add(hi.getToStep());
            }
        }

        // Also, add the steps that need to be painted, but are not part of a hop
        for (int x = 0; x < nrSteps(); x++)
        {
            StepMeta stepMeta = getStep(x);
            if (stepMeta.isDrawn() && !isStepUsedInTransHops(stepMeta))
            {
                st.add(stepMeta);
            }
            if (!stepMeta.getRemoteInputSteps().isEmpty() || !stepMeta.getRemoteOutputSteps().isEmpty())
            {
            	if (!st.contains(stepMeta)) st.add(stepMeta);
            }
        }

        return st;
    }

    /**
     * Get the name of the transformation
     *
     * @return The name of the transformation
     */
    public String getName()
    {
        return name;
    }

  /**
   * Set the name of the transformation.
   * 
   * @param newName
   *          The new name of the transformation
   */
  public void setName(String newName) {
    fireNameChangedListeners(this.name, newName);
    this.name = newName;
    setInternalNameKettleVariable(variables);
  }

    /**
     * Builds a name - if no name is set, yet - from the filename
     */
    public void nameFromFilename()
    {
        if (!Const.isEmpty(filename))
        {
            setName( Const.createName(filename) );
        }
    }

    /**
     * Get the filename (if any) of the transformation
     *
     * @return The filename of the transformation.
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * Set the filename of the transformation
     *
     * @param fname The new filename of the transformation.
     */
    public void setFilename(String fname)
    {
    	fireFilenameChangedListeners(this.filename, fname); 
      this.filename = fname;
      setInternalFilenameKettleVariables(variables);
    }


	/**
     * Determines if a step has been used in a hop or not.
     *
     * @param stepMeta The step queried.
     * @return True if a step is used in a hop (active or not), false if this is not the case.
     */
    public boolean isStepUsedInTransHops(StepMeta stepMeta)
    {
        TransHopMeta fr = findTransHopFrom(stepMeta);
        TransHopMeta to = findTransHopTo(stepMeta);
        if (fr != null || to != null) return true;
        return false;
    }


    /**
     * Sets the changed parameter of the transformation.
     *
     * @param ch True if you want to mark the transformation as changed, false if not.
     */
    public void setChanged(boolean ch)
    {
        if (ch)
        	setChanged();
        else
        	clearChanged();
    }

    /**
     * Clears the different changed flags of the transformation.
     *
     */
    public void clearChanged()
    {
    	clearChangedDatabases();
        changed_steps = false;
        changed_hops = false;
        changed_notes = false;

        for (int i = 0; i < nrSteps(); i++)
        {
            getStep(i).setChanged(false);
            if (getStep(i).getStepPartitioningMeta() != null) 
            {
            	getStep(i).getStepPartitioningMeta().hasChanged(false);
            }
        }
        for (int i = 0; i < nrTransHops(); i++)
        {
            getTransHop(i).setChanged(false);
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            getNote(i).setChanged(false);
        }
        for (int i = 0; i < partitionSchemas.size(); i++)
        {
            partitionSchemas.get(i).setChanged(false);
        }
        for (int i = 0; i < clusterSchemas.size(); i++)
        {
            clusterSchemas.get(i).setChanged(false);
        }
        
        super.clearChanged();
    }

    /**
     * Clears the different changed flags of the transformation.
     *
     */
    public void clearChangedDatabases()
    {
        changed_databases = false;

        for (int i = 0; i < nrDatabases(); i++)
        {
            getDatabase(i).setChanged(false);
        }
    }

    /* (non-Javadoc)
     * @see org.pentaho.di.trans.HasDatabaseInterface#haveConnectionsChanged()
     */
    public boolean haveConnectionsChanged()
    {
        if (changed_databases) return true;

        for (int i = 0; i < nrDatabases(); i++)
        {
            DatabaseMeta ci = getDatabase(i);
            if (ci.hasChanged()) return true;
        }
        return false;
    }

    /**
     * Checks whether or not the steps have changed.
     *
     * @return True if the connections have been changed.
     */
    public boolean haveStepsChanged()
    {
        if (changed_steps) return true;

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (stepMeta.hasChanged()) return true;
            if (stepMeta.getStepPartitioningMeta() != null && stepMeta.getStepPartitioningMeta().hasChanged() ) return true;
        }
        return false;
    }

    /**
     * Checks whether or not any of the hops have been changed.
     *
     * @return True if a hop has been changed.
     */
    public boolean haveHopsChanged()
    {
        if (changed_hops) return true;

        for (int i = 0; i < nrTransHops(); i++)
        {
            TransHopMeta hi = getTransHop(i);
            if (hi.hasChanged()) return true;
        }
        return false;
    }

    /**
     * Checks whether or not any of the notes have been changed.
     *
     * @return True if the notes have been changed.
     */
    public boolean haveNotesChanged()
    {
        if (changed_notes) return true;

        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            if (ni.hasChanged()) return true;
        }

        return false;
    }
    
    /**
     * Checks whether or not any of the partitioning schemas have been changed.
     *
     * @return True if the partitioning schemas have been changed.
     */
    public boolean havePartitionSchemasChanged()
    {
        for (int i = 0; i < partitionSchemas.size(); i++)
        {
            PartitionSchema ps = partitionSchemas.get(i);
            if (ps.hasChanged()) return true;
        }

        return false;
    }
    
    /**
     * Checks whether or not any of the clustering schemas have been changed.
     *
     * @return True if the clustering schemas have been changed.
     */
    public boolean haveClusterSchemasChanged()
    {
        for (int i = 0; i < clusterSchemas.size(); i++)
        {
            ClusterSchema cs = clusterSchemas.get(i);
            if (cs.hasChanged()) return true;
        }

        return false;
    }


    /**
     * Checks whether or not the transformation has changed.
     *
     * @return True if the transformation has changed.
     */
    public boolean hasChanged()
    {
        if (super.hasChanged()) return true;

        if (haveConnectionsChanged()) return true;
        if (haveStepsChanged()) return true;
        if (haveHopsChanged()) return true;
        if (haveNotesChanged()) return true;
        if (havePartitionSchemasChanged()) return true;
        if (haveClusterSchemasChanged()) return true;

        return false;
    }

    /**
     * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all
     * the previous steps. If you keep going backward and find the step, there is a loop. Both the informational and the
     * normal steps need to be checked for loops!
     *
     * @param stepMeta The step position to start looking
     *
     * @return True if a loop has been found, false if no loop is found.
     */
    public boolean hasLoop(StepMeta stepMeta)
    {
    	clearLoopCachce();
        return hasLoop(stepMeta, null, true) || hasLoop(stepMeta, null, false);
    }

    /**
     * See if there are any loops in the transformation, starting at the indicated step. This works by looking at all
     * the previous steps. If you keep going backward and find the original step again, there is a loop.
     *
     * @param stepMeta The step position to start looking
     * @param lookup The original step when wandering around the transformation.
     * @param info Check the informational steps or not.
     *
     * @return True if a loop has been found, false if no loop is found.
     */
    private boolean hasLoop(StepMeta stepMeta, StepMeta lookup, boolean info)
    {
    	String cacheKey = stepMeta.getName() + " - " + (lookup!=null?lookup.getName():"") + " - " + (info?"true":"false");
    	Boolean loop = loopCache.get(cacheKey);
    	if (loop!=null) {
    		return loop.booleanValue();
    	}
    	
    	boolean hasLoop=false;
    	
        int nr = findNrPrevSteps(stepMeta, info);
        for (int i = 0; i < nr && !hasLoop; i++)
        {
            StepMeta prevStepMeta = findPrevStep(stepMeta, i, info);
            if (prevStepMeta != null)
            {
                if (prevStepMeta.equals(stepMeta)) {
                	hasLoop = true;
                	break; //no need to check more but caching this one below
                } else if (prevStepMeta.equals(lookup)) {
                	hasLoop = true;
                	break; //no need to check more but caching this one below
                } else if (hasLoop(prevStepMeta, lookup == null ? stepMeta : lookup, info)) {
                	hasLoop = true;
                	break; //no need to check more but caching this one below
                }
            }
        }
        
        // Store in the cache...
        //
        loopCache.put(cacheKey, Boolean.valueOf(hasLoop));
        
        return hasLoop;
    }

    /**
     * Mark all steps in the transformation as selected.
     *
     */
    public void selectAll()
    {
        int i;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            stepMeta.setSelected(true);
        }
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            ni.setSelected(true);
        }
        
        setChanged();
        notifyObservers("refreshGraph");
    }

    /**
     * Clear the selection of all steps.
     *
     */
    public void unselectAll()
    {
        int i;
        for (i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            stepMeta.setSelected(false);
        }
        for (i = 0; i < nrNotes(); i++)
        {
            NotePadMeta ni = getNote(i);
            ni.setSelected(false);
        }
    }

    /**
     * Get an array of all the selected step and note locations
     *
     * @return The selected step and notes locations.
     */
    public Point[] getSelectedStepLocations()
    {
        List<Point> points = new ArrayList<Point>();

        for (StepMeta stepMeta : getSelectedSteps()) {
            Point p = stepMeta.getLocation();
            points.add(new Point(p.x, p.y)); // explicit copy of location
        }

        return points.toArray(new Point[points.size()]);
    }

    /**
     * Get an array of all the selected step and note locations
     *
     * @return The selected step and notes locations.
     */
    public Point[] getSelectedNoteLocations()
    {
        List<Point> points = new ArrayList<Point>();

        for (NotePadMeta ni : getSelectedNotes())
        {
            Point p = ni.getLocation();
            points.add(new Point(p.x, p.y)); // explicit copy of location
        }

        return points.toArray(new Point[points.size()]);
    }

    /**
     * @return A list of all the selected steps.
     */
    public List<StepMeta> getSelectedSteps()
    {
    	List<StepMeta> selection = new ArrayList<StepMeta>();
        for (StepMeta stepMeta : steps)
        {
            if (stepMeta.isSelected()) {
            	selection.add(stepMeta);
            }

        }
        return selection;
    }
    
    /**
     * Get an array of all the selected notes
     *
     * @return An array of all the selected notes.
     */
    public List<NotePadMeta> getSelectedNotes()
    {
    	List<NotePadMeta> selection =new ArrayList<NotePadMeta>();
        for (NotePadMeta note : notes) {
            if (note.isSelected()) {
            	selection.add(note);
            }
        }
        return selection;
    }

    /**
     * Get an array of all the selected step names
     *
     * @return An array of all the selected step names.
     */
    public String[] getSelectedStepNames()
    {
    	List<StepMeta> selection = getSelectedSteps();
        String retval[] = new String[selection.size()];
        for (int i = 0; i < retval.length; i++) {
            StepMeta stepMeta = selection.get(i);
            retval[i] = stepMeta.getName();
        }
        return retval;
    }

    /**
     * Get an array of the locations of an array of steps
     *
     * @param steps An array of steps
     * @return an array of the locations of an array of steps
     */
    public int[] getStepIndexes(List<StepMeta> steps)
    {
        int retval[] = new int[steps.size()];

        for (int i = 0; i < steps.size(); i++)
        {
            retval[i] = indexOfStep(steps.get(i));
        }

        return retval;
    }

    /**
     * Get an array of the locations of an array of notes
     *
     * @param notes An array of notes
     * @return an array of the locations of an array of notes
     */
    public int[] getNoteIndexes(List<NotePadMeta> notes)
    {
        int retval[] = new int[notes.size()];

        for (int i = 0; i < notes.size(); i++)
            retval[i] = indexOfNote(notes.get(i));

        return retval;
    }

    /**
     * Get the maximum number of undo operations possible
     *
     * @return The maximum number of undo operations that are allowed.
     */
    public int getMaxUndo()
    {
        return max_undo;
    }

    /**
     * Sets the maximum number of undo operations that are allowed.
     *
     * @param mu The maximum number of undo operations that are allowed.
     */
    public void setMaxUndo(int mu)
    {
        max_undo = mu;
        while (undo.size() > mu && undo.size() > 0)
            undo.remove(0);
    }

    /**
     * Add an undo operation to the undo list
     *
     * @param from array of objects representing the old state
     * @param to array of objectes representing the new state
     * @param pos An array of object locations
     * @param prev An array of points representing the old positions
     * @param curr An array of points representing the new positions
     * @param type_of_change The type of change that's being done to the transformation.
     * @param nextAlso indicates that the next undo operation needs to follow this one.
     */
    public void addUndo(Object from[], Object to[], int pos[], Point prev[], Point curr[], int type_of_change, boolean nextAlso)
    {
        // First clean up after the current position.
        // Example: position at 3, size=5
        // 012345
        // ^
        // remove 34
        // Add 4
        // 01234

        while (undo.size() > undo_position + 1 && undo.size() > 0)
        {
            int last = undo.size() - 1;
            undo.remove(last);
        }

        TransAction ta = new TransAction();
        switch (type_of_change)
        {
        case TYPE_UNDO_CHANGE:
            ta.setChanged(from, to, pos);
            break;
        case TYPE_UNDO_DELETE:
            ta.setDelete(from, pos);
            break;
        case TYPE_UNDO_NEW:
            ta.setNew(from, pos);
            break;
        case TYPE_UNDO_POSITION:
            ta.setPosition(from, pos, prev, curr);
            break;
        }
        ta.setNextAlso(nextAlso);
        undo.add(ta);
        undo_position++;

        if (undo.size() > max_undo)
        {
            undo.remove(0);
            undo_position--;
        }
    }

    /**
     * Get the previous undo operation and change the undo pointer
     *
     * @return The undo transaction to be performed.
     */
    public TransAction previousUndo()
    {
        if (undo.isEmpty() || undo_position < 0) return null; // No undo left!

        TransAction retval = undo.get(undo_position);

        undo_position--;

        return retval;
    }

    /**
     * View current undo, don't change undo position
     *
     * @return The current undo transaction
     */
    public TransAction viewThisUndo()
    {
        if (undo.isEmpty() || undo_position < 0) return null; // No undo left!

        TransAction retval = undo.get(undo_position);

        return retval;
    }

    /**
     * View previous undo, don't change undo position
     *
     * @return The previous undo transaction
     */
    public TransAction viewPreviousUndo()
    {
        if (undo.isEmpty() || undo_position - 1 < 0) return null; // No undo left!

        TransAction retval = undo.get(undo_position - 1);

        return retval;
    }

    /**
     * Get the next undo transaction on the list. Change the undo pointer.
     *
     * @return The next undo transaction (for redo)
     */
    public TransAction nextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        undo_position++;

        TransAction retval = undo.get(undo_position);

        return retval;
    }

    /**
     * Get the next undo transaction on the list.
     *
     * @return The next undo transaction (for redo)
     */
    public TransAction viewNextUndo()
    {
        int size = undo.size();
        if (size == 0 || undo_position >= size - 1) return null; // no redo left...

        TransAction retval = undo.get(undo_position + 1);

        return retval;
    }

    /**
     * Get the maximum size of the canvas by calculating the maximum location of a step
     *
     * @return Maximum coordinate of a step in the transformation + (100,100) for safety.
     */
    public Point getMaximum()
    {
        int maxx = 0, maxy = 0;
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            Point loc = stepMeta.getLocation();
            if (loc.x > maxx) maxx = loc.x;
            if (loc.y > maxy) maxy = loc.y;
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta notePadMeta = getNote(i);
            Point loc = notePadMeta.getLocation();
            if (loc.x + notePadMeta.width > maxx) maxx = loc.x + notePadMeta.width;
            if (loc.y + notePadMeta.height > maxy) maxy = loc.y + notePadMeta.height;
        }

        return new Point(maxx + 100, maxy + 100);
    }

    /**
     * Get the minimum point on the canvas of a transformation
     * @return Minimum coordinate of a step in the transformation 
     */
    public Point getMinimum()
    {
        int minx= Integer.MAX_VALUE, miny = Integer.MAX_VALUE;
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            Point loc = stepMeta.getLocation();
            if (loc.x < minx) minx = loc.x;
            if (loc.y < miny) miny = loc.y;
        }
        for (int i = 0; i < nrNotes(); i++)
        {
            NotePadMeta notePadMeta = getNote(i);
            Point loc = notePadMeta.getLocation();
            if (loc.x < minx) minx = loc.x;
            if (loc.y < miny) miny = loc.y;
        }
        
        if (minx>20) minx-=20; else minx=0;
        if (miny>20) miny-=20; else miny=0;

        return new Point(minx, miny);
    }

    /**
     * Get the names of all the steps.
     *
     * @return An array of step names.
     */
    public String[] getStepNames()
    {
        String retval[] = new String[nrSteps()];

        for (int i = 0; i < nrSteps(); i++)
            retval[i] = getStep(i).getName();

        return retval;
    }

    /**
     * Get all the steps in an array.
     *
     * @return An array of all the steps in the transformation.
     */
    public StepMeta[] getStepsArray()
    {
        StepMeta retval[] = new StepMeta[nrSteps()];

        for (int i = 0; i < nrSteps(); i++)
            retval[i] = getStep(i);

        return retval;
    }

    /**
     * Look in the transformation and see if we can find a step in a previous location starting somewhere.
     *
     * @param startStep The starting step
     * @param stepToFind The step to look for backward in the transformation
     * @return true if we can find the step in an earlier location in the transformation.
     */
    public boolean findPrevious(StepMeta startStep, StepMeta stepToFind)
    {
        // Normal steps
    	List<StepMeta> previousSteps = findPreviousSteps(startStep, false);
    	for (int i = 0; i < previousSteps.size(); i++)
        {
            StepMeta stepMeta = previousSteps.get(i);
            if (stepMeta.equals(stepToFind)) return true;

            boolean found = findPrevious(stepMeta, stepToFind); // Look further back in the tree.
            if (found) return true;
        }

        // Info steps
        List<StepMeta> infoSteps = findPreviousSteps(startStep, true);
        for (int i = 0; i < infoSteps.size(); i++)
        {
            StepMeta stepMeta = infoSteps.get(i);
            if (stepMeta.equals(stepToFind)) return true;

            boolean found = findPrevious(stepMeta, stepToFind); // Look further back in the tree.
            if (found) return true;
        }

        return false;
    }

    /**
     * Put the steps in alphabetical order.
     */
    public void sortSteps()
    {
        try
        {
            Collections.sort(steps);
        }
        catch (Exception e)
        {
            log.logError(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorOfSortingSteps") + e); //$NON-NLS-1$
            log.logError(Const.getStackTracker(e));
        }
    }

    public void sortHops()
    {
        Collections.sort(hops);
    }
    
    private long prevCount;

	private ObjectRevision	objectVersion;

    /**
     * Put the steps in a more natural order: from start to finish. For the moment, we ignore splits and joins. Splits
     * and joins can't be listed sequentially in any case!
     * 
     * @return a map containing all the previous steps per step
     *
     */
    public Map<StepMeta, Map<StepMeta, Boolean>> sortStepsNatural()
    {
    	long startTime = System.currentTimeMillis();
    	
    	prevCount = 0;
    	
    	// First create a map where all the previous steps of another step are kept...
    	// 
    	final Map<StepMeta, Map<StepMeta, Boolean>> stepMap = new HashMap<StepMeta, Map<StepMeta, Boolean>>();
    	
    	// Also cache the previous steps 
    	//
    	final Map<StepMeta, List<StepMeta>> previousCache = new HashMap<StepMeta, List<StepMeta>>();
    	
    	// Cache calculation of steps before another
    	//
    	Map<StepMeta, Map<StepMeta, Boolean>> beforeCache = new HashMap<StepMeta, Map<StepMeta, Boolean>>();
    	
    	for (StepMeta stepMeta : steps) {
    		// What are the previous steps? (cached version for performance)
    		//
    		List<StepMeta> prevSteps = previousCache.get(stepMeta);
    		if (prevSteps==null) {
    			prevSteps = findPreviousSteps(stepMeta);
    			prevCount++;
    			previousCache.put(stepMeta, prevSteps);
    		}
    		
    		// Now get the previous steps recursively, store them in the step map
    		//
    		for (StepMeta prev : prevSteps) {
    			Map<StepMeta, Boolean> beforePrevMap = updateFillStepMap(previousCache, beforeCache, stepMeta, prev);
    			stepMap.put(stepMeta, beforePrevMap);
    			
    			// Store it also in the beforeCache...
    			//
    			beforeCache.put(prev, beforePrevMap);
    		}
    	}
    	
    	Collections.sort(steps, new Comparator<StepMeta>() {
		
			public int compare(StepMeta o1, StepMeta o2) {

				Map<StepMeta, Boolean> beforeMap = stepMap.get(o1);
				if (beforeMap!=null) {
					if (beforeMap.get(o2)==null) {
						return -1;
					} else {
						return 1;
					}
				} else {
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		});
    	
    	long endTime = System.currentTimeMillis();
    	log.logBasic("Natural sort of steps executed in "+(endTime-startTime)+"ms ("+prevCount+" time previous steps calculated)");

        return stepMap;
    }
    
    /**
     * Fill the 
     * @param stepMap
     * @param previousCache 
     * @param beforeCache 
     * @param originStepMeta
     * @param previousStepMeta
     */
    private Map<StepMeta, Boolean> updateFillStepMap(Map<StepMeta, List<StepMeta>> previousCache, Map<StepMeta, Map<StepMeta, Boolean>> beforeCache, StepMeta originStepMeta, StepMeta previousStepMeta) {
    	
    	// See if we have a hash map to store step occurrence (located before the step)
    	//
    	Map<StepMeta, Boolean> beforeMap = beforeCache.get(previousStepMeta);
		if (beforeMap==null) {
			beforeMap = new HashMap<StepMeta, Boolean>();
		} else {
			return beforeMap; // Nothing left to do here!
		}
		
		// Store the current previous step in the map
    	//
		beforeMap.put(previousStepMeta, Boolean.TRUE);
		
		// Figure out all the previous steps as well, they all need to go in there...
		// 
		List<StepMeta> prevSteps = previousCache.get(previousStepMeta);
		if (prevSteps==null) {
			prevSteps = findPreviousSteps(previousStepMeta);
			prevCount++;
			previousCache.put(previousStepMeta, prevSteps);
		}
		
		// Now, get the previous steps for stepMeta recursively...
		// We only do this when the beforeMap is not known yet...
		//
		for (StepMeta prev : prevSteps) {
			Map<StepMeta, Boolean> beforePrevMap = updateFillStepMap(previousCache, beforeCache, originStepMeta, prev);
			
			// Keep a copy in the cache...
			//
			beforeCache.put(prev, beforePrevMap);
			
			// Also add it to the new map for this step...
			//
			beforeMap.putAll(beforePrevMap);
		}
		
		return beforeMap;
    }

    /**
     * Sort the hops in a natural way: from beginning to end
     */
    public void sortHopsNatural()
    {
        // Loop over the hops...
        for (int j = 0; j < nrTransHops(); j++)
        {
            // Buble sort: we need to do this several times...
            for (int i = 0; i < nrTransHops() - 1; i++)
            {
                TransHopMeta one = getTransHop(i);
                TransHopMeta two = getTransHop(i + 1);

                StepMeta a = two.getFromStep();
                StepMeta b = one.getToStep();

                if (!findPrevious(a, b) && !a.equals(b))
                {
                    setTransHop(i + 1, one);
                    setTransHop(i, two);
                }
            }
        }
    }

    /**
     * This procedure determines the impact of the different steps in a transformation on databases, tables and field.
     *
     * @param impact An ArrayList of DatabaseImpact objects.
     *
     */
    public void analyseImpact(List<DatabaseImpact> impact, ProgressMonitorListener monitor) throws KettleStepException
    {
        if (monitor != null)
        {
            monitor.beginTask(BaseMessages.getString(PKG, "TransMeta.Monitor.DeterminingImpactTask.Title"), nrSteps()); //$NON-NLS-1$
        }
        boolean stop = false;
        for (int i = 0; i < nrSteps() && !stop; i++)
        {
            if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.LookingAtStepTask.Title") + (i + 1) + "/" + nrSteps()); //$NON-NLS-1$ //$NON-NLS-2$
            StepMeta stepMeta = getStep(i);

            RowMetaInterface prev = getPrevStepFields(stepMeta);
            StepMetaInterface stepint = stepMeta.getStepMetaInterface();
            RowMetaInterface inform = null;
            StepMeta[] lu = getInfoStep(stepMeta);
            if (lu != null)
            {
                inform = getStepFields(lu);
            }
            else
            {
                inform = stepint.getTableFields();
            }

            stepint.analyseImpact(impact, this, stepMeta, prev, null, null, inform);

            if (monitor != null)
            {
                monitor.worked(1);
                stop = monitor.isCanceled();
            }
        }

        if (monitor != null) monitor.done();
    }

    /**
     * Proposes an alternative stepname when the original already exists...
     *
     * @param stepname The stepname to find an alternative for..
     * @return The alternative stepname.
     */
    public String getAlternativeStepname(String stepname)
    {
        String newname = stepname;
        StepMeta stepMeta = findStep(newname);
        int nr = 1;
        while (stepMeta != null)
        {
            nr++;
            newname = stepname + " " + nr; //$NON-NLS-1$
            stepMeta = findStep(newname);
        }

        return newname;
    }

    /**
     * Builds a list of all the SQL statements that this transformation needs in order to work properly.
     *
     * @return An ArrayList of SQLStatement objects.
     */
    public List<SQLStatement> getSQLStatements() throws KettleStepException
    {
        return getSQLStatements(null);
    }

    /**
     * Builds a list of all the SQL statements that this transformation needs in order to work properly.
     *
     * @return An ArrayList of SQLStatement objects.
     */
    public List<SQLStatement> getSQLStatements(ProgressMonitorListener monitor) throws KettleStepException
    {
        if (monitor != null) monitor.beginTask(BaseMessages.getString(PKG, "TransMeta.Monitor.GettingTheSQLForTransformationTask.Title"), nrSteps() + 1); //$NON-NLS-1$
        List<SQLStatement> stats = new ArrayList<SQLStatement>();

        for (int i = 0; i < nrSteps(); i++)
        {
            StepMeta stepMeta = getStep(i);
            if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.GettingTheSQLForStepTask.Title",""+stepMeta )); //$NON-NLS-1$ //$NON-NLS-2$
            RowMetaInterface prev = getPrevStepFields(stepMeta);
            SQLStatement sql = stepMeta.getStepMetaInterface().getSQLStatements(this, stepMeta, prev);
            if (sql.getSQL() != null || sql.hasError())
            {
                stats.add(sql);
            }
            if (monitor != null) monitor.worked(1);
        }

        // Also check the sql for the logtable...
        //
        if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.GettingTheSQLForTransformationTask.Title2")); //$NON-NLS-1$
        if (transLogTable.getDatabaseMeta() != null && ( !Const.isEmpty(transLogTable.getTableName()) || !Const.isEmpty(performanceLogTable.getTableName())) )
        {
            try
            {
                for (LogTableInterface logTable : new LogTableInterface[] { transLogTable, performanceLogTable, channelLogTable, stepLogTable, }) {
                	if (logTable.getDatabaseMeta()!=null && !Const.isEmpty(logTable.getTableName())) {
                		
                		Database db =null;
                		try {
	                        db = new Database(this, transLogTable.getDatabaseMeta());
	                        db.shareVariablesWith(this);
	                        db.connect();

	    	                RowMetaInterface fields = logTable.getLogRecord(LogStatus.START, null).getRowMeta();
	    	                String schemaTable = logTable.getDatabaseMeta().getSchemaTableCombination(logTable.getSchemaName(), logTable.getTableName());
	    	                String sql = db.getDDL(schemaTable, fields);
	    	                if (!Const.isEmpty(sql)) 
	    	                {
	    	                    SQLStatement stat = new SQLStatement("<this transformation>", transLogTable.getDatabaseMeta(), sql); //$NON-NLS-1$
	    	                    stats.add(stat);
	    	                }
                		} catch(Exception e) {
                			throw new KettleDatabaseException("Unable to connect to logging database ["+logTable.getDatabaseMeta()+"]", e);
                		} finally {
                			if (db!=null) {
                				db.disconnect();
                			}
                		}
                	}
                }
            }
            catch (KettleDatabaseException dbe)
            {
                SQLStatement stat = new SQLStatement("<this transformation>", transLogTable.getDatabaseMeta(), null); //$NON-NLS-1$
                stat.setError(BaseMessages.getString(PKG, "TransMeta.SQLStatement.ErrorDesc.ErrorObtainingTransformationLogTableInfo") + dbe.getMessage()); //$NON-NLS-1$
                stats.add(stat);
            }
        }
        if (monitor != null) monitor.worked(1);
        if (monitor != null) monitor.done();

        return stats;
    }

    /**
     * Get the SQL statements, needed to run this transformation, as one String.
     *
     * @return the SQL statements needed to run this transformation.
     */
    public String getSQLStatementsString() throws KettleStepException
    {
        String sql = ""; //$NON-NLS-1$
        List<SQLStatement> stats = getSQLStatements();
        for (int i = 0; i < stats.size(); i++)
        {
            SQLStatement stat = stats.get(i);
            if (!stat.hasError() && stat.hasSQL())
            {
                sql += stat.getSQL();
            }
        }

        return sql;
    }

    /**
     * Checks all the steps and fills a List of (CheckResult) remarks.
     *
     * @param remarks The remarks list to add to.
     * @param only_selected Check only the selected steps.
     * @param monitor The progress monitor to use, null if not used
     */
    public void checkSteps(List<CheckResultInterface> remarks, boolean only_selected, ProgressMonitorListener monitor)
    {
        try
        {
            remarks.clear(); // Start with a clean slate...

            Map<ValueMetaInterface,String> values = new Hashtable<ValueMetaInterface,String>();
            String stepnames[];
            StepMeta steps[];
            List<StepMeta> selectedSteps = getSelectedSteps();
            if (!only_selected || selectedSteps.isEmpty())
            {
                stepnames = getStepNames();
                steps = getStepsArray();
            }
            else
            {
                stepnames = getSelectedStepNames();
                steps = selectedSteps.toArray(new StepMeta[selectedSteps.size()]);
            }

            boolean stop_checking = false;

            if (monitor != null) monitor.beginTask(BaseMessages.getString(PKG, "TransMeta.Monitor.VerifyingThisTransformationTask.Title"), steps.length + 2); //$NON-NLS-1$

            for (int i = 0; i < steps.length && !stop_checking; i++)
            {
                if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.VerifyingStepTask.Title",stepnames[i])); //$NON-NLS-1$ //$NON-NLS-2$

                StepMeta stepMeta = steps[i];

                int nrinfo = findNrInfoSteps(stepMeta);
                StepMeta[] infostep = null;
                if (nrinfo > 0)
                {
                    infostep = getInfoStep(stepMeta);
                }

                RowMetaInterface info = null;
                if (infostep != null)
                {
                    try
                    {
                        info = getStepFields(infostep);
                    }
                    catch (KettleStepException kse)
                    {
                        info = null;
                        CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.ErrorOccurredGettingStepInfoFields.Description",""+stepMeta , Const.CR + kse.getMessage()), stepMeta); //$NON-NLS-1$
                        remarks.add(cr);
                    }
                }

                // The previous fields from non-informative steps:
                RowMetaInterface prev = null;
                try
                {
                    prev = getPrevStepFields(stepMeta);
                }
                catch (KettleStepException kse)
                {
                    CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.ErrorOccurredGettingInputFields.Description", ""+stepMeta
                            , Const.CR + kse.getMessage()), stepMeta); //$NON-NLS-1$
                    remarks.add(cr);
                    // This is a severe error: stop checking...
                    // Otherwise we wind up checking time & time again because nothing gets put in the database
                    // cache, the timeout of certain databases is very long... (Oracle)
                    stop_checking = true;
                }

                if (isStepUsedInTransHops(stepMeta))
                {
                    // Get the input & output steps!
                    // Copy to arrays:
                    String input[] = getPrevStepNames(stepMeta);
                    String output[] = getNextStepNames(stepMeta);

                    // Check step specific info...
                    stepMeta.check(remarks, this, prev, input, output, info);

                    // See if illegal characters etc. were used in field-names...
                    if (prev != null)
                    {
                        for (int x = 0; x < prev.size(); x++)
                        {
                            ValueMetaInterface v = prev.getValueMeta(x);
                            String name = v.getName();
                            if (name == null)
                                values.put(v, BaseMessages.getString(PKG, "TransMeta.Value.CheckingFieldName.FieldNameIsEmpty.Description")); //$NON-NLS-1$
                            else
                                if (name.indexOf(' ') >= 0)
                                    values.put(v, BaseMessages.getString(PKG, "TransMeta.Value.CheckingFieldName.FieldNameContainsSpaces.Description")); //$NON-NLS-1$
                                else
                                {
                                    char list[] = new char[] { '.', ',', '-', '/', '+', '*', '\'', '\t', '"', '|', '@', '(', ')', '{', '}', '!', '^' };
                                    for (int c = 0; c < list.length; c++)
                                    {
                                        if (name.indexOf(list[c]) >= 0)
                                            values.put(v, BaseMessages.getString(PKG, "TransMeta.Value.CheckingFieldName.FieldNameContainsUnfriendlyCodes.Description",String.valueOf(list[c]) )); //$NON-NLS-1$ //$NON-NLS-2$
                                    }
                                }
                        }

                        // Check if 2 steps with the same name are entering the step...
                        if (prev.size() > 1)
                        {
                            String fieldNames[] = prev.getFieldNames();
                            String sortedNames[] = Const.sortStrings(fieldNames);

                            String prevName = sortedNames[0];
                            for (int x = 1; x < sortedNames.length; x++)
                            {
                                // Checking for doubles
                                if (prevName.equalsIgnoreCase(sortedNames[x]))
                                {
                                    // Give a warning!!
                                    CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR,
                                            BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultWarning.HaveTheSameNameField.Description", prevName ), stepMeta); //$NON-NLS-1$ //$NON-NLS-2$
                                    remarks.add(cr);
                                }
                                else
                                {
                                    prevName = sortedNames[x];
                                }
                            }
                        }
                    }
                    else
                    {
                        CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.CannotFindPreviousFields.Description") + stepMeta.getName(), //$NON-NLS-1$
                                stepMeta);
                        remarks.add(cr);
                    }
                }
                else
                {
                    CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultWarning.StepIsNotUsed.Description"), stepMeta); //$NON-NLS-1$
                    remarks.add(cr);
                }
                
                // Also check for mixing rows...
                try
                {
                    checkRowMixingStatically(stepMeta, null);
                }
                catch(KettleRowException e)
                {
                    CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, e.getMessage(), stepMeta);
                    remarks.add(cr);
                }
                

                if (monitor != null)
                {
                    monitor.worked(1); // progress bar...
                    if (monitor.isCanceled()) stop_checking = true;
                }
            }

            // Also, check the logging table of the transformation...
            if (monitor == null || !monitor.isCanceled())
            {
                if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.CheckingTheLoggingTableTask.Title")); //$NON-NLS-1$
                if (transLogTable.getDatabaseMeta() != null)
                {
                    Database logdb = new Database(this, transLogTable.getDatabaseMeta());
                    logdb.shareVariablesWith(this);
                    try
                    {
                        logdb.connect();
                        CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultOK.ConnectingWorks.Description"), //$NON-NLS-1$
                                null);
                        remarks.add(cr);

                        if (transLogTable.getTableName() != null)
                        {
                            if (logdb.checkTableExists(transLogTable.getTableName()))
                            {
                                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultOK.LoggingTableExists.Description", transLogTable.getTableName() ), null); //$NON-NLS-1$ //$NON-NLS-2$
                                remarks.add(cr);

                                RowMetaInterface fields = transLogTable.getLogRecord(LogStatus.START, null).getRowMeta();
                                String sql = logdb.getDDL(transLogTable.getTableName(), fields);
                                if (sql == null || sql.length() == 0)
                                {
                                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultOK.CorrectLayout.Description"), null); //$NON-NLS-1$
                                    remarks.add(cr);
                                }
                                else
                                {
                                    cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.LoggingTableNeedsAdjustments.Description") + Const.CR + sql, //$NON-NLS-1$
                                            null);
                                    remarks.add(cr);
                                }

                            }
                            else
                            {
                                cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.LoggingTableDoesNotExist.Description"), null); //$NON-NLS-1$
                                remarks.add(cr);
                            }
                        }
                        else
                        {
                            cr = new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultError.LogTableNotSpecified.Description"), null); //$NON-NLS-1$
                            remarks.add(cr);
                        }
                    }
                    catch (KettleDatabaseException dbe)
                    {

                    }
                    finally
                    {
                        logdb.disconnect();
                    }
                }
                if (monitor != null) monitor.worked(1);

            }

            if (monitor != null) monitor.subTask(BaseMessages.getString(PKG, "TransMeta.Monitor.CheckingForDatabaseUnfriendlyCharactersInFieldNamesTask.Title")); //$NON-NLS-1$
            if (values.size() > 0)
            {
                for(ValueMetaInterface v:values.keySet())
                {
                    String message = values.get(v);
                    CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_WARNING, BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultWarning.Description",v.getName() , message ,v.getOrigin() ), findStep(v.getOrigin())); //$NON-NLS-1$
                    remarks.add(cr);
                }
            }
            else
            {
                CheckResult cr = new CheckResult(CheckResultInterface.TYPE_RESULT_OK,
                        BaseMessages.getString(PKG, "TransMeta.CheckResult.TypeResultOK.Description"), null); //$NON-NLS-1$
                remarks.add(cr);
            }
            if (monitor != null) monitor.worked(1);
        }
        catch (Exception e)
        {
            log.logError(Const.getStackTracker(e));
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Returns the resultRows.
     */
    public List<RowMetaAndData> getResultRows()
    {
        return resultRows;
    }

    /**
     * @param resultRows The resultRows to set.
     */
    public void setResultRows(List<RowMetaAndData> resultRows)
    {
        this.resultRows = resultRows;
    }

    /**
     * @return Returns the directory.
     */
    public RepositoryDirectory getRepositoryDirectory()
    {
        return directory;
    }

    /**
     * @param directory The directory to set.
     */
    public void setRepositoryDirectory(RepositoryDirectory directory)
    {
        this.directory = directory;
        setInternalKettleVariables();
    }

    /**
     * @return The directory path plus the name of the transformation
     */
    public String getPathAndName()
    {
        if (getRepositoryDirectory().isRoot())
            return getRepositoryDirectory().getPath() + getName();
        else
            return getRepositoryDirectory().getPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + getName();
    }

    /**
     * @return Returns the arguments.
     */
    public String[] getArguments()
    {
        return arguments;
    }

    /**
     * @param arguments The arguments to set.
     */
    public void setArguments(String[] arguments)
    {
        this.arguments = arguments;
    }

    /**
     * @return Returns the counters.
     */
    public Hashtable<String,Counter> getCounters()
    {
        return counters;
    }

    /**
     * @param counters The counters to set.
     */
    public void setCounters(Hashtable<String,Counter> counters)
    {
        this.counters = counters;
    }

    /**
     * @return Returns the dependencies.
     */
    public List<TransDependency> getDependencies()
    {
        return dependencies;
    }

    /**
     * @param dependencies The dependencies to set.
     */
    public void setDependencies(List<TransDependency> dependencies)
    {
        this.dependencies = dependencies;
    }


    /**
     * @return Returns the maxDateConnection.
     */
    public DatabaseMeta getMaxDateConnection()
    {
        return maxDateConnection;
    }

    /**
     * @param maxDateConnection The maxDateConnection to set.
     */
    public void setMaxDateConnection(DatabaseMeta maxDateConnection)
    {
        this.maxDateConnection = maxDateConnection;
    }

    /**
     * @return Returns the maxDateDifference.
     */
    public double getMaxDateDifference()
    {
        return maxDateDifference;
    }

    /**
     * @param maxDateDifference The maxDateDifference to set.
     */
    public void setMaxDateDifference(double maxDateDifference)
    {
        this.maxDateDifference = maxDateDifference;
    }

    /**
     * @return Returns the maxDateField.
     */
    public String getMaxDateField()
    {
        return maxDateField;
    }

    /**
     * @param maxDateField The maxDateField to set.
     */
    public void setMaxDateField(String maxDateField)
    {
        this.maxDateField = maxDateField;
    }

    /**
     * @return Returns the maxDateOffset.
     */
    public double getMaxDateOffset()
    {
        return maxDateOffset;
    }

    /**
     * @param maxDateOffset The maxDateOffset to set.
     */
    public void setMaxDateOffset(double maxDateOffset)
    {
        this.maxDateOffset = maxDateOffset;
    }

    /**
     * @return Returns the maxDateTable.
     */
    public String getMaxDateTable()
    {
        return maxDateTable;
    }

    /**
     * @param maxDateTable The maxDateTable to set.
     */
    public void setMaxDateTable(String maxDateTable)
    {
        this.maxDateTable = maxDateTable;
    }

    /**
     * @return Returns the sizeRowset.
     */
    public int getSizeRowset()
    {
        return sizeRowset;
    }

    /**
     * @param sizeRowset The sizeRowset to set.
     */
    public void setSizeRowset(int sizeRowset)
    {
        this.sizeRowset = sizeRowset;
    }

    /**
     * @return Returns the dbCache.
     */
    public DBCache getDbCache()
    {
        return dbCache;
    }

    /**
     * @param dbCache The dbCache to set.
     */
    public void setDbCache(DBCache dbCache)
    {
        this.dbCache = dbCache;
    }

    /**
     * @return Returns the createdDate.
     */
    public Date getCreatedDate()
    {
        return createdDate;
    }

    /**
     * @param createdDate The createdDate to set.
     */
    public void setCreatedDate(Date createdDate)
    {
        this.createdDate = createdDate;
    }

    /**
     * @param createdUser The createdUser to set.
     */
    public void setCreatedUser(String createdUser)
    {
        this.createdUser = createdUser;
    }

    /**
     * @return Returns the createdUser.
     */
    public String getCreatedUser()
    {
        return createdUser;
    }

    /**
     * @param modifiedDate The modifiedDate to set.
     */
    public void setModifiedDate(Date modifiedDate)
    {
        this.modifiedDate = modifiedDate;
    }

    /**
     * @return Returns the modifiedDate.
     */
    public Date getModifiedDate()
    {
        return modifiedDate;
    }

    /**
     * @param modifiedUser The modifiedUser to set.
     */
    public void setModifiedUser(String modifiedUser)
    {
        this.modifiedUser = modifiedUser;
    }

    /**
     * @return Returns the modifiedUser.
     */
    public String getModifiedUser()
    {
        return modifiedUser;
    }





	/**
	 * Get the description of the transformation
	 *
	 * @return The description of the transformation
	 */
	public String getDescription()
	{
		return description;
	}
	/**
	 * Set the description of the transformation.
	 *
	 * @param n The new description of the transformation
	 */
	public void setDescription(String n)
	{
		description = n;
	}
	
	/**
	 * Set the extended description of the transformation.
	 *
	 * @param n The new extended description of the transformation
	 */
	public void setExtendedDescription(String n)
	{
		extended_description = n;
	}

	/**
	 * Get the extended description of the transformation
	 *
	 * @return The extended description of the transformation
	 */
	public String getExtendedDescription()
	{
		return extended_description;
	}

	/**
	 * Get the version of the transformation
	 *
	 * @return The version of the transformation
	 */
	public String getTransversion()
	{
		return trans_version;
	}

	/**
	 * Set the version of the transformation.
	 *
	 * @param n The new version description of the transformation
	 */
	public void setTransversion(String n)
	{
		trans_version = n;
	}

	/**
	 * Set the status of the transformation.
	 *
	 * @param n The new status description of the transformation
	 */
	public void setTransstatus(int n)
	{
		trans_status = n;
	}
	/**
	 * Get the status of the transformation
	 *
	 * @return The status of the transformation
	 */
	public int getTransstatus()
	{
		return trans_status;
	}

    /**
     * @return the textual representation of the transformation: it's name. If the name has not been set, the classname
     * is returned.
     */
    public String toString()
    {
        if (!Const.isEmpty(filename)) {
        	if (Const.isEmpty(name)) {
        		return filename;
        	} else {
        		return filename+" : "+name;
        	}
        }

        if (name != null) {
        	if (directory!=null) {
        		String path = directory.getPath();
        		if (path.endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR)) {
        			return path+name;
        		} else {
        			return path+RepositoryDirectory.DIRECTORY_SEPARATOR+name;
        		}
        	} else {
        		return name;
        	}
        } else {
        	return TransMeta.class.getName();
        }
    }

    /**
     * Cancel queries opened for checking & fieldprediction
     */
    public void cancelQueries() throws KettleDatabaseException
    {
        for (int i = 0; i < nrSteps(); i++)
        {
            getStep(i).getStepMetaInterface().cancelQueries();
        }
    }

    /**
     * Get the arguments used by this transformation.
     *
     * @param arguments
     * @return A row with the used arguments in it.
     */
    public Map<String, String> getUsedArguments(String[] arguments)
    {
    	Map<String, String> transArgs = new HashMap<String, String>();
    	
        for (int i = 0; i < nrSteps(); i++)
        {
            StepMetaInterface smi = getStep(i).getStepMetaInterface();
            Map<String, String> stepArgs = smi.getUsedArguments(); // Get the command line arguments that this step uses.
            if (stepArgs != null)
            {
            	transArgs.putAll(stepArgs);
            }
        }

        // OK, so perhaps, we can use the arguments from a previous execution?
        String[] saved = Props.isInitialized() ? Props.getInstance().getLastArguments() : null;

        // Set the default values on it...
        // Also change the name to "Argument 1" .. "Argument 10"
        //
        for (String argument : transArgs.keySet()) 
        {
        	String value = "";
        	int argNr = Const.toInt(argument, -1);
            if (arguments!=null && argNr > 0 && argNr <= arguments.length)
            {
            	value = Const.NVL(arguments[argNr-1], "");
            }
            if (value.length()==0) // try the saved option...
            {
                if (argNr > 0 && argNr < saved.length && saved[argNr] != null)
                {
                    value = saved[argNr-1];
                }
            }
            transArgs.put(argument, value);
        }
        
        return transArgs;
    }

    /**
     * @return Sleep time waiting when buffer is empty, in nano-seconds
     */
    public int getSleepTimeEmpty()
    {
        return Const.TIMEOUT_GET_MILLIS;
    }

    /**
     * @return Sleep time waiting when buffer is full, in nano-seconds
     */
    public int getSleepTimeFull()
    {
        return Const.TIMEOUT_PUT_MILLIS;
    }

    /**
     * @param sleepTimeEmpty The sleepTimeEmpty to set.
     */
    public void setSleepTimeEmpty(int sleepTimeEmpty)
    {
        this.sleepTimeEmpty = sleepTimeEmpty;
    }

    /**
     * @param sleepTimeFull The sleepTimeFull to set.
     */
    public void setSleepTimeFull(int sleepTimeFull)
    {
        this.sleepTimeFull = sleepTimeFull;
    }

    /**
     * This method asks all steps in the transformation whether or not the specified database connection is used.
     * The connection is used in the transformation if any of the steps uses it or if it is being used to log to.
     * @param databaseMeta The connection to check
     * @return true if the connection is used in this transformation.
     */
    public boolean isDatabaseConnectionUsed(DatabaseMeta databaseMeta)
    {
        for (int i=0;i<nrSteps();i++)
        {
            StepMeta stepMeta = getStep(i);
            DatabaseMeta dbs[] = stepMeta.getStepMetaInterface().getUsedDatabaseConnections();
            for (int d=0;d<dbs.length;d++)
            {
                if (dbs[d].equals(databaseMeta)) return true;
            }
        }

        if (transLogTable.getDatabaseMeta()!=null && transLogTable.getDatabaseMeta().equals(databaseMeta)) return true;

        return false;
    }

    /*
    public List getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(List inputFiles) {
        this.inputFiles = inputFiles;
    }
    */

    /**
     * Get a list of all the strings used in this transformation.
     *
     * @return A list of StringSearchResult with strings used in the 
     */
    public List<StringSearchResult> getStringList(boolean searchSteps, boolean searchDatabases, boolean searchNotes, boolean includePasswords)
    {
        List<StringSearchResult> stringList = new ArrayList<StringSearchResult>();

        if (searchSteps)
        {
            // Loop over all steps in the transformation and see what the used vars are...
            for (int i=0;i<nrSteps();i++)
            {
                StepMeta stepMeta = getStep(i);
                stringList.add(new StringSearchResult(stepMeta.getName(), stepMeta, this, "Step name"));
                if (stepMeta.getDescription()!=null) stringList.add(new StringSearchResult(stepMeta.getDescription(), stepMeta, this, "Step description"));
                StepMetaInterface metaInterface = stepMeta.getStepMetaInterface();
                StringSearcher.findMetaData(metaInterface, 1, stringList, stepMeta, this);
            }
        }

        // Loop over all steps in the transformation and see what the used vars are...
        if (searchDatabases)
        {
            for (int i=0;i<nrDatabases();i++)
            {
                DatabaseMeta meta = getDatabase(i);
                stringList.add(new StringSearchResult(meta.getName(), meta, this, "Database connection name"));
                if (meta.getHostname()!=null) stringList.add(new StringSearchResult(meta.getHostname(), meta, this, "Database hostname"));
                if (meta.getDatabaseName()!=null) stringList.add(new StringSearchResult(meta.getDatabaseName(), meta, this, "Database name"));
                if (meta.getUsername()!=null) stringList.add(new StringSearchResult(meta.getUsername(), meta, this, "Database Username"));
                if (meta.getPluginId()!=null) stringList.add(new StringSearchResult(meta.getPluginId(), meta, this, "Database type description"));
                if (meta.getDatabasePortNumberString()!=null) stringList.add(new StringSearchResult(meta.getDatabasePortNumberString(), meta, this, "Database port"));
                if (meta.getServername()!=null) stringList.add(new StringSearchResult(meta.getServername(), meta, this, "Database server")); 
                if ( includePasswords )
                {
                	if (meta.getPassword()!=null) stringList.add(new StringSearchResult(meta.getPassword(), meta, this, "Database password"));
                }               
            }
        }

        // Loop over all steps in the transformation and see what the used vars are...
        if (searchNotes)
        {
            for (int i=0;i<nrNotes();i++)
            {
                NotePadMeta meta = getNote(i);
                if (meta.getNote()!=null) stringList.add(new StringSearchResult(meta.getNote(), meta, this, "Notepad text"));
            }
        }

        return stringList;
    }

    public List<StringSearchResult> getStringList(boolean searchSteps, boolean searchDatabases, boolean searchNotes)
    {
    	return getStringList(searchSteps, searchDatabases, searchNotes, false);
    }    
    
    public List<String> getUsedVariables()
    {
        // Get the list of Strings.
        List<StringSearchResult> stringList = getStringList(true, true, false, true);

        List<String> varList = new ArrayList<String>();

        // Look around in the strings, see what we find...
        for (int i=0;i<stringList.size();i++)
        {
            StringSearchResult result = stringList.get(i);
            StringUtil.getUsedVariables(result.getString(), varList, false);
        }

        return varList;
    }

	/**
	 * @return Returns the previousResult.
	 */
	public Result getPreviousResult()
	{
		return previousResult;
	}

	/**
	 * @param previousResult The previousResult to set.
	 */
	public void setPreviousResult(Result previousResult)
	{
		this.previousResult = previousResult;
	}

	/**
	 * @return Returns the resultFiles.
	 */
	public List<ResultFile> getResultFiles()
	{
		return resultFiles;
	}

	/**
	 * @param resultFiles The resultFiles to set.
	 */
	public void setResultFiles(List<ResultFile> resultFiles)
	{
		this.resultFiles = resultFiles;
	}   

    /**
     * @return the partitionSchemas
     */
    public List<PartitionSchema> getPartitionSchemas()
    {
        return partitionSchemas;
    }

    /**
     * @param partitionSchemas the partitionSchemas to set
     */
    public void setPartitionSchemas(List<PartitionSchema> partitionSchemas)
    {
        this.partitionSchemas = partitionSchemas;
    }

    /**
     * @return the available partition schema names.
     */
    public String[] getPartitionSchemasNames()
    {
        String names[] = new String[partitionSchemas.size()];
        for (int i=0;i<names.length;i++)
        {
            names[i] = partitionSchemas.get(i).getName();
        }
        return names;
    }

    /**
     * @return the feedbackShown
     */
    public boolean isFeedbackShown()
    {
        return feedbackShown;
    }

    /**
     * @param feedbackShown the feedbackShown to set
     */
    public void setFeedbackShown(boolean feedbackShown)
    {
        this.feedbackShown = feedbackShown;
    }

    /**
     * @return the feedbackSize
     */
    public int getFeedbackSize()
    {
        return feedbackSize;
    }

    /**
     * @param feedbackSize the feedbackSize to set
     */
    public void setFeedbackSize(int feedbackSize)
    {
        this.feedbackSize = feedbackSize;
    }

    /**
     * @return the usingUniqueConnections
     */
    public boolean isUsingUniqueConnections()
    {
        return usingUniqueConnections;
    }

    /**
     * @param usingUniqueConnections the usingUniqueConnections to set
     */
    public void setUsingUniqueConnections(boolean usingUniqueConnections)
    {
        this.usingUniqueConnections = usingUniqueConnections;
    }

    public List<ClusterSchema> getClusterSchemas()
    {
        return clusterSchemas;
    }

    public void setClusterSchemas(List<ClusterSchema> clusterSchemas)
    {
        this.clusterSchemas = clusterSchemas;
    }
    
    /**
     * @return The slave server strings from this cluster schema
     */
    public String[] getClusterSchemaNames()
    {
        String[] names = new String[clusterSchemas.size()];
        for (int i=0;i<names.length;i++)
        {
            names[i] = clusterSchemas.get(i).getName();
        }
        return names;
    }

    /**
     * Find a partition schema using its name.
     * @param name The name of the partition schema to look for.
     * @return the partition with the specified name of null if nothing was found 
     */
    public PartitionSchema findPartitionSchema(String name)
    {
        for (int i=0;i<partitionSchemas.size();i++)
        {
            PartitionSchema schema = partitionSchemas.get(i);
            if (schema.getName().equalsIgnoreCase(name)) return schema;
        }
        return null;
    }
    
    /**
     * Find a clustering schema using its name
     * @param name The name of the clustering schema to look for.
     * @return the cluster schema with the specified name of null if nothing was found 
     */
    public ClusterSchema findClusterSchema(String name)
    {
        for (int i=0;i<clusterSchemas.size();i++)
        {
            ClusterSchema schema = clusterSchemas.get(i);
            if (schema.getName().equalsIgnoreCase(name)) return schema;
        }
        return null;
    }
    
    /**
     * Add a new partition schema to the transformation if that didn't exist yet.
     * Otherwise, replace it.
     *
     * @param partitionSchema The partition schema to be added.
     */
    public void addOrReplacePartitionSchema(PartitionSchema partitionSchema)
    {
        int index = partitionSchemas.indexOf(partitionSchema);
        if (index<0)
        {
            partitionSchemas.add(partitionSchema);
        }
        else
        {
            PartitionSchema previous = partitionSchemas.get(index);
            previous.replaceMeta(partitionSchema);
        }
        setChanged();
    }
    
    /**
     * Add a new slave server to the transformation if that didn't exist yet.
     * Otherwise, replace it.
     *
     * @param slaveServer The slave server to be added.
     */
    public void addOrReplaceSlaveServer(SlaveServer slaveServer)
    {
        int index = slaveServers.indexOf(slaveServer);
        if (index<0)
        {
            slaveServers.add(slaveServer); 
        }
        else
        {
            SlaveServer previous = slaveServers.get(index);
            previous.replaceMeta(slaveServer);
        }
        setChanged();
    }
    
    /**
     * Add a new cluster schema to the transformation if that didn't exist yet.
     * Otherwise, replace it.
     *
     * @param clusterSchema The cluster schema to be added.
     */
    public void addOrReplaceClusterSchema(ClusterSchema clusterSchema)
    {
        int index = clusterSchemas.indexOf(clusterSchema);
        if (index<0)
        {
            clusterSchemas.add(clusterSchema); 
        }
        else 
        {
            ClusterSchema previous = clusterSchemas.get(index);
            previous.replaceMeta(clusterSchema);
        }
        setChanged();
    }

    public String getSharedObjectsFile()
    {
        return sharedObjectsFile;
    }

    public void setSharedObjectsFile(String sharedObjectsFile)
    {
        this.sharedObjectsFile = sharedObjectsFile;
    }

    public void saveSharedObjects() throws KettleException
    {
        try
        {
            // First load all the shared objects...
            String soFile = environmentSubstitute(sharedObjectsFile);
            SharedObjects sharedObjects = new SharedObjects(soFile);
            
            // Now overwrite the objects in there
            List<SharedObjectInterface> shared = new ArrayList<SharedObjectInterface>();
            shared.addAll(databases);
            shared.addAll(steps);
            shared.addAll(partitionSchemas);
            shared.addAll(slaveServers);
            shared.addAll(clusterSchemas);
            
            // The databases connections...
            for (SharedObjectInterface sharedObject : shared )
            {
                if (sharedObject.isShared()) 
                {
                    sharedObjects.storeObject(sharedObject);
                }
            }
            
            // Save the objects
            sharedObjects.saveToFile();
        }
        catch(Exception e)
        {
            throw new KettleException("Unable to save shared ojects", e);
        }
    }

    /**
     * @return the usingThreadPriorityManagment
     */
    public boolean isUsingThreadPriorityManagment()
    {
        return usingThreadPriorityManagment;
    }

    /**
     * @param usingThreadPriorityManagment the usingThreadPriorityManagment to set
     */
    public void setUsingThreadPriorityManagment(boolean usingThreadPriorityManagment)
    {
        this.usingThreadPriorityManagment = usingThreadPriorityManagment;
    }

    public SlaveServer findSlaveServer(String serverString)
    {
        return SlaveServer.findSlaveServer(slaveServers, serverString);
    }
    
    public String[] getSlaveServerNames()
    {
        return SlaveServer.getSlaveServerNames(slaveServers);
    }

    /**
     * @return the slaveServers
     */
    public List<SlaveServer> getSlaveServers()
    {
        return slaveServers;
    }

    /**
     * @param slaveServers the slaveServers to set
     */
    public void setSlaveServers(List<SlaveServer> slaveServers)
    {
        this.slaveServers = slaveServers;
    }

    /**
     * Check a step to see if there are no multiple steps to read from.
     * If so, check to see if the receiving rows are all the same in layout.
     * We only want to ONLY use the DBCache for this to prevent GUI stalls.
     * 
     * @param stepMeta the step to check
     * @throws KettleRowException in case we detect a row mixing violation
     *
     */
    public void checkRowMixingStatically(StepMeta stepMeta, ProgressMonitorListener monitor) throws KettleRowException
    {
       int nrPrevious = findNrPrevSteps(stepMeta);
       if (nrPrevious>1)
       {
           RowMetaInterface referenceRow = null;
           // See if all previous steps send out the same rows...
           for (int i=0;i<nrPrevious;i++)
           {
               StepMeta previousStep = findPrevStep(stepMeta, i);
               try
               {
                   RowMetaInterface row = getStepFields(previousStep, monitor); // Throws KettleStepException
                   if (referenceRow==null)
                   {
                       referenceRow = row;
                   }
                   else
                       if ( ! stepMeta.getStepMetaInterface().excludeFromRowLayoutVerification())
                       {
                       {
                    	   BaseStep.safeModeChecking(referenceRow, row);
                       }                       
                   }
               }
               catch(KettleStepException e)
               {
                   // We ignore this one because we are in the process of designing the transformation, anything intermediate can go wrong.
               }
           }
       }
    }   
    
    public void setInternalKettleVariables()
    {        
    	setInternalKettleVariables(variables);
    }
    
    public void setInternalKettleVariables(VariableSpace var)
    {        
      setInternalFilenameKettleVariables(var);
      setInternalNameKettleVariable(var);

        // The name of the directory in the repository
        //
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, directory!=null?directory.getPath():"");
        
        // Here we don't remove the job specific parameters, as they may come in handy.
        //
        if (var.getVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY)==null)
        {
        	var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "Parent Job File Directory"); //$NON-NLS-1$
        }
        if (var.getVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME)==null)
        {
        	var.setVariable(Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "Parent Job Filename"); //$NON-NLS-1$
        }
        if (var.getVariable(Const.INTERNAL_VARIABLE_JOB_NAME)==null)
        {
        	var.setVariable(Const.INTERNAL_VARIABLE_JOB_NAME, "Parent Job Name"); //$NON-NLS-1$
        }
        if (var.getVariable(Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY)==null)
        {
        	var.setVariable(Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, "Parent Job Repository Directory"); //$NON-NLS-1$        
        }
    }    
    
  private void setInternalNameKettleVariable(VariableSpace var) {
    // The name of the transformation
    //
    var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, Const.NVL(name, ""));
  }

  private void setInternalFilenameKettleVariables(VariableSpace var) {
    // If we have a filename that's defined, set variables.  If not, clear them.
    //
    if (!Const.isEmpty(filename)) 
    {
      try {
        FileObject fileObject = KettleVFS.getFileObject(filename, var);
        FileName fileName = fileObject.getName();

        // The filename of the transformation
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, fileName.getBaseName());

        // The directory of the transformation
        FileName fileDir = fileName.getParent();
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, fileDir.getURI());
      } catch (KettleFileException e) {
        log.logError("Unexpected error setting internal filename variables!", e);
        
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "");
        var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "");
      }
    } else {
      var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, "");
      var.setVariable(Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, "");
    }

  }

  public void copyVariablesFrom(VariableSpace space) {
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

    public StepMeta findMappingInputStep(String stepname) throws KettleStepException {
		if (!Const.isEmpty(stepname)) {
			StepMeta stepMeta = findStep(stepname); // TODO verify that it's a mapping input!!
			if (stepMeta==null) {
				throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.StepNameNotFound", stepname));
			}
			return stepMeta;
		}
		else {
			// Find the first mapping input step that fits the bill.
			StepMeta stepMeta = null;
			for (StepMeta mappingStep : steps) {
				if (mappingStep.getStepID().equals("MappingInput")) {
					if (stepMeta==null) {
						stepMeta = mappingStep;
					} 
					else if (stepMeta!=null) {
						throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.OnlyOneMappingInputStepAllowed", "2"));
					}
				}
			}
			if (stepMeta==null) {
				throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.OneMappingInputStepRequired"));
			}
			return stepMeta;
		}
    }
    
    public StepMeta findMappingOutputStep(String stepname) throws KettleStepException {
		if (!Const.isEmpty(stepname)) {
			StepMeta stepMeta = findStep(stepname); // TODO verify that it's a mapping output step.
			if (stepMeta==null) {
				throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.StepNameNotFound", stepname));
			}
			return stepMeta;
		}
		else {
			// Find the first mapping output step that fits the bill.
			StepMeta stepMeta = null;
			for (StepMeta mappingStep : steps) {
				if (mappingStep.getStepID().equals("MappingOutput")) {
					if (stepMeta==null) {
						stepMeta = mappingStep;
					} 
					else if (stepMeta!=null) {
						throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.OnlyOneMappingOutputStepAllowed", "2"));
					}
				}
			}
			if (stepMeta==null) {
				throw new KettleStepException(BaseMessages.getString(PKG, "TransMeta.Exception.OneMappingOutputStepRequired"));
			}
			return stepMeta;
		}
    }
    
    public List<ResourceReference> getResourceDependencies() {
    	List<ResourceReference> resourceReferences = new ArrayList<ResourceReference>();
    
    	for (StepMeta stepMeta : steps) {
    		resourceReferences.addAll( stepMeta.getResourceDependencies(this) );
    	}
    	
    	return resourceReferences;
    }
    
	public String exportResources(VariableSpace space, Map<String, ResourceDefinition> definitions, ResourceNamingInterface resourceNamingInterface, Repository repository) throws KettleException {
		try {
			// Handle naming for both repository and XML bases resources...
			//
			String baseName;
			String originalPath;
			String fullname;
			String extension="ktr";
			if (Const.isEmpty(getFilename())) {
				// Assume repository...
				//
				originalPath = directory.getPath();
				baseName = getName();
				fullname = directory.getPath()+( directory.getPath().endsWith(RepositoryDirectory.DIRECTORY_SEPARATOR) ? "" : RepositoryDirectory.DIRECTORY_SEPARATOR ) +getName()+"."+extension; // $NON-NLS-1$ // $NON-NLS-2$  
			} else {
				// Assume file
				//
				FileObject fileObject = KettleVFS.getFileObject(space.environmentSubstitute(getFilename()), space);
				originalPath = fileObject.getParent().getName().getPath();
				baseName = fileObject.getName().getBaseName();
				fullname = fileObject.getName().getPath();
			}
			
			String exportFileName = resourceNamingInterface.nameResource(baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.TRANSFORMATION);
			ResourceDefinition definition = definitions.get(exportFileName);
			if (definition == null) {
				// If we do this once, it will be plenty :-)
				//
				TransMeta transMeta = (TransMeta) this.realClone(false);
				// transMeta.copyVariablesFrom(space);

				// Add used resources, modify transMeta accordingly
				// Go through the list of steps, etc.
				// These critters change the steps in the cloned TransMeta
				// At the end we make a new XML version of it in "exported"
				// format...

				// loop over steps, databases will be exported to XML anyway.
				//
				for (StepMeta stepMeta : transMeta.getSteps()) {
					stepMeta.exportResources(space, definitions, resourceNamingInterface, repository);
				}

				// Change the filename, calling this sets internal variables
				// inside of the transformation.
				//
				transMeta.setFilename(exportFileName);
				
				// All objects get re-located to the root folder
				//
				transMeta.setRepositoryDirectory(new RepositoryDirectory());
				
				// Set a number of parameters for all the data files referenced so far...
				//
				Map<String, String> directoryMap = resourceNamingInterface.getDirectoryMap();
				if (directoryMap!=null) {
					for (String directory : directoryMap.keySet()) {
						String parameterName = directoryMap.get(directory);
						transMeta.addParameterDefinition(parameterName, directory, "Data file path discovered during export");
					}
				}

				// At the end, add ourselves to the map...
				//
				String transMetaContent = transMeta.getXML();

				definition = new ResourceDefinition(exportFileName, transMetaContent);

	  			// Also remember the original filename (if any), including variables etc.
	  			//
				if (Const.isEmpty(this.getFilename())) { // Repository
					definition.setOrigin(fullname);
				} else {
					definition.setOrigin(this.getFilename());
				}

				definitions.put(fullname, definition);
			}
			return exportFileName;
		} catch (FileSystemException e) {
			throw new KettleException(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename()), e); //$NON-NLS-1$
		} catch (KettleFileException e) {
			throw new KettleException(BaseMessages.getString(PKG, "TransMeta.Exception.ErrorOpeningOrValidatingTheXMLFile", getFilename()), e); //$NON-NLS-1$
		}
	}

	/**
	 * @return the slaveStepCopyPartitionDistribution
	 */
	public SlaveStepCopyPartitionDistribution getSlaveStepCopyPartitionDistribution() {
		return slaveStepCopyPartitionDistribution;
	}

	/**
	 * @param slaveStepCopyPartitionDistribution the slaveStepCopyPartitionDistribution to set
	 */
	public void setSlaveStepCopyPartitionDistribution(SlaveStepCopyPartitionDistribution slaveStepCopyPartitionDistribution) {
		this.slaveStepCopyPartitionDistribution = slaveStepCopyPartitionDistribution;
	}
	
	public ClusterSchema findFirstUsedClusterSchema() {
		for (StepMeta stepMeta : steps) {
			if (stepMeta.getClusterSchema()!=null) return stepMeta.getClusterSchema();
		}
		return null;
	}

	public boolean isSlaveTransformation() {
		return slaveTransformation;
	}

	public void setSlaveTransformation(boolean slaveTransformation) {
		this.slaveTransformation = slaveTransformation;
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
	 * @return the capturingStepPerformanceSnapShots
	 */
	public boolean isCapturingStepPerformanceSnapShots() {
		return capturingStepPerformanceSnapShots;
	}

	/**
	 * @param capturingStepPerformanceSnapShots the capturingStepPerformanceSnapShots to set
	 */
	public void setCapturingStepPerformanceSnapShots(boolean capturingStepPerformanceSnapShots) {
		this.capturingStepPerformanceSnapShots = capturingStepPerformanceSnapShots;
	}

	/**
	 * @return the stepPerformanceCapturingDelay
	 */
	public long getStepPerformanceCapturingDelay() {
		return stepPerformanceCapturingDelay;
	}

	/**
	 * @param stepPerformanceCapturingDelay the stepPerformanceCapturingDelay to set
	 */
	public void setStepPerformanceCapturingDelay(long stepPerformanceCapturingDelay) {
		this.stepPerformanceCapturingDelay = stepPerformanceCapturingDelay;
	}

	/**
   * @return the stepPerformanceCapturingSizeLimit
   */
  public String getStepPerformanceCapturingSizeLimit() {
    return stepPerformanceCapturingSizeLimit;
  }

  /**
   * @param stepPerformanceCapturingSizeLimit the stepPerformanceCapturingSizeLimit to set
   */
  public void setStepPerformanceCapturingSizeLimit(String stepPerformanceCapturingSizeLimit) {
    this.stepPerformanceCapturingSizeLimit = stepPerformanceCapturingSizeLimit;
  }

  /**
	 * @return the sharedObjects
	 */
	public SharedObjects getSharedObjects() {
		return sharedObjects;
	}

	/**
	 * @param sharedObjects the sharedObjects to set
	 */
	public void setSharedObjects(SharedObjects sharedObjects) {
		this.sharedObjects = sharedObjects;
	}
	
	private void clearStepFieldsCachce() {
		stepsFieldsCache.clear();
	}

	private void clearLoopCachce() {
		loopCache.clear();
	}
	
	public void addNameChangedListener(NameChangedListener listener) {
		if (nameChangedListeners==null) {
			nameChangedListeners = new ArrayList<NameChangedListener>();
		}
		nameChangedListeners.add(listener);
	}
	
	public void removeNameChangedListener(NameChangedListener listener) {
		nameChangedListeners.remove(listener);
	}

	public void addFilenameChangedListener(FilenameChangedListener listener) {
		if (filenameChangedListeners==null) {
			filenameChangedListeners = new ArrayList<FilenameChangedListener>();
		}
		filenameChangedListeners.add(listener);
	}

	public void removeFilenameChangedListener(FilenameChangedListener listener) {
		filenameChangedListeners.remove(listener);
	}
	
	private boolean nameChanged(String oldFilename, String newFilename) {
		if (oldFilename==null && newFilename==null) return false;
		if (oldFilename==null && newFilename!=null) return true;
		return oldFilename.equals(newFilename);
	}
	
	private void fireFilenameChangedListeners(String oldFilename, String newFilename) {
		if (nameChanged(oldFilename, newFilename)) {
			if (filenameChangedListeners!=null) {
				for (FilenameChangedListener listener : filenameChangedListeners) {
					listener.filenameChanged(this, oldFilename, newFilename);
				}
			}
		}
	}

	private void fireNameChangedListeners(String oldName, String newName) {
		if (nameChanged(oldName, newName)) {
			if (nameChangedListeners!=null) {
				for (NameChangedListener listener : nameChangedListeners) {
					listener.nameChanged(this, oldName, newName);
				}
			}
		}
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

	public void addParameterDefinition(String key, String defaultValue, String description) throws DuplicateParamException {
		namedParams.addParameterDefinition(key, defaultValue, description);		
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

	public void copyParametersFrom(NamedParams params) {
		namedParams.copyParametersFrom(params);		
	}
	
	public RepositoryObjectType getRepositoryElementType() {
		return REPOSITORY_ELEMENT_TYPE;
	}

	public void setObjectRevision(ObjectRevision objectRevision) {
		this.objectVersion = objectRevision;
	}
	
	public ObjectRevision getObjectRevision() {
		return objectVersion;
	}
	
	public LogChannelInterface getLogChannel() {
		return log;
	}

	public String getLogChannelId() {
		return log.getLogChannelId();
	}
	
	public String getObjectName() {
		return getName();
	}

	public String getObjectCopy() {
		return null;
	}

	public LoggingObjectType getObjectType() {
		return LoggingObjectType.TRANSMETA;
	}

	public LoggingObjectInterface getParent() {
		return null; // TODO, we could also keep a link to the parent and job metadata
	}
	
	public LogLevel getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(LogLevel logLevel) {
    this.logLevel = logLevel;
    log.setLogLevel(logLevel);
  }

	/**
	 * @return the transLogTable
	 */
	public TransLogTable getTransLogTable() {
		return transLogTable;
	}

	/**
	 * @param transLogTable the transLogTable to set
	 */
	public void setTransLogTable(TransLogTable transLogTable) {
		this.transLogTable = transLogTable;
	}

	public String[] getDatabaseNames() {
		String[] names = new String[databases.size()];
		for (int i=0;i<names.length;i++) {
			names[i] = databases.get(i).getName();
		}
		return names;
	}

	/**
	 * @return the performanceLogTable
	 */
	public PerformanceLogTable getPerformanceLogTable() {
		return performanceLogTable;
	}

	/**
	 * @param performanceLogTable the performanceLogTable to set
	 */
	public void setPerformanceLogTable(PerformanceLogTable performanceLogTable) {
		this.performanceLogTable = performanceLogTable;
	}

	/**
	 * @return the channelLogTable
	 */
	public ChannelLogTable getChannelLogTable() {
		return channelLogTable;
	}

	/**
	 * @param channelLogTable the channelLogTable to set
	 */
	public void setChannelLogTable(ChannelLogTable channelLogTable) {
		this.channelLogTable = channelLogTable;
	}

	/**
	 * @return the stepLogTable
	 */
	public StepLogTable getStepLogTable() {
		return stepLogTable;
	}

	/**
	 * @param stepLogTable the stepLogTable to set
	 */
	public void setStepLogTable(StepLogTable stepLogTable) {
		this.stepLogTable = stepLogTable;
	}

	public List<LogTableInterface> getLogTables() {
		List<LogTableInterface> logTables = new ArrayList<LogTableInterface>();
		logTables.add(transLogTable);
		logTables.add(stepLogTable);
		logTables.add(performanceLogTable);
		logTables.add(channelLogTable);
		return logTables;
	}

	/**
	 * @return the transformationType
	 */
	public TransformationType getTransformationType() {
		return transformationType;
	}

	/**
	 * @param transformationType the transformationType to set
	 */
	public void setTransformationType(TransformationType transformationType) {
		this.transformationType = transformationType;
	}
	
	public boolean canSave() {
	  return true;
	}

  /**
   * @return the containerObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * @param containerObjectId the containerObjectId to set
   */
  public void setCarteObjectId(String containerObjectId) {
    this.containerObjectId = containerObjectId;
  }
	
}