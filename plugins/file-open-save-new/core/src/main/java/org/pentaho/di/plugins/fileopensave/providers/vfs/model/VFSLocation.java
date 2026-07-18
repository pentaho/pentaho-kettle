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



package org.pentaho.di.plugins.fileopensave.providers.vfs.model;

import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;

/**
 * Created by bmorrise on 2/14/19.
 */
public class VFSLocation extends VFSDirectory {
  @Override
  public boolean isCanEdit() {
    return false;
  }

  @Override
  public boolean isCanDelete() {
    return false;
  }

  @Override
  public EntityType getEntityType(){
    return EntityType.VFS_LOCATION;
  }
}
