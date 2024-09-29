/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.vfs;

import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.FileName;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.connections.vfs.provider.ConnectionFileSystem;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.vfs.configuration.IKettleFileSystemConfigBuilder;
import org.pentaho.di.core.vfs.configuration.KettleFileSystemConfigBuilderFactory;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;

/**
 * Implementation of IKettleVFS. Implementation of VFS file access for kettle. Use KettleVFS.getInstance( Bowl ) to get
 * an instance.
 *
 */
public class KettleVFSImpl implements IKettleVFS {
  public static final String CONNECTION = ConnectionFileSystem.CONNECTION;

  private static Class<?> PKG = KettleVFS.class; // for i18n purposes, needed by Translator2!!
  private static final int TIMEOUT_LIMIT = 9000;
  private static final int TIME_TO_SLEEP_STEP = 50;

  private final Bowl bowl;

  private static VariableSpace defaultVariableSpace;

  static {
    // Create a new empty variable space...
    //
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
  }

  // non-public. Should only be created by the factory in KettleVFS
  KettleVFSImpl( Bowl bowl ) {
    this.bowl = Preconditions.checkNotNull( bowl );
  }

  @Override
  public FileObject getFileObject( String vfsFilename ) throws KettleFileException {
    return getFileObject( vfsFilename, defaultVariableSpace );
  }

  @Override
  public FileObject getFileObject( String vfsFilename, VariableSpace space ) throws KettleFileException {
    return getFileObject( vfsFilename, space, null );
  }

  @Override
  public FileObject getFileObject( String vfsFilename, FileSystemOptions fsOptions ) throws KettleFileException {
    return getFileObject( vfsFilename, defaultVariableSpace, fsOptions );
  }

  // IMPORTANT:
  // We have one problem with VFS: if the file is in a subdirectory of the current one: somedir/somefile
  // In that case, VFS doesn't parse the file correctly.
  // We need to put file: in front of it to make it work.
  // However, how are we going to verify this?
  //
  // We are going to see if the filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
  // If not, we are going to assume it's a file, when no scheme found ( flag as null ), and it only changes if
  // a scheme is provided.
  //
  @Override
  public FileObject getFileObject( String vfsFilename, VariableSpace space, FileSystemOptions fsOptions )
    throws KettleFileException {

    //  Protect the code below from invalid input.
    if ( vfsFilename == null ) {
      throw new IllegalArgumentException( "Unexpected null VFS filename." );
    }

    try {
      FileSystemManager fsManager = KettleVFS.getInstance().getFileSystemManager();

      String scheme = getSchemeSafe( vfsFilename, fsManager );

      fsOptions = getFileSystemOptions( scheme, vfsFilename, space, fsOptions );

      String filename = KettleVFS.normalizePath( vfsFilename, scheme );

      return fsOptions != null ? fsManager.resolveFile( filename, fsOptions ) : fsManager.resolveFile( filename );

    } catch ( IOException e ) {
      throw new KettleFileException( "Unable to get VFS File object for filename '"
        + KettleVFS.cleanseFilename( vfsFilename ) + "' : " + e.getMessage(), e );
    }
  }

