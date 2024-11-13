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


package org.pentaho.di.core.logging;

/**
 * Retains the default log level during execution
 *
 * @author matt
 */
public class DefaultLogLevel {
  private static DefaultLogLevel defaultLogLevel;

  private LogLevel logLevel;

  private DefaultLogLevel() {
    logLevel = LogLevel.BASIC;
  }

  private static DefaultLogLevel getInstance() {
    if ( defaultLogLevel == null ) {
      defaultLogLevel = new DefaultLogLevel();
    }
    return defaultLogLevel;
  }

  /**
   * @return The default log level for this application
   */
  public static LogLevel getLogLevel() {
    DefaultLogLevel instance = getInstance();
    return instance.logLevel;
  }

  /**
   * @param logLevel
   *          Set the default log level for this application
   */
  public static void setLogLevel( LogLevel logLevel ) {
    DefaultLogLevel instance = getInstance();
    LogChannel.updateGlobalLogChannels( logLevel );
    instance.logLevel = logLevel;
  }

}
