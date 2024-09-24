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