  private static String getSchemeSafe( String vfsFilename, FileSystemManager fsManager ) {
    String[] schemes = fsManager.getSchemes();

    String scheme = KettleVFS.getScheme( schemes, vfsFilename );

    //Waiting condition - PPP-4374:
    //We have to check for hasScheme even if scheme is null because that scheme could not
    //be available by getScheme at the time we validate our scheme flag ( Kitchen loading problem )
    //So we check if - even it has not a scheme - our vfsFilename has a possible scheme format (PROVIDER_PATTERN_SCHEME)
    //If it does, then give it some time and tries to load. It stops when timeout is up or a scheme is found.
    int timeOut = TIMEOUT_LIMIT;
    if ( KettleVFS.hasSchemePattern( vfsFilename, KettleVFS.PROVIDER_PATTERN_SCHEME ) ) {
      while (scheme == null && timeOut > 0) {
        // ask again to refresh schemes list
        schemes = fsManager.getSchemes();
        try {
          Thread.sleep( TIME_TO_SLEEP_STEP );
          timeOut -= TIME_TO_SLEEP_STEP;
          scheme = KettleVFS.getScheme( schemes, vfsFilename );
        } catch ( InterruptedException e ) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }

    return scheme;
  }

  @Override
  public FileName resolveURI( String vfsFilename ) throws KettleFileException {
    try {
      FileSystemManager fsManager = KettleVFS.getInstance().getFileSystemManager();

      // Make sure to wait for all providers to be loaded.
      getSchemeSafe( vfsFilename, fsManager );

      return fsManager.resolveURI( vfsFilename );

    } catch ( FileSystemException e ) {
      throw new KettleFileException( "Unable to resolve file name for VFS URI '"
        + KettleVFS.cleanseFilename( vfsFilename ) + "' : " + e.getMessage(), e );
    }
  }

  @Override
  public FileSystemOptions getFileSystemOptions( String scheme, String vfsFilename, VariableSpace space,
                                                  FileSystemOptions fileSystemOptions )
    throws IOException {
    if ( scheme == null ) {
      return fileSystemOptions;
    }

    return buildFsOptions( space, fileSystemOptions, vfsFilename, scheme );
  }

  private FileSystemOptions buildFsOptions( VariableSpace parentVariableSpace, FileSystemOptions sourceOptions,
                                            String vfsFilename, String scheme ) throws IOException {
    VariableSpace varSpace = parentVariableSpace;
    if ( vfsFilename == null ) {
      return null;
    }
    if ( varSpace == null ) {
      varSpace = defaultVariableSpace;
    }

    IKettleFileSystemConfigBuilder configBuilder =
      KettleFileSystemConfigBuilderFactory.getConfigBuilder( varSpace, scheme );

    FileSystemOptions fsOptions = ( sourceOptions == null ) ? new FileSystemOptions() : sourceOptions;
    configBuilder.setBowl( fsOptions, bowl );

    try {

      String[] varList = varSpace.listVariables();

      for ( String var : varList ) {
        if ( var.equalsIgnoreCase( CONNECTION ) && varSpace.getVariable( var ) != null ) {
          FileSystemOptions fileSystemOptions = VFSHelper.getOpts( bowl, vfsFilename, varSpace.getVariable( var ),
                                                                   varSpace );
          if ( fileSystemOptions != null ) {
            return fileSystemOptions;
          }
        }
        if ( var.startsWith( "vfs." ) ) {
          String param = configBuilder.parseParameterName( var, scheme );
          String varScheme = KettleGenericFileSystemConfigBuilder.extractScheme( var );
          if ( param != null ) {
            if ( varScheme == null || varScheme.equals( "sftp" ) || varScheme.equals( scheme ) ) {
              configBuilder.setParameter( fsOptions, param, varSpace.getVariable( var ), var, vfsFilename );
            }
          } else {
            throw new IOException( "FileSystemConfigBuilder could not parse parameter: " + var );
          }
        }
      }
      if ( scheme.equals( "pvfs" ) ) {
        configBuilder.setParameter( fsOptions, "VariableSpace", varSpace, vfsFilename );
      }
    } catch ( MetaStoreException ex ) {
      // keep backward compatible API in KettleVFS
      throw new IOException( ex );
    }
    return fsOptions;
  }

  @Override
  public String getFriendlyURI( String filename ) {
    return getFriendlyURI( filename, defaultVariableSpace );
  }

  @Override
  public String getFriendlyURI( String filename, VariableSpace space ) {
    if ( filename == null ) {
      return null;
    }

    String friendlyName;
    try {
      friendlyName = KettleVFS.getFriendlyURI( getFileObject( filename, space ) );
    } catch ( Exception e ) {
      // unable to get a friendly name from VFS object.
      // Cleanse name of pwd before returning
      friendlyName = KettleVFS.cleanseFilename( filename );
    }
    return friendlyName;
  }


  @Override
  public String getTextFileContent( String vfsFilename, String charSetName ) throws KettleFileException {
    return getTextFileContent( vfsFilename, null, charSetName );
  }

  @Override
  public String getTextFileContent( String vfsFilename, VariableSpace space, String charSetName )
    throws KettleFileException {
    try {
      InputStream inputStream = null;

      if ( space == null ) {
        inputStream = getInputStream( vfsFilename );
      } else {
        inputStream = getInputStream( vfsFilename, space );
      }
      InputStreamReader reader = new InputStreamReader( inputStream, charSetName );
      int c;
      StringBuilder aBuffer = new StringBuilder();
      while (( c = reader.read() ) != -1) {
        aBuffer.append( (char) c );
      }
      reader.close();
      inputStream.close();

      return aBuffer.toString();
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  @Override
  public boolean fileExists( String vfsFilename ) throws KettleFileException {
    return fileExists( vfsFilename, null );
  }

  @Override
  public boolean fileExists( String vfsFilename, VariableSpace space ) throws KettleFileException {
    FileObject fileObject = null;
    try {
      fileObject = getFileObject( vfsFilename, space );
      return fileObject.exists();
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    } finally {
      if ( fileObject != null ) {
        try {
          fileObject.close();
        } catch ( Exception e ) { /* Ignore */
        }
      }
    }
  }


  @Override
  public InputStream getInputStream( String vfsFilename ) throws KettleFileException {
    return getInputStream( vfsFilename, defaultVariableSpace );
  }

  @Override
  public InputStream getInputStream( String vfsFilename, VariableSpace space ) throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );

      return KettleVFS.getInputStream( fileObject );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  @Override
  public OutputStream getOutputStream( FileObject fileObject, boolean append ) throws IOException {
    FileObject parent = fileObject.getParent();
    if ( parent != null ) {
      if ( !parent.exists() ) {
        throw new IOException( BaseMessages.getString(
          PKG, "KettleVFS.Exception.ParentDirectoryDoesNotExist", KettleVFS.getFriendlyURI( parent ) ) );
      }
    }
    try {
      fileObject.createFile();
      FileContent content = fileObject.getContent();
      return content.getOutputStream( append );
    } catch ( FileSystemException e ) {
      // Perhaps if it's a local file, we can retry using the standard
      // File object. This is because on Windows there is a bug in VFS.
      //
      if ( fileObject instanceof LocalFile ) {
        try {
          String filename = KettleVFS.getFilename( fileObject );
          return new FileOutputStream( new File( filename ), append );
        } catch ( Exception e2 ) {
          throw e; // throw the original exception: hide the retry.
        }
      } else {
        throw e;
      }
    }
  }

  @Override
  public OutputStream getOutputStream( String vfsFilename, boolean append ) throws KettleFileException {
    return getOutputStream( vfsFilename, defaultVariableSpace, append );
  }

  @Override
  public OutputStream getOutputStream( String vfsFilename, VariableSpace space, boolean append )
    throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  @Override
  public OutputStream getOutputStream( String vfsFilename, VariableSpace space,
                                       FileSystemOptions fsOptions, boolean append ) throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space, fsOptions );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  @Override
  public FileObject createTempFile( String prefix, KettleVFS.Suffix suffix ) throws KettleFileException {
    return createTempFile( prefix, suffix, KettleVFS.TEMP_DIR );
  }

  @Override
  public FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, VariableSpace variableSpace )
    throws KettleFileException {
    return createTempFile( prefix, suffix, KettleVFS.TEMP_DIR, variableSpace );
  }

  @Override
  public FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, String directory )
    throws KettleFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  @Override
  public FileObject createTempFile( String prefix, String suffix, String directory ) throws KettleFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  @Override
  public FileObject createTempFile( String prefix, KettleVFS.Suffix suffix, String directory, VariableSpace space )
    throws KettleFileException {
    return createTempFile( prefix, suffix.getExt(), directory, space );
  }

  @Override
  public FileObject createTempFile( String prefix, String suffix, String directory, VariableSpace space )
    throws KettleFileException {
    try {
      FileObject fileObject;
      do {
        // Build temporary file name using UUID to ensure uniqueness. Old mechanism would fail using Sort Rows (for
        // example)
        // when there multiple nodes with multiple JVMs on each node. In this case, the temp file names would end up
        // being
        // duplicated which would cause the sort to fail.
        String filename =
          new StringBuilder( 50 ).append( directory ).append( '/' ).append( prefix ).append( '_' ).append(
            UUIDUtil.getUUIDAsString() ).append( suffix ).toString();
        fileObject = getFileObject( filename, space );
      } while (fileObject.exists());
      return fileObject;
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  @Override
  public void reset() {
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
  }


}
