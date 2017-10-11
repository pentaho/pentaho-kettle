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

package org.pentaho.di.trans.steps.infobrightoutput;

import java.util.ArrayList;

import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.i18n.BaseMessages;

/**
 * @author Infobright Inc.
 */
public abstract class AbstractMessages {
  protected ArrayList<String> packageNames = new ArrayList<String>();

  public void addPackage( Package packageObj ) {
    packageNames.add( packageObj.getName() );
  }

  public String get( String key, String... params ) {
    String res = null;
    String notFoundKey = "!" + key + "!";

    for ( String pName : packageNames ) {
      // Kettle will generate an exception message if there is a
      // failed message search. Since we are searching over multiple
      // packages, we don't want this message generated unless we
      // cannot find the message in any of the packages.
      LogLevel logLevel = DefaultLogLevel.getLogLevel();
      DefaultLogLevel.setLogLevel( LogLevel.NOTHING );
      try {
        res = BaseMessages.getString( pName, key );
      } finally {
        DefaultLogLevel.setLogLevel( logLevel );
      }
      if ( !res.equals( notFoundKey ) ) {
        return res;
      }
    }

    // This means we did not find the key, so let Kettle generate
    // its normal error.
    return BaseMessages.getString( packageNames.get( 0 ), key );
  }
}
