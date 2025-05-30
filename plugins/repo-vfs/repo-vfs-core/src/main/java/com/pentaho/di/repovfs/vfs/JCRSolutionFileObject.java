package com.pentaho.di.repovfs.vfs;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pentaho.di.repovfs.cfg.JCRSolutionConfig;
import com.pentaho.di.repovfs.repo.RepositoryClient;
import com.pentaho.di.repovfs.repo.RepositoryClient.RepositoryClientException;

import org.pentaho.di.core.Const;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileTreeDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAdapter;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class JCRSolutionFileObject extends AbstractFileObject<JCRSolutionFileSystem> {
  private static final Logger log = LoggerFactory.getLogger( JCRSolutionFileObject.class );

  private static final String FILE_NOT_FOUND = "The specified file name does not exist: {0}";

  private final RepositoryClient repoClient;

  public JCRSolutionFileObject( final AbstractFileName name,
                                 final JCRSolutionFileSystem fileSystem,
                                 JCRSolutionConfig config,
                                 final RepositoryClient repoClient ) {
    super( name, fileSystem );
    this.repoClient = repoClient;

    log.debug( "{}({})", getClass().getSimpleName(), name );
  }

  @Override
  protected boolean doIsReadable() throws Exception {
    return true;
  }

  @Override
  protected boolean doIsWriteable() throws Exception {
    if ( getName().getDepth() < 2 ) {
      return false;
    }
    // just attemp to write, the server will tell
    return true;
  }

  /**
   * Determines the type of this file.  Must not return null.  The return value of this method is cached, so the
   * implementation can be expensive.
   */
  @Override
  protected FileType doGetType() throws Exception {
    if ( getName().getDepth() < 2 ) {
      return FileType.FOLDER;
    }
    if ( !exists( getName() ) ) {
      return FileType.IMAGINARY;
    }
    if ( isDirectory() ) {
      return FileType.FOLDER;
    }
    return FileType.FILE;
  }

  /**
   * Lists the children of this file.  Is only called if {@link #doGetType}
   * returns {@link org.apache.commons.vfs2.FileType#FOLDER}.  The return value of this method
   * is cached, so the implementation can be expensive.
   */
  @Override
  protected String[] doListChildren() throws Exception {
    log.debug( "{}.doListChildren", getName() );

    final List<RepositoryFileTreeDto> children = getChildren( getName() );
    final String[] childrenArray = new String[ children.size() ];
    for ( int i = 0; i < children.size(); i++ ) {
      final RepositoryFileTreeDto repositoryFileTreeDto = children.get( i );
      if ( repositoryFileTreeDto == null ) {
        continue;
      }

      final RepositoryFileDto file = getFileDto( repositoryFileTreeDto );
      childrenArray[ i ] =
        file.getName().replaceAll( "\\%", "%25" ).replaceAll( "\\!", "%21" ).replaceAll( "\\+", "%2B" );
    }
    return childrenArray;
  }

  /**
   * Returns the size of the file content (in bytes).  Is only called if
   * {@link #doGetType} returns {@link org.apache.commons.vfs2.FileType#FILE}.
   */
  @Override
  protected long doGetContentSize() throws Exception {
    return getFileDto().getFileSize();
  }

  /**
   * Determines if this file is hidden.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs2.FileType#IMAGINARY}.
   * <p/>
   * This implementation always returns false.
   */
  @Override
  protected boolean doIsHidden() throws Exception {
    return getFileDto().isHidden();
  }

  /**
   * Returns the last modified time of this file.  Is only called if
   * {@link #doGetType} does not return {@link org.apache.commons.vfs2.FileType#IMAGINARY}.
   * <p/>
   * This implementation throws an exception.
   */
  @Override
  protected long doGetLastModifiedTime() throws Exception {
    RepositoryFileDto fileDto = getFileDto();

    final String lastModifiedDateRaw = fileDto.getLastModifiedDate();
    if ( lastModifiedDateRaw.isEmpty() ) {
      // Folders have an empty lastModifiedDate field
      // Returning -1 here means that lastModifiedDate wasn't found
      return -1;
    }

    final Date lastModifiedDate = RepositoryFileAdapter.unmarshalDate( lastModifiedDateRaw );
    if ( lastModifiedDate == null ) {
      log.error( "Repository returned <null> for last-modified-date on file: " + getName() );
      return -1;
    }
    return lastModifiedDate.getTime();
  }

  /** returns `RepositoryFileDto` for this file or throws */
  private RepositoryFileDto getFileDto() throws FileSystemException {
    String[] fileName = computeFileNames( getName() );
    RepositoryFileTreeDto fileInfo = lookupNodeOrThrow( fileName );
    return getFileDto( fileInfo );
  }

  /**
   * Sets an attribute of this file.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs2.FileType#IMAGINARY}.
   * <p/>
   * This implementation throws an exception.
   */
  @Override
  protected void doSetAttribute( final String atttrName, final Object value ) throws Exception {
    if ( "description".equals( atttrName ) ) {
      if ( value instanceof String ) {
        setDescription( getName(), String.valueOf( value ) );
      } else {
        setDescription( getName(), null );
      }
    }
  }

  private void setDescription( final FileName file, final String description ) throws FileSystemException {
    final String[] fileName = computeFileNames( file );
    final RepositoryFileTreeDto fileInfo = lookupNodeOrThrow( fileName );
    final RepositoryFileDto fileDto = getFileDto( fileInfo );
    fileDto.setDescription( description );
  }

  /**
   * Creates an input stream to read the file content from.  Is only called
   * if {@link #doGetType} returns {@link org.apache.commons.vfs2.FileType#FILE}.
   * <p/>
   * <p>It is guaranteed that there are no open output streams for this file when this method is called.
   * <p/>
   * <p>The returned stream does not have to be buffered.
   */
  @Override
  protected InputStream doGetInputStream() throws Exception {
    return repoClient.getData( getFileDto() );
  }

  @Override
  protected InputStream doGetInputStream( int bufferSize ) throws Exception {
    return repoClient.getData( getFileDto(), bufferSize );
  }

  @Override
  protected OutputStream doGetOutputStream( final boolean append ) throws Exception {
    OutputStream out = new JCRFileOutputStream( computeFileNames( getName() ), repoClient );
    if ( append ) {
      try ( InputStream in = repoClient.getData( getFileDto() ) ) {
        in.transferTo( out );
      }
    }
    return out;
  }

  /**
   * Creates this file as a folder.  Is only called when:
   * <ul>
   * <li>{@link #doGetType} returns {@link org.apache.commons.vfs2.FileType#IMAGINARY}.
   * <li>The parent folder exists and is writeable, or this file is the
   * root of the file system.
   * </ul>
   * <p/>
   */
  @Override
  protected void doCreateFolder() throws Exception {
    repoClient.createFolder( getName().getPath() );
  }

  @Override
  public void refresh() throws FileSystemException {
    log.debug( "refreshing {}", getName() );
    super.refresh();
    clearCache();
  }

  private void clearCache() {
    repoClient.clearCache( computeFileNames( getName() ) );
  }

  private void clearParentCache() {
    FileName parent = getName().getParent();
    if ( parent != null ) {
      repoClient.clearCache( computeFileNames( parent ) );
    } else {
      repoClient.refreshRoot();
    }
  }

  @Override
  public void moveTo( FileObject destFile ) throws FileSystemException {
    // TODO: this requires copying content, should use endpoints to do it on the server
    // overriding because default is to either call rename, or copy only current file, which is not good for folders
    destFile.copyFrom( this, Selectors.SELECT_ALL );
    delete();
  }

  @Override
  public int delete( final FileSelector selector ) throws FileSystemException {
    if ( selector == null || selector == Selectors.SELECT_SELF || selector == Selectors.SELECT_ALL ) {
      delete();
      return 1;
    } else {
      return super.delete( selector );
    }
  }

  @Override
  public boolean delete() throws FileSystemException {
    doDelete();
    try {
      handleDelete();
    } catch ( Exception e ) {
      throw new FileSystemException("vfs.provider/delete.error", e, getName() );
    }
    return true;
  }

  @Override
  protected void doDelete() throws FileSystemException {
    log.debug( "deleting {}", getName() );
    final RepositoryFileDto file = getFile( getName() );
    try {
      repoClient.delete( file );
    } catch ( RepositoryClientException e ) {
      throw new FileSystemException( e );
    }
  }

  @Override
  protected void onChildrenChanged( FileName child, FileType newType ) throws Exception {
    super.onChildrenChanged( child, newType );
    clearCache();
  }

  private RepositoryFileDto getFile( FileName name ) throws FileSystemException {

    if ( name == null ) {
      throw new FileSystemException( FILE_NOT_FOUND );
    }

    final String[] pathArray = computeFileNames( name );
    final RepositoryFileTreeDto fileInfo = lookupNodeOrThrow( pathArray );

    return getFileDto( fileInfo );
  }

  private RepositoryFileTreeDto lookupNodeOrThrow( String[] path ) throws FileSystemException {
    try {
      return repoClient.lookupNode( path ).orElseThrow( () -> new FileSystemException( FILE_NOT_FOUND, getName() ) );
    } catch ( RepositoryClientException e ) {
      throw new FileSystemException( e );
    }
  }

  private static String[] computeFileNames( FileName file ) {
    ArrayDeque<String> stack = new ArrayDeque<>();
    while ( file != null ) {
      String name;
      try {
        name = URLDecoder.decode( file.getBaseName().trim().replaceAll( "\\+", "%2B" ), StandardCharsets.UTF_8.name() );
      } catch ( UnsupportedEncodingException e ) {
        name = file.getBaseName().trim();
      }
      if ( StringUtils.isNotEmpty(name) ) {
        stack.push( name );
      }
      file = file.getParent();
    }

    final int size = stack.size();
    final String[] result = new String[ size ];
    for ( int i = 0; i < result.length; i++ ) {
      result[ i ] = stack.pop();
    }
    return result;
  }

  private boolean isDirectory() throws FileSystemException {
    return getFileDto().isFolder();
  }

  public boolean exists( final FileName file ) throws FileSystemException {
    final String[] fileName = computeFileNames( file );
    try {
      return repoClient.lookupNode( fileName ).isPresent();
    } catch ( RepositoryClientException e ) {
      throw new FileSystemException( e );
    }
  }

  @Override
  public void createFile() throws FileSystemException {
    log.debug( "new file: {}", getName() );
    if ( isKettleFile( getName().getBaseName() ) ) {
      // avoid error when trying to create empty trans/job
    } else {
      super.createFile();
    }
    clearParentCache();
  }

  private boolean isKettleFile( String fileName ) {
    switch ( FilenameUtils.getExtension( fileName ).toLowerCase() ) {
      case Const.STRING_TRANS_DEFAULT_EXT:
      case Const.STRING_JOB_DEFAULT_EXT:
        return true;
      default:
        return false;
    }
  }

  private RepositoryFileDto getFileDto( final RepositoryFileTreeDto child ) throws FileSystemException {
    final RepositoryFileDto file = child.getFile();
    if ( file == null ) {
      throw new FileSystemException(
        "BI-Server returned a RepositoryFileTreeDto without an attached RepositoryFileDto!" );
    }
    return file;
  }

  private List<RepositoryFileTreeDto> getChildren( final FileName parent ) throws FileSystemException {
    final String[] pathArray = computeFileNames( parent );
    final RepositoryFileTreeDto fileInfo = lookupNodeOrThrow( pathArray );

    final List<RepositoryFileTreeDto> childNodes = fileInfo.getChildren();
    return childNodes == null ? Collections.<RepositoryFileTreeDto>emptyList() : childNodes;
  }

}
