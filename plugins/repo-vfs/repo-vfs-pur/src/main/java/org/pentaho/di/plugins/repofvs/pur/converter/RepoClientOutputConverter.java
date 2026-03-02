package org.pentaho.di.plugins.repofvs.pur.converter;

import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.plugins.repofvs.pur.vfs.PurProvider.OutputConverter;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepoClientOutputConverter implements OutputConverter {

  private static final Logger log = LoggerFactory.getLogger( RepoClientOutputConverter.class );

  private final IUnifiedRepository pur;
  private final Repository repo;

  public RepoClientOutputConverter( Repository repo, IUnifiedRepository pur ) {
    this.pur = pur;
    this.repo = repo;
  }

  @Override
  public OutputStream getOutputStream( RepositoryFile file ) {
    String ext = FilenameUtils.getExtension( file.getName() );
    if ( ext != null ) {
      return switch ( ext.toLowerCase() ) {
        case Const.STRING_TRANS_DEFAULT_EXT -> RepoMetaOutputStream.createTransMetaOutputStream( repo, file );
        case Const.STRING_JOB_DEFAULT_EXT -> RepoMetaOutputStream.createJobMetaOutputStream( repo, file );
        default -> new RepositoryFileOutputStream( file, false, false, pur );
      };
    }
    // only reachable if filename is null
    return new RepositoryFileOutputStream( file, false, false, pur );
  }

  /** OutputStream for KTR/KJB that saves them to repository (as nodes) when closed. Will hold the whole file in memory */
  public static class RepoMetaOutputStream extends ByteArrayOutputStream {
    private final Repository repo;
    private final RepositoryFile file;
    private final MetaLoader metaLoader;

    private boolean closed;

    interface MetaLoader {
      AbstractMeta loadMeta( InputStream xmlInputStream ) throws KettleException;
    }

    public static RepoMetaOutputStream createTransMetaOutputStream( Repository repo, RepositoryFile file ) {
      return new RepoMetaOutputStream( repo, file, xmlIn -> new TransMeta( xmlIn, repo, false, null, null ) );
    }


    public static RepoMetaOutputStream createJobMetaOutputStream( Repository repo, RepositoryFile file ) {
      return new RepoMetaOutputStream( repo, file, xmlIn -> new JobMeta( xmlIn, repo, null ) );
    }

    RepoMetaOutputStream( Repository repo, RepositoryFile file, MetaLoader metaLoader ) {
      this.repo = repo;
      this.file = file;
      this.metaLoader = metaLoader;
    }

    @Override
    public void close() throws IOException {
      if ( closed ) {
        throw new IOException( new IllegalStateException( "Already closed" ) );
      }
      closed = true;
      saveToRepository();
      super.close();
    }

    private void saveToRepository() throws IOException {
      try ( ByteArrayInputStream bais = new ByteArrayInputStream( toByteArray() ) ) {
        var meta = metaLoader.loadMeta( bais );
        String filePath = file.getPath();
        String fileName = FilenameUtils.getName( filePath );
        String parentPath = FilenameUtils.getFullPath( filePath );
        meta.setFilename( fileName );
        meta.setRepositoryDirectory( repo.findDirectory( parentPath ) );
        repo.save( meta, null, null );
      } catch ( KettleException e ) {
        log.error( "Error saving file {}", file.getPath(), e );
      }
    }
  }
}
