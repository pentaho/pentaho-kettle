/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.database.util;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;

public class DatabaseLogExceptionFactory {

  public static final String KETTLE_GLOBAL_PROP_NAME = "KETTLE_LOG_EXCEPTION_FAILOVER";

  private static final LogExceptionBehaviourInterface throwable = new ThrowableBehaviour();
  private static final LogExceptionBehaviourInterface supressable = new SuppressBehaviour();

  /**
   * Depends on if environment variable 'KETTLE_LOG_EXCEPTION_FAILOVER' we can suppress or throw exception up.
   * 
   * @return
   */
  public static LogExceptionBehaviourInterface getExceptionStrategy( VariableSpace variables ) {
    // what is environment says?
    String val = variables.getVariable( KETTLE_GLOBAL_PROP_NAME );
    if (val==null || val.isEmpty()){
      return supressable;
    }
    return Boolean.valueOf( val ) ? throwable : supressable;
  }

  /**
   * Throw exception back to caller, this will be logged somewhere else.
   * 
   */
  private static class ThrowableBehaviour implements LogExceptionBehaviourInterface {

    @Override
    public void registerException(LogChannelInterface log, Class<?> packageClass, String key, String... parameters )
      throws KettleDatabaseException {
      throw new KettleDatabaseException( BaseMessages.getString( packageClass, key, parameters ) );
    }

    @Override
    public void registerException(LogChannelInterface log, Exception e, Class<?> packageClass, String key, String... parameters )
      throws KettleDatabaseException {
      throw new KettleDatabaseException( BaseMessages.getString( packageClass, key, parameters ), e );
    }
  }

  /**
   * Suppress exception, but still add a log record about it
   * 
   */
  private static class SuppressBehaviour implements LogExceptionBehaviourInterface {

    @Override
    public void registerException(LogChannelInterface log, Class<?> packageClass, String key, String... parameters ) {
      log.logError( BaseMessages.getString( packageClass, key ) );
    }

    @Override
    public void registerException(LogChannelInterface log, Exception e, Class<?> packageClass, String key, String... parameters )
      throws KettleDatabaseException {
      log.logError( BaseMessages.getString( packageClass, key, e.getMessage(), e ) );
    }
  }
}
