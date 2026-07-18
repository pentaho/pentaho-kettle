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



package org.pentaho.di.cluster;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.shared.SharedObjectsManagementInterface;

import java.util.List;

public interface ClusterSchemaManagementInterface extends SharedObjectsManagementInterface<ClusterSchema> {

  @FunctionalInterface
  public interface SlaveServersSupplier {
    List<SlaveServer> get() throws KettleException;
  }

}
