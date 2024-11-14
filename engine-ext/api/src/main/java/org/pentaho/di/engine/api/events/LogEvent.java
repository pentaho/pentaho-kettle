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


package org.pentaho.di.engine.api.events;

import org.pentaho.di.engine.api.model.LogicalModelElement;
import org.pentaho.di.engine.api.reporting.LogEntry;

/**
 * LogEvents encapsulate a LogEntry and contain a reference to the origin Element from the Logical Model.
 * <p>
 * Created by nbaker on 3/23/17.
 */
public class LogEvent<S extends LogicalModelElement> extends BaseEvent<S, LogEntry> {


  private static final long serialVersionUID = 782786153668299631L;

  public LogEvent( S source, LogEntry log ) {
    super( source, log );
  }

}
