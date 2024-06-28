/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.vfs;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.connections.vfs.VFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser;
import org.pentaho.di.connections.vfs.provider.ConnectionFileProvider;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileNotFoundException;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSLocation;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;
import org.pentaho.di.plugins.fileopensave.providers.vfs.service.KettleVFSService;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by bmorrise on 2/14/19.
 */
public class VFSFileProvider extends BaseFileProvider<VFSFile> {

  public static final String NAME = "VFS Connections";
  public static final String TYPE = "vfs";

  protected final Bowl bowl;

  private Map<String, List<VFSFile>> roots = new HashMap<>();

  protected final KettleVFSService kettleVFSService;

  private final VFSConnectionManagerHelper vfsConnectionManagerHelper;

  private final ConnectionFileNameParser connectionFileNameParser;

  public VFSFileProvider() {
    this( DefaultBowl.getInstance() );
  }

  public VFSFileProvider( Bowl bowl ) {
    this(
      bowl,
      new KettleVFSService( bowl ),
      VFSConnectionManagerHelper.getInstance(),
      ConnectionFileNameParser.getInstance() );
  }

  public VFSFileProvider( Bowl bowl,
                          KettleVFSService kettleVFSService,
                          VFSConnectionManagerHelper vfsConnectionManagerHelper,
                          ConnectionFileNameParser connectionFileNameParser ) {
    this.bowl = Objects.requireNonNull( bowl );
    this.kettleVFSService = Objects.requireNonNull( kettleVFSService );
    this.vfsConnectionManagerHelper = Objects.requireNonNull( vfsConnectionManagerHelper );
    this.connectionFileNameParser = Objects.requireNonNull( connectionFileNameParser );
  }

  @Override public Class<VFSFile> getFileClass() {
    return VFSFile.class;
  }

  @Override public String getName() {
    return NAME;
  }

  @Override public String getType() {
    return TYPE;
  }

  private ConnectionManager getConnectionManager() throws MetaStoreException {
    return bowl.getConnectionManager();
  }

  /**
   * Determines if a <code>filePath</code> is a VFS file
   *
   * @param filePath
   * @return true if VFS file, false otherwise.
   */
  public boolean isSupported( String filePath ) {
    if ( filePath == null ) {
      return false;
    }

    boolean ret = false;
    try {
      connectionFileNameParser.parseUri( filePath );
      ret = true;
    } catch ( FileSystemException e ) {
      // DO NOTHING
    }

    return ret;
  }

  /**
   * @return
   */
  public boolean isAvailable() {
    return true;
  }

  /**
   * Default implementation; typically not called.
   *
   * @return Unfiltered Tree
   */
  @Override public Tree getTree() {
    return getTree( new ArrayList<>() );
  }

  /**
   * Filtered implementation
   *
   * @return Filter Tree
   */
  @Override public VFSTree getTree( List<String> connectionTypes ) {
    VFSTree vfsTree = new VFSTree( NAME );

    try {
      ConnectionManager manager = getConnectionManager();

      for ( VFSConnectionDetails details : vfsConnectionManagerHelper.getAllDetails( manager ) ) {
        if ( connectionTypes.isEmpty() || connectionTypes.contains( details.getType() ) ) {

          VFSLocation vfsLocation = new VFSLocation();
          vfsLocation.setName( details.getName() );
          vfsLocation.setRoot( NAME );
          vfsLocation.setHasChildren( true );
          vfsLocation.setCanDelete( false );
          vfsLocation.setPath( vfsConnectionManagerHelper.getConnectionRootFileName( details ).getURI() );

          // Cannot add bucket folders via the VFS file API.
          vfsLocation.setCanAddChildren( !usesBuckets( details ) );

          vfsTree.addChild( vfsLocation );
        }
      }
    } catch ( MetaStoreException | FileException e ) {
      // ignored
    }
    return vfsTree;
  }

