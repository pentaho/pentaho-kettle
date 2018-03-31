// CHECKSTYLE:FileLength:OFF
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

package org.pentaho.di.job;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.attributes.AttributesUtil;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.exception.LookupReferencesException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogStatus;
import org.pentaho.di.core.logging.LogTableInterface;
import org.pentaho.di.core.logging.LogTablePluginInterface;
import org.pentaho.di.core.logging.LogTablePluginInterface.TableType;
import org.pentaho.di.core.logging.LogTablePluginType;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.reflection.StringSearchResult;
import org.pentaho.di.core.reflection.StringSearcher;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.util.StringUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLFormatter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.core.xml.XMLInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.entries.missing.MissingEntry;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.resource.ResourceDefinition;
import org.pentaho.di.resource.ResourceExportInterface;
import org.pentaho.di.resource.ResourceNamingInterface;
import org.pentaho.di.resource.ResourceReference;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The definition of a PDI job is represented by a JobMeta object. It is typically loaded from a .kjb file, a PDI
 * repository, or it is generated dynamically. The declared parameters of the job definition are then queried using
 * listParameters() and assigned values using calls to setParameterValue(..). JobMeta provides methods to load, save,
 * verify, etc.
 *
 * @author Matt
 * @since 11-08-2003
 */
