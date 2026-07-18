/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.di.core.logging;

public class LogChannelFactory implements LogChannelInterfaceFactory {
  public LogChannel create( Object subject ) {
    return new LogChannel( subject );
  }

  public LogChannel create( Object subject, boolean gatheringMetrics ) {
    return new LogChannel( subject, gatheringMetrics );
  }

  public LogChannel create( Object subject, LoggingObjectInterface parentObject ) {
    return new LogChannel( subject, parentObject );
  }

  public LogChannel create( Object subject, LoggingObjectInterface parentObject, boolean gatheringMetrics ) {
    return new LogChannel( subject, parentObject, gatheringMetrics );
  }
}
