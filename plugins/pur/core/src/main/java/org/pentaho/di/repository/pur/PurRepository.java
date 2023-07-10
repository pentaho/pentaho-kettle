/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2010-2023 by Hitachi Vantara : http://www.pentaho.com
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


package org.pentaho.di.repository.pur;

import org.apache.commons.lang.StringUtils;
import org.pentaho.di.cluster.ClusterSchema;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.ProgressMonitorListener;
import org.pentaho.di.core.annotations.RepositoryPlugin;
import org.pentaho.di.core.changed.ChangedFlagInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.IdNotFoundException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleSecurityException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.imp.Import;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.laf.BasePropertyHandler;
import org.pentaho.di.metastore.MetaStoreConst;
import org.pentaho.di.partition.PartitionSchema;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.IRepositoryExporter;
import org.pentaho.di.repository.IRepositoryImporter;
import org.pentaho.di.repository.IRepositoryService;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.ObjectRevision;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.RepositorySecurityManager;
import org.pentaho.di.repository.RepositorySecurityProvider;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.repository.pur.metastore.PurRepositoryMetaStore;
import org.pentaho.di.repository.pur.model.EEJobMeta;
import org.pentaho.di.repository.pur.model.EERepositoryObject;
import org.pentaho.di.repository.pur.model.EETransMeta;
import org.pentaho.di.repository.pur.model.EEUserInfo;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.shared.SharedObjects;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.repository.pur.services.IAclService;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.pur.services.IRevisionService;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.exceptions.MetaStoreNamespaceExistsException;
import org.pentaho.metastore.util.PentahoDefaults;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest.FILES_TYPE_FILTER;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryCreateFileException;
import org.pentaho.platform.api.repository2.unified.UnifiedRepositoryUpdateFileException;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.webservices.jaxws.IUnifiedRepositoryJaxwsWebService;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Implementation of {@link Repository} that delegates to the Pentaho unified repository (PUR), an instance of
 * {@link IUnifiedRepository}.
 *
 * @author Matt
 * @author mlowery
 */
@SuppressWarnings( "deprecation" )
@RepositoryPlugin( id = "PentahoEnterpriseRepository", name = "RepositoryType.Name.EnterpriseRepository",
  description = "RepositoryType.Description.EnterpriseRepository",
  metaClass = "org.pentaho.di.repository.pur.PurRepositoryMeta", i18nPackageName = "org.pentaho.di.repository.pur" )
