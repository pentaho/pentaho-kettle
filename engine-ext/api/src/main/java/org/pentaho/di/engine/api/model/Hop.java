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

package org.pentaho.di.engine.api.model;

import java.io.Serializable;

/**
 * @author nhudak
 */
public interface Hop extends LogicalModelElement, Serializable {
  @Override default String getId() {
    return getFrom().getId() + " -> " + getTo().getId();
  }

  String TYPE_NORMAL = "NORMAL";

  String TYPE_ERROR = "ERROR";

  Operation getFrom();

  Operation getTo();

  default String getType() {
    return TYPE_NORMAL;
  }

}
