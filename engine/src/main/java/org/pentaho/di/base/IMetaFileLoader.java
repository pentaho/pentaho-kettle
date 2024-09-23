/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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
