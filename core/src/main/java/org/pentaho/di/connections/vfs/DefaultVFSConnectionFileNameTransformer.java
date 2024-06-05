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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.vfs2.FileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileName;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameUtils;
import org.pentaho.di.core.exception.KettleException;

import java.util.Objects;

import static org.apache.commons.vfs2.FileName.SEPARATOR;

public class DefaultVFSConnectionFileNameTransformer<T extends VFSConnectionDetails>
  implements VFSConnectionFileNameTransformer<T> {

  @NonNull
  private static final String SCHEME_SUFFIX = ":" + SEPARATOR + SEPARATOR;

  @NonNull
  private final VFSConnectionManagerHelper vfsConnectionManagerHelper;

  @NonNull
  private final ConnectionFileNameUtils connectionFileNameUtils;

  public DefaultVFSConnectionFileNameTransformer() {
    this( VFSConnectionManagerHelper.getInstance(), ConnectionFileNameUtils.getInstance() );
  }

  public DefaultVFSConnectionFileNameTransformer( @NonNull VFSConnectionManagerHelper vfsConnectionManagerHelper,
                                                  @NonNull ConnectionFileNameUtils connectionFileNameUtils ) {
    this.vfsConnectionManagerHelper = Objects.requireNonNull( vfsConnectionManagerHelper );
    this.connectionFileNameUtils = Objects.requireNonNull( connectionFileNameUtils );
  }

  @NonNull
  protected VFSConnectionManagerHelper getVfsConnectionManagerHelper() {
    return vfsConnectionManagerHelper;
  }

  @NonNull
  protected ConnectionFileNameUtils getConnectionFileNameUtils() {
    return connectionFileNameUtils;
  }

  // region toProviderUri
  @NonNull
  @Override
  public String toProviderUri( @NonNull ConnectionFileName pvfsFileName, @NonNull T details )
    throws KettleException {

    StringBuilder providerUriBuilder = new StringBuilder();

    appendProviderUriConnectionRoot( providerUriBuilder, details );

    // Examples:
    //  providerUriBuilder: "hcp://domain.my:443/root/path" | "local:///C:/root/path" | "s3://"
    //  getPath():          "/folder/sub-folder" | "/"

    appendProviderUriRestPath( providerUriBuilder, pvfsFileName.getPath(), details );

    // Examples: "hcp://domain.my:443/root/path/folder/sub-folder" | "s3://folder/sub-folder"

    return providerUriBuilder.toString();
  }

  // IMPROVEMENT: The connection root provider URI is being repeatedly determined, but is actually constant as long as
  // the connection details does not change its configuration. Ideally, some kind of caching would be used.
  // Only the "rest path" differs each time. The problem being there is no way to know if connection details have
  // changed. These don't even implement hashCode, atm. Could use the connection manager to store and manage this
  // cache, by name, invalidating it whenever a connection is loaded/saved. Would still need to deal with in-memory
  // connections used by test(..) somehow.
  protected void appendProviderUriConnectionRoot( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
    throws KettleException {

    appendProviderRootPrefix( providerUriBuilder, details );

    // Examples: "hcp://" | "s3://"  | "local:///"

    appendProviderUriDomain( providerUriBuilder, details );

    // Examples: "hcp://domain.my:443"

    appendProviderUriRootPath( providerUriBuilder, details );

    // Examples: "hcp://domain.my:443/root/path" | "local:///C:/root/path"
  }

  protected void appendProviderRootPrefix( @NonNull StringBuilder providerUriBuilder, @NonNull T details )
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
    // Both uris are assumed to be in canonical form.
    // Need to add/ensure trailing slashes in both to avoid matching segments which share a prefix.
    // Examples:
    // - rootPathProviderUri: "hcp://domain.my:443/root/path/"           |  "s3://" |  "local://"
    // - providerUri:         "hcp://domain.my:443/root/path/rest/path"  |  "s3://rest/path"
    String rootPathProviderUri = connectionFileNameUtils.ensureTrailingSeparator(
      vfsConnectionManagerHelper.getConnectionRootProviderUri( this, details ) );

    String providerUri = connectionFileNameUtils.ensureTrailingSeparator( providerFileName.getURI() );
    if ( !providerUri.startsWith( rootPathProviderUri ) ) {
      throw new IllegalStateException( "Provider URI does not start with the provider root URI." );
    }

    String restPath = providerUri.substring( rootPathProviderUri.length() );

    // Examples: restPath: "/rest/path/" | "rest/path/"

    // According to FileName#getPath(), the path must always start with a / (even if it would be empty).
    // Also, the trailing slash is removed by the AbstractFileName constructor.
    restPath = connectionFileNameUtils.ensureLeadingSeparator( restPath );

    // Examples:
    // restPath: "/rest/path/"
    // connection root file name uri: "pvfs://my-connection/"
    // connection file name uri:      "pvfs://my-connection/rest/path"
    // NOTE:
    //  The way ConnectionFileName is implemented, connection is included as part of the root URI.
    //  There is one ConnectionFileSystem instance per root URI, i.e., per connection.
    //  Also, a root fs uri always has a / at the end, given that getPath() always starts with a /.
    return vfsConnectionManagerHelper
      .getConnectionRootFileName( details )
      .createName( restPath, providerFileName.getType() );
  }
  // endregion
}
