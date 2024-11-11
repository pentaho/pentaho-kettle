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

package org.pentaho.di.repository.pur;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.VersionSummary;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;

/**
 * Assembles {@link SharedObjectInterface}s of type {@link T} from related repository objects.
 * 
 * @author jganoff
 * 
 * @param <T>
 *          Type of {@link SharedObjectInterface} this adapter assembles
 */
public interface SharedObjectAssembler<T extends SharedObjectInterface> {
  /**
   * Assemble a shared object of type {@link T}.
   * 
   * @param file
   *          File reference for a shared object.
   * @param data
   *          Data representing the shared object identified by {@link file}.
   * @param version
   *          Version this shared object was fetched at.
   * 
   * @return Shared object assembled from the provided parameters.
   */
  public T assemble( RepositoryFile file, NodeRepositoryFileData data, VersionSummary version ) throws KettleException;
}