  /**
   * Retrieves the "bucket" folders for a connection. These folders are populated from a call
   * to {@link VFSConnectionProvider#getLocations(VFSConnectionDetails)}.
   * <p/>
   * <p>
   * For a "bucket" folder some core functions of {@link FileObject} can't be called. To list a few,
   * {@link FileObject#exists()} and {@link FileType#hasChildren()} would return incorrect results.
   *
   * @param fileConnectionRoot
   * @return
   */
  private List<VFSFile> getBuckets( VFSFile fileConnectionRoot,
                                    ConnectionFileName connectionRootFileName,
                                    VFSConnectionDetails details )
    throws FileException {

    if ( this.roots.containsKey( getKey( fileConnectionRoot ) ) ) {
      return this.roots.get( getKey( fileConnectionRoot ) );
    }

    List<VFSFile> files = new ArrayList<>();

    List<VFSRoot> bucketRoots;
    try {
      VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
        vfsConnectionManagerHelper.getExistingProvider( getConnectionManager(), details );

      bucketRoots = vfsConnectionProvider.getLocations( details );
    } catch ( Exception e ) {
      // Catching either the very unlikely undefined provider for the given details,
      // or a RuntimeException's possibly thrown by getLocations(.) (see BACKLOG-36560)
      throw new FileException(
        "Error getting VFS locations. Check your credentials and for connectivity." + e.getMessage(), e );
    }

    if ( bucketRoots == null || bucketRoots.isEmpty() ) {
      throw new FileNotFoundException( fileConnectionRoot.getPath(), fileConnectionRoot.getProvider() );
    }

    for ( VFSRoot bucketRoot : bucketRoots ) {
      // Assuming bucket names do not have special characters, which should be the case,
      // given buckets take the domain position of provider URIs.
      ConnectionFileName bucketFileName =
        connectionRootFileName.createChildName( bucketRoot.getName(), FileType.FOLDER );

      VFSDirectory vfsDirectory = new VFSDirectory();
      vfsDirectory.setName( bucketRoot.getName() );
      vfsDirectory.setDate( bucketRoot.getModifiedDate() );
      vfsDirectory.setHasChildren( true );
      vfsDirectory.setCanAddChildren( true );
      vfsDirectory.setParent( fileConnectionRoot.getPath() );
      vfsDirectory.setPath( bucketFileName.getURI() );
      vfsDirectory.setRoot( NAME );

      files.add( vfsDirectory );
    }

    this.roots.put( getKey( fileConnectionRoot ), files );

    return files;
  }

  /**
   * Get key for <code>file</code>
   *
   * @param vfsFile
   * @return
   */
  protected String getKey( VFSFile vfsFile ) {
    return vfsFile.getPath();
  }

  /**
   * @param file
   * @param filters
   * @return
   */
  @Override
  public List<VFSFile> getFiles( VFSFile file, String filters, VariableSpace space ) throws FileException {

    ConnectionFileName fileName = getConnectionFileName( file );

    if ( fileName.isConnectionRoot() ) {
      VFSConnectionDetails details = getExistingDetails( fileName );
      if ( usesBuckets( details ) ) {
        return getBuckets( file, fileName, details );
      }
    }

    FileObject fileObject;
    try {
      fileObject = getFileObject( file, space );
    } catch ( FileException e ) {
      throw new FileNotFoundException( file.getPath(), TYPE );
    }

    return populateChildren( file, fileObject, filters );
  }

  private VFSConnectionDetails getExistingDetails( ConnectionFileName fileName ) throws FileException {
    try {
      String connectionName = fileName.getConnection();
      return getConnectionManager().getExistingDetails( connectionName );
    } catch ( MetaStoreException | KettleException e ) {
      throw new FileException( e );
    }
  }

  private boolean usesBuckets( VFSConnectionDetails details ) throws FileException {
    try {
      return vfsConnectionManagerHelper.usesBuckets( details );
    } catch ( KettleException e ) {
      throw new FileException( e );
    }
  }

  private ConnectionFileName getConnectionFileName( VFSFile file ) throws FileException {
    try {
      return connectionFileNameParser.parseUri( file.getPath() );
    } catch ( FileSystemException e ) {
      throw new FileException( e );
    }
  }

  /**
   * Check if a file object has children
   *
   * @param fileObject
   * @return
   */
  private boolean hasChildren( FileObject fileObject ) {
    try {
      return fileObject != null && fileObject.getType().hasChildren();
    } catch ( FileSystemException e ) {
      return false;
    }
  }

  /**
   * Get the children if they are available, if an error return an empty list
   *
   * @param fileObject
   * @return
   */
  private FileObject[] getChildren( FileObject fileObject ) {
    try {
      return fileObject != null ? fileObject.getChildren() : new FileObject[] {};
    } catch ( FileSystemException e ) {
      return new FileObject[] {};
    }
  }

