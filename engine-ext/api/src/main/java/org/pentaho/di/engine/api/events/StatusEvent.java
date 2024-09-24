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
import org.pentaho.di.engine.api.reporting.Status;

/**
 * Created by nbaker on 1/17/17.
 */
public class StatusEvent<S extends LogicalModelElement> extends BaseEvent<S, Status> {
  private static final long serialVersionUID = 5019758572364906951L;

  public StatusEvent( S source, Status status ) {
    super( source, status );
  }
}
