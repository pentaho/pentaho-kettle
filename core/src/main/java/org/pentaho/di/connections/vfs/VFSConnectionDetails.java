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
   * Returns true if vfs connection supports root path
   * Defaults to {@code false}.
   *
   */
  default boolean isSupportsRootPath() {
    return false;
  }

  /**
   * Returns true if vfs connection requires root path
   * Defaults to {@code false}.
   *
   */
  default boolean isRootPathRequired() {
    return false;
  }

  /**
   *
   * Gets the root path of a vfs connection.
   *
   * Defaults to {@code null}
   * return the root path.
   */
  default String getRootPath() {
    return null;
  }

  /**
   * Sets the root path, given as a string
   *
   * @param rootPath The root path
   */
  default void setRootPath( String rootPath ) { }

  /**
   * Gets the list of roles with access to the connection from the Business Analytics product.
   * <p>
   * Access control does not distinguish between types of access operations (such as read, write, delete).
   * Access is granted to either all or none of the operations.
   * @return A non-null list of roles.
   */
  default List<String> getBaRoles() {
    // remove after implementation
    return Collections.emptyList();
  }
}
