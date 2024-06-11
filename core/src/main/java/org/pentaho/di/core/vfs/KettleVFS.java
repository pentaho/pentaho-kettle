/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class now serves two purposes: legacy (deprecated) backwards-compatible access to VFS methods for components
 * that don't yet support Bowls, and singleton/static data and methods that need to still be shared. Most code trying to
 * read and write files over VFS should use getInstance( Bowl ) and IKettleVFS.
 *
 */
public class KettleVFS {
  public static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );

  private static Class<?> PKG = KettleVFS.class; // for i18n purposes, needed by Translator2!!

  // for global state
  private static final KettleVFS kettleVFS = new KettleVFS();
  // for passing along previously-static methods to the new implementation
  private static final IKettleVFS ikettleVFS = new KettleVFSImpl( DefaultBowl.getInstance() );

  private final DefaultFileSystemManager fsm;
  static final String PROVIDER_PATTERN_SCHEME = "^[\\w\\d]+://(.*)";

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

  public FileSystemManager getFileSystemManager() {
    return fsm;
  }

  /**
   * Use only when the caller is positive that a Bowl is not in use.
   */
  public static KettleVFS getInstance() {
    return kettleVFS;
  }

  /**
   * Gets a handle on an IKettleVFS for a particular Bowl. It is important that all VFS Filesystems for a given Bowl are
   * shared correctly between instances. VFS Filesystem instances are compared by the FileSystemOptions, and the Bowl is
   * set as a parameter in those options. It is important that all Bowl implementations correctly implement equals() and
   * hashcode() to make this work.
   *
   * It is also critical that there is only one ConnectionManager for a given Bowl. Anything that needs a
   * ConnectionManager (especially including VFS code) should only use Bowl.getConnectionManager() to ensure there is a
   * single instance per bowl.
   *
   * @param bowl the bowl for the current context
   * @return IKettleVFS The API for file operations.
   */
  public static IKettleVFS getInstance( Bowl bowl ) {
    return new KettleVFSImpl( bowl );
  }

  /** only use this method as a last resort if you don't yet have a variables object.  Since VFS connections can be used
   * with variables for the dialog fields, it is important to pass the Variables to getFileObject.  Failure to do this
   * means any settings for the job/ktr will not be available for variables substitution.
   * Use getFileObject( String vfsFilename, VariableSpace space ), instead and pass the steps/entries variable space.
   * @param vfsFilename
   * @return
   * @throws KettleFileException
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject getFileObject( String vfsFilename ) throws KettleFileException {
    return ikettleVFS.getFileObject( vfsFilename );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject getFileObject( String vfsFilename, VariableSpace space ) throws KettleFileException {
    return ikettleVFS.getFileObject( vfsFilename, space );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject getFileObject( String vfsFilename, FileSystemOptions fsOptions ) throws KettleFileException {
    return ikettleVFS.getFileObject( vfsFilename, fsOptions );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject getFileObject( String vfsFilename, VariableSpace space, FileSystemOptions fsOptions )
    throws KettleFileException {
    return ikettleVFS.getFileObject( vfsFilename, space, fsOptions );
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

  public static boolean hasSchemePattern( String path ) {
    return hasSchemePattern( path, PROVIDER_PATTERN_SCHEME );
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
    for (String scheme : schemes) {
      if ( fileName.startsWith( scheme + ":" ) ) {
        return scheme;
      }
    }
    return null;
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  static FileSystemOptions getFileSystemOptions( String scheme, String vfsFilename, VariableSpace space,
                                                 FileSystemOptions fileSystemOptions )
    throws IOException {
    return ikettleVFS.getFileSystemOptions( scheme, vfsFilename, space, fileSystemOptions );
  }

  /**
   * Private method for stripping password from filename when a FileObject
   * can not be obtained.
   * getFriendlyURI(FileObject) or getFriendlyURI(String) are the public
   * methods.
   */
  public static String cleanseFilename( String vfsFilename ) {
    return vfsFilename.replaceAll( ":[^:@/]+@", ":<password>@" );
  }

  /**
   * Read a text file (like an XML document). WARNING DO NOT USE FOR DATA FILES.
   *
   * @param vfsFilename the filename or URL to read from
   * @param charSetName the character set of the string (UTF-8, ISO8859-1, etc)
   * @return The content of the file as a String
   * @deprecated use getInstance( Bowl )
   * @throws IOException
   */
  @Deprecated
  public static String getTextFileContent( String vfsFilename, String charSetName ) throws KettleFileException {
    return ikettleVFS.getTextFileContent( vfsFilename, charSetName );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static String getTextFileContent( String vfsFilename, VariableSpace space, String charSetName )
    throws KettleFileException {
    return ikettleVFS.getTextFileContent( vfsFilename, space, charSetName );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static boolean fileExists( String vfsFilename ) throws KettleFileException {
    return ikettleVFS.fileExists( vfsFilename );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static boolean fileExists( String vfsFilename, VariableSpace space ) throws KettleFileException {
    return ikettleVFS.fileExists( vfsFilename, space );
  }

  public static InputStream getInputStream( FileObject fileObject ) throws FileSystemException {
    FileContent content = fileObject.getContent();
    return content.getInputStream();
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static InputStream getInputStream( String vfsFilename ) throws KettleFileException {
    return ikettleVFS.getInputStream( vfsFilename );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static InputStream getInputStream( String vfsFilename, VariableSpace space ) throws KettleFileException {
    return ikettleVFS.getInputStream( vfsFilename, space );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static OutputStream getOutputStream( FileObject fileObject, boolean append ) throws IOException {
    return ikettleVFS.getOutputStream( fileObject, append );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static OutputStream getOutputStream( String vfsFilename, boolean append ) throws KettleFileException {
    return ikettleVFS.getOutputStream( vfsFilename, append );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space, boolean append )
    throws KettleFileException {
    return ikettleVFS.getOutputStream( vfsFilename, space, append );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static OutputStream getOutputStream( String vfsFilename, VariableSpace space,
                                              FileSystemOptions fsOptions, boolean append ) throws KettleFileException {
    return ikettleVFS.getOutputStream( vfsFilename, space, fsOptions, append );
  }

  public static String getFilename( FileObject fileObject ) {
    FileName fileName = fileObject.getName();
    String root = fileName.getRootURI();
    if ( !root.startsWith( "file:" ) ) {
      return fileName.getURI(); // nothing we can do about non-normal files.
    }
    // PDI-19865 - we'll see 4 forward slashes for a windows/smb network share,
    // so we need want to keep only the relevant part of the URI
    if ( root.startsWith( "file:////" ) ) {
      return fileName.getURI().substring( 7 );
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

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static String getFriendlyURI( String filename ) {
    return ikettleVFS.getFriendlyURI( filename );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static String getFriendlyURI( String filename, VariableSpace space ) {
    return ikettleVFS.getFriendlyURI( filename, space );
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
   * @deprecated use getInstance( Bowl )
   * @throws KettleFileException
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, Suffix suffix ) throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix );
  }

  /**
   * Creates a file using "java.io.tmpdir" directory
   *
   * @param prefix        - file name
   * @param suffix        - file extension
   * @param variableSpace is used to get system variables
   * @deprecated use getInstance( Bowl )
   * @return FileObject
   * @throws KettleFileException
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, Suffix suffix, VariableSpace variableSpace )
    throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix, variableSpace );
  }

  /**
   * @param prefix    - file name
   * @param suffix    - file extension
   * @param directory - directory where file will be created
   * @deprecated use getInstance( Bowl )
   * @return FileObject
   * @throws KettleFileException
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory ) throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix, directory );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, String suffix, String directory ) throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix, directory );
  }

  /**
   * @param prefix    - file name
   * @param directory path to directory where file will be created
   * @param space     is used to get system variables
   * @deprecated use getInstance( Bowl )
   * @return FileObject
   * @throws KettleFileException
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, Suffix suffix, String directory, VariableSpace space )
    throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix, directory, space );
  }

  /**
   * @deprecated use getInstance( Bowl )
   */
  @Deprecated
  public static FileObject createTempFile( String prefix, String suffix, String directory, VariableSpace space )
    throws KettleFileException {
    return ikettleVFS.createTempFile( prefix, suffix, directory, space );
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
   * Check if filename starts with one of the known protocols like file: zip: ram: smb: jar: etc.
   * If yes, return true otherwise return false
   *
   * @param vfsFileName
   * @return boolean
   */
  public static boolean startsWithScheme( String vfsFileName ) {
    FileSystemManager fsManager = getInstance().getFileSystemManager();

    boolean found = false;
    String[] schemes = fsManager.getSchemes();
    for (int i = 0; i < schemes.length; i++) {
      if ( vfsFileName.startsWith( schemes[i] + ":" ) ) {
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
   * resets the FileSystemManager
   *
   */
  public void reset() {
    ikettleVFS.reset();
    fsm.close();
    try {
      fsm.setFilesCache( new WeakRefFilesCache() );
      fsm.init();
    } catch ( FileSystemException ignored ) {
    }
  }

  /**
   * @see StandardFileSystemManager#freeUnusedResources()
   */
  public static void freeUnusedResources() {
    ( (StandardFileSystemManager) getInstance().getFileSystemManager() ).freeUnusedResources();
  }

  public enum Suffix {
    ZIP( ".zip" ), TMP( ".tmp" ), JAR( ".jar" );

    private String ext;

    Suffix( String ext ) {
      this.ext = ext;
    }
    public String getExt() {
      return ext;
    }
  }

  private void serializeVariableSpace( VariableSpace space ) {
    space.listVariables();
  }
}
