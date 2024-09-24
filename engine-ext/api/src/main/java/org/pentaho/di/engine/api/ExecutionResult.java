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

package org.pentaho.di.engine.api;

import org.pentaho.di.engine.api.model.Operation;
import org.pentaho.di.engine.api.reporting.Metrics;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by nbaker on 6/22/16.
 */
public interface ExecutionResult extends Serializable {
  Map<Operation, Metrics> getDataEventReport();
}
