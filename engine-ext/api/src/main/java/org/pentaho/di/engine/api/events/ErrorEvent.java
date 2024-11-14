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
 * Created by fcamara on 8/23/2017.
 */
public class ErrorEvent<S extends LogicalModelElement> extends BaseEvent<S, LogEntry> {

  private static final long serialVersionUID = 6308895090845470781L;

  public ErrorEvent( S source, LogEntry log ) {
    super( source, log );
  }

}
