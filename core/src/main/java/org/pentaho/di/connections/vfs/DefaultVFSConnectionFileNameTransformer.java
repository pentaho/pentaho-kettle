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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.provider.FileNameParser;
import org.apache.commons.vfs2.provider.UriParser;
import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameUtils;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.vfs.IKettleVFS;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.Objects;

import static org.apache.commons.vfs2.FileName.SEPARATOR;

public class DefaultVFSConnectionFileNameTransformer<T extends VFSConnectionDetails>
  implements VFSConnectionFileNameTransformer<T> {

  @NonNull
  private static final String SCHEME_SUFFIX = ":" + SEPARATOR + SEPARATOR;

  @NonNull
  private final ConnectionManager connectionManager;

  @NonNull
  private final VFSConnectionManagerHelper vfsConnectionManagerHelper;

  @NonNull
  private final ConnectionFileNameUtils connectionFileNameUtils;

  public DefaultVFSConnectionFileNameTransformer( @NonNull ConnectionManager connectionManager ) {
    this( connectionManager, VFSConnectionManagerHelper.getInstance(), ConnectionFileNameUtils.getInstance() );
  }

  public DefaultVFSConnectionFileNameTransformer( @NonNull ConnectionManager connectionManager,
                                                  @NonNull VFSConnectionManagerHelper vfsConnectionManagerHelper,
                                                  @NonNull ConnectionFileNameUtils connectionFileNameUtils ) {
    this.connectionManager = Objects.requireNonNull( connectionManager );
    this.vfsConnectionManagerHelper = Objects.requireNonNull( vfsConnectionManagerHelper );
    this.connectionFileNameUtils = Objects.requireNonNull( connectionFileNameUtils );
  }

  @NonNull
  protected ConnectionManager getConnectionManager() {
    return connectionManager;
  }

  @NonNull
  protected VFSConnectionManagerHelper getVfsConnectionManagerHelper() {
    return vfsConnectionManagerHelper;
  }

  @NonNull
  protected ConnectionFileNameUtils getConnectionFileNameUtils() {
    return connectionFileNameUtils;
  }

  // region toProviderFileName
  @NonNull
  @Override
  public FileName toProviderFileName( @NonNull ConnectionFileName pvfsFileName, @NonNull T details )
    throws KettleException {

    StringBuilder providerUriBuilder = new StringBuilder();

    appendProviderUriConnectionRoot( providerUriBuilder, details );

    // Examples:
    //  providerUriBuilder: "hcp://domain.my:443/root/path" | "local:///C:/root/path" | "s3://"
    //  getPath():          "/folder/sub-folder" | "/"

    appendProviderUriRestPath( providerUriBuilder, pvfsFileName.getPath(), details );

    // Examples: "hcp://domain.my:443/root/path/folder/sub-folder" | "s3://folder/sub-folder"

    // Preserve file type information.
    if ( pvfsFileName.getType().hasChildren() ) {
      providerUriBuilder.append( SEPARATOR );
    }

    return parseUri( providerUriBuilder.toString() );
  }

  // IMPROVEMENT: The connection root provider URI is being repeatedly determined, but is actually constant as long as
  // the connection details does not change its configuration. Ideally, some kind of caching would be used.
  // Only the "rest path" differs each time. The problem being there is no way to know if connection details have
  // changed. These don't even implement hashCode, atm. Could use the connection manager to store and manage this
  // cache, by name, invalidating it whenever a connection is loaded/saved. Would still need to deal with in-memory
  // connections used by test(..) somehow.
  // See also, related note in #getConnectionRootProviderUriPrefix(.).

  /**
   * Appends to the provider URI builder the root of the connection, which includes the scheme, the domain and
   * the root path.
   * <p>
   * The components appended to the builder may not be normalized. It is expected that an overall normalization step
   * follows on the entirety of the provider URI string.
   *
   * @param providerUriBuilder The provider URI builder.
   * @param details            The details of the connection.
   */
  protected void appendProviderUriConnectionRoot( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
    throws KettleException {

    appendProviderUriSchemePrefix( providerUriBuilder, details );

    // Examples: "hcp://" | "s3://"  | "local:///"

    appendProviderUriDomain( providerUriBuilder, details );

    // Examples: "hcp://domain.my:443"

    appendProviderUriRootPath( providerUriBuilder, details );

    // Examples: "hcp://domain.my:443/root/path" | "local:///C:/root/path"
  }

  protected void appendProviderUriSchemePrefix( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
    throws KettleException {

    providerUriBuilder
      .append( details.getType() )
      .append( SCHEME_SUFFIX );
  }

  protected void appendProviderUriDomain( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
    throws KettleException {
    // Assumed to not require any percent encoding. Limited to URI authority format / characters.
    connectionFileNameUtils.appendPath( providerUriBuilder, details.getDomain() );
  }

  protected void appendProviderUriRootPath( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
    throws KettleException {

    if ( details.isRootPathSupported() ) {
      // Root path is assumed to already be percent-encoded according to the rules of the specific
      // file/connection provider.
      connectionFileNameUtils
        .appendPath( providerUriBuilder, vfsConnectionManagerHelper.getResolvedRootPath( details ) );
    }
  }

  /**
   * Appends the rest of the path to the provider URI string builder of a given connection.
   *
   * @param providerUriBuilder The provider URI string builder.
   * @param restPath           The part of the original PVFS path following the PVFS connection root prefix.
   * @param details            The associated connection details.
   */
  protected void appendProviderUriRestPath( @NonNull StringBuilder providerUriBuilder,
                                            @NonNull String restPath,
                                            @NonNull T details )
    throws KettleException {

    connectionFileNameUtils.appendPath( providerUriBuilder, restPath );
  }
  // endregion

  // region toPvfsFileName
  @NonNull
  @Override
  public ConnectionFileName toPvfsFileName( @NonNull FileName providerFileName, @NonNull T details )
    throws KettleException {
    // Determine the part of provider file name following the connection "root".
    // Use the transformer to generate the connection root provider uri.
    // Both uris are assumed to be normalized.
    // Examples:
    // - connectionRootProviderUri: "hcp://domain.my:443/root/path/"           |  "s3://" |  "local://"
    // - providerUri:               "hcp://domain.my:443/root/path/rest/path"  |  "s3://rest/path"
    // Example: "pvfs://my-connection"

    String connectionRootProviderUri = getConnectionRootProviderUriPrefix( details );
    String providerUri = providerFileName.getURI();

    if ( !connectionFileNameUtils.isDescendantOrSelf( providerUri, connectionRootProviderUri ) ) {
      throw new IllegalArgumentException(
        String.format(
          "Provider file name '%s' is not a descendant of the connection root '%s'.",
          providerUri,
          connectionRootProviderUri ) );
    }

    String restUriPath = providerUri.substring( connectionRootProviderUri.length() );

    // Examples: "/rest/path" or "rest/path"

    return buildPvfsFileName( details, restUriPath, providerFileName.getType() );
  }

  /**
   * Builds a PVFS file name for a connection given the URI path and the file type.
   * <h3>Implementation Notes</h3>
   * <p>
   * {@code nonNormalizedRestPath} may be using the URI encoding, resulting from {@link FileName#getURI()} (which is
   * ensured by <pre>AbstractFileName#handleURISpecialCharacters(..)</pre>), which additionally encodes {@code " "} and
   * {@code "#"} on the URI path section, compared to only {@code "%"} in the path section of a
   * {@link ConnectionFileName}.
   *
   * <p>
   * If the PVFS file name were built using <pre>connectionRootFileName#createName(nonNormalizedRestPath ..)</pre>, it
   * could get a non-normalized path, which should not happen and is usually assured by the file name parser.
   *
   * <p>
   * The encoding of {@code nonNormalizedRestPath} could be normalized beforehand, by explicitly calling
   * {@link UriParser#canonicalizePath(StringBuilder, int, int, FileNameParser)}. However, that would be duplicating
   * logic that only the file name parser should have.
   *
   * <p>
   * Opting instead to take the slower route of giving the URI to the parser for full parsing, normalization, and later
   * re-considering if a performance bottleneck is found.
   *
   * @param details               The details of the connection.
   * @param nonNormalizedRestPath The possibly non-normalized file name path.
   * @param fileType              The file type.
   * @return The PVFS file name.
   */
  @NonNull
  protected ConnectionFileName buildPvfsFileName( @NonNull T details,
                                                  @NonNull String nonNormalizedRestPath,
                                                  @NonNull FileType fileType ) throws KettleException {
    // Always ends with a /. Why:
    //  The way ConnectionFileName is implemented, the connection name is included as part of the root URI.
    //  There is one FileSystem instance per root URI, and thus per connection.
    //  Finally, root FS URIs always have a trailing "/" (because getPath() always starts with a /).
    String pvfsConnectionRootUri = vfsConnectionManagerHelper.getConnectionRootFileName( details ).getURI();

    String folderSeparator = ( fileType.hasChildren() ? SEPARATOR : "" );

    // Any double // in the path section are normalized by the parser.
    return parseUri( pvfsConnectionRootUri + nonNormalizedRestPath + folderSeparator );
  }

  // IMPROVEMENT: Just like with appendProviderUriConnectionRoot, the results of this function could be cached
  // to avoid constant parsing/normalization of the result of appendProviderUriConnectionRoot.

  /**
   * Gets the normalized URI prefix of a connection's root folder.
   *
   * <p>
   * Builds the URI prefix by using {@link #appendProviderUriConnectionRoot(StringBuilder, VFSConnectionDetails)} and
   * then normalizes the result using {@link #normalizeConnectionRootProviderUriPrefix(String, VFSConnectionDetails)}.
   *
   * <p>
   * Normalization is important for being able to perform the relative URI operations using basic string comparison, as is
   * done in {@link #toPvfsFileName(FileName, VFSConnectionDetails)}. This is crucial for at least the Local provider,
   * in a Windows OS, where the normalized format uses a variable number of separator characters after the scheme,
   * depending on whether the path refers to a drive letter or UNC path. In general, for all providers, it is crucial to
   * ensure that the URI prefix is normalized, given it may include non-normalized components such as the domain and the
   * root folder path.
   *
   * <p>
   * Ideally, a provider file name representing the connection root would be built, avoiding explicit string
   * manipulation, however, that's not possible in general for connections using buckets and which have no domain.
   * If it were possible, then {@code toPvfsFileName} would not need to use basic string comparison operations and could
   * instead use the file name methods: {@link FileName#isDescendent(FileName, NameScope)} and
   * {@link FileName#getRelativeName(FileName)}.
   *
   * @param details The details of the connection.
   * @return The normalized URI prefix.
   */
  private String getConnectionRootProviderUriPrefix( @NonNull T details ) throws KettleException {
    StringBuilder providerUriBuilder = new StringBuilder();

    appendProviderUriConnectionRoot( providerUriBuilder, details );

    String providerUriPrefix = providerUriBuilder.toString();

    return normalizeConnectionRootProviderUriPrefix( providerUriPrefix, details );
  }

  /**
   * Normalizes the connection root provider URI prefix.
   *
   * <p>
   * This method is used by the default implementation of {@link #toPvfsFileName(FileName, VFSConnectionDetails)}
   *
   * <p>
   * This implementation normalizes the given URI prefixes by calling {@link #normalizeUri(String)}.
   * However, the normalization is skipped for connections using buckets, according to
   * {@link VFSConnectionManagerHelper#usesBuckets(VFSConnectionDetails)}, and with no
   * {@link VFSConnectionDetails#getDomain() domain}, given in general these URI prefixes cannot be normalized.
   * If, for a specific connection type, special normalization is needed in this situation, define a custom transformer
   * class and override this method.
   *
   * @param providerUriPrefix The connection root provider URI prefix.
   * @param details           The details of the connection.
   * @return A normalized connection root provider URI prefix.
   */
  @NonNull
  protected String normalizeConnectionRootProviderUriPrefix( @NonNull String providerUriPrefix, @NonNull T details )
    throws KettleException {

    if ( StringUtils.isEmpty( details.getDomain() ) && vfsConnectionManagerHelper.usesBuckets( details ) ) {
      // Parsing would likely fail, due to degenerate URIs which cannot be represented as FileName (e.g. s3://).
      // Return the original uri prefix. Override in specific bucketed providers if any normalization is really needed.
      return providerUriPrefix;
    }

    return normalizeUri( providerUriPrefix );
  }

  // endregion

  // region Helpers

  /**
   * Normalizes a URI by parsing it into a file name and then obtaining its URI string.
   *
   * @param nonNormalizedUri The URI to normalize.
   * @return The normalized URI.
   * @throws KettleException If the given URI has an invalid syntax, including when it is a degenerate connection root
   *                         provider URI prefix.
   * @see #parseUri(String)
   * @see FileName#getURI()
   */
  @NonNull
  protected String normalizeUri( @NonNull String nonNormalizedUri ) throws KettleException {
    return parseUri( nonNormalizedUri ).getURI();
  }

  /**
   * Parses a given PVFS or Provider URI string, which may not be in canonical form
   * (w.r.t., for example, to separators, percent-encoding).
   * <p>
   * The resulting file name can then be used to get a normalized path, via {@link FileName#getPath()},
   * or the URI, via {@link FileName#getURI()}.
   *
   * @param nonNormalizedUri A URI string.
   * @return The file name.
   */
  @SuppressWarnings( "unchecked" )
  @NonNull
  protected <F extends FileName> F parseUri( @NonNull String nonNormalizedUri ) throws KettleException {
    return (F) getKettleVFS( connectionManager.getBowl() ).resolveURI( nonNormalizedUri );
  }

  @VisibleForTesting
  @NonNull
  IKettleVFS getKettleVFS( @NonNull Bowl bowl ) {
    return KettleVFS.getInstance( bowl );
  }
  // endregion
}
