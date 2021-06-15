/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.client;

import java.io.Serializable;

import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.client.service.ClientService;


/**
 * Represents a remote client. The client that is connected to the current session can be obtained
 * by calling <code>RWT.getClient()</code>.
 * <p>
 * An RWT client can provide services, e.g. to allow access to device-specific capabilities. Those
 * client services implement the common interface {@link ClientService}.
 * </p>
 *
 * @see WebClient
 * @see RWT#getClient()
 * @since 2.0
 */
public interface Client extends Serializable {

  /**
   * Returns this client's implementation of a given service, if available.
   *
   * @param type the type of the requested service, must be a subtype of ClientService
   * @return the requested service if provided by this client, otherwise <code>null</code>
   * @see ClientService
   */
  <T extends ClientService> T getService( Class<T> type );

}
