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
package org.pentaho.di.trans.steps.writetolog;

import org.pentaho.di.core.logging.LogLevel;

public class WriteToLogMetaSymmetric extends WriteToLogMeta {

  /**
   * This class is here because of the asymmetry in WriteToLogMeta
   * with respect to the getter and setter for the "loglevel" variable.
   * The only getter for the variable actually returns a LogLevel object,
   * and the setter expects an int. The underlying storage is a String.
   * This asymmetry causes issues with test harnesses using reflection.
   *
   * MB - 5/2016
   */
  public WriteToLogMetaSymmetric() {
    super();
  }

  public String getLogLevelString() {
    LogLevel lvl = super.getLogLevelByDesc();
    if ( lvl == null ) {
      lvl = LogLevel.BASIC;
    }
    return WriteToLogMeta.logLevelCodes[ lvl.getLevel() ];
  }

  public void setLogLevelString( String value ) {
    LogLevel lvl = LogLevel.getLogLevelForCode( value );
    super.setLogLevel( lvl.getLevel() );
  }

}
