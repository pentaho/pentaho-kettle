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


package org.pentaho.di.core.extension;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannelInterface;

public class ExtensionPointHandler {

  /**
   * This method looks up the extension point plugins with the given ID in the plugin registry. If one or more are
   * found, their corresponding interfaces are instantiated and the callExtensionPoint() method is invoked.
   *
   * @param log
   *          the logging channel to write debugging information to
   * @param id
   *          The ID of the extension point to call
   * @param object
   *          The parent object that is passed to the plugin
   * @throws KettleException
   *           In case something goes wrong in the plugin and we need to stop what we're doing.
   */
  public static void callExtensionPoint( final LogChannelInterface log, final String id, final Object object )
    throws KettleException {
    ExtensionPointMap.getInstance().callExtensionPoint( log, id, object );
  }
}
