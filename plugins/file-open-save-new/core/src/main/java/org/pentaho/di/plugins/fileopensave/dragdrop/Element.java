/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023-2024 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.plugins.fileopensave.dragdrop;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.plugins.fileopensave.api.providers.Entity;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.Tree;
import org.pentaho.di.plugins.fileopensave.api.providers.Utils;
import org.pentaho.di.plugins.fileopensave.controllers.FileController;
import org.pentaho.di.plugins.fileopensave.providers.local.LocalFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalDirectory;
import org.pentaho.di.plugins.fileopensave.providers.local.model.LocalFile;
import org.pentaho.di.plugins.fileopensave.providers.recents.model.RecentFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryDirectory;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryObjectId;
import org.pentaho.di.plugins.fileopensave.providers.vfs.VFSFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSDirectory;
import org.pentaho.di.plugins.fileopensave.providers.vfs.model.VFSFile;
import org.pentaho.di.plugins.fileopensave.service.FileCacheService;
import org.pentaho.di.plugins.fileopensave.service.ProviderServiceService;
import org.pentaho.di.plugins.fileopensave.util.Util;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.ui.spoon.Spoon;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class Element {
  private String name = "";
  private EntityType entityType = EntityType.UNKNOWN;
  private String path = "";
  private String provider = "";
  private String repositoryName = "";//For repository types

  /**
   * Separate VFS domain variable is no longer needed.
   * @deprecated
   * The domain handled is internally with the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  private String domain = "";  //For VFS types

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  private String connection = ""; //The VFS connection name

  @VisibleForTesting
  static Spoon spoonInstance = Spoon.getInstance();

  private static final FileController
    FILE_CONTROLLER = new FileController( FileCacheService.INSTANCE.get(), ProviderServiceService.get() );

  // Use for Local or NamedCluster items
  public Element( String name, EntityType entityType, String path, String provider ) {
    this( name, entityType, path, provider, "" );
  }

  public Element( String name, EntityType entityType, String path, String provider, String repositoryName ) {
    this.name = name;
    this.entityType = entityType;
    this.path = path;
    this.provider = provider;
    this.repositoryName = repositoryName;
  }

  public Element( Object genericObject ) {
    if ( !( genericObject instanceof Entity ) ) {
      throw new IllegalArgumentException( "Cannot construct Element. " + genericObject.getClass() + " is not an"
        + " instance of Entity" );
    }
    entityType = ( (Entity) genericObject ).getEntityType();
    if ( entityType == EntityType.TREE ) {
      //Tree's have no parent
      Tree tree = (Tree) genericObject;
      name = tree.getName();
      path = ""; //No path for these, they are all attached to root
      provider = tree.getProvider();
      repositoryName = "";
      domain = "";
      connection = "";
    } else {
      //Directories are also Files
      if ( genericObject instanceof File ) {
        File file = (File) genericObject;
        name = file.getName();
        path = file.getPath();
        provider = file.getProvider();
        if ( entityType.isRepositoryType() ) {
          repositoryName = ( (RepositoryFile) file ).getRepository();
        }
      }
    }
  }

  @Override
  public boolean equals( Object object ) {
    if ( !( object instanceof Element ) ) {
      return false;
    }
    if ( this == object ) {
      return true;
    }
    Element other = ( (Element) object );
    return Objects.equals( this.name, other.name )
        && Objects.equals( this.path, other.path )
        && Objects.equals( this.provider, other.provider );
  }

  @Override
  public int hashCode() {
    return Objects.hash( name, path, provider );
  }

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath( String path ) {
    this.path = path;
  }

  public String getProvider() {
    return provider;
  }

  public String getRepositoryName() {
    return repositoryName;
  }

  /**
   * Separate VFS domain variable is no longer needed.
   * @deprecated
   * The domain handled is internally with the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getDomain() {
    return domain;
  }

  /**
   * Separate VFS domain variable is no longer needed.
   * @deprecated
   * The domain handled is internally with the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public void setDomain( String domain ) {
    this.domain = domain;
  }

  /**
   * Separate VFS connection name variable is no longer needed.
   * @deprecated
   * The connection name is in the URI since full {@value org.pentaho.di.connections.vfs.provider.ConnectionFileProvider#SCHEME } paths are being used.
   */
  @Deprecated
  public String getConnection() {
    return connection;
  }

  public EntityType getEntityType() {
    return entityType;
  }

  public void setEntityType( EntityType entityType ) {
    this.entityType = entityType;
  }

  public String toString() {
    return entityType.name() + "   path: " + path + "  name: " + name;
  }

  public File convertToFile( VariableSpace variables ) {
    try {
      Path localPath;
      switch( entityType ) {
        case LOCAL_DIRECTORY:
          localPath = Paths.get( getPath() );
          return localPath.getParent() == null ? LocalDirectory.create( null, localPath ) :
            LocalDirectory.create( localPath.getParent().toString(), localPath );
        case LOCAL_FILE:
          localPath = Paths.get( getPath() );
          return LocalFile.create( localPath.getParent().toString(), localPath );
        case RECENT_FILE:
          return RecentFile.create( convertToLastUsedFile() );
        case REPOSITORY_DIRECTORY:
          return getRepoDirectory();
        case REPOSITORY_FILE:
          return RepositoryFile.create( convertToLastUsedFile(), getRepoId() );
        case VFS_DIRECTORY:
          return VFSDirectory.create( calcEntityParent(), convertToFileObject( variables ), connection, domain );
        case VFS_FILE:
          return VFSFile.create( calcEntityParent(), convertToFileObject( variables ), connection, domain );
        case VFS_LOCATION:
          return VFSDirectory.create( calcEntityParent(), convertToFileObject( variables ), connection, domain );
        case TREE:
        case NAMED_CLUSTER_DIRECTORY:
        case NAMED_CLUSTER_LOCATION:
        case NAMED_CLUSTER_FILE:
          return FILE_CONTROLLER.getFile( this );
        default: //nothing to do
      }
    } catch ( KettleFileException e ) {
      e.printStackTrace();
    }
    return null;
  }

  private File getRepoDirectory() {
    try {
      RepositoryDirectoryInterface rdi = spoonInstance.rep.findDirectory( path );
      if ( rdi == null ) {
        return RepositoryDirectory.build( Util.getFolder( path ), Util.getName( path ), null, true );
      }
      return RepositoryDirectory.build( calcEntityParent(), rdi );
    } catch ( KettleException e ) {
      return null;
    }
  }

  private String calcEntityParent() {
    return Util.getFolder( path );
  }

  public FileObject convertToFileObject( VariableSpace variables ) throws KettleFileException {
    return KettleVFS.getFileObject( path, variables );
  }

  public EntityType calcParentEntityType() {
    // All File(s) have a parent that is a Directory unless the File is a root folder.  The entity type of that parent
    // can be determined by the entityType of the child.
    switch( getEntityType() ) {
      case LOCAL_DIRECTORY:
      case LOCAL_FILE:
      case RECENT_FILE:
        return EntityType.LOCAL_DIRECTORY;
      case REPOSITORY_DIRECTORY:
      case REPOSITORY_FILE:
      case REPOSITORY_OBJECT:
        return EntityType.REPOSITORY_DIRECTORY;
      case VFS_DIRECTORY:
      case VFS_FILE:
        return EntityType.VFS_DIRECTORY;
      case VFS_LOCATION:
      case TREE:
        return EntityType.UNKNOWN;
      case NAMED_CLUSTER_DIRECTORY:
      case NAMED_CLUSTER_FILE:
        return EntityType.NAMED_CLUSTER_DIRECTORY;
      case NAMED_CLUSTER_LOCATION:
        return EntityType.UNKNOWN;
      case TEST_FILE:
      case TEST_DIRECTORY:
        return EntityType.TEST_DIRECTORY;
      default: //Nothing to do
    }
    return EntityType.UNKNOWN;
  }

  private LastUsedFile convertToLastUsedFile() {
    String fileType;
    String extension = FilenameUtils.getExtension( getPath() );
    if ( File.KJB.equals( "." + extension ) ) {
      fileType = LastUsedFile.FILE_TYPE_JOB;
    } else if ( File.KTR.equals( "." + extension ) ) {
      fileType = LastUsedFile.FILE_TYPE_TRANSFORMATION;
    } else {
      fileType = LastUsedFile.FILE_TYPE_CUSTOM;
    }
    String effectivePath = entityType.isDirectory() ? path : calcEntityParent();
    if ( entityType.isRepositoryType() || repositoryName != null && !repositoryName.isEmpty() ) {
      return new LastUsedFile( fileType, name, effectivePath, true, repositoryName, false, 0 );
    } else {
      //more inconstistancy
      return new LastUsedFile( fileType, path, effectivePath, true, repositoryName, false, 0 );
    }
  }

  private ObjectId getRepoId() {
    ObjectId objectId = null;
    try {
      if ( entityType.isDirectory() ) {
        objectId = spoonInstance.rep.findDirectory( path ).getObjectId();
      } else if ( entityType.isFile() ) {
        if ( "ktr".equals( Utils.getExtension( path ) ) ) {
          objectId = spoonInstance.rep.getTransformationID( Util.getName( path ),
            spoonInstance.rep.findDirectory( Util.getFolder( path ) ) );
        } else if ( "kjb".equals( Utils.getExtension( path ) ) ) {
          objectId = spoonInstance.rep.getJobId( Util.getName( path ),
            spoonInstance.rep.findDirectory( Util.getFolder( path ) ) );
        }
      }
      if ( objectId == null ) {
        objectId = new RepositoryObjectId( null );
      }
      return objectId;
    } catch ( KettleException e ) {
      // Could only happen if destination got deleted during drag
    }
    return null;
  }

  /**
   * Converts a recent element to the actual element it was meant to point to.
   *
   * @return File
   */
  public Element convertRecent() {
    if ( entityType != EntityType.RECENT_FILE ) {
      throw new IllegalArgumentException( "Can only convert RecentFiles" );
    }
    Element newElement = null;
    try {
      URI uri = new URI( path );
      String scheme = uri.getScheme();
      if ( scheme == null ) {
        scheme = ""; //Switch won't accept a null
      }

      switch( scheme ) {
        case "pvfs":
          newElement = new Element( name, EntityType.VFS_FILE, path, VFSFileProvider.TYPE );
          break;
        case "hc":
          newElement = new Element( name, EntityType.NAMED_CLUSTER_FILE, path, "clusters" );
          break;
        default:
          if ( scheme.startsWith( "[" ) && scheme.endsWith( "]" ) ) {
            //Looks like a repo entry
            newElement = new Element( name, EntityType.REPOSITORY_FILE, path, RepositoryFileProvider.TYPE,
              scheme.substring( 1, scheme.length() - 2 ) );
          } else {
            //no schema or other schema here.  Try local
            newElement = new Element( name, EntityType.LOCAL_FILE, path, LocalFileProvider.TYPE );
          }
      }

    } catch ( URISyntaxException e ) {
      //If here we must be getting a local windows path with \
      newElement = new Element( name, EntityType.LOCAL_FILE, path, LocalFileProvider.TYPE );
    }
    return newElement;
  }

}
