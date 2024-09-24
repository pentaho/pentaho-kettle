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

/**
 * Operation roughly corresponds to a Step in PDI.
 * An IOperation represents the logical structure of
 * a step within the trans graph.  Materialization of
 * an IOperation converts it to an ICallableOperation,
 * which is associated with the behavior specific to
 * an Engine.
 */
public interface Operation extends LogicalModelElement, HasConfig, Serializable {

  List<Operation> getFrom();
  List<Operation> getTo();

  String getKey();

  List<Hop> getHopsIn();
  List<Hop> getHopsOut();
}
