package org.pentaho.di.repovfs.vfs;

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

import org.pentaho.di.repovfs.repo.RepositoryClient;
import org.pentaho.di.repovfs.repo.RepositoryClient.RepositoryClientException;

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
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.base.Objects;

public class JCRSolutionFileObject extends AbstractFileObject<JCRSolutionFileSystem> {
  private static final Logger log = LoggerFactory.getLogger( JCRSolutionFileObject.class );

  private static final String FILE_NOT_FOUND = "The specified file name does not exist: {0}";

  private final RepositoryClient repoClient;

  private final RemoteFile file;

  public JCRSolutionFileObject( final AbstractFileName name,
                                 final JCRSolutionFileSystem fileSystem,
                                 final RepositoryClient repoClient ) {
    super( name, fileSystem );
    this.repoClient = repoClient;
    this.file = new RemoteFile( new RepoFetcher() );
    log.debug( "{}({})", getClass().getSimpleName(), name );
  }

  @Override
  protected void doAttach() throws Exception {
    log.debug( "attach {}", getName() );
    file.attach();
  }

  @Override
  protected void doDetach() throws Exception {
    log.debug( "dettach {}", getName() );
    file.detach();
  }

  private class RepoFetcher implements RemoteFile.Fetcher {

    private final String[] path = computeFileNames( getName() );

    @Override
    public Optional<RepositoryFileDto> fetchFileInfo() {
      try {
        return repoClient.getFileInfo( path );
      } catch ( RepositoryClientException e ) {
        log.error( "Unable to fetch remote file", e );
        return Optional.empty();
      }
    }

    @Override
    public Optional<RepositoryFileTreeDto> fetchFileTree() {
      return repoClient.fetchChildTree( path );
    }

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
    // will be attached at this point
    if ( getName().getDepth() < 2 ) {
      return FileType.FOLDER;
    }
    return file.getFile().map( fileDto -> fileDto.isFolder() ? FileType.FOLDER : FileType.FILE )
      .orElse( FileType.IMAGINARY );
  }

  @Override
  public FileObject[] getChildren() throws FileSystemException {
    FileObject[] children = super.getChildren();

    if ( file.getChildrenCache().isPresent() ) {
      // we already fetched the children, let's reuse the dtos
      var cachedChildren = file.getChildrenCache().get();
      if ( cachedChildren.length != children.length ) {
        // supposed to be in sync, this should never happen
        log.error( "Children cache mismatch" );
        return children;
      }
      for ( int i = 0; i < children.length; i++ ) {
        if ( children[i] instanceof JCRSolutionFileObject child ) {
          child.preAttach( cachedChildren[i] );
        }
      }
    }
    return children;
  }

  /**
   * Lists the children of this file.  Is only called if {@link #doGetType}
   * returns {@link org.apache.commons.vfs2.FileType#FOLDER}. The return value of this method
   * is cached, so the implementation can be expensive.
   */
  @Override
  protected String[] doListChildren() throws Exception {
    log.debug( "{}.doListChildren", getName() );

    return getChildrenNames();
  }

  private String[] getChildrenNames() {
    RepositoryFileDto[] children = file.getChildrenCache().orElseGet( this::fetchChildren );
    if ( file.getChildrenCache().isEmpty() ) {
      file.setChildrenCache( children );
    }
    return Stream.of( children ).map( this::getChildName ).toArray( String[]::new );
  }

  public void preAttach( RepositoryFileDto fileDto ) {
    file.attachToFile( Optional.of( fileDto ) );
    ensureAttached();
  }

  private void ensureAttached() {
    try {
      // since attach() is private, this is one way to call it
      getType();
    } catch ( FileSystemException e ) {
      log.error( "Unable to read type", e );
    }
  }

  private String getChildName( RepositoryFileDto child ) {
    return child.getName().replace( "%", "%25" ).replace( "!", "%21" ).replace( "+", "%2B" );
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
    return file.getFile().orElseThrow( () -> new FileSystemException( FILE_NOT_FOUND, getName() ) );
  }

