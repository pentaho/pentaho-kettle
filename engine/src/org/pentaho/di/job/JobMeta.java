//CHECKSTYLE:FileLength:OFF
/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.listeners.FilenameChangedListener;
import org.pentaho.di.core.listeners.NameChangedListener;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LogTablePluginInterface;
import org.pentaho.di.core.logging.LogTablePluginInterface.TableType;
import org.pentaho.di.core.logging.LogTablePluginType;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.reflection.StringSearcher;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * The definition of a PDI job is represented by a JobMeta object. It is typically loaded from a .kjb file, a PDI
 * repository, or it is generated dynamically. The declared parameters of the job definition are then queried using
 * listParameters() and assigned values using calls to setParameterValue(..). JobMeta provides methods to load, save,
 * verify, etc.
 *
 * @author Matt
 * @since 11-08-2003
 *
 */
public class JobMeta extends ChangedFlag implements Cloneable, Comparable<JobMeta>, XMLInterface, UndoInterface,
  HasDatabasesInterface, VariableSpace, EngineMetaInterface, ResourceExportInterface, HasSlaveServersInterface,
  NamedParams, RepositoryElementInterface, LoggingObjectInterface, HasRepositoryInterface, AttributesInterface {

  private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "job";

  protected static final String XML_TAG_SLAVESERVERS = "slaveservers";

  /** A constant specifying the repository element type as a Job. */
  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.JOB;

  protected ObjectId objectId;

  protected LogLevel logLevel = DefaultLogLevel.getLogLevel();

  protected String containerObjectId;

  protected String name;

  protected String description;

  protected String extendedDescription;

  protected String jobVersion;

  protected int jobStatus;

  protected String filename;

  protected List<JobEntryCopy> jobcopies;

  protected List<JobHopMeta> jobhops;

  protected List<NotePadMeta> notes;

  protected List<DatabaseMeta> databases;

  protected List<SlaveServer> slaveServers;

  protected RepositoryDirectoryInterface directory;

  protected String[] arguments;

  protected boolean changedEntries, changedHops, changedNotes, changedDatabases;

  protected JobLogTable jobLogTable;

  protected JobEntryLogTable jobEntryLogTable;

  protected ChannelLogTable channelLogTable;

  protected List<LogTableInterface> extraLogTables;

  protected List<TransAction> undo;

  protected VariableSpace variables = new Variables();

  protected int max_undo;

  protected int undo_position;

  /** Constant = 1 **/
  public static final int TYPE_UNDO_CHANGE = 1;

  /** Constant = 2 **/
  public static final int TYPE_UNDO_NEW = 2;

  /** Constant = 3 **/
  public static final int TYPE_UNDO_DELETE = 3;

  /** Constant = 4 **/
  public static final int TYPE_UNDO_POSITION = 4;

  /** Constant = "SPECIAL" **/
  public static final String STRING_SPECIAL = "SPECIAL";

  /** Constant = "START" **/
  public static final String STRING_SPECIAL_START = "START";

  /** Constant = "DUMMY" **/
  public static final String STRING_SPECIAL_DUMMY = "DUMMY";

  /** Constant = "OK" **/
  public static final String STRING_SPECIAL_OK = "OK";

  /** Constant = "ERROR" **/
  public static final String STRING_SPECIAL_ERROR = "ERROR";

  /**
   * List of booleans indicating whether or not to remember the size and position of the different windows...
   */
  public boolean[] max = new boolean[1];

  protected String created_user, modifiedUser;

  protected Date created_date, modifiedDate;

  protected boolean batchIdPassed;

  /**
   * If this is null, we load from the default shared objects file : $KETTLE_HOME/.kettle/shared.xml
   */
  protected String sharedObjectsFile;

  /** The last loaded version of the shared objects */
  protected SharedObjects sharedObjects;

  protected List<NameChangedListener> nameChangedListeners;

  protected List<FilenameChangedListener> filenameChangedListeners;

  protected NamedParams namedParams = new NamedParamsDefault();

  protected static final String XML_TAG_PARAMETERS = "parameters";

  /** The repository to reference in the one-off case that it is needed */
  protected Repository repository;

  protected ObjectRevision objectRevision;

  protected List<ContentChangedListener> contentChangedListeners;

  protected IMetaStore metaStore;

  protected Map<String, Map<String, String>> attributesMap;

  /**
   * Instantiates a new job meta.
   *
   */
  public JobMeta() {
    clear();
    initializeVariablesFrom( null );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryElementInterface#getObjectId()
   */
  public ObjectId getObjectId() {
    return objectId;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.EngineMetaInterface#setObjectId(org.pentaho.di.repository.ObjectId)
   */
  public void setObjectId( ObjectId objectId ) {
    this.objectId = objectId;
  }

  /**
   * Clears or reinitializes many of the JobMeta properties.
   */
  public void clear() {
    setName( null );
    setFilename( null );

    jobcopies = new ArrayList<JobEntryCopy>();
    jobhops = new ArrayList<JobHopMeta>();
    notes = new ArrayList<NotePadMeta>();
    databases = new ArrayList<DatabaseMeta>();
    slaveServers = new ArrayList<SlaveServer>();

    jobLogTable = JobLogTable.getDefault( this, this );
    channelLogTable = ChannelLogTable.getDefault( this, this );
    jobEntryLogTable = JobEntryLogTable.getDefault( this, this );
    extraLogTables = new ArrayList<LogTableInterface>();

    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins( LogTablePluginType.class );
    for ( PluginInterface plugin : plugins ) {
      try {
        LogTablePluginInterface logTablePluginInterface =
          (LogTablePluginInterface) PluginRegistry.getInstance().loadClass( plugin );
        if ( logTablePluginInterface.getType() == TableType.JOB ) {
          logTablePluginInterface.setContext( this, this );
          extraLogTables.add( logTablePluginInterface );
        }
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Error loading log table plugin with ID " + plugin.getIds()[0], e );
      }
    }

    attributesMap = new HashMap<String, Map<String, String>>();

    arguments = null;

    max_undo = Const.MAX_UNDO;

    undo = new ArrayList<TransAction>();
    undo_position = -1;

    addDefaults();
    setChanged( false );

    created_user = "-";
    created_date = new Date();

    modifiedUser = "-";
    modifiedDate = new Date();
    directory = new RepositoryDirectory();
    description = null;
    jobStatus = -1;
    jobVersion = null;
    extendedDescription = null;

    // setInternalKettleVariables(); Don't clear the internal variables for
    // ad-hoc jobs, it's ruins the previews
    // etc.
  }

  /**
   * Adds the defaults.
   */
  public void addDefaults() {
    /*
     * addStart(); // Add starting point! addDummy(); // Add dummy! addOK(); // errors == 0 evaluation addError(); //
     * errors != 0 evaluation
     */

    clearChanged();
  }

  /**
   * Creates the start entry.
   *
   * @return the job entry copy
   */
  public static final JobEntryCopy createStartEntry() {
    JobEntrySpecial jobEntrySpecial = new JobEntrySpecial( STRING_SPECIAL_START, true, false );
    JobEntryCopy jobEntry = new JobEntryCopy();
    jobEntry.setObjectId( null );
    jobEntry.setEntry( jobEntrySpecial );
    jobEntry.setLocation( 50, 50 );
    jobEntry.setDrawn( false );
    jobEntry.setDescription( BaseMessages.getString( PKG, "JobMeta.StartJobEntry.Description" ) );
    return jobEntry;

  }

  /**
   * Creates the dummy entry.
   *
   * @return the job entry copy
   */
  public static final JobEntryCopy createDummyEntry() {
    JobEntrySpecial jobEntrySpecial = new JobEntrySpecial( STRING_SPECIAL_DUMMY, false, true );
    JobEntryCopy jobEntry = new JobEntryCopy();
    jobEntry.setObjectId( null );
    jobEntry.setEntry( jobEntrySpecial );
    jobEntry.setLocation( 50, 50 );
    jobEntry.setDrawn( false );
    jobEntry.setDescription( BaseMessages.getString( PKG, "JobMeta.DummyJobEntry.Description" ) );
    return jobEntry;
  }

  /**
   * Gets the start.
   *
   * @return the start
   */
  public JobEntryCopy getStart() {
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy cge = getJobEntry( i );
      if ( cge.isStart() ) {
        return cge;
      }
    }
    return null;
  }

  /**
   * Gets the dummy.
   *
   * @return the dummy
   */
  public JobEntryCopy getDummy() {
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy cge = getJobEntry( i );
      if ( cge.isDummy() ) {
        return cge;
      }
    }
    return null;
  }

  /**
   * Compares two transformation on name, filename
   */
  public int compare( JobMeta j1, JobMeta j2 ) {
    if ( Const.isEmpty( j1.getName() ) && !Const.isEmpty( j2.getName() ) ) {
      return -1;
    }
    if ( !Const.isEmpty( j1.getName() ) && Const.isEmpty( j2.getName() ) ) {
      return 1;
    }
    if ( Const.isEmpty( j1.getName() ) && Const.isEmpty( j2.getName() ) || j1.getName().equals( j2.getName() ) ) {
      if ( Const.isEmpty( j1.getFilename() ) && !Const.isEmpty( j2.getFilename() ) ) {
        return -1;
      }
      if ( !Const.isEmpty( j1.getFilename() ) && Const.isEmpty( j2.getFilename() ) ) {
        return 1;
      }
      if ( Const.isEmpty( j1.getFilename() ) && Const.isEmpty( j2.getFilename() ) ) {
        return 0;
      }
      return j1.getFilename().compareTo( j2.getFilename() );
    }

    // Compare by name : repositories etc.
    //
    if ( j1.getObjectRevision() != null && j2.getObjectRevision() == null ) {
      return 1;
    }
    if ( j1.getObjectRevision() == null && j2.getObjectRevision() != null ) {
      return -1;
    }
    int cmp;
    if ( j1.getObjectRevision() == null && j2.getObjectRevision() == null ) {
      cmp = 0;
    } else {
      cmp = j1.getObjectRevision().getName().compareTo( j2.getObjectRevision().getName() );
    }
    if ( cmp == 0 ) {
      return j1.getName().compareTo( j2.getName() );
    } else {
      return cmp;
    }
  }

  /**
   * Compares this job's meta-data to the specified job's meta-data. This method simply calls compare(this, o)
   *
   * @param o
   *          the o
   * @return the int
   * @see #compare(JobMeta, JobMeta)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo( JobMeta o ) {
    return compare( this, o );
  }

  /**
   * Checks whether this job's meta-data object is equal to the specified object. If the specified object is not an
   * instance of JobMeta, false is returned. Otherwise the method returns whether a call to compare() indicates equality
   * (i.e. compare(this, (JobMeta)obj)==0).
   *
   * @param obj
   *          the obj
   * @return true, if successful
   * @see #compare(JobMeta, JobMeta)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals( Object obj ) {
    if ( !( obj instanceof JobMeta ) ) {
      return false;
    }

    return compare( this, (JobMeta) obj ) == 0;
  }

  /**
   * Clones the job meta-data object.
   *
   * @return a clone of the job meta-data object
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    return realClone( true );
  }

  /**
   * Perform a real clone of the job meta-data object, including cloning all lists and copying all values. If the
   * doClear parameter is true, the clone will be cleared of ALL values before the copy. If false, only the copied
   * fields will be cleared.
   *
   * @param doClear
   *          Whether to clear all of the clone's data before copying from the source object
   * @return a real clone of the calling object
   */
  public Object realClone( boolean doClear ) {
    try {
      JobMeta jobMeta = (JobMeta) super.clone();
      if ( doClear ) {
        jobMeta.clear();
      } else {
        jobMeta.jobcopies = new ArrayList<JobEntryCopy>();
        jobMeta.jobhops = new ArrayList<JobHopMeta>();
        jobMeta.notes = new ArrayList<NotePadMeta>();
        jobMeta.databases = new ArrayList<DatabaseMeta>();
        jobMeta.slaveServers = new ArrayList<SlaveServer>();
        jobMeta.namedParams = new NamedParamsDefault();
      }

      for ( JobEntryCopy entry : jobcopies ) {
        jobMeta.jobcopies.add( (JobEntryCopy) entry.clone_deep() );
      }
      for ( JobHopMeta entry : jobhops ) {
        jobMeta.jobhops.add( (JobHopMeta) entry.clone() );
      }
      for ( NotePadMeta entry : notes ) {
        jobMeta.notes.add( (NotePadMeta) entry.clone() );
      }
      for ( DatabaseMeta entry : databases ) {
        jobMeta.databases.add( (DatabaseMeta) entry.clone() );
      }
      for ( SlaveServer slave : slaveServers ) {
        jobMeta.getSlaveServers().add( (SlaveServer) slave.clone() );
      }
      for ( String key : listParameters() ) {
        jobMeta.addParameterDefinition( key, getParameterDefault( key ), getParameterDescription( key ) );
      }
      return jobMeta;
    } catch ( Exception e ) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.EngineMetaInterface#getName()
   */
  public String getName() {
    return name;
  }

  /**
   * Set the name of the job.
   *
   * @param newName
   *          The new name of the job
   */
  public void setName( String newName ) {
    fireNameChangedListeners( this.name, newName );
    this.name = newName;
    setInternalNameKettleVariable( variables );
  }

  /**
   * Builds a name - if no name is set, yet - from the filename
   */
  public void nameFromFilename() {
    if ( !Const.isEmpty( filename ) ) {
      setName( Const.createName( filename ) );
    }
  }

  /**
   * Gets the directory.
   *
   * @return Returns the directory.
   */
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return directory;
  }

  /**
   * Sets the directory.
   *
   * @param directory
   *          The directory to set.
   */
  public void setRepositoryDirectory( RepositoryDirectoryInterface directory ) {
    this.directory = directory;
    setInternalKettleVariables();
  }

  /**
   * Gets the filename.
   *
   * @return filename
   * @see org.pentaho.di.core.EngineMetaInterface#getFilename()
   */
  public String getFilename() {
    return filename;
  }

  /**
   * Set the filename of the job
   *
   * @param newFilename
   *          The new filename of the job
   */
  public void setFilename( String newFilename ) {
    fireFilenameChangedListeners( this.filename, newFilename );
    this.filename = newFilename;
    setInternalFilenameKettleVariables( variables );
  }

  /**
   * Gets the job log table.
   *
   * @return the job log table
   */
  public JobLogTable getJobLogTable() {
    return jobLogTable;
  }

  /**
   * Sets the job log table.
   *
   * @param jobLogTable
   *          the new job log table
   */
  public void setJobLogTable( JobLogTable jobLogTable ) {
    this.jobLogTable = jobLogTable;
  }

  /**
   * Returns a list of the databases.
   *
   * @return Returns the databases.
   */
  public List<DatabaseMeta> getDatabases() {
    return databases;
  }

  /**
   * Gets the database names.
   *
   * @return the database names
   */
  public String[] getDatabaseNames() {
    String[] names = new String[databases.size()];
    for ( int i = 0; i < names.length; i++ ) {
      names[i] = databases.get( i ).getName();
    }
    return names;
  }

  /**
   * Sets the databases.
   *
   * @param databases
   *          The databases to set.
   */
  public void setDatabases( List<DatabaseMeta> databases ) {
    Collections.sort( databases, DatabaseMeta.comparator );
    this.databases = databases;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.changed.ChangedFlag#setChanged(boolean)
   */
  public void setChanged( boolean ch ) {
    if ( ch ) {
      setChanged();
    } else {
      clearChanged();
    }
    fireContentChangedListeners( ch );
  }

  /**
   * Clears the different changed flags of the job.
   *
   */
  public void clearChanged() {
    changedEntries = false;
    changedHops = false;
    changedNotes = false;
    changedDatabases = false;

    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy entry = getJobEntry( i );
      entry.setChanged( false );
    }
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops
      hi.setChanged( false );
    }
    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta db = getDatabase( i );
      db.setChanged( false );
    }
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta note = getNote( i );
      note.setChanged( false );
    }
    super.clearChanged();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.changed.ChangedFlag#hasChanged()
   */
  public boolean hasChanged() {
    if ( super.hasChanged() ) {
      return true;
    }

    if ( haveJobEntriesChanged() ) {
      return true;
    }
    if ( haveJobHopsChanged() ) {
      return true;
    }
    if ( haveConnectionsChanged() ) {
      return true;
    }
    if ( haveNotesChanged() ) {
      return true;
    }

    return false;
  }

  private Set<DatabaseMeta> getUsedDatabaseMetas() {
    Set<DatabaseMeta> databaseMetas = new HashSet<DatabaseMeta>();
    for ( JobEntryCopy jobEntryCopy : getJobCopies() ) {
      DatabaseMeta[] dbs = jobEntryCopy.getEntry().getUsedDatabaseConnections();
      if ( dbs != null ) {
        for ( DatabaseMeta db : dbs ) {
          databaseMetas.add( db );
        }
      }
    }

    databaseMetas.add( jobLogTable.getDatabaseMeta() );

    for ( LogTableInterface logTable : getExtraLogTables() ) {
      databaseMetas.add( logTable.getDatabaseMeta() );
    }
    return databaseMetas;
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
    return getUsedDatabaseMetas().contains( databaseMeta );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.EngineMetaInterface#getFileType()
   */
  public String getFileType() {
    return LastUsedFile.FILE_TYPE_JOB;
  }

  /**
   * Gets the job filter names.
   *
   * @return the filter names
   * @see org.pentaho.di.core.EngineMetaInterface#getFilterNames()
   */
  public String[] getFilterNames() {
    return Const.getJobFilterNames();
  }

  /**
   * Gets the job filter extensions. For JobMeta, this method returns the value of {@link Const.STRING_JOB_FILTER_EXT}
   *
   * @return the filter extensions
   * @see org.pentaho.di.core.EngineMetaInterface#getFilterExtensions()
   */
  public String[] getFilterExtensions() {
    return Const.STRING_JOB_FILTER_EXT;
  }

  /**
   * Gets the default extension for a job. For JobMeta, this method returns the value of
   * {@link Const#STRING_JOB_DEFAULT_EXT}
   *
   * @return the default extension
   * @see org.pentaho.di.core.EngineMetaInterface#getDefaultExtension()
   */
  public String getDefaultExtension() {
    return Const.STRING_JOB_DEFAULT_EXT;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.xml.XMLInterface#getXML()
   */
  public String getXML() {
    Props props = null;
    if ( Props.isInitialized() ) {
      props = Props.getInstance();
    }

    StringBuffer retval = new StringBuffer( 500 );

    retval.append( "<" ).append( XML_TAG ).append( ">" ).append( Const.CR );

    retval.append( "  " ).append( XMLHandler.addTagValue( "name", getName() ) );

    retval.append( "    " ).append( XMLHandler.addTagValue( "description", description ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "extended_description", extendedDescription ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "job_version", jobVersion ) );
    if ( jobStatus >= 0 ) {
      retval.append( "    " ).append( XMLHandler.addTagValue( "job_status", jobStatus ) );
    }

    retval.append( "  " ).append( XMLHandler.addTagValue( "directory", ( directory != null ? directory.getPath()
      : RepositoryDirectory.DIRECTORY_SEPARATOR ) ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "created_user", created_user ) );
    retval
      .append( "  " ).append( XMLHandler.addTagValue( "created_date", XMLHandler.date2string( created_date ) ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "modified_user", modifiedUser ) );
    retval
      .append( "  " ).append( XMLHandler.addTagValue( "modified_date", XMLHandler.date2string( modifiedDate ) ) );

    retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_PARAMETERS ) ).append( Const.CR );
    String[] parameters = listParameters();
    for ( int idx = 0; idx < parameters.length; idx++ ) {
      retval.append( "        " ).append( XMLHandler.openTag( "parameter" ) ).append( Const.CR );
      retval.append( "            " ).append( XMLHandler.addTagValue( "name", parameters[idx] ) );
      try {
        retval.append( "            " ).append(
          XMLHandler.addTagValue( "default_value", getParameterDefault( parameters[idx] ) ) );
        retval.append( "            " ).append(
          XMLHandler.addTagValue( "description", getParameterDescription( parameters[idx] ) ) );
      } catch ( UnknownParamException e ) {
        // skip the default value and/or description. This exception should never happen because we use listParameters()
        // above.
      }
      retval.append( "        " ).append( XMLHandler.closeTag( "parameter" ) ).append( Const.CR );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_PARAMETERS ) ).append( Const.CR );

    Set<DatabaseMeta> usedDatabaseMetas = getUsedDatabaseMetas();
    // Save the database connections...
    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta dbMeta = getDatabase( i );
      if ( props != null && props.areOnlyUsedConnectionsSavedToXML() ) {
        if ( usedDatabaseMetas.contains( dbMeta ) ) {
          retval.append( dbMeta.getXML() );
        }
      } else {
        retval.append( dbMeta.getXML() );
      }
    }

    // The slave servers...
    //
    retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_SLAVESERVERS ) ).append( Const.CR );
    for ( int i = 0; i < slaveServers.size(); i++ ) {
      SlaveServer slaveServer = slaveServers.get( i );
      retval.append( "         " ).append( slaveServer.getXML() ).append( Const.CR );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_SLAVESERVERS ) ).append( Const.CR );

    // Append the job logging information...
    //
    for ( LogTableInterface logTable : getLogTables() ) {
      retval.append( logTable.getXML() );
    }

    retval.append( "   " ).append( XMLHandler.addTagValue( "pass_batchid", batchIdPassed ) );
    retval.append( "   " ).append( XMLHandler.addTagValue( "shared_objects_file", sharedObjectsFile ) );

    retval.append( "  <entries>" ).append( Const.CR );
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy jge = getJobEntry( i );
      jge.getEntry().setRepository( repository );
      retval.append( jge.getXML() );
    }
    retval.append( "  </entries>" ).append( Const.CR );

    retval.append( "  <hops>" ).append( Const.CR );
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops
      retval.append( hi.getXML() );
    }
    retval.append( "  </hops>" ).append( Const.CR );

    retval.append( "  <notepads>" ).append( Const.CR );
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      retval.append( ni.getXML() );
    }
    retval.append( "  </notepads>" ).append( Const.CR );

    // Also store the attribute groups
    //
    retval.append( AttributesUtil.getAttributesXml( attributesMap ) ).append( Const.CR );

    retval.append( "</" ).append( XML_TAG ).append( ">" ).append( Const.CR );

    return retval.toString();
  }

  /**
   * Instantiates a new job meta.
   *
   * @param fname
   *          the fname
   * @param rep
   *          the rep
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public JobMeta( String fname, Repository rep ) throws KettleXMLException {
    this( null, fname, rep, null );
  }

  /**
   * Instantiates a new job meta.
   *
   * @param fname
   *          the fname
   * @param rep
   *          the rep
   * @param prompter
   *          the prompter
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public JobMeta( String fname, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    this( null, fname, rep, prompter );
  }

  /**
   * Load the job from the XML file specified.
   *
   * @param log
   *          the logging channel
   * @param fname
   *          The filename to load as a job
   * @param rep
   *          The repository to bind againt, null if there is no repository available.
   * @throws KettleXMLException
   */
  @Deprecated
  public JobMeta( VariableSpace parentSpace, String fname, Repository rep, OverwritePrompter prompter )
    throws KettleXMLException {
    this( parentSpace, fname, rep, null, prompter );
  }

  /**
   * Load the job from the XML file specified.
   *
   * @param log
   *          the logging channel
   * @param fname
   *          The filename to load as a job
   * @param rep
   *          The repository to bind againt, null if there is no repository available.
   * @throws KettleXMLException
   */
  public JobMeta( VariableSpace parentSpace, String fname, Repository rep, IMetaStore metaStore,
    OverwritePrompter prompter ) throws KettleXMLException {
    this.initializeVariablesFrom( parentSpace );
    this.metaStore = metaStore;
    try {
      // OK, try to load using the VFS stuff...
      Document doc = XMLHandler.loadXMLFile( KettleVFS.getFileObject( fname, this ) );
      if ( doc != null ) {
        // The jobnode
        Node jobnode = XMLHandler.getSubNode( doc, XML_TAG );

        loadXML( jobnode, fname, rep, prompter );
      } else {
        throw new KettleXMLException( BaseMessages.getString( PKG, "JobMeta.Exception.ErrorReadingFromXMLFile" )
          + fname );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobMeta.Exception.UnableToLoadJobFromXMLFile" )
        + fname + "]", e );
    }
  }

  /**
   * Instantiates a new job meta.
   *
   * @param inputStream
   *          the input stream
   * @param rep
   *          the rep
   * @param prompter
   *          the prompter
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public JobMeta( InputStream inputStream, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    Document doc = XMLHandler.loadXMLFile( inputStream, null, false, false );
    loadXML( XMLHandler.getSubNode( doc, JobMeta.XML_TAG ), rep, prompter );
  }

  /**
   * Create a new JobMeta object by loading it from a a DOM node.
   *
   * @param jobnode
   *          The node to load from
   * @param rep
   *          The reference to a repository to load additional information from
   * @param prompter
   *          The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public JobMeta( Node jobnode, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    loadXML( jobnode, rep, false, prompter );
  }

  /**
   * Create a new JobMeta object by loading it from a a DOM node.
   *
   * @param jobnode
   *          The node to load from
   * @param rep
   *          The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects
   *          Do not load shared objects, handled separately
   * @param prompter
   *          The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public JobMeta( Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects, OverwritePrompter prompter )
    throws KettleXMLException {
    loadXML( jobnode, rep, ignoreRepositorySharedObjects, prompter );
  }

  /**
   * Checks if is rep reference.
   *
   * @return true, if is rep reference
   */
  public boolean isRepReference() {
    return isRepReference( getFilename(), this.getName() );
  }

  /**
   * Checks if is file reference.
   *
   * @return true, if is file reference
   */
  public boolean isFileReference() {
    return !isRepReference( getFilename(), this.getName() );
  }

  /**
   * Checks if is rep reference.
   *
   * @param fileName
   *          the file name
   * @param transName
   *          the trans name
   * @return true, if is rep reference
   */
  public static boolean isRepReference( String fileName, String transName ) {
    return Const.isEmpty( fileName ) && !Const.isEmpty( transName );
  }

  /**
   * Checks if is file reference.
   *
   * @param fileName
   *          the file name
   * @param transName
   *          the trans name
   * @return true, if is file reference
   */
  public static boolean isFileReference( String fileName, String transName ) {
    return !isRepReference( fileName, transName );
  }

  /**
   * Load xml.
   *
   * @param jobnode
   *          the jobnode
   * @param rep
   *          the rep
   * @param prompter
   *          the prompter
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public void loadXML( Node jobnode, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    loadXML( jobnode, rep, false, prompter );
  }

  /**
   * Load xml.
   *
   * @param jobnode
   *          the jobnode
   * @param fname
   *          The filename
   * @param rep
   *          the rep
   * @param prompter
   *          the prompter
   * @throws KettleXMLException
   *           the kettle xml exception
   */
  public void loadXML( Node jobnode, String fname, Repository rep, OverwritePrompter prompter )
    throws KettleXMLException {
    loadXML( jobnode, fname, rep, false, prompter );
  }

  /**
   * Load a block of XML from an DOM node.
   *
   * @param jobnode
   *          The node to load from
   * @param rep
   *          The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects
   *          Do not load shared objects, handled separately
   * @param prompter
   *          The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public void loadXML( Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects,
    OverwritePrompter prompter ) throws KettleXMLException {
    loadXML( jobnode, null, rep, ignoreRepositorySharedObjects, prompter );
  }

  /**
   * Load a block of XML from an DOM node.
   *
   * @param jobnode
   *          The node to load from
   * @param fname
   *          The filename
   * @param rep
   *          The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects
   *          Do not load shared objects, handled separately
   * @param prompter
   *          The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   * @deprecated
   */
  @Deprecated
  public void loadXML( Node jobnode, String fname, Repository rep, boolean ignoreRepositorySharedObjects,
    OverwritePrompter prompter ) throws KettleXMLException {
    loadXML( jobnode, fname, rep, null, ignoreRepositorySharedObjects, prompter );
  }

  /**
   * Load a block of XML from an DOM node.
   *
   * @param jobnode
   *          The node to load from
   * @param fname
   *          The filename
   * @param rep
   *          The reference to a repository to load additional information from
   * @param metaStore
   *          the MetaStore to use
   * @param ignoreRepositorySharedObjects
   *          Do not load shared objects, handled separately
   * @param prompter
   *          The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public void loadXML( Node jobnode, String fname, Repository rep, IMetaStore metaStore,
    boolean ignoreRepositorySharedObjects, OverwritePrompter prompter ) throws KettleXMLException {
    Props props = null;
    if ( Props.isInitialized() ) {
      props = Props.getInstance();
    }

    try {
      // clear the jobs;
      clear();

      // If we are not using a repository, we are getting the job from a file
      // Set the filename here so it can be used in variables for ALL aspects of the job FIX: PDI-8890
      if ( null == rep ) {
        setFilename( fname );
      }

      //
      // get job info:
      //
      setName( XMLHandler.getTagValue( jobnode, "name" ) );

      // Optionally load the repository directory...
      //
      if ( rep != null ) {
        String directoryPath = XMLHandler.getTagValue( jobnode, "directory" );
        if ( directoryPath != null ) {
          directory = rep.findDirectory( directoryPath );
          if ( directory == null ) { // not found
            directory = new RepositoryDirectory(); // The root as default
          }
        }
      }

      // description
      description = XMLHandler.getTagValue( jobnode, "description" );

      // extended description
      extendedDescription = XMLHandler.getTagValue( jobnode, "extended_description" );

      // job version
      jobVersion = XMLHandler.getTagValue( jobnode, "job_version" );

      // job status
      jobStatus = Const.toInt( XMLHandler.getTagValue( jobnode, "job_status" ), -1 );

      // Created user/date
      created_user = XMLHandler.getTagValue( jobnode, "created_user" );
      String createDate = XMLHandler.getTagValue( jobnode, "created_date" );

      if ( createDate != null ) {
        created_date = XMLHandler.stringToDate( createDate );
      }

      // Changed user/date
      modifiedUser = XMLHandler.getTagValue( jobnode, "modified_user" );
      String modDate = XMLHandler.getTagValue( jobnode, "modified_date" );
      if ( modDate != null ) {
        modifiedDate = XMLHandler.stringToDate( modDate );
      }

      // Load the default list of databases
      // Read objects from the shared XML file & the repository
      try {
        sharedObjectsFile = XMLHandler.getTagValue( jobnode, "shared_objects_file" );
        if ( rep == null || ignoreRepositorySharedObjects ) {
          sharedObjects = readSharedObjects();
        } else {
          sharedObjects = rep.readJobMetaSharedObjects( this );
        }
      } catch ( Exception e ) {
        throw new KettleXMLException(
          BaseMessages.getString( PKG, "JobMeta.ErrorReadingSharedObjects.Message" ), e );
        // //
      }

      // Read the named parameters.
      Node paramsNode = XMLHandler.getSubNode( jobnode, XML_TAG_PARAMETERS );
      int nrParams = XMLHandler.countNodes( paramsNode, "parameter" );

      for ( int i = 0; i < nrParams; i++ ) {
        Node paramNode = XMLHandler.getSubNodeByNr( paramsNode, "parameter", i );

        String paramName = XMLHandler.getTagValue( paramNode, "name" );
        String defValue = XMLHandler.getTagValue( paramNode, "default_value" );
        String descr = XMLHandler.getTagValue( paramNode, "description" );

        addParameterDefinition( paramName, defValue, descr );
      }

      //
      // Read the database connections
      //
      int nr = XMLHandler.countNodes( jobnode, "connection" );
      for ( int i = 0; i < nr; i++ ) {
        Node dbnode = XMLHandler.getSubNodeByNr( jobnode, "connection", i );
        DatabaseMeta dbcon = new DatabaseMeta( dbnode );
        dbcon.shareVariablesWith( this );

        DatabaseMeta exist = findDatabase( dbcon.getName() );
        if ( exist == null ) {
          addDatabase( dbcon );
        } else {
          if ( !exist.isShared() ) {
            // skip shared connections

            boolean askOverwrite = Props.isInitialized() ? props.askAboutReplacingDatabaseConnections() : false;
            boolean overwrite = Props.isInitialized() ? props.replaceExistingDatabaseConnections() : true;
            if ( askOverwrite && prompter != null ) {
              overwrite =
                prompter.overwritePrompt(
                  BaseMessages.getString( PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.Message", dbcon
                    .getName() ), BaseMessages.getString(
                    PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ),
                  Props.STRING_ASK_ABOUT_REPLACING_DATABASES );
            }

            if ( overwrite ) {
              int idx = indexOfDatabase( exist );
              removeDatabase( idx );
              addDatabase( idx, dbcon );
            }
          }
        }
      }

      // Read the slave servers...
      //
      Node slaveServersNode = XMLHandler.getSubNode( jobnode, XML_TAG_SLAVESERVERS );
      int nrSlaveServers = XMLHandler.countNodes( slaveServersNode, SlaveServer.XML_TAG );
      for ( int i = 0; i < nrSlaveServers; i++ ) {
        Node slaveServerNode = XMLHandler.getSubNodeByNr( slaveServersNode, SlaveServer.XML_TAG, i );
        SlaveServer slaveServer = new SlaveServer( slaveServerNode );
        slaveServer.shareVariablesWith( this );

        // Check if the object exists and if it's a shared object.
        // If so, then we will keep the shared version, not this one.
        // The stored XML is only for backup purposes.
        SlaveServer check = findSlaveServer( slaveServer.getName() );
        if ( check != null ) {
          if ( !check.isShared() ) {
            // we don't overwrite shared objects.
            addOrReplaceSlaveServer( slaveServer );
          }
        } else {
          slaveServers.add( slaveServer );
        }
      }

      /*
       * Get the log database connection & log table
       */
      // Backward compatibility...
      //
      Node jobLogNode = XMLHandler.getSubNode( jobnode, JobLogTable.XML_TAG );
      if ( jobLogNode == null ) {
        // Load the XML
        //
        jobLogTable.setConnectionName( XMLHandler.getTagValue( jobnode, "logconnection" ) );
        jobLogTable.setTableName( XMLHandler.getTagValue( jobnode, "logtable" ) );
        jobLogTable.setBatchIdUsed( "Y".equalsIgnoreCase( XMLHandler.getTagValue( jobnode, "use_batchid" ) ) );
        jobLogTable.setLogFieldUsed( "Y".equalsIgnoreCase( XMLHandler.getTagValue( jobnode, "use_logfield" ) ) );
        jobLogTable.findField( JobLogTable.ID.CHANNEL_ID ).setEnabled( false );
        jobLogTable.findField( JobLogTable.ID.LINES_REJECTED ).setEnabled( false );
      } else {
        jobLogTable.loadXML( jobLogNode, databases, null );
      }

      Node channelLogTableNode = XMLHandler.getSubNode( jobnode, ChannelLogTable.XML_TAG );
      if ( channelLogTableNode != null ) {
        channelLogTable.loadXML( channelLogTableNode, databases, null );
      }
      jobEntryLogTable.loadXML( jobnode, databases, null );

      for ( LogTableInterface extraLogTable : extraLogTables ) {
        extraLogTable.loadXML( jobnode, databases, null );
      }

      batchIdPassed = "Y".equalsIgnoreCase( XMLHandler.getTagValue( jobnode, "pass_batchid" ) );

      /*
       * read the job entries...
       */
      Node entriesnode = XMLHandler.getSubNode( jobnode, "entries" );
      int tr = XMLHandler.countNodes( entriesnode, "entry" );
      for ( int i = 0; i < tr; i++ ) {
        Node entrynode = XMLHandler.getSubNodeByNr( entriesnode, "entry", i );
        // System.out.println("Reading entry:\n"+entrynode);

        JobEntryCopy je = new JobEntryCopy( entrynode, databases, slaveServers, rep, metaStore );
        JobEntryCopy prev = findJobEntry( je.getName(), 0, true );
        if ( prev != null ) {
          // See if the #0 (root entry) already exists!
          //
          if ( je.getNr() == 0 ) {

            // Replace previous version with this one: remove it first
            //
            int idx = indexOfJobEntry( prev );
            removeJobEntry( idx );

          } else if ( je.getNr() > 0 ) {

            // Use previously defined JobEntry info!
            //
            je.setEntry( prev.getEntry() );

            // See if entry already exists...
            prev = findJobEntry( je.getName(), je.getNr(), true );
            if ( prev != null ) {
              // remove the old one!
              //
              int idx = indexOfJobEntry( prev );
              removeJobEntry( idx );
            }
          }
        }
        // Add the JobEntryCopy...
        addJobEntry( je );
      }

      Node hopsnode = XMLHandler.getSubNode( jobnode, "hops" );
      int ho = XMLHandler.countNodes( hopsnode, "hop" );
      for ( int i = 0; i < ho; i++ ) {
        Node hopnode = XMLHandler.getSubNodeByNr( hopsnode, "hop", i );
        JobHopMeta hi = new JobHopMeta( hopnode, this );
        jobhops.add( hi );
      }

      // Read the notes...
      Node notepadsnode = XMLHandler.getSubNode( jobnode, "notepads" );
      int nrnotes = XMLHandler.countNodes( notepadsnode, "notepad" );
      for ( int i = 0; i < nrnotes; i++ ) {
        Node notepadnode = XMLHandler.getSubNodeByNr( notepadsnode, "notepad", i );
        NotePadMeta ni = new NotePadMeta( notepadnode );
        notes.add( ni );
      }

      // Load the attribute groups map
      //
      attributesMap = AttributesUtil.loadAttributes( XMLHandler.getSubNode( jobnode, AttributesUtil.XML_TAG ) );

      ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.JobMetaLoaded.id, this );

      clearChanged();
    } catch ( Exception e ) {
      throw new KettleXMLException(
        BaseMessages.getString( PKG, "JobMeta.Exception.UnableToLoadJobFromXMLNode" ), e );
    } finally {
      setInternalKettleVariables();
    }
  }

  /**
   * Read shared objects.
   *
   * @return the shared objects
   * @throws KettleException
   *           the kettle exception
   */
  public SharedObjects readSharedObjects() throws KettleException {
    // Extract the shared steps, connections, etc. using the SharedObjects
    // class
    //
    String soFile = environmentSubstitute( sharedObjectsFile );
    SharedObjects sharedObjects = new SharedObjects( soFile );
    Map<?, SharedObjectInterface> objectsMap = sharedObjects.getObjectsMap();

    // First read the databases...
    // We read databases & slaves first because there might be dependencies
    // that need to be resolved.
    //
    for ( SharedObjectInterface object : objectsMap.values() ) {
      if ( object instanceof DatabaseMeta ) {
        DatabaseMeta databaseMeta = (DatabaseMeta) object;
        databaseMeta.shareVariablesWith( this );
        addOrReplaceDatabase( databaseMeta );
      } else if ( object instanceof SlaveServer ) {
        SlaveServer slaveServer = (SlaveServer) object;
        slaveServer.shareVariablesWith( this );
        addOrReplaceSlaveServer( slaveServer );
      }
    }

    return sharedObjects;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.EngineMetaInterface#saveSharedObjects()
   */
  public void saveSharedObjects() throws KettleException {
    try {
      // First load all the shared objects...
      String soFile = environmentSubstitute( sharedObjectsFile );
      SharedObjects sharedObjects = new SharedObjects( soFile );

      // Now overwrite the objects in there
      List<Object> shared = new ArrayList<Object>();
      shared.addAll( databases );
      shared.addAll( slaveServers );

      // The databases connections...
      for ( int i = 0; i < shared.size(); i++ ) {
        SharedObjectInterface sharedObject = (SharedObjectInterface) shared.get( i );
        if ( sharedObject.isShared() ) {
          sharedObjects.storeObject( sharedObject );
        }
      }

      // Save the objects
      sharedObjects.saveToFile();
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save shared ojects", e );
    }
  }

  /**
   * Find a database connection by it's name
   *
   * @param name
   *          The database name to look for
   * @return The database connection or null if nothing was found.
   */
  public DatabaseMeta findDatabase( String name ) {
    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta ci = getDatabase( i );
      if (( ci != null ) && ( ci.getName().equalsIgnoreCase( name ) ) ||
              ( ci.getDisplayName().equalsIgnoreCase( name ) )) {
        return ci;
      }
    }
    return null;
  }

  /**
   * Gets the job entry copy.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @param iconsize
   *          the iconsize
   * @return the job entry copy
   */
  public JobEntryCopy getJobEntryCopy( int x, int y, int iconsize ) {
    int i, s;
    s = nrJobEntries();
    for ( i = s - 1; i >= 0; i-- ) {
      // Back to front because drawing goes from start to end

      JobEntryCopy je = getJobEntry( i );
      Point p = je.getLocation();
      if ( p != null ) {
        if ( x >= p.x && x <= p.x + iconsize && y >= p.y && y <= p.y + iconsize ) {
          return je;
        }
      }
    }
    return null;
  }

  /**
   * Nr job entries.
   *
   * @return the int
   */
  public int nrJobEntries() {
    return jobcopies.size();
  }

  /**
   * Nr job hops.
   *
   * @return the int
   */
  public int nrJobHops() {
    return jobhops.size();
  }

  /**
   * Nr notes.
   *
   * @return the int
   */
  public int nrNotes() {
    return notes.size();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#nrDatabases()
   */
  public int nrDatabases() {
    return databases.size();
  }

  /**
   * Gets the job hop.
   *
   * @param i
   *          the i
   * @return the job hop
   */
  public JobHopMeta getJobHop( int i ) {
    return jobhops.get( i );
  }

  /**
   * Gets the job entry.
   *
   * @param i
   *          the i
   * @return the job entry
   */
  public JobEntryCopy getJobEntry( int i ) {
    return jobcopies.get( i );
  }

  /**
   * Gets the note.
   *
   * @param i
   *          the i
   * @return the note
   */
  public NotePadMeta getNote( int i ) {
    return notes.get( i );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#getDatabase(int)
   */
  public DatabaseMeta getDatabase( int i ) {
    return databases.get( i );
  }

  /**
   * Adds the job entry.
   *
   * @param je
   *          the je
   */
  public void addJobEntry( JobEntryCopy je ) {
    jobcopies.add( je );
    je.setParentJobMeta( this );
    setChanged();
  }

  /**
   * Adds the job hop.
   *
   * @param hi
   *          the hi
   */
  public void addJobHop( JobHopMeta hi ) {
    jobhops.add( hi );
    setChanged();
  }

  /**
   * Adds the note.
   *
   * @param ni
   *          the ni
   */
  public void addNote( NotePadMeta ni ) {
    notes.add( ni );
    setChanged();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  public void addDatabase( DatabaseMeta ci ) {
    databases.add( ci );
    Collections.sort( databases, DatabaseMeta.comparator );
    changedDatabases = true;
  }

  /**
   * Adds the job entry.
   *
   * @param p
   *          the p
   * @param si
   *          the si
   */
  public void addJobEntry( int p, JobEntryCopy si ) {
    jobcopies.add( p, si );
    changedEntries = true;
  }

  /**
   * Adds the job hop.
   *
   * @param p
   *          the p
   * @param hi
   *          the hi
   */
  public void addJobHop( int p, JobHopMeta hi ) {
    jobhops.add( p, hi );
    changedHops = true;
  }

  /**
   * Adds the note.
   *
   * @param p
   *          the p
   * @param ni
   *          the ni
   */
  public void addNote( int p, NotePadMeta ni ) {
    notes.add( p, ni );
    changedNotes = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(int, org.pentaho.di.core.database.DatabaseMeta)
   */
  public void addDatabase( int p, DatabaseMeta ci ) {
    databases.add( p, ci );
    changedDatabases = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabaseInterface#addOrReplaceDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  public void addOrReplaceDatabase( DatabaseMeta databaseMeta ) {
    int index = databases.indexOf( databaseMeta );
    if ( index < 0 ) {
      addDatabase( databaseMeta );
    } else {
      DatabaseMeta previous = getDatabase( index );
      previous.replaceMeta( databaseMeta );
    }
    changedDatabases = true;
  }

  /**
   * Add a new slave server to the transformation if that didn't exist yet. Otherwise, replace it.
   *
   * @param slaveServer
   *          The slave server to be added.
   */
  public void addOrReplaceSlaveServer( SlaveServer slaveServer ) {
    int index = slaveServers.indexOf( slaveServer );
    if ( index < 0 ) {
      slaveServers.add( slaveServer );
    } else {
      SlaveServer previous = slaveServers.get( index );
      previous.replaceMeta( slaveServer );
    }
    setChanged();
  }

  /**
   * Removes the job entry.
   *
   * @param i
   *          the i
   */
  public void removeJobEntry( int i ) {
    jobcopies.remove( i );
    setChanged();
  }

  /**
   * Removes the job hop.
   *
   * @param i
   *          the i
   */
  public void removeJobHop( int i ) {
    jobhops.remove( i );
    setChanged();
  }

  /**
   * Removes the note.
   *
   * @param i
   *          the i
   */
  public void removeNote( int i ) {
    notes.remove( i );
    setChanged();
  }

  /**
   * Raise note.
   *
   * @param p
   *          the p
   */
  public void raiseNote( int p ) {
    // if valid index and not last index
    if ( ( p >= 0 ) && ( p < notes.size() - 1 ) ) {
      NotePadMeta note = notes.remove( p );
      notes.add( note );
      changedNotes = true;
    }
  }

  /**
   * Lower note.
   *
   * @param p
   *          the p
   */
  public void lowerNote( int p ) {
    // if valid index and not first index
    if ( ( p > 0 ) && ( p < notes.size() ) ) {
      NotePadMeta note = notes.remove( p );
      notes.add( 0, note );
      changedNotes = true;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#removeDatabase(int)
   */
  public void removeDatabase( int i ) {
    if ( i < 0 || i >= databases.size() ) {
      return;
    }
    databases.remove( i );
    changedDatabases = true;
  }

  /**
   * Index of job hop.
   *
   * @param he
   *          the he
   * @return the int
   */
  public int indexOfJobHop( JobHopMeta he ) {
    return jobhops.indexOf( he );
  }

  /**
   * Index of note.
   *
   * @param ni
   *          the ni
   * @return the int
   */
  public int indexOfNote( NotePadMeta ni ) {
    return notes.indexOf( ni );
  }

  /**
   * Index of job entry.
   *
   * @param ge
   *          the ge
   * @return the int
   */
  public int indexOfJobEntry( JobEntryCopy ge ) {
    return jobcopies.indexOf( ge );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#indexOfDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  public int indexOfDatabase( DatabaseMeta di ) {
    return databases.indexOf( di );
  }

  /**
   * Sets the job entry.
   *
   * @param idx
   *          the idx
   * @param jec
   *          the jec
   */
  public void setJobEntry( int idx, JobEntryCopy jec ) {
    jobcopies.set( idx, jec );
  }

  /**
   * Find an existing JobEntryCopy by it's name and number
   *
   * @param name
   *          The name of the job entry copy
   * @param nr
   *          The number of the job entry copy
   * @return The JobEntryCopy or null if nothing was found!
   */
  public JobEntryCopy findJobEntry( String name, int nr, boolean searchHiddenToo ) {
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy jec = getJobEntry( i );
      if ( jec.getName().equalsIgnoreCase( name ) && jec.getNr() == nr ) {
        if ( searchHiddenToo || jec.isDrawn() ) {
          return jec;
        }
      }
    }
    return null;
  }

  /**
   * Find job entry.
   *
   * @param full_name_nr
   *          the full_name_nr
   * @return the job entry copy
   */
  public JobEntryCopy findJobEntry( String full_name_nr ) {
    int i;
    for ( i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy jec = getJobEntry( i );
      JobEntryInterface je = jec.getEntry();
      if ( je.toString().equalsIgnoreCase( full_name_nr ) ) {
        return jec;
      }
    }
    return null;
  }

  /**
   * Find job hop.
   *
   * @param name
   *          the name
   * @return the job hop meta
   */
  public JobHopMeta findJobHop( String name ) {
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.toString().equalsIgnoreCase( name ) ) {
        return hi;
      }
    }
    return null;
  }

  /**
   * Find job hop from.
   *
   * @param jge
   *          the jge
   * @return the job hop meta
   */
  public JobHopMeta findJobHopFrom( JobEntryCopy jge ) {
    if ( jge != null ) {
      for ( JobHopMeta hi : jobhops ) {

        // Return the first we find!
        //
        if ( hi != null && ( hi.getFromEntry() != null ) && hi.getFromEntry().equals( jge ) ) {
          return hi;
        }
      }
    }
    return null;
  }

  /**
   * Find job hop.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @return the job hop meta
   */
  public JobHopMeta findJobHop( JobEntryCopy from, JobEntryCopy to ) {
    return findJobHop( from, to, false );
  }

  /**
   * Find job hop.
   *
   * @param from
   *          the from
   * @param to
   *          the to
   * @param includeDisabled
   *          the include disabled
   * @return the job hop meta
   */
  public JobHopMeta findJobHop( JobEntryCopy from, JobEntryCopy to, boolean includeDisabled ) {
    for ( JobHopMeta hi : jobhops ) {
      if ( hi.isEnabled() || includeDisabled ) {
        if ( hi != null
          && hi.getFromEntry() != null && hi.getToEntry() != null && hi.getFromEntry().equals( from )
          && hi.getToEntry().equals( to ) ) {
          return hi;
        }
      }
    }
    return null;
  }

  /**
   * Find job hop to.
   *
   * @param jge
   *          the jge
   * @return the job hop meta
   */
  public JobHopMeta findJobHopTo( JobEntryCopy jge ) {
    for ( JobHopMeta hi : jobhops ) {
      if ( hi != null && hi.getToEntry() != null && hi.getToEntry().equals( jge ) ) {
        // Return the first!
        return hi;
      }
    }
    return null;
  }

  /**
   * Find nr prev job entries.
   *
   * @param from
   *          the from
   * @return the int
   */
  public int findNrPrevJobEntries( JobEntryCopy from ) {
    return findNrPrevJobEntries( from, false );
  }

  /**
   * Find prev job entry.
   *
   * @param to
   *          the to
   * @param nr
   *          the nr
   * @return the job entry copy
   */
  public JobEntryCopy findPrevJobEntry( JobEntryCopy to, int nr ) {
    return findPrevJobEntry( to, nr, false );
  }

  /**
   * Find nr prev job entries.
   *
   * @param to
   *          the to
   * @param info
   *          the info
   * @return the int
   */
  public int findNrPrevJobEntries( JobEntryCopy to, boolean info ) {
    int count = 0;

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.isEnabled() && hi.getToEntry().equals( to ) ) {
        count++;
      }
    }
    return count;
  }

  /**
   * Find prev job entry.
   *
   * @param to
   *          the to
   * @param nr
   *          the nr
   * @param info
   *          the info
   * @return the job entry copy
   */
  public JobEntryCopy findPrevJobEntry( JobEntryCopy to, int nr, boolean info ) {
    int count = 0;

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.isEnabled() && hi.getToEntry().equals( to ) ) {
        if ( count == nr ) {
          return hi.getFromEntry();
        }
        count++;
      }
    }
    return null;
  }

  /**
   * Find nr next job entries.
   *
   * @param from
   *          the from
   * @return the int
   */
  public int findNrNextJobEntries( JobEntryCopy from ) {
    int count = 0;
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.isEnabled() && ( hi.getFromEntry() != null ) && hi.getFromEntry().equals( from ) ) {
        count++;
      }
    }
    return count;
  }

  /**
   * Find next job entry.
   *
   * @param from
   *          the from
   * @param cnt
   *          the cnt
   * @return the job entry copy
   */
  public JobEntryCopy findNextJobEntry( JobEntryCopy from, int cnt ) {
    int count = 0;

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.isEnabled() && ( hi.getFromEntry() != null ) && hi.getFromEntry().equals( from ) ) {
        if ( count == cnt ) {
          return hi.getToEntry();
        }
        count++;
      }
    }
    return null;
  }

  /**
   * Checks for loop.
   *
   * @param entry
   *          the entry
   * @return true, if successful
   */
  public boolean hasLoop( JobEntryCopy entry ) {
    return hasLoop( entry, null );
  }

  /**
   * Checks for loop.
   *
   * @param entry
   *          the entry
   * @param lookup
   *          the lookup
   * @return true, if successful
   */
  public boolean hasLoop( JobEntryCopy entry, JobEntryCopy lookup ) {
    return false;
  }

  /**
   * Checks if is entry used in hops.
   *
   * @param jge
   *          the jge
   * @return true, if is entry used in hops
   */
  public boolean isEntryUsedInHops( JobEntryCopy jge ) {
    JobHopMeta fr = findJobHopFrom( jge );
    JobHopMeta to = findJobHopTo( jge );
    if ( fr != null || to != null ) {
      return true;
    }
    return false;
  }

  /**
   * Count entries.
   *
   * @param name
   *          the name
   * @return the int
   */
  public int countEntries( String name ) {
    int count = 0;
    int i;
    for ( i = 0; i < nrJobEntries(); i++ ) {
      // Look at all the hops;

      JobEntryCopy je = getJobEntry( i );
      if ( je.getName().equalsIgnoreCase( name ) ) {
        count++;
      }
    }
    return count;
  }

  /**
   * Find unused nr.
   *
   * @param name
   *          the name
   * @return the int
   */
  public int findUnusedNr( String name ) {
    int nr = 1;
    JobEntryCopy je = findJobEntry( name, nr, true );
    while ( je != null ) {
      nr++;
      // log.logDebug("findUnusedNr()", "Trying unused nr: "+nr);
      je = findJobEntry( name, nr, true );
    }
    return nr;
  }

  /**
   * Find max nr.
   *
   * @param name
   *          the name
   * @return the int
   */
  public int findMaxNr( String name ) {
    int max = 0;
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy je = getJobEntry( i );
      if ( je.getName().equalsIgnoreCase( name ) ) {
        if ( je.getNr() > max ) {
          max = je.getNr();
        }
      }
    }
    return max;
  }

  /**
   * Proposes an alternative job entry name when the original already exists...
   *
   * @param entryname
   *          The job entry name to find an alternative for..
   * @return The alternative stepname.
   */
  public String getAlternativeJobentryName( String entryname ) {
    String newname = entryname;
    JobEntryCopy jec = findJobEntry( newname );
    int nr = 1;
    while ( jec != null ) {
      nr++;
      newname = entryname + " " + nr;
      jec = findJobEntry( newname );
    }

    return newname;
  }

  /**
   * Gets the all job graph entries.
   *
   * @param name
   *          the name
   * @return the all job graph entries
   */
  public JobEntryCopy[] getAllJobGraphEntries( String name ) {
    int count = 0;
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy je = getJobEntry( i );
      if ( je.getName().equalsIgnoreCase( name ) ) {
        count++;
      }
    }
    JobEntryCopy[] retval = new JobEntryCopy[count];

    count = 0;
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy je = getJobEntry( i );
      if ( je.getName().equalsIgnoreCase( name ) ) {
        retval[count] = je;
        count++;
      }
    }
    return retval;
  }

  /**
   * Gets the all job hops using.
   *
   * @param name
   *          the name
   * @return the all job hops using
   */
  public JobHopMeta[] getAllJobHopsUsing( String name ) {
    List<JobHopMeta> hops = new ArrayList<JobHopMeta>();

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.getFromEntry() != null && hi.getToEntry() != null ) {
        if ( hi.getFromEntry().getName().equalsIgnoreCase( name )
          || hi.getToEntry().getName().equalsIgnoreCase( name ) ) {
          hops.add( hi );
        }
      }
    }
    return hops.toArray( new JobHopMeta[hops.size()] );
  }

  /**
   * Gets the note.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @return the note
   */
  public NotePadMeta getNote( int x, int y ) {
    int i, s;
    s = notes.size();
    for ( i = s - 1; i >= 0; i-- ) {
      // Back to front because drawing goes from start to end

      NotePadMeta ni = notes.get( i );
      Point loc = ni.getLocation();
      Point p = new Point( loc.x, loc.y );
      if ( x >= p.x
        && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y
        && y <= p.y + ni.height + 2 * Const.NOTE_MARGIN ) {
        return ni;
      }
    }
    return null;
  }

  /**
   * Select all.
   */
  public void selectAll() {
    int i;
    for ( i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy ce = getJobEntry( i );
      ce.setSelected( true );
    }
    for ( i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      ni.setSelected( true );
    }
    setChanged();
    notifyObservers( "refreshGraph" );
  }

  /**
   * Unselect all.
   */
  public void unselectAll() {
    int i;
    for ( i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy ce = getJobEntry( i );
      ce.setSelected( false );
    }
    for ( i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      ni.setSelected( false );
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#getMaxUndo()
   */
  public int getMaxUndo() {
    return max_undo;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#setMaxUndo(int)
   */
  public void setMaxUndo( int mu ) {
    max_undo = mu;
    while ( undo.size() > mu && undo.size() > 0 ) {
      undo.remove( 0 );
    }
  }

  /**
   * Gets the undo size.
   *
   * @return the undo size
   */
  public int getUndoSize() {
    if ( undo == null ) {
      return 0;
    }
    return undo.size();
  }

  /**
   * Clear undo.
   */
  public void clearUndo() {
    undo = new ArrayList<TransAction>();
    undo_position = -1;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#addUndo(java.lang.Object[], java.lang.Object[], int[],
   * org.pentaho.di.core.gui.Point[], org.pentaho.di.core.gui.Point[], int, boolean)
   */
  public void addUndo( Object[] from, Object[] to, int[] pos, Point[] prev, Point[] curr, int type_of_change,
    boolean nextAlso ) {
    // First clean up after the current position.
    // Example: position at 3, size=5
    // 012345
    // ^
    // remove 34
    // Add 4
    // 01234

    while ( undo.size() > undo_position + 1 && undo.size() > 0 ) {
      int last = undo.size() - 1;
      undo.remove( last );
    }

    TransAction ta = new TransAction();
    switch ( type_of_change ) {
      case TYPE_UNDO_CHANGE:
        ta.setChanged( from, to, pos );
        break;
      case TYPE_UNDO_DELETE:
        ta.setDelete( from, pos );
        break;
      case TYPE_UNDO_NEW:
        ta.setNew( from, pos );
        break;
      case TYPE_UNDO_POSITION:
        ta.setPosition( from, pos, prev, curr );
        break;
      default:
        break;
    }
    undo.add( ta );
    undo_position++;

    if ( undo.size() > max_undo ) {
      undo.remove( 0 );
      undo_position--;
    }
  }

  // get previous undo, change position
  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#previousUndo()
   */
  public TransAction previousUndo() {
    if ( undo.isEmpty() || undo_position < 0 ) {
      return null; // No undo left!
    }

    TransAction retval = undo.get( undo_position );

    undo_position--;

    return retval;
  }

  /**
   * View current undo, don't change undo position
   *
   * @return The current undo transaction
   */
  public TransAction viewThisUndo() {
    if ( undo.isEmpty() || undo_position < 0 ) {
      return null; // No undo left!
    }

    TransAction retval = undo.get( undo_position );

    return retval;
  }

  // View previous undo, don't change position
  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#viewPreviousUndo()
   */
  public TransAction viewPreviousUndo() {
    if ( undo.isEmpty() || undo_position < 0 ) {
      return null; // No undo left!
    }

    TransAction retval = undo.get( undo_position );

    return retval;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#nextUndo()
   */
  public TransAction nextUndo() {
    int size = undo.size();
    if ( size == 0 || undo_position >= size - 1 ) {
      return null; // no redo left...
    }

    undo_position++;

    TransAction retval = undo.get( undo_position );

    return retval;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#viewNextUndo()
   */
  public TransAction viewNextUndo() {
    int size = undo.size();
    if ( size == 0 || undo_position >= size - 1 ) {
      return null; // no redo left...
    }

    TransAction retval = undo.get( undo_position + 1 );

    return retval;
  }

  /**
   * Gets the maximum.
   *
   * @return the maximum
   */
  public Point getMaximum() {
    int maxx = 0, maxy = 0;
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy entry = getJobEntry( i );
      Point loc = entry.getLocation();
      if ( loc.x > maxx ) {
        maxx = loc.x;
      }
      if ( loc.y > maxy ) {
        maxy = loc.y;
      }
    }
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      Point loc = ni.getLocation();
      if ( loc.x + ni.width > maxx ) {
        maxx = loc.x + ni.width;
      }
      if ( loc.y + ni.height > maxy ) {
        maxy = loc.y + ni.height;
      }
    }

    return new Point( maxx + 100, maxy + 100 );
  }

  /**
   * Get the minimum point on the canvas of a job
   *
   * @return Minimum coordinate of a step in the job
   */
  public Point getMinimum() {
    int minx = Integer.MAX_VALUE;
    int miny = Integer.MAX_VALUE;
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy jobEntryCopy = getJobEntry( i );
      Point loc = jobEntryCopy.getLocation();
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

    if ( minx > 20 ) {
      minx -= 20;
    } else {
      minx = 0;
    }
    if ( miny > 20 ) {
      miny -= 20;
    } else {
      miny = 0;
    }

    return new Point( minx, miny );
  }

  /**
   * Gets the selected locations.
   *
   * @return the selected locations
   */
  public Point[] getSelectedLocations() {
    List<JobEntryCopy> selectedEntries = getSelectedEntries();
    Point[] retval = new Point[selectedEntries.size()];
    for ( int i = 0; i < retval.length; i++ ) {
      JobEntryCopy si = selectedEntries.get( i );
      Point p = si.getLocation();
      retval[i] = new Point( p.x, p.y ); // explicit copy of location
    }
    return retval;
  }

  /**
   * Get all the selected note locations
   *
   * @return The selected step and notes locations.
   */
  public Point[] getSelectedNoteLocations() {
    List<Point> points = new ArrayList<Point>();

    for ( NotePadMeta ni : getSelectedNotes() ) {
      Point p = ni.getLocation();
      points.add( new Point( p.x, p.y ) ); // explicit copy of location
    }

    return points.toArray( new Point[points.size()] );
  }

  /**
   * Gets the selected entries.
   *
   * @return the selected entries
   */
  public List<JobEntryCopy> getSelectedEntries() {
    List<JobEntryCopy> selection = new ArrayList<JobEntryCopy>();
    for ( JobEntryCopy je : jobcopies ) {
      if ( je.isSelected() ) {
        selection.add( je );
      }
    }
    return selection;
  }

  /**
   * Gets a list of all selected notes.
   *
   * @return A list of all the selected notes.
   */
  public List<NotePadMeta> getSelectedNotes() {
    List<NotePadMeta> selection = new ArrayList<NotePadMeta>();
    for ( NotePadMeta note : notes ) {
      if ( note.isSelected() ) {
        selection.add( note );
      }
    }
    return selection;
  }

  /**
   * Gets the entry indexes.
   *
   * @param entries
   *          the entries
   * @return the entry indexes
   */
  public int[] getEntryIndexes( List<JobEntryCopy> entries ) {
    int[] retval = new int[entries.size()];

    for ( int i = 0; i < entries.size(); i++ ) {
      retval[i] = indexOfJobEntry( entries.get( i ) );
    }

    return retval;
  }

  /**
   * Get an array of the locations of an array of notes
   *
   * @param notes
   *          An array of notes
   * @return an array of the locations of an array of notes
   */
  public int[] getNoteIndexes( List<NotePadMeta> notes ) {
    int[] retval = new int[notes.size()];

    for ( int i = 0; i < notes.size(); i++ ) {
      retval[i] = indexOfNote( notes.get( i ) );
    }

    return retval;
  }

  /**
   * Find start.
   *
   * @return the job entry copy
   */
  public JobEntryCopy findStart() {
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      if ( getJobEntry( i ).isStart() ) {
        return getJobEntry( i );
      }
    }
    return null;
  }

  /**
   * Gets a textual representation of the job. If its name has been set, it will be returned, otherwise the classname is
   * returned.
   *
   * @return the textual representation of the job.
   */
  public String toString() {
    if ( !Const.isEmpty( filename ) ) {
      if ( Const.isEmpty( name ) ) {
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
      return JobMeta.class.getName();
    }
  }

  /**
   * Gets the boolean value of batch id passed.
   *
   * @return Returns the batchIdPassed.
   */
  public boolean isBatchIdPassed() {
    return batchIdPassed;
  }

  /**
   * Sets the batch id passed.
   *
   * @param batchIdPassed
   *          The batchIdPassed to set.
   */
  public void setBatchIdPassed( boolean batchIdPassed ) {
    this.batchIdPassed = batchIdPassed;
  }

  public List<SQLStatement> getSQLStatements( Repository repository, ProgressMonitorListener monitor )
    throws KettleException {
    return getSQLStatements( repository, null, monitor );
  }

  /**
   * Builds a list of all the SQL statements that this transformation needs in order to work properly.
   *
   * @return An ArrayList of SQLStatement objects.
   */
  public List<SQLStatement> getSQLStatements( Repository repository, IMetaStore metaStore,
    ProgressMonitorListener monitor ) throws KettleException {
    if ( monitor != null ) {
      monitor.beginTask(
        BaseMessages.getString( PKG, "JobMeta.Monitor.GettingSQLNeededForThisJob" ), nrJobEntries() + 1 );
    }
    List<SQLStatement> stats = new ArrayList<SQLStatement>();

    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy copy = getJobEntry( i );
      if ( monitor != null ) {
        monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.GettingSQLForJobEntryCopy" ) + copy + "]" );
      }
      stats.addAll( copy.getEntry().getSQLStatements( repository, metaStore, this ) );
      stats.addAll( compatibleGetEntrySQLStatements( copy.getEntry(), repository ) );
      stats.addAll( compatibleGetEntrySQLStatements( copy.getEntry(), repository, this ) );
      if ( monitor != null ) {
        monitor.worked( 1 );
      }
    }

    // Also check the sql for the logtable...
    if ( monitor != null ) {
      monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.GettingSQLStatementsForJobLogTables" ) );
    }
    if ( jobLogTable.getDatabaseMeta() != null && !Const.isEmpty( jobLogTable.getTableName() ) ) {
      Database db = new Database( this, jobLogTable.getDatabaseMeta() );
      try {
        db.connect();
        RowMetaInterface fields = jobLogTable.getLogRecord( LogStatus.START, null, null ).getRowMeta();
        String sql = db.getDDL( jobLogTable.getTableName(), fields );
        if ( sql != null && sql.length() > 0 ) {
          SQLStatement stat =
            new SQLStatement( BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ThisJob" ), jobLogTable
              .getDatabaseMeta(), sql );
          stats.add( stat );
        }
      } catch ( KettleDatabaseException dbe ) {
        SQLStatement stat =
          new SQLStatement( BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ThisJob" ), jobLogTable
            .getDatabaseMeta(), null );
        stat.setError( BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ErrorObtainingJobLogTableInfo" )
          + dbe.getMessage() );
        stats.add( stat );
      } finally {
        db.disconnect();
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
  private Collection<? extends SQLStatement> compatibleGetEntrySQLStatements( JobEntryInterface entry,
    Repository repository, VariableSpace variableSpace ) throws KettleException {
    return entry.getSQLStatements( repository, variableSpace );
  }

  @SuppressWarnings( "deprecation" )
  private Collection<? extends SQLStatement> compatibleGetEntrySQLStatements( JobEntryInterface entry,
    Repository repository ) throws KettleException {
    return entry.getSQLStatements( repository );
  }

  /**
   * Gets the arguments used for this job.
   *
   * @return Returns the arguments.
   * @deprecated Moved to the Job class
   */
  @Deprecated
  public String[] getArguments() {
    return arguments;
  }

  /**
   * Sets the arguments.
   *
   * @param arguments
   *          The arguments to set.
   * @deprecated moved to the job class
   */
  @Deprecated
  public void setArguments( String[] arguments ) {
    this.arguments = arguments;
  }

  /**
   * Get a list of all the strings used in this job.
   *
   * @return A list of StringSearchResult with strings used in the job
   */
  public List<StringSearchResult> getStringList( boolean searchSteps, boolean searchDatabases, boolean searchNotes ) {
    List<StringSearchResult> stringList = new ArrayList<StringSearchResult>();

    if ( searchSteps ) {
      // Loop over all steps in the transformation and see what the used
      // vars are...
      for ( int i = 0; i < nrJobEntries(); i++ ) {
        JobEntryCopy entryMeta = getJobEntry( i );
        stringList.add( new StringSearchResult( entryMeta.getName(), entryMeta, this, BaseMessages.getString(
          PKG, "JobMeta.SearchMetadata.JobEntryName" ) ) );
        if ( entryMeta.getDescription() != null ) {
          stringList.add( new StringSearchResult( entryMeta.getDescription(), entryMeta, this, BaseMessages
            .getString( PKG, "JobMeta.SearchMetadata.JobEntryDescription" ) ) );
        }
        JobEntryInterface metaInterface = entryMeta.getEntry();
        StringSearcher.findMetaData( metaInterface, 1, stringList, entryMeta, this );
      }
    }

    // Loop over all steps in the transformation and see what the used vars
    // are...
    if ( searchDatabases ) {
      for ( int i = 0; i < nrDatabases(); i++ ) {
        DatabaseMeta meta = getDatabase( i );
        stringList.add( new StringSearchResult( meta.getName(), meta, this, BaseMessages.getString(
          PKG, "JobMeta.SearchMetadata.DatabaseConnectionName" ) ) );
        if ( meta.getHostname() != null ) {
          stringList.add( new StringSearchResult( meta.getHostname(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabaseHostName" ) ) );
        }
        if ( meta.getDatabaseName() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabaseName(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabaseName" ) ) );
        }
        if ( meta.getUsername() != null ) {
          stringList.add( new StringSearchResult( meta.getUsername(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabaseUsername" ) ) );
        }
        if ( meta.getPluginId() != null ) {
          stringList.add( new StringSearchResult( meta.getPluginId(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabaseTypeDescription" ) ) );
        }
        if ( meta.getDatabasePortNumberString() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabasePortNumberString(), meta, this, BaseMessages
            .getString( PKG, "JobMeta.SearchMetadata.DatabasePort" ) ) );
        }
        if ( meta.getServername() != null ) {
          stringList.add( new StringSearchResult( meta.getServername(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabaseServer" ) ) );
        }
        // if ( includePasswords )
        // {
        if ( meta.getPassword() != null ) {
          stringList.add( new StringSearchResult( meta.getPassword(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.DatabasePassword" ) ) );
          // }
        }
      }
    }

    // Loop over all steps in the transformation and see what the used vars
    // are...
    if ( searchNotes ) {
      for ( int i = 0; i < nrNotes(); i++ ) {
        NotePadMeta meta = getNote( i );
        if ( meta.getNote() != null ) {
          stringList.add( new StringSearchResult( meta.getNote(), meta, this, BaseMessages.getString(
            PKG, "JobMeta.SearchMetadata.NotepadText" ) ) );
        }
      }
    }

    return stringList;
  }

  /**
   * Gets the used variables.
   *
   * @return the used variables
   */
  public List<String> getUsedVariables() {
    // Get the list of Strings.
    List<StringSearchResult> stringList = getStringList( true, true, false );

    List<String> varList = new ArrayList<String>();

    // Look around in the strings, see what we find...
    for ( StringSearchResult result : stringList ) {
      StringUtil.getUsedVariables( result.getString(), varList, false );
    }

    return varList;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#haveConnectionsChanged()
   */
  public boolean haveConnectionsChanged() {
    if ( changedDatabases ) {
      return true;
    }

    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta ci = getDatabase( i );
      if ( ci.hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Have job entries changed.
   *
   * @return true, if successful
   */
  public boolean haveJobEntriesChanged() {
    if ( changedEntries ) {
      return true;
    }

    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy entry = getJobEntry( i );
      if ( entry.hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Have job hops changed.
   *
   * @return true, if successful
   */
  public boolean haveJobHopsChanged() {
    if ( changedHops ) {
      return true;
    }

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Have notes changed.
   *
   * @return true, if successful
   */
  public boolean haveNotesChanged() {
    if ( changedNotes ) {
      return true;
    }

    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta note = getNote( i );
      if ( note.hasChanged() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets the shared objects file.
   *
   * @return the sharedObjectsFile
   */
  public String getSharedObjectsFile() {
    return sharedObjectsFile;
  }

  /**
   * Sets the shared objects file.
   *
   * @param sharedObjectsFile
   *          the sharedObjectsFile to set
   */
  public void setSharedObjectsFile( String sharedObjectsFile ) {
    this.sharedObjectsFile = sharedObjectsFile;
  }

  /**
   * Sets the user who last modified the job.
   *
   * @param modifiedUser
   *          The modifiedUser to set.
   */
  public void setModifiedUser( String modifiedUser ) {
    this.modifiedUser = modifiedUser;
  }

  /**
   * Gets the user who last modified the job.
   *
   * @return Returns the modifiedUser.
   */
  public String getModifiedUser() {
    return modifiedUser;
  }

  /**
   * Sets the date the job was modified.
   *
   * @param modifiedDate
   *          The modifiedDate to set.
   */
  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }

  /**
   * Gets the date the job was last modified.
   *
   * @return Returns the modifiedDate.
   */
  public Date getModifiedDate() {
    return modifiedDate;
  }

  /**
   * Gets the description of the job.
   *
   * @return The description of the job
   */
  public String getDescription() {
    return description;
  }

  /**
   * Gets the extended description of the job.
   *
   * @return The extended description of the job
   */
  public String getExtendedDescription() {
    return extendedDescription;
  }

  /**
   * Gets the version of the job.
   *
   * @return The version of the job
   */
  public String getJobversion() {
    return jobVersion;
  }

  /**
   * Gets the status of the job.
   *
   * @return the status of the job
   */
  public int getJobstatus() {
    return jobStatus;
  }

  /**
   * Set the description of the job.
   *
   * @param description
   *          The new description of the job
   */
  public void setDescription( String description ) {
    this.description = description;
  }

  /**
   * Set the description of the job.
   *
   * @param extendedDescription
   *          The new extended description of the job
   */
  public void setExtendedDescription( String extendedDescription ) {
    this.extendedDescription = extendedDescription;
  }

  /**
   * Set the version of the job.
   *
   * @param jobVersion
   *          The new version description of the job
   */
  public void setJobversion( String jobVersion ) {
    this.jobVersion = jobVersion;
  }

  /**
   * Set the status of the job.
   *
   * @param jobStatus
   *          The new status description of the job
   */
  public void setJobstatus( int jobStatus ) {
    this.jobStatus = jobStatus;
  }

  /**
   * Gets the date the job was created.
   *
   * @return Returns the createdDate.
   */
  public Date getCreatedDate() {
    return created_date;
  }

  /**
   * @param createdDate
   *          The createdDate to set.
   */
  public void setCreatedDate( Date createdDate ) {
    created_date = createdDate;
  }

  /**
   * @param createdUser
   *          The createdUser to set.
   */
  public void setCreatedUser( String createdUser ) {
    created_user = createdUser;
  }

  /**
   * Gets the user by whom the job was created.
   *
   * @return Returns the createdUser.
   */
  public String getCreatedUser() {
    return created_user;
  }

  /**
   * Find a jobentry with a certain ID in a list of job entries.
   *
   * @param jobentries
   *          The List of jobentries
   * @param id_jobentry
   *          The id of the jobentry
   * @return The JobEntry object if one was found, null otherwise.
   */
  public static final JobEntryInterface findJobEntry( List<JobEntryInterface> jobentries, ObjectId id_jobentry ) {
    if ( jobentries == null ) {
      return null;
    }

    for ( JobEntryInterface je : jobentries ) {
      if ( je.getObjectId() != null && je.getObjectId().equals( id_jobentry ) ) {
        return je;
      }
    }
    return null;
  }

  /**
   * Find a jobentrycopy with a certain ID in a list of job entry copies.
   *
   * @param jobcopies
   *          The List of jobentry copies
   * @param id_jobentry_copy
   *          The id of the jobentry copy
   * @return The JobEntryCopy object if one was found, null otherwise.
   */
  public static final JobEntryCopy findJobEntryCopy( List<JobEntryCopy> jobcopies, ObjectId id_jobentry_copy ) {
    if ( jobcopies == null ) {
      return null;
    }

    for ( JobEntryCopy jec : jobcopies ) {
      if ( jec.getObjectId() != null && jec.getObjectId().equals( id_jobentry_copy ) ) {
        return jec;
      }
    }
    return null;
  }

  /**
   * Calls setInternalKettleVariables on the default object.
   */
  public void setInternalKettleVariables() {
    setInternalKettleVariables( variables );
  }

  /**
   * This method sets various internal kettle variables that can be used by the transformation.
   */
  public void setInternalKettleVariables( VariableSpace var ) {
    setInternalFilenameKettleVariables( var );
    setInternalNameKettleVariable( var );

    // The name of the directory in the repository
    var.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, directory != null
      ? directory.getPath() : "" );

    // Undefine the transformation specific variables:
    // transformations can't run jobs, so if you use these they are 99.99%
    // wrong.
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null );
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null );
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY, null );
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_NAME, null );
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_NAME, null );
    var.setVariable( Const.INTERNAL_VARIABLE_TRANSFORMATION_REPOSITORY_DIRECTORY, null );
  }

  /**
   * Sets the internal name kettle variable.
   *
   * @param var
   *          the new internal name kettle variable
   */
  private void setInternalNameKettleVariable( VariableSpace var ) {
    // The name of the job
    var.setVariable( Const.INTERNAL_VARIABLE_JOB_NAME, Const.NVL( name, "" ) );
  }

  /**
   * Sets the internal filename kettle variables.
   *
   * @param var
   *          the new internal filename kettle variables
   */
  private void setInternalFilenameKettleVariables( VariableSpace var ) {
    if ( filename != null ) {
      // we have a filename that's defined.

      try {
        FileObject fileObject = KettleVFS.getFileObject( filename, var );
        FileName fileName = fileObject.getName();

        // The filename of the job
        var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the job
        FileName fileDir = fileName.getParent();
        var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI() );
      } catch ( Exception e ) {
        var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "" );
        var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "" );
      }
    } else {
      var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "" );
      var.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "" );
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#copyVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String)
   */
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  /*
   * (non-javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String[])
   */
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData )
    throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getParentVariableSpace()
   */
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.core.variables.VariableSpace#setParentVariableSpace(org.pentaho.di.core.variables.VariableSpace)
   */
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String, java.lang.String)
   */
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String)
   */
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getBooleanValueOfVariable(java.lang.String, boolean)
   */
  public boolean getBooleanValueOfVariable( String variableName, boolean defaultValue ) {
    if ( !Const.isEmpty( variableName ) ) {
      String value = environmentSubstitute( variableName );
      if ( !Const.isEmpty( value ) ) {
        return ValueMeta.convertStringToBoolean( value );
      }
    }
    return defaultValue;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.core.variables.VariableSpace#initializeVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#listVariables()
   */
  public String[] listVariables() {
    return variables.listVariables();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#setVariable(java.lang.String, java.lang.String)
   */
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#shareVariablesWith(org.pentaho.di.core.variables.VariableSpace)
   */
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#injectVariables(java.util.Map)
   */
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  @Deprecated
  public void checkJobEntries( List<CheckResultInterface> remarks, boolean only_selected,
    ProgressMonitorListener monitor ) {
    checkJobEntries( remarks, only_selected, monitor, this, null, null );
  }

  /**
   * Check all job entries within the job. Each Job Entry has the opportunity to check their own settings.
   *
   * @param remarks
   *          List of CheckResult remarks inserted into by each JobEntry
   * @param only_selected
   *          true if you only want to check the selected jobs
   * @param monitor
   *          Progress monitor (not presently in use)
   */
  public void checkJobEntries( List<CheckResultInterface> remarks, boolean only_selected,
    ProgressMonitorListener monitor, VariableSpace space, Repository repository, IMetaStore metaStore ) {
    remarks.clear(); // Empty remarks
    if ( monitor != null ) {
      monitor.beginTask(
        BaseMessages.getString( PKG, "JobMeta.Monitor.VerifyingThisJobEntryTask.Title" ), jobcopies.size() + 2 );
    }
    boolean stop_checking = false;
    for ( int i = 0; i < jobcopies.size() && !stop_checking; i++ ) {
      JobEntryCopy copy = jobcopies.get( i ); // get the job entry copy
      if ( ( !only_selected ) || ( only_selected && copy.isSelected() ) ) {
        JobEntryInterface entry = copy.getEntry();
        if ( entry != null ) {
          if ( monitor != null ) {
            monitor.subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.VerifyingJobEntry.Title", entry
              .getName() ) );
          }
          entry.check( remarks, this, space, repository, metaStore );
          compatibleEntryCheck( entry, remarks );
          if ( monitor != null ) {
            monitor.worked( 1 ); // progress bar...
            if ( monitor.isCanceled() ) {
              stop_checking = true;
            }
          }
        }
      }
      if ( monitor != null ) {
        monitor.worked( 1 );
      }
    }
    if ( monitor != null ) {
      monitor.done();
    }
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleEntryCheck( JobEntryInterface entry, List<CheckResultInterface> remarks ) {
    entry.check( remarks, this );
  }

  /**
   * Gets the resource dependencies.
   *
   * @return the resource dependencies
   */
  public List<ResourceReference> getResourceDependencies() {
    List<ResourceReference> resourceReferences = new ArrayList<ResourceReference>();
    JobEntryCopy copy = null;
    JobEntryInterface entry = null;
    for ( int i = 0; i < jobcopies.size(); i++ ) {
      copy = jobcopies.get( i ); // get the job entry copy
      entry = copy.getEntry();
      resourceReferences.addAll( entry.getResourceDependencies( this ) );
    }

    return resourceReferences;
  }

  public String exportResources( VariableSpace space, Map<String, ResourceDefinition> definitions,
    ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore )
    throws KettleException {
    String resourceName = null;
    try {
      // Handle naming for both repository and XML bases resources...
      //
      String baseName;
      String originalPath;
      String fullname;
      String extension = "kjb";
      if ( Const.isEmpty( getFilename() ) ) {
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
        originalPath = fileObject.getParent().getName().getPath();
        baseName = fileObject.getName().getBaseName();
        fullname = fileObject.getName().getPath();
      }

      resourceName =
        namingInterface.nameResource(
          baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.JOB );
      ResourceDefinition definition = definitions.get( resourceName );
      if ( definition == null ) {
        // If we do this once, it will be plenty :-)
        //
        JobMeta jobMeta = (JobMeta) this.realClone( false );

        // All objects get re-located to the root folder
        //
        jobMeta.setRepositoryDirectory( new RepositoryDirectory() );

        // Add used resources, modify transMeta accordingly
        // Go through the list of steps, etc.
        // These critters change the steps in the cloned TransMeta
        // At the end we make a new XML version of it in "exported"
        // format...

        // loop over steps, databases will be exported to XML anyway.
        //
        for ( JobEntryCopy jobEntry : jobMeta.jobcopies ) {
          compatibleJobEntryExportResources(
            jobEntry.getEntry(), jobMeta, definitions, namingInterface, repository );
          jobEntry.getEntry().exportResources( jobMeta, definitions, namingInterface, repository, metaStore );
        }

        // Set a number of parameters for all the data files referenced so far...
        //
        Map<String, String> directoryMap = namingInterface.getDirectoryMap();
        if ( directoryMap != null ) {
          for ( String directory : directoryMap.keySet() ) {
            String parameterName = directoryMap.get( directory );
            jobMeta.addParameterDefinition( parameterName, directory, "Data file path discovered during export" );
          }
        }

        // At the end, add ourselves to the map...
        //
        String jobMetaContent = jobMeta.getXML();

        definition = new ResourceDefinition( resourceName, jobMetaContent );

        // Also remember the original filename (if any), including variables etc.
        //
        if ( Const.isEmpty( this.getFilename() ) ) { // Repository
          definition.setOrigin( fullname );
        } else {
          definition.setOrigin( this.getFilename() );
        }

        definitions.put( fullname, definition );
      }
    } catch ( FileSystemException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename() ), e );
    } catch ( KettleFileException e ) {
      throw new KettleException( BaseMessages.getString(
        PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename() ), e );
    }

    return resourceName;
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleJobEntryExportResources( JobEntryInterface entry, JobMeta jobMeta,
    Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface,
    Repository repository2 ) throws KettleException {
    entry.exportResources( jobMeta, definitions, namingInterface, repository );
  }

  /**
   * Gets a list of slave servers.
   *
   * @return the slaveServer list
   */
  public List<SlaveServer> getSlaveServers() {
    return slaveServers;
  }

  /**
   * Sets the slave servers.
   *
   * @param slaveServers
   *          the slaveServers to set
   */
  public void setSlaveServers( List<SlaveServer> slaveServers ) {
    this.slaveServers = slaveServers;
  }

  /**
   * Find a slave server using the name
   *
   * @param serverString
   *          the name of the slave server
   * @return the slave server or null if we couldn't spot an approriate entry.
   */
  public SlaveServer findSlaveServer( String serverString ) {
    return SlaveServer.findSlaveServer( slaveServers, serverString );
  }

  /**
   * Gets an array of slave server names.
   *
   * @return An array list slave server names
   */
  public String[] getSlaveServerNames() {
    return SlaveServer.getSlaveServerNames( slaveServers );
  }

  /**
   * See if the name of the supplied job entry copy doesn't collide with any other job entry copy in the job.
   *
   * @param je
   *          The job entry copy to verify the name for.
   */
  public void renameJobEntryIfNameCollides( JobEntryCopy je ) {
    // First see if the name changed.
    // If so, we need to verify that the name is not already used in the
    // job.
    //
    String newname = je.getName();

    // See if this name exists in the other job entries
    //
    boolean found;
    int nr = 1;
    do {
      found = false;
      for ( JobEntryCopy copy : jobcopies ) {
        if ( copy != je && copy.getName().equalsIgnoreCase( newname ) && copy.getNr() == 0 ) {
          found = true;
        }
      }
      if ( found ) {
        nr++;
        newname = je.getName() + " (" + nr + ")";
      }
    } while ( found );

    // Rename if required.
    //
    je.setName( newname );
  }

  /**
   * Gets the shared objects.
   *
   * @return the sharedObjects
   */
  public SharedObjects getSharedObjects() {
    return sharedObjects;
  }

  /**
   * Sets the shared objects.
   *
   * @param sharedObjects
   *          the sharedObjects to set
   */
  public void setSharedObjects( SharedObjects sharedObjects ) {
    this.sharedObjects = sharedObjects;
  }

  /**
   * Adds the name changed listener.
   *
   * @param listener
   *          the listener
   */
  public void addNameChangedListener( NameChangedListener listener ) {
    if ( nameChangedListeners == null ) {
      nameChangedListeners = new ArrayList<NameChangedListener>();
    }
    nameChangedListeners.add( listener );
  }

  /**
   * Removes the name changed listener.
   *
   * @param listener
   *          the listener
   */
  public void removeNameChangedListener( NameChangedListener listener ) {
    nameChangedListeners.remove( listener );
  }

  /**
   * Adds the filename changed listener.
   *
   * @param listener
   *          the listener
   */
  public void addFilenameChangedListener( FilenameChangedListener listener ) {
    if ( filenameChangedListeners == null ) {
      filenameChangedListeners = new ArrayList<FilenameChangedListener>();
    }
    filenameChangedListeners.add( listener );
  }

  /**
   * Removes the filename changed listener.
   *
   * @param listener
   *          the listener
   */
  public void removeFilenameChangedListener( FilenameChangedListener listener ) {
    filenameChangedListeners.remove( listener );
  }

  /**
   * Adds the passed ContentChangedListener to the list of listeners.
   *
   * @param listener
   */
  public void addContentChangedListener( ContentChangedListener listener ) {
    if ( contentChangedListeners == null ) {
      contentChangedListeners = new ArrayList<ContentChangedListener>();
    }
    contentChangedListeners.add( listener );
  }

  /**
   * Removes the passed ContentChangedListener from the list of listeners.
   *
   * @param listener
   */
  public void removeContentChangedListener( ContentChangedListener listener ) {
    contentChangedListeners.remove( listener );
  }

  /**
   * Name changed.
   *
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   * @return true, if successful
   */
  private boolean nameChanged( String oldFilename, String newFilename ) {
    if ( oldFilename == null && newFilename == null ) {
      return false;
    }
    if ( oldFilename == null && newFilename != null ) {
      return true;
    }
    return oldFilename.equals( newFilename );
  }

  /**
   * Fire filename changed listeners.
   *
   * @param oldFilename
   *          the old filename
   * @param newFilename
   *          the new filename
   */
  private void fireFilenameChangedListeners( String oldFilename, String newFilename ) {
    if ( nameChanged( oldFilename, newFilename ) ) {
      if ( filenameChangedListeners != null ) {
        for ( FilenameChangedListener listener : filenameChangedListeners ) {
          listener.filenameChanged( this, oldFilename, newFilename );
        }
      }
    }
  }

  /**
   * Fire name changed listeners.
   *
   * @param oldName
   *          the old name
   * @param newName
   *          the new name
   */
  private void fireNameChangedListeners( String oldName, String newName ) {
    if ( nameChanged( oldName, newName ) ) {
      if ( nameChangedListeners != null ) {
        for ( NameChangedListener listener : nameChangedListeners ) {
          listener.nameChanged( this, oldName, newName );
        }
      }
    }
  }

  protected void fireContentChangedListeners( boolean ch ) {
    if ( contentChangedListeners != null ) {
      if ( ch ) {
        for ( ContentChangedListener listener : contentChangedListeners ) {
          listener.contentChanged( this );
        }
      } else {
        for ( ContentChangedListener listener : contentChangedListeners ) {
          listener.contentSafe( this );
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#activateParameters()
   */
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

      if ( Const.isEmpty( value ) ) {
        setVariable( key, defValue );
      } else {
        setVariable( key, value );
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#addParameterDefinition(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  public void addParameterDefinition( String key, String defValue, String description )
    throws DuplicateParamException {
    namedParams.addParameterDefinition( key, defValue, description );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDescription(java.lang.String)
   */
  public String getParameterDescription( String key ) throws UnknownParamException {
    return namedParams.getParameterDescription( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDefault(java.lang.String)
   */
  public String getParameterDefault( String key ) throws UnknownParamException {
    return namedParams.getParameterDefault( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterValue(java.lang.String)
   */
  public String getParameterValue( String key ) throws UnknownParamException {
    return namedParams.getParameterValue( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#listParameters()
   */
  public String[] listParameters() {
    return namedParams.listParameters();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#setParameterValue(java.lang.String, java.lang.String)
   */
  public void setParameterValue( String key, String value ) throws UnknownParamException {
    namedParams.setParameterValue( key, value );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#eraseParameters()
   */
  public void eraseParameters() {
    namedParams.eraseParameters();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#clearParameters()
   */
  public void clearParameters() {
    namedParams.clearParameters();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#copyParametersFrom(org.pentaho.di.core.parameters.NamedParams)
   */
  public void copyParametersFrom( NamedParams params ) {
    namedParams.copyParametersFrom( params );
  }

  /**
   * Gets the job copies.
   *
   * @return the job copies
   */
  public List<JobEntryCopy> getJobCopies() {
    return jobcopies;
  }

  /**
   * Gets the notes.
   *
   * @return the notes
   */
  public List<NotePadMeta> getNotes() {
    return notes;
  }

  /**
   * Gets the jobhops.
   *
   * @return the jobhops
   */
  public List<JobHopMeta> getJobhops() {
    return jobhops;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryElementInterface#getRepositoryElementType()
   */
  public RepositoryObjectType getRepositoryElementType() {
    return REPOSITORY_ELEMENT_TYPE;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryElementInterface#getObjectRevision()
   */
  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.repository.RepositoryElementInterface#setObjectRevision(org.pentaho.di.repository.ObjectRevision)
   */
  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  /**
   * Create a unique list of job entry interfaces
   *
   * @return
   */
  public List<JobEntryInterface> composeJobEntryInterfaceList() {
    List<JobEntryInterface> list = new ArrayList<JobEntryInterface>();

    for ( JobEntryCopy copy : jobcopies ) {
      if ( !list.contains( copy.getEntry() ) ) {
        list.add( copy.getEntry() );
      }
    }

    return list;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogChannelId()
   */
  public String getLogChannelId() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectName()
   */
  public String getObjectName() {
    return getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectCopy()
   */
  public String getObjectCopy() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectType()
   */
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.JOBMETA;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getParent()
   */
  public LoggingObjectInterface getParent() {
    return null; // TODO return parent job metadata
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogLevel()
   */
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
  }

  /**
   * Gets the channel log table for the job.
   *
   * @return the channel log table for the job.
   */
  public ChannelLogTable getChannelLogTable() {
    return channelLogTable;
  }

  /**
   * Sets the channel log table for the job.
   *
   * @param channelLogTable
   *          the channelLogTable to set
   */
  public void setChannelLogTable( ChannelLogTable channelLogTable ) {
    this.channelLogTable = channelLogTable;
  }

  /**
   * Gets the job entry log table.
   *
   * @return the jobEntryLogTable
   */
  public JobEntryLogTable getJobEntryLogTable() {
    return jobEntryLogTable;
  }

  /**
   * Sets the job entry log table.
   *
   * @param jobEntryLogTable
   *          the jobEntryLogTable to set
   */
  public void setJobEntryLogTable( JobEntryLogTable jobEntryLogTable ) {
    this.jobEntryLogTable = jobEntryLogTable;
  }

  /**
   * Gets the log tables.
   *
   * @return the log tables
   */
  public List<LogTableInterface> getLogTables() {
    List<LogTableInterface> logTables = new ArrayList<LogTableInterface>();
    logTables.add( jobLogTable );
    logTables.add( jobEntryLogTable );
    logTables.add( channelLogTable );
    logTables.addAll( extraLogTables );
    return logTables;
  }

  /**
   * Checks whether the job can be saved. For JobMeta, this method always returns true
   *
   * @return true
   * @see org.pentaho.di.core.EngineMetaInterface#canSave()
   */
  public boolean canSave() {
    return true;
  }

  /**
   * Gets the container object id.
   *
   * @return the carteObjectId
   */
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * Sets the carte object id.
   *
   * @param containerObjectId
   *          the execution container Object id to set
   */
  public void setCarteObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Gets the registration date for the transformation. For jobMeta, this method always returns null.
   *
   * @return null
   */
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * Checks whether the job has repository references.
   *
   * @return true if the job has repository references, false otherwise
   */
  public boolean hasRepositoryReferences() {
    for ( JobEntryCopy copy : jobcopies ) {
      if ( copy.getEntry().hasRepositoryReferences() ) {
        return true;
      }
    }
    return false;
  }

  /**
   * Look up the references after import
   *
   * @param repository
   *          the repository to reference.
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    for ( JobEntryCopy copy : jobcopies ) {
      if ( copy.getEntry().hasRepositoryReferences() ) {
        copy.getEntry().lookupRepositoryReferences( repository );
      }
    }
  }

  /**
   * Gets the repository.
   *
   * @return the repository
   */
  public Repository getRepository() {
    return repository;
  }

  /**
   * Sets the repository.
   *
   * @param repository
   *          the repository to set
   */
  public void setRepository( Repository repository ) {
    this.repository = repository;
  }

  /**
   * Returns whether or not the job is gathering metrics. For a JobMeta this is always false.
   *
   * @return is gathering metrics = false;
   */
  @Override
  public boolean isGatheringMetrics() {
    return false;
  }

  /**
   * Sets whether or not the job is gathering metrics. This is a stub with not executable code.
   */
  @Override
  public void setGatheringMetrics( boolean gatheringMetrics ) {
  }

  @Override
  public boolean isForcingSeparateLogging() {
    return false;
  }

  @Override
  public void setForcingSeparateLogging( boolean forcingSeparateLogging ) {
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
  }

  /**
   * This method needs to be called to store those objects which are used and referenced in the job metadata but not
   * saved in the serialization.
   *
   * @param metaStore
   *          The store to save to
   * @throws MetaStoreException
   *           in case there is an error.
   */
  public void saveMetaStoreObjects( Repository repository, IMetaStore metaStore ) throws MetaStoreException {
  }

  @Override
  public void setAttributesMap( Map<String, Map<String, String>> attributesMap ) {
    this.attributesMap = attributesMap;
  }

  @Override
  public Map<String, Map<String, String>> getAttributesMap() {
    return attributesMap;
  }

  @Override
  public void setAttribute( String groupName, String key, String value ) {
    Map<String, String> attributes = getAttributes( groupName );
    if ( attributes == null ) {
      attributes = new HashMap<String, String>();
      attributesMap.put( groupName, attributes );
    }
    attributes.put( key, value );
  }

  @Override
  public void setAttributes( String groupName, Map<String, String> attributes ) {
    attributesMap.put( groupName, attributes );
  }

  @Override
  public Map<String, String> getAttributes( String groupName ) {
    return attributesMap.get( groupName );
  }

  @Override
  public String getAttribute( String groupName, String key ) {
    Map<String, String> attributes = attributesMap.get( groupName );
    if ( attributes == null ) {
      return null;
    }
    return attributes.get( key );
  }

  public List<LogTableInterface> getExtraLogTables() {
    return extraLogTables;
  }

  public void setExtraLogTables( List<LogTableInterface> extraLogTables ) {
    this.extraLogTables = extraLogTables;
  }
}
