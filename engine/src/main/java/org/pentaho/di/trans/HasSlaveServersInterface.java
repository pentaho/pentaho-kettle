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



package org.pentaho.di.trans;

import java.util.List;

import org.pentaho.di.cluster.SlaveServer;

public interface HasSlaveServersInterface {
  public List<SlaveServer> getSlaveServers();
}
