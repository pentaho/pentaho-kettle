/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2024 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.connections.vfs;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.ConnectionProvider;
import org.pentaho.di.connections.LookupFilter;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameUtils;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The {@link VFSConnectionManagerHelper} class contains connection management logic which is VFS-specific.
 * <p>
 * Specifically, this class contains logic which is common to all VFS providers. Logic which is VFS-provider-specific is
 * instead handled by implementations of {@link VFSConnectionProvider<VFSConnectionDetails>}.
 * <p>
 * The {@link ConnectionManager} delegates certain operations to this helper class, for {@link VFSConnectionDetails}
 * connections. For example, this is the case of {@link ConnectionManager#test(ConnectionDetails)}.
 * <p>
 * However, this class can also be used directly, to gain access to VFS-specific functionality, not exposed generically
 * via {@link ConnectionManager}.
 * <p>
 * This pattern may work in the future if {@link ConnectionManager} is expanded to work with other connection types,
 * such as database connections.
 */
public class VFSConnectionManagerHelper {

  private static VFSConnectionManagerHelper instance;

  @NonNull
  private final ConnectionFileNameUtils vfsConnectionFileNameUtils;

  public VFSConnectionManagerHelper() {
    this( ConnectionFileNameUtils.getInstance() );
  }

  public VFSConnectionManagerHelper( @NonNull ConnectionFileNameUtils vfsConnectionFileNameUtils ) {
    this.vfsConnectionFileNameUtils = Objects.requireNonNull( vfsConnectionFileNameUtils );
  }

  @NonNull
  public static VFSConnectionManagerHelper getInstance() {
    if ( instance == null ) {
      instance = new VFSConnectionManagerHelper();
    }

    return instance;
  }

  @SuppressWarnings( "unchecked" )
  public List<VFSConnectionProvider<VFSConnectionDetails>> getProviders( @NonNull ConnectionManager manager ) {
    return manager.getProvidersByType( VFSConnectionProvider.class )
      .stream()
      .map( provider -> (VFSConnectionProvider<VFSConnectionDetails>) provider )
      .collect( Collectors.toList() );
  }

  @NonNull
  public List<VFSConnectionDetails> getAllDetails( @NonNull ConnectionManager manager ) {
    return getProviders( manager )
      .stream()
      .flatMap( provider -> {
        List<VFSConnectionDetails> providerDetails = provider.getConnectionDetails( manager );
        return providerDetails != null ? providerDetails.stream() : null;
      } )
      .collect( Collectors.toList() );
  }

  // region getProvider, getExistingProvider

  /**
   * Gets a VFS connection provider, given its key.
   * <p>
   * A provider key is either its own {@link ConnectionProvider#getKey()} or a registered alias, via
   * {@link ConnectionManager#addLookupFilter(LookupFilter)}.
   * <p>
   * This method will throw a {@link ClassCastException} exception if a provider with the given key is defined which is
   * not of the expected type, {@link VFSConnectionProvider<T>}.
   *
   * @param key The VFS connection provider key.
   * @return The VFS connection provider, if one exists; {@code null}, otherwise.
   */
  @SuppressWarnings( "unchecked" )
  @Nullable
  public <T extends VFSConnectionDetails> VFSConnectionProvider<T> getProvider(
    @NonNull ConnectionManager manager,
    @Nullable String key ) {
    return (VFSConnectionProvider<T>) manager.getConnectionProvider( key );
  }

  /**
   * Gets a VFS connection provider, given its key, and throwing an exception if one is not defined.
   * <p>
   * A provider key is either its own {@link ConnectionProvider#getKey()} or a registered alias, via
   * {@link ConnectionManager#addLookupFilter(LookupFilter)}.
   * <p>
   * This method will throw a {@link ClassCastException} exception if a provider with the given key is defined which is
   * not of the expected type, {@link VFSConnectionProvider<T>}.
   *
   * @param key The provider key.
   * @return The VFS connection provider.
   * @throws KettleException When a provider with the given key is not defined.
   */
  @NonNull
  public <T extends VFSConnectionDetails> VFSConnectionProvider<T> getExistingProvider(
    @NonNull ConnectionManager manager,
    @Nullable String key )
    throws KettleException {

    VFSConnectionProvider<T> provider = getProvider( manager, key );
    if ( provider == null ) {
      throw new KettleException( String.format( "Undefined connection provider for key '%s'.", key ) );
    }

    return provider;
  }

  /**
   * Gets a VFS provider, given the details of the VFS connection.
   * <p>
   * This method will throw a {@link ClassCastException} exception if a provider with the given key is defined which is
   * not of the expected type, {@link VFSConnectionProvider<T>}.
   *
   * @param details The details of the VFS connection.
   * @return The VFS connection provider, if one exists; {@code null}, otherwise.
   */
  @Nullable
  public <T extends VFSConnectionDetails> VFSConnectionProvider<T> getProvider(
    @NonNull ConnectionManager manager,
    @Nullable T details ) {
    return details != null ? getProvider( manager, details.getType() ) : null;
  }

  /**
   * Gets a VFS provider, given the details of the VFS connection, and throwing an exception if one is not defined.
   *
   * @param details The details of the VFS connection.
   * @return The VFS connection provider.
   * @throws KettleException When a provider with the given key is not defined.
   */
  @NonNull
  public <T extends VFSConnectionDetails> VFSConnectionProvider<T> getExistingProvider(
    @NonNull ConnectionManager manager,
    @NonNull T details )
    throws KettleException {
    return getExistingProvider( manager, details.getType() );
  }
  // endregion

  // region Connection Root
  @NonNull
  public ConnectionFileName getConnectionRootFileName( @NonNull VFSConnectionDetails details ) {

    String connectionName = details.getName();
    if ( StringUtils.isEmpty( connectionName ) ) {
      throw new IllegalArgumentException( "Unnamed connection" );
    }

    return new ConnectionFileName( connectionName );
  }

  @VisibleForTesting
  @NonNull <T extends VFSConnectionDetails> FileName getConnectionRootProviderFileName(
    @NonNull VFSConnectionFileNameTransformer<T> fileNameTransformer,
    @NonNull T details )
    throws KettleException {

    // Example: "pvfs://my-connection"
    ConnectionFileName rootPvfsFileName = getConnectionRootFileName( details );

    // Example: "s3://root-path-bucket"
    // Would fail if only "s3://"
    return fileNameTransformer.toProviderFileName( rootPvfsFileName, details );
  }

  // For bucketed connections with no domain, a provider file object / file name corresponding to the connection root
  // can only be obtained if there is a root path. Otherwise, we'd need to be able to build degenerate file name, such
  // as `s3://`, which is not a valid URI and is not supported by URLFileName, used by many of these connections.
  // The single caller of this method is the test method, and only calls this when there is a resolved root path,
  // for which the restriction necessarily does not apply.
  @NonNull
  private <T extends VFSConnectionDetails> FileObject getConnectionRootProviderFileObject(
    @NonNull ConnectionManager manager,
    @NonNull VFSConnectionProvider<T> provider,
    @NonNull T details )
    throws KettleException {

    FileName rootProviderFileName =
      getConnectionRootProviderFileName( provider.getFileNameTransformer( manager ), details );

    IKettleVFS kettleVFS = getKettleVFS( manager.getBowl() );

    return kettleVFS.getFileObject(
      rootProviderFileName.getURI(),
      new Variables(),
      provider.getOpts( details ) );
  }
  // endregion

  // region VFS Resolved Root Path
  /**
   * Gets the resolved root path of a given connection.
   * <p>
   * This method returns the value of the {@link VFSConnectionDetails#getRootPath() root path} after any variables
   * substituted and subsequent normalization.
   *
   * <p>
   * Root path normalization fixes separators, resolves "." and ".." segments, and removes any leading and trailing
   * path separator.
   *
   * <p>
   * Root path normalization does not affect the encoding of the original root path value, after variable
   * substitution, which should be that of the specific connection/file provider.
   *
   * @param details The VFS connection details.
   * @return The non-empty resolved root path, if any; {@code null}, if. none.
   */
  @Nullable
  public String getResolvedRootPath( @NonNull VFSConnectionDetails details )
    throws KettleException {

    String rootPath = details.getRootPath();
    if ( StringUtils.isNotEmpty( rootPath ) ) {

      // IMPROVEMENT: Consider detecting unsubstituted variables.
      String resolvedRootPath = getSpace( details ).environmentSubstitute( rootPath );
      if ( resolvedRootPath != null ) {
        return normalizeRootPath( resolvedRootPath );
      }
    }

    return null;
  }

  @Nullable
  protected String normalizeRootPath( @NonNull String rootPath ) throws KettleException {
    rootPath = rootPath.trim();
    if ( rootPath.isEmpty() ) {
      return null;
    }

    StringBuilder rootPathBuilder = new StringBuilder( rootPath );

    UriParser.fixSeparators( rootPathBuilder );

    try {
      UriParser.normalisePath( rootPathBuilder );
    } catch ( FileSystemException e ) {
      throw new KettleException( String.format( "Root path contains invalid relative segments: '%s'", rootPath ), e );
    }

    // Remove leading separator.
    vfsConnectionFileNameUtils.trimLeadingSeparator( rootPathBuilder );

    // Example: "" | "root/path"

    return rootPathBuilder.length() > 0
      ? rootPathBuilder.toString()
      : null;
  }
  // endregion

  // region test

  /**
   * Tests if a VFS connection is valid, given its details, optionally, with certain testing options.
   * <p>
   * This method first delegates to {@link ConnectionProvider#test(ConnectionDetails)} to perform basic
   * validation, independent of the connection's root path, {@link VFSConnectionDetails#getRootPath()}, if any,
   * immediately returning {@code false}, when unsuccessful.
   * <p>
   * When base validation is successful, if {@code options} has a {@code true}
   * {@link VFSConnectionTestOptions#isRootPathIgnored()}, this method should immediately return {@code true}.
   * <p>
   * Otherwise, the method should validate that the connection's root folder path is valid, taking into account the
   * values of {@link VFSConnectionDetails#isRootPathSupported()}, {@link VFSConnectionDetails#isRootPathRequired()} and
   * {@link VFSConnectionDetails#getRootPath()}.
   *
   * @param manager The connection manager.
   * @param details The details of the VFS connection to test.
   * @param options The testing options, or {@code null}. When {@code null}, a default instance of
   *                {@link VFSConnectionTestOptions} is constructed and used.
   * @return {@code true} if the connection is valid; {@code false} otherwise.
   */
  public <T extends VFSConnectionDetails> boolean test( @NonNull ConnectionManager manager,
                                                        @NonNull T details,
                                                        @Nullable VFSConnectionTestOptions options )
    throws KettleException {

    if ( options == null ) {
      options = new VFSConnectionTestOptions();
    }

    // The specified connection details may not exist saved in the meta-store,
    // but still needs to have a non-empty name in it, to be able to form a temporary PVFS URI.
    if ( StringUtils.isEmpty( details.getName() ) ) {
      return false;
    }

    VFSConnectionProvider<T> provider = getExistingProvider( manager, details );
    if ( !provider.test( details ) ) {
      return false;
    }

    if ( !details.isRootPathSupported() || options.isRootPathIgnored() ) {
      return true;
    }

    String resolvedRootPath;
    try {
      resolvedRootPath = getResolvedRootPath( details );
    } catch ( KettleException e ) {
      // Invalid root path.
      return false;
    }

    if ( resolvedRootPath == null ) {
      return !details.isRootPathRequired();
    }

    // Ensure that root path exists and is a folder.
    return isFolder( getConnectionRootProviderFileObject( manager, provider, details ) );
  }
  // endregion

  // region Buckets

  /**
   * Indicates if a VFS connection, given its details, uses buckets in its current configuration.
   * <p>
   * A VFS connection is using buckets if it can have buckets, as determined by
   * {@link VFSConnectionDetails#hasBuckets()}, and if its
   * {@link #getResolvedRootPath(VFSConnectionDetails) resolved root path} is {@code null}.
   *
   * @param details The VFS connection to check.
   * @return {@code true} if a connection uses buckets; {@code false} otherwise.
   */
  public boolean usesBuckets( @NonNull VFSConnectionDetails details ) throws KettleException {
    return details.hasBuckets() && getResolvedRootPath( details ) == null;
  }
  // endregion

  // region Helpers
  @VisibleForTesting
  @NonNull
  IKettleVFS getKettleVFS( @NonNull Bowl bowl ) {
    return KettleVFS.getInstance( bowl );
  }

  @NonNull
  private static VariableSpace getSpace( @NonNull ConnectionDetails connectionDetails ) {
    VariableSpace space = connectionDetails.getSpace();
    return space != null ? space : Variables.getADefaultVariableSpace();
  }

  private static boolean isFolder( @NonNull FileObject fileObject ) {
    try {
      return fileObject.exists() && fileObject.isFolder();
    } catch ( FileSystemException e ) {
      return false;
    }
  }
  // endregion
}
