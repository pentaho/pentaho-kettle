/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.connections.vfs.provider;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.vfs.KettleVFSFileSystemException;
import org.pentaho.di.core.vfs.configuration.IKettleFileSystemConfigBuilder;
import org.pentaho.di.core.vfs.configuration.KettleFileSystemConfigBuilderFactory;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public class ConnectionFileSystem extends AbstractFileSystem implements FileSystem {

  public static final String CONNECTION = "connection";

  @NonNull
  private final ConnectionManager connectionManager;

  @NonNull
  private final VFSConnectionManagerHelper vfsConnectionManagerHelper;

  public ConnectionFileSystem( @NonNull FileName rootName,
                               @NonNull FileSystemOptions fileSystemOptions,
                               @NonNull ConnectionManager connectionManager,
                               @NonNull VFSConnectionManagerHelper vfsConnectionManagerHelper ) {
    super( rootName, null, fileSystemOptions );

    this.connectionManager = Objects.requireNonNull( connectionManager );
    this.vfsConnectionManagerHelper = Objects.requireNonNull( vfsConnectionManagerHelper );
  }

  @Override
  protected FileObject createFile( AbstractFileName fileName ) throws Exception {
    ConnectionFileName pvfsFileName = (ConnectionFileName) fileName;

    // 1. pvfs:// has no associated connection, so no provider file object.
    String connectionName = pvfsFileName.getConnection();
    if ( connectionName == null ) {
      return new PvfsRootFileObject( pvfsFileName, this );
    }

    // 2. A file name that references a connection that does not exist.
    VFSConnectionDetails details = connectionManager.getDetails( connectionName );
    if ( details == null ) {
      return new UndefinedConnectionFileObject( pvfsFileName, this );
    }

    initializeVariableSpace( details );

    // 3. Connections with buckets don't support degenerate root provider URIs (e.g. s3://).
    //    If using buckets, such URIs would be generated, if transformer were called.
    if ( pvfsFileName.isConnectionRoot() && vfsConnectionManagerHelper.usesBuckets( details ) ) {
      return new ConnectionWithBucketsRootFileObject( pvfsFileName, this );
    }

    // 4. Normal case, for which there is an associated provider file object.
    FileName providerFileName = vfsConnectionManagerHelper
      .getExistingProvider( connectionManager, details )
      .getFileNameTransformer( connectionManager )
      .toProviderFileName( pvfsFileName, details );

    AbstractFileObject<?> providerFileObject =
      (AbstractFileObject<?>) getKettleVFS()
        .getFileObject( providerFileName.getURI(), details.getSpace() );

    return new ResolvedConnectionFileObject( pvfsFileName, this, providerFileObject );
  }

  /**
   * Called by a {@link ConnectionFileObject}, {@code parentFileObject} to create a child connection file for a given
   * child provider file.
   * <p>
   * As part of creating the child connection file object, this method performs the inverse translation of the URI, from
   * provider to PVFS.
   *
   * @param parentFileObject The parent connection file object.
   * @param childProviderFileObject The child provider file object.
   * @return The child connection file object.
   */
  ConnectionFileObject createChild( @NonNull ConnectionFileObject parentFileObject,
                                    @NonNull FileObject childProviderFileObject )
    throws FileSystemException {

    @Nullable
    String connectionName = parentFileObject.getName().getConnection();
    if ( connectionName == null ) {
      throw new IllegalArgumentException( "Cannot create a resolved child file object for the PVFS root file" );
    }

    ConnectionFileName childPvfsFileName = toPvfsFileName( childProviderFileObject.getName(), connectionName );

    return new ResolvedConnectionFileObject( childPvfsFileName, this, (AbstractFileObject<?>) childProviderFileObject );
  }

  @NonNull
  protected ConnectionFileName toPvfsFileName( @NonNull FileName providerFileName, @NonNull String connectionName )
    throws FileSystemException {

    VFSConnectionDetails details = getExistingConnectionDetails( connectionName );

    try {
      return vfsConnectionManagerHelper
        .getExistingProvider( connectionManager, details )
        .getFileNameTransformer( connectionManager )
        .toPvfsFileName( providerFileName, details );
    } catch ( KettleException e ) {
      throw new KettleVFSFileSystemException(
        "ConnectionFileSystem.FailedTransformProviderFilename",
        e,
        providerFileName.toString(),
        details.getName() );
    }
  }

  @Override
  protected void addCapabilities( Collection<Capability> collection ) {
    collection.addAll( ConnectionFileProvider.capabilities );
  }

  @Override
  public FileObject resolveFile( FileName name ) throws FileSystemException {
    try {
      return createFile( (AbstractFileName) name );
    } catch ( Exception e ) {
      throw new FileSystemException( "vfs.provider/resolve-file.error", name, e );
    }
  }

  // region Helpers
  @NonNull
  protected Bowl getBowl() {
    return KettleGenericFileSystemConfigBuilder.getInstance().getBowl( getFileSystemOptions() );
  }

  @NonNull
  protected IKettleVFS getKettleVFS() {
    return KettleVFS.getInstance( getBowl() );
  }

  @NonNull
  private VFSConnectionDetails getExistingConnectionDetails( @NonNull String connectionName )
    throws KettleVFSFileSystemException {
    try {
      return connectionManager.getExistingDetails( connectionName );
    } catch ( KettleException e ) {
      throw new KettleVFSFileSystemException( "ConnectionFileSystem.ExpectedConnectionNotFound", connectionName, e );
    }
  }

  @NonNull
  private VariableSpace initializeVariableSpace( @NonNull VFSConnectionDetails details )
    throws IOException {

    VariableSpace varSpace = getSpace();

    // Used by KettleVFSImpl#buildFsOptions -> VFSHelper#getOpts(...).
    varSpace.setVariable( CONNECTION, details.getName() );

    details.setSpace( varSpace );

    return varSpace;
  }

  @NonNull
  protected VariableSpace getSpace() throws IOException {
    return (VariableSpace) getConfigBuilder().getVariableSpace( getFileSystemOptions() );
  }

  @NonNull
  private static IKettleFileSystemConfigBuilder getConfigBuilder() throws IOException {
    return KettleFileSystemConfigBuilderFactory.getConfigBuilder( new Variables(), ConnectionFileProvider.SCHEME );
  }
  // endregion
}
