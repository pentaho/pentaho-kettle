package org.pentaho.di.plugins.repofvs.pur.vfs;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * VFS Provider based on {@link IUnifiedRepository}
 */
public class PurProvider extends AbstractOriginatingFileProvider {


  public interface OutputConverter {
    OutputStream getOutputStream( RepositoryFile file );
  }

  public interface RepositoryAccess {
    IUnifiedRepository getPur();
    IRepositoryContentConverterHandler getContentHandler();
    default Optional<OutputConverter> getOutputConverter() {
      return Optional.empty();
    }
  }

  public interface RepositoryAccessFactory {
    RepositoryAccess createRepositoryAccess( FileSystemOptions ops ) throws KettleException;
  }

  // schemes currently need to contain dashes so they bypass the regex
  // of the (bad) VFSLookupFilter implementation and allow our own LookupFilter to be called
  public static final String SCHEME_LOCAL = "pur-l";
  public static final String SCHEME_REMOTE = "pur-r";

  private static final Logger log = LoggerFactory.getLogger( PurProvider.class );

  public static final Collection<Capability> capabilities = Collections.unmodifiableCollection( Arrays.asList(
    Capability.GET_TYPE,
    Capability.GET_LAST_MODIFIED,
    Capability.LIST_CHILDREN,
    Capability.READ_CONTENT,
    Capability.WRITE_CONTENT,
    Capability.CREATE,
    Capability.FS_ATTRIBUTES,
    Capability.URI ) );

  private final RepositoryAccessFactory raf;

  public PurProvider( RepositoryAccessFactory raf ) {
    this.raf = raf;
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

  public boolean test( FileSystemOptions opts ) {
    try {
      var repo = raf.createRepositoryAccess( opts ).getPur();
      if ( repo == null ) {
        return false;
      }
      repo.logout();
      return true;
    } catch ( KettleException e ) {
      return false;
    }
  }

  @Override
  protected PurFileSystem doCreateFileSystem( FileName rootFileName, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    try {
      log.debug( "Creating file system" );
      var repoAccess = raf.createRepositoryAccess( fileSystemOptions );
      return new PurFileSystem( rootFileName, fileSystemOptions, repoAccess );
    } catch ( KettleException e ) {
      throw new FileSystemException( e );
    }
  }

}