  /**
   * Sets an attribute of this file.  Is only called if {@link #doGetType}
   * does not return {@link org.apache.commons.vfs2.FileType#IMAGINARY}.
   * <p/>
   * This implementation throws an exception.
   */
  @Override
  protected void doSetAttribute( final String atttrName, final Object value ) throws FileSystemException {
    if ( "description".equals( atttrName ) ) {
      if ( value instanceof String ) {
        setDescription( String.valueOf( value ) );
      } else {
        setDescription( null );
      }
    }
  }

  private void setDescription( String description ) throws FileSystemException {
    getFileDto().setDescription( description );
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
  }

  @Override
  public void moveTo( FileObject destFile ) throws FileSystemException {
    try {
      ensureAttached();
      if ( haveSameName( this, destFile ) ) {
        callRepoMove( destFile.getParent() );
      } else if ( haveSameParent( this, destFile ) && haveSameExtension( this, destFile ) ) {
        callRepoRename( getNameNoExt( destFile ) );
      } else {
        // can be optimized to use move+rename as well, but shouldn't be reached in normal use
        log.warn( "Unable to optimize moveTo, falling back to copy+delete" );
        // manual copy+delete
        // overriding because default is to either call rename, or copy only current file, which is not good for folders
        destFile.copyFrom( this, Selectors.SELECT_ALL );
        delete();
      }
    } catch ( RepositoryClientException e ) {
      throw new FileSystemException( e );
    }
    try {
      handleDelete();
    } catch ( Exception e ) {
      throw new FileSystemException( e );
    }
  }

  private boolean haveSameExtension( FileObject file1, FileObject file2 ) {
    return Objects.equal( file1.getName().getExtension(), file2.getName().getExtension() );
  }

  private static String getNameNoExt( FileObject file ) {
    return FilenameUtils.removeExtension( file.getName().getBaseName() );
  }

  /** Rename this file in place */
  private void callRepoRename( String newName ) throws RepositoryClientException {
    String[] origPath = computeFileNames( getName() );
    log.debug( "Optimized rename call" );
    repoClient.rename( origPath, newName );
  }

  /** Move this to a different parent */
  private void callRepoMove( FileObject targetFolder ) throws FileSystemException, RepositoryClientException {
    if ( targetFolder == null ) {
      // beyond root
      throw new FileSystemException( "Illegal move target" );
    }
    if ( !targetFolder.exists() ) {
      log.debug( "Creating target folder {} for move", targetFolder.getName() );
      targetFolder.createFolder();
    }
    RepositoryFileDto thisFile = getFileDto();
    String[] destPath = computeFileNames( targetFolder.getName() );
    log.debug( "Optimized move call" );
    repoClient.moveTo( thisFile, destPath );
  }

  private static boolean haveSameName( FileObject file1, FileObject file2 ) {
    return file1.getName().getBaseName().equals( file2.getName().getBaseName() );
  }

  private static boolean haveSameParent( FileObject file1, FileObject file2 ) throws FileSystemException {
    FileObject parent1 = file1.getParent();
    FileObject parent2 = file2.getParent();
    if ( parent1 == null ) {
      return parent2 == null;
    } else if ( parent2 == null ) {
      return false;
    }
    return Arrays.equals( computeFileNames( parent1.getName() ), computeFileNames( parent2.getName() ) );
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
    try {
      repoClient.delete( getFileDto() );
    } catch ( RepositoryClientException e ) {
      throw new FileSystemException( e );
    }
  }

  @Override
  protected void onChildrenChanged( FileName child, FileType newType ) throws Exception {
    super.onChildrenChanged( child, newType );
    refresh();
  }

  @Override
  protected void onChange() throws Exception {
    refresh();
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
      if ( StringUtils.isNotEmpty( name ) ) {
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

  @Override
  public void createFile() throws FileSystemException {
    log.debug( "new file: {}", getName() );
    if ( isKettleFile( getName().getBaseName() ) ) {
      // avoid error when trying to create empty trans/job
    } else {
      super.createFile();
    }
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

  private RepositoryFileDto[] fetchChildren() {
    return repoClient.fetchChildren( computeFileNames( getName() ) );
  }

}
