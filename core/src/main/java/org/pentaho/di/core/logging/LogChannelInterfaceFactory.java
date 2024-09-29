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


package org.pentaho.di.core.logging;

public interface LogChannelInterfaceFactory {
  public LogChannelInterface create( Object subject );

  public LogChannelInterface create( Object subject, boolean gatheringMetrics );

  public LogChannelInterface create( Object subject, LoggingObjectInterface parentObject );

  public LogChannelInterface create( Object subject, LoggingObjectInterface parentObject, boolean gatheringMetrics );
}
