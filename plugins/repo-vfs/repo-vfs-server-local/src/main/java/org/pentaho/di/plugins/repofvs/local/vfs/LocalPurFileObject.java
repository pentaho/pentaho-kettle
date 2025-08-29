package org.pentaho.di.plugins.repofvs.local.vfs;

import org.pentaho.platform.api.repository2.unified.Converter;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.platform.api.repository2.unified.data.simple.SimpleRepositoryFileData;
import org.pentaho.platform.repository.RepositoryFilenameUtils;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalPurFileObject extends AbstractFileObject<LocalPurFileSystem> {

  private static final Logger log = LoggerFactory.getLogger( LocalPurFileObject.class );

  private final IUnifiedRepository pur;
  private final IRepositoryContentConverterHandler converterHandler;
  private RepositoryFile file;

  protected LocalPurFileObject( AbstractFileName fileName, LocalPurFileSystem fileSystem, RepositoryFile file ) {
    super( fileName, fileSystem );
    this.file = file;
    this.pur = fileSystem.getRepository();
    this.converterHandler = fileSystem.getContentHandler();

   log.debug( "{}({})", getClass().getSimpleName(), fileName );
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

  @Override
  protected long doGetContentSize() throws Exception {
    return file.getFileSize();
  }

  @Override
  protected FileType doGetType() throws Exception {
    if ( file.getId() == null ) {
      return FileType.IMAGINARY;
    }
    if ( file.isFolder() ) {
      return FileType.FOLDER;
    }
    return FileType.FILE;
  }

  @Override
  protected String[] doListChildren() throws Exception {
    log.debug( "{}.doListChildren", getName() );


    // getChildren says it receives a path but it actually wants the ID...
    RepositoryRequest req = new RepositoryRequest( file.getId().toString(), true, 1, null );

    return pur.getChildren( req ).stream()
      .map( RepositoryFile::getName ).toArray( String[]::new );
  }

  @Override
  protected boolean doIsHidden() throws Exception {
    return file.isHidden();
  }

  @Override
  protected OutputStream doGetOutputStream( boolean bAppend ) throws Exception {
    return new RepositoryFileOutputStream( file, false, false, pur );
  }

  @Override
  protected InputStream doGetInputStream() throws Exception {
    Converter converter = converterHandler.getConverter( getFileExtension() );
    return converter.convert( file.getId() );
  }

  private String getFileExtension() {
    return RepositoryFilenameUtils.getExtension( file.getName() );
  }

  @Override
  protected void doCreateFolder() throws Exception {
    if ( !file.isFolder() ) {
      file = changeFolderStatus( file, true );
    }
    getRepoParentId().ifPresent( parentId -> {
      this.file = pur.createFolder( parentId, file, null );
    } );
  }

  private static RepositoryFile changeFolderStatus( RepositoryFile file, boolean isFolder ) {
    return new RepositoryFile.Builder( file ).folder( isFolder ).build();
  }

  private Optional<Serializable> getRepoParentId() {
    return getRepoParent().map( RepositoryFile::getId );
  }

  private Optional<RepositoryFile> getRepoParent() {
    String path = StringUtils.chomp( file.getPath(), "/" );
    if ( StringUtils.isEmpty( path ) ) {
      // i am root
      return Optional.empty();
    }
    String parentPath = FilenameUtils.getFullPath( path );

    // api can return null but shouldn't here
    return Optional.ofNullable( pur.getFile( parentPath ) );
  }

  @Override
  protected void doDelete() throws Exception {
    // doDelete only called if file exists (id != null)
    pur.deleteFile( file.getId(), null );
  }

  @Override
  public void createFile() throws FileSystemException {
    if ( !exists() ) {
      Serializable parentId = getRepoParentId().orElseThrow(
        () -> new FileSystemException( "vfs.provider/create-file.error", file.getName() ) );
      if ( file.isFolder() ) {
        file = changeFolderStatus(file, false);
      }
      file = pur.createFile( parentId, file, getEmptyFileData(), null );
    } else {
      // get the error
      super.createFile();
    }
  }

  private IRepositoryFileData getEmptyFileData() {
    //TODO: MIME
    // there is a bean for this
    return new SimpleRepositoryFileData( new ByteArrayInputStream( new byte[ 0 ] ), StandardCharsets.UTF_8.name(),
      "text/plain" );
  }

  @Override
  public boolean delete() throws FileSystemException {
    return delete( Selectors.SELECT_SELF_AND_CHILDREN ) > 0;
  }

  @Override
  protected void doRename( FileObject newFile ) throws Exception {
    final String newPath = newFile.getName().getPath();
    pur.moveFile( file.getId(), newPath, null );
    newFile.refresh();
  }

  @Override
  public void refresh() throws FileSystemException {
    RepositoryFile newFile = pur.getFile( getName().getPath() );
    if ( newFile != null ) {
      this.file = newFile;
    } else {
      invalidateFile();
    }
  }

  private void invalidateFile() {
    this.file = new RepositoryFile.Builder( this.file ).id( null ).build();
  }

}
