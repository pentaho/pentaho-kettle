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

package org.pentaho.di.base;

import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.metastore.api.IMetaStore;

public interface IMetaFileLoader<T> {
  /**
   * Get a JobMeta or TransMeta object for a job entry that needs it.  The entry will be cached and further
   * loads will come from the cache, instead.
   * @param bowl context for file operations
   * @param rep The repo in play if not using the filesystem
   * @param metaStore The metastore
   * @param space The variables for substitution
   * @return A JobMeta or TransMeta (T)
   * @throws KettleException
   */
  T getMetaForEntry( Bowl bowl, Repository rep, IMetaStore metaStore, VariableSpace space ) throws KettleException;

  /**
   * Get a JobMeta or TransMeta object for a transformation step for a Transformation that needs it.  The entry will be
   * cached and further loads will come from the cache, instead.
   * @param bowl context for file operations
   * @param rep The repo in play if not using the filesystem
   * @param metaStore The metastore
   * @param space The variables for substitution
   * @return A JobMeta or TransMeta (T)
   * @throws KettleException
   */
  T getMetaForStep( Bowl bowl, Repository rep, IMetaStore metaStore, VariableSpace space )
    throws KettleException;
}
