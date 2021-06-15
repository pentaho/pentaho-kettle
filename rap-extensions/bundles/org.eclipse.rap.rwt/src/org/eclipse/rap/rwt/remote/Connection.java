/*******************************************************************************
 * Copyright (c) 2014 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.rap.rwt.remote;

import org.eclipse.rap.rwt.service.UISession;


/**
 * An instance of this interface represents a connection used to communicate with the client. Every
 * UI session has exactly one connection which can be obtained by calling
 * {@link UISession#getConnection()}.
 *
 * @since 2.0
 * @see UISession#getConnection()
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface Connection {

  /**
   * Creates a new remote object on the client with the given remote type. The type must be known by
   * the client, and the client must be able to create an object of this type. The returned
   * <code>RemoteObject</code> can be used to communicate with the remote object.
   *
   * @param remoteType the type of the remote object to be created, must not be <code>null</code>
   * @return a representation of the remote object that has been created
   */
  RemoteObject createRemoteObject( String remoteType );

}
