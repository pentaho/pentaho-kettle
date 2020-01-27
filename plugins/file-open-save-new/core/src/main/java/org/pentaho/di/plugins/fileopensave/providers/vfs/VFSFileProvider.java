/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ConnectionProvider;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.connections.vfs.VFSRoot;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileNotFoundException;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSLocation;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/14/19.
 */
public class VFSFileProvider extends BaseFileProvider<VFSFile> {

  public static final String NAME = "VFS Connections";
  public static final String TYPE = "vfs";
  public static final String DOMAIN_ROOT = "[\\w]+://";

  private Supplier<ConnectionManager> connectionManagerSupplier = ConnectionManager::getInstance;
  private Map<String, List<VFSFile>> roots = new HashMap<>();

  @Override public Class<VFSFile> getFileClass() {
    return VFSFile.class;
  }

  @Override public String getName() {
    return NAME;
  }

  @Override public String getType() {
    return TYPE;
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

    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      connectionManagerSupplier.get().getProvidersByType( VFSConnectionProvider.class );

    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      for ( ConnectionDetails connectionDetails : provider.getConnectionDetails() ) {
        VFSConnectionDetails vfsConnectionDetails = (VFSConnectionDetails) connectionDetails;
        VFSLocation vfsLocation = new VFSLocation();
        vfsLocation.setName( connectionDetails.getName() );
        vfsLocation.setRoot( NAME );
        vfsLocation.setHasChildren( true );
        vfsLocation.setCanDelete( false );
        vfsLocation.setPath( vfsConnectionDetails.getType() + "://" + vfsConnectionDetails.getDomain() );
        vfsLocation.setDomain( vfsConnectionDetails.getDomain() );
        vfsLocation.setConnection( connectionDetails.getName() );
        if ( connectionTypes.isEmpty() || connectionTypes.contains( connectionDetails.getType() ) ) {
          vfsTree.addChild( vfsLocation );
        }
      }
    }

