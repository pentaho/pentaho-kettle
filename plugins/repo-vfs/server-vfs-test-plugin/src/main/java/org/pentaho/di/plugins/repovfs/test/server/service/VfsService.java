package org.pentaho.di.plugins.repovfs.test.server.service;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LoggingBuffer;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryBowl;
import org.pentaho.di.repository.RepositoryConnectionUtils;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VfsService {
  private static final Logger log = LoggerFactory.getLogger( VfsService.class );

  private static final FileSelector ALL_FILES = new AllFileSelector();

  private final IKettleVFS vfs;
  private final Optional<Repository> repository;

  private static Repository connectToRepository() {
    LoggingBuffer pdiUserAppender = KettleLogStore.getAppender();

    boolean singleDiServerInstance =
      "true".equals( PentahoSystem.getSystemSetting( "singleDiServerInstance", "true" ) );

    // Calling the kettle utility method to connect to the repository
    IPentahoSession session = PentahoSessionHolder.getSession();
    if ( session != null ) {
      try {
        return RepositoryConnectionUtils.connectToRepository( null, singleDiServerInstance,
          session.getName(), PentahoSystem.getApplicationContext().getFullyQualifiedServerURL(),
          pdiUserAppender );
      } catch ( KettleException e ) {
        log.error( "Unable to connect to repository", e );
      }
    }
    return null;
  }

  public VfsService() {
    Repository repo = connectToRepository();
    Bowl bowl = repo != null ? new RepositoryBowl( repo ) : DefaultBowl.getInstance();
    this.repository = Optional.ofNullable( repo );
    vfs = KettleVFS.getInstance( bowl );
  }

  public void close() {
    repository.ifPresent( Repository::disconnect );
  }

  private FileObject resolveFile( String path ) throws KettleFileException {
    if ( !path.startsWith( "pvfs://" ) ) {
      throw new KettleFileException( "Confined to pvfs" );
    }
    return vfs.getFileObject( path );
  }

  public List<FileInfo> listFiles( String path ) throws FileSystemException, KettleFileException {
    try ( FileObject fileObject = resolveFile( path ) ) {
      if ( !fileObject.exists() || fileObject.getType() != FileType.FOLDER ) {
        return Collections.emptyList();
      }
      FileObject[] children = fileObject.getChildren();
      List<FileInfo> fileList = new ArrayList<>( children.length ) ;
      for ( FileObject child : children ) {
        fileList.add( new FileInfo( child ) );
      }
      return fileList;
    }
  }

  public String readFileAsBase64( String path ) throws IOException, KettleFileException {
    try ( FileObject fileObject = resolveFile( path ) ) {
      if ( !fileObject.exists() || !fileObject.isFile() ) {
        throw new FileNotFoundException( "File not found: " + path );
      }
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try ( InputStream in = fileObject.getContent().getInputStream() ) {
        in.transferTo( out );
      }
      return Base64.getEncoder().encodeToString( out.toByteArray() );
    }
  }

  public void writeFileBase64( String path, String base64Content, boolean append )
    throws IOException, KettleFileException {
    try ( FileObject fileObject = resolveFile( path );
          OutputStream out = fileObject.getContent().getOutputStream( append ) ) {
      byte[] contents = Base64.getDecoder().decode( base64Content );
      out.write( contents );
    }
  }

  public void copy( String sourcePath, String destinationPath )
    throws FileSystemException, KettleFileException {
    try ( FileObject source = resolveFile( sourcePath );
          FileObject destination = resolveFile( destinationPath ) ) {
      destination.copyFrom( source, ALL_FILES );
    }
  }

  public void move( String sourcePath, String destinationPath )
    throws FileSystemException, KettleFileException {
    try ( FileObject source = resolveFile( sourcePath );
          FileObject destination = resolveFile( destinationPath ) ) {
      source.moveTo( destination );
    }
  }

  public boolean delete( String path ) throws FileSystemException, KettleFileException {
    try( FileObject fileObject = resolveFile( path ) ) {
      return fileObject.deleteAll() > 0;
    }
  }

  public boolean exists( String path ) throws FileSystemException, KettleFileException {
    try( FileObject fileObject = resolveFile( path ) ) {
      return fileObject.exists();
    }
  }

public void createDirectory( String path ) throws FileSystemException, KettleFileException {
    try( FileObject fileObject = resolveFile( path ) ) {
      fileObject.createFolder();
    }
  }

  public Optional<FileInfo> getFileInfo( String path ) throws FileSystemException, KettleFileException {
    try ( FileObject fileObject = resolveFile( path ) ) {
      if ( !fileObject.exists() ) {
        return Optional.empty();
      }
      return Optional.of( new FileInfo( fileObject ) );
    }
  }

  public void createEmptyFile( String path ) throws FileSystemException, KettleFileException {
    try ( FileObject fileObject = resolveFile(path) ) {
      fileObject.createFile();
    }
  }
}
