/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ConnectionFileProvider extends AbstractOriginatingFileProvider {

  public static final String SCHEME = "pvfs";
  public static final String ROOT_URI = SCHEME + "://";
  public static final String SCHEME_NAME = "Connection File System";

  protected static final Collection<Capability>
    capabilities = Collections.unmodifiableCollection( Arrays.asList( Capability.CREATE, Capability.DELETE,
    Capability.RENAME, Capability.GET_TYPE, Capability.LIST_CHILDREN, Capability.READ_CONTENT, Capability.URI,
    Capability.WRITE_CONTENT, Capability.GET_LAST_MODIFIED, Capability.RANDOM_ACCESS_READ ) );

  public ConnectionFileProvider() {
    setFileNameParser( ConnectionFileNameParser.getInstance() );
  }

  @Override
  protected FileSystem doCreateFileSystem( @NonNull FileName rootName, @NonNull FileSystemOptions fileSystemOptions )
    throws FileSystemException {

    Bowl bowl = getBowl( fileSystemOptions );
    ConnectionManager manager = getDefaultManager( bowl );
    VFSConnectionManagerHelper vfsConnectionManagerHelper = getVfsConnectionManagerHelper();
    return new ConnectionFileSystem( rootName, fileSystemOptions, manager, vfsConnectionManagerHelper );
  }

  @NonNull
  private VFSConnectionManagerHelper getVfsConnectionManagerHelper() {
    return new VFSConnectionManagerHelper( ConnectionFileNameUtils.getInstance() );
  }

  @Override
  protected ConnectionFileNameParser getFileNameParser() {
    return (ConnectionFileNameParser) super.getFileNameParser();
  }

  @Override
  protected void setFileNameParser( FileNameParser parser ) {
    if ( !( parser instanceof ConnectionFileNameParser ) ) {
      throw new IllegalArgumentException( "Argument 'parser' is not an instance of 'ConnectionFileNameParser'." );
    }

    super.setFileNameParser( parser );
  }

  // The bowl is stored in the FileSystemOptions.
  @NonNull
  protected Bowl getBowl( @NonNull FileSystemOptions fileSystemOptions ) {
    return KettleGenericFileSystemConfigBuilder.getInstance().getBowl( fileSystemOptions );
  }

  @NonNull
  protected ConnectionManager getDefaultManager( @NonNull Bowl bowl ) throws FileSystemException {
    try {
      return bowl.getManager( ConnectionManager.class );
    } catch ( KettleException e ) {
      throw new KettleVFSFileSystemException( "ConnectionFileProvider.FailedLoadConnectionManager", e );
    }
  }

  @Override
  public Collection<Capability> getCapabilities() {
    return capabilities;
  }
}
