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


package org.pentaho.di.ui.spoon.delegates;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.ui.spoon.Spoon;

public abstract class SpoonDelegate {
  public static final LoggingObjectInterface loggingObject = new SimpleLoggingObject(
    "Spoon (delegate)", LoggingObjectType.SPOON, null );

  protected Spoon spoon;
  protected LogChannelInterface log;

  protected SpoonDelegate( Spoon spoon ) {
    this.spoon = spoon;
    this.log = spoon.getLog();
  }

  protected static int getMaxTabLength() {
    return Const.toInt( System.getProperty( Const.KETTLE_MAX_TAB_LENGTH ), 17 );
  }
}