  /**
   * Populate VFS file objects from vfs FileObject types
   *
   * @param parent
   * @param fileObject
   * @param filters
   * @return
   */
  private List<VFSFile> populateChildren( VFSFile parent, FileObject fileObject, String filters ) {
    List<VFSFile> files = new ArrayList<>();
    if ( fileObject != null && hasChildren( fileObject ) ) {
      FileObject[] children = getChildren( fileObject );
      for ( FileObject child : children ) {
        if ( hasChildren( child ) ) {
          files.add( createVFSDirectory( parent.getPath(), child ) );
        } else {
          if ( child != null && Utils.matches( child.getName().getBaseName(), filters ) ) {
            files.add( createVFSFile( parent.getPath(), child ) );
          }
        }
      }
    }
    return files;
  }

  @Override public VFSFile getFile( VFSFile file, VariableSpace space ) {
    try {
      FileObject fileObject = getFileObject( file, space );
      if ( !fileObject.exists() ) {
        return null;
      }
      String parent = null;
      if ( fileObject.getParent() != null && fileObject.getParent().getName() != null ) {
        parent = fileObject.getParent().getName().getURI();
      } else {
        parent = fileObject.getURL().getProtocol() + "://";
      }
      if ( fileObject.getType().equals( FileType.FOLDER ) ) {
        return createVFSDirectory( parent, fileObject );
      } else {
        return createVFSFile( parent, fileObject );
      }
    } catch ( FileException | FileSystemException e ) {
      // File does not exist
    }
    return null;
  }

  /**
   * @param files
   * @param space
   * @return
   */
  @Override
  public List<VFSFile> delete( List<VFSFile> files, VariableSpace space ) {
    List<VFSFile> deletedFiles = new ArrayList<>();
    for ( VFSFile file : files ) {
      try {
        FileObject fileObject = getFileObject( file, space );
        if ( fileObject.delete( getAllFileSelector() ) > 0 ) {
          deletedFiles.add( file );
        }
      } catch ( FileException | FileSystemException kfe ) {
        // Ignore don't add
      }
    }
    return deletedFiles;
  }

  /**
   * @param folder
   * @return
   */
  @Override public VFSFile add( VFSFile folder, VariableSpace space ) {
    try { // NOTE: parent call from FileController#add(File) is not used for VFSFileProvider
      FileObject fileObject = getFileObject( folder.getPath(), space );
      fileObject.createFolder();
      String parent = folder.getPath().substring( 0, folder.getPath().length() - 1 );
      return createVFSDirectory( parent, fileObject );
    } catch ( FileException | FileSystemException ignored ) {
      // Ignored
    }
    return null;
  }

  /**
   * @param file
   * @param newPath
   * @param overwriteStatus
   * @param space
   * @return
   */
  @Override public VFSFile rename( VFSFile file, String newPath, OverwriteStatus overwriteStatus,
                                   VariableSpace space ) {
    return doMove( file, newPath, overwriteStatus, space );
  }

  /**
   * @param file
   * @param toPath
   * @param overwriteStatus
   * @return
   * @Parem space
   */
  @Override
  public VFSFile move( VFSFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space ) {
    return doMove( file, toPath, overwriteStatus, space );
  }

  /**
   * @param file
   * @param newPath
   * @param overwriteStatus
   * @return
   */
  private VFSFile doMove( VFSFile file, String newPath, OverwriteStatus overwriteStatus, VariableSpace space ) {
    try {
      FileObject fileObject = getFileObject( file.getPath(), space );
      FileObject renameObject = getFileObject( newPath, space );

      if ( renameObject.exists() ) {
        overwriteStatus.promptOverwriteIfNecessary( file.getPath(),
          file.getEntityType().isDirectory() ? "folder" : "file" );
        if ( overwriteStatus.isOverwrite() ) {
          renameObject.delete();
        } else if ( overwriteStatus.isCancel() || overwriteStatus.isSkip() ) {
          return null;
        } else if ( overwriteStatus.isRename() ) {
          VFSDirectory vfsDir =
            createVFSDirectory( renameObject.getParent().getPath().toString(), renameObject );
          newPath = getNewName( vfsDir, newPath, space );
          renameObject = getFileObject( newPath, space );
        }
      }
      fileObject.moveTo( renameObject );
      if ( file instanceof VFSDirectory ) {
        return createVFSDirectory( renameObject.getParent().getPublicURIString(), renameObject );
      } else {
        return createVFSFile( renameObject.getParent().getPublicURIString(), renameObject );
      }
    } catch ( FileException | FileSystemException e ) {
      return null;
    }
  }

