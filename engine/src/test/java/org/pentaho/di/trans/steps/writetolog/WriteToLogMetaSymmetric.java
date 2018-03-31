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
