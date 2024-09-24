/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019-2024 by Hitachi Vantara : http://www.pentaho.com
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

import org.pentaho.di.connections.ConnectionDetails;
import org.pentaho.di.connections.vfs.provider.ConnectionFileNameParser;

import java.util.Collections;
import java.util.List;

/**
 * Created by bmorrise on 2/13/19.
 */
public interface VFSConnectionDetails extends ConnectionDetails {
  /**
   * Allow for returning of domain for non-bucket vfs connections
   */
  default String getDomain() {
    return "";
  }

  /**
   * Indicates if the connection has buckets as the first-level folders.
   * <p>
   * The buckets of a connection are obtained via {@link VFSConnectionProvider#getLocations(VFSConnectionDetails)}.
   * Bucket management is done using provider specific means. For example, it is not possible to create a bucket folder
   * using the {@code KettleVFS} API, as a child of the connection's root folder.
   * <p>
   * The default interface implementation returns {@code true}.
   *
   * @return {@code true} if the connection supports buckets; {@code false}, otherwise.
   */
  default boolean hasBuckets() {
    return true;
  }

  /**
   * Returns true if vfs connection supports root path
   * Defaults to {@code false}.
   */
  default boolean isRootPathSupported() {
    return false;
  }

  /**
   * Returns true if vfs connection requires root path
   * Defaults to {@code false}.
   */
  default boolean isRootPathRequired() {
    return false;
  }

  /**
   * Gets the root folder path of this VFS connection.
   * <p>
   * The root folder path allows limiting the files exposed through a PVFS URL.
   * <p>
   * The default interface implementation exists to ensure backward compatibility and returns {@code null}.
   * <h3>
   * Semantics of the Root Folder Path
   * </h3>
   * Assume a connection without a configured root folder path, <code>connection-name</code>.
   * The general structure of a PVFS URL that resolves to a file in this connection is
   * <code>pvfs://(connection-name)/(rest-path)</code>.
   * If the <code>rest-path</code> component is split in two parts, the root path and the remainder,
   * the following form is achieved: <code>pvfs://(connection-name)/(root-path)/(rest-rest-path)</code>.
   * <p>
   * Assume a connection configured with the root folder path <code>root-path</code>, all other configurations equal,
   * named <code>connection-with-root-path</code>.
   * The same file would be exposed by a <code>pvfs</code> URL in which the <code>root-path</code> component is omitted:
   * <code>pvfs://(connection-with-root-path)/(rest-rest-path)</code>.
   * <p>
   * Necessarily, the configured root path must identify a file of type folder.
   * <p>
   * Files which are not descendant of a connection's root folder path cannot be identified/accessed using a
   * <code>pvfs</code> URL. Folder segments of a <code>pvfs</code> URL cannot have the special names <code>.</code> or
   * <code>..</code>.
   * <h3>
   * Syntax of the Root Folder Path
   * </h3>
   * The syntax of the root folder path is that of one or more folder names separated by a folder separator,
   * <code>/</code>. For example, the following would be syntactically valid: <code>my-vfs-bucket/my-folder</code>.
   * While a leading or a trailing folder separator should be tolerated, a <i>normalized</i> root folder path
   * should have none.
   * <p>
   * The value stored in this property is subject to variable substitution and thus may not conform to the syntax
   * of a root folder path. The syntax is validated only after variable substitution is performed.
   * <h3>
   * Impact of Root Folder Path on Provider URLs
   * </h3>
   * While omitted from the <code>pvfs</code> URL, the root folder path is incorporated in the <i>provider-specific</i>
   * (a.k.a. internal) URL, as a result of the conversion process from <code>pvfs</code> to <code>provider</code> URL.
   * The root folder path is not a required component of provider URLs, and files which are not descendants of the root
   * folder path are still resolvable. The root folder path is not a security feature, by itself.
   * <p>
   * The general structure of a provider URL corresponding to the above <code>pvfs</code> URL is like:
   * <code>(scheme):// [(domain) /] [(root-path) /] [(rest-rest-path)]</code>
   * <p>
   * Where the <i>scheme</i> component is given by the {@link #getType()} property, and the <i>domain</i> component is
   * given by the {@link #getDomain()} property.
   * <p>
   * The provider URL structure for specific providers may vary from this general structure. However, the semantics of
   * the root folder path property should be respected.
   * <h3>
   * Examples of <code>pvfs</code> and Provider URLs
   * </h3>
   * Given an S3 connection, with a configured root folder path of <code>my-bucket/my-folder</code>,
   * the <code>pvfs</code> URL, <code>pvfs://my-s3-connection/my-sub-folder/my-file</code>, would convert to the
   * provider URL, <code>s3://my-bucket/my-folder/my-sub-folder/my-file</code>.
   * <p>
   * Given an HCP connection, with a configured root folder path of <code>my-folder</code>, and a configured domain of
   * <code>my-domain.com:3000</code>,the <code>pvfs</code> URL,
   * <code>pvfs://my-hcp-connection/my-sub-folder/my-file</code>, would convert to the provider URL,
   * <code>hcp://my-domain.com:3000/my-folder/my-sub-folder/my-file</code>.
   *
   * @return A non-empty root path, if any; {@code null}, otherwise.
   *
   * @see ConnectionFileNameParser
   * @see VFSConnectionManagerHelper#getResolvedRootPath(VFSConnectionDetails)
   */
  default String getRootPath() {
    return null;
  }

  /**
   * Sets the root folder path, given as a string.
   * <p>
   * An empty root folder path value should be converted to {@code null}.
   * Further syntax validation is performed only after variable substitution.
   * <p>
   * The default interface implementation exists to ensure backward compatibility and does nothing.
   *
   * @param rootPath The root path.
   */
  default void setRootPath( String rootPath ) { }

  /**
   * Gets the list of roles with access to the connection from the Business Analytics product.
   * <p>
   * Access control does not distinguish between types of access operations (such as read, write, delete).
   * Access is granted to either all or none of the operations.
   *
   * @return A non-null list of roles.
   */
  default List<String> getBaRoles() {
    // remove after implementation
    return Collections.emptyList();
  }
}
