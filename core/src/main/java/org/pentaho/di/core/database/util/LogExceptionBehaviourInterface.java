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

package org.pentaho.di.core.database.util;

import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannelInterface;

public interface LogExceptionBehaviourInterface {

  /**
   * When exception during logging is raised, depending on item settings we will throw exception up, or just put a log
   * record on this event.
   * 
   * Different behaviors are created in backward compatibility with existing code. See PDI-9790.
   * 
   * @param packageClass
   * @param key
   * @param parameters
   */
  public void registerException( LogChannelInterface log, Class<?> packageClass, String key, String... parameters ) throws KettleDatabaseException;

  public void registerException( LogChannelInterface log, Exception e, Class<?> packageClass, String key,
      String... parameters ) throws KettleDatabaseException;

}
