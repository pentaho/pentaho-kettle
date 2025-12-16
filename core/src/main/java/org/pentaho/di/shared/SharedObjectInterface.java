/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.shared;

import java.util.Date;

import org.pentaho.di.core.exception.KettleException;
import org.w3c.dom.Node;

public interface SharedObjectInterface<T extends SharedObjectInterface<T>> {

  static final String OBJECT_ID = "object_id";

  /**
   * @deprecated
   *
   */
  @Deprecated(since = "11.0")
  public void setShared( boolean shared );

  /**
   * @deprecated
   *
   */
  @Deprecated(since = "11.0")

  public boolean isShared();

  public String getName();

  public String getXML() throws KettleException;

  public Date getChangedDate();

  /**
   * Wrapper method for clone() so clone() can be called from interface
   * @return Object
   */
  public T makeClone();

  public Node toNode() throws KettleException;

}
