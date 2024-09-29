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
import java.util.List;

/**
 * Represents a single row of data consisting of 0..N columns,
 * along with column names.
 */
public interface Row extends Serializable {
  List<String> getColumnNames();

  Object[] getObjects();
}
