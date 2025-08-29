package org.pentaho.di.plugins.repofvs.local.vfs;

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

public class LocalPurProvider extends AbstractOriginatingFileProvider {

  public static final String SCHEME = "pur-local";

  private static final Logger log = LoggerFactory.getLogger( LocalPurProvider.class );

  public static final Collection<Capability> capabilities = Collections.unmodifiableCollection( Arrays.asList(
    Capability.GET_TYPE,
    Capability.GET_LAST_MODIFIED,
    Capability.LIST_CHILDREN,
    Capability.READ_CONTENT,
    Capability.WRITE_CONTENT,
    Capability.CREATE,
    Capability.FS_ATTRIBUTES,
    Capability.URI ) );

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }

  @Override
  protected LocalPurFileSystem doCreateFileSystem( FileName rootFileName, FileSystemOptions fileSystemOptions )
    throws FileSystemException {
    log.debug( "creating filesystem" );
    IUnifiedRepository repo = createRepository( fileSystemOptions );
    if ( repo == null ) {
      log.error( "no repository!" );
    } else {
      log.info( "filesystem created" );
    }
    return new LocalPurFileSystem( rootFileName, fileSystemOptions, repo, getContentHandler( fileSystemOptions ) );
  }

  protected IUnifiedRepository createRepository( FileSystemOptions fileSystemOptions ) {
    return PentahoSystem.get( IUnifiedRepository.class );
  }

  protected IRepositoryContentConverterHandler getContentHandler( FileSystemOptions fileSystemOptions ) {
    return PentahoSystem.get( IRepositoryContentConverterHandler.class );
  }
}
