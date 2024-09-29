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


package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.reporting.Metrics;

/**
 * Created by nbaker on 1/20/17.
 */
public class MetricsEvent<S extends LogicalModelElement> extends BaseEvent<S, Metrics> {
  private static final long serialVersionUID = -4057083369872309043L;

  public MetricsEvent( S source, Metrics status ) {
    super( source, status );
  }
}