    return vfsTree;
  }

  /**
   * @param file
   * @return
   */
  private List<VFSFile> getRoot( VFSFile file ) throws FileException {
    if ( this.roots.containsKey( file.getConnection() ) ) {
      return this.roots.get( file.getConnection() );
    }
    List<VFSFile> files = new ArrayList<>();

    VFSConnectionDetails vfsConnectionDetails =
      (VFSConnectionDetails) ConnectionManager.getInstance().getConnectionDetails( file.getConnection() );

    @SuppressWarnings( "unchecked" )
    VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
      (VFSConnectionProvider<VFSConnectionDetails>) ConnectionManager.getInstance()
        .getConnectionProvider( vfsConnectionDetails.getType() );

    List<VFSRoot> vfsRoots = vfsConnectionProvider.getLocations( vfsConnectionDetails );
    if ( vfsRoots.isEmpty() ) {
      throw new FileNotFoundException( file.getPath(), file.getProvider() );
    }

    String scheme = vfsConnectionProvider.getProtocol( vfsConnectionDetails );
    for ( VFSRoot root : vfsRoots ) {
      VFSDirectory vfsDirectory = new VFSDirectory();
      vfsDirectory.setName( root.getName() );
      vfsDirectory.setDate( root.getModifiedDate() );
      vfsDirectory.setHasChildren( true );
      vfsDirectory.setCanAddChildren( true );
      vfsDirectory.setParent( scheme + "://" );
      vfsDirectory.setDomain( vfsConnectionDetails.getDomain() );
      vfsDirectory.setConnection( vfsConnectionDetails.getName() );
      vfsDirectory.setPath( scheme + "://" + root.getName() );
      vfsDirectory.setRoot( NAME );
      files.add( vfsDirectory );
    }
    this.roots.put( file.getConnection(), files );
    return files;
  }

  /**
   * @param file
   * @param filters
   * @return
   */
  @Override
  public List<VFSFile> getFiles( VFSFile file, String filters ) throws FileException {
    if ( file.getPath().matches( DOMAIN_ROOT ) ) {
      return getRoot( file );
    }
    FileObject fileObject;
    try {
      fileObject = KettleVFS
        .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
    } catch ( KettleFileException e ) {
      throw new FileNotFoundException( file.getPath(), TYPE );
    }
    return populateChildren( file, fileObject, filters );
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
          files.add( VFSDirectory.create( parent.getPath(), child, parent.getConnection(), parent.getDomain() ) );
        } else {
          if ( child != null && Utils.matches( child.getName().getBaseName(), filters ) ) {
            files.add( VFSFile.create( parent.getPath(), child, parent.getConnection(), parent.getDomain() ) );
          }
        }
      }
    }
    return files;
  }

  @Override public VFSFile getFile( VFSFile file ) {
    try {
      FileObject fileObject = KettleVFS
        .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
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
        return VFSDirectory.create( parent, fileObject, null, file.getDomain() );
      } else {
        return VFSFile.create( parent, fileObject, null, file.getDomain() );
      }
    } catch ( KettleFileException | FileSystemException e ) {
      // File does not exist
    }
    return null;
  }

  /**
   * @param files
   * @return
   */
  public List<VFSFile> delete( List<VFSFile> files ) {
    List<VFSFile> deletedFiles = new ArrayList<>();
    for ( VFSFile file : files ) {
      try {
        FileObject fileObject = KettleVFS
          .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
        if ( fileObject.delete() ) {
          deletedFiles.add( file );
        }
      } catch ( KettleFileException | FileSystemException kfe ) {
        // Ignore don't add
      }
    }
    return deletedFiles;
  }

  /**
   * @param folder
   * @return
   */
  @Override public VFSFile add( VFSFile folder ) {
    try {
      FileObject fileObject = KettleVFS
        .getFileObject( folder.getPath(), new Variables(),
          VFSHelper.getOpts( folder.getPath(), folder.getConnection() ) );
      fileObject.createFolder();
      String parent = folder.getPath().substring( 0, folder.getPath().length() - 1 );
      return VFSDirectory.create( parent, fileObject, folder.getConnection(), folder.getDomain() );
    } catch ( KettleFileException | FileSystemException ignored ) {
      // Ignored
    }
    return null;
  }

  /**
   * @param file
   * @param newPath
   * @param overwrite
   * @return
   */
  @Override public VFSFile rename( VFSFile file, String newPath, boolean overwrite ) {
    return doMove( file, newPath, overwrite );
  }

  /**
   * @param file
   * @param toPath
   * @param overwrite
   * @return
   */
  @Override
  public VFSFile move( VFSFile file, String toPath, boolean overwrite ) {
    return doMove( file, toPath, overwrite );
  }

  /**
   * @param file
   * @param newPath
   * @param overwrite
   * @return
   */
  private VFSFile doMove( VFSFile file, String newPath, boolean overwrite ) {
    try {
      FileObject fileObject = KettleVFS
        .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
      FileObject renameObject = KettleVFS
        .getFileObject( newPath, new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
      if ( overwrite && renameObject.exists() ) {
        renameObject.delete();
      }
      fileObject.moveTo( renameObject );
      if ( file instanceof VFSDirectory ) {
        return VFSDirectory.create( renameObject.getParent().getPublicURIString(), renameObject, file.getConnection(),
          file.getDomain() );
      } else {
        return VFSFile.create( renameObject.getParent().getPublicURIString(), renameObject, file.getConnection(),
          file.getDomain() );
      }
    } catch ( KettleFileException | FileSystemException e ) {
      return null;
    }
  }

  /**
   * @param file
   * @param toPath
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override
  public VFSFile copy( VFSFile file, String toPath, boolean overwrite ) throws FileException {
    try {
      FileObject fileObject = KettleVFS
        .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
      FileObject copyObject =
        KettleVFS.getFileObject( toPath, new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
      copyObject.copyFrom( fileObject, Selectors.SELECT_SELF );
      if ( file instanceof VFSDirectory ) {
        return VFSDirectory
          .create( copyObject.getParent().getPublicURIString(), fileObject, file.getConnection(), file.getDomain() );
      } else {
        return VFSFile
          .create( copyObject.getParent().getPublicURIString(), fileObject, file.getConnection(), file.getDomain() );
      }
    } catch ( KettleFileException | FileSystemException e ) {
      throw new FileException();
    }
  }

  /**
   * @param dir
   * @param path
   * @return
   * @throws FileException
   */
  @Override public boolean fileExists( VFSFile dir, String path ) throws FileException {
    path = sanitizeName( dir, path );
    try {
      FileObject fileObject =
        KettleVFS.getFileObject( path, new Variables(), VFSHelper.getOpts( path, dir.getConnection() ) );
      return fileObject.exists();
    } catch ( KettleFileException | FileSystemException e ) {
      throw new FileException();
    }
  }

  /**
   * @param file
   * @return
   */
  public InputStream readFile( VFSFile file ) {
    try {
      FileObject fileObject = KettleVFS
        .getFileObject( file.getPath(), new Variables(), VFSHelper.getOpts( file.getPath(), file.getConnection() ) );
      return fileObject.getContent().getInputStream();
    } catch ( KettleException | FileSystemException e ) {
      return null;
    }
  }

  /**
   * @param inputStream
   * @param destDir
   * @param path
   * @param overwrite
   * @return
   * @throws FileException
   */
  @Override public VFSFile writeFile( InputStream inputStream, VFSFile destDir,
                                      String path, boolean overwrite )
    throws FileException {
    FileObject fileObject = null;
    try {
      fileObject = KettleVFS
        .getFileObject( path, new Variables(), VFSHelper.getOpts( destDir.getPath(), destDir.getConnection() ) );
    } catch ( KettleException ke ) {
      throw new FileException();
    }
    if ( fileObject != null ) {
      try ( OutputStream outputStream = fileObject.getContent().getOutputStream(); ) {
        IOUtils.copy( inputStream, outputStream );
        outputStream.flush();
        return VFSFile.create( destDir.getPath(), fileObject, destDir.getConnection(), destDir.getDomain() );
      } catch ( IOException e ) {
        return null;
      }
    }
    return null;
  }

  /**
   * @param file1
   * @param file2
   * @return
   */
  @Override
  public boolean isSame( org.pentaho.di.plugins.fileopensave.api.providers.File file1,
                         org.pentaho.di.plugins.fileopensave.api.providers.File file2 ) {
    if ( file1 instanceof VFSFile && file2 instanceof VFSFile ) {
      VFSFile vfsFile1 = (VFSFile) file1;
      VFSFile vfsFile2 = (VFSFile) file2;
      return vfsFile1.getConnection().equals( vfsFile2.getConnection() );
    }
    return false;
  }

  /**
   * @param destDir
   * @param newPath
   * @return
   * @throws FileException
   */
  @Override public String getNewName( VFSFile destDir, String newPath ) throws FileException {
    String extension = Utils.getExtension( newPath );
    String parent = Utils.getParent( newPath );
    String name = Utils.getName( newPath ).replace( "." + extension, "" );
    int i = 1;
    String testName = sanitizeName( destDir, newPath );
    try {
      while ( KettleVFS
        .getFileObject( testName, new Variables(), VFSHelper.getOpts( testName, destDir.getConnection() ) )
        .exists() ) {
        if ( Utils.isValidExtension( extension ) ) {
          testName = sanitizeName( destDir, parent + name + " " + i + "." + extension );
        } else {
          testName = sanitizeName( destDir, newPath + " " + i );
        }
        i++;
      }
    } catch ( KettleFileException | FileSystemException e ) {
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
    return getConnectionProvider( newPath ).sanitizeName( newPath );
  }

  private VFSConnectionProvider<VFSConnectionDetails> getConnectionProvider( String key ) {
    @SuppressWarnings( "unchecked" )
    VFSConnectionProvider<VFSConnectionDetails> vfsConnectionProvider =
      (VFSConnectionProvider<VFSConnectionDetails>) ConnectionManager.getInstance()
        .getConnectionProvider( key );
    return vfsConnectionProvider;
  }

  public void clearProviderCache() {
    this.roots = new HashMap<>();
  }
}