public class PurRepository extends AbstractRepository implements Repository, ReconnectableRepository,
  RepositoryExtended, java.io.Serializable {

  private static final long serialVersionUID = 7460109109707189479L; /* EESOURCE: UPDATE SERIALVERUID */

  // Kettle property that when set to false disabled the lazy repository access
  public static final String LAZY_REPOSITORY = "KETTLE_LAZY_REPOSITORY";

  private static Class<?> PKG = PurRepository.class;

  // ~ Static fields/initializers ======================================================================================

  // private static final Log logger = LogFactory.getLog(PurRepository.class);

  private static final String REPOSITORY_VERSION = "1.0"; //$NON-NLS-1$

  private static final boolean VERSION_SHARED_OBJECTS = true;

  private static final String FOLDER_PDI = "pdi"; //$NON-NLS-1$

  private static final String FOLDER_PARTITION_SCHEMAS = "partitionSchemas"; //$NON-NLS-1$

  private static final String FOLDER_CLUSTER_SCHEMAS = "clusterSchemas"; //$NON-NLS-1$

  private static final String FOLDER_SLAVE_SERVERS = "slaveServers"; //$NON-NLS-1$

  private static final String FOLDER_DATABASES = "databases"; //$NON-NLS-1$

  private static final String SYSTEM_FOLDER = Const
          .safeAppendDirectory( BasePropertyHandler.getProperty( "systemDirBase", "system/" ), "" );

  // ~ Instance fields =================================================================================================
  /**
   * Indicates that this code should be run in unit test mode (where PUR is passed in instead of created inside this
   * class).
   */
  private boolean test = false;

  private IUnifiedRepository pur;

  private IUser user;

  private PurRepositoryMeta repositoryMeta;

  private DatabaseDelegate databaseMetaTransformer = new DatabaseDelegate( this );

  private PartitionDelegate partitionSchemaTransformer = new PartitionDelegate( this );

  private SlaveDelegate slaveTransformer = new SlaveDelegate( this );

  private ClusterDelegate clusterTransformer = new ClusterDelegate( this );

  private ISharedObjectsTransformer transDelegate;

  private ISharedObjectsTransformer jobDelegate;

  private Map<RepositoryObjectType, SharedObjectAssembler<?>> sharedObjectAssemblerMap;

  private RepositorySecurityManager securityManager;

  private RepositorySecurityProvider securityProvider;

  protected LogChannelInterface log;

  protected Serializable cachedSlaveServerParentFolderId;

  protected Serializable cachedPartitionSchemaParentFolderId;

  protected Serializable cachedClusterSchemaParentFolderId;

  protected Serializable cachedDatabaseMetaParentFolderId;

  private final RootRef rootRef = new RootRef();

  private UnifiedRepositoryLockService unifiedRepositoryLockService;

  private Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType = null;

  private boolean connected = false;

  private String connectMessage = null;

  protected PurRepositoryMetaStore metaStore;

  private ConnectionManager connectionManager = ConnectionManager.getInstance();

  // The servers (DI Server, BA Server) that a user can authenticate to
  protected enum RepositoryServers {
    DIS, POBS
  }

  private IRepositoryConnector purRepositoryConnector;

  private RepositoryServiceRegistry purRepositoryServiceRegistry = new RepositoryServiceRegistry();

  private static final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
  private static final ReadWriteLock sharedObjectsLock = new ReentrantReadWriteLock();


  // ~ Constructors ====================================================================================================

  public PurRepository() {
    super();
    initSharedObjectAssemblerMap();
  }

  // ~ Methods =========================================================================================================

  protected RepositoryDirectoryInterface getRootDir() throws KettleException {
    RepositoryDirectoryInterface ref = rootRef.getRef();
    return ref == null ? loadRepositoryDirectoryTree() : ref;
  }

  /**
   * public for unit tests.
   */
  public void setTest( final IUnifiedRepository pur ) {
    this.pur = pur;
    // set this to avoid NPE in connect()
    this.repositoryMeta.setRepositoryLocation( new PurRepositoryLocation( "doesnotmatch" ) );
    this.test = true;
  }

  private boolean isTest() {
    return test;
  }

  @Override
  public void init( final RepositoryMeta repositoryMeta ) {
    this.log = new LogChannel( this.getClass().getSimpleName() );
    this.repositoryMeta = (PurRepositoryMeta) repositoryMeta;
    purRepositoryConnector = new PurRepositoryConnector( this, this.repositoryMeta, rootRef );
  }

  public void setPurRepositoryConnector( IRepositoryConnector purRepositoryConnector ) {
    this.purRepositoryConnector = purRepositoryConnector;
  }

  public RootRef getRootRef() {
    return rootRef;
  }

  @Override public void connect( final String username, final String password ) throws KettleException {
    connected = false;
    if ( isTest() ) {
      connected = true;
      purRepositoryServiceRegistry
        .registerService( IRevisionService.class, new UnifiedRepositoryRevisionService( pur, getRootRef() ) );
      purRepositoryServiceRegistry.registerService( ILockService.class, new UnifiedRepositoryLockService( pur ) );
      purRepositoryServiceRegistry
        .registerService( IAclService.class, new UnifiedRepositoryConnectionAclService( pur ) );
      metaStore = new PurRepositoryMetaStore( this );
      try {
        metaStore.createNamespace( PentahoDefaults.NAMESPACE );
      } catch ( MetaStoreException e ) {
        log.logError( BaseMessages
            .getString( PKG, "PurRepositoryMetastore.NamespaceCreateException.Message", PentahoDefaults.NAMESPACE ),
          e );
      }
      this.user = new EEUserInfo( username, password, username, "test user", true );
      this.user.setAdmin( true );
      this.jobDelegate = new JobDelegate( this, pur );
      this.transDelegate = new TransDelegate( this, pur );
      this.unifiedRepositoryLockService = new UnifiedRepositoryLockService( pur );
      return;
    }
    try {
      if ( log != null && purRepositoryConnector != null && purRepositoryConnector.getLog() != null ) {
        purRepositoryConnector.getLog().setLogLevel( log.getLogLevel() );
      }
      RepositoryConnectResult result = purRepositoryConnector.connect( username, password );
      this.user = result.getUser();
      this.connected = result.isSuccess();
      this.securityProvider = result.getSecurityProvider();
      this.securityManager = result.getSecurityManager();
      IUnifiedRepository r = result.getUnifiedRepository();
      try {
        this.pur =
          (IUnifiedRepository) Proxy
            .newProxyInstance( r.getClass().getClassLoader(), new Class<?>[] { IUnifiedRepository.class },
              new UnifiedRepositoryInvocationHandler<IUnifiedRepository>( r ) );
        if ( this.securityProvider != null ) {
          this.securityProvider = (RepositorySecurityProvider)
            Proxy.newProxyInstance( this.securityProvider.getClass().getClassLoader(),
              new Class<?>[] {  RepositorySecurityProvider.class },
              new UnifiedRepositoryInvocationHandler<RepositorySecurityProvider>( this.securityProvider ) );
        }
      } catch ( Throwable th ) {
        if ( log.isError() ) {
          log.logError( "Failed to setup repository connection", th );
        }
        connected = false;
      }
      this.unifiedRepositoryLockService = new UnifiedRepositoryLockService( pur );
      this.connectMessage = result.getConnectMessage();
      this.purRepositoryServiceRegistry = result.repositoryServiceRegistry();
      this.transDelegate = new TransDelegate( this, pur );
      this.jobDelegate = new JobDelegate( this, pur );
    } finally {
      if ( connected ) {
        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "PurRepositoryMetastore.Create.Message" ) );
        }
        metaStore = new PurRepositoryMetaStore( this );
        IMetaStore activeMetaStore = metaStore;
        if ( activeMetaStore != null ) {
          final IMetaStore connectedMetaStore = activeMetaStore;
          connectionManager.setMetastoreSupplier( () -> connectedMetaStore );
        }

        // Create the default Pentaho namespace if it does not exist
        try {
          metaStore.createNamespace( PentahoDefaults.NAMESPACE );
          if ( log.isDetailed() ) {
            log.logDetailed( BaseMessages
              .getString( PKG, "PurRepositoryMetastore.NamespaceCreateSuccess.Message", PentahoDefaults.NAMESPACE ) );
          }
        } catch ( MetaStoreNamespaceExistsException e ) {
          // Ignore this exception, we only use it to save a call to check if the namespace exists, as the
          // createNamespace()
          // call will do the check for us and throw this exception.
        } catch ( MetaStoreException e ) {
          log.logError( BaseMessages
              .getString( PKG, "PurRepositoryMetastore.NamespaceCreateException.Message", PentahoDefaults.NAMESPACE ),
            e );
        }

        if ( log.isDetailed() ) {
          log.logDetailed( BaseMessages.getString( PKG, "PurRepository.ConnectSuccess.Message" ) );
        }
      }
    }
  }

  @Override public boolean isConnected() {
    return connected;
  }

  @Override public void disconnect() {
    connected = false;
    metaStore = null;
    IMetaStore activeMetaStore = null;
    try {
      activeMetaStore = MetaStoreConst.openLocalPentahoMetaStore();
    } catch ( MetaStoreException e ) {
      activeMetaStore = null;
    }
    if ( activeMetaStore != null ) {
      final IMetaStore connectedMetaStore = activeMetaStore;
      connectionManager.setMetastoreSupplier( () -> connectedMetaStore );
    }

    purRepositoryConnector.disconnect();
  }

  @Override public Optional<URI> getUri() {
    String url = Optional.ofNullable( repositoryMeta.getRepositoryLocation() )
      .orElseThrow( () -> new IllegalStateException( getName() + " does not have a defined location." ) )
      .getUrl();
    try {
      return Optional.of( new URI( url ) );
    } catch ( URISyntaxException e ) {
      log.logError( e.getMessage(), e );
    }
    return Optional.empty();
  }


  @Override public int countNrJobEntryAttributes( ObjectId idJobentry, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override public int countNrStepAttributes( ObjectId idStep, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public RepositoryDirectoryInterface createRepositoryDirectory( final RepositoryDirectoryInterface parentDirectory,
                                                                 final String directoryPath ) throws KettleException {
    // allow new folders only inside Home and Public directory, else - provide UI message
    try {
      // if parentDirectory is root then we have to check if the defined directory is valid ( only Public and Home folders are allowed )
      if ( parentDirectory.isRoot()
        &&
          ( directoryPath.equals( Import.ROOT_DIRECTORY )
            ||
          ( !directoryPath.startsWith( Import.HOME_DIRECTORY ) && !directoryPath.startsWith( Import.PUBLIC_DIRECTORY ) )
          ) ) {
        throw new KettleException( BaseMessages.getString( PKG, "PurRepository.invalidRepositoryDirectory", directoryPath ) );
      }

      RepositoryDirectoryInterface refreshedParentDir = findDirectory( parentDirectory.getPath() );

      // update the passed in repository directory with the children recently loaded from the repo
      parentDirectory.setChildren( refreshedParentDir.getChildren() );
      String[] path = Const.splitPath( directoryPath, RepositoryDirectory.DIRECTORY_SEPARATOR );

      RepositoryDirectoryInterface follow = parentDirectory;

      for ( int level = 0; level < path.length; level++ ) {
        RepositoryDirectoryInterface child = follow.findChild( path[level] );
        if ( child == null ) {
          // create this one
          child = new RepositoryDirectory( follow, path[level] );
          saveRepositoryDirectory( child );
          // link this with the parent directory
          follow.addSubdirectory( child );
        }

        follow = child;
      }
      return follow;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to create directory with path [" + directoryPath + "]", e );
    }
  }

  @Override public void saveRepositoryDirectory( final RepositoryDirectoryInterface dir ) throws KettleException {
    // id of root dir is null--check for it
    if ( "/".equals( dir.getParent().getName() ) ) {
      throw new KettleException( BaseMessages.getString( PKG, "PurRepository.FailedDirectoryCreation.Message" ) );
    }

    readWriteLock.writeLock().lock();
    try {
      RepositoryFile
        newFolder =
        pur.createFolder( dir.getParent().getObjectId() != null ? dir.getParent().getObjectId().getId() : null,
          new RepositoryFile.Builder( dir.getName() ).folder( true ).build(), null );

      dir.setObjectId( new StringObjectId( newFolder.getId().toString() ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save repository directory with path [" + getPath( null, dir, null ) + "]",
        e );
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * Determine if "baseFolder" is the same as "folder" or if "folder" is a descendant of "baseFolder"
   *
   * @param folder
   *          Folder to test for similarity / ancestory; Must not be null
   * @param baseFolder
   *          Folder that may be the same or an ancestor; Must not be null
   * @return True if folder is a descendant of baseFolder or False if not; False if either folder or baseFolder are null
   */
  protected boolean isSameOrAncestorFolder( RepositoryFile folder, RepositoryFile baseFolder ) {
    // If either folder is null, return false. We cannot do a proper comparison
    if ( folder != null && baseFolder != null ) {

      if (
        // If the folders are equal
        baseFolder.getId().equals( folder.getId() ) || (
          // OR if the folders are NOT siblings AND the folder to move IS an ancestor to the users home folder
          baseFolder.getPath().lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR )
            != folder.getPath().lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR )
            && baseFolder.getPath().startsWith( folder.getPath() ) ) ) {
        return true;
      }

    }
    return false;
  }

  /**
   * Test to see if the folder is a user's home directory If it is an ancestor to a user's home directory, false will be
   * returned. (It is not actually a user's home directory)
   *
   * @param folder
   *          The folder to test; Must not be null
   * @return True if the directory is a users home directory and False if it is not; False if folder is null
   */
  protected boolean isUserHomeDirectory( RepositoryFile folder ) {
    if ( folder != null ) {

      // Get the root of all home folders
      readWriteLock.readLock().lock();
      RepositoryFile homeRootFolder;
      try {
        homeRootFolder = pur.getFile( ClientRepositoryPaths.getHomeFolderPath() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      if ( homeRootFolder != null ) {
        // Strip the final RepositoryDirectory.DIRECTORY_SEPARATOR from the paths
        String temp = homeRootFolder.getPath();
        String
          homeRootPath =
          temp.endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR )
            && temp.length() > RepositoryDirectory.DIRECTORY_SEPARATOR.length()
            ? temp.substring( 0, temp.length() - RepositoryDirectory.DIRECTORY_SEPARATOR.length() ) : temp;
        temp = folder.getPath();
        String
          folderPath =
          temp.endsWith( RepositoryDirectory.DIRECTORY_SEPARATOR )
            && temp.length() > RepositoryDirectory.DIRECTORY_SEPARATOR.length()
            ? temp.substring( 0, temp.length() - RepositoryDirectory.DIRECTORY_SEPARATOR.length() ) : temp;

        // Is the folder in a user's home directory?
        if ( folderPath.startsWith( homeRootPath ) ) {
          if ( folderPath.equals( homeRootPath ) ) {
            return false;
          }

          // If there is exactly one more RepositoryDirectory.DIRECTORY_SEPARATOR in folderPath than homeRootFolder,
          // then the user is trying to delete another user's home directory
          int folderPathDirCount = 0;
          int homeRootPathDirCount = 0;

          for ( int x = 0; x >= 0; folderPathDirCount++ ) {
            x = folderPath.indexOf( RepositoryDirectory.DIRECTORY_SEPARATOR, x + 1 );
          }
          for ( int x = 0; x >= 0; homeRootPathDirCount++ ) {
            x = homeRootPath.indexOf( RepositoryDirectory.DIRECTORY_SEPARATOR, x + 1 );
          }

          if ( folderPathDirCount == ( homeRootPathDirCount + 1 ) ) {
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public void deleteRepositoryDirectory( final RepositoryDirectoryInterface dir ) throws KettleException {
    deleteRepositoryDirectory( dir, false );
  }

  @Override
  public void deleteRepositoryDirectory( final RepositoryDirectoryInterface dir, final boolean deleteHomeDirectories )
    throws KettleException {

    readWriteLock.writeLock().lock();
    try {
      // Fetch the folder to be deleted
      RepositoryFile folder;
      RepositoryFile homeFolder;

      folder = pur.getFileById( dir.getObjectId().getId() );
      // Fetch the user's home directory
      homeFolder = pur.getFile( ClientRepositoryPaths.getUserHomeFolderPath( user.getLogin() ) );

      // Make sure the user is not trying to delete their own home directory
      if ( isSameOrAncestorFolder( folder, homeFolder ) ) {
        // Then throw an exception that the user cannot delete their own home directory
        throw new KettleException( "You are not allowed to delete your home folder." );
      }

      if ( !deleteHomeDirectories && isUserHomeDirectory( folder ) ) {
        throw new RepositoryObjectAccessException( "Cannot delete another users home directory",
          RepositoryObjectAccessException.AccessExceptionType.USER_HOME_DIR );
      }

      pur.deleteFile( dir.getObjectId().getId(), null );
      rootRef.clearRef();
    } catch ( Exception e ) {
      throw new KettleException( "Unable to delete directory with path [" + getPath( null, dir, null ) + "]", e );
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public ObjectId renameRepositoryDirectory( final ObjectId dirId, final RepositoryDirectoryInterface newParent,
                                             final String newName ) throws KettleException {
    return renameRepositoryDirectory( dirId, newParent, newName, false );
  }

  @Override
  public ObjectId renameRepositoryDirectory( final ObjectId dirId, final RepositoryDirectoryInterface newParent,
                                             final String newName, final boolean renameHomeDirectories )
    throws KettleException {
    // dir ID is used to find orig obj; new parent is used as new parent (might be null meaning no change in parent);
    // new name is used as new file name (might be null meaning no change in name)
    String finalName = null;
    String finalParentPath = null;
    String interimFolderPath = null;

    readWriteLock.writeLock().lock();
    try {
      RepositoryFile homeFolder;
      RepositoryFile folder;

      homeFolder = pur.getFile( ClientRepositoryPaths.getUserHomeFolderPath( user.getLogin() ) );
      folder = pur.getFileById( dirId.getId() );

      finalName = ( newName != null ? newName : folder.getName() );
      interimFolderPath = getParentPath( folder.getPath() );
      finalParentPath = ( newParent != null ? getPath( null, newParent, null ) : interimFolderPath );
      // Make sure the user is not trying to move their own home directory
      if ( isSameOrAncestorFolder( folder, homeFolder ) ) {
        // Then throw an exception that the user cannot move their own home directory
        throw new KettleException( "You are not allowed to move/rename your home folder." );
      }

      if ( !renameHomeDirectories && isUserHomeDirectory( folder ) ) {
        throw new RepositoryObjectAccessException( "Cannot move another users home directory",
          RepositoryObjectAccessException.AccessExceptionType.USER_HOME_DIR );
      }

      pur.moveFile( dirId.getId(), finalParentPath + RepositoryFile.SEPARATOR + finalName, null );

      rootRef.clearRef();
      return dirId;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to move/rename directory with id [" + dirId + "] to new parent ["
        + finalParentPath + "] and new name [" + finalName + "]", e );
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  protected RepositoryFileTree loadRepositoryFileTree( String path ) {
    readWriteLock.readLock().lock();
    RepositoryFileTree result;
    try {
      result = pur.getTree( path, -1, null, true );
    } finally {
      readWriteLock.readLock().unlock();
    }

    return result;
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree(
    String path,
    String filter,
    int depth,
    boolean showHidden,
    boolean includeEmptyFolder,
    boolean includeAcls )
    throws KettleException {

    // First check for possibility of speedy algorithm
    if ( filter == null && "/".equals( path ) && includeEmptyFolder ) {
      return initRepositoryDirectoryTree( loadRepositoryFileTreeFolders( "/", -1, includeAcls, showHidden ) );
    }
    //load count levels from root to destination path to load folder tree
    int fromRootToDest = StringUtils.countMatches( path, "/" );
    //create new root directory "/"
    RepositoryDirectory dir = new RepositoryDirectory();
    //fetch folder tree from root "/" to destination path for populate folder
    RepositoryFileTree rootDirTree =
      loadRepositoryFileTree( "/", "*", fromRootToDest, showHidden, includeAcls, FILES_TYPE_FILTER.FOLDERS );
    //populate directory by folder tree
    fillRepositoryDirectoryFromTree( dir, rootDirTree );

    RepositoryDirectoryInterface destinationDir = dir.findDirectory( path );
    //search for goal path and filter
    RepositoryFileTree repoTree =
      loadRepositoryFileTree( path, filter, depth, showHidden, includeAcls, FILES_TYPE_FILTER.FILES_FOLDERS );
    //populate the directory with founded files and subdirectories with files
    fillRepositoryDirectoryFromTree( destinationDir, repoTree );

    if ( includeEmptyFolder ) {
      RepositoryDirectoryInterface folders =
        initRepositoryDirectoryTree(
          loadRepositoryFileTree( path, null, depth, showHidden, includeAcls, FILES_TYPE_FILTER.FOLDERS ) );
      return copyFrom( folders, destinationDir );
    } else {
      return destinationDir;
    }
  }

  private RepositoryFileTree loadRepositoryFileTree(
    String path,
    String filter,
    int depth,
    boolean showHidden,
    boolean includeAcls,
    FILES_TYPE_FILTER types ) {
    RepositoryRequest repoRequest = new RepositoryRequest();
    repoRequest.setPath( Utils.isEmpty( path ) ? "/" : path );
    repoRequest.setChildNodeFilter( filter == null ? "*" : filter );
    repoRequest.setDepth( depth );
    repoRequest.setShowHidden( showHidden );
    repoRequest.setIncludeAcls( includeAcls );
    repoRequest.setTypes( types == null ? FILES_TYPE_FILTER.FILES_FOLDERS : types );

    readWriteLock.readLock().lock();
    RepositoryFileTree fileTree;
    try {
      fileTree = pur.getTree( repoRequest );
    } finally {
      readWriteLock.readLock().unlock();
    }

    return fileTree;
  }

  // copies repo objects into folder struct on left
  private RepositoryDirectoryInterface copyFrom( RepositoryDirectoryInterface folders, RepositoryDirectoryInterface withFiles ) {
    if ( folders.getName().equals( withFiles.getName() ) ) {
      for ( RepositoryDirectoryInterface dir2 : withFiles.getChildren() ) {
        for ( RepositoryDirectoryInterface dir1 : folders.getChildren() ) {
          copyFrom( dir1, dir2 );
        }
      }
      folders.setRepositoryObjects( withFiles.getRepositoryObjects() );
    }
    return folders;
  }

  @Deprecated
  @Override public RepositoryDirectoryInterface loadRepositoryDirectoryTree( boolean eager ) throws KettleException {

    // this method forces a reload of the repository directory tree structure
    // a new rootRef will be obtained - this is a SoftReference which will be used
    // by any calls to getRootDir()

    RepositoryDirectoryInterface rootDir;
    if ( eager ) {
      RepositoryFileTree rootFileTree = loadRepositoryFileTree( ClientRepositoryPaths.getRootFolderPath() );
      rootDir = initRepositoryDirectoryTree( rootFileTree );
    } else {
      readWriteLock.readLock().lock();
      RepositoryFile root;
      try {
        root = pur.getFile( "/" );
      } finally {
        readWriteLock.readLock().unlock();
      }

      IUser user = this.getUserInfo();
      boolean showHidden = user != null ? user.isAdmin() : true;

      rootDir =
        new LazyUnifiedRepositoryDirectory( root, null, pur, purRepositoryServiceRegistry, showHidden );
    }
    rootRef.setRef( rootDir );
    return rootDir;
  }

  @Override
  public RepositoryDirectoryInterface loadRepositoryDirectoryTree() throws KettleException {
    return loadRepositoryDirectoryTree( isLoadingEager() );
  }

  private boolean isLoadingEager() {
    return "false".equals( System.getProperty( LAZY_REPOSITORY ) );
  }


  private RepositoryDirectoryInterface initRepositoryDirectoryTree( RepositoryFileTree repoTree )
    throws KettleException {
    RepositoryFile rootFolder = repoTree.getFile();
    RepositoryDirectory rootDir = new RepositoryDirectory();
    rootDir.setObjectId( new StringObjectId( rootFolder.getId().toString() ) );
    fillRepositoryDirectoryFromTree( rootDir, repoTree );

    // Example: /etc
    RepositoryDirectory etcDir = rootDir.findDirectory( ClientRepositoryPaths.getEtcFolderPath() );

    RepositoryDirectory newRoot = new RepositoryDirectory();
    newRoot.setObjectId( rootDir.getObjectId() );
    newRoot.setVisible( false );

    for ( int i = 0; i < rootDir.getNrSubdirectories(); i++ ) {
      RepositoryDirectory childDir = rootDir.getSubdirectory( i );
      // Don't show /etc
      boolean isEtcChild = childDir.equals( etcDir );
      if ( isEtcChild ) {
        continue;
      }
      newRoot.addSubdirectory( childDir );
    }
    return newRoot;
  }

  private void fillRepositoryDirectoryFromTree( final RepositoryDirectoryInterface parentDir,
                                                final RepositoryFileTree treeNode ) throws KettleException {
    try {
      List<RepositoryElementMetaInterface> fileChildren = new ArrayList<RepositoryElementMetaInterface>();
      List<RepositoryFileTree> children = treeNode.getChildren();
      if ( children != null ) {
        for ( RepositoryFileTree child : children ) {
          if ( child.getFile().isFolder() ) {
            RepositoryDirectory dir = new RepositoryDirectory( parentDir, child.getFile().getName() );
            dir.setObjectId( new StringObjectId( child.getFile().getId().toString() ) );
            parentDir.addSubdirectory( dir );
            fillRepositoryDirectoryFromTree( dir, child );
          } else {
            // a real file, like a Transformation or Job
            RepositoryLock lock = unifiedRepositoryLockService.getLock( child.getFile() );
            RepositoryObjectType objectType = getObjectType( child.getFile().getName() );

            fileChildren.add( new EERepositoryObject( child, parentDir, null, objectType, null, lock, false ) );
          }
        }
        parentDir.setRepositoryObjects( fileChildren );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load directory structure from repository", e );
    }
  }

  @Override
  public String[] getDirectoryNames( final ObjectId idDirectory ) throws KettleException {
    try {
      readWriteLock.readLock().lock();
      List<RepositoryFile> children;
      try {
        children = pur.getChildren( idDirectory.getId() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      List<String> childNames = new ArrayList<String>();
      for ( RepositoryFile child : children ) {
        if ( child.isFolder() ) {
          childNames.add( child.getName() );
        }
      }
      return childNames.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get list of object names from directory [" + idDirectory + "]", e );
    }
  }

  @Override
  public void deleteClusterSchema( ObjectId idCluster ) throws KettleException {
    permanentlyDeleteSharedObject( idCluster );
    removeFromSharedObjectCache( RepositoryObjectType.CLUSTER_SCHEMA, idCluster );
  }

  @Override
  public void deleteJob( ObjectId idJob ) throws KettleException {
    deleteFileById( idJob );
  }

  protected void permanentlyDeleteSharedObject( final ObjectId id ) throws KettleException {
    try {
      readWriteLock.writeLock().lock();
      try {
        pur.deleteFile( id.getId(), true, null );
      } finally {
        readWriteLock.writeLock().unlock();
      }

    } catch ( Exception e ) {
      throw new KettleException( "Unable to delete object with id [" + id + "]", e );
    }
  }

  public void deleteFileById( final ObjectId id ) throws KettleException {
    try {
      readWriteLock.writeLock().lock();
      try {
        pur.deleteFile( id.getId(), null );
      } finally {
        readWriteLock.writeLock().unlock();
      }

      rootRef.clearRef();
    } catch ( Exception e ) {
      throw new KettleException( "Unable to delete object with id [" + id + "]", e );
    }
  }

  @Override
  public void deletePartitionSchema( ObjectId idPartitionSchema ) throws KettleException {
    permanentlyDeleteSharedObject( idPartitionSchema );
    removeFromSharedObjectCache( RepositoryObjectType.PARTITION_SCHEMA, idPartitionSchema );
  }

  @Override
  public void deleteSlave( ObjectId idSlave ) throws KettleException {
    permanentlyDeleteSharedObject( idSlave );
    removeFromSharedObjectCache( RepositoryObjectType.SLAVE_SERVER, idSlave );
  }

  @Override
  public void deleteTransformation( ObjectId idTransformation ) throws KettleException {
    deleteFileById( idTransformation );
    rootRef.clearRef();
  }

  @Override
  public boolean exists( final String name, final RepositoryDirectoryInterface repositoryDirectory,
                         final RepositoryObjectType objectType ) throws KettleException {
    try {
      String absPath = getPath( name, repositoryDirectory, objectType );
      readWriteLock.readLock().lock();
      boolean result;
      try {
        result = pur.getFile( absPath ) != null;
      } finally {
        readWriteLock.readLock().unlock();
      }

      return result;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to verify if the repository element [" + name + "] exists in ", e );
    }
  }

  protected String getPath( final String name, final RepositoryDirectoryInterface repositoryDirectory,
                          final RepositoryObjectType objectType ) {

    String path = null;

    // need to check for null id since shared objects return a non-null repoDir (see
    // partSchema.getRepositoryDirectory())
    if ( repositoryDirectory != null && repositoryDirectory.getObjectId() != null ) {
      path = repositoryDirectory.getPath();
    }

    // return the directory path
    if ( objectType == null ) {
      return path;
    }

    String sanitizedName = checkAndSanitize( name );

    switch ( objectType ) {
      case DATABASE: {
        return getDatabaseMetaParentFolderPath() + RepositoryFile.SEPARATOR + sanitizedName
          + RepositoryObjectType.DATABASE.getExtension();
      }
      case PARTITION_SCHEMA: {
        return getPartitionSchemaParentFolderPath() + RepositoryFile.SEPARATOR + sanitizedName
          + RepositoryObjectType.PARTITION_SCHEMA.getExtension();
      }
      case SLAVE_SERVER: {
        return getSlaveServerParentFolderPath() + RepositoryFile.SEPARATOR + sanitizedName
          + RepositoryObjectType.SLAVE_SERVER.getExtension();
      }
      case CLUSTER_SCHEMA: {
        return getClusterSchemaParentFolderPath() + RepositoryFile.SEPARATOR + sanitizedName
          + RepositoryObjectType.CLUSTER_SCHEMA.getExtension();
      }
      case TRANSFORMATION:
      case JOB: {
        // Check for null path
        if ( path == null ) {
          return null;
        } else {
          String processedPath = path + ( path.endsWith( RepositoryFile.SEPARATOR ) ? "" : RepositoryFile.SEPARATOR ) + sanitizedName;

          if ( System.getProperty( Const.KETTLE_COMPATIBILITY_INVOKE_FILES_WITH_OR_WITHOUT_FILE_EXTENSION, "Y" ).equals( "Y" ) ) {
            processedPath = processedPath + ( sanitizedName.endsWith( objectType.getExtension() ) ? "" : objectType.getExtension() );
          } else {
            processedPath = processedPath + objectType.getExtension();
          }

          return processedPath;
        }
      }
      default: {
        throw new UnsupportedOperationException( "not implemented" );
      }
    }
  }

  @Override
  public ObjectId getClusterID( String name ) throws KettleException {
    try {
      return getObjectId( name, null, RepositoryObjectType.CLUSTER_SCHEMA, false );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get ID for cluster schema [" + name + "]", e );
    }
  }

  @Override
  public ObjectId[] getClusterIDs( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.CLUSTER_SCHEMA, includeDeleted );
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for ( RepositoryFile file : children ) {
        ids.add( new StringObjectId( file.getId().toString() ) );
      }
      return ids.toArray( new ObjectId[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all cluster schema IDs", e );
    }
  }

  @Override
  public String[] getClusterNames( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.CLUSTER_SCHEMA, includeDeleted );
      List<String> names = new ArrayList<String>();
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all cluster schema names", e );
    }
  }

  @Override
  public ObjectId getDatabaseID( final String name ) throws KettleException {
    try {
      ObjectId objectId = getObjectId( name, null, RepositoryObjectType.DATABASE, false );
      if ( objectId == null ) {
        List<RepositoryFile> allDatabases = getAllFilesOfType( null, RepositoryObjectType.DATABASE, false );
        String[] existingNames = new String[ allDatabases.size() ];
        for ( int i = 0; i < allDatabases.size(); i++ ) {
          RepositoryFile file = allDatabases.get( i );
          existingNames[ i ] = file.getTitle();
        }
        int index = DatabaseMeta.indexOfName( existingNames, name );
        if ( index != -1 ) {
          return new StringObjectId( allDatabases.get( index ).getId().toString() );
        }
      }
      return objectId;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get ID for database [" + name + "]", e );
    }
  }

  /**
   * Copying the behavior of the original JCRRepository, this implementation returns IDs of deleted objects too.
   */
  private ObjectId getObjectId( final String name, final RepositoryDirectoryInterface dir,
                                final RepositoryObjectType objectType, boolean includedDeleteFiles ) {
    final String absPath = getPath( name, dir, objectType );

    readWriteLock.readLock().lock();
    try {
      RepositoryFile file;
      file = pur.getFile( absPath );

      if ( file != null ) {
        // file exists
        return new StringObjectId( file.getId().toString() );
      } else if ( includedDeleteFiles ) {
        switch ( objectType ) {
          case DATABASE: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( getDatabaseMetaParentFolderPath(), name
                + RepositoryObjectType.DATABASE.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          case TRANSFORMATION: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( dir.getObjectId().getId(),
                name + RepositoryObjectType.TRANSFORMATION.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          case PARTITION_SCHEMA: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( getPartitionSchemaParentFolderPath(), name
                + RepositoryObjectType.PARTITION_SCHEMA.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          case SLAVE_SERVER: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( getSlaveServerParentFolderPath(), name
                + RepositoryObjectType.SLAVE_SERVER.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          case CLUSTER_SCHEMA: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( getClusterSchemaParentFolderPath(), name
                + RepositoryObjectType.CLUSTER_SCHEMA.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          case JOB: {
            // file either never existed or has been deleted
            List<RepositoryFile>
              deletedChildren =
              pur.getDeletedFiles( dir.getObjectId().getId(), name + RepositoryObjectType.JOB.getExtension() );
            if ( !deletedChildren.isEmpty() ) {
              return new StringObjectId( deletedChildren.get( 0 ).getId().toString() );
            } else {
              return null;
            }
          }
          default: {
            throw new UnsupportedOperationException( "not implemented" );
          }
        }
      } else {
        return null;
      }
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public ObjectId[] getDatabaseIDs( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.DATABASE, includeDeleted );
      List<ObjectId> ids = new ArrayList<ObjectId>( children.size() );
      for ( RepositoryFile file : children ) {
        ids.add( new StringObjectId( file.getId().toString() ) );
      }
      return ids.toArray( new ObjectId[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all database IDs", e );
    }
  }

  protected List<RepositoryFile> getAllFilesOfType( final ObjectId dirId, final RepositoryObjectType objectType,
                                                    final boolean includeDeleted ) throws KettleException {
    return getAllFilesOfType( dirId, Collections.singletonList( objectType ), includeDeleted );
  }

  protected List<RepositoryFile> getAllFilesOfType( final ObjectId dirId, final List<RepositoryObjectType> objectTypes,
                                                    final boolean includeDeleted ) throws KettleException {

    List<RepositoryFile> allChildren = new ArrayList<RepositoryFile>();
    List<RepositoryFile> children = getAllFilesOfType( dirId, objectTypes );
    allChildren.addAll( children );
    if ( includeDeleted ) {
      String dirPath = null;
      if ( dirId != null ) {
        // derive path using id
        readWriteLock.readLock().lock();
        try {
          dirPath = pur.getFileById( dirId.getId() ).getPath();
        } finally {
          readWriteLock.readLock().unlock();
        }
      }
      List<RepositoryFile> deletedChildren = getAllDeletedFilesOfType( dirPath, objectTypes );
      allChildren.addAll( deletedChildren );
      Collections.sort( allChildren );
    }
    return allChildren;
  }

  protected List<RepositoryFile> getAllFilesOfType( final ObjectId dirId, final List<RepositoryObjectType> objectTypes )
    throws KettleException {
    Set<Serializable> parentFolderIds = new HashSet<>();
    List<String> filters = new ArrayList<>();
    for ( RepositoryObjectType objectType : objectTypes ) {
      switch ( objectType ) {
        case DATABASE: {
          parentFolderIds.add( getDatabaseMetaParentFolderId() );
          filters.add( "*" + RepositoryObjectType.DATABASE.getExtension() ); //$NON-NLS-1$
          break;
        }
        case TRANSFORMATION: {
          parentFolderIds.add( dirId.getId() );
          filters.add( "*" + RepositoryObjectType.TRANSFORMATION.getExtension() ); //$NON-NLS-1$
          break;
        }
        case PARTITION_SCHEMA: {
          parentFolderIds.add( getPartitionSchemaParentFolderId() );
          filters.add( "*" + RepositoryObjectType.PARTITION_SCHEMA.getExtension() ); //$NON-NLS-1$
          break;
        }
        case SLAVE_SERVER: {
          parentFolderIds.add( getSlaveServerParentFolderId() );
          filters.add( "*" + RepositoryObjectType.SLAVE_SERVER.getExtension() ); //$NON-NLS-1$
          break;
        }
        case CLUSTER_SCHEMA: {
          parentFolderIds.add( getClusterSchemaParentFolderId() );
          filters.add( "*" + RepositoryObjectType.CLUSTER_SCHEMA.getExtension() ); //$NON-NLS-1$
          break;
        }
        case JOB: {
          parentFolderIds.add( dirId.getId() );
          filters.add( "*" + RepositoryObjectType.JOB.getExtension() ); //$NON-NLS-1$
          break;
        }
        case TRANS_DATA_SERVICE: {
          parentFolderIds.add( dirId.getId() );
          filters.add( "*" + RepositoryObjectType.TRANS_DATA_SERVICE.getExtension() ); //$NON-NLS-1$
          break;
        }
        default: {
          throw new UnsupportedOperationException( "not implemented" );
        }
      }
    }
    StringBuilder mergedFilterBuf = new StringBuilder();
    // build filter
    int i = 0;
    for ( String filter : filters ) {
      if ( i++ > 0 ) {
        mergedFilterBuf.append( " | " ); //$NON-NLS-1$
      }
      mergedFilterBuf.append( filter );
    }
    List<RepositoryFile> allFiles = new ArrayList<>();
    for ( Serializable parentFolderId : parentFolderIds ) {
      readWriteLock.readLock().lock();
      try {
        allFiles.addAll( pur.getChildren( parentFolderId, mergedFilterBuf.toString() ) );
      } finally {
        readWriteLock.readLock().unlock();
      }
    }
    Collections.sort( allFiles );
    return allFiles;
  }

  protected List<RepositoryFile> getAllDeletedFilesOfType( final String dirPath,
                                                           final List<RepositoryObjectType> objectTypes )
    throws KettleException {
    Set<String> parentFolderPaths = new HashSet<>();
    List<String> filters = new ArrayList<>();
    for ( RepositoryObjectType objectType : objectTypes ) {
      switch ( objectType ) {
        case DATABASE: {
          parentFolderPaths.add( getDatabaseMetaParentFolderPath() );
          filters.add( "*" + RepositoryObjectType.DATABASE.getExtension() ); //$NON-NLS-1$
          break;
        }
        case TRANSFORMATION: {
          parentFolderPaths.add( dirPath );
          filters.add( "*" + RepositoryObjectType.TRANSFORMATION.getExtension() ); //$NON-NLS-1$
          break;
        }
        case PARTITION_SCHEMA: {
          parentFolderPaths.add( getPartitionSchemaParentFolderPath() );
          filters.add( "*" + RepositoryObjectType.PARTITION_SCHEMA.getExtension() ); //$NON-NLS-1$
          break;
        }
        case SLAVE_SERVER: {
          parentFolderPaths.add( getSlaveServerParentFolderPath() );
          filters.add( "*" + RepositoryObjectType.SLAVE_SERVER.getExtension() ); //$NON-NLS-1$
          break;
        }
        case CLUSTER_SCHEMA: {
          parentFolderPaths.add( getClusterSchemaParentFolderPath() );
          filters.add( "*" + RepositoryObjectType.CLUSTER_SCHEMA.getExtension() ); //$NON-NLS-1$
          break;
        }
        case JOB: {
          parentFolderPaths.add( dirPath );
          filters.add( "*" + RepositoryObjectType.JOB.getExtension() ); //$NON-NLS-1$
          break;
        }
        default: {
          throw new UnsupportedOperationException();
        }
      }
    }
    StringBuilder mergedFilterBuf = new StringBuilder();
    // build filter
    int i = 0;
    for ( String filter : filters ) {
      if ( i++ > 0 ) {
        mergedFilterBuf.append( " | " ); //$NON-NLS-1$
      }
      mergedFilterBuf.append( filter );
    }

    List<RepositoryFile> allFiles = new ArrayList<RepositoryFile>();
    readWriteLock.readLock().lock();
    try {
      for ( String parentFolderPath : parentFolderPaths ) {

        allFiles.addAll( pur.getDeletedFiles( parentFolderPath, mergedFilterBuf.toString() ) );
      }
    } finally {
      readWriteLock.readLock().unlock();
    }

    Collections.sort( allFiles );
    return allFiles;
  }

  @Override
  public String[] getDatabaseNames( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.DATABASE, includeDeleted );
      List<String> names = new ArrayList<String>( children.size() );
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all database names", e );
    }
  }

  /**
   * Initialize the shared object assembler map with known assemblers
   */
  private void initSharedObjectAssemblerMap() {
    sharedObjectAssemblerMap =
      new EnumMap<RepositoryObjectType, SharedObjectAssembler<?>>( RepositoryObjectType.class );
    sharedObjectAssemblerMap.put( RepositoryObjectType.DATABASE, databaseMetaTransformer );
    sharedObjectAssemblerMap.put( RepositoryObjectType.CLUSTER_SCHEMA, clusterTransformer );
    sharedObjectAssemblerMap.put( RepositoryObjectType.PARTITION_SCHEMA, partitionSchemaTransformer );
    sharedObjectAssemblerMap.put( RepositoryObjectType.SLAVE_SERVER, slaveTransformer );
  }

  public DatabaseDelegate getDatabaseMetaTransformer() {
    return databaseMetaTransformer;
  }

  public ClusterDelegate getClusterTransformer() {
    return clusterTransformer;
  }

  public PartitionDelegate getPartitionSchemaTransformer() {
    return partitionSchemaTransformer;
  }

  @Override
  public void clearSharedObjectCache() {
    sharedObjectsLock.writeLock().lock();
    try {
      sharedObjectsByType = null;
    } finally {
      sharedObjectsLock.writeLock().unlock();
    }
  }

  /**
   * Read shared objects of the types provided from the repository. Every {@link SharedObjectInterface} that is read
   * will be fully loaded as if it has been loaded through {@link #loadDatabaseMeta(ObjectId, String)},
   * {@link #loadClusterSchema(ObjectId, List, String)}, etc.
   * <p>
   * This method was introduced to reduce the number of server calls for loading shared objects to a constant number:
   * {@code 2 + n, where n is the number of types requested}.
   * </p>
   *
   * @param sharedObjectsByType
   *          Map of type to shared objects. Each map entry will contain a non-null {@link List} of
   *          {@link RepositoryObjectType}s for every type provided. Only entries for types provided will be altered.
   * @param types
   *          Types of repository objects to read from the repository
   * @throws KettleException
   */
  protected void readSharedObjects(
    Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjectsByType,
    RepositoryObjectType... types ) throws KettleException {
    // Overview:
    // 1) We will fetch RepositoryFile, NodeRepositoryFileData, and VersionSummary for all types provided.
    // 2) We assume that unless an exception is thrown every RepositoryFile returned by getFilesByType(..) have a
    // matching NodeRepositoryFileData and VersionSummary.
    // 3) With all files, node data, and versions in hand we will iterate over them, merging them back into usable
    // shared objects
    List<RepositoryFile> allFiles = new ArrayList<RepositoryFile>();
    // Since type is not preserved in the RepositoryFile we fetch files by type so we don't rely on parsing the name to
    // determine type afterward
    // Map must be ordered or we can't match up files with data and version summary
    LinkedHashMap<RepositoryObjectType, List<RepositoryFile>> filesByType = getFilesByType( allFiles, types );
    readWriteLock.readLock().lock();
    try {
      List<NodeRepositoryFileData> data;
      List<VersionSummary> versions;
      data = pur.getDataForReadInBatch( allFiles, NodeRepositoryFileData.class );
      versions = pur.getVersionSummaryInBatch( allFiles );

      Iterator<NodeRepositoryFileData> dataIter = data.iterator();
      Iterator<VersionSummary> versionsIter = versions.iterator();

      // Assemble into completely loaded SharedObjectInterfaces by type
      for ( Entry<RepositoryObjectType, List<RepositoryFile>> entry : filesByType.entrySet() ) {
        SharedObjectAssembler<?> assembler = sharedObjectAssemblerMap.get( entry.getKey() );
        if ( assembler == null ) {
          throw new UnsupportedOperationException(
            String.format( "Cannot assemble shared object of type [%s]", entry.getKey() ) ); //$NON-NLS-1$
        }
        // For all files of this type, assemble them from the pieces of data pulled from the repository
        Iterator<RepositoryFile> filesIter = entry.getValue().iterator();
        List<SharedObjectInterface> sharedObjects = new ArrayList<SharedObjectInterface>( entry.getValue().size() );
        // Exceptions are thrown during lookup if data or versions aren't found so all the lists should be the same size
        // (no need to check for next on all iterators)
        while ( filesIter.hasNext() ) {
          RepositoryFile file = filesIter.next();
          NodeRepositoryFileData repoData = dataIter.next();
          VersionSummary version = versionsIter.next();

          // TODO: inexistent db types can cause exceptions assembling; prevent total failure
          try {
            sharedObjects.add( assembler.assemble( file, repoData, version ) );
          } catch ( Exception ex ) {
            // TODO i18n
            getLog().logError( "Unable to load shared objects", ex );
          }
        }
        sharedObjectsByType.put( entry.getKey(), sharedObjects );
      }
    } catch ( Exception ex ) {
      // TODO i18n
      throw new KettleException( "Unable to load shared objects", ex ); //$NON-NLS-1$
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  /**
   * Fetch {@link RepositoryFile}s by {@code RepositoryObjectType}.
   *
   * @param allFiles
   *          List to add files into.
   * @param types
   *          Types of files to fetch
   * @return Ordered map of object types to list of files.
   * @throws KettleException
   */
  private LinkedHashMap<RepositoryObjectType, List<RepositoryFile>> getFilesByType( List<RepositoryFile> allFiles,
                                                                                    RepositoryObjectType... types )
    throws KettleException {
    // Must be ordered or we can't match up files with data and version summary
    LinkedHashMap<RepositoryObjectType, List<RepositoryFile>>
      filesByType =
      new LinkedHashMap<RepositoryObjectType, List<RepositoryFile>>();
    // Since type is not preserved in the RepositoryFile we must fetch files by type
    for ( RepositoryObjectType type : types ) {
      try {
        List<RepositoryFile> files = getAllFilesOfType( null, type, false );
        filesByType.put( type, files );
        allFiles.addAll( files );
      } catch ( Exception ex ) {
        // TODO i18n
        throw new KettleException( String.format( "Unable to get all files of type [%s]", type ), ex ); //$NON-NLS-1$
      }
    }
    return filesByType;
  }

  @Override
  public List<DatabaseMeta> readDatabases() throws KettleException {
    readWriteLock.readLock().lock();
    try {
      boolean hasOsgiFolder = new File( SYSTEM_FOLDER ).exists();
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.DATABASE, false );
      List<DatabaseMeta> dbMetas = new ArrayList<>();

      //[PDI-18487] - Amount of POST calls from PDI client connected to Repository
      //Grab data from all objects at once to lower calls to server
      List<NodeRepositoryFileData> data = pur.getDataForReadInBatch( children, NodeRepositoryFileData.class );
      Iterator<NodeRepositoryFileData> dataIter = data.iterator();

      for ( RepositoryFile file : children ) {
        //Both list should be ordered, so the items match
        DataNode node = dataIter.next().getNode();
        if ( !hasOsgiFolder && StringUtils.equals( node.getProperty( "TYPE" ).getString(), "KettleThin" ) ) {
          log.logDetailed( "Unable to find database {" + file.getName() + "}" );
          continue;
        }
        DatabaseMeta databaseMeta = (DatabaseMeta) databaseMetaTransformer.dataNodeToElement( node );
        databaseMeta.setName( file.getTitle() );
        dbMetas.add( databaseMeta );
      }

      return dbMetas;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to read all databases", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public void deleteDatabaseMeta( final String databaseName ) throws KettleException {
    RepositoryFile fileToDelete = null;
    ObjectId idDatabase = null;

    try {
      readWriteLock.writeLock().lock();
      try {
        fileToDelete = pur.getFile( getPath( databaseName, null, RepositoryObjectType.DATABASE ) );
        idDatabase = new StringObjectId( fileToDelete.getId().toString() );
        permanentlyDeleteSharedObject( idDatabase );
        removeFromSharedObjectCache( RepositoryObjectType.DATABASE, idDatabase );
      } finally {
        readWriteLock.writeLock().unlock();
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to delete database with name [" + databaseName + "]", e );
    }
  }

  @Override
  public long getJobEntryAttributeInteger( ObjectId idJobentry, int nr, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public String getJobEntryAttributeString( ObjectId idJobentry, int nr, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean getJobEntryAttributeBoolean( ObjectId arg0, int arg1, String arg2, boolean arg3 )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getJobId( final String name, final RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    try {
      return getObjectId( name, repositoryDirectory, RepositoryObjectType.JOB, false );
    } catch ( Exception e ) {
      String path = repositoryDirectory != null ? repositoryDirectory.toString() : "null";
      throw new IdNotFoundException( "Unable to get ID for job [" + name + "]", e, name, path,
        RepositoryObjectType.JOB );
    }
  }

  @Override
  public String[] getJobNames( ObjectId idDirectory, boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( idDirectory, RepositoryObjectType.JOB, includeDeleted );
      List<String> names = new ArrayList<String>();
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all job names", e );
    }
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobObjects( ObjectId idDirectory, boolean includeDeleted )
    throws KettleException {
    return getPdiObjects( idDirectory, Arrays.asList( new RepositoryObjectType[] { RepositoryObjectType.JOB } ),
      includeDeleted );
  }

  @Override
  public LogChannelInterface getLog() {
    return log;
  }

  @Override
  public String getName() {
    return repositoryMeta.getName();
  }

  @Override
  public ObjectId getPartitionSchemaID( String name ) throws KettleException {
    try {
      return getObjectId( name, null, RepositoryObjectType.PARTITION_SCHEMA, false );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get ID for partition schema [" + name + "]", e );
    }
  }

  @Override
  public ObjectId[] getPartitionSchemaIDs( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.PARTITION_SCHEMA, includeDeleted );
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for ( RepositoryFile file : children ) {
        ids.add( new StringObjectId( file.getId().toString() ) );
      }
      return ids.toArray( new ObjectId[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all partition schema IDs", e );
    }
  }

  @Override
  public String[] getPartitionSchemaNames( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.PARTITION_SCHEMA, includeDeleted );
      List<String> names = new ArrayList<String>();
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all partition schema names", e );
    }
  }

  @Override
  public RepositoryMeta getRepositoryMeta() {
    return repositoryMeta;
  }

  @Override
  public RepositorySecurityProvider getSecurityProvider() {
    return securityProvider;
  }

  @Override
  public RepositorySecurityManager getSecurityManager() {
    return securityManager;
  }

  @Override
  public ObjectId getSlaveID( String name ) throws KettleException {
    try {
      return getObjectId( name, null, RepositoryObjectType.SLAVE_SERVER, false );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get ID for slave server with name [" + name + "]", e );
    }
  }

  @Override
  public ObjectId[] getSlaveIDs( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.SLAVE_SERVER, includeDeleted );
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for ( RepositoryFile file : children ) {
        ids.add( new StringObjectId( file.getId().toString() ) );
      }
      return ids.toArray( new ObjectId[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all slave server IDs", e );
    }
  }

  @Override
  public String[] getSlaveNames( boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile> children = getAllFilesOfType( null, RepositoryObjectType.SLAVE_SERVER, includeDeleted );
      List<String> names = new ArrayList<String>();
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[ 0 ] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all slave server names", e );
    }
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<SlaveServer> getSlaveServers() throws KettleException {
    return (List<SlaveServer>) loadAndCacheSharedObjects( true ).get( RepositoryObjectType.SLAVE_SERVER );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<DatabaseMeta> getConnections( boolean cached ) throws KettleException {
    return (List<DatabaseMeta>) getRepositoryObjects( RepositoryObjectType.DATABASE, cached );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<SlaveServer> getSlaveServers( boolean cached ) throws KettleException {
    return (List<SlaveServer>) getRepositoryObjects( RepositoryObjectType.SLAVE_SERVER, cached );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<PartitionSchema> getPartitions( boolean cached ) throws KettleException {
    return (List<PartitionSchema>) getRepositoryObjects( RepositoryObjectType.PARTITION_SCHEMA, cached );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public List<ClusterSchema> getClusters( boolean cached ) throws KettleException {
    return (List<ClusterSchema>) getRepositoryObjects( RepositoryObjectType.CLUSTER_SCHEMA, cached );
  }

  protected List<?> getRepositoryObjects( RepositoryObjectType repositoryObjectType, boolean cached ) throws KettleException {
    if ( cached ) {
      return loadAndCacheSharedObjects( true ).get( repositoryObjectType );
    } else {
      Map<RepositoryObjectType, List<? extends SharedObjectInterface>> sharedObjects = new EnumMap<>(
        RepositoryObjectType.class );
      readSharedObjects( sharedObjects, repositoryObjectType );
      return deepCopy( sharedObjects ).get( repositoryObjectType );
    }
  }

  public SlaveDelegate getSlaveTransformer() {
    return slaveTransformer;
  }

  @Override
  public boolean getStepAttributeBoolean( ObjectId idStep, int nr, String code, boolean def ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public long getStepAttributeInteger( ObjectId idStep, int nr, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public String getStepAttributeString( ObjectId idStep, int nr, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId getTransformationID( String name, RepositoryDirectoryInterface repositoryDirectory )
    throws KettleException {
    try {
      return getObjectId( name, repositoryDirectory, RepositoryObjectType.TRANSFORMATION, false );
    } catch ( Exception e ) {
      String path = repositoryDirectory != null ? repositoryDirectory.toString() : "null";
      throw new IdNotFoundException( "Unable to get ID for job [" + name + "]", e, name, path,
        RepositoryObjectType.TRANSFORMATION );
    }
  }

  @Override
  public String[] getTransformationNames( ObjectId idDirectory, boolean includeDeleted ) throws KettleException {
    try {
      List<RepositoryFile>
        children =
        getAllFilesOfType( idDirectory, RepositoryObjectType.TRANSFORMATION, includeDeleted );
      List<String> names = new ArrayList<String>();
      for ( RepositoryFile file : children ) {
        names.add( file.getTitle() );
      }
      return names.toArray( new String[0] );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get all transformation names", e );
    }
  }

  @Override
  public List<RepositoryElementMetaInterface> getTransformationObjects( ObjectId idDirectory, boolean includeDeleted )
    throws KettleException {
    return getPdiObjects( idDirectory, Arrays
      .asList( new RepositoryObjectType[] { RepositoryObjectType.TRANSFORMATION } ), includeDeleted );
  }

  protected List<RepositoryElementMetaInterface> getPdiObjects( ObjectId dirId, List<RepositoryObjectType> objectTypes,
                                                                boolean includeDeleted ) throws KettleException {

    readWriteLock.readLock().lock();
    try {
      RepositoryFile dirFile;
      dirFile = pur.getFileById( dirId.getId() );


      RepositoryDirectory repDir = new RepositoryDirectory();
      repDir.setObjectId( dirId );
      repDir.setName( dirFile.getName() );

      List<RepositoryElementMetaInterface> list = new ArrayList<RepositoryElementMetaInterface>();
      List<RepositoryFile> nonDeletedChildren = getAllFilesOfType( dirId, objectTypes );
      for ( RepositoryFile file : nonDeletedChildren ) {
        RepositoryLock lock = unifiedRepositoryLockService.getLock( file );
        RepositoryObjectType objectType = getObjectType( file.getName() );

        list.add( new EERepositoryObject( file, repDir, null, objectType, null, lock, false ) );
      }
      if ( includeDeleted ) {
        String dirPath = null;
        if ( dirId != null ) {
          // derive path using id
          dirPath = pur.getFileById( dirId.getId() ).getPath();
        }

        List<RepositoryFile> deletedChildren = getAllDeletedFilesOfType( dirPath, objectTypes );
        for ( RepositoryFile file : deletedChildren ) {
          RepositoryLock lock = unifiedRepositoryLockService.getLock( file );
          RepositoryObjectType objectType = getObjectType( file.getName() );
          list.add( new EERepositoryObject( file, repDir, null, objectType, null, lock, true ) );
        }
      }

      return list;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get list of objects from directory [" + dirId + "]", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  public static RepositoryObjectType getObjectType( final String filename ) throws KettleException {
    if ( filename.endsWith( RepositoryObjectType.TRANSFORMATION.getExtension() ) ) {
      return RepositoryObjectType.TRANSFORMATION;
    } else if ( filename.endsWith( RepositoryObjectType.JOB.getExtension() ) ) {
      return RepositoryObjectType.JOB;
    } else if ( filename.endsWith( RepositoryObjectType.DATABASE.getExtension() ) ) {
      return RepositoryObjectType.DATABASE;
    } else if ( filename.endsWith( RepositoryObjectType.SLAVE_SERVER.getExtension() ) ) {
      return RepositoryObjectType.SLAVE_SERVER;
    } else if ( filename.endsWith( RepositoryObjectType.CLUSTER_SCHEMA.getExtension() ) ) {
      return RepositoryObjectType.CLUSTER_SCHEMA;
    } else if ( filename.endsWith( RepositoryObjectType.PARTITION_SCHEMA.getExtension() ) ) {
      return RepositoryObjectType.PARTITION_SCHEMA;
    } else {
      return RepositoryObjectType.UNKNOWN;
    }
  }

  @Override
  public IUser getUserInfo() {
    return user;
  }

  @Override
  public String getVersion() {
    return REPOSITORY_VERSION;
  }

  @Override
  public void insertJobEntryDatabase( ObjectId idJob, ObjectId idJobentry, ObjectId idDatabase )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ObjectId insertLogEntry( String description ) throws KettleException {
    // We are not presently logging
    return null;
  }

  @Override
  public void insertStepDatabase( ObjectId idTransformation, ObjectId idStep, ObjectId idDatabase )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public ClusterSchema loadClusterSchema( ObjectId idClusterSchema, List<SlaveServer> slaveServers, String versionId )
    throws KettleException {
    readWriteLock.readLock().lock();
    try {
      // We dont need to use slaveServer variable as the dataNoteToElement method finds the server from the repository
      NodeRepositoryFileData
        data =
        pur.getDataAtVersionForRead( idClusterSchema.getId(), versionId, NodeRepositoryFileData.class );
      RepositoryFile file = null;
      if ( versionId != null ) {
        file = pur.getFileAtVersion( idClusterSchema.getId(), versionId );
      } else {
        file = pur.getFileById( idClusterSchema.getId() );
      }

      return clusterTransformer.assemble( file, data, pur.getVersionSummary( idClusterSchema.getId(), versionId ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load cluster schema with id [" + idClusterSchema + "]", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public Condition loadConditionFromStepAttribute( ObjectId idStep, String code ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromJobEntryAttribute( ObjectId idJobentry, String nameCode, int nr,
                                                             String idCode, List<DatabaseMeta> databases )
    throws KettleException {
    throw new UnsupportedOperationException();
  }

  @Override
  public DatabaseMeta loadDatabaseMetaFromStepAttribute( ObjectId idStep, String code, List<DatabaseMeta> databases )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public PartitionSchema loadPartitionSchema( ObjectId partitionSchemaId, String versionId ) throws KettleException {
    readWriteLock.readLock().lock();
    try {
      NodeRepositoryFileData data =
        pur.getDataAtVersionForRead( partitionSchemaId.getId(), versionId, NodeRepositoryFileData.class );
      RepositoryFile file = null;
      if ( versionId != null ) {
        file = pur.getFileAtVersion( partitionSchemaId.getId(), versionId );
      } else {
        file = pur.getFileById( partitionSchemaId.getId() );
      }

      return partitionSchemaTransformer.assemble( file, data, pur.getVersionSummary( partitionSchemaId.getId(),
        versionId ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load partition schema with id [" + partitionSchemaId + "]", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public SlaveServer loadSlaveServer( ObjectId idSlaveServer, String versionId ) throws KettleException {
    readWriteLock.readLock().lock();
    try {
      NodeRepositoryFileData
        data =
        pur.getDataAtVersionForRead( idSlaveServer.getId(), versionId, NodeRepositoryFileData.class );
      RepositoryFile file = null;
      if ( versionId != null ) {
        file = pur.getFileAtVersion( idSlaveServer.getId(), versionId );
      } else {
        file = pur.getFileById( idSlaveServer.getId() );
      }

      return slaveTransformer.assemble( file, data, pur.getVersionSummary( idSlaveServer.getId(), versionId ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load slave server with id [" + idSlaveServer + "]", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  protected Map<RepositoryObjectType, List<? extends SharedObjectInterface>> loadAndCacheSharedObjects(
    final boolean deepCopy ) throws KettleException {
    if ( sharedObjectsByType == null ) {
      sharedObjectsLock.writeLock().lock();
      try {
        sharedObjectsByType =
          new EnumMap<RepositoryObjectType, List<? extends SharedObjectInterface>>( RepositoryObjectType.class );
        // Slave Servers are referenced by Cluster Schemas so they must be loaded first
        readSharedObjects( sharedObjectsByType, RepositoryObjectType.DATABASE, RepositoryObjectType.PARTITION_SCHEMA,
          RepositoryObjectType.SLAVE_SERVER, RepositoryObjectType.CLUSTER_SCHEMA );
      } catch ( Exception e ) {
        sharedObjectsByType = null;
        // TODO i18n
        throw new KettleException( "Unable to read shared objects from repository", e ); //$NON-NLS-1$
      } finally {
        sharedObjectsLock.writeLock().unlock();
      }
    }
    return deepCopy ? deepCopy( sharedObjectsByType ) : sharedObjectsByType;
  }

  protected Map<RepositoryObjectType, List<? extends SharedObjectInterface>> loadAndCacheSharedObjects()
    throws KettleException {
    return loadAndCacheSharedObjects( true );
  }

  private Map<RepositoryObjectType, List<? extends SharedObjectInterface>> deepCopy(
    Map<RepositoryObjectType, List<? extends SharedObjectInterface>> orig ) throws KettleException {
    Map<RepositoryObjectType, List<? extends SharedObjectInterface>>
      copy =
      new EnumMap<RepositoryObjectType, List<? extends SharedObjectInterface>>( RepositoryObjectType.class );
    sharedObjectsLock.writeLock().lock();
    try {
      for ( Entry<RepositoryObjectType, List<? extends SharedObjectInterface>> entry : orig.entrySet() ) {
        RepositoryObjectType type = entry.getKey();
        List<? extends SharedObjectInterface> value = entry.getValue();

        List<SharedObjectInterface> newValue = new ArrayList<SharedObjectInterface>( value.size() );
        for ( SharedObjectInterface obj : value ) {
          SharedObjectInterface newValueItem;
          if ( obj instanceof DatabaseMeta ) {
            DatabaseMeta databaseMeta = (DatabaseMeta) ( (DatabaseMeta) obj ).deepClone( true );
            databaseMeta.setObjectId( ( (DatabaseMeta) obj ).getObjectId() );
            databaseMeta.setChangedDate( obj.getChangedDate() );
            databaseMeta.clearChanged();
            newValueItem = databaseMeta;
          } else if ( obj instanceof SlaveServer ) {
            SlaveServer slaveServer = (SlaveServer) ( (SlaveServer) obj ).clone();
            slaveServer.setObjectId( ( (SlaveServer) obj ).getObjectId() );
            slaveServer.clearChanged();
            newValueItem = slaveServer;
          } else if ( obj instanceof PartitionSchema ) {
            PartitionSchema partitionSchema = (PartitionSchema) ( (PartitionSchema) obj ).clone();
            partitionSchema.setObjectId( ( (PartitionSchema) obj ).getObjectId() );
            partitionSchema.clearChanged();
            newValueItem = partitionSchema;
          } else if ( obj instanceof ClusterSchema ) {
            ClusterSchema clusterSchema = ( (ClusterSchema) obj ).clone();
            clusterSchema.setObjectId( ( (ClusterSchema) obj ).getObjectId() );
            clusterSchema.clearChanged();
            newValueItem = clusterSchema;
          } else {
            throw new KettleException( "unknown shared object class" );
          }
          newValue.add( newValueItem );
        }
        copy.put( type, newValue );
      }
    } finally {
      sharedObjectsLock.writeLock().unlock();
    }
    return copy;
  }

  @Override
  public SharedObjects readJobMetaSharedObjects( final JobMeta jobMeta ) throws KettleException {
    return jobDelegate.loadSharedObjects( jobMeta, loadAndCacheSharedObjects( true ) );
  }

  @Override
  public SharedObjects readTransSharedObjects( final TransMeta transMeta ) throws KettleException {
    return transDelegate.loadSharedObjects( transMeta, loadAndCacheSharedObjects( true ) );
  }

  @Override
  public ObjectId renameJob( ObjectId idJob, RepositoryDirectoryInterface newDirectory,
                             String newName )
    throws KettleException {
    return renameJob( idJob, null, newDirectory, newName );
  }

  @Override
  public ObjectId renameJob( ObjectId idJobForRename, String versionComment,
                             RepositoryDirectoryInterface newDirectory, String newJobName )
    throws KettleException {
    return renameTransOrJob( idJobForRename, versionComment, newDirectory, newJobName, RepositoryObjectType.JOB,
      "PurRepository.ERROR_0006_UNABLE_TO_RENAME_JOB" );
  }

  @Override
  public ObjectId renameTransformation( ObjectId idTransformation, RepositoryDirectoryInterface newDirectory,
                                        String newName )
    throws KettleException {
    return renameTransformation( idTransformation, null, newDirectory, newName );
  }

  @Override
  public ObjectId renameTransformation( ObjectId idTransForRename, String versionComment,
                                        RepositoryDirectoryInterface newDirectory, String newTransName )
    throws KettleException {
    return renameTransOrJob( idTransForRename, versionComment, newDirectory, newTransName, RepositoryObjectType.TRANSFORMATION,
      "PurRepository.ERROR_0006_UNABLE_TO_RENAME_TRANS" );
  }

  /**
   * Renames and optionally moves a file having {@code idObject}. If {@code newDirectory} is <tt>null</tt>, then the
   * file is just renamed. If {@code newTitle} is <tt>null</tt>, then the file should keep its name.
   * <p/>
   * Note, it is expected that the file exists
   *
   * @param idObject
   *          file's id
   * @param versionComment
   *          comment on the revision
   * @param newDirectory
   *          new folder, where to move the file; <tt>null</tt> means the file should be left in its current
   * @param newTitle
   *          new file's title (title is a name w/o extension); <tt>null</tt> means the file should keep its current
   * @param objectType
   *          file's type; {@linkplain RepositoryObjectType#TRANSFORMATION} or {@linkplain RepositoryObjectType#JOB} are
   *          expected
   * @param errorMsgKey
   *          key for the error message passed with the exception
   * @throws KettleException
   *           if file with same path exists
   */
  private ObjectId renameTransOrJob( ObjectId idObject, String versionComment,
                                     RepositoryDirectoryInterface newDirectory, String newTitle,
                                     RepositoryObjectType objectType, String errorMsgKey ) throws KettleException {

    RepositoryFile file;
    readWriteLock.writeLock().lock();

    try {
      file = pur.getFileById( idObject.getId() );

      RepositoryFile.Builder builder = new RepositoryFile.Builder( file );
      // fullName = title + extension
      String fullName;
      if ( newTitle == null ) {
        // keep existing file name
        fullName = file.getName();
      } else {
        // set new title
        builder.title( RepositoryFile.DEFAULT_LOCALE, newTitle )
          // rename operation creates new revision, hence clear old value to be overwritten during saving
          .createdDate( null );
        fullName = checkAndSanitize( newTitle ) + objectType.getExtension();
      }

      String absPath = calcDestAbsPath( file, newDirectory, fullName );
      RepositoryFile fileFromDestination;
      // get file from destination path, should be null for rename goal
      fileFromDestination = pur.getFile( absPath );

      if ( fileFromDestination == null ) {
        file = builder.build();
        NodeRepositoryFileData data;
        data = pur.getDataAtVersionForRead( file.getId(), null, NodeRepositoryFileData.class );

        if ( newTitle != null ) {
          // update file's content only if the title should be changed
          // as this action creates another revision
          pur.updateFile( file, data, versionComment );
        }
        pur.moveFile( idObject.getId(), absPath, null );

        rootRef.clearRef();
        return idObject;
      } else {
        throw new KettleException( BaseMessages.getString( PKG, errorMsgKey, file.getName(), newTitle ) );
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  protected String getParentPath( final String path ) {
    if ( path == null ) {
      throw new IllegalArgumentException();
    } else if ( RepositoryFile.SEPARATOR.equals( path ) ) {
      return null;
    }
    int lastSlashIndex = path.lastIndexOf( RepositoryFile.SEPARATOR );
    if ( lastSlashIndex == 0 ) {
      return RepositoryFile.SEPARATOR;
    } else if ( lastSlashIndex > 0 ) {
      return path.substring( 0, lastSlashIndex );
    } else {
      throw new IllegalArgumentException();
    }
  }

  protected String calcDestAbsPath( RepositoryFile existingFile,
                                    RepositoryDirectoryInterface newDirectory,
                                    String newName ) {
    String newDirectoryPath = getPath( null, newDirectory, null );
    StringBuilder buf = new StringBuilder( existingFile.getPath().length() );
    if ( newDirectory != null ) {
      buf.append( newDirectoryPath );
    } else {
      buf.append( getParentPath( existingFile.getPath() ) );
    }
    return buf
      .append( RepositoryFile.SEPARATOR )
      .append( newName )
      .toString();
  }

  @Override
  public void save( final RepositoryElementInterface element, final String versionComment,
                    final ProgressMonitorListener monitor, final boolean overwriteAssociated ) throws KettleException {
    save( element, versionComment, Calendar.getInstance(), monitor, overwriteAssociated );
  }

  @Override
  public void save( RepositoryElementInterface element, String versionComment, Calendar versionDate,
                    ProgressMonitorListener monitor, boolean overwrite ) throws KettleException {

    try {
      switch ( element.getRepositoryElementType() ) {
        case TRANSFORMATION:
          saveTrans( element, versionComment, versionDate );
          break;
        case JOB:
          saveJob( element, versionComment, versionDate );
          break;
        case DATABASE:
          saveDatabaseMeta( element, versionComment, versionDate );
          break;
        case SLAVE_SERVER:
          saveSlaveServer( element, versionComment, versionDate );
          break;
        case CLUSTER_SCHEMA:
          saveClusterSchema( element, versionComment, versionDate );
          break;
        case PARTITION_SCHEMA:
          savePartitionSchema( element, versionComment, versionDate );
          break;
        default:
          throw new KettleException(
            "It's not possible to save Class [" + element.getClass().getName() + "] to the repository" );
      }
    } catch ( Exception e ) {
      throw new KettleException( "Unable to save repository element [" + element + "]", e );
    }
  }

  private boolean isRenamed( final RepositoryElementInterface element, final RepositoryFile file )
    throws KettleException {
    if ( element.getObjectId() == null ) {
      return false; // never been saved
    }
    String filename = element.getName();
    switch ( element.getRepositoryElementType() ) {
      case TRANSFORMATION:
        filename += RepositoryObjectType.TRANSFORMATION.getExtension();
        break;
      case JOB:
        filename += RepositoryObjectType.JOB.getExtension();
        break;
      case DATABASE:
        filename += RepositoryObjectType.DATABASE.getExtension();
        break;
      case SLAVE_SERVER:
        filename += RepositoryObjectType.SLAVE_SERVER.getExtension();
        break;
      case CLUSTER_SCHEMA:
        filename += RepositoryObjectType.CLUSTER_SCHEMA.getExtension();
        break;
      case PARTITION_SCHEMA:
        filename += RepositoryObjectType.PARTITION_SCHEMA.getExtension();
        break;
      default:
        throw new KettleException( "unknown element type [" + element.getClass().getName() + "]" );
    }
    if ( !file.getName().equals( checkAndSanitize( filename ) ) ) {
      return true;
    }
    return false;
  }

  private void renameIfNecessary( final RepositoryElementInterface element, final RepositoryFile file )
    throws KettleException {
    if ( !isRenamed( element, file ) ) {
      return;
    }

    // ObjectId id = element.getObjectId();
    StringBuilder buf = new StringBuilder( file.getPath().length() );
    buf.append( getParentPath( file.getPath() ) );
    buf.append( RepositoryFile.SEPARATOR );
    buf.append( checkAndSanitize( element.getName() ) );
    switch ( element.getRepositoryElementType() ) {
      case DATABASE:
        buf.append( RepositoryObjectType.DATABASE.getExtension() );
        break;
      case SLAVE_SERVER:
        buf.append( RepositoryObjectType.SLAVE_SERVER.getExtension() );
        break;
      case CLUSTER_SCHEMA:
        buf.append( RepositoryObjectType.CLUSTER_SCHEMA.getExtension() );
        break;
      case PARTITION_SCHEMA:
        buf.append( RepositoryObjectType.PARTITION_SCHEMA.getExtension() );
        break;
      default:
        throw new KettleException( "It's not possible to rename Class [" + element.getClass().getName()
          + "] to the repository" );
    }

    readWriteLock.writeLock().lock();
    try {
      pur.moveFile( file.getId(), buf.toString(), null );
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  /**
   * Use {@linkplain #saveKettleEntity} instead
   */
  @Deprecated
  protected void saveJob0( RepositoryElementInterface element, String versionComment,
                           boolean saveSharedObjects,
                           boolean checkLock, boolean checkRename,
                           boolean loadRevision, boolean checkDeleted ) throws KettleException {
    saveTransOrJob( jobDelegate, element, versionComment, null, saveSharedObjects,
      checkLock, checkRename, loadRevision, checkDeleted );
  }

  protected void saveJob( final RepositoryElementInterface element, final String versionComment, Calendar versionDate )
    throws KettleException {
    saveKettleEntity( element, versionComment, versionDate, true, true, true, true, true );
  }

  /**
   * Use {@linkplain #saveKettleEntity} instead
   */
  @Deprecated
  protected void saveTrans0( RepositoryElementInterface element, String versionComment, Calendar versionDate,
                             boolean saveSharedObjects,
                             boolean checkLock, boolean checkRename,
                             boolean loadRevision, boolean checkDeleted ) throws KettleException {
    saveTransOrJob( transDelegate, element, versionComment, versionDate, saveSharedObjects,
      checkLock, checkRename, loadRevision, checkDeleted );
  }

  protected boolean isDeleted( RepositoryFile file ) {
    // no better solution so far
    return isInTrash( file );
  }

  protected boolean isInTrash( final RepositoryFile file ) {
    // pretty hacky solution
    if ( file.getPath().contains( "/.trash/" ) ) {
      return true;
    } else {
      return false;
    }
  }

  protected void saveTrans( final RepositoryElementInterface element, final String versionComment,
                            Calendar versionDate ) throws KettleException {
    saveKettleEntity( element, versionComment, versionDate, true, true, true, true, true );
  }

  protected void saveDatabaseMeta( final RepositoryElementInterface element, final String versionComment,
                                   Calendar versionDate ) throws KettleException {

    readWriteLock.writeLock().lock();
    try {
      // Even if the object id is null, we still have to check if the element is not present in the PUR
      // For example, if we import data from an XML file and there is a element with the same name in it.
      //
      if ( element.getObjectId() == null ) {
        element.setObjectId( getDatabaseID( element.getName() ) );
      }

      boolean isUpdate = element.getObjectId() != null;
      RepositoryFile file = null;
      if ( isUpdate ) {
        file = pur.getFileById( element.getObjectId().getId() );

        // update title
        final String title = ( (DatabaseMeta) element ).getDisplayName();
        Date modifiedDate = null;
        if ( versionDate != null && versionDate.getTime() != null ) {
          modifiedDate = versionDate.getTime();
        } else {
          modifiedDate = new Date();
        }
        file = new RepositoryFile.Builder( file ).title( RepositoryFile.DEFAULT_LOCALE, title )
          .lastModificationDate( modifiedDate ).build();
        renameIfNecessary( element, file );

        file =
          pur.updateFile( file, new NodeRepositoryFileData( databaseMetaTransformer.elementToDataNode( element ) ),
            versionComment );

      } else {
        Date createdDate = null;
        if ( versionDate != null && versionDate.getTime() != null ) {
          createdDate = versionDate.getTime();
        } else {
          createdDate = new Date();
        }

        file =
          new RepositoryFile.Builder(
            checkAndSanitize( RepositoryFilenameUtils.escape( element.getName(), pur.getReservedChars() )
              + RepositoryObjectType.DATABASE.getExtension() ) ).title( RepositoryFile.DEFAULT_LOCALE,
            element.getName() ).createdDate( createdDate ).versioned( VERSION_SHARED_OBJECTS ).build();

        file =
          pur.createFile( getDatabaseMetaParentFolderId(), file,
            new NodeRepositoryFileData( databaseMetaTransformer.elementToDataNode( element ) ), versionComment );
      }
      // side effects
      ObjectId objectId = new StringObjectId( file.getId().toString() );
      element.setObjectId( objectId );
      element.setObjectRevision( getObjectRevision( objectId, null ) );
      if ( element instanceof ChangedFlagInterface ) {
        ( (ChangedFlagInterface) element ).clearChanged();
      }
      updateSharedObjectCache( element );
    } catch ( Exception e ) {
      // determine if there is an "access denied" issue and throw a nicer error message.
      if ( e.getMessage().indexOf( "access denied" ) >= 0 ) {
        throw new KettleException(
          BaseMessages.getString( PKG, "PurRepository.ERROR_0004_DATABASE_UPDATE_ACCESS_DENIED", element.getName() ),
          e );
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public DatabaseMeta loadDatabaseMeta( final ObjectId databaseId, final String versionId )
    throws KettleException {
    readWriteLock.readLock().lock();
    try {

      NodeRepositoryFileData
        data =
        pur.getDataAtVersionForRead( databaseId.getId(), versionId, NodeRepositoryFileData.class );
      RepositoryFile file = null;

      if ( versionId != null ) {
        file = pur.getFileAtVersion( databaseId.getId(), versionId );
      } else {
        file = pur.getFileById( databaseId.getId() );
      }

      return databaseMetaTransformer.assemble( file, data, pur.getVersionSummary( databaseId.getId(), versionId ) );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load database with id [" + databaseId + "]", e );
    } finally {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public TransMeta loadTransformation( final String transName, final RepositoryDirectoryInterface parentDir,
                                       final ProgressMonitorListener monitor, final boolean setInternalVariables,
                                       final String versionId )
    throws KettleException {
    String absPath = null;
    try {
      // if transName is empty, we cannot load the transformation - the transformation path was likely not provided
      // by the user
      if ( StringUtils.isBlank( transName ) ) {
        throw new KettleFileException( BaseMessages.getString( PKG,
          "PurRepository.ERROR_0007_TRANSFORMATION_NAME_MISSING" ) );
      }
      try {
        absPath = getPath( transName, parentDir, RepositoryObjectType.TRANSFORMATION );
      } catch ( Exception e ) {
        // ignore and handle null value below
      }
      // if absPath is empty, we cannot load the transformation - the path provided by the user was likely defined as a
      // variable that is not available at runtime
      if ( StringUtils.isBlank( absPath )  ) {
        // Couldn't resolve path, throw an exception
        throw new KettleFileException( BaseMessages.getString( PKG,
          "PurRepository.ERROR_0008_TRANSFORMATION_PATH_INVALID", transName ) );
      }

      RepositoryFile file;
      NodeRepositoryFileData data = null;
      ObjectRevision revision = null;

      readWriteLock.readLock().lock();
      try {
        file = pur.getFile( absPath );
        if ( versionId != null ) {
          // need to go back to server to get versioned info
          file = pur.getFileAtVersion( file.getId(), versionId );
        }

        // if file is null, we cannot load the transformation - the provided path provided by the user is likely not a
        // valid file
        if ( file == null ) {
          throw new KettleException( BaseMessages.getString( PKG,
            "PurRepository.ERROR_0008_TRANSFORMATION_PATH_INVALID", absPath ) );
        }

        data = pur.getDataAtVersionForRead( file.getId(), versionId, NodeRepositoryFileData.class );
      } finally {
        readWriteLock.readLock().unlock();
      }

      revision = getObjectRevision( new StringObjectId( file.getId().toString() ), versionId );
      TransMeta transMeta = buildTransMeta( file, parentDir, data, revision );
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationMetaLoaded.id, transMeta );
      return transMeta;
    } catch ( final KettleException ke ) {
      // if we have a KettleException, simply re-throw it
      throw ke;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load transformation from path [" + absPath + "]", e );
    }
  }

  private TransMeta buildTransMeta( final RepositoryFile file, final RepositoryDirectoryInterface parentDir,
                                    final NodeRepositoryFileData data, final ObjectRevision revision )
    throws KettleException {
    TransMeta transMeta = new TransMeta();
    transMeta.setName( file.getTitle() );
    transMeta.setFilename( file.getName() );
    transMeta.setDescription( file.getDescription() );
    transMeta.setObjectId( new StringObjectId( file.getId().toString() ) );
    transMeta.setObjectRevision( revision );
    transMeta.setRepository( this );
    transMeta.setRepositoryDirectory( parentDir );
    transMeta.setMetaStore( getMetaStore() );
    readTransSharedObjects( transMeta ); // This should read from the local cache
    transDelegate.dataNodeToElement( data.getNode(), transMeta );
    transMeta.clearChanged();
    return transMeta;
  }

  /**
   * Load all transformations referenced by {@code files}.
   *
   * @param monitor
   * @param log
   * @param files
   *          Transformation files to load.
   * @param setInternalVariables
   *          Should internal variables be set when loading? (Note: THIS IS IGNORED, they are always set)
   * @return Loaded transformations
   * @throws KettleException
   *           Error loading data for transformations from repository
   */
  protected List<TransMeta> loadTransformations( final ProgressMonitorListener monitor, final LogChannelInterface log,
                                                 final List<RepositoryFile> files, final boolean setInternalVariables )
    throws KettleException {
    List<TransMeta> transformations = new ArrayList<TransMeta>( files.size() );

    readWriteLock.readLock().lock();
    List<NodeRepositoryFileData> filesData;
    List<VersionSummary> versions;
    try {
      filesData = pur.getDataForReadInBatch( files, NodeRepositoryFileData.class );
      versions = pur.getVersionSummaryInBatch( files );
    } finally {
      readWriteLock.readLock().unlock();
    }

    Iterator<RepositoryFile> filesIter = files.iterator();
    Iterator<NodeRepositoryFileData> filesDataIter = filesData.iterator();
    Iterator<VersionSummary> versionsIter = versions.iterator();
    while ( ( monitor == null || !monitor.isCanceled() ) && filesIter.hasNext() ) {
      RepositoryFile file = filesIter.next();
      NodeRepositoryFileData fileData = filesDataIter.next();
      VersionSummary version = versionsIter.next();
      String
        dirPath =
        file.getPath().substring( 0, file.getPath().lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR ) );
      try {
        log.logDetailed( "Loading/Exporting transformation [{0} : {1}]  ({2})", dirPath, file.getTitle(), file
          .getPath() ); //$NON-NLS-1$
        if ( monitor != null ) {
          monitor.subTask( "Exporting transformation [" + file.getPath() + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        TransMeta
          transMeta =
          buildTransMeta( file, findDirectory( dirPath ), fileData, createObjectRevision( version ) );
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationMetaLoaded.id, transMeta );
        transformations.add( transMeta );
      } catch ( Exception ex ) {
        log.logDetailed( "Unable to load transformation [" + file.getPath() + "]", ex ); //$NON-NLS-1$ //$NON-NLS-2$
        log.logError( "An error occurred reading transformation [" + file.getTitle() + "] from directory [" + dirPath
          + "] : " + ex.getMessage() ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        log.logError( "Transformation [" + file.getTitle() + "] from directory [" + dirPath
          + "] was not exported because of a loading error!" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      }
    }
    return transformations;
  }

  @Override
  public JobMeta loadJob( String jobname, RepositoryDirectoryInterface parentDir, ProgressMonitorListener monitor,
                          String versionId ) throws KettleException {
    String absPath = null;
    try {
      absPath = getPath( jobname, parentDir, RepositoryObjectType.JOB );
      if ( absPath == null ) {
        // Couldn't resolve path, throw an exception
        throw new KettleFileException(
          BaseMessages.getString( PKG, "PurRepository.ERROR_0003_JOB_NOT_FOUND", jobname ) );
      }

      readWriteLock.readLock().lock();
      RepositoryFile file;
      NodeRepositoryFileData data = null;
      ObjectRevision revision = null;
      try {
        file = pur.getFile( absPath );
        if ( versionId != null ) {
          // need to go back to server to get versioned info
          file = pur.getFileAtVersion( file.getId(), versionId );
        }

        data = pur.getDataAtVersionForRead( file.getId(), versionId, NodeRepositoryFileData.class );
      } finally {
        readWriteLock.readLock().unlock();
      }

      revision = getObjectRevision( new StringObjectId( file.getId().toString() ), versionId );
      JobMeta jobMeta = buildJobMeta( file, parentDir, data, revision );
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.JobMetaLoaded.id, jobMeta );
      return jobMeta;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load job from path [" + absPath + "]", e );
    }
  }

  private JobMeta buildJobMeta( final RepositoryFile file, final RepositoryDirectoryInterface parentDir,
                                final NodeRepositoryFileData data, final ObjectRevision revision )
    throws KettleException {
    JobMeta jobMeta = new JobMeta();
    jobMeta.setName( file.getTitle() );
    jobMeta.setFilename( file.getName() );
    jobMeta.setDescription( file.getDescription() );
    jobMeta.setObjectId( new StringObjectId( file.getId().toString() ) );
    jobMeta.setObjectRevision( revision );
    jobMeta.setRepository( this );
    jobMeta.setRepositoryDirectory( parentDir );
    jobMeta.setMetaStore( getMetaStore() );
    readJobMetaSharedObjects( jobMeta ); // This should read from the local cache
    jobDelegate.dataNodeToElement( data.getNode(), jobMeta );
    jobMeta.clearChanged();
    return jobMeta;
  }

  /**
   * Load all jobs referenced by {@code files}.
   *
   * @param monitor
   * @param log
   * @param files
   *          Job files to load.
   * @param setInternalVariables
   *          Should internal variables be set when loading? (Note: THIS IS IGNORED, they are always set)
   * @return Loaded jobs
   * @throws KettleException
   *           Error loading data for jobs from repository
   */
  protected List<JobMeta> loadJobs( final ProgressMonitorListener monitor, final LogChannelInterface log,
                                    final List<RepositoryFile> files, final boolean setInternalVariables )
    throws KettleException {
    List<JobMeta> jobs = new ArrayList<JobMeta>( files.size() );

    readWriteLock.readLock().lock();
    List<NodeRepositoryFileData> filesData;
    List<VersionSummary> versions;
    try {
      filesData = pur.getDataForReadInBatch( files, NodeRepositoryFileData.class );
      versions = pur.getVersionSummaryInBatch( files );
    } finally {
      readWriteLock.readLock().unlock();
    }

    Iterator<RepositoryFile> filesIter = files.iterator();
    Iterator<NodeRepositoryFileData> filesDataIter = filesData.iterator();
    Iterator<VersionSummary> versionsIter = versions.iterator();
    while ( ( monitor == null || !monitor.isCanceled() ) && filesIter.hasNext() ) {
      RepositoryFile file = filesIter.next();
      NodeRepositoryFileData fileData = filesDataIter.next();
      VersionSummary version = versionsIter.next();
      try {
        String
          dirPath =
          file.getPath().substring( 0, file.getPath().lastIndexOf( RepositoryDirectory.DIRECTORY_SEPARATOR ) );
        log.logDetailed( "Loading/Exporting job [{0} : {1}]  ({2})", dirPath, file.getTitle(),
          file.getPath() ); //$NON-NLS-1$
        if ( monitor != null ) {
          monitor.subTask( "Exporting job [" + file.getPath() + "]" ); //$NON-NLS-1$ //$NON-NLS-2$
        }
        JobMeta jobMeta = buildJobMeta( file, findDirectory( dirPath ), fileData, createObjectRevision( version ) );
        ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.JobMetaLoaded.id, jobMeta );
        jobs.add( jobMeta );
      } catch ( Exception ex ) {
        log.logError( "Unable to load job [" + file.getPath() + "]", ex ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
    return jobs;

  }

  /**
   * Performs one-way conversion on incoming String to produce a syntactically valid JCR path (section 4.6 Path Syntax).
   */
  public static String checkAndSanitize( final String in ) {
    if ( in == null ) {
      throw new IllegalArgumentException();
    }
    String extension = null;
    if ( in.endsWith( RepositoryObjectType.CLUSTER_SCHEMA.getExtension() ) ) {
      extension = RepositoryObjectType.CLUSTER_SCHEMA.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.DATABASE.getExtension() ) ) {
      extension = RepositoryObjectType.DATABASE.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.JOB.getExtension() ) ) {
      extension = RepositoryObjectType.JOB.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.PARTITION_SCHEMA.getExtension() ) ) {
      extension = RepositoryObjectType.PARTITION_SCHEMA.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.SLAVE_SERVER.getExtension() ) ) {
      extension = RepositoryObjectType.SLAVE_SERVER.getExtension();
    } else if ( in.endsWith( RepositoryObjectType.TRANSFORMATION.getExtension() ) ) {
      extension = RepositoryObjectType.TRANSFORMATION.getExtension();
    }
    String out = in;
    if ( extension != null ) {
      out = out.substring( 0, out.length() - extension.length() );
    }
    if ( out.contains( "/" ) || out.equals( ".." ) || out.equals( "." ) || StringUtils.isBlank( out ) ) {
      throw new IllegalArgumentException();
    }
    if ( System.getProperty( "KETTLE_COMPATIBILITY_PUR_OLD_NAMING_MODE", "N" ).equals( "Y" ) ) {
      out = out.replaceAll( "[/:\\[\\]\\*'\"\\|\\s\\.]", "_" ); //$NON-NLS-1$//$NON-NLS-2$
    }
    if ( extension != null ) {
      return out + extension;
    } else {
      return out;
    }
  }

  protected void saveRepositoryElement( RepositoryElementInterface element, String versionComment,
                                        ITransformer transformer, Serializable elementsFolderId )
    throws KettleException {

    boolean isUpdate = ( element.getObjectId() != null );
    RepositoryFile file;

    readWriteLock.writeLock().lock();
    try {
      if ( isUpdate ) {
        file = pur.getFileById( element.getObjectId().getId() );

        // update title & description
        file =
          new RepositoryFile.Builder( file ).title( RepositoryFile.DEFAULT_LOCALE, element.getName() ).description(
            RepositoryFile.DEFAULT_LOCALE, Const.NVL( element.getDescription(), "" ) ).build();

        // first rename, it is safe as only a name is changed, but not a path
        renameIfNecessary( element, file );

        file =
          pur.updateFile( file,
            new NodeRepositoryFileData( transformer.elementToDataNode( element ) ), versionComment );

      } else {
        file =
          new RepositoryFile.Builder(
            checkAndSanitize( element.getName() + element.getRepositoryElementType().getExtension() ) )
            .title( RepositoryFile.DEFAULT_LOCALE, element.getName() )
            .description( RepositoryFile.DEFAULT_LOCALE, Const.NVL( element.getDescription(), "" ) ).versioned(
            VERSION_SHARED_OBJECTS ).build();

        file =
          pur
            .createFile( elementsFolderId, file, new NodeRepositoryFileData( transformer.elementToDataNode( element ) ),
              versionComment );
      }
    } finally {
      readWriteLock.writeLock().unlock();
    }

    // side effects
    ObjectId objectId = new StringObjectId( file.getId().toString() );
    element.setObjectId( objectId );
    element.setObjectRevision( getObjectRevision( objectId, null ) );
    if ( element instanceof ChangedFlagInterface ) {
      ( (ChangedFlagInterface) element ).clearChanged();
    }
    updateSharedObjectCache( element );
  }

  protected void savePartitionSchema( final RepositoryElementInterface element, final String versionComment,
                                      Calendar versionDate ) {
    try {
      // Even if the object id is null, we still have to check if the element is not present in the PUR
      // For example, if we import data from an XML file and there is a element with the same name in it.
      //
      if ( element.getObjectId() == null ) {
        element.setObjectId( getPartitionSchemaID( element.getName() ) );
      }

      saveRepositoryElement( element, versionComment, partitionSchemaTransformer, getPartitionSchemaParentFolderId() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
  }

  protected void saveSlaveServer( final RepositoryElementInterface element, final String versionComment,
                                  Calendar versionDate ) throws KettleException {
    try {
      // Even if the object id is null, we still have to check if the element is not present in the PUR
      // For example, if we import data from an XML file and there is a element with the same name in it.
      //
      if ( element.getObjectId() == null ) {
        element.setObjectId( getSlaveID( element.getName() ) );
      }

      saveRepositoryElement( element, versionComment, slaveTransformer, getSlaveServerParentFolderId() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
  }

  protected void saveClusterSchema( final RepositoryElementInterface element, final String versionComment,
                                    Calendar versionDate ) {
    try {
      // Even if the object id is null, we still have to check if the element is not present in the PUR
      // For example, if we import data from an XML file and there is a element with the same name in it.
      //
      if ( element.getObjectId() == null ) {
        element.setObjectId( getClusterID( element.getName() ) );
      }

      saveRepositoryElement( element, versionComment, clusterTransformer, getClusterSchemaParentFolderId() );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
  }

  private void updateSharedObjectCache( final RepositoryElementInterface element ) throws KettleException {
    updateSharedObjectCache( element, null, null );
  }

  private void removeFromSharedObjectCache( final RepositoryObjectType type, final ObjectId id )
    throws KettleException {
    updateSharedObjectCache( null, type, id );
  }

  /**
   * Do not call this method directly. Instead call updateSharedObjectCache or removeFromSharedObjectCache.
   */
  private void updateSharedObjectCache( final RepositoryElementInterface element, final RepositoryObjectType type,
                                        final ObjectId id ) throws KettleException {
    if ( element != null && ( element.getObjectId() == null || element.getObjectId().getId() == null ) ) {
      throw new IllegalArgumentException( element.getName() + " has a null id" );
    }

    loadAndCacheSharedObjects( false );

    boolean remove = element == null;
    ObjectId idToFind = element != null ? element.getObjectId() : id;
    RepositoryObjectType typeToUpdate = element != null ? element.getRepositoryElementType() : type;
    RepositoryElementInterface elementToUpdate = null;
    List<? extends SharedObjectInterface> origSharedObjects = null;
    sharedObjectsLock.writeLock().lock();
    try {
      switch ( typeToUpdate ) {
        case DATABASE:
          origSharedObjects = sharedObjectsByType.get( RepositoryObjectType.DATABASE );
          if ( !remove ) {
            elementToUpdate = (RepositoryElementInterface) ( (DatabaseMeta) element ).deepClone( true );
          }
          break;
        case SLAVE_SERVER:
          origSharedObjects = sharedObjectsByType.get( RepositoryObjectType.SLAVE_SERVER );
          if ( !remove ) {
            elementToUpdate = (RepositoryElementInterface) ( (SlaveServer) element ).clone();
          }
          break;
        case CLUSTER_SCHEMA:
          origSharedObjects = sharedObjectsByType.get( RepositoryObjectType.CLUSTER_SCHEMA );
          if ( !remove ) {
            elementToUpdate = ( (ClusterSchema) element ).clone();
          }
          break;
        case PARTITION_SCHEMA:
          origSharedObjects = sharedObjectsByType.get( RepositoryObjectType.PARTITION_SCHEMA );
          if ( !remove ) {
            elementToUpdate = (RepositoryElementInterface) ( (PartitionSchema) element ).clone();
          }
          break;
        default:
          throw new KettleException( "unknown type [" + typeToUpdate + "]" );
      }

      List<SharedObjectInterface> newSharedObjects = new ArrayList<SharedObjectInterface>( origSharedObjects );
      // if there's a match on id, replace the element
      boolean found = false;
      for ( int i = 0; i < origSharedObjects.size(); i++ ) {
        RepositoryElementInterface repositoryElementInterface = (RepositoryElementInterface) origSharedObjects.get( i );
        if ( repositoryElementInterface == null ) {
          continue;
        }
        ObjectId objectId = repositoryElementInterface.getObjectId();
        if ( objectId != null && objectId.equals( idToFind ) ) {
          if ( remove ) {
            newSharedObjects.remove( i );
          } else {
            elementToUpdate.setObjectId( idToFind ); // because some clones don't clone the ID!!!
            newSharedObjects.set( i, (SharedObjectInterface) elementToUpdate );
          }
          found = true;
        }
      }
      // otherwise, add it
      if ( !remove && !found ) {
        elementToUpdate.setObjectId( idToFind ); // because some clones don't clone the ID!!!
        newSharedObjects.add( (SharedObjectInterface) elementToUpdate );
      }
      sharedObjectsByType.put( typeToUpdate, newSharedObjects );
    } finally {
      sharedObjectsLock.writeLock().unlock();
    }
  }

  private ObjectRevision getObjectRevision( final ObjectId elementId, final String versionId ) {
    readWriteLock.readLock().lock();
    ObjectRevision result;
    try {
      result = createObjectRevision( pur.getVersionSummary( elementId.getId(), versionId ) );
    } finally {
      readWriteLock.readLock().unlock();
    }

    return result;
  }

  /**
   * @return Wrapped {@link VersionSummary} with a {@link ObjectRevision}.
   */
  protected ObjectRevision createObjectRevision( final VersionSummary versionSummary ) {
    return new PurObjectRevision( versionSummary.getId(), versionSummary.getAuthor(), versionSummary.getDate(),
      versionSummary.getMessage() );
  }

  private String getDatabaseMetaParentFolderPath() {
    return ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + FOLDER_PDI + RepositoryFile.SEPARATOR
      + FOLDER_DATABASES;
  }

  // package-local visibility for testing purposes
  Serializable getDatabaseMetaParentFolderId() {
    if ( cachedDatabaseMetaParentFolderId == null ) {
      readWriteLock.readLock().lock();
      RepositoryFile f;
      try {
        f = pur.getFile( getDatabaseMetaParentFolderPath() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      cachedDatabaseMetaParentFolderId = f.getId();
    }
    return cachedDatabaseMetaParentFolderId;
  }

  private String getPartitionSchemaParentFolderPath() {
    return ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + FOLDER_PDI + RepositoryFile.SEPARATOR
      + FOLDER_PARTITION_SCHEMAS;
  }

  private Serializable getPartitionSchemaParentFolderId() {
    if ( cachedPartitionSchemaParentFolderId == null ) {
      readWriteLock.readLock().lock();
      RepositoryFile f;
      try {
        f = pur.getFile( getPartitionSchemaParentFolderPath() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      cachedPartitionSchemaParentFolderId = f.getId();
    }
    return cachedPartitionSchemaParentFolderId;
  }

  private String getSlaveServerParentFolderPath() {
    return ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + FOLDER_PDI + RepositoryFile.SEPARATOR
      + FOLDER_SLAVE_SERVERS;
  }

  private Serializable getSlaveServerParentFolderId() {
    if ( cachedSlaveServerParentFolderId == null ) {
      readWriteLock.readLock().lock();
      RepositoryFile f;
      try {
        f = pur.getFile( getSlaveServerParentFolderPath() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      cachedSlaveServerParentFolderId = f.getId();
    }
    return cachedSlaveServerParentFolderId;
  }

  private String getClusterSchemaParentFolderPath() {
    return ClientRepositoryPaths.getEtcFolderPath() + RepositoryFile.SEPARATOR + FOLDER_PDI + RepositoryFile.SEPARATOR
      + FOLDER_CLUSTER_SCHEMAS;
  }

  private Serializable getClusterSchemaParentFolderId() {
    if ( cachedClusterSchemaParentFolderId == null ) {
      readWriteLock.readLock().lock();
      RepositoryFile f;
      try {
        f = pur.getFile( getClusterSchemaParentFolderPath() );
      } finally {
        readWriteLock.readLock().unlock();
      }

      cachedClusterSchemaParentFolderId = f.getId();
    }
    return cachedClusterSchemaParentFolderId;
  }

  @Override
  public void saveConditionStepAttribute( ObjectId idTransformation, ObjectId idStep, String code, Condition condition )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveDatabaseMetaJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String nameCode,
                                                 String idCode, DatabaseMeta database ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveDatabaseMetaStepAttribute( ObjectId idTransformation, ObjectId idStep, String code,
                                             DatabaseMeta database ) throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, String value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, boolean value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveJobEntryAttribute( ObjectId idJob, ObjectId idJobentry, int nr, String code, long value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, String value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, boolean value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, long value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void saveStepAttribute( ObjectId idTransformation, ObjectId idStep, int nr, String code, double value )
    throws KettleException {
    // implemented by RepositoryProxy
    throw new UnsupportedOperationException();
  }

  @Override
  public void undeleteObject( final RepositoryElementMetaInterface element ) throws KettleException {
    readWriteLock.writeLock().lock();
    try {
      pur.undeleteFile( element.getObjectId().getId(), null );
    } finally {
      readWriteLock.writeLock().unlock();
    }

    rootRef.clearRef();
  }

  @Override
  public List<RepositoryElementMetaInterface> getJobAndTransformationObjects( ObjectId id_directory,
                                                                              boolean includeDeleted ) throws KettleException {
    return getPdiObjects( id_directory, Arrays.asList( RepositoryObjectType.JOB, RepositoryObjectType.TRANSFORMATION ), includeDeleted );
  }

  @Override
  public IRepositoryService getService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    return purRepositoryServiceRegistry.getService( clazz );
  }

  @Override
  public List<Class<? extends IRepositoryService>> getServiceInterfaces() throws KettleException {
    return purRepositoryServiceRegistry.getRegisteredInterfaces();
  }

  @Override
  public boolean hasService( Class<? extends IRepositoryService> clazz ) throws KettleException {
    return purRepositoryServiceRegistry.getService( clazz ) != null;
  }

  @Override
  public RepositoryDirectoryInterface getDefaultSaveDirectory( RepositoryElementInterface repositoryElement )
    throws KettleException {
    return getUserHomeDirectory();
  }

  @Override
  public RepositoryDirectoryInterface getUserHomeDirectory() throws KettleException {
    return findDirectory( ClientRepositoryPaths.getUserHomeFolderPath( user.getLogin() ) );
  }

  @Override
  public RepositoryObject getObjectInformation( ObjectId objectId, RepositoryObjectType objectType )
    throws KettleException {
    try {
      RepositoryFile repositoryFile;
      RepositoryFileAcl repositoryFileAcl;

      readWriteLock.readLock().lock();
      try {
        try {
          repositoryFile = pur.getFileById( objectId.getId() );
        } catch ( Exception e ) {
          // javax.jcr.Session throws exception, if a node with specified ID does not exist
          // see http://jira.pentaho.com/browse/BISERVER-12758
          log.logError( "Error when trying to obtain a file by id: " + objectId.getId(), e );
          return null;
        }

        if ( repositoryFile == null ) {
          return null;
        }

        repositoryFileAcl = pur.getAcl( repositoryFile.getId() );

      } finally {
        readWriteLock.readLock().unlock();
      }

      String parentPath = getParentPath( repositoryFile.getPath() );
      String name = repositoryFile.getTitle();
      String description = repositoryFile.getDescription();
      Date modifiedDate = repositoryFile.getLastModifiedDate();
      // String creatorId = repositoryFile.getCreatorId();
      String ownerName = repositoryFileAcl != null ? repositoryFileAcl.getOwner().getName() : "";
      boolean deleted = isDeleted( repositoryFile );
      RepositoryDirectoryInterface directory = findDirectory( parentPath );
      return new RepositoryObject( objectId, name, directory, ownerName, modifiedDate, objectType, description,
        deleted );
    } catch ( Exception e ) {
      throw new KettleException( "Unable to get object information for object with id=" + objectId, e );
    }
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( String directory ) throws KettleException {
    RepositoryDirectoryInterface repositoryDirectoryInterface = null;
    // check if we have a rootRef cached
    boolean usingRootDirCache = rootRef.getRef() != null;
    repositoryDirectoryInterface = getRootDir().findDirectory( directory );
    // if we are using a cached version of the repository interface, allow a reload if we do not find
    if ( repositoryDirectoryInterface == null && usingRootDirCache ) {
      repositoryDirectoryInterface = loadRepositoryDirectoryTree().findDirectory( directory );
    }
    return repositoryDirectoryInterface;
  }

  @Override
  public RepositoryDirectoryInterface findDirectory( ObjectId directory ) throws KettleException {
    RepositoryDirectoryInterface repositoryDirectoryInterface = null;
    // check if we have a rootRef cached
    boolean usingRootDirCache = rootRef.getRef() != null;
    repositoryDirectoryInterface = getRootDir().findDirectory( directory );
    // if we are using a cached version of the repository interface, allow a reload if we do not find
    if ( repositoryDirectoryInterface == null && usingRootDirCache ) {
      repositoryDirectoryInterface = loadRepositoryDirectoryTree().findDirectory( directory );
    }
    return repositoryDirectoryInterface;
  }

  @Override
  public JobMeta loadJob( ObjectId idJob, String versionLabel ) throws KettleException {
    try {
      RepositoryFile file = null;
      EEJobMeta jobMeta = null;

      readWriteLock.readLock().lock();
      try {
        if ( versionLabel != null ) {
          file = pur.getFileAtVersion( idJob.getId(), versionLabel );
        } else {
          file = pur.getFileById( idJob.getId() );
        }

        jobMeta = new EEJobMeta();
        jobMeta.setName( file.getTitle() );
        jobMeta.setFilename( file.getPath() );
        jobMeta.setDescription( file.getDescription() );
        jobMeta.setObjectId( new StringObjectId( file.getId().toString() ) );
        jobMeta.setObjectRevision( getObjectRevision( new StringObjectId( file.getId().toString() ), versionLabel ) );
        jobMeta.setRepository( this );
        jobMeta.setRepositoryDirectory( findDirectory( getParentPath( file.getPath() ) ) );

        jobMeta.setMetaStore( getMetaStore() ); // inject metastore

        readJobMetaSharedObjects( jobMeta );
        // Additional obfuscation through obscurity
        jobMeta.setRepositoryLock( unifiedRepositoryLockService.getLock( file ) );

        jobDelegate.dataNodeToElement( pur.getDataAtVersionForRead( idJob.getId(), versionLabel,
          NodeRepositoryFileData.class ).getNode(), jobMeta );

      } finally {
        readWriteLock.readLock().unlock();
      }

      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.JobMetaLoaded.id, jobMeta );

      jobMeta.clearChanged();
      return jobMeta;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load job with id [" + idJob + "]", e );
    }
  }

  @Override
  public TransMeta loadTransformation( ObjectId idTransformation, String versionLabel ) throws KettleException {
    try {
      RepositoryFile file = null;
      EETransMeta transMeta = null;

      readWriteLock.readLock().lock();
      try {
        if ( versionLabel != null ) {
          file = pur.getFileAtVersion( idTransformation.getId(), versionLabel );
        } else {
          file = pur.getFileById( idTransformation.getId() );
        }
        transMeta = new EETransMeta();
        transMeta.setName( file.getTitle() );
        transMeta.setFilename( file.getPath() );
        transMeta.setDescription( file.getDescription() );
        transMeta.setObjectId( new StringObjectId( file.getId().toString() ) );
        transMeta.setObjectRevision( getObjectRevision( new StringObjectId( file.getId().toString() ), versionLabel ) );
        transMeta.setRepository( this );
        transMeta.setRepositoryDirectory( findDirectory( getParentPath( file.getPath() ) ) );
        transMeta.setRepositoryLock( unifiedRepositoryLockService.getLock( file ) );
        transMeta.setMetaStore( getMetaStore() ); // inject metastore

        readTransSharedObjects( transMeta );

        transDelegate.dataNodeToElement(
          pur.getDataAtVersionForRead( idTransformation.getId(), versionLabel, NodeRepositoryFileData.class ).getNode(),
          transMeta );
      } finally {
        readWriteLock.readLock().unlock();
      }

      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransformationMetaLoaded.id, transMeta );

      transMeta.clearChanged();
      return transMeta;
    } catch ( Exception e ) {
      throw new KettleException( "Unable to load transformation with id [" + idTransformation + "]", e );
    }
  }

  @Override
  public String getConnectMessage() {
    return connectMessage;
  }

  @Override
  public String[] getJobsUsingDatabase( ObjectId id_database ) throws KettleException {
    List<String> result = new ArrayList<String>();
    for ( RepositoryFile file : getReferrers( id_database, Collections.singletonList( RepositoryObjectType.JOB ) ) ) {
      result.add( file.getPath() );
    }
    return result.toArray( new String[result.size()] );
  }

  @Override
  public String[] getTransformationsUsingDatabase( ObjectId id_database ) throws KettleException {
    List<String> result = new ArrayList<String>();
    for ( RepositoryFile file : getReferrers( id_database, Collections.singletonList( RepositoryObjectType.TRANSFORMATION ) ) ) {
      result.add( file.getPath() );
    }
    return result.toArray( new String[result.size()] );
  }

  protected List<RepositoryFile> getReferrers( ObjectId fileId, List<RepositoryObjectType> referrerTypes )
    throws KettleException {
    // Use a result list to append to; Removing from the files list was causing a concurrency exception
    List<RepositoryFile> result = new ArrayList<RepositoryFile>();
    readWriteLock.readLock().lock();
    List<RepositoryFile> files;
    try {
      files = pur.getReferrers( fileId.getId() );
    } finally {
      readWriteLock.readLock().unlock();
    }

    // Filter out types
    if ( referrerTypes != null && referrerTypes.size() > 0 ) {
      for ( RepositoryFile file : files ) {
        if ( referrerTypes.contains( getObjectType( file.getName() ) ) ) {
          result.add( file );
        }
      }
    }
    return result;
  }

  @Override
  public IRepositoryExporter getExporter() throws KettleException {
    final List<String> exportPerms =
      Arrays.asList( IAbsSecurityProvider.CREATE_CONTENT_ACTION, IAbsSecurityProvider.EXECUTE_CONTENT_ACTION );
    IAbsSecurityProvider securityProvider = purRepositoryServiceRegistry.getService( IAbsSecurityProvider.class );
    StringBuilder errorMessage = new StringBuilder( "[" );
    for ( String perm : exportPerms ) {
      if ( securityProvider == null && PurRepositoryConnector.inProcess() ) {
        return new PurRepositoryExporter( this );
      }
      if ( securityProvider != null && securityProvider.isAllowed( perm ) ) {
        return new PurRepositoryExporter( this );
      }
      errorMessage.append( perm );
      errorMessage.append( ", " );
    }
    errorMessage.setLength( errorMessage.length() - 2 );
    errorMessage.append( "]" );

    throw new KettleSecurityException( BaseMessages.getString( PKG, "PurRepository.ERROR_0005_INCORRECT_PERMISSION",
      errorMessage.toString() ) );
  }

  @Override
  public IRepositoryImporter getImporter() {
    return new PurRepositoryImporter( this );
  }

  @Override
  public IUnifiedRepository getUnderlyingRepository() {
    return pur;
  }

  @Override
  public IMetaStore getMetaStore() {
    return metaStore;
  }

  public ServiceManager getServiceManager() {
    return purRepositoryConnector == null ? null : purRepositoryConnector.getServiceManager();
  }

  /**
   * Saves {@code element} in repository. {@code element} show represent either a transformation or a job. <br/>
   * The method throws {@code KettleException} in the following cases:
   * <ul>
   *   <li>{@code element} is not a {@linkplain TransMeta} or {@linkplain JobMeta}</li>
   *   <li>{@code checkLock == true} and the file is locked and cannot be unlocked</li>
   *   <li>{@code checkDeleted == true} and the file was removed</li>
   *   <li>{@code checkRename == true} and the file was renamed and renaming failed</li>
   * </ul>
   *
   * @param element
   *          job or transformation
   * @param versionComment
   *          revision comment
   * @param versionDate
   *          revision timestamp
   * @param saveSharedObjects
   *          flag of saving element's shared objects
   * @param checkLock
   *          flag of checking whether the corresponding file is locked
   * @param checkRename
   *          flag of checking whether it is necessary to rename the file
   * @param loadRevision
   *          flag of setting element's revision
   * @param checkDeleted
   *          flag of checking whether the file was deleted
   * @throws KettleException
   *           if any of aforementioned conditions is {@code true}
   */
  protected void saveKettleEntity( RepositoryElementInterface element,
                                   String versionComment, Calendar versionDate,
                                   boolean saveSharedObjects,
                                   boolean checkLock, boolean checkRename,
                                   boolean loadRevision, boolean checkDeleted )
    throws KettleException {
    ISharedObjectsTransformer objectTransformer;
    switch ( element.getRepositoryElementType() ) {
      case TRANSFORMATION:
        objectTransformer = transDelegate;
        break;
      case JOB:
        objectTransformer = jobDelegate;
        break;
      default:
        throw new KettleException(
          "Unknown RepositoryObjectType. Should be TRANSFORMATION or JOB " );
    }
    saveTransOrJob( objectTransformer, element, versionComment, versionDate, saveSharedObjects, checkLock, checkRename,
      loadRevision, checkDeleted );
  }

  protected void saveTransOrJob( ISharedObjectsTransformer objectTransformer, RepositoryElementInterface element,
                                 String versionComment, Calendar versionDate,
                                 boolean saveSharedObjects,
                                 boolean checkLock, boolean checkRename,
                                 boolean loadRevision, boolean checkDeleted ) throws KettleException {
    if ( Import.ROOT_DIRECTORY.equals( element.getRepositoryDirectory().toString() ) ) {
      // We don't have possibility to read this file via UI
      throw new KettleException( BaseMessages.getString( PKG, "PurRepository.fileCannotBeSavedInRootDirectory",
        element.getName() + element.getRepositoryElementType().getExtension() ) );
    }

    if ( saveSharedObjects ) {
      objectTransformer.saveSharedObjects( element, versionComment );
    }

    ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.BeforeSaveToRepository.id, element );

    final boolean isUpdate = ( element.getObjectId() != null && element.getObjectId().getId() != null  );
    RepositoryFile file = null;
    if ( isUpdate ) {

      readWriteLock.readLock().lock();
      try {
        ObjectId id = element.getObjectId();
        file = pur.getFileById( id.getId() );

        if ( checkLock && file.isLocked() && !unifiedRepositoryLockService.canUnlockFileById( id ) ) {
          throw new KettleException( "File is currently locked by another user for editing" );
        }
        if ( checkDeleted && isInTrash( file ) ) {
          // absolutely awful to have UI references in this class :(
          throw new KettleException( "File is in the Trash. Use Save As." );
        }
      } finally {
        readWriteLock.readLock().unlock();
      }

      readWriteLock.writeLock().lock();
      try {
        // update title and description
        file =
          new RepositoryFile.Builder( file ).title( RepositoryFile.DEFAULT_LOCALE, element.getName() ).createdDate(
            versionDate != null ? versionDate.getTime() : new Date() ).description( RepositoryFile.DEFAULT_LOCALE,
            Const.NVL( element.getDescription(), "" ) ).build();

        file =
          pur.updateFile( file, new NodeRepositoryFileData( objectTransformer.elementToDataNode( element ) ),
            versionComment );
      } catch ( SOAPFaultException e ) {
        if ( e.getMessage().contains( UnifiedRepositoryUpdateFileException.PREFIX ) ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "PurRepository.fileUpdateException", file.getName() ) );
        }
        throw e;
      } finally {
        readWriteLock.writeLock().unlock();
      }

      if ( checkRename && isRenamed( element, file ) ) {
        renameKettleEntity( element, null, element.getName() );
      }
    } else {
      readWriteLock.writeLock().lock();
      try {
        file =
          new RepositoryFile.Builder( checkAndSanitize( element.getName()
            + element.getRepositoryElementType().getExtension() ) ).versioned( true ).title(
            RepositoryFile.DEFAULT_LOCALE, element.getName() ).createdDate(
            versionDate != null ? versionDate.getTime() : new Date() ).description( RepositoryFile.DEFAULT_LOCALE,
            Const.NVL( element.getDescription(), "" ) ).build();

        file =
          pur.createFile( element.getRepositoryDirectory().getObjectId().getId(), file,
            new NodeRepositoryFileData( objectTransformer.elementToDataNode( element ) ), versionComment );
      } catch ( SOAPFaultException e ) {
        if ( e.getMessage().contains( UnifiedRepositoryCreateFileException.PREFIX ) ) {
          throw new KettleException(
            BaseMessages.getString( PKG, "PurRepository.fileCreateException", file.getName() ) );
        }
      } finally {
        readWriteLock.writeLock().unlock();
      }
    }
    // side effects
    ObjectId objectId = new StringObjectId( file.getId().toString() );
    element.setObjectId( objectId );
    if ( loadRevision ) {
      element.setObjectRevision( getObjectRevision( objectId, null ) );
    }
    if ( element instanceof ChangedFlagInterface ) {
      ( (ChangedFlagInterface) element ).clearChanged();
    }

    if ( element.getRepositoryElementType() == RepositoryObjectType.TRANSFORMATION ) {
      TransMeta transMeta = loadTransformation( objectId, null );
      ExtensionPointHandler.callExtensionPoint( log, KettleExtensionPoint.TransImportAfterSaveToRepo.id, transMeta );
    }
  }

  protected ObjectId renameKettleEntity( final RepositoryElementInterface transOrJob,
                                         final RepositoryDirectoryInterface newDirectory, final String newName ) throws KettleException {
    switch ( transOrJob.getRepositoryElementType() ) {
      case TRANSFORMATION:
        return renameTransformation( transOrJob.getObjectId(), null, newDirectory, newName );
      case JOB:
        return renameJob( transOrJob.getObjectId(), null, newDirectory, newName );
      default:
        throw new KettleException( "Unknown RepositoryObjectType. Should be TRANSFORMATION or JOB " );
    }
  }

  @Override
  public List<RepositoryObjectInterface> getChildren( String path, String filter ) {
    RepositoryRequest repoRequest = new RepositoryRequest();
    repoRequest.setDepth( -1 );
    repoRequest.setChildNodeFilter( "*" + filter + "*" );
    repoRequest.setIncludeAcls( false );
    repoRequest.setTypes( FILES_TYPE_FILTER.FILES_FOLDERS );
    repoRequest.setPath( path );
    repoRequest.setShowHidden( false );

    readWriteLock.readLock().lock();
    List<RepositoryFile> repositoryFiles;
    try {
      repositoryFiles = pur.getChildren( repoRequest );
    } finally {
      readWriteLock.readLock().unlock();
    }

    List<RepositoryObjectInterface> repositoryElementInterfaces = new ArrayList<>();
    for ( RepositoryFile repositoryFile : repositoryFiles ) {
      if ( repositoryFile.isFolder() ) {
        RepositoryDirectoryInterface repositoryDirectory = new RepositoryDirectory();
        repositoryDirectory.setName( repositoryFile.getName() );
        repositoryDirectory.setObjectId( () -> repositoryFile.getId().toString() );
        repositoryElementInterfaces.add( repositoryDirectory );
      } else {
        RepositoryObject repositoryObject = new RepositoryObject();
        repositoryObject.setName( repositoryFile.getName() );
        repositoryObject.setObjectId( () -> repositoryFile.getId().toString() );
        RepositoryObjectType repositoryObjectType = RepositoryObjectType.UNKNOWN;
        if ( repositoryFile.getName().endsWith( ".ktr" ) ) {
          repositoryObjectType = RepositoryObjectType.TRANSFORMATION;
        }
        if ( repositoryFile.getName().endsWith( ".kjb" ) ) {
          repositoryObjectType = RepositoryObjectType.JOB;
        }
        repositoryObject.setObjectType( repositoryObjectType );
        repositoryElementInterfaces.add( repositoryObject );
      }
    }
    return repositoryElementInterfaces;
  }

  @Override public boolean test() {
    String repoUrl = repositoryMeta.getRepositoryLocation().getUrl();
    final String url = repoUrl + ( repoUrl.endsWith( "/" ) ? "" : "/" ) + "webservices/unifiedRepository?wsdl";
    Service service;
    try {
      service = Service.create( new URL( url ), new QName( "http://www.pentaho.org/ws/1.0", "unifiedRepository" ) );
      if ( service != null ) {
        IUnifiedRepositoryJaxwsWebService repoWebService = service.getPort( IUnifiedRepositoryJaxwsWebService.class );
        if ( repoWebService != null ) {
          return true;
        }
      }
    } catch ( Exception e ) {
      return false;
    }
    return false;
  }

  private RepositoryFileTree loadRepositoryFileTreeFolders( String path, int depth, boolean includeAcls, boolean showHidden ) {
    RepositoryRequest repoRequest = new RepositoryRequest();
    repoRequest.setDepth( depth );
    repoRequest.setIncludeAcls( includeAcls );
    repoRequest.setChildNodeFilter( "*" );
    repoRequest.setTypes( FILES_TYPE_FILTER.FOLDERS );
    repoRequest.setPath( path );
    repoRequest.setShowHidden( showHidden );

    readWriteLock.readLock().lock();
    RepositoryFileTree result;
    try {
      result = pur.getTree( repoRequest );
    } finally {
      readWriteLock.readLock().unlock();
    }

    return result;
  }
}
