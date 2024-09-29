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
import org.pentaho.di.core.exception.KettleException;

/**
 * The {@code VFSConnectionFileNameTransformer} interface contains functionality for transforming file names between the
 * <i>PVFS</i> and <i>connection/provider</i> namespaces.
 */
public interface VFSConnectionFileNameTransformer<T extends VFSConnectionDetails> {
  /**
   * Transforms a <i>PVFS</i> file name to a provider file name.
   *
   * @param pvfsFileName The <i>PVFS</i> file name.
   * @param details      The connection details of the connection referenced by {@code pvfsFileName}.
   * @return The provider file name.
   */
  @NonNull
  FileName toProviderFileName( @NonNull ConnectionFileName pvfsFileName, @NonNull T details ) throws KettleException;

  /**
   * Transforms a provider file name to a <i>PVFS</i> file name.
   *
   * @param providerFileName The provider file name.
   * @param details          The connection details associated with {@code providerFileName}.
   * @return The <i>PVFS</i> file name.
   */
  @NonNull
  ConnectionFileName toPvfsFileName( @NonNull FileName providerFileName, @NonNull T details ) throws KettleException;
}
