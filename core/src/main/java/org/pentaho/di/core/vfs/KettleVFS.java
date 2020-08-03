/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.local.LocalFile;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSHelper;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.util.UUIDUtil;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.configuration.IKettleFileSystemConfigBuilder;
import org.pentaho.di.core.vfs.configuration.KettleFileSystemConfigBuilderFactory;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KettleVFS {
  public static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
  public static final String CONNECTION = "connection";
  private static final KettleVFS kettleVFS = new KettleVFS();
  private static final int TIMEOUT_LIMIT = 9000;
  private static final int TIME_TO_SLEEP_STEP = 50;
  private static final String PROVIDER_PATTERN_SCHEME = "^[\\w\\d]+://(.*)";
  private static final String DEFAULT_S3_CONFIG_PROPERTY = "defaultS3Config";
  private static Class<?> PKG = KettleVFS.class; // for i18n purposes, needed by Translator2!!
  private static Supplier<ConnectionManager> connectionManager = ConnectionManager::getInstance;
  private static VariableSpace defaultVariableSpace;

  static {
    // Create a new empty variable space...
    //
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
  }

  private final DefaultFileSystemManager fsm;

  private KettleVFS() {
    fsm = new ConcurrentFileSystemManager();
    // Forcibly overrides VFS's default StandardFileSystemManager with our Concurrent File System Manager, which will
    // also allow us to point at our own providers.xml file, instead of the default file that comes with the
    // commons-vfs2 library.
    VFS.setManager( fsm );
    try {
      fsm.setFilesCache( new WeakRefFilesCache() );
      fsm.init();
    } catch ( FileSystemException e ) {
      e.printStackTrace();
    }

    // Install a shutdown hook to make sure that the file system manager is closed
    // This will clean up temporary files in vfs_cache
    Runtime.getRuntime().addShutdownHook( new Thread( new Runnable() {
      @Override
      public void run() {
        if ( fsm != null ) {
          try {
            fsm.close();
          } catch ( Exception ignored ) {
            // Exceptions can be thrown due to a closed classloader
          }
        }
      }
    } ) );
  }

  public static KettleVFS getInstance() {
    return kettleVFS;
  }

  public static FileObject getFileObject( String vfsFilename ) throws KettleFileException {
    return getFileObject( vfsFilename, defaultVariableSpace );
  }

  public static FileObject getFileObject( String vfsFilename, VariableSpace space ) throws KettleFileException {
    return getFileObject( vfsFilename, space, null );
  }

  public static FileObject getFileObject( String vfsFilename, FileSystemOptions fsOptions ) throws KettleFileException {
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
  public static FileObject getFileObject( String vfsFilename, VariableSpace space, FileSystemOptions fsOptions )
    throws KettleFileException {

    //  Protect the code below from invalid input.
    if ( vfsFilename == null ) {
      throw new IllegalArgumentException( "Unexpected null VFS filename." );
    }

    try {
      FileSystemManager fsManager = getInstance().getFileSystemManager();
      String[] schemes = fsManager.getSchemes();

      String scheme = getScheme( schemes, vfsFilename );

      //Waiting condition - PPP-4374:
      //We have to check for hasScheme even if scheme is null because that scheme could not
      //be available by getScheme at the time we validate our scheme flag ( Kitchen loading problem )
      //So we check if - even it has not a scheme - our vfsFilename has a possible scheme format
      // (PROVIDER_PATTERN_SCHEME)
      //If it does, then give it some time and tries to load. It stops when timeout is up or a scheme is found.
      int timeOut = TIMEOUT_LIMIT;
      if ( hasSchemePattern( vfsFilename, PROVIDER_PATTERN_SCHEME ) ) {
        while ( scheme == null && timeOut > 0 ) {
          // ask again to refresh schemes list
          schemes = fsManager.getSchemes();
          try {
            Thread.sleep( TIME_TO_SLEEP_STEP );
            timeOut -= TIME_TO_SLEEP_STEP;
            scheme = getScheme( schemes, vfsFilename );
          } catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }

      fsOptions = getFileSystemOptions( scheme, vfsFilename, space, fsOptions );

      String filename = normalizePath( vfsFilename, scheme );

      return fsOptions != null ? fsManager.resolveFile( filename, fsOptions ) : fsManager.resolveFile( filename );

    } catch ( IOException e ) {
      throw new KettleFileException( "Unable to get VFS File object for filename '"
        + cleanseFilename( vfsFilename ) + "' : " + e.getMessage(), e );
    }
  }

  protected static String normalizePath( String path, String scheme ) {
    String normalizedPath = path;

    if ( path.startsWith( "\\\\" ) ) {
      File file = new File( path );
      normalizedPath = file.toURI().toString();
    } else if ( scheme == null ) {
      File file = new File( path );
      normalizedPath = file.getAbsolutePath();
    }

    return normalizedPath;
  }

  protected static boolean hasSchemePattern( String path, String patternString ) {
    boolean hasScheme = false;
    Pattern pattern = Pattern.compile( patternString );

    if ( pattern != null ) {
      Matcher matcher = pattern.matcher( path );
      hasScheme = matcher.matches();
    }

    return hasScheme;
  }

  static String getScheme( String[] schemes, String fileName ) {
    for ( String scheme : schemes ) {
      if ( fileName.startsWith( scheme + ":" ) ) {
        return scheme;
      }
    }
    return null;
  }

  static FileSystemOptions getFileSystemOptions( String scheme, String vfsFilename, VariableSpace space,
                                                 FileSystemOptions fileSystemOptions ) throws IOException {
    if ( scheme == null ) {
      return fileSystemOptions;
    }

    return buildFsOptions( space, fileSystemOptions, vfsFilename, scheme );
  }

  /**
   * Private method for stripping password from filename when a FileObject can not be obtained.
   * getFriendlyURI(FileObject) or getFriendlyURI(String) are the public methods.
   */
  private static String cleanseFilename( String vfsFilename ) {
    return vfsFilename.replaceAll( ":[^:@/]+@", ":<password>@" );
  }

  @SuppressWarnings( "squid:S108" )
  private static FileSystemOptions buildFsOptions( VariableSpace varSpace, FileSystemOptions sourceOptions,
                                                   String vfsFilename, String scheme ) throws IOException {
    if ( varSpace == null || vfsFilename == null ) {
      // We cannot extract settings from a non-existant variable space
      return null;
    }

    IKettleFileSystemConfigBuilder configBuilder =
      KettleFileSystemConfigBuilderFactory.getConfigBuilder( varSpace, scheme );

    FileSystemOptions fsOptions = ( sourceOptions == null ) ? new FileSystemOptions() : sourceOptions;

    String[] varList = varSpace.listVariables();

    for ( String var : varList ) {
      if ( scheme.equals( "s3" ) || scheme.equals( "s3n" ) || scheme.equals( "s3a" ) ) {
        try {
          Optional<? extends ConnectionDetails> defaultS3Connection =
            connectionManager.get().getConnectionDetailsByScheme( "s3" ).stream().filter(
              connectionDetails -> connectionDetails.getProperties().get( DEFAULT_S3_CONFIG_PROPERTY ) != null
                && connectionDetails.getProperties().get( DEFAULT_S3_CONFIG_PROPERTY ).equalsIgnoreCase( "true" ) )
              .findFirst();

          if ( defaultS3Connection.isPresent() ) {
            String vfsHelperUrl = vfsFilename.replaceFirst( scheme, "s3" );
            fsOptions = VFSHelper.getOpts( vfsHelperUrl, defaultS3Connection.get().getName() );
          }
        } catch ( Exception ignored ) {
          // Ignore the exception, it's OK if we can't find a default S3 connection.
        }
      }

      if ( var.equalsIgnoreCase( CONNECTION ) && varSpace.getVariable( var ) != null ) {
        FileSystemOptions fileSystemOptions = VFSHelper.getOpts( vfsFilename, varSpace.getVariable( var ) );
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
    return fsOptions;
  }

  /**
   * Read a text file (like an XML document). WARNING DO NOT USE FOR DATA FILES.
   *
   * @param vfsFilename the filename or URL to read from
   * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
   * @return The content of the file as a String
   * @throws IOException
   */
  public static String getTextFileContent( String vfsFilename, String charSetName ) throws KettleFileException {
    return getTextFileContent( vfsFilename, null, charSetName );
  }

  public static String getTextFileContent( String vfsFilename, VariableSpace space, String charSetName )
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
      while ( ( c = reader.read() ) != -1 ) {
        aBuffer.append( (char) c );
      }
      reader.close();
      inputStream.close();

      return aBuffer.toString();
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  public static boolean fileExists( String vfsFilename ) throws KettleFileException {
    return fileExists( vfsFilename, null );
  }

  public static boolean fileExists( String vfsFilename, VariableSpace space ) throws KettleFileException {
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

  public static InputStream getInputStream( FileObject fileObject ) throws FileSystemException {
    FileContent content = fileObject.getContent();
    return content.getInputStream();
  }

  public static InputStream getInputStream( String vfsFilename ) throws KettleFileException {
    return getInputStream( vfsFilename, defaultVariableSpace );
  }

  public static InputStream getInputStream( String vfsFilename, VariableSpace space ) throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );

      return getInputStream( fileObject );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  public static OutputStream getOutputStream( FileObject fileObject, boolean append ) throws IOException {
    FileObject parent = fileObject.getParent();
    if ( parent != null ) {
      if ( !parent.exists() ) {
        throw new IOException( BaseMessages.getString(
          PKG, "KettleVFS.Exception.ParentDirectoryDoesNotExist", getFriendlyURI( parent ) ) );
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
          String filename = getFilename( fileObject );
          return new FileOutputStream( new File( filename ), append );
        } catch ( Exception e2 ) {
          throw e; // throw the original exception: hide the retry.
        }
      } else {
        throw e;
      }
    }
  }

  public static OutputStream getOutputStream( String vfsFilename, boolean append ) throws KettleFileException {
    return getOutputStream( vfsFilename, defaultVariableSpace, append );
  }

  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space, boolean append )
    throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space,
                                              FileSystemOptions fsOptions, boolean append ) throws KettleFileException {
    try {
      FileObject fileObject = getFileObject( vfsFilename, space, fsOptions );
      return getOutputStream( fileObject, append );
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  public static String getFilename( FileObject fileObject ) {
    FileName fileName = fileObject.getName();
    String root = fileName.getRootURI();
    if ( !root.startsWith( "file:" ) ) {
      return fileName.getURI(); // nothing we can do about non-normal files.
    }
    if ( root.startsWith( "file:////" ) ) {
      return fileName.getURI(); // we'll see 4 forward slashes for a windows/smb network share
    }
    if ( root.endsWith( ":/" ) ) { // Windows
      root = root.substring( 8, 10 );
    } else { // *nix & OSX
      root = "";
    }
    String fileString = root + fileName.getPath();
    if ( !"/".equals( Const.FILE_SEPARATOR ) ) {
      fileString = Const.replace( fileString, "/", Const.FILE_SEPARATOR );
    }
    return fileString;
  }

  public static String getFriendlyURI( String filename ) {
    if ( filename == null ) {
      return null;
    }
    String friendlyName;
    try {
      friendlyName = getFriendlyURI( KettleVFS.getFileObject( filename ) );
    } catch ( Exception e ) {
      // unable to get a friendly name from VFS object.
      // Cleanse name of pwd before returning
      friendlyName = cleanseFilename( filename );
    }
    return friendlyName;
  }

  public static String getFriendlyURI( FileObject fileObject ) {
    return fileObject.getName().getFriendlyURI();
  }

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix - file name
   * @param prefix - file extension
   * @return FileObject
   * @throws KettleFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix ) throws KettleFileException {
    return createTempFile( prefix, suffix, TEMP_DIR );
  }

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix        - file name
   * @param suffix        - file extension
   * @param variableSpace is used to get system variables
   * @return FileObject
   * @throws KettleFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, VariableSpace variableSpace )
    throws KettleFileException {
    return createTempFile( prefix, suffix, TEMP_DIR, variableSpace );
  }

  /**
   * @param prefix    - file name
   * @param suffix    - file extension
   * @param directory - directory where file will be created
   * @return FileObject
   * @throws KettleFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory ) throws KettleFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  public static FileObject createTempFile( String prefix, String suffix, String directory ) throws KettleFileException {
    return createTempFile( prefix, suffix, directory, null );
  }

  /**
   * @param prefix    - file name
   * @param directory path to directory where file will be created
   * @param space     is used to get system variables
   * @return FileObject
   * @throws KettleFileException
   */
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory, VariableSpace space )
    throws KettleFileException {
    return createTempFile( prefix, suffix.ext, directory, space );
  }

  public static FileObject createTempFile( String prefix, String suffix, String directory, VariableSpace space )
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
      } while ( fileObject.exists() );
      return fileObject;
    } catch ( IOException e ) {
      throw new KettleFileException( e );
    }
  }

  public static Comparator<FileObject> getComparator() {
    return new Comparator<FileObject>() {
      @Override
      public int compare( FileObject o1, FileObject o2 ) {
        String filename1 = getFilename( o1 );
        String filename2 = getFilename( o2 );
        return filename1.compareTo( filename2 );
      }
    };
  }

  /**
   * Get a FileInputStream for a local file. Local files can be read with NIO.
   *
   * @param fileObject
   * @return a FileInputStream
   * @throws IOException
   * @deprecated because of API change in Apache VFS. As a workaround use FileObject.getName().getPathDecoded(); Then
   * use a regular File() object to create a File Input stream.
   */
  @Deprecated
  public static FileInputStream getFileInputStream( FileObject fileObject ) throws IOException {

    if ( !( fileObject instanceof LocalFile ) ) {
      // We can only use NIO on local files at the moment, so that's what we limit ourselves to.
      //
      throw new IOException( BaseMessages.getString( PKG, "FixedInput.Log.OnlyLocalFilesAreSupported" ) );
    }

    return new FileInputStream( fileObject.getName().getPathDecoded() );
  }

  /**
   * Check if filename starts with one of the known protocols like file: zip: ram: smb: jar: etc. If yes, return true
   * otherwise return false
   *
   * @param vfsFileName
   * @return boolean
   */
  public static boolean startsWithScheme( String vfsFileName ) {
    FileSystemManager fsManager = getInstance().getFileSystemManager();

    boolean found = false;
    String[] schemes = fsManager.getSchemes();
    for ( int i = 0; i < schemes.length; i++ ) {
      if ( vfsFileName.startsWith( schemes[ i ] + ":" ) ) {
        found = true;
        break;
      }
    }

    return found;
  }

  public static void closeEmbeddedFileSystem( String embeddedMetastoreKey ) {
    if ( getInstance().getFileSystemManager() instanceof ConcurrentFileSystemManager ) {
      ( (ConcurrentFileSystemManager) getInstance().getFileSystemManager() )
        .closeEmbeddedFileSystem( embeddedMetastoreKey );
    }
  }

  /**
   * @see StandardFileSystemManager#freeUnusedResources()
   */
  public static void freeUnusedResources() {
    ( (StandardFileSystemManager) getInstance().getFileSystemManager() ).freeUnusedResources();
  }

  public FileSystemManager getFileSystemManager() {
    return fsm;
  }

  public void reset() {
    defaultVariableSpace = new Variables();
    defaultVariableSpace.initializeVariablesFrom( null );
    fsm.close();
    try {
      fsm.setFilesCache( new WeakRefFilesCache() );
      fsm.init();
    } catch ( FileSystemException ignored ) {
    }

  }

  public enum Suffix {
    ZIP( ".zip" ), TMP( ".tmp" ), JAR( ".jar" );

    private String ext;

    Suffix( String ext ) {
      this.ext = ext;
    }
  }

}
