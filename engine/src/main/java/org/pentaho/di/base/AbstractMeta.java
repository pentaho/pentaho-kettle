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

package org.pentaho.di.base;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.AttributesInterface;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.osgi.api.MetastoreLocatorOsgi;
import org.pentaho.di.core.osgi.api.NamedClusterServiceOsgi;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.attributes.metastore.EmbeddedMetaStore;
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
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.parameters.NamedParams;
import org.pentaho.di.core.parameters.NamedParamsDefault;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.undo.TransAction;
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
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractMeta implements ChangedFlagInterface, UndoInterface, HasDatabasesInterface, VariableSpace,
  EngineMetaInterface, NamedParams, HasSlaveServersInterface, AttributesInterface, HasRepositoryInterface,
  LoggingObjectInterface {

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

  /**
   * The repository to reference in the one-off case that it is needed
   */
  protected Repository repository;

  protected List<DatabaseMeta> databases;

  protected List<NameChangedListener> nameChangedListeners;

  protected List<FilenameChangedListener> filenameChangedListeners;

  protected List<ContentChangedListener> contentChangedListeners;

  protected List<CurrentDirectoryChangedListener> currentDirectoryChangedListeners;

  protected List<SlaveServer> slaveServers;

  protected List<NotePadMeta> notes;

  protected ChannelLogTable channelLogTable;

  protected boolean changedNotes, changedDatabases;

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

  protected String embeddedMetastoreProviderKey;

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

  /**
   * The set of names of databases available only for this meta. The list is needed to distinguish connections when we
   * load/save the meta in JCR repository.
   * <p/>
   * Should be {@code null} if we use old meta
   *
   * @see <a href="http://jira.pentaho.com/browse/PPP-3405">PPP-3405</a>,
   * <a href="http://jira.pentaho.com/browse/PPP-3413">PPP-3413</a>
   **/
  protected Set<String> privateDatabases;

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.repository.RepositoryElementInterface#getObjectId()
   */
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
    this.repository = repository;
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
   * Find a database connection by it's name
   *
   * @param name The database name to look for
   * @return The database connection or null if nothing was found.
   */
  @Override
  public DatabaseMeta findDatabase( String name ) {
    for ( int i = 0; i < nrDatabases(); i++ ) {
      DatabaseMeta ci = getDatabase( i );
      if ( ( ci != null ) && ( ci.getName().equalsIgnoreCase( name ) )
        || ( ci.getDisplayName().equalsIgnoreCase( name ) ) ) {
        return ci;
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
    return ( databases == null ? 0 : databases.size() );
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#getDatabase(int)
   */
  @Override
  public DatabaseMeta getDatabase( int i ) {
    return databases.get( i );
  }

  public void importFromMetaStore() throws MetaStoreException, KettlePluginException {
    // Read the databases...
    //
    if ( metaStore != null ) {
      IMetaStoreElementType databaseType =
        metaStore.getElementTypeByName(
          PentahoDefaults.NAMESPACE, PentahoDefaults.DATABASE_CONNECTION_ELEMENT_TYPE_NAME );
      if ( databaseType != null ) {
        List<IMetaStoreElement> databaseElements = metaStore.getElements( PentahoDefaults.NAMESPACE, databaseType );
        for ( IMetaStoreElement databaseElement : databaseElements ) {
          addDatabase( DatabaseMetaStoreUtil.loadDatabaseMetaFromDatabaseElement(
            metaStore, databaseElement ), false );
        }
      }

      // TODO: do the same for slaves, clusters, partition schemas
    }
  }

  /**
   * Adds the name changed listener.
   *
   * @param listener the listener
   */
  public void addNameChangedListener( NameChangedListener listener ) {
    if ( nameChangedListeners == null ) {
      nameChangedListeners = new ArrayList<NameChangedListener>();
    }
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
    nameChangedListeners.remove( listener );
  }

  /**
   * Removes all the name changed listeners
   */
  public void clearNameChangedListeners() {
    if ( nameChangedListeners != null ) {
      nameChangedListeners.clear();
    }
  }

  /**
   * Fire name changed listeners.
   *
   * @param oldName the old name
   * @param newName the new name
   */
  protected void fireNameChangedListeners( String oldName, String newName ) {
    if ( nameChanged( oldName, newName ) ) {
      if ( nameChangedListeners != null ) {
        for ( NameChangedListener listener : nameChangedListeners ) {
          listener.nameChanged( this, oldName, newName );
        }
      }
    }
  }

  /**
   * Adds the filename changed listener.
   *
   * @param listener the listener
   */
  public void addFilenameChangedListener( FilenameChangedListener listener ) {
    if ( filenameChangedListeners == null ) {
      filenameChangedListeners = new ArrayList<FilenameChangedListener>();
    }
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
    filenameChangedListeners.remove( listener );
  }

  /**
   * Fire filename changed listeners.
   *
   * @param oldFilename the old filename
   * @param newFilename the new filename
   */
  protected void fireFilenameChangedListeners( String oldFilename, String newFilename ) {
    if ( nameChanged( oldFilename, newFilename ) ) {
      if ( filenameChangedListeners != null ) {
        for ( FilenameChangedListener listener : filenameChangedListeners ) {
          listener.filenameChanged( this, oldFilename, newFilename );
        }
      }
    }
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

  public List<ContentChangedListener> getContentChangedListeners() {
    if ( contentChangedListeners == null ) {
      return ImmutableList.of();
    } else {
      return ImmutableList.copyOf( contentChangedListeners );
    }
  }

  /**
   * Fire content changed listeners.
   */
  protected void fireContentChangedListeners() {
    fireContentChangedListeners( true );
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

  /**
   * Remove listener
   */
  public void addCurrentDirectoryChangedListener( CurrentDirectoryChangedListener listener ) {
    if ( currentDirectoryChangedListeners == null ) {
      currentDirectoryChangedListeners = new ArrayList<>();
    }
    if ( listener != null && !currentDirectoryChangedListeners.contains( listener ) ) {
      currentDirectoryChangedListeners.add( listener );
    }
  }

  /**
   * Add a listener to be notified of design-time changes to current directory variable
   */
  public void removeCurrentDirectoryChangedListener( CurrentDirectoryChangedListener listener ) {
    if ( currentDirectoryChangedListeners != null ) {
      currentDirectoryChangedListeners.remove( listener );
    }
  }

  /**
   * Notify listeners of a change in current directory.
   */
  protected void fireCurrentDirectoryChanged( String previous, String current ) {
    if ( currentDirectoryChangedListeners != null && nameChanged( previous, current ) ) {
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
   * Gets a list of slave servers.
   *
   * @return the slaveServer list
   */
  @Override
  public List<SlaveServer> getSlaveServers() {
    return slaveServers;
  }

  /**
   * Sets the slave servers.
   *
   * @param slaveServers the slaveServers to set
   */
  public void setSlaveServers( List<SlaveServer> slaveServers ) {
    this.slaveServers = slaveServers;
  }

  /**
   * Find a slave server using the name
   *
   * @param serverString the name of the slave server
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
    addDatabase( databaseMeta, true );
  }

  protected void addDatabase( DatabaseMeta databaseMeta, boolean replace ) {
    int index = databases.indexOf( databaseMeta );
    if ( index < 0 ) {
      addDatabase( databaseMeta );
    } else if ( replace ) {
      DatabaseMeta previous = getDatabase( index );
      previous.replaceMeta( databaseMeta );
      previous.setShared( databaseMeta.isShared() );
    }
    changedDatabases = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public void addDatabase( DatabaseMeta ci ) {
    databases.add( ci );
    Collections.sort( databases, DatabaseMeta.comparator );
    changedDatabases = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#addDatabase(int, org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public void addDatabase( int p, DatabaseMeta ci ) {
    databases.add( p, ci );
    changedDatabases = true;
  }

  /**
   * Returns a list of the databases.
   *
   * @return Returns the databases.
   */
  @Override
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

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#indexOfDatabase(org.pentaho.di.core.database.DatabaseMeta)
   */
  @Override
  public int indexOfDatabase( DatabaseMeta di ) {
    return databases.indexOf( di );
  }

  /**
   * Sets the databases.
   *
   * @param databases The databases to set.
   */
  @Override
  public void setDatabases( List<DatabaseMeta> databases ) {
    Collections.sort( databases, DatabaseMeta.comparator );
    this.databases = databases;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#haveConnectionsChanged()
   */
  @Override
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

  /*
   * (non-Javadoc)
   *
   * @see org.pentaho.di.trans.HasDatabasesInterface#removeDatabase(int)
   */
  @Override
  public void removeDatabase( int i ) {
    if ( i < 0 || i >= databases.size() ) {
      return;
    }
    databases.remove( i );
    changedDatabases = true;
  }

  /**
   * Clears the flags for whether the transformation's databases have changed.
   */
  public void clearChangedDatabases() {
    changedDatabases = false;

    for ( int i = 0; i < nrDatabases(); i++ ) {
      getDatabase( i ).setChanged( false );
    }
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
        setVariable( key, defValue );
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

    // First read the databases...
    // We read databases & slaves first because there might be dependencies
    // that need to be resolved.
    //
    for ( SharedObjectInterface object : objectsMap.values() ) {
      loadSharedObject( object );
    }

    return sharedObjects;
  }

  protected boolean loadSharedObject( SharedObjectInterface object ) {
    if ( object instanceof DatabaseMeta ) {
      DatabaseMeta databaseMeta = (DatabaseMeta) object;
      databaseMeta.shareVariablesWith( this );
      addOrReplaceDatabase( databaseMeta );
    } else if ( object instanceof SlaveServer ) {
      SlaveServer slaveServer = (SlaveServer) object;
      slaveServer.shareVariablesWith( this );
      addOrReplaceSlaveServer( slaveServer );
    } else {
      return false;
    }
    return true;
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
    databases = new ArrayList<DatabaseMeta>();
    slaveServers = new ArrayList<SlaveServer>();
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
   * Note, that the internal collection is returned with no protection wrapper!
   *
   * @return <b>nonSharableDatabases</b>
   */
  public Set<String> getPrivateDatabases() {
    return privateDatabases;
  }

  /**
   * Sets private databases' names
   *
   * @param privateDatabases - The list of databases available only for this meta
   */
  public void setPrivateDatabases( Set<String> privateDatabases ) {
    this.privateDatabases = privateDatabases;
  }

  public void saveSharedObjects() throws KettleException {
    try {
      // Load all the shared objects...
      String soFile = environmentSubstitute( sharedObjectsFile );
      SharedObjects sharedObjects = new SharedObjects( soFile );
      // in-memory shared objects are supposed to be in sync, discard those on file to allow edit/delete
      sharedObjects.setObjectsMap( new Hashtable<>() );

      for ( SharedObjectInterface sharedObject : getAllSharedObjects() ) {
        if ( sharedObject.isShared() ) {
          sharedObjects.storeObject( sharedObject );
        }
      }

      sharedObjects.saveToFile();
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save shared ojects", e );
    }
  }

  protected List<SharedObjectInterface> getAllSharedObjects() {
    List<SharedObjectInterface> shared = new ArrayList<>();
    shared.addAll( databases );
    shared.addAll( slaveServers );
    return shared;
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
    KettleVFS.closeEmbeddedFileSystem( embeddedMetastoreProviderKey );
    if ( embeddedMetastoreProviderKey != null ) {
      //Dispose of embedded metastore for this run
      getMetastoreLocatorOsgi().disposeMetastoreProvider( embeddedMetastoreProviderKey );
    }
  }

  public String getEmbeddedMetastoreProviderKey() {
    return embeddedMetastoreProviderKey;
  }

  public void setEmbeddedMetastoreProviderKey( String embeddedMetastoreProviderKey ) {
    this.embeddedMetastoreProviderKey = embeddedMetastoreProviderKey;
  }

  @Override
  public void setVersioningEnabled( Boolean versioningEnabled ) {
    this.versioningEnabled = versioningEnabled;
  }

  @Override
  public Boolean getVersioningEnabled() {
    return this.versioningEnabled;
  }

  private class RunOptions {
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
}