  /**
   * Note that this copy will only copy files within the SAME VFS connection.  For copies across different
   * connections use {@link org.pentaho.di.plugins.fileopensave.controllers.FileController#copyFileBetweenProviders}
   *
   * @param file
   * @param toPath
   * @param overwriteStatus
   * @return
   * @throws FileException
   */
  @Override
  public VFSFile copy( VFSFile file, String toPath, OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    try {
      overwriteStatus.setCurrentFileInProgressDialog( file.getPath() );

      FileObject fileObject = getFileObject( file, space );
      FileObject copyObject = getFileObject( toPath, space );
      overwriteStatus.promptOverwriteIfNecessary( copyObject.exists(), toPath,
        file.getEntityType().isDirectory() ? "folder" : "file"
        , null,
        "Note: Once this decision is made, the entire folder will be copied using a faster copy within the same"
          + " connection.  However, Any duplicate file encountered along the way can only be overwritten or skipped. "
          + " It can not be renamed." );
      if ( overwriteStatus.isCancel() || overwriteStatus.isSkip() ) {
        return null;
      }

      VFSFile toDirectory = null;
      if ( overwriteStatus.isRename() ) {
        toDirectory = createVFSDirectory( copyObject.getParent().getPublicURIString(), copyObject );
        String newDestination = getNewName( toDirectory, copyObject.getName().toString(), space );
        copyObject = getFileObject( newDestination, space );
      }

      copyObject.copyFrom( fileObject, new OverwriteAwareFileSelector( overwriteStatus, fileObject, copyObject,
        file.getConnection(), space ) );
      // Now get the return value
      if ( file instanceof VFSDirectory ) {
        return createVFSDirectory( copyObject.getParent().getPublicURIString(), fileObject );
      } else {
        return createVFSFile( copyObject.getParent().getPublicURIString(), fileObject );
      }
    } catch ( FileException | FileSystemException e ) {
      throw new FileException();
    }
  }

  /**
   * @param dir
   * @param path
   * @return
   * @throws FileException
   */
  @Override public boolean fileExists( VFSFile dir, String path, VariableSpace space ) throws FileException {
    String sanitizeName = sanitizeName( dir, path );
    try {
      FileObject fileObject = getFileObject( sanitizeName, space );
      return fileObject.exists();
    } catch ( FileException | FileSystemException e ) {
      throw new FileException();
    }
  }

  /**
   * @param file
   * @param space
   * @return
   */
  @Override
  public InputStream readFile( VFSFile file, VariableSpace space ) {
    try {
      FileObject fileObject = getFileObject( file.getPath(), space );
      return fileObject.getContent().getInputStream();
    } catch ( FileException | FileSystemException e ) {
      return null;
    }
  }

  /**
   * @param inputStream
   * @param destDir
   * @param path
   * @param overwriteStatus
   * @return
   * @throws FileException
   */
  @Override
  public VFSFile writeFile( InputStream inputStream, VFSFile destDir, String path, OverwriteStatus overwriteStatus,
                            VariableSpace space ) throws FileException {
    FileObject fileObject = getFileObject( path, space );
    if ( fileObject != null ) {
      try ( OutputStream outputStream = fileObject.getContent().getOutputStream(); ) {
        IOUtils.copy( inputStream, outputStream );
        outputStream.flush();
        return createVFSFile( destDir.getPath(), fileObject );
      } catch ( IOException e ) {
        return null;
      }
    }
    return null;
  }

  /**
   * Determines if two files are of type {@link VFSFile} and from the same VFS Connection.
   *
   * @param file1
   * @param file2
   * @return
   */
  @Override
  public boolean isSame( org.pentaho.di.plugins.fileopensave.api.providers.File file1,
                         org.pentaho.di.plugins.fileopensave.api.providers.File file2 ) {
    if ( file1 instanceof VFSFile && file2 instanceof VFSFile ) {
      String connection1 = getConnectionName( (VFSFile) file1 );
      String connection2 = getConnectionName( (VFSFile) file2 );

      return connection1 != null && connection1.equals( connection2 );
    }

    return false;
  }

