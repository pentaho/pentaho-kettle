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
