/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.osgi.api;

import org.pentaho.metastore.api.IMetaStore;

/**
 * Created by tkafalas on 7/10/2017.
 */
public interface MetastoreLocatorOsgi {
  final String LOCAL_PROVIDER_KEY = "LocalMetastoreProvider";
  final String REPOSITORY_PROVIDER_KEY = "RepositoryMetastoreProvider";
  final String EMBEDDED_METASTORE_KEY_PREFIX = "Embedded";

  /**
   * Attempts to pick the best the MetaStore based on environment; either
   * the local or repository metastore.
   *
   * @return The metastore to use
   */
  IMetaStore getMetastore();

  /**
   * Works similar to (@link #getMetastore()) except that it will fall back to a metastore with a key of
   * providerKey if the both repository and local metastore both are not found.
   *
   * @param providerKey
   * @return
   */
  IMetaStore getMetastore( String providerKey );

  /**
   * Registers a metastore provider that returns the received metastore with the current thread.
   * {@link #getMetastore()}
   * will used this metastore if the repository and local metastore provider cannot be found.
   *
   * @param metastore
   * @return
   */
  String setEmbeddedMetastore( IMetaStore metastore );

  /**
   * Dispose a metastore provider associated with the providerKey
   *
   * @param providerKey The key to the metastore provider.
   */
  public void disposeMetastoreProvider( String providerKey );

  /**
   * Unconditionally returns the metastore stored with the given provider key, or null if it does not exist.
   *
   * @param providerKey
   * @return
   */
  public IMetaStore getExplicitMetastore( String providerKey );

}