  /**
   * Gets the (decoded) connection name from a given <code>vfsFile</code>
   *
   * @param vfsFile
   * @return connection name or null otherwise
   */
  protected String getConnectionName( VFSFile vfsFile ) {
    String connectionName = null;

    if ( vfsFile != null ) {
      try {
        connectionName = getConnectionFileName( vfsFile ).getConnection();
      } catch ( NullPointerException | FileException e ) {
        // DO NOTHING
      }
    }

    return connectionName;
  }

  /**
   * @param destDir
   * @param newPath
   * @return
   * @throws FileException
   */
  @Override public String getNewName( VFSFile destDir, String newPath, VariableSpace space ) throws FileException {
    String extension = Utils.getExtension( newPath );
    String parent = Utils.getParent( newPath, "/" );
    String name = Utils.getName( newPath, "/" ).replace( "." + extension, "" );
    int i = 1;
    String testName = sanitizeName( destDir, newPath );
    try {
      while ( getFileObject( testName, space ).exists() ) {
        if ( Utils.isValidExtension( extension ) ) {
          testName = sanitizeName( destDir, parent + name + "_" + i + "." + extension );
        } else {
          testName = sanitizeName( destDir, newPath + "_" + i );
        }
        i++;
      }
    } catch ( FileException | FileSystemException e ) {
      return testName;
    }
    return testName;
  }

  /**
   * @param file
   * @return
   */
  @Override public VFSFile getParent( VFSFile file ) {
    VFSFile vfsFile = new VFSFile();
    vfsFile.setConnection( file.getConnection() );
    vfsFile.setPath( file.getParent() );
    return vfsFile;
  }

  @Override public String sanitizeName( VFSFile destDir, String newPath ) {
    if ( newPath.startsWith( ConnectionFileProvider.ROOT_URI ) ) {
      return newPath;
    }

    VFSConnectionProvider<VFSConnectionDetails> provider = getConnectionProvider( newPath );
    if ( provider == null ) {
      return newPath;
    }

    return provider.sanitizeName( newPath );
  }

  private VFSConnectionProvider<VFSConnectionDetails> getConnectionProvider( String key ) {
    try {
      return vfsConnectionManagerHelper.getProvider( getConnectionManager(), key );
    } catch ( MetaStoreException ex ) {
      return null;
    }
  }

  public void clearProviderCache() {
    this.roots = new HashMap<>();
  }

  @Override public VFSFile createDirectory( String parentPath, VFSFile fileParent, String newDirectoryName ) {
    try {
      String newDirectoryPath = fileParent.getPath() + VFSFile.DELIMITER + newDirectoryName;
      FileObject fileObject = getFileObject( newDirectoryPath, new Variables() );
      fileObject.createFolder();
      return createVFSDirectory( parentPath, fileObject );
    } catch ( FileException | FileSystemException ignored ) {
      // Ignored
    }
    return null;
  }

  /**
   * Wrapper around {@link KettleVFSService#getFileObject(String, VariableSpace)}
   *
   * @param file
   * @param space
   * @return
   * @throws FileException
   */
  protected FileObject getFileObject( VFSFile file, VariableSpace space ) throws FileException {
    return getFileObject( file.getPath(), space );
  }

  /**
   * Wrapper around {@link KettleVFSService#getFileObject(String, VariableSpace)}
   *
   * @param vfsPath
   * @param space
   * @return
   * @throws FileException
   */
  protected FileObject getFileObject( String vfsPath, VariableSpace space ) throws FileException {
    return kettleVFSService.getFileObject( vfsPath, space );
  }

  /**
   * Wrapper around {@link VFSFile#create(String, FileObject, String, String)}.
   *
   * @param parent
   * @param fileObject
   * @return
   */
  protected VFSFile createVFSFile( String parent, FileObject fileObject ) {
    // NOTE: connection and domain are null since they are deprecated
    return VFSFile.create( parent, fileObject, null, null );
  }

  /**
   * Wrapper around {@link VFSDirectory#create(String, FileObject, String, String)}
   *
   * @return
   */
  protected VFSDirectory createVFSDirectory( String parent, FileObject fileObject ) {
    // NOTE: connection and domain are null since they are deprecated
    return VFSDirectory.create( parent, fileObject, null, null );
  }


  private FileSelector getAllFileSelector() {
    return new FileSelector() {
      @Override public boolean includeFile( FileSelectInfo fileInfo ) throws Exception {
        return true;
      }

      @Override public boolean traverseDescendents( FileSelectInfo fileInfo ) throws Exception {
        return true;
      }
    };
  }

}
