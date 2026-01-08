package org.pentaho.di.plugins.repofvs.pur.vfs;

import org.pentaho.di.plugins.repofvs.pur.RepositoryLoader;
import org.pentaho.di.plugins.repofvs.pur.RepositoryLoader.RepositoryLoadException;
import org.pentaho.di.plugins.repofvs.pur.converter.RepoContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IRepositoryContentConverterHandler;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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

  public interface ContentConverterHandlerFactory {
    IRepositoryContentConverterHandler getContentHandler( FileSystemOptions fileSystemOptions,
                                                          IUnifiedRepository repo );
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

  private final RepositoryLoader purLoader;
  private final ContentConverterHandlerFactory handlerLoader;

  /**
   * Creates a provider for use within a Pentaho Server, using the single repository and
   * content converter handler provided by the platform
   * @return VFS provider
   */
  public static PurProvider createServerLocalProvider() {
    return new PurProvider(
      opts -> PentahoSystem.get( IUnifiedRepository.class ),
      (opts, pur) -> PentahoSystem.get( IRepositoryContentConverterHandler.class ) );
  }

  /**
   * Creates a provider able to be used by client applications (ie anything outside Pentaho Server).
   * It relies on the `pur` plugin to get the appropriate content converters for kettle files.
   * @param repoLoader Supplies a repository instance
   * @return VFS provider
   * @see RepoContentConverterHandler
   */
  public static PurProvider createClientRemoteProvider( RepositoryLoader repoLoader ) {
    return new PurProvider( repoLoader,
      (opts, pur) -> new RepoContentConverterHandler( pur ) );

  }

  /**
   * Create a new provider instance. Production use cases are covered by {@link #createClientRemoteProvider(RepositoryLoader)} and
   * {@link #createServerLocalProvider()}.
   * @param repoLoader Supplies a repository instance
   * @param handlerLoader Supplies a content converter handler that specifies how to access file contents in the repository
   */
  public PurProvider( RepositoryLoader repoLoader, ContentConverterHandlerFactory handlerLoader ) {
    this.purLoader = repoLoader;
    this.handlerLoader = handlerLoader;
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

  public boolean test( FileSystemOptions opts ) {
    try {
      IUnifiedRepository repo = purLoader.loadRepository( opts );
      if ( repo == null ) {
        return false;
      }
      repo.logout();
      return true;
    } catch ( RepositoryLoadException e ) {
      return false;
    }
  }

  @Override
  protected PurFileSystem doCreateFileSystem( FileName rootFileName, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    log.debug( "Creating file system" );
    IUnifiedRepository repo;
    try {
      repo = purLoader.loadRepository( fileSystemOptions );
    } catch ( RepositoryLoadException e ) {
      throw new FileSystemException( e );
    }
    if ( repo == null ) {
      throw new FileSystemException( "Unable to obtain a IUnifiedRepository instance" );
    } else {
      log.info( "PUR file system created ({})", rootFileName );
    }
    var contentHandler = handlerLoader.getContentHandler( fileSystemOptions, repo );
    return new PurFileSystem( rootFileName, fileSystemOptions, repo, contentHandler );
  }

}
