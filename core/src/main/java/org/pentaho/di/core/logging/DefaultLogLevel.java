/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
    instance.logLevel = logLevel;
  }

}
