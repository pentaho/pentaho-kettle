/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.metastore.locator.api;

import org.pentaho.metastore.api.IMetaStore;

/**
 * Created by bryan on 3/28/16.
 */
public interface MetastoreProvider {
  IMetaStore getMetastore();

  String getProviderType();
}
