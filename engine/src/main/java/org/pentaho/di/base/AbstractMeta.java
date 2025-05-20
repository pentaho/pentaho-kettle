/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.cluster.SlaveServerManagementInterface;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.bowl.HasBowlInterface;
import org.pentaho.di.core.changed.ChangedFlag;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.changed.PDIObserver;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.gui.OverwritePrompter;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.listeners.ContentChangedListener;
import org.pentaho.di.core.listeners.CurrentDirectoryChangedListener;
import org.pentaho.di.core.listeners.FilenameChangedListener;
import org.pentaho.di.core.listeners.NameChangedListener;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.osgi.api.NamedClusterServiceOsgi;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.undo.TransAction;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.metastore.DatabaseMetaStoreUtil;
import org.pentaho.di.repository.HasRepositoryInterface;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.shared.ChangeTrackingDatabaseManager;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.shared.DelegatingSharedObjectsIO;
import org.pentaho.di.shared.MemorySharedObjectsIO;
import org.pentaho.di.shared.PassthroughDbConnectionManager;
import org.pentaho.di.shared.PassthroughSlaveServerManager;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.shared.SharedObjectsIO;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.HasSlaveServersInterface;
import org.pentaho.di.trans.steps.named.cluster.NamedClusterEmbedManager;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.IMetaStoreElement;
import org.pentaho.metastore.api.IMetaStoreElementType;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.util.PentahoDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractMeta implements ChangedFlagInterface, UndoInterface, HasDatabasesInterface, VariableSpace,
  EngineMetaInterface, NamedParams, HasSlaveServersInterface, AttributesInterface, HasRepositoryInterface,
  HasBowlInterface, LoggingObjectInterface {

  /**
   * Constant = 1
   **/
  public static final int TYPE_UNDO_CHANGE = 1;

  /**
   * Constant = 2
   **/
  public static final int TYPE_UNDO_NEW = 2;

  /**
   * Constant = 3
   **/
  public static final int TYPE_UNDO_DELETE = 3;

  /**
   * Constant = 4
   **/
  public static final int TYPE_UNDO_POSITION = 4;

  protected ObjectId objectId;

  protected ObjectRevision objectRevision;

  protected String containerObjectId;

  protected String name;

  protected String description;

  protected String extendedDescription;

  protected String filename;

  protected RepositoryDirectoryInterface directory;

  protected Bowl bowl = DefaultBowl.getInstance();

  /**
   * The repository to reference in the one-off case that it is needed
   */
  protected Repository repository;

  protected Set<NameChangedListener> nameChangedListeners = Collections.newSetFromMap( new ConcurrentHashMap<NameChangedListener, Boolean>() );

  protected Set<FilenameChangedListener> filenameChangedListeners = Collections.newSetFromMap( new ConcurrentHashMap<FilenameChangedListener, Boolean>() );

  protected Set<ContentChangedListener> contentChangedListeners = Collections.newSetFromMap( new ConcurrentHashMap<ContentChangedListener, Boolean>() );

  protected Set<CurrentDirectoryChangedListener> currentDirectoryChangedListeners = Collections.newSetFromMap( new ConcurrentHashMap<CurrentDirectoryChangedListener, Boolean>() );

  protected List<NotePadMeta> notes;

  protected ChannelLogTable channelLogTable;

  protected boolean changedNotes;

  protected List<TransAction> undo;

  protected Map<String, Map<String, String>> attributesMap;

  protected EmbeddedMetaStore embeddedMetaStore = new EmbeddedMetaStore( this );

  protected VariableSpace variables = new Variables();

  protected NamedParams namedParams = new NamedParamsDefault();

  protected LogLevel logLevel = DefaultLogLevel.getLogLevel();

  protected IMetaStore metaStore;

  protected String createdUser, modifiedUser;

  protected Date createdDate, modifiedDate;

  protected NamedClusterServiceOsgi namedClusterServiceOsgi;

  protected MetastoreLocatorOsgi metastoreLocatorOsgi;

  @VisibleForTesting
  protected NamedClusterEmbedManager namedClusterEmbedManager;

  @SuppressWarnings( "java:S4738" )  // using guava for memoize
  // memoized, load-once-on-demand supplier for the embedded provider key.
  private Supplier<String> embeddedMetastoreProvKeySupplier = Suppliers.memoize( this::getEmbeddedMetastoreKey );

  // This is used as default directory for new jobs/transformation while saving the file
  private String defaultSaveDirectory;

  private String getEmbeddedMetastoreKey() {
    if ( getMetastoreLocatorOsgi() != null ) {
      return getMetastoreLocatorOsgi().setEmbeddedMetastore( getEmbeddedMetaStore() );
    }
    return null;
  }

  /**
   * If this is null, we load from the default shared objects file : $KETTLE_HOME/.kettle/shared.xml
   */
  protected String sharedObjectsFile;

  /**
   * The last loaded version of the shared objects
   */
  protected SharedObjects sharedObjects;

  protected final ChangedFlag changedFlag = new ChangedFlag();

  protected int max_undo;

  protected int undo_position;

  protected RunOptions runOptions = new RunOptions();

  private boolean showDialog = true;
  private boolean alwaysShowRunOptions = true;

  private Boolean versioningEnabled;

  //Caches JobMeta and TransMeta that were previously loaded during run.
  private IMetaFileCache metaFileCache;

  public boolean isShowDialog() {
    return showDialog;
  }

  public void setShowDialog( boolean showDialog ) {
    this.showDialog = showDialog;
  }

  public boolean isAlwaysShowRunOptions() {
    return alwaysShowRunOptions;
  }

  public void setAlwaysShowRunOptions( boolean alwaysShowRunOptions ) {
    this.alwaysShowRunOptions = alwaysShowRunOptions;
  }

  protected MemorySharedObjectsIO localSharedObjects = new MemorySharedObjectsIO();
  // important, these need to be updated if the bowl is changed.
  protected SharedObjectsIO combinedSharedObjects =
      new DelegatingSharedObjectsIO( bowl.getSharedObjectsIO(), localSharedObjects );
  protected DatabaseManagementInterface readDbManager = new PassthroughDbConnectionManager( combinedSharedObjects );

  protected SlaveServerManagementInterface readSlaveServerManager =
      new PassthroughSlaveServerManager( combinedSharedObjects );

  protected void initializeSharedObjects() {
    // NOTE: this has to assign new objects, not just clear existing ones, because it is used in clone(), and
    // updating the original objects will update the source of the clone.
    localSharedObjects = new MemorySharedObjectsIO();
    initializeNonLocalSharedObjects();
  }

  protected void initializeNonLocalSharedObjects() {
    combinedSharedObjects =
      new DelegatingSharedObjectsIO( bowl.getSharedObjectsIO(), localSharedObjects );

    readDbManager = new PassthroughDbConnectionManager( combinedSharedObjects );
    localDbMgr = new ChangeTrackingDatabaseManager( new PassthroughDbConnectionManager( localSharedObjects ) );
    
    readSlaveServerManager = new PassthroughSlaveServerManager( combinedSharedObjects );
    localSlaveServerMgr = new PassthroughSlaveServerManager( localSharedObjects );
  }

  protected ChangeTrackingDatabaseManager localDbMgr =
    new ChangeTrackingDatabaseManager( new PassthroughDbConnectionManager( localSharedObjects ) );
  protected SlaveServerManagementInterface localSlaveServerMgr = new PassthroughSlaveServerManager( localSharedObjects );

  @Override
  public ObjectId getObjectId() {
    return objectId;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.EngineMetaInterface#setObjectId(org.pentaho.di.repository.ObjectId)
   */
  @Override
  public void setObjectId( ObjectId objectId ) {
    this.objectId = objectId;
  }

  /**
   * Gets the container object id.
   *
   * @return the carteObjectId
   */
  @Override
  public String getContainerObjectId() {
    return containerObjectId;
  }

  /**
   * Sets the carte object id.
   *
   * @param containerObjectId the execution container Object id to set
   */
  public void setCarteObjectId( String containerObjectId ) {
    this.containerObjectId = containerObjectId;
  }

  /**
   * Get the name of the transformation.
   *
   * @return The name of the transformation
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Set the name.
   *
   * @param newName The new name
   */
  @Override
  public void setName( String newName ) {
    fireNameChangedListeners( this.name, newName );
    this.name = newName;
    setInternalNameKettleVariable( variables );
  }

  /**
   * Gets the description of the job.
   *
   * @return The description of the job
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Set the description of the job.
   *
   * @param description The new description of the job
   */
  @Override
  public void setDescription( String description ) {
    this.description = description;
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
   * Set the description of the job.
   *
   * @param extendedDescription The new extended description of the job
   */
  public void setExtendedDescription( String extendedDescription ) {
    this.extendedDescription = extendedDescription;
  }

  /**
   * Builds a name - if no name is set, yet - from the filename
   */
  @Override
  public void nameFromFilename() {
    if ( !Utils.isEmpty( filename ) ) {
      setName( Const.createName( filename ) );
    }
  }

  /**
   * Gets the filename.
   *
   * @return filename
   * @see org.pentaho.di.core.EngineMetaInterface#getFilename()
   */
  @Override
  public String getFilename() {
    return filename;
  }

  /**
   * Set the filename of the job
   *
   * @param newFilename The new filename of the job
   */
  @Override
  public void setFilename( String newFilename ) {
    fireFilenameChangedListeners( this.filename, newFilename );
    this.filename = newFilename;
    setInternalFilenameKettleVariables( variables );
  }

  /**
   * Gets the directory.
   *
   * @return Returns the directory.
   */
  @Override
  public RepositoryDirectoryInterface getRepositoryDirectory() {
    return directory;
  }

  /**
   * Sets the directory.
   *
   * @param directory The directory to set.
   */
  @Override
  public void setRepositoryDirectory( RepositoryDirectoryInterface directory ) {
    this.directory = directory;
    setInternalKettleVariables();
  }

  /**
   * Retrieves the Bowl for the execution context. This Bowl should not be used for write operations.
   *
   * @return Bowl The Bowl that should be used during execution.
   */
  public Bowl getBowl() {
    return bowl;
  }

  /**
   * Set the Bowl for the execution context. This Bowl should not be used for write operations.
   *
   */
  public void setBowl( Bowl bowl ) {
    this.bowl = Objects.requireNonNull( bowl );
    initializeNonLocalSharedObjects();
    // now that the bowl has changed, make sure we have up-to-date DatabaseMetas.
    allDatabasesUpdated();
  }

  /**
   * Gets the repository.
   *
   * @return the repository
   */
  @Override
  public Repository getRepository() {
    return repository;
  }

  /**
   * Sets the repository.
   *
   * @param repository the repository to set
   */
  @Override
  public void setRepository( Repository repository ) {
    if ( !Objects.equals( this.repository, repository ) ) {
      // TODO BACKLOG-41158  When we implement execution from repository with projects, revisit this.
      this.repository = repository;
      if ( repository != null ) {
        setBowl( repository.getBowl() );
      } else {
        setBowl( DefaultBowl.getInstance() );
      }
    }
  }

  /**
   * Calls setInternalKettleVariables on the default object.
   */
  @Override
  public void setInternalKettleVariables() {
    setInternalKettleVariables( variables );
  }

  /**
   * This method sets various internal kettle variables.
   */
  public abstract void setInternalKettleVariables( VariableSpace var );

  /**
   * Sets the internal filename kettle variables.
   *
   * @param var the new internal filename kettle variables
   */
  protected abstract void setInternalFilenameKettleVariables( VariableSpace var );

  /**
   * Return a list of the names of all the variables used by this meta or its contents
   *
   *
   * @return List&lt;String&gt; the names of used variables
   */
  public abstract List<String> getUsedVariables();

  /**
   * Find a database connection by it's name
   *
   * @param name The database name to look for
   * @return The database connection or null if nothing was found.
   */
  @Override
  public DatabaseMeta findDatabase( String name ) {
    try {
      List<DatabaseMeta> databases = readDbManager.getAll();
      return findMatchingDb( databases, name );
    } catch ( KettleException ex ) {
      return null;
    }
  }

  protected DatabaseMeta findMatchingDb( List<DatabaseMeta> databases, String name ) {
    for ( DatabaseMeta db : databases) {
      if ( ( db != null ) && ( db.getName().equalsIgnoreCase( name ) )
           || ( db.getDisplayName().equalsIgnoreCase( name ) ) ) {
        return db;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#nrDatabases()
   */
  @Override
  public int nrDatabases() {
    try {
      return readDbManager.getAll().size();
    } catch ( KettleException ex ) {
      return 0;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#getDatabase(int)
   */
  @Override
  public DatabaseMeta getDatabase( int i ) {
    return getDatabases().get( i );
  }

  public abstract Set<String> getUsedDatabaseConnectionNames();

  public void importFromMetaStore() throws MetaStoreException, KettlePluginException {
    // Read the databases...
    //
    try {
      if ( metaStore != null ) {
        IMetaStoreElementType databaseType =
          metaStore.getElementTypeByName(
            PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
        if ( databaseType != null ) {
          List<IMetaStoreElement> databaseElements = metaStore.getElements( PentahoDefaults.NAMESPACE, databaseType );
          for ( IMetaStoreElement databaseElement : databaseElements ) {
            DatabaseMeta imported = DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement(
              metaStore, databaseElement );
            if ( localDbMgr.get( imported.getName() ) == null ) {
              localDbMgr.add( imported );
            }
          }
        }

        // TODO: do the same for slaves, clusters, partition schemas
      }
    } catch ( KettleException ex ) {
      throw new KettlePluginException( ex );
    }
  }

  /**
   * Adds the name changed listener.
   *
   * @param listener the listener
   */
  public void addNameChangedListener( NameChangedListener listener ) {
    if ( listener != null ) {
      nameChangedListeners.add( listener );
    }
  }

  /**
   * Removes the name changed listener.
   *
   * @param listener the listener
   */
  public void removeNameChangedListener( NameChangedListener listener ) {
    if ( listener != null ) {
      nameChangedListeners.remove( listener );
    }
  }

  /**
   * Removes all the name changed listeners
   */
  public void clearNameChangedListeners() {
    nameChangedListeners.clear();
  }

  /**
   * Fire name changed listeners.
   *
   * @param oldName the old name
   * @param newName the new name
   */
  protected void fireNameChangedListeners( String oldName, String newName ) {
    if ( nameChanged( oldName, newName ) ) {
      for ( NameChangedListener listener : nameChangedListeners ) {
        listener.nameChanged( this, oldName, newName );
      }
    }
  }

  /**
   * Adds the filename changed listener.
   *
   * @param listener the listener
   */
  public void addFilenameChangedListener( FilenameChangedListener listener ) {
    if ( listener != null ) {
      filenameChangedListeners.add( listener );
    }
  }

  /**
   * Removes the filename changed listener.
   *
   * @param listener the listener
   */
  public void removeFilenameChangedListener( FilenameChangedListener listener ) {
    if ( listener != null ) {
      filenameChangedListeners.remove( listener );
    }
  }

  /**
   * Fire filename changed listeners.
   *
   * @param oldFilename the old filename
   * @param newFilename the new filename
   */
  protected void fireFilenameChangedListeners( String oldFilename, String newFilename ) {
    if ( nameChanged( oldFilename, newFilename ) ) {
      for ( FilenameChangedListener listener : filenameChangedListeners ) {
        listener.filenameChanged( this, oldFilename, newFilename );
      }
    }
  }

  /**
   * Adds the passed ContentChangedListener to the list of listeners.
   *
   * @param listener
   */
  public void addContentChangedListener( ContentChangedListener listener ) {
    if ( listener != null ) {
      contentChangedListeners.add( listener );
    }
  }

  /**
   * Removes the passed ContentChangedListener from the list of listeners.
   *
   * @param listener
   */
  public void removeContentChangedListener( ContentChangedListener listener ) {
    if ( listener != null ) {
      contentChangedListeners.remove( listener );
    }
  }

  public List<ContentChangedListener> getContentChangedListeners() {
    return ImmutableList.copyOf( contentChangedListeners );
  }

  /**
   * Fire content changed listeners.
   */
  protected void fireContentChangedListeners() {
    fireContentChangedListeners( true );
  }

  protected void fireContentChangedListeners( boolean ch ) {
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

  /**
   * Remove listener
   */
  public void addCurrentDirectoryChangedListener( CurrentDirectoryChangedListener listener ) {
    if ( listener != null && !currentDirectoryChangedListeners.contains( listener ) ) {
      currentDirectoryChangedListeners.add( listener );
    }
  }

  /**
   * Add a listener to be notified of design-time changes to current directory variable
   */
  public void removeCurrentDirectoryChangedListener( CurrentDirectoryChangedListener listener ) {
    if ( listener != null ) {
      currentDirectoryChangedListeners.remove( listener );
    }
  }

  /*
   * Remove all listeners; to be used during imports and other times when the directory property is changed but
   * we do not want to trigger the steps to update.
   */
  public void clearCurrentDirectoryChangedListeners() {
    if ( currentDirectoryChangedListeners != null ) {
      currentDirectoryChangedListeners.clear();
    }
  }

  /**
   * Notify listeners of a change in current directory.
   */
  protected void fireCurrentDirectoryChanged( String previous, String current ) {
    if ( nameChanged( previous, current ) ) {
      for ( CurrentDirectoryChangedListener listener : currentDirectoryChangedListeners ) {
        listener.directoryChanged( this, previous, current );
      }
    }
  }

  /**
   * Add a new slave server to the transformation if that didn't exist yet. Otherwise, replace it.
   *
   * @param slaveServer The slave server to be added.
   */
  public void addOrReplaceSlaveServer( SlaveServer slaveServer ) {
    try {
      localSlaveServerMgr.add( slaveServer );
    } catch ( KettleException exception ) {
      LogChannel.GENERAL.logBasic( exception.getMessage(), exception );
    }
    setChanged();
  }

  /**
   * Gets a list of slave servers.
   *
   * @return the slaveServer list
   */
  @Override
  public List<SlaveServer> getSlaveServers() {
    try {
      List<SlaveServer> slaveServers = readSlaveServerManager.getAll();
      Collections.sort( slaveServers, SlaveServer.COMPARATOR );
      return slaveServers;
    } catch ( KettleException exception ) {
      LogChannel.GENERAL.logError( exception.getMessage(), exception );
      return null;
    }
  }

  /**
   * Sets the slave servers.
   *
   * @param slaveServers the slaveServers to set
   */
  public void setSlaveServers( List<SlaveServer> slaveServers ) {
    try {
      localSlaveServerMgr.clear();
      for ( SlaveServer slaveServer : slaveServers ) {
        localSlaveServerMgr.add( slaveServer );
      }
    } catch ( KettleException exception ) {
      LogChannel.GENERAL.logError( exception.getMessage(), exception );
    }
  }

  /**
   * Find a slave server using the name
   *
   * @param serverString the name of the slave server
   * @return the slave server or null if we couldn't spot an approriate entry.
   */
  public SlaveServer findSlaveServer( String serverString ) {
    try {
      List<SlaveServer> slaveServers = readSlaveServerManager.getAll();
      for ( SlaveServer slaveServer : slaveServers ) {
        if ( ( slaveServer != null ) && ( slaveServer.getName() != null && slaveServer.getName().equalsIgnoreCase( serverString ) ) ) {
          return slaveServer;
        }
      }
      return null;
    } catch ( KettleException ex ) {
      return null;
    }
  }

  /**
   * Gets an array of slave server names.
   *
   * @return An array list slave server names
   */
  public String[] getSlaveServerNames() {
    try {
      List<SlaveServer> slaveServers = readSlaveServerManager.getAll();
      Collections.sort( slaveServers, SlaveServer.COMPARATOR );
      return slaveServers.stream().map( SlaveServer::getName ).toArray( String[]::new );
    } catch ( KettleException ex ) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#addUndo(java.lang.Object[], java.lang.Object[], int[],
   * org.pentaho.di.core.gui.Point[], org.pentaho.di.core.gui.Point[], int, boolean)
   */
  @Override
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
   * @see org.pentaho.di.core.gui.UndoInterface#nextUndo()
   */
  @Override
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
  @Override
  public TransAction viewNextUndo() {
    int size = undo.size();
    if ( size == 0 || undo_position >= size - 1 ) {
      return null; // no redo left...
    }

    TransAction retval = undo.get( undo_position + 1 );

    return retval;
  }

  // get previous undo, change position
  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#previousUndo()
   */
  @Override
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
  @Override
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
  @Override
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
   * @see org.pentaho.di.core.gui.UndoInterface#getMaxUndo()
   */
  @Override
  public int getMaxUndo() {
    return max_undo;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.gui.UndoInterface#setMaxUndo(int)
   */
  @Override
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

  public EmbeddedMetaStore getEmbeddedMetaStore() {
    return embeddedMetaStore;
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

  /**
   * Add a new note at a certain location (i.e. the specified index). Also marks that the notes have changed.
   *
   * @param p  The index into the notes list
   * @param ni The note to be added.
   */
  public void addNote( int p, NotePadMeta ni ) {
    notes.add( p, ni );
    changedNotes = true;
  }

  /**
   * Add a new note. Also marks that the notes have changed.
   *
   * @param ni The note to be added.
   */
  public void addNote( NotePadMeta ni ) {
    notes.add( ni );
    changedNotes = true;
  }

  /**
   * Find the note that is located on a certain point on the canvas.
   *
   * @param x the x-coordinate of the point queried
   * @param y the y-coordinate of the point queried
   * @return The note information if a note is located at the point. Otherwise, if nothing was found: null.
   */
  public NotePadMeta getNote( int x, int y ) {
    int i, s;
    s = notes.size();
    for ( i = s - 1; i >= 0; i-- ) {
      // Back to front because drawing goes from start to end

      NotePadMeta ni = notes.get( i );
      Point loc = ni.getLocation();
      Point p = new Point( loc.x, loc.y );
      if ( x >= p.x && x <= p.x + ni.width + 2 * Const.NOTE_MARGIN && y >= p.y
        && y <= p.y + ni.height + 2 * Const.NOTE_MARGIN ) {
        return ni;
      }
    }
    return null;
  }

  /**
   * Gets the note.
   *
   * @param i the i
   * @return the note
   */
  public NotePadMeta getNote( int i ) {
    return notes.get( i );
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
   * Finds the location (index) of the specified note.
   *
   * @param ni The note queried
   * @return The location of the note, or -1 if nothing was found.
   */
  public int indexOfNote( NotePadMeta ni ) {
    return notes.indexOf( ni );
  }

  /**
   * Lowers a note to the "bottom" of the list by removing the note at the specified index and re-inserting it at the
   * front. Also marks that the notes have changed.
   *
   * @param p the index into the notes list.
   */
  public void lowerNote( int p ) {
    // if valid index and not first index
    if ( ( p > 0 ) && ( p < notes.size() ) ) {
      NotePadMeta note = notes.remove( p );
      notes.add( 0, note );
      changedNotes = true;
    }
  }

  /**
   * Gets the number of notes.
   *
   * @return The number of notes.
   */
  public int nrNotes() {
    return notes.size();
  }

  /**
   * Raises a note to the "top" of the list by removing the note at the specified index and re-inserting it at the end.
   * Also marks that the notes have changed.
   *
   * @param p the index into the notes list.
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
   * Removes a note at a certain location (i.e. the specified index). Also marks that the notes have changed.
   *
   * @param i The index into the notes list
   */
  public void removeNote( int i ) {
    if ( i < 0 || i >= notes.size() ) {
      return;
    }
    notes.remove( i );
    changedNotes = true;
  }

  /**
   * Checks whether or not any of the notes have been changed.
   *
   * @return true if the notes have been changed, false otherwise
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
   * Get an array of the locations of an array of notes
   *
   * @param notes An array of notes
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
   * Gets the channel log table for the job.
   *
   * @return the channel log table for the job.
   */
  public ChannelLogTable getChannelLogTable() {
    return channelLogTable;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabaseInterface#addOrReplaceDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public void addOrReplaceDatabase( DatabaseMeta databaseMeta ) {
    addDatabase( databaseMeta );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public void addDatabase( DatabaseMeta ci ) {
    try {
      localDbMgr.add( ci );
    } catch ( KettleException ex ) {
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(int, org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public void addDatabase( int p, DatabaseMeta ci ) {
    addDatabase( ci );
  }

  /**
   * Returns a list of the databases.
   *
   * @return Returns the databases.
   */
  @Override
  public List<DatabaseMeta> getDatabases() {
    try {
      List<DatabaseMeta> databases = readDbManager.getAll();
      Collections.sort( databases, DatabaseMeta.comparator );
      return databases;
    } catch ( KettleException ex ) {
      return Collections.emptyList();
    }
  }

  /**
   * Propagate a change to a database to all parts of the transformation or job. Update steps or entries that have their
   * own copies of the database.
   * <p/>
   * This method should be used for any Create/Delete/Update operation at any level.
   *
   * @param name Name of the Database connection that has changed.
   */
  public void databaseUpdated( String name ) {
    // NOTE that this does not need to update "changedDatabases" because that write would already have gone through
    // localDbMgr.
    try {
      Optional<DatabaseMeta> newDb = Optional.ofNullable( readDbManager.get( name ) );
      databasesUpdated( name, newDb );
    } catch ( KettleException ex ) {
    }
  }

  /**
   * use the latest version of all databases used in the meta. Should be the equivalent of calling databaseUpdated( name
   * ) with every name returned from getUsedDatabaseConnectionNames(), but may be more efficient.
   *
   */
  public abstract void allDatabasesUpdated();

  /**
   * Propagate a change to a database to all parts of the transformation or job. Update steps or entries that have their
   * own copies of the database.
   * <p/>
   *
   *
   * @param name The name of the updated database
   * @param newDatabaseMeta the new DatabaseMeta, which could be empty if the database was deleted.
   */
  protected abstract void databasesUpdated( String name, Optional<DatabaseMeta> newDatabaseMeta ) throws KettleException;

  // helper method for databasesUpdated()
  protected void updateFields( DatabaseMeta existing, Optional<DatabaseMeta> newDb ) {
    newDb.ifPresentOrElse( existing::replaceMeta,
      // remember the name.
      () -> {
        DatabaseMeta newMeta = new DatabaseMeta();
        newMeta.setName( existing.getName() );
        existing.replaceMeta( newMeta );
      } );
  }


  /**
   * Gets the database names.
   *
   * @return the database names
   */
  public String[] getDatabaseNames() {
    try {
      List<DatabaseMeta> databases = readDbManager.getAll();
      Collections.sort( databases, DatabaseMeta.comparator );
      return databases.stream().map( DatabaseMeta::getName ).toArray( String[]::new );
    } catch ( KettleException ex ) {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#indexOfDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public int indexOfDatabase( DatabaseMeta di ) {
    List<DatabaseMeta> databases = getDatabases();
    return databases.indexOf( di );
  }

  /**
   * Sets the databases.
   *
   * @param databases The databases to set.
   */
  @Override
  public void setDatabases( List<DatabaseMeta> databases ) {
    try {
      localDbMgr.clear();
      for ( DatabaseMeta db : databases ) {
        localDbMgr.add( db );
      }
    } catch ( KettleException ex ) {
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#haveConnectionsChanged()
   */
  @Override
  public boolean haveConnectionsChanged() {
    if ( localDbMgr.hasChanged() ) {
      return true;
    }

    return false;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#removeDatabase(int)
   */
  @Override
  public void removeDatabase( int i ) {
    // note: getDatabase is operating on all databases, but remove is only working on Local.
    try {
      DatabaseMeta db = getDatabase( i );
      if ( db != null ) {
        localDbMgr.remove( db );
      }
    } catch ( KettleException ex ) {
    }
  }

  /**
   * Clears the flags for whether the transformation's databases have changed.
   */
  public void clearChangedDatabases() {
    localDbMgr.clearChanged();
  }

  /**
   * Sets the channel log table for the job.
   *
   * @param channelLogTable the channelLogTable to set
   */
  public void setChannelLogTable( ChannelLogTable channelLogTable ) {
    this.channelLogTable = channelLogTable;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#copyVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */

  @Override
  public void copyVariablesFrom( VariableSpace space ) {
    variables.copyVariablesFrom( space );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String)
   */
  @Override
  public String environmentSubstitute( String aString ) {
    return variables.environmentSubstitute( aString );
  }

  /*
   * (non-javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#environmentSubstitute(java.lang.String[])
   */
  @Override
  public String[] environmentSubstitute( String[] aString ) {
    return variables.environmentSubstitute( aString );
  }

  @Override
  public String fieldSubstitute( String aString, RowMetaInterface rowMeta, Object[] rowData ) throws KettleValueException {
    return variables.fieldSubstitute( aString, rowMeta, rowData );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getParentVariableSpace()
   */
  @Override
  public VariableSpace getParentVariableSpace() {
    return variables.getParentVariableSpace();
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.core.variables.VariableSpace#setParentVariableSpace(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void setParentVariableSpace( VariableSpace parent ) {
    variables.setParentVariableSpace( parent );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String, java.lang.String)
   */
  @Override
  public String getVariable( String variableName, String defaultValue ) {
    return variables.getVariable( variableName, defaultValue );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#getVariable(java.lang.String)
   */
  @Override
  public String getVariable( String variableName ) {
    return variables.getVariable( variableName );
  }

  /*
   * (non-Javadoc)
   *
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

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.core.variables.VariableSpace#initializeVariablesFrom(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void initializeVariablesFrom( VariableSpace parent ) {
    variables.initializeVariablesFrom( parent );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#listVariables()
   */
  @Override
  public String[] listVariables() {
    return variables.listVariables();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#setVariable(java.lang.String, java.lang.String)
   */
  @Override
  public void setVariable( String variableName, String variableValue ) {
    variables.setVariable( variableName, variableValue );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#shareVariablesWith(org.pentaho.di.core.variables.VariableSpace)
   */
  @Override
  public void shareVariablesWith( VariableSpace space ) {
    variables = space;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.variables.VariableSpace#injectVariables(java.util.Map)
   */
  @Override
  public void injectVariables( Map<String, String> prop ) {
    variables.injectVariables( prop );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#addParameterDefinition(java.lang.String, java.lang.String,
   * java.lang.String)
   */
  @Override
  public void addParameterDefinition( String key, String defValue, String description ) throws DuplicateParamException {
    namedParams.addParameterDefinition( key, defValue, description );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDescription(java.lang.String)
   */
  @Override
  public String getParameterDescription( String key ) throws UnknownParamException {
    return namedParams.getParameterDescription( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterDefault(java.lang.String)
   */
  @Override
  public String getParameterDefault( String key ) throws UnknownParamException {
    return namedParams.getParameterDefault( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#getParameterValue(java.lang.String)
   */
  @Override
  public String getParameterValue( String key ) throws UnknownParamException {
    return namedParams.getParameterValue( key );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#listParameters()
   */
  @Override
  public String[] listParameters() {
    return namedParams.listParameters();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#setParameterValue(java.lang.String, java.lang.String)
   */
  @Override
  public void setParameterValue( String key, String value ) throws UnknownParamException {
    namedParams.setParameterValue( key, value );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#eraseParameters()
   */
  @Override
  public void eraseParameters() {
    namedParams.eraseParameters();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.parameters.NamedParams#clearParameters()
   */
  @Override
  public void clearParameters() {
    namedParams.clearParameters();
  }

  /*
   * (non-Javadoc)
   *
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

  /*
   * (non-Javadoc)
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
        setVariable( key, value );
      }
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getLogLevel()
   */
  @Override
  public LogLevel getLogLevel() {
    return logLevel;
  }

  /**
   * Sets the log level.
   *
   * @param logLevel the new log level
   */
  public void setLogLevel( LogLevel logLevel ) {
    this.logLevel = logLevel;
  }

  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public void setMetaStore( IMetaStore metaStore ) {
    this.metaStore = metaStore;
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
   * @param sharedObjectsFile the sharedObjectsFile to set
   */
  public void setSharedObjectsFile( String sharedObjectsFile ) {
    this.sharedObjectsFile = sharedObjectsFile;
  }

  /**
   * Gets the shared objects.
   *
   * @return the sharedObjects
   */
  public SharedObjects getSharedObjects() {
    if ( sharedObjects == null ) {
      try {
        String soFile = environmentSubstitute( sharedObjectsFile );
        sharedObjects = new SharedObjects( soFile );
      } catch ( KettleException e ) {
        LogChannel.GENERAL.logDebug( e.getMessage(), e );
      }
    }
    return sharedObjects;
  }

  /**
   * Sets the shared objects.
   *
   * @param sharedObjects the sharedObjects to set
   */
  public void setSharedObjects( SharedObjects sharedObjects ) {
    this.sharedObjects = sharedObjects;
  }

  /**
   * Read shared objects.
   *
   * @return the shared objects
   * @throws KettleException the kettle exception
   */
  public SharedObjects readSharedObjects() throws KettleException {
    // Extract the shared steps, connections, etc. using the SharedObjects
    // class
    //
    String soFile = environmentSubstitute( sharedObjectsFile );
    SharedObjects sharedObjects = new SharedObjects( soFile );
    Map<?, SharedObjectInterface> objectsMap = sharedObjects.getObjectsMap();

    // This is basically only used for Steps now, and since those can no longer be shared, it's really just for
    // backwards compatibility. All other shared object types are handled through the DefaultBowl instead.
    for ( SharedObjectInterface<?> object : objectsMap.values() ) {
      loadSharedObject( object );
    }

    return sharedObjects;
  }

  public boolean loadSharedObject( SharedObjectInterface object ) {
    return false;
  }

  /**
   * Sets the internal name kettle variable.
   *
   * @param var the new internal name kettle variable
   */
  protected abstract void setInternalNameKettleVariable( VariableSpace var );

  /**
   * Gets the date the transformation was created.
   *
   * @return the date the transformation was created.
   */
  @Override
  public Date getCreatedDate() {
    return createdDate;
  }

  /**
   * Sets the date the transformation was created.
   *
   * @param createdDate The creation date to set.
   */
  @Override
  public void setCreatedDate( Date createdDate ) {
    this.createdDate = createdDate;
  }

  /**
   * Sets the user by whom the transformation was created.
   *
   * @param createdUser The user to set.
   */
  @Override
  public void setCreatedUser( String createdUser ) {
    this.createdUser = createdUser;
  }

  /**
   * Gets the user by whom the transformation was created.
   *
   * @return the user by whom the transformation was created.
   */
  @Override
  public String getCreatedUser() {
    return createdUser;
  }

  /**
   * Sets the date the transformation was modified.
   *
   * @param modifiedDate The modified date to set.
   */
  @Override
  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }

  /**
   * Gets the date the transformation was modified.
   *
   * @return the date the transformation was modified.
   */
  @Override
  public Date getModifiedDate() {
    return modifiedDate;
  }

  /**
   * Sets the user who last modified the transformation.
   *
   * @param modifiedUser The user name to set.
   */
  @Override
  public void setModifiedUser( String modifiedUser ) {
    this.modifiedUser = modifiedUser;
  }

  /**
   * Gets the user who last modified the transformation.
   *
   * @return the user who last modified the transformation.
   */
  @Override
  public String getModifiedUser() {
    return modifiedUser;
  }

  public void clear() {
    setName( null );
    setFilename( null );
    notes = new ArrayList<NotePadMeta>();
    localSharedObjects.clear();
    channelLogTable = ChannelLogTable.getDefault( this, this );
    attributesMap = new HashMap<String, Map<String, String>>();
    max_undo = Const.MAX_UNDO;
    clearUndo();
    clearChanged();
    setChanged( false );
    channelLogTable = ChannelLogTable.getDefault( this, this );

    createdUser = "-";
    createdDate = new Date();

    modifiedUser = "-";
    modifiedDate = new Date();
    directory = new RepositoryDirectory();
    description = null;
    extendedDescription = null;
  }

  @Override
  public void clearChanged() {
    clearChangedDatabases();
    changedNotes = false;
    for ( int i = 0; i < nrNotes(); i++ ) {
      getNote( i ).setChanged( false );
    }
    changedFlag.clearChanged();
    fireContentChangedListeners( false );
  }

  @Override
  public void setChanged() {
    changedFlag.setChanged();
    fireContentChangedListeners( true );
  }

  /*
     * (non-Javadoc)
     *
     * @see org.pentaho.di.core.changed.ChangedFlag#setChanged(boolean)
     */
  @Override
  public final void setChanged( boolean ch ) {
    if ( ch ) {
      setChanged();
    } else {
      clearChanged();
    }
  }

  public void addObserver( PDIObserver o ) {
    changedFlag.addObserver( o );
  }

  public void deleteObserver( PDIObserver o ) {
    changedFlag.deleteObserver( o );
  }

  public void notifyObservers( Object arg ) {
    changedFlag.notifyObservers( arg );
  }

  /**
   * Checks whether the job can be saved. For JobMeta, this method always returns true
   *
   * @return true
   * @see org.pentaho.di.core.EngineMetaInterface#canSave()
   */
  @Override
  public boolean canSave() {
    return true;
  }

  @Override
  public boolean hasChanged() {
    if ( changedFlag.hasChanged() ) {
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

  /**
   * Gets the registration date for the transformation. For AbstractMeta, this method always returns null.
   *
   * @return null
   */
  @Override
  public Date getRegistrationDate() {
    return null;
  }

  /**
   * Gets the interface to the parent log object. For AbstractMeta, this method always returns null.
   *
   * @return null
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getParent()
   */
  @Override
  public LoggingObjectInterface getParent() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectName()
   */
  @Override
  public String getObjectName() {
    return getName();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.core.logging.LoggingObjectInterface#getObjectCopy()
   */
  @Override
  public String getObjectCopy() {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryElementInterface#getObjectRevision()
   */
  @Override
  public ObjectRevision getObjectRevision() {
    return objectRevision;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * org.pentaho.di.repository.RepositoryElementInterface#setObjectRevision(org.pentaho.di.repository.ObjectRevision)
   */
  @Override
  public void setObjectRevision( ObjectRevision objectRevision ) {
    this.objectRevision = objectRevision;
  }

  /**
   * Checks whether the specified name has changed (i.e. is different from the specified old name). If both names are
   * null, false is returned. If the old name is null and the new new name is non-null, true is returned. Otherwise, if
   * the name strings are equal then true is returned; false is returned if the name strings are not equal.
   *
   * @param oldName the old name
   * @param newName the new name
   * @return true if the names have changed, false otherwise
   */
  private boolean nameChanged( String oldName, String newName ) {
    if ( oldName == null && newName == null ) {
      return false;
    }
    if ( oldName == null && newName != null ) {
      return true;
    }
    return !oldName.equals( newName );
  }

  protected boolean shouldOverwrite( OverwritePrompter prompter, Props props, String message, String rememberMessage ) {
    boolean askOverwrite = Props.isInitialized() ? props.askAboutReplacingDatabaseConnections() : false;
    boolean overwrite = Props.isInitialized() ? props.replaceExistingDatabaseConnections() : true;
    if ( askOverwrite ) {
      if ( prompter != null ) {
        overwrite = prompter.overwritePrompt( message, rememberMessage, Props.STRING_ASK_ABOUT_REPLACING_DATABASES );
      }
    }
    return overwrite;
  }

  public boolean hasMissingPlugins() {
    return false;
  }


  /**
   * Returns the set of databases available only for this meta or <b>null</b> if it was not initialized.
   *
   * @return <b>nonSharableDatabases</b>
   */
  public Set<String> getPrivateDatabases() {
    try {
      List<DatabaseMeta> databases = localDbMgr.getAll();
      return databases.stream().map( DatabaseMeta::getName ).collect( Collectors.toSet() );
    } catch ( KettleException ex ) {
      return null;
    }
  }

  @Override
  public void saveSharedObjects() throws KettleException {
    // AbstractMeta is no longer responsible for storing shared objects. so this is a No-Op.
  }

  /**
   * This method needs to be called to store those objects which are used and referenced in the transformation metadata
   * but not saved in the XML serialization. For example, the Kettle data service definition is referenced by name but
   * not stored when getXML() is called.<br>
   * @deprecated This method is empty since 2013.
   *
   * @param metaStore
   *          The store to save to
   * @throws MetaStoreException
   *           in case there is an error.
   */
  @Deprecated
  public void saveMetaStoreObjects( Repository repository, IMetaStore metaStore ) throws MetaStoreException {

  }

  protected int compare( AbstractMeta meta1, AbstractMeta meta2 ) {
    // If we don't have a filename, it comes from a repository
    if ( Utils.isEmpty( meta1.getFilename() ) ) {

      if ( !Utils.isEmpty( meta2.getFilename() ) ) {
        return -1;
      }

      // First compare names...
      if ( Utils.isEmpty( meta1.getName() ) && !Utils.isEmpty( meta2.getName() ) ) {
        return -1;
      }
      if ( !Utils.isEmpty( meta1.getName() ) && Utils.isEmpty( meta2.getName() ) ) {
        return 1;
      }
      int cmpName = meta1.getName().compareTo( meta2.getName() );
      if ( cmpName != 0 ) {
        return cmpName;
      }

      // Same name, compare Repository directory...
      int cmpDirectory = meta1.getRepositoryDirectory().getPath().compareTo( meta2.getRepositoryDirectory().getPath() );
      if ( cmpDirectory != 0 ) {
        return cmpDirectory;
      }

      // Same name, same directory, compare versions
      if ( meta1.getObjectRevision() != null && meta2.getObjectRevision() == null ) {
        return 1;
      }
      if ( meta1.getObjectRevision() == null && meta2.getObjectRevision() != null ) {
        return -1;
      }
      if ( meta1.getObjectRevision() == null && meta2.getObjectRevision() == null ) {
        return 0;
      }
      return meta1.getObjectRevision().getName().compareTo( meta2.getObjectRevision().getName() );

    } else {
      if ( Utils.isEmpty( meta2.getFilename() ) ) {
        return 1;
      }

      // First compare names
      //
      if ( Utils.isEmpty( meta1.getName() ) && !Utils.isEmpty( meta2.getName() ) ) {
        return -1;
      }
      if ( !Utils.isEmpty( meta1.getName() ) && Utils.isEmpty( meta2.getName() ) ) {
        return 1;
      }
      int cmpName = meta1.getName().compareTo( meta2.getName() );
      if ( cmpName != 0 ) {
        return cmpName;
      }

      // Same name, compare filenames...
      return meta1.getFilename().compareTo( meta2.getFilename() );
    }
  }

  @Override
  public int hashCode() {
    boolean inRepo = Utils.isEmpty( getFilename() );
    return Objects.hash( name, inRepo, inRepo ? filename : getRepositoryDirectory().getPath() );
  }

  public NamedClusterServiceOsgi getNamedClusterServiceOsgi() {
    return namedClusterServiceOsgi;
  }

  public void setNamedClusterServiceOsgi( NamedClusterServiceOsgi namedClusterServiceOsgi ) {
    this.namedClusterServiceOsgi = namedClusterServiceOsgi;
  }

  public MetastoreLocatorOsgi getMetastoreLocatorOsgi() {
    return metastoreLocatorOsgi;
  }

  public void setMetastoreLocatorOsgi( MetastoreLocatorOsgi metastoreLocatorOsgi ) {
    this.metastoreLocatorOsgi = metastoreLocatorOsgi;
  }

  public NamedClusterEmbedManager getNamedClusterEmbedManager( ) {
    return namedClusterEmbedManager;
  }

  public void disposeEmbeddedMetastoreProvider() {
    if ( embeddedMetastoreNoLongerUsed() && getEmbeddedMetastoreKey() != null ) {
      KettleVFS.closeEmbeddedFileSystem( getEmbeddedMetastoreKey() );
      //Dispose of embedded metastore for this run
      getMetastoreLocatorOsgi().disposeMetastoreProvider( getEmbeddedMetastoreKey() );
    }
  }

  /**
   * Check whether the embeddedMetastore is used anywhere else up the Trans / Job hierarchy.
   * It's possible for a Parent JobMeta to be shared by multiple child job copies, for example,
   * and we want to make sure it's only closed when the parent is done.
   */
  private boolean embeddedMetastoreNoLongerUsed() {
    boolean topLevel = !( this.getParent() instanceof AbstractMeta );
    return topLevel || !embeddedMetastoreUsedByAParent( embeddedMetaStore, this.getParent() );
  }

  private boolean embeddedMetastoreUsedByAParent( EmbeddedMetaStore embeddedMetaStore, LoggingObjectInterface parent ) {
    if ( parent instanceof AbstractMeta ) {
      return ( (AbstractMeta) parent ).embeddedMetaStore == embeddedMetaStore
        || embeddedMetastoreUsedByAParent( embeddedMetaStore, parent.getParent() );
    }
    return false;
  }

  public String getEmbeddedMetastoreProviderKey() {
    return embeddedMetastoreProvKeySupplier.get();
  }

  @Override
  public void setVersioningEnabled( Boolean versioningEnabled ) {
    this.versioningEnabled = versioningEnabled;
  }

  @Override
  public Boolean getVersioningEnabled() {
    return this.versioningEnabled;
  }

  private static class RunOptions {
    boolean clearingLog;
    boolean safeModeEnabled;

    RunOptions() {
      clearingLog = true;
      safeModeEnabled = false;
    }
  }

  public boolean isClearingLog() {
    return runOptions.clearingLog;
  }

  public void setClearingLog( boolean clearingLog ) {
    this.runOptions.clearingLog = clearingLog;
  }

  public boolean isSafeModeEnabled() {
    return runOptions.safeModeEnabled;
  }

  public void setSafeModeEnabled( boolean safeModeEnabled ) {
    this.runOptions.safeModeEnabled = safeModeEnabled;
  }

  public IMetaFileCache getMetaFileCache() {
    return metaFileCache;
  }

  public void setMetaFileCache( IMetaFileCache metaFileCache ) {
    this.metaFileCache = metaFileCache;
  }

  public DatabaseManagementInterface getDatabaseManagementInterface() {
    return localDbMgr;
  }

  /**
   * Returns the SharedObjectsManagementInterface that allows the CRUD operation of local sharedObjects
   * @return SharedObjectsManagementInterface
   */
  public SlaveServerManagementInterface getSlaveServerManagementInterface() {
    return localSlaveServerMgr;
  }

  public <T> T getSharedObjectManager( Class<T> clazz ) {
    if ( clazz.isAssignableFrom( SlaveServerManagementInterface.class ) ) {
      return clazz.cast( localSlaveServerMgr );
    } else if ( clazz.isAssignableFrom( DatabaseManagementInterface.class ) ) {
      return clazz.cast( localDbMgr );
    }

    return null;
  }

  /**
   * Return the default save directory for a transformation/Job
   * @return directory The full path to the default directory for saving transformation/Job
   */
  public String getDefaultSaveDirectory() {
    return defaultSaveDirectory;
  }

  /**
   * Set the default save directory for transformation/Job
   * @param defaultDir The directory displayed as default directory in FileOpenSaveDialog when this transformation/Job is
   *                   saved
   */
  public void setDefaultSaveDirectory( String defaultDir ) {
    defaultSaveDirectory = defaultDir;
  }

}

