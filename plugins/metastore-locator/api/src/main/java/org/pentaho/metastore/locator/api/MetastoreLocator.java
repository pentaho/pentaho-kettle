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

package org.pentaho.metastore.locator.api;

import org.pentaho.metastore.api.IMetaStore;

/**
 * Created by tkafalas on 6/19/2017
 */
public interface MetastoreLocator {
  String LOCAL_PROVIDER_KEY = "LocalMetastoreProvider";
  String REPOSITORY_PROVIDER_KEY = "RepositoryMetastoreProvider";
  String VFS_PROVIDER_KEY = "VfsMetastoreProvider";

  /**
   * Attempts to pick the best the MetaStore based on environment; either
   * the local or repository metastore.
   * @return The metastore to use
   */
  IMetaStore getMetastore();

  /**
   * Works similar to (@link #getMetastore()) except that it will fall back to a metastore with a key of
   * providerKey if the both repository and local metastore both are not found.
   * @param providerKey
   * @return
   */
  IMetaStore getMetastore( String providerKey );

  /**
   * Registers a metastore provider that returns the received metastore with the current thread. {@link #getMetastore()}
   * will use this metastore if the repository and local metastore provider cannot be found.
   * @param metastore
   * @return
   */
  String setEmbeddedMetastore( IMetaStore metastore );

  /**
   * Dispose a metastore provider associated with the providerKey
   * @param providerKey The key to the metastore provider.
   */
  void disposeMetastoreProvider( String providerKey );

  /**
   * Unconditionally returns the metastore stored with the given provider key, or null if it does not exist.
   * @param providerKey
   * @return
   */
  IMetaStore getExplicitMetastore( String providerKey );
}
