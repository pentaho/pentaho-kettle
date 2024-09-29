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

package org.pentaho.di.core.bowl;

import org.pentaho.di.connections.ConnectionManager;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.api.IMetaStore;


/**
 * A Bowl is a generic container/context/workspace concept. Different plugin implementations may implement this for
 * additional features.
 *
 * All implementations of Bowl should implement equals() and hashcode()
 *
 */
public interface Bowl {

  /**
   * Gets a Read-Only Metastore that handles any defaulting required for execution-time handling of metastores, for the
   * Bowl.
   *
   * @return IMetaStore A metastore for execution with the Bowl. Never null.
   */
  IMetaStore getMetastore() throws MetaStoreException;

  /**
   * Gets a Metastore only for accessing any bowl-specific objects.
   *
   *
   * @return IMetaStore A metastore for the specified Bowl. Never null.
   */
  IMetaStore getExplicitMetastore() throws MetaStoreException;

  /**
   * Gets a ConnectionManager for this Bowl. Uses a metastore from getMetastore(), so global connections will be
   * returned as well. This ConnectionManager is effectively read-only.
   *
   * Since constructing and initializing ConnectionManagers can be expensive, and ConnectionManager instances don't
   * share state, consumers should always use this method instead of ConnectionManager.getInstance()
   *
   * @return ConnectionManager, never null.
   */
  ConnectionManager getConnectionManager() throws MetaStoreException;


}