public class JobMeta extends AbstractMeta
    implements Cloneable, Comparable<JobMeta>, XMLInterface, ResourceExportInterface, RepositoryElementInterface,
    LoggingObjectInterface {

  private static Class<?> PKG = JobMeta.class; // for i18n purposes, needed by Translator2!!

  public static final String XML_TAG = "job";

  protected static final String XML_TAG_SLAVESERVERS = "slaveservers";

  /**
   * A constant specifying the repository element type as a Job.
   */
  public static final RepositoryObjectType REPOSITORY_ELEMENT_TYPE = RepositoryObjectType.JOB;

  static final int BORDER_INDENT = 20;

  protected String jobVersion;

  protected int jobStatus;

  protected List<JobEntryCopy> jobcopies;

  protected List<JobHopMeta> jobhops;

  protected String[] arguments;

  protected boolean changedEntries, changedHops;

  protected JobLogTable jobLogTable;

  protected JobEntryLogTable jobEntryLogTable;

  protected List<LogTableInterface> extraLogTables;

  protected String startCopyName;

  protected boolean expandingRemoteJob;

  /** The log channel interface. */
  protected LogChannelInterface log;

  /**
   * Constant = "SPECIAL"
   **/
  public static final String STRING_SPECIAL = "SPECIAL";

  /**
   * Constant = "START"
   **/
  public static final String STRING_SPECIAL_START = "START";

  /**
   * Constant = "DUMMY"
   **/
  public static final String STRING_SPECIAL_DUMMY = "DUMMY";

  /**
   * Constant = "OK"
   **/
  public static final String STRING_SPECIAL_OK = "OK";

  /**
   * Constant = "ERROR"
   **/
  public static final String STRING_SPECIAL_ERROR = "ERROR";

  /**
   * The loop cache.
   */
  protected Map<String, Boolean> loopCache;

  /**
   * List of booleans indicating whether or not to remember the size and position of the different windows...
   */
  public boolean[] max = new boolean[1];

  protected boolean batchIdPassed;

  protected static final String XML_TAG_PARAMETERS = "parameters";

  private List<MissingEntry> missingEntries;

  /**
   * Instantiates a new job meta.
   */
  public JobMeta() {
    clear();
    initializeVariablesFrom( null );
  }

  /**
   * Clears or reinitializes many of the JobMeta properties.
   */
  @Override
  public void clear() {
    jobcopies = new ArrayList<JobEntryCopy>();
    jobhops = new ArrayList<JobHopMeta>();

    jobLogTable = JobLogTable.getDefault( this, this );
    jobEntryLogTable = JobEntryLogTable.getDefault( this, this );
    extraLogTables = new ArrayList<LogTableInterface>();

    List<PluginInterface> plugins = PluginRegistry.getInstance().getPlugins( LogTablePluginType.class );
    for ( PluginInterface plugin : plugins ) {
      try {
        LogTablePluginInterface logTablePluginInterface = (LogTablePluginInterface) PluginRegistry.getInstance()
            .loadClass( plugin );
        if ( logTablePluginInterface.getType() == TableType.JOB ) {
          logTablePluginInterface.setContext( this, this );
          extraLogTables.add( logTablePluginInterface );
        }
      } catch ( Exception e ) {
        LogChannel.GENERAL.logError( "Error loading log table plugin with ID " + plugin.getIds()[0], e );
      }
    }

    arguments = null;

    super.clear();
    loopCache = new HashMap<String, Boolean>();
    addDefaults();
    jobStatus = -1;
    jobVersion = null;

    // setInternalKettleVariables(); Don't clear the internal variables for
    // ad-hoc jobs, it's ruins the previews
    // etc.

    log = LogChannel.GENERAL;
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
   * Compares two job on name, filename, repository directory, etc.
   * The comparison algorithm is as follows:<br/>
   * <ol>
   * <li>The first job's filename is checked first; if it has none, the job comes from a
   * repository. If the second job does not come from a repository, -1 is returned.</li>
   * <li>If the jobs are both from a repository, the jobs' names are compared. If the first
   * job has no name and the second one does, a -1 is returned.
   * If the opposite is true, a 1 is returned.</li>
   * <li>If they both have names they are compared as strings. If the result is non-zero it is returned. Otherwise the
   * repository directories are compared using the same technique of checking empty values and then performing a string
   * comparison, returning any non-zero result.</li>
   * <li>If the names and directories are equal, the object revision strings are compared using the same technique of
   * checking empty values and then performing a string comparison, this time ultimately returning the result of the
   * string compare.</li>
   * <li>If the first job does not come from a repository and the second one does, a 1 is returned. Otherwise
   * the job names and filenames are subsequently compared using the same technique of checking empty values
   * and then performing a string comparison, ultimately returning the result of the filename string comparison.
   * </ol>
   *
   * @param j1
   *          the first job to compare
   * @param j2
   *          the second job to compare
   * @return 0 if the two jobs are equal, 1 or -1 depending on the values (see description above)
   *
   */
  public int compare( JobMeta j1, JobMeta j2 ) {
    return super.compare( j1, j2 );
  }

  /**
   * Compares this job's meta-data to the specified job's meta-data. This method simply calls compare(this, o)
   *
   * @param o the o
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
   * @param obj the obj
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
   * @param doClear Whether to clear all of the clone's data before copying from the source object
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
   * @param jobLogTable the new job log table
   */
  public void setJobLogTable( JobLogTable jobLogTable ) {
    this.jobLogTable = jobLogTable;
  }

  /**
   * Clears the different changed flags of the job.
   */
  @Override
  public void clearChanged() {
    changedEntries = false;
    changedHops = false;

    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy entry = getJobEntry( i );
      entry.setChanged( false );
    }
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops
      hi.setChanged( false );
    }
    super.clearChanged();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.changed.ChangedFlag#hasChanged()
   */
  @Override
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
   * @param databaseMeta The connection to check
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
    //Clear the embedded named clusters.  We will be repopulating from steps that used named clusters
    getNamedClusterEmbedManager().clear();

    Props props = null;
    if ( Props.isInitialized() ) {
      props = Props.getInstance();
    }

    StringBuilder retval = new StringBuilder( 500 );

    retval.append( XMLHandler.openTag( XML_TAG ) ).append( Const.CR );

    retval.append( "  " ).append( XMLHandler.addTagValue( "name", getName() ) );

    retval.append( "  " ).append( XMLHandler.addTagValue( "description", description ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "extended_description", extendedDescription ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "job_version", jobVersion ) );
    if ( jobStatus >= 0 ) {
      retval.append( "  " ).append( XMLHandler.addTagValue( "job_status", jobStatus ) );
    }

    retval.append( "  " ).append( XMLHandler.addTagValue( "directory",
        ( directory != null ? directory.getPath() : RepositoryDirectory.DIRECTORY_SEPARATOR ) ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "created_user", createdUser ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "created_date", XMLHandler.date2string( createdDate ) ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "modified_user", modifiedUser ) );
    retval.append( "  " ).append( XMLHandler.addTagValue( "modified_date", XMLHandler.date2string( modifiedDate ) ) );

    retval.append( "    " ).append( XMLHandler.openTag( XML_TAG_PARAMETERS ) ).append( Const.CR );
    String[] parameters = listParameters();
    for ( int idx = 0; idx < parameters.length; idx++ ) {
      retval.append( "      " ).append( XMLHandler.openTag( "parameter" ) ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "name", parameters[idx] ) );
      try {
        retval.append( "        " )
            .append( XMLHandler.addTagValue( "default_value", getParameterDefault( parameters[idx] ) ) );
        retval.append( "        " )
            .append( XMLHandler.addTagValue( "description", getParameterDescription( parameters[idx] ) ) );
      } catch ( UnknownParamException e ) {
        // skip the default value and/or description. This exception should never happen because we use listParameters()
        // above.
      }
      retval.append( "      " ).append( XMLHandler.closeTag( "parameter" ) ).append( Const.CR );
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
      retval.append( slaveServer.getXML() );
    }
    retval.append( "    " ).append( XMLHandler.closeTag( XML_TAG_SLAVESERVERS ) ).append( Const.CR );

    // Append the job logging information...
    //
    for ( LogTableInterface logTable : getLogTables() ) {
      retval.append( logTable.getXML() );
    }

    retval.append( "   " ).append( XMLHandler.addTagValue( "pass_batchid", batchIdPassed ) );
    retval.append( "   " ).append( XMLHandler.addTagValue( "shared_objects_file", sharedObjectsFile ) );

    retval.append( "  " ).append( XMLHandler.openTag( "entries" ) ).append( Const.CR );
    for ( int i = 0; i < nrJobEntries(); i++ ) {
      JobEntryCopy jge = getJobEntry( i );
      jge.getEntry().setRepository( repository );
      retval.append( jge.getXML() );
    }
    retval.append( "  " ).append( XMLHandler.closeTag( "entries" ) ).append( Const.CR );

    retval.append( "  " ).append( XMLHandler.openTag( "hops" ) ).append( Const.CR );
    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops
      retval.append( hi.getXML() );
    }
    retval.append( "  " ).append( XMLHandler.closeTag( "hops" ) ).append( Const.CR );

    retval.append( "  " ).append( XMLHandler.openTag( "notepads" ) ).append( Const.CR );
    for ( int i = 0; i < nrNotes(); i++ ) {
      NotePadMeta ni = getNote( i );
      retval.append( ni.getXML() );
    }
    retval.append( "  " ).append( XMLHandler.closeTag( "notepads" ) ).append( Const.CR );

    // Also store the attribute groups
    //
    retval.append( AttributesUtil.getAttributesXml( attributesMap ) );

    retval.append( XMLHandler.closeTag( XML_TAG ) ).append( Const.CR );

    return XMLFormatter.format( retval.toString() );
  }

  /**
   * Instantiates a new job meta.
   *
   * @param fname the fname
   * @param rep   the rep
   * @throws KettleXMLException the kettle xml exception
   */
  public JobMeta( String fname, Repository rep ) throws KettleXMLException {
    this( null, fname, rep, null );
  }

  /**
   * Instantiates a new job meta.
   *
   * @param fname    the fname
   * @param rep      the rep
   * @param prompter the prompter
   * @throws KettleXMLException the kettle xml exception
   */
  public JobMeta( String fname, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    this( null, fname, rep, prompter );
  }

  /**
   * Load the job from the XML file specified.
   *
   * @param fname The filename to load as a job
   * @param rep   The repository to bind againt, null if there is no repository available.
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
   * @param fname The filename to load as a job
   * @param rep   The repository to bind againt, null if there is no repository available.
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

        loadXML( jobnode, fname, rep, metaStore, false, prompter );
      } else {
        throw new KettleXMLException(
            BaseMessages.getString( PKG, "JobMeta.Exception.ErrorReadingFromXMLFile" ) + fname );
      }
    } catch ( Exception e ) {
      throw new KettleXMLException(
          BaseMessages.getString( PKG, "JobMeta.Exception.UnableToLoadJobFromXMLFile" ) + fname + "]", e );
    }
  }

  /**
   * Instantiates a new job meta.
   *
   * @param inputStream the input stream
   * @param rep         the rep
   * @param prompter    the prompter
   * @throws KettleXMLException the kettle xml exception
   */
  public JobMeta( InputStream inputStream, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    this();
    Document doc = XMLHandler.loadXMLFile( inputStream, null, false, false );
    loadXML( XMLHandler.getSubNode( doc, JobMeta.XML_TAG ), rep, prompter );
  }

  /**
   * Create a new JobMeta object by loading it from a a DOM node.
   *
   * @param jobnode  The node to load from
   * @param rep      The reference to a repository to load additional information from
   * @param prompter The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public JobMeta( Node jobnode, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    this();
    loadXML( jobnode, rep, false, prompter );
  }

  /**
   * Create a new JobMeta object by loading it from a a DOM node.
   *
   * @param jobnode                       The node to load from
   * @param rep                           The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
   * @param prompter                      The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public JobMeta( Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects, OverwritePrompter prompter )
      throws KettleXMLException {
    this();
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
   * @param fileName  the file name
   * @param transName the trans name
   * @return true, if is rep reference
   */
  public static boolean isRepReference( String fileName, String transName ) {
    return Utils.isEmpty( fileName ) && !Utils.isEmpty( transName );
  }

  /**
   * Checks if is file reference.
   *
   * @param fileName  the file name
   * @param transName the trans name
   * @return true, if is file reference
   */
  public static boolean isFileReference( String fileName, String transName ) {
    return !isRepReference( fileName, transName );
  }

  /**
   * Load xml.
   *
   * @param jobnode  the jobnode
   * @param rep      the rep
   * @param prompter the prompter
   * @throws KettleXMLException the kettle xml exception
   */
  public void loadXML( Node jobnode, Repository rep, OverwritePrompter prompter ) throws KettleXMLException {
    loadXML( jobnode, rep, false, prompter );
  }

  /**
   * Load xml.
   *
   * @param jobnode  the jobnode
   * @param fname    The filename
   * @param rep      the rep
   * @param prompter the prompter
   * @throws KettleXMLException the kettle xml exception
   */
  public void loadXML( Node jobnode, String fname, Repository rep, OverwritePrompter prompter )
      throws KettleXMLException {
    loadXML( jobnode, fname, rep, false, prompter );
  }

  /**
   * Load a block of XML from an DOM node.
   *
   * @param jobnode                       The node to load from
   * @param rep                           The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
   * @param prompter                      The prompter to use in case a shared object gets overwritten
   * @throws KettleXMLException
   */
  public void loadXML( Node jobnode, Repository rep, boolean ignoreRepositorySharedObjects, OverwritePrompter prompter )
      throws KettleXMLException {
    loadXML( jobnode, null, rep, ignoreRepositorySharedObjects, prompter );
  }

  /**
   * Load a block of XML from an DOM node.
   *
   * @param jobnode                       The node to load from
   * @param fname                         The filename
   * @param rep                           The reference to a repository to load additional information from
   * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
   * @param prompter                      The prompter to use in case a shared object gets overwritten
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
   * @param jobnode                       The node to load from
   * @param fname                         The filename
   * @param rep                           The reference to a repository to load additional information from
   * @param metaStore                     the MetaStore to use
   * @param ignoreRepositorySharedObjects Do not load shared objects, handled separately
   * @param prompter                      The prompter to use in case a shared object gets overwritten
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
      }  else {
        // Set the repository here so it can be used in variables for ALL aspects of the job FIX: PDI-16441
        setRepository( rep );
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
      createdUser = XMLHandler.getTagValue( jobnode, "created_user" );
      String createDate = XMLHandler.getTagValue( jobnode, "created_date" );

      if ( createDate != null ) {
        createdDate = XMLHandler.stringToDate( createDate );
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
        LogChannel.GENERAL
            .logError( BaseMessages.getString( PKG, "JobMeta.ErrorReadingSharedObjects.Message", e.toString() ) );
        LogChannel.GENERAL.logError( Const.getStackTracker( e ) );
      }

      // Load the database connections, slave servers, cluster schemas & partition schemas into this object.
      //
      importFromMetaStore();

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
      Set<String> privateDatabases = new HashSet<String>( nr );
      for ( int i = 0; i < nr; i++ ) {
        Node dbnode = XMLHandler.getSubNodeByNr( jobnode, "connection", i );
        DatabaseMeta dbcon = new DatabaseMeta( dbnode );
        dbcon.shareVariablesWith( this );
        if ( !dbcon.isShared() ) {
          privateDatabases.add( dbcon.getName() );
        }

        DatabaseMeta exist = findDatabase( dbcon.getName() );
        if ( exist == null ) {
          addDatabase( dbcon );
        } else {
          if ( !exist.isShared() ) {
            // skip shared connections
            if ( shouldOverwrite( prompter, props,
                BaseMessages.getString( PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.Message", dbcon.getName() ),
                BaseMessages.getString( PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ) ) ) {
              int idx = indexOfDatabase( exist );
              removeDatabase( idx );
              addDatabase( idx, dbcon );
            }
          }
        }
      }
      setPrivateDatabases( privateDatabases );

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
            if ( shouldOverwrite( prompter, props, BaseMessages
                    .getString( PKG, "JobMeta.Dialog.SlaveServerExistsOverWrite.Message", slaveServer.getName() ),
                BaseMessages.getString( PKG, "JobMeta.Dialog.ConnectionExistsOverWrite.DontShowAnyMoreMessage" ) ) ) {
              addOrReplaceSlaveServer( slaveServer );
            }
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

        if ( je.isSpecial() && je.isMissing() ) {
          addMissingEntry( (MissingEntry) je.getEntry() );
        }
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
      throw new KettleXMLException( BaseMessages.getString( PKG, "JobMeta.Exception.UnableToLoadJobFromXMLNode" ), e );
    } finally {
      setInternalKettleVariables();
    }
  }

  /**
   * Gets the job entry copy.
   *
   * @param x        the x
   * @param y        the y
   * @param iconsize the iconsize
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
   * Gets the job hop.
   *
   * @param i the i
   * @return the job hop
   */
  public JobHopMeta getJobHop( int i ) {
    return jobhops.get( i );
  }

  /**
   * Gets the job entry.
   *
   * @param i the i
   * @return the job entry
   */
  public JobEntryCopy getJobEntry( int i ) {
    return jobcopies.get( i );
  }

  /**
   * Adds the job entry.
   *
   * @param je the je
   */
  public void addJobEntry( JobEntryCopy je ) {
    jobcopies.add( je );
    je.setParentJobMeta( this );
    setChanged();
  }

  /**
   * Adds the job hop.
   *
   * @param hi the hi
   */
  public void addJobHop( JobHopMeta hi ) {
    jobhops.add( hi );
    setChanged();
  }

  /**
   * Adds the job entry.
   *
   * @param p  the p
   * @param si the si
   */
  public void addJobEntry( int p, JobEntryCopy si ) {
    jobcopies.add( p, si );
    changedEntries = true;
  }

  /**
   * Adds the job hop.
   *
   * @param p  the p
   * @param hi the hi
   */
  public void addJobHop( int p, JobHopMeta hi ) {
    try {
      jobhops.add( p, hi );
    } catch ( IndexOutOfBoundsException e ) {
      jobhops.add( hi );
    }
    changedHops = true;
  }

  /**
   * Removes the job entry.
   *
   * @param i the i
   */
  public void removeJobEntry( int i ) {
    JobEntryCopy deleted = jobcopies.remove( i );
    if ( deleted != null ) {
      // give step a chance to cleanup
      deleted.setParentJobMeta( null );
      if ( deleted.getEntry() instanceof MissingEntry ) {
        removeMissingEntry( (MissingEntry) deleted.getEntry() );
      }
    }
    setChanged();
  }

  /**
   * Removes the job hop.
   *
   * @param i the i
   */
  public void removeJobHop( int i ) {
    jobhops.remove( i );
    setChanged();
  }

  /**
   * Removes a hop from the transformation. Also marks that the
   * transformation's hops have changed.
   *
   * @param hop The hop to remove from the list of hops
   */
  public void removeJobHop( JobHopMeta hop ) {
    jobhops.remove( hop );
    setChanged();
  }

  /**
   * Index of job hop.
   *
   * @param he the he
   * @return the int
   */
  public int indexOfJobHop( JobHopMeta he ) {
    return jobhops.indexOf( he );
  }

  /**
   * Index of job entry.
   *
   * @param ge the ge
   * @return the int
   */
  public int indexOfJobEntry( JobEntryCopy ge ) {
    return jobcopies.indexOf( ge );
  }

  /**
   * Sets the job entry.
   *
   * @param idx the idx
   * @param jec the jec
   */
  public void setJobEntry( int idx, JobEntryCopy jec ) {
    jobcopies.set( idx, jec );
  }

  /**
   * Find an existing JobEntryCopy by it's name and number
   *
   * @param name The name of the job entry copy
   * @param nr   The number of the job entry copy
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
   * @param full_name_nr the full_name_nr
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
   * @param name the name
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
   * @param jge the jge
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
   * @param from the from
   * @param to   the to
   * @return the job hop meta
   */
  public JobHopMeta findJobHop( JobEntryCopy from, JobEntryCopy to ) {
    return findJobHop( from, to, false );
  }

  /**
   * Find job hop.
   *
   * @param from            the from
   * @param to              the to
   * @param includeDisabled the include disabled
   * @return the job hop meta
   */
  public JobHopMeta findJobHop( JobEntryCopy from, JobEntryCopy to, boolean includeDisabled ) {
    for ( JobHopMeta hi : jobhops ) {
      if ( hi.isEnabled() || includeDisabled ) {
        if ( hi != null && hi.getFromEntry() != null && hi.getToEntry() != null && hi.getFromEntry().equals( from )
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
   * @param jge the jge
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
   * @param from the from
   * @return the int
   */
  public int findNrPrevJobEntries( JobEntryCopy from ) {
    return findNrPrevJobEntries( from, false );
  }

  /**
   * Find prev job entry.
   *
   * @param to the to
   * @param nr the nr
   * @return the job entry copy
   */
  public JobEntryCopy findPrevJobEntry( JobEntryCopy to, int nr ) {
    return findPrevJobEntry( to, nr, false );
  }

  /**
   * Find nr prev job entries.
   *
   * @param to   the to
   * @param info the info
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
   * @param to   the to
   * @param nr   the nr
   * @param info the info
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
   * @param from the from
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
   * @param from the from
   * @param cnt  the cnt
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
   * @param entry the entry
   * @return true, if successful
   */
  public boolean hasLoop( JobEntryCopy entry ) {
    clearLoopCache();
    return hasLoop( entry, null );
  }

  /**
   * @deprecated use {@link #hasLoop(JobEntryCopy, JobEntryCopy)}}
   */
  @Deprecated
  public boolean hasLoop( JobEntryCopy entry, JobEntryCopy lookup, boolean info ) {
    return hasLoop( entry, lookup );
  }
  /**
   * Checks for loop.
   *
   * @param entry  the entry
   * @param lookup the lookup
   * @return true, if successful
   */

  public boolean hasLoop( JobEntryCopy entry, JobEntryCopy lookup ) {
    return hasLoop( entry, lookup, new HashSet<JobEntryCopy>() );
  }

  /**
   * Checks for loop.
   *
   * @param entry  the entry
   * @param lookup the lookup
   * @return true, if successful
   */
  private boolean hasLoop( JobEntryCopy entry, JobEntryCopy lookup, HashSet<JobEntryCopy> checkedEntries ) {
    String cacheKey =
        entry.getName() + " - " + ( lookup != null ? lookup.getName() : "" );

    Boolean hasLoop = loopCache.get( cacheKey );

    if ( hasLoop != null ) {
      return hasLoop;
    }

    hasLoop = false;

    checkedEntries.add( entry );

    int nr = findNrPrevJobEntries( entry );
    for ( int i = 0; i < nr; i++ ) {
      JobEntryCopy prevJobMeta = findPrevJobEntry( entry, i );
      if ( prevJobMeta != null && ( prevJobMeta.equals( lookup )
              || ( !checkedEntries.contains( prevJobMeta ) && hasLoop( prevJobMeta, lookup == null ? entry : lookup, checkedEntries ) ) ) ) {
        hasLoop = true;
        break;
      }
    }

    loopCache.put( cacheKey, hasLoop );
    return hasLoop;
  }

  /**
   * Clears the loop cache.
   */
  private void clearLoopCache() {
    loopCache.clear();
  }

  /**
   * Checks if is entry used in hops.
   *
   * @param jge the jge
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
   * @param name the name
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
   * @param name the name
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
   * @param name the name
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
   * @param entryname The job entry name to find an alternative for..
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
   * @param name the name
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
   * @param name the name
   * @return the all job hops using
   */
  public JobHopMeta[] getAllJobHopsUsing( String name ) {
    List<JobHopMeta> hops = new ArrayList<JobHopMeta>();

    for ( JobHopMeta hi : jobhops ) {
      // Look at all the hops

      if ( hi.getFromEntry() != null && hi.getToEntry() != null ) {
        if ( hi.getFromEntry().getName().equalsIgnoreCase( name ) || hi.getToEntry().getName()
            .equalsIgnoreCase( name ) ) {
          hops.add( hi );
        }
      }
    }
    return hops.toArray( new JobHopMeta[hops.size()] );
  }

  public boolean isPathExist( JobEntryInterface from, JobEntryInterface to ) {
    for ( JobHopMeta hi : jobhops ) {
      if ( hi.getFromEntry() != null && hi.getToEntry() != null ) {
        if ( hi.getFromEntry().getName().equalsIgnoreCase( from.getName() ) ) {
          if ( hi.getToEntry().getName().equalsIgnoreCase( to.getName() ) ) {
            return true;
          }
          if ( isPathExist( hi.getToEntry().getEntry(), to ) ) {
            return true;
          }
        }
      }
    }

    return false;
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
   * Gets the entry indexes.
   *
   * @param entries the entries
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
   * @param batchIdPassed The batchIdPassed to set.
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
      monitor
          .beginTask( BaseMessages.getString( PKG, "JobMeta.Monitor.GettingSQLNeededForThisJob" ), nrJobEntries() + 1 );
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
    if ( jobLogTable.getDatabaseMeta() != null && !Utils.isEmpty( jobLogTable.getTableName() ) ) {
      Database db = new Database( this, jobLogTable.getDatabaseMeta() );
      try {
        db.connect();
        RowMetaInterface fields = jobLogTable.getLogRecord( LogStatus.START, null, null ).getRowMeta();
        String sql = db.getDDL( jobLogTable.getTableName(), fields );
        if ( sql != null && sql.length() > 0 ) {
          SQLStatement stat = new SQLStatement( BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ThisJob" ),
              jobLogTable.getDatabaseMeta(), sql );
          stats.add( stat );
        }
      } catch ( KettleDatabaseException dbe ) {
        SQLStatement stat = new SQLStatement( BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ThisJob" ),
            jobLogTable.getDatabaseMeta(), null );
        stat.setError(
            BaseMessages.getString( PKG, "JobMeta.SQLFeedback.ErrorObtainingJobLogTableInfo" ) + dbe.getMessage() );
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
   * @param arguments The arguments to set.
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
        stringList.add( new StringSearchResult( entryMeta.getName(), entryMeta, this,
            BaseMessages.getString( PKG, "JobMeta.SearchMetadata.JobEntryName" ) ) );
        if ( entryMeta.getDescription() != null ) {
          stringList.add( new StringSearchResult( entryMeta.getDescription(), entryMeta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.JobEntryDescription" ) ) );
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
        stringList.add( new StringSearchResult( meta.getName(), meta, this,
            BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseConnectionName" ) ) );
        if ( meta.getHostname() != null ) {
          stringList.add( new StringSearchResult( meta.getHostname(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseHostName" ) ) );
        }
        if ( meta.getDatabaseName() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabaseName(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseName" ) ) );
        }
        if ( meta.getUsername() != null ) {
          stringList.add( new StringSearchResult( meta.getUsername(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseUsername" ) ) );
        }
        if ( meta.getPluginId() != null ) {
          stringList.add( new StringSearchResult( meta.getPluginId(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseTypeDescription" ) ) );
        }
        if ( meta.getDatabasePortNumberString() != null ) {
          stringList.add( new StringSearchResult( meta.getDatabasePortNumberString(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabasePort" ) ) );
        }
        if ( meta.getServername() != null ) {
          stringList.add( new StringSearchResult( meta.getServername(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabaseServer" ) ) );
        }
        // if ( includePasswords )
        // {
        if ( meta.getPassword() != null ) {
          stringList.add( new StringSearchResult( meta.getPassword(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.DatabasePassword" ) ) );
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
          stringList.add( new StringSearchResult( meta.getNote(), meta, this,
              BaseMessages.getString( PKG, "JobMeta.SearchMetadata.NotepadText" ) ) );
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
   * Set the version of the job.
   *
   * @param jobVersion The new version description of the job
   */
  public void setJobversion( String jobVersion ) {
    this.jobVersion = jobVersion;
  }

  /**
   * Set the status of the job.
   *
   * @param jobStatus The new status description of the job
   */
  public void setJobstatus( int jobStatus ) {
    this.jobStatus = jobStatus;
  }

  /**
   * Find a jobentry with a certain ID in a list of job entries.
   *
   * @param jobentries  The List of jobentries
   * @param id_jobentry The id of the jobentry
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
   * @param jobcopies        The List of jobentry copies
   * @param id_jobentry_copy The id of the jobentry copy
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
   * This method sets various internal kettle variables that can be used by the transformation.
   */
  @Override
  public void setInternalKettleVariables( VariableSpace var ) {
    setInternalFilenameKettleVariables( var );
    setInternalNameKettleVariable( var );

    // The name of the directory in the repository
    variables
        .setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY, directory != null ? directory.getPath() : "" );

    boolean hasRepoDir = getRepositoryDirectory() != null && getRepository() != null;

    // setup fallbacks
    if ( hasRepoDir ) {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY,
          variables.getVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY ) );
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY,
          variables.getVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY ) );
    }
    updateCurrentDir();
  }

  private void updateCurrentDir() {
    String prevCurrentDir = variables.getVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY );
    String currentDir = variables.getVariable(
      repository != null
          ? Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY
          : Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY );
    variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, currentDir );
    fireCurrentDirectoryChanged( prevCurrentDir, currentDir );
  }

  /**
   * Sets the internal name kettle variable.
   *
   * @param var the new internal name kettle variable
   */
  @Override
  protected void setInternalNameKettleVariable( VariableSpace var ) {
    // The name of the job
    variables.setVariable( Const.INTERNAL_VARIABLE_JOB_NAME, Const.NVL( name, "" ) );
  }

  /**
   * Sets the internal filename kettle variables.
   *
   * @param var the new internal filename kettle variables
   */
  @Override
  protected void setInternalFilenameKettleVariables( VariableSpace var ) {
    if ( filename != null ) {
      // we have a filename that's defined.
      try {
        FileObject fileObject = KettleVFS.getFileObject( filename, var );
        FileName fileName = fileObject.getName();

        // The filename of the job
        variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, fileName.getBaseName() );

        // The directory of the job
        FileName fileDir = fileName.getParent();
        variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, fileDir.getURI() );
      } catch ( Exception e ) {
        variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "" );
        variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "" );
      }
    } else {
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY, "" );
      variables.setVariable( Const.INTERNAL_VARIABLE_JOB_FILENAME_NAME, "" );
    }

    variables.setVariable( Const.INTERNAL_VARIABLE_ENTRY_CURRENT_DIRECTORY, variables.getVariable(
        repository != null ? Const.INTERNAL_VARIABLE_JOB_REPOSITORY_DIRECTORY
            : Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY ) );
  }

  @Deprecated
  public void checkJobEntries( List<CheckResultInterface> remarks, boolean only_selected,
      ProgressMonitorListener monitor ) {
    checkJobEntries( remarks, only_selected, monitor, this, null, null );
  }

  /**
   * Check all job entries within the job. Each Job Entry has the opportunity to check their own settings.
   *
   * @param remarks       List of CheckResult remarks inserted into by each JobEntry
   * @param only_selected true if you only want to check the selected jobs
   * @param monitor       Progress monitor (not presently in use)
   */
  public void checkJobEntries( List<CheckResultInterface> remarks, boolean only_selected,
      ProgressMonitorListener monitor, VariableSpace space, Repository repository, IMetaStore metaStore ) {
    remarks.clear(); // Empty remarks
    if ( monitor != null ) {
      monitor.beginTask( BaseMessages.getString( PKG, "JobMeta.Monitor.VerifyingThisJobEntryTask.Title" ),
          jobcopies.size() + 2 );
    }
    boolean stop_checking = false;
    for ( int i = 0; i < jobcopies.size() && !stop_checking; i++ ) {
      JobEntryCopy copy = jobcopies.get( i ); // get the job entry copy
      if ( ( !only_selected ) || ( only_selected && copy.isSelected() ) ) {
        JobEntryInterface entry = copy.getEntry();
        if ( entry != null ) {
          if ( monitor != null ) {
            monitor
                .subTask( BaseMessages.getString( PKG, "JobMeta.Monitor.VerifyingJobEntry.Title", entry.getName() ) );
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
      ResourceNamingInterface namingInterface, Repository repository, IMetaStore metaStore ) throws KettleException {
    String resourceName = null;
    try {
      // Handle naming for both repository and XML bases resources...
      //
      String baseName;
      String originalPath;
      String fullname;
      String extension = "kjb";
      if ( Utils.isEmpty( getFilename() ) ) {
        // Assume repository...
        //
        originalPath = directory.getPath();
        baseName = getName();
        fullname =
            directory.getPath() + ( directory.getPath().endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR ) ? ""
                : RepositoryDirectory.DIRECTORY_SEPARATOR ) + getName() + "." + extension; //
      } else {
        // Assume file
        //
        FileObject fileObject = KettleVFS.getFileObject( space.environmentSubstitute( getFilename() ), space );
        originalPath = fileObject.getParent().getName().getPath();
        baseName = fileObject.getName().getBaseName();
        fullname = fileObject.getName().getPath();
      }

      resourceName = namingInterface
          .nameResource( baseName, originalPath, extension, ResourceNamingInterface.FileNamingType.JOB );
      ResourceDefinition definition = definitions.get( resourceName );
      if ( definition == null ) {
        // If we do this once, it will be plenty :-)
        //
        JobMeta jobMeta = (JobMeta) this.realClone( false );

        // All objects get re-located to the root folder,
        // but, when exporting, we need to see current directory
        // in order to make 'Internal.Entry.Current.Directory' variable work
        jobMeta.setRepositoryDirectory( directory );

        // Add used resources, modify transMeta accordingly
        // Go through the list of steps, etc.
        // These critters change the steps in the cloned TransMeta
        // At the end we make a new XML version of it in "exported"
        // format...

        // loop over steps, databases will be exported to XML anyway.
        //
        for ( JobEntryCopy jobEntry : jobMeta.jobcopies ) {
          compatibleJobEntryExportResources( jobEntry.getEntry(), jobMeta, definitions, namingInterface, repository );
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
        if ( Utils.isEmpty( this.getFilename() ) ) { // Repository
          definition.setOrigin( fullname );
        } else {
          definition.setOrigin( this.getFilename() );
        }

        definitions.put( fullname, definition );
      }
    } catch ( FileSystemException e ) {
      throw new KettleException(
          BaseMessages.getString( PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename() ), e );
    } catch ( KettleFileException e ) {
      throw new KettleException(
          BaseMessages.getString( PKG, "JobMeta.Exception.AnErrorOccuredReadingJob", getFilename() ), e );
    }

    return resourceName;
  }

  @SuppressWarnings( "deprecation" )
  private void compatibleJobEntryExportResources( JobEntryInterface entry, JobMeta jobMeta,
      Map<String, ResourceDefinition> definitions, ResourceNamingInterface namingInterface, Repository repository2 )
      throws KettleException {
    entry.exportResources( jobMeta, definitions, namingInterface, repository );
  }

  /**
   * See if the name of the supplied job entry copy doesn't collide with any other job entry copy in the job.
   *
   * @param je The job entry copy to verify the name for.
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
   * Gets the job copies.
   *
   * @return the job copies
   */
  public List<JobEntryCopy> getJobCopies() {
    return jobcopies;
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

  /**
   * Gets the log channel.
   *
   * @return the log channel
   */
  public LogChannelInterface getLogChannel() {
    return log;
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
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectType()
   */
  public LoggingObjectType getObjectType() {
    return LoggingObjectType.JOBMETA;
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
   * @param jobEntryLogTable the jobEntryLogTable to set
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
   * @param repository the repository to reference.
   */
  public void lookupRepositoryReferences( Repository repository ) throws KettleException {
    KettleException lastThrownException = null;
    Map<String, RepositoryObjectType> notFoundedReferences = new HashMap<>();
    for ( JobEntryCopy copy : jobcopies ) {
      if ( copy.getEntry().hasRepositoryReferences() ) {
        try {
          copy.getEntry().lookupRepositoryReferences( repository );
        } catch ( IdNotFoundException e ) {
          lastThrownException = e;
          String path = e.getPathToObject();
          String name = e.getObjectName();
          String key = StringUtils.isEmpty( path ) || path.equals( "null" ) ? name : path + "/" + name;
          notFoundedReferences.put( key, e.getObjectType() );
        }
      }
    }
    if ( lastThrownException != null && !notFoundedReferences.isEmpty() ) {
      throw new LookupReferencesException( lastThrownException, notFoundedReferences );
    }
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

  public List<LogTableInterface> getExtraLogTables() {
    return extraLogTables;
  }

  public void setExtraLogTables( List<LogTableInterface> extraLogTables ) {
    this.extraLogTables = extraLogTables;
  }

  public boolean containsJobCopy( JobEntryCopy jobCopy ) {
    return jobcopies.contains( jobCopy );
  }

  public List<MissingEntry> getMissingEntries() {
    return missingEntries;
  }

  public void addMissingEntry( MissingEntry missingEntry ) {
    if ( missingEntries == null ) {
      missingEntries = new ArrayList<MissingEntry>();
    }
    missingEntries.add( missingEntry );
  }

  public void removeMissingEntry( MissingEntry missingEntry ) {
    if ( missingEntries != null && missingEntry != null && missingEntries.contains( missingEntry ) ) {
      missingEntries.remove( missingEntry );
    }
  }

  public boolean hasMissingPlugins() {
    return missingEntries != null && !missingEntries.isEmpty();
  }

  @Override
  public NamedClusterEmbedManager getNamedClusterEmbedManager( ) {
    if ( namedClusterEmbedManager == null ) {
      namedClusterEmbedManager = new NamedClusterEmbedManager( this, LogChannel.GENERAL );
    }
    return namedClusterEmbedManager;
  }

  public String getStartCopyName() {
    return startCopyName;
  }

  public void setStartCopyName( String startCopyName ) {
    this.startCopyName = startCopyName;
  }

  public boolean isExpandingRemoteJob() {
    return expandingRemoteJob;
  }

  public void setExpandingRemoteJob( boolean expandingRemoteJob ) {
    this.expandingRemoteJob = expandingRemoteJob;
  }
}
