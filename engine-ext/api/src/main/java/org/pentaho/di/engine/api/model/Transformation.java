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

import org.pentaho.di.engine.api.HasConfig;

import java.io.Serializable;
import java.util.List;

public interface Transformation extends LogicalModelElement, HasConfig, Serializable {
  List<Operation> getOperations();

  List<Hop> getHops();

}
