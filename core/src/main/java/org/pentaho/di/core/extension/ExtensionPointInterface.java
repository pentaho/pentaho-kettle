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

package org.pentaho.di.core.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

public interface ExtensionPointInterface {

  /**
   * This method is called by the Kettle code
   *
   * @param log
   *          the logging channel to log debugging information to
   * @param object
   *          The subject object that is passed to the plugin code
   * @throws KettleException
   *           In case the plugin decides that an error has occurred and the parent process should stop.
   */
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException;
}
