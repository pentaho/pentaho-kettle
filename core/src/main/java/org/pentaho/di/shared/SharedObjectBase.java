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



package org.pentaho.di.shared;

import java.util.Date;

public class SharedObjectBase {
  private boolean shared;

  private Date changedDate;

  public SharedObjectBase() {
    changedDate = new Date();
  }

  public boolean isShared() {
    return shared;
  }

  public void setShared( boolean shared ) {
    this.shared = shared;
  }

  public Date getChangedDate() {
    return changedDate;
  }

  public void setChangedDate( Date changedDate ) {
    this.changedDate = changedDate;
  }
}
