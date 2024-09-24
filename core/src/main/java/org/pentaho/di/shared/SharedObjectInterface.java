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

package org.pentaho.di.shared;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;

public interface SharedObjectInterface {
  public void setShared( boolean shared );

  public boolean isShared();

  public String getName();

  public String getXML() throws KettleException;

  public Date getChangedDate();
}
