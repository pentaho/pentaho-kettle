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

package org.pentaho.di.connections.vfs;

import java.util.Date;

/**
 * Created by bmorrise on 2/27/19.
 */
public class VFSRoot {

  public VFSRoot( String name, Date modifiedDate ) {
    this.name = name;
    this.modifiedDate = modifiedDate;
  }

  private String name;
  private Date modifiedDate;

  public String getName() {
    return name;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public Date getModifiedDate() {
    return modifiedDate;
  }

  public void setModifiedDate( Date modifiedDate ) {
    this.modifiedDate = modifiedDate;
  }
}
