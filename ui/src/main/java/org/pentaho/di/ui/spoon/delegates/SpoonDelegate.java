/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
